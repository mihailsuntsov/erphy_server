package com.dokio.message.response.store.woo.v3;

import java.util.List;

public class TaxesJSON {
    private Integer queryResultCode; // look at _ErrorCodes file
    private List<TaxJSON> taxes;

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public List<TaxJSON> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<TaxJSON> taxes) {
        this.taxes = taxes;
    }
}
