package com.dokio.message.response.onlineScheduling;

import com.dokio.message.response.store.woo.v3.products.AttributeJSON;
import com.dokio.message.response.store.woo.v3.products.ImageJSON;

import java.util.List;
import java.util.Set;

public class OnlineSchedulingServiceJSON {

    private Long                id;
    private String              name;
    private String              type;
    private String              price;
    private String              sku;
    private Set<Long>           categories;
    private List<ImageJSON>     images;

//    private List<AttributeJSON> attributes;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Set<Long> getCategories() {
        return categories;
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

//    public List<AttributeJSON> getAttributes() {
//        return attributes;
//    }

//    public void setAttributes(List<AttributeJSON> attributes) {
//        this.attributes = attributes;
//    }
}
