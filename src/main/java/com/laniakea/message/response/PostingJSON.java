package com.laniakea.message.response;


public class PostingJSON {
    private Long id;
    private String master;
    private String creator;
    private String changer;
    private Long master_id;
    private Long creator_id;
    private Long changer_id;
    private Long company_id;
    private Long department_id;
    private String department;
    private Long doc_number;
    private String posting_date;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String description;
    private boolean is_completed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
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

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
    }

    public String getPosting_date() {
        return posting_date;
    }

    public void setPosting_date(String posting_date) {
        this.posting_date = posting_date;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
    }
}
