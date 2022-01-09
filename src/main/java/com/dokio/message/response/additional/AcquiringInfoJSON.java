/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
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
