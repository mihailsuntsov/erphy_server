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
import com.dokio.message.request.Settings.SettingsReturnForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsReturnJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesReturnJSON;
import com.dokio.message.response.additional.ReturnProductsListJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
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
public class ReturnRepository {

    Logger logger = Logger.getLogger("ReturnRepository");

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
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;


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
    public List<ReturnJSON> getReturnTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(28L, "352,353,354,355"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            boolean needToSetParameter_MyDepthsIds = false;
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String dateFormat=userRepositoryJPA.getMyDateFormat();

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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           cg.name as cagent, " +
                    "           (select count(*) from return_product ip where coalesce(ip.return_id,0)=p.id) as product_count," + //подсчет кол-ва товаров в данной инвентаризации
                    "           coalesce(p.is_completed,false) as is_completed " +  //  завершен?

                    "           from return p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(28L, "352")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(28L, "353")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(28L, "354")) //Если нет прав на просмотр всех доков в своих подразделениях
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
                List<ReturnJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ReturnJSON doc=new ReturnJSON();
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

                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getReturnSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from return p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(28L, "352")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(28L, "353")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(28L, "354")) //Если нет прав на просмотр всех доков в своих подразделениях
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
            logger.error("Exception in method getReturnSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<ReturnProductTableJSON> getReturnProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(28L, "352,353,354,355"))//(см. файл Permissions Id)
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
                    " ip.product_netcost," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                    " ip.product_count," +
                    " ip.product_sumprice," +
                    " ip.product_sumnetcost," +
                    " ip.nds_id, " +
                    " p.indivisible as indivisible" +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)

