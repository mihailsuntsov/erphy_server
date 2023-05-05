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
import com.dokio.message.request.Settings.SettingsInvoiceoutForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsInvoiceoutJSON;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class InvoiceoutRepositoryJPA {

    Logger logger = Logger.getLogger("InvoiceoutRepositoryJPA");

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
            .of("doc_number","name","cagent","status_name","sum_price","company","department","creator","date_time_created_sort","invoiceout_date_sort","description","is_completed","product_count")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<InvoiceoutJSON> getInvoiceoutTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(31L, "412,413,414,415"))//(см. файл Permissions Id)
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
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from invoiceout_product where invoiceout_id=p.id),0) as sum_price, " +
                    "           to_char(p.invoiceout_date, '"+dateFormat+"') as invoiceout_date, " + // in DB invoiceout_date hasn't information about timezone, and do not need to use 'at timezone' - it will returns wrong date
                    "           cg.name as cagent, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           (select count(*) from invoiceout_product ip where coalesce(ip.invoiceout_id,0)=p.id) as product_count," + //подсчет кол-ва товаров
                    "           p.invoiceout_date as invoiceout_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +

                    "           from invoiceout p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(31L, "412")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(31L, "413")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(31L, "414")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if(filterOptionsIds.contains(2)) // Только просроченные счета
                stringQuery = stringQuery +  " and coalesce(p.is_completed,false)=false and to_timestamp(to_char(p.invoiceout_date,'DD.MM.YYYY')||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') < now()";

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
                List<InvoiceoutJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    InvoiceoutJSON doc=new InvoiceoutJSON();
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
                    doc.setCompany((String)                       obj[11]);
                    doc.setDate_time_created((String)             obj[12]);
                    doc.setDate_time_changed((String)             obj[13]);
                    doc.setDescription((String)                   obj[14]);
                    doc.setStatus_id(obj[15]!=null?Long.parseLong(obj[15].toString()):null);
                    doc.setStatus_name((String)                   obj[16]);
                    doc.setStatus_color((String)                  obj[17]);
                    doc.setStatus_description((String)            obj[18]);
                    doc.setSum_price((BigDecimal)                 obj[19]);
                    doc.setInvoiceout_date((String)               obj[20]);
                    doc.setCagent((String)                        obj[21]);
                    doc.setIs_completed((Boolean)                 obj[22]);
                    doc.setProduct_count(Long.parseLong(          obj[23].toString()));
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInvoiceoutTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getInvoiceoutSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from invoiceout p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(31L, "412")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(31L, "413")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(31L, "414")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if(filterOptionsIds.contains(2)) // Только просроченные счета
            stringQuery = stringQuery +  " and coalesce(p.is_completed,false)=false and to_timestamp(to_char(p.invoiceout_date,'DD.MM.YYYY')||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') < now()";
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
            logger.error("Exception in method getInvoiceoutSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<InvoiceoutProductTableJSON> getInvoiceoutProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(31L, "412,413,414,415"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.invoiceout_id," +
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
                    "   sum(coalesce(reserved_current,0)) " +
                    //по логике: сумма( резерв > (всего - отгружено) ? (всего - отгружено) : резерв)    (при условии не позволять в заказах покупателей делать резерв больше "всего" (reserved_current!>product_count))
                    "   from " +
                    "   customers_orders_product " +
                    "   where " +
                    "   product_id=ap.product_id "+
                    "   and department_id = ap.department_id) as reserved, "+//зарезервировано в выбранном отделении

//                    " ap.product_count as shipped, "+//в розничных продажах все количество товара считается отгруженным, т.к. розн. продажа создается в момент продажи (отгрузки) товара.
                    " ap.department_id as department_id, " +
                    " (select name from departments where id= ap.department_id) as department, "+
                    " ap.id  as row_id, " +
                    " ppr.is_material as is_material, " +
//                    " ap.product_count as reserved_current " +//в розничных продажах нет резервов, так что приравниваем резерв к количеству товара в продаже (т.е. весь товар априори зарезервирован)
                    " p.indivisible as indivisible,"+// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce(nds.value,0) as nds_value" +
//                    " ROUND(((cast(nds.value AS numeric)/100)*ap.product_count*ap.product_price),2) as nds_value" +
//                    " (cast(nds.value AS numeric)/100) as nds_value" +
                    " from " +
                    " invoiceout_product ap " +
                    " INNER JOIN invoiceout a ON ap.invoiceout_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " LEFT OUTER JOIN sprav_taxes nds ON nds.id = ap.nds_id" +
                    " where a.master_id = " + myMasterId +
                    " and ap.invoiceout_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(31L, "412")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(31L, "413")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(31L, "414")) //Если нет прав на просмотр всех доков в своих подразделениях
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
                List<InvoiceoutProductTableJSON> returnList = new ArrayList<>();
                int row_num = 1; // номер строки при выводе печатной версии
                for(Object[] obj:queryList){
                    InvoiceoutProductTableJSON doc=new InvoiceoutProductTableJSON();
                    doc.setRow_num(row_num);
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setInvoiceout_id(Long.parseLong(                    obj[1].toString()));
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
                    doc.setIs_material((Boolean)                            obj[16]);
                    doc.setIndivisible((Boolean)                            obj[17]);
                    doc.setNds_value((BigDecimal)                           obj[18]);

                    returnList.add(doc);
                    row_num++;
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getRetailSalesProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public InvoiceoutJSON getInvoiceoutValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(31L, "412,413,414,415"))//см. _Permissions Id.txt
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
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(dp.price_id,0) as department_type_price_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           to_char(p.invoiceout_date, 'DD.MM.YYYY') as invoiceout_date, " + // the same as due_date but in system format DD.MM.YYYY format
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce(cg.price_type_id,0) as cagent_type_price_id, " +
                    "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id, " +
                    "           p.uid as uid, " +
                    "           p.is_completed as is_completed, " +
                    "           to_char(p.invoiceout_date, '"+dateFormat+"') as due_date, " + // the same as invoiceout_date but in user's format (for using in print templates)
                    "           to_char(p.invoiceout_date at time zone '"+myTimeZone+"', 'HH24:MI') as due_time " +

                    "           from invoiceout p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(31L, "412")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(31L, "413")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(31L, "414")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                InvoiceoutJSON returnObj=new InvoiceoutJSON();

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
                    returnObj.setCompany((String)                           obj[11]);
                    returnObj.setDate_time_created((String)                 obj[12]);
                    returnObj.setDate_time_changed((String)                 obj[13]);
                    returnObj.setDescription((String)                       obj[14]);
                    returnObj.setDepartment_type_price_id(Long.parseLong(   obj[15].toString()));
                    returnObj.setNds((Boolean)                              obj[16]);
                    returnObj.setNds_included((Boolean)                     obj[17]);
                    returnObj.setCagent_id(Long.parseLong(                  obj[18].toString()));
                    returnObj.setCagent((String)                            obj[19]);
                    returnObj.setInvoiceout_date((String)                   obj[20]);
                    returnObj.setStatus_id(obj[21]!=null?Long.parseLong(    obj[21].toString()):null);
                    returnObj.setStatus_name((String)                       obj[22]);
                    returnObj.setStatus_color((String)                      obj[23]);
                    returnObj.setStatus_description((String)                obj[24]);
                    returnObj.setCagent_type_price_id(Long.parseLong(       obj[25].toString()));
                    returnObj.setDefault_type_price_id(Long.parseLong(      obj[26].toString()));
                    returnObj.setUid((String)                               obj[27]);
                    returnObj.setIs_completed((Boolean)                     obj[28]);
                    returnObj.setDue_date((String)                          obj[29]);
                    returnObj.setInvoiceout_time((String)                   obj[30]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInvoiceoutValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class ,CantInsertProductRowCauseErrorException.class,CantInsertProductRowCauseOversellException.class,CantSaveProductQuantityException.class})
    public Long insertInvoiceout(InvoiceoutForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "invoiceout")){
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
                    (securityRepositoryJPA.userHasPermissions_OR(31L, "405")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(31L, "406") && myCompanyId.equals(request.getCompany_id())) ||
                            //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                            (securityRepositoryJPA.userHasPermissions_OR(31L, "407") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
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
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"invoiceout");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),31));
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
                        cagentForm.setDescription("Автоматическое создание из Счета покупателю №"+doc_number.toString());
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
                        logger.error("Exception in method insertInvoiceout on creating Cagent.", e);
                        e.printStackTrace();
                        return null;
                    }
                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery =
                        " insert into invoiceout (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " department_id," + //отделение, из(для) которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер документа
                        " invoiceout_date," +//план. дата
                        " description," +//доп. информация по заказу
                        " nds," +// НДС
                        " nds_included," +// НДС включен в цену
                        " status_id,"+//статус
                        " linked_docs_group_id," +// id группы связанных документов
                        " uid"+// уникальный идентификатор документа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер документа
                        "to_timestamp(CONCAT(:invoiceout_date,' ',:invoiceout_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"'," +//план. дата и время
                        ":description," +
                        request.isNds() + ", "+// НДС
                        request.isNds_included() + ", "+// НДС включен в цену
                        request.getStatus_id()  + ", "+//статус
                        linkedDocsGroupId+"," + // id группы связанных документов
                        ":uid)";// уникальный идентификатор документа
                try{
                    Date dateNow = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.setParameter("invoiceout_date", ((request.getInvoiceout_date()==null || request.getInvoiceout_date().equals("")) ? dateFormat.format(dateNow) : request.getInvoiceout_date()));
                    query.setParameter("invoiceout_time", ((request.getInvoiceout_time()==null || request.getInvoiceout_time().equals("")) ? timeFormat.format(dateNow) : request.getInvoiceout_time()));
                    query.executeUpdate();
                    stringQuery="select id from invoiceout where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    if(insertInvoiceoutProducts(request, newDocId, myMasterId)){
                        //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                        if (request.getLinked_doc_id() != null) {
                            linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "invoiceout", request.getUid(), request.getCompany_id(), myMasterId);
                        }
                        return newDocId;
                    } else return null;


                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertInvoiceout on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertInvoiceout on inserting into invoiceout_products cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseOversellException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertInvoiceout on inserting into invoiceout_products cause oversell.", e);
                    e.printStackTrace();
                    return 0L;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertInvoiceout on inserting into products_history.", e);
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method " + e.getClass().getName() + " on inserting into invoiceout. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } else {
            logger.info("Double UUID found on insertInvoiceout. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertInvoiceoutProducts(InvoiceoutForm request, Long docId, Long myMasterId) throws CantInsertProductRowCauseErrorException, CantInsertProductRowCauseOversellException, CantSaveProductHistoryException, CantSaveProductQuantityException {
        Set<Long> productIds=new HashSet<>();
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        int size = request.getInvoiceoutProductTable().size();
        //сохранение таблицы
        if (!Objects.isNull(request.getInvoiceoutProductTable()) && request.getInvoiceoutProductTable().size() != 0) {
            for (InvoiceoutProductTableForm row : request.getInvoiceoutProductTable()) {
                row.setInvoiceout_id(docId);// чтобы через API сюда нельзя было подсунуть рандомный id
                insertProductRowResult = saveInvoiceoutProductTable(row, request.getCompany_id(), request.getIs_completed(), 0L, myMasterId);  //сохранение строки таблицы товаров
                if (insertProductRowResult==null || !insertProductRowResult) {
                    if (insertProductRowResult==null){// - т.е. произошла ошибка в методе saveInvoiceoutProductTable
                        throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
                    }else{ // insertProductRowResult==false - товар материален, и его наличия не хватает для продажи
                        throw new CantInsertProductRowCauseOversellException();//кидаем исключение 'оверселл', чтобы произошла отмена транзакции
                    }
                }
                productIds.add(row.getProduct_id());
            }
        }if (!deleteInvoiceoutProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", docId)){
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class ,CantInsertProductRowCauseErrorException.class,CantInsertProductRowCauseOversellException.class,CantSaveProductQuantityException.class})
    public Integer updateInvoiceout(InvoiceoutForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(31L,"416") && securityRepositoryJPA.isItAllMyMastersDocuments("invoiceout",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(31L,"417") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoiceout",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(31L,"418") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoiceout",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(31L,"419") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoiceout",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                    !(  //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(31L,"420") && securityRepositoryJPA.isItAllMyMastersDocuments("invoiceout",request.getId().toString())) ||
                        //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(31L,"421") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoiceout",request.getId().toString()))||
                        //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                        (securityRepositoryJPA.userHasPermissions_OR(31L,"422") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoiceout",request.getId().toString()))||
                        //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                        (securityRepositoryJPA.userHasPermissions_OR(31L,"423") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoiceout",request.getId().toString()))
                    )
                ) return -1;
            }
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();

            String stringQuery;
            stringQuery =   " update invoiceout set " +
                    " changer_id = " + myId + ", "+
                    " nds  = " + request.isNds() + ", " +
                    " nds_included  = " + request.isNds_included() + ", " +
                    " date_time_changed= now()," +
                    " description = :description, " +
