package com.dokio.message.response.store.woo.v3.products;

import java.util.List;

public class VariationsJSON {

    private Integer queryResultCode; // look at _ErrorCodes file
    private List<VariationJSON> variations;

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public List<VariationJSON> getVariations() {
        return variations;
    }

    public void setVariations(List<VariationJSON> variations) {
        this.variations = variations;
    }
}
