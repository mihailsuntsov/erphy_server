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
import com.dokio.message.request.Settings.SettingsRetailSalesForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsRetailSalesJSON;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseOversellException;
import com.dokio.repository.Exceptions.CantSaveProductQuantityException;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private LinkedDocsUtilites linkedDocsUtilites;
    @Autowired
    private CustomersOrdersRepositoryJPA customersOrdersRepository;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","name","cagent","status_name","sum_price","hasSellReceipt","company","department","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<RetailSalesJSON> getRetailSalesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(25L, "316,317,318,319"))//(см. файл Permissions Id)
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
                    "           coalesce(p.shift_id,0) as shift_id, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from retail_sales_product where retail_sales_id=p.id),0) as sum_price, " +
                    "           p.name as name, " +
                    "           coalesce(sh.shift_number,0) as shift_number, " +
                    "           (select count(*) from receipts rec where coalesce(rec.retail_sales_id,0)=p.id and rec.operation_id='sell') as hasSellReceipt," + //подсчет кол-ва чеков на предмет того, был ли выбит чек продажи в данной Розничной продаже
                    "           cg.name as cagent " +

                    "           from retail_sales p " +
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

            if (!securityRepositoryJPA.userHasPermissions_OR(25L, "316")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(25L, "317")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(25L, "318")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            }


            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                query.setFirstResult(offsetreal).setMaxResults(result);


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
                    doc.setShift_id(obj[16]!=null?Long.parseLong(obj[16].toString()):null);
                    doc.setStatus_id(obj[19]!=null?Long.parseLong(obj[19].toString()):null);
                    doc.setStatus_name((String)                   obj[20]);
                    doc.setStatus_color((String)                  obj[21]);
                    doc.setStatus_description((String)            obj[22]);
                    doc.setSum_price((BigDecimal)                 obj[23]);
                    doc.setName((String)                          obj[24]);
                    doc.setShift_number((Integer)                 obj[25]);
                    doc.setHasSellReceipt(((BigInteger)           obj[26]).longValue() > 0L);//если есть чеки - вернется true
                    doc.setCagent((String)                        obj[27]);
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
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(25L, "316")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(25L, "317")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(25L, "318")) //Если нет прав на просмотр всех доков в своих подразделениях
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
            logger.error("Exception in method getRetailSalesSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<RetailSalesProductTableJSON> getRetailSalesProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(25L, "316,317,318,319"))//(см. файл Permissions Id)
        {
            String stringQuery;
            Long parentCustomersOrdersId = getParentCustomersOrdersId(docId);
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.retail_sales_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = p.edizm_id) as edizm," +
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
                    "   and customers_orders_id!="+parentCustomersOrdersId+") as reserved, "+//зарезервировано в других документах Заказ покупателя

//                    " ap.product_count as shipped, "+//в розничных продажах все количество товара считается отгруженным, т.к. розн. продажа создается в момент продажи (отгрузки) товара.
                    " ap.department_id as department_id, " +
                    " (select name from departments where id= ap.department_id) as department, "+
                    " ap.id  as row_id, " +
                    " ppr.name_api_atol as ppr_name_api_atol, " +
                    " ppr.is_material as is_material, " +
//                    " ap.product_count as reserved_current " +//в розничных продажах нет резервов, так что приравниваем резерв к количеству товара в продаже (т.е. весь товар априори зарезервирован)
                    " p.indivisible as indivisible" +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " from " +
                    " retail_sales_product ap " +
                    " INNER JOIN retail_sales a ON ap.retail_sales_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.retail_sales_id = " + docId;

//            if (!securityRepositoryJPA.userHasPermissions_OR(25L, "316")) //Если нет прав на просм по всем предприятиям
//            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
//                if (!securityRepositoryJPA.userHasPermissions_OR(25L, "317")) //Если нет прав на просм по своему предприятию
//                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
//                    if (!securityRepositoryJPA.userHasPermissions_OR(25L, "318")) //Если нет прав на просмотр всех доков в своих подразделениях
//                    {//остается только на свои документы
//                        stringQuery = stringQuery + " and a.company_id=" + myCompanyId+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
//                    }else{stringQuery = stringQuery + " and a.company_id=" + myCompanyId+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
//                } else stringQuery = stringQuery + " and a.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
//            }

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

    // Возвращает id родительского Заказа покупателя по id Розничной продажи. Если родительского документа нет - возвращает 0
    private Long getParentCustomersOrdersId(Long retailSalesId){
        String stringQuery = "select coalesce(rs.customers_orders_id,0) from retail_sales rs where rs.id=" + retailSalesId;
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
    public RetailSalesJSON getRetailSalesValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(25L, "316,317,318,319"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
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
                "           dp.name ||' '||dp.address  as department, " +
                "           p.doc_number as doc_number, " +
                "           coalesce(sh.shift_number,0) as shift_number, " +
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
                "           coalesce(p.shift_id,0) as shift_id, " +
                "           p.date_time_created as date_time_created_sort, " +
                "           p.date_time_changed as date_time_changed_sort, " +
                "           p.name as name, " +
                "           p.status_id as status_id, " +
                "           stat.name as status_name, " +
                "           stat.color as status_color, " +
                "           stat.description as status_description, " +
                "           coalesce(cg.price_type_id,0) as cagent_type_price_id, " +
                "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id, " +
                "           coalesce(p.receipt_id,0) as receipt_id, " +
                "           p.uid as uid" +
                "           from retail_sales p " +
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

            if (!securityRepositoryJPA.userHasPermissions_OR(25L, "316")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(25L, "317")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(25L, "318")) //Если нет прав на просмотр всех доков в своих подразделениях
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
                    returnObj.setReceipt_id(Long.parseLong(                 obj[32].toString()));
                    returnObj.setUid((String)                               obj[33]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getRetailSalesValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //возвращает набор типов цен: Тип цены по умолчанию для предприятия, Тип цены отделения, Тип цены контрагента
    public SetOfTypePricesJSON getSetOfTypePrices(Long company_id, Long department_id, Long cagent_id){

        String stringQuery;
        stringQuery = "select " +
                "coalesce((select price_type_id from cagents where company_id="+company_id+" and id="+cagent_id+"),0) as cagent_type_price_id, " +
                "coalesce((select price_id from departments where company_id="+company_id+" and id="+department_id+"),0) as department_type_price_id, " +
                "coalesce((select id from sprav_type_prices where company_id="+company_id+" and is_default=true),0) as default_type_price_id";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            SetOfTypePricesJSON returnObj=new SetOfTypePricesJSON();

            for(Object[] obj:queryList){
                returnObj.setCagent_type_price_id(Long.parseLong(       obj[0].toString()));
                returnObj.setDepartment_type_price_id(Long.parseLong(   obj[1].toString()));
                returnObj.setDefault_type_price_id(Long.parseLong(      obj[2].toString()));
            }
            return returnObj;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSetOfTypePrices. SQL query:" + stringQuery, e);
            return null;
        }

    }

    // Розничная продажа создается с её товарами, и в дальнейшем параметры таблицы с товарами (количество, цена) изменить невозможно, т.к. выбит чек.
    // Когда создаем товары - происходит проверка на то, что количество товара меньше или равно доступному количеству, чтобы не залезть в минуса или в резервы
    // Такая проверка происходит и на фронтэнде, но за время от добавления товара в таблицу и отправки розничной продажи на создание доступное количество товара
    // вследствие деятельности других продавцов предприятия может уменьшится, либо резервы вырасти. Поэтому эта проверка происходит еще и на бэкэнде
    // Возвращаем id Розничной продажи в случае ее успешного создания
    // Возвращаем 0 если невозможно по вышеизложенным причинам создать товарные позиции для Розничной продажи
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseOversellException.class ,CantInsertProductRowCauseErrorException.class,CantInsertProductRowCauseOversellException.class,CantSaveProductQuantityException.class})
    public Long insertRetailSales(RetailSalesForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "retail_sales")){
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
            Long docDepartment=request.getDepartment_id();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            boolean itIsMyDepartment = myDepartmentsIds.contains(docDepartment);
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long linkedDocsGroupId=null;

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

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
                Long newDocId;
                Long doc_number;//номер документа( = номер заказа)

                //генерируем номер документа, если его (номера) нет
                if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                    doc_number=Long.valueOf(request.getDoc_number());
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"retail_sales");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),25));
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
                        cagentForm.setDescription("Автоматическое создание из Розничной продажи №"+doc_number.toString());
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
                        " status_id,"+//статус заказа
                        " is_completed,"+
                        " linked_docs_group_id," +// id группы связанных документов
                        " uid"+// уникальный идентификатор документа, для предотвращения двойных созданий
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер заказа
                        ":name, "+
                        ":description," +
                        request.isNds() + ", "+// НДС
                        request.isNds_included() + ", "+// НДС включен в цену
                        request.getCustomers_orders_id() + ", "+
                        request.getShift_id() + ", "+
                        request.getStatus_id()  + ", "+//статус продажи
                        true + ", "+// розничная продажа априори проведена, т.к. создается уже по факту продажи (убытия товара со склада)
                        linkedDocsGroupId+"," + // id группы связанных документов
                        ":uid)";// уникальный идентификатор документа, для предотвращения двойных созданий
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("name",request.getName());
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.executeUpdate();
                    stringQuery="select id from retail_sales where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    if(insertRetailSalesProducts(request, newDocId, myMasterId)){



                        // корректируем резервы в родительском "Заказе покупателя" (если он есть и если резервы проставлены)
                        // берем id Заказа покупателя (или 0 если его нет)
                        Long customersOrdersId = request.getCustomers_orders_id()==null?0L:request.getCustomers_orders_id();

                        //получаем таблицу из родительского Заказа покупателя (если его нет - у листа просто будет size = 0)
                        List<CustomersOrdersProductTableJSON> customersOrdersProductTable = new ArrayList<>();
                        if(customersOrdersId>0L) {
                            customersOrdersProductTable = customersOrdersRepository.getCustomersOrdersProductTable(customersOrdersId);
                        }



                        // бежим по товарам в Розничной продаже
                        for (RetailSalesProductTableForm row : request.getRetailSalesProductTable()) {

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
                        }



                        //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                        if (request.getLinked_doc_id() != null) {
                            linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "retail_sales", request.getUid(), request.getCompany_id(), myMasterId);
                        }
                        return newDocId;
                    } else return null;


                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertRetailSales on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertRetailSales on inserting into retail_sales_products cause error.", e);
                    e.printStackTrace();
                    return null;

                } catch (CantInsertProductRowCauseOversellException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertRetailSales on inserting into retail_sales_products cause oversell.", e);
                    e.printStackTrace();
                    return 0L;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertRetailSales on inserting into products_history.", e);
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method " + e.getClass().getName() + " on inserting into retail_sales. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } else {
            logger.info("Double UUID found on insertRetailSales. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertRetailSalesProducts(RetailSalesForm request, Long newDocId, Long myMasterId) throws CantInsertProductRowCauseErrorException, CantInsertProductRowCauseOversellException, CantSaveProductHistoryException, CantSaveProductQuantityException {

        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка

        //сохранение таблицы
        if (request.getRetailSalesProductTable()!=null && request.getRetailSalesProductTable().size() > 0) {
            for (RetailSalesProductTableForm row : request.getRetailSalesProductTable()) {
                row.setRetail_sales_id(newDocId);
                insertProductRowResult = saveRetailSalesProductTable(row, request.getCompany_id(), request.getCustomers_orders_id(), myMasterId);  //сохранение таблицы товаров
                if (insertProductRowResult==null || !insertProductRowResult) {
                    if (insertProductRowResult==null){// - т.е. произошла ошибка в методе saveRetailSalesProductTable
                        throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
                    }else{ // insertProductRowResult==false - товар материален, и его наличия не хватает для продажи
                        throw new CantInsertProductRowCauseOversellException();//кидаем исключение 'оверселл', чтобы произошла отмена транзакции
                    }
                } else { // если сохранили удачно - значит нужно сделать запись в историю изменения данного товара
                    //создание записи в истории изменения товара
                    if(row.getIs_material()) { // но только если товар материален (т.е. это не услуга, работа и т.п.)
                        if (!addRetailSalesProductHistory(newDocId, row.getProduct_id(), row.getProduct_count(), row.getProduct_price(), request, myMasterId, row.getIs_material())) {
                            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
                        } else {//создание записи актуального количества товара на складе (таблица product_quantity)
                            if (!setProductQuantity(row.getProduct_id(), request.getDepartment_id(), myMasterId)) {
                                throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            throw new CantInsertProductRowCauseErrorException();
        }
    }

        @Transactional
        public Boolean updateRetailSales(RetailSalesForm request){
            //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
            if(     (securityRepositoryJPA.userHasPermissions_OR(25L,"320") && securityRepositoryJPA.isItAllMyMastersDocuments("retail_sales",request.getId().toString())) ||
                    //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(25L,"321") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("retail_sales",request.getId().toString()))||
                    //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(25L,"322") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("retail_sales",request.getId().toString()))||
                    //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                    (securityRepositoryJPA.userHasPermissions_OR(25L,"323") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("retail_sales",request.getId().toString())))
            {
                Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

                String stringQuery;
                stringQuery =   " update retail_sales set " +
                        " changer_id = " + myId + ", "+
                        " date_time_changed= now()," +
                        " description = :description, " +
                        " name = :name, " +
                        " status_id = " + request.getStatus_id() +
                        " where " +
                        " id= "+request.getId();
                try
                {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("name",request.getName());
                    query.setParameter("description",request.getDescription());
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
        private Boolean saveRetailSalesProductTable(RetailSalesProductTableForm row, Long company_id, Long customersOrdersId, Long master_id) {
            String stringQuery="";
//            Integer saveResult=0;   // 0 - если был резерв - он сохранился, 1 - если был резерв - он отменился. (это относится только к вновь поставленным резервам) Если резерв уже был выставлен - он не отменится.
            BigDecimal available;   // Если есть постановка в резерв - узнаём, есть ли свободные товары (пока мы редактировали таблицу, кто-то мог поставить эти же товары в свой резерв, и чтобы
            try {
                if(row.getIs_material()) //если номенклатура материальна (т.е. это товар, а не услуга и не работа)
                    //вычисляем доступное количество товара на складе. customersOrdersId нужен, чтобы не учитывать резервы родительского Закза покупателя
                    available = productsRepository.getAvailableExceptMyDoc(row.getProduct_id(), row.getDepartment_id(), (Objects.isNull(customersOrdersId)?0L:customersOrdersId));
                else available= BigDecimal.valueOf(0L);
                if (available.compareTo(row.getProduct_count()) > -1 || !row.getIs_material()) //если доступное количество товара больше или равно количеству к продаже, либо номенклатура не материальна (т.е. это не товар, а услуга или работа или т.п.)
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
                        "price_type_id, " +
                        "nds_id, " +
                        "department_id, " +
                        "product_price_of_type_price " +
                        ") values (" +
                        master_id + "," +
                        company_id + "," +
                        row.getProduct_id() + "," +
                        row.getRetail_sales_id() + "," +
                        row.getProduct_count() + "," +
                        row.getProduct_price() + "," +
                        row.getProduct_sumprice() + "," +
                        row.getPrice_type_id() + "," +
                        row.getNds_id() + ", " +
                        row.getDepartment_id() + ", " +
                        row.getProduct_price_of_type_price() +
                        " ) " +
                        "ON CONFLICT ON CONSTRAINT retail_sales_product_uq " +// "upsert"
                        " DO update set " +
                        " product_id = " + row.getProduct_id() + "," +
                        " retail_sales_id = " + row.getRetail_sales_id() + "," +
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
                logger.error("Exception in method saveRetailSalesProductTable. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        }


        //сохраняет настройки документа "Розничные продажи"
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
                                "autocreate_on_cheque, "+//автосоздание нового документа, если в текущем успешно напечатан чек
                                "show_kkm, "+               //показывать блок ККМ
                                "status_id_on_autocreate_on_cheque,"+//Перед автоматическим созданием после успешного отбития чека документ сохраняется. Данный статус - это статус документа при таком сохранении
                                "auto_add"+                 // автодобавление товара из формы поиска в таблицу
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
                                row.getAutocreateOnCheque() +", " +
                                row.getShowKkm() + "," +
                                row.getStatusIdOnAutocreateOnCheque()+ "," +
                                row.getAutoAdd() +
                                ") " +
                                "ON CONFLICT ON CONSTRAINT settings_retail_sales_user_uq " +// "upsert"
                                " DO update set " +
                                " pricing_type = '" + row.getPricingType() + "',"+
                                " price_type_id = " + row.getPriceTypeId() + ","+
                                " change_price = " + row.getChangePrice() + ","+
                                " plus_minus = '" + row.getPlusMinus() + "',"+
                                " change_price_type = '" + row.getChangePriceType() + "',"+
                                " hide_tenths = " + row.getHideTenths() + ","+
                                " save_settings = " + row.getSaveSettings() +
                                ", department_id = "+row.getDepartmentId()+
                                ", company_id = "+row.getCompanyId()+
                                ", customer_id = "+row.getCustomerId()+
                                ", name = '"+row.getName()+"'"+
                                ", priority_type_price_side = '"+row.getPriorityTypePriceSide()+"'"+
                                ", status_id_on_autocreate_on_cheque = "+row.getStatusIdOnAutocreateOnCheque()+
                                ", show_kkm = "+row.getShowKkm()+
                                ", autocreate_on_cheque = "+row.getAutocreateOnCheque()+
                                ", auto_add = "+row.getAutoAdd();
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

    //сохраняет настройки РАСЦЕНКИ документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean savePricingSettingsRetailSales(SettingsRetailSalesForm row) {
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
                            "save_settings " +      //сохранять настройки (флажок "Сохранить настройки" будет установлен) - boolean
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
                            row.getSaveSettings() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_retail_sales_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = '" + row.getPricingType() + "',"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = '" + row.getPlusMinus() + "',"+
                            " change_price_type = '" + row.getChangePriceType() + "',"+
                            " hide_tenths = " + row.getHideTenths() + ","+
                            " save_settings = " + row.getSaveSettings();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method savePricingSettingsRetailSales. SQL query:"+stringQuery, e);
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
//                    "           coalesce(p.autocreate_on_start,false) as autocreate_on_start," +
                    "           coalesce(p.autocreate_on_cheque,false) as autocreate_on_cheque," +
                    "           p.name as name, " +
                    "           p.status_id_on_autocreate_on_cheque as status_id_on_autocreate_on_cheque, " +
                    "           coalesce(p.show_kkm,false) as show_kkm,  " +                 // показывать блок ККМ
                    "           coalesce(p.auto_add,false) as auto_add  " +                 // автодобавление товара из формы поиска в таблицу
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
                    returnObj.setAutocreateOnCheque((Boolean)               obj[13]);
                    returnObj.setName((String)                              obj[14]);
                    returnObj.setStatusIdOnAutocreateOnCheque(obj[15]!=null?Long.parseLong(obj[15].toString()):null);
                    returnObj.setShowKkm((Boolean)                          obj[16]);
                    returnObj.setAutoAdd((Boolean)                          obj[17]);
                }
                return returnObj;
            }
            catch (Exception e) {
                logger.error("Exception in method getSettingsRetailSales. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw e;
            }

        }

        @SuppressWarnings("Duplicates") // проверка на наличие чека необходимой операции (operation id, например sell), определенного документа (например, розничной продажи, id в таблице documents = 25) с определенным id
        public Boolean isReceiptPrinted(Long company_id, int document_id, Long id, String operation_id )
        {
            String stringQuery;
            stringQuery = "" +
                    " select 1 from receipts where " +
                    " company_id="+company_id+
                    " and document_id ="+document_id +
                    " and operation_id = '" + operation_id + "'" +
                    " and retail_sales_id = " + id;//потом название этой колонки нужно будет определять динамически через отдельный метод, засылая туда operation_id
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                return(query.getResultList().size()>0);
            }
            catch (Exception e) {
                logger.error("Exception in method isReceiptPrinted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return true;
            }
        }




        //записывает историю изменения кол-ва товара
        //на данный момент нематериальная номенклатура тоже записывается в products_history, для ее отображения в карточке номенклатуры в разделе Операции, чтобы там можно было видеть Розничные продажи и Возвраты покупателей
        private Boolean addRetailSalesProductHistory(Long retailSalesId, Long product_id, BigDecimal product_count, BigDecimal product_price, RetailSalesForm request , Long masterId, boolean isMaterial) throws CantInsertProductRowCauseOversellException, CantSaveProductHistoryException {
            String stringQuery;
            ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(product_id,request.getDepartment_id());
            BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
            BigDecimal lastAvgPurchasePrice= lastProductHistoryRecord.getAvg_purchase_price();
            BigDecimal lastAvgNetcostPrice= lastProductHistoryRecord.getAvg_netcost_price();
            BigDecimal lastPurchasePrice= lastProductHistoryRecord.getLast_purchase_price();

            //необходимо проверить, что списываем количество товара не более доступного количества.
            //вообще, в БД установлено ограничение на кол-во товара >=0, и отмену транзакции мог бы сделать CantSaveProductHistoryException в блоке catch,
            //но данным исключением мы уточним, от чего именно произошла отмена транзакции:
            if((lastQuantity.subtract(product_count)).compareTo(new BigDecimal("0")) < 0) {
                logger.error("Для Розничной продажи с id = "+request.getId()+", номер документа "+request.getDoc_number()+", количество товара для продажи больше доступного количества товара на складе");
                throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
            }

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
                            25 +","+
                            retailSalesId + ","+
                            product_id + ","+
                            (isMaterial?(lastQuantity.subtract(product_count)):(new BigDecimal(0)))+","+//если номенклатура не материальна - количество = 0
                            product_count.multiply(new BigDecimal(-1)) +","+
                            lastAvgPurchasePrice +","+
                            lastAvgNetcostPrice +","+
                            lastPurchasePrice+","+
                            product_price+","+
                            " now())";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method addRetailSalesProductHistory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
            }
        }


    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(Long product_id, Long department_id , Long masterId) {
        String stringQuery;
        ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(product_id,department_id);
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
                            department_id + ","+
                            product_id + ","+
                            lastQuantity +
                            ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert"
                            " DO update set " +
                            " department_id = " + department_id + ","+
                            " product_id = " + product_id + ","+
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

// **************************************************** СМЕНЫ ********************************************************//































// **************************************************** ЧЕКИ *********************************************************//










    // устарело
//    public List<LinkedDocsJSON> getRetailSalesLinkedDocsList(Long docId, String docName) {
//        String stringQuery;
//        String myTimeZone = userRepository.getUserTimeZone();
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String tableName=(docName.equals("return")?"return":"");
//        stringQuery =   " select " +
//                " ap.id," +
//                " to_char(ap.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI'), " +
//                " ap.description," +
//                " coalesce(ap.is_completed,false)," +
//                " ap.doc_number" +
//                " from "+tableName+" ap" +
//                " where ap.master_id = " + myMasterId +
//                " and coalesce(ap.is_deleted,false)!=true "+
//                " and ap.retail_sales_id = " + docId;
//        stringQuery = stringQuery + " order by ap.date_time_created asc ";
//        try{
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Object[]> queryList = query.getResultList();
//            List<LinkedDocsJSON> returnList = new ArrayList<>();
//            for(Object[] obj:queryList){
//                LinkedDocsJSON doc=new LinkedDocsJSON();
//                doc.setId(Long.parseLong(                       obj[0].toString()));
//                doc.setDate_time_created((String)               obj[1]);
//                doc.setDescription((String)                     obj[2]);
//                doc.setIs_completed((Boolean)                   obj[3]);
//                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
//                returnList.add(doc);
//            }
//            return returnList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getRetailSalesLinkedDocsList. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }

    // С удалением пока все непонятно - Розничная продажа создается тогда, когда уже пробит чек, т.е. продажа уже совершена, и товар выбыл. Удалять такое однозначно нельзя. Но возможно будут какие-то
    // другие ситуации. Поэтому удаление пока оставляю закомментированным

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
                    e.printSckTrace();
                    return false;
                }
            } else return false;
        }
    /*    @Transactional
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
        */


//*****************************************************************************************************************************************************
//***************************************************      Exceptions      ****************************************************************************
//*****************************************************************************************************************************************************



}

class CantSaveProductHistoryException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert products_history table row");
    }
}
