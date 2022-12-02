package com.dokio.message.response.store.woo.v3.products;

import java.util.List;

public class ProductsJSON {
    private Integer queryResultCode; // look at _ErrorCodes file
    private List<ProductJSON> products;

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public void setProducts(List<ProductJSON> products) {
        this.products = products;
    }
}
