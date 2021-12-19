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
import com.dokio.model.*;
import com.dokio.repository.Exceptions.CantSetHistoryCauseNegativeSumException;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class WithdrawalRepositoryJPA {

    Logger logger = Logger.getLogger("WithdrawalRepositoryJPA");

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
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","kassa","creator","summ","company","department","date_time_created_sort","description")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<WithdrawalJSON> getWithdrawalTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(45L, "571,572,573,574"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            boolean needToSetParameter_MyDepthsIds = false;
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           ka.name as kassa, " +// наименование кассового аппарата
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.kassa_id as kassa_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           p.description as description, " +
                    "           coalesce(p.summ,0) as summ, " +
                    "           coalesce(p.is_delivered,false) as is_delivered, " +  //-- деньги доставлены до кассы предприятия (проведён документ Входящий ордер) (false = "зависшие деньги" - между кассой ККМ и кассой предприятия)
                    "           p.boxoffice_id as boxoffice_id, " +
                    "           bx.name as boxoffice, " +
                    "           p.date_time_created as date_time_created_sort " +

                    "           from withdrawal p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN sprav_boxoffice bx ON p.boxoffice_id=bx.id" +
                    "           where  p.master_id=" + myMasterId;

            if (!securityRepositoryJPA.userHasPermissions_OR(45L, "571")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(45L, "572")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(45L, "573")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(bx.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(ka.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
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
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                query.setFirstResult(offsetreal).setMaxResults(result);


                List<Object[]> queryList = query.getResultList();
                List<WithdrawalJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    WithdrawalJSON doc=new WithdrawalJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setKassa((String)                         obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setKassa_id(Long.parseLong(               obj[6].toString()));
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    doc.setDepartment((String)                    obj[9]);
                    doc.setDoc_number(Long.parseLong(             obj[10].toString()));
                    doc.setCompany((String)                       obj[11]);
                    doc.setDate_time_created((String)             obj[12]);
                    doc.setDescription((String)                   obj[13]);
                    doc.setSumm((BigDecimal)                      obj[14]);
                    doc.setIs_delivered((Boolean)                 obj[15]);
                    doc.setBoxoffice_id(obj[16]!=null?Long.parseLong(obj[16].toString()):null);
                    doc.setBoxoffice((String)                     obj[17]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getWithdrawalTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getWithdrawalSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from withdrawal p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                "           INNER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN sprav_boxoffice bx ON p.boxoffice_id=bx.id" +
                "           where  p.master_id=" + myMasterId;

        if (!securityRepositoryJPA.userHasPermissions_OR(45L, "571")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(45L, "572")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(45L, "573")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(bx.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(ka.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
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
            logger.error("Exception in method getWithdrawalSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public WithdrawalJSON getWithdrawalValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(45L, "571,572,573,574"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery = "select   p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           ka.name as kassa, " +// наименование кассового аппарата
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.kassa_id as kassa_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           p.description as description, " +
                    "           coalesce(p.summ,0) as summ, " +
                    "           coalesce(p.is_delivered,false) as is_delivered, " +  //-- деньги доставлены до кассы предприятия (проведён документ Входящий ордер) (false = "зависшие деньги" - между кассой ККМ и кассой предприятия)
                    "           p.boxoffice_id as boxoffice_id, " +
                    "           bx.name as boxoffice, " +
                    "           p.uid as uid " +

                    "           from withdrawal p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN sprav_boxoffice bx ON p.boxoffice_id=bx.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(45L, "571")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(45L, "572")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(45L, "573")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                WithdrawalJSON doc=new WithdrawalJSON();

                for(Object[] obj:queryList){
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setKassa((String)                         obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setKassa_id(Long.parseLong(               obj[6].toString()));
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    doc.setDepartment((String)                    obj[9]);
                    doc.setDoc_number(Long.parseLong(             obj[10].toString()));
                    doc.setCompany((String)                       obj[11]);
                    doc.setDate_time_created((String)             obj[12]);
                    doc.setDescription((String)                   obj[13]);
                    doc.setSumm((BigDecimal)                      obj[14]);
                    doc.setIs_delivered((Boolean)                 obj[15]);
                    doc.setBoxoffice_id(obj[16]!=null?Long.parseLong(obj[16].toString()):null);
                    doc.setBoxoffice((String)                     obj[17]);
                    doc.setUid((String)                           obj[18]);
                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getWithdrawalValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class ,CantSetHistoryCauseNegativeSumException.class})
    public Long insertWithdrawal(WithdrawalForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "withdrawal")){
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
                    (securityRepositoryJPA.userHasPermissions_OR(45L, "568")) ||
                    //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                    (securityRepositoryJPA.userHasPermissions_OR(45L, "569") && myCompanyId.equals(request.getCompany_id())) ||
                    //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                    (securityRepositoryJPA.userHasPermissions_OR(45L, "570") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
                    //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                    DocumentMasterId.equals(myMasterId))
            {
                String stringQuery;
                // Если приходит без id создателя, значит создается не вызовом из модуля кассы ККМ, а из меню документа кнопкой Создать
                if(Objects.isNull(request.getCreator_id()))
                    request.setCreator_id(userRepository.getUserId());// тогда в качествев создателя будет залогиненный пользователь

                Long newDocId;
                Long doc_number;//номер документа( = номер заказа)

                //генерируем номер документа, если его (номера) нет
                if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                    doc_number=Long.valueOf(request.getDoc_number());
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"withdrawal");

                //если документ создается из другого документа
                if (request.getLinked_doc_id() != null) {
                    //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                    linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                    if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery =   "insert into withdrawal (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель-кассир, залогиненный на кассовом модуле (Внимание! не в системе! На кассовом модуле может залогиниться другой человек)
                        " company_id," + //предприятие, для которого создается документ
                        " department_id," + //отделение, из(для) которого создается документ
                        " kassa_id," +//касса
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер документа
                        " description," +//доп. информация по заказу
                        " summ," +// сумма изъятия
                        " linked_docs_group_id," +// id группы связанных документов
                        " boxoffice_id," + // касса предприятия
                        " is_completed," + // проведено (выемка всегда проведена)
                        " uid"+// уникальный идентификатор документа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        request.getCreator_id() + ", "+ //создатель-кассир
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                        request.getKassa_id() + ", "+//касса ККМ
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер документа
                        ":description," +
                        request.getSumm() + ", "+// сумма
                        linkedDocsGroupId+"," + // id группы связанных документов
                        request.getBoxoffice_id()+"," + // касса предприятия
                        "true ," +  // проведено (выемка всегда проведена)
                        ":uid)";// уникальный идентификатор документа
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.executeUpdate();
                    stringQuery="select id from withdrawal where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+request.getCreator_id();
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "withdrawal", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    // обновляем состояние кассы ККМ, вычитая из неё изымаемую сумму
                    commonUtilites.addDocumentHistory("kassa", request.getCompany_id(), request.getKassa_id(), "withdrawal","withdrawal", newDocId, request.getSumm().negate(),request.getDoc_number(),null);

                    return newDocId;
                } catch (CantSetHistoryCauseNegativeSumException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertWithdrawal on inserting into setting kassa financial history (not enougth money).", e);
                    e.printStackTrace();
                    return -30L;
                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertWithdrawal on inserting into withdrawal. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertWithdrawal. UUID: " + request.getUid());
            return null;
        }
    }

    // отдает список выемок по кассе, деньги из которых еще не были доставлены в кассу предприятия (т.е. нет проведения приходного ордера по этим деньгам)
    @SuppressWarnings("Duplicates")
    public List<WithdrawalJSON> getWithdrawalList(Long kassa_id) {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery = "select  p.id as id, " +
                    "           us.name as creator, " +
                    "           '№'||p.doc_number||', '||to_char(p.summ, '9990.99')||' руб.' as kassa, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           coalesce(p.summ,0) as summ " +

                    "           from withdrawal p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.kassa_id = " + kassa_id +
                    "           and coalesce(p.is_completed,false) = true" +
                    "           and coalesce(p.is_delivered,false) = false";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                List<WithdrawalJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    WithdrawalJSON doc=new WithdrawalJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setCreator((String)                       obj[1]);
                    doc.setKassa((String)                         obj[2]);
                    doc.setDepartment((String)                    obj[3]);
                    doc.setDoc_number(Long.parseLong(             obj[4].toString()));
                    doc.setSumm((BigDecimal)                      obj[5]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getWithdrawalList. SQL query:" + stringQuery, e);
                return null;
            }
    }



}