                    " from " +
                    " return_product ip " +
                    " INNER JOIN products p ON ip.product_id=p.id " +
                    " INNER JOIN return i ON ip.return_id=i.id " +
                    " where ip.master_id = " + myMasterId +
                    " and ip.return_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(28L, "352")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(28L, "353")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(28L, "354")) //Если нет прав на просмотр всех доков в своих подразделениях
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
                List<ReturnProductTableJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ReturnProductTableJSON doc=new ReturnProductTableJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setName((String)                                    obj[1]);
                    doc.setProduct_id(Long.parseLong(                       obj[2].toString()));
                    doc.setProduct_price(                                   obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                    doc.setProduct_netcost(                                 obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                    doc.setEdizm((String)                                   obj[5]);
                    doc.setProduct_count(                                   obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                    doc.setProduct_sumprice(                                obj[7]==null?BigDecimal.ZERO:(BigDecimal)obj[7]);
                    doc.setProduct_sumnetcost(                              obj[8]==null?BigDecimal.ZERO:(BigDecimal)obj[8]);
                    doc.setNds_id((Integer)                                 obj[9]);
                    doc.setIndivisible((Boolean)                            obj[10]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }


    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public ReturnJSON getReturnValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(28L, "352,353,354,355"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            String dateFormat=userRepositoryJPA.getMyDateFormat();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           to_char(p.date_return at time zone '"+myTimeZone+"', '"+dateFormat+"') as date_return, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +  // инвентаризация завершена?
                    "           cg.id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.nds as nds, " +
                    "           p.uid as uid" +

                    "           from return p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;


            if (!securityRepositoryJPA.userHasPermissions_OR(28L, "352")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(28L, "353")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(28L, "354")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                ReturnJSON doc = new ReturnJSON();

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
                    doc.setUid((String)                           obj[24]);

                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class,Exception.class})
    public Integer updateReturn(ReturnForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(28L,"356") && securityRepositoryJPA.isItAllMyMastersDocuments("return",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(28L,"357") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(28L,"358") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(28L,"359") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                        !(      //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(28L,"619") && securityRepositoryJPA.isItAllMyMastersDocuments("return",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(28L,"620") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",request.getId().toString()))||
                                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                                (securityRepositoryJPA.userHasPermissions_OR(28L,"621") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",request.getId().toString()))||
                                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                                (securityRepositoryJPA.userHasPermissions_OR(28L,"622") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",request.getId().toString()))
                        )
                ) return -1;
            }

            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всему возврату
            String stringQuery;
            stringQuery =   " update return set " +
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

                //сохранение товаров
                insertReturnProducts(request, request.getId(), myMasterId);

                //если возврат ПРОВОДИТСЯ - запись в историю товара
                if(request.getIs_completed()){
                    for (ReturnProductTableForm row : request.getReturnProductTable()) {
                        docProductsSum=docProductsSum.add(row.getProduct_sumprice());

                        addProductHistory(row, request, myMasterId);

                    }
                    // обновляем баланс с контрагентом
                    commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "return","return", request.getId(), docProductsSum, new BigDecimal(0),true,request.getDoc_number(),request.getStatus_id());//при возврате покупателя баланс с ним должен смещаться в положительную сторону, т.е. в наш долг покупателю
                }
                return 1;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/insertReturn on updating return_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductHistoryException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/addReturnProductHistory on updating return_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductQuantityException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/setReturnQuantity on updating return_products cause error.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/updateReturn. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CalculateNetcostNegativeSumException.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setReturnAsDecompleted(ReturnForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(28L,"619") && securityRepositoryJPA.isItAllMyMastersDocuments("return",request.getId().toString())) ||
                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(28L,"620") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",request.getId().toString()))||
                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(28L,"621") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",request.getId().toString()))||
                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(28L,"622") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",request.getId().toString()))
        )
        {
            if(request.getReturnProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update return set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "return"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();



                //сохранение истории движения товара
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                request.setIs_completed(false);
                BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всем товарам документа
                for (ReturnProductTableForm row : request.getReturnProductTable()) {
                    docProductsSum=docProductsSum.add(row.getProduct_sumprice());
                    addProductHistory(row, request, myMasterId);
                }
                // обновляем баланс с контрагентом
                commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "return","return", request.getId(), docProductsSum,new BigDecimal(0),false, request.getDoc_number().toString(),request.getStatus_id());//при приёмке баланс с контрагентом должен смещаться в положительную сторону, т.е. в наш долг контрагенту
                return 1;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/addProductHistory on inserting into products_history cause oversell.", e);
                e.printStackTrace();
                return -80;
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (setReturnAsDecompleted).", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/setReturnAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/setReturnAsDecompleted.", e);
                e.printStackTrace();
                return -80; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnRepository/setReturnAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    @SuppressWarnings("Duplicates")
    private Boolean addProductHistory(ReturnProductTableForm row, ReturnForm request , Long masterId) throws Exception {
        try {

            // все записи в таблицы product_history и product_quantity производим только если товар материален (т.е. это не услуга и т.п.)
            if (productsRepository.isProductMaterial(row.getProduct_id())) {
                // загружаем настройки, чтобы узнать политику предприятия по подсчёту себестоимости (по всему предприятию или по каждому отделению отдельно)
                String netcostPolicy = commonUtilites.getCompanySettings(request.getCompany_id()).getNetcost_policy();
                // берём информацию о товаре (кол-во и ср. себестоимость) в данном отделении (если netcostPolicy == "all" то независимо от отделения)
                ProductHistoryJSON productInfo = productsRepository.getProductQuantityAndNetcost(masterId, request.getCompany_id(), row.getProduct_id(), netcostPolicy.equals("each") ? request.getDepartment_id() : null);
                // актуальное количество товара В ОТДЕЛЕНИИ
                // Используется для записи нового кол-ва товара в отделении путем сложения lastQuantity и row.getProduct_count()
                // если политика подсчета себестоимости ПО КАЖДОМУ отделению - lastQuantity отдельно высчитывать не надо - она уже высчитана шагом ранее в productInfo
                BigDecimal lastQuantity =  netcostPolicy.equals("each") ? productInfo.getQuantity() : productsRepository.getProductQuantity(masterId, request.getCompany_id(), row.getProduct_id(), request.getDepartment_id());
                // имеющееся количество (если учёт себестоимости по отделениям - то В ОТДЕЛЕНИИ, если по всему предприятию - то кол-во ВО ВСЕХ ОТДЕЛЕНИЯХ.)
                // Используется для расчёта себестоимости
                BigDecimal availableQuantity = netcostPolicy.equals("each") ? lastQuantity : productInfo.getQuantity();
                // средняя себестоимость уже имеющегося товара
                BigDecimal lastAvgNetcostPrice = productInfo.getAvg_netcost_price();

                // т.к. это  операция поступления, при отмене её проведения необходимо проверить,
                // сколько товара останется после этого, и если это кол-во <0 то не допустить этого
                if(!request.getIs_completed() && (lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                    logger.error("Для отмены приёмки с id = "+request.getId()+", номер документа "+request.getDoc_number()+", количество товара к выбытию со склада больше количества товара на складе");
                    throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
                }

                Timestamp timestamp = new Timestamp(((Date) commonUtilites.getFieldValueFromTableById("return", "date_time_created", masterId, request.getId())).getTime());

                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        request.getDepartment_id(),
                        28,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count(),
                        row.getProduct_price(),
                        row.getProduct_netcost(),
                        timestamp,
                        request.getIs_completed()
                );

                // новая средняя себестоимость
                BigDecimal avgNetcostPrice;
                if (request.getIs_completed())   // Если проводим, то считаем по формуле
                    // ((ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
                    avgNetcostPrice = ((availableQuantity.multiply(lastAvgNetcostPrice)).add(row.getProduct_count().multiply(row.getProduct_netcost()))).divide(availableQuantity.add(row.getProduct_count()), 2, BigDecimal.ROUND_HALF_UP);
                else // Если снимаем с проведения, то пересчитываем на основании прежних движений товара
                    avgNetcostPrice = productsRepository.recountProductNetcost(request.getCompany_id(), request.getDepartment_id(), row.getProduct_id());

                if (request.getIs_completed())   // Если проводим
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            request.getDepartment_id(),
                            lastQuantity.add(row.getProduct_count()),
                            avgNetcostPrice
                    );
                else                            // Если снимаем с проведения
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            request.getDepartment_id(),
                            lastQuantity.subtract(row.getProduct_count()),
                            avgNetcostPrice
                    );
            }

            return true;

        }catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (addProductHistory).", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantSaveProductQuantityException e) {
            logger.error("Exception in method addProductHistory on inserting into product_quantity cause error.", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantInsertProductRowCauseOversellException e) {
            logger.error("Exception in method addProductHistory on inserting into product_quantity cause error - Not enough product count.", e);
            e.printStackTrace();
            throw new CantInsertProductRowCauseOversellException();
        } catch (CantSaveProductHistoryException e) {
            logger.error("Exception in method addProductHistory on inserting into product_history.", e);
            e.printStackTrace();
            throw new CantSaveProductHistoryException();
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method addProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class})
    public Long insertReturn(ReturnForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDoc( request.getCompany_id(), request.getDepartment_id(), 28L, "345", "346", "347");
        if(iCan==Boolean.TRUE)
        {

            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDocId;
            Long doc_number;//номер документа
            Long linkedDocsGroupId=null;

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"return");

            if(request.getStatus_id()==null)
                request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(), 28));

