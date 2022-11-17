package com.dokio.message.response.additional;

import com.dokio.message.request.ProductProductAttributeForm;

import java.util.List;

public class ProductProductAttributesJSON {

    private List<ProductProductAttributeJSON> productAttributesList;

    public List<ProductProductAttributeJSON> getProductAttributesList() {
        return productAttributesList;
    }

    public void setProductAttributesList(List<ProductProductAttributeJSON> productAttributesList) {
        this.productAttributesList = productAttributesList;
    }
}
