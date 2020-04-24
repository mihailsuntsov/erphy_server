package com.laniakea.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.laniakea.model.Sprav.SpravSysOPF;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@NamedQueries({
        @NamedQuery(name="Companies.deleteCompaniesByNumber", query="delete from com.laniakea.model.Companies obj where obj.compId in(:delNumbers) ")
})

@Entity
@Table(name="companies")
public class Companies {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "companies_id_seq", sequenceName = "companies_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "companies_id_seq")
    private Long compId;

    @Column(name = "name")
    private String compName;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @Column(name="date_time_created")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp compDateTimeCreated;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp compDateTimeChanged;

    @Column(name="namefull")
    private String compNameFull;

    @Column(name="addressjur")
    private String compAddressJur;

    @Column(name="addressfact")
    private String compAddressFact;

    @ManyToOne
    @JoinColumn(name = "opf")
    private SpravSysOPF compOpf;

    @Column(name="inn")
    private String compInn;

    @Column(name="reg_num")
    private String compReg_num;

    @Column(name="who_got")
    private String compWho_got;

    @Column(name="datereg")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Date compDateReg;

    @Column(name="korschet")
    private String compKorschet;

    @Column(name="rs")
    private String compRs;

    @Column(name="bank")
    private String compBank;

    @Column(name="bik")
    private String compbik;

    @Column(name="st_prefix_barcode_pieced")
    private Integer st_prefix_barcode_pieced;

    @Column(name="st_prefix_barcode_packed")
    private Integer st_prefix_barcode_packed;

    @Column(name="currency_id")
    private Integer currency_id;


    public Long getCompId() {
        return compId;
    }

    public void setCompId(Long compId) {
        this.compId = compId;
    }

    public Integer getSt_prefix_barcode_pieced() {
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

    public String getCompNameFull() {
        return compNameFull;
    }

    public void setCompNameFull(String compNameFull) {
        this.compNameFull = compNameFull;
    }

    public String getCompAddressJur() {
        return compAddressJur;
    }

    public void setCompAddressJur(String compAddressJur) {
        this.compAddressJur = compAddressJur;
    }

    public String getCompAddressFact() {
        return compAddressFact;
    }

    public void setCompAddressFact(String compAddressFact) {
        this.compAddressFact = compAddressFact;
    }

    public SpravSysOPF getCompOpf() {
        return compOpf;
    }

    public void setCompOpf(SpravSysOPF compOpf) {
        this.compOpf = compOpf;
    }

    public String getCompInn() {
        return compInn;
    }

    public void setCompInn(String compInn) {
        this.compInn = compInn;
    }

    public String getCompReg_num() {
        return compReg_num;
    }

    public void setCompReg_num(String compReg_num) {
        this.compReg_num = compReg_num;
    }

    public String getCompWho_got() {
        return compWho_got;
    }

    public void setCompWho_got(String compWho_got) {
        this.compWho_got = compWho_got;
    }

    public Date getCompDateReg() {
        return compDateReg;
    }

    public void setCompDateReg(Date compDateReg) {
        this.compDateReg = compDateReg;
    }

    public String getCompKorschet() {
        return compKorschet;
    }

    public void setCompKorschet(String compKorschet) {
        this.compKorschet = compKorschet;
    }

    public String getCompRs() {
        return compRs;
    }

    public void setCompRs(String compRs) {
        this.compRs = compRs;
    }

    public String getCompBank() {
        return compBank;
    }

    public void setCompBank(String compBank) {
        this.compBank = compBank;
    }

    public String getCompbik() {
        return compbik;
    }

    public void setCompbik(String compbik) {
        this.compbik = compbik;
    }
}