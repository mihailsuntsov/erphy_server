//package com.laniakea.model.ManyToManyKeys;
//
//import com.laniakea.model.Products;
//import com.laniakea.model.Sprav.SpravSysBarcode;
//
//import javax.persistence.*;
//import javax.validation.constraints.Size;
//
//@Entity
//public class ProductBarcodes {
//
//    @EmbeddedId
//    private ProductBarcodesKey id;
//
//    @ManyToOne
//    @MapsId("product_id") //@MapsId означает, что мы связываем эти поля с частью ключа (ProductCagentsKey), и что
//    @JoinColumn(name = "product_id")//они являются foreign keys для связи many-to-one
//    private Products product;
//
//    @ManyToOne
//    @MapsId("barcode_id")//@MapsId ---//---//---
//    @JoinColumn(name = "barcode_id")
//    private SpravSysBarcode barcode;
//
//    @Column(name = "description")
//    @Size(max = 256)
//    private String description;
//
//    public ProductBarcodesKey getId() {
//        return id;
//    }
//
//    public void setId(ProductBarcodesKey id) {
//        this.id = id;
//    }
//
//    public Products getProduct() {
//        return product;
//    }
//
//    public void setProduct(Products product) {
//        this.product = product;
//    }
//
//    public SpravSysBarcode getBarcode() {
//        return barcode;
//    }
//
//    public void setBarcode(SpravSysBarcode barcode) {
//        this.barcode = barcode;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//}
