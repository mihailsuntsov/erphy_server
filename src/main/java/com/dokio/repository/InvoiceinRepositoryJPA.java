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
import com.dokio.message.request.Settings.SettingsInvoiceinForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsInvoiceinJSON;
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
public class InvoiceinRepositoryJPA {

    Logger logger = Logger.getLogger("InvoiceinRepositoryJPA");

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
            .of("doc_number","name","cagent","status_name","sum_price","company","department","creator","date_time_created_sort","invoicein_date_sort","description","is_completed","product_count")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<InvoiceinJSON> getInvoiceinTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(32L, "452,453,454,455"))//(см. файл Permissions Id)
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
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from invoicein_product where invoicein_id=p.id),0) as sum_price, " +
                    "           to_char(p.invoicein_date at time zone '"+myTimeZone+"', '"+dateFormat+"') as invoicein_date, " +
                    "           cg.name as cagent, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           (select count(*) from invoicein_product ip where coalesce(ip.invoicein_id,0)=p.id) as product_count," + //подсчет кол-ва товаров\
                    "           p.name as name," +
                    "           p.income_number as income_number," +
                    "           to_char(p.income_number_date at time zone '"+myTimeZone+"', '"+dateFormat+"') as income_number_date, " +

                    "           p.invoicein_date as invoicein_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +


                    "           from invoicein p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(32L, "452")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(32L, "453")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(32L, "454")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.income_number) like upper(CONCAT('%',:sg,'%')) or "+
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
                List<InvoiceinJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    InvoiceinJSON doc=new InvoiceinJSON();
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
                    doc.setInvoicein_date((String)                obj[20]);
                    doc.setCagent((String)                        obj[21]);
                    doc.setIs_completed((Boolean)                 obj[22]);
                    doc.setProduct_count(Long.parseLong(          obj[23].toString()));
                    doc.setName((String)                          obj[24]);
                    doc.setIncome_number((String)                 obj[25]);
                    doc.setIncome_number_date((String)            obj[26]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInvoiceinTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getInvoiceinSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from invoicein p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(32L, "452")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(32L, "453")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(32L, "454")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.income_number) like upper(CONCAT('%',:sg,'%')) or "+
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
            logger.error("Exception in method getInvoiceinSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<InvoiceinProductTableJSON> getInvoiceinProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(32L, "452,453,454,455"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = p.edizm_id) as edizm," +
                    " ap.nds_id," +
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = a.department_id),0) as total, "+ //всего на складе (т.е остаток)
                    " (select " +
                    "   sum(coalesce(reserved_current,0)) " +
                    "   from " +
                    "   customers_orders_product " +
                    "   where " +
                    "   product_id=ap.product_id "+
                    "   and department_id = a.department_id) as reserved, "+//зарезервировано в выбранном отделении
                    " ap.id  as id, " +
                    " ppr.is_material as is_material, " +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce(nds.value,0) as nds_value," +
                    " ap.product_sumprice" +
                    " from " +
                    " invoicein_product ap " +
                    " INNER JOIN invoicein a ON ap.invoicein_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " LEFT OUTER JOIN sprav_taxes nds ON nds.id = ap.nds_id" +
                    " where a.master_id = " + myMasterId +
                    " and ap.invoicein_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(32L, "452")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(32L, "453")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(32L, "454")) //Если нет прав на просмотр всех доков в своих подразделениях
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
                List<InvoiceinProductTableJSON> returnList = new ArrayList<>();
                int row_num = 1; // номер строки при выводе печатной версии
                for(Object[] obj:queryList){
                    InvoiceinProductTableJSON doc=new InvoiceinProductTableJSON();
                    doc.setRow_num(row_num);
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setProduct_count(                                   obj[1]==null?BigDecimal.ZERO:(BigDecimal)obj[1]);
                    doc.setProduct_price(                                   obj[2]==null?BigDecimal.ZERO:(BigDecimal)obj[2]);
                    doc.setName((String)                                    obj[3]);
                    doc.setEdizm((String)                                   obj[4]);
                    doc.setNds_id(Long.parseLong(                           obj[5].toString()));
                    doc.setTotal(                                           obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                    doc.setReserved(                                        obj[7]==null?BigDecimal.ZERO:(BigDecimal)obj[7]);
                    doc.setId(Long.parseLong(                               obj[8].toString()));
                    doc.setIs_material((Boolean)                            obj[9]);
                    doc.setIndivisible((Boolean)                            obj[10]);
                    doc.setNds_value((Integer)                              obj[11]);
                    doc.setProduct_sumprice(                                obj[12]==null?BigDecimal.ZERO:(BigDecimal)obj[12]);
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
    public InvoiceinJSON getInvoiceinValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(32L, "452,453,454,455"))//см. _Permissions Id.txt
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
                    "           to_char(p.invoicein_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as invoicein_date, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce(cg.price_type_id,0) as cagent_type_price_id, " +
                    "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id, " +
                    "           p.uid as uid, " +
                    "           p.is_completed as is_completed, " +
                    "           coalesce(p.name,'') as name," +
                    "           coalesce(p.income_number,'') as income_number," +
                    "           to_char(p.income_number_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as income_number_date, " +
                    "           to_char(p.invoicein_date at time zone '"+myTimeZone+"', 'HH24:MI') as _time " +

                    "           from invoicein p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(32L, "452")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(32L, "453")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(32L, "454")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                InvoiceinJSON returnObj=new InvoiceinJSON();

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
                    returnObj.setInvoicein_date((String)                     obj[20]);
                    returnObj.setStatus_id(obj[21]!=null?Long.parseLong(    obj[21].toString()):null);
                    returnObj.setStatus_name((String)                       obj[22]);
                    returnObj.setStatus_color((String)                      obj[23]);
                    returnObj.setStatus_description((String)                obj[24]);
                    returnObj.setCagent_type_price_id(Long.parseLong(       obj[25].toString()));
                    returnObj.setDefault_type_price_id(Long.parseLong(      obj[26].toString()));
                    returnObj.setUid((String)                               obj[27]);
                    returnObj.setIs_completed((Boolean)                     obj[28]);
                    returnObj.setName((String)                              obj[29]);
                    returnObj.setIncome_number((String)                     obj[30]);
                    returnObj.setIncome_number_date((String)                obj[31]);
                    returnObj.setInvoicein_time((String)                    obj[32]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInvoiceinValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class ,CantInsertProductRowCauseErrorException.class})
    public Long insertInvoicein(InvoiceinForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "invoicein")){
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
                    (securityRepositoryJPA.userHasPermissions_OR(32L, "445")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(32L, "446") && myCompanyId.equals(request.getCompany_id())) ||
                            //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                            (securityRepositoryJPA.userHasPermissions_OR(32L, "447") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
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
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"invoicein");

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
                        cagentForm.setDescription("");
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
                        logger.error("Exception in method insertInvoicein on creating Cagent.", e);
                        e.printStackTrace();
                        return null;
                    }
                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery =
                        " insert into invoicein (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " department_id," + //отделение, из(для) которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер документа
                        " invoicein_date, " +//план. дата
                        " income_number," +//входящий внутренний номер поставщика
                        " income_number_date," +// входящая дата счета поставщика
                        " description," +//доп. информация по заказу
                        " nds," +// НДС
                        " nds_included," +// НДС включен в цену
                        " status_id,"+//статус
                        " linked_docs_group_id," +// id группы связанных документов
                        " name,"+
                        " uid"+// уникальный идентификатор документа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер документа
                        "to_timestamp(CONCAT(:invoicein_date,' ',:invoicein_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"'," +//план. дата и время
                        ":income_number,"+
                        ((request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))?" to_date(:income_number_date,'DD.MM.YYYY')":"null")+","+// входящая дата счета поставщика
                        ":description," +
                        request.isNds() + ", "+// НДС
                        request.isNds_included() + ", "+// НДС включен в цену
                        request.getStatus_id()  + ", "+//статус
                        linkedDocsGroupId+"," + // id группы связанных документов
                        ":name,"+ //наименование заказа поставщику
                        ":uid)";// уникальный идентификатор документа
                try{
                    Date dateNow = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.setParameter("name",request.getName());
                    query.setParameter("income_number",request.getIncome_number());
                    if(request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))
                        query.setParameter("income_number_date",request.getIncome_number_date());
                    query.setParameter("invoicein_date", ((request.getInvoicein_date()==null || request.getInvoicein_date().equals("")) ? dateFormat.format(dateNow) : request.getInvoicein_date()));
                    query.setParameter("invoicein_time", ((request.getInvoicein_time()==null || request.getInvoicein_time().equals("")) ? timeFormat.format(dateNow) : request.getInvoicein_time()));

                    query.executeUpdate();
                    stringQuery="select id from invoicein where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    if(insertInvoiceinProducts(request, newDocId, myMasterId)){
                        //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                        if (request.getLinked_doc_id() != null) {
                            linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "invoicein", request.getUid(), request.getCompany_id(), myMasterId);
                        }
                        return newDocId;
                    } else return null;

                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertInvoicein on inserting into invoicein_products cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertInvoicein on inserting into invoicein. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertInvoicein. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertInvoiceinProducts(InvoiceinForm request, Long docId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> productIds=new HashSet<>();
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        //сохранение таблицы
        if (!Objects.isNull(request.getInvoiceinProductTable()) && request.getInvoiceinProductTable().size() != 0) {
            for (InvoiceinProductTableForm row : request.getInvoiceinProductTable()) {
                row.setInvoicein_id(docId);// чтобы через API сюда нельзя было подсунуть рандомный id
                insertProductRowResult = saveInvoiceinProductTable(row, request.getCompany_id(),  myMasterId);  //сохранение строки таблицы товаров
                if (insertProductRowResult==null)// - т.е. произошла ошибка в методе saveInvoiceinProductTable
                    throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
                productIds.add(row.getProduct_id());
            }
        }if (!deleteInvoiceinProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", docId)){
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class ,CantInsertProductRowCauseErrorException.class})
    public Integer updateInvoicein(InvoiceinForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(32L,"456") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(32L,"457") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(32L,"458") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(32L,"459") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                    !(  //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(32L,"460") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein",request.getId().toString())) ||
                    //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(32L,"461") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",request.getId().toString()))||
                    //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                    (securityRepositoryJPA.userHasPermissions_OR(32L,"462") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",request.getId().toString()))||
                    //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                    (securityRepositoryJPA.userHasPermissions_OR(32L,"463") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein",request.getId().toString()))
                    )
                ) return -1;
            }
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();

            String stringQuery;
            stringQuery =   "update invoicein set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, " +
                    " nds = "+request.isNds()+"," +// НДС
                    " name=:name," +
                    " nds_included = "+request.isNds_included()+"," +// НДС включен в цену
