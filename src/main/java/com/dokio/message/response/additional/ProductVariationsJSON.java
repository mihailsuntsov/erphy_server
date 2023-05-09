package com.dokio.message.response.additional;


import java.util.List;

public class ProductVariationsJSON {

    private Long id;
    private Long product_id;
    private Long menu_order;
    private Long variation_product_id;
    private String variation_product_name;
    private List<ProductVariationsRowItemsJSON> productVariationsRowItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(Long menu_order) {
        this.menu_order = menu_order;
    }

    public Long getVariation_product_id() {
        return variation_product_id;
    }

    public void setVariation_product_id(Long variation_product_id) {
        this.variation_product_id = variation_product_id;
    }

    public String getVariation_product_name() {
        return variation_product_name;
    }

    public void setVariation_product_name(String variation_product_name) {
        this.variation_product_name = variation_product_name;
    }

    public List<ProductVariationsRowItemsJSON> getProductVariationsRowItems() {
        return productVariationsRowItems;
    }

    public void setProductVariationsRowItems(List<ProductVariationsRowItemsJSON> productVariationsRowItems) {
        this.productVariationsRowItems = productVariationsRowItems;
    }
}
