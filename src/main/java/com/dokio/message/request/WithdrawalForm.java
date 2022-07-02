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

public class WithdrawalForm {

    private Long        id;
    private Long        company_id;
    private Long        department_id;   // отделение в котором установлена касса
    private Long        creator_id;      // кассир, залогиненный на кассовом модуле (Внимание! не в системе! На кассем может залогиниться другой человек)
    private Long        kassa_id;        // id кассы
    private String      description;
    private String      doc_number;
    private BigDecimal  summ;
    private Boolean     is_completed;    // проведено - всегда true, т.к. выемка создается уже проведенной, не редактируется, не проводится и не удаляется,
    private Boolean     is_delivered;    // деньги доставлены до кассы предприятия (проведён документ Входящий ордер) (false = "зависшие деньги" - между кассой ККМ и кассой предприятия)
    private String      uid;
    private Long        linked_doc_id;   // id связанного документа
    private String      linked_doc_name; // имя (таблицы) связанного документа
    private String      parent_uid;      // uid исходящего (родительского) документа
    private String      child_uid;       // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Long        boxoffice_id;    // касса предприятия

    public Long getBoxoffice_id() {
        return boxoffice_id;
    }

    public void setBoxoffice_id(Long boxoffice_id) {
        this.boxoffice_id = boxoffice_id;
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

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getKassa_id() {
        return kassa_id;
    }

    public void setKassa_id(Long kassa_id) {
        this.kassa_id = kassa_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Boolean getIs_delivered() {
        return is_delivered;
    }

    public void setIs_delivered(Boolean is_delivered) {
        this.is_delivered = is_delivered;
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
}