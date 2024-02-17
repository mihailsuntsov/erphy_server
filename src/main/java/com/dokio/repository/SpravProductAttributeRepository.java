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

import com.dokio.message.request.Sprav.ProductAttributeForm;
import com.dokio.message.request.Sprav.ProductAttributeTermForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.ProductAttributeJSON;
import com.dokio.message.response.Sprav.ProductAttributeTermJSON;
import com.dokio.message.response.Sprav.ProductAttributesListJSON;
import com.dokio.message.response.additional.StoreTranslationAttributeJSON;
import com.dokio.message.response.additional.StoreTranslationTermJSON;
import com.dokio.model.Companies;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
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
public class SpravProductAttributeRepository {

    @Autowired
    private EntityManagerFactory emf;
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
    ProductsRepositoryJPA productsRepository;
    @Autowired
    CommonUtilites commonUtilites;

    private Logger logger = Logger.getLogger("SpravProductAttribute");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name_short","company","name","creator","date_time_created_sort","code_lit","date_created","slug", "type", "is_default","description")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


    @SuppressWarnings("Duplicates")
    public List<ProductAttributeJSON> getProductAttributeTable (int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(53L, "667,668"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Invalid query parameters");

            stringQuery =       "select " +
                    "           p.id as id," +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.slug as slug, " +
                    "           p.type as type, " +
                    "           p.order_by as order_by, " +
                    "           p.has_archives as has_archives, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from product_attributes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(53L, "667")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name) like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.slug)  like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.description)  like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.type)   like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.order_by)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            try {
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<ProductAttributeJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    ProductAttributeJSON doc = new ProductAttributeJSON();
                    doc.setId(Long.parseLong(           obj[0].toString()));
                    doc.setCreator((String)             obj[1]);
                    doc.setChanger((String)             obj[2]);
                    doc.setCompany((String)             obj[3]);
                    doc.setDate_time_created((String)   obj[4]);
                    doc.setDate_time_changed((String)   obj[5]);
                    doc.setName((String)                obj[6]);
                    doc.setSlug((String)                obj[7]);
                    doc.setType((String)                obj[8]);
                    doc.setOrder_by((String)            obj[9]);
                    doc.setHas_archives((Boolean)       obj[10]);
                    doc.setDescription((String)         obj[11]);
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductAttributeTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public Integer getProductAttributeSize(String searchString, Long companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(53L, "667,668")){//(см. файл Permissions Id)
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery =       "select " +
                    "           p.id as id " +
                    "           from product_attributes p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;
            if (!securityRepositoryJPA.userHasPermissions_OR(53L, "667")){ //Если нет прав на "Просмотр по всем предприятиям" - остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name) like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.slug)  like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.description)  like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.type)   like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.order_by)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                return query.getResultList().size();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductAttributeSize. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public ProductAttributeJSON getProductAttributeValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(53L, "667,668"))// (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long masterId = userRepositoryJPA.getMyMasterId();

            stringQuery = "     select  p.id as id, " +
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
                    "           p.slug as slug, " +
                    "           p.type as type, " +
                    "           p.order_by as order_by, " +
                    "           p.has_archives as has_archives, " +
                    "           p.description as description " +
                    "           from product_attributes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + masterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(53L, "667")){ //Если нет прав на "Просмотр документов по всем предприятиям"
                //остается только на своё предприятие (668)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            ProductAttributeJSON doc = new ProductAttributeJSON();
            for (Object[] obj : queryList) {
                doc.setId(Long.parseLong(           obj[0].toString()));
                doc.setMaster((String)              obj[1]);
                doc.setCreator((String)             obj[2]);
                doc.setChanger((String)             obj[3]);
                doc.setMaster_id(Long.parseLong(    obj[4].toString()));
                doc.setCreator_id(Long.parseLong(   obj[5].toString()));
                doc.setChanger_id(                  obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(   obj[7].toString()));
                doc.setCompany((String)             obj[8]);
                doc.setDate_time_created((String)   obj[9]);
                doc.setDate_time_changed((String)   obj[10]);
                doc.setName((String)                obj[11]);
                doc.setSlug((String)                obj[12]);
                doc.setType((String)                obj[13]);
                doc.setOrder_by((String)            obj[14]);
                doc.setHas_archives((Boolean)       obj[15]);
                doc.setDescription((String)         obj[16]);
            }
            doc.setStoresIds(getAttributeStoresIds(id, masterId));
            doc.setStoreAttributeTranslations(getStoreAttributeTranslationsList(doc.getId(), masterId));
            return doc;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updateProductAttribute(ProductAttributeForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(53L,"669") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(53L,"670") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update product_attributes set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " name = :name, " +
                    " type = :type, " +
                    " slug = :slug, " +
                    " order_by = :order_by, " +
                    " description = :description," +
                    " has_archives = " + request.getHas_archives() +
                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;
            try
            {
                commonUtilites.idBelongsMyMaster("product_attributes", request.getId(), myMasterId); // check because even if here won't update, after this id will use in another transactions
                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("type",request.getType());
                query.setParameter("description",request.getDescription());
                query.setParameter("slug",request.getSlug());
                query.setParameter("order_by",request.getOrder_by());
                query.executeUpdate();

                saveProductAttributeTermsOrder(request.getTerms(),myMasterId);

                //save the list of translations of this attribute
                if (!Objects.isNull(request.getStoreAttributeTranslations()) && request.getStoreAttributeTranslations().size() > 0) {
                    for (StoreTranslationAttributeJSON row : request.getStoreAttributeTranslations()) {
                        saveStoreAttributeTranslations(row, myMasterId, request.getCompany_id(), request.getId());
                    }
                }

                //Saving the list of online stores that attribute belongs to
                List<Long> oldAttributeStoresIds =  getAttributeStoresIds(request.getId(), myMasterId);// getting current stores ids
                List<Long> newAttributeStoresIds =  request.getStoresIds();// getting new stores ids
                deleteNonSelectedAttributeStores(request.getStoresIds(), request.getId(), myMasterId);
                if (!Objects.isNull(request.getStoresIds()) && request.getStoresIds().size() > 0) {
                    for (Long storeId : request.getStoresIds()) {
                        commonUtilites.idBelongsMyMaster("stores", storeId, myMasterId);
                        saveAttributeStore(request.getId(), storeId, myMasterId, request.getCompany_id());
                    }
                }

                //Saving the list of stores IDs for each term of this attribute
                deleteNonSelectedAttributeTermsStores(request.getStoresIds(), request.getId(), myMasterId);
                List<ProductAttributeTermJSON> attributeTermsList = getProductAttributeTermsList(request.getId());
                if (!Objects.isNull(attributeTermsList) && attributeTermsList.size() > 0 && !Objects.isNull(request.getStoresIds()) && request.getStoresIds().size() > 0) {
                    for (Long storeId : request.getStoresIds()) {
                        for (ProductAttributeTermJSON termRow : attributeTermsList) {
                            commonUtilites.idBelongsMyMaster("stores", storeId, myMasterId);
                            commonUtilites.idBelongsMyMaster("product_attribute_terms", termRow.getId(), myMasterId);
                            saveTermStore(termRow.getId(), storeId, myMasterId, request.getCompany_id());
                        }
                    }
                }
                //Set this attribute as need to be synchronized with online store(s)
                markAttributesAsNeedToSyncWoo(new HashSet<>(Arrays.asList(request.getId())),myMasterId);



                List<Long> storesToMarkTheirProdsForResync = new ArrayList<>();
                for(Long newSid : newAttributeStoresIds){
                    if(!Objects.isNull(newSid) && !oldAttributeStoresIds.contains(newSid))
                        storesToMarkTheirProdsForResync.add(newSid);
                }
                if(storesToMarkTheirProdsForResync.size()>0)
                    productsRepository.markProductsOfStoresAndAttributesAsNeedToSyncWoo(storesToMarkTheirProdsForResync, new ArrayList<>(Arrays.asList(request.getId())), myMasterId);

                return 1;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateProductAttribute. SQL query:"+stringQuery, e);
                e.printStackTrace();
                Throwable t = e.getCause();
                while ((t != null) && !(t instanceof ConstraintViolationException)) {
                    t = t.getCause();
                }
                if (t != null) {
                    String message = ((ConstraintViolationException) t).getSQLException().getMessage();
//                    if(message.contains("product_attributes_name_uq")){
//                        logger.error("ConstraintViolationException (product_attributes) in method updateProductAttribute. (product_attributes_name_uq)", e);
//                        return -211; //product_attributes_name_uq - now this constraint is deleted because WooCommerce allows to have non-unical attribute names
//                    } else
                    if(message.contains("product_attributes_slug_uq")){
                        logger.error("ConstraintViolationException (product_attributes) in method updateProductAttribute. (product_attributes_slug_uq)", e);
                        return -213;
//                    } else if (message.contains("store_translate_attributes_name_uq")){
//                        logger.error("ConstraintViolationException (store_translate_attributes) in method saveStoreAttributeTranslations->updateProductAttribute. (store_translate_attributes_name_uq)", e);
//                        return -212; //store_translate_attributes_name_uq
                    } else if (message.contains("store_translate_attributes_slug_uq")){
                        logger.error("ConstraintViolationException (store_translate_attributes) in method saveStoreAttributeTranslations->updateProductAttribute. (store_translate_attributes_slug_uq)", e);
                        return -214; //store_translate_attributes_slug_uq
                    } else {
                        logger.error("Exception in method updateProductAttribute. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    logger.error("Exception in method updateProductAttribute. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1; //недостаточно прав
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем null в случае ошибки
    // Возвращаем -1 в случае отсутствия прав
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertProductAttribute(ProductAttributeForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(53L, "663")) ||
                //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, и
                (securityRepositoryJPA.userHasPermissions_OR(53L, "664") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery = "insert into product_attributes (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " date_time_created," + //дата и время создания
                    " name," +//
                    " type," +//
                    " slug," +//
                    " description," +
                    " order_by," +//
                    " has_archives," +
                    " is_deleted" +
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    " :name, " +
                    " :type, " +
                    " :slug, " +
                    " :description," +
                    " :order_by, " +
                    request.getHas_archives() + ", " +
                    " false)";
            try{
                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("type",request.getType());
                query.setParameter("description",request.getDescription());
                query.setParameter("slug",request.getSlug());
                query.setParameter("order_by",request.getOrder_by());
                query.executeUpdate();
                stringQuery="select id from product_attributes where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);

                Long newAttributeId = Long.valueOf(query2.getSingleResult().toString());

                //save the list of translations of this attribute
                if (!Objects.isNull(request.getStoreAttributeTranslations()) && request.getStoreAttributeTranslations().size() > 0) {
                    for (StoreTranslationAttributeJSON row : request.getStoreAttributeTranslations()) {
                        saveStoreAttributeTranslations(row, myMasterId, request.getCompany_id(), newAttributeId);
                    }
                }
//                //Saving the list of online stores that attribute belongs to
                if (!Objects.isNull(request.getStoresIds()) && request.getStoresIds().size() > 0) {
                    for (Long storeId : request.getStoresIds()) {
                        commonUtilites.idBelongsMyMaster("stores", storeId, myMasterId);
                        saveAttributeStore(newAttributeId, storeId, myMasterId, request.getCompany_id());
                    }
                }
                return newAttributeId;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertProductAttribute on inserting into product_attributes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                Throwable t = e.getCause();
                while ((t != null) && !(t instanceof ConstraintViolationException)) {
                    t = t.getCause();
                }
                if (t != null) {
                    String message = ((ConstraintViolationException) t).getSQLException().getMessage();
//                    if(message.contains("product_attributes_name_uq")){
//                        logger.error("ConstraintViolationException (product_attributes) in method insertProductAttribute. (product_attributes_name_uq)", e);
//                        return -211L; //product_attributes_name_uq - now this constraint is deleted because WooCommerce allows to have non-unical attribute names
//                    } else
                    if (message.contains("product_attributes_slug_uq")){
                        logger.error("ConstraintViolationException (product_attributes) in method insertProductAttribute. (product_attributes_slug_uq)", e);
                        return -213L;//product_attributes_slug_uq
//                    } else if  (message.contains("store_translate_attributes_name_uq")){
//                        logger.error("ConstraintViolationException (store_translate_attributes) in method saveStoreAttributeTranslations->insertProductAttribute. (store_translate_attributes_name_uq)", e);
//                        return -212L; //store_translate_attributes_name_uq
                    } else if (message.contains("store_translate_attributes_slug_uq")){
                        logger.error("ConstraintViolationException (store_translate_attributes) in method saveStoreAttributeTranslations->insertProductAttribute. (store_translate_attributes_slug_uq)", e);
                        return -214L; //store_translate_attributes_slug_uq
                    } else {
                        logger.error("Exception in method insertProductAttribute. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    logger.error("Exception in method insertProductAttribute. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else {
            return -1L;
        }
    }
    private void saveAttributeStore(Long attribute_id, Long store_id, Long master_id, Long company_id) throws Exception {
        String stringQuery = "insert into stores_attributes (" +
                "   master_id," +
                "   company_id," +
                "   store_id," +
                "   attribute_id " +
                "   ) values (" +
                master_id+", "+
                company_id+", "+
                "(select id from stores where id="+store_id+" and master_id="+master_id+"), "+// +", " +
                "(select id from product_attributes where id="+attribute_id+" and master_id="+master_id+") "+// чтобы не мочь изменить атрибут другого master_id, случайно или намеренно
                ") ON CONFLICT ON CONSTRAINT stores_attributes_uq " +// "upsert"
                " DO NOTHING";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveAttributeStore. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void deleteNonSelectedAttributeStores(List<Long> storesIds, Long attributeId, Long masterId) throws Exception {
        String stringQuery = "delete from stores_attributes " +
                " where master_id=" + masterId +
                " and attribute_id=" + attributeId +
                " and store_id not in " + ((!Objects.isNull(storesIds)&&storesIds.size()>0)?(commonUtilites.ListOfLongToString(storesIds,",","(",")")):"(0)");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteNonSelectedAttributeStores. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    private void deleteNonSelectedAttributeTermsStores(List<Long> storesIds, Long attributeId, Long masterId) throws Exception {
        String stringQuery = "delete from stores_terms " +
                " where master_id=" + masterId +
                " and term_id in " +
                "(" +
                "   select id from product_attribute_terms where attribute_id = " + attributeId +
                "   and store_id not in " + ((!Objects.isNull(storesIds)&&storesIds.size()>0)?(commonUtilites.ListOfLongToString(storesIds,",","(",")")):"(0)") +
                ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteNonSelectedTermStores. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    private List<StoreTranslationAttributeJSON> getStoreAttributeTranslationsList(Long attributeId, Long masterId){
        String stringQuery = "      select   p.lang_code as lang_code," +
                "           coalesce(p.name,'') as name, " +
                "           coalesce(p.slug,'') as slug " +
//                "           coalesce(p.description,'') as description " +
                "           from     store_translate_attributes p " +
                "           where    p.master_id=" + masterId +
                "           and      p.attribute_id =" + attributeId +
                "           order by p.lang_code";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<StoreTranslationAttributeJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoreTranslationAttributeJSON doc = new StoreTranslationAttributeJSON();
                doc.setLangCode((String)                                obj[0]);
                doc.setName((String)                                    obj[1]);
                doc.setSlug((String)                                    obj[2]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getStoreAttributeTranslationsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
    private void saveStoreAttributeTranslations(StoreTranslationAttributeJSON row, Long master_id, Long company_id, Long attribute_id) throws Exception {
        String stringQuery = "insert into store_translate_attributes (" +
                "   master_id," +
                "   company_id," +
                "   lang_code," +
                "   attribute_id, " +
                "   name, " +
                "   slug " +
                "   ) values (" +
                master_id+", "+
                company_id+", "+
                ":lang_code," +
                "(select id from product_attributes where id="+attribute_id+" and master_id="+master_id+"), "+// чтобы не мочь изменить атрибут другого master_id, случайно или намеренно
                ":name," +
                ":slug" +
                ") ON CONFLICT ON CONSTRAINT attribute_lang_uq " +// "upsert"
                " DO update set " +
                " name = :name, " +
                " slug = :slug";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name", row.getName());
            query.setParameter("slug", row.getSlug());
            query.setParameter("lang_code", row.getLangCode());
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveStoreAttributeTranslations. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteProductAttribute(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(53L, "665") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(53L, "666") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
//            stringQuery = "Update product_attributes p" +
//                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
//                    " date_time_changed = now(), " +//дату и время изменения
//                    " is_deleted=true " +
//                    " where p.master_id=" + myMasterId +
//                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";

            stringQuery = "delete from product_attributes p" +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductAttribute. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public Integer undeleteProductAttribute(String delNumbers) {
//        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
//        if ((securityRepositoryJPA.userHasPermissions_OR(53L, "665") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes", delNumbers)) ||
//                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
//                (securityRepositoryJPA.userHasPermissions_OR(53L, "666") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes", delNumbers))) {
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myId = userRepositoryJPA.getMyId();
//            String stringQuery;
//            stringQuery = "Update product_attributes p" +
//                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
//                    " date_time_changed = now(), " +//дату и время изменения
//                    " is_deleted=false " +
//                    " where p.master_id=" + myMasterId +
//                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
//            try
//            {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                query.executeUpdate();
//                return 1;
//            } catch (Exception e) {
//                logger.error("Exception in method undeleteProductAttribute. SQL query:"+stringQuery, e);
//                e.printStackTrace();
//                return null;
//            }
//        } else return -1;
//    }

    private List<Long> getAttributeStoresIds(Long attributeId, Long masterId){
        String stringQuery = "  select   csd.store_id as id" +
                "               from     stores_attributes csd " +
                "               where    csd.master_id=" + masterId +
                "               and      csd.attribute_id =" + attributeId;
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
            logger.error("Exception in method getAttributeStoresIds. SQL query:" + stringQuery, e);
            return null;
        }
    }

    // inserting base set of product attributes on register of new user
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertProductAttributeFast(Long mId, Long uId, Long cId, Long storeId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        List<Long> attrList = new ArrayList<>();
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{
                "'color'","'size'","'black'","'white'"});
        stringQuery = "insert into product_attributes ( master_id,creator_id,company_id,date_time_created,name,type,slug,order_by,has_archives,is_deleted) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("color")+"', 'select','color','menu_order',false, false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("size")+"', 'select','size','menu_order',false,false);"+
                      "insert into product_attribute_terms ( master_id,name,slug,menu_order,description,attribute_id) values "+
                "("+mId+",'"+map.get("black")+"', 'black',1,'',(select id from product_attributes where company_id="+cId+" and slug='color')),"+
                "("+mId+",'"+map.get("white")+"', 'white',2,'',(select id from product_attributes where company_id="+cId+" and slug='color')),"+
                "("+mId+",'S','s',1,'',(select id from product_attributes where company_id="+cId+" and slug='size')),"+
                "("+mId+",'M','m',2,'',(select id from product_attributes where company_id="+cId+" and slug='size')),"+
                "("+mId+",'L','l',3,'',(select id from product_attributes where company_id="+cId+" and slug='size'));";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery="select id from product_attributes where " +
                    " date_time_created = (to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and " +
                    " creator_id = " + mId + " and " +
                    " slug = 'color'";
            query = entityManager.createNativeQuery(stringQuery);
            saveAttributeStore(Long.valueOf(query.getSingleResult().toString()), storeId, mId, cId);
            stringQuery="select id from product_attributes where " +
                    " date_time_created = (to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and " +
                    " creator_id = " + mId + " and " +
                    " slug = 'size'";
            query = entityManager.createNativeQuery(stringQuery);
            saveAttributeStore(Long.valueOf(query.getSingleResult().toString()), storeId, mId, cId);
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertProductAttributeFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public List<ProductAttributesListJSON> getProductAttributesList (Long companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(53L, "667,668"))//(см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =       "select " +
                    "           p.id as id," +
                    "           p.name as name, " +
                    "           coalesce(p.description,'') as description " +
                    "           from product_attributes p " +
                    "           where  p.master_id=" + myMasterId + " and company_id = " + companyId + " order by name";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                List<Object[]> queryList = query.getResultList();
                List<ProductAttributesListJSON> returnList = new ArrayList<>();
                String shortDescriotion;
                for (Object[] obj : queryList) {
                    ProductAttributesListJSON doc = new ProductAttributesListJSON();
                    doc.setId(Long.parseLong(           obj[0].toString()));
                    shortDescriotion=(String)obj[2];
                    if(shortDescriotion.length()>30)
                        shortDescriotion=shortDescriotion.substring(0,28)+"...";
                    doc.setName(obj[1]+(shortDescriotion.length()>0?" ("+shortDescriotion+")":""));
                    doc.setTerms(getProductAttributeTermsList (Long.parseLong(obj[0].toString())));
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductAttributeTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }
//*****************************************************************************************************************************************************
//****************************************************   T  E  R  M  S   ******************************************************************************
//*****************************************************************************************************************************************************


    @SuppressWarnings("Duplicates")
    public List<ProductAttributeTermJSON> getProductAttributeTermsList (Long attrbuteId) {
            String stringQuery;
            Long masterId = userRepositoryJPA.getMyMasterId();
            stringQuery =       "select " +
                    "           p.id as id," +
                    "           p.name as name, " +
                    "           p.slug as slug, " +
                    "           p.description as description " +
                    "           from product_attribute_terms p " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           where  p.master_id=" + masterId +
                    "           and p.attribute_id =" + attrbuteId +
                    "           order by p.menu_order";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                List<ProductAttributeTermJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    ProductAttributeTermJSON doc = new ProductAttributeTermJSON();
                    doc.setId(Long.parseLong(           obj[0].toString()));
                    doc.setName((String)                obj[1]);
                    doc.setSlug((String)                obj[2]);
                    doc.setDescription((String)         obj[3]);
                    doc.setStoreTermTranslations(getStoreTermTranslationsList(doc.getId(), masterId));
                    returnList.add(doc);
                }

                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductAttributeTermsList. SQL query:" + stringQuery, e);
                return null;
            }
    }
    private List<StoreTranslationTermJSON> getStoreTermTranslationsList(Long termId, Long masterId){
        String stringQuery = "      select   p.lang_code as lang_code," +
                "           coalesce(p.name,'') as name, " +
                "           coalesce(p.slug,'') as slug, " +
                "           coalesce(p.description,'') as description " +
                "           from     store_translate_terms p " +
                "           where    p.master_id=" + masterId +
                "           and      p.term_id =" + termId +
                "           order by p.lang_code";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<StoreTranslationTermJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoreTranslationTermJSON doc = new StoreTranslationTermJSON();
                doc.setLangCode((String)                                obj[0]);
                doc.setName((String)                                    obj[1]);
                doc.setSlug((String)                                    obj[2]);
                doc.setDescription((String)                             obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getStoreTermTranslationsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer insertProductAttributeTerm(ProductAttributeTermForm request) {
        if(     (securityRepositoryJPA.userHasPermissions_OR(53L,"669") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes",request.getAttribute_id().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(53L,"670") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes",request.getAttribute_id().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery =
                    " insert into product_attribute_terms " +
                            "(" +
                            "master_id, " +
                            "name, " +
                            "slug, " +
                            "description, " +
                            "attribute_id, " +
                            "menu_order" +
                            ") values (" +
                            myMasterId + "," +
                            ":name," +
                            ":slug," +
                            ":description," +
                            request.getAttribute_id() + ", " +
                            " (select coalesce(max(menu_order),0)+1 from product_attribute_terms where attribute_id=" + request.getAttribute_id() + ")" +
                            ")";
            try {
                commonUtilites.idBelongsMyMaster("product_attributes",  request.getAttribute_id(),  myMasterId);
                commonUtilites.idBelongsMyMaster("companies",           request.getCompanyId(),     myMasterId);

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", request.getName());
                query.setParameter("description", request.getDescription());
                query.setParameter("slug", request.getSlug());
                query.executeUpdate();

                // find created term by product_attribute_terms_name_uq (product attribute need to have unique terms names)
                stringQuery="select id from product_attribute_terms where master_id="+myMasterId+" and attribute_id = "+request.getAttribute_id()+" and name=:name";
                Query query2 = entityManager.createNativeQuery(stringQuery);
                query2.setParameter("name", request.getName());
                Long newTermId = Long.valueOf(query2.getSingleResult().toString());

                //Saving the list of translations of this term
                if (!Objects.isNull(request.getStoreTermTranslations()) && request.getStoreTermTranslations().size() > 0) {
                    for (StoreTranslationTermJSON row : request.getStoreTermTranslations()) {
                        saveStoreTermTranslations(row, myMasterId, request.getCompanyId(), request.getAttribute_id(), newTermId);
                    }
                }
                //Saving the list of online stores that term belongs to
                List<Long> storesIds = getAttributeStoresIds(request.getAttribute_id(), myMasterId);
                if (!Objects.isNull(storesIds) && storesIds.size() > 0) {
                    for (Long storeId : storesIds) {
                        commonUtilites.idBelongsMyMaster("stores", storeId, myMasterId);
                        saveTermStore(newTermId, storeId, myMasterId, request.getCompanyId());
                    }
                }

                return 1;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                e.printStackTrace();
                Throwable t = e.getCause();
                while ((t != null) && !(t instanceof ConstraintViolationException)) {
                    t = t.getCause();
                }
                if (t != null) {
                    String message = ((ConstraintViolationException) t).getSQLException().getMessage();
                    if (message.contains("product_attribute_terms_name_uq")) {
                        logger.error("ConstraintViolationException (product_attribute_terms) in method insertProductAttributeTerm. (product_attribute_terms_name_uq)", e);
                        return -215; //product_attribute_terms_name_uq
                    } else if (message.contains("product_attribute_terms_slug_uq")) {
                        logger.error("ConstraintViolationException (product_attribute_terms) in method insertProductAttributeTerm. (product_attribute_terms_slug_uq)", e);
                        return -217;
                    } else if (message.contains("store_translate_terms_name_uq")){
                        logger.error("ConstraintViolationException (store_translate_terms) in method saveStoreTermTranslations->updateProductAttributeTerm. (store_translate_terms_name_uq)", e);
                        return -216; //store_translate_terms_name_uq
                    } else if (message.contains("store_translate_terms_slug_uq")){
                        logger.error("ConstraintViolationException (store_translate_terms) in method saveStoreTermTranslations->updateProductAttributeTerm. (store_translate_terms_slug_uq)", e);
                        return -218; //store_translate_terms_slug_uq
                    } else {
                        logger.error("Exception in method insertProductAttributeTerm. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    logger.error("Exception in method insertProductAttributeTerm. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updateProductAttributeTerm(ProductAttributeTermForm request) {
        if(     (securityRepositoryJPA.userHasPermissions_OR(53L,"669") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes",request.getAttribute_id().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(53L,"670") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes",request.getAttribute_id().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery =
                    " update " +
                        " product_attribute_terms " +
                    " set " +
                        " name = :name, " +
                        " slug = :slug, " +
                        " description = :description, " +
                        " date_time_changed = now()" +
                    " where " +
                        " master_id = " + myMasterId +
                        " and id = " + request.getId() +
                        " and attribute_id = " + request.getAttribute_id();
            try{

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.setParameter("slug",request.getSlug());
                query.executeUpdate();
                //save the list of translations of this term
                if (!Objects.isNull(request.getStoreTermTranslations()) && request.getStoreTermTranslations().size() > 0) {
                    for (StoreTranslationTermJSON row : request.getStoreTermTranslations()) {
                        saveStoreTermTranslations(row, myMasterId, request.getCompanyId(), request.getAttribute_id(), request.getId());
                    }
                }
                //Set this term as need to be synchronized with online store(s)
                markTermsAsNeedToSyncWoo(new HashSet<>(Arrays.asList(request.getId())),myMasterId);
                return 1;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                e.printStackTrace();
                Throwable t = e.getCause();
                while ((t != null) && !(t instanceof ConstraintViolationException)) {
                    t = t.getCause();
                }
                if (t != null) {
                    String message = ((ConstraintViolationException) t).getSQLException().getMessage();
                    if(message.contains("product_attribute_terms_name_uq")){
                        logger.error("ConstraintViolationException (product_attribute_terms) in method updateProductAttributeTerm. (product_attribute_terms_name_uq)", e);
                        return -215; //product_attribute_terms_name_uq
                    } else if (message.contains("product_attribute_terms_slug_uq")){
                        logger.error("ConstraintViolationException (product_attribute_terms) in method updateProductAttributeTerm. (product_attribute_terms_slug_uq)", e);
                        return -217;
                    } else if (message.contains("store_translate_terms_name_uq")){
                        logger.error("ConstraintViolationException (store_translate_terms) in method saveStoreTermTranslations->updateProductAttributeTerm. (store_translate_terms_name_uq)", e);
                        return -216; //store_translate_terms_name_uq
                    } else if (message.contains("store_translate_terms_slug_uq")){
                        logger.error("ConstraintViolationException (store_translate_terms) in method saveStoreTermTranslations->updateProductAttributeTerm. (store_translate_terms_slug_uq)", e);
                        return -218; //store_translate_terms_slug_uq
                    } else {
                        logger.error("Exception in method updateProductAttributeTerm. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    logger.error("Exception in method updateProductAttributeTerm. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1;
    }

    private void saveTermStore(Long term_id, Long store_id, Long master_id, Long company_id) throws Exception {
        String stringQuery =
                "insert into stores_terms (" +
                "   master_id," +
                "   company_id," +
                "   store_id," +
                "   term_id" +
                ") values (" +
                    master_id+", "+
                    company_id+", "+
                    "(select id from stores where id="+store_id+" and master_id="+master_id+"), "+// +", " +
                    "(select id from product_attribute_terms where id="+term_id+" and master_id="+master_id+") "+// чтобы не мочь изменить терм другого master_id, случайно или намеренно
                ") ON CONFLICT ON CONSTRAINT stores_terms_uq " +// "upsert"
                " DO NOTHING";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveTermStore. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }
    private void saveStoreTermTranslations(StoreTranslationTermJSON row, Long master_id, Long company_id, Long attribute_id, Long term_id) throws Exception {
        String stringQuery = "insert into store_translate_terms (" +
                "   master_id," +
                "   company_id," +
                "   attribute_id," +
                "   lang_code," +
                "   term_id, " +
                "   name, " +
                "   slug, " +
                "   description " +
                "   ) values (" +
                master_id+", "+
                company_id+", "+
                attribute_id+", "+
                ":lang_code," +
                "(select id from product_attribute_terms where id="+term_id+" and master_id="+master_id+"), "+// чтобы не мочь изменить терм другого master_id, случайно или намеренно
                ":name," +
                ":slug," +
                ":description" +
                ") ON CONFLICT ON CONSTRAINT term_lang_uq " +// "upsert"
                " DO update set " +
                " name = :name, " +
                " slug = :slug, " +
                " description = :description";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name", row.getName());
            query.setParameter("slug", row.getSlug());
            query.setParameter("description", row.getDescription());
            query.setParameter("lang_code", row.getLangCode());
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveStoreTermTranslations. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    @SuppressWarnings("Duplicates")
    public Integer saveProductAttributeTermsOrder(List<ProductAttributeTermForm> request, Long myMasterId) throws Exception{
        String stringQuery="";
        int i = 1;
        Set<Long> termsIds = new HashSet();
        try {
            for (ProductAttributeTermForm field : request) {
                stringQuery =
                        " update product_attribute_terms set " +
                        " menu_order=" + i +
                        " where id=" + field.getId() +
                        " and master_id=" + myMasterId;
                if (!securityRepositoryJPA.userHasPermissions_OR(53L, "669")) //Если нет прав по всем предприятиям
                {
//            остается только на своё предприятие
                    int myCompanyId = userRepositoryJPA.getMyCompanyId();
                    stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
                }
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                i++;
                termsIds.add(field.getId());
            }
            //Set terms as need to be synchronized with online store(s)
            if(termsIds.size() > 0)
                markTermsAsNeedToSyncWoo(termsIds, myMasterId);
            return 1;
        } catch (Exception e) {
            logger.error("Exception in method saveProductAttributeTermsOrder. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e); // cancelling the parent transaction
        }
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer deleteProductAttributeTerm(Long termId) {
        if (securityRepositoryJPA.userHasPermissions_OR(53L, "669,670"))// редактирование своих или чужих предприятий
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery="";
            try {
                    stringQuery =
                            "   delete from product_attribute_terms  " +
                            "   where " +
                            "   master_id = "   + myMasterId +
                            "   and id = "            + termId;
            /*if (!securityRepositoryJPA.userHasPermissions_OR(53L, "669")) //Если нет прав по всем предприятиям
                    {
                        int myCompanyId = userRepositoryJPA.getMyCompanyId();
                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
                    }*/
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductAttributeTerm. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    private void markAttributesAsNeedToSyncWoo(Set<Long> attributesIds, Long masterId) throws Exception {

        String stringQuery =
                " update stores_attributes " +
                " set need_to_syncwoo = true " +
                " where " +
                " master_id = " + masterId +
                " and attribute_id in "+ commonUtilites.SetOfLongToString(attributesIds,",","(",")");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markAttributesAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    private void markTermsAsNeedToSyncWoo(Set<Long> termsIds, Long masterId) throws Exception {

        String stringQuery =
                " update stores_terms " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        " master_id = " + masterId +
                        " and term_id in "+ commonUtilites.SetOfLongToString(termsIds,",","(",")");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markTermsAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

}
