package com.dokio.message.response.additional;

import java.util.List;

public class EmployeeListJSON {

    private Long id;
    private String name;
    private String job_title;
    private Boolean is_currently_employed;
    private Long cagent_id;
    private List<DepartmentWithPartsJSON> departments_with_parts;
    private List<IdAndNameJSON> employee_services;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob_title() {
        return job_title;
    }

    public void setJob_title(String job_title) {
        this.job_title = job_title;
    }

    public Boolean getIs_currently_employed() {
        return is_currently_employed;
    }

    public void setIs_currently_employed(Boolean is_currently_employed) {
        this.is_currently_employed = is_currently_employed;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public List<DepartmentWithPartsJSON> getDepartments_with_parts() {
        return departments_with_parts;
    }

    public void setDepartments_with_parts(List<DepartmentWithPartsJSON> departments_with_parts) {
        this.departments_with_parts = departments_with_parts;
    }

    public List<IdAndNameJSON> getEmployee_services() {
        return employee_services;
    }

    public void setEmployee_services(List<IdAndNameJSON> employee_services) {
        this.employee_services = employee_services;
    }
}
