package com.dokio.message.response.onlineScheduling;

public class EmployeeWorkshiftDeppart {

    private Long employeeId;
    private Long workshiftId;
    private Long deppartId;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getWorkshiftId() {
        return workshiftId;
    }

    public void setWorkshiftId(Long workshiftId) {
        this.workshiftId = workshiftId;
    }

    public Long getDeppartId() {
        return deppartId;
    }

    public void setDeppartId(Long deppartId) {
        this.deppartId = deppartId;
    }
}
