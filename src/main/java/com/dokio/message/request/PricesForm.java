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
public class PricesForm {

        private String          sortColumn;
        private String          searchString;
        private String          offset;
        private String          sortAsc;
        private String          result;
        private Long            companyId;
        private Long            cagentId;
        private Long            categoryId;

        private Long            priceTypeId;
        private Set<Long>       priceTypesIds;
        private String          priceTypesIdsList;

        private BigDecimal      priceValue;

        private Set<Long>       productsIds;
        private String          productsIdsList;

        private Set<Integer>    filterOptionsIds;


    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
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

    public Long getCompanyId() {
        return companyId;
    }

    public Set<Long> getPriceTypesIds() {
        return priceTypesIds;
    }

    public void setPriceTypesIds(Set<Long> priceTypesIds) {
        this.priceTypesIds = priceTypesIds;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public BigDecimal getPriceValue() {
        return priceValue;
    }

    public String getPriceTypesIdsList() {
        return priceTypesIdsList;
    }

    public void setPriceTypesIdsList(String priceTypesIdsList) {
        this.priceTypesIdsList = priceTypesIdsList;
    }

    public void setPriceValue(BigDecimal priceValue) {
        this.priceValue = priceValue;
    }


    public Long getPriceTypeId() {
        return priceTypeId;
    }

    public void setPriceTypeId(Long priceTypeId) {
        this.priceTypeId = priceTypeId;
    }

    public Long getCagentId() {
        return cagentId;
    }

    public void setCagentId(Long cagentId) {
        this.cagentId = cagentId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Set<Long> getProductsIds() {
        return productsIds;
    }

    public void setProductsIds(Set<Long> productsIds) {
        this.productsIds = productsIds;
    }

    public String getProductsIdsList() {
        return productsIdsList;
    }

    public void setProductsIdsList(String productsIdsList) {
        this.productsIdsList = productsIdsList;
    }

    public Set<Integer> getFilterOptionsIds() {
        return filterOptionsIds;
    }

    public void setFilterOptionsIds(Set<Integer> filterOptionsIds) {
        this.filterOptionsIds = filterOptionsIds;
    }

    @Override
    public String toString() {
        return "PricesForm: companyId=" + this.companyId + ", cagentId" + this.cagentId + ", categoryId" + this.categoryId +
                ", priceTypeId" + this.priceTypeId + ", priceTypesIdsList" + this.priceTypesIdsList;
    }
}
