package com.dokio.message.request.additional;

public class LabelsPrintProduct {

    private Long    product_id;
    private String  product_name;
    private int     labels_quantity; // the quantity of labels that needs to be printed at this time

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public int getLabels_quantity() {
        return labels_quantity;
    }

    public void setLabels_quantity(int labels_quantity) {
        this.labels_quantity = labels_quantity;
    }
}
