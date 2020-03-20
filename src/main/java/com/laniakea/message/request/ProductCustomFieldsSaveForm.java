package com.laniakea.message.request;

public class ProductCustomFieldsSaveForm {

    private Long id;
    private Long product_id;
    private String name;
    private String parent_set_id;
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent_set_id() {
        return parent_set_id;
    }

    public void setParent_set_id(String parent_set_id) {
        this.parent_set_id = parent_set_id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
