package com.dokio.message.response.additional.appointment;

import java.util.Set;

public class DepartmentPartWithServicesIds {
    private Long id;
    private Set<Long> servicesIds;

    public DepartmentPartWithServicesIds() {
    }

    public DepartmentPartWithServicesIds(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Long> getServicesIds() {
        return servicesIds;
    }

    public void setServicesIds(Set<Long> servicesIds) {
        this.servicesIds = servicesIds;
    }
}
