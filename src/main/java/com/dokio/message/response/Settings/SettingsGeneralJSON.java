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
package com.dokio.message.response.Settings;

import java.math.BigDecimal;

public class SettingsGeneralJSON {

    private boolean showRegistrationLink;
    private boolean allowRegistration;
    private boolean showForgotLink;
    private boolean allowRecoverPassword;
    private String  backendVersion;
    private String  backendVersionDate;
    private String  databaseVersion;
    private String  databaseVersionDate;
    private String  showInSignin;
    private int     planDefaultId;
    private int     freeTrialDays;
    private BigDecimal planPrice;
    private boolean isSaas;                 // id DokioCRM using as a SaaS
    private boolean let_woo_plugin_to_sync;
    private String  woo_plugin_oldest_acceptable_ver; // most old supported by DokioCRM plugin version
    private boolean is_sites_distribution; //in this SaaS there is a sites distribution
    private String  stores_alert_email;    // email of rent-stores responsible employee.
    private int     min_qtt_stores_alert;  //quantity of stores to sent email to stores_alert_email
    private int     max_store_orders_per_24h_1_account; // max quantity of online stores that can be ordered in 24h from one account
    private int     max_store_orders_per_24h_1_ip;      // max quantity of online stores that can be ordered in 24h from one IP address
    private String  saas_payment_currency; // currency of SaaS subscription accounting
    private boolean create_support_user; // create or not a support user account

    // billing settings
    private Long    billing_master_id;
    private Long    billing_shipment_creator_id;
    private Long    billing_shipment_company_id;
    private Long    billing_shipment_department_id;
    private Long    billing_cagents_category_id;
    private Long    billing_companies_product_id;
    private Long    billing_departments_product_id;
    private Long    billing_users_product_id;
    private Long    billing_products_product_id;
    private Long    billing_counterparties_product_id;
    private Long    billing_megabytes_product_id;
    private Long    billing_stores_product_id;
    private Long    billing_stores_woo_product_id;
    private Long    billing_plan_product_id;

    // URLs of Legal information
    private String  url_terms_and_conditions;
    private String  url_privacy_policy;
    private String  url_data_processing_agreement;
    private String  root_domain;                   // like dokio.me

    public boolean isCreate_support_user() {
        return create_support_user;
    }

    public void setCreate_support_user(boolean create_support_user) {
        this.create_support_user = create_support_user;
    }

    public String getRoot_domain() {
        return root_domain;
    }

    public void setRoot_domain(String root_domain) {
        this.root_domain = root_domain;
    }

    public Long getBilling_master_id() {
        return billing_master_id;
    }

    public void setBilling_master_id(Long billing_master_id) {
        this.billing_master_id = billing_master_id;
    }

    public Long getBilling_shipment_creator_id() {
        return billing_shipment_creator_id;
    }

    public void setBilling_shipment_creator_id(Long billing_shipment_creator_id) {
        this.billing_shipment_creator_id = billing_shipment_creator_id;
    }

    public Long getBilling_shipment_company_id() {
        return billing_shipment_company_id;
    }

    public void setBilling_shipment_company_id(Long billing_shipment_company_id) {
        this.billing_shipment_company_id = billing_shipment_company_id;
    }

    public Long getBilling_shipment_department_id() {
        return billing_shipment_department_id;
    }

    public void setBilling_shipment_department_id(Long billing_shipment_department_id) {
        this.billing_shipment_department_id = billing_shipment_department_id;
    }

    public Long getBilling_cagents_category_id() {
        return billing_cagents_category_id;
    }

    public void setBilling_cagents_category_id(Long billing_cagents_category_id) {
        this.billing_cagents_category_id = billing_cagents_category_id;
    }

    public Long getBilling_companies_product_id() {
        return billing_companies_product_id;
    }

    public void setBilling_companies_product_id(Long billing_companies_product_id) {
        this.billing_companies_product_id = billing_companies_product_id;
    }

    public Long getBilling_departments_product_id() {
        return billing_departments_product_id;
    }

    public void setBilling_departments_product_id(Long billing_departments_product_id) {
        this.billing_departments_product_id = billing_departments_product_id;
    }

    public Long getBilling_users_product_id() {
        return billing_users_product_id;
    }

    public void setBilling_users_product_id(Long billing_users_product_id) {
        this.billing_users_product_id = billing_users_product_id;
    }

    public Long getBilling_products_product_id() {
        return billing_products_product_id;
    }

    public void setBilling_products_product_id(Long billing_products_product_id) {
        this.billing_products_product_id = billing_products_product_id;
    }

    public Long getBilling_counterparties_product_id() {
        return billing_counterparties_product_id;
    }

    public void setBilling_counterparties_product_id(Long billing_counterparties_product_id) {
        this.billing_counterparties_product_id = billing_counterparties_product_id;
    }

    public Long getBilling_megabytes_product_id() {
        return billing_megabytes_product_id;
    }

    public void setBilling_megabytes_product_id(Long billing_megabytes_product_id) {
        this.billing_megabytes_product_id = billing_megabytes_product_id;
    }

    public Long getBilling_stores_product_id() {
        return billing_stores_product_id;
    }

    public void setBilling_stores_product_id(Long billing_stores_product_id) {
        this.billing_stores_product_id = billing_stores_product_id;
    }

    public Long getBilling_stores_woo_product_id() {
        return billing_stores_woo_product_id;
    }

