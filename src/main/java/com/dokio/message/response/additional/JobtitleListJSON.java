package com.dokio.message.response.additional;

public class JobtitleListJSON {

    private Long   jobtitle_id;
    private String name;
    private String description;

    public Long getJobtitle_id() {
        return jobtitle_id;
    }

    public void setJobtitle_id(Long jobtitle_id) {
        this.jobtitle_id = jobtitle_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
