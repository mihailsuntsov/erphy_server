package com.dokio.message.response.store.woo.v3.products;

import java.util.List;

public class VariationJSON {
    private Long                            crm_id;
    private Integer                         woo_id;
    private String                          name;
    private String                          slug;
    private String                          regular_price;
    private String                          sale_price;
    private String                          description;
    private String                          short_description;
    private String                          stock_status; //Controls the stock status of the product. Options: instock, outofstock, onbackorder. Default is instock.
    private String                          sku;
    private Integer                         stock_quantity;
    private Boolean                         sold_individually;
    private Boolean                         manage_stock; //Stock management at product level. Default is false.
    private String                          backorders;
    private String                          purchase_note;
    private Integer                         menu_order;
    private Boolean                         reviews_allowed;
    private ImageJSON                       image;
    private Integer                         parent_product_woo_id;
    private List<VariationAttributesJSON>   listOfVariationAttributes;

    public List<VariationAttributesJSON> getListOfVariationAttributes() {
        return listOfVariationAttributes;
    }

    public void setListOfVariationAttributes(List<VariationAttributesJSON> listOfVariationAttributes) {
        this.listOfVariationAttributes = listOfVariationAttributes;
    }

    public Integer getParent_product_woo_id() {
        return parent_product_woo_id;
    }

    public void setParent_product_woo_id(Integer parent_product_woo_id) {
        this.parent_product_woo_id = parent_product_woo_id;
    }

    public Long getCrm_id() {
        return crm_id;
    }

    public void setCrm_id(Long crm_id) {
        this.crm_id = crm_id;
    }

    public Integer getWoo_id() {
        return woo_id;
    }

    public void setWoo_id(Integer woo_id) {
        this.woo_id = woo_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getRegular_price() {
        return regular_price;
    }

    public void setRegular_price(String regular_price) {
        this.regular_price = regular_price;
    }

    public String getSale_price() {
        return sale_price;
    }

    public void setSale_price(String sale_price) {
        this.sale_price = sale_price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public String getStock_status() {
        return stock_status;
    }

    public void setStock_status(String stock_status) {
        this.stock_status = stock_status;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getStock_quantity() {
        return stock_quantity;
    }

    public void setStock_quantity(Integer stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public Boolean getSold_individually() {
        return sold_individually;
    }

    public void setSold_individually(Boolean sold_individually) {
        this.sold_individually = sold_individually;
    }

    public Boolean getManage_stock() {
        return manage_stock;
    }

    public void setManage_stock(Boolean manage_stock) {
        this.manage_stock = manage_stock;
    }

    public String getBackorders() {
        return backorders;
    }

    public void setBackorders(String backorders) {
        this.backorders = backorders;
    }

    public String getPurchase_note() {
        return purchase_note;
    }

    public void setPurchase_note(String purchase_note) {
        this.purchase_note = purchase_note;
    }

    public Integer getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(Integer menu_order) {
        this.menu_order = menu_order;
    }

    public Boolean getReviews_allowed() {
        return reviews_allowed;
    }

    public void setReviews_allowed(Boolean reviews_allowed) {
        this.reviews_allowed = reviews_allowed;
    }

    public ImageJSON getImage() {
        return image;
    }

    public void setImage(ImageJSON image) {
        this.image = image;
    }
}
