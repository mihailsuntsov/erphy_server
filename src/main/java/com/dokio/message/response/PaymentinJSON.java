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

public class PaymentinJSON {

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
    private Boolean is_completed;           // проведено
    private String uid;
    private BigDecimal summ;                // сумма операции
    private String income_number;           // входящий внутренний номер поставщика
    private String income_number_date;      // входящая дата счета поставщика
    private Long payment_account_id;        // id расчтёного счёта
    private String payment_account;         // расчтёный счёт
    private Boolean internal;               // внутренний платеж
    private String moving_type;             // тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
    private Long boxoffice_from_id;         // id кассы предприятия - источника
    private Long payment_account_from_id;   // id расч счёта
    private Long paymentout_id;             // id исходящего платежа, из которого поступили средства
    private Long orderout_id;               // id расходного ордера, из которого поступили средства
    private String paymentout;              // исходящий платеж, из которого поступили средства
    private String orderout;                // расходный ордер, из которого поступили средства

    private String boxoffice_from;          // наименование кассы предприятия - источника
    private String payment_account_from;    // наименование расч счёта - источника


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

    public String getPayment_account() {
        return payment_account;
    }

    public void setPayment_account(String payment_account) {
        this.payment_account = payment_account;
    }

    public Long getPayment_account_id() {
        return payment_account_id;
    }

    public void setPayment_account_id(Long payment_account_id) {
        this.payment_account_id = payment_account_id;
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

    public String getIncome_number() {
        return income_number;
    }

    public void setIncome_number(String income_number) {
        this.income_number = income_number;
    }

    public String getIncome_number_date() {
        return income_number_date;
    }

    public void setIncome_number_date(String income_number_date) {
        this.income_number_date = income_number_date;
    }
}