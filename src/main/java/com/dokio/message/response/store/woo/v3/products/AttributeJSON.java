package com.dokio.message.response.store.woo.v3.products;

import java.util.List;

public class AttributeJSON {

    private Long            crm_id;
    private Integer         woo_id;
    private Integer         position;
    private Boolean         visible;
    private Boolean         variation;
    private List<String>    options;

    public Long getCrm_id() {
        return crm_id;
    }

    public void setCrm_id(Long crm_id) {
        this.crm_id = crm_id;
    }

    public Integer getWoo_id() {
        return woo_id;
    }

    public void setWoo_id(Integer woo_id) {
        this.woo_id = woo_id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getVariation() {
        return variation;
    }

    public void setVariation(Boolean variation) {
        this.variation = variation;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
