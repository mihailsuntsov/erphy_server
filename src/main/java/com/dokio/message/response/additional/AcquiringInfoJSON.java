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

package com.dokio.message.response.additional;

import java.math.BigDecimal;

public class AcquiringInfoJSON {

    private Long acquiring_bank_id;
    private BigDecimal acquiring_precent;
    private Long acquiring_service_id;
    private Long payment_account_id;
    private Long expenditure_id;

    public Long getPayment_account_id() {
        return payment_account_id;
    }

    public void setPayment_account_id(Long payment_account_id) {
        this.payment_account_id = payment_account_id;
    }

    public Long getExpenditure_id() {
        return expenditure_id;
    }

    public void setExpenditure_id(Long expenditure_id) {
        this.expenditure_id = expenditure_id;
    }

    public Long getAcquiring_bank_id() {
        return acquiring_bank_id;
    }

    public void setAcquiring_bank_id(Long acquiring_bank_id) {
        this.acquiring_bank_id = acquiring_bank_id;
    }

    public BigDecimal getAcquiring_precent() {
        return acquiring_precent;
    }

    public void setAcquiring_precent(BigDecimal acquiring_precent) {
        this.acquiring_precent = acquiring_precent;
    }

    public Long getAcquiring_service_id() {
        return acquiring_service_id;
    }

    public void setAcquiring_service_id(Long acquiring_service_id) {
        this.acquiring_service_id = acquiring_service_id;
    }
}
