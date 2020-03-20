//Группы товаров. Каждый товар может относиться к 1 товарной группе, которую объединяют общие поля
//Например Футболка - поля: Материал, Производитель, Ворот, Размер
//Это нужно, чтобы быстро создавать и описывать товары (не создавая каждый раз в товаре необходимые поля)
//а также для обеспечения работы фильтров по полям товаров в интернет-магазине.
//Сами поля группы описываются классом ProductGroupFields (таблица product_group_fields)
package com.laniakea.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name="product_groups")
public class ProductGroups {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="product_groups_id_seq", sequenceName="product_groups_id_seq", allocationSize=1)
    @GeneratedValue(generator="product_groups_id_seq")
    private Long id;

    @Size(max = 256)
    private String name;

    @Size(max = 1024)
    @Column(name = "description")
    private String description;

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

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @OneToMany(mappedBy = "productGroup")
    Set<ProductGroupFields> productGroupFields;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<ProductGroupFields> getProductGroupFields() {
        return productGroupFields;
    }

    public void setProductGroupFields(Set<ProductGroupFields> productGroupFields) {
        this.productGroupFields = productGroupFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
