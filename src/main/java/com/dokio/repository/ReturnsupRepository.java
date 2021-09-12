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

import com.dokio.message.request.ReturnsupProductTableForm;
import com.dokio.message.response.ReturnsupProductTableJSON;
import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsReturnsupForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsReturnsupJSON;
import com.dokio.message.response.additional.FilesReturnsupJSON;
import com.dokio.message.response.additional.ReturnsupProductsListJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseOversellException;
import com.dokio.repository.Exceptions.CantSaveProductQuantityException;
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
public class ReturnsupRepository {

    Logger logger = Logger.getLogger("ReturnsupRepository");

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
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("cagent","doc_number","name","status_name","product_count","is_completed","company","department","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<ReturnsupJSON> getReturnsupTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//(см. файл Permissions Id)
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
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           cg.name as cagent, " +
                    "           (select count(*) from returnsup_product ip where coalesce(ip.returnsup_id,0)=p.id) as product_count," + //подсчет кол-ва товаров в данной инвентаризации
                    "           coalesce(p.is_completed,false) as is_completed " +  //  завершен?

                    "           from returnsup p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
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

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}
                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<ReturnsupJSON> returnsupList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ReturnsupJSON doc=new ReturnsupJSON();
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
                    doc.setStatus_id(obj[17]!=null?Long.parseLong(obj[17].toString()):null);
                    doc.setStatus_name((String)                   obj[18]);
                    doc.setStatus_color((String)                  obj[19]);
                    doc.setStatus_description((String)            obj[20]);
                    doc.setCagent((String)                        obj[21]);
                    doc.setProduct_count(Long.parseLong(          obj[22].toString()));
                    doc.setIs_completed((Boolean)                 obj[23]);

