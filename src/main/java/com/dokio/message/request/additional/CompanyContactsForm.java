package com.dokio.message.request.additional;

public class CompanyContactsForm {

    private Long    id;
    private Long    master_id;
    private Long    company_id;
    private String  additional;     // eg. "Sales manager telephone"
    private String  contact_type;   //instagram/youtube/email/telephone
    private String  contact_value;  //  eg. https://www.instagram.com/msuntsov
    private Boolean display_in_os;  // where display this contact in Online scheduling
    private String  location_os;    // vertical/horizontal
    private Integer output_order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getContact_type() {
        return contact_type;
    }

    public void setContact_type(String contact_type) {
        this.contact_type = contact_type;
    }

    public String getContact_value() {
        return contact_value;
    }

    public void setContact_value(String contact_value) {
        this.contact_value = contact_value;
    }

    public Boolean getDisplay_in_os() {
        return display_in_os;
    }

    public void setDisplay_in_os(Boolean display_in_os) {
        this.display_in_os = display_in_os;
    }

    public String getLocation_os() {
        return location_os;
    }

    public void setLocation_os(String location_os) {
        this.location_os = location_os;
    }

    public Integer getOutput_order() {
        return output_order;
    }

    public void setOutput_order(Integer output_order) {
        this.output_order = output_order;
    }
}
