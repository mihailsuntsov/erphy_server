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
import com.dokio.message.response.store.woo.v3.AttributeTermJSON;
import com.dokio.message.response.store.woo.v3.AttributeTermsJSON;
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

@Repository
public class StoreAttributeTermsRepository {

    private Logger logger = Logger.getLogger(StoreAttributeTermsRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;

    public AttributeTermsJSON syncAttributeTermsToStore(String key) {
        String stringQuery = "";
        AttributeTermsJSON result = new AttributeTermsJSON();
        try{
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            String langCode = (String)cu.getByCrmSecretKey("lang_code", key);

            stringQuery=" select " +
                        " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
                        " coalesce(NULLIF(translator.description, ''), coalesce(p.description,'')) as description, " +
                        " coalesce(NULLIF(translator.slug, ''), coalesce(p.slug,'')) as slug, " +
                        " coalesce(p.menu_order,0) as menu_order," +
                        " p.id as crm_id," +
                        " spt.woo_id as woo_id, " +
                        " pa.id as attribute_crm_id," +
                        " spc.woo_id as attribute_woo_id" +
                        " from product_attribute_terms p" +
                        " INNER JOIN product_attributes pa ON pa.id = p.attribute_id" +
                        " INNER JOIN stores_attributes spc ON spc.attribute_id = p.attribute_id" +
                        " INNER JOIN stores_terms spt ON spt.term_id = p.id  " +
                        " INNER JOIN stores str ON spc.store_id = str.id " +
                        " LEFT OUTER JOIN store_translate_terms translator ON p.id = translator.term_id and translator.lang_code = '" + langCode + "'" +
                        " where pa.company_id = " + companyId +
                        " and str.id = " + storeId +
                        " and pa.is_deleted = false";
            Query query = entityManager.createNativeQuery(stringQuery);
            //.setFirstResult(offsetreal)
            //.setMaxResults(result);
            List<Object[]> queryList = query.getResultList();
            List<AttributeTermJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                AttributeTermJSON doc = new AttributeTermJSON();
                doc.setName((String)                                obj[0]);
                doc.setDescription((String)                         obj[1]);
                doc.setSlug((String)                                obj[2]);
                doc.setMenu_order((Integer)                         obj[3]);
                doc.setCrm_id(Long.parseLong(                       obj[4].toString()));
                doc.setWoo_id((Integer)                             obj[5]);
                doc.setAttribute_crm_id(Long.parseLong(             obj[6].toString()));
                doc.setAttribute_woo_id((Integer)                   obj[7]);
                returnList.add(doc);
            }
            result.setQueryResultCode(1);
            result.setAttributeTerms(returnList);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreAttributeTermsRepository/syncAttributeTermsToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreAttributeTermsRepository/syncAttributeTermsToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncAttributeTermsIds(SyncIdsForm request) {
        String stringQuery = "";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());

            for (SyncIdForm row : request.getIdsSet()) {
                syncAttributeTermId(row, companyId, storeId, masterId);
            }
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreAttributeTermsRepository/syncAttributeTermsIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreAttributeTermsRepository/syncAttributeTermsIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    private Boolean syncAttributeTermId(SyncIdForm ids, Long companyId, Long storeId, Long masterId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =   " insert into stores_terms (" +
                    " master_id, " +
                    " company_id, " +
                    " store_id, " +
                    " term_id, " +
                    " woo_id" +
                    " ) values (" +
                    masterId + ", " +
                    companyId + ", " +
                    storeId + ", " +
                    "(select id from product_attribute_terms where master_id = "+masterId+" and company_id = "+companyId+" and id = " + ids.getCrm_id() + "), " +
                    ids.getId() + ") " +
                    " ON CONFLICT ON CONSTRAINT stores_terms_uq " +// "upsert"
                    " DO update set " +
                    " woo_id = "+ids.getId();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreAttributeTermsRepository/syncAttributeTermId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


}
