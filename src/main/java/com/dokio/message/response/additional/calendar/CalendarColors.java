package com.dokio.message.response.additional.calendar;

public class CalendarColors {

    private String primary;
    private String secondary;

    public CalendarColors(String primary, String secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public CalendarColors() {
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public void setSecondary(String secondary) {
        this.secondary = secondary;
    }
}
