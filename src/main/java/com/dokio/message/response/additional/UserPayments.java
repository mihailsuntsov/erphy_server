package com.dokio.message.response.additional;


import java.math.BigDecimal;

public class UserPayments {

    private String for_what_date;
    private BigDecimal operation_sum;
    private String  operation_type;

    public String getOperation_type() {
        return operation_type;
    }

    public void setOperation_type(String operation_type) {
        this.operation_type = operation_type;
    }

    public String getFor_what_date() {
        return for_what_date;
    }

    public void setFor_what_date(String for_what_date) {
        this.for_what_date = for_what_date;
    }

    public BigDecimal getOperation_sum() {
        return operation_sum;
    }

    public void setOperation_sum(BigDecimal operation_sum) {
        this.operation_sum = operation_sum;
    }
}
