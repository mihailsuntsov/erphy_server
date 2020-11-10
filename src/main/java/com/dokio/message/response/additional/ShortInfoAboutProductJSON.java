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
package com.dokio.message.response.additional;

import java.math.BigDecimal;
public class ShortInfoAboutProductJSON {


    private BigDecimal      quantity;
    private BigDecimal      change;
    private BigDecimal      avg_purchase_price;
    private BigDecimal      last_purchase_price;
    private BigDecimal      avg_netcost_price;
    private BigDecimal      department_sell_price;
    private String          department_type_price;
    private String          date_time_created;

    public String getDepartment_type_price() {
        return department_type_price;
    }
    public void setDepartment_type_price(String department_type_price) {
        this.department_type_price = department_type_price;
    }
    public BigDecimal getDepartment_sell_price() {
        return department_sell_price;
    }
    public void setDepartment_sell_price(BigDecimal department_sell_price) {
        this.department_sell_price = department_sell_price;
    }
    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getChange() {
        return change;
    }
    public void setChange(BigDecimal change) {
        this.change = change;
    }
    public BigDecimal getAvg_purchase_price() {
        return avg_purchase_price;
    }
    public void setAvg_purchase_price(BigDecimal avg_purchase_price) {
        this.avg_purchase_price = avg_purchase_price;
    }
    public BigDecimal getAvg_netcost_price() {
        return avg_netcost_price;
    }
    public void setAvg_netcost_price(BigDecimal avg_netcost_price) {
        this.avg_netcost_price = avg_netcost_price;
    }
    public String getDate_time_created() {
        return date_time_created;
    }
    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }
    public BigDecimal getLast_purchase_price() {
        return last_purchase_price;
    }
    public void setLast_purchase_price(BigDecimal last_purchase_price) {
        this.last_purchase_price = last_purchase_price;
    }
}
