package com.dokio.message.response.additional.appointment;

import java.util.List;
import java.util.Set;

public class AppointmentService {

    private Long id;
    private String name;
    private Long departmentId;
    private List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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

    public List<DepartmentPartWithResourcesIds> getDepartmentPartsWithResourcesIds() {
        return departmentPartsWithResourcesIds;
    }

    public void setDepartmentPartsWithResourcesIds(List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds) {
        this.departmentPartsWithResourcesIds = departmentPartsWithResourcesIds;
    }
}
