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
import com.dokio.message.response.store.woo.v3.ProductAttributeJSON;
import com.dokio.message.response.store.woo.v3.ProductAttributesJSON;
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
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Repository
public class StoreProductAttributesRepository {

    private Logger logger = Logger.getLogger(StoreProductAttributesRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;
    @Autowired
    ProductsRepositoryJPA productsRepository;

    public ProductAttributesJSON syncProductAttributesToStore(String key) {
        String stringQuery="";
        ProductAttributesJSON result = new ProductAttributesJSON();
        try{
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            String langCode = (String)cu.getByCrmSecretKey("lang_code", key);

            stringQuery =
                " select " +
                " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
                " coalesce(p.type, 'select') as type," +
                " coalesce(NULLIF(translator.slug, ''), coalesce(p.slug,'')) as slug, " +
                " coalesce(p.order_by, 'menu_order') as order_by," +
                " coalesce(p.has_archives,false) as has_archives," +
                " p.id as crm_id," +
                " spc.woo_id as woo_id " +
                " from product_attributes p" +
                " INNER JOIN stores_attributes spc ON spc.attribute_id = p.id  " +
                " INNER JOIN stores str ON spc.store_id = str.id " +
                " LEFT OUTER JOIN store_translate_attributes translator ON p.id = translator.attribute_id and translator.lang_code = '" + langCode + "'" +
                " where p.company_id = " + companyId +
                " and str.id = " + storeId +
                " and p.is_deleted = false " +
                " and str.is_deleted=false";


            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ProductAttributeJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ProductAttributeJSON doc = new ProductAttributeJSON();
                doc.setName((String)                                obj[0]);
                doc.setType((String)                                obj[1]);
                doc.setSlug((String)                                obj[2]);
                doc.setOrder_by((String)                            obj[3]);
                doc.setHas_archives((Boolean)                       obj[4]);
                doc.setCrm_id(Long.parseLong(                       obj[5].toString()));
                doc.setWoo_id((Integer)                             obj[6]);
                returnList.add(doc);
            }
            result.setQueryResultCode(1);
            result.setProductAttributes(returnList);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductAttributesRepository/syncProductAttributesToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreProductAttributesRepository/syncProductAttributesToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncProductAttributesIds(SyncIdsForm request) {
        String stringQuery = "";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());
            // if attribute was deleted in the store side, its products will lost their belonging to this attribute.
            // And if this attribute recreated, these products will not be assigned to this attribute.
            // So, need to mark this products as need to be resynchronized
            Set<Long> setOfAttributesdIdsWhoseProductsNeedToBeResynchronized= new HashSet<>();
            for (SyncIdForm row : request.getIdsSet()) {
                syncProductAttributeId(row, companyId, storeId, masterId);
                setOfAttributesdIdsWhoseProductsNeedToBeResynchronized.add(row.getCrm_id());
            }
            productsRepository.markProductsOfAttributesAsNeedToSyncWoo(setOfAttributesdIdsWhoseProductsNeedToBeResynchronized,masterId, new ArrayList<>(Arrays.asList(storeId)));
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreProductAttributesRepository/syncProductAttributesIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreProductAttributesRepository/syncProductAttributesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean syncProductAttributeId(SyncIdForm ids, Long companyId, Long storeId, Long masterId) throws Exception {
        String stringQuery="";
        try {
            stringQuery = "insert into stores_attributes (" +
                    " master_id, " +
                    " company_id, " +
                    " store_id, " +
                    " attribute_id, " +
                    " woo_id" +
                    " ) values (" +
                    masterId + ", " +
                    companyId + ", " +
                    storeId + ", " +
                    "(select id from product_attributes where master_id = "+masterId+" and company_id = "+companyId+" and id = " + ids.getCrm_id() + "), " +
                    ids.getId() + ") " +
                    " ON CONFLICT ON CONSTRAINT stores_attributes_uq " +// "upsert"
                    " DO update set " +
                    " woo_id = "+ids.getId();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreProductAttributesRepository/syncProductAttributeId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
}
