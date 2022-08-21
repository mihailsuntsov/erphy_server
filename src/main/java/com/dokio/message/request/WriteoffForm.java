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

import java.util.Set;

public class WriteoffForm {
    private Long id;
    private Long company_id;
    private String description;
    private Long department_id;
    private Long cagent_id;
    private Integer doc_number;
    private String writeoff_date;
    private boolean is_completed;
    private Set<WriteoffProductForm> writeoffProductTable;                          // остаток на складе
    private Long    status_id;
    private String uid;
    private Long linked_doc_id;//id связанного документа (того, из которого инициируется создание данного документа)
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String  parent_uid;// uid исходящего (родительского) документа
    private String  child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private String writeoff_time;

    public String getWriteoff_time() {
        return writeoff_time;
    }

    public void setWriteoff_time(String writeoff_time) {
        this.writeoff_time = writeoff_time;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
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

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public Integer getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Integer doc_number) {
        this.doc_number = doc_number;
    }

    public String getWriteoff_date() {
        return writeoff_date;
    }

    public void setWriteoff_date(String writeoff_date) {
        this.writeoff_date = writeoff_date;
    }

    public boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Set<WriteoffProductForm> getWriteoffProductTable() {
        return writeoffProductTable;
    }

    public void setWriteoffProductTable(Set<WriteoffProductForm> writeoffProductTable) {
        this.writeoffProductTable = writeoffProductTable;
    }

//    public Long getReturn_id() {
//        return return_id;
//    }
//
//    public void setReturn_id(Long return_id) {
//        this.return_id = return_id;
//    }
//
//    public Long getInventory_id() {
//        return inventory_id;
//    }
//
//    public void setInventory_id(Long inventory_id) {
//        this.inventory_id = inventory_id;
//    }

    @Override
    public String toString() {
        return "WriteoffForm: id=" + this.id + ", company_id=" + this.company_id + ", description=" + this.description
                + ", department_id=" + this.department_id  + ", cagent_id=" + this.cagent_id + ", doc_number=" + this.doc_number
                + ", is_completed=";
    }
}
