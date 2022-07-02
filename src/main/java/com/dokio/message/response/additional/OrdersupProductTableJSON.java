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

public class OrdersupProductTableJSON {

    private Long        id;                          // id строки в таблице - необходимо для идентификации ряда row_id в фронтэнде
    private String      name;                        // наименование товара
    private Long        product_id;                  // id товара
    private BigDecimal  product_count;               // кол-во товара
    private BigDecimal  estimated_balance;           // кол-во товара по БД (на момент формирования документа Инвентаризаиця)
    private BigDecimal  actual_balance;              // кол-во товара фактическое (по ручному пересчёту товара в магазине)
    private String      edizm;                       // наименование единицы измерения товара
    private Long        nds_id;                      // id ндс
    private BigDecimal  product_price;               // цена товара (может быть разная - закупочная, себестоимость, одна из типов цен)
    private BigDecimal  product_sumprice;               // цена товара (может быть разная - закупочная, себестоимость, одна из типов цен)
    private String      additional;                  // доп. инфо для поставщика
    private Boolean     indivisible;                 // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
    private BigDecimal  total;                       // всего на складе
    private BigDecimal  reserved;                    // в резервах
    private Boolean     is_material;                 // материален ли товар
    // для печатной версии
    private Integer row_num;                         // номер строки при выводе печатной версии
    private Integer nds_value;                       // сколько % НДС у данного товара

    public Integer getRow_num() {
        return row_num;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }

    public void setRow_num(Integer row_num) {
        this.row_num = row_num;
    }

    public Integer getNds_value() {
        return nds_value;
    }

    public void setNds_value(Integer nds_value) {
        this.nds_value = nds_value;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public Boolean getIs_material() {
        return is_material;
    }

    public void setIs_material(Boolean is_material) {
        this.is_material = is_material;
    }

    public Long getNds_id() {
        return nds_id;
    }

    public void setNds_id(Long nds_id) {
        this.nds_id = nds_id;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
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

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
    }
}
