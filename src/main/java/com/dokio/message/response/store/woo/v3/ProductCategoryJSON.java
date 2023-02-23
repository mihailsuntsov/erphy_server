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

public class  ProductCategoryJSON {
    private Long            crm_id;
    private Integer         woo_id;
    private Integer         parent_woo_id;      // parent category id in store database
    private String          description;
    private String          display;
    private String          name;
    private String          slug;
    private Long            parent_crm_id;
    private String          img_original_name;
    private String          img_address;
    private String          img_alt;
    private Boolean         img_anonyme_access;
    private Integer         menu_order;

    public Integer getParent_woo_id() {
        return parent_woo_id;
    }

    public void setParent_woo_id(Integer parent_woo_id) {
        this.parent_woo_id = parent_woo_id;
    }

    public String getImg_original_name() {
        return img_original_name;
    }

    public void setImg_original_name(String img_original_name) {
        this.img_original_name = img_original_name;
    }

    public String getImg_address() {
        return img_address;
    }

    public void setImg_address(String img_address) {
        this.img_address = img_address;
    }

    public String getImg_alt() {
        return img_alt;
    }

    public void setImg_alt(String img_alt) {
        this.img_alt = img_alt;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getParent_crm_id() {
        return parent_crm_id;
    }

    public void setParent_crm_id(Long parent_crm_id) {
        this.parent_crm_id = parent_crm_id;
    }

    public Boolean getImg_anonyme_access() {
        return img_anonyme_access;
    }

    public void setImg_anonyme_access(Boolean img_anonyme_access) {
        this.img_anonyme_access = img_anonyme_access;
    }

    public Integer getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(Integer menu_order) {
        this.menu_order = menu_order;
    }
}
