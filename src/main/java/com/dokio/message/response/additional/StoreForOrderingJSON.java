package com.dokio.message.response.additional;

public class StoreForOrderingJSON {

    private Long id;

    private String date_time_created;
    private boolean ready_to_distribute;
    private boolean distributed;
    private boolean is_queried_to_delete;   //user sent query to delete
    private boolean is_deleted;             //site was physically deleted from server

    private String panel_domain;
    private String client_no;
    private String client_name;
    private String client_login;
    private String client_password;
    private String site_domain;
    private String site_root;
    private String ftp_user;
    private String ftp_password;
    private String db_user;
    private String db_password;
    private String db_name;
    private String wp_login;
    private String wp_password;
    private String wp_server_ip;
    private String dokio_secret_key;
    private String record_creator_name;         // name of employee who created this record

    private String date_time_ordered;           //when user sent query to get site
    private String date_time_distributed;       //when user got the site
    private String date_time_query_to_delete;   //-- when user sent query to delete - store mark as "is_queried_to_delete=true"
    private String date_time_deleted;           //-- when site was physically deleted from server
    private Long master_id;
    private Long company_id;
    private Long store_id;                        // online store connection
    private Long orderer_id;                      // who ordered (who is clicked on the button "Order store")
    private Long deleter_id;                      // who deleted (who is clicked on the button "Delete store")
    private String orderer_ip;                    // ip address from which store ordered


    public String getOrderer_ip() {
        return orderer_ip;
    }

    public void setOrderer_ip(String orderer_ip) {
        this.orderer_ip = orderer_ip;
    }

    public boolean isIs_queried_to_delete() {
        return is_queried_to_delete;
    }

    public void setIs_queried_to_delete(boolean is_queried_to_delete) {
        this.is_queried_to_delete = is_queried_to_delete;
    }

    public String getDate_time_query_to_delete() {
        return date_time_query_to_delete;
    }

    public void setDate_time_query_to_delete(String date_time_query_to_delete) {
        this.date_time_query_to_delete = date_time_query_to_delete;
    }

    public String getPanel_domain() {
        return panel_domain;
    }

    public void setPanel_domain(String panel_domain) {
        this.panel_domain = panel_domain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public boolean isReady_to_distribute() {
        return ready_to_distribute;
    }

    public void setReady_to_distribute(boolean ready_to_distribute) {
        this.ready_to_distribute = ready_to_distribute;
    }

    public boolean isDistributed() {
        return distributed;
    }

    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }

    public boolean isIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getClient_no() {
        return client_no;
    }

    public void setClient_no(String client_no) {
        this.client_no = client_no;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_login() {
        return client_login;
    }

    public void setClient_login(String client_login) {
        this.client_login = client_login;
    }

    public String getClient_password() {
        return client_password;
    }

    public void setClient_password(String client_password) {
        this.client_password = client_password;
    }

    public String getSite_domain() {
        return site_domain;
    }

    public void setSite_domain(String site_domain) {
        this.site_domain = site_domain;
    }

    public String getSite_root() {
        return site_root;
    }

    public void setSite_root(String site_root) {
        this.site_root = site_root;
    }

    public String getFtp_user() {
        return ftp_user;
    }

    public void setFtp_user(String ftp_user) {
        this.ftp_user = ftp_user;
    }

    public String getFtp_password() {
        return ftp_password;
    }

    public void setFtp_password(String ftp_password) {
        this.ftp_password = ftp_password;
    }

    public String getDb_user() {
        return db_user;
    }

    public void setDb_user(String db_user) {
        this.db_user = db_user;
    }

    public String getDb_password() {
        return db_password;
    }

    public void setDb_password(String db_password) {
        this.db_password = db_password;
    }

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

    public String getWp_login() {
        return wp_login;
    }

    public void setWp_login(String wp_login) {
        this.wp_login = wp_login;
    }

    public String getWp_password() {
        return wp_password;
    }

    public void setWp_password(String wp_password) {
        this.wp_password = wp_password;
    }

    public String getWp_server_ip() {
        return wp_server_ip;
    }

    public void setWp_server_ip(String wp_server_ip) {
        this.wp_server_ip = wp_server_ip;
    }

    public String getDokio_secret_key() {
        return dokio_secret_key;
    }

    public void setDokio_secret_key(String dokio_secret_key) {
        this.dokio_secret_key = dokio_secret_key;
    }

    public String getRecord_creator_name() {
        return record_creator_name;
    }

    public void setRecord_creator_name(String record_creator_name) {
        this.record_creator_name = record_creator_name;
    }

    public String getDate_time_ordered() {
        return date_time_ordered;
    }

    public void setDate_time_ordered(String date_time_ordered) {
        this.date_time_ordered = date_time_ordered;
    }

    public String getDate_time_distributed() {
        return date_time_distributed;
    }

    public void setDate_time_distributed(String date_time_distributed) {
        this.date_time_distributed = date_time_distributed;
    }

    public String getDate_time_deleted() {
        return date_time_deleted;
    }

    public void setDate_time_deleted(String date_time_deleted) {
        this.date_time_deleted = date_time_deleted;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getStore_id() {
        return store_id;
    }

    public void setStore_id(Long store_id) {
        this.store_id = store_id;
    }

    public Long getOrderer_id() {
        return orderer_id;
    }

    public void setOrderer_id(Long orderer_id) {
        this.orderer_id = orderer_id;
    }

    public Long getDeleter_id() {
        return deleter_id;
    }

    public void setDeleter_id(Long deleter_id) {
        this.deleter_id = deleter_id;
    }

    @Override
    public String toString() {
        return "StoreForOrderingJSON{" +
                "id=" + id +
                ", date_time_created='" + date_time_created + '\'' +
                ", client_no='" + client_no + '\'' +
                ", client_name='" + client_name + '\'' +
                ", site_domain='" + site_domain + '\'' +
                ", site_root='" + site_root + '\'' +
                ", wp_server_ip='" + wp_server_ip + '\'' +
                ", record_creator_name='" + record_creator_name + '\'' +
                ", date_time_ordered='" + date_time_ordered + '\'' +
                ", date_time_distributed='" + date_time_distributed + '\'' +
                ", master_id=" + master_id +
                ", company_id=" + company_id +
                ", store_id=" + store_id +
                ", orderer_id=" + orderer_id +
                ", orderer_ip='" + orderer_ip + '\'' +
                '}';
    }
}
