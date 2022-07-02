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
package com.dokio.message.request;

import java.util.List;

//для запроса списка товаров по их id или id их категорий
public class ProductsInfoListForm {
    private Long companyId;             // предприятие, по которому идет запрос данных
    private Long departmentId;          // id отделения
    private Long priceTypeId;           // тип цены, по которому будут выданы цены
    private String reportOn;            // по категориям или по товарам/услугам (categories, products)
    private List<Long> reportOnIds;     // id категорий или товаров/услуг (того, что выбрано в reportOn)

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

    public Long getPriceTypeId() {
        return priceTypeId;
    }

    public void setPriceTypeId(Long priceTypeId) {
        this.priceTypeId = priceTypeId;
    }

    public String getReportOn() {
        return reportOn;
    }

    public void setReportOn(String reportOn) {
        this.reportOn = reportOn;
    }

    public List<Long> getReportOnIds() {
        return reportOnIds;
    }

    public void setReportOnIds(List<Long> reportOnIds) {
        this.reportOnIds = reportOnIds;
    }
}
