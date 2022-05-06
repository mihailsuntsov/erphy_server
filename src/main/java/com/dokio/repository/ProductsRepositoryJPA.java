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
import com.dokio.message.response.*;
import com.dokio.message.response.additional.*;
import com.dokio.model.*;
import com.dokio.model.Sprav.SpravSysEdizm;
import com.dokio.model.Sprav.SpravSysMarkableGroup;
import com.dokio.model.Sprav.SpravSysNds;
import com.dokio.model.Sprav.SpravSysPPR;
import com.dokio.repository.Exceptions.CalculateNetcostNegativeSumException;
import com.dokio.repository.Exceptions.CantSaveProductQuantityException;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private UserDetailsServiceImpl userService;
    @Autowired
    private CommonUtilites commonUtilites;

    // Инициализация логера
    private static final Logger logger = Logger.getLogger(ProductsRepositoryJPA.class);

    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("docName","price","change","quantity","last_operation_price","last_purchase_price","avg_purchase_price","avg_netcost_price","doc_number","name","status_name","product_count","is_completed","company","department","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    @Transactional
    @SuppressWarnings("Duplicates")
    public List<ProductsTableJSON> getProductsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int categoryId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Меню - таблица
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            String dateFormat=userRepositoryJPA.getMyDateFormat();
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
                    "           p.group_id as productgroup_id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pg.name as productgroup, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '" + myTimeZone + "', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '" + myTimeZone + "', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.not_buy, false) as not_buy, " +
                    "           coalesce(p.not_sell, false) as not_sell " +
                    "           from products p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
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
                        "upper(p.name) like upper('%" + searchString + "%') or " +
                        "upper(p.article) like upper('%" + searchString + "%') or " +
                        "(upper('" + searchString + "') in (select upper(value) from product_barcodes where product_id=p.id))  or " +
                        "to_char(p.product_code_free,'fm0000000000') like upper('%" + searchString + "%') or " +
                        "upper(pg.name) like upper('%" + searchString + "%')" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, ProductsTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getProductsSize(String searchString, int companyId, int categoryId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//"Группы товаров" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id, " +
                    "           pg.name as productgroup " +
                    "           from products p " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
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
                        "upper(p.name) like upper('%" + searchString + "%') or " +
                        "upper(p.article) like upper('%" + searchString + "%') or " +
                        "(upper('" + searchString + "') in (select upper(value) from product_barcodes where product_id=p.id))  or " +
                        "to_char(p.product_code_free,'fm0000000000') like upper('%" + searchString + "%') or " +
                        "upper(pg.name) like upper('%" + searchString + "%')" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery);

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public ProductsJSON getProductValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String dateFormat=userRepositoryJPA.getMyDateFormat();

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

                    "           p.article as article, " +
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
                    "           to_char(p.date_time_created at time zone '" + myTimeZone + "', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '" + myTimeZone + "', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.description as description, " +
                    "           coalesce(p.not_buy, false) as not_buy, " +
                    "           coalesce(p.indivisible, false) as indivisible, " +
                    "           coalesce(p.not_sell, false) as not_sell " +

                    "           from products p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where p.id= " + id +
                    "           and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductsJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                ProductsJSON response = (ProductsJSON) query.getSingleResult();
                return response;
            } catch (NoResultException nre) {
                logger.error("Exception in method getProductValues. SQL query:"+stringQuery, nre);
                return null;
            } catch (Exception e) {
                logger.error("Exception in method getProductValues. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    // апдейчу в 3 захода (3 метода), т.к. если делать все в одном методе, получаю ошибку Executing an update/delete query
    // т.к. транзакция entityManager не коммитится пока нет выхода из метода
    public boolean updateProducts(ProductsForm request){
        Long myMasterId = userRepositoryJPA.getMyMasterId();

        if (updateProductsWithoutOrders(request)) {                                      //метод 1

            //сохранение порядка поставщиков товара
            try
            {
                if (request.getCagentsIdsInOrderOfList().size() > 1) {
                    int c = 0;
                    for (Long field : request.getCagentsIdsInOrderOfList()) {
                        c++;
                        if (!saveChangeProductCagentsOrder(field, request.getId(), c)) {//         //метод 2
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Exception in method updateProducts. Этап сохранение порядка поставщиков товара.", e);
                e.printStackTrace();
                return false;
            }


            //сохранение порядка картинок товара
            try
            {
                if (request.getImagesIdsInOrderOfList().size() > 1) {
                    int i = 0;
                    for (Long field : request.getImagesIdsInOrderOfList()) {
                        i++;
                        if (!saveChangeProductImagesOrder(field, request.getId(), i)) {//         //метод 3
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Exception in method updateProducts. Этап сохранения порядка картинок товара", e);
                e.printStackTrace();
                return false;
            }


            // сохранение цен
            try
            {
                if (request.getProductPricesTable().size() > 0) {
                    for (ProductPricesJSON field : request.getProductPricesTable()) {
                        if (!savePrice(field.getPrice_type_id(), request.getId(), myMasterId, request.getCompany_id(), field.getPrice_value())) {//         //метод 4
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Exception in method updateProducts. Этап сохранения цен", e);
                e.printStackTrace();
                return false;
            }



            return true;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private boolean savePrice(Long priceTypeId, Long productId, Long myMasterId, Long companyId, BigDecimal priceValue){
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
                        (priceValue==null?"0":priceValue.toString()) + "," +
                        myMasterId + ", "+ companyId + ")" +
                        "ON CONFLICT ON CONSTRAINT product_prices_uq " +// "upsert"
                        " DO update set " +
                        "price_value = " + (priceValue==null?"0":priceValue.toString());
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method savePrice. SQL query:" + stringQuery, e);
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
//    @Transactional
    public boolean updateProductsWithoutOrders(ProductsForm request) {
        EntityManager emgr = emf.createEntityManager();
        Products document = emgr.find(Products.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate = securityRepositoryJPA.userHasPermissions_OR(14L, "170"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate = securityRepositoryJPA.userHasPermissions_OR(14L, "169"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany = (Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId = document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDoc = (DocumentMasterId.equals(myMasterId));// документ под юрисдикцией главного аккаунта

        if (((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                || (!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try {
                Long id = Long.valueOf(request.getId());
                Products product = emgr.find(Products.class, id);

                emgr.getTransaction().begin();

                product.setName(request.getName() == null ? "" : request.getName());
                product.setDescription(request.getDescription() == null ? "" : request.getDescription());
                product.setArticle(request.getArticle() == null ? "" : request.getArticle());

                if ((request.getProduct_code_free() == null ? 0L : request.getProduct_code_free()) > 0L) {
                    product.setProduct_code_free(request.getProduct_code_free());
                } else product.setProduct_code_free(generateFreeProductCode(request.getCompany_id()));

                //группа товаров
                if (request.getProductgroup_id() != null) {
                    ProductGroups pg = emgr.find(ProductGroups.class, request.getProductgroup_id());
                    product.setProductGroup(pg);
                }

                //категории товаров и услуг
                Set<Long> categories = request.getSelectedProductCategories();
                if (!categories.isEmpty()) { //если есть выбранные чекбоксы категорий
                    Set<ProductCategories> setCategoriesOfProduct = getCategoriesSetBySetOfCategoriesId(categories);
                    product.setProductCategories(setCategoriesOfProduct);
                } else { // если ни один чекбокс категорий не выбран
                    product.setProductCategories(null);
                }

                //код товара (до 99999)
                //product.setProduct_code(request.getProduct_code());
                //признак предмета расчёта
                if (request.getPpr_id() != null) {
                    SpravSysPPR ed = emgr.find(SpravSysPPR.class, request.getPpr_id());
                    product.setPpr(ed);
                }
                //весовой товар (Boolean)
                product.setBy_weight(request.isBy_weight());
                //единица измерения товара
                if (request.getEdizm_id() != null) {
                    SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getEdizm_id());
                    product.setEdizm(ed);
                }
                //НДС
                if (request.getNds_id() != null) {
                    SpravSysNds ed = emgr.find(SpravSysNds.class, request.getNds_id());
                    product.setNds(ed);
                }
                //Вес товара (приходит String, конвертим в BigDecimal)
                if (request.getWeight() != null && !request.getWeight().isEmpty() && request.getWeight().trim().length() > 0) {
                    product.setWeight(new BigDecimal(request.getWeight().replace(",", ".")));
                } else {
                    product.setWeight(new BigDecimal("0"));
                }
                //Объём товара (приходит String, конвертим в BigDecimal)
                if (request.getVolume() != null && !request.getVolume().isEmpty() && request.getVolume().trim().length() > 0) {
                    product.setVolume(new BigDecimal(request.getVolume().replace(",", ".")));
                } else {
                    product.setVolume(new BigDecimal("0"));
                }
                //единица измерения веса товара
                if (request.getWeight_edizm_id() != null) {
                    SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getWeight_edizm_id());
                    product.setWeight_edizm(ed);
                }
                //единица измерения объёма товара
                if (request.getVolume_edizm_id() != null) {
                    SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getVolume_edizm_id());
                    product.setVolume_edizm(ed);
                }
                //маркированный товар (Boolean)
                product.setMarkable(request.isMarkable());
                //группа маркированных товаров
                if (request.getMarkable_group_id() != null) {
                    SpravSysMarkableGroup ed = emgr.find(SpravSysMarkableGroup.class, request.getMarkable_group_id());
                    product.setMarkable_group(ed);
                }
                // не закупаемый товар (Boolean)
                product.setNot_buy(request.isNot_buy());
                // снятый с продажи товар (Boolean)
                product.setNot_sell(request.isNot_sell());
                // неделимый товар (Boolean)
                product.setIndivisible(request.isIndivisible());

                User changer = userService.getUserByUsername(userService.getUserName());
                product.setChanger(changer);//кто изменил

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                product.setDate_time_changed(timestamp);//дату изменения

                emgr.getTransaction().commit();
                emgr.close();
            } catch (Exception e) {
                logger.error("Exception in method updateProductsWithoutOrders.", e);
                e.printStackTrace();
                return false;
            }
            return true;
        } else return false;
    }

    // сохранение порядка картинок товара (права не нужны, т.к. вызывается после проверки всех прав)
    @SuppressWarnings("Duplicates")
//    @Transactional
    private boolean saveChangeProductImagesOrder(Long ProductImageId, Long productId, int order) {
        String stringQuery="";
        try {
            stringQuery = " update product_files set " +
                    " output_order=" + order +
                    " where file_id=" + ProductImageId +
                    " and product_id=" + productId;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method saveChangeProductImagesOrder. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // сохранение порядка картинок товара (права не нужны, т.к. вызывается после проверки всех прав)
    @SuppressWarnings("Duplicates")
//    @Transactional
    private boolean saveChangeProductCagentsOrder(Long cagentId, Long productId, int order) {
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
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertProduct(ProductsForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "163,164"))//  "Создание"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие создаваемого документа
            Long DocumentMasterId = companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(14L, "163") &&
                    Long.valueOf(myCompanyId) != request.getCompany_id()) || DocumentMasterId != myMasterId) {
                return null;
            } else {
                try {
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
                    if (request.getProductgroup_id() != null) {
                        ProductGroups pg = emgr.find(ProductGroups.class, request.getProductgroup_id());
                        newDocument.setProductGroup(pg);
                    }

                    Set<Long> categories = request.getSelectedProductCategories();
                    if (!categories.isEmpty()) {
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
                    //маркированный товар (Boolean)
                    newDocument.setMarkable(request.isMarkable());
                    //группа маркированных товаров
                    if (request.getMarkable_group_id() != null) {
                        SpravSysMarkableGroup ed = emgr.find(SpravSysMarkableGroup.class, request.getMarkable_group_id());
                        newDocument.setMarkable_group(ed);
                    }
                    //не закупаемый товар (Boolean)
                    newDocument.setNot_buy(request.isNot_buy());
                    //неделимый товар (Boolean)
                    newDocument.setIndivisible(request.isIndivisible());
                    //товар снят с продажи (Boolean)
                    newDocument.setNot_sell(request.isNot_sell());

                    entityManager.persist(newDocument);
                    entityManager.flush();
                    return newDocument.getId();
                } catch (Exception e) {
                    logger.error("Exception in method insertProduct.", e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return null;
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
            stringQuery = "Update products p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.id in (" + delNumbers+")";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
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
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update products p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.id in (" + delNumbers+")";
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


    @SuppressWarnings("Duplicates")
    public List<ProductHistoryJSON> getProductHistoryTable(Long companyId, Long departmentId, Long productId, String dateFrom, String dateTo, String sortColumn, String sortAsc, int result, List<Long> docTypesIds, int offsetreal) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))// Просмотр по (всем,своим) предприятиям
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  " +
                    "           p.id as id, " +
                    "           dep.name as department," +
                    "           to_char(p.date_time_created at time zone '" + myTimeZone + "', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           doc.name as docName," +
                    "           p.doc_id as docId," +
                    "           p.doc_type_id as docTypeId," +
                    "           (select sum(change) from product_history where master_id=" + myMasterId + " and  department_id = p.department_id and is_completed = true and product_id=" + productId +"  and date_time_created<=p.date_time_created) as quantity," +
                    "           p.change as change," +
                    "           p.price as  price," +   // цена единицы в операции
                    "           p.netcost as netcost," + // себестоимость единицы в операции
                    "           0.00 as avg_netcost_price, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           dep.id as department_id "+
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
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Недопустимые параметры запроса");
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
                    doc.setQuantity(quantity);
                    doc.setChange(change);
                    doc.setPrice(price);
                    doc.setNetcost(netcost);
                    doc.setAvg_netcost_price(recountProductNetcost(companyId, netcostPolicy.equals("each") ? Long.parseLong(obj[12].toString()) : null, productId, (Timestamp) obj[11]));
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
                addImagesToProduct(universalForm);
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
            List<Object[]> listOfProductFields = getListOfProductFields(origProductId);
            for (Object[] val : listOfProductFields) {
                //            Long product_id,       Long field_id,                    String value
                createCustomField(newProductId, Long.parseLong(String.valueOf(val[1])), String.valueOf(val[2]));
            }
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
            newDocument.setName(response.getName() == null ? "" : response.getName() + " (копия " + countCopy + ")");
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
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                "   sum(coalesce(reserved_current,0)-0) " +//пока отгрузки не реализованы, считаем, что отгружено 0. Потом надо будет высчитывать из всех Отгрузок, исходящих из этого Заказа покупателя
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
    public List getProductsList(String searchString, Long companyId, Long departmentId, Long document_id, Long priceTypeId) {
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
                "   coalesce((select pp.price_value from product_prices pp where pp.product_id=p.id and pp.price_type_id = "+priceTypeId+"),0) as price_by_typeprice, " +
                // средняя себестоимость
                "           (select ph.avg_netcost_price   from product_quantity ph where ph.department_id = "  + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgCostPrice, " +
                // средняя закупочная цена
                "           (select ph.avg_purchase_price  from products_history ph  where ph.department_id = " + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgPurchasePrice, " +
                // последняя закупочная цена
                "           (select ph.price from product_history ph  where ph.department_id = " + departmentId +" and ph.product_id = p.id and ph.is_completed=true order by ph.date_time_created desc limit 1) as lastPurchasePrice " +

                " from products p " +
                " left outer join product_barcodes pb on pb.product_id=p.id" +
                " left outer join files f on f.id=(select file_id from product_files where product_id=p.id and output_order=1 limit 1)" +
                " where  p.master_id=" + myMasterId +
                " and coalesce(p.is_deleted,false) !=true ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " upper(p.name) like upper('%" + searchString + "%') or " +
                    " upper(p.article) like upper ('%" + searchString + "%') or " +
                    " to_char(p.product_code_free,'fm0000000000') = '" + searchString + "' or " +
                    " pb.value = '" + searchString + "'";
            stringQuery = stringQuery + ")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id,f.name  order by p.name asc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
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
        Long myCompanyId= userRepositoryJPA.getMyCompanyId_();
        String stringQuery;
        stringQuery=
                "select "+
                        " p.id                                  as price_type_id, " +
                        " p.name                                as price_name, " +
                        " p.description                         as price_description, " +

                        " coalesce(" +
                        "(select coalesce(price_value,0) " +
                        "from product_prices " +
                        "where " +
                        "product_id="+productId+" and " +
                        "price_type_id=p.id) " +
                        ",0)                                as price_value " +

                        " from " +
                        " sprav_type_prices p " +
                        " where " +
                        " coalesce(p.is_deleted,false) = false " +
                        " and company_id = " + myCompanyId +
                        " and p.master_id = " + myMasterId +
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






    @SuppressWarnings("Duplicates")
    //отдает информацию состоянии товара (кол-во, последняя поставка) в отделении, и средним ценам (закупочной и себестоимости) товара
    public ShortInfoAboutProductJSON getShortInfoAboutProduct(Long department_id, Long product_id/*, Long price_type_id*/) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String myTimeZone = userRepository.getUserTimeZone();
        String stringQuery = "select" +
                "           p.quantity as quantity," +
                "           p.change as change," +
                "           p.avg_purchase_price as avg_purchase_price," +
                "           p.last_purchase_price as last_purchase_price," +
                "           p.avg_netcost_price as avg_netcost_price," +
                "           to_char(p.date_time_created at time zone '" + myTimeZone + "', 'DD.MM.YYYY') as date_time_created " +
                /*"           '-' as department_type_price, " +
                "           coalesce((select price_value from product_prices where product_id = "+product_id+" and price_type_id = "+price_type_id+"),0) as department_sell_price " +*/

                "           from" +
                "           products_history p " +
                "           left outer join" +
                "           departments dp " +
                "           on dp.id= " + department_id +
                "           where" +
                "               p.department_id= " + department_id +
                "           and p.product_id= " + product_id +
                "           and p.master_id= " + myMasterId +
                "           order by p.id desc limit 1";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            ShortInfoAboutProductJSON returnObj = new ShortInfoAboutProductJSON();

            for (Object[] obj : queryList) {
                returnObj.setQuantity((BigDecimal) obj[0]);
                returnObj.setChange((BigDecimal) obj[1]);
                returnObj.setAvg_purchase_price((BigDecimal) obj[2]);
                returnObj.setLast_purchase_price((BigDecimal) obj[3]);
                returnObj.setAvg_netcost_price((BigDecimal) obj[4]);
                returnObj.setDate_time_created((String) obj[5]);
                /*returnObj.setDepartment_type_price((String) obj[6]);
                returnObj.setDepartment_sell_price((BigDecimal) obj[7]);*/
            }
            return returnObj;
        } catch (Exception e) {
            logger.error("Exception in method getShortInfoAboutProduct. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }



    //*****************************************************************************************************************************************************
//***********************************************   C A T E G O R I E S   *****************************************************************************
//*****************************************************************************************************************************************************
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

    //права не нужны т.к. не вызывается по API, только из контроллера
    public List<Integer> getProductsCategoriesIdsByProductId(Long id) {
        String stringQuery = "select p.category_id from product_productcategories p where p.product_id= " + id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
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
                    " where company_id =" + companyId + " and master_id=" + myMasterId + " and upper(name) like upper('%" + searchString + "%')";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Long insertProductCategory(ProductCategoriesForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "171,172"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId = companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(14L, "171") &&
                    Long.valueOf(myCompanyId) != request.getCompanyId()) || DocumentMasterId != myMasterId) {
                return null;
            } else {
                String stringQuery;
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                Long myId = userRepository.getUserId();
                stringQuery = "insert into product_categories (" +
                        "name," +
                        "master_id," +
                        "creator_id," +
                        "parent_id," +
                        "company_id," +
                        "date_time_created" +
                        ") values ( " +
                        "'" + request.getName() + "', " +
                        myMasterId + "," +
                        myId + "," +
                        (request.getParentCategoryId() > 0 ? request.getParentCategoryId() : null) + ", " +
                        request.getCompanyId() + ", " +
                        "(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')))";
                try {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.executeUpdate() == 1) {
                        stringQuery = "select id from product_categories where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id=" + myId;
                        Query query2 = entityManager.createNativeQuery(stringQuery);
                        return Long.valueOf(Integer.parseInt(query2.getSingleResult().toString()));
                    } else return (0L);
                } catch (Exception e) {
                    logger.error("Exception in method insertProductCategory. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return 0L;
                }
            }
        } else return 0L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateProductCategory(ProductCategoriesForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))//  "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long changer = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "update product_categories set " +
                    " name='" + request.getName() + "', " +
                    " date_time_changed= now()," +
                    " changer_id= " + changer +
                    " where id=" + request.getCategoryId() +
                    " and master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав по всем предприятиям
            {
//            остается только на своё предприятие (140)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method updateProductCategory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductCategory(ProductCategoriesForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "175,176"))// "Удаление категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "delete from product_categories " +
                    " where id=" + request.getCategoryId() +
                    " and master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "175")) //Если нет прав по всем предприятиям
            {
                //остается только на своё предприятие
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method deleteProductCategory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean saveChangeCategoriesOrder(List<ProductCategoriesForm> request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))// редактирование своих или чужих предприятий
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            User changer = userRepository.getUserByUsername(userRepository.getUserName());
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
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))//"Контрагенты" (см. файл Permissions Id)
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
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    //отдает информацию о состоянии товара(кол-во всего, зарезервировано и цена) по его id ,в отделении по его id, по цене по её id, для документа Заказ покупателя по его id
    public ProductsPriceAndRemainsJSON getProductsPriceAndRemains(Long department_id, Long product_id, Long price_type_id, Long document_id) {

        Long myMasterId = userRepositoryJPA.getMyMasterId();
//        String myDepthsIds = userRepositoryJPA.getMyDepartmentsId().toString().replace("[","").replace("]","");
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
                "           ei.short_name as edizm, " +
                // всего единиц товара в отделении (складе)
                "           (select coalesce(quantity,0)   from product_quantity     where department_id = "    + departmentId +" and product_id = p.id) as estimated_balance, " +
                // цена по запрашиваемому типу цены priceTypeId (если тип цены не запрашивается - ставим null в качестве цены по отсутствующему в запросе типу цены)
                "           coalesce((select pp.price_value from product_prices pp where pp.product_id=p.id and  pp.price_type_id = 10),0) as price_by_typeprice, " +
                // средняя себестоимость
                "           (select ph.avg_netcost_price   from products_history ph where ph.department_id = "  + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgCostPrice, " +
                // средняя закупочная цена
                "           (select ph.avg_purchase_price  from products_history ph  where ph.department_id = " + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgPurchasePrice, " +
                // последняя закупочная цена
                "           (select ph.last_purchase_price from products_history ph  where ph.department_id = " + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as lastPurchasePrice, " +
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

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean updateProductCustomFields(List<ProductCustomFieldsSaveForm> request) {
        //log.info("in updateProductCustomFields class");
        if (request.size() > 0) { //если поля на сохранение есть
            Long productId = request.get(0).getProduct_id();//за id товара берем productId из первого же объекта (т.к. они ДОЛЖНЫ быть все одинаковы)
            //log.info("productId="+productId.toString());
            //log.info("Поля на сохранение есть ");
            for (ProductCustomFieldsSaveForm custumField : request) {
                if (!productId.equals(custumField.getProduct_id())) {
                    //log.info("В листе не одинаковые productId!");
                    return false; //проверяю что в листе все productId одинаковые
                }
            }
            //log.info("Прошли цикл, перед проверкой прав.");
            // проверка на то, что все поля принадлежат к тем документам Товары, на которые есть соответствующие права:
            //Если есть право на "Изменение по всем предприятиям" и все id для изменения принадлежат владельцу аккаунта (с которого изменяют), ИЛИ
            if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", productId.toString())) ||
                    //Если есть право на "Изменение по своему предприятияю" и все id для изменения принадлежат владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                    (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", productId.toString()))) {
                //log.info("Права есть!");
                try {
                    for (ProductCustomFieldsSaveForm custumField : request) {
                        //log.info("В цикле: поле "+custumField.getName());
                        if (isThereThisField(custumField.getProduct_id(), custumField.getId())) { // если поле уже есть в product_fields
                            //log.info("поле уже есть в product_fields");
                            updateCustomField(custumField.getProduct_id(), custumField.getId(), custumField.getValue()); //то апдейтим
                        } else {
                            //log.info("поля нет в product_fields");
                            if (custumField.getValue() != null && !custumField.getValue().isEmpty() && custumField.getValue().trim().length() > 0) {
                                //если поля нет в product_fields, и в нём есть текст, то инсертим (чтобы пустые строки в product_fields не разводить)
                                //log.info("поля нет в product_fields, и в нём есть текст - инсертим");
                                createCustomField(custumField.getProduct_id(), custumField.getId(), custumField.getValue()); // нет - то инсертим
                                //log.info("после инсерта");
                            }
                        }
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method updateProductCustomFields. ", e);
                    return false;
                }
            } else {
                //log.info("НЕТ ПРАВ! ");
                return false;
            }
        } else return true;// тут true чтобы не было ошибки в консоли браузера.
    }

    private List<Object[]> getListOfProductFields(Long productId) {
        List<Object[]> a;
        String stringQuery = "select  product_id as product_id, field_id as id, field_value as value, '1' as name, '1' as parent_set_id from product_fields p where p.product_id=" + productId;
        Query query = entityManager.createNativeQuery(stringQuery);
        a = query.getResultList();
        return a;
    }

    public boolean isThereThisField(Long product_id, Long field_id) {
        String stringQuery = "select 1 from product_fields p where p.product_id=" + product_id + " and p.field_id =" + field_id;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }

    @SuppressWarnings("Duplicates")
    public boolean updateCustomField(Long product_id, Long field_id, String value) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "update product_fields set " +
                " field_value='" + value + "'" +
                " where product_id=" + product_id +
                " and field_id=" + field_id +
                " and (select master_id from products where id=" + product_id + ") = " + myMasterId; //для безопасности, чтобы не кидали json на авось у кого-то что-то проапдейтить
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            int i = query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method updateCustomField. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    public boolean createCustomField(Long product_id, Long field_id, String value) {
        String stringQuery;
        stringQuery = "insert into product_fields (product_id, field_id, field_value) values (" + product_id + "," + field_id + ",'" + value + "')";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            if (query.executeUpdate() == 1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception in method createCustomField. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<ProductGroupFieldTableJSON> getProductGroupFieldsListWithValues(int field_type, int productId) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "167,168,169,170"))//просмотр или редактирование (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());


            stringQuery = "select " +
                    " pgf.id as id, " +
                    " pgf.name as name, " +
                    " pgf.description as description, " +
                    " pgf.field_type as field_type, " +
                    " pgf.group_id as group_id, " +
                    " pgf.output_order as output_order, " +
                    //field_type: 1 - сеты (наборы) полей, 2 - поля
                    (field_type == 1 ? "''" : "(select coalesce (pf.field_value,'') from product_fields pf where pf.field_id=pgf.id and pf.product_id=p.id limit 1)") + " as value, " +
                    " pgf.parent_set_id as parent_set_id " +
                    " from  " +
                    " product_group_fields pgf," +
                    " product_groups pg," +
                    " products p " +
                    " where pgf.group_id=pg.id " +
                    " and p.group_id=pg.id " +
                    " and pgf.field_type = " + field_type +// тип: 1 - сеты (наборы) полей, 2 - поля
                    " and p.id=" + productId +
                    " and pgf.master_id=" + myMasterId;


            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167,169")) //Если нет прав на просм. или редактир. по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and pgf.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by pgf.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery, ProductGroupFieldTableJSON.class);
            return query.getResultList();
        } else return null;
    }


//*****************************************************************************************************************************************************
//******************************************************   C A G E N T S    ***************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addCagentsToProduct(UniversalForm request) {
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
                        manyToMany_productId_CagentId(prouctId, Id);
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
    boolean manyToMany_productId_CagentId(Long prouctId, Long cagentId) {
        try {
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
            entityManager.createNativeQuery("" +
                    "insert into product_cagents " +
                    "(product_id, cagent_id, output_order, cagent_article, additional) " +
                    "values " +
                    "(" + newProductId + ", " +
                    request.getCagent_id() +
                    " , (select coalesce(max(output_order)+1,1) from product_cagents where product_id=" + newProductId + "), '" +
                    request.getCagent_article() + "', '" + request.getAdditional() + "')")

                    .executeUpdate();
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
                        " set  cagent_article= '" + (request.getString1() != null ? request.getString1() : "") + "'" +
                        "    , additional= '" + (request.getString2() != null ? request.getString2() : "") + "'" +
                        " where (select master_id from cagents where id=p.cagent_id)=" + myMasterId + //контроль того, что лицо, имеющее доступ к редактированию документа, не может через сторонние сервисы типа postman изменить документы других аккаунтов
                        " and p.cagent_id=" + request.getId1() +
                        " and p.product_id=" + request.getId2();
                entityManager.createNativeQuery(stringQuery).executeUpdate();
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
    public boolean addImagesToProduct(UniversalForm request) {
        Long prouctId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", prouctId.toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", prouctId.toString()))) {
            String stringQuery="";
            try {

                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select product_id from product_files where product_id=" + prouctId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (fileIsImage(fileId) && query.getResultList().size() == 0) {//если таких картинок еще нет у товара, и файл является картинкой
                        entityManager.close();
                        manyToMany_productId_FileId(prouctId, fileId);
                    }
                }
                return true;
            } catch (Exception e) {
                logger.error("Exception in method addImagesToProduct. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_productId_FileId(Long prouctId, Long fileId) {
        try {
            entityManager.createNativeQuery(" " +
                    "insert into product_files " +
                    "(product_id,file_id,output_order) " +
                    "values " +
                    "(" + prouctId + ", " + fileId + " , (select coalesce(max(output_order)+1,1) from product_files where product_id=" + prouctId + "))")
                    .executeUpdate();
            entityManager.close();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method manyToMany_productId_FileId.", e);
            e.printStackTrace();
            return false;
        }
    }

    private boolean fileIsImage(Long fileId) {
        return true;
    }

    @SuppressWarnings("Duplicates") //отдает информацию по картинкам товара (fullSize - полным, нет - по их thumbnails)
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

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductImage(SearchForm request) {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", request.getAny_id().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", request.getAny_id().toString()))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery = " delete from product_files " +
                    " where product_id=" + request.getAny_id() +
                    " and file_id=" + request.getId() +
                    " and (select master_id from products where id=" + request.getAny_id() + ")=" + myMasterId;
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
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
                entityManager.createNativeQuery("" +
                        "insert into product_barcodes " +
                        "(product_id,barcode_id,value, description) " +
                        "values " +
                        "(" + request.getId2() + " , " +
                        request.getId3() + " , " +
                        (request.getString1() != null ? ("'" + request.getString1() + "'") : "''") + " , " +
                        (request.getString2() != null ? ("'" + request.getString2() + "'") : "''") + ")")
                        .executeUpdate();
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
                    " set  value= '" + (request.getString1() != null ? request.getString1() : "") + "'" +
                    "    , description= '" + (request.getString2() != null ? request.getString2() : "") + "'" +
                    " where p.id=" + request.getId1() +
                    " and (select master_id from products where id=p.product_id)=" + myMasterId; //контроль того, что лицо, имеющее доступ к редактированию документа, не может через сторонние сервисы типа postman изменить документы других аккаунтов
            try {     entityManager.createNativeQuery(stringQuery).executeUpdate();
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
            stringQuery = " delete from product_barcodes " +
                    " where id=" + request.getId1() +
                    " and (select master_id from products where id=" + request.getId2() + ")=" + myMasterId;
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
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
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();


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
        String stringQuery =
                "insert into product_barcodes " +
                        "(product_id, barcode_id, value, description) " +
                        "values " +
                        "(" + newProductId + ", " +
                        request.getBarcode_id() + ", '" +
                        request.getValue() + "', '" +
                        request.getDescription() + "')";
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
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


    //синхронизирует кол-во товаров в products_history и в product_quantity по предприятию
    //данная операция для работы Докио не нужна, проводилась 1 раз, при введении таблицы product_quantity,
    // необходимой для быстрой отдачи кол-ва товара и его средней себестоимости
//    @Transactional
//    public boolean syncQuantityProducts(UniversalForm request) {
//        Long companyId = request.getId();
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String stringQuery = "select id from departments where company_id=" + companyId;
//
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Integer> queryList = query.getResultList();
//
//            for (Integer obj : queryList) { //цикл по id отделений предприятия
//                Long departmentId = Long.parseLong(obj.toString());
//
//                stringQuery = "select id from products where company_id=" + companyId;
//                query = entityManager.createNativeQuery(stringQuery);
//                List<Integer> queryList2 = query.getResultList();
//                for (Integer obj2 : queryList2) {//цикл по всем товарам предприятия
//                    Long productId = Long.parseLong(obj2.toString());
//                    BigDecimal quantity = getLastProductHistoryQuantity(productId,departmentId);//получили кол-во товара в текущем предприятии в таблице по истории изменения количества товара
//
//                    if (!setProductQuantity(myMasterId, productId, departmentId, quantity)) {// запись о количестве товара в отделении в отдельной таблице
//                        break;
//                    }
//                }
//            }
//            return true;
//        }
//        catch (Exception e) {
//            logger.error("Exception in method syncQuantityProducts. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            return false;
//        }
//    }

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
    //products_history - таблица, в которой хранится история операций с товаром в отделении: вид операции (doc_type_id), сколько (change) и по какой цене (price)
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
            Timestamp date_time_created,
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
                        " date_time_created," + // время создания документа, к которому относится данная строка таблицы
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
                        "'" + date_time_created + "',"+
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
                throw new Exception("id объектов не принадлежат к их master_id. master_id="+masterId+", company_id="+company_id+", product_id="+product_id);




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
    //*****************************************************************************************************************************************************
//***************************************************      UTILS      *********************************************************************************
//*****************************************************************************************************************************************************
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
                        " coalesce((select quantity from product_quantity where department_id="+department_id+" and product_id="+product_id+"),0)     as quantity,   "+                       " coalesce(change,0)                as change               "+
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
//                returnObj.setLast_purchase_price(       (new BigDecimal(0)));
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



                      /*  " coalesce((select avg_netcost_price from product_quantity where ";
                        if(!Objects.isNull(department_id))
                            stringQuery = stringQuery+"department_id="+department_id+" and ";
                        stringQuery = stringQuery+" product_id="+product_id;
                        if(Objects.isNull(department_id))
                            stringQuery = stringQuery+" order by date_time_created desc limit 1";
                        stringQuery = stringQuery+"),0)     as avg_netcost_price,   "; // если отделение не задано - средняя себестоимость берется из последней записи по товару не зависимо от отделения
                        if(!Objects.isNull(department_id))
                            stringQuery = stringQuery+" coalesce((select quantity from product_quantity where department_id="+department_id+" and product_id="+product_id+"),0)     as quantity   ";
                        else
                            stringQuery = stringQuery+" coalesce((select sum(quantity) from product_quantity where product_id="+product_id+"),0)     as quantity   ";

                        */




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
//                for (Object[] obj : queryList) {
//                    returnObj.setAvg_netcost_price(recountProductNetcost(companyId, departmentId, productId));
//                    returnObj.setQuantity((BigDecimal)              obj[0]);
//                }
            }
            return returnObj;
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
                        "   order by date_time_created asc";
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
                        "   and date_time_created <= '" + timeBefore + "'" +
                        "   order by date_time_created asc";
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

            return lastAvgNetcostPrice;
        }
//        catch (CalculateNetcostNegativeSumException e) {
//            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            throw new CalculateNetcostNegativeSumException();
//        }
        catch (Exception e) {
            logger.error("Exception in method recountProductNetcost. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean setCategoriesToProducts(Set<Long> productsIds, Set<Long> categoriesIds, Boolean save) {
        try{
            String products = commonUtilites.SetOfLongToString(productsIds, ",", "", "");
            //поверка на то, что присланные id товаров действительно являются товарами мастер-аккаунта
            if (securityRepositoryJPA.isItAllMyMastersDocuments("products", products)) {
                if (!save) {//если не нужно сохранять те категории у товара, которые уже есть
                    //удаляем все категории у всех запрашиваемых товаров
                    if (deleteAllProductsCategories(products)) {
                        //назначаем товарам категории
                        if(setCategoriesToProducts(productsIds,categoriesIds))
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
                            if(!setCategoriesToProducts(prod,productCategoriesIds))
                                return null; // ошибка на прописывании категорий у товара
                        } else return null; // ошибка на стадии удаления категорий текущего товара в deleteAllProductsCategories
                    }
                    return true;
                }
            } else return null; // не прошли по безопасности - подсунуты "левые" id товаров
        } catch (Exception e) {
            logger.error("Exception in method setCategoriesToProducts", e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean setCategoriesToProducts(Set<Long> productsIds, Set<Long> categoriesIds) throws Exception {
        if(categoriesIds.size()>0) {//если категории есть
            //прописываем их у всех запрашиваемых товаров
            StringBuilder stringQuery = new StringBuilder("insert into product_productcategories (product_id, category_id) values ");
            int i = 0;
            for (Long p : productsIds) {
                for (Long c : categoriesIds) {
                    stringQuery.append(i > 0 ? "," : "").append("(").append(p).append(",").append(c).append(")");
                    i++;
                }
            }
            try {
                entityManager.createNativeQuery(stringQuery.toString()).executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method setCategoriesToProducts. SQL query:" + stringQuery, e);
                e.printStackTrace();
                throw new Exception();
            }
        } else return true;
    }

    private Boolean deleteAllProductsCategories(String products) throws Exception {
        String stringQuery = "delete from product_productcategories where product_id in("+products+")";
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

}

