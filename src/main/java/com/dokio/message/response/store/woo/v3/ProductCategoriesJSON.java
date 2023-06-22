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

package com.dokio.message.response.store.woo.v3;

import java.util.List;

public class ProductCategoriesJSON {

    private Integer queryResultCode; // look at _ErrorCodes file
    private List<ProductCategoryJSON> productCategories;
    private List<Integer>  allProductCategoriesWooIds; // all Woo Ids that there is

    public List<Integer> getAllProductCategoriesWooIds() {
        return allProductCategoriesWooIds;
    }

    public void setAllProductCategoriesWooIds(List<Integer> allProductCategoriesWooIds) {
        this.allProductCategoriesWooIds = allProductCategoriesWooIds;
    }

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public List<ProductCategoryJSON> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<ProductCategoryJSON> productCategories) {
        this.productCategories = productCategories;
    }
}
