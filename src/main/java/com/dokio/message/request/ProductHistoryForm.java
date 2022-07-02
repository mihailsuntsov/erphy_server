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

public class ProductHistoryForm {

    private Long        companyId; //id предприятия
    private Long        departmentId;//стринг т.к. может быть как номер, так и строка номеров через запятую
    private Long        productId;  // id товара
    private String      dateFrom; //с даты
    private String      dateTo; //по дату;
    private String      sortColumn;//колонка сортировки
    private String      offset;//с какой строки (по умолчанию 0)
    private String      sortAsc; // asc/desc
    private String      result;//количество строк
    private List<Long>  docTypesIds;//id типов документов из таблицы documents

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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(String sortAsc) {
        this.sortAsc = sortAsc;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Long> getDocTypesIds() {
        return docTypesIds;
    }

    public void setDocTypesIds(List<Long> docTypesIds) {
        this.docTypesIds = docTypesIds;
    }

    @Override
    public String toString() {
        return "ProductHistoryForm: companyId=" + this.companyId + ", departmentId=" + this.departmentId +
                ", productId=" + this.productId;
    }
}
