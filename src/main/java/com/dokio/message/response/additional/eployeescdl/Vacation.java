package com.dokio.message.response.additional.eployeescdl;

import java.math.BigDecimal;

public class Vacation {

    private Long        id;
    private String      name;
    private Boolean     is_paid;
    private BigDecimal  payment_per_day;

//    public Vacation(String name, Boolean is_paid, BigDecimal payment_per_day) {
//        this.name = name;
//        this.is_paid = is_paid;
//        this.payment_per_day = payment_per_day;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIs_paid() {
        return is_paid;
    }

    public void setIs_paid(Boolean is_paid) {
        this.is_paid = is_paid;
    }

    public BigDecimal getPayment_per_day() {
        return payment_per_day;
    }

    public void setPayment_per_day(BigDecimal payment_per_day) {
        this.payment_per_day = payment_per_day;
    }
}
