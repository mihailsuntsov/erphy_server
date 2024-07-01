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

package com.dokio.message.request;

import java.math.BigDecimal;
import java.util.Set;

public class PaymentinForm {

    private Long id;
    private Long company_id;
    private String description;
    private Long cagent_id;
    private String new_cagent;
    private Long status_id;
    private String doc_number;
    private BigDecimal nds;
    private BigDecimal summ;
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String parent_uid;// uid исходящего (родительского) документа
    private String child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Boolean is_completed;// проведён
    private String income_number;
    private String income_number_date;
    private Long payment_account_id;//id расчтёного счёта
    private Boolean internal; // внутренний платеж
    private Long department_id; //отделение, из которого создают входящий платеж. Нужно для определения расчетного счета, привязанного к отделению
    private String moving_type;             // тип перевода (источник): касса ККМ (kassa), касса предприятия (boxoffice), расч. счёт (account)
    private Long boxoffice_from_id;         // id кассы предприятия - источника
    private Long payment_account_from_id;   // id расч счёта
    private Long paymentout_id;             // id исходящего платежа, из которого поступили средства
    private Long orderout_id;               // id расходного ордера, из которого поступили средства

    public Long getPaymentout_id() {
        return paymentout_id;
    }

    public void setPaymentout_id(Long paymentout_id) {
        this.paymentout_id = paymentout_id;
    }

    public Long getOrderout_id() {
        return orderout_id;
    }

    public void setOrderout_id(Long orderout_id) {
        this.orderout_id = orderout_id;
    }

    public String getMoving_type() {
        return moving_type;
    }

    public void setMoving_type(String moving_type) {
        this.moving_type = moving_type;
    }

    public Long getBoxoffice_from_id() {
        return boxoffice_from_id;
    }

    public void setBoxoffice_from_id(Long boxoffice_from_id) {
        this.boxoffice_from_id = boxoffice_from_id;
    }

    public Long getPayment_account_from_id() {
        return payment_account_from_id;
    }

    public void setPayment_account_from_id(Long payment_account_from_id) {
        this.payment_account_from_id = payment_account_from_id;
    }


    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Long getPayment_account_id() {
        return payment_account_id;
    }

    public void setPayment_account_id(Long payment_account_id) {
        this.payment_account_id = payment_account_id;
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

    public String getIncome_number() {
        return income_number;
    }

    public void setIncome_number(String income_number) {
        this.income_number = income_number;
    }

    public String getIncome_number_date() {
        return income_number_date;
    }

    public void setIncome_number_date(String income_number_date) {
        this.income_number_date = income_number_date;
    }

    @Override
    public String toString() {
        return "PaymentinForm{" +
                "id=" + id +
                ", company_id=" + company_id +
                ", description='" + description + '\'' +
                ", cagent_id=" + cagent_id +
                ", new_cagent='" + new_cagent + '\'' +
                ", status_id=" + status_id +
                ", doc_number='" + doc_number + '\'' +
                ", nds=" + nds +
                ", summ=" + summ +
                ", uid='" + uid + '\'' +
                ", linked_doc_id=" + linked_doc_id +
                ", linked_doc_name='" + linked_doc_name + '\'' +
                ", parent_uid='" + parent_uid + '\'' +
                ", child_uid='" + child_uid + '\'' +
                ", is_completed=" + is_completed +
                ", income_number='" + income_number + '\'' +
                ", income_number_date='" + income_number_date + '\'' +
                ", payment_account_id=" + payment_account_id +
                ", internal=" + internal +
                ", department_id=" + department_id +
                ", moving_type='" + moving_type + '\'' +
                ", boxoffice_from_id=" + boxoffice_from_id +
                ", payment_account_from_id=" + payment_account_from_id +
                ", paymentout_id=" + paymentout_id +
                ", orderout_id=" + orderout_id +
                '}';
    }
}