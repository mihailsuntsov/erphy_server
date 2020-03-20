//Класс для формирования JSON
package com.laniakea.message.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

@Entity
public class CompaniesJSON {

    @Id
    private Long id;
    private String name;
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_created;
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_changed;
    private String owner_id;
    private String creator_id;
    private String changer_id;
    private String owner;
    private String creator;
    private String changer;
    private String namefull;
    private String addressjur;
    private String addressfact;
    private String opf_name;
    private String opf_id;
    private String inn;
    private String reg_num;
    private String who_got;
//    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
//    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
//    private Date datereg;
    private String datereg;
    private String korschet;
    private String rs;
    private String bank;
    private String bik;
    private String st_prefix_barcode_pieced;
    private String st_prefix_barcode_packed;


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

    public Timestamp getDate_time_created() {
        return date_time_created;
    }

    public String getSt_prefix_barcode_pieced() {
        return st_prefix_barcode_pieced;
    }

    public void setSt_prefix_barcode_pieced(String st_prefix_barcode_pieced) {
        this.st_prefix_barcode_pieced = st_prefix_barcode_pieced;
    }

    public String getSt_prefix_barcode_packed() {
        return st_prefix_barcode_packed;
    }

    public void setSt_prefix_barcode_packed(String st_prefix_barcode_packed) {
        this.st_prefix_barcode_packed = st_prefix_barcode_packed;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public String getNamefull() {
        return namefull;
    }

    public void setNamefull(String namefull) {
        this.namefull = namefull;
    }

    public String getAddressjur() {
        return addressjur;
    }

    public void setAddressjur(String addressjur) {
        this.addressjur = addressjur;
    }

    public String getAddressfact() {
        return addressfact;
    }

    public void setAddressfact(String addressfact) {
        this.addressfact = addressfact;
    }

    public String getOpf_name() {
        return opf_name;
    }

    public void setOpf_name(String opf_name) {
        this.opf_name = opf_name;
    }

    public String getOpf_id() {
        return opf_id;
    }

    public void setOpf_id(String opf_id) {
        this.opf_id = opf_id;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getReg_num() {
        return reg_num;
    }

    public void setReg_num(String reg_num) {
        this.reg_num = reg_num;
    }

    public String getWho_got() {
        return who_got;
    }

    public void setWho_got(String who_got) {
        this.who_got = who_got;
    }

//    public Date getDatereg() {
//        return datereg;
//    }
//
//    public void setDatereg(Date datereg) {
//        this.datereg = datereg;
//    }


    public String getDatereg() {
        return datereg;
    }

    public void setDatereg(String datereg) {
        this.datereg = datereg;
    }

    public String getKorschet() {
        return korschet;
    }

    public void setKorschet(String korschet) {
        this.korschet = korschet;
    }

    public String getRs() {
        return rs;
    }

    public void setRs(String rs) {
        this.rs = rs;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBik() {
        return bik;
    }

    public void setBik(String bik) {
        this.bik = bik;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(String changer_id) {
        this.changer_id = changer_id;
    }
}