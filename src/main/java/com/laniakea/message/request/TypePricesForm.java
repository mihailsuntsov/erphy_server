package com.laniakea.message.request;

public class TypePricesForm {

    private Long id;
    private String company_id;
    private String master_id;
    private String creator_id;
    private String changer_id;
    private String name;
    private String description;
    private String pricerole_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPricerole_id() {
        return pricerole_id;
    }

    public void setPricerole_id(String pricerole_id) {
        this.pricerole_id = pricerole_id;
    }
}
