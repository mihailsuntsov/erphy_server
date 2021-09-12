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

import com.dokio.message.request.PostingForm;
import com.dokio.message.request.PostingProductForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsPostingForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.PostingJSON;
import com.dokio.message.response.Settings.SettingsPostingJSON;
import com.dokio.message.response.additional.FilesPostingJSON;
import com.dokio.message.response.ProductHistoryJSON;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.repository.Exceptions.CantSaveProductQuantityException;
import com.dokio.repository.Exceptions.InsertProductHistoryExceprions;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
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
public class PostingRepository {
    @PersistenceContext
    private EntityManager           entityManager;
    @Autowired
    private UserDetailsServiceImpl  userRepository;
    @Autowired
    private UserRepositoryJPA       userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA           securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA            companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA         departmentRepositoryJPA;
    @Autowired
    private CommonUtilites          commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;

    Logger logger = Logger.getLogger("PostingRepository");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","status_name","product_count","is_completed","posting_date_sort","company","department","creator","date_time_created_sort","description")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<PostingJSON> getPostingTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

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
                    "           to_char(p.posting_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as posting_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.posting_date as posting_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           (select count(*) from posting_product ip where coalesce(ip.posting_id,0)=p.id) as product_count" + //подсчет кол-ва товаров
                    "           from posting p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.posting_date, 'DD.MM.YYYY') = :sg or "+
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
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
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            }

            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);
                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}
                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<PostingJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    PostingJSON doc=new PostingJSON();
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
                    doc.setPosting_date((String)(                 obj[11]));
                    doc.setCompany((String)                       obj[12]);
                    doc.setDate_time_created((String)             obj[13]);
                    doc.setDate_time_changed((String)             obj[14]);
                    doc.setDescription((String)                   obj[15]);
                    doc.setIs_completed((Boolean)                 obj[16]);
                    doc.setStatus_name((String)                   obj[20]);
                    doc.setStatus_color((String)                  obj[21]);
                    doc.setStatus_description((String)            obj[22]);
                    doc.setProduct_count(Long.parseLong(          obj[23].toString()));
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPostingTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;

    }
    @SuppressWarnings("Duplicates")
    public int getPostingSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
