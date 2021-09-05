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

import com.dokio.message.request.WriteoffForm;
import com.dokio.message.request.WriteoffProductForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.ProductHistoryJSON;
import com.dokio.message.response.WriteoffJSON;
import com.dokio.message.response.FilesWriteoffJSON;
import com.dokio.repository.Exceptions.*;
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
public class WriteoffRepositoryJPA {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    private ProductsRepositoryJPA productsRepositoryJPA;

    Logger logger = Logger.getLogger("WriteoffRepository");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","writeoff_date_sort","company","department","creator","date_time_created_sort","description","is_completed")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<WriteoffJSON> getWriteoffTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            boolean needToSetParameter_MyDepthsIds = false;
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
                    "           dp.name || ' ' || dp.address as department, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.writeoff_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as writeoff_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.writeoff_date as writeoff_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from writeoff p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true ";

            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.writeoff_date, 'DD.MM.YYYY') = :sg or "+
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

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<WriteoffJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    WriteoffJSON doc=new WriteoffJSON();
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
                    doc.setWriteoff_date((String)(                 obj[11]));
                    doc.setCompany((String)                       obj[12]);
                    doc.setDate_time_created((String)             obj[13]);
                    doc.setDate_time_changed((String)             obj[14]);
                    doc.setDescription((String)                   obj[15]);
                    doc.setIs_completed((Boolean)                 obj[16]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getWriteoffTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getWriteoffSize(String searchString, int companyId, int departmentId) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        stringQuery = "select  p.id as id " +
                "           from writeoff p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_archive,false) !=true ";

        if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.writeoff_date, 'DD.MM.YYYY') = :sg or "+
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

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getWriteoffSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<WriteoffProductForm> getWriteoffProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.writeoff_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " p.name as name," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                    " ap.reason_id," +
                    " coalesce(ap.additional,'')," +
                    " (select rasons.name from sprav_sys_writeoff rasons where rasons.id = ap.reason_id) as reason" +
                    " from " +
                    " writeoff_product ap " +
                    " INNER JOIN writeoff a ON ap.writeoff_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.writeoff_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and a.company_id=" + myCompanyId+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and a.company_id=" + myCompanyId+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and a.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<WriteoffProductForm> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    WriteoffProductForm doc=new WriteoffProductForm();
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setWriteoff_id(Long.parseLong(                      obj[1].toString()));
                    doc.setProduct_count((BigDecimal)                       obj[2]);
                    doc.setProduct_price((BigDecimal)                       obj[3]);
                    doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                    doc.setName((String)                                    obj[5]);
                    doc.setEdizm((String)                                   obj[6]);
                    doc.setReason_id((Integer)                              obj[7]);
                    doc.setAdditional((String)                              obj[8]);
                    doc.setReason((String)                                  obj[9]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getWriteoffProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public WriteoffJSON getWriteoffValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//см. _Permissions Id.txt
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
                    "           to_char(p.writeoff_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as writeoff_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.writeoff_date as writeoff_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from writeoff p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";
            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                if (needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {
                    query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
                }

                List<Object[]> queryList = query.getResultList();

                WriteoffJSON returnObj = new WriteoffJSON();

                for (Object[] obj : queryList) {
                    returnObj.setId(Long.parseLong(obj[0].toString()));
                    returnObj.setMaster((String) obj[1]);
                    returnObj.setCreator((String) obj[2]);
                    returnObj.setChanger((String) obj[3]);
                    returnObj.setMaster_id(Long.parseLong(obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(obj[5].toString()));
                    returnObj.setChanger_id(obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                    returnObj.setCompany_id(Long.parseLong(obj[7].toString()));
                    returnObj.setDepartment_id(Long.parseLong(obj[8].toString()));
                    returnObj.setDepartment((String) obj[9]);
                    returnObj.setDoc_number(Long.parseLong(obj[10].toString()));
                    returnObj.setWriteoff_date((String) (obj[11]));
                    returnObj.setCompany((String) obj[12]);
                    returnObj.setDate_time_created((String) obj[13]);
                    returnObj.setDate_time_changed((String) obj[14]);
                    returnObj.setDescription((String) obj[15]);
                    returnObj.setIs_completed((Boolean) obj[16]);
                }
                return returnObj;
            }
            catch (Exception e){
                e.printStackTrace();
                logger.error("Exception in method WriteoffRepository/getWriteoffValuesById. stringQuery=" + stringQuery, e);
                return null;
            }


        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public Long insertWriteoff(WriteoffForm request) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDock( request.getCompany_id(), request.getDepartment_id(), 17L, "216", "217", "218");
        if(iCan==Boolean.TRUE)
        {

            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDockId;
            Long doc_number;//номер документа

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"writeoff");

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =   "insert into writeoff (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " date_time_created," + //дата и время создания
                    " doc_number," + //номер заказа
                    " description," +//доп. информация по заказу
                    " inventory_id, " + //если документ создаётся из Инвенторизации - тут будет ее id
                    " return_id, " + //если документ создаётся из Возврата покупателя - тут будет его id
                    " writeoff_date " +// дата списания
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    " :description, " +//описание
                    request.getInventory_id() + ", "+//
                    request.getReturn_id() + ", "+//
                    " to_date(:writeoff_date,'DD.MM.YYYY')) ";// дата списания
            try {

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                //если дата не пришла (это может быть, если создаем из Инвентаризации) - нужно вставить текукщую
                query.setParameter("writeoff_date", ((request.getWriteoff_date()==null || request.getWriteoff_date().equals("")) ? dateFormat.format(dateNow) : request.getWriteoff_date()));
                query.executeUpdate();
                stringQuery = "select id from writeoff where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id=" + myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDockId = Long.valueOf(query2.getSingleResult().toString());

                //если есть таблица с товарами - нужно создать их
                insertWriteoffProducts(request, newDockId, myMasterId);
                return newDockId;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertWriteoff on inserting into writeoff cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertWriteoff. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(iCan==null) return null; else return 0L;
        }
    }

    //сохранение таблицы товаров
    @SuppressWarnings("Duplicates")
    private boolean insertWriteoffProducts(WriteoffForm request, Long parentDockId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> productIds=new HashSet<>();

        if (request.getWriteoffProductTable()!=null && request.getWriteoffProductTable().size() > 0) {
            for (WriteoffProductForm row : request.getWriteoffProductTable()) {
                row.setWriteoff_id(parentDockId);// чтобы через API сюда нельзя было подсунуть рандомный id
                if (!saveWriteoffProductTable(row, myMasterId)) {
                    throw new CantInsertProductRowCauseErrorException();
                }
                productIds.add(row.getProduct_id());
            }
        }
        if (!deleteWriteoffProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", parentDockId)){
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseOversellException.class, CantInsertProductRowCauseErrorException.class, CantSaveProductQuantityException.class, InsertProductHistoryExceprions.class})
    public  Integer updateWriteoff(WriteoffForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого редактируют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"227") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"228") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"229") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"230") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            if(updateWriteoffWithoutTable(request)){                                      //метод 1
                try {//сохранение таблицы товаров
                    insertWriteoffProducts(request,request.getId(),myMasterId);
                    //если завершается списание - запись в историю товара
                    if(request.isIs_completed()){
                        for (WriteoffProductForm row : request.getWriteoffProductTable()) {
                            if (!addWriteoffProductHistory(row, request, myMasterId)) {//         //метод 3
                                break;
                            } else {//если запись в историю изменений товара прошла успешно
                                if (!setProductQuantity(row, request, myMasterId)) {// запись о количестве товара в отделении в отдельной таблице
                                    break;
                                }
                            }
                        }
                    }
                    return 1;

                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateWriteoff on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateWriteoff on inserting into writeoff_products cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateWriteoff on inserting into products_history.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseOversellException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateWriteoff on inserting into products_history cause error.", e);
                    e.printStackTrace();
                    return 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method WriteoffRepository/updateWriteoff. ", e);
                    return null;
                }

            } else return null;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    private Boolean updateWriteoffWithoutTable(WriteoffForm request) {

        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery;
        stringQuery =   " update writeoff set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, "+
                " doc_number =" + request.getDoc_number() + "," +
                " is_completed = " + request.isIs_completed() + "," +
                " writeoff_date = to_date(:writeoff_date,'DD.MM.YYYY') " +
                " where " +
                " id= "+request.getId() +
                " and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
            query.setParameter("writeoff_date", (request.getWriteoff_date() == "" ? null :request.getWriteoff_date()));

            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method WriteoffRepository/updateWriteoffWithoutTable. stringQuery=" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    private Boolean saveWriteoffProductTable(WriteoffProductForm row, Long myMasterId){
        try {
                    String stringQuery;

                    stringQuery = " insert into writeoff_product (" +
                            "product_id," +
                            "writeoff_id," +
                            "product_count," +
                            "product_price," +
                            "product_sumprice," +
                            "reason_id," +
                            "additional" +
                            ") values ("
                            + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит
                            + "(select id from writeoff where id="+row.getWriteoff_id() +" and master_id="+myMasterId+"),"
                            + row.getProduct_count() + ","
                            + row.getProduct_price() + ","
                            + row.getProduct_sumprice() + ","
                            + row.getReason_id() + ", "
                            + ":additional)" +
                            "ON CONFLICT ON CONSTRAINT writeoff_product_uq " +// "upsert"
                            " DO update set " +
                            " product_id = " + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+")," +
                            " writeoff_id = " + "(select id from writeoff where id="+row.getWriteoff_id() +" and master_id="+myMasterId+")," +
                            " product_count = " + row.getProduct_count() + "," +
                            " product_price = " + row.getProduct_price() + "," +
                            " product_sumprice = " + row.getProduct_sumprice() + "," +
                            " reason_id = "  + row.getReason_id() + "," +
                            " additional = :additional";
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("additional", (row.getAdditional() == null ? "" : row.getAdditional()));
                    query.executeUpdate();
                    return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method WriteoffRepository/saveWriteoffProductTable. ", e);
                return false;
            }
    }

    private Boolean deleteWriteoffProductTableExcessRows(String productIds, Long writeoff_id) {
        String stringQuery;
        stringQuery =   " delete from writeoff_product " +
                    " where writeoff_id=" + writeoff_id +
                    " and product_id not in (" + productIds + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method WriteoffRepository/deleteWriteoffProductTableExcessRows. SQL - "+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean addWriteoffProductHistory(WriteoffProductForm row, WriteoffForm request , Long masterId) throws CantSaveProductHistoryException, CantInsertProductRowCauseOversellException {
        String stringQuery;

            ProductHistoryJSON lastProductHistoryRecord =  productsRepositoryJPA.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
            BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
            BigDecimal lastAvgPurchasePrice= lastProductHistoryRecord.getAvg_purchase_price();
            BigDecimal lastAvgNetcostPrice= lastProductHistoryRecord.getAvg_netcost_price();
            BigDecimal lastPurchasePrice= lastProductHistoryRecord.getLast_purchase_price();
            //необходимо проверить, что списываем количество товара не более доступного количества.
            //вообще, в БД установлено ограничение на кол-во товара >=0, и отмену транзакции мог бы сделать CantSaveProductHistoryException в блоке catch,
            //но данным исключением мы уточним, от чего именно произошла отмена транзакции:
            if((lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                logger.error("Для Списания с id = "+request.getId()+", номер документа "+request.getDoc_number()+", количество товара к списанию больше количества товара на складе");
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
                    17 +","+
                    row.getWriteoff_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantity.subtract(row.getProduct_count())+","+
                    row.getProduct_count().multiply(new BigDecimal(-1)) +","+
                    lastAvgPurchasePrice +","+
                    lastAvgNetcostPrice +","+
                    lastPurchasePrice+","+
                    row.getProduct_price()+","+
                    " now())";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method WriteoffRepository/addWriteoffProductHistory. SQL-"+stringQuery, e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }


    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(WriteoffProductForm row, WriteoffForm request , Long masterId) throws CantSaveProductQuantityException {
        String stringQuery;
        try {
            ProductHistoryJSON lastProductHistoryRecord =  productsRepositoryJPA.getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
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
            logger.error("Exception in method WriteoffRepository/setProductQuantity. ", e);
            throw new CantSaveProductQuantityException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean deleteWriteoff (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if     ((securityRepositoryJPA.userHasPermissions_OR(17L, "219") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L, "220") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff", delNumbers)) ||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L, "221") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff", delNumbers)) ||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L, "222") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff", delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            stringQuery = "Update writeoff p" +
                    " set is_archive=true " +
                    " where p.id in (" + delNumbers + ")" +
                    " and coalesce(p.is_completed,false) !=true";
            try {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method WriteoffRepository/deleteWriteoff. stringQuery-" + stringQuery, e);
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
        stringQuery = "select coalesce(max(doc_number)+1,1) from writeoff where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method WriteoffRepository/generateDocNumberCode. stringQuery=" + stringQuery, e);
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isWriteoffNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long doc_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from writeoff where " +
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
            logger.error("Exception in method WriteoffRepository/isWriteoffNumberUnical. stringQuery=" + stringQuery, e);
            return true;
        }
    }


//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToWriteoff(UniversalForm request){
        Long writeoffId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"227") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff",writeoffId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"228") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",writeoffId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"229") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",writeoffId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"230") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",writeoffId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select writeoff_id from writeoff_files where writeoff_id=" + writeoffId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_WriteoffId_FileId(writeoffId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method WriteoffRepository/addFilesToWriteoff.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_WriteoffId_FileId(Long writeoffId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into writeoff_files " +
                    "(writeoff_id,file_id) " +
                    "values " +
                    "(" + writeoffId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method WriteoffRepository/manyToMany_WriteoffId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesWriteoffJSON> getListOfWriteoffFiles(Long writeoffId) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//Просмотр документов
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
                    "           writeoff p" +
                    "           inner join" +
                    "           writeoff_files pf" +
                    "           on p.id=pf.writeoff_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + writeoffId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                List<FilesWriteoffJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesWriteoffJSON doc=new FilesWriteoffJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfWriteoffFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteWriteoffFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"227") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"228") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"229") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"230") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from writeoff_files "+
                    " where writeoff_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from writeoff where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method WriteoffRepository/deleteWriteoffFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}
