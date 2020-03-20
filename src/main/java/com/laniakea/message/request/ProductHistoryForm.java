package com.laniakea.message.request;

public class ProductHistoryForm {

    private Long    companyId; //шв предприятия
    private String  departmentId;//стринг т.к. может быть как номер, так и строка номеров через запятую
    private Long    productId;  // id товара
    private String  dateFrom; //с даты
    private String  dateTo; //по дату;
    private String  sortColumn;//колонка сортировки
    private String  offset;//с какой строки (по умолчанию 0)
    private String  sortAsc; // asc/desc
    private String  result;//количество строк
    private String  dockTypesIds;//строка с id типов документов из таблицы documents

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
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

    public String getDockTypesIds() {
        return dockTypesIds;
    }

    public void setDockTypesIds(String dockTypesIds) {
        this.dockTypesIds = dockTypesIds;
    }
}
