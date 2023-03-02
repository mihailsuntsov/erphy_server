package com.dokio.message.response.store.woo.v3.products;

//import java.util.List;
import java.util.Set;

public class ProductsJSON {
    private Integer queryResultCode; // look at _ErrorCodes file
    private Set<ProductJSON> products;

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public Set<ProductJSON> getProducts() {
        return products;
    }

    public void setProducts(Set<ProductJSON> products) {
        this.products = products;
    }
}
