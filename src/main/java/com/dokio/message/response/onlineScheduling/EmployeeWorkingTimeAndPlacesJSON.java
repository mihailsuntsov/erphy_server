package com.dokio.message.response.onlineScheduling;

import java.util.List;

public class EmployeeWorkingTimeAndPlacesJSON {
    private String          start;                  // time in format 2024-04-11T02:00:00.000Z
    private String          end;                    // time in format 2024-04-11T08:20:41.258Z",
    private Long            workshift_id;           // ID of work shift
    private Long            employee_id;            // ID of the employee who works the work shift
    private List<Long>      department_parts_ids;   // IDs of department parts, where the work shift takes place

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Long getWorkshift_id() {
        return workshift_id;
    }

    public void setWorkshift_id(Long workshift_id) {
        this.workshift_id = workshift_id;
    }

    public Long getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(Long employee_id) {
        this.employee_id = employee_id;
    }

    public List<Long> getDepartment_parts_ids() {
        return department_parts_ids;
    }

    public void setDepartment_parts_ids(List<Long> department_parts_ids) {
        this.department_parts_ids = department_parts_ids;
    }
}
