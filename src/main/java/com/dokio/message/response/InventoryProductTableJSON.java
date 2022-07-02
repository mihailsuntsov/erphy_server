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

package com.dokio.message.response;

import java.math.BigDecimal;

public class InventoryProductTableJSON {

    private Long        id;                          // id строки в таблице inventory_product - необходимо для идентификации ряда row_id в фронтэнде
    private String      name;                        // наименование товара
    private Long        product_id;                  // id товара
    private BigDecimal  estimated_balance;           // кол-во товара по БД (на момент формирования документа Инвентаризаиця)
    private BigDecimal  actual_balance;              // кол-во товара фактическое (по ручному пересчёту товара в магазине)
    private String      edizm;                       // наименование единицы измерения товара
    private BigDecimal  product_price;               // цена товара (может быть разная - закупочная, себестоимость, одна из типов цен)
    private Boolean     indivisible;                 // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
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

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public BigDecimal getEstimated_balance() {
        return estimated_balance;
    }

    public void setEstimated_balance(BigDecimal estimated_balance) {
        this.estimated_balance = estimated_balance;
    }

    public BigDecimal getActual_balance() {
        return actual_balance;
    }

    public void setActual_balance(BigDecimal actual_balance) {
        this.actual_balance = actual_balance;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }
}
