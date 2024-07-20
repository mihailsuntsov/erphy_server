package com.dokio.message.response.additional.calendar;

import java.util.List;

public class ShortServiceInfoWithAttributes {

    private Long id;
    private String name;
    private String description;
    private String imageFile;
    private List<ProductAttribute> attributesList;

    public ShortServiceInfoWithAttributes() {
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
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

    public List<ProductAttribute> getAttributesList() {
        return attributesList;
    }

    public void setAttributesList(List<ProductAttribute> attributesList) {
        this.attributesList = attributesList;
    }
}