                    returnsupList.add(doc);
                }
                return returnsupList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnsupTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getReturnsupSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from returnsup p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
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
            logger.error("Exception in method getReturnsupSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<ReturnsupProductTableJSON> getReturnsupProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ip.id  as id, " +
                    " p.name as name," +
                    " ip.product_id," +
                    " ip.product_price," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                    " ip.product_count," +
                    " ip.product_sumprice," +
                    " ip.nds_id, " +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce((select quantity from product_quantity where product_id = ip.product_id and department_id = i.department_id),0) as remains "+ //всего на складе (т.е остаток)

                    " from " +
                    " returnsup_product ip " +
                    " INNER JOIN products p ON ip.product_id=p.id " +
                    " INNER JOIN returnsup i ON ip.returnsup_id=i.id " +
                    " where ip.master_id = " + myMasterId +
                    " and ip.returnsup_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and i.company_id=" + myCompanyId+" and i.department_id in :myDepthsIds and i.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and i.company_id=" + myCompanyId+" and i.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and i.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<ReturnsupProductTableJSON> returnsupList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ReturnsupProductTableJSON doc=new ReturnsupProductTableJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setName((String)                                    obj[1]);
                    doc.setProduct_id(Long.parseLong(                       obj[2].toString()));
                    doc.setProduct_price(                                   obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                    doc.setEdizm((String)                                   obj[4]);
                    doc.setProduct_count(                                   obj[5]==null?BigDecimal.ZERO:(BigDecimal)obj[5]);
                    doc.setProduct_sumprice(                                obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                    doc.setNds_id((Integer)                                 obj[7]);
                    doc.setIndivisible((Boolean)                            obj[8]);
                    doc.setRemains((BigDecimal)                             obj[9]);
                    returnsupList.add(doc);
                }
                return returnsupList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnsupProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }


    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public ReturnsupJSON getReturnsupValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           to_char(p.date_return at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_return, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +  // инвентаризация завершена?
                    "           cg.id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.nds as nds " +

                    "           from returnsup p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;


            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                ReturnsupJSON doc = new ReturnsupJSON();

                for(Object[] obj:queryList){
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
                    doc.setDate_return((String)                   obj[19]);
                    doc.setIs_completed((Boolean)                 obj[20]);
                    doc.setCagent_id(Long.parseLong(              obj[21].toString()));
                    doc.setCagent((String)                        obj[22]);
                    doc.setNds((Boolean)                          obj[23]);

                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnsupValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class, CantInsertProductRowCauseOversellException.class})
    public Integer updateReturnsup(ReturnsupForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(29L,"372") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(29L,"373") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(29L,"374") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(29L,"375") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery =   " update returnsup set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, "+
                    " nds = " + request.getNds() + ", " +
                    " date_return = to_date(:date_return,'DD.MM.YYYY'), " +
                    " is_completed = " + (request.getIs_completed() == null ? false : request.getIs_completed()) + ", " +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("date_return", ((request.getDate_return()==null || request.getDate_return().equals("")) ? dateFormat.format(dateNow) : request.getDate_return()));
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));

                query.executeUpdate();
                if(insertReturnsupProducts(request, request.getId(), myMasterId)){//если сохранение товаров из таблицы товаров прошло успешно
                    if(request.getIs_completed()){//если завершается возврат - запись в историю товара
                        for (ReturnsupProductTableForm row : request.getReturnsupProductTable()) {
                            Boolean isMaterial=productsRepository.isProductMaterial(row.getProduct_id());
                            if (!addReturnsupProductHistory(row, request, myMasterId)) {//      запись в историю товара
                                break;
                            } else {
                                if (isMaterial) { //если товар материален, т.е. это не услуга, работа и т.п.
                                    if (!setReturnsupQuantity(row, request, myMasterId)) {// запись о количестве товара в отделении в отдельной таблице
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    return 1;
                } else return null;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/updateReturnsup on updating returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductHistoryException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/addReturnsupProductHistory on updating returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductQuantityException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/setReturnsupQuantity on updating returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/addReturnsupProductHistory on inserting into products_history cause oversell.", e);
                e.printStackTrace();
                return 0;
            }catch (Exception e) {
                logger.error("Exception in method ReturnsupRepository/updateReturnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }

    @SuppressWarnings("Duplicates")
    private Boolean addReturnsupProductHistory(ReturnsupProductTableForm row, ReturnsupForm request , Long masterId) throws CantSaveProductHistoryException, CantInsertProductRowCauseOversellException {
        String stringQuery;
        try {
            //материален ли товар
            Boolean isMaterial=productsRepository.isProductMaterial(row.getProduct_id());
            //берем последнюю запись об истории товара в данном отделении
            ProductHistoryJSON lastProductHistoryRecord =  productsRepository.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
            //последнее количество товара (прибавим его в запросе к тому количеству, которое возвращают, но только если товар материален)
            BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
            //средняя цена закупа - оставляем прежней
            BigDecimal avgPurchasePrice =lastProductHistoryRecord.getAvg_purchase_price();
            //последняя цена покупки
            BigDecimal lastPurchasePrice =lastProductHistoryRecord.getLast_purchase_price();
            //средняя себестоимость - оставляем прежней
            BigDecimal avgNetcostPrice =  lastProductHistoryRecord.getAvg_netcost_price();
            //Цена операции - пока единственный показатель, который меняется у товара в его истории в связи с данным возвратом товара
            BigDecimal lastOperationPrice=row.getProduct_price();// за цену операции берем цену возврата товара поставщику

            if((lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                logger.error("Для возврата поставщику с id = "+request.getId()+", номер документа "+request.getDoc_number()+", количество товара к возврату больше количества товара на складе");
                throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
            }

            stringQuery = " insert into products_history (" +
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
                    29 +","+
                    row.getReturnsup_id() + ","+
                    row.getProduct_id() + ","+
                    (isMaterial?lastQuantity.subtract(row.getProduct_count()):(new BigDecimal(0)))+","+//если товар материален - записываем его кол-во, равное разности прежнего и возвращаемого, иначе 0
                    row.getProduct_count() +","+
                    avgPurchasePrice +","+
                    avgNetcostPrice +","+
                    lastPurchasePrice+","+
                    lastOperationPrice+","+
                    " now())";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (CantInsertProductRowCauseOversellException e) { //т.к. весь метод обёрнут в try, данное исключение ловим сначала здесь и перекидываем в родительский метод updateReturnsup
            e.printStackTrace();
            logger.error("Exception in method ReturnsupRepository/addReturnsupProductHistory (CantInsertProductRowCauseOversellException). ", e);
            throw new CantInsertProductRowCauseOversellException();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method ReturnsupRepository/addReturnsupProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean setReturnsupQuantity(ReturnsupProductTableForm row, ReturnsupForm request , Long masterId) throws CantSaveProductQuantityException {
        String stringQuery;
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
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method ReturnsupRepository/setProductQuantity. SQL query:"+stringQuery, e);
            throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class})
    public Long insertReturnsup(ReturnsupForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDock( request.getCompany_id(), request.getDepartment_id(), 29L, "361", "362", "363");
        if(iCan==Boolean.TRUE)
        {

            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDockId;
            Long doc_number;//номер документа

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"returnsup");

            if(request.getStatus_id()==null)
                request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(), 29));

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery =   "insert into returnsup (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " cagent_id," + //покупатель, возвращающий заказ
                    " date_time_created," + //дата и время создания
                    " doc_number," + //номер заказа
                    " date_return," + //дата возврата
                    " description," +//доп. информация по заказу
                    " status_id,"+//статус инвентаризации
                    " acceptance_id,"+// id родительского документа Приёмка, из которого может быть создан возврат
                    " nds" +
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    request.getCagent_id() + ", "+//покупатель, возвращающий заказ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    " to_date(:date_return,'DD.MM.YYYY'), "+// дата списания
                    " :description, " +//описание
                    request.getStatus_id() + ", " + //статус док-та
                    request.getAcceptance_id() + ", " + //id родительского документа Розничная продажа, из которого может быть создан возврат
                    request.getNds()+")";
            try{
                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("date_return", ((request.getDate_return()==null || request.getDate_return().equals("")) ? dateFormat.format(dateNow) : request.getDate_return()));
                query.executeUpdate();
                stringQuery="select id from returnsup where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDockId=Long.valueOf(query2.getSingleResult().toString());

                if(insertReturnsupProducts(request, newDockId, myMasterId)){
                    return newDockId;
                } else return null;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertReturnsup on inserting into returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertReturnsup on inserting into returnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(iCan==null) return null; else return 0L;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertReturnsupProducts(ReturnsupForm request, Long newDockId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        String productIds = "";
        //сохранение таблицы
        if (request.getReturnsupProductTable()!=null && request.getReturnsupProductTable().size() > 0) {

            for (ReturnsupProductTableForm row : request.getReturnsupProductTable()) {
                row.setReturnsup_id(newDockId);
                insertProductRowResult = saveReturnsupProductTable(row, request.getCompany_id(), myMasterId);  //сохранение таблицы товаров
                if (insertProductRowResult==null) {
                    throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции из-за ошибки записи строки в таблицу товаров returnsup_product
                }
                //копим id сохранённых товаров
                productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
            }
        }
        deleteReturnsupProductTableExcessRows(productIds, request.getId(), myMasterId);
        return true;
    }

    @SuppressWarnings("Duplicates")//  удаляет лишние позиции товаров при сохранении инвентаризации (те позиции, которые ранее были в заказе, но потом их удалили)
    private Boolean deleteReturnsupProductTableExcessRows(String productIds, Long returnsup_id, Long myMasterId) {
        String stringQuery="";
        try {
            stringQuery =   " delete from returnsup_product " +
                    " where returnsup_id=" + returnsup_id +
                    " and master_id=" + myMasterId +
                    (productIds.length()>0?(" and product_id not in (" + productIds + ")"):"");//если во фронте удалили все товары, то удаляем все товары в данном Заказе покупателя
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteReturnsupProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean saveReturnsupProductTable(ReturnsupProductTableForm row, Long company_id, Long master_id) {
        String stringQuery="";
        try {
            stringQuery =
                    " insert into returnsup_product (" +
                            "master_id, " +
                            "company_id, " +
                            "product_id, " +
                            "returnsup_id, " +
                            "product_count, " +
                            "product_price, " +
                            "product_sumprice, " +
                            "nds_id" +
                            ") values (" +
                            master_id + "," +
                            company_id + "," +
                            row.getProduct_id() + "," +
                            row.getReturnsup_id() + "," +
                            row.getProduct_count() + "," +
                            row.getProduct_price() + "," +
                            row.getProduct_sumprice() + "," +
                            row.getNds_id() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT returnsup_product_uq " +// "upsert"  - уникальность по product_id, returnsup_id
                            " DO update set " +
                            " product_id = " + row.getProduct_id() + "," +
                            " returnsup_id = " + row.getReturnsup_id() + "," +
                            " product_count = " + row.getProduct_count() + "," +
                            " product_price = " + row.getProduct_price() + "," +
                            " product_sumprice = " + row.getProduct_sumprice() + "," +
                            " nds_id = " + row.getNds_id();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveReturnsupProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean deleteReturnsupProductTableRow(Long id) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery = " delete from returnsup_product " +
                " where id="+id+" and master_id="+myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.executeUpdate() == 1;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteReturnsupProductTableRow. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsReturnsup(SettingsReturnsupForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_returnsup (" +
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
                            "ON CONFLICT ON CONSTRAINT settings_returnsup_user_uq " +// "upsert"
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
            logger.error("Exception in method saveSettingsReturnsup. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Возврат покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsReturnsupJSON getSettingsReturnsup() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add " +                 // автодобавление товара из формы поиска в таблицу
                "           from settings_returnsup p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsReturnsupJSON returnsupObj=new SettingsReturnsupJSON();

            for(Object[] obj:queryList){
                returnsupObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                returnsupObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                returnsupObj.setStatusOnFinishId(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                returnsupObj.setAutoAdd((Boolean)                              obj[3]);
            }
            return returnsupObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsReturnsup. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean deleteReturnsup (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"364") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"365") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"366") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление документов созданных собой" и id принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"367") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update returnsup p" +
                    " set is_deleted=true, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")" +
                    " and coalesce(p.is_completed,false) !=true";
            try{
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method deleteReturnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean undeleteReturnsup(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"364") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"365") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"366") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление документов созданных собой" и id принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"367") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update returnsup p" +
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
                logger.error("Exception in method undeleteReturnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    public List<ReturnsupProductsListJSON> getReturnsupProductsList(String searchString, Long companyId, Long departmentId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery = "select  p.id as id, " +
                // наименование товара
                "           p.name as name, " +
                // наименование ед. измерения
                "           ei.short_name as edizm, " +
                // картинка
                "           f.name as filename, " +
                // всего единиц товара в отделении (складе)
                "           (select coalesce(quantity,0)   from product_quantity     where department_id = "    + departmentId +" and product_id = p.id) as remains, " +
                // НДС
                "           coalesce(p.nds_id,null)  as nds_id, " +
                // материален ли товар
                " (select is_material from sprav_sys_ppr where id=p.ppr_id) as is_material, " +
                // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                " p.indivisible as indivisible" +

                " from products p " +
                " left outer join product_barcodes pb on pb.product_id=p.id" +
                " left outer join files f on f.id=(select file_id from product_files where product_id=p.id and output_order=1 limit 1)" +
                " left outer join sprav_sys_ppr ssp on ssp.id=p.ppr_id" +
                " left outer join sprav_sys_edizm ei on p.edizm_id=ei.id" +
                " where  p.master_id=" + myMasterId +
                " and coalesce(p.is_archive,false) !=true ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.product_code_free,'fm0000000000') like CONCAT('%',:sg) or "+
                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.article) like upper (CONCAT('%',:sg,'%')) or " +
                    " pb.value = :sg";

            stringQuery = stringQuery + ")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id, f.name, ei.short_name" +

                "  order by p.name asc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty()){query.setParameter("sg", searchString);}

            List<Object[]> queryList = query.getResultList();
            List<ReturnsupProductsListJSON> returnsupList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ReturnsupProductsListJSON product = new ReturnsupProductsListJSON();
                product.setProduct_id(Long.parseLong(                       obj[0].toString()));
                product.setName((String)                                    obj[1]);
                product.setEdizm((String)                                   obj[2]);
                product.setFilename((String)                                obj[3]);
                product.setRemains(                                         obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                product.setNds_id((Integer)                                 obj[5]);
                product.setIs_material((Boolean)                            obj[6]);
                product.setIndivisible((Boolean)                            obj[7]);
                returnsupList.add(product);
            }
            return returnsupList;
        } catch (Exception e) {
            logger.error("Exception in method getReturnsupProductsList. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<LinkedDocsJSON> getReturnsupLinkedDocsList(Long docId, String docName) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String tableName=(docName.equals("writeoff")?"writeoff":"");//не могу воткнуть имя таблицы параметром, т.к. the parameters can come from the outside and could take any value, whereas the table and column names are static.
        stringQuery =   " select " +
                " ap.id," +
                " to_char(ap.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI'), " +
                " ap.description," +
                " coalesce(ap.is_completed,false)," +
                " ap.doc_number" +
                " from "+tableName+" ap" +
                " where ap.master_id = " + myMasterId +
                " and coalesce(ap.is_archive,false)!=true "+
                " and ap.returnsup_id = " + docId;
        stringQuery = stringQuery + " order by ap.date_time_created asc ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsJSON> returnsupList = new ArrayList<>();
            for(Object[] obj:queryList){
                LinkedDocsJSON doc=new LinkedDocsJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setDate_time_created((String)               obj[1]);
                doc.setDescription((String)                     obj[2]);
                doc.setIs_completed((Boolean)                   obj[3]);
                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
                returnsupList.add(doc);
            }
            return returnsupList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getReturnsupLinkedDocsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean addFilesToReturnsup(UniversalForm request){
        Long returnsupId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"372") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",returnsupId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"373") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",returnsupId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"374") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",returnsupId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"375") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",returnsupId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select returnsup_id from returnsup_files where returnsup_id=" + returnsupId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_ReturnsupId_FileId(returnsupId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method ReturnsupRepository/addFilesToReturnsup.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_ReturnsupId_FileId(Long returnsupId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into returnsup_files " +
                    "(returnsup_id,file_id) " +
                    "values " +
                    "(" + returnsupId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method ReturnsupRepository/manyToMany_ReturnsupId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesReturnsupJSON> getListOfReturnsupFiles(Long returnsupId) {
        if(securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//Просмотр документов
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
                    "           returnsup p" +
                    "           inner join" +
                    "           returnsup_files pf" +
                    "           on p.id=pf.returnsup_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + returnsupId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                List<FilesReturnsupJSON> returnsupList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesReturnsupJSON doc=new FilesReturnsupJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnsupList.add(doc);
                }
                return returnsupList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfReturnsupFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteReturnsupFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"372") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"373") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"374") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"375") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from returnsup_files "+
                    " where returnsup_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from returnsup where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method ReturnsupRepository/deleteReturnsupFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}

