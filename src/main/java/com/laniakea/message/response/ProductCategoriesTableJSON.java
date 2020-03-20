package com.laniakea.message.response;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProductCategoriesTableJSON {

    @Id
    private Long id;
    private String name;
    private String parent_id;
    private String output_order;

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

    public String getParentt_id() {
        return parent_id;
    }

    public void setParentt_id(String parentt_id) {
        this.parent_id = parentt_id;
    }

    public String getOutput_order() {
        return output_order;
    }

    public void setOutput_order(String output_order) {
        this.output_order = output_order;
    }

}
