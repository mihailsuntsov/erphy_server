//package com.dokio.model.ManyToManyKeys;
//// Данный класс выполняет роль первичного ключа для класса ProductBarcodes и таблицы product_barcodes
//// которая связывает таблицы products и sprav_sys_barcode
//// https://www.baeldung.com/jpa-many-to-many
//
//// Класс для связи Товаров и услуг (Products) и их штрихкодов (SpravSysBarcode)
//
//import javax.persistence.Column;
//import javax.persistence.Embeddable;
//import java.io.Serializable;
//
//@Embeddable
//class ProductBarcodesKey implements Serializable {
//
//    @Column(name = "product_id")
//    private Long productId;
//
//    @Column(name = "barcode_id")
//    private Long barcodeId;
//
//    public Long getProductId() {
//        return productId;
//    }
//
//    public void setProductId(Long productId) {
//        this.productId = productId;
//    }
//
//    public Long getBarcodeId() {
//        return barcodeId;
//    }
//
//    public void setBarcodeId(Long barcodeId) {
//        this.barcodeId = barcodeId;
//    }
//}
