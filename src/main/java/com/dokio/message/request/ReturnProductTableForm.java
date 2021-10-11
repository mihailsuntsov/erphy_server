package com.dokio.message.request;

import java.math.BigDecimal;

public class ReturnProductTableForm {
    private Long return_id;                         // id родиельского документа
    private Long product_id;                        // id товара
    private BigDecimal product_price;               // цена
    private BigDecimal  product_count;              // кол-во товара
    private BigDecimal product_netcost;             // себестоимость возврата
    private Integer nds_id;                         // ндс
    private BigDecimal product_sumprice;            // сумма
    private BigDecimal product_sumnetcost;          // сумма себестоимости

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Long getReturn_id() {
        return return_id;
    }

    public void setReturn_id(Long return_id) {
        this.return_id = return_id;
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

    public BigDecimal getProduct_netcost() {
        return product_netcost;
    }

    public void setProduct_netcost(BigDecimal product_netcost) {
        this.product_netcost = product_netcost;
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

    public BigDecimal getProduct_sumnetcost() {
        return product_sumnetcost;
    }

    public void setProduct_sumnetcost(BigDecimal product_sumnetcost) {
        this.product_sumnetcost = product_sumnetcost;
    }
}