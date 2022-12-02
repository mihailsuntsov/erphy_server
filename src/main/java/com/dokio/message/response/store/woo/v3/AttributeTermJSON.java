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

public class AttributeTermJSON {

    private Long            crm_id;
    private Integer         woo_id;
    private Long            attribute_crm_id;
    private Integer         attribute_woo_id;
    private String          name;
    private String          description;
    private String          slug;
    private int             menu_order;

    public Long getAttribute_crm_id() {
        return attribute_crm_id;
    }

    public void setAttribute_crm_id(Long attribute_crm_id) {
        this.attribute_crm_id = attribute_crm_id;
    }

    public Integer getAttribute_woo_id() {
        return attribute_woo_id;
    }

    public void setAttribute_woo_id(Integer attribute_woo_id) {
        this.attribute_woo_id = attribute_woo_id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(int menu_order) {
        this.menu_order = menu_order;
    }
}
