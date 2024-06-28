package com.dokio.message.request.additional;

import java.util.Set;

public class AppointmentDocsListSearchForm {

    private String  searchString;           //  строка поиска
    private Long    companyId;              //  id предприятия
    private Long    departmentId;           //  id отделения
    private Long    appointmentId;          //  id документа из которого идет вызов
    private Long    customerId;             //  id заказчика
    private String  sortColumn;             //  колонка сортировки
    private String  sortAsc;                //  asc desc
    private Integer result;                 //  количество записей, отображаемых на странице
    private Integer pagenum;                //  отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
    private Integer offset;                 //  это номер страницы начиная с 0. Изначально (при первом запросе таблицы) это null
    private String  dateFrom;               //  с даты
    private String  dateTo;                 //  по дату
    private Set<Integer> filterOptionsIds;  //  опции поиска


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

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Integer getPagenum() {
        return pagenum;
    }

    public void setPagenum(Integer pagenum) {
        this.pagenum = pagenum;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
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

    public Set<Integer> getFilterOptionsIds() {
        return filterOptionsIds;
    }

    public void setFilterOptionsIds(Set<Integer> filterOptionsIds) {
        this.filterOptionsIds = filterOptionsIds;
    }

    @Override
    public String toString() {
        return "AppointmentDocsListSearchForm{" +
                "searchString='" + searchString + '\'' +
                ", companyId=" + companyId +
                ", departmentId=" + departmentId +
                ", appointmentId=" + appointmentId +
                ", customerId=" + customerId +
                ", sortColumn='" + sortColumn + '\'' +
                ", sortAsc='" + sortAsc + '\'' +
                ", result=" + result +
                ", pagenum=" + pagenum +
                ", offset=" + offset +
                ", dateFrom='" + dateFrom + '\'' +
                ", dateTo='" + dateTo + '\'' +
                ", filterOptionsIds=" + filterOptionsIds +
                '}';
    }
}
