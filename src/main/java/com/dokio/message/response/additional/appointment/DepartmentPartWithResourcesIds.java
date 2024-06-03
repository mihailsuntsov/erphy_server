package com.dokio.message.response.additional.appointment;

import java.util.Set;

public class DepartmentPartWithResourcesIds {
    private Long id;
    private String name;
    private Set<ResourceOfDepartmentPart> resourcesOfDepartmentPart;

    public DepartmentPartWithResourcesIds() {
    }

    public DepartmentPartWithResourcesIds(Long id, String name) {
        this.id = id;
        this.name = name;
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

    public Set<ResourceOfDepartmentPart> getResourcesOfDepartmentPart() {
        return resourcesOfDepartmentPart;
    }

    public void setResourcesOfDepartmentPart(Set<ResourceOfDepartmentPart> resourcesOfDepartmentPart) {
        this.resourcesOfDepartmentPart = resourcesOfDepartmentPart;
    }
}
