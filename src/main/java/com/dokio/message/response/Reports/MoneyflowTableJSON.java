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

public class MoneyflowTableJSON {

    private String      date_created;           // дата проведения
    private String      company;                // предприятие
    private String      cagent;                 // наименование контрагента
    private Long        cagent_id;              // id контрагента
    private BigDecimal  summ_before_pa;         // предыдущ. сумма прихода и расхода по р. счёту
    private BigDecimal  summ_in_pa;             // приход по р. счету
    private BigDecimal  summ_out_pa;            // расход по р. счету
    private BigDecimal  summ_result_pa;         // сумма прихода и расхода по р. счёту
    private BigDecimal  summ_before_bx;         // предыдущ. сумма прихода и расхода по кассе предприятия
    private BigDecimal  summ_in_bx;             // приход по кассе предприятия
    private BigDecimal  summ_out_bx;            // расход по кассе предприятия
    private BigDecimal  summ_result_bx;         // сумма прихода и расхода по кассе предприятия
    private BigDecimal  summ_before_all;        // предыд. сумма по всем
    private BigDecimal  summ_in_all;            // приход по всем
    private BigDecimal  summ_out_all;           // расход по всем
    private BigDecimal  summ_result_all;        // сумма по всем

    private BigDecimal  total_summ_in_pa;       // итого приход по р. счету
    private BigDecimal  total_summ_out_pa;      // итого расход по р. счету
    private BigDecimal  total_summ_in_bx;       // итого приход по кассе предприятия
    private BigDecimal  total_summ_out_bx;      // итого расход по кассе предприятия
    private BigDecimal  total_summ_in_all;      // итого приход по всем
    private BigDecimal  total_summ_out_all;     // итого расход по всем


    // для детализированной таблицы
    private String      date_time_created;      // дата проведения
    private BigDecimal  summ_in;                // приход
    private BigDecimal  summ_out;               // расход
    private String      doc_name;               // aka doc_name_ru - Наименование документа из таблицы documents (например, Корректировка, Входящий платёж)
    private String      pagename;               // имя страницы в ангулар
    private Long        doc_id;                 // id документа (для доступа типа shipment/344 )
    private String      doc_number;             // номер документа (String - на будущее, когда будут префикс и постфикс)
    private String      doc_page_name;          // страница документа во фронтэнде
    private String      obj_name;               // наименование объекта (например, Касса "Главная" или р.с. 3432032099322 в Отделение Банка "Такой то банк")
    private String      status;                 // статус документа

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
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

    public String getObj_name() {
        return obj_name;
    }

    public void setObj_name(String obj_name) {
        this.obj_name = obj_name;
    }

    public BigDecimal getTotal_summ_in_pa() {
        return total_summ_in_pa;
    }

    public void setTotal_summ_in_pa(BigDecimal total_summ_in_pa) {
        this.total_summ_in_pa = total_summ_in_pa;
    }

    public BigDecimal getTotal_summ_out_pa() {
        return total_summ_out_pa;
    }

    public void setTotal_summ_out_pa(BigDecimal total_summ_out_pa) {
        this.total_summ_out_pa = total_summ_out_pa;
    }

    public BigDecimal getTotal_summ_in_bx() {
        return total_summ_in_bx;
    }

    public void setTotal_summ_in_bx(BigDecimal total_summ_in_bx) {
        this.total_summ_in_bx = total_summ_in_bx;
    }

    public BigDecimal getTotal_summ_out_bx() {
        return total_summ_out_bx;
    }

    public void setTotal_summ_out_bx(BigDecimal total_summ_out_bx) {
        this.total_summ_out_bx = total_summ_out_bx;
    }

    public BigDecimal getTotal_summ_in_all() {
        return total_summ_in_all;
    }

    public void setTotal_summ_in_all(BigDecimal total_summ_in_all) {
        this.total_summ_in_all = total_summ_in_all;
    }

    public BigDecimal getTotal_summ_out_all() {
        return total_summ_out_all;
    }

    public void setTotal_summ_out_all(BigDecimal total_summ_out_all) {
        this.total_summ_out_all = total_summ_out_all;
    }

    public BigDecimal getSumm_result_pa() {
        return summ_result_pa;
    }

    public void setSumm_result_pa(BigDecimal summ_result_pa) {
        this.summ_result_pa = summ_result_pa;
    }

    public BigDecimal getSumm_result_bx() {
        return summ_result_bx;
    }

    public void setSumm_result_bx(BigDecimal summ_result_bx) {
        this.summ_result_bx = summ_result_bx;
    }

    public BigDecimal getSumm_result_all() {
        return summ_result_all;
    }

    public void setSumm_result_all(BigDecimal summ_result_all) {
        this.summ_result_all = summ_result_all;
    }

    public BigDecimal getSumm_before_pa() {
        return summ_before_pa;
    }

    public void setSumm_before_pa(BigDecimal summ_before_pa) {
        this.summ_before_pa = summ_before_pa;
    }

    public BigDecimal getSumm_before_bx() {
        return summ_before_bx;
    }

    public void setSumm_before_bx(BigDecimal summ_before_bx) {
        this.summ_before_bx = summ_before_bx;
    }

    public BigDecimal getSumm_before_all() {
        return summ_before_all;
    }

    public void setSumm_before_all(BigDecimal summ_before_all) {
        this.summ_before_all = summ_before_all;
    }

    public BigDecimal getSumm_in_all() {
        return summ_in_all;
    }

    public void setSumm_in_all(BigDecimal summ_in_all) {
        this.summ_in_all = summ_in_all;
    }

    public BigDecimal getSumm_out_all() {
        return summ_out_all;
    }

    public void setSumm_out_all(BigDecimal summ_out_all) {
        this.summ_out_all = summ_out_all;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
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

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public BigDecimal getSumm_in_pa() {
        return summ_in_pa;
    }

    public void setSumm_in_pa(BigDecimal summ_in_pa) {
        this.summ_in_pa = summ_in_pa;
    }

    public BigDecimal getSumm_out_pa() {
        return summ_out_pa;
    }

    public void setSumm_out_pa(BigDecimal summ_out_pa) {
        this.summ_out_pa = summ_out_pa;
    }

    public BigDecimal getSumm_in_bx() {
        return summ_in_bx;
    }

    public void setSumm_in_bx(BigDecimal summ_in_bx) {
        this.summ_in_bx = summ_in_bx;
    }

    public BigDecimal getSumm_out_bx() {
        return summ_out_bx;
    }

    public void setSumm_out_bx(BigDecimal summ_out_bx) {
        this.summ_out_bx = summ_out_bx;
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

    public String getDoc_page_name() {
        return doc_page_name;
    }

    public void setDoc_page_name(String doc_page_name) {
        this.doc_page_name = doc_page_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
