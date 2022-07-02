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

package com.dokio.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name="traderesults")
public class TradeResults {
    @Id
    @Column(name="id")
    @SequenceGenerator(name="traderesults_id_seq", sequenceName="traderesults_id_seq", allocationSize=1)
    @GeneratedValue(generator="traderesults_id_seq")
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)//сотрудник
    private User employee;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name="trade_date")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)// Дата торговой смены
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date trade_date;

    @Column(name = "incoming_cash_checkout")//Учтённые наличные
    private Integer incoming_cash_checkout;

    @Column(name = "incoming_cashless_checkout")//Учтённые безналичные
    private Integer incoming_cashless_checkout;

    @Column(name = "incoming_cash2")//Неучтенные наличные (черный нал)
    private Integer incoming_cash2;

    @Column(name = "incoming_cashless2")//Неучтённые безналичные (например перевод на Сбербанк онлайн)
    private Integer incoming_cashless2;

    @Column(name = "refund_cash")//Возвраты наличные
    private Integer refund_cash;

    @Column(name = "refund_cashless")//Возвраты безналичные
    private Integer refund_cashless;

    @Column(name = "encashment_cash")//Инкассация (изъятие) наличные
    private Integer encashment_cash;

    @Column(name = "encashment_cashless")//Инкассация (изъятие) безналичные
    private Integer encashment_cashless;

    @Column(name = "additional")
    @Size(max = 2048)
    private String additional;

    @Column(name = "is_archive")//Удалён
    private Boolean is_archive;

    public TradeResults(){}

    public TradeResults(Integer incoming_cash_checkout, Integer incoming_cashless_checkout, Integer incoming_cash2, Integer incoming_cashless2, Integer refund_cash, Integer refund_cashless, Integer encashment_cash, Integer encashment_cashless, @Size(max = 2048) String additional)
    {
        this.incoming_cash_checkout = incoming_cash_checkout;
        this.incoming_cashless_checkout = incoming_cashless_checkout;
        this.incoming_cash2 = incoming_cash2;
        this.incoming_cashless2 = incoming_cashless2;
        this.refund_cash = refund_cash;
        this.refund_cashless = refund_cashless;
        this.encashment_cash = encashment_cash;
        this.encashment_cashless = encashment_cashless;
        this.additional = additional;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
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

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
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

    public Date getTrade_date() {
        return trade_date;
    }

    public void setTrade_date(Date trade_date) {
        this.trade_date = trade_date;
    }

    public Integer getIncoming_cash_checkout() {
        return incoming_cash_checkout;
    }

    public void setIncoming_cash_checkout(Integer incoming_cash_checkout) {
        this.incoming_cash_checkout = incoming_cash_checkout;
    }

    public Integer getIncoming_cashless_checkout() {
        return incoming_cashless_checkout;
    }

    public void setIncoming_cashless_checkout(Integer incoming_cashless_checkout) {
        this.incoming_cashless_checkout = incoming_cashless_checkout;
    }

    public Integer getIncoming_cash2() {
        return incoming_cash2;
    }

    public void setIncoming_cash2(Integer incoming_cash2) {
        this.incoming_cash2 = incoming_cash2;
    }

    public Integer getIncoming_cashless2() {
        return incoming_cashless2;
    }

    public void setIncoming_cashless2(Integer incoming_cashless2) {
        this.incoming_cashless2 = incoming_cashless2;
    }

    public Integer getRefund_cash() {
        return refund_cash;
    }

    public void setRefund_cash(Integer refund_cash) {
        this.refund_cash = refund_cash;
    }

    public Integer getRefund_cashless() {
        return refund_cashless;
    }

    public void setRefund_cashless(Integer refund_cashless) {
        this.refund_cashless = refund_cashless;
    }

    public Integer getEncashment_cash() {
        return encashment_cash;
    }

    public void setEncashment_cash(Integer encashment_cash) {
        this.encashment_cash = encashment_cash;
    }

    public Integer getEncashment_cashless() {
        return encashment_cashless;
    }

    public void setEncashment_cashless(Integer encashment_cashless) {
        this.encashment_cashless = encashment_cashless;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }
}
