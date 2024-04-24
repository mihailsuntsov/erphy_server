package com.dokio.message.response.additional.eployeescdl;

import com.dokio.message.response.additional.DepartmentsWithPartsJSON;
import com.dokio.message.response.additional.IdAndNameJSON;

import java.util.List;

public class EmployeeInfo {

    private Long id;
    private String name;            // Employee's name
    private String photo_link;      // link to the picture of employee
    private String jobtitle;        // Job title of employee
    private Boolean is_currently_employed;
    private Long cagent_id;
    private List<DepartmentsWithPartsJSON> departments_with_parts;
    private List<IdAndNameJSON> employee_services;
    private List<FreeTimeSlot> freeTimeSlots;  // free time slot


}
