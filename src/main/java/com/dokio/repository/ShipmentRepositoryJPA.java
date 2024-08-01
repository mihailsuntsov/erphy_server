/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.repository;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsShipmentForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.CompanySettingsJSON;
import com.dokio.message.response.Settings.SettingsShipmentJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
import com.dokio.repository.Exceptions.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ShipmentRepositoryJPA {

    Logger logger = Logger.getLogger("ShipmentRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    private CagentRepositoryJPA cagentRepository;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;
    @Autowired
    private CustomersOrdersRepositoryJPA customersOrdersRepository;



    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","name","cagent","status_name","sum_price","hasSellReceipt","company","department","creator","date_time_created_sort","shipment_date_sort","description","is_completed","product_count")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<ShipmentJSON> getShipmentTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(21L, "260,261,262,263"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            boolean needToSetParameter_MyDepthsIds = false;
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           p.customers_orders_id as customers_orders_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.shift_id,0) as shift_id, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from shipment_product where shipment_id=p.id),0) as sum_price, " +
                    "           to_char(p.shipment_date at time zone '"+myTimeZone+"', '"+dateFormat+"') as shipment_date, " +
                    "           coalesce(sh.shift_number,0) as shift_number, " +
                    "           (select count(*) from receipts rec where coalesce(rec.shipment_id,0)=p.id and rec.operation_id='sell') as hasSellReceipt," + //подсчет кол-ва чеков на предмет того, был ли выбит чек продажи в данной Розничной продаже
                    "           cg.name as cagent, " +
                    "           p.shipment_date as shipment_date_sort, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           (select count(*) from shipment_product ip where coalesce(ip.shipment_id,0)=p.id) as product_count" + //подсчет кол-ва товаров

                    "           from shipment p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN shifts sh ON p.shift_id=sh.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(21L, "260")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(21L, "261")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(21L, "262")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }


            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }


            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                query.setFirstResult(offsetreal).setMaxResults(result);


                List<Object[]> queryList = query.getResultList();
                List<ShipmentJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ShipmentJSON doc=new ShipmentJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    doc.setDepartment((String)                    obj[9]);
                    doc.setDoc_number(Long.parseLong(             obj[10].toString()));
                    doc.setCustomers_orders_id(obj[11]!=null?Long.parseLong(obj[11].toString()):null);
                    doc.setCompany((String)                       obj[12]);
                    doc.setDate_time_created((String)             obj[13]);
                    doc.setDate_time_changed((String)             obj[14]);
                    doc.setDescription((String)                   obj[15]);
                    doc.setShift_id(obj[16]!=null?Long.parseLong(obj[16].toString()):null);
                    doc.setStatus_id(obj[19]!=null?Long.parseLong(obj[19].toString()):null);
                    doc.setStatus_name((String)                   obj[20]);
                    doc.setStatus_color((String)                  obj[21]);
                    doc.setStatus_description((String)            obj[22]);
                    doc.setSum_price((BigDecimal)                 obj[23]);
                    doc.setShipment_date((String)                 obj[24]);
                    doc.setShift_number((Integer)                 obj[25]);
                    doc.setHasSellReceipt(((BigInteger)           obj[26]).longValue() > 0L);//если есть чеки - вернется true
                    doc.setCagent((String)                        obj[27]);
                    doc.setIs_completed((Boolean)                 obj[29]);
                    doc.setProduct_count(Long.parseLong(          obj[30].toString()));


                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getShipmentTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getShipmentSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from shipment p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(21L, "260")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(21L, "261")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(21L, "262")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
//                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        try{

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getShipmentSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<ShipmentProductTableJSON> getShipmentProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(21L, "260,261,262,263"))//(см. файл Permissions Id)
        {
            String stringQuery;
            Long parentCustomersOrdersId = getParentCustomersOrdersId(docId);
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.shipment_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = p.edizm_id) as edizm," +
                    " ap.nds_id," +
                    " nds.name as nds," +
                    " ap.price_type_id," +
                    " (select pt.name from sprav_type_prices pt where pt.id = ap.price_type_id) as price_type, " +
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = ap.department_id),0) as total, "+ //всего на складе (т.е остаток)
                    " (select " +
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                    //по логике: сумма( резерв > (всего - отгружено) ? (всего - отгружено) : резерв)    (при условии не позволять в заказах покупателей делать резерв больше "всего" (reserved_current!>product_count))
                    "   from " +
                    "   customers_orders_product " +
                    "   where " +
                    "   product_id=ap.product_id "+
                    "   and department_id = ap.department_id "+
                    "   and customers_orders_id!="+parentCustomersOrdersId+") as reserved, "+//зарезервировано в других документах Заказ покупателя

//                    " ap.product_count as shipped, "+//в розничных продажах все количество товара считается отгруженным, т.к. розн. продажа создается в момент продажи (отгрузки) товара.
                    " ap.department_id as department_id, " +
                    " (select name from departments where id= ap.department_id) as department, "+
                    " ap.id  as row_id, " +
                    " ppr.name_api_atol as ppr_name_api_atol, " +
                    " ppr.is_material as is_material, " +
