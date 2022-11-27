package com.dokio.message.response.Sprav;

import java.util.List;

public class ProductAttributesListJSON {
    private Long id;
    private String name;
    private List<ProductAttributeTermJSON> terms;

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

    public List<ProductAttributeTermJSON> getTerms() {
        return terms;
    }

    public void setTerms(List<ProductAttributeTermJSON> terms) {
        this.terms = terms;
    }
}
