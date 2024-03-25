package com.dokio.message.response.additional.eployeescdl;

public class Break {

    private Long    id;
    private String  time_from;          // time of work shift start
    private String  time_to;            // time of work shift end
    private Boolean paid;               // is break paid by employer
    private int     precent;            // 1-100

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime_from() {
        return time_from;
    }

    public void setTime_from(String time_from) {
        this.time_from = time_from;
    }

    public String getTime_to() {
        return time_to;
    }

    public void setTime_to(String time_to) {
        this.time_to = time_to;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public int getPrecent() {
        return precent;
    }

    public void setPrecent(int precent) {
        this.precent = precent;
    }
}
