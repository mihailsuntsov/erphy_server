package com.dokio.message.response.additional.eployeescdl;

import com.dokio.message.response.additional.DepartmentsWithPartsJSON;
import com.dokio.message.response.additional.IdAndNameJSON;

import java.util.List;

public class EmployeeScedule {
    private Long id;
    private String name;            // Employee's name
    private String photo_link;      // link to the picture of employee
    private String jobtitle;        // Job title of employee
    private Boolean is_currently_employed;
    private Long cagent_id;
//    private List<DepartmentsWithPartsJSON> departments_with_parts;
    private List<IdAndNameJSON> departments;
    private List<IdAndNameJSON> employee_services;
    private List<SceduleDay> days;  // days array

//    public EmployeeScedule(String name, String photo_link, String jobtitle, Boolean is_currently_employed, Long cagent_id, List<DepartmentsWithPartsJSON> departments_with_parts, List<IdAndNameJSON> employee_services, List<SceduleDay> days, Long id) {
//        this.name = name;
//        this.photo_link = photo_link;
//        this.jobtitle = jobtitle;
//        this.is_currently_employed = is_currently_employed;
//        this.cagent_id = cagent_id;
//        this.departments_with_parts = departments_with_parts;
//        this.employee_services = employee_services;
//        this.days = days;
//        this.id=id;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

//    public List<DepartmentsWithPartsJSON> getDepartments_with_parts() {
//        return departments_with_parts;
//    }
//
//    public void setDepartments_with_parts(List<DepartmentsWithPartsJSON> departments_with_parts) {
//        this.departments_with_parts = departments_with_parts;
//    }

    public List<IdAndNameJSON> getDepartments() {
        return departments;
    }

    public void setDepartments(List<IdAndNameJSON> departments) {
        this.departments = departments;
    }

    public List<IdAndNameJSON> getEmployee_services() {
        return employee_services;
    }

    public void setEmployee_services(List<IdAndNameJSON> employee_services) {
        this.employee_services = employee_services;
    }

    public EmployeeScedule() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_link() {
        return photo_link;
    }

    public void setPhoto_link(String photo_link) {
        this.photo_link = photo_link;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

    public List<SceduleDay> getDays() {
        return days;
    }

    public void setDays(List<SceduleDay> days) {
        this.days = days;
    }
}
