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

import java.math.BigDecimal;

public class KassaJSON {
    private Long id;
    private String master;
    private String creator;
    private String changer;
    private String cagent;
    private Long   master_id;
    private Long   cagent_id;
    private Long   creator_id;
    private Long   changer_id;
    private Long   company_id;
    private Long   department_id;
    private String department;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String name;
    private String server_type;
    private int    sno1_id;
    private String sno1_name_api_atol;
    private String device_server_uid;
    private String additional;
    private String server_address;
    private Boolean allow_to_use;
    private Boolean is_deleted;
    private String company_name;
    private String company_email;
    private String company_vatin;
    private String billing_address;
    private String zn_kkt;

    private Boolean is_virtual; //виртуальная касса
    private Boolean allow_acquiring; //прием безнала на данной кассе
    private Long acquiring_bank_id; // id банк-эквайер
    private BigDecimal acquiring_precent; // процент банку за услугу эквайринга
    private String acquiring_bank; // банк-эквайер

    public String getAcquiring_bank() {
        return acquiring_bank;
    }

    public void setAcquiring_bank(String acquiring_bank) {
        this.acquiring_bank = acquiring_bank;
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


    public String getCompany_vatin() {
        return company_vatin;
    }

    public void setCompany_vatin(String company_vatin) {
        this.company_vatin = company_vatin;
    }

    public String getCompany_email() {
        return company_email;
    }

    public void setCompany_email(String company_email) {
        this.company_email = company_email;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public Long getId() {
        return id;
    }

    public String getSno1_name_api_atol() {
        return sno1_name_api_atol;
    }

    public void setSno1_name_api_atol(String sno1_name_api_atol) {
        this.sno1_name_api_atol = sno1_name_api_atol;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Long changer_id) {
        this.changer_id = changer_id;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
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

    public String getBilling_address() {
        return billing_address;
    }

    public void setBilling_address(String billing_address) {
        this.billing_address = billing_address;
    }
}