//                    ((request.getInvoicein_date()!=null&& !request.getInvoicein_date().equals(""))?" invoicein_date = to_date(:invoicein_date,'DD.MM.YYYY'),":"invoicein_date = null,") +
                    " invoicein_date = to_timestamp(CONCAT(:invoicein_date,' ',:invoicein_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"',"+
                    " income_number = :income_number," +// входящий номер
                    ((request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))?" income_number_date = to_date(:income_number_date,'DD.MM.YYYY'),":"income_number_date = null,") +//входящая дата
                    " is_completed = " + request.getIs_completed() + "," +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId()+" and master_id="+myMasterId;
            try
            {
                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "invoicein"))
                    throw new DocumentAlreadyCompletedException();

                if(request.getIs_completed()!=null && request.getIs_completed() && request.getInvoiceinProductTable().size()==0) throw new Exception("There is no products in product list");

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                Query query = entityManager.createNativeQuery(stringQuery);
//                query.setParameter("invoicein_date", ((request.getInvoicein_date()==null || request.getInvoicein_date().equals("")) ? dateFormat.format(dateNow) : request.getInvoicein_date()));
                query.setParameter("description",request.getDescription());
                query.setParameter("name",request.getName());
                query.setParameter("income_number",request.getIncome_number());
                if(request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))
                    query.setParameter("income_number_date",request.getIncome_number_date());
