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
package com.dokio.message.response.store.woo.v3;

public class ProductAttributeJSON {
    private Long            crm_id;
    private Integer         woo_id;
    private String          name;
    private String          type;
    private String          slug;
    private String          order_by;
    private Boolean         has_archives;

    public Long getCrm_id() {
        return crm_id;
    }

    public void setCrm_id(Long crm_id) {
        this.crm_id = crm_id;
    }

    public Integer getWoo_id() {
        return woo_id;
    }

    public void setWoo_id(Integer woo_id) {
        this.woo_id = woo_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getOrder_by() {
        return order_by;
    }

    public void setOrder_by(String order_by) {
        this.order_by = order_by;
    }

    public Boolean getHas_archives() {
        return has_archives;
    }

    public void setHas_archives(Boolean has_archives) {
        this.has_archives = has_archives;
    }
}
