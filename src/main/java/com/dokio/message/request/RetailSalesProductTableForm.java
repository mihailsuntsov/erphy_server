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

public class RetailSalesProductTableForm {

    private BigDecimal available;                   // кол-во доступно (на момент формирования документа) (высчитывается не сохраняется)
    private Long retail_sales_id;                   // id родиельского документа
    private Long department_id;                     // id отделения (склада) отгрузки
    private Long edizm_id;                          // id единицы измерения
    private Long nds_id;                            // id ндс
    private Long price_type_id;                     // id типа цены
    private BigDecimal product_count;               // кол-во товара
    private Long product_id;                        // id товара
    private BigDecimal product_price;               // цена продажи
    private BigDecimal product_price_of_type_price; // цена по типу цены на момент составления документа
    private BigDecimal product_sumprice;            // сумма (цена*кол-во)
    private BigDecimal shipped;                     // отгружено (высчитывается, не сохраняется)
    private Boolean is_material;                    // материален ли данный товар/услуга (если не материален - не проводим проверку на его количество на складе)


    public Boolean getIs_material() {
        return is_material;
    }

    public void setIs_material(Boolean is_material) {
        this.is_material = is_material;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public Long getRetail_sales_id() {
        return retail_sales_id;
    }

    public void setRetail_sales_id(Long retail_sales_id) {
        this.retail_sales_id = retail_sales_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
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

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
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

    public BigDecimal getProduct_price_of_type_price() {
        return product_price_of_type_price;
    }

    public void setProduct_price_of_type_price(BigDecimal product_price_of_type_price) {
        this.product_price_of_type_price = product_price_of_type_price;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }

    public BigDecimal getShipped() {
        return shipped;
    }

    public void setShipped(BigDecimal shipped) {
        this.shipped = shipped;
    }
}
