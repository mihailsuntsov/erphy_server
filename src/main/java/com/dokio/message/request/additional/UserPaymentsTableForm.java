package com.dokio.message.request.additional;

public class UserPaymentsTableForm {
    private String  sortColumn;             //  колонка сортировки
    private String  sortAsc;                //  asc desc
    private Integer result;                 //  количество записей, отображаемых на странице
    private Integer pagenum;                //  отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
    private Integer offset;                 //  это номер страницы начиная с 0. Изначально (при первом запросе таблицы) это null
    private String  dateFrom;               //  с даты
    private String  dateTo;                 //  по дату

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
}
