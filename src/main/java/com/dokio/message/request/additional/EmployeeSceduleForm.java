package com.dokio.message.request.additional;

import java.util.List;

public class EmployeeSceduleForm {

    private Long companyId;
    private String dateFrom;
    private String dateTo;
    private List<Long> departments;
    private List<Long> jobtitles;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public List<Long> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Long> departments) {
        this.departments = departments;
    }

    public List<Long> getJobtitles() {
        return jobtitles;
    }

    public void setJobtitles(List<Long> jobtitles) {
        this.jobtitles = jobtitles;
    }

    @Override
    public String toString() {
        return "EmployeeSceduleForm{" +
                "companyId=" + companyId +
                ", dateFrom='" + dateFrom + '\'' +
                ", dateTo='" + dateTo + '\'' +
                ", departments=" + departments +
                ", jobtitles=" + jobtitles +
                '}';
    }
}
