package com.dokio.message.response.additional;

import com.dokio.message.request.additional.CompanyContactsForm;

import java.util.List;

public class DepartmentWithPartsJSON {

    private Long   department_id;
    private String department_name;
    private String department_address;
    private String department_additional;
    private List<DepartmentPartJSON> parts;
    List<CompanyContactsForm> contactsList;

    public DepartmentWithPartsJSON(Long department_id, String department_name, String department_address, String department_additional, List<DepartmentPartJSON> parts, List<CompanyContactsForm> contactsList) {
        this.department_id = department_id;
        this.department_name = department_name;
        this.parts = parts;
        this.department_address = department_address;
        this.department_additional = department_additional;
        this.contactsList = contactsList;
    }

    public List<CompanyContactsForm> getContactsList() {
        return contactsList;
    }

    public void setContactsList(List<CompanyContactsForm> contactsList) {
        this.contactsList = contactsList;
    }

    public String getDepartment_address() {
        return department_address;
    }

    public void setDepartment_address(String department_address) {
        this.department_address = department_address;
    }

    public String getDepartment_additional() {
        return department_additional;
    }

    public void setDepartment_additional(String department_additional) {
        this.department_additional = department_additional;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public List<DepartmentPartJSON> getParts() {
        return parts;
    }

    public void setParts(List<DepartmentPartJSON> parts) {
        this.parts = parts;
    }
}
