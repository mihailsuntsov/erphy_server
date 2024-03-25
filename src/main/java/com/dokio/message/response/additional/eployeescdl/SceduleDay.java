package com.dokio.message.response.additional.eployeescdl;

public class SceduleDay {
    private Long      id;
    private Boolean   is_changed;      // only days with is_changed = true will be saved in database
    private String    name;            // day name in format DDMMYYYY like '25042024'
    private String    date;            // day date in format DD.MM.YYYY like '25.04.2024'
    private Workshift workshift;       // contains information if day type is 'workshift'
    private Vacation  vacation;        // contains information if day type is 'vacation'

    public Boolean getIs_changed() {
        return is_changed;
    }

    public void setIs_changed(Boolean is_changed) {
        this.is_changed = is_changed;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Workshift getWorkshift() {
        return workshift;
    }

    public void setWorkshift(Workshift workshift) {
        this.workshift = workshift;
    }

    public Vacation getVacation() {
        return vacation;
    }

    public void setVacation(Vacation vacation) {
        this.vacation = vacation;
    }
}