//                    " invoiceout_date = to_date(:invoiceout_date,'DD.MM.YYYY'), " +
                    " invoiceout_date = to_timestamp(CONCAT(:invoiceout_date,' ',:invoiceout_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"',"+
                    " is_completed = " + request.getIs_completed() + "," +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "invoiceout"))
                    throw new DocumentAlreadyCompletedException();

                if(request.getIs_completed()!=null && request.getIs_completed() && request.getInvoiceoutProductTable().size()==0) throw new Exception("There is no products in product list");

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("invoiceout_date", ((request.getInvoiceout_date()==null || request.getInvoiceout_date().equals("")) ? dateFormat.format(dateNow) : request.getInvoiceout_date()));
                query.setParameter("invoiceout_time", ((request.getInvoiceout_time()==null || request.getInvoiceout_time().equals("")) ? "00:00" : request.getInvoiceout_time()));
//                query.setParameter("invoiceout_date", ((request.getInvoiceout_date()==null || request.getInvoiceout_date().equals("")) ? dateFormat.format(dateNow) : request.getInvoiceout_date()));
                query.setParameter("description",request.getDescription());
                query.executeUpdate();
                if(request.getIs_completed()==null)request.setIs_completed(false);
                if(insertInvoiceoutProducts(request, request.getId(), myMasterId)){//если сохранение товаров из таблицы товаров прошло успешно
                    return 1;
                } else return null;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateInvoiceout.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceoutRepository/updateInvoiceout on updating invoiceout_product cause error.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceoutRepository/updateInvoiceout. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, NotEnoughPermissionsException.class})
    public Integer setInvoiceoutAsDecompleted(InvoiceoutForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(31L,"420") && securityRepositoryJPA.isItAllMyMastersDocuments("invoiceout",request.getId().toString())) ||
            //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(31L,"421") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoiceout",request.getId().toString()))||
            //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(31L,"422") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoiceout",request.getId().toString()))||
            //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(31L,"423") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoiceout",request.getId().toString()))
        )
        {
            if(request.getInvoiceoutProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update invoiceout set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "invoiceout"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                // сохранение истории движения товара не делаем, т.к. в данный документ не влияет на движение товаров
                // по той же причине не делаем коррекцию баланса с контрагентом
                return 1;
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceoutRepository/setInvoiceoutAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceoutRepository/setInvoiceoutAsDecompleted.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceoutRepository/setInvoiceoutAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    //проверяет, не превышает ли продаваемое количество товара доступное количество, имеющееся на складе
    //если не превышает - пишется строка с товаром в БД
    //возвращает: true если все ок, false если превышает и записать нельзя, null если ошибка
    @SuppressWarnings("Duplicates")
    private Boolean saveInvoiceoutProductTable(InvoiceoutProductTableForm row, Long company_id, Boolean is_completed, Long customersOrdersId, Long master_id) {
        String stringQuery="";
        customersOrdersId = customersOrdersId==null?0L:customersOrdersId;//  на случай если у отгрузки нет родительского Заказа покупателя
        BigDecimal available;   // Если есть постановка в резерв - узнаём, есть ли свободные товары (пока мы редактировали таблицу, кто-то мог поставить эти же товары в свой резерв, и чтобы
        try {
//            if(row.getIs_material()) //если номенклатура материальна (т.е. это товар, а не услуга и не работа)
                //вычисляем доступное количество товара на складе
//                available = productsRepository.getAvailableExceptMyDoc(row.getProduct_id(), row.getDepartment_id(), customersOrdersId);
//            else available= BigDecimal.valueOf(0L);
            //если доступное количество товара больше или равно количеству к продаже, либо номенклатура не материальна (т.е. это не товар, а услуга или работа или т.п.) или если документ не проводится

            // НА ДАННЫЙ МОМЕНТ ТАКУЮ ПРОВЕРКУ НЕ ДЕЛАЕМ, Т.К. ДЛЯ СЧЁТА ПОКУПАТЕЛЯ ЭТО НЕ ТАК ВАЖНО - КОЛИЧЕСТВО ТОВАРА МОЖЕТ ПРЕВЫШАТЬ ДОСТУПНОЕ, НО ЧАСТЬ ТОВАРА МОЖЕТ БЫТЬ В ПУТИ, И ПОКУПАТЕЛЬ ПРОСТО ДЕЛАЕТ ПРЕДОПЛАТУ ПОД БУДУЩУЮ ПОСТАВКУ
            // ТАКЖЕ СЧЁТ ПОКУПАТЕЛЮ НЕ ВЛИЯЕТ НА КОЛИЧЕСТВО ТОВАРА НА СКЛАДЕ, И НЕ УЧАСТВУЕТ В РЕЗЕРВИРОВАНИИ ТОВАРА

//          if (available.compareTo(row.getProduct_count()) > -1 || !row.getIs_material() || !is_completed)
//          {
                stringQuery =
                        " insert into invoiceout_product (" +
                                "master_id, " +
                                "company_id, " +
                                "product_id, " +
                                "invoiceout_id, " +
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
                                row.getInvoiceout_id() + "," +
                                row.getProduct_count() + "," +
                                row.getProduct_price() + "," +
                                row.getProduct_sumprice() + "," +
                                row.getPrice_type_id() + "," +
                                row.getNds_id() + ", " +
                                row.getDepartment_id() + ", " +
                                row.getProduct_price_of_type_price() +
                                " ) " +
                                "ON CONFLICT ON CONSTRAINT invoiceout_product_uq " +// "upsert"
                                " DO update set " +
                                " product_id = " + row.getProduct_id() + "," +
                                " invoiceout_id = " + row.getInvoiceout_id() + "," +
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
//            } else return false;
        }
        catch (Exception e) {
            logger.error("Exception in method saveInvoiceoutProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean deleteInvoiceoutProductTableExcessRows(String productIds, Long invoiceout_id) {
        String stringQuery;
        stringQuery =   " delete from invoiceout_product " +
                " where invoiceout_id=" + invoiceout_id +
                " and product_id not in (" + productIds.replaceAll("[^0-9\\,]", "") + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method InvoiceoutRepository/deleteInvoiceoutProductTableExcessRows. SQL - "+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsInvoiceout(SettingsInvoiceoutForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into settings_invoiceout (" +
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
                            "autocreate, "+         //автосоздание нового документа
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
                            row.getAutocreate() +", " +
                            row.getStatusIdOnComplete()+ "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_invoiceout_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = :pricing_type,"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = :plusMinus,"+
                            " change_price_type = :changePriceType,"+
                            " hide_tenths = " + row.getHideTenths() + ","+
                            " date_time_update = now(), " +
                            " save_settings = " + row.getSaveSettings() +
                            (row.getDepartmentId() == null ? "": (", department_id = "+row.getDepartmentId()))+//некоторые строки (как эту) проверяем на null, потому что при сохранении из расценки они не отправляются, и эти настройки сбрасываются изза того, что в них прописываются null
                            (row.getCompanyId() == null ? "": (", company_id = "+row.getCompanyId()))+
                            ", customer_id = "+row.getCustomerId()+
                            ", priority_type_price_side = :priorityTypePriceSide"+
                            (row.getStatusIdOnComplete() == null ? "": (", status_id_on_complete = "+row.getStatusIdOnComplete()))+
                            (row.getAutocreate() == null ? "": (", autocreate = "+row.getAutocreate()))+
                            (row.getAutoAdd() == null ? "": (", auto_add = "+row.getAutoAdd()));

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("pricing_type", row.getPricingType());
            query.setParameter("plusMinus", row.getPlusMinus());
            query.setParameter("changePriceType", row.getChangePriceType());
            query.setParameter("priorityTypePriceSide", row.getPriorityTypePriceSide());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsInvoiceout. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsInvoiceoutJSON getSettingsInvoiceout() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.pricing_type as pricing_type, " +
                "           p.price_type_id as price_type_id, " +
                "           p.change_price as change_price, " +
                "           p.plus_minus as plus_minus, " +
                "           p.change_price_type as change_price_type, " +
                "           coalesce(p.hide_tenths,false) as hide_tenths, " +
                "           coalesce(p.save_settings,false) as save_settings, " +
                "           p.department_id as department_id, " +
                "           p.customer_id as customer_id, " +
                "           cg.name as customer, " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +
                "           p.priority_type_price_side as priority_type_price_side," +
                "           coalesce(p.autocreate,false) as autocreate," +
                "           p.status_id_on_complete as status_id_on_complete, " +
                "           coalesce(p.auto_add,false) as auto_add  " +                 // автодобавление товара из формы поиска в таблицу
                "           from settings_invoiceout p " +
                "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsInvoiceoutJSON returnObj=new SettingsInvoiceoutJSON();

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
                returnObj.setStatusIdOnComplete(obj[14]!=null?Long.parseLong(obj[14].toString()):null);
                returnObj.setAutoAdd((Boolean)                          obj[15]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsInvoiceout. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }

    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteInvoiceout (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(31L,"408") && securityRepositoryJPA.isItAllMyMastersDocuments("invoiceout",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(31L,"409") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoiceout",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(31L,"410") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoiceout",delNumbers)) ||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(31L, "411") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoiceout", delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "invoiceout");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update invoiceout p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "invoiceout")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteInvoiceout. SQL query:" + stringQuery, e);
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
    public Integer undeleteInvoiceout(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(31L,"408") && securityRepositoryJPA.isItAllMyMastersDocuments("invoiceout",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(31L,"409") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoiceout",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(31L,"410") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoiceout",delNumbers)) ||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(31L, "411") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoiceout", delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update invoiceout p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "")+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteInvoiceout. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
}
