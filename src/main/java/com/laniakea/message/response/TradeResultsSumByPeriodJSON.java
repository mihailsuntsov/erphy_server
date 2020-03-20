package com.laniakea.message.response;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TradeResultsSumByPeriodJSON {
    @Id
    private Long id;
    private String cash_all;
    private String cash_minus_encashment;
    private String total_incoming;
    private String checkout_all;

    public String getCash_all() {
        return cash_all;
    }

    public void setCash_all(String cash_all) {
        this.cash_all = cash_all;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCash_minus_encashment() {
        return cash_minus_encashment;
    }

    public void setCash_minus_encashment(String cash_minus_encashment) {
        this.cash_minus_encashment = cash_minus_encashment;
    }

    public String getTotal_incoming() {
        return total_incoming;
    }

    public void setTotal_incoming(String total_incoming) {
        this.total_incoming = total_incoming;
    }

    public String getCheckout_all() {
        return checkout_all;
    }

    public void setCheckout_all(String checkout_all) {
        this.checkout_all = checkout_all;
    }
}
