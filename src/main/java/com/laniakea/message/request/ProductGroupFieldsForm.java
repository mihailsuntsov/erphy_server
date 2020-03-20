package com.laniakea.message.request;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProductGroupFieldsForm {
    @Id
    private Long id;
    private Long company_id;
    private String name;
    private String description;
    private Long group_id;
    private String field_type;
    private Long parent_set_id;
    private String output_order;

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

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public String getField_type() {
        return field_type;
    }

    public void setField_type(String field_type) {
        this.field_type = field_type;
    }

    public Long getParent_set_id() {
        return parent_set_id;
    }

    public void setParent_set_id(Long parent_set_id) {
        this.parent_set_id = parent_set_id;
    }

    public String getOutput_order() {
        return output_order;
    }

    public void setOutput_order(String output_order) {
        this.output_order = output_order;
    }
}
