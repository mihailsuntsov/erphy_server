package com.dokio.message.response.additional.calendar;

import java.util.List;

public class BreakJSON {

    private CalendarUser    user;
    private String          start;                  // time in format 2024-04-11T02:00:00.000Z
    private String          end;                    // time in format 2024-04-11T08:20:41.258Z",
    private Long            workshift_id;           // ID of work shift, started at the end of break

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

    public Long getWorkshift_id() {
        return workshift_id;
    }

    public void setWorkshift_id(Long workshift_id) {
        this.workshift_id = workshift_id;
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
