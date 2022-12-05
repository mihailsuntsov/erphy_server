package com.dokio.message.response.store.woo.v3.products;

import java.math.BigInteger;

public class ProductCountJSON {
    private BigInteger productCount;
    private Integer queryResultCode; // look at _ErrorCodes file

    public BigInteger getProductCount() {
        return productCount;
    }

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setProductCount(BigInteger productCount) {
        this.productCount = productCount;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }
}
