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

package com.dokio.message.response.Reports;

import java.math.BigDecimal;
import java.util.Set;

public class ShiftsJSON {

    private Long id;
    private Long master_id;
    private Long creator_id;
    private Long closer_id;
    private Long company_id;
    private Long department_id;
    private Long kassa_id;  // id KKM
    private Long acquiring_bank_id; // банк эквайер
    private String date_time_created;
    private String date_time_closed;
    private String shift_status_id;
    private String master;
    private String creator;
    private String closer;
    private String company;
    private String department;
    private String kassa;
    private String acquiring_bank;
    private Integer shift_number;
    private String zn_kkt;
    private String shift_expired_at;
    private String fn_serial;
    private String uid;
    private BigDecimal revenue_all; // выручка всего
    private BigDecimal revenue_cash; // выручка нал
    private BigDecimal revenue_electronically; // выручка безнал
    private Long num_receipts; // кол-во чеков

    public Long getNum_receipts() {
        return num_receipts;
    }

    public void setNum_receipts(Long num_receipts) {
        this.num_receipts = num_receipts;
    }

    public BigDecimal getRevenue_all() {
        return revenue_all;
    }

    public void setRevenue_all(BigDecimal revenue_all) {
        this.revenue_all = revenue_all;
    }

    public BigDecimal getRevenue_cash() {
        return revenue_cash;
    }

    public void setRevenue_cash(BigDecimal revenue_cash) {
        this.revenue_cash = revenue_cash;
    }

    public BigDecimal getRevenue_electronically() {
        return revenue_electronically;
    }

    public void setRevenue_electronically(BigDecimal revenue_electronically) {
        this.revenue_electronically = revenue_electronically;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getCloser_id() {
        return closer_id;
    }

    public void setCloser_id(Long closer_id) {
        this.closer_id = closer_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getKassa_id() {
        return kassa_id;
    }

    public void setKassa_id(Long kassa_id) {
        this.kassa_id = kassa_id;
    }

    public String getKassa() {
        return kassa;
    }

    public void setKassa(String kassa) {
        this.kassa = kassa;
    }

    public Long getAcquiring_bank_id() {
        return acquiring_bank_id;
    }

    public void setAcquiring_bank_id(Long acquiring_bank_id) {
        this.acquiring_bank_id = acquiring_bank_id;
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

    public String getCloser() {
        return closer;
    }

    public void setCloser(String closer) {
        this.closer = closer;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcquiring_bank() {
        return acquiring_bank;
    }

    public void setAcquiring_bank(String acquiring_bank) {
        this.acquiring_bank = acquiring_bank;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_closed() {
        return date_time_closed;
    }

    public void setDate_time_closed(String date_time_closed) {
        this.date_time_closed = date_time_closed;
    }

    public Integer getShift_number() {
        return shift_number;
    }

    public void setShift_number(Integer shift_number) {
        this.shift_number = shift_number;
    }

    public String getZn_kkt() {
        return zn_kkt;
    }

    public void setZn_kkt(String zn_kkt) {
        this.zn_kkt = zn_kkt;
    }

    public String getShift_status_id() {
        return shift_status_id;
    }

    public void setShift_status_id(String shift_status_id) {
        this.shift_status_id = shift_status_id;
    }

    public String getShift_expired_at() {
        return shift_expired_at;
    }

    public void setShift_expired_at(String shift_expired_at) {
        this.shift_expired_at = shift_expired_at;
    }

    public String getFn_serial() {
        return fn_serial;
    }

    public void setFn_serial(String fn_serial) {
        this.fn_serial = fn_serial;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}