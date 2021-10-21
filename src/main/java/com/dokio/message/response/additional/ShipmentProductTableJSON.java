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

public class ShipmentProductTableJSON {
    private Long id;
    private Long product_id;
    private Long customers_orders_id;
    private Long department_id; //отделение  (склад)
    private String department;
    private BigDecimal product_count;
    private Long edizm_id;
    private BigDecimal product_price;
    private BigDecimal product_sumprice;
    private Long nds_id;
    private Long   price_type_id;
    private String price_type;
    private String name;
    private String nds;
    private String edizm;
    private String additional;
    private BigDecimal reserved; // сколько зарезервировано в других (не связанных с этой розн. продажей) Заказах покупателя
    private BigDecimal reserved_current; // сколько зарезервировано в данном документе
    private BigDecimal total; // всего на складе
    private BigDecimal shipped; //отгружено
    private String ppr_name_api_atol; //Признак предмета расчета в системе Атол
    private Boolean is_material; //определяет материальный ли товар/услуга. Нужен для отображения полей, относящихся к товару и их скрытия в случае если это услуга (например, остатки на складе, резервы - это неприменимо к нематериальным вещам - услугам, работам)
    // доступно получаем из разницы total и reserved
    private Boolean     indivisible;         // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)

    public BigDecimal getReserved_current() {
        return reserved_current;
    }

    public void setReserved_current(BigDecimal reserved_current) {
        this.reserved_current = reserved_current;
    }

    public BigDecimal getShipped() {
        return shipped;
    }

    public void setShipped(BigDecimal shipped) {
        this.shipped = shipped;
    }

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

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getCustomers_orders_id() {
        return customers_orders_id;
    }

    public void setCustomers_orders_id(Long customers_orders_id) {
        this.customers_orders_id = customers_orders_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNds() {
        return nds;
    }

    public void setNds(String nds) {
        this.nds = nds;
    }

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPpr_name_api_atol() {
        return ppr_name_api_atol;
    }

    public void setPpr_name_api_atol(String ppr_name_api_atol) {
        this.ppr_name_api_atol = ppr_name_api_atol;
    }

    public Boolean getIs_material() {
        return is_material;
    }

    public void setIs_material(Boolean is_material) {
        this.is_material = is_material;
    }
}
