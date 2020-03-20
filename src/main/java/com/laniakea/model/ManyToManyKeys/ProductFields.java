package com.laniakea.model.ManyToManyKeys;

import com.laniakea.model.ProductGroupFields;
import com.laniakea.model.Products;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
public class ProductFields {

    @EmbeddedId
    ProductFieldsKey id;

    @ManyToOne
    @MapsId("product_id") //@MapsId означает, что мы связываем эти поля с частью ключа (ProductFieldsKey), и что
    @JoinColumn(name = "product_id")//они являются foreign keys для связи many-to-one
    private Products product;

    @ManyToOne
    @MapsId("field_id")//@MapsId ---//---//---
    @JoinColumn(name = "field_id")
    private ProductGroupFields productGroupField;

    @Column(name = "field_value")
    @Size(max = 256)
    private String fieldValue;

    public ProductFieldsKey getId() {
        return id;
    }

    public void setId(ProductFieldsKey id) {
        this.id = id;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public ProductGroupFields getProductGroupField() {
        return productGroupField;
    }

    public void setProductGroupField(ProductGroupFields productGroupField) {
        this.productGroupField = productGroupField;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
