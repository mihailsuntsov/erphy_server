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

import com.dokio.message.request.CompaniesPaymentAccountsForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.CompaniesPaymentAccountsJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CompaniesPaymentAccountsRepositoryJPA {

    Logger logger = Logger.getLogger("CompaniesPaymentAccountsRepositoryJPA");

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
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userService;
    @Autowired
    CommonUtilites cu;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","description","cagent","is_main","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<CompaniesPaymentAccountsJSON> getPaymentAccountTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(52L, "658,659"))//(см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            String dateFormat=userRepositoryJPA.getMyDateFormat();
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_main,false) as is_main, " +
                    "           p.swift as swift, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort  " +
                    "           from companies_payment_accounts p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(52L, "658")) //Если нет прав на "Просмотр "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name)             like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.swift)            like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.iban)             like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.corr_account)     like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.bik)              like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.payment_account)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.address)          like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.intermediatery)   like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description)      like upper(CONCAT('%',:sg,'%')) )";



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

                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<CompaniesPaymentAccountsJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    CompaniesPaymentAccountsJSON doc = new CompaniesPaymentAccountsJSON();

                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setMaster((String)                                  obj[1]);
                    doc.setCreator((String)                                 obj[2]);
                    doc.setChanger((String)                                 obj[3]);
                    doc.setMaster_id(Long.parseLong(                        obj[4].toString()));
                    doc.setCreator_id(obj[5] != null ? Long.parseLong(      obj[5].toString()) : null);
                    doc.setChanger_id(obj[6] != null ? Long.parseLong(      obj[6].toString()) : null);
                    doc.setCompany_id(Long.parseLong(                       obj[7].toString()));
                    doc.setCompany((String)                                 obj[8]);
                    doc.setDate_time_created((String)                       obj[9]);
                    doc.setDate_time_changed((String)                       obj[10]);
                    doc.setName((String)                                    obj[11]);
                    doc.setDescription((String)                             obj[12]);
                    doc.setIs_main((Boolean)                                obj[13]);
                    doc.setSwift((String)                                   obj[14]);
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPaymentAccountTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getPaymentAccountSize(String searchString, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(52L, "658,659"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from companies_payment_accounts p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(52L, "658")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name)             like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.swift)            like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.iban)             like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.corr_account)     like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.bik)              like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.payment_account)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.address)          like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.intermediatery)   like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description)      like upper(CONCAT('%',:sg,'%')) )";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public CompaniesPaymentAccountsJSON getPaymentAccountValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(52L, "658,659"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
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
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_main,false) as is_main, " +
                    "           p.intermediatery as intermediatery, " +
                    "           p.swift as swift, " +
                    "           p.iban as iban, " +
                    "           p.corr_account as corr_account, " +
                    "           p.bik as bik, " +
                    "           coalesce(p.payment_account,'') as payment_account, " +
                    "           p.address as address " +

                    "           from companies_payment_accounts p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(52L, "658")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (659)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            CompaniesPaymentAccountsJSON doc = new CompaniesPaymentAccountsJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setMaster((String) obj[1]);
                doc.setCreator((String) obj[2]);
                doc.setChanger((String) obj[3]);
                doc.setMaster_id(Long.parseLong(obj[4].toString()));
                doc.setCreator_id(obj[5] != null ? Long.parseLong(obj[5].toString()) : null);
                doc.setChanger_id(obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(obj[7].toString()));
                doc.setCompany((String) obj[8]);
                doc.setDate_time_created((String) obj[9]);
                doc.setDate_time_changed((String) obj[10]);
                doc.setName((String) obj[11]);
                doc.setDescription((String) obj[12]);
                doc.setIs_main((Boolean) obj[13]);
                doc.setIntermediatery((String) obj[14]);
                doc.setSwift((String) obj[15]);
                doc.setIban((String) obj[16]);
                doc.setCorr_account((String) obj[17]);
                doc.setBik((String) obj[18]);
                doc.setPayment_account((String) obj[19]);
                doc.setAddress((String) obj[20]);
            }
            return doc;
        } else return null;

    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updatePaymentAccount(CompaniesPaymentAccountsForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(52L,"660") && securityRepositoryJPA.isItAllMyMastersDocuments("companies_payment_accounts",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(52L,"661") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("companies_payment_accounts",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update companies_payment_accounts set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " name = :name, " +
                    " intermediatery = :intermediatery, " +
                    " corr_account = :corr_account, " +
                    " payment_account = :payment_account, " +
                    " bik = :bik, " +
                    " swift = :swift, " +
                    " iban = :iban, " +
                    " address = :address, " +
                    " description = :description " +
                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;

            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.setParameter("intermediatery",request.getIntermediatery());
                query.setParameter("swift",request.getSwift());
                query.setParameter("iban",request.getIban());
                query.setParameter("corr_account",request.getCorr_account());
                query.setParameter("payment_account",request.getPayment_account());
                query.setParameter("bik",request.getBik());
                query.setParameter("address",request.getAddress());
                query.executeUpdate();

                return 1;

            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updatePaymentAccount. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав

    }

    // Возвращаем id в случае успешного создания
    // Возвращаем null в случае ошибки
    // Возвращаем -1 в случае отсутствия прав
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertPaymentAccount(CompaniesPaymentAccountsForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(52L, "654")) ||
                //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                (securityRepositoryJPA.userHasPermissions_OR(52L, "655") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery = "insert into companies_payment_accounts (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " date_time_created," + //дата и время создания
                    " name," +//наименование
                    " is_main, " +
                    " intermediatery, " +
                    " swift, " +
                    " iban, " +
                    " corr_account, " +
                    " payment_account, " +
                    " bik, " +
                    " output_order, " +
                    " address, " +
                    " description" +// тип
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    " :name, " +
                    " false, " +
                    " :intermediatery, " +
                    " :swift, " +
                    " :iban, " +
                    " :corr_account, " +
                    " :payment_account, " +
                    " :bik, " +
                    " (select max(output_order)+1 from companies_payment_accounts where company_id="+request.getCompany_id()+")," +
                    " :address, " +
                    " :description)";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.setParameter("intermediatery",request.getIntermediatery());
                query.setParameter("swift",request.getSwift());
                query.setParameter("iban",request.getIban());
                query.setParameter("corr_account",request.getCorr_account());
                query.setParameter("payment_account",request.getPayment_account());
                query.setParameter("bik",request.getBik());
                query.setParameter("address",request.getAddress());
                query.executeUpdate();
                stringQuery="select id from companies_payment_accounts where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);

                return Long.valueOf(query2.getSingleResult().toString());
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertPaymentAccount on inserting into companies_payment_accounts. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            return -1L;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deletePaymentAccount(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(52L, "656") && securityRepositoryJPA.isItAllMyMastersDocuments("companies_payment_accounts", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(52L, "657") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("companies_payment_accounts", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update companies_payment_accounts p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deletePaymentAccount. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeletePaymentAccount(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(52L, "656") && securityRepositoryJPA.isItAllMyMastersDocuments("companies_payment_accounts", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(52L, "657") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("companies_payment_accounts", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update companies_payment_accounts p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeletePaymentAccount. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer setMainPaymentAccount(UniversalForm request) {// id : предприятие, id3 : id расхода
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getId());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if ((   //если есть право на редактирование по всем предприятиям, или
            (securityRepositoryJPA.userHasPermissions_OR(52L, "660")) ||
            //если есть право на редактирование по всем отделениям своего предприятия, и предприятие документа своё, и
            (securityRepositoryJPA.userHasPermissions_OR(52L, "661") && myCompanyId.equals(request.getId()))) &&
            //редактируется документ предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
            DocumentMasterId.equals(myMasterId))
        {
            try
            {
                String stringQuery;
                stringQuery =   " update companies_payment_accounts set is_main=(" +
                        " case when (id="+request.getId3()+") then true else false end) " +
                        " where " +
                        " company_id= "+request.getId();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    public Long getDefaultPaymentAccount(Long companyId) {
        String stringQuery =
                " select id from companies_payment_accounts where company_id= " + companyId + " and is_main = true";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).longValue();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            logger.error("Exception in method getDefaultPaymentAccount. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // inserting base set of cash room for new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertPaymentAccountsFast(Long mId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = cu.translateForUser(mId, new String[]{"'main_bank_acc'"});
        stringQuery = "insert into companies_payment_accounts ( master_id,creator_id,company_id,date_time_created,name,is_main,is_deleted,output_order) values "+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("main_bank_acc")+"',true,false,1)";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery="select id from companies_payment_accounts where date_time_created=(to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+mId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query2.getSingleResult().toString());
        } catch (Exception e) {
            logger.error("Exception in method insertPaymentAccountsFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

}