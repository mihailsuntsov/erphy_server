package com.dokio.message.request;

import java.math.BigDecimal;

public class ReturnsupProductTableForm {
    private Long returnsup_id;                         // id родиельского документа
    private Long product_id;                        // id товара
    private BigDecimal product_price;               // цена
    private BigDecimal  product_count;              // кол-во товара
    private Integer nds_id;                         // ндс
    private BigDecimal product_sumprice;            // сумма

    public Long getReturnsup_id() {
        return returnsup_id;
    }

    public void setReturnsup_id(Long returnsup_id) {
        this.returnsup_id = returnsup_id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }
}
