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

import com.dokio.message.request.CagentsForm;
import com.dokio.message.request.OrderinForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsOrderinForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.OrderinJSON;
import com.dokio.message.response.Settings.SettingsOrderinJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesUniversalJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.model.Companies;
import com.dokio.repository.Exceptions.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class OrderinRepositoryJPA {

    Logger logger = Logger.getLogger("OrderinRepositoryJPA");

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
            .of("doc_number","name","cagent","status_name","sum_price","company","creator","date_time_created_sort","description","is_completed","summ")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<OrderinJSON> getOrderinTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(35L, "480,481"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
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
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce(p.summ,0) as summ, " +
                    "           cg.name as cagent, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.moving_type as moving_type, " +

                    "           p.paymentout_id as paymentout_id," +
                    "           p.orderout_id as orderout_id," +
                    "           p.withdrawal_id as withdrawal_id," +

                    /*
                    "           p.kassa_from_id as kassa_from_id, " +
                    "           p.boxoffice_from_id as boxoffice_from_id, " +
                    "           p.payment_account_from_id as payment_account_from_id, " +*/

                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +


                    "           from orderin p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(35L, "480")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
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
                List<OrderinJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    OrderinJSON doc=new OrderinJSON();
                    doc.setId(Long.parseLong(                                       obj[0].toString()));
                    doc.setMaster((String)                                          obj[1]);
                    doc.setCreator((String)                                         obj[2]);
                    doc.setChanger((String)                                         obj[3]);
                    doc.setMaster_id(Long.parseLong(                                obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(                               obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(                  obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(                               obj[7].toString()));
                    doc.setDoc_number(Long.parseLong(                               obj[8].toString()));
                    doc.setCompany((String)                                         obj[9]);
                    doc.setDate_time_created((String)                               obj[10]);
                    doc.setDate_time_changed((String)                               obj[11]);
                    doc.setDescription((String)                                     obj[12]);
                    doc.setStatus_id(obj[13]!=null?Long.parseLong(                  obj[13].toString()):null);
                    doc.setStatus_name((String)                                     obj[14]);
                    doc.setStatus_color((String)                                    obj[15]);
                    doc.setStatus_description((String)                              obj[16]);
                    doc.setSumm((BigDecimal)                                        obj[17]);
                    doc.setCagent((String)                                          obj[18]);
                    doc.setIs_completed((Boolean)                                   obj[19]);
                    doc.setMoving_type((String)                                     obj[20]);
                    doc.setPaymentout_id(obj[21]!=null?Long.parseLong(              obj[21].toString()):null);
                    doc.setOrderout_id(obj[22]!=null?Long.parseLong(                obj[22].toString()):null);
                    doc.setWithdrawal_id(obj[23]!=null?Long.parseLong(              obj[23].toString()):null);



                    /*
                    doc.setKassa_from_id(obj[21]!=null?Long.parseLong(              obj[21].toString()):null);
                    doc.setBoxoffice_from_id(obj[22]!=null?Long.parseLong(          obj[22].toString()):null);
                    doc.setPayment_account_from_id(obj[23]!=null?Long.parseLong(    obj[23].toString()):null);*/

                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getOrderinTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getOrderinSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from orderin p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(35L, "480")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие
            stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
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
            logger.error("Exception in method getOrderinSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public OrderinJSON getOrderinValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(35L, "480,481"))//см. _Permissions Id.txt
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            String dateFormat=userRepositoryJPA.getMyDateFormat();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.summ,0) as summ, " +
                    "           coalesce(p.nds,0) as nds, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.uid as uid, " +
                    "           p.is_completed as is_completed, " +
                    "           p.internal as internal," +
                    "           p.boxoffice_id as boxoffice_id," +
                    "           p.moving_type as moving_type, " +
                    "           p.kassa_from_id as kassa_from_id, " +
                    "           p.boxoffice_from_id as boxoffice_from_id, " +
                    "           p.payment_account_from_id as payment_account_from_id, " +

                    "           p.paymentout_id as paymentout_id," +
                    "           p.orderout_id as orderout_id," +
                    "           p.withdrawal_id as withdrawal_id," +

                    "           pto.doc_number||', '||to_char(pto.summ, '9990.99') as paymentout," +
                    "           oou.doc_number||', '||to_char(oou.summ, '9990.99') as orderout," +
                    "           wdw.doc_number||', '||to_char(wdw.summ, '9990.99') as withdrawal," +

                    "           ka.name as kassa_from, " +
                    "           sb.name as boxoffice_from, " +
                    "           cpa.payment_account||', '||cpa.name as payment_account_from, " +

                    "           sbx.name as boxoffice " +

                    "           from orderin p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN sprav_boxoffice sbx ON p.boxoffice_id=sbx.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +

                    "           LEFT OUTER JOIN withdrawal wdw ON p.withdrawal_id=wdw.id " +
                    "           LEFT OUTER JOIN orderout oou ON p.orderout_id=oou.id " +
                    "           LEFT OUTER JOIN paymentout pto ON p.paymentout_id=pto.id " +

                    "           LEFT OUTER JOIN kassa ka ON p.kassa_from_id=ka.id " +
                    "           LEFT OUTER JOIN sprav_boxoffice sb ON p.boxoffice_from_id=sb.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_from_id=cpa.id " +

                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(35L, "480")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                OrderinJSON returnObj=new OrderinJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                         obj[0].toString()));
                    returnObj.setMaster((String)                            obj[1]);
                    returnObj.setCreator((String)                           obj[2]);
                    returnObj.setChanger((String)                           obj[3]);
                    returnObj.setMaster_id(Long.parseLong(                  obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(                 obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(    obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(                 obj[7].toString()));
                    returnObj.setDoc_number(Long.parseLong(                 obj[8].toString()));
                    returnObj.setCompany((String)                           obj[9]);
                    returnObj.setDate_time_created((String)                 obj[10]);
                    returnObj.setDate_time_changed((String)                 obj[11]);
                    returnObj.setDescription((String)                       obj[12]);
                    returnObj.setSumm((BigDecimal)                          obj[13]);
                    returnObj.setNds((BigDecimal)                           obj[14]);
                    returnObj.setCagent_id(obj[15]!=null?Long.parseLong(    obj[15].toString()):null);
                    returnObj.setCagent((String)                            obj[16]);
                    returnObj.setStatus_id(obj[17]!=null?Long.parseLong(    obj[17].toString()):null);
                    returnObj.setStatus_name((String)                       obj[18]);
                    returnObj.setStatus_color((String)                      obj[19]);
                    returnObj.setStatus_description((String)                obj[20]);
                    returnObj.setUid((String)                               obj[21]);
                    returnObj.setIs_completed((Boolean)                     obj[22]);
                    returnObj.setInternal((Boolean)                         obj[23]);
                    returnObj.setBoxoffice_id(Long.parseLong(               obj[24].toString()));
                    returnObj.setMoving_type((String)                                     obj[25]);
                    returnObj.setKassa_from_id(obj[26]!=null?Long.parseLong(              obj[26].toString()):null);
                    returnObj.setBoxoffice_from_id(obj[27]!=null?Long.parseLong(          obj[27].toString()):null);
                    returnObj.setPayment_account_from_id(obj[28]!=null?Long.parseLong(    obj[28].toString()):null);
                    returnObj.setPaymentout_id(obj[29]!=null?Long.parseLong(              obj[29].toString()):null);
                    returnObj.setOrderout_id(obj[30]!=null?Long.parseLong(                obj[30].toString()):null);
                    returnObj.setWithdrawal_id(obj[31]!=null?Long.parseLong(              obj[31].toString()):null);

                    returnObj.setPaymentout(obj[32]!=null?                  obj[32].toString():"");
                    returnObj.setOrderout(obj[33]!=null?                    obj[33].toString():"");
                    returnObj.setWithdrawal(obj[34]!=null?                  obj[34].toString():"");

                    returnObj.setKassa_from(obj[35]!=null?                  obj[35].toString():"");
                    returnObj.setBoxoffice_from(obj[36]!=null?              obj[36].toString():"");
                    returnObj.setPayment_account_from(obj[37]!=null?        obj[37].toString():"");

                    returnObj.setBoxoffice((String)                         obj[38]);
                }

                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getOrderinValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertOrderin(OrderinForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "orderin")){
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long linkedDocsGroupId=null;

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            if ((   //если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(35L, "476")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(35L, "477") && myCompanyId.equals(request.getCompany_id()))) &&
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
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"orderin");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),35));
                }

                //если документ создается из другого документа
                if (request.getLinked_doc_id() != null) {
                    //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                    linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                    if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
                    //если касса неизвестна
                    if(Objects.isNull(request.getBoxoffice_id())) {
                        // но известно отделение -
                        if (!Objects.isNull(request.getDepartment_id())) {
                            //пытаемся получть кассу из привязки к отделению
                            request.setBoxoffice_id(companyRepositoryJPA.getBoxofficeIdByDepartment(request.getDepartment_id()));
                        }
                    }
                    //касса неизвестна или если не получилось получить её из привязки к отделению(например в карточке отделения нет привязки к расч счёту
                    if(Objects.isNull(request.getBoxoffice_id()) || request.getBoxoffice_id()==0L){
                        // пытаемся получить первую созданную неудаленную кассу предприятия в качетстве главной кассы
                        request.setBoxoffice_id(companyRepositoryJPA.getMainBoxofficeIdOfCompany(request.getCompany_id()));
                        //Если опять не получилось (в справочнике касс предприятия ничего нет)
                        if(Objects.isNull(request.getBoxoffice_id()) || request.getBoxoffice_id()==0L)
                            return -21L;//касса не определена (см. файл _ErrorCodes)
                    }
                }

                //Возможно 2 ситуации: контрагент выбран из существующих, или выбрано создание нового контрагента
                //Если присутствует 2я ситуация, то контрагента нужно сначала создать, получить его id и уже затем создавать Заказ покупателя:
//                if(request.getCagent_id()==null){
//                    try{
//                        CagentsForm cagentForm = new CagentsForm();
//                        cagentForm.setName(request.getNew_cagent());
//                        cagentForm.setCompany_id(request.getCompany_id());
//                        cagentForm.setOpf_id(2);//ставим по-умолчанию Физ. лицо
//                        cagentForm.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),12));
//                        cagentForm.setDescription("Автоматическое создание из Счёта поставщика №"+doc_number.toString());
//                        cagentForm.setPrice_type_id(commonUtilites.getPriceTypeDefault(request.getCompany_id()));
//                        cagentForm.setTelephone("");
//                        cagentForm.setEmail("");
//                        cagentForm.setZip_code("");
//                        cagentForm.setCountry_id(null);
//                        cagentForm.setRegion_id(null);
//                        cagentForm.setCity_id(null);
//                        cagentForm.setStreet("");
//                        cagentForm.setHome("");
//                        cagentForm.setFlat("");
//                        cagentForm.setAdditional_address("");
//                        request.setCagent_id(cagentRepository.insertCagent(cagentForm));
//                    }
//                    catch (Exception e) {
//                        logger.error("Exception in method insertOrderin on creating Cagent.", e);
//                        e.printStackTrace();
//                        return null;
//                    }
//                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery = "insert into orderin (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер документа
                        " description," +//доп. информация по заказу
                        " nds," +// НДС
                        " status_id,"+//статус
                        " linked_docs_group_id," +// id группы связанных документов
                        " summ,"+
                        " internal,"+ //внутренний платеж (перемещение денег внутри предприятия)
                        " boxoffice_id," + // касса предприятия (это не ККМ, это именно касса предприятия, обычно в 99% случаев, она одна. Исключение - обособленные подразделения)
                        " moving_type," +// тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
                        " kassa_from_id," +// id кассы ККМ - источника
                        " boxoffice_from_id," +// id кассы предприятия - источник
                        " payment_account_from_id," +// id расч счёта
                        " withdrawal_id," +             // id выемки, из которой поступили средства
                        " paymentout_id," +             // id исходящего платежа, из которого поступили средства
                        " orderout_id," +               // id расходного ордера, из которого поступили средства
                        " uid"+// уникальный идентификатор документа
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие, для которого создается документ
                        request.getCagent_id() + ", "+//контрагент
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        doc_number + ", "+//номер документа
                        ":description," +
                        request.getNds() + ", "+// НДС
                        request.getStatus_id()  + ", "+//статус
                        linkedDocsGroupId+"," + // id группы связанных документов
                        request.getSumm()+"," + //наименование заказа поставщику
                        request.getInternal()+"," +
                        request.getBoxoffice_id()+"," +
                        ":moving_type," +
                        request.getKassa_from_id()+"," +
                        request.getBoxoffice_from_id()+"," +
                        request.getPayment_account_from_id()+"," +
                        request.getWithdrawal_id() +"," +
                        request.getPaymentout_id() +"," +
                        request.getOrderout_id() +"," +
                        ":uid)";// уникальный идентификатор документа
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.setParameter("moving_type",request.getMoving_type());
                    query.executeUpdate();
                    stringQuery="select id from orderin where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "orderin", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    return newDocId;

                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertOrderin on inserting into orderin. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertOrderin. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, IllegalArgumentException.class, OutcomingPaymentIsDecompletedException.class, CantSetHistoryCauseNegativeSumException.class})
    public Integer updateOrderin(OrderinForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(35L,"482") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(35L,"483") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                        !(
                                (securityRepositoryJPA.userHasPermissions_OR(35L,"484") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin",request.getId().toString())) ||
                                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(35L,"485") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",request.getId().toString()))
                        )
                ) return -1;
            }

            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            // если перевод внутренний - чтобы нельзя было "нарисовать" любую сумму, берем ее из исходящего платежа

            // также из исходящего платежа берем moving_type, т.к. в теории может быть такая ситуация, что пока принимающий документ открыт, в исходящем платеже
            // можно изменить тип перемещения, и провести как Приходный ордер, так и Входящий платёж из одного исходящего платежа
            String outgoingPaymentMovingType = request.getMoving_type();
            if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
            if(request.getInternal()){
                switch (request.getMoving_type()){
                    case "account": {
                        request.setSumm((BigDecimal)commonUtilites.getFieldValueFromTableById("paymentout","summ", myMasterId, request.getPaymentout_id()));
                        outgoingPaymentMovingType = (String)commonUtilites.getFieldValueFromTableById("paymentout","moving_type", myMasterId, request.getPaymentout_id());
                        break;
                    }
                    case "boxoffice": {
                        request.setSumm((BigDecimal)commonUtilites.getFieldValueFromTableById("orderout","summ", myMasterId, request.getOrderout_id()));
                        outgoingPaymentMovingType = (String)commonUtilites.getFieldValueFromTableById("orderout","moving_type", myMasterId, request.getOrderout_id());
                        break;
                    }
                    case "kassa": {
                        request.setSumm((BigDecimal)commonUtilites.getFieldValueFromTableById("withdrawal","summ", myMasterId, request.getWithdrawal_id()));
                        outgoingPaymentMovingType = "boxoffice"; // из кассы ККМ можно перевеси только в кассу предприятия
                        break;
                    }
                }
            }



            String stringQuery;
            stringQuery =   " update orderin set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, " +
                    " cagent_id = " +request.getCagent_id() +"," +
                    " nds = "+request.getNds()+"," +// НДС
                    " summ=" + request.getSumm()+"," + // сумма платежа
                    " is_completed = " + request.getIs_completed() + "," +
                    " internal = " + request.getInternal() + "," +//внутренний платеж (перемещение денег внутри предприятия)
                    " boxoffice_id = " + request.getBoxoffice_id() + "," +// касса предприятия (это не ККМ, это именно касса предприятия, обычно в 99% случаев, она одна. Исключение - обособленные подразделения)
                    " status_id = " + request.getStatus_id() +"," +
                    " moving_type = :moving_type," +// тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
                    " kassa_from_id = "+request.getKassa_from_id()+"," +// id кассы ККМ - источника
                    " boxoffice_from_id = "+request.getBoxoffice_from_id()+"," +// id кассы предприятия - источника
                    " withdrawal_id = " +   request.getWithdrawal_id()+"," +        // id выемки, из которой поступили средства
                    " paymentout_id = " +   request.getPaymentout_id()+"," +        // id исходящего платежа, из которого поступили средства
                    " orderout_id = " +     request.getOrderout_id()+"," +          // id расходного ордера, из которого поступили средства
                    " payment_account_from_id = "+request.getPayment_account_from_id() +// id расч счёта
                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;
            try
            {
                // проверим, не является ли он уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "orderin"))
                    throw new DocumentAlreadyCompletedException();
                if(Objects.isNull(request.getSumm()))
                    throw new Exception("Error determining the amount in the outgoing payment");
                if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                if(request.getInternal()&&Objects.isNull(outgoingPaymentMovingType))
                    throw new Exception("Error determining transfer type in outgoing payment");
                if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                if(request.getInternal()&&!(outgoingPaymentMovingType.equals("boxoffice")||outgoingPaymentMovingType.equals("kassa")))
                    throw new Exception("The transfer type in the receiving payment does not match the transfer type in the outgoing payment. Outgoing - " +outgoingPaymentMovingType+", receiving - boxoffice or kassa");
//                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description",request.getDescription());
                query.setParameter("moving_type",request.getMoving_type());
                query.executeUpdate();

                // если проводим документ
                if((request.getIs_completed()==null?false:request.getIs_completed())){
                    // определим тип платежа - внутренний или контрагенту (внутренний имеет тип moving)
                    if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                    if(!request.getInternal()){// если это не внутренний платёж -
                        // записываем контрагенту положительную сумму, увеличивая наш долг ему
                        commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "orderin","orderin", request.getId(), request.getSumm(),new BigDecimal(0),true,request.getDoc_number(),request.getStatus_id());
                    } else { // если платеж внутренний -

                        // отмечаем исходящий внутренний платеж как доставленный,
                        // предварительно проверяя, проведён ли он
                        switch (request.getMoving_type()) {
                            case "kassa":{
                                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getWithdrawal_id(),"withdrawal"))
                                    throw new OutcomingPaymentIsDecompletedException();
                                commonUtilites.setDelivered("withdrawal", request.getWithdrawal_id());
                                break;}
                            case "account":{
                                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getPaymentout_id(),"paymentout"))
                                    throw new OutcomingPaymentIsDecompletedException();
                                commonUtilites.setDelivered("paymentout", request.getPaymentout_id());
                                break;}
                            case "boxoffice":{
                                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getOrderout_id(),"orderout"))
                                    throw new OutcomingPaymentIsDecompletedException();
                                commonUtilites.setDelivered("orderout", request.getOrderout_id());
                                break;}
                        }
                    }
                    // обновляем состояние счета нашего предприятия, прибавляя к нему полученную сумму
                    commonUtilites.addDocumentHistory("boxoffice", request.getCompany_id(), request.getBoxoffice_id(), "orderin","orderin", request.getId(), request.getSumm(),new BigDecimal(0),true,request.getDoc_number(),request.getStatus_id());
                }

                return 1;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderinRepository/updateOrderin.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            } catch (OutcomingPaymentIsDecompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderinRepository/updateOrderin.", e);
                e.printStackTrace();
                return -31; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderinRepository/updateOrderin.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            } catch (IllegalArgumentException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method setDelivered ", e);
                e.printStackTrace();
                return null; // см. _ErrorCodes
            }catch (Exception e) {//ConstraintViolationException напрямую не отлавливается, она обернута в родительские классы, и нужно определить, есть ли она в Exception
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                e.printStackTrace();
                Throwable t = e.getCause();
                while ((t != null) && !(t instanceof ConstraintViolationException)) {
                    t = t.getCause();
                }
                if (t != null) {
                    logger.error("ConstraintViolationException in method OrderinRepository/updateOrderin.", e);
                    return -40; // см. _ErrorCodes
                } else {
                    logger.error("Exception in method OrderinRepository/updateOrderin. SQL query:"+stringQuery, e);
                    return null;
                }
            }
        } else return -1; //см. _ErrorCodes
    }

    // смена проведёности документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setOrderinAsDecompleted(OrderinForm request){
        // Есть ли права на проведение
        if((securityRepositoryJPA.userHasPermissions_OR(35L,"484") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin",request.getId().toString())) ||
        //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
        (securityRepositoryJPA.userHasPermissions_OR(35L,"485") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update orderin set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже спроведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "orderin"))
                    throw new DocumentAlreadyDecompletedException();

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                if (!request.getInternal()) {// если это не внутренний платёж -
                    // меняем в истории контрагента проведенность, увеличивая его долг нашему предприятию (т.к. при отмене входящего платежа деньги возвращаются контрагенту)
                    commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "orderin","orderin", request.getId(), request.getSumm(), new BigDecimal(0),false,request.getDoc_number(),request.getStatus_id());
                } else { // если это внутренний платеж

                    // Необходимо установить у исходящего документа is_delivered=false
                    if (request.getMoving_type().equals("account"))
                        commonUtilites.setUndelivered("paymentout", request.getPaymentout_id());
                    else if (request.getMoving_type().equals("boxoffice"))
                        commonUtilites.setUndelivered("orderout", request.getOrderout_id());
                    else if (request.getMoving_type().equals("kassa"))
                        commonUtilites.setUndelivered("withdrawal", request.getWithdrawal_id());
                    else throw new Exception("Outgoing document not defined");
                }
                // меняем проведенность в истории кассы, тем самым отнимая у неё переводимую сумму
                commonUtilites.addDocumentHistory("boxoffice", request.getCompany_id(), request.getBoxoffice_id(), "orderin","orderin", request.getId(), request.getSumm(), new BigDecimal(0),false,request.getDoc_number(),request.getStatus_id());
                return 1;

            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderinRepository/setOrderinAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderinRepository/setOrderinAsDecompleted.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderinRepository/setOrderinAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsOrderin(SettingsOrderinForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into settings_orderin (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "cagent_id, "+          //поставщик по умолчанию
                            "status_id_on_complete"+// статус документа при проведении
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            row.getCagentId() + ","+
                            row.getStatusIdOnComplete()+
                            ") " +
                            " ON CONFLICT ON CONSTRAINT settings_orderin_user_id_key " +// "upsert"
                            " DO update set " +
                            " cagent_id = "+row.getCagentId()+"," +
                            " company_id = "+row.getCompanyId()+"," +
                            " status_id_on_complete = "+row.getStatusIdOnComplete();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsOrderin. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsOrderinJSON getSettingsOrderin() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.cagent_id as cagent_id, " +
                "           cg.name as cagent, " +                                          // контрагент
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                 // предприятие
                "           p.status_id_on_complete as status_id_on_complete " +           // статус по проведении
                "           from settings_orderin p " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsOrderinJSON returnObj=new SettingsOrderinJSON();

            for(Object[] obj:queryList){
                returnObj.setCagentId(obj[1]!=null?Long.parseLong(          obj[0].toString()):null);
                returnObj.setCagent((String)                                obj[1]);
                returnObj.setId(Long.parseLong(                             obj[2].toString()));
                returnObj.setCompanyId(Long.parseLong(                      obj[3].toString()));
                returnObj.setStatusIdOnComplete(obj[4]!=null?Long.parseLong(obj[4].toString()):null);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsOrderin. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteOrderin (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(35L,"478") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(35L,"479") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "orderin");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update orderin p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "orderin")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteOrderin. SQL query:" + stringQuery, e);
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
    public Integer undeleteOrderin(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(35L,"478") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(35L,"479") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update orderin p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "")+ ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteOrderin. SQL query:"+stringQuery, e);
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
    public Boolean addFilesToOrderin(UniversalForm request){
        Long orderinId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(35L,"482") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin",orderinId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(35L,"483") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",orderinId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select orderin_id from orderin_files where orderin_id=" + orderinId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_OrderinId_FileId(orderinId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method OrderinRepository/addFilesToOrderin.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_OrderinId_FileId(Long orderinId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into orderin_files " +
                    "(orderin_id,file_id) " +
                    "values " +
                    "(" + orderinId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method OrderinRepository/manyToMany_OrderinId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesUniversalJSON> getListOfOrderinFiles(Long orderinId) {
        if(securityRepositoryJPA.userHasPermissions_OR(35L, "480,481"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           orderin p" +
                    "           inner join" +
                    "           orderin_files pf" +
                    "           on p.id=pf.orderin_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + orderinId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(35L, "480")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                List<FilesUniversalJSON> orderinList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesUniversalJSON doc=new FilesUniversalJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    orderinList.add(doc);
                }
                return orderinList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfOrderinFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteOrderinFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(35L,"482") && securityRepositoryJPA.isItAllMyMastersDocuments("orderin", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(35L,"483") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderin",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from orderin_files "+
                    " where orderin_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from orderin where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method OrderinRepository/deleteOrderinFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}
