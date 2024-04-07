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
    private Integer time_zone_id;

    private int     booking_doc_name_variation_id;        // variation's id of name of booking document: 1-appointment, 2-reservation
    private String     booking_doc_name_variation;        // variation of name of booking document: appointment, reservation
//    private Long    store_orders_department_id;         // department for creation Customer order from store
//    private String  store_if_customer_not_found;        // "create_new" or "use_default" customer (counterparty)
//    private Long    store_default_customer_id;          // counterparty id if store_if_customer_not_found == use_default
//    private Long    store_default_creator_id;           // ID of default user, that will be marked as a creator of store order. Default is master user
//    private Integer store_days_for_esd;                 // number of days for ESD of created store order. Default is 0
    private boolean vat;
    private boolean vat_included;
//    private boolean store_auto_reserve;
    private boolean is_store;                             // true if there is at least 1 non-deleted online stores

    public boolean isIs_store() {
        return is_store;
    }

    public int getBooking_doc_name_variation_id() {
        return booking_doc_name_variation_id;
    }

    public void setBooking_doc_name_variation_id(int booking_doc_name_variation_id) {
        this.booking_doc_name_variation_id = booking_doc_name_variation_id;
    }

    public Integer getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(Integer time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public String getBooking_doc_name_variation() {
        return booking_doc_name_variation;
    }

    public void setBooking_doc_name_variation(String booking_doc_name_variation) {
        this.booking_doc_name_variation = booking_doc_name_variation;
    }

    public void setIs_store(boolean is_store) {
        this.is_store = is_store;
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
}
