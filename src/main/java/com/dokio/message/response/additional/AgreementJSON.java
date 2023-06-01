package com.dokio.message.response.additional;

public class AgreementJSON {

    private int    id;
    private String type;
    private String version;
    private String version_date;
    private String name;
    private String text;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion_date() {
        return version_date;
    }

    public void setVersion_date(String version_date) {
        this.version_date = version_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
