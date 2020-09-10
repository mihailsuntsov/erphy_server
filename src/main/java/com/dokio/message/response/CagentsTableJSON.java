///*
//Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
//Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
//Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
//соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
//программного обеспечения;
//Эта программа распространяется в расчете на то, что она окажется полезной, но
//БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
//ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
//лицензией GNU для получения более подробной информации.
//Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
//программой. Если Вы ее не получили, то перейдите по адресу:
//<http://www.gnu.org/licenses/>
// */
//package com.dokio.message.response;
//
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//
//@Entity
//public class CagentsTableJSON {
//
//    @Id
//    private Long id;
//    private String name;
//    private String description;
//    private String company;
//    private String company_id;
//    private String master;
//    private String master_id;
//    private String creator;
//    private String creator_id;
//    private String changer;
//    private String changer_id;
//    private String opf;
//    private String opf_id;
//    private String date_time_created;
//    private String date_time_changed;
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
//    public String getCompany() {
//        return company;
//    }
//
//    public void setCompany(String company) {
//        this.company = company;
//    }
//
//    public String getCompany_id() {
//        return company_id;
//    }
//
//    public void setCompany_id(String company_id) {
//        this.company_id = company_id;
//    }
//
//    public String getMaster() {
//        return master;
//    }
//
//    public void setMaster(String master) {
//        this.master = master;
//    }
//
//    public String getMaster_id() {
//        return master_id;
//    }
//
//    public void setMaster_id(String master_id) {
//        this.master_id = master_id;
//    }
//
//    public String getCreator() {
//        return creator;
//    }
//
//    public void setCreator(String creator) {
//        this.creator = creator;
//    }
//
//    public String getCreator_id() {
//        return creator_id;
//    }
//
//    public void setCreator_id(String creator_id) {
//        this.creator_id = creator_id;
//    }
//
//    public String getChanger() {
//        return changer;
//    }
//
//    public void setChanger(String changer) {
//        this.changer = changer;
//    }
//
//    public String getChanger_id() {
//        return changer_id;
//    }
//
//    public void setChanger_id(String changer_id) {
//        this.changer_id = changer_id;
//    }
//
//    public String getOpf() {
//        return opf;
//    }
//
//    public void setOpf(String opf) {
//        this.opf = opf;
//    }
//
//    public String getOpf_id() {
//        return opf_id;
//    }
//
//    public void setOpf_id(String opf_id) {
//        this.opf_id = opf_id;
//    }
//
//    public String getDate_time_created() {
//        return date_time_created;
//    }
//
//    public void setDate_time_created(String date_time_created) {
//        this.date_time_created = date_time_created;
//    }
//
//    public String getDate_time_changed() {
//        return date_time_changed;
//    }
//
//    public void setDate_time_changed(String date_time_changed) {
//        this.date_time_changed = date_time_changed;
//    }
//}
