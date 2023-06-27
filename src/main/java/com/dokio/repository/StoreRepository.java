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
import com.dokio.message.request.additional.RentStoreOrderForm;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.Sprav.StoresJSON;
import com.dokio.message.response.Sprav.StoresListJSON;
import com.dokio.message.response.additional.StoreForOrderingJSON;
import com.dokio.message.response.additional.StoreForOrderingShortInfoJSON;
import com.dokio.message.response.additional.StoreOrderingResultJSON;
import com.dokio.message.response.additional.StoreTranslationCategoryJSON;
import com.dokio.model.Companies;
import com.dokio.repository.Exceptions.LanguageVersionOfSiteIsNotUniqueException;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class StoreRepository {

    Logger logger = Logger.getLogger(StoreRepository.class);

    @Value("${stores.secret}")
    private String stores_secret;

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
    MailRepository mailRepository;
    @Autowired
    CommonUtilites cu;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","company","creator","is_let_sync","lang_code","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

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
                    "           p.is_let_sync as is_let_sync, " +
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
                doc.setIs_let_sync((Boolean)                        obj[14]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
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

            return ((BigInteger)query.getSingleResult()).intValue();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

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
                    "           cag.name as store_default_customer," + // customer_id if store_if_customer_not_found="use_default"
                    "           uoc.name as store_default_creator," +// user-creator of orders that incoming from the online store
                    "           p.is_let_sync," + // synchronization allowed
                    "           (select is_saas from settings_general) as is_saas," + // is this SaaS? (getting from settings_general)
                    "           (select is_sites_distribution from settings_general) as is_sites_distribution," + // is there possibility to order sites in this SaaS? (getting from settings_general)
                    "           (select count(*) from _saas_stores_for_ordering where store_id="+id+" and is_deleted=false)=0 as can_order_store," + // can user order the store at this moment
                    "           p.is_deleted as is_deleted" +
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
                    doc.setIs_let_sync((Boolean)                        obj[27]);
                    doc.setIs_saas((Boolean)                            obj[28]);
                    doc.setIs_sites_distribution((Boolean)              obj[29]);
                    doc.setCan_order_store((Boolean)                    obj[30]);
                    doc.setIs_deleted((Boolean)                         obj[31]);

                    doc.setStoreDepartments(getStoreDepartmentsIds(id, doc.getCompany_id()));

                }
                return doc;
            } catch (Exception e) {
                logger.error("Exception in method getStoresValues on selecting from stores. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Integer updateStores(StoresForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(54L,"678") && securityRepositoryJPA.isItAllMyMastersDocuments("stores",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(54L,"679") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("stores",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId=userRepository.getUserId();
            boolean needToMarkProducts = false;
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
                    " store_auto_reserve = " + request.getStore_auto_reserve() + ", " +
                    " is_let_sync = " + request.getIs_let_sync() +
                    " where " +
                    " master_id = " + myMasterId +
                    " and id= "+request.getId();
            try
            {
                cu.idBelongsMyMaster("sprav_type_prices", request.getStore_price_type_regular(), myMasterId);
                cu.idBelongsMyMaster("sprav_type_prices", request.getStore_price_type_sale(), myMasterId);
                cu.idBelongsMyMaster("departments", request.getStore_orders_department_id(), myMasterId);
                cu.idBelongsMyMaster("cagents", request.getStore_default_customer_id(), myMasterId);
                cu.idBelongsMyMaster("users", request.getStore_default_creator_id(), myMasterId);
                cu.idBelongsMyMaster("stores", request.getId(), myMasterId);
                cu.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);


                StoresJSON oldValues = getStoresValues(request.getId());

                boolean isStoreDepartsChanged = isStoreDepartsChanged(request.getStoreDepartments(), myMasterId, request.getCompany_id(), request.getId());
                boolean isLanguageChanged =     !oldValues.getLang_code().toUpperCase().equals(request.getLang_code().toUpperCase());
                boolean isRegularPriceChanged = !(oldValues.getStore_price_type_regular()==request.getStore_price_type_regular());
                boolean isSalePriceChanged =    !(oldValues.getStore_price_type_sale()==request.getStore_price_type_sale());

                needToMarkProducts=isStoreDepartsChanged||isLanguageChanged||isRegularPriceChanged||isSalePriceChanged;

                //сохранение полей документа
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", (request.getName() == null ? "" : request.getName()));
                query.setParameter("lang_code", request.getLang_code());
                query.setParameter("store_ip", request.getStore_ip());
                query.setParameter("crm_secret_key", request.getCrm_secret_key());
                query.setParameter("store_if_customer_not_found", request.getStore_if_customer_not_found());
                query.executeUpdate();

                if(needToMarkProducts)
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
            //plan limit check
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if(userRepositoryJPA.getMyConsumedResources().getStores()>=userRepositoryJPA.getMyMaxAllowedResources().getStores())
                    return -121L; // number of stores is out of bounds of tariff plan

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
                            " is_deleted," +                     // deleted
                            " is_let_sync" +
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
                            "false" + ", " +
                            request.getIs_let_sync() +
                            ")";// уникальный идентификатор документа
            try{

                cu.idBelongsMyMaster("sprav_type_prices", request.getStore_price_type_regular(), myMasterId);
                cu.idBelongsMyMaster("sprav_type_prices", request.getStore_price_type_sale(), myMasterId);
                cu.idBelongsMyMaster("departments", request.getStore_orders_department_id(), myMasterId);
                cu.idBelongsMyMaster("cagents", request.getStore_default_customer_id(), myMasterId);
                cu.idBelongsMyMaster("users", request.getStore_default_creator_id(), myMasterId);
                cu.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);

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
            Long masterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();

            String stringQuery =
                    " update stores p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + masterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")" +
                    " and p.id not in (select store_id from _saas_stores_for_ordering where coalesce(master_id,0)="+masterId+" and distributed=true and is_deleted=false)";
            try{
                if(storesHaveNonDeletedSites(delNumbers,masterId))
                    //С одним из удаляемых интернет-магазинов связан сайт, который не был удален. Сначала нужно удалить сайт
                    //One of the online stores to be deleted has a site linked to it that has not been deleted. First you need to delete the site
                    return -360;
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

    private boolean storesHaveNonDeletedSites(String delNumbers, Long masterId) throws Exception {
        String stringQuery =
                " select count(*) from _saas_stores_for_ordering where coalesce(master_id,0)="+masterId+" and" +
//                        " distributed=true and " +
                        " is_deleted=false and " +
                        " store_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger) query.getSingleResult()).intValue()>0;
        } catch (Exception e) {
            logger.error("Exception in method storesHaveNonDeletedSites on updating stores. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
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
            long amountToRepair = delNumbers.split(",").length;
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if((userRepositoryJPA.getMyConsumedResources().getStores()+amountToRepair)>userRepositoryJPA.getMyMaxAllowedResources().getStores())
                    return -121; // number of stores is out of bounds of tariff plan
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
    public List<Long> getStoreDepartmentsIds (Long storeId, Long companyId) {
        String stringQuery;
        stringQuery = "     select   csd.department_id as id" +
                "           from     store_departments csd " +
                "           where    csd.company_id =" + companyId +
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
                    cu.idBelongsMyMaster("departments", departId, masterId);
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


    private void markAllStoreProductsAsNeedToSyncWoo(Long masterId, Long companyId, Long storeId) throws Exception {
        String stringQuery =

                " update stores_products " +
                    " set need_to_syncwoo = true " +
                    " where " +
                    " master_id = " + masterId +" and company_id = " + companyId + " and store_id = " + storeId + ";"+

                " update stores_variations " +
                    " set need_to_syncwoo = true " +
                    " where " +
                    " master_id = " + masterId + " and company_id = " + companyId + " and store_id = " + storeId + ";";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markAllStoreProductsAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

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

    @SuppressWarnings("Duplicates")
    public StoresJSON getStoreBaseValues(Long id) {
        String stringQuery = "select  " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +
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
                "           p.store_ip " +                      // internet-store ip address
                "           from stores p " +
                "           where p.id = " + id;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            StoresJSON doc = new StoresJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setCompany_id(Long.parseLong(obj[1].toString()));
                doc.setName((String) obj[2]);
                doc.setLang_code((String) obj[3]);
                doc.setStore_type((String) obj[4]);
                doc.setStore_api_version((String) obj[5]);
                doc.setCrm_secret_key((String) obj[6]);
                doc.setStore_price_type_regular(Long.parseLong(obj[7].toString()));
                doc.setStore_price_type_sale(obj[8] != null ? Long.parseLong(obj[8].toString()) : null);
                doc.setStore_orders_department_id(Long.parseLong(obj[9].toString()));
                doc.setStore_if_customer_not_found((String) obj[10]);
                doc.setStore_default_customer_id(obj[11] != null ? Long.parseLong(obj[11].toString()) : null);
                doc.setStore_default_creator_id(Long.parseLong(obj[12].toString()));
                doc.setStore_days_for_esd((Integer) obj[13]);
                doc.setStore_auto_reserve((Boolean) obj[14]);
                doc.setStore_ip((String) obj[15]);
                doc.setStoreDepartments(getStoreDepartmentsIds(id, doc.getCompany_id()));
            }
            return doc;
        } catch (Exception e) {
            logger.error("Exception in method getStoresValues on selecting from stores. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public StoreOrderingResultJSON getMyRentSite(RentStoreOrderForm request){
                //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        StoreOrderingResultJSON storeOrderingResult = new StoreOrderingResultJSON();
        if(     (securityRepositoryJPA.userHasPermissions_OR(54L,"678") && securityRepositoryJPA.isItAllMyMastersDocuments("stores",request.getStoreId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(54L,"679") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("stores",request.getStoreId().toString())))
        {
            String  user_ip =           request.getUserIp();
            boolean iagree =            request.getIagree();
            Long    companyId =         request.getCompanyId();
            Long    storeId =           request.getStoreId();
            String  agreementType =     request.getAgreementType();
            String  agreementVer =      request.getAgreementVer();
            String  thirdLvlName =      request.getThirdLvlName();
            Boolean isVar =             request.getExistedStoreVariation();   // site is a language variation of the existed site
            Long    parentVarSiteId =   request.getParentVarSiteId(); // parent site of variation site
            String  position =          request.getPosition(); // position of variation name in a domain name "after" or "before". before:"es.mysite.com" or after:"mysite.com/es"
            String  varName =           request.getVarName(); // like es, fr, it ...

            Long masterId = userRepositoryJPA.getMyMasterId();
            Long myId=userRepository.getUserId();
            String langCode = userRepositoryJPA.getUserSuffix(myId);
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            SettingsGeneralJSON settingsGeneral = cu.getSettingsGeneral();
            String siteUrl;
            Long agreementId;

            if(!iagree) {
                storeOrderingResult.setResult(-320);
                return storeOrderingResult; // Не получено согласие на предоставление услуги
                                            // Consent to the service agreement not received
            }

            try{

                cu.idBelongsMyMaster("stores", storeId, masterId);
                cu.idBelongsMyMaster("companies", companyId, masterId);
                cu.idBelongsMyMaster("_saas_stores_for_ordering", parentVarSiteId, masterId);

                if(settingsGeneral.getMax_store_orders_per_24h_1_account()<=getQttOfStoreOrdersOnPeriodFromAccount(24, masterId)){
                    storeOrderingResult.setResult(-350);
                    return storeOrderingResult; // Превышено максимально допустимое количество интернет-магазинов, которые можно заказать за 24 часа с одного аккаунта
                                                // Exceeded the maximum allowable quantity of online stores that can be ordered in 24h from one account
                }
                if(settingsGeneral.getMax_store_orders_per_24h_1_ip()<=getQttOfStoreOrdersOnPeriodFromIp(24, user_ip)){
                    storeOrderingResult.setResult(-351);
                    return storeOrderingResult; // Превышено максимально допустимое количество интернет-магазинов, которые можно заказать за 24 часа с одного IP-адреса
                                                // Exceeded the maximum allowable quantity of online stores that can be ordered in 24h from one IP address
                }

                if(!cu.isCanOrderStoreForRent(storeId)){//This online store connection already has an active online store linked to it
                    storeOrderingResult.setResult(-340);
                    return storeOrderingResult;
                }

                Long rentSiteId = cu.getFreeSiteToRentId();
                StoreForOrderingJSON orderedStoreReturnData = new StoreForOrderingJSON();

                siteUrl = thirdLvlName+"."+settingsGeneral.getRoot_domain();

                if(!Objects.isNull(rentSiteId)){// there are free sites(stores) for rent

                    //getting email address of master user
                    String masterUserEmail = (String)cu.getFieldValueFromTableById("users", "email", masterId, masterId);


                    cu.SetStoreRentAgreementUnit(masterId, myId, storeId, rentSiteId, agreementType, agreementVer, timestamp);
                    // in this case the URL of site is equivalent to site domain name:
                    //siteUrl=(String)cu.getFieldValueFromTableById("_saas_stores_for_ordering","site_domain",masterId, rentSiteId);


                    // set free online store to user
                    distributeOnlineStoreToUser(rentSiteId, timestamp, user_ip, companyId, storeId, masterId, myId, thirdLvlName, isVar, parentVarSiteId, position, varName);

                    // getting distributed store data
                    StoreForOrderingJSON orderedStoreFullData = getStoreForOrderingData(rentSiteId);
                    orderedStoreReturnData.setSite_url(orderedStoreFullData.getSite_url());

                    //updating Store (connection) - setting store IP address and DokioCRM secret key
                    setIpAndSecretKeyToStoreConnection(orderedStoreFullData.getWp_server_ip(), orderedStoreFullData.getDokio_secret_key(), masterId, storeId);

                    //get message for user
                    Map<String, String> map = cu.translateHTMLmessages(myId, new String[]{"'success_online_store_order'"});

                    //info to return:
                    storeOrderingResult.setResult(1);
                    storeOrderingResult.setMessage(map.get("success_online_store_order"));

                    if(!settingsGeneral.getStores_alert_email().equals(""))
                        alarmLowFreeStoresToRent(settingsGeneral.getStores_alert_email(), settingsGeneral.getMin_qtt_stores_alert());

                    if(!settingsGeneral.getStores_alert_email().equals(""))
                        storesToRentOrderedSuccessfully(settingsGeneral.getStores_alert_email(), masterUserEmail, orderedStoreFullData.toString());

                    String subj = "Thank you for ordering online store!";
                        String body =

                            "At first the site will be accessible with a system-generated url: " + orderedStoreFullData.getSite_domain() + "\n"+
                            "It will be available at your chosen name "+siteUrl+" within 24h.\n\n"+

                            "Store this information securely: \n\n\n"+

                            "Site url:                  "   + siteUrl + "\n\n "+
                            "Site admin panel url:      "   + siteUrl + "/wp-admin\n\n "+
                            "Site admin panel login:    "   + orderedStoreFullData.getWp_login() + "\n\n "+
                            "Site admin panel password: "   + orderedStoreFullData.getWp_password() + "\n\n "+

                            "FTP login:                 "   + orderedStoreFullData.getFtp_user() + "\n\n "+
                            "FTP password:              "   + orderedStoreFullData.getFtp_password() + "\n\n "+

                            "Panel domain:              "   + orderedStoreFullData.getPanel_domain() + "\n\n "+
                            "Panel login:               "   + orderedStoreFullData.getClient_login() + "\n\n "+
                            "Panel password:            "   + orderedStoreFullData.getClient_password() + "\n\n "+

                            "Site DB user:              "   + orderedStoreFullData.getDb_user() + "\n\n "+
                            "Site DB password:          "   + orderedStoreFullData.getDb_password() + "\n\n\n"+



                            "Best regards, DokioCRM team!";

                    mailRepository.sentMessage(masterUserEmail,subj,body,langCode);

                } else {    // there are no free stores for rent

                    agreementId = cu.SetStoreRentAgreementUnit(masterId, myId, storeId, null, agreementType, agreementVer, timestamp);
                    //1. creating a waiting store record
                    String siteDomain = null;
                    if(isVar) { // if there the site is an variation of another site
                        String parentSiteDomain = (String)cu.getFieldValueFromTableById("_saas_stores_for_ordering","site_domain", masterId, parentVarSiteId);
                        siteDomain = varName+"."+parentSiteDomain;
                        siteUrl = position.equals("before")?siteDomain:(parentSiteDomain+"/"+varName);
                        thirdLvlName = null;
                    } else {
                        siteDomain = thirdLvlName+"."+settingsGeneral.getRoot_domain();
                        siteUrl = siteDomain;
                    }



                    rentSiteId = addNewWaitingRecordForOnlineStore(timestamp, user_ip, masterId, companyId, storeId, myId, siteDomain, thirdLvlName, isVar, parentVarSiteId, position, varName, siteUrl);



                    //2. sending e-mail to the employee who responsible for online stores
                    if(!settingsGeneral.getStores_alert_email().equals("")){
                        String subj = "There is store order, but no free stores";
                        String body =   "Record ID = " + rentSiteId + "\n\n "+
                                        "Master ID = " + masterId + "\n\n "+
                                        "Company ID = " + companyId + "\n\n "+
                                        "Store ID = " + storeId + "\n\n "+
                                        "Orderer ID = " + myId + "\n\n "+
                                        "Created at = " + timestamp + "\n\n "+
                                        "Agreement ID = " + agreementId;
                        mailRepository.sentMessage(settingsGeneral.getStores_alert_email(),subj,body,"en");
                    }
                    //get message for user
                    Map<String, String> map = cu.translateHTMLmessages(myId, new String[]{"'online_store_no_free_but_ordered'"});

                    //info to return:

                    storeOrderingResult.setMessage(map.get("online_store_no_free_but_ordered"));
                    storeOrderingResult.setResult(-330);

                }

                // getting store data
                StoreForOrderingJSON orderedStoreFullData = getStoreForOrderingData(rentSiteId);
                orderedStoreReturnData.setSite_url(orderedStoreFullData.getSite_url());
                storeOrderingResult.setStoreInfo(orderedStoreReturnData);
                return storeOrderingResult;

            } catch (LanguageVersionOfSiteIsNotUniqueException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("-370: The same site cannot have the same language versions", e);
                e.printStackTrace();
                storeOrderingResult.setResult(-370);
                return storeOrderingResult;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method getMyRentSite.", e);
                e.printStackTrace();

                storeOrderingResult.setResult(null);
                return storeOrderingResult;
            }
        } else {
            storeOrderingResult.setResult(-1);
            return storeOrderingResult;
        }
    }


    private void alarmLowFreeStoresToRent(String alarmEmail, int alarmQtt) throws Exception {
        String stringQuery =
                "select count(*) from _saas_stores_for_ordering where ready_to_distribute=true and distributed=false";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            int storesQtt = ((BigInteger)query.getSingleResult()).intValue();
            if(storesQtt<alarmQtt){
                String subj = "Low level of free stores!";
                String body = "Only "+storesQtt+" stores left!";
                mailRepository.sentMessage(alarmEmail,subj,body,"en");
            }
        } catch (Exception e) {
            logger.error("Exception in method alarmLowFreeStoresToRent. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private void storesToRentOrderedSuccessfully(String succEmail, String ordererEmail, String storeShortData) throws Exception {
        try {
                String subj = "Store distributed successfully!";
                String body = "Customer email: "+ordererEmail+ "\n\n" +
                        "Store short data: "+ "\n\n"+
                        storeShortData;
                mailRepository.sentMessage(succEmail,subj,body,"en");
        } catch (Exception e) {
            logger.error("Exception in method storesToRentOrderedSuccessfully.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private void distributeOnlineStoreToUser(Long rentedStoreRecordId,
                                             String timestamp,
                                             String ordererIp,
                                             Long companyId,
                                             Long storeId,
                                             Long masterId,
                                             Long ordererId,
                                             String thirdLvlName,
                                             Boolean isVar,
                                             Long parentVarSiteId,
                                             String position,
                                             String varName
//            ,                                String siteUrl
    ) throws Exception {

        String stringQuery = " update _saas_stores_for_ordering set "+
                " distributed = true, " +
                " date_time_ordered =     to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                " date_time_distributed = to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                " master_id = " + masterId + ", " +
                " company_id = " + companyId + ", " +
                " store_id = "  + storeId + ", " +
                " orderer_ip = '"  + ordererIp + "', " +
                " orderer_id = "  + ordererId + ", " +
                " third_lvl_user_domain = :third_lvl_user_domain," +
                " is_existed_store_variation = " + isVar + ", " +
                " parent_variation_store_id = " + parentVarSiteId + ", " +
                " variation_name_position = :position," +
                " variation_name = :variation_name" +
//                " site_url = :site_url" +
                " where id = " + rentedStoreRecordId +
                " and distributed = false"; // just for unlikely case - when in the one moment two users gets the same id of store
        try {

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("third_lvl_user_domain",thirdLvlName);
            query.setParameter("position",position);
            query.setParameter("variation_name",varName);
//            query.setParameter("site_url",siteUrl);

            query.executeUpdate();
            // check that onlone store was realy distributed (that unlikely case is not happened)
            stringQuery = "select count(*) from _saas_stores_for_ordering where id = "+rentedStoreRecordId+" and store_id = "+storeId+" and distributed = true";
            query = entityManager.createNativeQuery(stringQuery);
            if(((BigInteger)query.getSingleResult()).intValue()!=1) throw new Exception();

        } catch (Exception e) {
            //ConstraintViolationException напрямую не отлавливается, она обернута в родительские классы, и нужно определить, есть ли она в Exception
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            Throwable t = e.getCause();
            while ((t != null) && !(t instanceof ConstraintViolationException)) {
                t = t.getCause();
            }
            if (t != null) {
                String message = ((ConstraintViolationException) t).getSQLException().getMessage();
                if(message.contains("_saas_stores_for_ordering_var_uq")) { // store can't have the same languages(e.g. mystore.me/fr is already existed, & mystore.me/fr creation -> error)
                    logger.error("ConstraintViolationException (_saas_stores_for_ordering_var_uq) in method StoreRepositoryJPA/distributeOnlineStoreToUser.", e);
                    throw new LanguageVersionOfSiteIsNotUniqueException();
                } else {
                    logger.error("Exception in method distributeOnlineStoreToUser. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    throw new Exception();
                }
            } else {
                logger.error("Exception in method distributeOnlineStoreToUser. SQL query:" + stringQuery, e);
                e.printStackTrace();
                throw new Exception();
            }
        }
    }

    private void setIpAndSecretKeyToStoreConnection(String serverIp, String secretKey, Long masterId, Long storeId) throws Exception {
        String stringQuery =
                " update stores set" +
                " crm_secret_key=:crm_secret_key," +
                " is_let_sync=true," +
                " store_ip=:store_ip" +
                " where master_id="+masterId+
                " and id="+storeId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("store_ip",serverIp);
            query.setParameter("crm_secret_key",secretKey);
            query.executeUpdate();
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method setIpAndSecretKeyToStoreConnection. SQL: "+stringQuery, e);
            throw new Exception();
        }
    }

    @Transactional
    public Integer deleteRentStore(Long recordId, Long storeId){
        if(     (securityRepositoryJPA.userHasPermissions_OR(54L,"678") && securityRepositoryJPA.isItAllMyMastersDocuments("stores",storeId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(54L,"679") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("stores",storeId.toString()))) {
            Long masterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            Long myId = userRepository.getUserId();
            String langCode = userRepositoryJPA.getUserSuffix(masterId);
            try {
                stringQuery = " update _saas_stores_for_ordering set " +
                        " date_time_query_to_delete = now(), " +
                        " deleter_id = " + myId + ", " +
                        " is_queried_to_delete = true " +
                        " where " +
                        " master_id = " + masterId +
                        " and id = " + recordId +
                        " and store_id = " + storeId +
                        " and is_queried_to_delete = false " +
                        " and is_deleted = false " +
                        " and deleter_id is null ";

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                SettingsGeneralJSON settingsGeneral = cu.getSettingsGeneral();
                String masterEmail = (String)cu.getFieldValueFromTableById("users","email", masterId, masterId);
                String whoRequestDelete=(String)cu.getFieldValueFromTableById("users","name", masterId, myId);
                String emailRequestDelete=(String)cu.getFieldValueFromTableById("users","email", masterId, myId);

                StoreForOrderingJSON storeForOrderingData = getStoreForOrderingData(recordId);

                // sending email to "Online stores support team of CRM"
                String subj = "Online store deletion request received";
                String body =
                        "Master customer email: "+masterEmail+ "<br><br>" +
                        "Date and time query for delete: "  + storeForOrderingData.getDate_time_query_to_delete() + "<br>" +
                        "Who requested removal: "  + whoRequestDelete + "<br>" +
                        "Email of the person who requested deletion: "  + emailRequestDelete + "<br><br>" +
                        "Store data: "+ "<br><br>"+
                        "Online store connection Id: " + storeId + "<br>" +
                        "Rent store record Id: " + recordId + "<br>" +
                        "Site domain: "                     + storeForOrderingData.getSite_domain() + "<br>" +
                        "Site root: "                       + storeForOrderingData.getSite_root() + "<br>" +
                        "Site server IP: "                  + storeForOrderingData.getWp_server_ip() + "<br>" +
                        "FTP user: "                        + storeForOrderingData.getFtp_user() + "<br>" +
                        "MySQL DB name: "                     + storeForOrderingData.getDb_name() + "<br>" +
                        "Site domain: "                     + storeForOrderingData.getClient_no() + "<br>";
                mailRepository.sentMessage(settingsGeneral.getStores_alert_email(), subj,body,"en");


                // sending email to master-account owner for confirmation of delete online store
                Map<String, String> map_h = cu.translateHTMLmessages(myId, new String[]{"'delete_online_store_request'"});
                Map<String, String> map =   cu.translateForMe(new String[]{"'site_data'","'site_address'","'site_name'","'who_requested_removal'","'confirmation_email'","'os_req_rcvd'"});

                subj =  map.get("os_req_rcvd"); // Subj: Received a request to delete a site with an online store
                body =  map_h.get("delete_online_store_request")+ "<br><br>" +
                        map.get("confirmation_email")+ ": "+ settingsGeneral.getStores_alert_email() +"<br><br>"+
                        map.get("who_requested_removal")+ ": "+ whoRequestDelete +"<br><br>"+
//                        map.get("site_data")+ "\n"+
                        map.get("site_address")+ ": "+ storeForOrderingData.getSite_domain() +"<br><br>"+
                        map.get("site_name")+ ": "+ cu.getFieldValueFromTableById("stores","name", masterId, storeId) +"<br><br>";

                mailRepository.sentMessage(masterEmail,subj,body,langCode);

                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method deleteRentStore.", e);
                return null;
            }
        }else return -1;
    }

    public List<StoreForOrderingShortInfoJSON> getRentStoresShortInfo(Long storeId){
        try{
            Long masterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            stringQuery = "select " +

                "           p.id as id," +
                "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                "           p.ready_to_distribute as ready_to_distribute," +
                "           p.distributed as distributed," +
                "           p.is_deleted as is_deleted," +
                "           p.site_domain as site_domain," +
                "           p.record_creator_name as record_creator_name," + // name of employee who created this record
                "           to_char(p.date_time_ordered at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_ordered, " +
                "           to_char(p.date_time_distributed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_distributed, " +
                "           to_char(p.date_time_deleted at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_deleted, " +
                "           uo.name as orderer," +          // who ordered (who is clicked on the button "Order store")
                "           coalesce(ud.name,'') as deleter_id," +
                "           p.panel_domain as panel_domain," +
                "           p.is_queried_to_delete as is_queried_to_delete," +
                "           to_char(p.date_time_query_to_delete at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_query_to_delete," +
                "           p.site_url as site_url" +
                "           from _saas_stores_for_ordering p" +
                "           inner join users uo on p.orderer_id = uo.id"+
                "           left outer join users ud on p.deleter_id = ud.id"+
                "           where p.store_id = " + storeId +
                "           and p.master_id = " + masterId +
                "           order by p.date_time_distributed desc limit 1";

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<StoreForOrderingShortInfoJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoreForOrderingShortInfoJSON doc = new StoreForOrderingShortInfoJSON();

                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setDate_time_created((String)                   obj[1]);
                doc.setReady_to_distribute((Boolean)                obj[2]);
                doc.setDistributed((Boolean)                        obj[3]);
                doc.setIs_deleted((Boolean)                         obj[4]);
                doc.setSite_domain((String)                         obj[5]);
                doc.setRecord_creator_name((String)                 obj[6]);
                doc.setDate_time_ordered((String)                   obj[7]);
                doc.setDate_time_distributed((String)               obj[8]);
                doc.setDate_time_deleted((String)                   obj[9]);
                doc.setOrderer((String)                             obj[10]);
                doc.setDeleter((String)                             obj[11]);
                doc.setPanel_domain((String)                        obj[12]);
                doc.setIs_queried_to_delete((Boolean)               obj[13]);
                doc.setDate_time_query_to_delete((String)           obj[14]);
                doc.setSite_url((String)                            obj[15]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getRentStoresShortInfo.", e);
            return null;
        }
    }


    private StoreForOrderingJSON getStoreForOrderingData(Long siteId) throws Exception {
        UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
        String myTimeZone = userSettings.getTime_zone();
        String dateFormat = userSettings.getDateFormat();
        String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'

        String stringQuery = "select " +

                "           id as id," +
                "           to_char(date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                "           ready_to_distribute as ready_to_distribute," +
                "           distributed as distributed," +
                "           is_deleted as is_deleted," +
                "           client_no as client_no," +
                "           client_name as client_name," +
                "           client_login as client_login," +
                "           pgp_sym_decrypt(\"client_password\",'"+stores_secret+"') as client_password,"+
                "           site_domain as site_domain," +
                "           site_root as site_root," +
                "           ftp_user as ftp_user," +
                "           pgp_sym_decrypt(\"ftp_password\",'"+stores_secret+"') as ftp_password,"+
                "           db_user as db_user," +
                "           pgp_sym_decrypt(\"db_password\",'"+stores_secret+"') as db_password,"+
                "           db_name as db_name," +
                "           wp_login as wp_login," +
                "           pgp_sym_decrypt(\"wp_password\",'"+stores_secret+"') as wp_password,"+
                "           wp_server_ip as wp_server_ip," +
                "           dokio_secret_key as dokio_secret_key," +
                "           record_creator_name as record_creator_name," + // name of employee who created this record
                "           to_char(date_time_ordered at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_ordered, " +
                "           to_char(date_time_distributed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_distributed, " +
                "           to_char(date_time_deleted at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_deleted, " +
                "           master_id  as master_id," +
                "           company_id as company_id," +
                "           store_id   as store_id," +           // online store connection
                "           orderer_id as orderer_id," +          // who ordered (who is clicked on the button "Order store")
                "           deleter_id as deleter_id," +
                "           panel_domain as panel_domain," +
                "           is_queried_to_delete as is_queried_to_delete," +
                "           to_char(date_time_query_to_delete at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_query_to_delete, " +
                "           orderer_ip as orderer_ip," +
                "           is_existed_store_variation, " +
                "           parent_variation_store_id, " +
                "           variation_name_position," +
                "           variation_name," +
                "           site_url" +
                "           from _saas_stores_for_ordering " +
                "           where  id= " + siteId;

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            StoreForOrderingJSON doc = new StoreForOrderingJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setDate_time_created((String)                   obj[1]);
                doc.setReady_to_distribute((Boolean)                obj[2]);
                doc.setDistributed((Boolean)                        obj[3]);
                doc.setIs_deleted((Boolean)                         obj[4]);
                doc.setClient_no((String)                           obj[5]);
                doc.setClient_name((String)                         obj[6]);
                doc.setClient_login((String)                        obj[7]);
                doc.setClient_password((String)                     obj[8]);
                doc.setSite_domain((String)                         obj[9]);
                doc.setSite_root((String)                           obj[10]);
                doc.setFtp_user((String)                            obj[11]);
                doc.setFtp_password((String)                        obj[12]);
                doc.setDb_user((String)                             obj[13]);
                doc.setDb_password((String)                         obj[14]);
                doc.setDb_name((String)                             obj[15]);
                doc.setWp_login((String)                            obj[16]);
                doc.setWp_password((String)                         obj[17]);
                doc.setWp_server_ip((String)                        obj[18]);
                doc.setDokio_secret_key((String)                    obj[19]);
                doc.setRecord_creator_name((String)                 obj[20]);
                doc.setDate_time_ordered((String)                   obj[21]);
                doc.setDate_time_distributed((String)               obj[22]);
                doc.setDate_time_deleted((String)                   obj[23]);
                doc.setMaster_id(Long.parseLong(                    obj[24].toString()));
                doc.setCompany_id(Long.parseLong(                   obj[25].toString()));
                doc.setStore_id(Long.parseLong(                     obj[26].toString()));
                doc.setOrderer_id(Long.parseLong(                   obj[27].toString()));
                doc.setDeleter_id(obj[28] != null ? Long.parseLong( obj[28].toString()) : null);
                doc.setPanel_domain((String)                        obj[29]);
                doc.setIs_queried_to_delete((Boolean)               obj[30]);
                doc.setDate_time_query_to_delete((String)           obj[31]);
                doc.setOrderer_ip((String)                          obj[32]);
                doc.setVar((Boolean)                                obj[33]);
                doc.setParentVarSiteId(obj[34]!=null?Long.parseLong(obj[34].toString()) : null);
                doc.setPosition((String)                            obj[35]);
                doc.setVarName((String)                             obj[36]);
                doc.setSite_url((String)                            obj[37]);
            }
            return doc;
        } catch (Exception e) {
            logger.error("Exception in method getStoreForOrderingData. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }

    }

    private Long addNewWaitingRecordForOnlineStore(String timestamp,
                                                   String ipAddress,
                                                   Long masterId,
                                                   Long companyId,
                                                   Long storeId,
                                                   Long ordererId,
                                                   String siteDomain,
                                                   String thirdLvlName,
                                                   Boolean isVar,
                                                   Long parentVarSiteId,
                                                   String position,
                                                   String varName,
                                                   String siteUrl) throws Exception{
        String stringQuery;

        stringQuery= " insert into _saas_stores_for_ordering ("+
                        " date_time_created,"+
                        " date_time_ordered,"+
                        " master_id,"+
                        " company_id,"+
                        " store_id,"+
                        " orderer_id,"+
                        " orderer_ip," +
                        " ready_to_distribute,"+
                        " distributed,"+
                        " is_queried_to_delete,"+
                        " is_deleted," +
                        " is_existed_store_variation," +
                        " parent_variation_store_id," +
                        " variation_name_position," +
                        " variation_name," +
                        " site_domain," +
                        " third_lvl_user_domain," +
                        " site_url" +
                    ") values ("+
                        " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                        " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                        masterId+", " +
                        "(select id from companies where master_id="+masterId+" and id="+companyId+")," +
                        storeId+", " +
                        ordererId+", " +
                        "'"+ipAddress+"', " +
                        " false," +
                        " false," +
                        " false," +
                        " false," +
                        isVar+", " +
                        parentVarSiteId+", " +
                        ":position," +
                        ":varName," +
                        ":site_domain,"+
                        ":third_lvl_user_domain," +
                        ":site_url"+
                    ");";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("position", position);
            query.setParameter("varName", varName);
            query.setParameter("site_domain", siteDomain);
            query.setParameter("third_lvl_user_domain", thirdLvlName);
            query.setParameter("site_url", siteUrl);
            query.executeUpdate();
            stringQuery="select id from _saas_stores_for_ordering where master_id = "+masterId+" and store_id="+storeId+" and date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS'))";
            query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (Exception e) {
            //ConstraintViolationException напрямую не отлавливается, она обернута в родительские классы, и нужно определить, есть ли она в Exception
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            Throwable t = e.getCause();
            while ((t != null) && !(t instanceof ConstraintViolationException)) {
                t = t.getCause();
            }
            if (t != null) {
                String message = ((ConstraintViolationException) t).getSQLException().getMessage();
                if(message.contains("_saas_stores_for_ordering_var_uq")) { // store can't have the same languages(e.g. mystore.me/fr is already existed, & mystore.me/fr creation -> error)
                    logger.error("ConstraintViolationException (_saas_stores_for_ordering_var_uq) in method StoreRepositoryJPA/distributeOnlineStoreToUser.", e);
                    throw new LanguageVersionOfSiteIsNotUniqueException();
                } else {
                    logger.error("Exception in method addNewWaitingRecordForOnlineStore. SQL query:" + stringQuery, e);
                    throw new Exception();
                }
            } else {
                logger.error("Exception in method addNewWaitingRecordForOnlineStore. SQL query:" + stringQuery, e);
                throw new Exception();
            }
        }
    }

    private int getQttOfStoreOrdersOnPeriodFromIp(int hours, String ipAddress) throws Exception {
        String stringQuery =
                "select count(*) from _saas_stores_for_ordering where " +
                        " orderer_ip = '"+ipAddress+"'" +
                        " and date_time_ordered >= NOW() - INTERVAL '"+hours+" HOURS'";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).intValue();
        } catch (Exception e) {
            logger.error("Exception in method getQttOfStoreOrdersOnPeriodFromIp. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    private int getQttOfStoreOrdersOnPeriodFromAccount(int hours, Long accId) throws Exception {
        String stringQuery =
                "select count(*) from _saas_stores_for_ordering where " +
                        " master_id = "+accId+
                        " and date_time_ordered >= NOW() - INTERVAL '"+hours+" HOURS'";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).intValue();
        } catch (Exception e) {
            logger.error("Exception in method getQttOfStoreOrdersOnPeriodFromAccount. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    public List<IdAndName> getExistedRentSitesList(Long companyId){
        Long masterId = userRepositoryJPA.getMyMasterId();
        String stringQuery;
        stringQuery = "select " +
                "           p.id as id," +
                "           p.site_domain as site_domain" +
                "           from _saas_stores_for_ordering p" +
                "           where p.master_id = " + masterId +
                "           and p.company_id = " + companyId +
                "           and p.distributed = true" +
                "           and p.is_queried_to_delete = false" +
                "           and p.is_deleted = false" +
                "           and coalesce(p.is_existed_store_variation, false) = false " +
                "           order by p.date_time_distributed ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<IdAndName> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                IdAndName doc = new IdAndName();

                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setName((String)                                obj[1]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getExistedRentSitesList. SQL: "+stringQuery, e);
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertStoreFast(StoresForm request, Long mId, Long uId) {
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
                            " is_deleted," +                     // deleted
                            " is_let_sync" +
                            ") values ("+
                            mId + ", "+//мастер-аккаунт
                            uId + ", "+ //создатель
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
                            "false" + ", " +
                            request.getIs_let_sync() +
                            ")";// уникальный идентификатор документа
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("lang_code",request.getLang_code());
                query.setParameter("crm_secret_key",request.getCrm_secret_key());
                query.setParameter("store_if_customer_not_found",request.getStore_if_customer_not_found());
                query.setParameter("store_ip", request.getStore_ip());
                query.executeUpdate();
                stringQuery="select id from stores where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+uId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                Long createdDoc = Long.valueOf(query2.getSingleResult().toString());
                // saving store departments
                request.setId(createdDoc);
                insertStoreDepartmentsFast(request, mId);
                return createdDoc;
            } catch (Exception e) {
                logger.error("Exception in method insertStoreFast on creating new user account. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
    }


    private void insertStoreDepartmentsFast(StoresForm request, Long masterId) throws Exception {
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
        }catch (Exception e) {
            logger.error("Error of insertStoreDepartmentsFast.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional
    public void saveStoreSyncStatus(Long storeId, String job, Long masterId, String status) throws Exception {
        String stringQuery;

        stringQuery =   " insert into stores_sync_statuses (" +
                " master_id," +
                " store_id," +
                " job," +
                " date_time_start" +
                ") values (" +
                masterId + ", " +
                storeId + ", " +
                "'"+ job +"', " +
                (status.equals("begin")?"now()":"null") +
                ") ON CONFLICT ON CONSTRAINT stores_sync_statuses_uq " +// "upsert"
                " DO update set " +
                (status.equals("begin")?" date_time_start = now(),":"") +
                " date_time_end = "+(status.equals("end")?"now()":"null");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method StoreRepository/saveStoreSyncStatus. SQL query:"+stringQuery, e);
        }
    }
    @Transactional
    public void setStoreSyncStatusAsFinished(Long storeId) throws Exception {
        String stringQuery;
        stringQuery =   " update stores_sync_statuses set " +
                        " date_time_end = now() " +
                        " where " +
                        " store_id=" +storeId+ " and date_time_end is null";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method StoreRepository/setStoreSyncStatusAsFinished. SQL query:"+stringQuery, e);
        }
    }
}