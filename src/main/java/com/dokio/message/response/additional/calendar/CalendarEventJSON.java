package com.dokio.message.response.additional.calendar;

public class CalendarEventJSON {

    private Long id;                // id of document
    private String start;           // time in format 2024-04-11T02:00:00.000Z
    private String end;             // time in format 2024-04-11T08:20:41.258Z",
    private String title;           // title of event
    private CalendarColors color;   // colors of event
    private Meta meta;              // Any info like user, type of document ('appointment', 'task') etc.

    public CalendarEventJSON(Long id, String start, String end, String title, CalendarColors color, Meta meta) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.title = title;
        this.color = color;
        this.meta = meta;
    }

    public CalendarEventJSON() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CalendarColors getColor() {
        return color;
    }

    public void setColor(CalendarColors color) {
        this.color = color;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}

