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

import com.dokio.message.request.CagentsForm;
import com.dokio.message.request.OrderoutForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsOrderoutForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.OrderoutJSON;
import com.dokio.message.response.Settings.SettingsOrderoutJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesUniversalJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.model.Companies;
import com.dokio.repository.Exceptions.*;
import com.dokio.security.CryptoService;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.FinanceUtilites;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class OrderoutRepositoryJPA {

    Logger logger = Logger.getLogger("OrderoutRepositoryJPA");

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
    @Autowired
    private SpravExpenditureRepositoryJPA spravExpenditureRepository;
    @Autowired
    private FinanceUtilites financeUtilites;
    @Autowired
    private SpravExpenditureRepositoryJPA expenditureRepository;
    @Autowired
    private CryptoService cryptoService;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","name","cagent","status_name","sum_price","company","creator","date_time_created_sort","expenditure","description","is_completed","summ")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<OrderoutJSON> getOrderoutTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(36L, "522,523"))//(см. файл Permissions Id)
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
                    "           pgp_sym_decrypt(cg.name_enc,:cryptoPassword) as cagent, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           sei.name as expenditure," +
                    "           p.moving_type as moving_type, " +
                    "           coalesce(p.is_delivered,false) as is_delivered, " +
                    "           sei.type as expenditure_type," +

                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +


                    "           from orderout p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN sprav_expenditure_items sei ON p.expenditure_id=sei.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(36L, "522")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(sei.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(pgp_sym_decrypt(cg.name_enc,:cryptoPassword))  like upper(CONCAT('%',:sg,'%')) or "+
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
                String cryptoPassword = cryptoService.getCryptoPasswordFromDatabase(myMasterId);
                query.setParameter("cryptoPassword", cryptoPassword);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                query.setFirstResult(offsetreal).setMaxResults(result);


                List<Object[]> queryList = query.getResultList();
                List<OrderoutJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    OrderoutJSON doc=new OrderoutJSON();
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
                    doc.setExpenditure((String)                   obj[20]);
                    doc.setMoving_type((String)                   obj[21]);
                    doc.setIs_delivered((Boolean)                 obj[22]);
                    doc.setExpenditure_type((String)              obj[23]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getOrderoutTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getOrderoutSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from orderout p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN sprav_expenditure_items sei ON p.expenditure_id=sei.id " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(36L, "522")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие
            stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(sei.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(pgp_sym_decrypt(cg.name_enc,:cryptoPassword))  like upper(CONCAT('%',:sg,'%')) or "+
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
            {   query.setParameter("sg", searchString);
                String cryptoPassword = cryptoService.getCryptoPasswordFromDatabase(myMasterId);
                query.setParameter("cryptoPassword", cryptoPassword);}
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getOrderoutSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public OrderoutJSON getOrderoutValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(36L, "522,523"))//см. _Permissions Id.txt
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
                    "           coalesce(p.nds,0) as nds, " +
                    "           p.cagent_id as cagent_id, " +
                    "           pgp_sym_decrypt(cg.name_enc,:cryptoPassword) as cagent, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.uid as uid, " +
                    "           p.is_completed as is_completed, " +
                    "           p.expenditure_id as expenditure_id,"+
                    "           sei.name as expenditure," +
                    "           p.moving_type as moving_type," +   // тип внутреннего перемещения денежных средств: boxoffice - касса предприятия (не путать с ККМ!), account - банковский счёт препдриятия
                    "           p.boxoffice_id as boxoffice_id," +  // касса предприятия (не путать с ККМ!), откуда перемещаем деньги
                    "           p.payment_account_to_id as payment_account_to_id," + //  банковский счёт препдриятия, куда перемещаем денежные средства"
                    "           p.boxoffice_to_id as boxoffice_to_id," + // касса предприятия куда перемещаем деньги
                    "           p.kassa_to_id as kassa_to_id," + // id касса ККМ куда перемещаем деньги (операция внесения)
                    "           p.kassa_department_id as kassa_department_id," + //id отделения, где находится касса ККМ, в которую будет внесение
                    "           kd.name as kassa_department," + // отделение, где находится касса ККМ, в которую будет внесение
                    "           ka.name as kassa_to " +// наименование кассы ККМ куда перемещаем деньги (операция внесения)

                    "           from orderout p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN sprav_expenditure_items sei ON p.expenditure_id=sei.id " +
                    "           LEFT OUTER JOIN kassa ka ON p.kassa_to_id=ka.id " +
                    "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN departments kd ON p.kassa_department_id=kd.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(36L, "522")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                String cryptoPassword = cryptoService.getCryptoPasswordFromDatabase(myMasterId);
                query.setParameter("cryptoPassword", cryptoPassword);

                List<Object[]> queryList = query.getResultList();

                OrderoutJSON returnObj=new OrderoutJSON();

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
                    returnObj.setCagent_id(    obj[15]!=null?Long.parseLong(obj[15].toString()):null);
                    returnObj.setCagent((String)                            obj[16]);
                    returnObj.setStatus_id(obj[17]!=null?Long.parseLong(    obj[17].toString()):null);
                    returnObj.setStatus_name((String)                       obj[18]);
                    returnObj.setStatus_color((String)                      obj[19]);
                    returnObj.setStatus_description((String)                obj[20]);
                    returnObj.setUid((String)                               obj[21]);
                    returnObj.setIs_completed((Boolean)                     obj[22]);
                    returnObj.setExpenditure_id(Long.parseLong(             obj[23].toString()));
                    returnObj.setExpenditure((String)                       obj[24]);
                    returnObj.setMoving_type((String)                       obj[25]);
                    returnObj.setBoxoffice_id( obj[26]!=null?Long.parseLong(obj[26].toString()):null);
                    returnObj.setPayment_account_to_id(obj[27]!=null?Long.parseLong(obj[27].toString()):null);
                    returnObj.setBoxoffice_to_id(obj[28]!=null?Long.parseLong(obj[28].toString()):null);
                    returnObj.setKassa_to_id(obj[29]!=null?Long.parseLong(  obj[29].toString()):null);
                    returnObj.setKassa_department_id(obj[30]!=null?Long.parseLong(obj[30].toString()):null);
                    returnObj.setKassa_department((String)                  obj[31]);
                    returnObj.setKassa_to((String)                          obj[32]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getOrderoutValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем 0 если невозможно создать товарные позиции
    // Возвращаем null в случае ошибки
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertOrderout(OrderoutForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "orderout")){
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long linkedDocsGroupId=null;
            Long expenditureId;

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            if ((   //если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(36L, "518")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(36L, "519") && myCompanyId.equals(request.getCompany_id()))) &&
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
                } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"orderout");

                // статус по умолчанию (если не выбран)
                if (request.getStatus_id() ==null){
                    request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),36));
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
                if(Objects.isNull(request.getExpenditure_id()))
                    expenditureId = expenditureRepository.getDefaultExpenditure(request.getCompany_id());
                else expenditureId = request.getExpenditure_id();

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
//                        logger.error("Exception in method insertOrderout on creating Cagent.", e);
//                        e.printStackTrace();
//                        return null;
//                    }
//                }

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                stringQuery = "insert into orderout (" +
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
                        " expenditure_id,"+
                        " moving_type," + // тип внутреннего перемещения денежных средств: boxoffice - касса предприятия (не путать с ККМ!), account - банковский счёт препдриятия
                        " boxoffice_id," + // касса предприятия (не путать с ККМ!) из которой перемещаем деньги
                        " boxoffice_to_id," + // касса предприятия куда перемещаем деньги
                        " payment_account_to_id," +  //  банковский счёт препдриятия, куда перемещаем денежные средства
                        " kassa_to_id, " + // касса ККМ, куда перемещаем деньги
                        " kassa_department_id, " + //отделение, где находится касса ККМ, в которую будет внесение
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
                        expenditureId+"," + // расхода вид
                        ":moving_type," +
                        request.getBoxoffice_id()+"," +
                        request.getBoxoffice_to_id()+"," +
                        request.getPayment_account_to_id()+"," +
                        request.getKassa_to_id()+"," +
                        request.getKassa_department_id()+"," +
                        ":uid)";// уникальный идентификатор документа
                try{

                    commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_to_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("companies_payment_accounts", request.getPayment_account_to_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("kassa", request.getKassa_to_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("departments", request.getKassa_department_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_expenditure_items", expenditureId, myMasterId);


                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("description",request.getDescription());
                    query.setParameter("moving_type",request.getMoving_type());
                    query.setParameter("uid",request.getUid());
                    query.executeUpdate();
                    stringQuery="select id from orderout where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());

                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "orderout", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    return newDocId;

                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method insertOrderout on inserting into orderout. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else {
                return -1L;
            }
        } else {
            logger.info("Double UUID found on insertOrderout. UUID: " + request.getUid());
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer updateOrderout(OrderoutForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(36L,"524") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(36L,"525") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                    !(
                        (securityRepositoryJPA.userHasPermissions_OR(36L,"526") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout",request.getId().toString())) ||
                        //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(36L,"527") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",request.getId().toString()))
                    )
                ) return -1;
            }


            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update orderout set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, " +
                    " cagent_id = " +request.getCagent_id() +"," +
                    " expenditure_id = " + request.getExpenditure_id()+"," + // вид расхода
                    " nds = "+request.getNds()+"," +// НДС
                    " summ=" + request.getSumm()+"," + // сумма платежа
                    " is_completed = " + request.getIs_completed() + "," +
                    " moving_type = :moving_type" + "," +// тип внутреннего перемещения денежных средств: boxoffice - касса предприятия (не путать с ККМ!), account - банковский счёт препдриятия
                    " boxoffice_id = " + request.getBoxoffice_id()+ "," + // касса предприятия (не путать с ККМ!) из которой переводят ден. средства
                    " boxoffice_to_id = " + request.getBoxoffice_to_id()+ "," + // касса предприятия в которую переводят ден. средства
                    " payment_account_to_id = " + request.getPayment_account_to_id() + "," +//  банковский счёт предприятия, куда перемещаем денежные средства
                    " kassa_to_id = " + request.getKassa_to_id()+ "," +// касса ккм, куда перемещаем деньги
                    " kassa_department_id = " + request.getKassa_department_id()+"," +//отделение, где находится касса ККМ, в которую будет внесение
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_expenditure_items", request.getExpenditure_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_boxoffice", request.getBoxoffice_to_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("companies_payment_accounts", request.getPayment_account_to_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("kassa", request.getKassa_to_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("departments", request.getKassa_department_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);

                // проверим, не является ли он уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "orderout"))
                    throw new DocumentAlreadyCompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description",request.getDescription());
                query.setParameter("moving_type",request.getMoving_type());
                query.executeUpdate();

                // если проводим документ
                if((request.getIs_completed()==null?false:request.getIs_completed())){
                    // определим тип платежа - внутренний или контрагенту (внутренний имеет тип moving)
                    String expType = spravExpenditureRepository.getExpTypeByExpId(request.getExpenditure_id());
                    if (!expType.equals("moving")) {// если это не внутренний платёж -
                        // меняем в истории контрагента проведенность, уменьшая его долг нашему предприятию
                        commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "orderout","orderout", request.getId(), new BigDecimal(0), request.getSumm(),true,request.getDoc_number(),request.getStatus_id());
                    }
                    // обновляем состояние в истории кассы нашего предприятия, вычитая из нее переводимую сумму
                    commonUtilites.addDocumentHistory("boxoffice", request.getCompany_id(), request.getBoxoffice_id(), "orderout","orderout", request.getId(), new BigDecimal(0), request.getSumm(),true,request.getDoc_number(),request.getStatus_id());
                }
                return 1;
            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/updateOrderout.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            } catch (NotEnoughPermissionsException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/updateOrderout.", e);
                e.printStackTrace();
                return -1; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/updateOrderout.", e);
                e.printStackTrace();
                return -30; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/updateOrderout. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }




    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class,IncomingPaymentIsCompletedException.class})
    public Integer setOrderoutAsDecompleted(OrderoutForm request){
        // Есть ли права на проведение
        if((securityRepositoryJPA.userHasPermissions_OR(36L,"526") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout",request.getId().toString())) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(36L,"527") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update orderout set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " is_completed = false" +
                    " where " +
                    " id= " + request.getId();

            try {
                // проверим, не снят ли он уже спроведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "orderout"))
                    throw new DocumentAlreadyDecompletedException();

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();

                // определим тип платежа - внутренний или контрагенту (внутренний имеет тип moving)
                String expType = spravExpenditureRepository.getExpTypeByExpId(request.getExpenditure_id());
                if (!expType.equals("moving")) {// если это не внутренний платёж -
                    // меняем в истории контрагента проведенность, уменьшая его долг нашему предприятию
                    commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "orderout","orderout", request.getId(), new BigDecimal(0), request.getSumm(),false,request.getDoc_number(),request.getStatus_id());
                } else { // если это внутренний платёж
                    // При отмене проведения исходящих платежей (Исходящий платеж, Расходный ордер, Выемка) необходимо проверить,
                    // проведён ли входящий платёж.
                    // Если да - сначала нужно отменить проведение входящего платежа (Входящий платёж, Приходный ордер, Внесение),
                    // а затем уже отменять проведение исходящего (естесственно, не программно здесь, а пользователем)
                    Boolean isRecipientCompleted;
                    if(request.getMoving_type().equals("account")) {
                        isRecipientCompleted = financeUtilites.isRecipientCompleted(myMasterId, request.getId(), "paymentin","orderout_id");
                        if(Objects.isNull(isRecipientCompleted))
                            throw new Exception("Error detecting the presence of a posted incoming payment for this outgoing payment");
                        if(isRecipientCompleted)
                            throw new IncomingPaymentIsCompletedException();
                    }else if(request.getMoving_type().equals("boxoffice")) {
                        isRecipientCompleted = financeUtilites.isRecipientCompleted(myMasterId,  request.getId(), "orderin", "orderout_id");
                        if(Objects.isNull(isRecipientCompleted))
                            throw new Exception("Error detecting the presence of a posted incoming order for a given outgoing payment");
                        if(isRecipientCompleted)
                            throw new IncomingPaymentIsCompletedException();
                    }else if(request.getMoving_type().equals("kassa")) {
                        isRecipientCompleted = financeUtilites.isRecipientCompleted(myMasterId,  request.getId(), "depositing", "orderout_id");
                        if(Objects.isNull(isRecipientCompleted))
                            throw new Exception("Error detecting the presence of a posted incoming order for a given outgoing payment");
                        if(isRecipientCompleted)
                            throw new IncomingPaymentIsCompletedException();
                    }else throw new Exception("Outgoing document not defined");
                }
                // меняем проведенность в истории кассы нашего предприятия, тем самым добавляя к ней переводимую сумму
                commonUtilites.addDocumentHistory("boxoffice", request.getCompany_id(), request.getBoxoffice_id(), "orderout","orderout", request.getId(), new BigDecimal(0), request.getSumm(),false,request.getDoc_number(),request.getStatus_id());

                return 1;
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/setOrderoutAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (IncomingPaymentIsCompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/setOrderoutAsDecompleted.", e);
                e.printStackTrace();
                return -32; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method OrderoutRepository/setOrderoutAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsOrderout(SettingsOrderoutForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);
            commonUtilites.idBelongsMyMaster("cagents", row.getCagentId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_status_dock", row.getStatusIdOnComplete(), myMasterId);
            stringQuery =
                    " insert into settings_orderout (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "date_time_update, " +
                            "cagent_id, "+          //поставщик по умолчанию
                            "status_id_on_complete"+// статус документа при проведении
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            "now(), " +
                            row.getCagentId() + ","+
                            row.getStatusIdOnComplete()+
                            ") " +
                            " ON CONFLICT ON CONSTRAINT settings_orderout_user_uq " +// "upsert"
                            " DO update set " +
                            " cagent_id = "+row.getCagentId()+"," +
                            " company_id = "+row.getCompanyId()+"," +
                            " date_time_update = now()," +
                            " status_id_on_complete = "+row.getStatusIdOnComplete();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsOrderout. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsOrderoutJSON getSettingsOrderout() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        Long masterId = userRepositoryJPA.getMyMasterId();
        stringQuery = "select " +
                "           p.cagent_id as cagent_id, " +
                "           pgp_sym_decrypt(cg.name_enc,:cryptoPassword) as cagent, " +                                          // контрагент
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                 // предприятие
                "           p.status_id_on_complete as status_id_on_complete " +           // статус по проведении
                "           from settings_orderout p " +
                "           LEFT OUTER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            String cryptoPassword = cryptoService.getCryptoPasswordFromDatabase(masterId);
            query.setParameter("cryptoPassword", cryptoPassword);
            List<Object[]> queryList = query.getResultList();

            SettingsOrderoutJSON returnObj=new SettingsOrderoutJSON();

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
            logger.error("Exception in method getSettingsOrderout. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteOrderout (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(36L,"520") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(36L,"521") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "orderout");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update orderout p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "orderout")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteOrderout. SQL query:" + stringQuery, e);
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
    public Integer undeleteOrderout(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(36L,"520") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(36L,"521") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update orderout p" +
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
                logger.error("Exception in method undeleteOrderout. SQL query:"+stringQuery, e);
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
    public Boolean addFilesToOrderout(UniversalForm request){
        Long orderoutId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(36L,"524") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout",orderoutId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(36L,"525") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",orderoutId.toString())))
        {
            try
            {
                String stringQuery;
                Long masterId = userRepositoryJPA.getMyMasterId();
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {
                    commonUtilites.idBelongsMyMaster("files", fileId, masterId);
                    stringQuery = "select orderout_id from orderout_files where orderout_id=" + orderoutId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_OrderoutId_FileId(orderoutId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method OrderoutRepository/addFilesToOrderout.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_OrderoutId_FileId(Long orderoutId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into orderout_files " +
                    "(orderout_id,file_id) " +
                    "values " +
                    "(" + orderoutId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method OrderoutRepository/manyToMany_OrderoutId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesUniversalJSON> getListOfOrderoutFiles(Long orderoutId) {
        if(securityRepositoryJPA.userHasPermissions_OR(36L, "522,523"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           orderout p" +
                    "           inner join" +
                    "           orderout_files pf" +
                    "           on p.id=pf.orderout_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + orderoutId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(36L, "522")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();

                List<FilesUniversalJSON> orderoutList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesUniversalJSON doc=new FilesUniversalJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    orderoutList.add(doc);
                }
                return orderoutList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfOrderoutFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteOrderoutFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(36L,"524") && securityRepositoryJPA.isItAllMyMastersDocuments("orderout", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(36L,"525") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("orderout",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from orderout_files "+
                    " where orderout_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from orderout where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method OrderoutRepository/deleteOrderoutFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
    // отдает список внутренних наличных платежей по кассе предприятия, деньги из которых еще не были доставлены в кассу предприятия или на расч. счёт (т.е. нет проведения приходного ордера или входящего платежа по этим деньгам)
    @SuppressWarnings("Duplicates")
    public List<OrderoutJSON> getOrderoutList(Long boxoffice_id,Long recipient_id) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery = "select  p.id as id, " +
                "           '№'||p.doc_number||', '||to_char(p.summ, '999999999990.99')||' руб.' as boxoffice, " +// наименование кассы
                "           p.doc_number as doc_number, " +
                "           coalesce(p.summ,0) as summ " +

                "           from orderout p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN sprav_expenditure_items exp on p.expenditure_id=exp.id" +
                "           INNER JOIN sprav_boxoffice ap ON p.boxoffice_id=ap.id " +
                "           where  p.master_id=" + myMasterId +
                "           and p.boxoffice_id = " + boxoffice_id +
                "           and coalesce(p.is_completed,false) = true" +
                "           and coalesce(p.is_deleted,false) = false" +
                "           and ((      p.moving_type='boxoffice'   and p.boxoffice_to_id=:recipient_id) " +
                "                   or (p.moving_type='account'     and p.payment_account_to_id=:recipient_id) " +
                "                   or (p.moving_type='kassa'       and p.kassa_to_id=:recipient_id))" +
                "           and exp.type = 'moving'" +                  // moving - внутреннее перевод
                "           and coalesce(p.is_delivered,false) = false";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("recipient_id",recipient_id);
            List<Object[]> queryList = query.getResultList();
            List<OrderoutJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                OrderoutJSON doc=new OrderoutJSON();
                doc.setId(Long.parseLong(                     obj[0].toString()));
                doc.setBoxoffice((String)                     obj[1]);
                doc.setDoc_number(Long.parseLong(             obj[2].toString()));
                doc.setSumm((BigDecimal)                      obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getOrderoutList. SQL query:" + stringQuery, e);
            return null;
        }
    }
}
