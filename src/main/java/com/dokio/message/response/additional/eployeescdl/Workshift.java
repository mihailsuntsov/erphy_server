package com.dokio.message.response.additional.eployeescdl;

import com.dokio.message.response.additional.IdAndNameJSON;

import java.util.List;

public class Workshift {

    private Long                id;
//    private List<IdAndNameJSON> depparts;                 // set of department parts
    private List<Long>          depparts;                   // set of department parts
    private String              time_from;                  // time of work shift start
    private String              time_to;                    // time of work shift end
    private List<Break>         breaks;                     // breaks

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public List<IdAndNameJSON> getDepparts() {
//        return depparts;
//    }

//    public void setDepparts(List<IdAndNameJSON> depparts) {
//        this.depparts = depparts;
//    }

    public List<Long> getDepparts() {
        return depparts;
    }

    public void setDepparts(List<Long> depparts) {
        this.depparts = depparts;
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

    public List<Break> getBreaks() {
        return breaks;
    }

    public void setBreaks(List<Break> breaks) {
        this.breaks = breaks;
    }
}
