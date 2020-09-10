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
import com.dokio.model.Geo.Cities;
import com.dokio.model.Geo.Countries;
import com.dokio.model.Geo.Regions;
import com.dokio.model.Sprav.SpravStatusDocks;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="customers_orders")
public class CustomersOrders {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="customers_orders_id_seq", sequenceName="customers_orders_id_seq", allocationSize=1)
    @GeneratedValue(generator="customers_orders_id_seq")
    private Long id;

    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "cagent_id", nullable = false)//заказчик
    private Cagents cagent;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @Column(name = "doc_number")//номер документа
    private Long doc_number;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @Column(name = "nds")
    private Boolean nds;

    @Column(name = "nds_included")
    private Boolean nds_included;

    @Column(name="shipment_date")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)// Предполагаемая дата отгрузки
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date shipment_date;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name = "is_completed")//Отгрузка завершено
    private Boolean is_completed;

    @Column(name = "is_archive")//Удалён (возаможно только для незавершенных документов, т.е. где is_completed = false)
    private Boolean is_archive;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "customers_orders_product",
            joinColumns = @JoinColumn(name = "customers_orders_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Products> products = new HashSet<>();


    @Size(max = 120)
    @Column(name = "name")
    private String name;

    @Size(max = 120)
    @Column(name = "fio")
    private String fio;

    @Size(max = 254)
    @Column(name = "email")
    private String email;

    @Size(max = 60)
    @Column(name = "telephone")
    private String telephone;

    @Column(name = "zip_code")
    private Integer zip_code;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Countries country;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Regions region;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private Cities city;

    @Size(max = 240)
    @Column(name = "additional_address")
    private String additional_address;

    @Size(max = 30)
    @Column(name = "track_number")
    private String track_number;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private SpravStatusDocks status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public Cagents getCagent() {
        return cagent;
    }

    public void setCagent(Cagents cagent) {
        this.cagent = cagent;
    }

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
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

    public Boolean getNds() {
        return nds;
    }

    public void setNds(Boolean nds) {
        this.nds = nds;
    }

    public Boolean getNds_included() {
        return nds_included;
    }

    public void setNds_included(Boolean nds_included) {
        this.nds_included = nds_included;
    }

    public Date getShipment_date() {
        return shipment_date;
    }

    public void setShipment_date(Date shipment_date) {
        this.shipment_date = shipment_date;
    }

    public Timestamp getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(Timestamp date_time_created) {
        this.date_time_created = date_time_created;
    }

    public Timestamp getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(Timestamp date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Integer getZip_code() {
        return zip_code;
    }

    public void setZip_code(Integer zip_code) {
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

    public String getAdditional_address() {
        return additional_address;
    }

    public void setAdditional_address(String additional_address) {
        this.additional_address = additional_address;
    }

    public String getTrack_number() {
        return track_number;
    }

    public void setTrack_number(String track_number) {
        this.track_number = track_number;
    }

    public SpravStatusDocks getStatus() {
        return status;
    }

    public void setStatus(SpravStatusDocks status) {
        this.status = status;
    }
}
