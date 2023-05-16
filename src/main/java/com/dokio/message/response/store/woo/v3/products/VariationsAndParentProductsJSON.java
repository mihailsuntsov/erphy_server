package com.dokio.message.response.store.woo.v3.products;

import java.util.List;

public class VariationsAndParentProductsJSON {
    private Integer queryResultCode; // look at _ErrorCodes file
    private List<VariationAndParentProductJSON> variations_and_parent_products;

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public List<VariationAndParentProductJSON> getVariations_and_parent_products() {
        return variations_and_parent_products;
    }

    public void setVariations_and_parent_products(List<VariationAndParentProductJSON> variations_and_parent_products) {
        this.variations_and_parent_products = variations_and_parent_products;
    }
}
