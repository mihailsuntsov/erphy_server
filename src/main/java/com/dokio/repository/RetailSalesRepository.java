/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.repository;

import com.dokio.message.request.*;
import com.dokio.message.response.*;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class RetailSalesRepository {

    Logger logger = Logger.getLogger("RetailSalesRepository");

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
    private WriteoffRepositoryJPA writeoffRepository;

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<RetailSalesJSON> getRetailSalesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(25L, "315,316,317"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.shift_id as shift_id, " +
                    "           p.shipment_date as shipment_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from retail_sales_product where retail_sales_id=p.id),0) as sum_price, " +
                    "           p.name as name, " +
                    "           sh.shift_number as shift_number " +

                    "           from retail_sales p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN shifts sh ON p.shift_id=sh.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "315")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(24L, "316")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (317)
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.shipment_date, 'DD.MM.YYYY') ='"+searchString+"' or "+
                        " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                        " upper(dp.name) like upper('%" + searchString + "%') or "+
                        " upper(cmp.name) like upper('%" + searchString + "%') or "+
                        " upper(us.name) like upper('%" + searchString + "%') or "+
                        " upper(uc.name) like upper('%" + searchString + "%') or "+
                        " upper(p.description) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<RetailSalesJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    RetailSalesJSON doc=new RetailSalesJSON();
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
                    doc.setShift_id(Long.parseLong(               obj[16].toString()));
                    doc.setStatus_id(obj[20]!=null?Long.parseLong(obj[20].toString()):null);
                    doc.setStatus_name((String)                   obj[21]);
                    doc.setStatus_color((String)                  obj[22]);
                    doc.setStatus_description((String)            obj[23]);
                    doc.setSum_price((BigDecimal)                 obj[24]);
                    doc.setName((String)                          obj[25]);
                    doc.setShift_number((Integer)                 obj[26]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getRetailSalesTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getRetailSalesSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from retail_sales p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(24L, "315")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения
            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "316")) //Если нет прав на просм по своему предприятию
            {//остается только на просмотр всех доков в своих отделениях (317)
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
            }//т.е. по всем и своему предприятиям нет а на свои отделения есть
            else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.shipment_date, 'DD.MM.YYYY') ='"+searchString+"' or "+
                    " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                    " upper(dp.name) like upper('%" + searchString + "%') or "+
                    " upper(cmp.name) like upper('%" + searchString + "%') or "+
                    " upper(us.name) like upper('%" + searchString + "%') or "+
                    " upper(uc.name) like upper('%" + searchString + "%') or "+
                    " upper(p.description) like upper('%" + searchString + "%')"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getRetailSalesSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<RetailSalesProductTableJSON> getRetailSalesProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(25L, "315,316,317"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.retail_sales_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " ap.edizm_id," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = ap.edizm_id) as edizm," +
                    " ap.nds_id," +
                    " (select nds.name from sprav_sys_nds nds where nds.id = ap.nds_id) as nds," +
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
                    "   and customers_orders_id!=coalesce(ap.customers_orders_id,0)) as reserved, "+//зарезервировано в других документах Заказ покупателя

//                    " ap.product_count as shipped, "+//в розничных продажах все количество товара считается отгруженным, т.к. розн. продажа создается в момент продажи (отгрузки) товара.
                    " ap.department_id as department_id, " +
                    " (select name from departments where id= ap.department_id) as department, "+
                    " ap.id  as row_id, " +
                    " ppr.name_api_atol as ppr_name_api_atol, " +
                    " ppr.is_material as is_material, " +
