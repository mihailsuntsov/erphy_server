package com.dokio.message.request.store.woo.v3.orders;

import java.util.List;

public class OrdersForm {

    private String crmSecretKey;
    private List<OrderForm> orders;

    public String getCrmSecretKey() {
        return crmSecretKey;
    }

    public void setCrmSecretKey(String crmSecretKey) {
        this.crmSecretKey = crmSecretKey;
    }

    public List<OrderForm> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderForm> orders) {
        this.orders = orders;
    }
}
