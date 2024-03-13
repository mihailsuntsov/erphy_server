package com.dokio.message.request.Sprav;

import com.dokio.message.request.additional.JobtitleProductsForm;

import java.util.List;

public class SpravJobtitleForm {

    private Long id;
    private Long company_id;
    private String name;
    private String description;
    private List<JobtitleProductsForm> jobtitleProductsFormTable;

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

    public List<JobtitleProductsForm> getJobtitleProductsFormTable() {
        return jobtitleProductsFormTable;
    }

    public void setJobtitleProductsFormTable(List<JobtitleProductsForm> jobtitleProductsFormTable) {
        this.jobtitleProductsFormTable = jobtitleProductsFormTable;
    }
}
