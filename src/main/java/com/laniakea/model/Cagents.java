package com.laniakea.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.laniakea.model.Sprav.SpravSysOPF;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="cagents")
public class Cagents {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="cagents_id_seq", sequenceName="cagents_id_seq", allocationSize=1)
    @GeneratedValue(generator="cagents_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "opf_id")
    private SpravSysOPF cagentOpf;

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

    @Column(name = "is_archive")//Удалён
    private Boolean is_archive;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cagent_cagentcategories",
            joinColumns = @JoinColumn(name = "cagent_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<CagentCategories> cagentCategories = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "cagents")
    private Set<Products> products;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SpravSysOPF getCagentOpf() {
        return cagentOpf;
    }

    public void setCagentOpf(SpravSysOPF cagentOpf) {
        this.cagentOpf = cagentOpf;
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

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }

    public Set<CagentCategories> getCagentCategories() {
        return cagentCategories;
    }

    public void setCagentCategories(Set<CagentCategories> cagentCategories) {
        this.cagentCategories = cagentCategories;
    }

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
    }
}
