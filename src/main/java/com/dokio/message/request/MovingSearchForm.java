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

import java.util.Set;

public class MovingSearchForm {
    private String          searchString;       // поисковая строка
    private String          sortColumn;         // колонка сортировки
    private Integer         offset;             // номер страницы начиная с 0. Изначально (при первом запросе таблицы) это null
    private String          sortAsc;            // сортировка (asc desc)
    private Integer         result;             // количество записей, отображаемых на странице
    private Long            companyId;          // id предприятия
    private Set<Integer>    filterOptionsIds;   // номера опций для фильтра
    private Long            departmentFromId;   // id предприятия из
    private Long            departmentToId;     // id предприятия в

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(String sortAsc) {
        this.sortAsc = sortAsc;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Set<Integer> getFilterOptionsIds() {
        return filterOptionsIds;
    }

    public void setFilterOptionsIds(Set<Integer> filterOptionsIds) {
        this.filterOptionsIds = filterOptionsIds;
    }

    public Long getDepartmentFromId() {
        return departmentFromId;
    }

    public void setDepartmentFromId(Long departmentFromId) {
        this.departmentFromId = departmentFromId;
    }

    public Long getDepartmentToId() {
        return departmentToId;
    }

    public void setDepartmentToId(Long departmentToId) {
        this.departmentToId = departmentToId;
    }
}
