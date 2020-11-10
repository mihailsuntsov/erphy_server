/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
//Класс для преобразования запросов ПОИСКА в объект
//Например, в Контроллере:
//    @PostMapping("/api/auth/getSize")
//    public ResponseEntity<?> searchRequest(@RequestBody SearchForm searchRequest){
//    //@RequestBody указывает, что параметр метода должен быть привязан к значению тела запроса HTTP.
//    //Запрос преобразовывается в объект (в данном случае в данный класс)
package com.dokio.message.request;

import java.util.Set;

public class SearchForm {
    private int id;
    private int id2;
    private String masterId; //id владельца (аккаунта) записи
    private String searchString; //строка поиска
    private String companyId; //id предприятия
    private String employeeId; //id сотрудника
    private String departmentId; //id предприятия
    private String documentId; //id документа
    private String parentId; //id родителя
    private String dateFrom; //с даты
    private String dateTo; //по дату
    private boolean has_parent;//есть ли родитель
    private String sortColumn; //колонка сортировки
    private String sortAsc; // asc desc
    private String result; //  количество записей, отображаемых на странице
    private String pagenum;//  отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
    private String offset; //  это номер страницы начиная с 0. Изначально (при первом запросе таблицы) это null

    private String groupId; // id группы товаров, которой принадлежат поля или сеты
    private String field_type; // тип: 1 - сеты (наборы) полей, 2 - поля, 0 - все
    private String parentSetId;// родительский сет поля
    private String categoryId;// родительский сет поля
    private boolean any_boolean;// универсальная для любого логического параметра запроса
    private boolean any_boolean2;// универсальная для любого логического параметра запроса
    private Integer any_id;// универсальная для любого id
    private Set<Integer> filterOptionsIds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public boolean isAny_boolean() {
        return any_boolean;
    }

    public Integer getAny_id() {
        return any_id;
    }

    public Set<Integer> getFilterOptionsIds() {
        return filterOptionsIds;
    }

    public void setFilterOptionsIds(Set<Integer> filterOptionsIds) {
        this.filterOptionsIds = filterOptionsIds;
    }

    public void setAny_id(Integer any_id) {
        this.any_id = any_id;
    }

    public void setAny_boolean(boolean any_boolean) {
        this.any_boolean = any_boolean;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAny_boolean2() {
        return any_boolean2;
    }

    public void setAny_boolean2(boolean any_boolean2) {
        this.any_boolean2 = any_boolean2;
    }

    public String getSortAsc() {
        return sortAsc;
    }

    public boolean isHas_parent() {
        return has_parent;
    }

    public void setHas_parent(boolean has_parent) {
        this.has_parent = has_parent;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getField_type() {
        return field_type;
    }

    public void setField_type(String field_type) {
        this.field_type = field_type;
    }

    public String getParentSetId() {
        return parentSetId;
    }

    public void setParentSetId(String parentSetId) {
        this.parentSetId = parentSetId;
    }

    @Override
    public String toString() {
        return "SearchForm: id=" + this.id + ", masterId=" + this.masterId + ", searchString=" + this.searchString;
    }
}
