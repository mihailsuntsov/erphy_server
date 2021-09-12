package com.dokio.message.response;

import java.math.BigDecimal;

public class ProductsInfoListJSON {
    private Long        product_id;          // id товара
    private String      name;                // наименование товара
    private String      edizm;               // наименование ед. измерения товара
    private String      filename;            // картинка товара
    private BigDecimal  estimated_balance;   // фактическое кол-во товара в отделении
    private BigDecimal  priceOfTypePrice;    // цена по запрошенному id типа цены
    private BigDecimal  avgCostPrice;        // средняя себестоимость
    private BigDecimal  lastPurchasePrice;   // последняя закупочная цена
    private BigDecimal  avgPurchasePrice ;   // средняя закупочная цена
    private BigDecimal  remains;             // остаток на складе
    private BigDecimal  total;               // остаток на складе (в некоторых документах на фронте используется total)
    private Integer     nds_id;              // id ставки НДС
    private Boolean     indivisible;         // неделимый товар (все действия только с кол-вом, кратным 1)

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public BigDecimal getEstimated_balance() {
        return estimated_balance;
    }

    public void setEstimated_balance(BigDecimal estimated_balance) {
        this.estimated_balance = estimated_balance;
    }

    public BigDecimal getPriceOfTypePrice() {
        return priceOfTypePrice;
    }

    public void setPriceOfTypePrice(BigDecimal priceOfTypePrice) {
        this.priceOfTypePrice = priceOfTypePrice;
    }

    public BigDecimal getAvgCostPrice() {
        return avgCostPrice;
    }

    public void setAvgCostPrice(BigDecimal avgCostPrice) {
        this.avgCostPrice = avgCostPrice;
    }

    public BigDecimal getLastPurchasePrice() {
        return lastPurchasePrice;
    }

    public void setLastPurchasePrice(BigDecimal lastPurchasePrice) {
        this.lastPurchasePrice = lastPurchasePrice;
    }

    public BigDecimal getAvgPurchasePrice() {
        return avgPurchasePrice;
    }

    public void setAvgPurchasePrice(BigDecimal avgPurchasePrice) {
        this.avgPurchasePrice = avgPurchasePrice;
    }

    public BigDecimal getRemains() {
        return remains;
    }

    public void setRemains(BigDecimal remains) {
        this.remains = remains;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }
}
