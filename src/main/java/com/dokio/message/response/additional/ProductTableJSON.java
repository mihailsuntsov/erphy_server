/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.message.response.additional;

import java.math.BigDecimal;

// Универсальный класс для отдачи информации о товарах
public class ProductTableJSON {

    private Integer     row_num;                // номер строки при выводе печатной версии
    private Long        product_id;             // id товара
    private BigDecimal  product_count;          // кол-во товара
    private BigDecimal  product_price;          // цена товара
    private BigDecimal  product_sumprice;       // сумма по цене
    private Long        nds_id;                 // id ндс из справочника НДС
    private String      nds;                    // наименование НДС (например "20%")
    private Integer     nds_value;              // сколько % НДС у данного товара
    private String      name;                   // наименование товара
    private String      edizm;                  // наименование ед. измерения
    private Boolean     is_material;            // материальность

    public Integer getRow_num() {
        return row_num;
    }

    public void setRow_num(Integer row_num) {
        this.row_num = row_num;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }

    public Long getNds_id() {
        return nds_id;
    }

    public void setNds_id(Long nds_id) {
        this.nds_id = nds_id;
    }

    public String getNds() {
        return nds;
    }

    public void setNds(String nds) {
        this.nds = nds;
    }

    public Integer getNds_value() {
        return nds_value;
    }

    public void setNds_value(Integer nds_value) {
        this.nds_value = nds_value;
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

    public Boolean getIs_material() {
        return is_material;
    }

    public void setIs_material(Boolean is_material) {
        this.is_material = is_material;
    }
}
