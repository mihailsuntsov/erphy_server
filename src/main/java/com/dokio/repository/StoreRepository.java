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

import com.dokio.message.request.Sprav.StoresForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.Sprav.StoresJSON;
import com.dokio.message.response.Sprav.StoresListJSON;
import com.dokio.message.response.additional.StoreTranslationCategoryJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class StoreRepository {

    Logger logger = Logger.getLogger(StoreRepository.class);

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
            .of("name","company","creator","lang_code","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<StoresJSON> getStoresTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(54L, "676,677"))// (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.lang_code as lang_code, " +
                    "           p.is_deleted as is_deleted, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort  " +
                    "           from stores p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(54L, "676"))
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.lang_code) like upper(CONCAT('%',:sg,'%'))" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            List<Object[]> queryList = query.getResultList();
            List<StoresJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoresJSON doc = new StoresJSON();

                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setMaster((String)                              obj[1]);
                doc.setCreator((String)                             obj[2]);
                doc.setChanger((String)                             obj[3]);
                doc.setMaster_id(Long.parseLong(                    obj[4].toString()));
                doc.setCreator_id(Long.parseLong(                   obj[5].toString()));
                doc.setChanger_id(obj[6] != null ? Long.parseLong(  obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(                   obj[7].toString()));
                doc.setCompany((String)                             obj[8]);
                doc.setDate_time_created((String)                   obj[9]);
                doc.setDate_time_changed((String)                   obj[10]);
                doc.setName((String)                                obj[11]);
                doc.setLang_code((String)                           obj[12]);
                doc.setIs_deleted((Boolean)                         obj[13]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getStoresSize(String searchString, Long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(54L, "676,677"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  count(*) " +
                    "           from stores p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(54L, "676")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.lang_code) like upper(CONCAT('%',:sg,'%'))" + ")";
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
    public StoresJSON getStoresValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(54L,"676,677"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           upper(p.lang_code) as lang_code, " + // code of store language, e.g. EN
                    "           p.store_type, " +                   // e.g. woo
                    "           p.store_api_version, " +            // e.g. v3
                    "           p.crm_secret_key, " +               // like UUID generated
                    "           p.store_price_type_regular, " +     // id of regular type price
                    "           p.store_price_type_sale, " +        // id of sale type price
                    "           p.store_orders_department_id, " +   // department for creation Customer order from store
                    "           p.store_if_customer_not_found, " +  // "create_new" or "use_default". Default is "create_new"
                    "           p.store_default_customer_id, " +    // counterparty id if store_if_customer_not_found=use_default
                    "           p.store_default_creator_id, " +     // default user that will be marked as a creator of store order. Default is master user
                    "           p.store_days_for_esd, " +           // number of days for ESD of created store order. Default is 0
                    "           coalesce(p.store_auto_reserve,false), " +// auto reserve product after getting internet store order
                    "           p.store_ip, " +                     // internet-store ip address
                    "           cag.name as store_default_customer," +
                    "           uoc.name as store_default_creator" +
                    "           from stores p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN cagents cag ON p.store_default_customer_id=cag.id" +
                    "           LEFT OUTER JOIN users uoc ON p.store_default_creator_id=uoc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(54L, "676")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (677)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();

                StoresJSON doc = new StoresJSON();

                for (Object[] obj : queryList) {

                    doc.setId(Long.parseLong(                           obj[0].toString()));
                    doc.setMaster((String)                              obj[1]);
                    doc.setCreator((String)                             obj[2]);
                    doc.setChanger((String)                             obj[3]);
                    doc.setMaster_id(Long.parseLong(                    obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(                   obj[5].toString()));
                    doc.setChanger_id(obj[6] != null ? Long.parseLong(  obj[6].toString()) : null);
                    doc.setCompany_id(Long.parseLong(                   obj[7].toString()));
                    doc.setCompany((String)                             obj[8]);
                    doc.setDate_time_created((String)                   obj[9]);
                    doc.setDate_time_changed((String)                   obj[10]);
                    doc.setName((String)                                obj[11]);
                    doc.setLang_code((String)                           obj[12]);
                    doc.setStore_type((String)                          obj[13]);
                    doc.setStore_api_version((String)                   obj[14]);
                    doc.setCrm_secret_key((String)                      obj[15]);
                    doc.setStore_price_type_regular(Long.parseLong(     obj[16].toString()));
                    doc.setStore_price_type_sale(obj[17]!=null?Long.parseLong(obj[17].toString()):null);
                    doc.setStore_orders_department_id(Long.parseLong(   obj[18].toString()));
                    doc.setStore_if_customer_not_found((String)         obj[19]);
                    doc.setStore_default_customer_id(obj[20]!=null?Long.parseLong(obj[20].toString()):null);
                    doc.setStore_default_creator_id(Long.parseLong(     obj[21].toString()));
                    doc.setStore_days_for_esd((Integer)                 obj[22]);
                    doc.setStore_auto_reserve((Boolean)                 obj[23]);
                    doc.setStore_ip((String)                            obj[24]);
                    doc.setStore_default_customer(obj[25]!=null?(String)obj[25]:"");
                    doc.setStore_default_creator((String)               obj[26]);
                    doc.setStoreDepartments(getStoreDepartmentsIds(id, doc.getCompany_id(), myMasterId));

                }
                return doc;
            } catch (Exception e) {
                logger.error("Exception in method getStoresValues on selecting from stores. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Integer updateStores(StoresForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(54L,"678") && securityRepositoryJPA.isItAllMyMastersDocuments("stores",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(54L,"679") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("stores",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId=userRepository.getUserId();
            String stringQuery;
            stringQuery =   " update " +
                    " stores " +
                    " set " +
                    " changer_id = " + myId + ", " +
                    " date_time_changed= now()," +
                    " name = :name, " +
                    " lang_code = upper(:lang_code), " +
                    " store_ip = :store_ip, " +
                    " store_type = 'woo', " +
                    " store_api_version = 'v3', " +
                    " crm_secret_key = :crm_secret_key, " +
                    " store_price_type_regular = " + request.getStore_price_type_regular() + ", " +
                    " store_price_type_sale = " + request.getStore_price_type_sale() + ", " +
                    " store_orders_department_id = " + request.getStore_orders_department_id() + ", " +
                    " store_if_customer_not_found = :store_if_customer_not_found, " +
                    " store_default_customer_id = " + request.getStore_default_customer_id() + ", " +
                    " store_default_creator_id = " + request.getStore_default_creator_id() + ", " +
                    " store_days_for_esd = " + request.getStore_days_for_esd() + ", " +
                    " store_auto_reserve = " + request.getStore_auto_reserve() +
                    " where " +
                    " master_id = " + myMasterId +
                    " and id= "+request.getId();
            try
            {
                //сохранение полей документа
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", (request.getName() == null ? "" : request.getName()));
                query.setParameter("lang_code", request.getLang_code());
                query.setParameter("store_ip", request.getStore_ip());
                query.setParameter("crm_secret_key", request.getCrm_secret_key());
                query.setParameter("store_if_customer_not_found", request.getStore_if_customer_not_found());
                query.executeUpdate();
                if(isStoreDepartsChanged(request.getStoreDepartments(), myMasterId, request.getCompany_id(), request.getId()))
                    markAllStoreProductsAsNeedToSyncWoo(myMasterId, request.getCompany_id(), request.getId());
                insertStoreDepartments(request, myMasterId);
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method insertStores on updating stores. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertStores(StoresForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(54L, "672")) ||
                        //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                        (securityRepositoryJPA.userHasPermissions_OR(54L, "673") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            Long myId = userRepository.getUserId();
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            String stringQuery =
                    "insert into stores (" +
                            " master_id," +                     // мастер-аккаунт
                            " creator_id," +                    // создатель
                            " company_id," +                    // предприятие, для которого создается документ
                            " date_time_created," +             // дата и время создания
                            " name," +                          // name
                            " lang_code, " +                    // e.g. EN
                            " store_ip, " +                     // e.g. 127.0.0.1
                            " store_type, " +                   // now always = woo
                            " store_api_version, " +            // now always = v3
                            " crm_secret_key, " +               // like UUID generated
                            " store_price_type_regular, " +     // id of regular type price
                            " store_price_type_sale, " +        // id of sale type price
                            " store_orders_department_id, " +   // department for creation Customer order from store
                            " store_if_customer_not_found, " +  // "create_new" or "use_default". Default is "create_new"
                            " store_default_customer_id, " +    // counterparty id if store_if_customer_not_found=use_default
                            " store_default_creator_id, " +     // default user that will be marked as a creator of store order. Default is master user
                            " store_days_for_esd, " +           // number of days for ESD of created store order. Default is 0
                            " store_auto_reserve, " +           // auto reserve product after getting internet store order
                            " is_deleted" +                     // deleted
                            ") values ("+
                            myMasterId + ", "+//мастер-аккаунт
                            myId + ", "+ //создатель
                            request.getCompany_id() + ", "+//предприятие, для которого создается документ
                            "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                            ":name," +
                            "upper(:lang_code)," +
                            ":store_ip," +
                            "'woo',"+
                            "'v3',"+
                            ":crm_secret_key," +
                            request.getStore_price_type_regular() + ", " +
                            request.getStore_price_type_sale() + ", " +
                            request.getStore_orders_department_id() + ", " +
                            ":store_if_customer_not_found, " +
                            request.getStore_default_customer_id() + ", " +
                            request.getStore_default_creator_id() + ", " +
                            request.getStore_days_for_esd() + ", " +
                            request.getStore_auto_reserve() + ", " +
                            "false" +
                            ")";// уникальный идентификатор документа

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("lang_code",request.getLang_code());
                query.setParameter("crm_secret_key",request.getCrm_secret_key());
                query.setParameter("store_if_customer_not_found",request.getStore_if_customer_not_found());
                query.setParameter("store_ip", request.getStore_ip());
                query.executeUpdate();
                stringQuery="select id from stores where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                Long createdDoc = Long.valueOf(query2.getSingleResult().toString());
                // saving store departments
                request.setId(createdDoc);
                insertStoreDepartments(request, myMasterId);
                return createdDoc;
            } catch (Exception e) {
                logger.error("Exception in method insertStores on inserting into stores. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteStores(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(54L, "674") && securityRepositoryJPA.isItAllMyMastersDocuments("stores", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(54L, "675") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("stores", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery =
                    " update stores p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteStores on updating stores. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteStores(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(54L, "674") && securityRepositoryJPA.isItAllMyMastersDocuments("stores", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(54L, "675") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("stores", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery =
                    " update stores p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteStores on updating stores. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
//*****************************************************************************************************************************************************
//*******************************************************************  U T I L S **********************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<Long> getStoreDepartmentsIds (Long storeId, Long companyId, Long masterId) {
        String stringQuery;
        stringQuery = "     select   csd.department_id as id" +
                "           from     store_departments csd " +
                "           where    csd.master_id=" + masterId +
                "           and      csd.company_id =" + companyId +
                "           and      csd.store_id =" + storeId +
                "           order by csd.menu_order";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<BigInteger> queryList = query.getResultList();
            List<Long> returnList = new ArrayList<>();
            for (BigInteger obj : queryList) {
                returnList.add(obj.longValue());
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getStoreDepartmentsIds. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    private void insertStoreDepartments(StoresForm request, Long masterId) throws Exception {
        Set<Long> departsIds=new HashSet<>();
        int i = 0;
        try{
            if (request.getStoreDepartments()!=null && request.getStoreDepartments().size() > 0) {
                for (Long departId : request.getStoreDepartments()) {
                    saveStoreDepartment(departId,request.getCompany_id(), masterId, request.getId(), i);
                    departsIds.add(departId);
                    i++;
                }
            }
            deleteStoreDepartmentsExcessRows(departsIds.size()>0?(cu.SetOfLongToString(departsIds,",","","")):"0", masterId, request.getCompany_id(), request.getId());
        }catch (Exception e) {
            logger.error("Error of insertStoreDepartments.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private void saveStoreDepartment(Long departId, Long companyId, Long masterId, Long storeId, int menuOrder) throws Exception {
        String stringQuery;

        stringQuery =   " insert into store_departments (" +
                        " master_id," +
                        " company_id," +
                        " store_id," +
                        " department_id," +
                        " menu_order" +
                        ") values (" +
                        masterId + ", " +
                        companyId + ", " +
                        storeId + ", " +
                        departId + ", " +
                        menuOrder +
                        ") ON CONFLICT ON CONSTRAINT store_department_uq " +// "upsert"
                        " DO update set " +
                        " menu_order = " + menuOrder;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method StoreRepository/saveCompanyStoreDepartment. SQL query:"+stringQuery, e);
            throw new Exception();//кидаем исключение чтобы произошла отмена транзакции
        }
    }
    private Boolean deleteStoreDepartmentsExcessRows(String departIds, Long masterId, Long companyId, Long storeId) {
        String stringQuery;

        stringQuery =   " delete from store_departments " +
                        " where master_id = " + masterId +" and company_id = " + companyId +
                        " and store_id = " + storeId + " and department_id not in (" + departIds.replaceAll("[^0-9\\,]", "") + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreRepository/deleteStoreDepartmentsExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    private Boolean isStoreDepartsChanged (List<Long> newDepartsList, Long masterId, Long companyId, Long storeId) throws Exception  {
        String stringQuery;
        stringQuery =
                        " select csd.department_id" +
                        " from   store_departments csd " +
                        " where  csd.master_id = "+masterId+" and csd.company_id =" + companyId + " and csd.store_id =" + storeId ;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<BigInteger> queryList = query.getResultList();
            List<Long> allDataBaseIds = new ArrayList<>();
            for (BigInteger obj : queryList) {
                allDataBaseIds.add(obj.longValue());
            }
            return !(new HashSet<>(allDataBaseIds).equals(new HashSet<>(newDepartsList)));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method isStoreDepartsChanged. SQL query:" + stringQuery, e);
            throw new Exception();
        }
    }

    @SuppressWarnings("Duplicates")
    private void markAllStoreProductsAsNeedToSyncWoo(Long masterId, Long companyId, Long storeId) throws Exception {
        String stringQuery =
                " update stores_products " +
                " set need_to_syncwoo = true " +
                " where " +
                " master_id = " + masterId +" and company_id = " + companyId + " and store_id = " + storeId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markAllStoreProductsAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional//отдает список налогов по id предприятия
    @SuppressWarnings("Duplicates")
    public List<StoresListJSON> getStoresList(Long companyId) {

        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select" +
                "           id as id, " +
                "           name as name " +
                "           from stores " +
                "           where  master_id=" + myMasterId +
                "           and company_id=" + companyId +
                "           and coalesce(is_deleted,false)=false" +
                "           order by name";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<StoresListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoresListJSON doc = new StoresListJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setName((String) obj[1]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getStoresList. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getStoresLanguagesList(Long companyId) {
        String stringQuery;
        Long masterId = userRepositoryJPA.getMyMasterId();
        stringQuery = "select" +
                "           p.lang_code as lang_code " +
                "           from stores p " +
                "           where  p.master_id=" + masterId +
                "           and p.company_id=" + companyId +
                "           and coalesce(p.is_deleted,false)=false" +
                "           group by lang_code " +
                "           order by lang_code";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<String> queryList = query.getResultList();
            List<String> languagesList = new ArrayList<>();
            for (String obj : queryList) {
                languagesList.add(obj);
            }
            return queryList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getStoresLanguagesList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    public List<StoreTranslationCategoryJSON> getStoreCategoryTranslationsList(Long categoryId){
        Long masterId = userRepositoryJPA.getMyMasterId();
        String stringQuery = "      select   p.lang_code as lang_code," +
                        "           coalesce(p.name,'') as name, " +
                        "           coalesce(p.slug,'') as slug, " +
                        "           coalesce(p.description,'') as description " +
                        "           from     store_translate_categories p " +
                        "           where    p.master_id=" + masterId +
                        "           and      p.category_id =" + categoryId +
                        "           order by p.lang_code";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<StoreTranslationCategoryJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoreTranslationCategoryJSON doc = new StoreTranslationCategoryJSON();
                doc.setLangCode((String)                                obj[0]);
                doc.setName((String)                                    obj[1]);
                doc.setSlug((String)                                    obj[2]);
                doc.setDescription((String)                             obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getStoreCategoryTranslationsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
}