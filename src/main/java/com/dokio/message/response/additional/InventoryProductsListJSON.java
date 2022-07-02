/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.response.additional;

import java.math.BigDecimal;

public class InventoryProductsListJSON {

    private Long        product_id;          // id товара
    private String      name;                // наименование товара
    private String      edizm;               // наименование ед. измерения товара
    private String      filename;            // картинка товара
    private BigDecimal  estimated_balance;   // расчётное кол-во товара в отделении
    private BigDecimal  priceOfTypePrice;    // цена по запрошенному id типа цены
    private BigDecimal  avgCostPrice;        // средняя себестоимость
    private BigDecimal  lastPurchasePrice;   // последняя закупочная цена
    private BigDecimal  avgPurchasePrice ;   // средняя закупочная цена
    private BigDecimal  remains;             // остаток на складе (для инвенторизации не нужен, но нужен в других, например в возвратах)
    private Boolean     indivisible;         // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public BigDecimal getRemains() {
        return remains;
    }

    public void setRemains(BigDecimal remains) {
        this.remains = remains;
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
}
