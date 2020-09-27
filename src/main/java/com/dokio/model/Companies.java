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
package com.dokio.model;

/*import com.dokio.model.Geo.Cities;
import com.dokio.model.Geo.Countries;
import com.dokio.model.Geo.Regions;
import com.dokio.model.Sprav.SpravStatusDocks;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dokio.model.Sprav.SpravSysOPF;*/

import javax.persistence.*;
/*import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;*/

@Entity
@Table(name="companies")
public class Companies {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "companies_id_seq", sequenceName = "companies_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "companies_id_seq")
    private Long compId;

 /*   @Column(name = "name")
    private String compName;*/

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;
/*
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @Column(name="date_time_created")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp compDateTimeCreated;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp compDateTimeChanged;

    @ManyToOne
    @JoinColumn(name = "opf_id")
    private SpravSysOPF compOpf;

    @Column(name="st_prefix_barcode_pieced")
    private Integer st_prefix_barcode_pieced;

    @Column(name="st_prefix_barcode_packed")
    private Integer st_prefix_barcode_packed;

    @Column(name="currency_id")
    private Integer currency_id;

// Апдейт Предприятий:

    @Column(name = "code")
    @Size(max = 30)
    private String code;

    @Column(name = "telephone")
    @Size(max = 60)
    private String telephone;

    @Column(name = "site")
    @Size(max = 120)
    private String site;

    @Column(name = "email")
    @Size(max = 254)
    private String email;

    @Column(name = "zip_code")
    @Size(max = 40)
    private String zip_code;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Countries country;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Regions region;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private Cities city;

    @Column(name = "street")
    @Size(max = 128)
    private String street;

    @Column(name = "home")
    @Size(max = 16)
    private String home;

    @Column(name = "flat")
    @Size(max = 8)
    private String flat;

    @Column(name = "additional_address")
    @Size(max = 240)
    private String additional_address;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private SpravStatusDocks status;

    @Column(name = "jr_jur_full_name")
    @Size(max = 512)
    private String jr_jur_full_name;

    @Column(name = "jr_jur_kpp")
    private Long jr_jur_kpp;

    @Column(name = "jr_jur_ogrn")
    private Long jr_jur_ogrn;

    @Column(name = "jr_zip_code")
    @Size(max = 40)
    private String jr_zip_code;

    @ManyToOne
    @JoinColumn(name = "jr_country_id")
    private Countries jr_country;

    @ManyToOne
    @JoinColumn(name = "jr_region_id")
    private Regions jr_region;

    @ManyToOne
    @JoinColumn(name = "jr_city_id")
    private Cities jr_city;

    @Column(name = "jr_street")
    @Size(max = 128)
    private String jr_street;

    @Column(name = "jr_home")
    @Size(max = 16)
    private String jr_home;

    @Column(name = "jr_flat")
    @Size(max = 8)
    private String jr_flat;

    @Column(name = "jr_additional_address")
    @Size(max = 240)
    private String jr_additional_address;

    @Column(name = "jr_inn")
    private Long jr_inn;

    @Column(name = "jr_okpo")
    private Long jr_okpo;

    @Column(name = "jr_fio_family")
    @Size(max = 127)
    private String jr_fio_family;

    @Column(name = "jr_fio_name")
    @Size(max = 127)
    private String jr_fio_name;

    @Column(name = "jr_fio_otchestvo")
    @Size(max = 127)
    private String jr_fio_otchestvo;

    @Column(name = "jr_ip_ogrnip")
    private Long jr_ip_ogrnip;

    @Column(name = "jr_ip_svid_num")
    @Size(max = 30)
    private String jr_ip_svid_num;

    @Column(name="jr_ip_reg_date")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)// Дата регистрации ИП
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date jr_ip_reg_date;

    @Column(name = "nds_payer")
    private Boolean nds_payer;

    @Column(name = "fio_director")
    @Size(max = 127)
    private String fio_director;

    @Column(name = "director_position")
    @Size(max = 127)
    private String director_position;

    @Column(name = "fio_glavbuh")
    @Size(max = 127)
    private String fio_glavbuh;

    @Column(name = "director_signature_id")
    private Long director_signature_id;

    @Column(name = "glavbuh_signature_id")
    private Long glavbuh_signature_id;

    @Column(name = "stamp_id")
    private Long stamp_id;*/

 /*   public Long getStamp_id() {
        return stamp_id;
    }

    public void setStamp_id(Long stamp_id) {
        this.stamp_id = stamp_id;
    }*/

    public Long getCompId() {
        return compId;
    }

