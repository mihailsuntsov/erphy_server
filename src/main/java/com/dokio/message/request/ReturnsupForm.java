package com.dokio.message.request;

import java.util.Set;

public class ReturnsupForm {
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
    private Long    acceptance_id;
    //------------- таблица товаров -----------------
    private Set<ReturnsupProductTableForm> returnsupProductTable;
    private String  uid;
    private Long    linked_doc_id;//id связанного документа
    private String  linked_doc_name;//имя (таблицы) связанного документа
    private String  uid_from;// uid исходящего (родительского) документа
    private String  uid_to; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.

    public String getUid_from() {
        return uid_from;
    }

    public void setUid_from(String uid_from) {
        this.uid_from = uid_from;
    }

    public String getUid_to() {
        return uid_to;
    }

    public void setUid_to(String uid_to) {
        this.uid_to = uid_to;
    }

    public Long getLinked_doc_id() {
        return linked_doc_id;
    }

    public void setLinked_doc_id(Long linked_doc_id) {
        this.linked_doc_id = linked_doc_id;
    }

    public String getLinked_doc_name() {
        return linked_doc_name;
    }

    public void setLinked_doc_name(String linked_doc_name) {
        this.linked_doc_name = linked_doc_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getId() {
        return id;
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

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate_return() {
        return date_return;
    }

    public void setDate_return(String date_return) {
        this.date_return = date_return;
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

    public Set<ReturnsupProductTableForm> getReturnsupProductTable() {
        return returnsupProductTable;
    }

    public void setReturnsupProductTable(Set<ReturnsupProductTableForm> returnsupProductTable) {
        this.returnsupProductTable = returnsupProductTable;
    }

    public Long getAcceptance_id() {
        return acceptance_id;
    }

    public void setAcceptance_id(Long acceptance_id) {
        this.acceptance_id = acceptance_id;
    }
}