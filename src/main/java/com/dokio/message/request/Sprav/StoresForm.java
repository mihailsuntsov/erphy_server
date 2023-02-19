package com.dokio.message.request.Sprav;

import java.util.List;

public class StoresForm {

    private Long    id;
    private Long    company_id;
    private String  name;
    private boolean is_deleted;
    private String  lang_code;                   // e.g. EN
    private String  store_type;                  // e.g. woo
    private String  store_api_version;           // e.g. v3
    private String  crm_secret_key;              // like UUID generated
    private Long    store_price_type_regular;    // id of regular type price
    private Long    store_price_type_sale;       // id of sale type price
    private Long    store_orders_department_id;  // department for creation Customer order from store
    private String  store_if_customer_not_found; // "create_new" or "use_default". Default is "create_new"
    private Long    store_default_customer_id;   // counterparty id if store_if_customer_not_found=use_default
    private Long    store_default_creator_id;    // default user that will be marked as a creator of store order. Default is master user
    private Integer store_days_for_esd;          // number of days for ESD of created store order. Default is 0
    private List<Long> storeDepartments;         // IDs of the departments in which calculated the amount of products for the online store
    private Boolean store_auto_reserve;          // auto reserve product after getting internet store order
    private String  store_ip;                    // internet-store ip address
//
//    private Boolean store_auto_reserve;
//    private int store_days_for_esd;
//    private Long store_default_creator_id;
//    private Long store_default_customer_id;
//    private String store_if_customer_not_found varchar(11);
//    private Long store_orders_department_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getLang_code() {
        return lang_code;
    }

    public void setLang_code(String lang_code) {
        this.lang_code = lang_code;
    }

    public String getStore_type() {
        return store_type;
    }

    public void setStore_type(String store_type) {
        this.store_type = store_type;
    }

    public String getStore_api_version() {
        return store_api_version;
    }

    public void setStore_api_version(String store_api_version) {
        this.store_api_version = store_api_version;
    }

    public String getCrm_secret_key() {
        return crm_secret_key;
    }

    public void setCrm_secret_key(String crm_secret_key) {
        this.crm_secret_key = crm_secret_key;
    }

    public Long getStore_price_type_regular() {
        return store_price_type_regular;
    }

    public void setStore_price_type_regular(Long store_price_type_regular) {
        this.store_price_type_regular = store_price_type_regular;
    }

    public Long getStore_price_type_sale() {
        return store_price_type_sale;
    }

    public void setStore_price_type_sale(Long store_price_type_sale) {
        this.store_price_type_sale = store_price_type_sale;
    }

    public Long getStore_orders_department_id() {
        return store_orders_department_id;
    }

    public void setStore_orders_department_id(Long store_orders_department_id) {
        this.store_orders_department_id = store_orders_department_id;
    }

    public String getStore_if_customer_not_found() {
        return store_if_customer_not_found;
    }

    public void setStore_if_customer_not_found(String store_if_customer_not_found) {
        this.store_if_customer_not_found = store_if_customer_not_found;
    }

    public Long getStore_default_customer_id() {
        return store_default_customer_id;
    }

    public void setStore_default_customer_id(Long store_default_customer_id) {
        this.store_default_customer_id = store_default_customer_id;
    }

    public Long getStore_default_creator_id() {
        return store_default_creator_id;
    }

    public void setStore_default_creator_id(Long store_default_creator_id) {
        this.store_default_creator_id = store_default_creator_id;
    }

    public Integer getStore_days_for_esd() {
        return store_days_for_esd;
    }

    public void setStore_days_for_esd(Integer store_days_for_esd) {
        this.store_days_for_esd = store_days_for_esd;
    }

    public List<Long> getStoreDepartments() {
        return storeDepartments;
    }

    public void setStoreDepartments(List<Long> storeDepartments) {
        this.storeDepartments = storeDepartments;
    }

    public Boolean getStore_auto_reserve() {
        return store_auto_reserve;
    }

    public void setStore_auto_reserve(Boolean store_auto_reserve) {
        this.store_auto_reserve = store_auto_reserve;
    }

    public String getStore_ip() {
        return store_ip;
    }

    public void setStore_ip(String store_ip) {
        this.store_ip = store_ip;
    }
}
