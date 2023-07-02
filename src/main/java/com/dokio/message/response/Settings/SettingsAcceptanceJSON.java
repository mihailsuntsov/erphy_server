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

package com.dokio.message.response.Settings;

public class SettingsAcceptanceJSON {
    private Long        companyId;              // id предприятия
    private Long        departmentId;           // id отделения
    private Long        statusOnFinishId;       // статус документа при завершении инвентаризации
    private Boolean     autoAdd;                // автодобавление товара из формы поиска в таблицу
    private Boolean     autoPrice;              // автоподставление последней закупочной цены

    public SettingsAcceptanceJSON(Boolean autoAdd, Boolean autoPrice) {
        this.autoAdd = autoAdd;
        this.autoPrice = autoPrice;
    }

    public SettingsAcceptanceJSON() {
    }

    public Boolean getAutoPrice() {
        return autoPrice;
    }

    public void setAutoPrice(Boolean autoPrice) {
        this.autoPrice = autoPrice;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getStatusOnFinishId() {
        return statusOnFinishId;
    }

    public void setStatusOnFinishId(Long statusOnFinishId) {
        this.statusOnFinishId = statusOnFinishId;
    }

    public Boolean getAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(Boolean autoAdd) {
        this.autoAdd = autoAdd;
    }

}
