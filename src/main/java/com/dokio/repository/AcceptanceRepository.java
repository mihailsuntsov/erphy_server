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

import com.dokio.message.request.AcceptanceForm;
import com.dokio.message.request.AcceptanceProductForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.AcceptanceJSON;
import com.dokio.message.response.FilesAcceptanceJSON;
import com.dokio.message.response.ProductHistoryJSON;
import com.dokio.model.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
public class AcceptanceRepository {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;

    Logger logger = Logger.getLogger("AcceptanceRepository");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","acceptance_date_sort","company","department","cagent","creator","date_time_created_sort","description","is_completed")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));
    //*****************************************************************************************************************************************************
    //****************************************************      MENU      *********************************************************************************
    //*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<AcceptanceJSON> getAcceptanceTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_id as department_id, " +
                    "           dp.name || ' ' || dp.address as department, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.acceptance_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as acceptance_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.acceptance_date as acceptance_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method " +
                    "           from acceptance p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true ";

            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +

                        " to_char(p.acceptance_date, 'DD.MM.YYYY') = :sg or "+
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cg.name) like  upper(CONCAT('%',:sg,'%')) or "+
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

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<AcceptanceJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    AcceptanceJSON doc=new AcceptanceJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setNds((Boolean)                          obj[8]);
                    doc.setNds_included((Boolean)                 obj[9]);
                    doc.setOverhead((BigDecimal)                  obj[10]);
                    doc.setDepartment_id(Long.parseLong(          obj[11].toString()));
                    doc.setDepartment((String)                    obj[12]);
                    doc.setCagent_id(Long.parseLong(              obj[13].toString()));
                    doc.setCagent((String)                        obj[14]);
                    doc.setDoc_number(Long.parseLong(             obj[15].toString()));
                    doc.setAcceptance_date((String)(              obj[16]));
                    doc.setCompany((String)                       obj[17]);
                    doc.setDate_time_created((String)             obj[18]);
                    doc.setDate_time_changed((String)             obj[19]);
                    doc.setDescription((String)                   obj[20]);
                    doc.setIs_completed((Boolean)                 obj[21]);
                    doc.setOverhead_netcost_method((Integer)      obj[25]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getAcceptanceTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getAcceptanceSize(String searchString, int companyId, int departmentId) {
        Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from acceptance p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_archive,false) !=true ";

        if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +

                    " to_char(p.acceptance_date, 'DD.MM.YYYY') = :sg or "+
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cg.name) like  upper(CONCAT('%',:sg,'%')) or "+
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
            logger.error("Exception in method getAcceptanceSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<AcceptanceProductForm> getAcceptanceProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.acceptance_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " ap.product_netcost," +
                    " ap.nds_id," +
                    " ap.edizm_id," +
                    " p.name as name," +
                    " (select nds.name from sprav_sys_nds nds where nds.id = ap.nds_id) as nds," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = ap.edizm_id) as edizm" +
                    " from " +
                    " acceptance_product ap " +
                    " INNER JOIN acceptance a ON ap.acceptance_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.acceptance_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);
            try{
                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<AcceptanceProductForm> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    AcceptanceProductForm doc=new AcceptanceProductForm();
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setAcceptance_id(Long.parseLong(                    obj[1].toString()));
                    doc.setProduct_count((BigDecimal)                       obj[2]);
                    doc.setProduct_price((BigDecimal)                       obj[3]);
                    doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                    doc.setProduct_netcost((BigDecimal)                     obj[5]);
                    doc.setNds_id((Integer)                                 obj[6]);
                    doc.setEdizm_id(obj[7]!=null?Long.parseLong(            obj[7].toString()):null);
                    doc.setName((String)                                    obj[8]);
                    doc.setNds((String)                                     obj[9]);
                    doc.setEdizm((String)                                   obj[10]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getAcceptanceProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public AcceptanceJSON getAcceptanceValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
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
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_id as department_id, " +
                    "           dp.name ||' '||dp.address  as department, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.acceptance_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as acceptance_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.acceptance_date as acceptance_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method " +
                    "           from acceptance p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                AcceptanceJSON returnObj=new AcceptanceJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                     obj[0].toString()));
                    returnObj.setMaster((String)                        obj[1]);
                    returnObj.setCreator((String)                       obj[2]);
                    returnObj.setChanger((String)                       obj[3]);
                    returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                    returnObj.setNds((Boolean)                          obj[8]);
                    returnObj.setNds_included((Boolean)                 obj[9]);
                    returnObj.setOverhead((BigDecimal)                  obj[10]);
                    returnObj.setDepartment_id(Long.parseLong(          obj[11].toString()));
                    returnObj.setDepartment((String)                    obj[12]);
                    returnObj.setCagent_id(Long.parseLong(              obj[13].toString()));
                    returnObj.setCagent((String)                        obj[14]);
                    returnObj.setDoc_number(Long.parseLong(             obj[15].toString()));
                    returnObj.setAcceptance_date((String)(              obj[16]));
                    returnObj.setCompany((String)                       obj[17]);
                    returnObj.setDate_time_created((String)             obj[18]);
                    returnObj.setDate_time_changed((String)             obj[19]);
                    returnObj.setDescription((String)                   obj[20]);
                    returnObj.setIs_completed((Boolean)                 obj[21]);
                    returnObj.setOverhead_netcost_method((Integer)      obj[25]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getAcceptanceValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public Long insertAcceptance(AcceptanceForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDock( request.getCompany_id(), request.getDepartment_id(), 15L, "184", "185", "192");
        if(iCan==Boolean.TRUE)
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDockId;
            Long doc_number;//номер документа

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"acceptance");

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =   "insert into acceptance (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " date_time_created," + //дата и время создания
                    " cagent_id," +//поставщик
                    " nds," +
                    " nds_included," +
                    " overhead," +
                    " overhead_netcost_method," +
                    " doc_number," + //номер заказа
                    " description," +//доп. информация по заказу
                    " acceptance_date " +// дата списания
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    request.getCagent_id() + ", "+
                    request.isNds() + ", "+
                    request.isNds_included() + ", "+
                    request.getOverhead() + ", "+
                    request.getOverhead_netcost_method() + ", "+
                    doc_number + ", "+//номер заказа
                    " :description, " +//описание
                    " to_date(:acceptance_date,'DD.MM.YYYY')) ";// дата списания
            try {

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                //если дата не пришла (это может быть, если создаем из Инвентаризации) - нужно вставить текукщую
                query.setParameter("acceptance_date", ((request.getAcceptance_date()==null || request.getAcceptance_date().equals("")) ? dateFormat.format(dateNow) : request.getAcceptance_date()));
                query.executeUpdate();
                stringQuery = "select id from acceptance where creator_id=" + myId + " and date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS'))";
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDockId = Long.valueOf(query2.getSingleResult().toString());

                //если есть таблица с товарами - нужно создать их
                insertAcceptanceProducts(request, newDockId, myMasterId);
                return newDockId;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertAcceptance on inserting into acceptance_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertAcceptance. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(Objects.isNull(iCan)) return null; else return 0L;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class, CantSaveProductQuantityException.class, InsertProductHistoryExceprions.class})
    public  Boolean updateAcceptance(AcceptanceForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            if(updateAcceptanceWithoutTable(request,myMasterId)){                                      //апдейт основного документа, без таблицы товаров
                try {//сохранение таблицы
                    insertAcceptanceProducts(request, request.getId(), myMasterId);
                    //если завершается приемка - запись в историю товара
                    if(request.is_completed()){

                        for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                            if (!addAcceptanceProductHistory(row, request, myMasterId)) {//       //сохранение истории операций с записью актуальной инфо о количестве товара в отделении
                                break;
                            } else {
                                if (!setProductQuantity(row, request, myMasterId)) {// запись о количестве товара в отделении в отдельной таблице
                                    break;
                                }
                            }
                        }
                    }
                    return true;
                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return false;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance on inserting into acceptance_products cause error.", e);
                    e.printStackTrace();
                    return false;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance on inserting into products_history.", e);
                    e.printStackTrace();
                    return false;
                } catch (Exception e){
                    logger.error("Exception in method updateAcceptance.", e);
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private Boolean updateAcceptanceWithoutTable(AcceptanceForm request, Long myMasterId) {

        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery =   " update acceptance set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, "+
                " doc_number =" + request.getDoc_number() + "," +
                " nds =" + request.isNds() + "," +
                " nds_included =" + request.isNds_included() + "," +
                " overhead =" + request.getOverhead() + "," +                               //расходы
                " overhead_netcost_method =" + request.getOverhead_netcost_method() + "," + //Распределение затрат на себестоимость товаров. 0 - нет, 1 - по весу цены в поставке
                " is_completed = " + request.isIs_completed() + "," +
                " acceptance_date = to_date(:acceptance_date,'DD.MM.YYYY') " +
                " where " +
                " id= "+request.getId() +
                " and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
            query.setParameter("acceptance_date", (request.getAcceptance_date() == "" ? null :request.getAcceptance_date()));

            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method AcceptanceRepository/updateAcceptanceWithoutTable. stringQuery=" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохранение таблицы товаров
    @SuppressWarnings("Duplicates")
    private boolean insertAcceptanceProducts(AcceptanceForm request, Long parentDockId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> productIds=new HashSet<>();

        if (request.getAcceptanceProductTable()!=null && request.getAcceptanceProductTable().size() > 0) {
            for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                row.setAcceptance_id(parentDockId);// т.к. он может быть неизвестен при создании документа
                if (!saveAcceptanceProductTable(row, myMasterId)) {
                    throw new CantInsertProductRowCauseErrorException();
                }
                productIds.add(row.getProduct_id());
            }
        }
        if (!deleteAcceptanceProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", request.getId())){
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    private Boolean saveAcceptanceProductTable(AcceptanceProductForm row, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        String stringQuery;

        stringQuery =   " insert into acceptance_product (" +
                "product_id," +
                "acceptance_id," +
                "product_count," +
                "product_price," +
                "product_sumprice," +
                "product_netcost," +
                "nds_id," +
                "edizm_id" +
                ") values ("
                + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит
                + "(select id from acceptance where id="+row.getAcceptance_id() +" and master_id="+myMasterId+"),"
                + row.getProduct_count() + ","
                + row.getProduct_price() +","
                + row.getProduct_sumprice() +","
                + row.getProduct_netcost() +","
                + row.getNds_id() +","
                + row.getEdizm_id() +")" +
                "ON CONFLICT ON CONSTRAINT acceptance_product_uq " +// "upsert"
                " DO update set " +
                " product_id = " + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+")," +
                " acceptance_id = "+ "(select id from acceptance where id="+row.getAcceptance_id() +" and master_id="+myMasterId+")," +
                " product_count = " + row.getProduct_count() + "," +
                " product_price = " + row.getProduct_price() + "," +
                " product_sumprice = " + row.getProduct_sumprice() + "," +
                " product_netcost = " + row.getProduct_netcost() +"," +
                " nds_id = " + row.getNds_id() +"," +
                " edizm_id = "+ row.getEdizm_id();
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method AcceptanceRepository/saveAcceptanceProductTable. SQL query:"+stringQuery, e);
            throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    private Boolean deleteAcceptanceProductTableExcessRows(String productIds, Long acceptance_id) {
        String stringQuery;

        stringQuery =   " delete from acceptance_product " +
                " where acceptance_id=" + acceptance_id +
                " and product_id not in (" + productIds + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method AcceptanceRepository/deleteAcceptanceProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    private Boolean addAcceptanceProductHistory(AcceptanceProductForm row, AcceptanceForm request , Long masterId) throws CantSaveProductHistoryException {
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
            //средняя себестоимость = ((ПОСЛЕДНЕЕ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ПОСЛЕДНЕЕ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
            BigDecimal avgNetcostPrice =  ((lastQuantity.multiply(lastAvgNetcostPrice)).add(row.getProduct_count().multiply(row.getProduct_netcost()))).divide(lastQuantity.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);
            //для последней закуп. цены нельзя брать row.getProduct_price(), т.к. она не учитывает НДС, если он не включен в цену. А row.getProduct_sumprice() учитывает.
            BigDecimal last_purchase_price=row.getProduct_sumprice().divide(row.getProduct_count(),2,BigDecimal.ROUND_HALF_UP);



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
                    15 +","+
                    row.getAcceptance_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantity.add(row.getProduct_count())+","+
                    row.getProduct_count() +","+
                    avgPurchasePrice +","+
                    avgNetcostPrice +","+
                    last_purchase_price+","+//в операциях поступления (оприходование и приёмка) цена закупки (или оприходования) last_purchase_price равна цене операции last_operation_price,
                    last_purchase_price+","+//..в отличии от операций убытия (списания, продажа), где цена закупки остается старой
                    " now())";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method AcceptanceRepository/addAcceptanceProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }


    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(AcceptanceProductForm row, AcceptanceForm request , Long masterId) throws CantSaveProductQuantityException {
        String stringQuery;
        try {
            ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
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
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method AcceptanceRepository/setProductQuantity. ", e);
            throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }


    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteAcceptance (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"186") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"187") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"193") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"194") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            stringQuery = "Update acceptance p" +
                    " set is_archive=true " +
                    " where p.id in ("+delNumbers+")"+
                    " and coalesce(p.is_completed,false) !=true";
            try {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                logger.error("Exception in method AcceptanceRepository/setProductQuantity. SQL-"+stringQuery, e);
                return false;
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
        stringQuery = "select coalesce(max(doc_number)+1,1) from acceptance where company_id="+company_id+" and master_id="+myMasterId;
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
    public Boolean isAcceptanceNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long product_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from acceptance where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(product_id>0) stringQuery=stringQuery+" and id !="+product_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.getResultList().size()>0)
                return false;// код не уникальный
            else return true; // код уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isAcceptanceNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }



//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToAcceptance(UniversalForm request){
        Long acceptanceId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта, ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",acceptanceId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",acceptanceId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",acceptanceId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",acceptanceId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select acceptance_id from acceptance_files where acceptance_id=" + acceptanceId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_AcceptanceId_FileId(acceptanceId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method addFilesToAcceptance.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_AcceptanceId_FileId(Long acceptanceId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into acceptance_files " +
                    "(acceptance_id,file_id) " +
                    "values " +
                    "(" + acceptanceId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method manyToMany_AcceptanceId_FileId. ", ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesAcceptanceJSON> getListOfAcceptanceFiles(Long acceptanceId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           acceptance p" +
                    "           inner join" +
                    "           acceptance_files pf" +
                    "           on p.id=pf.acceptance_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + acceptanceId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";

            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                if (needToSetParameter_MyDepthsIds) {
                    query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
                }

                List<Object[]> queryList = query.getResultList();

                List<FilesAcceptanceJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    FilesAcceptanceJSON doc = new FilesAcceptanceJSON();
                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setDate_time_created((Timestamp) obj[1]);
                    doc.setName((String) obj[2]);
                    doc.setOriginal_name((String) obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            }
            catch (Exception e) {
                logger.error("Exception in method getListOfAcceptanceFiles. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteAcceptanceFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from acceptance_files "+
                    " where acceptance_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from acceptance where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method ______________. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}