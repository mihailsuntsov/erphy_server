package com.dokio.message.response.additional;

public class StoreForOrderingShortInfoJSON {

    private Long id;

    private String date_time_created;
    private String date_time_ordered;           //when user sent query to get site
    private String date_time_distributed;       //when user got the site
    private String date_time_query_to_delete;   //-- when user sent query to delete - store mark as "is_queried_to_delete=true"
    private String date_time_deleted;           //-- when site was physically deleted from server
    private boolean distributed;
    private boolean ready_to_distribute;
    private boolean is_queried_to_delete;   //user sent query to delete
    private boolean is_deleted;             //site was physically deleted from server
    private String panel_domain;
    private String site_domain;
    private String site_url;
    private String record_creator_name;         // name of employee who created this record
    private String orderer;                      // who ordered (who is clicked on the button "Order store")
    private String deleter;                      // who deleted (who is clicked on the button "Delete store")

    public String getSite_url() {
        return site_url;
    }

    public void setSite_url(String site_url) {
        this.site_url = site_url;
    }

    public boolean isReady_to_distribute() {
        return ready_to_distribute;
    }

    public void setReady_to_distribute(boolean ready_to_distribute) {
        this.ready_to_distribute = ready_to_distribute;
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

    public boolean isDistributed() {
        return distributed;
    }

    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }

    public boolean isIs_queried_to_delete() {
        return is_queried_to_delete;
    }

    public void setIs_queried_to_delete(boolean is_queried_to_delete) {
        this.is_queried_to_delete = is_queried_to_delete;
    }

    public boolean isIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getPanel_domain() {
        return panel_domain;
    }

    public void setPanel_domain(String panel_domain) {
        this.panel_domain = panel_domain;
    }

    public String getSite_domain() {
        return site_domain;
    }

    public void setSite_domain(String site_domain) {
        this.site_domain = site_domain;
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

    public String getDate_time_query_to_delete() {
        return date_time_query_to_delete;
    }

    public void setDate_time_query_to_delete(String date_time_query_to_delete) {
        this.date_time_query_to_delete = date_time_query_to_delete;
    }

    public String getDate_time_deleted() {
        return date_time_deleted;
    }

    public void setDate_time_deleted(String date_time_deleted) {
        this.date_time_deleted = date_time_deleted;
    }

    public String getOrderer() {
        return orderer;
    }

    public void setOrderer(String orderer) {
        this.orderer = orderer;
    }

    public String getDeleter() {
        return deleter;
    }

    public void setDeleter(String deleter) {
        this.deleter = deleter;
    }
}
