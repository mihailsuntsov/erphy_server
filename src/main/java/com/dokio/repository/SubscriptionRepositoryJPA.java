package com.dokio.repository;


import com.dokio.message.request.PlanAdditionalOptionsForm;
import com.dokio.message.response.additional.MasterAccountInfoJSON;
import com.dokio.message.response.additional.PlanJSON;
import com.dokio.message.response.additional.UserResources;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository("SubscriptionRepositoryJPA")
public class SubscriptionRepositoryJPA {


    Logger logger = Logger.getLogger(SubscriptionRepositoryJPA.class);

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
//
//    @Autowired
//    private CommonUtilites commonUtilites;
//
//    @Autowired
//    private UserDetailsServiceImpl userDetailsService;

    @Transactional
    public void createMasterUserPlanOptions(Long userId){
        String stringQuery=
                "insert into plans_add_options (" +
                        "user_id, " +
                        "n_companies," +
                        "n_departments," +
                        "n_users," +
                        "n_products," +
                        "n_counterparties," +
                        "n_megabytes," +
                        "n_stores," +
                        "n_stores_woo," +
                        "companies_ppu," +
                        "departments_ppu," +
                        "users_ppu," +
                        "products_ppu," +
                        "counterparties_ppu," +
                        "megabytes_ppu," +
                        "stores_ppu," +
                        "stores_woo_ppu" +
                        ")" +
                        " values " +
                        "(" + userId + ", " +
                        "0," +
                        "0," +
                        "0," +
                        "0," +
                        "0," +
                        "0," +
                        "1," +
                        "1," +
                        "(select ppu from plans_add_options_prices where name='companies')," +
                        "(select ppu from plans_add_options_prices where name='departments')," +
                        "(select ppu from plans_add_options_prices where name='users')," +
                        "(select ppu from plans_add_options_prices where name='products')," +
                        "(select ppu from plans_add_options_prices where name='counterparties')," +
                        "(select ppu from plans_add_options_prices where name='megabytes')," +
                        "(select ppu from plans_add_options_prices where name='stores')," +
                        "(select ppu from plans_add_options_prices where name='stores_woo')" +
                        ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method createMasterUserPlanOptions. SQL = "+stringQuery, e);
        }
    }

    public MasterAccountInfoJSON getMasterAccountInfo(){
        Long masterId=userRepositoryJPA.getMyMasterId();
        MasterAccountInfoJSON accInfo = new MasterAccountInfoJSON();
        String suffix = userRepositoryJPA.getMySuffix();
        int planId = userRepositoryJPA.getMasterUserPlan(masterId);
        String limitColumnName="quantity_trial_limit";
        String stringQuery="";
        try{

            // get the info about tariff plan

            stringQuery = "select coalesce(sum(operation_sum),0) from _saas_billing_history u where master_account_id = "+masterId;
            Query query = entityManager.createNativeQuery(stringQuery);
            accInfo.setMoney(((BigDecimal) query.getSingleResult()).setScale(2, BigDecimal.ROUND_HALF_UP));
            stringQuery = "select coalesce(free_trial_days,0) from users u where id = "+masterId;
            query = entityManager.createNativeQuery(stringQuery);
            accInfo.setFree_trial_days(((Integer) query.getSingleResult()));

            //there is limits for trial period and for non-trial period.
            if(accInfo.getFree_trial_days()==0) limitColumnName = "quantity_limit";

            stringQuery = "select " +
                    " n_companies as n_companies, " +
                    " n_departments as n_departments, " +
                    " n_users as n_users, " +
                    " n_products as n_products, " +
                    " n_counterparties as n_counterparties, " +
                    " n_megabytes as n_megabytes, " +
                    " n_stores as n_stores, " +
                    " n_stores_woo as n_stores_woo, " +
                    " name_"+suffix+" as name, " +
                    " version as version, " +
                    " daily_price as daily_price," +
                    " is_nolimits as is_nolimits, "+
                    " is_free as is_free "+
                    " from plans u where id = "+planId;

            query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            accInfo.setPlan_id(planId);
            accInfo.setN_companies(Long.valueOf(queryList.get(0)[0].toString()));
            accInfo.setN_departments(Long.valueOf(queryList.get(0)[1].toString()));
            accInfo.setN_users(Long.valueOf(queryList.get(0)[2].toString()));
            accInfo.setN_products(Long.valueOf(queryList.get(0)[3].toString()));
            accInfo.setN_counterparties(Long.valueOf(queryList.get(0)[4].toString()));
            accInfo.setN_megabytes(Integer.valueOf(queryList.get(0)[5].toString()));
            accInfo.setN_stores(Long.valueOf(queryList.get(0)[6].toString()));
            accInfo.setN_stores_woo(Long.valueOf(queryList.get(0)[7].toString()));
            accInfo.setPlan_name((String) queryList.get(0)[8]);
            accInfo.setPlan_version((Integer) queryList.get(0)[9]);
            accInfo.setPlan_price((BigDecimal) queryList.get(0)[10]);
            accInfo.setPlan_no_limits((Boolean) queryList.get(0)[11]);
            accInfo.setPlan_free((Boolean) queryList.get(0)[12]);

            // get the info about an additional options

            stringQuery = "select " +
                    "n_companies as n_companies, " +
                    "n_departments as n_departments, " +
                    "n_users as n_users, " +
                    "n_products as n_products, " +
                    "n_counterparties as n_counterparties, " +
                    "n_megabytes as n_megabytes, " +
                    "n_stores as n_stores, " +
                    "n_stores_woo as n_stores_woo, " +
                    "companies_ppu as companies_ppu, " +
                    "departments_ppu as departments_ppu, " +
                    "users_ppu as users_ppu, " +
                    "products_ppu as products_ppu, " +
                    "counterparties_ppu as counterparties_ppu, " +
                    "megabytes_ppu as megabytes_ppu, " +
                    "stores_ppu as stores_ppu, " +
                    "stores_woo_ppu as stores_woo_ppu, " +

                    "(select step from plans_add_options_prices where name = 'companies') as companies_step," +
                    "(select step from plans_add_options_prices where name = 'departments') as departments_step," +
                    "(select step from plans_add_options_prices where name = 'users') as users_step," +
                    "(select step from plans_add_options_prices where name = 'products') as products_step," +
                    "(select step from plans_add_options_prices where name = 'counterparties') as counterparties_step," +
                    "(select step from plans_add_options_prices where name = 'megabytes') as megabytes_step," +
                    "(select step from plans_add_options_prices where name = 'stores') as stores_step," +
                    "(select step from plans_add_options_prices where name = 'stores_woo') as stores_woo_step," +



                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'companies') as companies_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'departments') as departments_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'users') as users_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'products') as products_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'counterparties') as counterparties_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'megabytes') as megabytes_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'stores') as stores_quantity_limit," +
                    "(select "+limitColumnName+" from plans_add_options_prices where name = 'stores_woo') as stores_woo_quantity_limit" +

                    " from plans_add_options u where user_id = "+masterId;

            query = entityManager.createNativeQuery(stringQuery);
            queryList = query.getResultList();

            accInfo.setN_companies_add(Long.valueOf(queryList.get(0)[0].toString()));
            accInfo.setN_departments_add(Long.valueOf(queryList.get(0)[1].toString()));
            accInfo.setN_users_add(Long.valueOf(queryList.get(0)[2].toString()));
            accInfo.setN_products_add(Long.valueOf(queryList.get(0)[3].toString()));
            accInfo.setN_counterparties_add(Long.valueOf(queryList.get(0)[4].toString()));
            accInfo.setN_megabytes_add(Integer.valueOf(queryList.get(0)[5].toString()));
            accInfo.setN_stores_add(Long.valueOf(queryList.get(0)[6].toString()));
            accInfo.setN_stores_woo_add(Long.valueOf(queryList.get(0)[7].toString()));

            accInfo.setCompanies_ppu((BigDecimal)               queryList.get(0)[8]);
            accInfo.setDepartments_ppu((BigDecimal)             queryList.get(0)[9]);
            accInfo.setUsers_ppu((BigDecimal)                   queryList.get(0)[10]);
            accInfo.setProducts_ppu((BigDecimal)                queryList.get(0)[11]);
            accInfo.setCounterparties_ppu((BigDecimal)          queryList.get(0)[12]);
            accInfo.setMegabytes_ppu((BigDecimal)               queryList.get(0)[13]);
            accInfo.setStores_ppu((BigDecimal)                  queryList.get(0)[14]);
            accInfo.setStores_woo_ppu((BigDecimal)              queryList.get(0)[15]);

            accInfo.setStep_companies((Integer)                 queryList.get(0)[16]);
            accInfo.setStep_departments((Integer)               queryList.get(0)[17]);
            accInfo.setStep_users((Integer)                     queryList.get(0)[18]);
            accInfo.setStep_products((Integer)                  queryList.get(0)[19]);
            accInfo.setStep_counterparties((Integer)            queryList.get(0)[20]);
            accInfo.setStep_megabytes((Integer)                 queryList.get(0)[21]);
            accInfo.setStep_stores((Integer)                    queryList.get(0)[22]);
            accInfo.setStep_stores_woo((Integer)                queryList.get(0)[23]);

            accInfo.setQuantity_limit_companies((Integer)       queryList.get(0)[24]);
            accInfo.setQuantity_limit_departments((Integer)     queryList.get(0)[25]);
            accInfo.setQuantity_limit_users((Integer)           queryList.get(0)[26]);
            accInfo.setQuantity_limit_products((Integer)        queryList.get(0)[27]);
            accInfo.setQuantity_limit_counterparties((Integer)  queryList.get(0)[28]);
            accInfo.setQuantity_limit_megabytes((Integer)       queryList.get(0)[29]);
            accInfo.setQuantity_limit_stores((Integer)          queryList.get(0)[30]);
            accInfo.setQuantity_limit_stores_woo((Integer)      queryList.get(0)[31]);



            // get the info about consumed resources
            UserResources userResources = userRepositoryJPA.getMyConsumedResources();

            accInfo.setN_companies_fact(userResources.getCompanies());
            accInfo.setN_departments_fact(userResources.getDepartments());
            accInfo.setN_users_fact(userResources.getUsers());
            accInfo.setN_products_fact(userResources.getProducts());
            accInfo.setN_counterparties_fact(userResources.getCounterparties());
            accInfo.setN_megabytes_fact(userResources.getMegabytes());
            accInfo.setN_stores_fact(userResources.getStores());
            accInfo.setN_stores_woo_fact(userResources.getStores_woo());

            return accInfo;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMasterAccountInfo. SQL = "+stringQuery, e);
            return null;
        }
    }

    @Transactional
    public Integer updateAddOptions(PlanAdditionalOptionsForm options){

        Long masterId=userRepositoryJPA.getMyMasterId();
        int planId = userRepositoryJPA.getMasterUserPlan(masterId);
        //Если есть право на "Редактирование"
        if(securityRepositoryJPA.userHasPermissions_OR(55L,"682")) {

            String stringQuery = " update plans_add_options set " +
                    " n_companies        =" + options.getN_companies_add()      + ", " +
                    " n_departments      =" + options.getN_departments_add()    + ", " +
                    " n_users            =" + options.getN_users_add()          + ", " +
                    " n_products         =" + options.getN_products_add()       + ", " +
                    " n_counterparties   =" + options.getN_counterparties_add() + ", " +
                    " n_megabytes        =" + options.getN_megabytes_add()      + ", " +
                    " n_stores           =" + options.getN_stores_add()         + ", " +
                    " n_stores_woo       =" + options.getN_stores_woo_add()     +
                    " where user_id      =" + masterId                          + "; ";
            if (options.getPlan_id() != planId)
                stringQuery = stringQuery + " update users set plan_id = " + options.getPlan_id() + " where id = " + masterId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method updateAddOptions. SQL = "+stringQuery, e);
                return null;
            }
            return 1;
        } else return -1;// no permissions
    }

    public List<PlanJSON> getPlansList(){
        String suffix = userRepositoryJPA.getMySuffix();
        String stringQuery = "select " +
                " n_companies as n_companies, " +
                " n_departments as n_departments, " +
                " n_users as n_users, " +
                " n_products as n_products, " +
                " n_counterparties as n_counterparties, " +
                " n_megabytes as n_megabytes, " +
                " n_stores as n_stores, " +
                " n_stores_woo as n_stores_woo, " +
                " name_"+suffix+" as name, " +
                " version as version, " +
                " daily_price as daily_price," +
                " is_nolimits as is_nolimits, "+
                " is_free as is_free, "+
                " id as id " +
                " from plans " +
                " where " +
                " is_available_for_user_switching = true and " +
                " is_archive = false ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<PlanJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                PlanJSON doc = new PlanJSON();
                doc.setN_companies(Long.valueOf(obj[0].toString()));
                doc.setN_departments(Long.valueOf(obj[1].toString()));
                doc.setN_users(Long.valueOf(obj[2].toString()));
                doc.setN_products(Long.valueOf(obj[3].toString()));
                doc.setN_counterparties(Long.valueOf(obj[4].toString()));
                doc.setN_megabytes(Integer.valueOf(obj[5].toString()));
                doc.setN_stores(Long.valueOf(obj[6].toString()));
                doc.setN_stores_woo(Long.valueOf(obj[7].toString()));
                doc.setName((String) obj[8]);
                doc.setVersion((Integer) obj[9]);
                doc.setDaily_price((BigDecimal) obj[10]);
                doc.setIs_nolimits((Boolean) obj[11]);
                doc.setIs_free((Boolean) obj[12]);
                doc.setId((Integer) obj[13]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getPlansList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @Transactional
    public Integer stopTrialPeriod(){
        Long masterId=userRepositoryJPA.getMyMasterId();
        //Если есть право на "Редактирование"
        if(securityRepositoryJPA.userHasPermissions_OR(55L,"682")) {
            String stringQuery = " update users set free_trial_days = 0 where id =" + masterId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method stopTrialPeriod. SQL = "+stringQuery, e);
                return null;
            }
            return 1;
        } else return -1;// no permissions
    }


}
