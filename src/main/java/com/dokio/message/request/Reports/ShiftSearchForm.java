package com.dokio.message.request.Reports;

import java.util.Set;

public class ShiftSearchForm {

    public class SearchForm {

    }
        private int id;
        private Long masterId;      //  id владельца (аккаунта) записи
        private String searchString;//  строка поиска
        private Long companyId;     //  id предприятия
        private Long departmentId;  //  id отделения
        private String sortColumn;  //  колонка сортировки
        private String sortAsc;     //  asc desc
        private String result;      //  количество записей, отображаемых на странице
        private String pagenum;     //  отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        private String offset;      //  это номер страницы начиная с 0. Изначально (при первом запросе таблицы) это null
        private Long kassaId;       //  касса
        private Long cashierId;     //  кассир
        private Set<Integer> filterOptionsIds; //опции поиска

    public Set<Integer> getFilterOptionsIds() {
        return filterOptionsIds;
    }

    public void setFilterOptionsIds(Set<Integer> filterOptionsIds) {
        this.filterOptionsIds = filterOptionsIds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
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

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
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

    public String getPagenum() {
        return pagenum;
    }

    public void setPagenum(String pagenum) {
        this.pagenum = pagenum;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public Long getKassaId() {
        return kassaId;
    }

    public void setKassaId(Long kassaId) {
        this.kassaId = kassaId;
    }

    public Long getCashierId() {
        return cashierId;
    }

    public void setCashierId(Long cashierId) {
        this.cashierId = cashierId;
    }
}
