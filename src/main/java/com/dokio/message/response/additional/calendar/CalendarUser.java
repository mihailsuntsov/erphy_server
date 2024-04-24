package com.dokio.message.response.additional.calendar;

public class CalendarUser {

    private Long id;
    private String name;
    private CalendarColors color;

    public CalendarUser(Long id, String name, CalendarColors color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public CalendarUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CalendarColors getColor() {
        return color;
    }

    public void setColor(CalendarColors color) {
        this.color = color;
    }
}
