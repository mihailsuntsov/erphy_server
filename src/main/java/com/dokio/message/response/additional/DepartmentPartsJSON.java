package com.dokio.message.response.additional;

import java.util.List;

public class DepartmentPartsJSON {

    private Long id;
    private Long creator_id;
    private Long changer_id;
    private Long company_id;
    private String creator;
    private String changer;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String name;
    private String description;
    private Boolean is_active;
    private Boolean is_deleted;
    private Long department_id;     // used in    getDepartmentPartsWithResourceQttList
    private String department_name; // used in    getDepartmentPartsWithResourceQttList
    private Integer resource_qtt;   // used in    getDepartmentPartsWithResourceQttList
    private List<IdAndNameJSON> deppartProducts; // list of services in this department part

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public Integer getResource_qtt() {
        return resource_qtt;
    }

    public void setResource_qtt(Integer resource_qtt) {
        this.resource_qtt = resource_qtt;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public List<IdAndNameJSON> getDeppartProducts() {
        return deppartProducts;
    }

    public void setDeppartProducts(List<IdAndNameJSON> deppartProducts) {
        this.deppartProducts = deppartProducts;
    }
}
