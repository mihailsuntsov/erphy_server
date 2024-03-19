package com.dokio.message.response.additional;

import java.util.List;

public class DepartmentPartsJSON {

    private Long id;
    private Long department_id;
    private String department_name;
    private String name;
    private String description;
    private Boolean is_active;
    private Boolean is_deleted;
    private Integer resource_qtt;
    private List<IdAndNameJSON> deppart_services; // list of services in this department part

    public List<IdAndNameJSON> getDeppart_services() {
        return deppart_services;
    }

    public void setDeppart_services(List<IdAndNameJSON> deppart_services) {
        this.deppart_services = deppart_services;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
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

    public Integer getResource_qtt() {
        return resource_qtt;
    }

    public void setResource_qtt(Integer resource_qtt) {
        this.resource_qtt = resource_qtt;
    }
}
