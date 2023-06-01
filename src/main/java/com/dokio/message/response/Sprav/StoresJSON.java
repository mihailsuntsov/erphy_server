package com.dokio.message.response.Sprav;

import java.util.List;

public class StoresJSON {
    private Long        id;
    private Long        master_id;
    private Long        creator_id;
    private Long        changer_id;
    private Long        company_id;
    private String      master;
    private String      company;
    private String      creator;
    private String      changer;
    private String      name;
    private boolean     is_deleted;
    private String      date_time_created;
    private String      date_time_changed;
    private String      lang_code;                   // e.g. EN
    private String      store_type;                  // e.g. woo
    private String      store_api_version;           // e.g. v3
    private String      crm_secret_key;              // like UUID generated
    private Long        store_price_type_regular;    // id of regular type price
    private Long        store_price_type_sale;       // id of sale type price
    private Long        store_orders_department_id;  // department for creation Customer order from store
    private String      store_if_customer_not_found; // "create_new" or "use_default". Default is "create_new"
    private Long        store_default_customer_id;   // counterparty id if store_if_customer_not_found=use_default
    private Long        store_default_creator_id;    // default user that will be marked as a creator of store order. Default is master user
    private Integer     store_days_for_esd;          // number of days for ESD of created store order. Default is 0
    private List<Long>  storeDepartments;            // IDs of the departments in which calculated the amount of products for the online store
    private Boolean     store_auto_reserve;          // auto reserve product after getting internet store order
    private String      store_ip;                    // internet-store ip address
    private String      store_default_creator;       // Name of default user that will be marked as a creator of store order.
    private String      store_default_customer;      // the name of default customer
    private Boolean     is_let_sync;                 // synchronization is allowed
    private Boolean     is_saas;                     // is this SaaS? (getting from settings_general)
    private Boolean     is_sites_distribution;       // is there sites (stores) distribution in this SaaS? (getting from settings_general)
    private Boolean     can_order_store;             // is there possibility to order a store in current store connection? (there is no previously ordered stores or all these stores are deleted)
//    private String      store_ordered_user;          // who is clicked on the button "Order store"
//    private Boolean     store_distributed;           // the ordered store was successfully distributed
//    private String      date_time_store_ordered;     // date-time of store ordered
//    private String      date_time_store_distributed; // date-time of store distributed

    public Boolean getIs_saas() {
        return is_saas;
    }

    public void setIs_saas(Boolean is_saas) {
        this.is_saas = is_saas;
    }

    public Boolean getIs_sites_distribution() {
        return is_sites_distribution;
    }

    public void setIs_sites_distribution(Boolean is_sites_distribution) {
        this.is_sites_distribution = is_sites_distribution;
    }

    public Boolean getCan_order_store() {
        return can_order_store;
    }

    public void setCan_order_store(Boolean can_order_store) {
        this.can_order_store = can_order_store;
    }

    public Boolean      getIs_let_sync() {
        return is_let_sync;
    }

    public void         setIs_let_sync(Boolean is_let_sync) {
        this.is_let_sync = is_let_sync;
    }

    public String getStore_default_creator() {
        return store_default_creator;
    }

    public void setStore_default_creator(String store_default_creator) {
        this.store_default_creator = store_default_creator;
    }

    public String getStore_default_customer() {
        return store_default_customer;
    }

    public void setStore_default_customer(String store_default_customer) {
        this.store_default_customer = store_default_customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Long changer_id) {
        this.changer_id = changer_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
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

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
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
