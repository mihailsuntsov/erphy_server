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
package com.dokio.message.request;

import java.math.BigDecimal;

public class KassaForm {
    private Long id;
    private Long company_id;
    private Long department_id;
    private String name;
    private String server_type;
    private int sno1_id;
    private String device_server_uid;
    private String additional;
    private String server_address;
    private String billing_address;
    private Boolean allow_to_use;
    private Boolean is_deleted;
    private String zn_kkt;
    private Boolean is_virtual; //виртуальная касса
    private Boolean allow_acquiring; //прием безнала на данной кассе
    private Long acquiring_bank_id; // id банк-эквайер
    private BigDecimal acquiring_precent; // процент банку за услугу эквайринга
    private Long acquiring_service_id; // id услуги банка-эквайера
    private Long payment_account_id; // id расчетного счета
    private Long expenditure_id; // id статьи расходов

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

    public Long getAcquiring_service_id() {
        return acquiring_service_id;
    }

    public void setAcquiring_service_id(Long acquiring_service_id) {
        this.acquiring_service_id = acquiring_service_id;
    }

    public Boolean getIs_virtual() {
        return is_virtual;
    }

    public void setIs_virtual(Boolean is_virtual) {
        this.is_virtual = is_virtual;
    }

    public Boolean getAllow_acquiring() {
        return allow_acquiring;
    }

    public void setAllow_acquiring(Boolean allow_acquiring) {
        this.allow_acquiring = allow_acquiring;
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

    public String getZn_kkt() {
        return zn_kkt;
    }

    public void setZn_kkt(String zn_kkt) {
        this.zn_kkt = zn_kkt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer_type() {
        return server_type;
    }

    public void setServer_type(String server_type) {
        this.server_type = server_type;
    }

    public int getSno1_id() {
        return sno1_id;
    }

    public void setSno1_id(int sno1_id) {
        this.sno1_id = sno1_id;
    }

    public String getDevice_server_uid() {
        return device_server_uid;
    }

    public String getBilling_address() {
        return billing_address;
    }

    public void setBilling_address(String billing_address) {
        this.billing_address = billing_address;
    }

    public void setDevice_server_uid(String device_server_uid) {
        this.device_server_uid = device_server_uid;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getServer_address() {
        return server_address;
    }

    public void setServer_address(String server_address) {
        this.server_address = server_address;
    }

    public Boolean getAllow_to_use() {
        return allow_to_use;
    }

    public void setAllow_to_use(Boolean allow_to_use) {
        this.allow_to_use = allow_to_use;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }
}
