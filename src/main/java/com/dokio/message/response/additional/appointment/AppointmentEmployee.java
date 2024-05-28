package com.dokio.message.response.additional.appointment;

import java.util.List;

public class AppointmentEmployee {

    private Long id;                            // Employee's ID
    private String name;                        // Employee's name
    private Long jobtitle_id;                   // Job title ID of employee
    private List<DepartmentPartWithServicesIds> departmentPartsWithServicesIds; // Department parts, whose services coincide with the services of the employee.
                                                                                // It contains the set of these services
    private String state;                       // "free" / "busyByAppointments" / "busyBySchedule"

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public Long getJobtitle_id() {
        return jobtitle_id;
    }

    public void setJobtitle_id(Long jobtitle_id) {
        this.jobtitle_id = jobtitle_id;
    }

    public List<DepartmentPartWithServicesIds> getDepartmentPartsWithServicesIds() {
        return departmentPartsWithServicesIds;
    }

    public void setDepartmentPartsWithServicesIds(List<DepartmentPartWithServicesIds> departmentPartsWithServicesIds) {
        this.departmentPartsWithServicesIds = departmentPartsWithServicesIds;
    }
}
