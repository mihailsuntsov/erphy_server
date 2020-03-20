package com.laniakea.model;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="writeoff")
public class Writeoff {
    @Id
    @Column(name="id")
    @SequenceGenerator(name="writeoff_id_seq", sequenceName="writeoff_id_seq", allocationSize=1)
    @GeneratedValue(generator="writeoff_id_seq")
    private Long id;

    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

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

    @Column(name="writeoff_date")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)// Дата торговой смены
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Date writeoff_date;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name = "is_completed")//Оприходование завершено
    private Boolean is_completed;

    @Column(name = "is_archive")//Удалён (возаможно только для незавершенных документов, т.е. где is_completed = false)
    private Boolean is_archive;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "writeoff_product",
            joinColumns = @JoinColumn(name = "writeoff_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Products> products = new HashSet<>();

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

    public Date getWriteoff_date() {
        return writeoff_date;
    }

    public void setWriteoff_date(Date writeoff_date) {
        this.writeoff_date = writeoff_date;
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
}