//                    " ap.product_count as reserved_current " +//в розничных продажах нет резервов, так что приравниваем резерв к количеству товара в продаже (т.е. весь товар априори зарезервирован)
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce(nds.value,0) as nds_value" +
                    " from " +
                    " shipment_product ap " +
                    " INNER JOIN shipment a ON ap.shipment_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " LEFT OUTER JOIN sprav_taxes nds ON nds.id = ap.nds_id" +
                    " where a.master_id = " + myMasterId +
                    " and ap.shipment_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(21L, "260")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(21L, "261")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(21L, "262")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<ShipmentProductTableJSON> returnList = new ArrayList<>();
                int row_num = 1; // номер строки при выводе печатной версии
                for(Object[] obj:queryList){
                    ShipmentProductTableJSON doc=new ShipmentProductTableJSON();
                    doc.setRow_num(row_num);
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setCustomers_orders_id(Long.parseLong(              obj[1].toString()));
                    doc.setProduct_count(                                   obj[2]==null?BigDecimal.ZERO:(BigDecimal)obj[2]);
                    doc.setProduct_price(                                   obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                    doc.setProduct_sumprice(                                obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                    doc.setName((String)                                    obj[5]);
                    doc.setEdizm((String)                                   obj[6]);
                    doc.setNds_id(Long.parseLong(                           obj[7].toString()));
                    doc.setNds((String)                                     obj[8]);
                    doc.setPrice_type_id(obj[9]!=null?Long.parseLong(       obj[9].toString()):null);
                    doc.setPrice_type((String)                              obj[10]);
                    doc.setTotal(                                           obj[11]==null?BigDecimal.ZERO:(BigDecimal)obj[11]);
                    doc.setReserved(                                        obj[12]==null?BigDecimal.ZERO:(BigDecimal)obj[12]);
                    doc.setDepartment_id(Long.parseLong(                    obj[13].toString()));
                    doc.setDepartment((String)                              obj[14]);
                    doc.setId(Long.parseLong(                               obj[15].toString()));
                    doc.setPpr_name_api_atol((String)                       obj[16]);
                    doc.setIs_material((Boolean)                            obj[17]);
                    doc.setIndivisible((Boolean)                            obj[18]);
                    doc.setNds_value((BigDecimal)                           obj[19]);
                    returnList.add(doc);
                    row_num++;
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getShipmentProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращает id родительского Заказа покупателя по id Розничной продажи. Если родительского документа нет - возвращает 0
    @SuppressWarnings("Duplicates")
    private Long getParentCustomersOrdersId(Long shipmentId){
        String stringQuery = "select coalesce(rs.customers_orders_id,0) from shipment rs where rs.id=" + shipmentId;
        try{
            return Long.valueOf(entityManager.createNativeQuery(stringQuery).getSingleResult().toString());
        }
        catch(NoResultException nre){return 0L;}
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getParentCustomersOrdersId. SQL query:" + stringQuery, e);
            return null;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public ShipmentJSON getShipmentValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(21L, "260,261,262,263"))//см. _Permissions Id.txt
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           coalesce(sh.shift_number,0) as shift_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.customers_orders_id as customers_orders_id, " +
                    "           coalesce(dp.price_id,0) as department_type_price_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           coalesce(p.shift_id,0) as shift_id, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           to_char(p.shipment_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as shipment_date, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce(cg.price_type_id,0) as cagent_type_price_id, " +
                    "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id, " +
//                    "           coalesce(p.receipt_id,0) as receipt_id, " +
                    "           p.uid as uid, " +
                    "           p.is_completed as is_completed, " +
                    "           to_char(p.shipment_date at time zone '"+myTimeZone+"', 'HH24:MI') as _time " +

                    "           from shipment p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN shifts sh ON p.shift_id=sh.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(21L, "260")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(21L, "261")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(21L, "262")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                ShipmentJSON returnObj=new ShipmentJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                         obj[0].toString()));
                    returnObj.setMaster((String)                            obj[1]);
                    returnObj.setCreator((String)                           obj[2]);
                    returnObj.setChanger((String)                           obj[3]);
                    returnObj.setMaster_id(Long.parseLong(                  obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(                 obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(    obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(                 obj[7].toString()));
                    returnObj.setDepartment_id(Long.parseLong(              obj[8].toString()));
                    returnObj.setDepartment((String)                        obj[9]);
                    returnObj.setDoc_number(Long.parseLong(                 obj[10].toString()));
                    returnObj.setShift_number((Integer)                     obj[11]);
                    returnObj.setCompany((String)                           obj[12]);
                    returnObj.setDate_time_created((String)                 obj[13]);
                    returnObj.setDate_time_changed((String)                 obj[14]);
                    returnObj.setDescription((String)                       obj[15]);
                    returnObj.setCustomers_orders_id(obj[16]!=null?Long.parseLong(obj[16].toString()):null);
                    returnObj.setDepartment_type_price_id(Long.parseLong(   obj[17].toString()));
                    returnObj.setNds((Boolean)                              obj[18]);
                    returnObj.setNds_included((Boolean)                     obj[19]);
                    returnObj.setCagent_id(Long.parseLong(                  obj[20].toString()));
                    returnObj.setCagent((String)                            obj[21]);
                    returnObj.setShift_id(Long.parseLong(                   obj[22].toString()));
                    returnObj.setShipment_date((String)                     obj[25]);
                    returnObj.setStatus_id(obj[26]!=null?Long.parseLong(    obj[26].toString()):null);
                    returnObj.setStatus_name((String)                       obj[27]);
                    returnObj.setStatus_color((String)                      obj[28]);
                    returnObj.setStatus_description((String)                obj[29]);
                    returnObj.setCagent_type_price_id(Long.parseLong(       obj[30].toString()));
                    returnObj.setDefault_type_price_id(Long.parseLong(      obj[31].toString()));
//                    returnObj.setReceipt_id(Long.parseLong(                 obj[32].toString()));
                    returnObj.setUid((String)                               obj[32]);
                    returnObj.setIs_completed((Boolean)                     obj[33]);
                    returnObj.setShipment_time((String)                     obj[34]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getShipmentValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class ,CantInsertProductRowCauseErrorException.class,CantInsertProductRowCauseOversellException.class,CantSaveProductQuantityException.class})
    public Long insertShipment(ShipmentForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "shipment")){
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
            String myTimeZone = userRepository.getUserTimeZone();
            Long docDepartment=request.getDepartment_id();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            boolean itIsMyDepartment = myDepartmentsIds.contains(docDepartment);
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long linkedDocsGroupId=null;

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            if ((//если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(21L, "253")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(21L, "254") && myCompanyId.equals(request.getCompany_id())) ||
                            //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                            (securityRepositoryJPA.userHasPermissions_OR(21L, "255") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
                    //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                    DocumentMasterId.equals(myMasterId))
            {
                String stringQuery;
                Long myId = userRepository.getUserId();
                Long newDocId;
                Long doc_number;//номер документа( = номер заказа)

                //генерируем номер документа, если его (номера) нет
                if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                    doc_number=Long.valueOf(request.getDoc_number());
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"shipment");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),21));
                }

                //если документ создается из другого документа
                if (request.getLinked_doc_id() != null) {
                    //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                    linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                    if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
                }

                //Возможно 2 ситуации: контрагент выбран из существующих, или выбрано создание нового контрагента
                //Если присутствует 2я ситуация, то контрагента нужно сначала создать, получить его id и уже затем создавать Заказ покупателя:
                if(request.getCagent_id()==null){
                    try{
                        CagentsForm cagentForm = new CagentsForm();
                        cagentForm.setName(request.getNew_cagent());
                        cagentForm.setCompany_id(request.getCompany_id());
                        cagentForm.setOpf_id(2);//ставим по-умолчанию Физ. лицо
                        cagentForm.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),12));
                        cagentForm.setDescription("Автоматическое создание из Отгрузки №"+doc_number.toString());
                        cagentForm.setPrice_type_id(commonUtilites.getPriceTypeDefault(request.getCompany_id()));
                        cagentForm.setTelephone("");
                        cagentForm.setEmail("");
                        cagentForm.setZip_code("");
                        cagentForm.setCountry_id(null);
                        cagentForm.setRegion_id(null);
                        cagentForm.setCity_id(null);
                        cagentForm.setStreet("");
                        cagentForm.setHome("");
                        cagentForm.setFlat("");
                        cagentForm.setAdditional_address("");
                        request.setCagent_id(cagentRepository.insertCagent(cagentForm));
                    }
                    catch (Exception e) {
                        logger.error("Exception in method insertShipment on creating Cagent.", e);
                        e.printStackTrace();
                        return null;
                    }
                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery =
                        " insert into shipment (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " department_id," + //отделение, из(для) которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер заказа
                        " shipment_date," +//план. дата отгрузки
                        " description," +//доп. информация по заказу
                        " nds," +// НДС
                        " nds_included," +// НДС включен в цену
                        " customers_orders_id, " + //родительский Заказ покупателя (если есть)
                        " shift_id, " + // id смены
                        " status_id,"+//статус заказа
                        " linked_docs_group_id," +// id группы связанных документов
                        " uid"+// уникальный идентификатор документа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер заказа
                        "to_timestamp(CONCAT(:shipment_date,' ',:shipment_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"'," +// план. дата и время отгрузки
//                        ((request.getShipment_date()!=null&& !request.getShipment_date().equals(""))?" to_date('"+request.getShipment_date().replaceAll("[^0-9\\.]", "")+"','DD.MM.YYYY'),":"'',")+//план. дата отгрузки
                        ":description," +
                        request.isNds() + ", "+// НДС
                        request.isNds_included() + ", "+// НДС включен в цену
                        request.getCustomers_orders_id() + ", "+
                        request.getShift_id() + ", "+
                        request.getStatus_id()  + ", "+//статус продажи
                        linkedDocsGroupId+"," + // id группы связанных документов
                        ":uid)";// уникальный идентификатор документа
                try{

                    commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("departments", request.getDepartment_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("customers_orders", request.getCustomers_orders_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("shifts", request.getShift_id(), myMasterId);

                    Date dateNow = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.setParameter("shipment_date", ((request.getShipment_date()==null || request.getShipment_date().equals("")) ? dateFormat.format(dateNow) : request.getShipment_date()));
                    query.setParameter("shipment_time", ((request.getShipment_time()==null || request.getShipment_time().equals("")) ? timeFormat.format(dateNow) : request.getShipment_time()));
                    query.executeUpdate();
                    stringQuery="select id from shipment where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    if(insertShipmentProducts(request, newDocId, myMasterId)){
                        //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                        if (request.getLinked_doc_id() != null) {
                            linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "shipment", request.getUid(), request.getCompany_id(), myMasterId);
                        }
                        return newDocId;
                    } else return null;


                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertShipment on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertShipment on inserting into shipment_products cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseOversellException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertShipment on inserting into shipment_products cause oversell.", e);
                    e.printStackTrace();
                    return 0L;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertShipment on inserting into product_history.", e);
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method " + e.getClass().getName() + " on inserting into shipment. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertShipment. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertShipmentProducts(ShipmentForm request, Long docId, Long myMasterId) throws CantInsertProductRowCauseErrorException, CantInsertProductRowCauseOversellException, CantSaveProductHistoryException, CantSaveProductQuantityException {
        Set<Long> rowIds=new HashSet<>();
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        int size = request.getShipmentProductTable().size();
        // перед сохранением таблицы товаров удалим все товары, что удалили на фронтэнде. Для этого накопим id неудалённых товаров и удалим все что не входит в эти id
        for (ShipmentProductTableForm row : request.getShipmentProductTable()) {
            if(!Objects.isNull(row.getId())) rowIds.add(row.getId());
        }
        if (!deleteShipmentProductTableExcessRows((rowIds.size()>0?(commonUtilites.SetOfLongToString(rowIds,",","","")):"0"), request.getId(), myMasterId))
            throw new CantInsertProductRowCauseErrorException();
        //сохранение таблицы
        if (!Objects.isNull(request.getShipmentProductTable()) && request.getShipmentProductTable().size() != 0) {
            for (ShipmentProductTableForm row : request.getShipmentProductTable()) {
                row.setShipment_id(docId);// чтобы через API сюда нельзя было подсунуть рандомный id? да и при создании Отгрузки его id еще не известен
                insertProductRowResult = saveShipmentProductTable(row, request.getCompany_id(), request.isIs_completed(), request.getCustomers_orders_id(), myMasterId);  //сохранение строки таблицы товаров
                if (insertProductRowResult==null || !insertProductRowResult) {
                    if (insertProductRowResult==null){// - т.е. произошла ошибка в методе saveShipmentProductTable
                        throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
                    }else{ // insertProductRowResult==false - товар материален, и его наличия не хватает для продажи
                        throw new CantInsertProductRowCauseOversellException();//кидаем исключение 'оверселл', чтобы произошла отмена транзакции
                    }
                }
            }
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class ,CantInsertProductRowCauseErrorException.class,CantInsertProductRowCauseOversellException.class,CantSaveProductQuantityException.class,Exception.class})
    public Integer updateShipment(ShipmentForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(21L,"264") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(21L,"265") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(21L,"266") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(21L,"267") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.isIs_completed()!=null && request.isIs_completed())){
                if(
                        !(      //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(21L,"396") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(21L,"397") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",request.getId().toString()))||
                                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                                (securityRepositoryJPA.userHasPermissions_OR(21L,"398") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",request.getId().toString()))||
                                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                                (securityRepositoryJPA.userHasPermissions_OR(21L,"399") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment",request.getId().toString()))
                        )
                ) return -1;
            }


            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();
            BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всей отгрузке
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment

            String stringQuery;
            stringQuery =   "update shipment set " +
                    " changer_id = " + myId + ", "+
                    " nds  = " + request.isNds() + ", " +
                    " nds_included  = " + request.isNds_included() + ", " +
                    " date_time_changed= now()," +
                    " description = :description, " +
//                    " shipment_date = to_date(:shipment_date,'DD.MM.YYYY'), " +
                    " shipment_date = to_timestamp(CONCAT(:shipment_date,' ',:shipment_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"',"+
                    " is_completed = " + request.isIs_completed() + "," +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);

                // если документ проводится и нет товаров - ошибка
                if(request.isIs_completed()!=null && request.isIs_completed() && request.getShipmentProductTable().size()==0)
                    throw new CantInsertProductRowCauseErrorException();

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                Query query = entityManager.createNativeQuery(stringQuery);
//                query.setParameter("shipment_date", ((request.getShipment_date()==null || request.getShipment_date().equals("")) ? dateFormat.format(dateNow) : request.getShipment_date()));
                query.setParameter("shipment_date", ((request.getShipment_date()==null || request.getShipment_date().equals("")) ? dateFormat.format(dateNow) : request.getShipment_date()));
                query.setParameter("shipment_time", ((request.getShipment_time()==null || request.getShipment_time().equals("")) ? timeFormat.format(dateNow) : request.getShipment_time()));
                query.setParameter("description",request.getDescription());
                query.executeUpdate();
                if(request.isIs_completed()==null)request.setIs_completed(false);
                if(insertShipmentProducts(request, request.getId(), myMasterId)){//если сохранение товаров из таблицы товаров прошло успешно
                    if(request.isIs_completed()){//если отгузка проводится

                        // корректируем резервы в родительском "Заказе покупателя" (если он есть и если резервы проставлены)
                        // берем id Заказа покупателя (или 0 если его нет)
                        Long customersOrdersId = request.getCustomers_orders_id()==null?0L:request.getCustomers_orders_id();

                        //получаем таблицу из родительского Заказа покупателя (если его нет - у листа просто будет size = 0)
                        List<CustomersOrdersProductTableJSON> customersOrdersProductTable = new ArrayList<>();
                        if(customersOrdersId>0L) {
                            customersOrdersProductTable = customersOrdersRepository.getCustomersOrdersProductTable(customersOrdersId);
                        }
                        // бежим по товарам в Отгрузке
                        for (ShipmentProductTableForm row : request.getShipmentProductTable()) {
                            docProductsSum=docProductsSum.add(row.getProduct_sumprice());
                            // collect product IDs to sytchronization with WooCommerce
                            productsIdsToSyncWoo.add(row.getProduct_id());
                            // if product has "Out of stock after sale" = true, then need to set it as Out-of-stock
                            productsRepository.setProductAsOutOfStockIfOutofstockAftersale(row.getProduct_id(), request.getCompany_id(), myMasterId);
                            //если товар материален и есть родительский Заказ покупателя - нужно у данного товара в Заказе покупателя изменить резерв (если конечно его нужно будет менять
                            if(row.getIs_material() && customersOrdersProductTable.size()>0){
                                //нужно найти этот товар в списке товаров Заказа покупателя по совпадению его id и id его склада (т.к. в Заказе покупателя могут быть несколько позиций одного и того же товара, но с разных складов)
                                for (CustomersOrdersProductTableJSON cuRow : customersOrdersProductTable) {
                                    if(cuRow.getProduct_id().equals(row.getProduct_id()) && cuRow.getDepartment_id().equals(row.getDepartment_id())){
                                        //Товар найден. Сейчас нужно списать его резервы. Для этого нам нужны:

                                        // [Всего заказ] Всего кол-во товара в заказе
                                        BigDecimal product_count = cuRow.getProduct_count();
                                        // [Всего отгружено] - это сумма отгруженных в дочерних документах Заказа покупателя (по проведенным Отгрузкам, проданных по Розничным продажам и в данной отгрузке. )
                                        BigDecimal shipped = productsRepository.getShippedAndSold(row.getProduct_id(),row.getDepartment_id(),request.getCustomers_orders_id())/*.add(product_count)*/;
                                        // [Резервы] - Резервы на данный момент по данному товару в данном складе в данном Заказе покупателя
                                        BigDecimal reserves = productsRepository.getProductReserves(row.getDepartment_id(),row.getProduct_id(),request.getCustomers_orders_id());

                                        // формула расчета нового количества резервов (т.е. до какого значения резервы должны уменьшиться):

                                        // ЕСЛИ [Всего заказ] - [Всего отгружено] < [Резервы] ТО [Резервы] = [Всего заказ] - [Всего отгружено] ИНАЧЕ [Резервы] не трогаем

                                        if(((product_count.subtract(shipped)).compareTo(reserves)) < 0){
                                            reserves = product_count.subtract(shipped);
                                            if(reserves.compareTo(new BigDecimal(0)) < 0) // на тот случай, если отгрузили больше чем в заказе, и резерв насчитался отрицательный - делаем его = 0
                                                reserves = new BigDecimal(0);

                                            productsRepository.updateProductReserves(row.getDepartment_id(),row.getProduct_id(),request.getCustomers_orders_id(), reserves);

                                        }
                                    }
                                }
                            }
                            addProductHistory(row, request, myMasterId);
                        }
                        // обновляем баланс с контрагентом
                        commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "shipment","shipment", request.getId(), new BigDecimal(0), docProductsSum,true,request.getDoc_number(),request.getStatus_id());
                        // отмечаем товары как необходимые для синхронизации с WooCommerce
                        productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, myMasterId);
                    }
                    return 1;
                } else return null;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/updateShipment on updating shipment_product cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductHistoryException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/addShipmentProductHistory on updating shipment_product cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductQuantityException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/setShipmentQuantity on updating shipment_product cause error.", e);
                e.printStackTrace();
                return null;
            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/updateShipment.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method ShipmentRepository/updateShipment.", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.warn("Exception in method ShipmentRepository/addShipmentProductHistory on inserting into product_history cause oversell.", e);
                e.printStackTrace();
                return -80;// недостаточно товара на складе
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.warn("Exception in method ShipmentRepository/setShipmentAsDecompleted.", e);
                e.printStackTrace();
                return -80; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/updateShipment. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }
    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CalculateNetcostNegativeSumException.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setShipmentAsDecompleted(ShipmentForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(21L,"396") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment",request.getId().toString())) ||
                        //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(21L,"397") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",request.getId().toString()))||
                        //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                        (securityRepositoryJPA.userHasPermissions_OR(21L,"398") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",request.getId().toString()))||
                        //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                        (securityRepositoryJPA.userHasPermissions_OR(21L,"399") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment",request.getId().toString()))
        )
        {
            if(request.getShipmentProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment
            String stringQuery =
                    " update shipment set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "shipment"))
                    throw new DocumentAlreadyDecompletedException();

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();

                //сохранение истории движения товара
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                request.setIs_completed(false);
                BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всем товарам документа

                for (ShipmentProductTableForm row : request.getShipmentProductTable()) {
                    docProductsSum=docProductsSum.add(row.getProduct_sumprice());
                    addProductHistory(row, request, myMasterId);
                    productsIdsToSyncWoo.add(row.getProduct_id());
                }
                // обновляем баланс с контрагентом
                commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "shipment","shipment", request.getId(), docProductsSum,new BigDecimal(0),false, request.getDoc_number().toString(),request.getStatus_id());//при приёмке баланс с контрагентом должен смещаться в положительную сторону, т.е. в наш долг контрагенту
                // отмечаем товары как необходимые для синхронизации с WooCommerce
                productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, myMasterId);
                return 1;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/addProductHistory on inserting into product_history cause oversell.", e);
                e.printStackTrace();
                return -80;
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (setShipmentAsDecompleted).", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/setShipmentAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/setShipmentAsDecompleted.", e);
                e.printStackTrace();
                return -80; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ShipmentRepository/setShipmentAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    @SuppressWarnings("Duplicates")
    private Boolean addProductHistory(ShipmentProductTableForm row, ShipmentForm request , Long masterId) throws Exception {
        try {
            // все записи в таблицы product_history и product_quantity производим только если товар материален (т.е. это не услуга и т.п.)
            if (productsRepository.isProductMaterial(row.getProduct_id())) {
                // загружаем настройки, чтобы узнать политику предприятия по подсчёту себестоимости (по всему предприятию или по каждому отделению отдельно)
                String netcostPolicy = commonUtilites.getCompanySettings(request.getCompany_id()).getNetcost_policy();
                // берём информацию о товаре (кол-во и ср. себестоимость) в данном отделении (если netcostPolicy == "all" то независимо от отделения)
                ProductHistoryJSON productInfo = productsRepository.getProductQuantityAndNetcost(masterId, request.getCompany_id(), row.getProduct_id(), netcostPolicy.equals("each") ? row.getDepartment_id() : null);
                // актуальное количество товара В ОТДЕЛЕНИИ
                // Используется для записи нового кол-ва товара в отделении путем вычитания row.getProduct_count() из lastQuantity
                // если политика подсчета себестоимости ПО КАЖДОМУ отделению - lastQuantity отдельно высчитывать не надо - она уже высчитана шагом ранее в productInfo
                BigDecimal lastQuantity =  netcostPolicy.equals("each") ? productInfo.getQuantity() : productsRepository.getProductQuantity(masterId, request.getCompany_id(), row.getProduct_id(), row.getDepartment_id());
                // средняя себестоимость уже имеющегося товара
                BigDecimal lastAvgNetcostPrice = productInfo.getAvg_netcost_price();

                // т.к. это  операция "не поступления" (а убытия), при ее проведении необходимо проверить,
                // сколько товара останется после ее проведения, и если это кол-во <0 то не допустить этого
                if(request.isIs_completed() && (lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                    logger.error("Shipment with id = "+request.getId()+", doc number "+request.getDoc_number()+": the quantity of product to be disposed of from the department is greater than the quantity of product in the department");
                    throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
                }

//                Timestamp timestamp = new Timestamp(((Date) commonUtilites.getFieldValueFromTableById("shipment", "date_time_created", masterId, request.getId())).getTime());

                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), masterId);
                commonUtilites.idBelongsMyMaster("departments", request.getDepartment_id(), masterId);
                commonUtilites.idBelongsMyMaster("products", row.getProduct_id(), masterId);

                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        row.getDepartment_id(),
                        21,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count().negate(),
                        row.getProduct_price(),
                        lastAvgNetcostPrice,// в операциях не поступления товара себестоимость равна актуальной на момент данной операции себестоимости
                        request.isIs_completed()
                );

                if (request.isIs_completed())   // Если проводим
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            row.getDepartment_id(),
                            lastQuantity.subtract(row.getProduct_count()),
                            lastAvgNetcostPrice
                    );
                else                            // Если снимаем с проведения
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            row.getDepartment_id(),
                            lastQuantity.add(row.getProduct_count()),
                            lastAvgNetcostPrice
                    );
            } else{
                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        row.getDepartment_id(),
                        21,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count().negate(),
                        row.getProduct_price(),
                        new BigDecimal("0"),
                        request.isIs_completed()
                );
            }

            return true;

        } catch (CantInsertProductRowCauseOversellException e) { //т.к. весь метод обёрнут в try, данное исключение ловим сначала здесь и перекидываем в родительский метод updateShipment
            e.printStackTrace();
            logger.error("Exception in method ShipmentRepository/addProductHistory (CantInsertProductRowCauseOversellException). ", e);
            throw new CantInsertProductRowCauseOversellException();
        }catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (addProductHistory).", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantSaveProductQuantityException e) {
            logger.error("Exception in method ShipmentRepository/addProductHistory on inserting into product_quantity cause error.", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantSaveProductHistoryException e) {
            logger.error("Exception in method ShipmentRepository/addProductHistory on inserting into product_history.", e);
            e.printStackTrace();
            throw new CantSaveProductHistoryException();
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method ShipmentRepository/addProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }
    //проверяет, не превышает ли продаваемое количество товара доступное количество, имеющееся на складе
    //если не превышает - пишется строка с товаром в БД
    //возвращает: true если все ок, false если превышает и записать нельзя, null если ошибка
    @SuppressWarnings("Duplicates")
    private Boolean saveShipmentProductTable(ShipmentProductTableForm row, Long company_id, Boolean is_completed, Long customersOrdersId, Long master_id) {
        String stringQuery="";
        customersOrdersId = customersOrdersId==null?0L:customersOrdersId;//  на случай если у отгрузки нет родительского Заказа покупателя
        BigDecimal available;   // Если есть постановка в резерв - узнаём, есть ли свободные товары (пока мы редактировали таблицу, кто-то мог поставить эти же товары в свой резерв, и чтобы
        try {

            commonUtilites.idBelongsMyMaster("products",    row.getProduct_id(), master_id);
            commonUtilites.idBelongsMyMaster("shipment",    row.getShipment_id(), master_id);
            commonUtilites.idBelongsMyMaster("sprav_taxes", row.getNds_id(), master_id);
            commonUtilites.idBelongsMyMaster("companies",   company_id, master_id);

            if(row.getIs_material()) //если номенклатура материальна (т.е. это товар, а не услуга и не работа)
                //вычисляем доступное количество товара на складе
                available = productsRepository.getAvailableExceptMyDoc(row.getProduct_id(), row.getDepartment_id(), customersOrdersId);
            else available= BigDecimal.valueOf(0L);
            //если доступное количество товара больше или равно количеству к продаже, либо номенклатура не материальна (т.е. это не товар, а услуга или работа или т.п.) или если документ не проводится
            if (available.compareTo(row.getProduct_count()) > -1 || !row.getIs_material() || (Objects.isNull(is_completed) || !is_completed))
            {
                stringQuery =
                        " insert into shipment_product (" +
                                "master_id, " +
                                "company_id, " +
                                "product_id, " +
                                "shipment_id, " +
                                "product_count, " +
                                "product_price, " +
                                "product_sumprice, " +
                                "price_type_id, " +
                                "nds_id, " +
                                "department_id, " +
                                "product_price_of_type_price " +
                                ") values (" +
                                master_id + "," +
                                company_id + "," +
                                row.getProduct_id() + "," +
                                row.getShipment_id() + "," +
                                row.getProduct_count() + "," +
                                row.getProduct_price() + "," +
                                row.getProduct_sumprice() + "," +
                                row.getPrice_type_id() + "," +
                                row.getNds_id() + ", " +
                                row.getDepartment_id() + ", " +
                                row.getProduct_price_of_type_price() +
                                " ) " +
                                "ON CONFLICT ON CONSTRAINT shipment_product_uq " +// "upsert"
                                " DO update set " +
                                " product_id = " + row.getProduct_id() + "," +
                                " shipment_id = " + row.getShipment_id() + "," +
                                " product_count = " + row.getProduct_count() + "," +
                                " product_price = " + row.getProduct_price() + "," +
                                " product_sumprice = " + row.getProduct_sumprice() + "," +
                                " price_type_id = " + row.getPrice_type_id() + "," +
                                " nds_id = " + row.getNds_id() + "," +
                                " department_id = " + row.getDepartment_id() + "," +
                                " product_price_of_type_price = " + row.getProduct_price_of_type_price();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            } else return false;
        }
        catch (Exception e) {
            logger.error("Exception in method saveShipmentProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean deleteShipmentProductTableExcessRows(String productIds, Long shipment_id, Long myMasterId) {
        String stringQuery;
        stringQuery =   " delete from shipment_product " +
                " where shipment_id=" + shipment_id +
                " and master_id=" + myMasterId +
                " and product_id not in (" + productIds.replaceAll("[^0-9\\,]", "") + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method ShipmentRepository/deleteShipmentProductTableExcessRows. SQL - "+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsShipment(SettingsShipmentForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);
            commonUtilites.idBelongsMyMaster("departments", row.getDepartmentId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_status_dock", row.getStatusIdOnComplete(), myMasterId);
            commonUtilites.idBelongsMyMaster("cagents", row.getCustomerId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_type_prices", row.getPriceTypeId(), myMasterId);

            stringQuery =
                    " insert into settings_shipment (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "date_time_update, " +
                            "pricing_type, " +      //тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Себестоимость (costPrice) 3. Вручную (manual))
                            "price_type_id, " +     //тип цены из справочника Типы цен
                            "change_price, " +      //наценка/скидка в цифре (например, 50)
                            "plus_minus, " +        //определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
                            "change_price_type, " + //тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
                            "hide_tenths, " +       //убирать десятые (копейки) - boolean
                            "save_settings, " +     //сохранять настройки (флажок "Сохранить настройки" будет установлен) - boolean
                            "department_id, " +     //отделение по умолчанию
                            "customer_id, "+        //покупатель по умолчанию
                            "priority_type_price_side, "+ // приоритет типа цены: Склад (sklad) Покупатель (cagent) Цена по-умолчанию (defprice)
//                            "name, "+               //наименование отгрузки
                            "autocreate, "+         //автосоздание нового документа
                            "show_kkm, "+           //показывать блок ККМ
                            "status_id_on_complete,"+// статус документа при проведении
                            "auto_add"+             // автодобавление товара из формы поиска в таблицу
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            "now(), " +
                            ":pricing_type," +
                            row.getPriceTypeId() + "," +
                            row.getChangePrice() + "," +
                            ":plusMinus," +
                            ":changePriceType," +
                            row.getHideTenths() + "," +
                            row.getSaveSettings() + "," +
                            row.getDepartmentId() + "," +
                            row.getCustomerId() + ","+
                            ":priorityTypePriceSide,"+
//                            ":name, " +//наименование
                            row.getAutocreate() +"," +
                            row.getShowKkm() + "," +
                            row.getStatusIdOnComplete()+ "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_shipment_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = :pricing_type,"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = :plusMinus,"+
                            " change_price_type = :changePriceType,"+
                            " hide_tenths = " + row.getHideTenths() + ","+
                            " save_settings = " + row.getSaveSettings() +
                            ", date_time_update = now()" +
                            ", department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", customer_id = "+row.getCustomerId()+
                            ", priority_type_price_side = :priorityTypePriceSide"+
                            ", status_id_on_complete = "+row.getStatusIdOnComplete()+
                            ", show_kkm = "+row.getShowKkm()+
                            ", autocreate = "+row.getAutocreate()+
                            ", auto_add = "+row.getAutoAdd();
//                            (row.getName() == null ? "": (", name = '"+row.getName()+"'"))+


            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("pricing_type", row.getPricingType());
            query.setParameter("plusMinus", row.getPlusMinus());
            query.setParameter("changePriceType", row.getChangePriceType());
            query.setParameter("priorityTypePriceSide", row.getPriorityTypePriceSide());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsShipment. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    //сохраняет настройки РАСЦЕНКИ документа "Отгрузка"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean savePricingSettingsShipment(SettingsShipmentForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_type_prices", row.getPriceTypeId(), myMasterId);
            stringQuery =
                    " insert into settings_shipment (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "pricing_type, " +      //тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Себестоимость (costPrice) 3. Вручную (manual))
                            "price_type_id, " +     //тип цены из справочника Типы цен
                            "change_price, " +      //наценка/скидка в цифре (например, 50)
                            "plus_minus, " +        //определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
                            "change_price_type, " + //тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
                            "hide_tenths, " +       //убирать десятые (копейки) - boolean
                            "save_settings " +      //сохранять настройки (флажок "Сохранить настройки" будет установлен) - boolean
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            ":pricing_type," +
                            row.getPriceTypeId() + "," +
                            row.getChangePrice() + "," +
                            ":plusMinus," +
                            ":changePriceType," +
                            row.getHideTenths() + "," +
                            row.getSaveSettings() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_shipment_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = :pricing_type, " +
                            " price_type_id = " + row.getPriceTypeId() + "," +
                            " change_price = " + row.getChangePrice() + "," +
                            " plus_minus = :plusMinus" + "," +
                            " change_price_type = :changePriceType" + "," +
                            " hide_tenths = " + row.getHideTenths() + "," +
                            " save_settings = " + row.getSaveSettings();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("pricing_type", row.getPricingType());
            query.setParameter("plusMinus", row.getPlusMinus());
            query.setParameter("changePriceType", row.getChangePriceType());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method savePricingSettingsShipment. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsShipmentJSON getSettingsShipment() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           coalesce(p.pricing_type,'priceType') as pricing_type,"+
                "           p.price_type_id as price_type_id, " +
                "           coalesce(p.change_price, 0.00) as change_price, " +
                "           coalesce(p.plus_minus,'plus') as plus_minus, " +
                "           coalesce(p.change_price_type,'procents') as change_price_type,"+
                "           coalesce(p.hide_tenths,false) as hide_tenths, " +
                "           coalesce(p.save_settings,false) as save_settings, " +
                "           p.department_id as department_id, " +
                "           p.customer_id as customer_id, " +
                "           cg.name as customer, " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +
                "           coalesce(p.priority_type_price_side,'defprice') as priority_type_price_side," +
                "           coalesce(p.autocreate,false) as autocreate," +
//                "           p.name as name, " +
                "           p.status_id_on_complete as status_id_on_complete, " +
                "           coalesce(p.show_kkm,false) as show_kkm,  " +                 // показывать блок ККМ
                "           coalesce(p.auto_add,false) as auto_add  " +                 // автодобавление товара из формы поиска в таблицу
                "           from settings_shipment p " +
                "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsShipmentJSON returnObj=new SettingsShipmentJSON();

            for(Object[] obj:queryList){
                returnObj.setPricingType((String)                       obj[0]);
                returnObj.setPriceTypeId(obj[1]!=null?Long.parseLong(   obj[1].toString()):null);
                returnObj.setChangePrice((BigDecimal)                   obj[2]);
                returnObj.setPlusMinus((String)                         obj[3]);
                returnObj.setChangePriceType((String)                   obj[4]);
                returnObj.setHideTenths((Boolean)                       obj[5]);
                returnObj.setSaveSettings((Boolean)                     obj[6]);
                returnObj.setDepartmentId(obj[7]!=null?Long.parseLong(  obj[7].toString()):null);
                returnObj.setCustomerId(obj[8]!=null?Long.parseLong(    obj[8].toString()):null);
                returnObj.setCustomer((String)                          obj[9]);
                returnObj.setId(Long.parseLong(                         obj[10].toString()));
                returnObj.setCompanyId(Long.parseLong(                  obj[11].toString()));
                returnObj.setPriorityTypePriceSide((String)             obj[12]);
                returnObj.setAutocreate((Boolean)                       obj[13]);
//                returnObj.setName((String)                              obj[14]);
                returnObj.setStatusIdOnComplete(obj[14]!=null?Long.parseLong(obj[14].toString()):null);
                returnObj.setShowKkm((Boolean)                          obj[15]);
                returnObj.setAutoAdd((Boolean)                          obj[16]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsShipment. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }

    }

//    @SuppressWarnings("Duplicates") // проверка на наличие чека необходимой операции (operation id, например sell), определенного документа (например, отгрузки, id в таблице documents = 21) с определенным id
//    public Boolean isReceiptPrinted(Long company_id, int document_id, Long id, String operation_id )
//    {
//        String stringQuery;
//        stringQuery = "" +
//                " select 1 from receipts where " +
//                " company_id="+company_id+
//                " and document_id ="+document_id +
//                " and operation_id = '" + operation_id + "'" +
//                " and shipment_id = " + id;//потом название этой колонки нужно будет определять динамически через отдельный метод, засылая туда operation_id
//        try
//        {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            return(query.getResultList().size()>0);
//        }
//        catch (Exception e) {
//            logger.error("Exception in method isReceiptPrinted. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            return true;
//        }
//    }




//    @SuppressWarnings("Duplicates")
//    private Boolean setProductQuantity(ShipmentProductTableForm row, ShipmentForm request , Long masterId) throws CantSaveProductQuantityException {
//        String stringQuery;
//        ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
//        BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
//        stringQuery =
//                " insert into product_quantity (" +
//                        " master_id," +
//                        " department_id," +
//                        " product_id," +
//                        " quantity" +
//                        ") values ("+
//                        masterId + ","+
//                        request.getDepartment_id() + ","+
//                        row.getProduct_id() + ","+
//                        lastQuantity +
//                        ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert"
//                        " DO update set " +
//                        " department_id = " + request.getDepartment_id() + ","+
//                        " product_id = " + row.getProduct_id() + ","+
//                        " master_id = "+ masterId + "," +
//                        " quantity = "+ lastQuantity;
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.executeUpdate();
//            return true;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method ShipmentRepository/setProductQuantity. SQL query:"+stringQuery, e);
//            throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
//        }
//    }



    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteShipment (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(21L,"256") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(21L,"257") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(21L,"258") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",delNumbers)) ||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(21L, "259") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment", delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "shipment");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update shipment p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "shipment")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteShipment. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        delResult.setResult(1);// 1 - ошибка выполнения операции
                        return delResult;
                    }
                } else { //один или несколько документов имеют связь с дочерними документами
                    delResult.setResult(3);// 3 -  связи с дочерними документами
                    delResult.setDocs(checkChilds);
                    return delResult;
                }
            } else { //ошибка проверки на связь с дочерними документами
                delResult.setResult(1);// 1 - ошибка выполнения операции
                return delResult;
            }
        } else {
            delResult.setResult(2);// 2 - нет прав
            return delResult;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteShipment(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(21L,"256") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment",delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(21L,"257") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",delNumbers))||
            //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(21L,"258") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",delNumbers)) ||
            //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(21L, "259") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment", delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update shipment p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteShipment. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
}
