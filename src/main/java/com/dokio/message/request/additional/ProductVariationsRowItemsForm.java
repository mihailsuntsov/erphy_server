package com.dokio.message.request.additional;

public class ProductVariationsRowItemsForm {

    private Long variation_id;
    private Long attribute_id;
    private Long term_id;

    public Long getVariation_id() {
        return variation_id;
    }

    public void setVariation_id(Long variation_id) {
        this.variation_id = variation_id;
    }

    public Long getAttribute_id() {
        return attribute_id;
    }

    public void setAttribute_id(Long attribute_id) {
        this.attribute_id = attribute_id;
    }

    public Long getTerm_id() {
        return term_id;
    }

    public void setTerm_id(Long term_id) {
        this.term_id = term_id;
    }
}
