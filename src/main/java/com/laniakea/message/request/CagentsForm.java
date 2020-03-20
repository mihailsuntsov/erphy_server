package com.laniakea.message.request;

import java.util.Set;

public class CagentsForm {
    private Long id;
    private String name;
    private String description;
    private Long opf_id;
    private Long company_id;
    private Set<Long> selectedCagentCategories;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOpf_id() {
        return opf_id;
    }

    public void setOpf_id(Long opf_id) {
        this.opf_id = opf_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Set<Long> getSelectedCagentCategories() {
        return selectedCagentCategories;
    }

    public void setSelectedCagentCategories(Set<Long> selectedCagentCategories) {
        this.selectedCagentCategories = selectedCagentCategories;
    }
}
