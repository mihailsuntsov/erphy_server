package com.laniakea.message.response;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProductBarcodesJSON {

    @Id
    private Long    id;
    private Long    barcode_id;
    private Long    product_id;
    private String  name;
    private String  value;
    private String  description;

    public Long getBarcode_id() {
        return barcode_id;
    }

    public void setBarcode_id(Long barcode_id) {
        this.barcode_id = barcode_id;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
