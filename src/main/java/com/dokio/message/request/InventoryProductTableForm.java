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

public class InventoryProductTableForm {

    private BigDecimal available;                   // кол-во доступно (на момент формирования документа) (высчитывается не сохраняется)
    private Long inventory_id;                      // id родиельского документа
    private Long product_id;                        // id товара
    private BigDecimal estimated_balance;           // кол-во товара по БД
    private BigDecimal actual_balance;              // кол-во товара фактическое (по пересчёту)
    private BigDecimal product_price;               // цена товара (может быть разная - закупочная, себестоимость, одна из типов цен)

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public Long getInventory_id() {
        return inventory_id;
    }

    public void setInventory_id(Long inventory_id) {
        this.inventory_id = inventory_id;
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
}