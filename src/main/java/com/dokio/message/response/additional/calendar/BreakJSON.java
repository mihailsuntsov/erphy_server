package com.dokio.message.response.additional.calendar;

public class BreakJSON {

    private CalendarUser user;
    private String start;           // time in format 2024-04-11T02:00:00.000Z
    private String end;             // time in format 2024-04-11T08:20:41.258Z",

    public BreakJSON() {
    }

    public BreakJSON(CalendarUser user, String start, String end) {
        this.user = user;
        this.start = start;
        this.end = end;
    }

    public BreakJSON(CalendarUser user, String start) {
        this.user = user;
        this.start = start;
    }


    public CalendarUser getUser() {
        return user;
    }

    public void setUser(CalendarUser user) {
        this.user = user;
    }

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
}