//        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//(см. файл Permissions Id)
//        {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

        stringQuery = "select  p.id as id " +
                "           from posting p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.posting_date, 'DD.MM.YYYY') = :sg or "+
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
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
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getPostingSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<PostingProductForm> getPostingProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.posting_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " p.name as name," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = a.department_id),0) as total "+ //всего на складе (т.е остаток)
                    " from " +
                    " posting_product ap " +
                    " INNER JOIN posting a ON ap.posting_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.posting_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and a.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and a.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and a.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<PostingProductForm> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    PostingProductForm doc=new PostingProductForm();
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setPosting_id(Long.parseLong(                       obj[1].toString()));
                    doc.setProduct_count((BigDecimal)                       obj[2]);
                    doc.setProduct_price((BigDecimal)                       obj[3]);
                    doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                    doc.setName((String)                                    obj[5]);
                    doc.setEdizm((String)                                   obj[6]);
                    doc.setIndivisible((Boolean)                            obj[7]);
                    doc.setTotal((BigDecimal)                               obj[8]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPostingProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //*****************************************************************************************************************************************************
    //****************************************************      CRUD      *********************************************************************************
    //*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public PostingJSON getPostingValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//см. _Permissions Id.txt
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
                    "           to_char(p.posting_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as posting_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.posting_date as posting_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description " +
                    "           from posting p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;
            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                PostingJSON returnObj=new PostingJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                     obj[0].toString()));
                    returnObj.setMaster((String)                        obj[1]);
                    returnObj.setCreator((String)                       obj[2]);
                    returnObj.setChanger((String)                       obj[3]);
                    returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                    returnObj.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    returnObj.setDepartment((String)                    obj[9]);
                    returnObj.setDoc_number(Long.parseLong(             obj[10].toString()));
                    returnObj.setPosting_date((String)(                 obj[11]));
                    returnObj.setCompany((String)                       obj[12]);
                    returnObj.setDate_time_created((String)             obj[13]);
                    returnObj.setDate_time_changed((String)             obj[14]);
                    returnObj.setDescription((String)                   obj[15]);
                    returnObj.setIs_completed((Boolean)                 obj[16]);
                    returnObj.setStatus_id(obj[20]!=null?Long.parseLong(obj[20].toString()):null);
                    returnObj.setStatus_name((String)                   obj[21]);
                    returnObj.setStatus_color((String)                  obj[22]);
                    returnObj.setStatus_description((String)            obj[23]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPostingValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public Long insertPosting(PostingForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDock( request.getCompany_id(), request.getDepartment_id(), 16L, "200", "201", "202");
        if(iCan==Boolean.TRUE)
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDockId;
            Long doc_number;//номер документа

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"posting");

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =   "insert into posting (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " date_time_created," + //дата и время создания
                    " doc_number," + //номер заказа
                    " description," +//доп. информация по заказу
                    " inventory_id, " + //если документ создаётся из Инвенторизации - тут будет ее id
                    " posting_date " +// дата списания
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    " :description, " +//описание
                    request.getInventory_id() + ", "+//
                    " to_date(:posting_date,'DD.MM.YYYY')) ";// дата списания
            try {

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                //если дата не пришла (это может быть, если создаем из Инвентаризации) - нужно вставить текукщую
                query.setParameter("posting_date", ((request.getPosting_date()==null || request.getPosting_date().equals("")) ? dateFormat.format(dateNow) : request.getPosting_date()));
                query.executeUpdate();
                stringQuery = "select id from posting where creator_id=" + myId + " and date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS'))";
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDockId = Long.valueOf(query2.getSingleResult().toString());

                //если есть таблица с товарами - нужно создать их
                insertPostingProducts(request, newDockId, myMasterId);
                return newDockId;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertPosting on inserting into posting_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertPosting. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(Objects.isNull(iCan)) return null; else return 0L;
        }
    }

    //сохранение таблицы товаров
    @SuppressWarnings("Duplicates")
    private boolean insertPostingProducts(PostingForm request, Long parentDockId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> productIds=new HashSet<>();

        if (request.getPostingProductTable()!=null && request.getPostingProductTable().size() > 0) {
            for (PostingProductForm row : request.getPostingProductTable()) {
                row.setPosting_id(parentDockId);// чтобы через API сюда нельзя было подсунуть рандомный id
                if (!savePostingProductTable(row, myMasterId)) {
                    throw new CantInsertProductRowCauseErrorException();
                }
                productIds.add(row.getProduct_id());
            }
        }
        if (!deletePostingProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", parentDockId)) {
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class, CantSaveProductQuantityException.class, CantSaveProductHistoryException.class, InsertProductHistoryExceprions.class})
    public  Boolean updatePosting(PostingForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(16L,"211") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"212") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"213") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"214") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            if(updatePostingWithoutTable(request)){
                try {//сохранение таблицы
                    insertPostingProducts(request, request.getId(),myMasterId);
                    //если документ завершается - запись в историю товара
                    if(request.isIs_completed()){
                        for (PostingProductForm row : request.getPostingProductTable()) {
                            if (!addPostingProductHistory(row, request, myMasterId)) {//
                                break;
                            } else {
                                //Запись в таблицу кол-ва товара. Только для вещественной номенклатуры. Например, для услуг кол-во не записываем
//                                row.
                                if (!setProductQuantity(row, request, myMasterId)) {// запись о количестве товара в отделении в отдельной таблице
                                    break;
                                }

                            }
                        }
                    }
                    return true;
                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updatePosting on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return false;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updatePosting on inserting into posting_products cause error.", e);
                    e.printStackTrace();
                    return false;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updatePosting on inserting into products_history.", e);
                    e.printStackTrace();
                    return false;
                } catch (Exception e){
                    logger.error("Exception in method PostingRepository/updatePosting.", e);
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private Boolean updatePostingWithoutTable(PostingForm request) {
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery;
        stringQuery =   " update posting set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, "+
                " doc_number =" + request.getDoc_number() + "," +
                " is_completed = " + request.isIs_completed() + "," +
                " posting_date = to_date(:posting_date,'DD.MM.YYYY') " +
                " where " +
                " id= "+request.getId() +
                " and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
            query.setParameter("posting_date", (request.getPosting_date() == "" ? null :request.getPosting_date()));

            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method PostingRepository/updatePostingWithoutTable. stringQuery=" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    private Boolean savePostingProductTable(PostingProductForm row, Long myMasterId) {
        String stringQuery;

        stringQuery =   " insert into posting_product (" +
                "product_id," +
                "posting_id," +
                "product_count," +
                "product_price," +
                "product_sumprice" +
//                        "edizm_id" +
                ") values ("
                + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит
                + "(select id from posting where id="+row.getPosting_id() +" and master_id="+myMasterId+"),"
                + row.getProduct_count() + ","
                + row.getProduct_price() +","
                + row.getProduct_sumprice()+")" +
                "ON CONFLICT ON CONSTRAINT posting_product_uq " +// "upsert"
                " DO update set " +
                " product_id = " + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+")," +
                " posting_id = "+ "(select id from posting where id="+row.getPosting_id() +" and master_id="+myMasterId+")," +
                " product_count = " + row.getProduct_count() + "," +
                " product_price = " + row.getProduct_price() + "," +
                " product_sumprice = " + row.getProduct_sumprice();
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method PostingRepository/savePostingProductTable. SQL query:"+stringQuery, e);
            return false;
        }
    }

    private Boolean deletePostingProductTableExcessRows(String productIds, Long posting_id) {
        String stringQuery;

        stringQuery =   " delete from posting_product " +
                " where posting_id=" + posting_id +
                " and product_id not in (" + productIds + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;

        } catch (Exception e) {
            logger.error("Exception in method PostingRepository/deletePostingProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }

    }

    @SuppressWarnings("Duplicates")
    private Boolean addPostingProductHistory(PostingProductForm row, PostingForm request , Long masterId) throws CantSaveProductHistoryException {
        String stringQuery;
        try {
            //берем последнюю запись об истории товара в данном отделении
            ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
            //последнее количество товара
            BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
            //средняя цена закупа
            BigDecimal lastAvgPurchasePrice= lastProductHistoryRecord.getAvg_purchase_price();
            //средняя себестоимость
            BigDecimal lastAvgNetcostPrice= lastProductHistoryRecord.getAvg_netcost_price();
            //средняя цена закупа = ((ПОСЛЕДНЕЕ_КОЛИЧЕСТВО*СРЕДНЯЯ_ЦЕНА_ЗАКУПА)+СУММА_ПО_НОВОМУ_ТОВАРУ) / ПОСЛЕДНЕЕ_КОЛИЧЕСТВО+КОЛИЧЕСТВО_ПО_НОВОМУ_ТОВАРУ
            //Именно поэтому нельзя допускать отрицательных остатков - если знаменатель будет = 0, то возникнет эксепшн деления на 0.
            BigDecimal avgPurchasePrice = ((lastQuantity.multiply(lastAvgPurchasePrice)).add(row.getProduct_sumprice())).divide(lastQuantity.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);
            // т.к. в Оприходовании нет расходов, то себестоимость товара равна цене. Поэтому в расчете средней себестоимости (avgNetcostPrice) вместо произведения
            // количества товара на себестоимость единицы товара берём сумму (т.е. произведение количества на цену едииницы товара)
            // т.е. идет замена row.getProduct_count().multiply(row.getProduct_netcost()))  на   row.getProduct_sumprice()
            BigDecimal avgNetcostPrice =  ((lastQuantity.multiply(lastAvgNetcostPrice)). add(row.getProduct_sumprice())).divide(lastQuantity.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);

            stringQuery =   " insert into products_history (" +
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
                    16 +","+
                    row.getPosting_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantity.add(row.getProduct_count())+","+
                    row.getProduct_count() +","+
                    avgPurchasePrice +","+
                    avgNetcostPrice +","+
                    row.getProduct_price()+","+
                    row.getProduct_price()+","+
                    " now())";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method PostingRepository/addPostingProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(PostingProductForm row, PostingForm request , Long masterId) throws CantSaveProductQuantityException {
        String stringQuery;
        ProductHistoryJSON lastProductHistoryRecord = productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
        BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
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
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method PostingRepository/setProductQuantity. SQL query:"+stringQuery, e);
            throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }



    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean deletePosting (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(16L,"203") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"204") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"205") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"206") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update posting p" +
                    " set is_deleted=true, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")" +
                    " and coalesce(p.is_completed,false) !=true";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method deletePosting. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return false;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean undeletePosting (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(16L,"203") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"204") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"205") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"206") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update posting p" +
                    " set is_deleted=false, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method undeletePosting. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return false;
    }


    //*****************************************************************************************************************************************************
