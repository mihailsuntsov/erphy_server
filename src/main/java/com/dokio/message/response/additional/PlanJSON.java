package com.dokio.message.response.additional;

import java.math.BigDecimal;

public class PlanJSON {
    private int id;
    private String name;
    private int version;
    private BigDecimal daily_price;
    private boolean is_nolimits;
    private boolean is_free;
    private boolean is_available_for_user_switching;
    private Long n_companies;
    private Long n_departments;
    private Long n_users;
    private BigDecimal n_products;
    private BigDecimal n_counterparties;
    private BigDecimal  n_megabytes;
    private Long n_stores;
    private Long n_stores_woo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public BigDecimal getDaily_price() {
        return daily_price;
    }

    public void setDaily_price(BigDecimal daily_price) {
        this.daily_price = daily_price;
    }

    public boolean isIs_nolimits() {
        return is_nolimits;
    }

    public void setIs_nolimits(boolean is_nolimits) {
        this.is_nolimits = is_nolimits;
    }

    public boolean isIs_free() {
        return is_free;
    }

    public void setIs_free(boolean is_free) {
        this.is_free = is_free;
    }

    public boolean isIs_available_for_user_switching() {
        return is_available_for_user_switching;
    }

    public void setIs_available_for_user_switching(boolean is_available_for_user_switching) {
        this.is_available_for_user_switching = is_available_for_user_switching;
    }

    public BigDecimal getN_products() {
        return n_products;
    }

    public void setN_products(BigDecimal n_products) {
        this.n_products = n_products;
    }

    public BigDecimal getN_counterparties() {
        return n_counterparties;
    }

    public void setN_counterparties(BigDecimal n_counterparties) {
        this.n_counterparties = n_counterparties;
    }

    public BigDecimal getN_megabytes() {
        return n_megabytes;
    }

    public void setN_megabytes(BigDecimal n_megabytes) {
        this.n_megabytes = n_megabytes;
    }

    public Long getN_companies() {
        return n_companies;
    }

    public void setN_companies(Long n_companies) {
        this.n_companies = n_companies;
    }

    public Long getN_departments() {
        return n_departments;
    }

    public void setN_departments(Long n_departments) {
        this.n_departments = n_departments;
    }

    public Long getN_users() {
        return n_users;
    }

    public void setN_users(Long n_users) {
        this.n_users = n_users;
    }

    public Long getN_stores() {
        return n_stores;
    }

    public void setN_stores(Long n_stores) {
        this.n_stores = n_stores;
    }

    public Long getN_stores_woo() {
        return n_stores_woo;
    }

    public void setN_stores_woo(Long n_stores_woo) {
        this.n_stores_woo = n_stores_woo;
    }
}
