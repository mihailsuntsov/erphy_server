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
package com.dokio.message.request.store.woo.v3;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TaxForm {
    private int id; // Unique identifier for the resource in WooCommerce
    private String country; //Country ISO 3166 code. See ISO 3166 Codes (Countries) for more details
    private String state; //State code.
    private String postcode; //Postcode/ZIP, it doesn't support multiple values. Deprecated as of WooCommerce 5.3, postcodes should be used instead.
    private String city; //City name, it doesn't support multiple values. Deprecated as of WooCommerce 5.3, postcodes should be used instead.
    private String[] postcodes; //Postcodes/ZIPs. Introduced in WooCommerce 5.3.
    private String[] cities; //City names. Introduced in WooCommerce 5.3.
    private String rate; //Tax rate.
    private String name; //Tax rate name.
    private int priority; //Tax priority. Only 1 matching rate per priority will be used. To define multiple tax rates for a single area you need to specify a different priority per rate. Default is 1.
    private Boolean compound; //Whether or not this is a compound rate. Compound tax rates are applied on top of other tax rates. Default is false.
    private Boolean shipping; //Whether or not this tax rate also gets applied to shipping. Default is true.
    private int order; //Indicates the order that will appear in queries.
    @JsonProperty("class") // class is a reserved keyword, and can't be used as a variable name
    private String class_; //Tax class. Default is standard.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String[] getPostcodes() {
        return postcodes;
    }

    public void setPostcodes(String[] postcodes) {
        this.postcodes = postcodes;
    }

    public String[] getCities() {
        return cities;
    }

    public void setCities(String[] cities) {
        this.cities = cities;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Boolean getCompound() {
        return compound;
    }

    public void setCompound(Boolean compound) {
        this.compound = compound;
    }

    public Boolean getShipping() {
        return shipping;
    }

    public void setShipping(Boolean shipping) {
        this.shipping = shipping;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getClass_() {
        return class_;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }
}
