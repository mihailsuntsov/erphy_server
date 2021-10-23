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
package com.dokio.message.request;

import java.math.BigDecimal;

public class InvoiceoutProductTableForm {

    private Long product_id;
    private Long invoiceout_id;
    private BigDecimal product_count;
    private BigDecimal product_price;
    private BigDecimal product_sumprice;
    private Long nds_id;
    private Long   price_type_id;
    private String price_type;
    private String additional;
    private BigDecimal product_price_of_type_price; // цена по типу цены на момент составления документа
    private Long department_id;                     // id отделения (склада) отгрузки
    private Boolean is_material;                    // материален ли данный товар/услуга (если не материален - не проводим проверку на его количество на складе)

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getInvoiceout_id() {
        return invoiceout_id;
    }

    public void setInvoiceout_id(Long invoiceout_id) {
        this.invoiceout_id = invoiceout_id;
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

    public Long getPrice_type_id() {
        return price_type_id;
    }

    public void setPrice_type_id(Long price_type_id) {
        this.price_type_id = price_type_id;
    }

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public BigDecimal getProduct_price_of_type_price() {
        return product_price_of_type_price;
    }

    public void setProduct_price_of_type_price(BigDecimal product_price_of_type_price) {
        this.product_price_of_type_price = product_price_of_type_price;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Boolean getIs_material() {
        return is_material;
    }

    public void setIs_material(Boolean is_material) {
        this.is_material = is_material;
    }
}
