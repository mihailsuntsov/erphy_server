package com.dokio.message.request;

import java.util.Set;

public class ReturnForm {
    private Long    id;
    private Long    company_id;
    private Long    department_id;
    private Long    cagent_id;
    private String  description;
    private String  date_return;
    private Long    status_id;
    private String  doc_number;
    private Boolean is_completed; // завершена
    private Boolean nds;
    private Long    retail_sales_id;
    //------------- таблица товаров -----------------
    private Set<ReturnProductTableForm> returnProductTable;


    public Long getRetail_sales_id() {
        return retail_sales_id;
    }

    public void setRetail_sales_id(Long retail_sales_id) {
        this.retail_sales_id = retail_sales_id;
    }

    public Long getId() {
        return id;
    }

    public String getDate_return() {
        return date_return;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public void setDate_return(String date_return) {
        this.date_return = date_return;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Boolean getNds() {
        return nds;
    }

    public void setNds(Boolean nds) {
        this.nds = nds;
    }

    public Set<ReturnProductTableForm> getReturnProductTable() {
        return returnProductTable;
    }

    public void setReturnProductTable(Set<ReturnProductTableForm> returnProductTable) {
        this.returnProductTable = returnProductTable;
    }
}
