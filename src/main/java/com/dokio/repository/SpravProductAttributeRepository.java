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
    CommonUtilites commonUtilites;

    private Logger logger = Logger.getLogger("SpravProductAttribute");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name_short","company","name","creator","date_time_created_sort","code_lit","date_created","slug", "type", "is_default")
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
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

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
                    "           p.has_archives as has_archives " +
                    "           from product_attributes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
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
            }
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
                    " has_archives = " + request.getHas_archives() +
                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("type",request.getType());
                query.setParameter("slug",request.getSlug());
                query.setParameter("order_by",request.getOrder_by());
                query.executeUpdate();
                saveProductAttributeTermsOrder(request.getTerms(),myMasterId);
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
                    if(message.contains("product_attributes_slug_uq")){
                        logger.error("ConstraintViolationException (product_attributes) in method updateProductAttribute. (product_attributes_slug_uq)", e);
                        return -212;
                    } else {
                        logger.error("ConstraintViolationException (product_attributes) in method updateProductAttribute. (product_attributes_name_uq)", e);
                        return -214; //product_attributes_name_uq
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
                    " :order_by, " +
                    request.getHas_archives() + ", " +
                    " false)";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("type",request.getType());
                query.setParameter("slug",request.getSlug());
                query.setParameter("order_by",request.getOrder_by());
                query.executeUpdate();
                stringQuery="select id from product_attributes where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                return Long.valueOf(query2.getSingleResult().toString());
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
                    if(message.contains("product_attributes_slug_uq")){
                        logger.error("ConstraintViolationException (product_attributes) in method updateProductAttribute. (product_attributes_slug_uq)", e);
                        return -212L;
                    } else {
                        logger.error("ConstraintViolationException (product_attributes) in method updateProductAttribute. (product_attributes_name_uq)", e);
                        return -214L; //product_attributes_name_uq
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

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteProductAttribute(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(53L, "665") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(53L, "666") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update product_attributes p" +
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
                logger.error("Exception in method deleteProductAttribute. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteProductAttribute(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(53L, "665") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(53L, "666") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update product_attributes p" +
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
                logger.error("Exception in method undeleteProductAttribute. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }


    // inserting base set of product attributes on register of new user
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertProductAttributeFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{
                "'color'","'size'"});
        stringQuery = "insert into product_attributes ( master_id,creator_id,company_id,date_time_created,name,type,slug,order_by,has_archives,is_deleted) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("color")+"', 'select','menu_order',false, false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("size")+"', 'select','menu_order',false,false)";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertProductAttributeFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
//*****************************************************************************************************************************************************
//****************************************************   T  E  R  M  S   ******************************************************************************
//*****************************************************************************************************************************************************


    @SuppressWarnings("Duplicates")
    public List<ProductAttributeTermJSON> getProductAttributeTermsList (Long attrbuteId) {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            stringQuery =       "select " +
                    "           p.id as id," +
                    "           p.name as name, " +
                    "           p.slug as slug, " +
                    "           p.description as description " +
                    "           from product_attribute_terms p " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           where  p.master_id=" + myMasterId +
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
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductAttributeTermsList. SQL query:" + stringQuery, e);
                return null;
            }
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer insertProductAttributeTerm(ProductAttributeTermForm row) {
        if(     (securityRepositoryJPA.userHasPermissions_OR(53L,"669") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes",row.getAttribute_id().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(53L,"670") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes",row.getAttribute_id().toString())))
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
                            row.getAttribute_id() + ", " +
                            " (select coalesce(max(menu_order),0)+1 from product_attribute_terms where attribute_id=" + row.getAttribute_id() + ")" +
                            ")";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", row.getName());
                query.setParameter("description", row.getDescription());
                query.setParameter("slug", row.getSlug());
                query.executeUpdate();
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
                    if (message.contains("product_attribute_terms_slug_uq")) {
                        logger.error("ConstraintViolationException (product_attribute_terms) in method insertProductAttributeTerm. (product_attribute_terms_slug_uq)", e);
                        return -212;
                    } else {
                        logger.error("ConstraintViolationException (product_attribute_terms) in method insertProductAttributeTerm. (product_attribute_terms_name_uq)", e);
                        return -214; //product_attribute_terms_name_uq
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
    public Integer updateProductAttributeTerm(ProductAttributeTermForm row) {
        if(     (securityRepositoryJPA.userHasPermissions_OR(53L,"669") && securityRepositoryJPA.isItAllMyMastersDocuments("product_attributes",row.getAttribute_id().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(53L,"670") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_attributes",row.getAttribute_id().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery =
                    " update " +
                        " product_attribute_terms " +
                    " set " +
                        " name = :name, " +
                        " slug = :slug, " +
                        " description = :description " +
                    " where " +
                        " master_id = " + myMasterId +
                        " and id = " + row.getId() +
                        " and attribute_id = " + row.getAttribute_id();
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",row.getName());
                query.setParameter("description",row.getDescription());
                query.setParameter("slug",row.getSlug());
                query.executeUpdate();
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
                    if(message.contains("product_attribute_terms_slug_uq")){
                        logger.error("ConstraintViolationException (product_attribute_terms) in method updateProductAttributeTerm. (product_attribute_terms_slug_uq)", e);
                        return -212;
                    } else {
                        logger.error("ConstraintViolationException (product_attribute_terms) in method updateProductAttributeTerm. (product_attribute_terms_name_uq)", e);
                        return -214; //product_attribute_terms_name_uq
                    }
                } else {
                    logger.error("Exception in method updateProductAttributeTerm. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1;
    }


    @SuppressWarnings("Duplicates")
    public Integer saveProductAttributeTermsOrder(List<ProductAttributeTermForm> request, Long myMasterId) throws Exception{
        String stringQuery="";
        int i = 1;
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
            }
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
            if (!securityRepositoryJPA.userHasPermissions_OR(53L, "669")) //Если нет прав по всем предприятиям
                    {
//            остается только на своё предприятие
                        int myCompanyId = userRepositoryJPA.getMyCompanyId();
                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
                    }
                    entityManager.createNativeQuery(stringQuery).executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductAttributeTerm. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }



}
