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

import com.dokio.message.request.CorrectionForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsCorrectionForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.CorrectionJSON;
import com.dokio.message.response.Settings.SettingsCorrectionJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.ExpenditureItemsListForm;
import com.dokio.message.response.additional.FilesUniversalJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.model.Companies;
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CorrectionRepositoryJPA {

    Logger logger = Logger.getLogger("CorrectionRepositoryJPA");

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
            .of("doc_number","name","cagent","status_name","boxoffice","payment_account","company","creator","date_time_created_sort","type","description","is_completed","summ")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<CorrectionJSON> getCorrectionTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(41L, "544,545"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
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
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce(p.summ,0) as summ, " +
                    "           cg.name as cagent, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           sb.name as boxoffice, " +                 //  касса предприятия
                    "           p.type as type, "+                                  // boxoffice - коррекция кассы, cagent - коррекция баланса с контрагентом, account - коррекция расчётного счёта
                    "           coalesce(cpa.payment_account,'')||' ('||cpa.name||')' as payment_account," + //расч счёт предприятия (номер и адрес)
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +

                    "           from correction p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_id=cpa.id " +
                    "           LEFT OUTER JOIN sprav_boxoffice sb ON p.boxoffice_id=sb.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(41L, "544")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(cpa.payment_account) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cpa.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(sb.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
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

                query.setFirstResult(offsetreal).setMaxResults(result);


                List<Object[]> queryList = query.getResultList();
                List<CorrectionJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    CorrectionJSON doc=new CorrectionJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setMaster((String)                                  obj[1]);
                    doc.setCreator((String)                                 obj[2]);
                    doc.setChanger((String)                                 obj[3]);
                    doc.setMaster_id(Long.parseLong(                        obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(                       obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(          obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(                       obj[7].toString()));
                    doc.setDoc_number(Long.parseLong(                       obj[8].toString()));
                    doc.setCompany((String)                                 obj[9]);
                    doc.setDate_time_created((String)                       obj[10]);
                    doc.setDate_time_changed((String)                       obj[11]);
                    doc.setDescription((String)                             obj[12]);
                    doc.setStatus_id(obj[13]!=null?Long.parseLong(          obj[13].toString()):null);
                    doc.setStatus_name((String)                             obj[14]);
                    doc.setStatus_color((String)                            obj[15]);
                    doc.setStatus_description((String)                      obj[16]);
                    doc.setSumm((BigDecimal)                                obj[17]);
                    doc.setCagent((String)                                  obj[18]);
                    doc.setIs_completed((Boolean)                           obj[19]);
                    doc.setBoxoffice( (String)                              obj[20]);
                    doc.setType((String)                                    obj[21]);
                    doc.setPayment_account((String)                         obj[22]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCorrectionTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getCorrectionSize(String searchString, int companyId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from correction p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_id=cpa.id " +
                "           LEFT OUTER JOIN sprav_boxoffice sb ON p.boxoffice_id=sb.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(41L, "544")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие
            stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(cpa.payment_account) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cpa.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(sb.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
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
            logger.error("Exception in method getCorrectionSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public CorrectionJSON getCorrectionValues (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(41L, "544,545"))//см. _Permissions Id.txt
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
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
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.summ,0) as summ, " +
                    "           p.type as type, "+        // boxoffice - коррекция кассы, cagent - коррекция баланса с контрагентом, account - коррекция расчётного счёта
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.uid as uid, " +
                    "           p.is_completed as is_completed, " +

                    "           p.payment_account_id as payment_account_id,"+ // id банковский счёт препдриятия, откуда перемещаем денежные средства"
                    "           coalesce(cpa.payment_account,'')||' ('||cpa.name||')' as payment_account," +//  банковский счёт препдриятия, откуда перемещаем денежные средства"
                    "           p.boxoffice_id as boxoffice_id" +  // касса предприятия (не путать с ККМ!)

                    "           from correction p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_id=cpa.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(41L, "544")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                CorrectionJSON returnObj=new CorrectionJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                                 obj[0].toString()));
                    returnObj.setMaster((String)                                    obj[1]);
                    returnObj.setCreator((String)                                   obj[2]);
                    returnObj.setChanger((String)                                   obj[3]);
                    returnObj.setMaster_id(Long.parseLong(                          obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(                         obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(            obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(                         obj[7].toString()));
                    returnObj.setDoc_number(Long.parseLong(                         obj[8].toString()));
                    returnObj.setCompany((String)                                   obj[9]);
                    returnObj.setDate_time_created((String)                         obj[10]);
                    returnObj.setDate_time_changed((String)                         obj[11]);
                    returnObj.setDescription((String)                               obj[12]);
                    returnObj.setSumm((BigDecimal)                                  obj[13]);
                    returnObj.setType((String)                                      obj[14]);
                    returnObj.setCagent_id(    obj[15]!=null?Long.parseLong(        obj[15].toString()):null);
                    returnObj.setCagent((String)                                    obj[16]);
                    returnObj.setStatus_id(obj[17]!=null?Long.parseLong(            obj[17].toString()):null);
                    returnObj.setStatus_name((String)                               obj[18]);
                    returnObj.setStatus_color((String)                              obj[19]);
                    returnObj.setStatus_description((String)                        obj[20]);
                    returnObj.setUid((String)                                       obj[21]);
                    returnObj.setIs_completed((Boolean)                             obj[22]);
                    returnObj.setPayment_account_id(obj[23]!=null?Long.parseLong(   obj[23].toString()):null);
                    returnObj.setPayment_account((String)                           obj[24]);
                    returnObj.setBoxoffice_id( obj[25]!=null?Long.parseLong(        obj[25].toString()):null);

                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCorrectionValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertCorrection(CorrectionForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "correction")){
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long linkedDocsGroupId=null;

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            if ((   //если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(41L, "540")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(41L, "541") && myCompanyId.equals(request.getCompany_id()))) &&
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
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"correction");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),41));
                }

                //если документ создается из другого документа
                if (request.getLinked_doc_id() != null) {
                    //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                    linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                    if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery = "insert into correction (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер документа
                        " description," +//доп. информация по заказу
                        " status_id,"+//статус
                        " linked_docs_group_id," +// id группы связанных документов
                        " summ,"+
                        " payment_account_id,"+// расчетный счет, с которого перемещаем деньги
                        " type," + // boxoffice - коррекция кассы, cagent - коррекция баланса с контрагентом, account - коррекция расчётного счёта
                        " boxoffice_id," + // касса предприятия (не путать с ККМ!)
                        " uid"+// уникальный идентификатор документа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер документа
                        ":description," +
                        request.getStatus_id()  + ", "+//статус
                        linkedDocsGroupId+"," + // id группы связанных документов
                        request.getSumm()+"," + //наименование заказа поставщику
                        request.getPayment_account_id()+"," + //банковский счет
                        ":type," + //  вид коррекции
                        request.getBoxoffice_id()+"," +
                        ":uid)";// уникальный идентификатор документа
                try{

                    commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("companies_payment_accounts", request.getPayment_account_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_id(), myMasterId);

                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.setParameter("type",request.getType());
                    query.executeUpdate();
                    stringQuery="select id from correction where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "correction", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    return newDocId;

                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertCorrection on inserting into correction. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertCorrection. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer updateCorrection(CorrectionForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(41L,"546") && securityRepositoryJPA.isItAllMyMastersDocuments("correction",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(41L,"547") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                        !(
                                (securityRepositoryJPA.userHasPermissions_OR(41L,"548") && securityRepositoryJPA.isItAllMyMastersDocuments("correction",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(41L,"549") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",request.getId().toString()))
                        )
                ) return -1;
            }

            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            String stringQuery;
            stringQuery =   " update correction set " +
                    " changer_id = " + myId + ","+
                    " type = :type,"+
                    " date_time_changed= now()," +
                    " description = :description, " +
                    " summ=" + request.getSumm()+"," + // сумма коррекции
                    " payment_account_id = " + request.getPayment_account_id()+"," + //банковский счет который корректируют
                    " boxoffice_id = " + request.getBoxoffice_id()+ "," + // касса предприятия (не путать с ККМ!),которую корректируют
                    " cagent_id = " + request.getCagent_id()+ "," + // контрагент, баланс с которым корректируют
                    " is_completed = " + request.getIs_completed() + "," +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId() + " and master_id = " + myMasterId;





            try
            {

                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("companies_payment_accounts", request.getPayment_account_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_id(), myMasterId);

                // проверим, не является ли он уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "correction"))
                    throw new DocumentAlreadyCompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("type",request.getType());
                query.setParameter("description",request.getDescription());
                query.executeUpdate();

                // если проводим документ
                if((request.getIs_completed()==null?false:request.getIs_completed())){
                    // определим тип корректировки. boxoffice - коррекция кассы, cagent - коррекция баланса с контрагентом, account - коррекция расчётного счёта
                    if(request.getType().equals("boxoffice"))// если коррекция кассы предприятия -
                        commonUtilites.addDocumentHistory("boxoffice", request.getCompany_id(), request.getBoxoffice_id(), "correction","correction", request.getId(), (request.getSumm().compareTo(new BigDecimal(0))>0?request.getSumm():new BigDecimal(0)),(request.getSumm().compareTo(new BigDecimal(0))>0?new BigDecimal(0):request.getSumm().abs()),true,request.getDoc_number(),request.getStatus_id());
                    if(request.getType().equals("cagent"))// если коррекция баланса с контрагентом -
                        commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "correction","correction", request.getId(), (request.getSumm().compareTo(new BigDecimal(0))>0?request.getSumm():new BigDecimal(0)),(request.getSumm().compareTo(new BigDecimal(0))>0?new BigDecimal(0):request.getSumm().abs()),true,request.getDoc_number(),request.getStatus_id());
                    if(request.getType().equals("account"))// если коррекция расч. счёта предприятия -
                        commonUtilites.addDocumentHistory("payment_account", request.getCompany_id(), request.getPayment_account_id(), "correction","correction", request.getId(), (request.getSumm().compareTo(new BigDecimal(0))>0?request.getSumm():new BigDecimal(0)),(request.getSumm().compareTo(new BigDecimal(0))>0?new BigDecimal(0):request.getSumm().abs()),true,request.getDoc_number(),request.getStatus_id());
                }

                return 1;

            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method CorrectionRepository/updateCorrection.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method CorrectionRepository/updateCorrection.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method CorrectionRepository/updateCorrection. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer setCorrectionAsDecompleted(CorrectionForm request){
        // Есть ли права на проведение
        if((securityRepositoryJPA.userHasPermissions_OR(41L,"548") && securityRepositoryJPA.isItAllMyMastersDocuments("correction",request.getId().toString())) ||
            //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(41L,"549") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery =
                    " update correction set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId() + " and master_id = " + myMasterId;

            try {

                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("companies_payment_accounts", request.getPayment_account_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_id(), myMasterId);

                // проверим, не снят ли он уже спроведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "correction"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();


                // определим тип корректировки. boxoffice - коррекция кассы, cagent - коррекция баланса с контрагентом, account - коррекция расчётного счёта
                if(request.getType().equals("boxoffice"))// если коррекция кассы предприятия -
                    commonUtilites.addDocumentHistory("boxoffice",          request.getCompany_id(), request.getBoxoffice_id(),         "correction","correction", request.getId(), (request.getSumm().compareTo(new BigDecimal(0))>0?request.getSumm():new BigDecimal(0)),(request.getSumm().compareTo(new BigDecimal(0))>0?new BigDecimal(0):request.getSumm().abs()),false,request.getDoc_number(),request.getStatus_id());
                if(request.getType().equals("cagent"))// если коррекция баланса с контрагентом -
                    commonUtilites.addDocumentHistory("cagent",             request.getCompany_id(), request.getCagent_id(),            "correction","correction", request.getId(), (request.getSumm().compareTo(new BigDecimal(0))>0?request.getSumm():new BigDecimal(0)),(request.getSumm().compareTo(new BigDecimal(0))>0?new BigDecimal(0):request.getSumm().abs()),false,request.getDoc_number(),request.getStatus_id());
                if(request.getType().equals("account"))// если коррекция расч. счёта предприятия -
                    commonUtilites.addDocumentHistory("payment_account",    request.getCompany_id(), request.getPayment_account_id(),   "correction","correction", request.getId(), (request.getSumm().compareTo(new BigDecimal(0))>0?request.getSumm():new BigDecimal(0)),(request.getSumm().compareTo(new BigDecimal(0))>0?new BigDecimal(0):request.getSumm().abs()),false,request.getDoc_number(),request.getStatus_id());



                return 1;

            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method CorrectionRepository/setCorrectionAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method CorrectionRepository/setCorrectionAsDecompleted.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method CorrectionRepository/setCorrectionAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsCorrection(SettingsCorrectionForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_status_dock", row.getStatusIdOnComplete(), myMasterId);

            stringQuery =
                    " insert into settings_correction (" +
                            "master_id, " +
                            "company_id, " +
                            "date_time_update, " +
                            "user_id, " +
                            "status_id_on_complete"+// статус документа при проведении
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            "now(), " +
                            myId + "," +
                            row.getStatusIdOnComplete()+
                            ") " +
                            " ON CONFLICT ON CONSTRAINT settings_correction_user_uq " +// "upsert"
                            " DO update set " +
                            "company_id = "+row.getCompanyId()+"," +
                            "date_time_update = now(), " +
                            "status_id_on_complete = "+row.getStatusIdOnComplete();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsCorrection. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsCorrectionJSON getSettingsCorrection() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                 // предприятие
                "           p.status_id_on_complete as status_id_on_complete " +           // статус по проведении
                "           from settings_correction p " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsCorrectionJSON returnObj=new SettingsCorrectionJSON();

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                             obj[0].toString()));
                returnObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                returnObj.setStatusIdOnComplete(obj[2]!=null?Long.parseLong(obj[2].toString()):null);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsCorrection. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteCorrection (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(41L,"542") && securityRepositoryJPA.isItAllMyMastersDocuments("correction",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(41L,"543") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "correction");
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update correction p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true and master_id = " + myMasterId;
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "correction")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteCorrection. SQL query:" + stringQuery, e);
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
    public Integer undeleteCorrection(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(41L,"542") && securityRepositoryJPA.isItAllMyMastersDocuments("correction",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(41L,"543") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "Update correction p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+") and master_id = " + myMasterId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteCorrection. SQL query:"+stringQuery, e);
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
    public Boolean addFilesToCorrection(UniversalForm request){
        Long correctionId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(41L,"546") && securityRepositoryJPA.isItAllMyMastersDocuments("correction",correctionId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(41L,"547") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",correctionId.toString())))
        {
            try
            {
                String stringQuery;
                Long masterId = userRepositoryJPA.getMyMasterId();
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {
                    commonUtilites.idBelongsMyMaster("files", fileId, masterId);
                    stringQuery = "select correction_id from correction_files where correction_id=" + correctionId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_CorrectionId_FileId(correctionId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method CorrectionRepository/addFilesToCorrection.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_CorrectionId_FileId(Long correctionId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into correction_files " +
                    "(correction_id,file_id) " +
                    "values " +
                    "(" + correctionId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method CorrectionRepository/manyToMany_CorrectionId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    public List<FilesUniversalJSON> getListOfCorrectionFiles(Long correctionId) {
        if(securityRepositoryJPA.userHasPermissions_OR(41L, "544,545"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           correction p" +
                    "           inner join" +
                    "           correction_files pf" +
                    "           on p.id=pf.correction_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + correctionId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(41L, "544")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                List<FilesUniversalJSON> correctionList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesUniversalJSON doc=new FilesUniversalJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    correctionList.add(doc);
                }
                return correctionList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfCorrectionFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCorrectionFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(41L,"546") && securityRepositoryJPA.isItAllMyMastersDocuments("correction", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(41L,"547") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("correction",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from correction_files "+
                    " where correction_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from correction where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                commonUtilites.idBelongsMyMaster("files", request.getId(), myMasterId);
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method CorrectionRepository/deleteCorrectionFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}