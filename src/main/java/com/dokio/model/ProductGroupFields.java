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
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.dokio.model.ManyToManyKeys.ProductFields;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.sql.Timestamp;
//import java.util.Set;
//
//@Entity
//@Table(name="product_group_fields")
//public class ProductGroupFields {
//
//    @Id
//    @Column(name="id")
//    @SequenceGenerator(name="product_group_fields_id_seq", sequenceName="product_group_fields_id_seq", allocationSize=1)
//    @GeneratedValue(generator="product_group_fields_id_seq")
//    private Long id;
//
//    @Size(max = 256)
//    private String name;
//
//    @Size(max = 2048)
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
//    @ManyToOne
//    @JsonIgnore
//    @JoinColumn(name = "group_id", nullable = false)
//    private ProductGroups productGroup;
//
//    @Column(name="field_type")
//    @NotNull
//    private Integer field_type;//1- Сет, т.е. набор полей 2-само поле
//
//    @ManyToOne
//    @JoinColumn(name = "parent_set_id")// если это поле, а не сет, то ссылка на id этой же таблицы с родительским Сетом полей
//    private ProductGroupFields productGroupField;
//
//    @Column(name="output_order")
//    private Integer output_order;// порядок вывода сетов полей и самих полей в сетах
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "productGroupField")
//    Set<ProductFields> fieldValues;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
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
//
//    public ProductGroups getProductGroup() {
//        return productGroup;
//    }
//
//    public void setProductGroup(ProductGroups productGroup) {
//        this.productGroup = productGroup;
//    }
//
//    public Integer getField_type() {
//        return field_type;
//    }
//
//    public void setField_type(Integer field_type) {
//        this.field_type = field_type;
//    }
//
//    public ProductGroupFields getProductGroupField() {
//        return productGroupField;
//    }
//
//    public void setProductGroupField(ProductGroupFields productGroupField) {
//        this.productGroupField = productGroupField;
//    }
//
//    public Integer getOutput_order() {
//        return output_order;
//    }
//
//    public void setOutput_order(Integer output_order) {
//        this.output_order = output_order;
//    }
//
//    public Set<ProductFields> getFieldValues() {
//        return fieldValues;
//    }
//
//    public void setFieldValues(Set<ProductFields> fieldValues) {
//        this.fieldValues = fieldValues;
//    }
//}
