/*
        DokioCRM - server part. Sales, finance and warehouse management system
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

package com.dokio.repository.store.woo.v3;

import com.dokio.message.request.store.woo.v3.SyncIdForm;
import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.response.store.woo.v3.ProductCategoriesJSON;
import com.dokio.message.response.store.woo.v3.ProductCategoryJSON;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.repository.StoreRepository;
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
import java.util.*;

@Repository
public class StoreProductCategoriesRepository {

    private Logger logger = Logger.getLogger(StoreProductCategoriesRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    StoreRepository storeRepository;

    public ProductCategoriesJSON syncProductCategoriesToStore(String key) {
        ProductCategoriesJSON result = new ProductCategoriesJSON();
        try{
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            String langCode = (String)cu.getByCrmSecretKey("lang_code", key);
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",key).toString());
            storeRepository.saveStoreSyncStatus(storeId, "products", masterId, "begin");

            String stringQuery2;
            String stringQuery =
            " select " +
            " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
            " coalesce(NULLIF(translator.description, ''), coalesce(p.description,'')) as description, " +
            " coalesce(NULLIF(translator.slug, ''), coalesce(p.slug,'')) as slug, " +
            " coalesce(p.display, 'default') as display, " +
            " p.id as crm_id, " +
            " coalesce(p.parent_id, 0) as parent_crm_id, " +
            " spc.woo_id as woo_id, " +
            " parent_ctg.woo_id as parent_woo_id, " +
            " coalesce(f.original_name,'') as img_original_name, " +
            " coalesce(f.name,'') as img_name, " +
            " coalesce(f.alt,'') as img_alt, " +
            " coalesce(f.anonyme_access, false) as img_anonyme_access, " +
            " coalesce(p.output_order,10000) as menu_order, " +
            " coalesce(parent_ctg_.is_store_category,false) as is_parent_store_category" +
            //        " spc.store_id as store_id " +
            " from product_categories p " +
            " INNER JOIN companies c ON p.company_id = c.id  " +
            " INNER JOIN stores_productcategories spc ON spc.category_id = p.id  " +
            " INNER JOIN stores str ON spc.store_id = str.id " +
            " LEFT OUTER JOIN files f ON p.image_id=f.id  " +
            " LEFT OUTER JOIN stores_productcategories parent_ctg ON p.parent_id=parent_ctg.category_id and parent_ctg.store_id=str.id " +
            " LEFT OUTER JOIN product_categories parent_ctg_ ON p.parent_id=parent_ctg_.id   " +
            " LEFT OUTER JOIN store_translate_categories translator ON p.id = translator.category_id and translator.lang_code = '" + langCode + "'" +
            " where p.company_id = " + companyId +
            " and str.id = " + storeId +
            " and coalesce(p.is_store_category, false) = true " +
            " and (" +
                    " (coalesce(spc.need_to_syncwoo,true) = true) or " + // if the category is need to be synchronized
                    " (spc.date_time_syncwoo is null) or " + // if the category is created recently, or changed, but still not synchronized
                    " (p.date_time_changed is not null and spc.date_time_syncwoo is not null and p.date_time_changed > spc.date_time_syncwoo)" +
            " ) ";

            stringQuery2=" select " +
                    " spc.woo_id as woo_id " +
                    " from product_categories p " +
                    " INNER JOIN companies c ON p.company_id = c.id  " +
                    " INNER JOIN stores_productcategories spc ON spc.category_id = p.id  " +
                    " INNER JOIN stores str ON spc.store_id = str.id " +
                    " where p.company_id = " + companyId +
                    " and str.id = " + storeId +
                    " and coalesce(p.is_store_category, false) = true ";

            Query query = entityManager.createNativeQuery(stringQuery);
            //.setFirstResult(offsetreal)
            //.setMaxResults(result);
            List<Object[]> queryList = query.getResultList();
            List<ProductCategoryJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductCategoryJSON doc = new ProductCategoryJSON();
                doc.setName((String)                                obj[0]);
                doc.setDescription((String)                         obj[1]);
                doc.setSlug((String)                                obj[2]);
                doc.setDisplay((String)                             obj[3]);
                doc.setCrm_id(Long.parseLong(                       obj[4].toString()));
                doc.setParent_crm_id(((Boolean)obj[13])?Long.parseLong(obj[5].toString()):0L);// If parent category of the current category is not "store category" - then the current category will be the one of root categories in WooCommerce
                doc.setWoo_id((Integer)                             obj[6]);
                doc.setParent_woo_id((Integer)                      obj[7]);
                doc.setImg_original_name((String)                   obj[8]);
                doc.setImg_address(((Boolean)obj[11])?apiserver+"/api/public/getFile/"+obj[9]:null); // if the image is not shared file - then it couldn't be opened in WooCommerce
                doc.setImg_alt((String)                             obj[10]);
                doc.setImg_anonyme_access((Boolean)                 obj[11]);
                doc.setMenu_order((Integer)                         obj[12]);
                returnList.add(doc);
            }
            query = entityManager.createNativeQuery(stringQuery2);
            List<Integer> allStoreWooIds = (List<Integer>)query.getResultList();

            result.setQueryResultCode(1);
            result.setProductCategories(returnList);
            result.setAllProductCategoriesWooIds(allStoreWooIds);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncProductCategoriesIds(SyncIdsForm request) {
        try{
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());
            // if category was deleted in the store side, its products will lost their belonging to this category.
            // And if this category recreated, these products will not be assigned to this category
            // So, need to mark this products as need to be resynchronized
            // Set<Long> setOfCategoriesIdsWhoseProductsNeedToBeResynchronized= new HashSet<>();
            for (SyncIdForm row : request.getIdsSet()) {
                syncProductCategoryId(row, companyId, storeId, masterId);
                //setOfCategoriesIdsWhoseProductsNeedToBeResynchronized.add(row.getCrm_id());
            }
            //productsRepository.markProductsOfCategoriesAndStoresAsNeedToSyncWoo(setOfCategoriesIdsWhoseProductsNeedToBeResynchronized,masterId, new ArrayList<>(Arrays.asList(storeId)));
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesIds. request:"+request.toString(), e);
            e.printStackTrace();
            return null;
        }
    }
    private Boolean syncProductCategoryId(SyncIdForm ids, Long companyId, Long storeId, Long masterId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =   " insert into stores_productcategories (" +
                            " master_id, " +
                            " company_id, " +
                            " store_id, " +
                            " category_id, " +
                            " woo_id," +
                            " need_to_syncwoo, " +
                            " date_time_syncwoo " +
                            " ) values (" +
                            masterId + ", " +
                            companyId + ", " +
                            storeId + ", " +
                            "(select id from product_categories where master_id = "+masterId+" and company_id = "+companyId+" and id = " + ids.getCrm_id() + "), " +
                            ids.getId()+ "," +
                            " false, " +
                            " now()) " +
                            " ON CONFLICT ON CONSTRAINT stores_categories_uq " +// "upsert"
                            " DO update set " +
                            " woo_id = "+ids.getId() + ", " +
                            " need_to_syncwoo = false, " +
                            " date_time_syncwoo = now()";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreProductCategoriesRepository/syncProductCategoryId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
}
