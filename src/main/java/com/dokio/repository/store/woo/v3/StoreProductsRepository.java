package com.dokio.repository.store.woo.v3;

import com.dokio.message.request.store.woo.v3.IntListForm;
import com.dokio.message.request.store.woo.v3.SyncIdForm;
import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.store.woo.v3.products.*;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Repository
public class StoreProductsRepository {

    private Logger logger = Logger.getLogger(StoreProductsRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    CompanyRepositoryJPA companyRepository;

    @SuppressWarnings("Duplicates")
    public ProductCountJSON countProductsToStoreSync(String key) {
        String stringQuery = "";
        ProductCountJSON result = new ProductCountJSON();
        try{
             Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
             Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",key).toString());
             Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
             String langCode = (String)cu.getByCrmSecretKey("lang_code", key);
             stringQuery =
             "select count(*) " +
             " from (" +
             "   select p.id from products p" +
             "   INNER JOIN product_productcategories ppc ON ppc.product_id=p.id " +
             "   INNER JOIN product_categories pc ON pc.id=ppc.category_id " +
             "   INNER JOIN stores_productcategories spc ON pc.id=spc.category_id " +
             "   INNER JOIN stores s on s.id=spc.store_id " +
             "   LEFT OUTER JOIN store_translate_products translator ON p.id = translator.product_id and translator.lang_code = '" + langCode + "'" +
             "   LEFT OUTER JOIN stores_products sp ON sp.product_id = p.id and sp.store_id=s.id " +
             "   where " +
             "   p.company_id = " + companyId +
             "   and coalesce(pc.is_store_category,false)=true " +
             "   and coalesce(p.is_deleted, false) = false " +
             "   and p.id not in (select variation_product_id from product_variations where master_id = " + masterId + ")" + // product is not used as variation
             "   and s.id = " + storeId +
             "   and coalesce(s.is_deleted, false) = false " +
             "   and (" +
             "   (coalesce(sp.need_to_syncwoo,true) = true) or " +
             "   (sp.date_time_syncwoo is null) or " +
             "   (p.date_time_changed is not null and sp.date_time_syncwoo is not null and p.date_time_changed > sp.date_time_syncwoo)" +
             "   )" +
             "   group by p.id" +
             " ) as subqery ";

            Query query = entityManager.createNativeQuery(stringQuery);
            result.setQueryResultCode(1);
            result.setProductCount((BigInteger)query.getSingleResult());
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/syncProductsToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        } catch (NoResultException nre) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsToStore. SQL query:"+stringQuery, nre);
            nre.printStackTrace();
            result.setQueryResultCode(1);
            result.setProductCount(new BigInteger("0"));
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    public ProductsJSON syncProductsToStore(String key, Integer firstResult, Integer maxResults) {
        ProductsJSON result = new ProductsJSON();
        String stringQuery = "";
        try{
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",key).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            String langCode = (String)cu.getByCrmSecretKey("lang_code", key);
            stringQuery =
                    "select " +
                    " p.id as crm_id," +
                    " sp.woo_id as woo_id, " +
                    " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
                    " coalesce(p.type, 'simple') as type, " +
                    " coalesce(NULLIF(translator.description, ''), p.description) as description, " +
                    " coalesce(NULLIF(translator.short_description, ''), p.short_description) as short_description, " +
                    " coalesce(NULLIF(translator.description_html, ''), p.description_html) as description_html, " +
                    " coalesce(NULLIF(translator.short_description_html, ''), p.short_description_html) as short_description_html, " +
                    " ppr.price_value as price_regular, " +
                    " pps.price_value as price_sale, " +
                    " p.stock_status as stock_status, " +
                    " p.article as sku, " +
                    " p.sold_individually as sold_individually, " +
                    " p.backorders as backorders, " +
                    " p.manage_stock as manage_stock, " +
                    " p.purchase_note as purchase_note, " +
                    " p.menu_order as menu_order, " +
                    " p.reviews_allowed as reviews_allowed, " +
                    " coalesce(p.description_type, 'editor') as description_type, " + // "editor" or "custom"
                    " coalesce(p.short_description_type, 'editor') as short_description_type " + // "editor" or "custom"

                    " from products p" +

                    " INNER JOIN product_productcategories ppc ON ppc.product_id=p.id " +
                    " INNER JOIN product_categories pc ON pc.id=ppc.category_id " +
                    " INNER JOIN stores_productcategories spc ON pc.id=spc.category_id " +
                    " INNER JOIN stores s on s.id=spc.store_id " +
                    " LEFT OUTER JOIN store_translate_products translator ON p.id = translator.product_id and translator.lang_code = '" + langCode + "'" +
                    " LEFT OUTER JOIN stores_products sp ON sp.product_id = p.id and sp.store_id=s.id " +
                    " LEFT OUTER JOIN product_prices ppr on ppr.product_id = p.id and ppr.price_type_id = s.store_price_type_regular " +
                    " LEFT OUTER JOIN product_prices pps on pps.product_id = p.id and pps.price_type_id = s.store_price_type_sale " +

                    " where " +

                    " p.company_id = " + companyId +
                    " and coalesce(pc.is_store_category,false)=true " + // if product is in the store category
                    " and coalesce(p.is_deleted, false) = false " +
                    " and p.id not in (select variation_product_id from product_variations where master_id = " + masterId + ")" + // product is not used as variation
                    " and s.id = " + storeId +
                    " and coalesce(s.is_deleted, false) = false " +
                    " and (" +
                    " (coalesce(sp.need_to_syncwoo,true) = true) or " +// if the product need to be synchronized
                    " (sp.date_time_syncwoo is null) or " +// if the product is created recently, or changed, but still not synchronized
                    " (p.date_time_changed is not null and sp.date_time_syncwoo is not null and p.date_time_changed > sp.date_time_syncwoo)" +
                    " ) " +
                    " group by 1,2,3,4,5,6,7,8,9,10";  // Without grouping will output rows per each product as many categories product has.
                                                       // There is no categories data in output here, but they need to filter products by
                                                       // their belongings to categories where selected needed online store

            Query query = entityManager.createNativeQuery(stringQuery);
            if (firstResult != null) {query.setFirstResult(firstResult);} // from 0
            if (maxResults != null) {query.setMaxResults(maxResults);}
            List<Object[]> queryList = query.getResultList();
            Set<ProductJSON> returnList = new HashSet<>();
            List<Long> storeDepartments = companyRepository.getCompanyStoreDepartmentsIds(companyId,storeId,masterId);
            for (Object[] obj : queryList) {
                ProductJSON doc = new ProductJSON();
                doc.setCrm_id(Long.parseLong(                       obj[0].toString()));
                doc.setWoo_id((Integer)                             obj[1]);
                doc.setName((String)                                obj[2]);
                doc.setType((String)                                obj[3]);
                doc.setDescription(((String)obj[18]).equals("editor")?((String)obj[4]):((String)obj[6]));
                doc.setShort_description(((String)obj[19]).equals("editor")?((String)obj[5]):((String)obj[7]));
                doc.setRegular_price(                               Objects.isNull(obj[8])?"":obj[8].toString());
                doc.setSale_price(                                  getSalePrice((BigDecimal)obj[8],(BigDecimal)obj[9]));
                doc.setImages(                                      getSetOfProductImages(Long.parseLong(obj[0].toString()),companyId));
                doc.setCategories(                                  getProductCategoriesIds(Long.parseLong(obj[0].toString()), storeId));
                doc.setAttributes(                                  getListOfProductAttributes(Long.parseLong(obj[0].toString()), storeId));
                doc.setStock_status((String)                        obj[10]);
                doc.setSku((String)                                 obj[11]);
                doc.setSold_individually((Boolean)                  obj[12]);
                doc.setBackorders((String)                          obj[13]); //If managing stock, this controls if backorders are allowed. Options: no, notify and yes. Default is no
                doc.setManage_stock((Boolean)                       obj[14]);
                doc.setPurchase_note((String)                       obj[15]);
                doc.setMenu_order((Integer)                         obj[16]);
                doc.setReviews_allowed((Boolean)                    obj[17]);
                doc.setStock_quantity(productsRepository.getAvailable(doc.getCrm_id(), storeDepartments, true).intValue());
                doc.setUpsells   (getUpsellCrosssells(doc.getCrm_id(), "product_upsell", storeId));
                doc.setCrosssells(getUpsellCrosssells(doc.getCrm_id(), "product_crosssell", storeId));
                doc.setVariations_woo_ids(getVariationsWooIds(doc.getCrm_id(), storeId));
                doc.setDefaultAttributes(getDefaultAttributes(doc.getCrm_id(), storeId));
                returnList.add(doc);
            }
            result.setQueryResultCode(1);
            result.setProducts(returnList);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/syncProductsToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        } catch (NoResultException nre) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsToStore. SQL query:"+stringQuery, nre);
            nre.printStackTrace();
            result.setQueryResultCode(1);
            result.setProducts(new HashSet<>());
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    String getSalePrice(BigDecimal Regular_price, BigDecimal Sale_price){
        if(!Objects.isNull(Regular_price) && !Objects.isNull(Sale_price) && Regular_price.compareTo(Sale_price)>0)
            return Sale_price.toString();
        else return "";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncProductsIds(SyncIdsForm request) {
        String stringQuery = "";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());

            for (SyncIdForm row : request.getIdsSet()) {
                syncProductId(row, companyId, masterId, storeId );
            }
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/syncProductsIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean syncProductId(SyncIdForm ids, Long companyId, Long masterId, Long storeId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " insert into stores_products (" +
                    " master_id, " +
                    " company_id, " +
                    " store_id, " +
                    " product_id, " +
                    " woo_id," +
                    " need_to_syncwoo, " +
                    " date_time_syncwoo " +
                    " ) values (" +
                    masterId + ", " +
                    companyId + ", " +
                    storeId + ", " +
                    "(select id from products where master_id = "+masterId+" and company_id = "+companyId+" and id = " + ids.getCrm_id() + "), " +
                    ids.getId() + "," +
                    " false, " +
                    " now()) " +
                    " ON CONFLICT ON CONSTRAINT stores_products_uq " +// "upsert"
                    " DO update set " +
                    " woo_id = " + ids.getId() + ", " +
                    " need_to_syncwoo = false, " +
                    " date_time_syncwoo = now()";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreProductsRepository/syncProductId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private List<ImageJSON> getSetOfProductImages(Long productId, Long companyId) throws Exception {
        String stringQuery =
                "           select " +
                "           coalesce(f.original_name,'') as img_original_name, "+
                "           coalesce(f.name,'') as img_name, "+
                "           coalesce(f.alt,'') as img_alt "+
                "           from " +
                "           products p " +
                "           inner join product_files pf on p.id=pf.product_id " +
                "           inner join files f on pf.file_id=f.id " +
                "           where " +
                "           p.id= " + productId +
                "           and f.trash is not true " +
                "           and p.company_id= " + companyId +
                "           and coalesce(f.anonyme_access, false) is true " +
                "           order by pf.output_order";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ImageJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ImageJSON doc = new ImageJSON();
                doc.setImg_original_name((String)                       obj[0]);
                doc.setImg_address(apiserver + "/api/public/getFile/" + obj[1]);
                doc.setImg_alt((String)                                 obj[2]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method StoreProductsRepository/getSetOfProductImages. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private Set<Long> getProductCategoriesIds(Long productId, Long storeId) throws Exception {
        String stringQuery="" +

        " select " +
        " spc.woo_id as woo_id " +
        " from " +
        " stores_productcategories spc " +
        " INNER JOIN product_categories pc ON pc.id=spc.category_id " +
        " INNER JOIN product_productcategories ppc ON ppc.category_id=pc.id  " +
        " INNER JOIN products p ON p.id=ppc.product_id " +
        " INNER JOIN stores s on s.id=spc.store_id " +
        " where " +
        " coalesce(pc.is_store_category,false)=true " +
        " and s.id = " + storeId +
        " and spc.woo_id is not null" +
        " and p.id=" + productId;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            Set<Long> categoriesIds = new HashSet<>();
            for (Object i : query.getResultList()) {categoriesIds.add(new Long(i.toString()));}
            return categoriesIds;
        }catch (Exception e) {
            logger.error("Exception in method getProductCategoriesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // returns List of product attributes of product by its ID with selected terms
    private List<AttributeJSON> getListOfProductAttributes(Long productId, Long storeId) throws Exception {
        String stringQuery =
                " select " +
                        " pa.id as crm_id, " +
                        " sa.woo_id as woo_id, " +
                        " ppa.position as position, " +
                        " coalesce(ppa.visible, false) as visible, " +
                        " coalesce(ppa.variation, false) as variation " +
                        " from " +
                        " product_productattributes ppa " +
                        " inner join product_attributes pa on pa.id = ppa.attribute_id " +
                        " inner join stores_attributes  sa on pa.id = sa.attribute_id " +
                        " where " +
                        " ppa.product_id = " + productId +
                        " and sa.store_id= " + storeId +
                        " order by ppa.position ";

        try {
            List<List<String>> mapOfAttributesAndTerms = getMapOfAttributesAndTerms(productId, storeId);
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<AttributeJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                AttributeJSON doc = new AttributeJSON();
                doc.setCrm_id(Long.parseLong(                           obj[0].toString()));
                doc.setWoo_id((Integer)                                 obj[1]);
                doc.setPosition((Integer)                               obj[2]);
                doc.setVisible((Boolean)                                obj[3]);
                doc.setVariation((Boolean)                              obj[4]);
                doc.setOptions(GetTermsOfProductAttribute(obj[0].toString(), mapOfAttributesAndTerms));
                returnList.add(doc);
            }
            return returnList;

        } catch (Exception e) {
            logger.error("Exception in method StoreProductsRepository/getListOfProductAttributes. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private List<DefaultAttributes> getDefaultAttributes(Long productId, Long storeId) throws Exception {
        String stringQuery =
                        " select " +
                        " da.attribute_id as attribute_id," +
                        " sa.woo_id as attribute_woo_id, " +            // aka id
                        " coalesce(NULLIF(translator_a.name, ''), pa.name) as attribute_name, " + // aka name
                        " coalesce(NULLIF(translator_t.name, ''), (select name from product_attribute_terms where id=da.term_id)) as term_name " + // aka option (can be NULL if selected "ANY")
                        " from  default_attributes da" +
                        " inner join product_attributes pa on pa.id = da.attribute_id" +
                        " inner join stores_attributes sa on pa.id = sa.attribute_id and sa.woo_id is not null" +
                        " LEFT OUTER JOIN store_translate_attributes translator_a ON da.attribute_id = translator_a.attribute_id and translator_a.lang_code = (select coalesce(lang_code,'EN') from stores where id="+storeId+") " +
                        " LEFT OUTER JOIN store_translate_terms translator_t      ON da.term_id =      translator_t.term_id      and translator_t.lang_code = (select coalesce(lang_code,'EN') from stores where id="+storeId+") " +
                        " where da.product_id = " + productId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> resultList = query.getResultList();
            List<DefaultAttributes> returnList = new ArrayList<>();
            for(Object[] obj:resultList){
                if(!Objects.isNull(obj[3])){ // if Attribute has not selected option "-Any-"
                    DefaultAttributes doc=new DefaultAttributes();
                    doc.setCrm_attribute_id(Long.parseLong(             obj[0].toString()));
                    doc.setWoo_attribute_id((Integer)                   obj[1]);
                    doc.setName((String)                                obj[2]);
                    doc.setOption((String)                              obj[3]);
                    returnList.add(doc);
                }
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getDefaultAttributes. SQL query:" + stringQuery, e);
            throw new Exception();
        }
    }
    private List<String> GetTermsOfProductAttribute(String attributeId, List<List<String>> mapOfAttributesAndTerms){
        List<String> returnList = new ArrayList<>();
        mapOfAttributesAndTerms.forEach(innerList -> {
            if(innerList.get(0).equals(attributeId))
                returnList.add(innerList.get(1));
        });
        return returnList;
    }

    // it returns List of map with pairs of Attribute ID and Term name (translated if it is required and the translation exists)
    //                       GENERAL list
    //                attribute_id      term_name
    // inner List 1      1                blue
    // inner List 2      1                red
    // inner List 3      1                green
    // inner List 4      2                big
    // inner List 5      2                small

    private List<List<String>> getMapOfAttributesAndTerms(Long productId, Long storeId) throws Exception {
        String stringQuery =
                " select " +
                    " pat.attribute_id as attribute_id, " +
                    " coalesce(NULLIF(translator.name, ''), pat.name) as term_name " +
                " from " +
                    " product_attribute_terms pat " +
                    " INNER JOIN product_terms pt ON pat.id = pt.term_id " +
                    " LEFT OUTER JOIN store_translate_terms translator ON pat.id = translator.term_id and translator.lang_code = (select coalesce(lang_code,'EN') from stores where id="+storeId+") " +
                " where " +
                    " pt.product_id = " + productId +
                " order by pat.menu_order ";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<List<String>> returnMap = new ArrayList<>();
            for (Object[] obj : queryList) {
                returnMap.add(Arrays.asList(obj[0].toString(), (String)obj[1]));
            }
            return returnMap;
        } catch (Exception e) {
            logger.error("Exception in method StoreProductsRepository/getListOfTerms. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // возвращает список товаров которые не подлежат синхронизации (они не относится к товарам у которых есть категории или не относится к товарам,
    // у которых есть хотя бы одна категория 'интернет-магазин' с выбранным интернет-магазином)
    // returns a list of products that are not subject to synchronization (they do not apply to products that have categories or do not apply to products
    // that have at least one category 'online store' with an online store selected)
    public Set<Integer> getProductWooIdsToDeleteInStore(String key) {
        String stringQuery="";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",key).toString());

            stringQuery=
            " select " +
                " sp_.woo_id " +
            " from " +
                " stores_products sp_ " +
            " where " +
                " sp_.woo_id is not null and sp_.store_id ="+storeId+" and sp_.product_id not in " +
                "( " +
                    " select p.id from products p " +
                    " INNER JOIN product_productcategories ppc ON ppc.product_id=p.id " +
                    " INNER JOIN product_categories pc ON pc.id=ppc.category_id " +
                    " INNER JOIN stores_productcategories spc ON pc.id=spc.category_id " +
                    " INNER JOIN stores s on s.id=spc.store_id " +
                    " LEFT OUTER JOIN stores_products sp ON sp.product_id = p.id and sp.store_id=s.id " +
                    " where " +
                    " p.company_id = " + companyId +
                    " and coalesce(pc.is_store_category,false)=true " +

                    " and coalesce(p.is_deleted, false) = false " +

                    " and s.id = " + storeId +

                    " and p.id not in (select variation_product_id from product_variations where master_id = " + masterId + ")" + // product is not used as variation

                    " and coalesce(s.is_deleted, false) = false " +

                " ) ";

            Query query = entityManager.createNativeQuery(stringQuery);
            Set<Integer> productIds = new HashSet<>();
            for (Object i : query.getResultList()) {
                productIds.add((Integer) i);
            }
            return productIds;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/getProductWooIdsToDeleteInStore. Key:"+key, e);
            e.printStackTrace();
            return null;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/getProductWooIdsToDeleteInStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer deleteWooIdsFromProducts(IntListForm request){
        String stringQuery = "";
        try {
            String wooIds = cu.ListOfIntToString(request.getIdsSet(),",","(", ")");
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());

                stringQuery=
                " update stores_products set woo_id = null where " +
                "     master_id = " + masterId +
                " and company_id = "+ companyId +
                " and store_id = " + storeId +
                " and coalesce(woo_id, 0) in " + wooIds;

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/syncProductsIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            logger.error("Exception in method getProductCategoriesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private List<Integer> getUpsellCrosssells(Long productId, String tableName, Long storeId) throws Exception {
        String stringQuery =
                "select woo_id from stores_products where store_id = " + storeId + " and product_id in (" +
                    "select p.child_id from "+tableName+" p where p.product_id= " + productId +
                ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (List<Integer>) query.getResultList();
        } catch (Exception e) {
            logger.error("Exception in method geUpsellCrosssells. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }


    private List<Integer> getVariationsWooIds(Long productId, Long storeId) throws Exception {
        String stringQuery =
                " select sv.woo_id from stores_variations sv " +
                " inner join product_variations pv on sv.product_id = pv.variation_product_id" +
                " where sv.store_id = " + storeId +
                " and pv.product_id="+productId+" and sv.woo_id is not null";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (List<Integer>) query.getResultList();
        } catch (Exception e) {
            logger.error("Exception in method getVariationsWooIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
