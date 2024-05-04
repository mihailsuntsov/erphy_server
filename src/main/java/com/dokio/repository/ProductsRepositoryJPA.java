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

import com.dokio.message.request.*;
import com.dokio.message.request.additional.*;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.Sprav.SpravSysAssignmentsJSON;
import com.dokio.message.response.Sprav.SpravSysRemindersJSON;
import com.dokio.message.response.Sprav.StoresListJSON;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
import com.dokio.model.Sprav.SpravSysEdizm;
import com.dokio.model.Sprav.SpravSysNds;
import com.dokio.model.Sprav.SpravSysPPR;
import com.dokio.repository.Exceptions.*;
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
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Repository
public class ProductsRepositoryJPA {
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
//    @Autowired
//    private StoreProductsRepository storeProductsRepository;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    private FileRepositoryJPA fileRepository;
//    @Autowired
//    private StoreProductsRepository storeProductsRepository;

    // Инициализация логера
    private static final Logger logger = Logger.getLogger(ProductsRepositoryJPA.class);

    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("docName","price","change","quantity","last_operation_price","ppr_id","description","p.article","p.name","article","name","not_buy","not_sell","last_purchase_price","avg_purchase_price","avg_netcost_price","doc_number","name","status_name","product_count","is_completed","company","department","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_ASSIGNMENT_TYPES
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("customer","manually")
            .collect(Collectors.toCollection(HashSet::new)));
    @Transactional
    public List<ProductsTableJSON> getProductsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int categoryId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Меню - таблица
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = Objects.isNull(filterOptionsIds)?false:filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           p.product_code as product_code, " +
                    "           p.ppr_id as ppr_id, " +
                    "           coalesce(p.by_weight,false) as by_weight, " +
                    "           p.edizm_id as edizm_id, " +
                    "           p.nds_id as nds_id, " +
                    "           p.weight as weight, " +
                    "           p.volume as volume, " +
                    "           p.weight_edizm_id as weight_edizm_id, " +
                    "           p.volume_edizm_id as volume_edizm_id, " +
                    "           coalesce(p.markable,false) as markable, " +
                    "           p.markable_group_id as markable_group_id, " +
                    "           coalesce(p.excizable,false) as excizable, " +
                    "           p.article as article, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.type as type, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '" + myTimeZone + "', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '" + myTimeZone + "', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           coalesce(p.label_description,'') as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.not_buy, false) as not_buy, " +
                    "           coalesce(p.not_sell, false) as not_sell, " +
                    "           coalesce(p.label_description,'') as label_description" +
                    "           from products p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
//                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted +
                    (categoryId != 0 ? " and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id=" + categoryId + ") " : "");

            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.label_description) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.article) like upper(CONCAT('%',:sg,'%')) or " +
                        "(upper(CONCAT('%',:sg,'%')) in (select upper(value) from product_barcodes where product_id=p.id))  or " +
                        "to_char(p.product_code_free,'fm0000000000') like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(coalesce(p.type,'simple')) like upper(CONCAT('%',:sg,'%')))";
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
                Query query = entityManager.createNativeQuery(stringQuery, ProductsTableJSON.class)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                return query.getResultList();

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductsTable. SQL query:" + stringQuery, e);
                return null;
            }

        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getProductsSize(String searchString, int companyId, int categoryId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//"Группы товаров" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = Objects.isNull(filterOptionsIds)?false:filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
//                    "           pg.name as productgroup " +
                    "           from products p " +
//                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted +
                    (categoryId != 0 ? " and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id=" + categoryId + ") " : "");

            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.label_description) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.article) like upper(CONCAT('%',:sg,'%')) or " +
                        "(upper(CONCAT('%',:sg,'%')) in (select upper(value) from product_barcodes where product_id=p.id))  or " +
                        "to_char(p.product_code_free,'fm0000000000') like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(coalesce(p.type,'simple')) like upper(CONCAT('%',:sg,'%')))";
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

    public ProductsJSON getProductValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           p.product_code as product_code, " +
                    "           p.ppr_id as ppr_id, " +
                    "           coalesce(p.by_weight,false) as by_weight, " +
                    "           p.edizm_id as edizm_id, " +
                    "           p.nds_id as nds_id, " +
                    "           p.weight as weight, " +
                    "           p.volume as volume, " +
                    "           p.weight_edizm_id as weight_edizm_id, " +
                    "           p.volume_edizm_id as volume_edizm_id, " +
                    "           coalesce(p.markable,false) as markable, " +
                    "           p.markable_group_id as markable_group_id, " +
                    "           coalesce(p.excizable,false) as excizable, " +
                    "           coalesce(p.article,'') as article, " +
                    "           p.product_code_free as product_code_free, " +
                    "           p.group_id as productgroup_id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pg.name as productgroup, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '" + myTimeZone + "', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '" + myTimeZone + "', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.description as description, " +
                    "           coalesce(p.not_buy, false) as not_buy, " +
                    "           coalesce(p.indivisible, false) as indivisible, " +
                    "           coalesce(p.not_sell, false) as not_sell, " +

                    ///////////////////////////// STORE ///////////////////////////////////
                    "           coalesce(p.type,'simple') as type, " +
                    "           p.slug as slug, " +
                    "           coalesce(p.featured,false) as featured, " +
                    "           p.short_description as short_description, " +
                    "           coalesce(p.virtual,false) as virtual, " +
                    "           coalesce(p.downloadable,false) as downloadable, " +
                    "           coalesce(p.download_limit,-1) as download_limit, " +
                    "           coalesce(p.download_expiry,-1) as download_expiry, " +
                    "           p.external_url as external_url, " +
                    "           p.button_text as button_text, " +
                    "           coalesce(p.tax_status,'taxable') as tax_status, " +
                    "           coalesce(p.manage_stock,false) as manage_stock, " +
                    "           coalesce(p.stock_status,'instock') as stock_status, " +
                    "           coalesce(p.backorders, 'no') as backorders, " +
                    "           coalesce(p.sold_individually,false) as sold_individually, " +
                    "           p.height as height, " +
                    "           p.width as width, " +
                    "           p.length as length, " +
                    "           p.shipping_class as shipping_class, " +
                    "           coalesce(p.reviews_allowed,true) as reviews_allowed, " +
                    "           p.parent_id as parent_id, " +
                    "           coalesce(p.purchase_note,'') as purchase_note, " +
                    "           p.menu_order as menu_order, " +
                    "           pp.name as parent_name, " +
                    "           to_char(p.date_on_sale_from_gmt at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_on_sale_from_gmt, " +
                    "           to_char(p.date_on_sale_to_gmt   at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_on_sale_to_gmt, " +
//                    "           array_to_string(array(select child_id from product_upsell where master_id=" + myMasterId +" and product_id="+id+"),',', '*') as upsell_ids, " +
//                    "           array_to_string(array(select child_id from product_crosssell where master_id=" + myMasterId +" and product_id="+id+"),',', '*') as crosssell_ids " +
                    "           coalesce(p.low_stock_threshold, 0) as low_stock_threshold"  + ", " +
                    "           coalesce(p.outofstock_aftersale,false) as outofstock_aftersale, " + // auto set product as out-of-stock after it has been sold
                    "           coalesce(p.label_description,'') as label_description," +
                    "           coalesce(p.description_html,'') as description_html, " +  // custom HTML description
                    "           coalesce(p.short_description_html,'') as short_description_html, " +  // custom HTML short description
                    "           coalesce(p.description_type,'editor') as description_type, " + // "editor" or "custom"
                    "           coalesce(p.short_description_type,'editor') as short_description_type, " + // "editor" or "custom"

                    "           coalesce(p.is_srvc_by_appointment, false) as is_srvc_by_appointment, " +
                    "           coalesce(p.scdl_is_only_on_start, false) as scdl_is_only_on_start, " +
                    "           coalesce(p.scdl_max_pers_on_same_time, 1) as scdl_max_pers_on_same_time, " +
                    "           coalesce(p.scdl_srvc_duration, 1) as scdl_srvc_duration, " +
                    "           coalesce(p.scdl_appointment_atleast_before_time, 0) as scdl_appointment_atleast_before_time, " +
                    "           p.scdl_appointment_atleast_before_unit_id as scdl_appointment_atleast_before_unit_id " +

                    "           from products p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           LEFT OUTER JOIN products pp ON p.parent_id=pp.id " +
                    "           where p.id= " + id +
                    "           and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
//            Query query = entityManager.createNativeQuery(stringQuery, ProductsJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
//                ProductsJSON response = (ProductsJSON) query.getSingleResult();
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                ProductsJSON doc = new ProductsJSON();
                if(queryList.size()>0) {
                    doc.setId(Long.parseLong(                       queryList.get(0)[0].toString()));
                    doc.setMaster((String)                          queryList.get(0)[1]);
                    doc.setName((String)                            queryList.get(0)[2]);
                    doc.setProduct_code((Integer)                   queryList.get(0)[3]);
                    doc.setPpr_id((Integer)                         queryList.get(0)[4]);
                    doc.setBy_weight((Boolean)                      queryList.get(0)[5]);
                    doc.setEdizm_id((Integer)                       queryList.get(0)[6]);
                    doc.setNds_id((Integer)                         queryList.get(0)[7]);
                    doc.setWeight((BigDecimal)                      queryList.get(0)[8]);
                    doc.setVolume((BigDecimal)                      queryList.get(0)[9]);
                    doc.setWeight_edizm_id((Integer)                queryList.get(0)[10]);
                    doc.setVolume_edizm_id((Integer)                queryList.get(0)[11]);
                    doc.setMarkable((Boolean)                       queryList.get(0)[12]);
                    doc.setMarkable_group_id((Integer)              queryList.get(0)[13]);
                    doc.setExcizable((Boolean)                      queryList.get(0)[14]);
                    doc.setArticle((String)                         queryList.get(0)[15]);
                    doc.setProduct_code_free(queryList.get(0)[16]!=null?Long.parseLong(queryList.get(0)[16].toString()):null);
                    doc.setProductgroup_id((Integer)                queryList.get(0)[17]);
                    doc.setCreator((String)                         queryList.get(0)[18]);
                    doc.setChanger((String)                         queryList.get(0)[19]);
                    doc.setProductgroup((String)                    queryList.get(0)[20]);
                    doc.setMaster_id((Integer)                      queryList.get(0)[21]);
                    doc.setCreator_id((Integer)                     queryList.get(0)[22]);
                    doc.setChanger_id((Integer)                     queryList.get(0)[23]);
                    doc.setCompany_id((Integer)                     queryList.get(0)[24]);
                    doc.setCompany((String)                         queryList.get(0)[25]);
                    doc.setDate_time_created((String)               queryList.get(0)[26]);
                    doc.setDate_time_changed((String)               queryList.get(0)[27]);
                    doc.setDescription((String)                     queryList.get(0)[30]);
                    doc.setNot_buy((Boolean)                        queryList.get(0)[31]);
                    doc.setIndivisible((Boolean)                    queryList.get(0)[32]);
                    doc.setNot_sell((Boolean)                       queryList.get(0)[33]);
                    doc.setType((String)                            queryList.get(0)[34]);
                    doc.setSlug((String)                            queryList.get(0)[35]);
                    doc.setFeatured((Boolean)                       queryList.get(0)[36]);
                    doc.setShort_description((String)               queryList.get(0)[37]);
                    doc.setVirtual((Boolean)                        queryList.get(0)[38]);
                    doc.setDownloadable((Boolean)                   queryList.get(0)[39]);
                    doc.setDownload_limit((Integer)                 queryList.get(0)[40]);
                    doc.setDownload_expiry((Integer)                queryList.get(0)[41]);
                    doc.setExternal_url((String)                    queryList.get(0)[42]);
                    doc.setButton_text((String)                     queryList.get(0)[43]);
                    doc.setTax_status((String)                      queryList.get(0)[44]);
                    doc.setManage_stock((Boolean)                   queryList.get(0)[45]);
                    doc.setStock_status((String)                    queryList.get(0)[46]);
                    doc.setBackorders((String)                      queryList.get(0)[47]);
                    doc.setSold_individually((Boolean)              queryList.get(0)[48]);
                    doc.setHeight((BigDecimal)                      queryList.get(0)[49]);
                    doc.setWidth((BigDecimal)                       queryList.get(0)[50]);
                    doc.setLength((BigDecimal)                      queryList.get(0)[51]);
                    doc.setShipping_class((String)                  queryList.get(0)[52]);
                    doc.setReviews_allowed((Boolean)                queryList.get(0)[53]);
                    doc.setParent_id(queryList.get(0)[54]!=null?Long.parseLong(queryList.get(0)[54].toString()):null);
                    doc.setPurchase_note((String)                   queryList.get(0)[55]);
                    doc.setMenu_order((Integer)                     queryList.get(0)[56]);
                    doc.setParent_name((String)                     queryList.get(0)[57]);
                    doc.setDate_on_sale_from_gmt((String)           queryList.get(0)[58]);
                    doc.setDate_on_sale_to_gmt((String)             queryList.get(0)[59]);
                    doc.setLow_stock_threshold((BigDecimal)         queryList.get(0)[60]);
                    doc.setOutofstock_aftersale((Boolean)           queryList.get(0)[61]);
                    doc.setLabel_description((String)               queryList.get(0)[62]);
                    doc.setDescription_html((String)                queryList.get(0)[63]);
                    doc.setShort_description_html((String)          queryList.get(0)[64]);
                    doc.setDescription_type((String)                queryList.get(0)[65]);
                    doc.setShort_description_type((String)          queryList.get(0)[66]);

                    doc.setIs_srvc_by_appointment((Boolean)         queryList.get(0)[67]);
                    doc.setScdl_is_only_on_start((Boolean)          queryList.get(0)[68]);
                    doc.setScdl_max_pers_on_same_time((Integer)     queryList.get(0)[69]);
                    doc.setScdl_srvc_duration((Integer)             queryList.get(0)[70]);
                    doc.setScdl_appointment_atleast_before_time((Integer) queryList.get(0)[71]);
                    doc.setScdl_appointment_atleast_before_unit_id( queryList.get(0)[72]!=null?Long.parseLong(queryList.get(0)[72].toString()):null);

                    doc.setDefaultAttributes(getDefaultAttributes(myMasterId, doc.getId()));
                    doc.setProductVariations(getProductVariations(myMasterId, id));
                    doc.setVariation(isProductUsedAsVariation(doc.getId())); // if this product is a variation of another product
                    doc.setProductResourcesTable(getProductResources(myMasterId, doc.getId()));
                    doc.setScdl_customer_reminders(getAppointmentReminders(myMasterId, doc.getId(), "customer"));
                    doc.setScdl_employee_reminders(getAppointmentReminders(myMasterId, doc.getId(), "employee"));
                    doc.setScdl_assignments(getAppointmentAssignments(myMasterId, doc.getId()));
                }
