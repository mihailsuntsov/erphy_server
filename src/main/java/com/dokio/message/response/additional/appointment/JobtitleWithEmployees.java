package com.dokio.message.response.additional.appointment;

import java.util.List;

public class JobtitleWithEmployees {

    private Long id;
    private String name;
    private String description;
    private List<EmployeeWithServices> employees;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EmployeeWithServices> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeWithServices> employees) {
        this.employees = employees;
    }
}
