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
import com.dokio.message.response.store.woo.v3.products.*;
import com.dokio.repository.CompanyRepositoryJPA;
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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Repository
public class StoreVariationsRepository {

    private Logger logger = Logger.getLogger(StoreVariationsRepository.class);

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
    @Autowired
    StoreProductsRepository storeProductsRepository;
    @Autowired
    StoreRepository storeRepository;



    @SuppressWarnings("Duplicates")
    public ProductCountJSON countVariationsToStoreSync(String key) {
        String stringQuery = "";
        ProductCountJSON result = new ProductCountJSON();
        try{
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",key).toString());
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            String langCode = (String)cu.getByCrmSecretKey("lang_code", key);


//            -- SELECT variations
//                                               WHERE
//            -- CHILD product:  1. Not deleted;
//                               2. Was changed and need to be synchronized
//            --                                  OR
//            -- PARENT product: 1. Is in store category;
//                               2. Not deleted;
//                               3. Was changed and need to be synchronized

            stringQuery =
           // " select pv.variation_product_id from product_variations pv" +
            " select count(*) from product_variations pv" +
                                                              " WHERE " +

            " pv.variation_product_id in ( "+
                " select p.id from products p "+
                " INNER JOIN product_productcategories ppc ON ppc.product_id=p.id "+
                " INNER JOIN product_categories pc ON pc.id=ppc.category_id "+
                " INNER JOIN stores_productcategories spc ON pc.id=spc.category_id "+
                " INNER JOIN stores s on s.id=spc.store_id "+
                " LEFT OUTER JOIN stores_variations sp ON sp.product_id = p.id and sp.store_id = " + storeId +
                " where"+
                " p.company_id =  " + companyId +
                " and coalesce(p.is_deleted, false) = false " +
                " and coalesce(pc.is_store_category,false)=true " +
                " and p.id in (select variation_product_id from product_variations where master_id = "+masterId+") " +
                " and s.id = "+storeId+" and " +
                "("+
                    " (coalesce(sp.need_to_syncwoo,true) = true) or " +
                    " (sp.date_time_syncwoo is null) or"+
                    " (p.date_time_changed is not null and sp.date_time_syncwoo is not null and p.date_time_changed > sp.date_time_syncwoo) " +
                " ) " +
            " ) "
            ;

            Query query = entityManager.createNativeQuery(stringQuery);
            result.setQueryResultCode(1);
            result.setProductCount((BigInteger)query.getSingleResult());
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreVariationsRepository/countVariationsToStoreSync. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        } catch (NoResultException nre) {
            logger.error("Exception in method woo/v3/StoreVariationsRepository/countVariationsToStoreSync. SQL query:"+stringQuery, nre);
            nre.printStackTrace();
            result.setQueryResultCode(1);
            result.setProductCount(new BigInteger("0"));
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreVariationsRepository/countVariationsToStoreSync. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    @SuppressWarnings("Duplicates")
    public VariationsJSON syncVariationsToStore(String key, Integer firstResult, Integer maxResults) {
        VariationsJSON result = new VariationsJSON();
        String stringQuery = "";

        try{
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",key).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
            String langCode = (String)cu.getByCrmSecretKey("lang_code", key);

            stringQuery =

            " select " +

                " p.id as crm_id, " +
                " sp.woo_id as woo_id, " +
                " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
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
                " coalesce(p.description_type, 'editor') as description_type, " +
                " coalesce(p.short_description_type, 'editor') as short_description_type, " +
                " sp1.woo_id as parent_product_woo_id, " +
                " pv.id as variation_id" +

            " from products p " +

                " INNER JOIN stores s on s.id = " + storeId +
                " INNER JOIN product_variations pv ON p.id = pv.variation_product_id " +
                " LEFT OUTER JOIN store_translate_products translator ON p.id = translator.product_id and translator.lang_code = '" + langCode + "' " +
                " LEFT OUTER JOIN stores_variations sp ON sp.product_id = p.id and sp.store_id = " + storeId +
                " LEFT OUTER JOIN stores_products  sp1 ON sp1.product_id = pv.product_id and sp1.store_id = " + storeId +
                " LEFT OUTER JOIN product_prices ppr on ppr.product_id = p.id and ppr.price_type_id = s.store_price_type_regular " +
                " LEFT OUTER JOIN product_prices pps on pps.product_id = p.id and pps.price_type_id = s.store_price_type_sale " +

            " where p.id in ( " +

                " select pv.variation_product_id from product_variations pv  " +

                                                            " WHERE " +

                " pv.variation_product_id in ( " +
                    " select p.id from products p " +
                    " INNER JOIN product_productcategories ppc ON ppc.product_id=p.id "+
                    " INNER JOIN product_categories pc ON pc.id=ppc.category_id "+
                    " INNER JOIN stores_productcategories spc ON pc.id=spc.category_id "+
                    " INNER JOIN stores s on s.id=spc.store_id "+
                    " LEFT OUTER JOIN stores_variations sp ON sp.product_id = p.id and sp.store_id = " + storeId +
                    " where " +
                    " p.company_id =  " + companyId +
                    " and coalesce(p.is_deleted, false) = false " +
                    " and coalesce(pc.is_store_category,false)=true " +
                    " and p.id in (select variation_product_id from product_variations where master_id = "+masterId+") " +
                    " and s.id = "+storeId+" and " +
                    " ( " +
                        "(coalesce(sp.need_to_syncwoo,true) = true) or " +
                        "(sp.date_time_syncwoo is null) or " +
                        "(p.date_time_changed is not null and sp.date_time_syncwoo is not null and p.date_time_changed > sp.date_time_syncwoo) " +
                    ") " +
                ") " +
            ") ";

            Query query = entityManager.createNativeQuery(stringQuery);
            if (firstResult != null) {query.setFirstResult(firstResult);} // from 0
            if (maxResults != null) {query.setMaxResults(maxResults);}
            List<Object[]> queryList = query.getResultList();
            List<VariationJSON> returnList = new ArrayList<>();
            List<Long> storeDepartments = companyRepository.getCompanyStoreDepartmentsIds(companyId,storeId,masterId);
            for (Object[] obj : queryList) {
                VariationJSON doc = new VariationJSON();
                doc.setCrm_id(Long.parseLong(                       obj[0].toString()));
                doc.setWoo_id((Integer)                             obj[1]);
                doc.setName((String)                                obj[2]);
                doc.setDescription(((String)obj[17]).equals("editor")?((String)obj[3]):((String)obj[5]));
                doc.setShort_description(((String)obj[18]).equals("editor")?((String)obj[4]):((String)obj[6]));
                doc.setRegular_price(                               Objects.isNull(obj[7])?"":obj[7].toString());
                doc.setSale_price(storeProductsRepository.getSalePrice((BigDecimal)obj[7],(BigDecimal)obj[8]));
                doc.setImage(                                       getVariationImage(Long.parseLong(obj[0].toString()),companyId));
                doc.setStock_status((String)                        obj[9]);
                doc.setSku((String)                                 obj[10]);
                doc.setSold_individually((Boolean)                  obj[11]);
                doc.setBackorders((String)                          obj[12]); //If managing stock, this controls if backorders are allowed. Options: no, notify and yes. Default is no
                doc.setManage_stock((Boolean)                       obj[13]);
                doc.setPurchase_note((String)                       obj[14]);
                doc.setMenu_order((Integer)                         obj[15]);
                doc.setReviews_allowed((Boolean)                    obj[16]);
                doc.setStock_quantity(productsRepository.getAvailable(doc.getCrm_id(), storeDepartments, true).intValue());
                doc.setParent_product_woo_id((Integer)              obj[19]);
                doc.setListOfVariationAttributes(getListOfVariationAttributes(Long.parseLong(obj[20].toString()),storeId,langCode));
                returnList.add(doc);
            }
            result.setQueryResultCode(1);
            result.setVariations(returnList);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreVariationsRepository/syncVariationsToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        } catch (NoResultException nre) {
            logger.error("Exception in method woo/v3/StoreVariationsRepository/syncVariationsToStore. SQL query:"+stringQuery, nre);
            nre.printStackTrace();
            result.setQueryResultCode(1);
            result.setVariations(new ArrayList<>());
            return result;
        }catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreVariationsRepository/syncVariationsToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }

    // WooCommerce use only one image per variation
    private ImageJSON getVariationImage(Long productId, Long companyId) throws Exception {
        String stringQuery =
                "           select " +
                        "           coalesce(f.original_name,'') as img_original_name, " +
                        "           coalesce(f.name,'') as img_name, " +
                        "           coalesce(f.alt,'') as img_alt " +
                        "           from " +
                        "           products p " +
                        "           inner join product_files pf on p.id=pf.product_id " +
                        "           inner join files f on pf.file_id=f.id " +
                        "           where " +
                        "           p.id= " + productId +
                        "           and f.trash is not true " +
                        "           and p.company_id= " + companyId +
                        "           and coalesce(f.anonyme_access, false) is true " +
                        "           order by pf.output_order limit 1";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            ImageJSON doc = new ImageJSON();
            if (queryList.size() > 0) {
                doc.setImg_original_name((String) queryList.get(0)[0]);
                doc.setImg_address(apiserver + "/api/public/getFile/" + queryList.get(0)[1]);
                doc.setImg_alt((String) queryList.get(0)[2]);
            }
            return doc;
        } catch (Exception e) {
            logger.error("Exception in method StoreVariationsRepository/getVariationImage. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncVariationsIds(SyncIdsForm request) {
        String stringQuery = "";
        try {
            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",request.getCrmSecretKey()).toString());
            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",request.getCrmSecretKey()).toString());
            Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",request.getCrmSecretKey()).toString());

            for (SyncIdForm row : request.getIdsSet()) {
                syncVariationId(row, companyId, masterId, storeId );
            }

            storeRepository.saveStoreSyncStatus(storeId, "products", masterId, "end");
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreVariationsRepository/syncVariationsIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreVariationsRepository/syncVariationsIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    private Boolean syncVariationId(SyncIdForm ids, Long companyId, Long masterId, Long storeId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " insert into stores_variations (" +
                            " master_id, " +
                            " company_id, " +
                            " store_id, " +
                            " product_id, " +
                            " woo_id," +
                            " need_to_syncwoo, " +
                            " date_time_syncwoo " +
                            " ) values (" +
                            masterId +  ", " +
                            companyId + ", " +
                            storeId +   ", " +
                            "(select id from products where master_id = "+masterId+" and company_id = "+companyId+" and id = " + ids.getCrm_id() + "), " +
                            ids.getId() + "," +
                            " false, " +
                            " now()) " +
                            " ON CONFLICT ON CONSTRAINT stores_variations_uq " +// "upsert"
                            " DO update set " +
                            " woo_id = " + ids.getId() + ", " +
                            " need_to_syncwoo = false, " +
                            " date_time_syncwoo = now()";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreVariationsRepository/syncVariationId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }



    // возвращает список ВАРИАЦИЙ которые не подлежат синхронизации
    // returns a list of VARIATIONS that are not subject to synchronization

//    public VariationsAndParentProductsJSON getVariationsWooIdsToDeleteInStore(String key) {
//        String stringQuery="";
//            VariationsAndParentProductsJSON result = new VariationsAndParentProductsJSON();
//        try {
//            Long companyId = Long.valueOf(cu.getByCrmSecretKey("company_id",key).toString());
//            Long storeId = Long.valueOf(cu.getByCrmSecretKey("id",key).toString());
//            stringQuery=
//                    " select " +
//                    " sp_.woo_id, " +
//                    " (select woo_id from stores_products where product_id = pv.product_id) as parent_product_woo_id" +
//                    " from " +
//                    " stores_variations sp_ " +
//                    " inner join product_variations pv on sp_.product_id = pv.variation_product_id " +
//                    " where " +
//                    " sp_.woo_id is not null and sp_.store_id = "+storeId+" and sp_.product_id not in " +
//                    "( " +
//
//                        "select pv.variation_product_id from product_variations pv  " +
//
//                                                        " WHERE  " +
//
//                        " pv.variation_product_id in ( " +
//                            " select p.id from products p " +
//                            " where " +
//                            " p.company_id = "  + companyId +
//                            " and coalesce(p.is_deleted, false) = false " +
//
//                        ") " +

//                                                        " OR " +
//
//                        "pv.product_id in ( " +
//                            " select p.id from products p " +
//                            " INNER JOIN product_productcategories ppc ON ppc.product_id=p.id " +
//                            " INNER JOIN product_categories pc ON pc.id=ppc.category_id " +
//                            " INNER JOIN stores_productcategories spc ON pc.id=spc.category_id " +
//                            " INNER JOIN stores s on s.id=spc.store_id " +
//                            //" LEFT OUTER JOIN stores_products sp ON sp.product_id = p.id and sp.store_id=s.id " +
//                            " where " +
//                            " p.company_id = " + companyId +
//                            " and coalesce(pc.is_store_category,false)=true " +
//                            " and coalesce(p.is_deleted, false) = false " +
//                            " and s.id = " + storeId +
//                            " and coalesce(s.is_deleted, false) = false " +
//
//                        ") " +
//                    " ) ";
//
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Object[]> queryList = query.getResultList();
//            List<VariationAndParentProductJSON> returnList = new ArrayList<>();
//            for (Object[] obj : queryList) {
//                VariationAndParentProductJSON doc = new VariationAndParentProductJSON();
//                doc.setVariation_woo_id((Integer)                             obj[0]);
//                doc.setParent_product_woo_id((Integer)                        obj[1]);
//                returnList.add(doc);
//            }
//            result.setQueryResultCode(1);
//            result.setVariations_and_parent_products(returnList);
//            return result;
//        }catch (WrongCrmSecretKeyException e) {
//            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreVariationsRepository/getVariationsWooIdsToDeleteInStore. Key:"+key, e);
//            e.printStackTrace();
//            result.setQueryResultCode(-200);
//            return result;
//        } catch (NoResultException nre) {
//            logger.error("Exception in method woo/v3/StoreVariationsRepository/getVariationsWooIdsToDeleteInStore. SQL query:"+stringQuery, nre);
//            nre.printStackTrace();
//            result.setQueryResultCode(1);
//            result.setVariations_and_parent_products(new ArrayList<>());
//            return result;
//        }catch (Exception e) {
//            logger.error("Exception in method woo/v3/StoreVariationsRepository/getVariationsWooIdsToDeleteInStore. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            result.setQueryResultCode(null);
//            return result;
//        }
//    }




    // returns List of product attributes of product by its ID with selected terms
    private List<VariationAttributesJSON> getListOfVariationAttributes(Long variationId, Long storeId, String langCode) throws Exception {
        String stringQuery =
        " select " +
        " spt.woo_id as id, " +  //woo_id of attribute
        " coalesce(NULLIF(translator.name, ''), pat.name) as name " +
        " from product_variations_row_items pvri " +
        " inner join product_attribute_terms pat on pat.id=pvri.term_id " +
        " inner join stores_attributes spt ON spt.attribute_id = pvri.attribute_id and  spt.store_id= " + storeId +
        " left outer join store_translate_terms translator ON pvri.term_id = translator.term_id and translator.lang_code = '" + langCode + "'"  +
        " where pvri.variation_id = " + variationId;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<VariationAttributesJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                VariationAttributesJSON doc = new VariationAttributesJSON();
                doc.setId((Integer)                         obj[0]);
                doc.setOption((String)                      obj[1]);
                returnList.add(doc);
            }
            return returnList;

        } catch (Exception e) {
            logger.error("Exception in method woo/v3/StoreVariationsRepository/getListOfVariationAttributes. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


}
