/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/

package com.dokio.message.response;

import java.math.BigDecimal;

public class VatinvoiceinJSON {

    private Long id;
    private String master;
    private String creator;
    private String changer;
    private String cagent;
    private Long master_id;
    private Long cagent_id;
    private Long creator_id;
    private Long changer_id;
    private Long company_id;
    private String parent_tablename;
    private Long orderout_id;
    private Long paymentout_id;
    private Long acceptance_id;
    private String gov_id;
    private BigDecimal summ;
    private Long doc_number;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String description;
    private String status_name;
    private String status_color;
    private String status_description;
    private Long status_id;
    private Boolean is_completed;              // проведено
    private String uid;
    private String paydoc_number;              // Платежный документ
    private String paydoc_date;                // Платежный документ - дата

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
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

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Long changer_id) {
        this.changer_id = changer_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getStatus_color() {
        return status_color;
    }

    public void setStatus_color(String status_color) {
        this.status_color = status_color;
    }

    public String getStatus_description() {
        return status_description;
    }

    public void setStatus_description(String status_description) {
        this.status_description = status_description;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getParent_tablename() {
        return parent_tablename;
    }

    public void setParent_tablename(String parent_tablename) {
        this.parent_tablename = parent_tablename;
    }

    public String getGov_id() {
        return gov_id;
    }

    public void setGov_id(String gov_id) {
        this.gov_id = gov_id;
    }

    public String getPaydoc_number() {
        return paydoc_number;
    }

    public void setPaydoc_number(String paydoc_number) {
        this.paydoc_number = paydoc_number;
    }

    public String getPaydoc_date() {
        return paydoc_date;
    }

    public void setPaydoc_date(String paydoc_date) {
        this.paydoc_date = paydoc_date;
    }

    public Long getOrderout_id() {
        return orderout_id;
    }

    public void setOrderout_id(Long orderout_id) {
        this.orderout_id = orderout_id;
    }

    public Long getPaymentout_id() {
        return paymentout_id;
    }

    public void setPaymentout_id(Long paymentout_id) {
        this.paymentout_id = paymentout_id;
    }

    public Long getAcceptance_id() {
        return acceptance_id;
    }

    public void setAcceptance_id(Long acceptance_id) {
        this.acceptance_id = acceptance_id;
    }
}