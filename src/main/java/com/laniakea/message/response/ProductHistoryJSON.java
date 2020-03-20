package com.laniakea.message.response;

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
    private BigDecimal  avg_netcost_price;
    private BigDecimal  last_operation_price;

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
