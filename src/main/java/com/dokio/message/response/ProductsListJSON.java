/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.message.response;

import java.math.BigDecimal;

public class ProductsListJSON {
    private Long id;                                // id товара
    private Long product_id;                        // и это тоже id товара (так надо)
    private String name;                            // наименование товара
    private Integer nds_id;                         // id ндс
    private Long edizm_id;                          // id ед. измерения
    private String edizm;                           // наименование ед. измерения
    private String filename;                        // имя файла
    private BigDecimal total;                       // всего товаров
    private BigDecimal reserved;                    // зарезервировано в других документах Заказ покупателя
    private BigDecimal total_in_all_my_depths;      // всего товаров во всех моих отделениях (складах)
    private BigDecimal reserved_in_all_my_depths;   // зарезервировано товаров во всех моих отделениях (складах)
    private String ppr_name_api_atol;               // Признак предмета расчета в системе Атол
//    private String nds_name_api_atol;             // НДС в системе Атол
    private Boolean is_material;                    // определяет материальный ли товар/услуга. Нужен для отображения полей, относящимся к товару и их скрытия в противном случае (например, остатки на складе, резервы - это неприменимо к нематериальным вещам - услугам, работам)
    private BigDecimal reserved_current;            // зарезервировано единиц товара в отделении (складе) в ЭТОМ (текущем) Заказе покупателя:
    private Boolean     indivisible;                // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
    private BigDecimal  priceOfTypePrice;           // цена по запрошенному id типа цены
    private BigDecimal  avgCostPrice;               // средняя себестоимость
    private BigDecimal  lastPurchasePrice;          // последняя закупочная цена
    private BigDecimal  avgPurchasePrice ;          // средняя закупочная цена

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEdizm() {
        return edizm;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public BigDecimal getReserved_current() {
        return reserved_current;
    }

    public void setReserved_current(BigDecimal reserved_current) {
        this.reserved_current = reserved_current;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
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

    public BigDecimal getTotal_in_all_my_depths() {
        return total_in_all_my_depths;
    }

    public void setTotal_in_all_my_depths(BigDecimal total_in_all_my_depths) {
        this.total_in_all_my_depths = total_in_all_my_depths;
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
//    public String getNds_name_api_atol() {
//        return nds_name_api_atol;
//    }
//
//    public void setNds_name_api_atol(String nds_name_api_atol) {
//        this.nds_name_api_atol = nds_name_api_atol;
//    }

    public BigDecimal getReserved_in_all_my_depths() {
        return reserved_in_all_my_depths;
    }

    public void setReserved_in_all_my_depths(BigDecimal reserved_in_all_my_depths) {
        this.reserved_in_all_my_depths = reserved_in_all_my_depths;
    }
}
