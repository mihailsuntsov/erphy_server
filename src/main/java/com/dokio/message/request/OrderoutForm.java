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
package com.dokio.message.request;

import java.math.BigDecimal;

public class OrderoutForm {

    private Long id;
    private Long company_id;
    private String description;
    private Long cagent_id;
    private String new_cagent;
    private Long status_id;
    private String doc_number;
    private BigDecimal nds;
    private BigDecimal summ;
    private Long expenditure_id; // id вида расходов
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String parent_uid;// uid исходящего (родительского) документа
    private String child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Boolean is_completed;// проведён
    private Long kassa_department_id;// отделение, где находится касса ККМ, в которую будет внесение

    private String moving_type; // тип внутреннего перемещения денежных средств: boxoffice - касса предприятия (не путать с ККМ!), account - банковский счёт препдриятия
    private Long boxoffice_id; // касса предприятия (не путать с ККМ!) из которой производится выплата
    private Long payment_account_to_id;  //  банковский счёт препдриятия, куда перемещаем денежные средства
    private Long boxoffice_to_id; // касса предприятия куда пермещаем ден. ср-ва
    private Long kassa_to_id;             // id кассы ККМ - назначения платежа (куда производится внесение)
    private Boolean internal;               // внутренний перевод
    private Long department_id;             // отделение, из которого создают приходный ордер. Нужно для определения кассы, привязанной к отделению

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getKassa_department_id() {
        return kassa_department_id;
    }

    public void setKassa_department_id(Long kassa_department_id) {
        this.kassa_department_id = kassa_department_id;
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

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public String getNew_cagent() {
        return new_cagent;
    }

    public void setNew_cagent(String new_cagent) {
        this.new_cagent = new_cagent;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public BigDecimal getNds() {
        return nds;
    }

    public void setNds(BigDecimal nds) {
        this.nds = nds;
    }

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getLinked_doc_id() {
        return linked_doc_id;
    }

    public void setLinked_doc_id(Long linked_doc_id) {
        this.linked_doc_id = linked_doc_id;
    }

    public String getLinked_doc_name() {
        return linked_doc_name;
    }

    public void setLinked_doc_name(String linked_doc_name) {
        this.linked_doc_name = linked_doc_name;
    }

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }
}