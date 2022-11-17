package com.dokio.message.response.additional;

import java.util.List;

public class ProductProductAttributeJSON {
    private String name;
    private List<ProductAttributeTermsJSON> terms_list;
    private boolean visible;      //    Define if the attribute is visible on the "Additional information" tab in the product's page. Default is false.
    private boolean variation;    //    Define if the attribute can be used as variation. Default is false.

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductAttributeTermsJSON> getTerms_list() {
        return terms_list;
    }

    public void setTerms_list(List<ProductAttributeTermsJSON> terms_list) {
        this.terms_list = terms_list;
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
