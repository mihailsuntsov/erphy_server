package com.dokio.message.request.additional;

public class DefaultAttributesForm {

    private Long    attribute_id;   // Attribute ID in DokioCRM.
    private Long    term_id;        // Selected attribute term id.

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
