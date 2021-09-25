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
