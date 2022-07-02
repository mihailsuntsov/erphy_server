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
