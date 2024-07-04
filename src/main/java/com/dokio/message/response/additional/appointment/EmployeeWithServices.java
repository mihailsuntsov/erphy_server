package com.dokio.message.response.additional.appointment;

import com.dokio.message.response.Sprav.IdAndName;

import java.util.Set;

public class EmployeeWithServices {
    private Long id;
    private String name;
    private Set<IdAndName> services;

    public EmployeeWithServices(Long id, String name, Set<IdAndName> services) {
        this.id = id;
        this.name = name;
        this.services = services;
    }

    public EmployeeWithServices(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public EmployeeWithServices() {
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

    public Set<IdAndName> getServices() {
        return services;
    }

    public void setServices(Set<IdAndName> services) {
        this.services = services;
    }
}
