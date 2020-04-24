//Класс для преобразования CRUD-запросов в объект
package com.laniakea.message.request;

public class CompanyForm {

    private int id;
    private String nameShort;
    private String nameFull;
    private String addressjur;
    private String addressfact;
    private String opf_id;
    private String inn;
    private String reg_num;
    private String who_got;
    private String dateReg;
    private String korschet;
    private String rs;
    private String bank;
    private String bik;
    private String checked;
    private Integer st_prefix_barcode_pieced;
    private Integer st_prefix_barcode_packed;
    private Integer currency_id;

    public Integer getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(Integer currency_id) {
        this.currency_id = currency_id;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameShort() {
        return nameShort;
    }

    public void setNameShort(String nameShort) {
        this.nameShort = nameShort;
    }

    public String getNameFull() {
        return nameFull;
    }

    public void setNameFull(String nameFull) {
        this.nameFull = nameFull;
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

    public String getDateReg() {
        return dateReg;
    }

    public void setDateReg(String dateReg) {
        this.dateReg = dateReg;
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
}
