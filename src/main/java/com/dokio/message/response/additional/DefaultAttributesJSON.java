package com.dokio.message.response.additional;

public class DefaultAttributesJSON {

    private Long    attribute_id;   // Attribute ID in DokioCRM.
    private String  name;           // Attribute name.
    private Long    term_id;        // Selected attribute term id.
    private String  term;           // Selected attribute term name.

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

    public Long getTerm_id() {
        return term_id;
    }

    public void setTerm_id(Long term_id) {
        this.term_id = term_id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
