/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
//Класс для преобразования CRUD-запросов в объект
package com.dokio.message.request;

import java.util.Set;

public class CompaniesForm {
    private Long id;
    private Integer currency_id;
    private Integer opf_id;
    private String checked;//для удаления
    // Апдейт Предприятия:
    private String name;
    private String code;
    private String telephone;
    private String site;
    private String email;
    private String zip_code;
    private Integer country_id;
    private Integer region_id;
    private Integer city_id;
    private String street;
    private String home;
    private String flat;
    private String additional_address;
    private Long status_id;
    private String jr_jur_full_name;
    private String jr_jur_kpp;
    private String jr_jur_ogrn;
    private String jr_zip_code;
    private Integer jr_country_id;
    private Integer jr_region_id;
    private Integer jr_city_id;
    private String jr_street;
    private String jr_home;
    private String jr_flat;
    private String jr_additional_address;
    private String jr_inn;
    private String jr_okpo;
    private String jr_fio_family;
    private String jr_fio_name;
    private String jr_fio_otchestvo;
    private String jr_ip_ogrnip;
    private String jr_ip_svid_num; // string т.к. оно может быть типа "серия 77 №42343232"
    private String jr_ip_reg_date;
    private Set<CompaniesPaymentAccountsForm> companiesPaymentAccountsTable;//банковские счета
    private Boolean nds_payer;
    private String fio_director;
    private String director_position;
    private String fio_glavbuh;
    private Long director_signature_id;
    private Long glavbuh_signature_id;
    private Long stamp_id;
    private Long card_template_id;

    public Long getCard_template_id() {
        return card_template_id;
    }

    public void setCard_template_id(Long card_template_id) {
        this.card_template_id = card_template_id;
    }

    public Long getStamp_id() {
        return stamp_id;
    }

