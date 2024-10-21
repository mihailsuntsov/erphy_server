/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dokio.model.Sprav.SpravTypePrices;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="departments")
public class Departments {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="departments_id_seq", sequenceName="departments_id_seq", allocationSize=1)
    @GeneratedValue(generator="departments_id_seq")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "additional")
    private String additional;

    @Column(name = "address")
    private String address;

    @Column(name = "boxoffice_id")
    private Long boxoffice_id;

    @Column(name = "payment_account_id")
    private Long payment_account_id;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Departments parent;

    @Column(name = "is_deleted")
    private Boolean is_deleted;

    @ManyToOne
    @JoinColumn(name = "price_id")
    private SpravTypePrices priceType;//основной тип цены, используемый в данном отделении

    @ManyToMany(mappedBy="departments")
    private Set<User> users = new HashSet<User>();

    @Column(name="display_in_online_scheduling")
    private Boolean display_in_online_scheduling;

    public Boolean getDisplay_in_online_scheduling() {
        return display_in_online_scheduling;
    }

    public void setDisplay_in_online_scheduling(Boolean display_in_online_scheduling) {
        this.display_in_online_scheduling = display_in_online_scheduling;
    }

    public SpravTypePrices getPriceType() {
        return priceType;
    }

    public void setPriceType(SpravTypePrices priceType) {
        this.priceType = priceType;
    }

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

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Companies getCompany() {
        return company;
    }

    public Long getBoxoffice_id() {
        return boxoffice_id;
    }

    public void setBoxoffice_id(Long boxoffice_id) {
        this.boxoffice_id = boxoffice_id;
    }

    public Long getPayment_account_id() {
        return payment_account_id;
    }

    public void setPayment_account_id(Long payment_account_id) {
        this.payment_account_id = payment_account_id;
    }

    public void setCompany(Companies company) {
        this.company = company;
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

    public Departments getParent() {
        return parent;
    }

    public void setParent(Departments parent) {
        this.parent = parent;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
