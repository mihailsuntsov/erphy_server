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

package com.dokio.message.response.additional;

import java.math.BigDecimal;

public class ProductPricesJSON {

    private Long price_type_id;
    private String price_name;
    private String price_description;
    private BigDecimal price_value;
    private int row_id;
    private Boolean is_store_price_type_regular;
    private Boolean is_store_price_type_sale;

    public int getRow_id() {
        return row_id;
    }

    public void setRow_id(int row_id) {
        this.row_id = row_id;
    }

    public Long getPrice_type_id() {
        return price_type_id;
    }

    public void setPrice_type_id(Long price_type_id) {
        this.price_type_id = price_type_id;
    }

    public String getPrice_name() {
        return price_name;
    }

    public void setPrice_name(String price_name) {
        this.price_name = price_name;
    }

    public BigDecimal getPrice_value() {
        return price_value;
    }

    public void setPrice_value(BigDecimal price_value) {
        this.price_value = price_value;
    }

    public String getPrice_description() {
        return price_description;
    }

    public void setPrice_description(String price_description) {
        this.price_description = price_description;
    }

    public Boolean getIs_store_price_type_regular() {
        return is_store_price_type_regular;
    }

    public void setIs_store_price_type_regular(Boolean is_store_price_type_regular) {
        this.is_store_price_type_regular = is_store_price_type_regular;
    }

    public Boolean getIs_store_price_type_sale() {
        return is_store_price_type_sale;
    }

    public void setIs_store_price_type_sale(Boolean is_store_price_type_sale) {
        this.is_store_price_type_sale = is_store_price_type_sale;
    }
}
