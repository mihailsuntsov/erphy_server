package com.laniakea.message.request;

import java.util.Set;

public class AcceptanceForm {
    private Long id;
    private Long company_id;
    private String description;
    private Long department_id;
    private Long cagent_id;
    private String doc_number;
    private String acceptance_date;
    private boolean nds;
    private boolean nds_included;
    private String overhead;
    private boolean is_completed;
    private Integer overhead_netcost_method;//0 - нет, 1 - по весу цены в поставке
    private Set<AcceptanceProductForm> acceptanceProductTable;
//    private Set<Long> testSet;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public boolean is_completed() {
        return is_completed;
    }

    public boolean isIs_completed() {
        return is_completed;
    }

    public Integer getOverhead_netcost_method() {
        return overhead_netcost_method;
    }

    public void setOverhead_netcost_method(Integer overhead_netcost_method) {
        this.overhead_netcost_method = overhead_netcost_method;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
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

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
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

    public String getOverhead() {
        return overhead;
    }

    public void setOverhead(String overhead) {
        this.overhead = overhead;
    }

    public Set<AcceptanceProductForm> getAcceptanceProductTable() {
        return acceptanceProductTable;
    }

    public void setAcceptanceProductTable(Set<AcceptanceProductForm> acceptanceProductTable) {
        this.acceptanceProductTable = acceptanceProductTable;
    }

//    public Set<Long> getTestSet() {
//        return testSet;
//    }
//
//    public void setTestSet(Set<Long> testSet) {
//        this.testSet = testSet;
//    }
}

