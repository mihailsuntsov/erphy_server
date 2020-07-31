/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
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