//***************************************************      UTILS      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")  //генератор номера документа
    private Long generateDocNumberCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from posting where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isPostingNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long doc_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from posting where " +
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
            logger.error("Exception in method isPostingNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

//    @SuppressWarnings("Duplicates") //проверка на то, есть ли уже в таблице товаров данный товар
//    private Boolean clearPostingProductTable(Long product_id, Long posting_id) {
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String stringQuery = " delete from " +
//                " posting_product where " +
//                "product_id="+product_id+
//                " and posting_id="+posting_id +
//                " and (select master_id from posting where id="+posting_id+")="+myMasterId;
//        try
//        {
//            entityManager.createNativeQuery(stringQuery).executeUpdate();
//            return true;
//        }
//        catch (Exception e) {
//            logger.error("Exception in method clearPostingProductTable. SQL query:" + stringQuery, e);
//            e.printStackTrace();
//            return false;
//        }
//    }

//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToPosting(UniversalForm request){
        Long postingId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(16L,"211") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",postingId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"212") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",postingId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"213") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",postingId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"214") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",postingId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select posting_id from posting_files where posting_id=" + postingId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_PostingId_FileId(postingId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error("Exception in method addFilesToPosting. " , ex);
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_PostingId_FileId(Long postingId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into posting_files " +
                    "(posting_id,file_id) " +
                    "values " +
                    "(" + postingId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("Exception in method manyToMany_PostingId_FileId. " , ex);
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesPostingJSON> getListOfPostingFiles(Long postingId) {
        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           posting p" +
                    "           inner join" +
                    "           posting_files pf" +
                    "           on p.id=pf.posting_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + postingId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                List<FilesPostingJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesPostingJSON doc=new FilesPostingJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfPostingFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deletePostingFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(16L,"211") && securityRepositoryJPA.isItAllMyMastersDocuments("posting", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"212") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"213") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"214") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from posting_files "+
                    " where posting_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from posting where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method deletePostingFile. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }


    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsPosting(SettingsPostingForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_posting (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "department_id, " +         //отделение по умолчанию
                            "status_on_finish_id, "+    //статус документа при завершении инвентаризации
                            "auto_add"+                 // автодобавление товара из формы поиска в таблицу
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            row.getDepartmentId() + "," +
                            row.getStatusOnFinishId() + "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_posting_user_uq " +// "upsert"
                            " DO update set " +
                            "  department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", auto_add = "+row.getAutoAdd();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsPosting. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsPostingJSON getSettingsPosting() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add " +                  // автодобавление товара из формы поиска в таблицу
                "           from settings_posting p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsPostingJSON postingObj=new SettingsPostingJSON();

            for(Object[] obj:queryList){
                postingObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                postingObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                postingObj.setStatusOnFinishId(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                postingObj.setAutoAdd((Boolean)                              obj[3]);
            }
            return postingObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsPosting. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }


}