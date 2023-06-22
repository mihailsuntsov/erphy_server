package com.dokio.repository.store.woo.v3;

import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.message.response.additional.UserResources;
import com.dokio.message.response.store.woo.v3.IsLetSyncJSON;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class StoreGeneralRepository {
    private Logger logger = Logger.getLogger(StoreGeneralRepository.class);

    @Value("${apiserver.host}")
    private String apiserver;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CommonUtilites cu;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    private UserRepositoryJPA userRepository;

    public IsLetSyncJSON isLetSync(String pluginVersion, String crmSecretKey) {
        String stringQuery="";
        try {
        boolean is_let_sync      = (Boolean)cu.getByCrmSecretKey("is_let_sync",crmSecretKey);
        boolean is_store_deleted = (Boolean)cu.getByCrmSecretKey("is_deleted", crmSecretKey);

            IsLetSyncJSON isLetSyncJSON = new IsLetSyncJSON();
            SettingsGeneralJSON settingsGeneral = cu.getSettingsGeneral();

            if(!settingsGeneral.isLet_woo_plugin_to_sync()){
                isLetSyncJSON.setIs_sync_allowed(false);
                isLetSyncJSON.setReason("Global server restriction on WooCommerce synchronizations.");
                return isLetSyncJSON;
            }
            if(is_store_deleted){
                isLetSyncJSON.setIs_sync_allowed(false);
                isLetSyncJSON.setReason("The store is deleted");
                return isLetSyncJSON;
            }
            if(!is_let_sync){
                isLetSyncJSON.setIs_sync_allowed(false);
                isLetSyncJSON.setReason("In a store connection settings the \"Connection allowed\" setting is switched into \"OFF\" mode");
                return isLetSyncJSON;
            }
            if(!versionsCompare(settingsGeneral.getWoo_plugin_oldest_acceptable_ver(), pluginVersion)){
                isLetSyncJSON.setIs_sync_allowed(false);
                isLetSyncJSON.setReason("Plugin version is too old. Plugin version is "+pluginVersion+", but oldest allowed version is " + settingsGeneral.getWoo_plugin_oldest_acceptable_ver());
                return isLetSyncJSON;
            }

            if(settingsGeneral.isSaas()){
                Long masterId = Long.valueOf(cu.getByCrmSecretKey("master_id",crmSecretKey).toString());
                int planId = (Integer)cu.getFieldValueFromTableById("users","plan_id",masterId, masterId);
                boolean isPlanNoLimits = userRepository.isPlanNoLimits(planId);
                if(!isPlanNoLimits){
                    UserResources consumedRes = userRepository.getMyConsumedResources();
                    UserResources maxAllowed = userRepository.getMyMaxAllowedResources();

                    if(consumedRes.getStores() > maxAllowed.getStores()){
                        isLetSyncJSON.setIs_sync_allowed(false);
                        isLetSyncJSON.setReason("Consumed quantity of online store connections more than allowed quantity. Consumed is "+consumedRes.getStores()+", allowed is " + maxAllowed.getStores());
                        return isLetSyncJSON;
                    }
                }
            }

            isLetSyncJSON.setIs_sync_allowed(true);
            isLetSyncJSON.setReason("Synchronization allowed");
            return isLetSyncJSON;
        }
        catch (Exception e) {
            logger.error("Exception in method StoreGeneralRepository/isLetSync. SQL query:"+stringQuery, e);
            e.printStackTrace();
            IsLetSyncJSON isLetSyncJSON = new IsLetSyncJSON();
            isLetSyncJSON.setIs_sync_allowed(false);
            isLetSyncJSON.setReason("Exception in method StoreGeneralRepository/isLetSync. Exception message: "+e.getMessage());
            return isLetSyncJSON;
        }
    }

    //true if current version suitable for limits of oldest allowed version ( equivalent or lalater)
    //    old   1.2.6                              1.4.0                    1.4.4
    //    cur   1.3.0 - true (1!>1, 2!>3, )        1.3.5 - false            1.4.5
    private boolean versionsCompare(String oldestAllowedVer, String curVer) throws Exception {
        try {
            //As curVer can come with subversion like 1.3.5-5 here need to clear it and get only 1.3.5
            curVer = curVer.split("-")[0];

            List<Integer> oldestAllowedVerList =
                    Arrays.stream(oldestAllowedVer.split("[.]")).map(Integer::parseInt).collect(Collectors.toList());
            List<Integer> curVerList =
                    Arrays.stream(curVer.split("[.]")).map(Integer::parseInt).collect(Collectors.toList());

            if (oldestAllowedVerList.get(0) > curVerList.get(0))
                return false;
            if (oldestAllowedVerList.get(0).equals(curVerList.get(0)) && oldestAllowedVerList.get(1) > curVerList.get(1))
                return false;
            if (oldestAllowedVerList.get(0).equals(curVerList.get(0)) && oldestAllowedVerList.get(1).equals(curVerList.get(1)) && oldestAllowedVerList.get(2) > curVerList.get(2))
                return false;
            return true;
        } catch (Exception e) {
            logger.error("Exception in method StoreGeneralRepository/versionsCompare. oldestAllowedVer: "+oldestAllowedVer+", curVer: "+curVer, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

}
