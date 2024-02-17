package com.dokio.message.request.Sprav;

import com.dokio.message.request.additional.ResourceDepPartsForm;

import java.util.List;

public class SpravResourceForm {

    private Long id;
    private Long company_id;
    private String name;
    private String description;
    private List<ResourceDepPartsForm> departmentPartsTable; // quantity of this resource in each department part

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ResourceDepPartsForm> getDepartmentPartsTable() {
        return departmentPartsTable;
    }

    public void setDepartmentPartsTable(List<ResourceDepPartsForm> departmentPartsTable) {
        this.departmentPartsTable = departmentPartsTable;
    }
}
