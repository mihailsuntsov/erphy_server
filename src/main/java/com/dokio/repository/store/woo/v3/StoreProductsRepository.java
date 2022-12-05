package com.dokio.repository.store.woo.v3;

import com.dokio.message.request.store.woo.v3.IntListForm;
import com.dokio.message.request.store.woo.v3.SyncIdForm;
import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.response.additional.FilesProductImagesJSON;
import com.dokio.message.response.store.woo.v3.products.ImageJSON;
import com.dokio.message.response.store.woo.v3.products.ProductCountJSON;
import com.dokio.message.response.store.woo.v3.products.ProductJSON;
import com.dokio.message.response.store.woo.v3.products.ProductsJSON;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import javax.persistence.EntityManager;
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

    public ProductCountJSON countProductsToStoreSync(String key) {
        Long companyId = cu.getByCrmSecretKey("id",key);
        ProductCountJSON result = new ProductCountJSON();
        String stringQuery =
                        " select count(*) from products p  " +
                        " where p.company_id = " + companyId +
                        " and coalesce(p.is_deleted, false) = false " +
                        " and (" +
                        // if the product is created recently, or changed, but still not synchronized
                        "(p.date_time_syncwoo is null) or " +
                        // or product is changed after synchronization
                        "(p.date_time_changed is not null and p.date_time_syncwoo is not null and p.date_time_changed > p.date_time_syncwoo)" +
                        ")";
        try{
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            Query query = entityManager.createNativeQuery(stringQuery);
            result.setQueryResultCode(1);
            result.setProductCount((BigInteger)query.getSingleResult());
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/syncProductsToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    public ProductsJSON syncProductsToStore(String key, Integer firstResult, Integer maxResults) {
        Long companyId = cu.getByCrmSecretKey("id",key);
        ProductsJSON result = new ProductsJSON();
        String stringQuery =
                        " select " +
                        " p.id as crm_id, " +
                        " p.woo_id as woo_id, " +
                        " p.name as name, " +
                        " coalesce(p.type, 'simple') as type, " +
                        " coalesce(p.description, '') as description, " +
                        " coalesce(p.short_description, '') as short_description, " +
                        " ppr.price_value as price_regular, " +
                        " pps.price_value as price_sale " +
                        " from products p  " +
                        " inner join companies c on c.id = p.company_id  " +
                        " left outer join product_prices ppr on ppr.product_id = p.id and ppr.price_type_id = c.store_price_type_regular " +
                        " left outer join product_prices pps on pps.product_id = p.id and pps.price_type_id = c.store_price_type_sale " +
                        " where p.company_id = " + companyId +
                        " and coalesce(p.is_deleted, false) = false " +
                        " and (" +
                                // if the product is created recently, or changed, but still not synchronized
                                "(p.date_time_syncwoo is null) or " +
                                // or product is changed after synchronization
                                "(p.date_time_changed is not null and p.date_time_syncwoo is not null and p.date_time_changed > p.date_time_syncwoo)" +
                            ")";
        try{
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            Query query = entityManager.createNativeQuery(stringQuery);
            if (firstResult != null) {query.setFirstResult(firstResult);} // from 0
            if (maxResults != null) {query.setMaxResults(maxResults);}
            List<Object[]> queryList = query.getResultList();
            List<ProductJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductJSON doc = new ProductJSON();
                doc.setCrm_id(Long.parseLong(                       obj[0].toString()));
                doc.setWoo_id((Integer)                             obj[1]);
                doc.setName((String)                                obj[2]);
                doc.setType((String)                                obj[3]);
                doc.setDescription((String)                         obj[4]);
                doc.setShort_description((String)                   obj[5]);
                doc.setRegular_price(                               Objects.isNull(obj[6])?"":obj[6].toString());
                doc.setSale_price(                                  getSalePrice((BigDecimal)obj[6],(BigDecimal)obj[7]));
                doc.setImages(                                      getSetOfProductImages(Long.parseLong(obj[0].toString()),companyId));
                doc.setCategories(                                  getProductCategoriesIds(Long.parseLong(obj[0].toString())));
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
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductsRepository/syncProductsToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    private String getSalePrice(BigDecimal Regular_price, BigDecimal Sale_price){
        if(!Objects.isNull(Regular_price) && !Objects.isNull(Sale_price) && Regular_price.compareTo(Sale_price)>0)
            return Sale_price.toString();
        else return "";
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncProductsIds(SyncIdsForm request) {
        String stringQuery = "";
        Long companyId = cu.getByCrmSecretKey("id",request.getCrmSecretKey());
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            for (SyncIdForm row : request.getIdsSet()) {
                syncProductId(row, companyId);
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

    private Boolean syncProductId(SyncIdForm ids, Long companyId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " update products " +
                            " set " +
                            " woo_id = " + ids.getId() + ", " +
                            " date_time_syncwoo = now() " +
                            " where " +
                            " company_id = " + companyId + " and " +
                            " id = " + ids.getCrm_id() + " and " +
                            ids.getCrm_id() + " in (select id from products where company_id = "+companyId+")";


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

    private Set<Long> getProductCategoriesIds(Long productId) throws Exception {
        String stringQuery="" +
                " select c.woo_id " +
                " from " +
                " product_categories c " +
                " inner join product_productcategories ppc on c.id = ppc.category_id" +
                " where " +
                " ppc.product_id = " + productId +
                " and coalesce(c.is_store_category, false) = true";
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

    public Set<Integer> getProductWooIdsToDeleteInStore(String key) {
        Long companyId = cu.getByCrmSecretKey("id",key);
        String stringQuery="" +
        " select p.woo_id " +
        " from products p where  " +
        " p.company_id = "+companyId+" and  " +
        " p.woo_id is not null and " +
        "(" +
            "( " +
    //       не относится к товарам у которых есть категории
            " p.id not in (select product_id from product_productcategories) or " +
    //       или не относится к товарам, у которых есть хотя бы одна категория 'интернет-магазин'
            " p.id not in (select product_id from product_productcategories where category_id in (select id from product_categories where coalesce(is_store_category,false)=true)) " +
            " ) " +
            "or coalesce(p.is_deleted, false) = true " +
        " )";
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            Query query = entityManager.createNativeQuery(stringQuery);
            Set<Integer> productIds = new HashSet<>();
            for (Object i : query.getResultList()) {
                productIds.add((Integer) i);
            }
            return productIds;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductsRepository/syncProductsIds. Key:"+key, e);
            e.printStackTrace();
            return null;
        }catch (Exception e) {
            logger.error("Exception in method getProductCategoriesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public Integer deleteWooIdsFromProducts(IntListForm request){
        String wooIds = cu.ListOfIntToString(request.getIdsSet(),",","(", ")");
        Long companyId = cu.getByCrmSecretKey("id",request.getCrmSecretKey());
        String stringQuery="update products p set p.woo_id = null where " +
                " p.company_id = "+companyId+" and  " +
                "coalesce(p.woo_id,0) in "+wooIds;
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
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

}