    public void setCompId(Long compId) {
        this.compId = compId;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }
/*    public Integer getSt_prefix_barcode_pieced() {
        return st_prefix_barcode_pieced;
    }

    public void setSt_prefix_barcode_pieced(Integer st_prefix_barcode_pieced) {
        this.st_prefix_barcode_pieced = st_prefix_barcode_pieced;
    }

    public Integer getSt_prefix_barcode_packed() {
        return st_prefix_barcode_packed;
    }

    public void setSt_prefix_barcode_packed(Integer st_prefix_barcode_packed) {
        this.st_prefix_barcode_packed = st_prefix_barcode_packed;
    }

    public Integer getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(Integer currency_id) {
        this.currency_id = currency_id;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getChanger() {
        return changer;
    }

    public void setChanger(User changer) {
        this.changer = changer;
    }

    public Timestamp getCompDateTimeCreated() {
        return compDateTimeCreated;
    }

    public void setCompDateTimeCreated(Timestamp compDateTimeCreated) {
        this.compDateTimeCreated = compDateTimeCreated;
    }

    public Timestamp getCompDateTimeChanged() {
        return compDateTimeChanged;
    }

    public void setCompDateTimeChanged(Timestamp compDateTimeChanged) {
        this.compDateTimeChanged = compDateTimeChanged;
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

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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

    public Countries getCountry() {
        return country;
    }

    public void setCountry(Countries country) {
        this.country = country;
    }

    public Regions getRegion() {
        return region;
    }

    public void setRegion(Regions region) {
        this.region = region;
    }

    public Cities getCity() {
        return city;
    }

    public void setCity(Cities city) {
        this.city = city;
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

    public SpravStatusDocks getStatus() {
        return status;
    }

    public void setStatus(SpravStatusDocks status) {
        this.status = status;
    }

    public String getJr_jur_full_name() {
        return jr_jur_full_name;
    }

    public void setJr_jur_full_name(String jr_jur_full_name) {
        this.jr_jur_full_name = jr_jur_full_name;
    }

    public Long getJr_jur_kpp() {
        return jr_jur_kpp;
    }

    public void setJr_jur_kpp(Long jr_jur_kpp) {
        this.jr_jur_kpp = jr_jur_kpp;
    }

    public Long getJr_jur_ogrn() {
        return jr_jur_ogrn;
    }

    public void setJr_jur_ogrn(Long jr_jur_ogrn) {
        this.jr_jur_ogrn = jr_jur_ogrn;
    }

    public String getJr_zip_code() {
        return jr_zip_code;
    }

    public void setJr_zip_code(String jr_zip_code) {
        this.jr_zip_code = jr_zip_code;
    }

    public Countries getJr_country() {
        return jr_country;
    }

    public void setJr_country(Countries jr_country) {
        this.jr_country = jr_country;
    }

    public Regions getJr_region() {
        return jr_region;
    }

    public void setJr_region(Regions jr_region) {
        this.jr_region = jr_region;
    }

    public Cities getJr_city() {
        return jr_city;
    }

    public void setJr_city(Cities jr_city) {
        this.jr_city = jr_city;
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

    public Long getJr_inn() {
        return jr_inn;
    }

    public void setJr_inn(Long jr_inn) {
        this.jr_inn = jr_inn;
    }

    public Long getJr_okpo() {
        return jr_okpo;
    }

    public void setJr_okpo(Long jr_okpo) {
        this.jr_okpo = jr_okpo;
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

    public Long getJr_ip_ogrnip() {
        return jr_ip_ogrnip;
    }

    public void setJr_ip_ogrnip(Long jr_ip_ogrnip) {
        this.jr_ip_ogrnip = jr_ip_ogrnip;
    }

    public String getJr_ip_svid_num() {
        return jr_ip_svid_num;
    }

    public void setJr_ip_svid_num(String jr_ip_svid_num) {
        this.jr_ip_svid_num = jr_ip_svid_num;
    }

    public Date getJr_ip_reg_date() {
        return jr_ip_reg_date;
    }

    public void setJr_ip_reg_date(Date jr_ip_reg_date) {
        this.jr_ip_reg_date = jr_ip_reg_date;
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

    public Long getGlavbuh_signature_id() {
        return glavbuh_signature_id;
    }

    public void setGlavbuh_signature_id(Long glavbuh_signature_id) {
        this.glavbuh_signature_id = glavbuh_signature_id;
    }

    public SpravSysOPF getCompOpf() {
        return compOpf;
    }

    public void setCompOpf(SpravSysOPF compOpf) {
        this.compOpf = compOpf;
    }*/
}