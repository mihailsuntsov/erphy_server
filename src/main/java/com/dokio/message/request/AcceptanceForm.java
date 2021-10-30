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
import java.util.Set;

public class AcceptanceForm {
    private Long    id;
    private Long    company_id;
    private String  description;
    private Long    department_id;
    private Long    cagent_id;
    private Integer doc_number;
    private String  acceptance_date;
    private boolean nds;
    private boolean nds_included;
    private BigDecimal overhead;
    private Integer overhead_netcost_method;//0 - нет, 1 - по весу цены в поставке
    private Set<AcceptanceProductForm> acceptanceProductTable;
    private Long    status_id;
    private String  uid;
    private Long    linked_doc_id;//id связанного документа
    private String  linked_doc_name;//имя (таблицы) связанного документа
    private String  parent_uid;// uid исходящего (родительского) документа
    private String  child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Boolean is_completed;// проведён

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
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

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
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

    public Integer getOverhead_netcost_method() {
        return overhead_netcost_method;
    }

    public void setOverhead_netcost_method(Integer overhead_netcost_method) {
        this.overhead_netcost_method = overhead_netcost_method;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Integer doc_number) {
        this.doc_number = doc_number;
    }

    public String getAcceptance_date() {
        return acceptance_date;
    }

    public void setAcceptance_date(String acceptance_date) {
        this.acceptance_date = acceptance_date;
    }

    public boolean isNds() {
        return nds;
    }

    public void setNds(boolean nds) {
        this.nds = nds;
    }

    public boolean isNds_included() {
        return nds_included;
    }

    public void setNds_included(boolean nds_included) {
        this.nds_included = nds_included;
    }

    public BigDecimal getOverhead() {
        return overhead;
    }

    public void setOverhead(BigDecimal overhead) {
        this.overhead = overhead;
    }

    public Set<AcceptanceProductForm> getAcceptanceProductTable() {
        return acceptanceProductTable;
    }

    public void setAcceptanceProductTable(Set<AcceptanceProductForm> acceptanceProductTable) {
        this.acceptanceProductTable = acceptanceProductTable;
    }

    @Override
    public String toString() {
        return "AcceptanceForm: id=" + this.id + ", company_id=" + this.company_id;
    }

//    public Set<Long> getTestSet() {
//        return testSet;
//    }
//
//    public void setTestSet(Set<Long> testSet) {
//        this.testSet = testSet;
//    }
}

