package com.dokio.message.response.additional.eployeescdl;

public class FreeTimeSlot {

    private int             index;      // 0,1,...
    private String          start;      // start time of free time slot
    private String          end;        // end time of free time slot
    private Integer         duration;   // duration of free time slot

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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
