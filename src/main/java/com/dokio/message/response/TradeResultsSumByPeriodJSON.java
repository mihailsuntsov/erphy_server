/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.response;

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
