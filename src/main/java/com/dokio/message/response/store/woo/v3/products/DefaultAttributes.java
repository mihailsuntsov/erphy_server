package com.dokio.message.response.store.woo.v3.products;

// Defaults variation attributes.

public class DefaultAttributes {

    private int     woo_attribute_id;   // Attribute ID in WooCommerce.
    private Long    crm_attribute_id;   // Attribute ID in DokioCRM.
    private String  name;               // Attribute name.
    private String  option;             // Selected attribute term name.

    public int getWoo_attribute_id() {
        return woo_attribute_id;
    }

    public void setWoo_attribute_id(int woo_attribute_id) {
        this.woo_attribute_id = woo_attribute_id;
    }

    public Long getCrm_attribute_id() {
        return crm_attribute_id;
    }

    public void setCrm_attribute_id(Long crm_attribute_id) {
        this.crm_attribute_id = crm_attribute_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