    public void setBilling_stores_woo_product_id(Long billing_stores_woo_product_id) {
        this.billing_stores_woo_product_id = billing_stores_woo_product_id;
    }

    public Long getBilling_plan_product_id() {
        return billing_plan_product_id;
    }

    public void setBilling_plan_product_id(Long billing_plan_product_id) {
        this.billing_plan_product_id = billing_plan_product_id;
    }

    public String getUrl_terms_and_conditions() {
        return url_terms_and_conditions;
    }

    public void setUrl_terms_and_conditions(String url_terms_and_conditions) {
        this.url_terms_and_conditions = url_terms_and_conditions;
    }

    public String getUrl_privacy_policy() {
        return url_privacy_policy;
    }

    public void setUrl_privacy_policy(String url_privacy_policy) {
        this.url_privacy_policy = url_privacy_policy;
    }

    public String getUrl_data_processing_agreement() {
        return url_data_processing_agreement;
    }

    public void setUrl_data_processing_agreement(String url_data_processing_agreement) {
        this.url_data_processing_agreement = url_data_processing_agreement;
    }

    public String getSaas_payment_currency() {
        return saas_payment_currency;
    }

    public void setSaas_payment_currency(String saas_payment_currency) {
        this.saas_payment_currency = saas_payment_currency;
    }

    public int getMax_store_orders_per_24h_1_account() {
        return max_store_orders_per_24h_1_account;
    }

    public void setMax_store_orders_per_24h_1_account(int max_store_orders_per_24h_1_account) {
        this.max_store_orders_per_24h_1_account = max_store_orders_per_24h_1_account;
    }

    public int getMax_store_orders_per_24h_1_ip() {
        return max_store_orders_per_24h_1_ip;
    }

    public void setMax_store_orders_per_24h_1_ip(int max_store_orders_per_24h_1_ip) {
        this.max_store_orders_per_24h_1_ip = max_store_orders_per_24h_1_ip;
    }

    public boolean isIs_sites_distribution() {
        return is_sites_distribution;
    }

    public void setIs_sites_distribution(boolean is_sites_distribution) {
        this.is_sites_distribution = is_sites_distribution;
    }

    public String getStores_alert_email() {
        return stores_alert_email;
    }

    public void setStores_alert_email(String stores_alert_email) {
        this.stores_alert_email = stores_alert_email;
    }

    public int getMin_qtt_stores_alert() {
        return min_qtt_stores_alert;
    }

    public void setMin_qtt_stores_alert(int min_qtt_stores_alert) {
        this.min_qtt_stores_alert = min_qtt_stores_alert;
    }

    public boolean isLet_woo_plugin_to_sync() {
        return let_woo_plugin_to_sync;
    }

    public void setLet_woo_plugin_to_sync(boolean let_woo_plugin_to_sync) {
        this.let_woo_plugin_to_sync = let_woo_plugin_to_sync;
    }

    public String getWoo_plugin_oldest_acceptable_ver() {
        return woo_plugin_oldest_acceptable_ver;
    }

    public void setWoo_plugin_oldest_acceptable_ver(String woo_plugin_oldest_acceptable_ver) {
        this.woo_plugin_oldest_acceptable_ver = woo_plugin_oldest_acceptable_ver;
    }

    public boolean isSaas() {
        return isSaas;
    }

    public void setSaas(boolean saas) {
        isSaas = saas;
    }

    public BigDecimal getPlanPrice() {
        return planPrice;
    }

    public void setPlanPrice(BigDecimal planPrice) {
        this.planPrice = planPrice;
    }

    public int getFreeTrialDays() {
        return freeTrialDays;
    }

    public void setFreeTrialDays(int freeTrialDays) {
        this.freeTrialDays = freeTrialDays;
    }

    public int getPlanDefaultId() {
        return planDefaultId;
    }

    public void setPlanDefaultId(int planDefaultId) {
        this.planDefaultId = planDefaultId;
    }

    public String getShowInSignin() {
        return showInSignin;
    }

    public void setShowInSignin(String showInSignin) {
        this.showInSignin = showInSignin;
    }

    public boolean isShowRegistrationLink() {
        return showRegistrationLink;
    }

    public void setShowRegistrationLink(boolean showRegistrationLink) {
        this.showRegistrationLink = showRegistrationLink;
    }

    public boolean isAllowRegistration() {
        return allowRegistration;
    }

    public void setAllowRegistration(boolean allowRegistration) {
        this.allowRegistration = allowRegistration;
    }

    public boolean isShowForgotLink() {
        return showForgotLink;
    }

    public void setShowForgotLink(boolean showForgotLink) {
        this.showForgotLink = showForgotLink;
    }

    public boolean isAllowRecoverPassword() {
        return allowRecoverPassword;
    }

    public void setAllowRecoverPassword(boolean allowRecoverPassword) {
        this.allowRecoverPassword = allowRecoverPassword;
    }

    public String getBackendVersion() {
        return backendVersion;
    }

    public void setBackendVersion(String backendVersion) {
        this.backendVersion = backendVersion;
    }

    public String getBackendVersionDate() {
        return backendVersionDate;
    }

    public void setBackendVersionDate(String backendVersionDate) {
        this.backendVersionDate = backendVersionDate;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getDatabaseVersionDate() {
        return databaseVersionDate;
    }

    public void setDatabaseVersionDate(String databaseVersionDate) {
        this.databaseVersionDate = databaseVersionDate;
    }
}
