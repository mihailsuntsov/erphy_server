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

import java.util.Set;

public class ShipmentForm {
    private Long id;
    private Long company_id;
    private String description;
    private Long department_id;
    private Long cagent_id;
    private String doc_number;
    private String shipment_date;
    private boolean is_completed;
    private Set<ShipmentProductForm> shipmentProductTable;
    private boolean nds;
    private boolean nds_included;
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String  parent_uid;// uid исходящего (родительского) документа
    private String  child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.

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

    public boolean isNds() {
        return nds;
    }

    public void setNds(boolean nds) {
        this.nds = nds;
    }

    public boolean isNds_included() {
        return nds_included;
    }

    public void setNds_included(boolean nds_included) {
        this.nds_included = nds_included;
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

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public String getShipment_date() {
        return shipment_date;
    }

    public void setShipment_date(String shipment_date) {
        this.shipment_date = shipment_date;
    }

    public boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
    }

    public Set<ShipmentProductForm> getShipmentProductTable() {
        return shipmentProductTable;
    }

    public void setShipmentProductTable(Set<ShipmentProductForm> shipmentProductTable) {
        this.shipmentProductTable = shipmentProductTable;
    }

    @Override
    public String toString() {
        return "ShipmentForm: id=" + this.id + ", company_id=" + this.company_id +
                ", department_id=" + this.department_id + ", cagent_id=" + this.cagent_id;
    }
}
