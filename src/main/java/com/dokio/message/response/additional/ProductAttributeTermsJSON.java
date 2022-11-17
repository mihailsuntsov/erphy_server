package com.dokio.message.response.additional;

public class ProductAttributeTermsJSON {
    private Long id;
    private String name;
    private Boolean is_selected;// true when product's attribute contain this term

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

    public Boolean getIs_selected() {
        return is_selected;
    }

    public void setIs_selected(Boolean is_selected) {
        this.is_selected = is_selected;
    }
}
