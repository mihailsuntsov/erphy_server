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

public class SpravSysEdizmForm {

    private Long id;
    private String name;
    private String short_name;
    private String type_id;
    private String equals_si;
    private String company_id;
    private String master_id;
    private String creator_id;
    private String changer_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_) {
        this.type_id = type_;
    }

    public String getEquals_si() {
        return equals_si;
    }

    public void setEquals_si(String equals_si) {
        this.equals_si = equals_si;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getMaster_id() {
        return master_id;
    }

    public void setMaster_id(String master_id) {
        this.master_id = master_id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(String changer_id) {
        this.changer_id = changer_id;
    }

    @Override
    public String toString() {
        return "SpravSysEdizmForm: id=" + this.id + ", name" + this.name + ", short_name" + this.short_name +
                ", type_id" + this.type_id + ", company_id" + this.company_id + ", master_id" + this.master_id +
                ", creator_id" + this.creator_id + ", changer_id" + this.changer_id;
    }
}
