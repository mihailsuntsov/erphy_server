package com.dokio.message.response.additional;

import java.util.List;

public class ProductProductAttributeJSON {
    private Long attribute_id;
    private String name;
    private List<ProductAttributeTermsJSON> terms;
    private boolean visible;      //    Define if the attribute is visible on the "Additional information" tab in the product's page. Default is false.
    private boolean variation;    //    Define if the attribute can be used as variation. Default is false.

    public Long getAttribute_id() {
        return attribute_id;
    }

    public void setAttribute_id(Long attribute_id) {
        this.attribute_id = attribute_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductAttributeTermsJSON> getTerms() {
        return terms;
    }

    public void setTerms(List<ProductAttributeTermsJSON> terms) {
        this.terms = terms;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVariation() {
        return variation;
    }

    public void setVariation(boolean variation) {
        this.variation = variation;
    }
}
