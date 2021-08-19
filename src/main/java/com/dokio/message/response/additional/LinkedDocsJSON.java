package com.dokio.message.response.additional;

//отправка информации по связанным дочерним документам
public class LinkedDocsJSON {

    private Long        id;
    private Long        doc_number;
    private String      date_time_created;
    private String      description;
    private boolean     is_completed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
    }
}
