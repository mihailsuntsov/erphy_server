package com.dokio.message.request.additional.calendar;

import java.util.Set;

public class CalendarEventsQueryForm {

    private Long companyId;
    private String dateFrom;
    private String dateTo;
    private String timeFrom;
    private String timeTo;
    private Set<Long> depparts;
    private Set<Long> jobtitles;
    private Set<Long> employees;

    private Boolean withCancelledEvents;
    private Boolean ifNoEmployeesThenNoEvents;

    public CalendarEventsQueryForm(Long companyId, String dateFrom, String dateTo, Set<Long> depparts, Set<Long> jobtitles, Set<Long> employees) {
        this.companyId = companyId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.depparts = depparts;
        this.jobtitles = jobtitles;
        this.employees = employees;
    }

    public Boolean getWithCancelledEvents() {
        return withCancelledEvents;
    }

    public void setWithCancelledEvents(Boolean withCancelledEvents) {
        this.withCancelledEvents = withCancelledEvents;
    }

    public Boolean getIfNoEmployeesThenNoEvents() {
        return ifNoEmployeesThenNoEvents;
    }

    public void setIfNoEmployeesThenNoEvents(Boolean ifNoEmployeesThenNoEvents) {
        this.ifNoEmployeesThenNoEvents = ifNoEmployeesThenNoEvents;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public Set<Long> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Long> employees) {
        this.employees = employees;
    }

    public CalendarEventsQueryForm() {
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

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public Set<Long> getDepparts() {
        return depparts;
    }

    public void setDepparts(Set<Long> depparts) {
        this.depparts = depparts;
    }

    public Set<Long> getJobtitles() {
        return jobtitles;
    }

    public void setJobtitles(Set<Long> jobtitles) {
        this.jobtitles = jobtitles;
    }

//    public Set<Integer> getDocuments() {
//        return documents;
//    }
//
//    public void setDocuments(Set<Integer> documents) {
//        this.documents = documents;
//    }
}
