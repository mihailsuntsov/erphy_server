package com.dokio.message.response.store.woo.v3.products;

import java.util.List;
import java.util.Set;

public class ProductJSON {

    private Long                crm_id;
    private Integer             woo_id;
    private String              name;
    private String              type;
    private String              regular_price;
    private String              sale_price;
    private String              description;
    private String              short_description;
    private Set<Long>           categories;
    private List<ImageJSON>     images;
    private List<AttributeJSON> attributes;

    public List<AttributeJSON> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeJSON> attributes) {
        this.attributes = attributes;
    }

    public Long getCrm_id() {
        return crm_id;
    }

    public Integer getWoo_id() {
        return woo_id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRegular_price() {
        return regular_price;
    }

    public String getSale_price() {
        return sale_price;
    }

    public String getDescription() {
        return description;
    }

    public String getShort_description() {
        return short_description;
    }

    public Set<Long> getCategories() {
        return categories;
    }

    public void setCrm_id(Long crm_id) {
        this.crm_id = crm_id;
    }

    public void setWoo_id(Integer woo_id) {
        this.woo_id = woo_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRegular_price(String regular_price) {
        this.regular_price = regular_price;
    }

    public void setSale_price(String sale_price) {
        this.sale_price = sale_price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public void setCategories(Set<Long> categories) {
        this.categories = categories;
    }

    public List<ImageJSON> getImages() {
        return images;
    }

    public void setImages(List<ImageJSON> images) {
        this.images = images;
    }
}