    public void setStamp_id(Long stamp_id) {
        this.stamp_id = stamp_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(Integer currency_id) {
        this.currency_id = currency_id;
    }

    public Integer getOpf_id() {
        return opf_id;
    }

    public void setOpf_id(Integer opf_id) {
        this.opf_id = opf_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public Integer getCountry_id() {
        return country_id;
    }

    public void setCountry_id(Integer country_id) {
        this.country_id = country_id;
    }

    public Integer getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Integer region_id) {
        this.region_id = region_id;
    }

    public Integer getCity_id() {
        return city_id;
    }

    public void setCity_id(Integer city_id) {
        this.city_id = city_id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getAdditional_address() {
        return additional_address;
    }

    public void setAdditional_address(String additional_address) {
        this.additional_address = additional_address;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getJr_jur_full_name() {
        return jr_jur_full_name;
    }

    public void setJr_jur_full_name(String jr_jur_full_name) {
        this.jr_jur_full_name = jr_jur_full_name;
    }

    public String getJr_zip_code() {
        return jr_zip_code;
    }

    public void setJr_zip_code(String jr_zip_code) {
        this.jr_zip_code = jr_zip_code;
    }

    public Integer getJr_country_id() {
        return jr_country_id;
    }

    public void setJr_country_id(Integer jr_country_id) {
        this.jr_country_id = jr_country_id;
    }

    public Integer getJr_region_id() {
        return jr_region_id;
    }

    public void setJr_region_id(Integer jr_region_id) {
        this.jr_region_id = jr_region_id;
    }

    public Integer getJr_city_id() {
        return jr_city_id;
    }

    public void setJr_city_id(Integer jr_city_id) {
        this.jr_city_id = jr_city_id;
    }

    public String getJr_street() {
        return jr_street;
    }

    public void setJr_street(String jr_street) {
        this.jr_street = jr_street;
    }

    public String getJr_home() {
        return jr_home;
    }

    public void setJr_home(String jr_home) {
        this.jr_home = jr_home;
    }

    public String getJr_flat() {
        return jr_flat;
    }

    public void setJr_flat(String jr_flat) {
        this.jr_flat = jr_flat;
    }

    public String getJr_additional_address() {
        return jr_additional_address;
    }

    public void setJr_additional_address(String jr_additional_address) {
        this.jr_additional_address = jr_additional_address;
    }

    public String getJr_fio_family() {
        return jr_fio_family;
    }

    public void setJr_fio_family(String jr_fio_family) {
        this.jr_fio_family = jr_fio_family;
    }

    public String getJr_fio_name() {
        return jr_fio_name;
    }

    public void setJr_fio_name(String jr_fio_name) {
        this.jr_fio_name = jr_fio_name;
    }

    public String getJr_fio_otchestvo() {
        return jr_fio_otchestvo;
    }

    public void setJr_fio_otchestvo(String jr_fio_otchestvo) {
        this.jr_fio_otchestvo = jr_fio_otchestvo;
    }

    public String getJr_ip_svid_num() {
        return jr_ip_svid_num;
    }

    public void setJr_ip_svid_num(String jr_ip_svid_num) {
        this.jr_ip_svid_num = jr_ip_svid_num;
    }

    public String getJr_ip_reg_date() {
        return jr_ip_reg_date;
    }

    public void setJr_ip_reg_date(String jr_ip_reg_date) {
        this.jr_ip_reg_date = jr_ip_reg_date;
    }

    public Set<CompaniesPaymentAccountsForm> getCompaniesPaymentAccountsTable() {
        return companiesPaymentAccountsTable;
    }

    public void setCompaniesPaymentAccountsTable(Set<CompaniesPaymentAccountsForm> companiesPaymentAccountsTable) {
        this.companiesPaymentAccountsTable = companiesPaymentAccountsTable;
    }

    public Boolean getNds_payer() {
        return nds_payer;
    }

    public void setNds_payer(Boolean nds_payer) {
        this.nds_payer = nds_payer;
    }

    public String getFio_director() {
        return fio_director;
    }

    public void setFio_director(String fio_director) {
        this.fio_director = fio_director;
    }

    public String getDirector_position() {
        return director_position;
    }

    public void setDirector_position(String director_position) {
        this.director_position = director_position;
    }

    public String getFio_glavbuh() {
        return fio_glavbuh;
    }

    public void setFio_glavbuh(String fio_glavbuh) {
        this.fio_glavbuh = fio_glavbuh;
    }

    public Long getDirector_signature_id() {
        return director_signature_id;
    }

    public void setDirector_signature_id(Long director_signature_id) {
        this.director_signature_id = director_signature_id;
    }

    public String getJr_jur_kpp() {
        return jr_jur_kpp;
    }

    public void setJr_jur_kpp(String jr_jur_kpp) {
        this.jr_jur_kpp = jr_jur_kpp;
    }

    public String getJr_jur_ogrn() {
        return jr_jur_ogrn;
    }

    public void setJr_jur_ogrn(String jr_jur_ogrn) {
        this.jr_jur_ogrn = jr_jur_ogrn;
    }

    public String getJr_inn() {
        return jr_inn;
    }

    public void setJr_inn(String jr_inn) {
        this.jr_inn = jr_inn;
    }

    public String getJr_okpo() {
        return jr_okpo;
    }

    public void setJr_okpo(String jr_okpo) {
        this.jr_okpo = jr_okpo;
    }

    public String getJr_ip_ogrnip() {
        return jr_ip_ogrnip;
    }

    public void setJr_ip_ogrnip(String jr_ip_ogrnip) {
        this.jr_ip_ogrnip = jr_ip_ogrnip;
    }

    public Long getGlavbuh_signature_id() {
        return glavbuh_signature_id;
    }

    public void setGlavbuh_signature_id(Long glavbuh_signature_id) {
        this.glavbuh_signature_id = glavbuh_signature_id;
    }

    @Override
    public String toString() {
        return "CompaniesForm: id=" + this.id + ", currency_id=" + this.currency_id +
                ", opf_id=" + this.opf_id + ", name=" + this.name + ", checked=" + this.checked;
    }
}
