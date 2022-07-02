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
