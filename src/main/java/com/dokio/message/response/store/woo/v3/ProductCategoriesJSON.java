package com.dokio.message.response.store.woo.v3;

import java.util.List;

public class ProductCategoriesJSON {

    private Integer queryResultCode; // look at _ErrorCodes file
    private List<ProductCategoryJSON> productCategories;

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
