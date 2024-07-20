package com.dokio.message.response.additional.calendar;

import com.dokio.message.response.additional.IdNameAndDescription;

import java.util.List;

public class ProductAttribute {
    private Long id;
    private String name;
    private String description;
    private List<IdNameAndDescription> termsList;

    public ProductAttribute() {
    }

    public ProductAttribute(Long id) {
        this.id = id;
    }

    public ProductAttribute(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public ProductAttribute(Long id, String name, String description, List<IdNameAndDescription> termsList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.termsList = termsList;
    }

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

    public List<IdNameAndDescription> getTermsList() {
        return termsList;
    }

    public void setTermsList(List<IdNameAndDescription> termsList) {
        this.termsList = termsList;
    }
}