//              doc.setStoresIds(getProductStoresIds(id, myMasterId));
                doc.setStoreProductTranslations(getStoreProductTranslationsList(doc.getId(), myMasterId));
                return doc;
            } catch (NoResultException nre) {
                logger.error("Exception in method getProductValues. SQL query:"+stringQuery, nre);
                return null;
            } catch (Exception e) {
                logger.error("Exception in method getProductValues. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, ProductAlreadyUsedAsVariation.class})
    public Integer updateProduct(ProductsForm request){
        EntityManager emgr = emf.createEntityManager();
        Products document = emgr.find(Products.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate = securityRepositoryJPA.userHasPermissions_OR(14L, "170"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate = securityRepositoryJPA.userHasPermissions_OR(14L, "169"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany = (Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId = document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getMyMasterId();//владелец моего аккаунта
        boolean isItMyMastersDoc = (DocumentMasterId.equals(myMasterId));// документ под юрисдикцией главного аккаунта
        if (((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                || (!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try {

                int subjOfTradeInDB = (Integer)commonUtilites.getFieldValueFromTableById("products","ppr_id",myMasterId,request.getId());
                int subjOfTradeIncome = Integer.parseInt(request.getPpr_id().toString());
                boolean wasOperationsOnProduct = wasOperationsOnProduct(request.getId());
                boolean isProductUsedAsVariation = isProductUsedAsVariation(request.getId());
                // Subject of trade was changed (from Service to Commodity or from Commodity to Service)
                // If this product (or service) is already has the history of operations - Reject this update
                // because it will produce discrepancy of product quantity in warehouse / negative quantity / fraud opportunities
                if(subjOfTradeInDB != subjOfTradeIncome && wasOperationsOnProduct)
                    return -230;

                //non-unique sku check
                // не уникальный Артикул
                if(!Objects.isNull(request.getArticle()) && !request.getArticle().equals("") && isThereProductWithSameSku(request.getArticle(),request.getId(), request.getCompany_id()))
                    return -250;

                // Product selected as a variable, but already used as a variation in another variable product
                // Variation can't be variable product!
                // Вариация не может быть вариативным товаром!
                if(request.getType().equals("variable") && isProductUsedAsVariation)
                    return -280;

                // Нельзя изменить тип товара, у которого уже есть история складских операций, на «Вариативный».
                // You cannot change a product type that already has a warehouse history to "variable".
                if(request.getType().equals("variable") && wasOperationsOnProduct)
                    return -290;

                commonUtilites.idBelongsMyMaster("products", request.getId(), myMasterId);

                // no matter - variable product or not - because can be that it was Variable and now selected as Simple
                // at this place method running for deleted variations (here they are still belongs to product)
                markProductVariationsAsNeedToSyncWoo(request.getId(), myMasterId);

                //if variation changed or deleted - mark its parent product to resync (it will delete non-existed or excess variations on the Woo side)
                if(isProductUsedAsVariation)
                    markVariableProductAsNeedToSyncWoo(request.getId(), myMasterId);

                // сохранение базовых полей
                updateProductsWithoutOrders(request, myMasterId);
                //сохранение порядка поставщиков товара
                if (request.getCagentsIdsInOrderOfList().size() > 1) {
                    int c = 0;
                    for (Long field : request.getCagentsIdsInOrderOfList()) {
                        c++;
                        saveChangeProductCagentsOrder(field, request.getId(), c);
                    }
                }
                //сохранение порядка картинок товара
                if (request.getImagesIdsInOrderOfList().size() > 1) {
                    int i = 0;
                    for (Long field : request.getImagesIdsInOrderOfList()) {
                        i++;
                        saveChangeProductFilesOrder(field, request.getId(), i, "product_files");
                    }
                }

                //сохранение порядка скачиваемых товаров
                if (request.getDfilesIdsInOrderOfList().size() > 1) {
                    int i = 0;
                    for (Long field : request.getDfilesIdsInOrderOfList()) {
                        i++;
                        saveChangeProductFilesOrder(field, request.getId(), i, "product_downloadable_files");
                    }
                }
                // сохранение цен
                if (request.getProductPricesTable().size() > 0) {
                    for (ProductPricesJSON field : request.getProductPricesTable()) {
                        savePrice(field.getPrice_type_id(), request.getId(), myMasterId, request.getCompany_id(), field.getPrice_value()); //         //метод 4
                    }
                }
                // сохранение выбранных категорий
                Set<Long> categories = request.getSelectedProductCategories();
                if (!categories.isEmpty()) { //если есть выбранные чекбоксы категорий
                    saveCategories(request.getId(), categories, myMasterId);
                }
                // deleting categories that not in selected categories
                deleteCategories(myMasterId, request.getId(), categories);
                // сохранение up sell products
                if (!request.getUpsell_ids().isEmpty()) { //если есть выбранные чекбоксы категорий
                    saveUpsellCrosssells(request.getId(), request.getUpsell_ids(), myMasterId, "product_upsell");
                }
                // сохранение cross sell products
                if (!request.getCrosssell_ids().isEmpty()) { //если есть выбранные чекбоксы категорий
                    saveUpsellCrosssells(request.getId(), request.getCrosssell_ids(), myMasterId, "product_crosssell");
                }

                // сохранение grouped products
                if (!request.getGrouped_ids().isEmpty()) { //если есть выбранные чекбоксы категорий
                    saveUpsellCrosssells(request.getId(), request.getGrouped_ids(), myMasterId, "product_grouped");
                }
                // saving attributes
                updateProductAttributes(request.getProductAttributes(), myMasterId, request.getId() );
                //save the list of translations of this product
                if (!Objects.isNull(request.getStoreProductTranslations()) && request.getStoreProductTranslations().size() > 0) {
                    for (StoreTranslationProductJSON row : request.getStoreProductTranslations()) {
                        saveStoreProductTranslations(row, myMasterId, request.getCompany_id(), request.getId());
                    }
                }

                // saving resources
                Set<Long>existingProductResources = new HashSet<>();
                for (ProductResourcesForm row : request.getProductResourcesTable()) {
                    saveProductResourcesQtt(myMasterId, row.getResource_id(), request.getId(), row.getResource_qtt());
                    existingProductResources.add(row.getResource_id());
                }

                // Reminders and Assignment types
                saveAppointmentReminders(myMasterId, request.getId(), request.getScdl_customer_reminders(), "customer");
                saveAppointmentReminders(myMasterId, request.getId(), request.getScdl_employee_reminders(), "employee");
                saveAppointmentAssignments(myMasterId, request.getId(), request.getScdl_assignments());

                // deleting resources that was deleted on frontend
                deleteResourcesThatNoMoreContainedInThisProduct(existingProductResources,request.getId(), myMasterId );

                // deleting attributes that was deleted on frontend
                deleteAttributesFromProduct(request.getProductAttributes(), request.getId(), myMasterId);

                // deleting attribute terms, which attributes was deleted on frontend
                deleteTermsOfDeletedAttributes(request.getProductAttributes(), request.getId(), myMasterId);
                deleteUpsellCrosssells(request.getId(), request.getUpsell_ids(), myMasterId, "product_upsell");
                deleteUpsellCrosssells(request.getId(), request.getCrosssell_ids(), myMasterId, "product_crosssell");
                deleteUpsellCrosssells(request.getId(), request.getGrouped_ids(), myMasterId, "product_grouped");

                //saving Default Variations
                updateDefaultAttributes(request.getDefaultAttributes(), request.getId(), myMasterId);

                //saving variations
                updateProductVariations(request.getProductVariations(), request.getId(), myMasterId);

                // setting variations as need to be synchronized
                // at this place method running for added variations
                markProductVariationsAsNeedToSyncWoo(request.getId(), myMasterId);



                //variation can't be upsell or crosssell product (вариация не может быть upsell or crosssell)
                clearUpsellCrosssellsFromVariations(request.getId(), myMasterId);

                return 1;

            } catch (ProductAlreadyUsedAsVariation e) {
                e.printStackTrace();
                logger.error("ProductAlreadyUsedAsVariation exception in method updateProduct. ", e);
                return -260;
            } catch (VariableProductCantBeUsedAsVariation e) {
                e.printStackTrace();
                logger.error("VariableProductCantBeUsedAsVariation exception in method updateProduct. ", e);
                return -270;
            } catch (Exception e) {
                logger.error("Exception in method updateProduct.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    public List<StoreTranslationProductJSON> getStoreProductTranslationsList(Long productId, Long masterId){
        String stringQuery = "      select   " +
                "           p.lang_code as lang_code," +
                "           coalesce(p.name,'') as name, " +
                "           coalesce(p.slug,'') as slug, " +
                "           coalesce(p.description,'') as description, " +
                "           coalesce(p.short_description,'') as short_description, " +
                "           coalesce(p.description_html,'') as description_html, " +
                "           coalesce(p.short_description_html,'') as short_description_html " +
                "           from     store_translate_products p " +
                "           where    p.master_id=" + masterId +
                "           and      p.product_id =" + productId +
                "           order by p.lang_code";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<StoreTranslationProductJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                StoreTranslationProductJSON doc = new StoreTranslationProductJSON();
                doc.setLangCode((String)                                obj[0]);
                doc.setName((String)                                    obj[1]);
                doc.setSlug((String)                                    obj[2]);
                doc.setDescription((String)                             obj[3]);
                doc.setShortDescription((String)                        obj[4]);
                doc.setDescriptionHtml((String)                         obj[5]);
                doc.setShortDescriptionHtml((String)                    obj[6]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getStoreProductTranslationsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
    private void saveStoreProductTranslations(StoreTranslationProductJSON row, Long master_id, Long company_id, Long product_id) throws Exception {
        String stringQuery = "insert into store_translate_products (" +
                "   master_id," +
                "   company_id," +
                "   lang_code," +
                "   product_id, " +
                "   name, " +
                "   slug, " +
                "   description, " +
                "   short_description, " +
                "   description_html, " +
                "   short_description_html " +
                "   ) values (" +
                master_id+", "+
                company_id+", "+
                ":lang_code," +
                "(select id from products where id="+product_id+" and master_id="+master_id+"), "+// чтобы не мочь изменить товар другого master_id, случайно или намеренно
                ":name," +
                ":slug," +
                ":description, " +
                ":short_description, " +
                ":description_html, " +
                ":short_description_html " +
                ") ON CONFLICT ON CONSTRAINT product_lang_uq " +// "upsert"
                " DO update set " +
                " name = :name, " +
                " slug = :slug, " +
                " description=:description, " +
                " short_description=:short_description, " +
                " description_html=:description_html, " +
                " short_description_html=:short_description_html";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name", row.getName());
            query.setParameter("slug", row.getSlug());
            query.setParameter("lang_code", row.getLangCode());
            query.setParameter("description", row.getDescription());
            query.setParameter("short_description", row.getShortDescription());
            query.setParameter("description_html", row.getDescriptionHtml());
            query.setParameter("short_description_html", row.getShortDescriptionHtml());
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveStoreProductTranslations. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }
    @SuppressWarnings("Duplicates")
    private void saveUpsellCrosssells(Long productId, Set<Long> childs, Long masterId, String tableName) throws Exception {
        String stringQuery;
        BigInteger cnt;
        try{
            for(Long childId : childs){

                //check that this pair (category and product) is not in table
                stringQuery=
                        "select count(*) from "+tableName+" where product_id = " + productId + " and child_id = " + childId + " and master_id=" + masterId;
                try {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    cnt = (BigInteger) query.getSingleResult();
                } catch (NoResultException nre) {
                    cnt = new BigInteger("0");
                } catch (Exception e) {
                    logger.error("Exception in method saveUpsellCrosssells. SQL: " + stringQuery, e);
                    e.printStackTrace();
                    throw new Exception(e);
                }
                //if there is no the pair like this
                if(cnt.equals(new BigInteger("0"))){
                    commonUtilites.idBelongsMyMaster("products", childId, masterId);
                    stringQuery="insert into "+tableName+" (" +
                            "master_id, product_id, child_id) values ("+masterId+", "+productId+", "+childId+")";
                    try {
                        Query query = entityManager.createNativeQuery(stringQuery);
                        query.executeUpdate();
                    } catch (Exception e) {
                        logger.error("Exception in method saveUpsellCrosssells. SQL: " + stringQuery, e);
                        e.printStackTrace();
                        throw new Exception(e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveUpsellCrosssells.", e);
            throw new Exception(e);
        }
    }
    @SuppressWarnings("Duplicates")
    private void deleteUpsellCrosssells(Long productId, Set<Long> childs, Long masterId, String tableName) throws Exception {
        String stringQuery;
        try{
            String gc;
            if(childs.size()>0)
                gc=commonUtilites.SetOfLongToString(childs,",","(",")");
            else gc="(0)";

            stringQuery=
                    " delete from " + tableName +
                            " where product_id="+productId+" " +
                            " and child_id not in " + gc +
                            " and (select count(*) from products where master_id="+masterId+" and id="+productId+")>0";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method deleteUpsellCrosssells.", e);
            throw new Exception(e);
        }
    }
    @SuppressWarnings("Duplicates")
    private void saveCategories(Long productId, Set<Long> categories, Long masterId) throws Exception {
        String stringQuery;
        BigInteger cnt;
        try{
            for(Long categoryId : categories){

                commonUtilites.idBelongsMyMaster("product_categories", categoryId, masterId);

                //check that this pair (category and product) is not in table
                stringQuery=
                    "select count(*) from product_productcategories where product_id = " + productId + " and category_id = " + categoryId;
                try {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    cnt = (BigInteger) query.getSingleResult();
                } catch (NoResultException nre) {
                    cnt = new BigInteger("0");
                } catch (Exception e) {
                    logger.error("Exception in method saveCategories. SQL: " + stringQuery, e);
                    e.printStackTrace();
                    throw new Exception(e);
                }
                //if there is no the pair like this
                if(cnt.equals(new BigInteger("0"))){
                    stringQuery="insert into product_productcategories (" +
                    "product_id, category_id) values ("+productId+", "+categoryId+")";
                    try {
                        Query query = entityManager.createNativeQuery(stringQuery);
                        query.executeUpdate();
                    } catch (Exception e) {
                        logger.error("Exception in method saveCategories. SQL: " + stringQuery, e);
                        e.printStackTrace();
                        throw new Exception(e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveCategories.", e);
            throw new Exception(e);
        }
    }

    @SuppressWarnings("Duplicates")
    private void deleteCategories(Long masterId, Long productId, Set<Long> goodCategories) throws Exception {
        String stringQuery;
        try{
            String gc;
            if(goodCategories.size()>0)
                gc=commonUtilites.SetOfLongToString(goodCategories,",","(",")");
            else gc="(0)";

            stringQuery=
            " delete from product_productcategories " +
            " where product_id="+productId+" " +
            " and category_id not in " + gc +
            " and (select count(*) from products where master_id="+masterId+" and id="+productId+")>0";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method deleteCategories.", e);
            throw new Exception(e);
        }
    }

    @SuppressWarnings("Duplicates")
    private void savePrice(Long priceTypeId, Long productId, Long myMasterId, Long companyId, BigDecimal priceValue) throws Exception {
        String stringQuery;
        stringQuery=
                "   insert into product_prices (" +
                        "   product_id," +
                        "   price_type_id," +
                        "   price_value," +
                        "   master_id, " +
                        "   company_id " +
                        "   ) values (" +
                        "(select id from products where id="+productId +" and master_id="+myMasterId+"), "+// чтобы не мочь переназначить цену товара другого master_id, случайно или намеренно
                        priceTypeId+", "+
//                        (priceValue==null?"-1":priceValue.toString()) + "," +
                        priceValue+", "+
                        myMasterId + ", "+ companyId + ")" +
                        "ON CONFLICT ON CONSTRAINT product_prices_uq " +// "upsert"
                        " DO update set " +
//                        "price_value = " + (priceValue==null?"-1":priceValue.toString());
                        "price_value = " + priceValue;
        try{

            commonUtilites.idBelongsMyMaster("sprav_type_prices", priceTypeId, myMasterId);
            commonUtilites.idBelongsMyMaster("products", productId, myMasterId);
            commonUtilites.idBelongsMyMaster("companies", companyId, myMasterId);

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method savePrice. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    @SuppressWarnings("Duplicates")
    private void updateProductsWithoutOrders(ProductsForm request, Long myMasterId) throws Exception {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();
            String stringQuery;
        try
        {

            commonUtilites.idBelongsMyMaster("sprav_taxes", request.getNds_id(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_sys_edizm", request.getEdizm_id(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_sys_edizm", request.getWeight_edizm_id(), myMasterId);
            commonUtilites.idBelongsMyMaster("products", request.getParent_id(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_sys_edizm", request.getScdl_appointment_atleast_before_unit_id(), myMasterId);


            stringQuery = " update products set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " name = :name," +
                    " description = :description, "+
                    " article = :article, "+
                    " product_code_free = " + (((request.getProduct_code_free() == null ? 0L : request.getProduct_code_free()) > 0L)?request.getProduct_code_free():generateFreeProductCode(request.getCompany_id())) + ", "+
                    " group_id = " + request.getProductgroup_id() + ", "+
                    " nds_id = " + request.getNds_id() + ", "+
                    " edizm_id = " + request.getEdizm_id() + ", "+
                    " ppr_id = "+request.getPpr_id() +"," +
                    " by_weight = "+request.isBy_weight() +"," +
                    " weight = "+ ((request.getWeight() != null && !request.getWeight().isEmpty() && request.getWeight().trim().length() > 0)?(new BigDecimal(request.getWeight().replace(",", "."))):(new BigDecimal("0"))) +"," +
                    " weight_edizm_id = "+request.getWeight_edizm_id() +"," +
                    " not_buy = "+request.isNot_buy() +"," +
                    " not_sell = "+request.isNot_sell() +"," +
                    " indivisible = "+request.isIndivisible() +"," +
                    " type = :type, "+
                    " slug = :slug, "+
                    " featured = "+ request.getFeatured() +"," +
                    " short_description = :short_description, "+
                    " virtual = " + request.getVirtual() +"," +
                    " downloadable = " + request.getDownloadable() +"," +
                    " download_limit = "+(Objects.isNull(request.getDownload_limit())?-1:request.getDownload_limit()) +"," +
                    " download_expiry = "+(Objects.isNull(request.getDownload_expiry())?-1:request.getDownload_expiry()) +"," +
                    " external_url = :external_url, "+
                    " button_text = :button_text, "+
                    " tax_status = :tax_status, "+
                    " manage_stock = "+request.getManage_stock() +"," +
                    " stock_status = :stock_status, "+
                    " backorders = :backorders, "+
                    " sold_individually = "+request.getSold_individually() +"," +
                    " height = "+ ((request.getHeight() != null && !request.getHeight().isEmpty() && request.getHeight().trim().length() > 0)?(new BigDecimal(request.getHeight().replace(",", "."))):(new BigDecimal("0"))) +"," +
                    " width = "+ ((request.getWidth() != null && !request.getWidth().isEmpty() && request.getWidth().trim().length() > 0)?(new BigDecimal(request.getWidth().replace(",", "."))):(new BigDecimal("0"))) +"," +
                    " length = "+ ((request.getLength() != null && !request.getLength().isEmpty() && request.getLength().trim().length() > 0)?(new BigDecimal(request.getLength().replace(",", "."))):(new BigDecimal("0"))) +"," +
                    " shipping_class = :shipping_class, "+
                    " reviews_allowed = "+request.getReviews_allowed() +"," +
                    " parent_id = "+request.getParent_id() +"," +
                    " purchase_note = :purchase_note, "+
                    " menu_order = "+request.getMenu_order() +"," +
                    " date_on_sale_from_gmt = "+((!Objects.isNull(request.getDate_on_sale_from_gmt())&&!request.getDate_on_sale_from_gmt().equals(""))?("to_timestamp(:date_on_sale_from_gmt,'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'GMT' at time zone '"+myTimeZone+"'"):"null")+","+
                    " date_on_sale_to_gmt = "+((!Objects.isNull(request.getDate_on_sale_to_gmt())&&!request.getDate_on_sale_to_gmt().equals(""))?("to_timestamp(:date_on_sale_to_gmt,'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'GMT' at time zone '"+myTimeZone+"'"):"null")+","+
                    " low_stock_threshold = "+ ((request.getLow_stock_threshold() != null && !request.getLow_stock_threshold().isEmpty() && request.getLow_stock_threshold().trim().length() > 0)?(new BigDecimal(request.getLow_stock_threshold().replace(",", "."))):(new BigDecimal("0"))) +"," +
                    " outofstock_aftersale = " + request.getOutofstock_aftersale() + "," + // auto set product as out-of-stock after it has been sold
                    " label_description = :label_description, "+
                    " description_html = :description_html, "+
                    " short_description_html = :short_description_html, "+
                    " description_type = :description_type, "+
                    " short_description_type = :short_description_type, "+

                    " is_srvc_by_appointment = " + request.isIs_srvc_by_appointment() + "," +
                    " scdl_is_only_on_start = " + request.isScdl_is_only_on_start() + "," +
                    " scdl_max_pers_on_same_time = " + request.getScdl_max_pers_on_same_time() + "," +
                    " scdl_srvc_duration = " + request.getScdl_srvc_duration() + "," +
                    " scdl_appointment_atleast_before_time = " + request.getScdl_appointment_atleast_before_time() + "," +
                    " scdl_appointment_atleast_before_unit_id = " + request.getScdl_appointment_atleast_before_unit_id() +

                    " where master_id = " + myMasterId + " and id = " + request.getId();

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("name", (request.getName() == null ? "" : request.getName()));
                query.setParameter("article", (request.getArticle() == null ? "" : request.getArticle()));
                query.setParameter("type", (request.getType() == null ? "" : request.getType()));
                query.setParameter("slug", (request.getSlug() == null ? "" : request.getSlug()));
                query.setParameter("short_description", (request.getShort_description() == null ? "" : request.getShort_description()));
                query.setParameter("external_url", (request.getExternal_url() == null ? "" : request.getExternal_url()));
                query.setParameter("button_text", (request.getButton_text() == null ? "" : request.getButton_text()));
                query.setParameter("tax_status", (request.getTax_status() == null ? "" : request.getTax_status()));
                query.setParameter("stock_status", (request.getStock_status() == null ? "" : request.getStock_status()));
                query.setParameter("backorders", (request.getBackorders() == null ? "" : request.getBackorders()));
                query.setParameter("shipping_class", (request.getShipping_class() == null ? "" : request.getShipping_class()));
                query.setParameter("purchase_note", (request.getPurchase_note() == null ? "" : request.getPurchase_note()));
                query.setParameter("label_description", (request.getLabel_description() == null ? "" : request.getLabel_description()));
                query.setParameter("description_html", (request.getDescription_html() == null ? "" : request.getDescription_html()));
                query.setParameter("short_description_html", (request.getShort_description_html() == null ? "" : request.getShort_description_html()));
                query.setParameter("description_type", (request.getDescription_type() == null ? "editor" : request.getDescription_type()));
                query.setParameter("short_description_type", (request.getShort_description_type() == null ? "editor" : request.getShort_description_type()));

                if(!Objects.isNull(request.getDate_on_sale_from_gmt())&&!request.getDate_on_sale_from_gmt().equals(""))
                    query.setParameter("date_on_sale_from_gmt", request.getDate_on_sale_from_gmt()+" 00:00:00.000");
                if(!Objects.isNull(request.getDate_on_sale_to_gmt())&&!request.getDate_on_sale_to_gmt().equals(""))
                    query.setParameter("date_on_sale_to_gmt", request.getDate_on_sale_to_gmt()+" 23:59:59.999");
                query.executeUpdate();
            }catch (Exception e) {
                logger.error("Exception in method updateProducts. The stage of saving base product fields. stringQuery=", e);
                e.printStackTrace();
                throw new Exception();
            }
    }

    // сохранение порядка картинок товара (права не нужны, т.к. вызывается после проверки всех прав)
    @SuppressWarnings("Duplicates")
    private boolean saveChangeProductFilesOrder(Long ProductImageId, Long productId, int order, String tableName) throws Exception {
        String stringQuery="";
        try {
            stringQuery = " update "+tableName+" set " +
                    " output_order=" + order +
                    " where file_id=" + ProductImageId +
                    " and product_id=" + productId;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method saveChangeProductFilesOrder. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // сохранение порядка картинок товара (права не нужны, т.к. вызывается после проверки всех прав)
    @SuppressWarnings("Duplicates")
    private boolean saveChangeProductCagentsOrder(Long cagentId, Long productId, int order) throws Exception {
        String stringQuery="";
        try {
            stringQuery = " update product_cagents set " +
                    " output_order=" + order +
                    " where cagent_id=" + cagentId +
                    " and product_id=" + productId;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method saveChangeProductCagentsOrder. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertProduct(ProductsForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "163,164"))//  "Создание"
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();

            //non-unique sku check
            if(!Objects.isNull(request.getArticle()) && !request.getArticle().equals("") && isThereProductWithSameSku(request.getArticle(),0L,request.getCompany_id()))
                return -250L;

            //plan limit check
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if(userRepositoryJPA.getMyConsumedResources().getProducts()>=userRepositoryJPA.getMyMaxAllowedResources().getProducts())
                    return -120L; // number of products is out of bounds of tariff plan
            try {



                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_sys_edizm", request.getEdizm_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_taxes", request.getNds_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_sys_edizm", request.getWeight_edizm_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_sys_edizm", request.getVolume_edizm_id(), myMasterId);

                EntityManager emgr = emf.createEntityManager();
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие создаваемого документа
                Long DocumentMasterId = companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
                boolean notMyCompany = !((Long.valueOf(myCompanyId)).equals(request.getCompany_id()));
                boolean canCreateAllCompanies = securityRepositoryJPA.userHasPermissions_OR(14L, "163");
                //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
                if ((!canCreateAllCompanies && notMyCompany) || !DocumentMasterId.equals(myMasterId)) {
                    return null;
                } else {

                        Products newDocument = new Products();
                        //создатель
                        User creator = userRepository.getUserByUsername(userRepository.getUserName());
                        newDocument.setCreator(creator);//создателя
                        //владелец
                        User master = userRepository.getUserById(myMasterId);
                        newDocument.setMaster(master);
                        //дата и время создания
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        newDocument.setDate_time_created(timestamp);//
                        //предприятие
                        newDocument.setCompany(companyRepositoryJPA.getCompanyById(request.getCompany_id()));
                        //Наименование
                        newDocument.setName(request.getName() == null ? "" : request.getName());
                        //дополнительная информация
                        newDocument.setDescription(request.getDescription() == null ? "" : request.getDescription());
                        //артикул
                        newDocument.setArticle(request.getArticle() == null ? "" : request.getArticle());
                        //свободный код
                        //              newDocument.setProduct_code_free        (request.getProduct_code_free() == null ? "": request.getProduct_code_free());
                        newDocument.setProduct_code_free(generateFreeProductCode(request.getCompany_id()));

                        //группа товаров
//                        if (request.getProductgroup_id() != null) {
//                            ProductGroups pg = emgr.find(ProductGroups.class, request.getProductgroup_id());
//                            newDocument.setProductGroup(pg);
//                        }

                        Set<Long> categories = request.getSelectedProductCategories();
                        if (!categories.isEmpty()) {

                            for(Long categoryId:categories){
                                commonUtilites.idBelongsMyMaster("product_categories", categoryId, myMasterId);
                            }
                            Set<ProductCategories> setCategoriesOfProduct = getCategoriesSetBySetOfCategoriesId(categories);
                            newDocument.setProductCategories(setCategoriesOfProduct);
                        }

                        //код товара (до 99999)
                        //newDocument.setProduct_code(request.getProduct_code());
                        //признак предмета расчёта
                        if (request.getPpr_id() != null) {
                            SpravSysPPR ed = emgr.find(SpravSysPPR.class, request.getPpr_id());
                            newDocument.setPpr(ed);
                        }
                        //весовой товар (Boolean)
                        newDocument.setBy_weight(request.isBy_weight());
                        //единица измерения товара
                        if (request.getEdizm_id() != null) {
                            SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getEdizm_id());
                            newDocument.setEdizm(ed);
                        }
                        //НДС
                        if (request.getNds_id() != null) {
                            SpravSysNds ed = emgr.find(SpravSysNds.class, request.getNds_id());
                            newDocument.setNds(ed);
                        }
                        //Вес товара (приходит String, конвертим в BigDecimal)
                        if (request.getWeight() != null && !request.getWeight().isEmpty() && request.getWeight().trim().length() > 0) {
                            newDocument.setWeight(new BigDecimal(request.getWeight().replace(",", ".")));
                        } else {
                            newDocument.setWeight(new BigDecimal("0"));
                        }
                        //Объём товара (приходит String, конвертим в BigDecimal)
                        if (request.getVolume() != null && !request.getVolume().isEmpty() && request.getVolume().trim().length() > 0) {
                            newDocument.setVolume(new BigDecimal(request.getVolume().replace(",", ".")));
                        } else {
                            newDocument.setVolume(new BigDecimal("0"));
                        }
                        //единица измерения веса товара
                        if (request.getWeight_edizm_id() != null) {
                            SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getWeight_edizm_id());
                            newDocument.setWeight_edizm(ed);
                        }
                        //единица измерения объёма товара
                        if (request.getVolume_edizm_id() != null) {
                            SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getVolume_edizm_id());
                            newDocument.setVolume_edizm(ed);
                        }
//                        //маркированный товар (Boolean)
//                        newDocument.setMarkable(request.isMarkable());
//                        //группа маркированных товаров
//                        if (request.getMarkable_group_id() != null) {
//                            SpravSysMarkableGroup ed = emgr.find(SpravSysMarkableGroup.class, request.getMarkable_group_id());
//                            newDocument.setMarkable_group(ed);
//                        }
                        //не закупаемый товар (Boolean)
                        newDocument.setNot_buy(request.isNot_buy());
                        //неделимый товар (Boolean)
                        newDocument.setIndivisible(request.isIndivisible());
                        //товар снят с продажи (Boolean)
                        newDocument.setNot_sell(request.isNot_sell());
                        //Set as Out-of-stock after first completed shipment. Default is false.
                        newDocument.setOutofstock_aftersale(request.getOutofstock_aftersale());
                        // Label description
                        newDocument.setLabel_description(request.getLabel_description());

                        entityManager.persist(newDocument);
                        entityManager.flush();
                        return newDocument.getId();
                }
            } catch (Exception e) {
                logger.error("Exception in method insertProduct.", e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    public boolean isThereProductWithSameSku(String sku, Long productId, Long companyId){
        String stringQuery="select count(*) from products where company_id = "+companyId+" and article = :sku and article !=''";
        if(productId>0L) // on update
            stringQuery = stringQuery + " and id != " + productId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("sku",sku);
            return ((BigInteger)query.getSingleResult()).compareTo(new BigInteger("0"))>0;
        } catch (Exception e) {
            logger.error("Exception in method isThereProductWithSameSku. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteProducts(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"165") && securityRepositoryJPA.isItAllMyMastersDocuments("products",delNumbers)) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
           (securityRepositoryJPA.userHasPermissions_OR(14L,"166") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;




            stringQuery =

                    "update products p" +
                        " set changer_id="+ myId + ", " + // кто изменил (удалил)
                        " date_time_changed = now(), " +//дату и время изменения
                        " is_deleted=true " +
                        " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +"); " +

                    " update stores_products " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        " product_id in (" +
                        "   select variation_product_id from product_variations where " +
                        "   product_id           in (" + delNumbers.replaceAll("[^0-9\\,]", "") +") or " +
                        "   variation_product_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")" +
                        " );" +

                    " update stores_products " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        " product_id in (" +
                        "   select product_id from product_variations where " +
                        "   variation_product_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")" +
                        " );"+

                    " delete from product_variations where " +
                        " product_id           in (" + delNumbers.replaceAll("[^0-9\\,]", "") +") or " +
                        " variation_product_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +");" +

                    " delete from product_upsell where " +
                        " product_id           in (" + delNumbers.replaceAll("[^0-9\\,]", "") +") or " +
                        " child_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +");" +

                    " delete from product_crosssell where " +
                        " product_id           in (" + delNumbers.replaceAll("[^0-9\\,]", "") +") or " +
                        " child_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +");";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                Long myMasterId = userRepositoryJPA.getMyMasterId();

                //if if in deleted products there are variations - mark its parent products to resync
                List<Long> productsIds = Arrays.asList(delNumbers.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                for(Long i: productsIds){
                    if(isProductUsedAsVariation(i))
                        markVariableProductAsNeedToSyncWoo(i, myMasterId);
                }
                return 1;
            }catch (Exception e) {
                logger.error("Exception in method deleteProducts. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteProducts(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"165") && securityRepositoryJPA.isItAllMyMastersDocuments("products",delNumbers)) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(14L,"166") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",delNumbers)))
        {
            //plan limit check
            Long masterId =  userRepositoryJPA.getMyMasterId();
            long amountToRepair = delNumbers.split(",").length;
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(masterId))) // if plan with limits - checking limits
                if((userRepositoryJPA.getMyConsumedResources().getProducts()+amountToRepair)>userRepositoryJPA.getMyMaxAllowedResources().getProducts())
                    return -120; // number of users is out of bounds of tariff plan
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery =
                    " update products p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +"); "+

                    " update stores_products " +
                    " set need_to_syncwoo = true " +
                    " where " +
                    " product_id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +"); ";

            ;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteProducts. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    public List<ProductHistoryJSON> getProductHistoryTable(Long companyId, Long departmentId, Long productId, String dateFrom, String dateTo, String sortColumn, String sortAsc, int result, List<Long> docTypesIds, int offsetreal) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))// Просмотр по (всем,своим) предприятиям
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String suffix = userRepositoryJPA.getMySuffix();

            stringQuery = "select  " +
                    "           p.id as id, " +
                    "           dep.name as department," +
                    "           to_char(p.date_time_created at time zone '" + myTimeZone + "', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           doc.doc_name_"+suffix+" as docName," +
                    "           p.doc_id as docId," +
                    "           p.doc_type_id as docTypeId," +
                    "           (select sum(change) from product_history where master_id=" + myMasterId + " and  department_id = p.department_id and is_completed = true and product_id=" + productId +"  and date_time_created<=p.date_time_created) as quantity," +
                    "           p.change as change," +
                    "           p.price as  price," +   // цена единицы в операции
                    "           p.netcost as netcost," + // себестоимость единицы в операции
                    "           0.00 as avg_netcost_price, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           dep.id as department_id, "+
                    "           doc.page_name as page_name " +
                    "           from product_history p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN departments dep ON p.department_id=dep.id " +
                    "           INNER JOIN documents doc ON p.doc_type_id=doc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and  p.doc_type_id in ("+commonUtilites.ListOfLongToString(docTypesIds,",","","")+")" +
                    (departmentId!=0?(" and  p.department_id = "+departmentId):"") +
                    "           and  p.product_id = " + productId +
                    "           and is_completed = true "  +
                    "           and p.date_time_created at time zone '" + myTimeZone + "' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')" +
                    "           and p.date_time_created at time zone '" + myTimeZone + "' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            { //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by date_time_created_sort " + sortAsc;
//                stringQuery = stringQuery + " order by p.id desc";

            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                query.setParameter("dateFrom",dateFrom);
                query.setParameter("dateTo",dateTo);

                List<Object[]> queryList = query.getResultList();
                List<ProductHistoryJSON> returnList = new ArrayList<>();
                // загружаем настройки, чтобы узнать политику предприятия по подсчёту себестоимости (по всему предприятию или по каждому отделению отдельно)
                String netcostPolicy = commonUtilites.getCompanySettings(companyId).getNetcost_policy();

                boolean isMaterial = isProductMaterial(productId);
                BigDecimal quantity;
                BigDecimal change;
                BigDecimal price;
                BigDecimal netcost;
                for (Object[] obj : queryList) {
                    ProductHistoryJSON doc = new ProductHistoryJSON();

                    quantity=(BigDecimal) obj[6];
                    change  =(BigDecimal) obj[7];
                    price   =(BigDecimal) obj[8];
                    netcost =(BigDecimal) obj[9];

                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setDepartment((String) obj[1]);
                    doc.setDate_time_created((String) obj[2]);
                    doc.setDocName((String) obj[3]);
                    doc.setDocId(Long.parseLong(obj[4].toString()));
                    doc.setDocTypeId((Integer) obj[5]);
                    doc.setPage_name((String) obj[13]);
                    doc.setQuantity(quantity);
                    doc.setChange(change);
                    doc.setPrice(price);
                    doc.setNetcost(netcost);
                    doc.setAvg_netcost_price(isMaterial?(recountProductNetcost(companyId, netcostPolicy.equals("each") ? Long.parseLong(obj[12].toString()) : null, productId, (Timestamp) obj[11])):new BigDecimal(0));
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                logger.error("Exception in method ProductsRepositoryJPA/getProductHistoryTable . SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }


    @Transactional
    public Products getProduct(Long Id) {
        EntityManager em = emf.createEntityManager();
        Products response = em.find(Products.class, Id);
        em.close();
        return response;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean copyProducts(UniversalForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "163,164"))//  Группы товаров : "Создание"
        {
            int numCopies = request.getId1() > 4L ? 4 : (request.getId1().intValue());  // количество копий. Проверка на случай если пошлют более 5
            try {
                for (int i = 0; i < numCopies; i++) { //цикл по заданному количеству копий.
                    if (!copyProduct(request, i + 1)) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                logger.error("Exception in method copyProducts.", e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean copyProduct(UniversalForm request, int countCopy) {
        Long origProductId = request.getId();   // id товара
        Long copyBarcode = request.getId4();  // штрих-код: 1-оставить пустым, 2-как в оригинале
        Products response = getProduct(origProductId);
        response.setProductCategories(getCategoriesSetByProductId(origProductId));
        try {
            Long newProductId = copyProducts_createBaseDocument(response, request, countCopy);
            //копирование картинок
            Set<Long> imagesIds = getProductsImagesSetIdsByProductId(origProductId);
            if (imagesIds.size() > 0) {//если есть картинки
                UniversalForm universalForm = new UniversalForm();
                universalForm.setId1(newProductId);
                universalForm.setSetOfLongs1(imagesIds);
                addFilesToProduct(universalForm);
            }
            //копирование поставщиков
            List<ProductCagentsJSON> listOfProductCagents = getListOfProductCagents(origProductId);
            for (ProductCagentsJSON val : listOfProductCagents) {
                addCagentToProduct(val, newProductId);
            }
            //копирование штрих-кодов
            if (copyBarcode == 2L) {
                List<ProductBarcodesJSON> listOfProductBarcodes = getListOfProductBarcodes(origProductId);
                for (ProductBarcodesJSON val : listOfProductBarcodes) {
                    addBarcodeToProduct(val, newProductId);
                }
            }
            //копирование доп. полей
//            List<Object[]> listOfProductFields = getListOfProductFields(origProductId);
//            for (Object[] val : listOfProductFields) {
//                //            Long product_id,       Long field_id,                    String value
//                createCustomField(newProductId, Long.parseLong(String.valueOf(val[1])), String.valueOf(val[2]));
//            }
        } catch (Exception e) {
            logger.error("Exception in method copyProduct.", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
//СОздание базового документа (поля+категории). Остальное (поставщики, штрихкоды, картинки) прицепляется после его создания в copyProducts
    @Transactional//первым параметром передаём сам объект документа с категориями, вторым - опции копирования
    public Long copyProducts_createBaseDocument(Products response, UniversalForm request, int countCopy) {
        Long copyArticle = request.getId2();  // артикул: 1-копировать, 2-не копировать
        Long copyCode = request.getId3();  // код: 1-оставить пустым, 2-как в оригинале, 3-присвоить новый
        try {
            //СОЗДАЛИ НОВЫЙ ДОКУМЕНТ Товары и услуги (НО ПОКА БЕЗ СЕТОВ И ПОЛЕЙ)
            Products newDocument = new Products();
            //создатель
            User creator = userRepository.getUserByUsername(userRepository.getUserName());
            newDocument.setCreator(creator);//создателя
            //владелец
            User master = userRepository.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository.getUserName())));
            newDocument.setMaster(master);
            //дата и время создания
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            newDocument.setDate_time_created(timestamp);//
            //предприятие
            newDocument.setCompany(response.getCompany());
            //Наименование
            newDocument.setName(response.getName() == null ? "" : response.getName() + " (copy " + countCopy + ")");
            //дополнительная информация
            newDocument.setDescription(response.getDescription());
            //признак предмета расчёта
            newDocument.setPpr(response.getPpr());
            //НДС
            newDocument.setNds(response.getNds());
            //артикул
            if (copyArticle == 1L) {
                newDocument.setArticle(response.getArticle());
            } else newDocument.setArticle(null);
            //код// код: 1-присвоить новый, 2-как в оригинале, 3-оставить пустым
            if (copyCode == 3) {
                newDocument.setProduct_code_free(null);
            } else if (copyCode == 1) {
                newDocument.setProduct_code_free(generateFreeProductCode(response.getCompany().getCompId()));
            } else
                newDocument.setProduct_code_free(response.getProduct_code_free());
            //группа товаров
            newDocument.setProductGroup(response.getProductGroup());
            //единица измерения
            newDocument.setEdizm(response.getEdizm());
            //весовой товар
            newDocument.setBy_weight(response.getBy_weight());
            //весовой код
            newDocument.setProduct_code(response.getProduct_code());
            //маркированный товар
            newDocument.setMarkable(response.getMarkable());
            //группа маркированных товаров
            newDocument.setMarkable_group(response.getMarkable_group());
            //вес
            newDocument.setWeight(response.getWeight());
            //ед. изм. веса
            newDocument.setWeight_edizm(response.getWeight_edizm());
            //объём
            newDocument.setVolume(response.getVolume());
            //ед. изм. объёма
            newDocument.setVolume_edizm(response.getVolume_edizm());
            //товар не закупается
            newDocument.setNot_buy(response.getNot_buy());
            //товар снят с продажи
            newDocument.setNot_sell(response.getNot_sell());
            //неделимый товар
            newDocument.setIndivisible(response.getIndivisible());

            newDocument.setProductCategories(response.getProductCategories());

            newDocument.setImages(response.getImages());

            entityManager.persist(newDocument);

            entityManager.flush();

            return newDocument.getId();//временно

        } catch (Exception e) {
            logger.error("Exception in method copyProducts_createBaseDocument.", e);
            e.printStackTrace();
            return 0L;
        }
    }

    // возвращает доступное количество товара в отделении (складе), исключая позиции, добавленные в документ "Заказ клиента" с id document_id
    public BigDecimal getAvailableExceptMyDoc(Long product_id, Long department_id, Long document_id){
        // всего единиц товара в отделении (складе):
        String stringQuery = " select (select coalesce(quantity,0) from product_quantity where department_id = "+ department_id +" and product_id = "+product_id+") as total, " +
                " (select " +
                "   sum(coalesce(reserved_current,0)) " +
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id="+product_id+
                "   and department_id =" + department_id +
                "   and customers_orders_id!="+document_id+") as reserved ";//зарезервировано в других документах Заказ покупателя
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            ProductsPriceAndRemainsJSON res = new ProductsPriceAndRemainsJSON();

            for (Object[] obj : queryList) {
                res.setTotal(                                         obj[0]==null?BigDecimal.ZERO:(BigDecimal)obj[0]);
                res.setReserved(                                      obj[1]==null?BigDecimal.ZERO:(BigDecimal)obj[1]);
            }
            return res.getTotal().subtract(res.getReserved());
        } catch (Exception e) {
            logger.error("Exception in method getAvailableExceptMyDoc. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    public void setProductAsOutOfStockIfOutofstockAftersale(Long productId, Long companyId, Long masterId){
        String stringQuery=
                " update products set " +
                " not_sell=true, " +
                " stock_status='outofstock'" +
                " where " +
                " master_id="+masterId+" and " +
                " company_id="+companyId+" and " +
                " id="+productId+" and " +
                " coalesce(outofstock_aftersale,false) = true";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method isOutofstockAftersale. SQL query:" + stringQuery, e);
            e.printStackTrace();
        }
    }
    public boolean isOutofstockAftersale(Long product_id){
        String stringQuery="select coalesce(outofstock_aftersale,false) from products where id="+product_id;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (Boolean) query.getSingleResult();
        } catch (Exception e) {
            logger.error("Exception in method isOutofstockAftersale. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    public boolean isThereServicesInProductsList(Set<Long> productIds){
        String stringQuery="select count(*) from products where ppr_id = 4 and id in "+commonUtilites.SetOfLongToString(productIds,",","(",")");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).compareTo(new BigInteger("0"))>0;
        } catch (Exception e) {
            logger.error("Exception in method isThereServicesInProductsList. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    // возвращает доступное количество товара в отделениях
    public BigDecimal getAvailable(Long product_id, List<Long> departmentsIds, boolean roundToInteger){
        if(departmentsIds.size()==0) return new BigDecimal("0");
        // всего единиц товара в отделении (складе):
        String stringQuery = " select " +
                "  (select sum(coalesce(quantity,0)) from product_quantity where " +
                "  department_id in "+ commonUtilites.ListOfLongToString(departmentsIds,",","(",")") +" and product_id = "+product_id+") as total, " +
                "  (select sum(coalesce(reserved_current,0)) " +
                "   from customers_orders_product " +
                "   where product_id="+product_id+
                "   and department_id in "+ commonUtilites.ListOfLongToString(departmentsIds,",","(",")") +" and product_id = "+product_id+") as reserved ";//зарезервировано в других документах Заказ покупателя
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> ql = query.getResultList();
            BigDecimal total = (ql.get(0)[0]==null?BigDecimal.ZERO:(BigDecimal)ql.get(0)[0]);
            BigDecimal reserved = (ql.get(0)[1]==null?BigDecimal.ZERO:(BigDecimal)ql.get(0)[1]);
            // на всякий случай проверяем что разница "Всего" и "В резерве" не отрицательная (если так то возвращаем 0)
            // вообще такого не должно быть, но "случаи разные бывают"
            if(roundToInteger)
                return (total.subtract(reserved).compareTo(new BigDecimal("0"))<0?(new BigDecimal("0")):total.subtract(reserved)).setScale(0, RoundingMode.UP);
            else
                return total.subtract(reserved).compareTo(new BigDecimal("0"))<0?(new BigDecimal("0")):total.subtract(reserved);
        } catch (Exception e) {
            logger.error("Exception in method getAvailable. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    // возвращает отгруженное через Отгрузки и проданное через Розн. продажи кол-во товара в отделении у Заказа покупателя
    public BigDecimal getShippedAndSold(Long product_id, Long department_id, Long document_id){
        String stringQuery = " select " +
                "coalesce(" +//отгружено через Отгрузки у Заказа покупателя
                "   (select sum(product_count) " +
                "   from shipment_product " +
                "   where " +
                "   shipment_id in " +
                "   (" +
                "       select id " +
                "       from shipment " +
                "       where " +
                "       customers_orders_id="+document_id+" and " +
                "       coalesce(is_deleted,false)=false and " +
                "       coalesce(is_completed,false)=true" +
                "   ) and " +
                "   department_id = "+ department_id +" and " +
                "   product_id = "+product_id+")" +
                ",0) + " +

                "coalesce(" +// продано через Розничные продажи у Заказа покупателя
                "   (select sum(product_count) " +
                "   from retail_sales_product " +
                "   where " +
                "   retail_sales_id in " +
                "   (" +
                "       select id " +
                "       from retail_sales " +
                "       where " +
                "       customers_orders_id="+document_id+" and " +
                "       coalesce(is_deleted,false)=false and " +
                "       coalesce(is_completed,false)=true" +
                "   ) and " +
                "   department_id = "+ department_id +" and " +
                "   product_id = "+product_id+")" +
                ",0)";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (BigDecimal) query.getSingleResult();
        } catch (Exception e) {
            logger.error("Exception in method getShippedAndSold. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // тут не надо прописывать права, т.к. это сервисный запрос
    @SuppressWarnings("Duplicates")
    public List getProductsList(String searchString, Long companyId, Long departmentId, Long document_id, Long priceTypeId, boolean showRemovedFromSale, boolean showNotPurchased, boolean showServices) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myDepthsIds = userRepositoryJPA.getMyDepartmentsId().toString().replace("[","").replace("]","");
        stringQuery = "select  p.id as id, " +
                "           p.name as name, " +
                "           p.nds_id as nds_id, " +
                "           coalesce(p.edizm_id,0) as edizm_id, " +
                "           f.name as filename, " +

                // всего единиц товара в отделении (складе):
                " (select coalesce(quantity,0) from product_quantity where department_id = "+ departmentId +" and product_id = p.id) as total, " +

                // зарезервировано единиц товара в отделении (складе) в других Заказах покупателя:
                "(select " +
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id=p.id"+
                "   and department_id=" + departmentId +
                "   and customers_orders_id!="+document_id+") as reserved, "+//зарезервировано в этом отделении в других Заказах покупателя

                //всего единиц товара во всех моих отделениях (складах)
                " (select sum(coalesce(quantity,0)) from product_quantity where department_id in ("+ myDepthsIds +") and product_id = p.id) as total_in_all_my_depths, " +

                //всего зарезервировано единиц товара во всех моих отделениях (складах)
                "(select " +
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id=p.id"+
                "   and department_id in (" + myDepthsIds + ") " +
                "   and customers_orders_id!="+document_id+") as reserved_in_all_my_depths, " +

                " (select name_api_atol from sprav_sys_ppr where id=p.ppr_id) as ppr_name_api_atol, " +
                " (select is_material from sprav_sys_ppr where id=p.ppr_id) as is_material," +

                // зарезервировано единиц товара в отделении (складе) в ЭТОМ Заказе покупателя:
                "(select " +
                "   coalesce(reserved_current,0)" +
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id=p.id"+
                "   and department_id=" + departmentId +
                "   and customers_orders_id="+document_id+") as reserved_current, "+
                " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                // цена по запрашиваемому типу цены (будет 0 если такой типа цены у товара не назначен)
                " coalesce((select pp.price_value from product_prices pp where pp.product_id=p.id and pp.price_type_id = "+priceTypeId+"),0) as price_by_typeprice, " +
                // средняя себестоимость
                " (select ph.avg_netcost_price   from product_quantity ph where ph.department_id = "  + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgCostPrice, " +
                // средняя закупочная цена - deprecated and not used
                " 0.00  as avgPurchasePrice, " +
                // последняя закупочная цена                                                                15 = Acceptance
                " (select ph.price from product_history ph  where ph.department_id = " + departmentId +" and doc_type_id=15 and ph.product_id = p.id and ph.is_completed=true order by ph.date_time_created desc limit 1) as lastPurchasePrice " +

                " from products p " +
                " left outer join product_barcodes pb on pb.product_id=p.id" +
                " left outer join files f on f.id=(select file_id from product_files where product_id=p.id and output_order=1 limit 1)" +
                " where  p.master_id=" + myMasterId +
                " and coalesce(p.is_deleted,false) = false " +
                " and coalesce(p.type, 'simple') != 'variable'" +
                (!showRemovedFromSale?" and coalesce(p.not_sell,false) = false ":" ") +
                (!showNotPurchased?" and coalesce(p.not_buy,false) = false ":" ") +
                (!showServices?" and p.ppr_id != 4":" ")  // if  showServices=false
        ;
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                    " upper(p.article) like upper (CONCAT('%',:sg,'%')) or " +
                    " to_char(p.product_code_free,'fm0000000000') = :sg or " +
                    " pb.value = :sg" +
                    ")";

        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id,f.name  order by p.name asc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}


            List<Object[]> queryList = query.getResultList();
            List<ProductsListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductsListJSON product = new ProductsListJSON();
                product.setId(Long.parseLong(                               obj[0].toString()));
                product.setProduct_id(Long.parseLong(                       obj[0].toString()));
                product.setName((String)                                    obj[1]);
                product.setNds_id((Integer)                                 obj[2]);
                product.setEdizm_id(Long.parseLong(                         obj[3].toString()));
                product.setFilename((String)                                obj[4]);
                product.setTotal(                                           obj[5]==null?BigDecimal.ZERO:(BigDecimal)obj[5]);
                product.setReserved(                                        obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                product.setTotal_in_all_my_depths(                          obj[7]==null?BigDecimal.ZERO:(BigDecimal)obj[7]);
                product.setReserved_in_all_my_depths(                       obj[8]==null?BigDecimal.ZERO:(BigDecimal)obj[8]);
                product.setPpr_name_api_atol((String)                       obj[9]);
                product.setIs_material((Boolean)                            obj[10]);
                product.setReserved_current(                                obj[11]==null?BigDecimal.ZERO:(BigDecimal)obj[11]);
                product.setIndivisible((Boolean)                            obj[12]);
                product.setEdizm((String)                                   obj[13]);
                product.setPriceOfTypePrice(                                obj[14]==null?BigDecimal.ZERO:(BigDecimal)obj[14]);
                product.setAvgCostPrice(                                    obj[15]==null?BigDecimal.ZERO:(BigDecimal)obj[15]);
                product.setAvgPurchasePrice(                                obj[16]==null?BigDecimal.ZERO:(BigDecimal)obj[16]);
                product.setLastPurchasePrice(                               obj[17]==null?BigDecimal.ZERO:(BigDecimal)obj[17]);
                returnList.add(product);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getProductsList. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }

    @SuppressWarnings("Duplicates")
    //отдает информацию цене товара
    public BigDecimal getProductPrice(Long company_id, Long product_id, Long price_type_id) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery =
                "       select " +
                        "           price_value as price" +
                        "       from " +
                        "           product_prices " +
                        "       where " +
                        "               product_id = "+product_id +
                        "           and price_type_id = "+price_type_id +
                        "           and company_id=" + company_id +
                        "           and master_id=" + myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (BigDecimal) query.getSingleResult();
        } catch (Exception e) {
            logger.error("Exception in method getProductPrice. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //отдает информацию количестве резервов товара на складе
    public BigDecimal getProductReserves(Long departmentId, Long productId) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery =
                "select " +
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                        "   from " +
                        "   customers_orders_product " +
                        "   where " +
                        "   product_id = " + productId +
                        "   and department_id=" + departmentId +
                        "   and master_id=" + myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            BigDecimal result = (BigDecimal) query.getSingleResult();
            if(Objects.isNull(result)) {
                return new BigDecimal(0);// т.к. запрос может возвращать null в случаях, если товара еще не было в заказах покупателей, вместо null вернем 0
            } else {
                return result;
            }
        } catch (Exception e) {
            logger.error("Exception in method getProductReserves (ver.1). SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")  // возвращает кол-во резервов товара в отделении (и если нужно - по определенному заказу покупателя)
    public BigDecimal getProductReserves(Long departmentId, Long productId,  Long customersOrdersId)
    {
        String stringQuery = " select " +
                "sum(" +
                "   coalesce(reserved_current,0))  " +
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id="+productId+
                "   and department_id =" + departmentId +
                (customersOrdersId>0L?("   and customers_orders_id = " + customersOrdersId):"");
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<BigDecimal> queryList = query.getResultList();
            ProductHistoryJSON returnObj=new ProductHistoryJSON();
            if(queryList.size()==0){//если записей истории по данному товару ещё нет
                returnObj.setQuantity(                  (new BigDecimal(0)));
            }else {
                for (BigDecimal obj : queryList) {
                    returnObj.setQuantity(obj);
                }
            }
            return returnObj.getQuantity();
        }
        catch(NoResultException nre){return new BigDecimal(0);}
        catch (Exception e) {
            logger.error("Exception in method getProductReserves (ver.2). SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public Boolean updateProductReserves(Long departmentId, Long productId, Long customersOrdersId, BigDecimal reserves){
        String stringQuery = "update customers_orders_product " +
                "   set reserved_current = " + reserves +
                "   where " +
                "   product_id="+productId+
                "   and department_id =" + departmentId +
                "   and customers_orders_id = " + customersOrdersId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method updateProductReserves. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // возвращает все типы цен (названия, id) с их значениеми для товара с id = productId
    @SuppressWarnings("Duplicates")
    public List<ProductPricesJSON> getProductPrices(Long productId){

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        Long myCompanyId= userRepositoryJPA.getMyCompanyId_();
        String stringQuery;
        stringQuery=
                "select "+
                        " p.id                                  as price_type_id, " +
                        " p.name                                as price_name, " +
                        " p.description                         as price_description, " +

                        " coalesce(" +
                        "(select coalesce(price_value,null) " +
                        "from product_prices " +
                        "where " +
                        "product_id="+productId+" and " +
                        "price_type_id=p.id) " +
                        ",null)                                as price_value " +
                        //"(select count(*) from companies where is_store=true and store_price_type_regular = p.id) > 0 as is_store_price_type_regular," +
                        //"(select count(*) from companies where is_store=true and store_price_type_sale = p.id) > 0 as is_store_price_type_sale" +

                        " from " +
                        " sprav_type_prices p, products prod " +
                        " where " +

                        " p.master_id = " + myMasterId +
                        " and prod.id="+productId+
                        " and p.company_id=prod.company_id"+
                        " and coalesce(p.is_deleted,false) = false " +
                        " order by p.name asc ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            List<ProductPricesJSON> returnList = new ArrayList<>();

            for(Object[] obj:queryList){
                ProductPricesJSON doc=new ProductPricesJSON();
                doc.setPrice_type_id(Long.parseLong(                    obj[0].toString()));
                doc.setPrice_name((String)                              obj[1]);
                doc.setPrice_description((String)                       obj[2]);
                doc.setPrice_value((BigDecimal)                         obj[3]);
//                doc.setIs_store_price_type_regular((Boolean)            obj[4]);
//                doc.setIs_store_price_type_sale((Boolean)               obj[5]);
                returnList.add(doc);
            }
            return returnList;

        }
        catch (Exception e) {
            logger.error("Exception in method getProductPrices. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }

//*****************************************************************************************************************************************************
//***********************************************   C A T E G O R I E S   *****************************************************************************
//*****************************************************************************************************************************************************

    // return information about product category by its ID
    public ProductCategoryJSON getProductCategory(Long categoryId) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery =
                " select " +
                        " name as name," +
                        " coalesce(description,'') as description," +
                        " coalesce(display, 'default') as display," +
                        " coalesce(slug,'') as slug," +
                        " image_id as image_id," +
                        " coalesce(parent_id, 0) as parent_id," +
                        " coalesce(is_store_category, false) as is_store_category" +
                        " from product_categories " +
                        " where master_id = " + myMasterId + " and id = " + categoryId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            ProductCategoryJSON doc = new ProductCategoryJSON();
            if (queryList.size() > 0) {
                doc.setId(categoryId);
                doc.setName((String)                                queryList.get(0)[0]);
                doc.setDescription((String)                         queryList.get(0)[1]);
                doc.setDisplay((String)                             queryList.get(0)[2]);
                doc.setSlug((String)                                queryList.get(0)[3]);
                doc.setImage(Objects.isNull(queryList.get(0)[4])?null:fileRepository.getImageFileInfo(((BigInteger) queryList.get(0)[4]).longValue()));
                doc.setParentCategoryId(Long.parseLong(             queryList.get(0)[5].toString()));
                doc.setIsStoreCategory((Boolean)                    queryList.get(0)[6]);
                doc.setStoresIds(getCategoryStoresIds(categoryId, myMasterId));
            }
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductCategory. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private List<Long> getCategoryStoresIds(Long categoryId, Long masterId){
        String stringQuery = "  select   csd.store_id as id" +
                "               from     stores_productcategories csd " +
                "               where    csd.master_id=" + masterId +
                "               and      csd.category_id =" + categoryId;
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
            logger.error("Exception in method getCategoryStoresIds. SQL query:" + stringQuery, e);
            return null;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    @SuppressWarnings("Duplicates")
    public Long insertProductCategory(ProductCategoryJSON request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "171,172"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId = companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean notMyCompany = !((Long.valueOf(myCompanyId)).equals(request.getCompanyId()));
            boolean canCreateAllCompanies = securityRepositoryJPA.userHasPermissions_OR(14L, "171");
            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!canCreateAllCompanies && notMyCompany) || !DocumentMasterId.equals(myMasterId)) {
                return null;
            } else {
                String stringQuery;
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();

                Long myId = userRepository.getUserId();
                stringQuery = "insert into product_categories (" +
                        "name," +
                        "slug," +
                        "description," +
                        "display," +
                        "image_id," +
                        "master_id," +
                        "creator_id," +
                        "parent_id," +
                        "company_id," +
                        "date_time_created," +
                        "is_store_category," +
                        "output_order" +
                        ") values ( " +
                        ":name, " +
                        ":slug," +
                        ":description," +
                        ":display, " +
                        (Objects.isNull(request.getImage())?null:request.getImage().getId()) + ", " +
                        myMasterId + "," +
                        myId + "," +
                        (request.getParentCategoryId() > 0 ? request.getParentCategoryId() : null) + ", " +
                        request.getCompanyId() + ", " +
                        "(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')), " +
                        request.getIsStoreCategory() + ", " +
                        "(select coalesce(max(output_order)+1,1) from product_categories where company_id = " + request.getCompanyId() + " and parent_id " + (request.getParentCategoryId() > 0 ?(" = " + request.getParentCategoryId() ): " is null") + ")" +
                        ")";
                try {
                    commonUtilites.idBelongsMyMaster("product_categories",(request.getParentCategoryId()>0?request.getParentCategoryId():null),myMasterId);
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("name", request.getName());
                    query.setParameter("description", request.getDescription());
                    query.setParameter("display", request.getDisplay());
                    query.setParameter("slug", request.getSlug());
                    if (query.executeUpdate() == 1) {
                        stringQuery = "select id from product_categories where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id=" + myId;
                        Query query2 = entityManager.createNativeQuery(stringQuery);
                        Long newCategoryId = Long.valueOf(query2.getSingleResult().toString());
                        // If category belongs to online store(s) - saving store translations
                        if (request.getIsStoreCategory() && !Objects.isNull(request.getStoreCategoryTranslations()) && request.getStoreCategoryTranslations().size() > 0) {
                            Long parentCategoryId = getParentCategoryId(newCategoryId);
                            for (StoreTranslationCategoryJSON row : request.getStoreCategoryTranslations()) {
                                checkTranslatedCategoryNameUniqueOnSameLevel(parentCategoryId, newCategoryId, request.getCompanyId(), row.getLangCode(),row.getName(), row.getSlug());
                                saveStoreCategoryTranslations(row, myMasterId, request.getCompanyId(), newCategoryId);
                            }
                        }
                        //Saving list of online stores that category belongs to
                        if (request.getIsStoreCategory() && !Objects.isNull(request.getStoresIds()) && request.getStoresIds().size() > 0) {
                            for (Long storeId : request.getStoresIds()) {
                                commonUtilites.idBelongsMyMaster("stores",storeId,myMasterId);
                                saveCategoryStore(newCategoryId, storeId, myMasterId, request.getCompanyId());
                            }
                            // Now need to mark all products of this category as need to be synchronized
                            markProductsOfCategoriesAndStoresAsNeedToSyncWoo(new HashSet<>(Arrays.asList(newCategoryId)) , myMasterId, request.getStoresIds());

                            markAllCategoriesAsNeedToSyncWoo(request.getCompanyId(),myMasterId);
                        }

                        return newCategoryId;
                    } else return (0L);
                } catch (TranslatedCategoryNameIsNotUniqueOnSameLevel e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                    return -209L; // см. _ErrorCodes
                } catch (TranslatedCategorySlugIsNotUnique e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                    return -208L; // см. _ErrorCodes
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
                        if(message.contains("product_categories_name_nn_uq")) { // unique category names on the same level FOR ROOT CATEGORIES
                            logger.error("ConstraintViolationException (product_categories_name_nn_uq) in method ProductRepositoryJPA/insertProductCategory.", e);
                            return -210L; // см. _ErrorCodes
                        } else if (message.contains("product_categories_name_uq")){ // unique category names on the same level FOR SUBCATEGORIES
                            logger.error("ConstraintViolationException (product_categories_name_uq) in method ProductRepositoryJPA/insertProductCategory.", e);
                            return -210L; // см. _ErrorCodes
                        } else if(message.contains("product_categories_slug_uq")){
                            logger.error("ConstraintViolationException (product_categories_slug_uq) in method ProductRepositoryJPA/insertProductCategory.", e);
                            return -207L; // см. _ErrorCodes
                        } else {
                            logger.error("Exception in method insertProductCategory. SQL query:" + stringQuery, e);
                            e.printStackTrace();
                            return null;
                        }
                    } else {
                        logger.error("Exception in method insertProductCategory. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } else return -1L;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    @SuppressWarnings("Duplicates")
    public Integer updateProductCategory(ProductCategoryJSON request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))//  "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long changer = userRepository.getUserIdByUsername(userRepository.getUserName());
            boolean markAllCategoriesAsNeedToSyncWoo = false;

            String stringQuery;
            stringQuery = "update product_categories set " +
                    " name = :name, " +
                    " slug = :slug," +
                    " description = :description," +
                    " display = :display," +
                    " parent_id = "+(request.getParentCategoryId()==0L?null:request.getParentCategoryId())+"," +
                    " image_id = " + (Objects.isNull(request.getImage())?null:request.getImage().getId()) + ", " +
                    " date_time_changed = now()," +
                    " changer_id = " + changer + ", " +
                    " is_store_category = " + request.getIsStoreCategory() +
                    " where id = " + request.getId() +
                    " and master_id = " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав по всем предприятиям
            {
//            остается только на своё предприятие (140)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try {

                commonUtilites.idBelongsMyMaster("product_categories", request.getId(),myMasterId);
                commonUtilites.idBelongsMyMaster("product_categories",(request.getParentCategoryId()>0?request.getParentCategoryId():null),myMasterId);
                commonUtilites.idBelongsMyMaster("files",(Objects.isNull(request.getImage())?null:request.getImage().getId()),myMasterId);
                commonUtilites.idBelongsMyMaster("companies", request.getCompanyId(), myMasterId);

                boolean isStoreCategory = isStoreCategory(request.getId()); // is it store category on this moment

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", request.getName());
                query.setParameter("slug", (request.getSlug().trim().equals("") ? null : request.getSlug()));
                query.setParameter("description", request.getDescription());
                query.setParameter("display", request.getDisplay());

                if(request.getStoresIds().size()>0)
                    for(Long storeId:request.getStoresIds()){
                        commonUtilites.idBelongsMyMaster("stores",storeId,myMasterId);
                    }


                // if it wasn't a store category but now it will be, and it has a stores - we need to mark all products of this category as need to be synchronized
                // !!! category is online-store category if it marked as "is_store_category" and it has selected internet stores !!!
                if (!isStoreCategory && request.getIsStoreCategory() && request.getStoresIds().size()>0)
                    markProductsOfCategoriesAndStoresAsNeedToSyncWoo(new HashSet<>(Arrays.asList(request.getId())), myMasterId, request.getStoresIds());

                // if it was a store category, or it is a store category, or it'll be a store category
                if (isStoreCategory || request.getIsStoreCategory())
                    markAllCategoriesAsNeedToSyncWoo=true;

                //При сохранении категории у неё появился новый магазин
                //When saving a category, it has a new store

                // If it STILL a store category and has a selected internet stores
                // then need to check on the new added stores by comparing an old list of stores with a new list
                if (isStoreCategory && request.getIsStoreCategory() && request.getStoresIds().size()>0) {

                    List<Long> categoryOldStoresIds = new ArrayList<>(getCategoryStoresIds(request.getId()));
                    List<Long> categoryNewStoresIds = new ArrayList<>(request.getStoresIds());
                    categoryNewStoresIds.removeAll(categoryOldStoresIds);
                    if (categoryNewStoresIds.size() > 0) {
                        markProductsOfCategoriesAndStoresAsNeedToSyncWoo(new HashSet<>(Arrays.asList(request.getId())), myMasterId, categoryNewStoresIds);
                        markAllCategoriesAsNeedToSyncWoo=true;
                    }
                }


                query.executeUpdate();



                // if category belongs to online store(s) - saving store translations
                Long parentCategoryId = getParentCategoryId(request.getId());
                if (request.getIsStoreCategory() && !Objects.isNull(request.getStoreCategoryTranslations()) && request.getStoreCategoryTranslations().size() > 0) {
                    for (StoreTranslationCategoryJSON row : request.getStoreCategoryTranslations()) {
                        checkTranslatedCategoryNameUniqueOnSameLevel(parentCategoryId, request.getId(), request.getCompanyId(), row.getLangCode(),row.getName(), row.getSlug());
                        saveStoreCategoryTranslations(row, myMasterId, request.getCompanyId(), request.getId());
                    }
                }
                deleteNonSelectedCategoryStores(request.getStoresIds(), request.getId(), myMasterId);
                if (request.getIsStoreCategory() && !Objects.isNull(request.getStoresIds()) && request.getStoresIds().size() > 0) {
                    for (Long storeId : request.getStoresIds()) {
                        saveCategoryStore(request.getId(), storeId, myMasterId, request.getCompanyId());
                    }
                }

                if(markAllCategoriesAsNeedToSyncWoo)
                    markAllCategoriesAsNeedToSyncWoo(request.getCompanyId(),myMasterId);

                return 1;
            } catch (TranslatedCategoryNameIsNotUniqueOnSameLevel e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                e.printStackTrace();
                logger.error(e.getMessage(), e);
                return -209; // см. _ErrorCodes
            } catch (TranslatedCategorySlugIsNotUnique e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                e.printStackTrace();
                logger.error(e.getMessage(), e);
                return -208; // см. _ErrorCodes
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
                    if(message.contains("product_categories_name_nn_uq")) { // unique category names on the same level FOR ROOT CATEGORIES
                        logger.error("ConstraintViolationException (product_categories_name_nn_uq) in method ProductRepositoryJPA/insertProductCategory.", e);
                        return -210; // см. _ErrorCodes
                    } else if (message.contains("product_categories_name_uq")){ // unique category names on the same level FOR SUBCATEGORIES
                        logger.error("ConstraintViolationException (product_categories_name_uq) in method ProductRepositoryJPA/insertProductCategory.", e);
                        return -210; // см. _ErrorCodes
                    } else if (message.contains("product_categories_slug_uq")){
                        logger.error("ConstraintViolationException (product_categories_name_uq) in method ProductRepositoryJPA/updateProductCategory.", e);
                        return -207; // см. _ErrorCodes
                    } else {
                        logger.error("Exception in method updateProductCategory. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    logger.error("Exception in method updateProductCategory. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1;
    }

    private void saveCategoryStore(Long category_id, Long store_id, Long master_id, Long company_id) throws Exception {
        String stringQuery = "insert into stores_productcategories (" +
                "   master_id," +
                "   company_id," +
                "   store_id," +
                "   category_id " +
                "   ) values (" +
                master_id+", "+
                company_id+", "+
                "(select id from stores where id="+store_id+" and master_id="+master_id+"), "+// +", " +
                "(select id from product_categories where id="+category_id+" and master_id="+master_id+") "+// чтобы не мочь изменить категорию другого master_id, случайно или намеренно
                ") ON CONFLICT ON CONSTRAINT stores_categories_uq " +// "upsert"
                " DO NOTHING";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveCategoryStore. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void markAllCategoriesAsNeedToSyncWoo(Long companyId, Long masterId) throws Exception {

        String stringQuery =
                " update stores_productcategories " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        "       master_id = " + masterId +
                        " and   company_id =" + companyId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markAllCategoriesAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private void saveStoreCategoryTranslations(StoreTranslationCategoryJSON row, Long master_id, Long company_id, Long category_id) throws Exception {
        String stringQuery = "insert into store_translate_categories (" +
                        "   master_id," +
                        "   company_id," +
                        "   lang_code," +
                        "   category_id, " +
                        "   name, " +
                        "   slug, " +
                        "   description " +
                        "   ) values (" +
                        master_id+", "+
                        company_id+", "+
                        ":lang_code," +
                        "(select id from product_categories where id="+category_id+" and master_id="+master_id+"), "+// чтобы не мочь изменить категорию другого master_id, случайно или намеренно
                        ":name," +
                        ":slug," +
                        ":description" +
                        ") ON CONFLICT ON CONSTRAINT category_lang_uq " +// "upsert"
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
            logger.error("Exception in method saveStoreCategoryTranslations. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void deleteNonSelectedCategoryStores(List<Long> storesIds, Long categoryId, Long masterId) throws Exception {
        String stringQuery = "delete from stores_productcategories " +
                " where master_id=" + masterId +
                " and category_id=" + categoryId +
                " and store_id not in " + ((!Objects.isNull(storesIds)&&storesIds.size()>0)?(commonUtilites.ListOfLongToString(storesIds,",","(",")")):"(0)");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
//            if(isStoreCategory(request.getCategoryId()))
//                markProductsofCategoryAsNeedToSyncWoo(request.getCategoryId(), myMasterId);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteNonSelectedCategoryStores. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


    // returns the list of internet-stores of category
    @SuppressWarnings("Duplicates")
    public List<StoresListJSON> getCategoryStoresList(Long categoryId) {

        String stringQuery;
        Long myMasterId = userRepositoryJPA.getMyMasterId();

        stringQuery = "select" +
                "           id as id, " +
                "           name as name " +
                "           from stores " +
                "           where  master_id = " + myMasterId +
                "           and coalesce(is_deleted, false) = false " +
                "           and id in (select store_id from stores_productcategories where category_id="+categoryId+")" +
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
            logger.error("Exception in method getCategoryStoresList. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public boolean deleteProductCategory(ProductCategoriesForm request) {
//        if (securityRepositoryJPA.userHasPermissions_OR(14L, "175,176"))// "Удаление категорий"
//        {
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            String stringQuery;
//            stringQuery = "delete from product_categories " +
//                    " where id=" + request.getCategoryId() +
//                    " and master_id=" + myMasterId;
//            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "175")) //Если нет прав по всем предприятиям
//            {
//                //остается только на своё предприятие
//                int myCompanyId = userRepositoryJPA.getMyCompanyId();
//                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
//            }
//            try {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                if(isStoreCategory(request.getCategoryId()))
//                    markProductsofCategoryAsNeedToSyncWoo(request.getCategoryId(), myMasterId);
//                query.executeUpdate();
//
//                return true;
//            } catch (Exception e) {
//                logger.error("Exception in method deleteProductCategory. SQL query:"+stringQuery, e);
//                e.printStackTrace();
//                return false;
//            }
//        } else return false;
//    }

    @Transactional
    public Integer deleteProductCategories(UniversalForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "175,176"))// "Удаление категорий"
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            stringQuery = "delete from product_categories " +
                    " where id in " + commonUtilites.SetOfLongToString(request.getSetOfLongs1(),",","(",")") +
                    " and master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "175")) //Если нет прав по всем предприятиям
            {
                //остается только на своё предприятие
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try {
                commonUtilites.idBelongsMyMaster("companies",request.getId(),myMasterId);
                Query query = entityManager.createNativeQuery(stringQuery);

                if(isStoreCategories(request.getSetOfLongs1(), myMasterId))
                    markAllCategoriesAsNeedToSyncWoo(request.getId(), myMasterId);

                query.executeUpdate();



                // If category is deleted - do not need to mark products of this category as need to sync woo.
                // The set of product categories on the store side will be automatically corrected by WooCommerce on a stage of categories synchronization.
                // The products that lost all they categories on the CRM side will be deleted in the woo-store on a synchronization stage "getProductWooIdsToDeleteInStore"
                // such product can be: 1) Deleted on the CRM side products OR 2) Products than no more belong to store categories
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductCategories. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    private boolean isStoreCategories(Set<Long> categoriesIds, Long masterId) throws Exception {

        String stringQuery = "select count(*) from product_categories where " +
                " master_id=" + masterId +
                " and id in "+commonUtilites.SetOfLongToString(categoriesIds,",","(",")") +
                " and coalesce(is_store_category, false) = true ";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).longValue() > 0L;
        } catch (Exception e) {
            logger.error("Exception in method areCategoriesHaveStores. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }



    private void clearUpsellCrosssellsFromVariations(Long productId, Long masterId) throws Exception {
        String stringQuery =

                " delete from product_upsell where " +
                        " master_id = " + masterId +
                        " and (product_id in (select variation_product_id from product_variations where product_id = "+productId+") or " +
                        "     child_id in   (select variation_product_id from product_variations where product_id = "+productId+"));" +

                        " delete from product_crosssell where " +
                        " master_id = " + masterId +
                        " and (product_id in (select variation_product_id from product_variations where product_id = "+productId+") or " +
                        "     child_id in   (select variation_product_id from product_variations where product_id = "+productId+"));";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method clearUpsellCrosssellsFromVariations. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    //--------------------------------------  MARKING AS NEED TO SYNC WOO ----------------------------------------------

    //*
    public void markProductsAsNeedToSyncWoo(Set<Long> productsIds, Long masterId) throws Exception {

        String stringQuery =
                " update stores_products " +
                " set need_to_syncwoo = true " +
                " where " +
                " master_id = " + masterId +
                " and product_id in "+ commonUtilites.SetOfLongToString(productsIds,",","(",");")+


                " update stores_variations " +
                " set need_to_syncwoo = true " +
                " where " +
                " master_id = " + masterId +
                " and (" +
                        // if productsIds contains Variative product - mark its variations
                    "product_id in (select variation_product_id from product_variations where product_id in "+ commonUtilites.SetOfLongToString(productsIds,",","(",")")+")"+
                        // if productsIds contains Variations - mark them
                    "or " +
                    "product_id in  "+ commonUtilites.SetOfLongToString(productsIds,",","(",")") +
                " );";



        try {
            if(productsIds.size()>0) {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            }
        } catch (Exception e) {
            logger.error("Exception in method markProductsAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    //*
    private void markProductVariationsAsNeedToSyncWoo(Long productId, Long masterId) throws Exception {
        String stringQuery =
                " update stores_variations " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        " master_id = " + masterId +
                        " and product_id in (select variation_product_id from product_variations where product_id = "+productId+"); " +
        //just in case - if product productId was Variable and now it is Simple
                " update stores_products " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        " master_id = " + masterId +
                        " and product_id in (select variation_product_id from product_variations where product_id = "+productId+"); ";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markProductVariationsAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    //if variation changed or deleted - mark its parent product to resync (it will delete non-existed or excess variations on the Woo side)
    private void markVariableProductAsNeedToSyncWoo(Long variationId, Long masterId) throws Exception {
        String stringQuery =
                        " update stores_products " +
                        " set need_to_syncwoo = true " +
                        " where " +
                        " master_id = " + masterId +
                        " and product_id = (select product_id from product_variations where variation_product_id = "+variationId+" limit 1)";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markVariableProductAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
//    public void markProductsOfStoresAsNeedToSyncWoo(List<Long> storesIds, Long masterId) throws Exception {
//        String stringQuery =
//                " update stores_products " +
//                " set need_to_syncwoo = true " +
//                " where " +
//                " master_id = " + masterId +
//                " and product_id in ("+
//                    " select pc.product_id " +
//                    " from product_productcategories pc where " +
//                    " pc.category_id in ( " +
//                        " select category_id from stores_productcategories where master_id="+masterId+" and store_id in (" +
//                            commonUtilites.ListOfLongToString(storesIds,",","","") +
//                        " )" +
//                    " ) and " +
//                    " pc.category_id in ( " +
//                        " select id from product_categories where master_id="+masterId+" and is_store_category=true" +
//                    " )  "+
//                " )";
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.executeUpdate();
//        } catch (Exception e) {
//            logger.error("Exception in method markProductsOfStoresAsNeedToSyncWoo. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            throw new Exception();
//        }
//    }



    //*
    void markProductsOfStoresAndAttributesAsNeedToSyncWoo(List<Long> storesIds, List<Long> attributesIds, Long masterId) throws Exception {
        String stringQuery =
                "   select pc.product_id" +
                "   from product_productcategories pc" +
                "   where" +
                "   pc.product_id in (" +
                "       select product_id from product_productattributes where attribute_id in ("+commonUtilites.ListOfLongToString(attributesIds,",","","")+")" +
                "   ) and" +
                "   pc.category_id in (" +
                "       select category_id from stores_productcategories where master_id="+masterId+" and store_id in ("+commonUtilites.ListOfLongToString(storesIds,",","","")+")" +
                "   ) and" +
                "   pc.category_id in (" +
                "       select id from product_categories where master_id="+masterId+" and is_store_category=true" +
                "   )";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Long> productIdsList = new ArrayList<>();
            for (Object i : query.getResultList()) {
                productIdsList.add(new Long(i.toString()));
            }
            if(productIdsList.size()>0) {
                stringQuery =
                        " update stores_products   set need_to_syncwoo = true where product_id in (" + commonUtilites.ListOfLongToString(productIdsList, ",", "", "") + ");" +
                        " update stores_variations set need_to_syncwoo = true where product_id in (" +
                            "select variation_product_id from product_variations where product_id in (" + commonUtilites.ListOfLongToString(productIdsList, ",", "", "") + ")" +
                        ")";

                query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            }
        } catch (Exception e) {
            logger.error("Exception in method markProductsOfStoresAndAttributesAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    //*
    public void markProductsOfCategoriesAndStoresAsNeedToSyncWoo(Set<Long> categoriesIds, Long masterId, List<Long> storesIds) throws Exception {

        String stringQuery =
                " update stores_products " +
                " set need_to_syncwoo = true " +
                " where " +
                " master_id = " + masterId +
                " and product_id in (select product_id from product_productcategories where category_id in " +  commonUtilites.SetOfLongToString(categoriesIds, ",","(",")") + ")" +
                " and store_id in " + commonUtilites.ListOfLongToString(storesIds, ",","(",")") + ";" +

                " update stores_variations " +
                " set need_to_syncwoo = true " +
                " where " +
                " master_id = " + masterId +
                " and product_id in (" +
                    " select variation_product_id from product_variations where product_id in (" +
                        "select product_id from product_productcategories where category_id in " +  commonUtilites.SetOfLongToString(categoriesIds, ",","(",")") +
                    " )"+
                " )" +
                " and store_id in " + commonUtilites.ListOfLongToString(storesIds, ",","(",")");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method markProductsOfCategoriesAndStoresAsNeedToSyncWoo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
//    public void markProductsOfAttributesAsNeedToSyncWoo(Set<Long> attributesIds, Long masterId, List<Long> storesIds) throws Exception {
//        String stringQuery =
//        " update stores_products " +
//        " set need_to_syncwoo = true " +
//        " where " +
//        " master_id = " + masterId +
//        " and product_id in (select product_id from product_productattributes where attribute_id in "+ commonUtilites.SetOfLongToString(attributesIds,",","(",")") + ")" +
//        " and store_id in " + commonUtilites.ListOfLongToString(storesIds, ",","(",")");
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.executeUpdate();
//        } catch (Exception e) {
//            logger.error("Exception in method markProductsOfAttributesAsNeedToSyncWoo. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            throw new Exception();
//        }
//    }
//    public void markProductsOfTermsAsNeedToSyncWoo(Set<Long> termsIds, Long masterId, List<Long> storesIds) throws Exception {
//        String stringQuery =
//        " update stores_products " +
//        " set need_to_syncwoo = true " +
//        " where " +
//        " master_id = " + masterId +
//        " and product_id in (select product_id from product_terms where term_id in "+ commonUtilites.SetOfLongToString(termsIds,",","(",")") + ")" +
//        " and store_id in " + commonUtilites.ListOfLongToString(storesIds, ",","(",")");
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.executeUpdate();
//        } catch (Exception e) {
//            logger.error("Exception in method markProductsOfTermsAsNeedToSyncWoo. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            throw new Exception();
//        }
//    }

    private boolean isStoreCategory(Long categoryId) throws Exception {
        // category is online-store category if it marked as "is_store_category" and it has selected internet stores
        String stringQuery = "select count(*) from product_categories where id="+categoryId+" and coalesce(is_store_category,false) = true " +
                " and " +categoryId+ " in (select category_id from stores_productcategories)";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).longValue() > 0L;
        } catch (Exception e) {
            logger.error("Exception in method isCategoryStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean saveChangeCategoriesOrder(List<ProductCategoriesForm> request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))// редактирование своих или чужих предприятий
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            User changer = userRepository.getUserByUsername(userRepository.getUserName());
            String stringQuery="";
            try {
                for (ProductCategoriesForm field : request) {
                    stringQuery = "update product_categories set " +
                            " output_order=" + field.getOutput_order() +
                            " where id=" + field.getId() +
                            " and master_id=" + myMasterId;
                    if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав по всем предприятиям
                    {
//            остается только на своё предприятие
                        int myCompanyId = userRepositoryJPA.getMyCompanyId();
                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
                    }
                    entityManager.createNativeQuery(stringQuery).executeUpdate();
                }
                return true;
            } catch (Exception e) {
                logger.error("Exception in method saveChangeCategoriesOrder. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private Set<ProductCategories> getCategoriesSetByProductId(Long productId) {
        Set<Long> categories = getProductsCategoriesSetIdsByProductId(productId);
        Set<ProductCategories> setCategoriesOfProduct = getCategoriesSetBySetOfCategoriesId(categories);
        return setCategoriesOfProduct;
    }

    @SuppressWarnings("Duplicates") //права не нужны т.к. private, не вызывается по API
    private Set<ProductCategories> getCategoriesSetBySetOfCategoriesId(Set<Long> categories) {
        EntityManager em = emf.createEntityManager();
        Set<ProductCategories> categoriesSet = new HashSet<>();
        for (Long i : categories) {
            categoriesSet.add(em.find(ProductCategories.class, i));
        }
        return categoriesSet;
    }

    @SuppressWarnings("Duplicates")//права не нужны т.к. не вызывается по API, только из контроллера
    public Set<Long> getProductsCategoriesSetIdsByProductId(Long id) {
        String stringQuery = "select p.category_id from product_productcategories p where p.product_id= " + id;
        List<Integer> depIds = entityManager.createNativeQuery(stringQuery).getResultList();
        Set<Long> categoriesSet = new HashSet<>();
        for (Integer i : depIds) {
            categoriesSet.add(Long.valueOf(i));
        }
        //иначе в categoriesSet один хрен попадают Integer'ы, хоть как кастуй, и здравствуй, java.lang.Integer cannot be cast to java.lang.Long в getCategoriesSetBySetOfCategoriesId
        return categoriesSet;
    }

    private Long getParentCategoryId(Long categoryId) throws Exception {
        String stringQuery="";
        try{
            stringQuery="select coalesce(parent_id,0) from product_categories where id = " + categoryId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            logger.error("Exception in method getParentCategoryId. SQL: " + stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }


    private void checkTranslatedCategoryNameUniqueOnSameLevel(Long parentCategoryId, Long currCategoryId, Long companyId, String language, String name, String slug) throws Exception {
        String stringQueryName="";
        String stringQuerySlug="";
        try {
            stringQueryName = "select count(*) from store_translate_categories " +
                    " where " +
                    " lang_code = :language" +
                    " and company_id = " + companyId +
                    " and category_id != " + currCategoryId;
            stringQuerySlug = stringQueryName + " and upper(slug) = upper(:slug) and slug !=''";
            stringQueryName = stringQueryName + " and category_id in " +
                    "(" +
                    "select id from product_categories where parent_id " +
                    (parentCategoryId == 0 ? " is null " : ("=" + parentCategoryId)) +
                    ") " +
                    "and upper(name) = upper(:name) and name !=''";
            Query query = entityManager.createNativeQuery(stringQueryName);
            query.setParameter("name", name);
            query.setParameter("language", language);
            long cnt = ((BigInteger)query.getSingleResult()).longValue();
            if(cnt>0) throw new TranslatedCategoryNameIsNotUniqueOnSameLevel();
            Query query2 = entityManager.createNativeQuery(stringQuerySlug);
            query2.setParameter("slug", slug);
            query2.setParameter("language", language);
            cnt = ((BigInteger)query2.getSingleResult()).longValue();
            if(cnt>0) throw new TranslatedCategorySlugIsNotUnique();

        } catch (TranslatedCategoryNameIsNotUniqueOnSameLevel e){throw new TranslatedCategoryNameIsNotUniqueOnSameLevel();
        } catch (TranslatedCategorySlugIsNotUnique e){throw new TranslatedCategorySlugIsNotUnique();
        } catch (Exception e) {
            logger.error("Exception in method isTranslatedCategoryNameUniqueOnSameLevel. stringQuerySlug: " + stringQuerySlug, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    //права не нужны т.к. не вызывается по API, только из контроллера
    public List<Integer> getProductsCategoriesIdsByProductId(Long id) {
        String stringQuery = "select p.category_id from product_productcategories p where p.product_id= " + id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }

    public List<IdAndName> getProductsUpsellCrosssells(Long id, String tableName) throws Exception {
        String stringQuery = "select p.child_id, (select name from products where id=p.child_id) from "+tableName+" p where p.product_id= " + id;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<IdAndName> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                IdAndName doc = new IdAndName();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setName((String) obj[1]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getProductsUpsellCrosssells. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Transactional//права не нужны т.к. не вызывается по API, только из контроллера
    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
    public List<ProductCategories> getProductCategoriesTrees(List<Integer> rootIds) {
        List<ProductCategories> returnTreesList = new ArrayList<ProductCategories>();
        String stringQuery;
        stringQuery = "from ProductCategories p ";
        stringQuery = stringQuery + " left join fetch p.children";
        entityManager.createQuery(stringQuery, ProductCategories.class).getResultList();
        for (int rootId : rootIds) {
            returnTreesList.add(entityManager.find(ProductCategories.class, (long) rootId));
        }
        return returnTreesList;
    }

    //возвращает сет всех id интернет-магазинов от id категории товаров
    @SuppressWarnings("Duplicates")
    private Set<Long> getCategoryStoresIds(Long categoryId) throws Exception {
        String stringQuery="    SELECT store_id FROM stores_productcategories WHERE category_id = " + categoryId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            Set<Long> catIds = new HashSet<>();
            for (Object i : query.getResultList()) {
                catIds.add(new Long(i.toString()));
            }
            return catIds;
        }catch (Exception e) {
            logger.error("Exception in method getCategoryStoresIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception (e);
        }
    }
    //возвращает сет всех id категорий от id товара
    @SuppressWarnings("Duplicates")
    private Set<Long> getProductCategoriesIds(Long productId) {
        String stringQuery="    SELECT category_id FROM product_productcategories WHERE product_id = " + productId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            Set<Long> catIds = new HashSet<>();
            for (Object i : query.getResultList()) {
                catIds.add(new Long(i.toString()));
            }
            return catIds;
        }catch (Exception e) {
            logger.error("Exception in method getProductCategoriesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //возвращает сет всех id дочерних категорий от id категории
    @SuppressWarnings("Duplicates")
    public Set<Long> getProductCategoryChildIds(Long parentId) {
        String stringQuery="WITH RECURSIVE nodes(id) AS (" +
                "    SELECT ps1.id " +
                "    FROM product_categories ps1 WHERE parent_id = " + parentId +
                "        UNION" +
                "    SELECT ps2.id" +
                "    FROM product_categories ps2, nodes ps1 WHERE ps2.parent_id = ps1.id" +
                ")" +
                "SELECT * FROM nodes;";
        Query query = entityManager.createNativeQuery(stringQuery);
        Set<Long> catIds = new HashSet<>();
        for(Object i: query.getResultList()){
            catIds.add(new Long(i.toString()));
        }//иначе в этом листе будут интеджеры, хоть он и лонг
        return catIds;
    }


    //возвращает сет id дочерних категорий от листа id присланных категорий
    @Transactional
    @SuppressWarnings("Duplicates")
    public Set<Long> getProductCategoriesChildIds(List<Long> parentIds) {
        Set<Long> returnIdsSet = new HashSet<>();
        for (Long parentId : parentIds) {
            Set<Long> categoryChildsIds = getProductCategoryChildIds(parentId);
            returnIdsSet.addAll(categoryChildsIds);
        }
        return returnIdsSet;
    }

    //права на просмотр документов в таблице меню
    @SuppressWarnings("Duplicates") //отдает только найденные категорий, без иерархии
    public List<ProductCategoriesTableJSON> searchProductCategory(Long companyId, String searchString) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))// Меню - таблица
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = "select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from product_categories " +
                    " where company_id =" + companyId + " and master_id=" + myMasterId + " and upper(name) like upper(CONCAT('%',:sg,'%'))";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            query.setParameter("sg", searchString);
            return query.getResultList();
        } else return null;
    }


    @SuppressWarnings("Duplicates") //возвращает id корневых категорий
    public List<Integer> getCategoriesRootIds(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Меню - таблица
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = "select id from product_categories " +
                    "  where company_id =" + id + " and master_id=" + myMasterId + " and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
//    отдает только список корневых категорий, без детей
//    нужно для изменения порядка вывода корневых категорий
    public List<ProductCategoriesTableJSON> getRootProductCategories(Long companyId) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))// (см. файл Permissions Id)
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = "select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from product_categories " +
                    "  where company_id =" + companyId + " and master_id=" + myMasterId + " and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }


    @SuppressWarnings("Duplicates") //отдает только список детей, без их детей
    public List<ProductCategoriesTableJSON> getChildrensProductCategories(Long parentId) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))//редактирование категорий
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = "select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from product_categories " +
                    " where parent_id =" + parentId + " and master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    //отдает список отделений в виде их Id с зарезервированным количеством и общим количеством товара в отделении
    public List<IdAndCount> getProductCount(Long product_id, Long company_id, Long document_id) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery = "select" +
                " d.id as id, " +
                " (select coalesce(quantity,0) from product_quantity where department_id = d.id and product_id = "+product_id+") as total, " +
                " (select " +
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id="+product_id+
                "   and department_id = d.id ";
        // Eсли запрос отправляется из Заказа покупателя или из док-та, связанного с ним, нужно вернуть информацию, исключающую данный заказ.
        // Если же отправляется из другого док-та (например Рознчная продажа) НЕ связанного с Заказом покупателя - возвращается полная информация
        if(document_id>0L) {
            stringQuery=stringQuery+"   and customers_orders_id!=" + document_id;//зарезервировано в других документах Заказ покупателя
        }
        stringQuery=stringQuery+"  ) as reserved" +
                " from" +
                " departments d " +
                " where" +
                " d.company_id= " + company_id +
                " and d.master_id= " + myMasterId +
                " and coalesce(d.is_deleted,false)=false";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<IdAndCount> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                IdAndCount product = new IdAndCount();
                product.setId(Long.parseLong(              obj[0].toString()));
                product.setTotal(                          obj[1]==null?BigDecimal.ZERO:(BigDecimal)obj[1]);
                product.setReserved(                       obj[2]==null?BigDecimal.ZERO:(BigDecimal)obj[2]);
                returnList.add(product);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getProductCount. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    //отдает информацию о состоянии товара(кол-во всего, зарезервировано и цена) по его id ,в отделении по его id, по цене по её id, для документа Заказ покупателя по его id
    public ProductsPriceAndRemainsJSON getProductsPriceAndRemains(Long department_id, Long product_id, Long price_type_id, Long document_id) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
        //себестоимость
        ProductHistoryJSON lastProductHistoryRecord = getLastProductHistoryRecord(product_id,department_id);

        String stringQuery = "select" +
                " coalesce((select quantity from product_quantity where product_id = "+product_id+" and department_id = d.id),0) as total, "+ //всего на складе (т.е остаток)
                " (select " +
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id="+product_id+
                "   and department_id = d.id ";
        // Eсли запрос отправляется из Заказа покупателя или из док-та, связанного с ним, нужно вернуть информацию, исключающую данный заказ.
        // Если же отправляется из другого док-та (например Рознчная продажа) НЕ связанного с Заказом покупателя - возвращается полное количество
        if(document_id>0L) {
            stringQuery=stringQuery+"   and customers_orders_id!=" + document_id;//зарезервировано в других документах Заказ покупателя
        }
        stringQuery=stringQuery+"  ) as reserved";//зарезервировано в других документах Заказ покупателя

        if(price_type_id!=0) {//если тип цены был выбран
            stringQuery=stringQuery+", coalesce((select price_value from product_prices where product_id = " + product_id + " and price_type_id = " + price_type_id + " and company_id = d.company_id),0) as price ";// цена по типу цены
        }
        stringQuery=stringQuery+" from" +
                " departments d " +
                " where" +
                " d.id= " + department_id +
                " and d.master_id= " + myMasterId;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            ProductsPriceAndRemainsJSON returnObj = new ProductsPriceAndRemainsJSON();

            for (Object[] obj : queryList) {
                returnObj.setTotal(                                         obj[0]==null?BigDecimal.ZERO:(BigDecimal)obj[0]);
                returnObj.setReserved(                                      obj[1]==null?BigDecimal.ZERO:(BigDecimal)obj[1]);
                if (price_type_id != 0) {
                    returnObj.setPrice((BigDecimal)                         obj[2]);
                } else {
                    returnObj.setPrice(                      BigDecimal.valueOf(0));
                }
                //Устанавливаем последние цены: Среднюю себестоимость, Среднюю закупочную, Последнюю закупочную
                returnObj.setAvgCostPrice(      lastProductHistoryRecord.getAvg_netcost_price());
                returnObj.setAvgPurchasePrice(  lastProductHistoryRecord.getAvg_purchase_price());
                returnObj.setLastPurchasePrice( lastProductHistoryRecord.getLast_purchase_price());
            }
            return returnObj;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductsPriceAndRemains. SQL query:"+stringQuery, e);
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    //отдает 4 цены на товар (средняя себестоимость, последяя закупочная, средняя закупочная, цена по запрошенному типу цены)
    public ProductPricingInfoJSON getProductPricesAll(Long departmentId, Long productId, Long priceTypeId) {
        String stringQuery = "";
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        ProductHistoryJSON lastProductHistoryRecord = getLastProductHistoryRecord(productId,departmentId);
        ProductPricingInfoJSON returnObj = new ProductPricingInfoJSON();
        try {
            returnObj.setAvgCostPrice(      lastProductHistoryRecord.getAvg_netcost_price());
            returnObj.setAvgPurchasePrice(  lastProductHistoryRecord.getAvg_purchase_price());
            returnObj.setLastPurchasePrice( lastProductHistoryRecord.getLast_purchase_price());

            if(priceTypeId!=0) {//если тип цены был выбран
                stringQuery="select price_value from product_prices where product_id = " + productId + " and price_type_id = " + priceTypeId + " and master_id = "+myMasterId+"";// цена по типу цены
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                for (Iterator i = queryList.iterator(); i.hasNext();) {
                    BigDecimal value = (BigDecimal) i.next();
                    returnObj.setPriceOfTypePrice(value);
                }
            } else returnObj.setPriceOfTypePrice(BigDecimal.ZERO);
            return returnObj;
        } catch (Exception e) {
            logger.error("Exception in method getProductPricesAll. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<ProductsInfoListJSON> getProductsInfoListByIds( ProductsInfoListForm request) {
        Long        companyId=request.getCompanyId();
        Long        departmentId=request.getDepartmentId();
        Long        priceTypeId=request.getPriceTypeId();
        Long        myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String      reportOn=request.getReportOn();            // по категориям или по товарам/услугам (categories, products)
        List<Long>  reportOnIds=request.getReportOnIds();     // id категорий или товаров/услуг (того, что выбрано в reportOn)
        String ids =commonUtilites.ListOfLongToString(reportOnIds,",","","");

        String  stringQuery = "select  p.id as id, " +
                // наименование товара
                "           p.name as name, " +
                // наименование ед. измерения
                " ei.short_name as edizm, " +
                // всего единиц товара в отделении (складе)
                " (select coalesce(quantity,0)   from product_quantity     where department_id = "    + departmentId +" and product_id = p.id) as estimated_balance, " +
                // цена по запрашиваемому типу цены priceTypeId (если тип цены не запрашивается - ставим null в качестве цены по отсутствующему в запросе типу цены)
                " coalesce((select pp.price_value from product_prices pp where pp.product_id=p.id and pp.price_type_id = "+priceTypeId+"),0) as price_by_typeprice, " +
                // средняя себестоимость
                " (select ph.avg_netcost_price from product_quantity ph where ph.department_id = "  + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgCostPrice, " +
                // средняя закупочная цена - deprecated and no more used
                " 0.00 as avgPurchasePrice, " +
                // последняя закупочная цена                                                                // 15 = Acceptance
                " (select ph.price from product_history ph  where ph.department_id = " + departmentId +" and doc_type_id=15 and ph.product_id = p.id and ph.is_completed=true order by ph.date_time_created desc limit 1) as lastPurchasePrice, " +
                //всего на складе (т.е остаток)
                "           coalesce((select quantity from product_quantity where product_id = p.id and department_id = " + departmentId + "),0) as remains, " +
                // id ставки НДС
                "           p.nds_id as nds_id," +
                // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                " p.indivisible as indivisible," +
                // материален ли товар
                " ppr.is_material as is_material, " +
                " (select " +
                // Резервы товара !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
                //по логике: сумма( резерв > (всего - отгружено) ? (всего - отгружено) : резерв)    (при условии не позволять в заказах покупателей делать резерв больше "всего" (reserved_current!>product_count))
                "   from " +
                "   customers_orders_product " +
                "   where " +
                "   product_id=p.id "+
                "   and department_id = " + departmentId +" ) as reserved "+//зарезервировано во всех документах Заказ покупателя

                " from products p " +
                " inner join sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                " left outer join sprav_sys_ppr ssp on ssp.id=p.ppr_id" +
                (priceTypeId>0?" left outer join product_prices pp on pp.product_id=p.id":"")   +
                " left outer join sprav_sys_edizm ei on p.edizm_id=ei.id" +
                " where  p.master_id=" + myMasterId;



        if (request.getReportOn().equals("products"))
            stringQuery = stringQuery + " and p.id in (" + ids + ")";
        if (request.getReportOn().equals("categories")) {
            //  необходимо запросить все айдишники подкатегорий у присланных id родительских категорий
            Set<Long> childCategories = getProductCategoriesChildIds(request.getReportOnIds());
            String childIds = commonUtilites.SetOfLongToString(childCategories, ",", "", "");
            stringQuery = stringQuery + " and p.id in (select ppc.product_id from product_productcategories ppc where ppc.category_id in (" + ids + (childIds.length()>0?",":"") + childIds + "))";
        }

        stringQuery = stringQuery + (priceTypeId>0?(" and pp.price_type_id = " + priceTypeId):"");//если тип цены запрашивается

        stringQuery = stringQuery + " and coalesce(p.is_deleted,false) !=true ";
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id, ei.short_name, ppr.is_material" +
//                (priceTypeId>0?(", pp.price_value"):"") + //если тип цены запрашивается - группируем таже и по нему
                "  order by p.name asc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ProductsInfoListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductsInfoListJSON product = new ProductsInfoListJSON();
                product.setProduct_id(Long.parseLong(                       obj[0].toString()));
                product.setName((String)                                    obj[1]);
                product.setEdizm((String)                                   obj[2]);
                product.setEstimated_balance(                               obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                product.setPriceOfTypePrice(                                obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                product.setAvgCostPrice(                                    obj[5]==null?BigDecimal.ZERO:(BigDecimal)obj[5]);
                product.setAvgPurchasePrice(                                obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                product.setLastPurchasePrice(                               obj[7]==null?BigDecimal.ZERO:(BigDecimal)obj[7]);
                product.setRemains(                                         obj[8]==null?BigDecimal.ZERO:(BigDecimal)obj[8]);
                product.setTotal(                                           obj[8]==null?BigDecimal.ZERO:(BigDecimal)obj[8]);
                product.setNds_id((Integer)                                 obj[9]);
                product.setIndivisible((Boolean)                            obj[10]);
                product.setIs_material((Boolean)                            obj[11]);
                product.setReserved(                                        obj[12]==null?BigDecimal.ZERO:(BigDecimal)obj[12]);
                returnList.add(product);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getProductsInfoListByIds. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


//*****************************************************************************************************************************************************
//***********************************************   Product Custom Fields   ***************************************************************************
//*****************************************************************************************************************************************************
//
//    @SuppressWarnings("Duplicates")
//    @Transactional
//    public boolean updateProductCustomFields(List<ProductCustomFieldsSaveForm> request) {
//        //log.info("in updateProductCustomFields class");
//        if (request.size() > 0) { //если поля на сохранение есть
//            Long productId = request.get(0).getProduct_id();//за id товара берем productId из первого же объекта (т.к. они ДОЛЖНЫ быть все одинаковы)
//            //log.info("productId="+productId.toString());
//            //log.info("Поля на сохранение есть ");
//            for (ProductCustomFieldsSaveForm custumField : request) {
//                if (!productId.equals(custumField.getProduct_id())) {
//                    //log.info("В листе не одинаковые productId!");
//                    return false; //проверяю что в листе все productId одинаковые
//                }
//            }
//            //log.info("Прошли цикл, перед проверкой прав.");
//            // проверка на то, что все поля принадлежат к тем документам Товары, на которые есть соответствующие права:
//            //Если есть право на "Изменение по всем предприятиям" и все id для изменения принадлежат владельцу аккаунта (с которого изменяют), ИЛИ
//            if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", productId.toString())) ||
//                    //Если есть право на "Изменение по своему предприятияю" и все id для изменения принадлежат владельцу аккаунта (с которого изменяют) и предприятию аккаунта
//                    (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", productId.toString()))) {
//                //log.info("Права есть!");
//                try {
//                    for (ProductCustomFieldsSaveForm custumField : request) {
//                        //log.info("В цикле: поле "+custumField.getName());
//                        if (isThereThisField(custumField.getProduct_id(), custumField.getId())) { // если поле уже есть в product_fields
//                            //log.info("поле уже есть в product_fields");
//                            updateCustomField(custumField.getProduct_id(), custumField.getId(), custumField.getValue()); //то апдейтим
//                        } else {
//                            //log.info("поля нет в product_fields");
//                            if (custumField.getValue() != null && !custumField.getValue().isEmpty() && custumField.getValue().trim().length() > 0) {
//                                //если поля нет в product_fields, и в нём есть текст, то инсертим (чтобы пустые строки в product_fields не разводить)
//                                //log.info("поля нет в product_fields, и в нём есть текст - инсертим");
//                                createCustomField(custumField.getProduct_id(), custumField.getId(), custumField.getValue()); // нет - то инсертим
//                                //log.info("после инсерта");
//                            }
//                        }
//                    }
//                    return true;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    logger.error("Exception in method updateProductCustomFields. ", e);
//                    return false;
//                }
//            } else {
//                //log.info("НЕТ ПРАВ! ");
//                return false;
//            }
//        } else return true;// тут true чтобы не было ошибки в консоли браузера.
//    }
//
//    private List<Object[]> getListOfProductFields(Long productId) {
//        List<Object[]> a;
//        String stringQuery = "select  product_id as product_id, field_id as id, field_value as value, '1' as name, '1' as parent_set_id from product_fields p where p.product_id=" + productId;
//        Query query = entityManager.createNativeQuery(stringQuery);
//        a = query.getResultList();
//        return a;
//    }
//
//    public boolean isThereThisField(Long product_id, Long field_id) {
//        String stringQuery = "select 1 from product_fields p where p.product_id=" + product_id + " and p.field_id =" + field_id;
//        Query query = entityManager.createNativeQuery(stringQuery);
//        return (query.getResultList().size() > 0);
//    }
//
//    @SuppressWarnings("Duplicates")
//    public boolean updateCustomField(Long product_id, Long field_id, String value) {
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String stringQuery;
//        stringQuery = "update product_fields set " +
//                " field_value=:value_ " +
//                " where product_id=" + product_id +
//                " and field_id=" + field_id +
//                " and (select master_id from products where id=" + product_id + ") = " + myMasterId; //для безопасности, чтобы не кидали json на авось у кого-то что-то проапдейтить
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.setParameter("value_", value);
//            int i = query.executeUpdate();
//            return true;
//        } catch (Exception e) {
//            logger.error("Exception in method updateCustomField. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @SuppressWarnings("Duplicates")
//    public boolean createCustomField(Long product_id, Long field_id, String value) {
//        String stringQuery;
//        stringQuery = "insert into product_fields (product_id, field_id, field_value) values (" + product_id + "," + field_id + ", :value_)";
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.setParameter("value_", value);
//            if (query.executeUpdate() == 1) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            logger.error("Exception in method createCustomField. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public List<ProductGroupFieldTableJSON> getProductGroupFieldsListWithValues(int field_type, int productId) {
//        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168,169,170"))//просмотр или редактирование (см. файл Permissions Id)
//        {
//            String stringQuery;
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//
//
//            stringQuery = "select " +
//                    " pgf.id as id, " +
//                    " pgf.name as name, " +
//                    " pgf.description as description, " +
//                    " pgf.field_type as field_type, " +
//                    " pgf.group_id as group_id, " +
//                    " pgf.output_order as output_order, " +
//                    //field_type: 1 - сеты (наборы) полей, 2 - поля
//                    (field_type == 1 ? "''" : "(select coalesce (pf.field_value,'') from product_fields pf where pf.field_id=pgf.id and pf.product_id=p.id limit 1)") + " as value, " +
//                    " pgf.parent_set_id as parent_set_id " +
//                    " from  " +
//                    " product_group_fields pgf," +
//                    " product_groups pg," +
//                    " products p " +
//                    " where pgf.group_id=pg.id " +
//                    " and p.group_id=pg.id " +
//                    " and pgf.field_type = " + field_type +// тип: 1 - сеты (наборы) полей, 2 - поля
//                    " and p.id=" + productId +
//                    " and pgf.master_id=" + myMasterId;
//
//
//            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167,169")) //Если нет прав на просм. или редактир. по всем предприятиям"
//            {
//                //остается только на своё предприятие
//                stringQuery = stringQuery + " and pgf.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
//            }
//            stringQuery = stringQuery + " order by pgf.output_order asc ";
//            Query query = entityManager.createNativeQuery(stringQuery, ProductGroupFieldTableJSON.class);
//            return query.getResultList();
//        } else return null;
//    }

//*****************************************************************************************************************************************************
//******************************************************   C A G E N T S    ***************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addCagentsToProduct(UniversalForm request) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery="";
        Set<Long> Ids = request.getSetOfLongs1();
        Long prouctId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", prouctId.toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", prouctId.toString()))) {
            try {
                for (Long Id : Ids) {

                    stringQuery = "select product_id from product_cagents where product_id=" + prouctId + " and cagent_id=" + Id;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких поставщиков еще нет у товара
                        entityManager.close();
                        manyToMany_productId_CagentId(prouctId, Id, myMasterId);
                    }
                }
                return true;
            } catch (Exception e) {
                logger.error("Exception in method addCagentsToProduct. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional//права не нужны, внутренниЙ вызов
    @SuppressWarnings("Duplicates")
    boolean manyToMany_productId_CagentId(Long prouctId, Long cagentId, Long masterId) {
        try {
            commonUtilites.idBelongsMyMaster("cagents", cagentId, masterId);
            commonUtilites.idBelongsMyMaster("products", prouctId, masterId);
            entityManager.createNativeQuery("" +
                    "insert into product_cagents " +
                    "(product_id,cagent_id,output_order) " +
                    "values " +
                    "(" + prouctId + ", " + cagentId + " , (select coalesce(max(output_order)+1,1) from product_cagents where product_id=" + prouctId + "))")
                    .executeUpdate();
            entityManager.close();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method manyToMany_productId_CagentId.", e);
            e.printStackTrace();
            return false;
        }
    }

    @Transactional//права не нужны, внутренниЙ вызов
    @SuppressWarnings("Duplicates")
        //используется при копировании (создании дубликата) документа
    boolean addCagentToProduct(ProductCagentsJSON request, Long newProductId) {
        try {
            Query query = entityManager.createNativeQuery("" +
                    "insert into product_cagents " +
                    "(product_id, cagent_id, output_order, cagent_article, additional) " +
                    "values " +
                    "(" + newProductId + ", " +
                    request.getCagent_id() +
                    " , (select coalesce(max(output_order)+1,1) from product_cagents where product_id="+ newProductId + "), :cg_article, :additional)");

            query.setParameter("cg_article",request.getCagent_article());
            query.setParameter("additional",request.getAdditional());
            query.executeUpdate();
            entityManager.close();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addCagentToProduct.", e);
            e.printStackTrace();
            return false;
        }
    }


    @SuppressWarnings("Duplicates") //отдает информацию по поставщикам товара
    public List<ProductCagentsJSON> getListOfProductCagents(Long productId) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            List<ProductCagentsJSON> returnlist;
            String stringQuery = "select" +
                    "           c.id as cagent_id," +
                    "           f.product_id as product_id," +
                    "           c.name as name," +
                    "           f.output_order as output_order," +
                    "           f.cagent_article as cagent_article," +
                    "           f.additional as additional" +
                    "           from" +
                    "           product_cagents f" +
                    "           inner join" +
                    "           products p" +
                    "           on f.product_id=p.id" +
                    "           inner join" +
                    "           cagents c" +
                    "           on f.cagent_id=c.id" +
                    "           where" +
                    "           f.product_id= " + productId +
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by f.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery, ProductCagentsJSON.class);
            returnlist = query.getResultList();
            return returnlist;
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateProductCagentProperties(UniversalForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getId2().toString()))) {
            String stringQuery="";
            try {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

                stringQuery = "Update product_cagents p" +
                        " set   cagent_article= :cg_article, " +
                        "       additional= :additional" +
                        " where (select master_id from cagents where id=p.cagent_id)=" + myMasterId + //контроль того, что лицо, имеющее доступ к редактированию документа, не может через сторонние сервисы типа postman изменить документы других аккаунтов
                        " and p.cagent_id=" + request.getId1() +
                        " and p.product_id=" + request.getId2();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("cg_article",(request.getString1() != null ? request.getString1() : ""));
                query.setParameter("additional",(request.getString2() != null ? request.getString2() : ""));
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method updateProductCagentProperties. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductCagent(UniversalForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getId2().toString()))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = " delete from product_cagents " +
                    " where product_id=" + request.getId2() +
                    " and cagent_id=" + request.getId1() +
                    " and (select master_id from products where id=" + request.getId2() + ")=" + myMasterId;
            try {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductCagent. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }


//*****************************************************************************************************************************************************
//****************************************************   I  M  A  G  E  S   ***************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    //права не нужны т.к. не вызывается по API, только из контроллера
    public Set<Long> getProductsImagesSetIdsByProductId(Long id) {
        String stringQuery = "select p.file_id from product_files p where p.product_id= " + id;
        List<Integer> depIds = entityManager.createNativeQuery(stringQuery).getResultList();
        Set<Long> categoriesSet = new HashSet<>();
        for (Integer i : depIds) {
            categoriesSet.add(Long.valueOf(i));
        }
        return categoriesSet;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer addFilesToProduct(UniversalForm request) {
        Long prouctId = request.getId1();
        String tableName = request.getString1();
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", prouctId.toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", prouctId.toString()))) {
            String stringQuery="";
            try {

                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select product_id from " +tableName+ " where product_id=" + prouctId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (fileIsImage(fileId) && query.getResultList().size() == 0) {//если таких картинок еще нет у товара, и файл является картинкой
                        entityManager.close();
                        manyToMany_productId_FileId(prouctId, fileId, tableName);
                    }
                }
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method addFilesToProduct. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    boolean manyToMany_productId_FileId(Long prouctId, Long fileId, String tableName) throws Exception {
        try {
            entityManager.createNativeQuery(" " +
                    "insert into " + tableName +
                    " (product_id,file_id,output_order) " +
                    " values " +
                    " (" + prouctId + ", " + fileId + " , (select coalesce(max(output_order)+1,1) from " + tableName + " where product_id=" + prouctId + "))")
                    .executeUpdate();
            entityManager.close();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method manyToMany_productId_FileId.", e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private boolean fileIsImage(Long fileId) {
        return true;
    }

    //отдает информацию по картинкам товара (fullSize - полным, нет - по их thumbnails)
    public List<FilesProductImagesJSON> getListOfProductImages(Long productId, boolean fullSize) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = "select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name," +
                    "           pf.output_order as output_order" +
                    "           from" +
                    "           products p" +
                    "           inner join" +
                    "           product_files pf" +
                    "           on p.id=pf.product_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + productId +
                    "           and f.trash is not true" +
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by pf.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery, FilesProductImagesJSON.class);
            return query.getResultList();
        } else return null;
    }

    public List<FilesProductDownloadable> getProductDownloadableFiles(Long productId) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = "select" +
                    "           f.id as id," +
                    "           f.name as name," +
                    "           f.original_name as original_name," +
                    "           pf.output_order as output_order" +
                    "           from" +
                    "           products p" +
                    "           inner join product_downloadable_files pf on p.id=pf.product_id" +
                    "           inner join files f on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + productId +
                    "           and f.trash is not true" +
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by pf.output_order asc ";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();

                List<FilesProductDownloadable> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    FilesProductDownloadable doc = new FilesProductDownloadable();
                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setName((String) obj[1]);
                    doc.setOriginal_name((String) obj[2]);
                    doc.setOutput_order((Integer) obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            }
            catch (Exception e) {
                logger.error("Exception in method getProductDownloadableFiles. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductFile(UniversalForm request) {
        String tableName = request.getString1();
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getId1().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getId1().toString()))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery = " delete from " + tableName +
                    " where product_id=" + request.getId1() +
                    " and file_id=" + request.getId() +
                    " and (select master_id from products where id=" + request.getId1() + ")=" + myMasterId;
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductImage. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }



//*****************************************************************************************************************************************************
//****************************************************   B  A  R  C  O  D  E  S   *********************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean insertProductBarcode(UniversalForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getId2().toString()))) {
            try {
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                commonUtilites.idBelongsMyMaster("products", request.getId2(), myMasterId);

                Query query = entityManager.createNativeQuery(
                        "insert into product_barcodes " +
                        "(product_id,barcode_id,value, description) " +
                        "values " +
                        "(" + request.getId2() + " , " +
                        request.getId3() + " , :value_, :description)");

                query.setParameter("value_",(request.getString1() != null ? (request.getString1()) : ""));
                query.setParameter("description",(request.getString2() != null ? (request.getString2()) : ""));
                query.executeUpdate();
                entityManager.close();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method insertProductBarcode.", e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates") //отдает информацию по штрих-кодам товара
    public List<ProductBarcodesJSON> getListOfProductBarcodes(Long productId) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            List<ProductBarcodesJSON> returnlist;
            String stringQuery = "select" +
                    "           pb.id as id," +
                    "           pb.barcode_id as barcode_id," +
                    "           pb.product_id as product_id," +
                    "           b.name as name," +
                    "           pb.value as value," +
                    "           pb.description as description" +
                    "           from" +
                    "           product_barcodes pb" +
                    "           inner join" +
                    "           sprav_sys_barcode b" +
                    "           on b.id=pb.barcode_id" +
                    "           inner join" +
                    "           products p" +
                    "           on p.id=pb.product_id" +
                    "           where" +
                    "           pb.product_id= " + productId +
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by pb.id asc ";
            Query query = entityManager.createNativeQuery(stringQuery, ProductBarcodesJSON.class);
            returnlist = query.getResultList();
            return returnlist;
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateProductBarcode(UniversalForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", getProductIdByBarcodeId(request.getId1()))) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", getProductIdByBarcodeId(request.getId1())))) {

            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "Update product_barcodes p" +
                    " set  value= :value_" +
                    "    , description= :description" +
                    " where p.id=" + request.getId1();
            try {
                commonUtilites.idBelongsMyMaster("product_barcodes",request.getId1(), myMasterId);

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("value_",(request.getString1() != null ? (request.getString1()) : ""));
                query.setParameter("description",(request.getString2() != null ? (request.getString2()) : ""));
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method updateProductBarcode. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductBarcode(UniversalForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getId2().toString()))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            //int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery = " delete from product_barcodes where id=" + request.getId1();
            try {
                commonUtilites.idBelongsMyMaster("product_barcodes",request.getId1(), myMasterId);

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductBarcode. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer generateWeightProductCode(UniversalForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getId1().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getId1().toString()))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;

            stringQuery = "update products set " +
                    " product_code=(select coalesce(max(product_code)+1,1) from products where company_id=" + request.getId2() + " and master_id=" + myMasterId + ")" +
                    " where id=" + request.getId1() +
                    " and master_id=" + myMasterId +
                    " and company_id=" + request.getId2() +
                    " and product_code is null";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                if (query.executeUpdate() == 1) {
                    stringQuery = "select product_code from products where id=" + request.getId1();
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    return Integer.parseInt(query2.getSingleResult().toString());
                } else return (0);
            } catch (Exception e) {
                logger.error("Exception in method generateWeightProductCode. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return 0;
            }
        } else return 0;
    }

    @SuppressWarnings("Duplicates")  //права не нужны, внутренний вызов
    private Long generateFreeProductCode(Long company_id) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(product_code_free)+1,1) from products where company_id=" + company_id + " and master_id=" + myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(), 10);
        } catch (Exception e) {
            logger.error("Exception in method generateFreeProductCode. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates")
    public Boolean isProductCodeFreeUnical(UniversalForm request) {
        Long company_id = request.getId1();
        Long code = request.getId2();
        Long product_id = request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from products where " +
                "company_id=" + company_id +
                " and master_id=" + myMasterId +
                " and product_code_free=" + code;
        if (product_id > 0) stringQuery = stringQuery + " and id !=" + product_id; // чтобы он не срабатывал сам на себя
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            if (query.getResultList().size() > 0)
                return false;// код не уникальный
            else return true; // код уникальный
        } catch (Exception e) {
            logger.error("Exception in method isProductCodeFreeUnical. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

    @SuppressWarnings("Duplicates")
    public Object getProductBarcodesPrefixes(UniversalForm request) {
        Long company_id = request.getId1();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = "select " +
                " st_prefix_barcode_pieced as st_prefix_barcode_pieced, " +//штучный
                " st_prefix_barcode_packed as st_prefix_barcode_packed " + //весовой
                " from companies where id=" + company_id + " and master_id=" + myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Exception in method getProductBarcodesPrefixes. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates") //права не нужны, внутренний вызов
    private String getProductIdByBarcodeId(Long barcodeIdKey) { //да, мне тут нужен стринг

        String stringQuery = "select product_id from product_barcodes where id=" + barcodeIdKey;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getSingleResult().toString();
        } catch (Exception e) {
            logger.error("Exception in method getProductIdByBarcodeId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional//права не нужны, внутренниЙ вызов
    @SuppressWarnings("Duplicates")
        //используется при копировании (создании дубликата) документа
    boolean addBarcodeToProduct(ProductBarcodesJSON request, Long newProductId) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();

        String stringQuery =
                "insert into product_barcodes " +
                        "(product_id, barcode_id, value, description) " +
                        "values " +
                        "(" + newProductId + ", " +
                        request.getBarcode_id() + ", :value_, :description)";
        try {
            commonUtilites.idBelongsMyMaster("products", newProductId, myMasterId);

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("value_",request.getValue());
            query.setParameter("description",request.getDescription());
            query.executeUpdate();
            entityManager.close();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addBarcodeToProduct. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************  C O M M O N   U T I L I T E S   ***************************************************************
//*****************************************************************************************************************************************************

    // определяет, материален ли товар, по его признаку предмета расчёта
    public Boolean isProductMaterial(Long prodId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " select is_material from sprav_sys_ppr where id= (" +
                            " select ppr_id from products where id=" + prodId +")";
            Query query = entityManager.createNativeQuery(stringQuery);
            return (Boolean) query.getSingleResult();
        }
        catch (Exception e) {
            logger.error("Exception in method isProductMaterial. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception("Exception in method ProductsRepositoryJpa/isProductMaterial");
        }
    }

    @SuppressWarnings("Duplicates")
    //product_quantity - таблица, в которой хранится актуальное количество товара (при условии что товар материален, т.е. isProductMaterial возвращает true).
    public Boolean setProductQuantity(Long masterId, Long product_id, Long department_id, BigDecimal quantity, BigDecimal avg_netcost_price) throws CantSaveProductQuantityException {
        String stringQuery="";
        try {
            stringQuery =
                    " insert into product_quantity (" +
                            " master_id," +
                            " department_id," +
                            " product_id," +
                            " quantity," +
                            " date_time_created, " +
                            " avg_netcost_price" +
                            ") values ("+
                            masterId + ","+
                            department_id + ","+
                            product_id + ","+
                            quantity + ","+
                            "now()," +
                            avg_netcost_price +
                            ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert" - unique: product_id, department_id
                            " DO update set " +
//                            " department_id = " + department_id + ","+
//                            " product_id = " + product_id + ","+
//                            " master_id = "+ masterId + "," +
                            " quantity = "+ quantity + "," +
                            " date_time_created = now(), " +
                            " avg_netcost_price = "+ avg_netcost_price;


            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method setProductQuantity. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new CantSaveProductQuantityException();
        }
    }
    @SuppressWarnings("Duplicates")
    //product_history - таблица, в которой хранится история операций с товаром в отделении: вид операции (doc_type_id), сколько (change) и по какой цене (price)
    public Boolean setProductHistory(
            Long masterId,
            Long company_id,
            Long department_id,
            int  doc_type_id,
            Long doc_id,
            Long product_id,
            BigDecimal change,
            BigDecimal price,
            BigDecimal netcost,
//            Timestamp date_time_created,
            boolean is_completed
    ) throws CantSaveProductHistoryException {
        String stringQuery =
                " insert into product_history (" +
                        " master_id," +
                        " company_id," +
                        " department_id," +     // отделение в котором проводится операция
                        " doc_type_id," +       // id типа документа (id из таблицы documents)
                        " doc_id," +            // id документа
                        " product_id," +        // id товара
                        " change," +            // изменение кол-ва товара в операции
                        " price," +             // цена единицы товара в данной операции
                        " netcost," +           // себестоимость единицы товара (т.е. цена единицы товара + часть распределенной по всем товарам себестоимости операции)
                        " date_time_created," + // время первого проведения документа, к которому относится данная строка таблицы
                        " is_completed "+       // проведён ли документ, к которому относится данная строка таблицы
                        ") values ("+
                        masterId +","+
                        company_id +","+
                        department_id + ","+
                        doc_type_id +","+
                        doc_id + ","+
                        product_id + ","+
                        change+","+
                        price+","+
                        netcost+","+
//                        "'" + date_time_created + "',"+
                        "now(),"+ // В истории изменения товара - ТОЛЬКО дата-время первого проведения документа (не его создания или последующих перепроведений), иначе возможны ситуации с минусовыми остатками и, как следствие - ошибочной средней себестоимостью
                        is_completed+
                        ")" + // при отмене проведения или повторном проведении срабатывает ключ уникальности записи в БД по doc_type_id, doc_id, product_id
                        " ON CONFLICT ON CONSTRAINT product_history_uq" +// "upsert"
                        " DO update set " +
                        (is_completed?(" change = " + change +", "):" ") + // только если проводим
                        (is_completed?(" price = " + price +", "):" ") + // только если проводим
                        (is_completed?(" netcost = " + netcost +", "):" ") + // только если проводим
                        " is_completed = " + is_completed; // единственный апдейт при отмене проведения
        try {
            // проверки что по API прислали id-шники от своего master_id (что в таблице есть совпадение таких masterId и id-шников )
            if( Objects.isNull(commonUtilites.getFieldValueFromTableById("companies","id", masterId, company_id)) ||
                    Objects.isNull(commonUtilites.getFieldValueFromTableById("products","id", masterId, product_id)) )
                throw new Exception("id's of the objects don't belong to their master_id. master_id="+masterId+", company_id="+company_id+", product_id="+product_id);

            // additional safety checks
            commonUtilites.idBelongsMyMaster("departments", department_id, masterId);
            commonUtilites.idBelongsMyMaster("products", product_id, masterId);

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method ProductsRepository/setProductHistory. SQL-"+stringQuery, e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")  // возвращает значения из последней строки истории изменений товара в отделении (если department_id = null то не зависимо от отделения)
    public ProductHistoryJSON  getLastProductHistoryRecord(Long product_id, Long department_id)
    {
        String stringQuery;
        stringQuery =
                " select                                        "+
//                        " coalesce(last_purchase_price,0)   as last_purchase_price, "+
//                        " coalesce(avg_purchase_price,0)    as avg_purchase_price,  "+
                        " coalesce((select avg_netcost_price from product_quantity where department_id="+department_id+" and product_id="+product_id+"),0)     as avg_netcost_price,   "+
                        " coalesce(price,0)  as price,"+
                        " coalesce((select quantity from product_quantity where department_id="+department_id+" and product_id="+product_id+"),0)     as quantity,   "+
                        " coalesce(change,0)                as change,               "+
                        "coalesce((select ph.price from product_history ph  where ph.department_id = " + department_id +" and ph.product_id = "+product_id+" and ph.doc_type_id= 15 and ph.is_completed=true order by ph.date_time_created desc limit 1),0) as lastPurchasePrice "+


        "          from product_history                "+
                        "          where                                "+
                        "          product_id="+product_id;
        if(!Objects.isNull(department_id))
            stringQuery = stringQuery+"          and department_id="+department_id;
        stringQuery = stringQuery+ "          order by id desc limit 1             ";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            ProductHistoryJSON returnObj=new ProductHistoryJSON();
            if(queryList.size()==0){//если записей истории по данному товару ещё нет
                returnObj.setLast_purchase_price(       (new BigDecimal(0)));
//                returnObj.setAvg_purchase_price(        (new BigDecimal(0)));
                returnObj.setAvg_netcost_price(         (new BigDecimal(0)));
                returnObj.setPrice(                     (new BigDecimal(0)));
                returnObj.setQuantity(                  (new BigDecimal(0)));
                returnObj.setChange(                    (new BigDecimal(0)));
            }else {
                for (Object[] obj : queryList) {
//                    returnObj.setLast_purchase_price((BigDecimal)   obj[0]);
//                    returnObj.setAvg_purchase_price((BigDecimal)    obj[1]);
                    returnObj.setAvg_netcost_price((BigDecimal)     obj[0]);
                    returnObj.setPrice((BigDecimal)                 obj[1]);
                    returnObj.setQuantity((BigDecimal)              obj[2]);
                    returnObj.setChange((BigDecimal)                obj[3]);
                    returnObj.setLast_purchase_price((BigDecimal)   obj[4]);
                }
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getLastProductHistoryRecord. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    // возвращает значения количества и себестоимости товара (если department_id = null то не зависимо от
    // отделения - т.е. всё кол-во товара на предприятии и последнее вычисленное изменение)
    public ProductHistoryJSON  getProductQuantityAndNetcost(Long masterId, Long companyId, Long productId, Long departmentId) throws Exception {
        String stringQuery;
        stringQuery =
                " select sum(change) from product_history where "+
                        "   master_id = " + masterId +
                        "   and company_id = " + companyId +
                        (Objects.isNull(departmentId)?" ":" and department_id=" + departmentId) +
                        "   and product_id=" + productId +
                        "   and is_completed = true ";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            Object queryList = query.getSingleResult();

            ProductHistoryJSON returnObj=new ProductHistoryJSON();
            if(Objects.isNull(queryList)){//если записей истории по данному товару ещё нет
                returnObj.setAvg_netcost_price(         (new BigDecimal(0)));
                returnObj.setQuantity(                  (new BigDecimal(0)));
            }else {
                returnObj.setAvg_netcost_price(recountProductNetcost(companyId, departmentId, productId));
                returnObj.setQuantity((BigDecimal) queryList);
            }
            return returnObj;
        }
        catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method getProductQuantityAndNetcost->recountProductNetcost. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        }
        catch (Exception e) {
            logger.error("Exception in method getProductQuantityAndNetcost. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


    @SuppressWarnings("Duplicates")  // возвращает кол-во товара в отделении (или во всех отделениях предприятия, если departmentId == null )
    public BigDecimal getProductQuantity(Long masterId, Long companyId, Long productId, Long departmentId) throws Exception {
        String stringQuery;
        stringQuery =

                " select sum(change) from product_history where "+
                        "   master_id = " + masterId +
                        "   and company_id = " + companyId +
                        (Objects.isNull(departmentId)?"":("   and department_id=" + departmentId)) +
                        "   and product_id=" + productId +
                        "   and is_completed = true ";

        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<BigDecimal> queryList = query.getResultList();

            ProductHistoryJSON returnObj=new ProductHistoryJSON();
            if(queryList.size()==0){//если записей истории по данному товару ещё нет
                returnObj.setQuantity(                  (new BigDecimal(0)));
            }else {
                for (BigDecimal obj : queryList) {
                    returnObj.setQuantity(Objects.isNull(obj)?new BigDecimal(0):obj);
                }
            }
            return returnObj.getQuantity();
        }
        catch (Exception e) {
            logger.error("Exception in method geProductQuantity. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    private Boolean wasOperationsOnProduct(Long productId) throws Exception {
        // category is online-store category if it marked as "is_store_category" and it has selected internet stores
        String stringQuery = "select count(*) from product_history where product_id="+productId+" and coalesce(is_completed,false) = true";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger)query.getSingleResult()).longValue() > 0L;
        } catch (Exception e) {
            logger.error("Exception in method wasOperationsOnProduct. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    // Считает себестоимость товара по проведенным операциям
    @SuppressWarnings("Duplicates")
    BigDecimal recountProductNetcost(Long companyId, Long departmentId, Long productId) throws Exception {

        String stringQuery =
                "   select " +
                        "   change as change, " +
                        "   netcost as netcost, " +
                        "   doc_type_id as doc_type_id" +
                        "   from product_history                "+
                        "   where                                "+
                        "   company_id = " + companyId +
                        (Objects.isNull(departmentId)?" ":"  and department_id=" + departmentId) +
                        "   and is_completed = true " +
                        "   and product_id=" + productId +
                        "   order by id asc";
        try{

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            BigDecimal lastAvgNetcostPrice = new BigDecimal(0); // себестоимость
            BigDecimal availableQuantity = new BigDecimal(0); // имеющееся количество

            if(queryList.size()>0)
                for (Object[] obj : queryList) {
                    BigDecimal change = (BigDecimal)obj[0];
                    BigDecimal netcost  = (BigDecimal)obj[1];

                    if(availableQuantity.compareTo(new BigDecimal(0))>=0) {


                        if (change.compareTo(new BigDecimal(0)) > 0) // пересчитываем себестоимость только для документов поступления (Приёмка, Оприходование, Возврат покупателя)
                            // новая средняя себестоимость = ((ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
                            lastAvgNetcostPrice = ((availableQuantity.multiply(lastAvgNetcostPrice)).add(change.multiply(netcost))).divide(availableQuantity.add(change), 2, BigDecimal.ROUND_HALF_UP);


                    } else throw new CalculateNetcostNegativeSumException();

                    availableQuantity=availableQuantity.add(change);
                }

            // If in a result of recounting there is no products - the Average net cost price >0 is no matter
            // else can be the situation when you sold all products, but the Average net cost price > 0
            // because it counting only for operations of adding products (when change>0)
            if(availableQuantity.compareTo(new BigDecimal(0))==0)
                return  new BigDecimal(0);

            return lastAvgNetcostPrice;
        }
        catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        }
        catch (Exception e) {
            logger.error("Exception in method recountProductNetcost. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // Считает себестоимость товара по проведенным операциям на время timeBefore
    @SuppressWarnings("Duplicates")
    private BigDecimal recountProductNetcost(Long companyId, Long departmentId, Long productId, Timestamp timeBefore) throws Exception {
        BigDecimal lastAvgNetcostPrice = new BigDecimal(0); // себестоимость
        BigDecimal availableQuantity = new BigDecimal(0); // имеющееся количество
        BigDecimal change = new BigDecimal(0) ;
        BigDecimal netcost;
        int doc_type_id = 0;
        long doc_id = 0L;
        String stringQuery =
                "   select " +
                        "   change as change, " +
                        "   netcost as netcost, " +
                        "   doc_type_id as doc_type_id," +
                        "   doc_id as doc_id " +
                        "   from product_history                "+
                        "   where                                "+
                        "   company_id = " + companyId +
                        (Objects.isNull(departmentId)?" ":"  and department_id=" + departmentId) +
                        "   and is_completed = true " +
                        "   and product_id=" + productId +
                        "   and date_time_created <= '" + timeBefore + "'" +
//                        "   order by date_time_created asc";
                        "   order by id asc";
        try{

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();



            if(queryList.size()>0)
                for (Object[] obj : queryList) {
                    change = (BigDecimal)obj[0];
                    netcost  = (BigDecimal)obj[1];
                    doc_type_id = (Integer) obj[2];
                    doc_id = ((BigInteger) obj[3]).longValue();

                    if(availableQuantity.compareTo(new BigDecimal(0))>=0) {



                            if (change.compareTo(new BigDecimal(0)) > 0) // пересчитываем себестоимость только для документов поступления (Приёмка, Оприходование, Возврат покупателя)
                                // новая средняя себестоимость = ((ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
                                lastAvgNetcostPrice = ((availableQuantity.multiply(lastAvgNetcostPrice)).add(change.multiply(netcost))).divide(availableQuantity.add(change), 2, BigDecimal.ROUND_HALF_UP);


                    }
//                    else return new BigDecimal(0);
                    else throw new CalculateNetcostNegativeSumException();

                    availableQuantity=availableQuantity.add(change);
                }

            // If in a result of recounting there is no products - the Average net cost price >0 is no matter
            // else can be the situation when you sold all products, but the Average net cost price > 0
            // because it counting only for operations of adding products (when change > 0)
            if(availableQuantity.compareTo(new BigDecimal(0))==0)
                return  new BigDecimal(0);

            return lastAvgNetcostPrice;
        }
        catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost. companyId: "+companyId+", departmentId: "+departmentId+", productId: "+productId+", doc_type_id: "+doc_type_id+", doc_id: "+doc_id+", availableQuantity: "+availableQuantity+", change: "+change+"SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        }
        catch (Exception e) {
            logger.error("Exception in method recountProductNetcost. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


    // Mass addind categories to products
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Boolean setCategoriesToProducts(Set<Long> productsIds, Set<Long> categoriesIds, Boolean save) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        try{
            String products = commonUtilites.SetOfLongToString(productsIds, ",", "", "");
            //поверка на то, что присланные id товаров действительно являются товарами мастер-аккаунта
            if (securityRepositoryJPA.isItAllMyMastersDocuments("products", products)) {
                if (!save) {//если не нужно сохранять те категории у товара, которые уже есть
                    //удаляем все категории у всех запрашиваемых товаров
                    if (deleteAllProductsCategories(products)) {
                        //назначаем товарам категории
                        if(setCategoriesToProducts(productsIds,categoriesIds,myMasterId))
                            return true;
                        else return null; // ошибка на прописывании категорий у товаров
                    } else return null; // ошибка на стадии удаления категорий товаров в deleteAllProductsCategories
                } else {//нужно сохранить предыдущие категории у товаров. Тут уже сложнее - нужно отдельно работать с каждым товаром
                    //цикл по товарам
                    for (Long p : productsIds) {
                        //получим уже имеющиеся категории у текущего товара
                        Set<Long> productCategoriesIds=getProductCategoriesIds(p);
                        //дополним их новыми категориями
                        if (productCategoriesIds != null) {
                            productCategoriesIds.addAll(categoriesIds);
                        }
                        //удалим старые категории
                        if (deleteAllProductsCategories(p.toString())) {
                            Set<Long> prod = new HashSet<>();
                            prod.add(p);
                            //назначаем текущему товару категории
                            if(!setCategoriesToProducts(prod,productCategoriesIds,myMasterId))
                                return null; // ошибка на прописывании категорий у товара
                        } else return null; // ошибка на стадии удаления категорий текущего товара в deleteAllProductsCategories
                    }
                    return true;
                }
            } else return null; // не прошли по безопасности - подсунуты "левые" id товаров
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method setCategoriesToProducts", e);
            e.printStackTrace();
            return null;
        }
    }
    private Boolean setCategoriesToProducts(Set<Long> productsIds, Set<Long> categoriesIds, Long myMasterId) throws Exception {
        if(categoriesIds.size()>0) {//если категории есть
            //прописываем их у всех запрашиваемых товаров
            StringBuilder stringQuery = new StringBuilder("insert into product_productcategories (product_id, category_id) values ");
            int i = 0;
            try {
                for (Long p : productsIds) {
                    for (Long c : categoriesIds) {
                        commonUtilites.idBelongsMyMaster("products", p, myMasterId);
                        commonUtilites.idBelongsMyMaster("product_categories", c, myMasterId);
                        stringQuery.append(i > 0 ? "," : "").append("(").append(p).append(",").append(c).append(")");
                        i++;
                    }
                }

                entityManager.createNativeQuery(stringQuery.toString()).executeUpdate();
                markProductsAsNeedToSyncWoo(productsIds, userRepositoryJPA.getMyMasterId());
                return true;
            } catch (Exception e) {
                logger.error("Exception in method setCategoriesToProducts. SQL query:" + stringQuery, e);
                e.printStackTrace();
                throw new Exception();
            }
        } else return true;
    }
    private Boolean deleteAllProductsCategories(String products) throws Exception {
        String stringQuery = "delete from product_productcategories where product_id in("+products.replaceAll("[^0-9\\,]", "")+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteAllProductsCategories. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // Multiple adding online stores to categoties
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Boolean setStoresToCategories(Set<Long> categoriesIds, Set<Long> storesIds, Long companyId, Boolean save) {
        try {
            String categories = commonUtilites.SetOfLongToString(categoriesIds, ",", "", "");
            //поверка на то, что присланные id категорий действительно являются категориями мастер-аккаунта
            if (securityRepositoryJPA.isItAllMyMastersDocuments("product_categories", categories) &&
                    (storesIds.size() == 0 || (securityRepositoryJPA.isItAllMyMastersDocuments("stores", commonUtilites.SetOfLongToString(storesIds, ",", "", "")))) &&
                    securityRepositoryJPA.isItAllMyMastersDocuments("companies", companyId.toString())) {
                Long masterId = userRepositoryJPA.getMyMasterId();
                if (!save) {//Если не нужно сохранять те магазины у категории, которые уже есть
                    //удаляем все магазины у всех запрашиваемых категорий
                    deleteAllCategoriesStores(categories);
                    //назначаем категориям магазины
                    setStoresToCategories(categoriesIds, storesIds, masterId, companyId);
                    setProductCategoriesAsStoreOrUnstore(categoriesIds, !(storesIds.size() == 0), masterId, companyId);
                } else {//Если нужно сохранить предыдущие магазины у категорий. Отдельно работаем с каждой категорией
                    //цикл по категориям
                    for (Long p : categoriesIds) {
                        //получим уже имеющиеся магазины у текущей категории
                        Set<Long> categoryStoresIds = getCategoryStoresIds(p);
                        //дополним их новыми магазинами
                        categoryStoresIds.addAll(storesIds);
                        //удалим старые магазины
                        deleteAllCategoriesStores(p.toString());
                        Set<Long> category = new HashSet<>();
                        category.add(p);
                        //назначаем текущей категории магазины
                        setStoresToCategories(category, categoryStoresIds, masterId, companyId);
                        setProductCategoriesAsStoreOrUnstore(category, !(categoryStoresIds.size() == 0), masterId, companyId);
                    }
                }

                // Now need to mark all products of these categories as need to be synchronized
                if (storesIds.size() > 0)
                    markProductsOfCategoriesAndStoresAsNeedToSyncWoo(new HashSet<>(categoriesIds), masterId, new ArrayList<>(storesIds));
                markAllCategoriesAsNeedToSyncWoo(companyId, masterId);

                return true;
            } else {
                logger.error("Not all IDs are master's account IDs");
                return null; // не прошли по безопасности - подсунуты "левые" id категорий
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method setStoresToCategories", e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean setStoresToCategories(Set<Long> categoriesIds, Set<Long> storesIds, Long masterId, Long companyId) throws Exception {
        if(storesIds.size()>0) {//если магазины есть
            //прописываем их у всех запрашиваемых категорий
            StringBuilder stringQuery = new StringBuilder("insert into stores_productcategories (category_id, store_id, master_id, company_id) values ");
            int i = 0;
            for (Long p : categoriesIds) {
                for (Long c : storesIds) {

                    commonUtilites.idBelongsMyMaster("stores", c, masterId);
                    commonUtilites.idBelongsMyMaster("product_categories", p, masterId);

                    stringQuery.append(i > 0 ? "," : "")
                            .append("(")
                            .append(p).append(",")
                            .append(c).append(",")
                            .append(masterId).append(",")
                            .append(companyId)
                            .append(")");
                    i++;
                }
            }
            try {
                entityManager.createNativeQuery(stringQuery.toString()).executeUpdate();
//                markProductsAsNeedToSyncWoo(categoriesIds, userRepositoryJPA.getMyMasterId());
                return true;
            } catch (Exception e) {
                logger.error("Exception in method setStoresToCategories. SQL query:" + stringQuery, e);
                e.printStackTrace();
                throw new Exception();
            }
        } else return true;
    }

    private Boolean deleteAllCategoriesStores(String categories) throws Exception {
        String stringQuery = "delete from stores_productcategories where category_id in("+categories.replaceAll("[^0-9\\,]", "")+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteAllCategoriesStores. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private void setProductCategoriesAsStoreOrUnstore(Set<Long> categoriesIds, boolean isStoreCategories, Long masterId, Long companyId) throws Exception {
        String stringQuery =
                " update product_categories set is_store_category = " + isStoreCategories +
                " where master_id="+masterId+
                " and company_id="+companyId+
                " and id in "+commonUtilites.SetOfLongToString(categoriesIds,",","(",")")+
                " and coalesce(is_store_category,false) != " + isStoreCategories;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method setProductCategoriesAsStoreOrUnstore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
//*****************************************************************************************************************************************************
//**********************************************  P R O D U C T   A T T R I B U T E S   ***************************************************************
//*****************************************************************************************************************************************************

    private Integer updateProductAttributes(List<ProductProductAttributeForm> attributesList, Long masterId, Long productId) throws Exception {
        if (attributesList.size() > 0) {
            String stringQuery;
            int i = 1;
            for (ProductProductAttributeForm attribute : attributesList) {


                stringQuery=
                        " insert into product_productattributes (" +
                        " master_id, " +
                        " product_id, " +
                        " attribute_id, " +
                        " position, " +
                        " visible, " +
                        " variation " +
                        " ) values (" +
                        masterId + ", " +
                        " (select id from products where id="+productId +" and master_id="+masterId+"), "+// чтобы не мочь переназначить цену товара другого master_id, случайно или намеренно
                        attribute.getAttribute_id() + ", " +
//                        " coalesce((select max(position)+1 from product_productattributes where product_id="+productId+"),1), " +
                        i + ", " +
                        attribute.isVisible() + ", "+
                        attribute.isVariation() + ")" +
                        " ON CONFLICT ON CONSTRAINT product_productattribute_uq " +// "upsert"
                        " DO update set " +
                        " position = " + i + ", " +
                        " visible = " + attribute.isVisible() + ", " +
                        " variation = " + attribute.isVariation();
                try{
                    commonUtilites.idBelongsMyMaster("product_attributes", attribute.getAttribute_id(), masterId);

                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    if(!Objects.isNull(attribute.getTerms_ids())) {
                        setProductTerms(attribute.getTerms_ids(),productId , masterId, attribute.getAttribute_id()); // add new terms
                        deleteTerms(attribute.getTerms_ids(),productId , masterId, attribute.getAttribute_id()); // delete terms that was deleted on frontend
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method updateProductAttributes. SQL query:" + stringQuery, e);
                    throw new Exception(e);
                }
                i++;
            }
        }
        return 1;
    }

    private void deleteAttributesFromProduct(List<ProductProductAttributeForm> attributesList, Long productId, Long masterId) throws Exception  {
        List<Long> attributesIds = new ArrayList<>();
        for(ProductProductAttributeForm attribute : attributesList){
            attributesIds.add(attribute.getAttribute_id());
        }
            String stringQuery =
                    " delete from product_productattributes " +
                            " where " +
                            " master_id = " + masterId + " and " +
                            " product_id = " +productId;
            if(attributesIds.size()>0) {
                stringQuery = stringQuery + " and ((attribute_id not in " + commonUtilites.ListOfLongToString(attributesIds,",","(",")") + ")" +
                " or " +
                "(attribute_id not in (select product_attribute_id from product_terms where product_id = "+productId+")))";
            }
            try {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
            } catch (Exception e) {
                logger.error("Exception in method deleteAttributesFromProduct. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw new Exception(e);
            }
    }
    private void deleteTermsOfDeletedAttributes(List<ProductProductAttributeForm> attributesList, Long productId, Long masterId) throws Exception  {
        List<Long> attributesIds = new ArrayList<>();
        for(ProductProductAttributeForm attribute : attributesList){
            attributesIds.add(attribute.getAttribute_id());
        }
        String stringQuery =
                " delete from product_terms " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " product_id = " +productId;
        if(attributesIds.size()>0) {
            stringQuery = stringQuery + " and product_attribute_id not in " + commonUtilites.ListOfLongToString(attributesIds,",","(",")");
        }
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteTermsOfDeletedAttributes. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    private void setProductTerms(List<Long> terms_ids, Long productId, Long masterId, Long attributeId) throws Exception {
        for (Long termId : terms_ids) {
            String stringQuery =
                    " insert into product_terms (" +
                            " master_id, " +
                            " product_id, " +
                            " product_attribute_id, " +
                            " term_id " +
                            " ) values (" +
                            masterId + ", " +
                            productId + ", " +
                            attributeId + ", " +
                            " (select id from product_attribute_terms where id=" + termId + " and master_id=" + masterId + ")) " +// чтобы не мочь переназначить на другого master_id, случайно или намеренно
                            " ON CONFLICT " +// "upsert"
                            " DO NOTHING ";
            try {
                commonUtilites.idBelongsMyMaster("product_attributes", attributeId, masterId);
                commonUtilites.idBelongsMyMaster("products", productId, masterId);
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method setProductTerms. SQL query:" + stringQuery, e);
                throw new Exception(e);
            }
        }
    }

    private void deleteTerms(List<Long> terms_ids, Long productId, Long masterId, Long attributeId) throws Exception  {
            String stringQuery =
                    " delete from product_terms " +
                    " where " +
                    " master_id = " + masterId + " and " +
                    " product_id = " +productId + " and " +
                    " product_attribute_id = " + attributeId;
            if(terms_ids.size()>0)
                stringQuery = stringQuery + " and term_id not in " + commonUtilites.ListOfLongToString(terms_ids,",","(",")");
            try {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
            } catch (Exception e) {
                logger.error("Exception in method deleteTerms. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw new Exception(e);
            }
    }


    public List<ProductProductAttributeJSON> getProductAttributes (Long product_id){
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        stringQuery =
            "           select " +
            "           p.name as name, " +
            "           coalesce(pp.visible, false) as visible, " +
            "           coalesce(pp.variation, false) as variation, " +
            "           p.id as id" +
            "           from product_attributes p " +
            "           INNER JOIN product_productattributes pp ON pp.attribute_id=p.id " +
            "           where  p.master_id=" + myMasterId +
            "           and pp.product_id =" + product_id +
            "           order by pp.position";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ProductProductAttributeJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductProductAttributeJSON doc = new ProductProductAttributeJSON();
                doc.setName((String)                obj[0]);
                doc.setVisible((Boolean)            obj[1]);
                doc.setVariation((Boolean)          obj[2]);
                doc.setTerms(getAllAttributeTermsWith(product_id,Long.parseLong(obj[3].toString()), myMasterId));
                doc.setAttribute_id(Long.parseLong( obj[3].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductAttributes. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private List<ProductAttributeTermsJSON> getAllAttributeTermsWith(Long productId, Long attrbuteId, Long masterId) throws Exception {
        String stringQuery =       "select " +
                "           p.id as id," +
                "           p.name as name, " +
                "           coalesce((select count(*) from product_terms where master_id = " + masterId + " and product_id = " + productId + " and term_id = p.id),0) as is_selected" +
                "           from product_attribute_terms p " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           where  p.master_id=" + masterId +
                "           and p.attribute_id =" + attrbuteId +
                "           order by p.menu_order";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ProductAttributeTermsJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductAttributeTermsJSON doc = new ProductAttributeTermsJSON();
                doc.setId(Long.parseLong(           obj[0].toString()));
                doc.setName((String)                obj[1]);
                doc.setIs_selected(                 !(obj[2].toString()).equals("0"));// if this term is selected in the product - it will be true
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getAllAttributeTermsWith. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }
//*****************************************************************************************************************************************************
//**********************************************  P R O D U C T   VARIATIONS   ***************************************************************
//*****************************************************************************************************************************************************

    // Forming the set of attribute fields for "Default Form Values" selection in WooCommerce
    private Set<DefaultAttributesJSON> getDefaultAttributes(Long masterId, Long productId){
        String stringQuery =
                " select " +
                        " da.attribute_id as attribute_id," +
                        " coalesce(da.term_id,0) as term_id," +
                        " pa.name as attribute_name " +
                        " from  default_attributes da" +
                        " inner join product_attributes pa on pa.id = da.attribute_id" +
                        " where da.master_id = " + masterId + " and da.product_id = " + productId + " order by da.attribute_id";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> resultList = query.getResultList();
            Set<DefaultAttributesJSON> returnList = new HashSet<>();
            for(Object[] obj:resultList){
                DefaultAttributesJSON doc=new DefaultAttributesJSON();
                doc.setAttribute_id(Long.parseLong(                 obj[0].toString()));
                doc.setTerm_id(Long.parseLong(                      obj[1].toString()));
                doc.setName((String)                                obj[2]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getDefaultAttributes. SQL query:" + stringQuery, e);
            return null;
        }
    }

    // Updating the set of attribute fields for "Default Form Values" selection in WooCommerce
    private Integer updateDefaultAttributes(Set<DefaultAttributesForm> attributesList, Long productId, Long masterId) throws Exception {
        String stringQuery="";
        Set<Long>existingAttributes = new HashSet<>();
        try{
            for (DefaultAttributesForm attribute : attributesList) {
                commonUtilites.idBelongsMyMaster("product_attribute_terms", (attribute.getTerm_id()==0L?null:attribute.getTerm_id()), masterId);
                stringQuery=
                        " insert into default_attributes (" +
                                " master_id, " +
                                " product_id, " +       // variable (parent) product.
                                " attribute_id, " +     // default attribute
                                " term_id " +           // value of default attribute
                                " ) values (" +
                                masterId + ", " +
                                " (select id from products where id=" + productId +" and master_id = " + masterId+"), "+// чтобы не мочь переназначить цену товара другого master_id, случайно или намеренно
                                " (select id from product_attributes where id = " + attribute.getAttribute_id() + " and master_id="+masterId+"), "+//
                                (attribute.getTerm_id()==0L?null:attribute.getTerm_id()) + ")" +
                                " ON CONFLICT ON CONSTRAINT variation_default_attributes_uq " +// "upsert"
                                " DO update set " +
                                " term_id = " + (attribute.getTerm_id()==0L?null:attribute.getTerm_id());

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                existingAttributes.add(attribute.getAttribute_id());
            }
            deleteDefaultAttributes(existingAttributes, productId, masterId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method updateDefaultAttributes. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
        return 1;
    }

    // Deleting default attributes that no more existed in product card
    private void deleteDefaultAttributes(Set<Long> existingAttributes, Long productId, Long masterId) throws Exception  {
        String stringQuery =
                " delete from default_attributes " +
                " where " +
                " master_id = " + masterId + " and " +
                " product_id = " +productId;
        if(existingAttributes.size()>0)
            stringQuery = stringQuery + " and attribute_id not in " + commonUtilites.SetOfLongToString(existingAttributes,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteDefaultAttributes. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private List<ProductVariationsJSON> getProductVariations(Long masterId, Long productId){
        String stringQuery =
                " select " +
                " da.id as id," +
                " da.product_id as product_id," +
                " da.variation_product_id as variation_product_id," +
                " p.name as variation_product_name," +
                " da.menu_order as menu_order " +
                " from  product_variations da" +
                " inner join products p on p.id = da.variation_product_id" +
                " where da.master_id = " + masterId + " and da.product_id = " + productId + " order by da.menu_order";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> resultList = query.getResultList();
            List<ProductVariationsJSON> returnList = new ArrayList<>();
            for(Object[] obj:resultList){
                ProductVariationsJSON doc=new ProductVariationsJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setProduct_id(Long.parseLong(               obj[1].toString()));
                doc.setVariation_product_id(Long.parseLong(     obj[2].toString()));
                doc.setVariation_product_name((String)          obj[3]);
                doc.setProductVariationsRowItems(getProductVariationsRowItems(doc.getId(), masterId));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductVariations. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private List<ProductVariationsRowItemsJSON> getProductVariationsRowItems(Long variationId, Long masterId){
        String stringQuery =
                " select " +
                        " da.attribute_id as attribute_id," +
                        " coalesce(da.term_id,0) as term_id" +
                        " from  product_variations_row_items da" +
                        " where da.master_id = " + masterId + " and da.variation_id = " + variationId + " order by da.attribute_id";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> resultList = query.getResultList();
            List<ProductVariationsRowItemsJSON> returnList = new ArrayList<>();
            for(Object[] obj:resultList){
                ProductVariationsRowItemsJSON doc=new ProductVariationsRowItemsJSON();
                doc.setAttribute_id(Long.parseLong(                 obj[0].toString()));
                doc.setTerm_id(Long.parseLong(                      obj[1].toString()));
                doc.setVariation_id(variationId);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductVariationsRowItems. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private Integer updateProductVariations(List<ProductVariationsForm> variationsList, Long productId, Long masterId) throws Exception {
//        if (variationsList.size() > 0) {
        String stringQuery="";
            try{

                int i = 1;
                List<Long> variationsIds = new ArrayList<>();
                for (ProductVariationsForm variation : variationsList) {
                    if(isProductUsedAsVariation(productId, variation.getVariation_product_id())) throw new ProductAlreadyUsedAsVariation();
                    if(isProductVariable(variation.getVariation_product_id())) throw new VariableProductCantBeUsedAsVariation();
                    stringQuery=
                    " insert into product_variations (" +
                    " master_id, " +
                    " product_id, " + // variable (parent) product.
                    " variation_product_id, " + // variation of variable (parent) product.
                    " menu_order " +
                    " ) values (" +
                    masterId + ", " +
                    " (select id from products where id="+variation.getProduct_id() +" and master_id="+masterId+"), "+// чтобы не мочь переназначить цену товара другого master_id, случайно или намеренно
                    " (select id from products where id="+variation.getVariation_product_id() +" and master_id="+masterId+"), "+//
                    i + ")" +
                    " ON CONFLICT ON CONSTRAINT variation_product_id_uq " +// "upsert"
                    " DO update set " +
//                                " variation_product_id = (select id from products where id="+variation.getVariation_product_id() +" and master_id="+masterId+")" + ", " +
//                    " menu_order = " + variation.getMenu_order();
                    " menu_order = " + i;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    stringQuery="select id from product_variations where master_id="+masterId+" and product_id = "+variation.getProduct_id()+" and variation_product_id = "+variation.getVariation_product_id();
                    query = entityManager.createNativeQuery(stringQuery);
                    Long variationId=Long.valueOf(query.getSingleResult().toString());
                    variationsIds.add(variationId);
                    if(!Objects.isNull(variation.getProductVariationsRowItems())) {
                        setProductVariationAttributes(variation.getProductVariationsRowItems(),masterId, variationId);// add new attribute values, update existing, remove non-existing
                    }

                    i++;
                }
            deleteVariations(variationsIds, productId, masterId); // delete variations that was deleted on frontend
            } catch (ProductAlreadyUsedAsVariation e) {
                e.printStackTrace();
                logger.error("ProductAlreadyUsedAsVariation exception in method updateProductVariations. SQL query:" + stringQuery, e);
                throw new ProductAlreadyUsedAsVariation();
            } catch (VariableProductCantBeUsedAsVariation e) {
                e.printStackTrace();
                logger.error("VariableProductCantBeUsedAsVariation exception in method updateProductVariations. SQL query:" + stringQuery, e);
                throw new VariableProductCantBeUsedAsVariation();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method updateProductVariations. SQL query:" + stringQuery, e);
                throw new Exception(e);
            }

//        }
        return 1;
    }
    private boolean isProductVariable(long productId) throws Exception {
        try{
            String stringQuery="select count(*) from products where id="+productId+" and type='variable'";
            Query query = entityManager.createNativeQuery(stringQuery);
            long cnt = ((BigInteger)query.getSingleResult()).longValue();
            return (cnt>0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method isProductVariable", e);
            throw new Exception(e);
        }
    }
    // if product is variation of any product
    private boolean isProductUsedAsVariation(long variationProductId) throws Exception {
        try{
            String stringQuery="select count(*) from product_variations where variation_product_id="+variationProductId;
            Query query = entityManager.createNativeQuery(stringQuery);
            long cnt = ((BigInteger)query.getSingleResult()).longValue();
            return (cnt>0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method isProductUsedAsVariation(1).", e);
            throw new Exception(e);
        }
    }

    // if product is variation of productId product
    private boolean isProductUsedAsVariation(long productId, long variationProductId) throws Exception {
        try{
            String stringQuery="select count(*) from product_variations where variation_product_id="+variationProductId+" and product_id !="+productId;
            Query query = entityManager.createNativeQuery(stringQuery);
            long cnt = ((BigInteger)query.getSingleResult()).longValue();
            return (cnt>0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method isProductUsedAsVariation(2).", e);
            throw new Exception(e);
        }
    }



    private void setProductVariationAttributes(List<ProductVariationsRowItemsForm> productVariationsRowItems, Long masterId, Long variationId) throws Exception {
        String stringQuery="";
        try {
            List<Long> itemsIds = new ArrayList<>();
            commonUtilites.idBelongsMyMaster("product_variations", variationId, masterId);

            for (ProductVariationsRowItemsForm rowItems : productVariationsRowItems) {

                 stringQuery =
                        " insert into product_variations_row_items (" +
                        " master_id, " +
                        " variation_id, " +
                        " attribute_id, " +
                        " term_id " +
                        " ) values (" +
                        masterId + ", " +
                        variationId + ", " +
                        " (select id from product_attributes where id="+rowItems.getAttribute_id()+" and master_id="+masterId+")," +
                        // Attrinute value can be "Any". In this case incomes 0. Should be null in DB :
                        (rowItems.getTerm_id()==0?null:(" (select id from product_attribute_terms where id="+rowItems.getTerm_id()+" and master_id="+masterId+")")) +
                        " )" +
                        " ON CONFLICT ON CONSTRAINT variation_row_attribute_id_uq" +// "upsert on unique variation_id, attribute_id"
                        " DO update set " +
                        " term_id = (select id from product_attribute_terms where id="+rowItems.getTerm_id()+" and master_id="+masterId+")";

                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                stringQuery="select id from product_variations_row_items where master_id="+masterId+" and attribute_id = "+rowItems.getAttribute_id()+" and variation_id = "+variationId;
                query = entityManager.createNativeQuery(stringQuery);
                Long rowItemId=Long.valueOf(query.getSingleResult().toString());
                itemsIds.add(rowItemId);
            }
            deleteVariationsRowItems(itemsIds, variationId, masterId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method setProductVariationAttributes. SQL query: " + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void deleteVariations(List<Long> existingVariationsIds, Long productId, Long masterId) throws Exception  {
        String stringQuery =
                " delete from product_variations " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " product_id = " +productId;
        if(existingVariationsIds.size()>0)
            stringQuery = stringQuery + " and id not in " + commonUtilites.ListOfLongToString(existingVariationsIds,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteVariations. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private void deleteVariationsRowItems(List<Long> existingItemsIds, Long variation_id, Long masterId) throws Exception  {
        String stringQuery =
                " delete from product_variations_row_items " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " variation_id = " +variation_id;
        if(existingItemsIds.size()>0)
            stringQuery = stringQuery + " and id not in " + commonUtilites.ListOfLongToString(existingItemsIds,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteVariationsRowItems. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }



















    // returns list of product data (name, sku, description, ... and product's price by priceTypeId)
    public List<ProductLabel> getProductLabelsList(List<LabelsPrintProduct> labelsPrintProductList , Long priceTypeId, Long companyId){
        List<Long> productIds = labelsPrintProductList.stream().map(LabelsPrintProduct::getProduct_id).collect(Collectors.toList());
        UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
        String myTimeZone = userSettings.getTime_zone();
        String dateFormat = userSettings.getDateFormat();
//        String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
        String stringQuery = " select " +
                "           p.id as product_id, " +
                "           p.name as name, " +
                "           p.article as article, " +
                "           cmp.name as company, " +
                "           to_char(p.product_code_free,'fm0000000000') as product_code, " +
                "           p.label_description as label_description," +
                "           coalesce((select value from product_barcodes where product_id = p.id order by id asc limit 1),'') as barcode," +
                "           coalesce((select name_short  from sprav_currencies where company_id="+companyId+" and is_default=true limit 1),'') as currency," +
                "           coalesce((select short_name  from sprav_sys_edizm where company_id="+companyId+" and id=coalesce(p.edizm_id,0)),'') as unit," +
                "           coalesce((select price_value from product_prices   where product_id=p.id and price_type_id="+priceTypeId+"),0.00) as price," +
                "           to_char(now() at time zone '" + myTimeZone + "', '"+dateFormat+"') as date " +
                "           from products p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           where  p.company_id=" + companyId +
                "           and p.id in " + commonUtilites.ListOfLongToString(productIds, ",","(", ")");
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            List<ProductLabel> returnList = new ArrayList<>();
//            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
//            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
//
//            symbols.setGroupingSeparator('*');
//            formatter.setDecimalFormatSymbols(symbols);

            int currentLabelQuantity=1;
            String priceTagIntDecDelimitter = ".";
            String priceTagThousandsDelimitter = " ";
            for(Object[] obj:queryList){
                ProductLabel doc=new ProductLabel();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setProductName((String)                     obj[1]);
                doc.setSku((String)                             obj[2]);
                doc.setCompanyName((String)                     obj[3]);
                doc.setProductCode((String)                     obj[4]);
                doc.setLabelDescription((String)                obj[5]);
                doc.setBarcode((String)                         obj[6]);
                doc.setCurrency((String)                        obj[7]);
                doc.setShortUnit((String)                       obj[8]);
//                doc.setPriceFull(((BigDecimal)                  obj[9]).toString());
//                doc.setPriceFull(new DecimalFormat("#,###.00").format(Double.parseDouble(((BigDecimal)obj[9]).toString())).replace(",", "."));
                doc.setPriceIntegerPart(new DecimalFormat("#,###.##").format(Double.parseDouble(((BigDecimal)obj[9]).toBigInteger().toString())).replace(",", priceTagThousandsDelimitter));
                doc.setPriceDecimalPart(((BigDecimal)obj[9]).toString().split("\\.")[1]);
                doc.setPriceFull(doc.getPriceIntegerPart()+priceTagIntDecDelimitter+doc.getPriceDecimalPart());
                doc.setDate((String)                            obj[10]);

                for(LabelsPrintProduct lpp:labelsPrintProductList){
                    if(doc.getId().equals(lpp.getProduct_id()))
                        currentLabelQuantity=lpp.getLabels_quantity();
                }

                for (int i = 0; i < currentLabelQuantity; i++) returnList.add(doc);

            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductLabelsList. SQL query:" + stringQuery, e);
            return null;
        }

    }


    // **********************  A L L   A B O U T   A S S I G N M E N T S  **********************


    private void saveProductResourcesQtt(Long master_id, Long resource_id, Long product_id, int quantity) throws Exception {
        String stringQuery = "insert into scdl_product_resource_qtt (" +
                "   master_id," +
                "   resource_id," +
                "   product_id," +
                "   quantity " +
                "   ) values (" +
                master_id+", "+
                resource_id+", "+
                product_id+", " +
                quantity +
                ") ON CONFLICT ON CONSTRAINT scdl_product_resource_qtt_uq " +// "upsert"
                "   DO update set " +
                "   quantity = " + quantity;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveProductResourcesQtt. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    // Deleting resources that no more contain this product
    private void deleteResourcesThatNoMoreContainedInThisProduct(Set<Long> existingResources, Long productId, Long masterId) throws Exception  {
        String stringQuery =
                " delete from scdl_product_resource_qtt " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " product_id = " +productId;
        if(existingResources.size()>0)
            stringQuery = stringQuery + " and resource_id not in " + commonUtilites.SetOfLongToString(existingResources,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteResourcesThatNoMoreContainedInThisProduct. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private List<ResourceJSON> getProductResources(Long masterId, Long productId){
        String stringQuery =
                        " select " +
                        " da.resource_id as id," +
                        " da.quantity    as resource_qtt," +
                        " p.name         as resource_name," +
                        " p.description  as resource_description" +
                        " from  scdl_product_resource_qtt da" +
                        " inner join sprav_resources p on p.id = da.resource_id" +
                        " where da.master_id = " + masterId + " and da.product_id = " + productId + " order by p.name";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> resultList = query.getResultList();
            List<ResourceJSON> returnList = new ArrayList<>();
            for(Object[] obj:resultList){
                ResourceJSON doc=new ResourceJSON();
                doc.setResource_id(Long.parseLong(              obj[0].toString()));
                doc.setResource_qtt((Integer)                   obj[1]);
                doc.setName((String)                            obj[2]);
                doc.setDescription((String)                     obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductResources. SQL query:" + stringQuery, e);
            return null;
        }
    }
    public List<SpravSysAssignmentsJSON> getSpravSysAssignmentsList(){
        try{
            Map<String, String> map = commonUtilites.translateForMe(new String[]{"'customer_on_website'","'manually'"});
            List<SpravSysAssignmentsJSON> returnList = new ArrayList<>();
            returnList.add(new SpravSysAssignmentsJSON("customer", map.get("customer_on_website")));
            returnList.add(new SpravSysAssignmentsJSON("manually", map.get("manually")));
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSpravSysAssignmentsList.", e);
            return null;
        }
    }

    public List<SpravSysRemindersJSON> getSpravSysRemindersList(){
        Map<String, String> map = commonUtilites.translateForMe(new String[]{
                    "'5_minutes_before_start'",
                    "'10_minutes_before_start'",
                    "'15_minutes_before_start'",
                    "'30_minutes_before_start'",
                    "'1_hour_before_start'",
                    "'2_hour_before_start'",
                    "'4_hour_before_start'",
                    "'1_day_before_start'"
        });

        String stringQuery = "select id, type, value_text, value_seconds from sprav_sys_scdl_reminders where is_active=true order by id";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> resultList = query.getResultList();
            List<SpravSysRemindersJSON> returnList = new ArrayList<>();
            for(Object[] obj:resultList){
                SpravSysRemindersJSON doc=new SpravSysRemindersJSON();
                doc.setId((Integer)                             obj[0]);
                doc.setType((String)                            obj[1]);
                doc.setValue_text(map.get((String)              obj[2]));
                doc.setValue_seconds((Integer)                  obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSpravSysRemindersList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private void saveAppointmentReminders(Long master_id, Long product_id, List<Integer> reminders, String type) throws Exception {
        // type: "customer" / "employee"
        String stringQuery = "";
        Set<Integer> existingRemindersIds = new HashSet<>();
        for (Integer reminderId : reminders) {

            stringQuery = stringQuery+
                    "   insert into scdl_reminders (" +
                    "   master_id," +
                    "   product_id," +
                    "   reminder_id," +
                    "   type " +
                    "   ) values (" +
                    master_id+", "+
                    product_id+", "+
                    reminderId+", '" +
                    type + "'" +
                    "   ) ON CONFLICT ON CONSTRAINT scdl_reminders_uq " +// "upsert"
                    "   DO NOTHING; ";

            existingRemindersIds.add(reminderId);
        }

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            deleteAppointmentRemindersThatNoMoreContainedInThisProduct(existingRemindersIds, product_id, master_id, type);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveAppointmentReminders. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    // Deleting reminders that no more contain this product
    private void deleteAppointmentRemindersThatNoMoreContainedInThisProduct(Set<Integer> existingRemindersIds, Long productId, Long masterId, String type) throws Exception  {
        String stringQuery =
                " delete from scdl_reminders " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " product_id = " +productId + " and " +
                        " type = '" + type +"'";
        if(existingRemindersIds.size()>0)
            stringQuery = stringQuery + " and reminder_id not in " + commonUtilites.SetOfIntToString(existingRemindersIds,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteAppointmentRemindersThatNoMoreContainedInThisProduct. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private List<Integer> getAppointmentReminders(Long masterId, Long productId, String type) throws Exception {
        String stringQuery =
                " select reminder_id from scdl_reminders " +
                        " where master_id = " + masterId +
                        " and product_id = " + productId +
                        " and type = '" + type + "'";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (List<Integer>) query.getResultList();
        } catch (Exception e) {
            logger.error("Exception in method getAppointmentReminders. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private void saveAppointmentAssignments(Long master_id, Long product_id, List<String> assignments) throws Exception {
        // assignments can be: "customer" / "manually"
        String stringQuery = "";
        Set<String> existingAssignmentsIds = new HashSet<>();
        for (String assignment_type : assignments) {

            if (VALID_ASSIGNMENT_TYPES.contains(assignment_type)) {

                stringQuery = stringQuery+
                        "   insert into scdl_assignments (" +
                        "   master_id," +
                        "   product_id," +
                        "   assignment_type" +
                        "   ) values (" +
                        master_id+", "+
                        product_id+", '"+
                        assignment_type + "'" +
                        "   ) ON CONFLICT ON CONSTRAINT scdl_assignments_uq " +// "upsert"
                        "   DO NOTHING; ";

                existingAssignmentsIds.add(assignment_type);

            } else {
                throw new IllegalArgumentException("Invalid assignment type '" + assignment_type + "'");
            }

        }

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            deleteAppointmentAssignmentsThatNoMoreContainedInThisProduct(existingAssignmentsIds, product_id, master_id);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveAppointmentReminders. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    // Deleting reminders that no more contain this product
    private void deleteAppointmentAssignmentsThatNoMoreContainedInThisProduct(Set<String> existingAssignments, Long productId, Long masterId) throws Exception  {
        String stringQuery =
                " delete from scdl_assignments " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " product_id = " +productId;
        if(existingAssignments.size()>0)
            stringQuery = stringQuery + " and assignment_type not in " + commonUtilites.SetOfStringToString(existingAssignments,"','","('","')");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteAppointmentAssignmentsThatNoMoreContainedInThisProduct. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }


    private List<String> getAppointmentAssignments(Long masterId, Long productId) throws Exception {
        String stringQuery =
                " select assignment_type from scdl_assignments " +
                        " where master_id = " + masterId +
                        " and product_id = " + productId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (List<String>) query.getResultList();
        } catch (Exception e) {
            logger.error("Exception in method getAppointmentAssignments. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }











    // inserting base set of categories of new account or company
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertProductCategoriesFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'p_catg_myprods'","'p_catg_srvcs'","'p_catg_in_srvcs'","'prod_work_empl'","'prod_transp'","'prod_rent'","'prod_prolltax'","'prod_incomtax'","'prod_banking'","'prod_my_prod'","'prod_my_srvc'","'prod_accounting'","'um_piece'","'um_uncountable'","'tax_no_tax'"});
        String t = new Timestamp(System.currentTimeMillis()).toString();
        stringQuery = "insert into product_categories ( master_id,creator_id,company_id,date_time_created,parent_id,output_order,name) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,1,'"+map.get("p_catg_myprods")+"'),"+ // cagent_supplier
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,2,'"+map.get("p_catg_srvcs")+"'),"+ // cagent_customer
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,3,'"+map.get("p_catg_in_srvcs")+"');" +// cagent_taxoffce

                "insert into products (master_id, creator_id, company_id, date_time_created, name, ppr_id, indivisible, not_buy, not_sell, edizm_id, nds_id) values " +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_work_empl")+"' ,4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_transp")+"'    ,4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_rent")+"'      ,4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_prolltax")+"'  ,4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_incomtax")+"'  ,4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_banking")+"'   ,4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_accounting")+"',4,true,false,true, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_my_srvc")+"'   ,4,true,true,false, (select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_uncountable")+"'), (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'))," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("prod_my_prod")+"'   ,1,true,false,false,(select id from sprav_sys_edizm where company_id="+cId+" and name='"+map.get("um_piece")+"'),       (select id from sprav_taxes where company_id="+cId+" and name='"+map.get("tax_no_tax")+"'));" +

                "insert into product_productcategories (category_id,product_id) values " +
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_accounting")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_work_empl")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_transp")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_rent")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_prolltax")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_incomtax")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_in_srvcs")+"'),(select id from products where company_id="+cId+" and name = '"+map.get("prod_banking")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_srvcs")+"')   ,(select id from products where company_id="+cId+" and name = '"+map.get("prod_my_srvc")+"')),"+
                "((select id from product_categories where company_id="+cId+" and name = '"+map.get("p_catg_myprods")+"') ,(select id from products where company_id="+cId+" and name = '"+map.get("prod_my_prod")+"'));";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertCagentCategoriesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

}

