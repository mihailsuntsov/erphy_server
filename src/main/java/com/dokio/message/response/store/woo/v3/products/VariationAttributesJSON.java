package com.dokio.message.response.store.woo.v3.products;

public class VariationAttributesJSON {

    private Integer id;     // woo_id of attribute
    private String option;  // term name

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}

