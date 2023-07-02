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

public class ProductHistoryJSON {
    private Long        id;
    private String      department;
    private String      docName;
    private Long        docId;
    private Integer     docTypeId;
    private BigDecimal  quantity;
    private BigDecimal  change;
    private String      date_time_created;
    private BigDecimal  last_purchase_price;
    private BigDecimal  avg_purchase_price;
    private BigDecimal  avg_netcost_price; // средняя себестоимость единицы товара при его продаже
    private BigDecimal  last_operation_price;
    private BigDecimal  price; // то же самое что и last_operation_price, для переделки product_history -> product_history
    private BigDecimal  netcost;    // себест. за 1 ед товара в операции
    private String      page_name; // для перехода из таблицы истории товара в карточку товара

    public String getPage_name() {
        return page_name;
    }

    public void setPage_name(String page_name) {
        this.page_name = page_name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getNetcost() {
        return netcost;
    }

    public void setNetcost(BigDecimal netcost) {
        this.netcost = netcost;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Integer getDocTypeId() {
        return docTypeId;
    }

    public void setDocTypeId(Integer docTypeId) {
        this.docTypeId = docTypeId;
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

    public BigDecimal getLast_operation_price() {
        return last_operation_price;
    }

    public void setLast_operation_price(BigDecimal last_operation_price) {
        this.last_operation_price = last_operation_price;
    }
}
