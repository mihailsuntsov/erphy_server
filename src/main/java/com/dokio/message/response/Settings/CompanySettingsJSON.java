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

package com.dokio.message.response.Settings;

public class CompanySettingsJSON {

    private Integer st_prefix_barcode_pieced;
    private Integer st_prefix_barcode_packed;
    private String  netcost_policy;
    private Long    store_orders_department_id;         // department for creation Customer order from store
    private String  store_if_customer_not_found;        // "create_new" or "use_default" customer (counterparty)
    private Long    store_default_customer_id;          // counterparty id if store_if_customer_not_found == use_default
    private Long    store_default_creator_id;           // ID of default user, that will be marked as a creator of store order. Default is master user
    private Integer store_days_for_esd;                 // number of days for ESD of created store order. Default is 0
    private boolean vat;
    private boolean vat_included;
    private boolean store_auto_reserve;
    private boolean is_store;                           // the store of company is switched on

    public boolean isIs_store() {
        return is_store;
    }

    public void setIs_store(boolean is_store) {
        this.is_store = is_store;
    }

    public boolean isStore_auto_reserve() {
        return store_auto_reserve;
    }

    public void setStore_auto_reserve(boolean store_auto_reserve) {
        this.store_auto_reserve = store_auto_reserve;
    }

    public boolean isVat() {
        return vat;
    }

    public void setVat(boolean vat) {
        this.vat = vat;
    }

    public boolean isVat_included() {
        return vat_included;
    }

    public void setVat_included(boolean vat_included) {
        this.vat_included = vat_included;
    }

    public Long getStore_default_creator_id() {
        return store_default_creator_id;
    }

    public void setStore_default_creator_id(Long store_default_creator_id) {
        this.store_default_creator_id = store_default_creator_id;
    }

    public Integer getStore_days_for_esd() {
        return store_days_for_esd;
    }

    public void setStore_days_for_esd(Integer store_days_for_esd) {
        this.store_days_for_esd = store_days_for_esd;
    }

    public Long getStore_orders_department_id() {
        return store_orders_department_id;
    }

    public void setStore_orders_department_id(Long store_orders_department_id) {
        this.store_orders_department_id = store_orders_department_id;
    }

    public String getStore_if_customer_not_found() {
        return store_if_customer_not_found;
    }

    public void setStore_if_customer_not_found(String store_if_customer_not_found) {
        this.store_if_customer_not_found = store_if_customer_not_found;
    }

    public Long getStore_default_customer_id() {
        return store_default_customer_id;
    }

    public void setStore_default_customer_id(Long store_default_customer_id) {
        this.store_default_customer_id = store_default_customer_id;
    }

    public Integer getSt_prefix_barcode_pieced() {
        return st_prefix_barcode_pieced;
    }

    public void setSt_prefix_barcode_pieced(Integer st_prefix_barcode_pieced) {
        this.st_prefix_barcode_pieced = st_prefix_barcode_pieced;
    }

    public Integer getSt_prefix_barcode_packed() {
        return st_prefix_barcode_packed;
    }

    public void setSt_prefix_barcode_packed(Integer st_prefix_barcode_packed) {
        this.st_prefix_barcode_packed = st_prefix_barcode_packed;
    }

    public String getNetcost_policy() {
        return netcost_policy;
    }

    public void setNetcost_policy(String netcost_policy) {
        this.netcost_policy = netcost_policy;
    }
}
