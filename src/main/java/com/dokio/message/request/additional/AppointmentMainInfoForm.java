package com.dokio.message.request.additional;

import java.util.Set;

public class AppointmentMainInfoForm {

    private String searchString;
    private Boolean isFree;       // is employee free? (for getting employees list)
    private String kindOfNoFree;  // employees busyByAppointments or busyBySchedule (for getting employees list)
    private Long appointmentId;   // parent document's ID
    private Long employeeId;      // ID of employee (for getting services list)
    private Long companyId;
    private String dateFrom;
    private String timeFrom;
    private String dateTo;
    private String timeTo;
    private Set<Long> servicesIds;
    private Set<Long> depPartsIds;
    private Set<Long> jobTitlesIds;

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Boolean getFree() {
        return isFree;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setFree(Boolean free) {
        isFree = free;
    }

    public String getKindOfNoFree() {
        return kindOfNoFree;
    }

    public void setKindOfNoFree(String kindOfNoFree) {
        this.kindOfNoFree = kindOfNoFree;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

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

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public Set<Long> getServicesIds() {
        return servicesIds;
    }

    public void setServicesIds(Set<Long> servicesIds) {
        this.servicesIds = servicesIds;
    }

    public Set<Long> getDepPartsIds() {
        return depPartsIds;
    }

    public void setDepPartsIds(Set<Long> depPartsIds) {
        this.depPartsIds = depPartsIds;
    }

    public Set<Long> getJobTitlesIds() {
        return jobTitlesIds;
    }

    public void setJobTitlesIds(Set<Long> jobTitlesIds) {
        this.jobTitlesIds = jobTitlesIds;
    }
}
