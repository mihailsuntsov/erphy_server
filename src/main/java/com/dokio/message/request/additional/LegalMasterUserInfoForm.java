package com.dokio.message.request.additional;

// this information needs to payments
public class LegalMasterUserInfoForm {

    private String jr_legal_form;
    private String jr_jur_name;
    private String jr_name;
    private String jr_surname;
    private int jr_country_id;
    private String jr_vat;

    public String getJr_legal_form() {
        return jr_legal_form;
    }

    public void setJr_legal_form(String jr_legal_form) {
        this.jr_legal_form = jr_legal_form;
    }

    public String getJr_jur_name() {
        return jr_jur_name;
    }

    public void setJr_jur_name(String jr_jur_name) {
        this.jr_jur_name = jr_jur_name;
    }

    public String getJr_name() {
        return jr_name;
    }

    public void setJr_name(String jr_name) {
        this.jr_name = jr_name;
    }

    public String getJr_surname() {
        return jr_surname;
    }

    public void setJr_surname(String jr_surname) {
        this.jr_surname = jr_surname;
    }

    public int getJr_country_id() {
        return jr_country_id;
    }

    public void setJr_country_id(int jr_country_id) {
        this.jr_country_id = jr_country_id;
    }

    public String getJr_vat() {
        return jr_vat;
    }

    public void setJr_vat(String jr_vat) {
        this.jr_vat = jr_vat;
    }

    @Override
    public String toString() {
        return "LegalMasterUserInfoForm{" +
                "jr_legal_form='" + jr_legal_form + '\'' +
                ", jr_jur_name='" + jr_jur_name + '\'' +
                ", jr_name='" + jr_name + '\'' +
                ", jr_surname='" + jr_surname + '\'' +
                ", jr_country_id=" + jr_country_id +
                ", jr_vat='" + jr_vat + '\'' +
                '}';
    }
}
