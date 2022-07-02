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

package com.dokio.message.request.Sprav;

import java.util.List;

public class SpravStatusDocForm {

    private Long id;
    private Long company_id;
    private int doc_id;
    private String name;
    private int status_type;
    private String color;
    private int output_order;
    private String description;
    private boolean is_default;
    private List<Long> statusesIdsInOrderOfList;//List id статусов для упорядочивания по порядку вывода
    /*getters-setters*/

    public Long getId() {
        return id;
    }

    public boolean isIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
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

    public int getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(int doc_id) {
        this.doc_id = doc_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus_type() {
        return status_type;
    }

    public void setStatus_type(int status_type) {
        this.status_type = status_type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getOutput_order() {
        return output_order;
    }

    public void setOutput_order(int output_order) {
        this.output_order = output_order;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getStatusesIdsInOrderOfList() {
        return statusesIdsInOrderOfList;
    }

    public void setStatusesIdsInOrderOfList(List<Long> statusesIdsInOrderOfList) {
        this.statusesIdsInOrderOfList = statusesIdsInOrderOfList;
    }

    @Override
    public String toString() {
        return "SpravStatusDocForm: id=" + this.id + ", company_id" + this.company_id + ", dock_id" + this.doc_id +
                ", name" + this.name;
    }
}
