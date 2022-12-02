package com.dokio.repository.store.woo.v3;

import com.dokio.message.request.store.woo.v3.SyncIdForm;
import com.dokio.message.request.store.woo.v3.SyncIdsForm;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class StoreProductsRepository {

    private Logger logger = Logger.getLogger(StoreProductsRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;



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
                        " where p.company_id = 1 " +
                        " and coalesce(p.is_deleted, false) = false";
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
                doc.setRegular_price((String)                       obj[6]);
                doc.setSale_price((String)                          obj[7]);
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
                            " woo_id = " + ids.getId() +
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



}
