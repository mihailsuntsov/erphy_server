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
import com.dokio.message.request.store.woo.v3.TaxForm;
import com.dokio.message.request.store.woo.v3.TaxesForm;
import com.dokio.message.response.store.woo.v3.TaxJSON;
import com.dokio.message.response.store.woo.v3.TaxesJSON;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

@Repository
public class StoreTaxesRepository {

    private Logger logger = Logger.getLogger(StoreTaxesRepository.class);

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    CommonUtilites cu;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncTaxesFromStore(TaxesForm request) {
        String stringQuery = "";
        Long companyId = cu.getByCrmSecretKey("id",request.getCrmSecretKey());
        Long masterId = cu.getByCrmSecretKey("master_id",request.getCrmSecretKey());
        Set<Integer> wooIds = new HashSet<>();
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            for (TaxForm row : request.getTaxes()) {
                syncTax(row, masterId, companyId);
                wooIds.add(row.getId());
            }
            // удалить позиции, имеющие woo_id но которых нет в request.getTaxes()
            if(!request.getSaveTaxes()){//
                deleteTaxes(wooIds, masterId, companyId);
            }
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreTaxesRepository/syncTaxesFromStore. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreTaxesRepository/syncTaxesFromStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public TaxesJSON syncTaxesToStore(String key) {
        String stringQuery = "";
        Long companyId = cu.getByCrmSecretKey("id",key);
        TaxesJSON result = new TaxesJSON();
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            stringQuery = "select  p.woo_id as id, " +
                    "           p.id as crm_id, " +
                    "           p.name as name, " +
                    "           to_char(p.value, '90.9999') as value " +
                    "           from sprav_taxes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           where  p.company_id=" + companyId +
                    "           and coalesce(p.is_deleted,false) = false";
            Query query = entityManager.createNativeQuery(stringQuery);
//                    .setFirstResult(offsetreal)
//                    .setMaxResults(result);
            List<Object[]> queryList = query.getResultList();
            List<TaxJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                TaxJSON doc = new TaxJSON();
                doc.setId((Integer)                                 obj[0]);
                doc.setCrm_id(Long.parseLong(                       obj[1].toString()));
                doc.setName((String)                                obj[2]);
                doc.setRate(((String)                               obj[3]));
                returnList.add(doc);
            }
            result.setQueryResultCode(1);
            result.setTaxes(returnList);
            return result;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreTaxesRepository/syncTaxesToStore. Key:"+key, e);
            e.printStackTrace();
            result.setQueryResultCode(-200);
            return result;
        }catch (Exception e) {
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreTaxesRepository/syncTaxesToStore. SQL query:"+stringQuery, e);
            e.printStackTrace();
            result.setQueryResultCode(null);
            return result;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer syncTaxesIds(SyncIdsForm request) {
        String stringQuery = "";
        Long companyId = cu.getByCrmSecretKey("id",request.getCrmSecretKey());
        try {
            if(Objects.isNull(companyId)) throw new WrongCrmSecretKeyException();
            for (SyncIdForm row : request.getIdsSet()) {
                syncTaxId(row, companyId);
            }
            return 1;
        }catch (WrongCrmSecretKeyException e) {
            logger.error("WrongCrmSecretKeyException in method woo/v3/StoreTaxesRepository/syncTaxesIds. Key:"+request.getCrmSecretKey(), e);
            e.printStackTrace();
            return -200;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method woo/v3/StoreTaxesRepository/syncTaxesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Boolean syncTaxId(SyncIdForm ids, Long companyId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " update sprav_taxes " +
                            " set " +
                            " woo_id = " + ids.getId() +
                            " where " +
                            " company_id = " + companyId + " and " +
                            " id = " + ids.getCrm_id() + " and " +
                            ids.getCrm_id() + " in (select id from sprav_taxes where company_id = "+companyId+")";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method syncTaxId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private Boolean syncTax(TaxForm row, Long masterId, Long companyId) throws Exception {
        String stringQuery="";
        BigDecimal rate = new BigDecimal(row.getRate());
        BigDecimal multiplier = rate.divide(new BigDecimal(100),4,BigDecimal.ROUND_HALF_UP).add(new BigDecimal(1));
        try {
            stringQuery =
                    " insert into sprav_taxes (" +
                    "master_id, " +
                    "creator_id, " +
                    "company_id, " +
                    "date_time_created, " +
                    "name, " +
                    "is_active, " +
                    "value, " +
                    "multiplier, " +
                    "output_order," +
                    "is_deleted," +
                    "woo_id"+
                    ") values (" +
                    masterId + "," +
                    masterId + "," +
                    companyId + "," +
                    "now()," +
                    ":name," +
                    "true," +
                    row.getRate().replaceAll("[^0-9\\.]", "")+"," +
                    multiplier.toString() + "," +
                    " (select coalesce(max(output_order),0)+1 from sprav_taxes where company_id="+companyId+")," +
                    "false," +
                    row.getId() +
                    ") " +
                    " ON CONFLICT ON CONSTRAINT woo_id_uq " +// "upsert" by unique company_id and woo_id
                    " DO update set " +
                    " is_deleted = false," +
                    " name = :name," +
                    " value = "+row.getRate().replaceAll("[^0-9\\.]", "")+"," +
                    " multiplier = " + multiplier.toString();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",row.getName());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method syncTax. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private Boolean deleteTaxes(Set<Integer> ids, Long masterId, Long companyId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =
                    " update sprav_taxes " +
                    " set " +
                    " is_deleted = true" +
                    " where " +
                    " master_id = "+masterId+" and " +
                    " company_id = "+companyId;
            if(ids.size()>0)
                stringQuery = stringQuery + " and coalesce(woo_id, 0) not in ("+cu.SetOfIntToString(ids,",","","")+")";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteTaxes. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


}