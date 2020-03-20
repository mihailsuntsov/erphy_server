package com.laniakea.message.response.Sprav;

public class SpravSysTimeZonesJSON {
    private Long id;
    private String time_offset;
    private String canonical_id;
    private String name_rus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime_offset() {
        return time_offset;
    }

    public void setTime_offset(String time_offset) {
        this.time_offset = time_offset;
    }

    public String getCanonical_id() {
        return canonical_id;
    }

    public void setCanonical_id(String Canonical_id) {
        this.canonical_id = canonical_id;
    }

    public String getName_rus() {
        return name_rus;
    }

    public void setName_rus(String name_rus) {
        this.name_rus = name_rus;
    }
}