//                    " ap.product_count as reserved_current " +//в розничных продажах нет резервов, так что приравниваем резерв к количеству товара в продаже (т.е. весь товар априори зарезервирован)
                    " from " +
                    " retail_sales_product ap " +
                    " INNER JOIN retail_sales a ON ap.retail_sales_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.retail_sales_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "315")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(24L, "316")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (317)
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<RetailSalesProductTableJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    RetailSalesProductTableJSON doc=new RetailSalesProductTableJSON();
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setCustomers_orders_id(Long.parseLong(              obj[1].toString()));
                    doc.setProduct_count(                                   obj[2]==null?BigDecimal.ZERO:(BigDecimal)obj[2]);
                    doc.setProduct_price(                                   obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                    doc.setProduct_sumprice(                                obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                    doc.setEdizm_id(obj[7]!=null?Long.parseLong(            obj[5].toString()):null);
                    doc.setName((String)                                    obj[6]);
                    doc.setEdizm((String)                                   obj[7]);
                    doc.setNds_id(Long.parseLong(                           obj[8].toString()));
                    doc.setNds((String)                                     obj[9]);
                    doc.setPrice_type_id(obj[10]!=null?Long.parseLong(      obj[10].toString()):null);
                    doc.setPrice_type((String)                              obj[11]);
                    doc.setTotal(                                           obj[12]==null?BigDecimal.ZERO:(BigDecimal)obj[12]);
                    doc.setReserved(                                        obj[13]==null?BigDecimal.ZERO:(BigDecimal)obj[13]);
                    doc.setDepartment_id(Long.parseLong(                    obj[14].toString()));
                    doc.setDepartment((String)                              obj[15]);
                    doc.setId(Long.parseLong(                               obj[16].toString()));
                    doc.setPpr_name_api_atol((String)                       obj[17]);
                    doc.setIs_material((Boolean)                            obj[18]);
    //                doc.setReserved_current(                                obj[19]==null?BigDecimal.ZERO:(BigDecimal)obj[20]);

                    returnList.add(doc);
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
//    @Transactional
    public RetailSalesJSON getRetailSalesValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(25L, "315,316,317"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
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
                "           dp.name ||' '||dp.address  as department, " +
                "           p.doc_number as doc_number, " +
                "           sh.shift_number as shift_number, " +
                "           cmp.name as company, " +
                "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                "           p.description as description, " +
                "           p.customers_orders_id as customers_orders_id, " +
                "           coalesce(dp.price_id,0) as department_type_price_id, " +
                "           coalesce(p.nds,false) as nds, " +
                "           coalesce(p.nds_included,false) as nds_included, " +
                "           p.cagent_id as cagent_id, " +
                "           cg.name as cagent, " +
                "           p.shift_id as shift_id, " +
                "           p.date_time_created as date_time_created_sort, " +
                "           p.date_time_changed as date_time_changed_sort, " +
                "           p.name as name, " +
                "           p.status_id as status_id, " +
                "           stat.name as status_name, " +
                "           stat.color as status_color, " +
                "           stat.description as status_description, " +
                "           coalesce(cg.price_type_id,0) as cagent_type_price_id, " +
                "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id " +
                "           from retail_sales p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN shifts sh ON p.shift_id=sh.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                "           LEFT OUTER JOIN sprav_sys_countries ctr ON p.country_id=ctr.id" +
                "           LEFT OUTER JOIN sprav_sys_regions reg ON p.region_id=reg.id" +
                "           LEFT OUTER JOIN sprav_sys_cities cty ON p.city_id=cty.id" +
                "           where  p.master_id=" + myMasterId +
                "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(25L, "315")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(25L, "316")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(25L, "317")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                RetailSalesJSON returnObj=new RetailSalesJSON();

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
                    returnObj.setName((String)                              obj[25]);
                    returnObj.setStatus_id(obj[26]!=null?Long.parseLong(    obj[26].toString()):null);
                    returnObj.setStatus_name((String)                       obj[27]);
                    returnObj.setStatus_color((String)                      obj[28]);
                    returnObj.setStatus_description((String)                obj[29]);
                    returnObj.setCagent_type_price_id(Long.parseLong(       obj[30].toString()));
                    returnObj.setDefault_type_price_id(Long.parseLong(      obj[31].toString()));
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getRetailSalesValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }



    // Розничная продажа создается с её товарами, и в дальнейшем параметры таблицы с товарами (количество, цена) изменить невозможно, т.к. выбит чек.
    // Когда создаем товары - происходит проверка на то, что количество товара меньше или равно доступному количеству, чтобы не залезть в минуса или в резервы
    // Такая проверка происходит и на фронтэнде, но за время от добавления товара в таблицу и отправки розничной продажи на создание доступное количество товара
    // вследствие деятельности других продавцов предприятия может уменьшится, либо резервы вырасти. Поэтому эта проверка происходит еще и на бэкэнде
    // Возвращаем id Розничной продажи в случае ее успешного создания
    // Возвращаем 0 если невозможно по вышеизложенным причинам создать товарные позиции для Розничной продажи
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertRetailSales(RetailSalesForm request) throws CantInsertProductRowException{
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Long dockDepartment=request.getDepartment_id();
        List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
        boolean itIsMyDepartment = myDepartmentsIds.contains(dockDepartment);
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.



        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

//        try{

            if ((//если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(25L, "309")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(25L, "310") && myCompanyId.equals(request.getCompany_id())) ||
                            //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                            (securityRepositoryJPA.userHasPermissions_OR(25L, "311") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
                    //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                    DocumentMasterId.equals(myMasterId))
            {
                String stringQuery;
                Long myId = userRepository.getUserId();
                Long newDockId;
                Long doc_number;//номер документа( = номер заказа)

                //генерируем номер документа, если его (номера) нет
                if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                    doc_number=Long.valueOf(request.getDoc_number());
                } else doc_number=generateDocNumberCode(request.getCompany_id());

                //Возможно 2 ситуации: контрагент выбран из существующих, или выбрано создание нового контрагента
                //Если присутствует 2я ситуация, то контрагента нужно сначала создать, получить его id и уже затем создавать Заказ покупателя:
                if(request.getCagent_id()==null){
                    try{
                        CagentsForm cagentForm = new CagentsForm();
                        cagentForm.setName(request.getNew_cagent());
                        cagentForm.setCompany_id(request.getCompany_id());
                        cagentForm.setOpf_id(2);//ставим по-умолчанию Физ. лицо
                        cagentForm.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),12));
                        cagentForm.setDescription("Автоматическое создание из Заказа покупателя №"+doc_number.toString());
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
                        logger.error("Exception in method insertRetailSales on creating Cagent.", e);
                        e.printStackTrace();
                        return null;
                    }
                }


                String timestamp = new Timestamp(System.currentTimeMillis()).toString();

                stringQuery =   "insert into retail_sales (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " department_id," + //отделение, из(для) которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер заказа
                        " name," + //наименование заказа
                        " description," +//доп. информация по заказу
                        " nds," +// НДС
                        " nds_included," +// НДС включен в цену
                        " customers_orders_id, " + //родительский Заказ покупателя (если есть)
                        " shift_id, " + // id смены
                        " status_id"+//статус заказа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер заказа
                        "'" + (request.getName() == null ? "": request.getName()) + "', " +//наименование
                        "'" + (request.getDescription() == null ? "": request.getDescription()) +  "', " +//описание
                        request.isNds() + ", "+// НДС
                        request.isNds_included() + ", "+// НДС включен в цену
                        request.getCustomers_orders_id() + ", "+
                        request.getShift_id() + ", "+
                        request.getStatus_id() + ")";//статус продажи
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    stringQuery="select id from retail_sales where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDockId=Long.valueOf(query2.getSingleResult().toString());

                    RetailSalesInsertReportJSON updateResults = new RetailSalesInsertReportJSON();// отчет об апдейте
                    Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). 0- успешно с сохранением вкл. резерва. 1 - включенный резерв не был сохранён

                    //сохранение таблицы
                    if (request.getRetailSalesProductTable()!=null && request.getRetailSalesProductTable().size() > 0) {
                        for (RetailSalesProductTableForm row : request.getRetailSalesProductTable()) {
                            insertProductRowResult = saveRetailSalesProductTable(row, request.getCompany_id(), myMasterId);  //метод 2 - сохранение таблицы товаров
                            if (!insertProductRowResult) {
                                throw new CantInsertProductRowException();
                            }
                        }
                    }

                    return newDockId;
                } catch (CantInsertProductRowException e) {
                    logger.error("Exception in method insertRetailSales on inserting into retail_sales_products.", e);
                    e.printStackTrace();
                    return 0L;
                } catch (Exception e) {
                    logger.error("Exception in method insertRetailSales on inserting into retail_sales. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
    }

        @Transactional
        public Boolean updateRetailSales(RetailSalesForm request)  throws Exception{
            //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
            if(     (securityRepositoryJPA.userHasPermissions_OR(25L,"318") && securityRepositoryJPA.isItAllMyMastersDocuments("retail_sales",request.getId().toString())) ||
                    //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(25L,"319") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("retail_sales",request.getId().toString()))||
                    //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(25L,"320") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("retail_sales",request.getId().toString())))
            {
                Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

                String stringQuery;
                stringQuery =   " update retail_sales set " +
                        " changer_id = " + myId + ", "+
                        " date_time_changed= now()," +
                        " description = '" + (request.getDescription() == null ? "" : request.getDescription()) + "', " +
                        " name = '" + (request.getName() == null ? "" : request.getName()) + "', " +
                        " status_id = " + request.getStatus_id() +
                        " where " +
                        " id= "+request.getId();
                try
                {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    return true;
                }catch (Exception e) {
                    logger.error("Exception in method updateRetailSales. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return false;
                }
            } else return null;
        }

        //проверяет, не превышает ли продаваемое количество товара доступное количество, имеющееся на складе
        //если не превышает - пишется строка с товаром в БД
        //возвращает: true если все ок, false если превышает и записать нельзя, null если ошибка
        @SuppressWarnings("Duplicates")
        private Boolean saveRetailSalesProductTable(RetailSalesProductTableForm row, Long company_id, Long master_id) {
            String stringQuery="";
            Integer saveResult=0;   // 0 - если был резерв - он сохранился, 1 - если был резерв - он отменился. (это относится только к вновь поставленным резервам) Если резерв уже был выставлен - он не отменится.
            BigDecimal available;   // Если есть постановка в резерв - узнаём, есть ли свободные товары (пока мы редактировали таблицу, кто-то мог поставить эти же товары в свой резерв, и чтобы
            try {
                //вычисляем доступное количество товара на складе
                available = productsRepository.getAvailableExceptMyDock(row.getProduct_id(), row.getDepartment_id(), row.getCustomers_orders_id());
                if (available.compareTo(row.getProduct_count()) > -1) //если доступное количество товара больше или равно количеству к продаже
                {
                    stringQuery =
                    " insert into retail_sales_product (" +
                    "master_id, " +
                    "company_id, " +
                    "product_id, " +
                    "retail_sales_id, " +
                    "product_count, " +
                    "product_price, " +
                    "product_sumprice, " +
                    "edizm_id, " +
                    "price_type_id, " +
                    "nds_id, " +
                    "department_id, " +
                    "product_price_of_type_price, " +
                    "reserved_current " +
                    ") values (" +
                    master_id + "," +
                    company_id + "," +
                    row.getProduct_id() + "," +
                    row.getCustomers_orders_id() + "," +
                    row.getProduct_count() + "," +
                    row.getProduct_price() + "," +
                    row.getProduct_sumprice() + "," +
                    row.getEdizm_id() + "," +
                    row.getPrice_type_id() + "," +
                    row.getNds_id() + ", " +
                    row.getDepartment_id() + ", " +
                    row.getProduct_price_of_type_price() +
                    " ) " +
                    "ON CONFLICT ON CONSTRAINT retail_sales_product_uq " +// "upsert"
                    " DO update set " +
                    " product_id = " + row.getProduct_id() + "," +
                    " retail_sales_id = " + row.getCustomers_orders_id() + "," +
                    " product_count = " + row.getProduct_count() + "," +
                    " product_price = " + row.getProduct_price() + "," +
                    " product_sumprice = " + row.getProduct_sumprice() + "," +
                    " edizm_id = " + row.getEdizm_id() + "," +
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
                logger.error("Exception in method saveRetailSalesProductTable. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        }
//        @SuppressWarnings("Duplicates")
//        private Boolean updateRetailSalesWithoutTable(RetailSalesForm request) {
//            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
//
//            String stringQuery;
//            stringQuery =   " update retail_sales set " +
//                    " changer_id = " + myId + ", "+
//                    " date_time_changed= now()," +
//                    " description = '" + (request.getDescription() == null ? "" : request.getDescription()) + "', " +
//                    " name = '" + (request.getName() == null ? "" : request.getName()) + "', " +
//                    " status_id = " + request.getStatus_id() +
//                    " where " +
//                    " id= "+request.getId();
//            try
//            {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                query.executeUpdate();
//                return true;
//            }catch (Exception e) {
//                logger.error("Exception in method updateRetailSalesWithoutTable. SQL query:"+stringQuery, e);
//                e.printStackTrace();
//                return false;
//            }
//        }
    /*

        //сохраняет настройки документа "Заказ покупателя"
        @SuppressWarnings("Duplicates")
        @Transactional
        public Boolean saveSettingsRetailSales(SettingsRetailSalesForm row) {
            String stringQuery="";
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId=userRepository.getUserId();
            try {
                stringQuery =
                        " insert into settings_retail_sales (" +
                                "master_id, " +
                                "company_id, " +
                                "user_id, " +
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
                                "name, "+               //наименование заказа
                                "autocreate_on_start , "+//автосоздание на старте документа, если автозаполнились все поля
                                "autocreate_on_cheque, "+//автосоздание нового документа, если в текущем успешно напечатан чек
                                "status_id_on_autocreate_on_cheque"+//Перед автоматическим созданием после успешного отбития чека документ сохраняется. Данный статус - это статус документа при таком сохранении
                                ") values (" +
                                myMasterId + "," +
                                row.getCompanyId() + "," +
                                myId + ",'" +
                                row.getPricingType() + "'," +
                                row.getPriceTypeId() + "," +
                                row.getChangePrice() + ",'" +
                                row.getPlusMinus() + "','" +
                                row.getChangePriceType() + "'," +
                                row.getHideTenths() + "," +
                                row.getSaveSettings() + "," +
                                row.getDepartmentId() + "," +
                                row.getCustomerId() + ",'"+
                                row.getPriorityTypePriceSide() + "',"+
                                "'" + (row.getName() == null ? "": row.getName()) + "', " +//наименование
                                row.getAutocreateOnStart()+ ", " +
                                row.getAutocreateOnCheque() +", " +
                                row.getStatusIdOnAutocreateOnCheque() +
                                ") " +
                                "ON CONFLICT ON CONSTRAINT settings_retail_sales_user_uq " +// "upsert"
                                " DO update set " +
                                " pricing_type = '" + row.getPricingType() + "',"+
                                " price_type_id = " + row.getPriceTypeId() + ","+
                                " change_price = " + row.getChangePrice() + ","+
                                " plus_minus = '" + row.getPlusMinus() + "',"+
                                " change_price_type = '" + row.getChangePriceType() + "',"+
                                " hide_tenths = " + row.getHideTenths() + ","+
                                " save_settings = " + row.getSaveSettings() + ","+
                                " department_id = " +row.getDepartmentId()  + ","+
                                " company_id = " +row.getCompanyId()  + ","+
                                " customer_id = "+row.getCustomerId()  + ","+
                                " name = '" +(row.getName() == null ? "": row.getName()) + "',"+
                                " priority_type_price_side = '"+row.getPriorityTypePriceSide()+"'," +
                                " autocreate_on_start = "+row.getAutocreateOnStart() + ","+
                                " status_id_on_autocreate_on_cheque = "+row.getStatusIdOnAutocreateOnCheque() + ","+
                                " autocreate_on_cheque = "+row.getAutocreateOnCheque();

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method saveSettingsRetailSales. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        }

        //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
        @SuppressWarnings("Duplicates")
        public SettingsRetailSalesJSON getSettingsRetailSales() {

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
                    "           coalesce(p.autocreate_on_start,false) as autocreate_on_start," +
                    "           coalesce(p.autocreate_on_cheque,false) as autocreate_on_cheque," +
                    "           p.name as name, " +
                    "           p.status_id_on_autocreate_on_cheque as status_id_on_autocreate_on_cheque " +
                    "           from settings_retail_sales p " +
                    "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
                    "           where p.user_id= " + myId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();

                SettingsRetailSalesJSON returnObj=new SettingsRetailSalesJSON();

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
                    returnObj.setAutocreateOnStart((Boolean)                obj[13]);
                    returnObj.setAutocreateOnCheque((Boolean)               obj[14]);
                    returnObj.setName((String)                              obj[15]);
                    returnObj.setStatusIdOnAutocreateOnCheque(obj[16]!=null?Long.parseLong(obj[16].toString()):null);
                }
                return returnObj;
            }
            catch (Exception e) {
                logger.error("Exception in method getSettingsRetailSales. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw e;
            }

        }

    //        //Отдает список приоритетных типов цен (для склада, покупателя и тип цена по умолчанию)
    //        @SuppressWarnings("Duplicates")
    //        public PriorityTypePricesJSON getPriorityTypePrices(Long company_id) {
    //
    //            String stringQuery;
    //            stringQuery = "select " +
    //                    "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id " +
    //                    "           coalesce((select price_type_id from cagents where id=p.company_id and is_default=true),0) as default_type_price_id " +
    //                    "           p.change_price as change_price, " +
    //                    "           p.plus_minus as plus_minus, " +
    //                    "           p.change_price_type as change_price_type, " +
    //                    "           p.hide_tenths as hide_tenths, " +
    //                    "           p.save_settings as save_settings, " +
    //                    "           p.department_id as department_id, " +
    //                    "           p.customer_id as customer_id, " +
    //                    "           cg.name as customer, " +
    //                    "           p.id as id, " +
    //                    "           p.company_id as company_id " +
    //                    "           from settings_retail_sales p " +
    //                    "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
    //                    "           where p.user_id= " + myId;
    //
    //            Query query = entityManager.createNativeQuery(stringQuery);
    //
    //            List<Object[]> queryList = query.getResultList();
    //
    //            PriorityTypePricesJSON returnObj=new PriorityTypePricesJSON();
    //
    //            for(Object[] obj:queryList){
    //                returnObj.setPricingType((String)                       obj[0]);
    //                returnObj.setPriceTypeId(obj[1]!=null?Long.parseLong(   obj[1].toString()):null);
    //                returnObj.setChangePrice((BigDecimal)                   obj[2]);
    //                returnObj.setPlusMinus((String)                         obj[3]);
    //                returnObj.setChangePriceType((String)                   obj[4]);
    //                returnObj.setHideTenths((Boolean)                       obj[5]);
    //                returnObj.setSaveSettings((Boolean)                     obj[6]);
    //                returnObj.setDepartmentId(obj[7]!=null?Long.parseLong(  obj[7].toString()):null);
    //                returnObj.setCustomerId(obj[8]!=null?Long.parseLong(    obj[8].toString()):null);
    //                returnObj.setCustomer((String)                          obj[9]);
    //                returnObj.setId(Long.parseLong(                         obj[10].toString()));
    //                returnObj.setCompanyId(Long.parseLong(                  obj[11].toString()));
    //            }
    //            return returnObj;
    //        }
    //
        //удаление 1 строки из таблицы товаров
        @SuppressWarnings("Duplicates")
        @Transactional
        public Boolean deleteRetailSalesProductTableRow(Long id) {
            if(canDeleteProductTableRow(id)){
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                String stringQuery;
                try {
                    stringQuery = " delete from retail_sales_product " +
                            " where id="+id+" and master_id="+myMasterId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    return query.executeUpdate() == 1;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        }

        @SuppressWarnings("Duplicates")
        // Нельзя удалять товарные позиции, по которым уже есть Отгрузка, т.к. товар могут сначала отгрузить, а потом завершить (закрыть) Отгрузку.
        // И если между этими 2 действиями удалить товарную позицию, товар физически уйдет со склада, но будет числисться в системе.
        private boolean canDeleteProductTableRow(Long row_id){
    //        !!!!!!!!!!!!!!!!!!!!!!!!!  тут вставить проверку того, есть ли данная позиция в Отгрузке
            return true;
        }

        @SuppressWarnings("Duplicates")//  удаляет лишние позиции товаров при сохранении заказа (те позиции, которые ранее были в заказе, но потом их удалили)
        private Boolean deleteRetailSalesProductTableExcessRows(String productIds, Long retail_sales_id) {
            String stringQuery="";
            try {
                stringQuery =   " delete from retail_sales_product " +
                        " where retail_sales_id=" + retail_sales_id +
                        " and product_id not in (" + productIds + ")";
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method deleteRetailSalesProductTableExcessRows. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        }
        @SuppressWarnings("Duplicates")
        public List<RetailSalesReservesTable> getReservesTable(Long companyId, Long departmentId, Long productId, Long documentId) {
            if(securityRepositoryJPA.userHasPermissions_OR(25L, "315,316,317"))//(см. файл Permissions Id)
            {
                String stringQuery;
                String myTimeZone = userRepository.getUserTimeZone();
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

                stringQuery = "select  p.id as id, " +
                        "           u.name as creator, " +
                        "           p.name as custome_order_name, " +
                        "           (coalesce(cop.reserved_current,0)-0) as non_shipped, " + // !!!!!!!!!!!!!!!!!!!!!!!!! ноль потом заменить на вычисленное отгруженное по всем Отгрузкам данного Заказа покупателя
                        "           p.doc_number as doc_number, " +
                        "           to_char(p.shipment_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as shipment_date, " +
                        "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                        "           p.description as description, " +
                        "           coalesce(p.is_completed,false) as is_completed, " +
                        "           stat.name as status_name, " +
                        "           stat.color as status_color, " +
                        "           cg.name as cagent, " +
                        "           p.is_deleted as is_deleted, " +
                        "           dp.name as department, " +
                        "           p.date_time_created as date_time_created_sort " +
                        "           from retail_sales p " +
                        "           INNER JOIN users u ON p.creator_id=u.id " +
                        "           INNER JOIN retail_sales_product cop on p.id=cop.retail_sales_id" +
                        "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                        "           INNER JOIN departments dp ON p.department_id=dp.id " +
                        "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                        "           where " +
                        "           p.company_id=" + companyId +
                        "           and cop.product_id=" + productId +
                        "           and p.master_id=" + myMasterId +
                        "           and coalesce(cop.reserved_current,0)-0 > 0"; // !!!!!!!!!!!!!!!!!!!!!!!!! ноль потом заменить на вычисленное отгруженное по всем Отгрузкам данного Заказа покупателя
                if(departmentId>0L){
                    stringQuery=stringQuery +" and cop.department_id=" + departmentId;
                }
                if(documentId>0L){
                    stringQuery=stringQuery +" and p.id !=" + documentId;
                }
                stringQuery = stringQuery + "  order by date_time_created_sort asc";
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                List<RetailSalesReservesTable> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    RetailSalesReservesTable doc=new RetailSalesReservesTable();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setCreator((String)                       obj[1]);
                    doc.setName((String)                          obj[2]);
                    doc.setNon_shipped((BigDecimal)               obj[3]);
                    doc.setDoc_number(Long.parseLong(             obj[4].toString()));
                    doc.setShipment_date((String)(                obj[5]));
                    doc.setDate_time_created((String)             obj[6]);
                    doc.setDescription((String)                   obj[7]);
                    doc.setIs_completed((Boolean)                 obj[8]);
                    doc.setStatus_name((String)                   obj[9]);
                    doc.setStatus_color((String)                  obj[10]);
                    doc.setCagent((String)                        obj[11]);
                    doc.setIs_deleted((Boolean)                   obj[12]);
                    doc.setDepartment((String)                    obj[13]);
                    returnList.add(doc);
                }
                return returnList;
            } else return null;
        }





        /*
            @SuppressWarnings("Duplicates")
            private Boolean addRetailSalesProductHistory(RetailSalesProductForm row, RetailSalesForm request , Long masterId) {
                String stringQuery;
                ProductHistoryJSON lastProductHistoryRecord =  getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
                BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
                BigDecimal lastAvgPurchasePrice= lastProductHistoryRecord.getAvg_purchase_price();
                BigDecimal lastAvgNetcostPrice= lastProductHistoryRecord.getAvg_netcost_price();
                BigDecimal lastPurchasePrice= lastProductHistoryRecord.getLast_purchase_price();

                try {
                    stringQuery =
                            " insert into products_history (" +
                                    " master_id," +
                                    " company_id," +
                                    " department_id," +
                                    " doc_type_id," +
                                    " doc_id," +
                                    " product_id," +
                                    " quantity," +
                                    " change," +
                                    " avg_purchase_price," +
                                    " avg_netcost_price," +
                                    " last_purchase_price," +
                                    " last_operation_price," +
                                    " date_time_created"+
                                    ") values ("+
                                    masterId +","+
                                    request.getCompany_id() +","+
                                    request.getDepartment_id() + ","+
                                    21 +","+
                                    row.getCustomers_orders_id() + ","+
                                    row.getProduct_id() + ","+
                                    lastQuantity.subtract(row.getProduct_count())+","+
                                    row.getProduct_count().multiply(new BigDecimal(-1)) +","+
                                    lastAvgPurchasePrice +","+
                                    lastAvgNetcostPrice +","+
                                    lastPurchasePrice+","+
                                    row.getProduct_price()+","+
                                    " now())";
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        */
/*
    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(RetailSalesProductForm row, RetailSalesForm request , Long masterId) {
        String stringQuery;
        ProductHistoryJSON lastProductHistoryRecord =  getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
        BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();

        try {
            stringQuery =
                    " insert into product_quantity (" +
                            " master_id," +
                            " department_id," +
                            " product_id," +
                            " quantity" +
                            ") values ("+
                            masterId + ","+
                            request.getDepartment_id() + ","+
                            row.getProduct_id() + ","+
                            lastQuantity +
                            ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert"
                            " DO update set " +
                            " department_id = " + request.getDepartment_id() + ","+
                            " product_id = " + row.getProduct_id() + ","+
                            " master_id = "+ masterId + "," +
                            " quantity = "+ lastQuantity;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
*/
/*     @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteRetailSales (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(25L,"312") && securityRepositoryJPA.isItAllMyMastersDocuments("retail_sales",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(25L,"313") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("retail_sales",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(25L,"314") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("retail_sales",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update retail_sales p" +
                    " set is_deleted=true, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")";
//                    " and coalesce(p.is_completed,false) !=true";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method deleteRetailSales. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean undeleteRetailSales(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(25L,"312") && securityRepositoryJPA.isItAllMyMastersDocuments("retail_sales",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(25L,"313") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("retail_sales",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(25L,"314") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("retail_sales",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update retail_sales p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return true;
                } else return false;
            }catch (Exception e) {
                logger.error("Exception in method undeleteRetailSales. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
    @SuppressWarnings("Duplicates")
    //отдает список отделений в виде их Id с зарезервированным количеством и общим количеством товара в отделении
    public List<IdAndCount> getProductCount(Long product_id, Long company_id, Long document_id) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery = "select" +
                " d.id as id, " +
                " (select coalesce(quantity,0) from product_quantity where department_id = d.id and product_id = "+product_id+") as total, " +
                " (select " +
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                "   from " +
                "   retail_sales_product " +
                "   where " +
                "   product_id="+product_id+
                "   and department_id = d.id "+
                "   and retail_sales_id!="+document_id+") as reserved "+//зарезервировано в других документах Заказ покупателя

                " from" +
                " departments d " +
                " where" +
                " d.company_id= " + company_id +
                " and d.master_id= " + myMasterId +
                " and coalesce(d.is_deleted,false)=false";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<IdAndCount> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                IdAndCount product = new IdAndCount();
                product.setId(Long.parseLong(              obj[0].toString()));
                product.setTotal(                          obj[1]==null?BigDecimal.ZERO:(BigDecimal)obj[1]);
                product.setReserved(                       obj[2]==null?BigDecimal.ZERO:(BigDecimal)obj[2]);
                returnList.add(product);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    //отдает информацию о состоянии товара(кол-во всего, зарезервировано и цена) по его id ,в отделении по его id, по цене по её id, для документа Заказ покупателя по его id
    public ProductsPriceAndRemainsJSON getProductsPriceAndRemains(Long department_id, Long product_id, Long price_type_id, Long document_id) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
//        String myDepthsIds = userRepositoryJPA.getMyDepartmentsId().toString().replace("[","").replace("]","");
        //себестоимость
        ProductHistoryJSON lastProductHistoryRecord =  writeoffRepository.getLastProductHistoryRecord(product_id,department_id);
        BigDecimal netCost= lastProductHistoryRecord.getAvg_netcost_price();

        String stringQuery = "select" +
                " coalesce((select quantity from product_quantity where product_id = "+product_id+" and department_id = d.id),0) as total, "+ //всего на складе (т.е остаток)
                " (select " +
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                "   from " +
                "   retail_sales_product " +
                "   where " +
                "   product_id="+product_id+
                "   and department_id = d.id "+
                "   and retail_sales_id!="+document_id+") as reserved ";//зарезервировано в других документах Заказ покупателя
        if(price_type_id!=0) {//если тип цены был выбран
            stringQuery=stringQuery+", coalesce((select price_value from product_prices where product_id = " + product_id + " and price_type_id = " + price_type_id + " and company_id = d.company_id),0) as price ";// цена по типу цены
        }
        stringQuery=stringQuery+" from" +
                " departments d " +
                " where" +
                " d.id= " + department_id +
                " and d.master_id= " + myMasterId;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            ProductsPriceAndRemainsJSON returnObj = new ProductsPriceAndRemainsJSON();

            for (Object[] obj : queryList) {
                returnObj.setTotal(                                         obj[0]==null?BigDecimal.ZERO:(BigDecimal)obj[0]);
                returnObj.setReserved(                                      obj[1]==null?BigDecimal.ZERO:(BigDecimal)obj[1]);
                if (price_type_id != 0) {
                    returnObj.setPrice((BigDecimal)                         obj[2]);
                } else {
                    returnObj.setPrice(                      BigDecimal.valueOf(0));
                }
                returnObj.setNetCost(netCost);
            }
            return returnObj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//*****************************************************************************************************************************************************
//***************************************************      UTILS      *********************************************************************************
//*****************************************************************************************************************************************************

*/
    @SuppressWarnings("Duplicates")  //генератор номера документа
    private Long generateDocNumberCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from retail_sales where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            return 0L;
        }
    }
/*
    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isRetailSalesNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long doc_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from retail_sales where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(doc_id>0) stringQuery=stringQuery+" and id !="+doc_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.getResultList().size()>0)
                return false;// код не уникальный
            else return true; // код уникальный
        }
        catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
    */
}
class CantInsertProductRowException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert retail_sales_products table row because of available < product_count");
    }
}