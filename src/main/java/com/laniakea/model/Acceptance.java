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
@Table(name="acceptance")
public class Acceptance {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="acceptance_id_seq", sequenceName="acceptance_id_seq", allocationSize=1)
    @GeneratedValue(generator="acceptance_id_seq")
    private Long id;

    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "cagent_id", nullable = false)//поставщик
    private Cagents cagent;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @Column(name = "nds")
    private Boolean nds;

    @Column(name = "nds_included")
    private Boolean nds_included;

    @Column(name = "overhead")//накладные расходы
    private BigDecimal overhead;

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

    @Column(name="acceptance_date")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)// Дата торговой смены
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Date acceptance_date;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name = "is_completed")//Приёмка завершена
    private Boolean is_completed;

    @Column(name = "is_archive")//Удалён
    private Boolean is_archive;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "acceptance_product",
            joinColumns = @JoinColumn(name = "acceptance_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Products> products = new HashSet<>();

    @Column(name = "overhead_netcost_method")//Распределение затрат на себестоимость товаров. 0 - нет, 1 - по весу цены в поставке
    private Integer overhead_netcost_method;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
    }

    public Date getAcceptance_date() {
        return acceptance_date;
    }

    public void setAcceptance_date(Date acceptance_date) {
        this.acceptance_date = acceptance_date;
    }

    public Integer getOverhead_netcost_method() {
        return overhead_netcost_method;
    }

    public void setOverhead_netcost_method(Integer overhead_netcost_method) {
        this.overhead_netcost_method = overhead_netcost_method;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public String getDescription() {
        return description;
    }

    public Cagents getCagent() {
        return cagent;
    }

    public void setCagent(Cagents cagent) {
        this.cagent = cagent;
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

    public BigDecimal getOverhead() {
        return overhead;
    }

    public void setOverhead(BigDecimal overhead) {
        this.overhead = overhead;
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

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
    }
}