//                if(request.getInvoicein_date()!=null&& !request.getInvoicein_date().equals(""))
//                    query.setParameter("invoicein_date",request.getInvoicein_date());
                query.setParameter("invoicein_date", ((request.getInvoicein_date()==null || request.getInvoicein_date().equals("")) ? dateFormat.format(dateNow) : request.getInvoicein_date()));
                query.setParameter("invoicein_time", ((request.getInvoicein_time()==null || request.getInvoicein_time().equals("")) ? "00:00" : request.getInvoicein_time()));
                query.executeUpdate();
                if(request.getIs_completed()==null)request.setIs_completed(false);
                if(insertInvoiceinProducts(request, request.getId(), myMasterId)){//если сохранение товаров из таблицы товаров прошло успешно
                    return 1;
                } else return null;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateInvoicein.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceinRepository/updateInvoicein on updating invoicein_product cause error.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceinRepository/updateInvoicein. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }
    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, NotEnoughPermissionsException.class})
    public Integer setInvoiceinAsDecompleted(InvoiceinForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(32L,"460") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein",request.getId().toString())) ||
            //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(32L,"461") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",request.getId().toString()))||
            //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(32L,"462") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",request.getId().toString()))||
            //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(32L,"463") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein",request.getId().toString()))
        )
        {
            if(request.getInvoiceinProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update invoicein set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "invoicein"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                // сохранение истории движения товара не делаем, т.к. в данный документ не влияет на движение товаров
                // по той же причине не делаем коррекцию баланса с контрагентом
                return 1;
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceinRepository/setInvoiceinAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceinRepository/setInvoiceinAsDecompleted.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InvoiceinRepository/setInvoiceinAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    //проверяет, не превышает ли продаваемое количество товара доступное количество, имеющееся на складе
    //если не превышает - пишется строка с товаром в БД
    //возвращает: true если все ок, false если превышает и записать нельзя, null если ошибка
    @SuppressWarnings("Duplicates")
    private Boolean saveInvoiceinProductTable(InvoiceinProductTableForm row, Long company_id, Long master_id) {
        String stringQuery="";
        try {

            // ПРОВЕРКИ НА КОЛИЧЕСТВО ТОВАРА НЕ ДЕЛАЕМ, Т.К. ДЛЯ ДАННОГО ДОКУМЕНТА ОНО НЕ ВАЖНО.
            // ЗАКАЗ ПОСТАВЩИКУ НЕ ВЛИЯЕТ НА КОЛИЧЕСТВО ТОВАРА НА СКЛАДЕ, И НЕ УЧАСТВУЕТ В РЕЗЕРВИРОВАНИИ ТОВАРА

            stringQuery =
                    " insert into invoicein_product (" +
                            "master_id, " +
                            "company_id, " +
                            "product_id, " +
                            "invoicein_id, " +
                            "product_count, " +
                            "product_price, " +
                            "product_sumprice, " +
                            "nds_id " +
                            ") values (" +
                            master_id + "," +
                            company_id + "," +
                            row.getProduct_id() + "," +
                            row.getInvoicein_id() + "," +
                            row.getProduct_count() + "," +
                            row.getProduct_price() + "," +
                            row.getProduct_sumprice() + "," +
                            row.getNds_id() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT invoicein_product_uq " +// "upsert"
                            " DO update set " +
                            " product_id = " + row.getProduct_id() + "," +
                            " invoicein_id = " + row.getInvoicein_id() + "," +
                            " product_count = " + row.getProduct_count() + "," +
                            " product_price = " + row.getProduct_price() + "," +
                            " product_sumprice = " + row.getProduct_sumprice() + "," +
                            " nds_id = " + row.getNds_id();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveInvoiceinProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean deleteInvoiceinProductTableExcessRows(String productIds, Long invoicein_id) {
        String stringQuery;
        stringQuery =   " delete from invoicein_product " +
                " where invoicein_id=" + invoicein_id +
                " and product_id not in (" + productIds.replaceAll("[^0-9\\,]", "") + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method InvoiceinRepository/deleteInvoiceinProductTableExcessRows. SQL - "+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsInvoicein(SettingsInvoiceinForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into settings_invoicein (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "department_id, " +     //отделение по умолчанию
                            "cagent_id, "+          //поставщик по умолчанию
                            "autocreate, "+         //автосоздание нового документа
                            "status_id_on_complete,"+// статус документа при проведении
                            "name," +
                            "auto_price, "+
                            "auto_add"+             // автодобавление товара из формы поиска в таблицу
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            row.getDepartmentId() + "," +
                            row.getCagentId() + ","+
                            row.getAutocreate() +"," +
                            row.getStatusIdOnComplete()+ "," +
                            ":name" +"," +
                            row.getAutoPrice()+"," +
                            row.getAutoAdd() +
                            ") " +
                            " ON CONFLICT ON CONSTRAINT settings_invoicein_user_id_key " +// "upsert"
                            " DO update set " +
                            " cagent_id = "+row.getCagentId()+"," +
                            " name=:name"+
                            ", department_id = "+row.getDepartmentId()+//некоторые строки (как эту) проверяем на null, потому что при сохранении из расценки они не отправляются, и эти настройки сбрасываются изза того, что в них прописываются null
                            ", company_id = "+row.getCompanyId()+
                            ", status_id_on_complete = "+row.getStatusIdOnComplete()+
                            ", autocreate = "+row.getAutocreate()+
                            ", auto_price = "+row.getAutoPrice()+
                            ", auto_add = "+row.getAutoAdd();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",row.getName());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsInvoicein. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsInvoiceinJSON getSettingsInvoicein() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                           // отделение
                "           p.cagent_id as cagent_id, " +
                "           cg.name as cagent, " +                                          // контрагент
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                 // предприятие
                "           coalesce(p.autocreate,false) as autocreate," +                  // автосоздание (не нужно нажимать кнопку Создать)
                "           p.status_id_on_complete as status_id_on_complete, " +           // статус по проведении
                "           coalesce(p.auto_add,false) as auto_add,  " +                    // автодобавление товара из формы поиска в таблицу
                "           coalesce(p.auto_price,false) as auto_price,  " +                // автоцена из предыдущих закупок
                "           coalesce(p.name,'') as name " +                                 // наименование заказа по умолчанию
                "           from settings_invoicein p " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsInvoiceinJSON returnObj=new SettingsInvoiceinJSON();

            for(Object[] obj:queryList){
                returnObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                returnObj.setCagentId(obj[1]!=null?Long.parseLong(          obj[1].toString()):null);
                returnObj.setCagent((String)                                obj[2]);
                returnObj.setId(Long.parseLong(                             obj[3].toString()));
                returnObj.setCompanyId(Long.parseLong(                      obj[4].toString()));
                returnObj.setAutocreate((Boolean)                           obj[5]);
                returnObj.setStatusIdOnComplete(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                returnObj.setAutoAdd((Boolean)                              obj[7]);
                returnObj.setAuto_price((Boolean)                           obj[8]);
                returnObj.setName((String)                             obj[9]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsInvoicein. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }

    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteInvoicein (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(32L,"448") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(32L,"449") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(32L,"450") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",delNumbers)) ||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(32L, "451") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein", delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "invoicein");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update invoicein p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "invoicein")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteInvoicein. SQL query:" + stringQuery, e);
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
    public Integer undeleteInvoicein(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(32L,"448") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(32L,"449") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(32L,"450") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",delNumbers)) ||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(32L,"451") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein", delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update invoicein p" +
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
                logger.error("Exception in method undeleteInvoicein. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean addFilesToInvoicein(UniversalForm request){
        Long invoiceinId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(32L,"456") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein",invoiceinId.toString())) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(32L,"457") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",invoiceinId.toString()))||
            //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(32L,"458") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",invoiceinId.toString()))||
            //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(32L,"459") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein",invoiceinId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select invoicein_id from invoicein_files where invoicein_id=" + invoiceinId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_InvoiceinId_FileId(invoiceinId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method InvoiceinRepository/addFilesToInvoicein.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_InvoiceinId_FileId(Long invoiceinId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into invoicein_files " +
                    "(invoicein_id,file_id) " +
                    "values " +
                    "(" + invoiceinId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method InvoiceinRepository/manyToMany_InvoiceinId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesUniversalJSON> getListOfInvoiceinFiles(Long invoiceinId) {
        if(securityRepositoryJPA.userHasPermissions_OR(32L, "452,453,454,455"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           invoicein p" +
                    "           inner join" +
                    "           invoicein_files pf" +
                    "           on p.id=pf.invoicein_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + invoiceinId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(32L, "452")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(32L, "453")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(32L, "454")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                List<FilesUniversalJSON> invoiceinList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesUniversalJSON doc=new FilesUniversalJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    invoiceinList.add(doc);
                }
                return invoiceinList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfInvoiceinFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteInvoiceinFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(32L,"456") && securityRepositoryJPA.isItAllMyMastersDocuments("invoicein", String.valueOf(request.getAny_id()))) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(32L,"457") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("invoicein",String.valueOf(request.getAny_id())))||
            //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(32L,"458") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("invoicein",String.valueOf(request.getAny_id())))||
            //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(32L,"459") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("invoicein",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from invoicein_files "+
                    " where invoicein_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from invoicein where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method InvoiceinRepository/deleteInvoiceinFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}
