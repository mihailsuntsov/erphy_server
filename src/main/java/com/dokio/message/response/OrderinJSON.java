/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/


package com.dokio.message.response;

import java.math.BigDecimal;

public class OrderinJSON {

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
    private Long doc_number;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String description;
    private BigDecimal nds;
    private String status_name;
    private String status_color;
    private String status_description;
    private Long status_id;
    private Boolean is_completed;               // проведено
    private String uid;
    private BigDecimal summ;
    private Boolean internal; // внутренний платеж
    private Long boxoffice_id;
    private String moving_type;             // тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
    private Long kassa_from_id;             // id кассы ККМ - источника
    private Long boxoffice_from_id;         // id кассы предприятия - источника
    private Long payment_account_from_id;   // id расч счёта
    private Long withdrawal_id;             // id выемки, из которой поступили средства
    private Long paymentout_id;             // id исходящего платежа, из которого поступили средства
    private Long orderout_id;               // id расходного ордера, из которого поступили средства
    private String withdrawal;              // выемка, из которой поступили средства
    private String paymentout;              // исходящий платеж, из которого поступили средства
    private String orderout;                // расходный ордер, из которого поступили средства

    private String kassa_from;              // наименование кассы ККМ - источника
    private String boxoffice_from;          // наименование кассы предприятия - источника
    private String payment_account_from;    // наименование расч счёта - источника

    private String boxoffice;               // наименование кассы предприятия - назначения

    public String getBoxoffice() {
        return boxoffice;
    }

    public void setBoxoffice(String boxoffice) {
        this.boxoffice = boxoffice;
    }

    public String getKassa_from() {
        return kassa_from;
    }

    public void setKassa_from(String kassa_from) {
        this.kassa_from = kassa_from;
    }

    public String getBoxoffice_from() {
        return boxoffice_from;
    }

    public void setBoxoffice_from(String boxoffice_from) {
        this.boxoffice_from = boxoffice_from;
    }

    public String getPayment_account_from() {
        return payment_account_from;
    }

    public void setPayment_account_from(String payment_account_from) {
        this.payment_account_from = payment_account_from;
    }

    public String getWithdrawal() {
        return withdrawal;
    }

    public void setWithdrawal(String withdrawal) {
        this.withdrawal = withdrawal;
    }

    public String getPaymentout() {
        return paymentout;
    }

    public void setPaymentout(String paymentout) {
        this.paymentout = paymentout;
    }

    public String getOrderout() {
        return orderout;
    }

    public void setOrderout(String orderout) {
        this.orderout = orderout;
    }

    public Long getWithdrawal_id() {
        return withdrawal_id;
    }

    public void setWithdrawal_id(Long withdrawal_id) {
        this.withdrawal_id = withdrawal_id;
    }

    public Long getPaymentout_id() {
        return paymentout_id;
    }

    public void setPaymentout_id(Long paymentout_id) {
        this.paymentout_id = paymentout_id;
    }

    public Long getOrderout_id() {
        return orderout_id;
    }

    public void setOrderout_id(Long orderout_id) {
        this.orderout_id = orderout_id;
    }

    public String getMoving_type() {
        return moving_type;
    }

    public void setMoving_type(String moving_type) {
        this.moving_type = moving_type;
    }

    public Long getKassa_from_id() {
        return kassa_from_id;
    }

    public void setKassa_from_id(Long kassa_from_id) {
        this.kassa_from_id = kassa_from_id;
    }

    public Long getBoxoffice_from_id() {
        return boxoffice_from_id;
    }

    public void setBoxoffice_from_id(Long boxoffice_from_id) {
        this.boxoffice_from_id = boxoffice_from_id;
    }

    public Long getPayment_account_from_id() {
        return payment_account_from_id;
    }

    public void setPayment_account_from_id(Long payment_account_from_id) {
        this.payment_account_from_id = payment_account_from_id;
    }


    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Long getBoxoffice_id() {
        return boxoffice_id;
    }

    public void setBoxoffice_id(Long boxoffice_id) {
        this.boxoffice_id = boxoffice_id;
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

    public BigDecimal getNds() {
        return nds;
    }

    public void setNds(BigDecimal nds) {
        this.nds = nds;
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

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

}