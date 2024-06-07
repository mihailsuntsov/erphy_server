package com.dokio.message.response.additional.eployeescdl;

public class DeppartProduct {

    private Long id;
    private String name;
    private Boolean     isEmployeeRequired;
    public  Boolean getEmployeeRequired() {
        return isEmployeeRequired;
    }

    public void setEmployeeRequired(Boolean employeeRequired) {
        isEmployeeRequired = employeeRequired;
    }

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

}
