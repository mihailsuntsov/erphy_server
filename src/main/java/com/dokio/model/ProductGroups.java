///*
//        Dokio CRM - server part. Sales, finance and warehouse management system
//        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU Affero General Public License as
//        published by the Free Software Foundation, either version 3 of the
//        License, or (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU Affero General Public License for more details.
//
//        You should have received a copy of the GNU Affero General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>
//*/
//
//package com.dokio.model;
//
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import javax.persistence.*;
//import javax.validation.constraints.Size;
//import java.sql.Timestamp;
//import java.util.Set;
//
//@Entity
//@Table(name="product_groups")
//public class ProductGroups {
//
//    @Id
//    @Column(name="id")
//    @SequenceGenerator(name="product_groups_id_seq", sequenceName="product_groups_id_seq", allocationSize=1)
//    @GeneratedValue(generator="product_groups_id_seq")
//    private Long id;
//
//    @Size(max = 256)
//    private String name;
//
//    @Size(max = 1024)
//    @Column(name = "description")
//    private String description;
//
//    @ManyToOne
//    @JoinColumn(name = "company_id", nullable = false)
//    private Companies company;
//
//    @ManyToOne
//    @JoinColumn(name = "master_id", nullable = false)
//    private User master;
//
//    @ManyToOne
//    @JoinColumn(name = "creator_id", nullable = false)
//    private User creator;
//
//    @ManyToOne
//    @JoinColumn(name = "changer_id")
//    private User changer;
//
//    @Column(name="date_time_created", nullable = false)
//    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
//    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
//    private Timestamp date_time_created;
//
//    @Column(name="date_time_changed")
//    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
//    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
//    private Timestamp date_time_changed;
//
//    @OneToMany(mappedBy = "productGroup")
//    Set<ProductGroupFields> productGroupFields;
//
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Set<ProductGroupFields> getProductGroupFields() {
//        return productGroupFields;
//    }
//
//    public void setProductGroupFields(Set<ProductGroupFields> productGroupFields) {
//        this.productGroupFields = productGroupFields;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Companies getCompany() {
//        return company;
//    }
//
//    public void setCompany(Companies company) {
//        this.company = company;
//    }
//
//    public User getMaster() {
//        return master;
//    }
//
//    public void setMaster(User master) {
//        this.master = master;
//    }
//
//    public User getCreator() {
//        return creator;
//    }
//
//    public void setCreator(User creator) {
//        this.creator = creator;
//    }
//
//    public User getChanger() {
//        return changer;
//    }
//
//    public void setChanger(User changer) {
//        this.changer = changer;
//    }
//
//    public Timestamp getDate_time_created() {
//        return date_time_created;
//    }
//
//    public void setDate_time_created(Timestamp date_time_created) {
//        this.date_time_created = date_time_created;
//    }
//
//    public Timestamp getDate_time_changed() {
//        return date_time_changed;
//    }
//
//    public void setDate_time_changed(Timestamp date_time_changed) {
//        this.date_time_changed = date_time_changed;
//    }
//}
