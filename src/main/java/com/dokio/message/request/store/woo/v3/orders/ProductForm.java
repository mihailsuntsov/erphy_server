package com.dokio.message.request.store.woo.v3.orders;

import java.util.List;

public class ProductForm {

    private int id;
    private String name;
    private int product_id;
    private int variation_id;
    private int quantity;
    private String tax_class;
    private String subtotal;
    private String subtotal_tax;
    private String total;
    private String total_tax;
    private List<ProductTaxForm> taxes;
    private String sku;
    private String price;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public int getVariation_id() {
        return variation_id;
    }

    public void setVariation_id(int variation_id) {
        this.variation_id = variation_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTax_class() {
        return tax_class;
    }

    public void setTax_class(String tax_class) {
        this.tax_class = tax_class;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public String getSubtotal_tax() {
        return subtotal_tax;
    }

    public void setSubtotal_tax(String subtotal_tax) {
        this.subtotal_tax = subtotal_tax;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTotal_tax() {
        return total_tax;
    }

    public void setTotal_tax(String total_tax) {
        this.total_tax = total_tax;
    }

    public List<ProductTaxForm> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<ProductTaxForm> taxes) {
        this.taxes = taxes;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
