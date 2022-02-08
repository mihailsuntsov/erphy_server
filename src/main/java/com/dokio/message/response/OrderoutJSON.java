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

package com.dokio.message.response;

import java.math.BigDecimal;

public class OrderoutJSON {

    private Long id;
    private String master;
    private String creator;
    private String changer;
    private String cagent;
    private Long master_id;
    private Long cagent_id;
    private Long creator_id;
    private Long changer_id;
    private Long company_id;
    private Long doc_number;
    private String company;
    private String date_time_created;
    private String date_time_changed;
    private String description;
    private BigDecimal nds;
    private String status_name;
    private String status_color;
    private String status_description;
    private Long status_id;
    private Boolean is_delivered;               // для внутренних переводов - доставлено или нет
    private Boolean is_completed;               // проведено
    private String uid;
    private BigDecimal summ;
    private Long expenditure_id; // id вида расходов
    private String expenditure; // вид расходов
    private String expenditure_type; // тип вида расходов. Например вид расходов - Внутренний платеж, его тип - moving

    private Long kassa_department_id;// id отделения, где находится касса ККМ, в которую будет внесение
    private String kassa_department;// отделение, где находится касса ККМ, в которую будет внесение

    private String moving_type; // тип внутреннего перемещения денежных средств: boxoffice - касса предприятия (не путать с ККМ!), account - банковский счёт препдриятия
    private Long boxoffice_id; // id кассы предприятия (не путать с ККМ!)
    private String boxoffice; // наименование кассы предприятия (не путать с ККМ!)
    private Long payment_account_to_id;  //  банковский счёт препдриятия, куда перемещаем денежные средства
    private Long boxoffice_to_id; // касса предприятия куда пермещаем ден. ср-ва
    private Long kassa_to_id;             // id кассы ККМ - назначения (куда производим внесение ден средств)
    private String kassa_to;             // касса ККМ - назначения (куда производим внесение ден средств)

    public Boolean getIs_delivered() {
        return is_delivered;
    }

    public String getExpenditure_type() {
        return expenditure_type;
    }

    public void setExpenditure_type(String expenditure_type) {
        this.expenditure_type = expenditure_type;
    }

    public void setIs_delivered(Boolean is_delivered) {
        this.is_delivered = is_delivered;
    }

    public String getKassa_to() {
        return kassa_to;
    }

    public void setKassa_to(String kassa_to) {
        this.kassa_to = kassa_to;
    }

    public Long getKassa_department_id() {
        return kassa_department_id;
    }

    public void setKassa_department_id(Long kassa_department_id) {
        this.kassa_department_id = kassa_department_id;
    }

    public String getKassa_department() {
        return kassa_department;
    }

    public void setKassa_department(String kassa_department) {
        this.kassa_department = kassa_department;
    }

    public String getBoxoffice() {
        return boxoffice;
    }

    public void setBoxoffice(String boxoffice) {
        this.boxoffice = boxoffice;
    }

    public Long getKassa_to_id() {
        return kassa_to_id;
    }

    public void setKassa_to_id(Long kassa_to_id) {
        this.kassa_to_id = kassa_to_id;
    }

    public Long getBoxoffice_to_id() {
        return boxoffice_to_id;
    }

    public void setBoxoffice_to_id(Long boxoffice_to_id) {
        this.boxoffice_to_id = boxoffice_to_id;
    }

    public String getMoving_type() {
        return moving_type;
    }

    public void setMoving_type(String moving_type) {
        this.moving_type = moving_type;
    }

    public Long getBoxoffice_id() {
        return boxoffice_id;
    }

    public void setBoxoffice_id(Long boxoffice_id) {
        this.boxoffice_id = boxoffice_id;
    }

    public Long getPayment_account_to_id() {
        return payment_account_to_id;
    }

    public void setPayment_account_to_id(Long payment_account_to_id) {
        this.payment_account_to_id = payment_account_to_id;
    }

    public String getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(String expenditure) {
        this.expenditure = expenditure;
    }

    public Long getExpenditure_id() {
        return expenditure_id;
    }

    public void setExpenditure_id(Long expenditure_id) {
        this.expenditure_id = expenditure_id;
    }

    public Long getId() {
        return id;
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

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getNds() {
        return nds;
    }

    public void setNds(BigDecimal nds) {
        this.nds = nds;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getStatus_color() {
        return status_color;
    }

    public void setStatus_color(String status_color) {
        this.status_color = status_color;
    }

    public String getStatus_description() {
        return status_description;
    }

    public void setStatus_description(String status_description) {
        this.status_description = status_description;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

}