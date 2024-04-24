package com.dokio.message.response.additional.calendar;

public class Meta {

    private CalendarUser user;
    private String docType;
//    private Boolean isBreak;

    public Meta() {
    }

    public Meta(CalendarUser user, String docType) {
        this.user = user;
        this.docType = docType;
//        this.isBreak = isBreak;
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

//    public Boolean getBreak() {
//        return isBreak;
//    }

//    public void setBreak(Boolean aBreak) {
//        isBreak = aBreak;
//    }
}
