/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.repository;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsCustomersOrdersForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsCustomersOrdersJSON;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.message.response.ProductHistoryJSON;
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
public class CustomersOrdersRepositoryJPA {

    Logger logger = Logger.getLogger("CustomersOrdersRepositoryJPA");

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
//    @Autowired
//    DepartmentRepositoryJPA departmentRepositoryJPA;
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
    public List<CustomersOrdersJSON> getCustomersOrdersTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(23L, "287,288,289,290"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            boolean needToSetParameter_MyDepthsIds = false;
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
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
                    "           to_char(p.shipment_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as shipment_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.shipment_date as shipment_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from customers_orders_product where customers_orders_id=p.id),0) as sum_price, " +
                    "           p.name as name " +
//                    "           cnt.name_ru, ' ', reg.name_ru, ' ', cty.name_ru, ' ',
                    "           from customers_orders p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(23L, "287")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(23L, "288")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(23L, "289")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
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
            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<CustomersOrdersJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                CustomersOrdersJSON doc=new CustomersOrdersJSON();
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
                doc.setShipment_date((String)(                obj[11]));
                doc.setCompany((String)                       obj[12]);
                doc.setDate_time_created((String)             obj[13]);
                doc.setDate_time_changed((String)             obj[14]);
                doc.setDescription((String)                   obj[15]);
                doc.setIs_completed((Boolean)                 obj[16]);
                doc.setStatus_id(obj[20]!=null?Long.parseLong(obj[20].toString()):null);
                doc.setStatus_name((String)                   obj[21]);
                doc.setStatus_color((String)                  obj[22]);
                doc.setStatus_description((String)            obj[23]);
                doc.setSum_price((BigDecimal)                 obj[24]);
                doc.setName((String)                          obj[25]);

                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getCustomersOrdersSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
//        if(securityRepositoryJPA.userHasPermissions_OR(23L, "287,288,289,290"))//(см. файл Permissions Id)
//        {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from customers_orders p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(23L, "287")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(23L, "288")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(23L, "289")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
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
        Query query = entityManager.createNativeQuery(stringQuery);

        if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
        {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

        return query.getResultList().size();
//        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    public List<CustomersOrdersProductTableJSON> getCustomersOrdersProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(23L, "287,288,289,290"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            String myDepthsIds = userRepositoryJPA.getMyDepartmentsId().toString().replace("[","").replace("]","");
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.customers_orders_id," +
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
                    "   from " +
                    "   customers_orders_product " +
                    "   where " +
                    "   product_id=ap.product_id "+
                    "   and department_id = ap.department_id "+
                    "   and customers_orders_id!=ap.customers_orders_id) as reserved, "+//зарезервировано в других документах Заказ покупателя
                    "   0 as shipped, "+//!!!!!!!!!!!!!!!!!!!!!!!!пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                    " ap.department_id as department_id, " +
                    " (select name from departments where id= ap.department_id) as department, "+
                    " ap.id  as row_id, " +
                    " ppr.name_api_atol as ppr_name_api_atol, " +
                    " ppr.is_material as is_material, " +
                    " coalesce(ap.reserved_current,0) as reserved_current " +//зарезервировано в данном документе Заказ покупателя
                    " from " +
                    " customers_orders_product ap " +
                    " INNER JOIN customers_orders a ON ap.customers_orders_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
//                    " INNER JOIN sprav_sys_nds nds ON p.nds_id=nds.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.customers_orders_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(23L, "287")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(23L, "288")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(23L, "289")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<CustomersOrdersProductTableJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                CustomersOrdersProductTableJSON doc=new CustomersOrdersProductTableJSON();
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
                doc.setShipped(BigDecimal.valueOf((Integer)             obj[14]));//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                doc.setDepartment_id(Long.parseLong(                    obj[15].toString()));
                doc.setDepartment((String)                              obj[16]);
                doc.setId(Long.parseLong(                               obj[17].toString()));
                doc.setPpr_name_api_atol((String)                       obj[18]);
                doc.setIs_material((Boolean)                            obj[19]);
                doc.setReserved_current(                                obj[20]==null?BigDecimal.ZERO:(BigDecimal)obj[20]);

                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }
//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public CustomersOrdersJSON getCustomersOrdersValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(23L, "287,288,289,290"))//см. _Permissions Id.txt
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
                    "           to_char(p.shipment_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as shipment_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           coalesce(dp.price_id,0) as department_type_price_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.shipment_date as shipment_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.name as name, " +
                    "           p.status_id as status_id, " +
                    "           p.fio as fio, " +
                    "           p.email as email, " +
                    "           p.telephone as telephone, " +
                    "           p.zip_code as zip_code, " +
                    "           p.country_id as country_id, " +
                    "           p.region_id as region_id, " +
                    "           p.city_id as city_id, " +
                    "           p.additional_address as additional_address, " +
                    "           p.track_number as track_number, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.street as street, " +
                    "           p.home as home, " +
                    "           p.flat as flat, " +
                    "           coalesce(ctr.name_ru,'') as country, " +
                    "           coalesce(reg.name_ru,'') as region, " +
                    "           coalesce(cty.name_ru,'') as city, " +
                    "           coalesce(cty.area_ru,'') as area, " +
                    "           coalesce(cg.price_type_id,0) as cagent_type_price_id, " +
                    "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id " +
                    "           from customers_orders p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           LEFT OUTER JOIN sprav_sys_countries ctr ON p.country_id=ctr.id" +
                    "           LEFT OUTER JOIN sprav_sys_regions reg ON p.region_id=reg.id" +
                    "           LEFT OUTER JOIN sprav_sys_cities cty ON p.city_id=cty.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(23L, "287")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(23L, "288")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(23L, "289")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            CustomersOrdersJSON returnObj=new CustomersOrdersJSON();

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
                returnObj.setShipment_date((String)(                    obj[11]));
                returnObj.setCompany((String)                           obj[12]);
                returnObj.setDate_time_created((String)                 obj[13]);
                returnObj.setDate_time_changed((String)                 obj[14]);
                returnObj.setDescription((String)                       obj[15]);
                returnObj.setIs_completed((Boolean)                     obj[16]);
                returnObj.setDepartment_type_price_id(Long.parseLong(   obj[17].toString()));
                returnObj.setNds((Boolean)                              obj[18]);
                returnObj.setNds_included((Boolean)                     obj[19]);
                returnObj.setCagent_id(Long.parseLong(                  obj[20].toString()));
                returnObj.setCagent((String)                            obj[21]);
                returnObj.setName((String)                              obj[25]);
                returnObj.setStatus_id(obj[26]!=null?Long.parseLong(    obj[26].toString()):null);
                returnObj.setFio((String)                               obj[27]);
                returnObj.setEmail((String)                             obj[28]);
                returnObj.setTelephone((String)                         obj[29]);
                returnObj.setZip_code((String)                          obj[30]);
                returnObj.setCountry_id((Integer)                       obj[31]);
                returnObj.setRegion_id((Integer)                        obj[32]);
                returnObj.setCity_id((Integer)                          obj[33]);
                returnObj.setAdditional_address((String)                obj[34]);
                returnObj.setTrack_number((String)                      obj[35]);
                returnObj.setStatus_name((String)                       obj[36]);
                returnObj.setStatus_color((String)                      obj[37]);
                returnObj.setStatus_description((String)                obj[38]);
                returnObj.setStreet((String)                            obj[39]);
                returnObj.setHome((String)                              obj[40]);
                returnObj.setFlat((String)                              obj[41]);
                returnObj.setCountry((String)                           obj[42]);
                returnObj.setRegion((String)                            obj[43]);
                returnObj.setCity((String)                              obj[44]);
                returnObj.setArea((String)                              obj[45]);
                returnObj.setCagent_type_price_id(Long.parseLong(       obj[46].toString()));
                returnObj.setDefault_type_price_id(Long.parseLong(      obj[47].toString()));
            }
            return returnObj;
        } else return null;
    }




    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertCustomersOrders(CustomersOrdersForm request) {

        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Long dockDepartment=request.getDepartment_id();
        List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
        boolean itIsMyDepartment = myDepartmentsIds.contains(dockDepartment);
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.



        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        try{

            if ((//если есть право на создание по всем предприятиям, или
            (securityRepositoryJPA.userHasPermissions_OR(23L, "280")) ||
            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
            (securityRepositoryJPA.userHasPermissions_OR(23L, "281") && myCompanyId.equals(request.getCompany_id())) ||
            //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
            (securityRepositoryJPA.userHasPermissions_OR(23L, "282") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
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
                        cagentForm.setTelephone(request.getTelephone());
                        cagentForm.setEmail((request.getEmail()));
                        cagentForm.setZip_code(request.getZip_code());
                        cagentForm.setCountry_id(request.getCountry_id());
                        cagentForm.setRegion_id(request.getRegion_id());
                        cagentForm.setCity_id(request.getCity_id());
                        cagentForm.setStreet(request.getStreet());
                        cagentForm.setHome(request.getHome());
                        cagentForm.setFlat(request.getFlat());
                        cagentForm.setAdditional_address(request.getAdditional_address());
                        request.setCagent_id(cagentRepository.insertCagent(cagentForm));
                    }
                    catch (Exception e) {
                        logger.error("Exception in method insertCustomersOrders on creating Cagent.", e);
                        e.printStackTrace();
                        return null;
                    }
                }


                String timestamp = new Timestamp(System.currentTimeMillis()).toString();

                stringQuery =   "insert into customers_orders (" +
                " master_id," + //мастер-аккаунт
                " creator_id," + //создатель
                " company_id," + //предприятие, для которого создается документ
                " department_id," + //отделение, из(для) которого создается документ
                " cagent_id," +//контрагент
                " date_time_created," + //дата и время создания
                " doc_number," + //номер заказа
                " name," + //наименование заказа
                " description," +//доп. информация по заказу
                " shipment_date," +//план. дата отгрузки
                " nds," +// НДС
                " nds_included," +// НДС включен в цену
                " telephone,"+//телефон
                " email,"+//емейл
                " zip_code,"+// почтовый индекс
                " country_id,"+//страна
                " region_id,"+//область
                " city_id,"+//город/нас.пункт
                " street,"+//улица
                " home,"+//дом
                " flat,"+//квартира
                " additional_address,"+//дополнение к адресу
                " track_number," + //трек-номер отправленного заказа
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
                ((request.getShipment_date()!=null&& !request.getShipment_date().equals(""))?" to_date('"+request.getShipment_date()+"','DD.MM.YYYY'),":"'',")+//план. дата отгрузки
                request.isNds() + ", "+// НДС
                request.isNds_included() + ", "+// НДС включен в цену
                "'" + (request.getTelephone() == null ? "": request.getTelephone()) +"', " +//телефон
                "'" + (request.getEmail() == null ? "": request.getEmail()) +"', " +//емейл
                "'" + (request.getZip_code() == null ? "": request.getZip_code()) +"', " +//почтовый индекс
                request.getCountry_id() + ", " +//страна
                request.getRegion_id() + ", " +//область
                request.getCity_id() + ", " +//город/нас.пункт
                "'" + (request.getStreet() == null ? "": request.getStreet()) +"', " +//улица
                "'" + (request.getHome() == null ? "": request.getHome()) +"', " +//дом
                "'" + (request.getFlat() == null ? "": request.getFlat()) +"', " +//квартира
                "'" + (request.getAdditional_address() == null ? "": request.getAdditional_address()) +"', " +//дополнение к адресу
                "'" + (request.getTrack_number() == null ? "": request.getTrack_number()) + "', " +//трек-номер отправленного заказа
                request.getStatus_id() + ")";//статус заказа

                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    stringQuery="select id from customers_orders where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDockId=Long.valueOf(query2.getSingleResult().toString());
                    return newDockId;
                } catch (Exception e) {
                    logger.error("Exception in method insertCustomersOrders on inserting into customers_orders. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public CustomersOrdersUpdateReportJSON updateCustomersOrders(CustomersOrdersForm request)  throws Exception{
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(23L,"291") && securityRepositoryJPA.isItAllMyMastersDocuments("customers_orders",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(23L,"292") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("customers_orders",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(23L,"293") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("customers_orders",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(23L,"294") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("customers_orders",request.getId().toString())))
        {
            CustomersOrdersUpdateReportJSON updateResults = new CustomersOrdersUpdateReportJSON();// отчет об апдейте
            Integer updateProductRowResult; // отчет о сохранении позиции товара (строки таблицы). 0- успешно с сохранением вкл. резерва. 1 - включенный резерв не был сохранён
            updateResults.setFail_to_reserve(0);// иначе NullPointerException, т.к. там сейчас null
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            if(updateCustomersOrdersWithoutTable(request)){               //метод 1 - сохранение всех полей документа
                try {//сохранение таблицы
                    String productIds = "";
                    if (request.getCustomersOrdersProductTable()!=null && request.getCustomersOrdersProductTable().size() > 0) {
                        for (CustomersOrdersProductTableForm row : request.getCustomersOrdersProductTable()) {
                            updateProductRowResult = saveCustomersOrdersProductTable(row, request.getCompany_id(), myMasterId);  //метод 2 - сохранение таблицы товаров
                            if (updateProductRowResult==null) {//
                                throw new Exception("Can't update Customers orders table row!");
                            }
                            //если при сохранении позиции товара не удалось сохранить включенным резерв - добавляем этот случай к сумме таких случаев по всем позициям (для отчета о сохранении)
                            updateResults.setFail_to_reserve((updateResults.getFail_to_reserve()+updateProductRowResult));
                            //копим id сохранённых товаров
                            productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
                        }
                    }//удаление лишних
                    if (productIds.length()>0) {
                        deleteCustomersOrdersProductTableExcessRows(productIds, request.getId());
                    }


                    updateResults.setSuccess(true);
                    return updateResults;
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            } else return null;
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    private Integer saveCustomersOrdersProductTable(CustomersOrdersProductTableForm row, Long company_id, Long master_id) {
        String stringQuery="";
        Integer saveResult=0;   // 0 - если был резерв - он сохранился, 1 - если был резерв - он отменился. (это относится только к вновь поставленным резервам) Если резерв уже был выставлен - он не отменится.
        BigDecimal available;   // Если есть постановка в резерв - узнаём, есть ли свободные товары (пока мы редактировали таблицу, кто-то мог поставить эти же товары в свой резерв, и чтобы
        BigDecimal reserved_current = row.getReserved_current()==null?new BigDecimal(0):row.getReserved_current(); // зарезервированное количество товара
        try {
            //Проверка на то, чтобы зарезервированное кличество товара не превышало заказанное количество товара (графа Кол-во)
            if(reserved_current.compareTo(row.getProduct_count()) > 0) { //1, т.е. резерв превышает заказываемое количество товара
                row.setReserved_current(new BigDecimal(0));// отменяем резерв, т.к. он превышает заказываемое количество товара
                saveResult = 1;
            } else { // резерв НЕ превышает заказываемое количество товара. Тогда проверим еще на то, что резерв не превышает доступное количество товара.
                // Данная проверка нужна, чтобы сумма резервов по складу из всех "Заказов покупателя" не превышала общее количество товара на складе.
                // Проверки в 2 захода делается чтобы не делать лишний запрос - если в первом случае уже выявлено нарушение - лишнего запроса к базе для вычисления доступного количество товара на складе не будет
                //вычисляем доступное количество товара на складе
                available = productsRepository.getAvailableExceptMyDock(row.getProduct_id(), row.getDepartment_id(), row.getCustomers_orders_id());
                if (row.getReserved_current().compareTo(available) > 0) {
                    row.setReserved_current(new BigDecimal(0));// и если превышает - резерв отменяется
                    saveResult = 1;
                }
            }

            stringQuery =
                    " insert into customers_orders_product (" +
                            "master_id, " +
                            "company_id, " +
                            "product_id, " +
                            "customers_orders_id, " +
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
                            row.getProduct_price_of_type_price() + ", " +
                            row.getReserved_current() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT customers_orders_product_uq " +// "upsert"
                            " DO update set " +
                            " product_id = " + row.getProduct_id() + ","+
                            " customers_orders_id = " + row.getCustomers_orders_id() + ","+
                            " product_count = " + row.getProduct_count() + ","+
                            " product_price = " + row.getProduct_price() + ","+
                            " product_sumprice = " + row.getProduct_sumprice() + ","+
                            " edizm_id = " + row.getEdizm_id() + ","+
                            " price_type_id = " + row.getPrice_type_id() + ","+
                            " nds_id = " + row.getNds_id() + ","+
                            " department_id = " + row.getDepartment_id() + ","+
                            " product_price_of_type_price = " + row.getProduct_price_of_type_price() + ","+
                            " reserved_current = " + row.getReserved_current();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return saveResult;
        }
        catch (Exception e) {
            logger.error("Exception in method saveCustomersOrdersProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    private Boolean updateCustomersOrdersWithoutTable(CustomersOrdersForm request) {
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update customers_orders set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = '" + (request.getDescription() == null ? "" : request.getDescription()) + "', " +
                    " shipment_date = to_date('" + (request.getShipment_date() == "" ? null :request.getShipment_date()) + "','DD.MM.YYYY'), " + // иначе дата будет 01-01-0001
                    " nds  = " + request.isNds() + ", " +
                    " nds_included  = " + request.isNds_included() + ", " +
                    " cagent_id  = " + request.getCagent_id() + ", " +
                    " doc_number = " + request.getDoc_number() + ", " +
                    " name = '" + (request.getName() == null ? "" : request.getName()) + "', " +
                    " email = '" + (request.getEmail() == null ? "" : request.getEmail()) + "', " +
                    " telephone = '" + (request.getTelephone() == null ? "" : request.getTelephone()) + "', " +
                    " zip_code = '" + (request.getZip_code() == null ? "" : request.getZip_code()) + "', " +
                    " country_id  = " + request.getCountry_id() + ", " +
                    " region_id  = " + request.getRegion_id() + ", " +
                    " city_id  = " + request.getCity_id() + ", " +
                    " street = '" + (request.getStreet() == null ? "" : request.getStreet()) + "', " +
                    " home = '" + (request.getHome() == null ? "" : request.getHome()) + "', " +
                    " flat = '" + (request.getFlat() == null ? "" : request.getFlat()) + "', " +
                    " additional_address = '" + (request.getAdditional_address() == null ? "" : request.getAdditional_address()) + "', " +
                    " track_number = '" + (request.getTrack_number() == null ? "" : request.getTrack_number()) + "', " +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method updateCustomersOrdersWithoutTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }


    //сохраняет настройки документа "Заказ покупателя"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsCustomersOrders(SettingsCustomersOrdersForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into settings_customers_orders (" +
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
                    "ON CONFLICT ON CONSTRAINT settings_customers_orders_user_uq " +// "upsert"
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
            logger.error("Exception in method saveSettingsCustomersOrders. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsCustomersOrdersJSON getSettingsCustomersOrders() {

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
                    "           from settings_customers_orders p " +
                    "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
                    "           where p.user_id= " + myId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();

                SettingsCustomersOrdersJSON returnObj=new SettingsCustomersOrdersJSON();

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
                logger.error("Exception in method getSettingsCustomersOrders. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw e;
            }

    }

/*    //Отдает список приоритетных типов цен (для склада, покупателя и тип цена по умолчанию)
    @SuppressWarnings("Duplicates")
    public PriorityTypePricesJSON getPriorityTypePrices(Long company_id) {

        String stringQuery;
        stringQuery = "select " +
                "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id " +
                "           coalesce((select price_type_id from cagents where id=p.company_id and is_default=true),0) as default_type_price_id " +
                "           p.change_price as change_price, " +
                "           p.plus_minus as plus_minus, " +
                "           p.change_price_type as change_price_type, " +
                "           p.hide_tenths as hide_tenths, " +
                "           p.save_settings as save_settings, " +
                "           p.department_id as department_id, " +
                "           p.customer_id as customer_id, " +
                "           cg.name as customer, " +
                "           p.id as id, " +
                "           p.company_id as company_id " +
                "           from settings_customers_orders p " +
                "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
                "           where p.user_id= " + myId;

        Query query = entityManager.createNativeQuery(stringQuery);

        List<Object[]> queryList = query.getResultList();

        PriorityTypePricesJSON returnObj=new PriorityTypePricesJSON();

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
        }
        return returnObj;
    }
*/
    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean deleteCustomersOrdersProductTableRow(Long id) {
        if(canDeleteProductTableRow(id)){
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            try {
                stringQuery = " delete from customers_orders_product " +
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
    private Boolean deleteCustomersOrdersProductTableExcessRows(String productIds, Long customers_orders_id) {
        String stringQuery="";
        try {
            stringQuery =   " delete from customers_orders_product " +
                    " where customers_orders_id=" + customers_orders_id +
                    " and product_id not in (" + productIds + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteCustomersOrdersProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    public List<CustomersOrdersReservesTable> getReservesTable(Long companyId, Long departmentId, Long productId, Long documentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(23L, "287,288,289,290"))//(см. файл Permissions Id)
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
                    "           from customers_orders p " +
                    "           INNER JOIN users u ON p.creator_id=u.id " +
                    "           INNER JOIN customers_orders_product cop on p.id=cop.customers_orders_id" +
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
            List<CustomersOrdersReservesTable> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                CustomersOrdersReservesTable doc=new CustomersOrdersReservesTable();
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
    private Boolean addCustomersOrdersProductHistory(CustomersOrdersProductForm row, CustomersOrdersForm request , Long masterId) {
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
    private Boolean setProductQuantity(CustomersOrdersProductForm row, CustomersOrdersForm request , Long masterId) {
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
    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCustomersOrders (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(23L,"283") && securityRepositoryJPA.isItAllMyMastersDocuments("customers_orders",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(23L,"284") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("customers_orders",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(23L,"285") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("customers_orders",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(23L,"286") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("customers_orders",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update customers_orders p" +
                    " set is_deleted=true, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")";
//                    " and coalesce(p.is_completed,false) !=true";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method deleteCustomersOrders. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean undeleteCustomersOrders(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(23L,"283") && securityRepositoryJPA.isItAllMyMastersDocuments("customers_orders",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(23L,"284") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("customers_orders",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(23L,"285") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("customers_orders",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(23L,"286") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("customers_orders",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update customers_orders p" +
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
                logger.error("Exception in method undeleteCustomersOrders. SQL query:"+stringQuery, e);
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
                "   customers_orders_product " +
                "   where " +
                "   product_id="+product_id+
                "   and department_id = d.id "+
                "   and customers_orders_id!="+document_id+") as reserved "+//зарезервировано в других документах Заказ покупателя

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
                                "   customers_orders_product " +
                                "   where " +
                                "   product_id="+product_id+
                                "   and department_id = d.id "+
                                "   and customers_orders_id!="+document_id+") as reserved ";//зарезервировано в других документах Заказ покупателя
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
/*    @SuppressWarnings("Duplicates")  // возвращает значения из последней строки истории изменений товара
    private ProductHistoryJSON getLastProductHistoryRecord(Long product_id, Long department_id)
    {
        String stringQuery;
        stringQuery =
                " select                                        "+
                        " last_purchase_price   as last_purchase_price, "+
                        " avg_purchase_price    as avg_purchase_price,  "+
                        " avg_netcost_price     as avg_netcost_price,   "+
                        " last_operation_price  as last_operation_price,"+
                        " quantity              as quantity,            "+
                        " change                as change               "+
                        "          from products_history                "+
                        "          where                                "+
                        "          product_id="+product_id+" and        "+
                        "          department_id="+department_id         +
                        "          order by id desc limit 1             ";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            ProductHistoryJSON returnObj=new ProductHistoryJSON();
            if(queryList.size()==0){//если записей истории по данному товару ещё нет
                returnObj.setLast_purchase_price(       (new BigDecimal(0)));
                returnObj.setAvg_purchase_price(        (new BigDecimal(0)));
                returnObj.setAvg_netcost_price(         (new BigDecimal(0)));
                returnObj.setLast_operation_price(      (new BigDecimal(0)));
                returnObj.setQuantity(                  (new BigDecimal(0)));
            }else {
                for (Object[] obj : queryList) {
                    returnObj.setLast_purchase_price((BigDecimal)   obj[0]);
                    returnObj.setAvg_purchase_price((BigDecimal)    obj[1]);
                    returnObj.setAvg_netcost_price((BigDecimal)     obj[2]);
                    returnObj.setLast_operation_price((BigDecimal)  obj[3]);
                    returnObj.setQuantity((BigDecimal)              obj[4]);
                }
            }
            return returnObj;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
*/


    @SuppressWarnings("Duplicates")  //генератор номера документа
    private Long generateDocNumberCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from customers_orders where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isCustomersOrdersNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long doc_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from customers_orders where " +
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
/*
    @SuppressWarnings("Duplicates") //удаление строки с товаром перед перезаписью
    private Boolean clearCustomersOrdersProductTable(Long product_id, Long customers_orders_id) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = " delete from " +
                " customers_orders_product where " +
                "product_id="+product_id+
                " and customers_orders_id="+customers_orders_id +
                " and (select master_id from customers_orders where id="+customers_orders_id+")="+myMasterId;
        try
        {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
*/
//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************
/* Пока не понятно, нужно ли прицепление файлов к заказу клиента. Если да, то что там вообще может быть?
   Поэтому блок пока закомментирован и функционал не реализован

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToCustomersOrders(UniversalForm request){
        Long shipmentId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(23L,"291") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment",shipmentId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(23L,"292") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",shipmentId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(23L,"293") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",shipmentId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(23L,"294") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment",shipmentId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select shipment_id from shipment_files where shipment_id=" + shipmentId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_CustomersOrdersId_FileId(shipmentId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_CustomersOrdersId_FileId(Long shipmentId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into shipment_files " +
                    "(shipment_id,file_id) " +
                    "values " +
                    "(" + shipmentId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesCustomersOrdersJSON> getListOfCustomersOrdersFiles(Long shipmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(23L, "287,288"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           shipment p" +
                    "           inner join" +
                    "           shipment_files pf" +
                    "           on p.id=pf.shipment_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + shipmentId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(23L, "287")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(23L, "288")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(23L, "289")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            List<FilesCustomersOrdersJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                FilesCustomersOrdersJSON doc=new FilesCustomersOrdersJSON();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setDate_time_created((Timestamp)                    obj[1]);
                doc.setName((String)                                    obj[2]);
                doc.setOriginal_name((String)                           obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCustomersOrdersFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(23L,"291") && securityRepositoryJPA.isItAllMyMastersDocuments("shipment", String.valueOf(request.getId()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(23L,"292") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("shipment",String.valueOf(request.getId())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(23L,"293") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("shipment",String.valueOf(request.getId())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(23L,"294") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("shipment",String.valueOf(request.getId()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from shipment_files "+
                    " where shipment_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from shipment where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }*/
}
