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
import com.dokio.message.request.Settings.SettingsPaymentinForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsPaymentinJSON;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
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

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class PaymentinRepositoryJPA {

    Logger logger = Logger.getLogger("PaymentinRepositoryJPA");

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
            .of("doc_number","name","cagent","status_name","sum_price","company","creator","date_time_created_sort","income_number_date_sort","description","is_completed","summ")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<PaymentinJSON> getPaymentinTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(33L, "469,470"))//(см. файл Permissions Id)
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
                    "           p.income_number as income_number," +
                    "           to_char(p.income_number_date at time zone '"+myTimeZone+"', '"+dateFormat+"') as income_number_date, " +
                    "           p.payment_account_id as payment_account_id,"+
                    "           p.moving_type as moving_type, " +
                    "           p.paymentout_id as paymentout_id," +
                    "           p.orderout_id as orderout_id," +

                    "           p.income_number_date as income_number_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +


                    "           from paymentin p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(33L, "469")) //Если нет прав на просм по всем предприятиям
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
                        " upper(p.income_number) like upper(CONCAT('%',:sg,'%')) or "+
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
                List<PaymentinJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    PaymentinJSON doc=new PaymentinJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDoc_number(Long.parseLong(             obj[8].toString()));
                    doc.setCompany((String)                       obj[9]);
                    doc.setDate_time_created((String)             obj[10]);
                    doc.setDate_time_changed((String)             obj[11]);
                    doc.setDescription((String)                   obj[12]);
                    doc.setStatus_id(obj[13]!=null?Long.parseLong(obj[13].toString()):null);
                    doc.setStatus_name((String)                   obj[14]);
                    doc.setStatus_color((String)                  obj[15]);
                    doc.setStatus_description((String)            obj[16]);
                    doc.setSumm((BigDecimal)                      obj[17]);
                    doc.setCagent((String)                        obj[18]);
                    doc.setIs_completed((Boolean)                 obj[19]);
                    doc.setIncome_number((String)                 obj[20]);
                    doc.setIncome_number_date((String)            obj[21]);
                    doc.setPayment_account_id(obj[22]!=null?Long.parseLong(obj[22].toString()):null);
                    doc.setMoving_type((String)                   obj[23]);
                    doc.setPaymentout_id(obj[24]!=null?Long.parseLong(              obj[24].toString()):null);
                    doc.setOrderout_id(obj[25]!=null?Long.parseLong(                obj[25].toString()):null);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPaymentinTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getPaymentinSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from paymentin p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(33L, "469")) //Если нет прав на просм по всем предприятиям
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
                    " upper(p.income_number) like upper(CONCAT('%',:sg,'%')) or "+
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
            logger.error("Exception in method getPaymentinSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public PaymentinJSON getPaymentinValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(33L, "469,470"))//см. _Permissions Id.txt
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
                    "           coalesce(p.income_number,'') as income_number," +
                    "           to_char(p.income_number_date at time zone '"+myTimeZone+"', '"+dateFormat+"') as income_number_date, " +
                    "           p.payment_account_id as payment_account_id,"+
                    "           cpas.payment_account||' ('||cpas.name||')' as payment_account," +
                    "           p.internal as internal," +
                    "           p.moving_type as moving_type, " +
                    "           p.boxoffice_from_id as boxoffice_from_id, " +
                    "           p.payment_account_from_id as payment_account_from_id, " +

                    "           p.paymentout_id as paymentout_id," +
                    "           p.orderout_id as orderout_id," +

                    "           pto.doc_number||', '||to_char(pto.summ, '9990.99') as paymentout," +
                    "           oou.doc_number||', '||to_char(oou.summ, '9990.99') as orderout," +

                    "           sb.name as boxoffice_from, " +
                    "           cpa.payment_account||', '||cpa.name as payment_account_from " +


                    "           from paymentin p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpas ON p.payment_account_id=cpas.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +

                    "           LEFT OUTER JOIN orderout oou ON p.orderout_id=oou.id " +
                    "           LEFT OUTER JOIN paymentout pto ON p.paymentout_id=pto.id " +

                    "           LEFT OUTER JOIN sprav_boxoffice sb ON p.boxoffice_from_id=sb.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_from_id=cpa.id " +

                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(33L, "469")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                PaymentinJSON returnObj=new PaymentinJSON();

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
                    returnObj.setIncome_number((String)                     obj[23]);
                    returnObj.setIncome_number_date((String)                obj[24]);
                    returnObj.setPayment_account_id(obj[25]!=null?Long.parseLong(obj[25].toString()):null);
                    returnObj.setPayment_account((String)                   obj[26]);
                    returnObj.setInternal((Boolean)                         obj[27]);
                    returnObj.setMoving_type((String)                                     obj[28]);
                    returnObj.setBoxoffice_from_id(obj[29]!=null?Long.parseLong(          obj[29].toString()):null);
                    returnObj.setPayment_account_from_id(obj[30]!=null?Long.parseLong(    obj[30].toString()):null);
                    returnObj.setPaymentout_id(obj[31]!=null?Long.parseLong(              obj[31].toString()):null);
                    returnObj.setOrderout_id(obj[32]!=null?Long.parseLong(                obj[32].toString()):null);

                    returnObj.setPaymentout((String)                        obj[33]);
                    returnObj.setOrderout((String)                          obj[34]);

                    returnObj.setBoxoffice_from(obj[35]!=null?              obj[35].toString():"");
                    returnObj.setPayment_account_from(obj[36]!=null?        obj[36].toString():"");
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPaymentinValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertPaymentin(PaymentinForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "paymentin")){
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long linkedDocsGroupId=null;

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            if ((   //если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(33L, "465")) ||
                    //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                    (securityRepositoryJPA.userHasPermissions_OR(33L, "466") && myCompanyId.equals(request.getCompany_id()))) &&
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
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"paymentin");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),33));
                }

                //если документ создается из другого документа
                if (request.getLinked_doc_id() != null) {
                    //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                    linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                    if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
                    //если расч счет неизвестен
                    if(Objects.isNull(request.getPayment_account_id())) {
                        // но известно отделение -
                        if (!Objects.isNull(request.getDepartment_id())) {
                            //пытаемся получть расч. счёт из привязки к отделению
                            request.setPayment_account_id(companyRepositoryJPA.getPaymentAccountIdByDepartment(request.getDepartment_id()));
                        }
                    }
                    //если расч счет неизвестен или если не получилось получить его из привязки к отделению(например в карточке отделения нет привязки к расч счету
                    if(Objects.isNull(request.getPayment_account_id()) || request.getPayment_account_id()==0L){
                        // пытаемся получить главный расч. счёт (верхний) из списка счетов предприятия
                        request.setPayment_account_id(companyRepositoryJPA.getMainPaymentAccountIdOfCompany(request.getCompany_id()));
                        //Если опять не получилось (в карточке предприятия не заведены расчётные счета)
                        if(Objects.isNull(request.getPayment_account_id()) || request.getPayment_account_id()==0L)
                            return -20L;//расчётный счёт не определен (см. файл _ErrorCodes)
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
//                        logger.error("Exception in method insertPaymentin on creating Cagent.", e);
//                        e.printStackTrace();
//                        return null;
//                    }
//                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery = "insert into paymentin (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие, для которого создается документ
                        " cagent_id," +//контрагент
                        " date_time_created," + //дата и время создания
                        " doc_number," + //номер документа
                        " income_number," +//входящий внутренний номер поставщика
                        ((request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))?" income_number_date,":"") +// входящая дата счета поставщика
                        " description," +//доп. информация по заказу
                        " nds," +// НДС
                        " status_id,"+//статус
                        " linked_docs_group_id," +// id группы связанных документов
                        " summ,"+
                        " payment_account_id,"+
                        " internal,"+ //внутренний платеж (перемещение денег внутри предприятия)
                        " moving_type," +// тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
                        " boxoffice_from_id," +// id кассы предприятия - источника
                        " payment_account_from_id," +// id расч счёта
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
                        ":income_number,"+
                        ((request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))?" to_date(:income_number_date,'DD.MM.YYYY'),":"")+// входящая дата счета поставщика
                        ":description," +
                        request.getNds() + ", "+// НДС
                        request.getStatus_id()  + ", "+//статус
                        linkedDocsGroupId+"," + // id группы связанных документов
                        request.getSumm()+"," + //наименование заказа поставщику
                        request.getPayment_account_id()+"," + //банковский счет
                        request.getInternal()+"," +
                        ":moving_type," +
                        request.getBoxoffice_from_id()+"," +
                        request.getPayment_account_from_id()+"," +
                        request.getPaymentout_id() +"," +
                        request.getOrderout_id() +"," +
                        ":uid)";// уникальный идентификатор документа
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("uid",request.getUid());
                    query.setParameter("income_number",request.getIncome_number());
                    query.setParameter("moving_type",request.getMoving_type());
                    if(request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))
                        query.setParameter("income_number_date",request.getIncome_number_date());
                    query.executeUpdate();
                    stringQuery="select id from paymentin where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "paymentin", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    return newDocId;

                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertPaymentin on inserting into paymentin. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertPaymentin. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, IllegalArgumentException.class, CantSetHistoryCauseNegativeSumException.class, OutcomingPaymentIsDecompletedException.class})
    public Integer updatePaymentin(PaymentinForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(33L,"471") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(33L,"472") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                        !(
                                (securityRepositoryJPA.userHasPermissions_OR(33L,"473") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(33L,"474") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",request.getId().toString()))
                        )
                ) return -1;
            }

            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            // Если перевод внутренний - чтобы нельзя было "нарисовать" любую сумму, берем ее из исходящего платежа.
            // Также из исходящего платежа берем moving_type, т.к. в теории может быть такая ситуация, что пока принимающий документ открыт, в исходящем платеже
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
                }
            }


            String stringQuery;
            stringQuery =   " update paymentin set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, " +
                " cagent_id = " +request.getCagent_id() +"," +
                " nds = "+request.getNds()+"," +// НДС
                " summ = " + request.getSumm() + "," + // сумма платежа
                " income_number = :income_number," +// входящий номер
                " internal = " + request.getInternal() + "," +//внутренний платеж (перемещение денег внутри предприятия)
                " payment_account_id = " + request.getPayment_account_id()+"," + //банковский счет
                ((request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))?" income_number_date = to_date(:income_number_date,'DD.MM.YYYY'),":"income_number_date = null,") +//входящая дата
                " is_completed = " + request.getIs_completed() + "," +
                " moving_type = :moving_type," +// тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
                " boxoffice_from_id = "+request.getBoxoffice_from_id()+"," +// id кассы предприятия - источника
                " payment_account_from_id = "+request.getPayment_account_from_id()+"," +// id расч счёта
                " paymentout_id = " +   request.getPaymentout_id()+"," +        // id исходящего платежа, из которого поступили средства
                " orderout_id = " +     request.getOrderout_id()+"," +          // id расходного ордера, из которого поступили средства
                " status_id = " + request.getStatus_id() +
                " where " +
                " master_id= " + myMasterId +
                " and id= "+request.getId();
            try
            {
                // проверим, не является ли он уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "paymentin"))
                    throw new DocumentAlreadyCompletedException();
                if(Objects.isNull(request.getSumm()))
                    throw new Exception("Error determining the amount in the outgoing payment");
                if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                if(request.getInternal()&&Objects.isNull(outgoingPaymentMovingType))
                    throw new Exception("Error determining transfer type in outgoing payment");
                if(request.getInternal()&&!outgoingPaymentMovingType.equals("account"))
                    throw new Exception("The transfer type in the receiving payment does not match the transfer type in the outgoing payment. Outgoing - " +outgoingPaymentMovingType+", receiving - \"account\"");

                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description",request.getDescription());
                query.setParameter("income_number",request.getIncome_number());
                query.setParameter("moving_type",request.getMoving_type());
                if(request.getIncome_number_date()!=null&& !request.getIncome_number_date().equals(""))
                    query.setParameter("income_number_date",request.getIncome_number_date());
                query.executeUpdate();
                // если проводим документ
                if((request.getIs_completed()==null?false:request.getIs_completed())){

                    // определим тип платежа - внутренний или контрагенту (внутренний имеет тип moving)
                    if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                    if(!request.getInternal()){// если это не внутренний платёж -
                        // записываем контрагенту положительную сумму, увеличивая наш долг ему
                        commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "paymentin","paymentin", request.getId(), request.getSumm(),new BigDecimal(0),true,request.getDoc_number(),request.getStatus_id());
                    } else { // если платеж внутренний -

                    // отмечаем исходящий внутренний платеж как доставленный, заодно перед этим проверяя,
                    // всё ли еще данный исходящий платеж для нашего внутреннего входящего платежа проведён?
                    // Теоретически, пока открыт входящий платеж, исходящий могли снять с проведения, и после этого
                    // деньги в итоге проведения обоих платежей задвоятся

                        switch (request.getMoving_type()) {
                            case "account":{ // если деньги поступили из документа "Исходящий платёж"
                                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getPaymentout_id(),"paymentout"))
                                    throw new OutcomingPaymentIsDecompletedException();
                                commonUtilites.setDelivered("paymentout", request.getPaymentout_id());
                                break;
                            }
                            case "boxoffice":{// если деньги поступили из документа "Расходный ордер"
                                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getOrderout_id(),"orderout"))
                                    throw new OutcomingPaymentIsDecompletedException();
                                commonUtilites.setDelivered("orderout", request.getOrderout_id());
                                break;
                            }
                            default: throw new Exception("Outgoing document not defined");

                        }
                    }
                    // обновляем состояние счета нашего предприятия, прибавляя к нему полученную сумму
                    commonUtilites.addDocumentHistory("payment_account", request.getCompany_id(), request.getPayment_account_id(), "paymentin","paymentin", request.getId(), request.getSumm(),new BigDecimal(0),true,request.getDoc_number(),request.getStatus_id());
                }
                return 1;
            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PaymentinRepository/updatePaymentin.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            } catch (OutcomingPaymentIsDecompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PaymentinRepository/updatePaymentin.", e);
                e.printStackTrace();
                return -31; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CantSetHistoryCauseNegativeSumException in method PaymentinRepository/updatePaymentin.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            } catch (IllegalArgumentException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("IllegalArgumentException in method setDelivered ", e);
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
                    logger.error("ConstraintViolationException in method PaymentinRepository/updatePaymentin.", e);
                    return -40; // см. _ErrorCodes
                } else {
                    logger.error("Exception in method PaymentinRepository/updatePaymentin. SQL query:"+stringQuery, e);
                    return null;
                }
            }
        } else return -1; //недостаточно прав
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setPaymentinAsDecompleted(PaymentinForm request){
        // Есть ли права на проведение
        if((securityRepositoryJPA.userHasPermissions_OR(33L,"473") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin",request.getId().toString())) ||
                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(33L,"474") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update paymentin set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже спроведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "paymentin"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                if(Objects.isNull(request.getInternal())) request.setInternal(false); // to avoid NullPointerException
                if (!request.getInternal()) {// если это не внутренний платёж -
                    // меняем в истории контрагента проведенность, увеличивая его долг нашему предприятию (т.к. при отмене входящего платежа деньги возвращаются контрагенту)
                    commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "paymentin","paymentin", request.getId(), request.getSumm(), new BigDecimal(0),false,request.getDoc_number(),request.getStatus_id());
                } else { // если это внутренний платеж

                    // Необходимо установить у исходящего документа is_delivered=false
                    if (request.getMoving_type().equals("account"))
                        commonUtilites.setUndelivered("paymentout", request.getPaymentout_id());
                    else if (request.getMoving_type().equals("boxoffice"))
                        commonUtilites.setUndelivered("orderout", request.getOrderout_id());
                    else throw new Exception("Outgoing document not defined");
                }
                // меняем проведенность в истории р. счёта, тем самым отнимая у него переводимую сумму
                commonUtilites.addDocumentHistory("payment_account", request.getCompany_id(), request.getPayment_account_id(), "paymentin","paymentin", request.getId(), request.getSumm(), new BigDecimal(0),false,request.getDoc_number(),request.getStatus_id());
                return 1;

            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PaymentinRepository/setPaymentinAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PaymentinRepository/setPaymentinAsDecompleted.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PaymentinRepository/setPaymentinAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }


    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsPaymentin(SettingsPaymentinForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into settings_paymentin (" +
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
                            " ON CONFLICT ON CONSTRAINT settings_paymentin_user_id_key " +// "upsert"
                            " DO update set " +
                            " cagent_id = "+row.getCagentId()+"," +
                            " company_id = "+row.getCompanyId()+"," +
                            " status_id_on_complete = "+row.getStatusIdOnComplete();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsPaymentin. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsPaymentinJSON getSettingsPaymentin() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.cagent_id as cagent_id, " +
                "           cg.name as cagent, " +                                          // контрагент
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                 // предприятие
                "           p.status_id_on_complete as status_id_on_complete " +           // статус по проведении
                "           from settings_paymentin p " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsPaymentinJSON returnObj=new SettingsPaymentinJSON();

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
            logger.error("Exception in method getSettingsPaymentin. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deletePaymentin (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(33L,"467") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(33L,"468") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "paymentin");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update paymentin p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "paymentin")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deletePaymentin. SQL query:" + stringQuery, e);
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
    public Integer undeletePaymentin(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(33L,"467") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(33L,"468") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update paymentin p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeletePaymentin. SQL query:"+stringQuery, e);
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
    public Boolean addFilesToPaymentin(UniversalForm request){
        Long paymentinId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(33L,"471") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin",paymentinId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(33L,"472") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",paymentinId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select paymentin_id from paymentin_files where paymentin_id=" + paymentinId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_PaymentinId_FileId(paymentinId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method PaymentinRepository/addFilesToPaymentin.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_PaymentinId_FileId(Long paymentinId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into paymentin_files " +
                    "(paymentin_id,file_id) " +
                    "values " +
                    "(" + paymentinId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method PaymentinRepository/manyToMany_PaymentinId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesUniversalJSON> getListOfPaymentinFiles(Long paymentinId) {
        if(securityRepositoryJPA.userHasPermissions_OR(33L, "469,470"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           paymentin p" +
                    "           inner join" +
                    "           paymentin_files pf" +
                    "           on p.id=pf.paymentin_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + paymentinId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(33L, "469")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
               stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                List<FilesUniversalJSON> paymentinList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesUniversalJSON doc=new FilesUniversalJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    paymentinList.add(doc);
                }
                return paymentinList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfPaymentinFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deletePaymentinFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(33L,"471") && securityRepositoryJPA.isItAllMyMastersDocuments("paymentin", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(33L,"472") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("paymentin",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from paymentin_files "+
                    " where paymentin_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from paymentin where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method PaymentinRepository/deletePaymentinFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}
