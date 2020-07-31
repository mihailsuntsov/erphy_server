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
package com.dokio.message.request;

import java.math.BigDecimal;
import java.util.Set;

public class RemainsForm {

    private String      sortColumn;
    private String      searchString;
    private String      offset;
    private String      sortAsc;
    private String      result;
    private String      companyId;
    private String      cagentId;
    private String      categoryId;

    private BigDecimal  min_quantity;

    private Set<Long>   productsIds;
    private String      productsIdsList;

    private Long        departmentId;
    private Set<Long>   departmentsIds;
    private String      departmentsIdsList;

    private Set<Integer>filterOptionsIds;


    public Set<Integer> getFilterOptionsIds() {
        return filterOptionsIds;
    }

    public void setFilterOptionsIds(Set<Integer> filterOptionsIds) {
        this.filterOptionsIds = filterOptionsIds;
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

    public BigDecimal getMin_quantity() {
        return min_quantity;
    }

    public void setMin_quantity(BigDecimal min_quantity) {
        this.min_quantity = min_quantity;
    }

    public String getProductsIdsList() {
        return productsIdsList;
    }

    public void setProductsIdsList(String productsIdsList) {
        this.productsIdsList = productsIdsList;
    }

    public Set<Long> getDepartmentsIds() {
        return departmentsIds;
    }

    public void setDepartmentsIds(Set<Long> departmentsIds) {
        this.departmentsIds = departmentsIds;
    }

    public String getDepartmentsIdsList() {
        return departmentsIdsList;
    }

    public void setDepartmentsIdsList(String departmentsIdsList) {
        this.departmentsIdsList = departmentsIdsList;
    }

    public Set<Long> getProductsIds() {
        return productsIds;
    }

    public void setProductsIds(Set<Long> productsIds) {
        this.productsIds = productsIds;
    }

    public String getSortAsc() {
        return sortAsc;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
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

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getCagentId() {
        return cagentId;
    }

    public void setCagentId(String cagentId) {
        this.cagentId = cagentId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
