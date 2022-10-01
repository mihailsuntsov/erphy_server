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

package com.dokio.repository.store.woo.v3;

import com.dokio.message.request.store.woo.v3.SyncIdForm;
import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.response.store.woo.v3.ProductCategoriesJSON;
import com.dokio.message.response.store.woo.v3.ProductCategoryJSON;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class StoreProductCategoriesRepository {

    private Logger logger = Logger.getLogger(StoreProductCategoriesRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;

    public ProductCategoriesJSON syncProductCategoriesToStore(String key) {
        Long companyId = cu.getByCrmSecretKey("id",key);
        ProductCategoriesJSON result = new ProductCategoriesJSON();
        String stringQuery =
                " select " +
                        " p.name as name," +
                        " coalesce(p.description,'') as description," +
                        " coalesce(p.display, 'default') as display," +
                        " coalesce(p.slug,'') as slug," +
                        " coalesce(p.parent_id, 0) as parent_crm_id," +
                        " p.id as crm_id," +
                        " p.woo_id as woo_id," +
                        " parent_ctg.woo_id as parent_woo_id," +
                        " coalesce(f.original_name,'') as img_original_name,"+
                        " coalesce(f.name,'') as img_name,"+
                        " coalesce(f.alt,'') as img_alt,"+
                        " coalesce(f.anonyme_access, false) as img_anonyme_access,"+
                        " coalesce(p.output_order,10000) as menu_order," +
                        " coalesce(parent_ctg.is_store_category,false) as is_parent_store_category" +
                        " from product_categories p" +
                        " LEFT OUTER JOIN files f ON p.image_id=f.id " +
                        " LEFT OUTER JOIN product_categories parent_ctg ON p.parent_id=parent_ctg.id " +
                        " where p.company_id = " + companyId +
                        " and coalesce(p.is_store_category, false) = true";
        try{
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            Query query = entityManager.createNativeQuery(stringQuery);
            //.setFirstResult(offsetreal)
            //.setMaxResults(result);
            List<Object[]> queryList = query.getResultList();
            List<ProductCategoryJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductCategoryJSON doc = new ProductCategoryJSON();
                doc.setName((String)                                obj[0]);
                doc.setDescription((String)                         obj[1]);
                doc.setDisplay((String)                             obj[2]);
                doc.setSlug((String)                                obj[3]);
                doc.setParent_crm_id(((Boolean)obj[13])?Long.parseLong(obj[4].toString()):0L);// If parent category of the current category is not "store category" - then the current category is the root category in WooCommerce
                doc.setCrm_id(Long.parseLong(                       obj[5].toString()));
                doc.setWoo_id((Integer)                             obj[6]);
                doc.setParent_woo_id((Integer)                      obj[7]);
                doc.setImg_original_name((String)                   obj[8]);
                doc.setImg_address(((Boolean)obj[11])?apiserver+"/api/public/getFile/"+obj[9]:null); // if the image is not shared file - then it couldn't be opened in WooCommerce
                doc.setImg_alt((String)                             obj[10]);
                doc.setImg_anonyme_access((Boolean)                 obj[11]);
                doc.setMenu_order((Integer)                         obj[12]);
                returnList.add(doc);
            }
            result.setQueryResultCode(1);
            result.setProductCategories(returnList);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncProductCategoriesIds(SyncIdsForm request) {
        String stringQuery = "";
        Long companyId = cu.getByCrmSecretKey("id",request.getCrmSecretKey());
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            for (SyncIdForm row : request.getIdsSet()) {
                syncProductCategoryId(row, companyId);
            }
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreProductCategoriesRepository/syncProductCategoriesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    private Boolean syncProductCategoryId(SyncIdForm ids, Long companyId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " update product_categories " +
                            " set " +
                            " woo_id = " + ids.getId() +
                            " where " +
                            " company_id = " + companyId + " and " +
                            " id = " + ids.getCrm_id() + " and " +
                            ids.getCrm_id() + " in (select id from product_categories where company_id = "+companyId+")";

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
