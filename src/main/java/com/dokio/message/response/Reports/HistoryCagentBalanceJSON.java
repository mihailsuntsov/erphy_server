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

public class HistoryCagentBalanceJSON {

    private String      date_time_created;      // дата проведения
    private String      company;                // предприятие
    private String      cagent;                 // наименование контрагента
    private Long        cagent_id;              // id контрагента
    private BigDecimal  summ_on_start;          // начальный остаток
    private BigDecimal  summ_on_end;            // конечный остаток
    private BigDecimal  summ_in;                // приход
    private BigDecimal  summ_out;               // расход
    private String      doc_name;               // aka doc_name_ru - Наименование документа из таблицы documents
    private String      pagename;               // имя страницы в ангулар
    private Long        doc_id;                 // id документа (для доступа типа shipment/344 )
    private String      doc_number;             // номер документа (String - на будущее, когда будут префикс и постфикс)
    private String      doc_page_name;          // страница документа во фронтэнде
    private String      status;                 // статус документа

    public String getDoc_page_name() {
        return doc_page_name;
    }

    public void setDoc_page_name(String doc_page_name) {
        this.doc_page_name = doc_page_name;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public BigDecimal getSumm_on_start() {
        return summ_on_start;
    }

    public void setSumm_on_start(BigDecimal summ_on_start) {
        this.summ_on_start = summ_on_start;
    }

    public BigDecimal getSumm_on_end() {
        return summ_on_end;
    }

    public void setSumm_on_end(BigDecimal summ_on_end) {
        this.summ_on_end = summ_on_end;
    }

    public BigDecimal getSumm_in() {
        return summ_in;
    }

    public void setSumm_in(BigDecimal summ_in) {
        this.summ_in = summ_in;
    }

    public BigDecimal getSumm_out() {
        return summ_out;
    }

    public void setSumm_out(BigDecimal summ_out) {
        this.summ_out = summ_out;
    }

    public String getDoc_name() {
        return doc_name;
    }

    public void setDoc_name(String doc_name) {
        this.doc_name = doc_name;
    }

    public String getPagename() {
        return pagename;
    }

    public void setPagename(String pagename) {
        this.pagename = pagename;
    }

    public Long getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(Long doc_id) {
        this.doc_id = doc_id;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
