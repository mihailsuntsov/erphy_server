package com.dokio.message.response.Sprav;

public class SpravSysRemindersJSON {

    private int id;
    private String type;
    private String value_text;
    private int value_seconds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue_text() {
        return value_text;
    }

    public void setValue_text(String value_text) {
        this.value_text = value_text;
    }

    public int getValue_seconds() {
        return value_seconds;
    }

    public void setValue_seconds(int value_seconds) {
        this.value_seconds = value_seconds;
    }
}
