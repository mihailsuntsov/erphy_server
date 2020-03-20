package com.laniakea.message.request.Sprav;

public class SpravSysEdizmForm {

    private Long id;
    private String name;
    private String short_name;
    private String type_id;
    private String equals_si;
    private String company_id;
    private String master_id;
    private String creator_id;
    private String changer_id;

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

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_) {
        this.type_id = type_;
    }

    public String getEquals_si() {
        return equals_si;
    }

    public void setEquals_si(String equals_si) {
        this.equals_si = equals_si;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getMaster_id() {
        return master_id;
    }

    public void setMaster_id(String master_id) {
        this.master_id = master_id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(String changer_id) {
        this.changer_id = changer_id;
    }
}