            //если документ создается из другого документа
            if (request.getLinked_doc_id() != null) {
                //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
            }

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery =   "insert into return (" +
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
                    " retail_sales_id,"+// id родительского документа Розничная продажа, из которого может быть создан возврат
                    " uid," +//
                    " linked_docs_group_id," +// id группы связанных документов
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
                    request.getRetail_sales_id() + ", " + //id родительского документа Розничная продажа, из которого может быть создан возврат
                    " :uid, " + //uid
                    linkedDocsGroupId+"," + // id группы связанных документов
                    request.getNds()+")";
            try{
                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("date_return", ((request.getDate_return()==null || request.getDate_return().equals("")) ? dateFormat.format(dateNow) : request.getDate_return()));
                query.setParameter("uid", (request.getUid() == null ? "" : request.getUid()));
                query.executeUpdate();
                stringQuery="select id from return where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDocId=Long.valueOf(query2.getSingleResult().toString());

                // сохраняем товарные позции
                insertReturnProducts(request, newDocId, myMasterId);

                //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                if (request.getLinked_doc_id() != null) {
                    linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "return", request.getUid(), request.getCompany_id(), myMasterId);
                }

                    return newDocId;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertReturn on inserting into return_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertReturn on inserting into return. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //-1
            if(iCan==null) return null; else return -1L;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertReturnProducts(ReturnForm request, Long newDocId, Long myMasterId) throws Exception {
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        String productIds = "";
        //сохранение таблицы
        if (request.getReturnProductTable()!=null && request.getReturnProductTable().size() > 0) {

            for (ReturnProductTableForm row : request.getReturnProductTable()) {
                row.setReturn_id(newDocId);
                insertProductRowResult = saveReturnProductTable(row, request.getCompany_id(), myMasterId);  //сохранение таблицы товаров
                if (insertProductRowResult==null) {
                    throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции из-за ошибки записи строки в таблицу товаров return_product
                }
                //копим id сохранённых товаров
                productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
            }
        }
        deleteReturnProductTableExcessRows(productIds, request.getId(), myMasterId);
        return true;
    }

    @SuppressWarnings("Duplicates")//  удаляет лишние позиции товаров при сохранении инвентаризации (те позиции, которые ранее были в заказе, но потом их удалили)
    private Boolean deleteReturnProductTableExcessRows(String productIds, Long return_id, Long myMasterId) {
        String stringQuery="";
        try {
            stringQuery =   " delete from return_product " +
                    " where return_id=" + return_id +
                    " and master_id=" + myMasterId +
                    (productIds.length()>0?(" and product_id not in (" + productIds + ")"):"");//если во фронте удалили все товары, то удаляем все товары в данном Заказе покупателя
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteReturnProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean saveReturnProductTable(ReturnProductTableForm row, Long company_id, Long master_id) {
        String stringQuery="";
        try {
            stringQuery =
                    " insert into return_product (" +
                            "master_id, " +
                            "company_id, " +
                            "product_id, " +
                            "return_id, " +
                            "product_netcost, " +
                            "product_count, " +
                            "product_price, " +
                            "product_sumprice, " +
                            "product_sumnetcost, " +
                            "nds_id" +
                            ") values (" +
                            master_id + "," +
                            company_id + "," +
                            row.getProduct_id() + "," +
                            row.getReturn_id() + "," +
                            row.getProduct_netcost() + "," +
                            row.getProduct_count() + "," +
                            row.getProduct_price() + "," +
                            row.getProduct_sumprice() + "," +
                            row.getProduct_sumnetcost() + "," +
                            row.getNds_id() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT return_product_uq " +// "upsert"  - уникальность по product_id, return_id
                            " DO update set " +
                            " product_id = " + row.getProduct_id() + "," +
                            " return_id = " + row.getReturn_id() + "," +
                            " product_netcost = " + row.getProduct_netcost() + "," +
                            " product_count = " + row.getProduct_count() + "," +
                            " product_price = " + row.getProduct_price() + "," +
                            " product_sumprice = " + row.getProduct_sumprice() + "," +
                            " product_sumnetcost = " + row.getProduct_sumnetcost() + "," +
                            " nds_id = " + row.getNds_id();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveReturnProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean deleteReturnProductTableRow(Long id) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery = " delete from return_product " +
                " where id="+id+" and master_id="+myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.executeUpdate() == 1;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteReturnProductTableRow. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsReturn(SettingsReturnForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_return (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "department_id, " +         //отделение по умолчанию
                            "status_on_finish_id, "+    //статус документа при завершении инвентаризации
                            "show_kkm, "+               //показывать блок ККМ
                            "auto_add"+                 // автодобавление товара из формы поиска в таблицу
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            row.getDepartmentId() + "," +
                            row.getStatusOnFinishId() + "," +
                            row.getShowKkm() + "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_return_user_uq " +// "upsert"
                            " DO update set " +
                            "  department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", show_kkm = "+row.getShowKkm()+
                            ", auto_add = "+row.getAutoAdd();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsReturn. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Возврат покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsReturnJSON getSettingsReturn() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add, " +                 // автодобавление товара из формы поиска в таблицу
                "           coalesce(p.show_kkm,false) as show_kkm  " +                 // показывать блок ККМ
                "           from settings_return p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsReturnJSON returnObj=new SettingsReturnJSON();

            for(Object[] obj:queryList){
                returnObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                returnObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                returnObj.setStatusOnFinishId(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                returnObj.setAutoAdd((Boolean)                              obj[3]);
                returnObj.setShowKkm((Boolean)                              obj[4]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsReturn. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteReturn (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(28L,"348") && securityRepositoryJPA.isItAllMyMastersDocuments("return",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(28L,"349") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(28L,"350") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",delNumbers))||
                //Если есть право на "Удаление документов созданных собой" и id принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(28L,"351") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",delNumbers)))
        {// сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "return");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update return p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "return")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteReturn. SQL query:" + stringQuery, e);
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
    public Integer undeleteReturn(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(28L,"348") && securityRepositoryJPA.isItAllMyMastersDocuments("return",delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(28L,"349") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",delNumbers))||
            //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(28L,"350") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",delNumbers))||
            //Если есть право на "Удаление документов созданных собой" и id принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(28L,"351") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update return p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteReturn. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    public List<ReturnProductsListJSON> getReturnProductsList(String searchString, Long companyId, Long departmentId) {
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
            List<ReturnProductsListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ReturnProductsListJSON product = new ReturnProductsListJSON();
                product.setProduct_id(Long.parseLong(                       obj[0].toString()));
                product.setName((String)                                    obj[1]);
                product.setEdizm((String)                                   obj[2]);
                product.setFilename((String)                                obj[3]);
                product.setRemains(                                         obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                product.setNds_id((Integer)                                 obj[5]);
                product.setIs_material((Boolean)                            obj[6]);
                product.setIndivisible((Boolean)                            obj[7]);
                returnList.add(product);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getReturnProductsList. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<LinkedDocsJSON> getReturnLinkedDocsList(Long docId, String docName) {
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
                " and coalesce(ap.is_deleted,false)!=true "+
                " and ap.return_id = " + docId;
        stringQuery = stringQuery + " order by ap.date_time_created asc ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                LinkedDocsJSON doc=new LinkedDocsJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setDate_time_created((String)               obj[1]);
                doc.setDescription((String)                     obj[2]);
                doc.setIs_completed((Boolean)                   obj[3]);
                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getReturnLinkedDocsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
    //*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean addFilesToReturn(UniversalForm request){
        Long returnId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(28L,"356") && securityRepositoryJPA.isItAllMyMastersDocuments("return",returnId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(28L,"357") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",returnId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(28L,"358") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",returnId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(28L,"359") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",returnId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select return_id from return_files where return_id=" + returnId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_ReturnId_FileId(returnId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method ReturnRepository/addFilesToReturn.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_ReturnId_FileId(Long returnId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into return_files " +
                    "(return_id,file_id) " +
                    "values " +
                    "(" + returnId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method ReturnRepository/manyToMany_ReturnId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesReturnJSON> getListOfReturnFiles(Long returnId) {
        if(securityRepositoryJPA.userHasPermissions_OR(28L, "352,353,354,355"))//Просмотр документов
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
                    "           return p" +
                    "           inner join" +
                    "           return_files pf" +
                    "           on p.id=pf.return_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + returnId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(28L, "352")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(28L, "353")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(28L, "354")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                List<FilesReturnJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesReturnJSON doc=new FilesReturnJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfReturnFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteReturnFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(28L,"356") && securityRepositoryJPA.isItAllMyMastersDocuments("return", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(28L,"357") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("return",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(28L,"358") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("return",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(28L,"359") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("return",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from return_files "+
                    " where return_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from return where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method ReturnRepository/deleteReturnFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}

