package com.dokio.message.response.additional.calendar;

import java.util.Set;

public class Meta {

    private CalendarUser user;
    private String docType;
    private Long departmentPartId;
    private Set<ItemResource> itemResources;

    public Meta() {
    }

    public Meta(CalendarUser user, String docType) {
        this.user = user;
        this.docType = docType;
    }

    public Meta(CalendarUser user, String docType, Long departmentPartId, Set<ItemResource> itemResources) {
        this.user = user;
        this.docType = docType;
        this.departmentPartId = departmentPartId;
        this.itemResources = itemResources;
    }

    public CalendarUser getUser() {
        return user;
    }

    public void setUser(CalendarUser user) {
        this.user = user;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Long getDepartmentPartId() {
        return departmentPartId;
    }

    public void setDepartmentPartId(Long departmentPartId) {
        this.departmentPartId = departmentPartId;
    }

    public Set<ItemResource> getItemResources() {
        return itemResources;
    }

    public void setItemResources(Set<ItemResource> itemResources) {
        this.itemResources = itemResources;
    }
}
