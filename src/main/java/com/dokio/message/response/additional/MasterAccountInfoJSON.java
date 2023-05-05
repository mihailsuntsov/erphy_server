/*
        DokioCRM - server part. Sales, finance and warehouse management system
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

public class MasterAccountInfoJSON {

    private BigDecimal money;               // how much money on main account
    private String     plan_name;           // the name of tariff plan
    private int        plan_version;        // the version of tariff plan
    private BigDecimal plan_price;          // how much writeoff per day for tariff plan
    private boolean    plan_no_limits;      // tariff plan has no limits
    private BigDecimal companies_ppu;       // writeoff per day for 1 additional company
    private BigDecimal departments_ppu;     // writeoff per day for 1 additional department
    private BigDecimal users_ppu;           // writeoff per day for 1 additional user
    private BigDecimal products_ppu;        // writeoff per day for 1 additional product or service
    private BigDecimal counterparties_ppu;  // writeoff per day for 1 additional counterparty
    private BigDecimal megabytes_ppu;       // writeoff per day for 1 additional Mb
    private BigDecimal stores_ppu;          // writeoff per day for 1 additional WooCommerce store connection (document "Store")
    private BigDecimal stores_woo_ppu;      // writeoff per day for 1 additional WooCommerce hosting

    // plan
    private Long n_companies;
    private Long n_departments;
    private Long n_users;
    private Long n_products;
    private Long n_counterparties;
    private int n_megabytes;
    private Long n_stores;
    private Long n_stores_woo;

    // additional options
    private Long n_companies_add;
    private Long n_departments_add;
    private Long n_users_add;
    private Long n_products_add;
    private Long n_counterparties_add;
    private int n_megabytes_add;
    private Long n_stores_add;
    private Long n_stores_woo_add;
    private Long free_trial_days;

    // using in fact
    private Long n_companies_fact;
    private Long n_departments_fact;
    private Long n_users_fact;
    private Long n_products_fact;
    private Long n_counterparties_fact;
    private int n_megabytes_fact;
    private Long n_stores_fact;
    private Long n_stores_woo_fact;

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getPlan_name() {
        return plan_name;
    }

    public void setPlan_name(String plan_name) {
        this.plan_name = plan_name;
    }

    public int getPlan_version() {
        return plan_version;
    }

    public void setPlan_version(int plan_version) {
        this.plan_version = plan_version;
    }

    public BigDecimal getPlan_price() {
        return plan_price;
    }

    public void setPlan_price(BigDecimal plan_price) {
        this.plan_price = plan_price;
    }

    public boolean isPlan_no_limits() {
        return plan_no_limits;
    }

    public void setPlan_no_limits(boolean plan_no_limits) {
        this.plan_no_limits = plan_no_limits;
    }

    public BigDecimal getCompanies_ppu() {
        return companies_ppu;
    }

    public void setCompanies_ppu(BigDecimal companies_ppu) {
        this.companies_ppu = companies_ppu;
    }

    public BigDecimal getDepartments_ppu() {
        return departments_ppu;
    }

    public void setDepartments_ppu(BigDecimal departments_ppu) {
        this.departments_ppu = departments_ppu;
    }

    public BigDecimal getUsers_ppu() {
        return users_ppu;
    }

    public void setUsers_ppu(BigDecimal users_ppu) {
        this.users_ppu = users_ppu;
    }

    public BigDecimal getProducts_ppu() {
        return products_ppu;
    }

    public void setProducts_ppu(BigDecimal products_ppu) {
        this.products_ppu = products_ppu;
    }

    public BigDecimal getCounterparties_ppu() {
        return counterparties_ppu;
    }

    public void setCounterparties_ppu(BigDecimal counterparties_ppu) {
        this.counterparties_ppu = counterparties_ppu;
    }

    public BigDecimal getMegabytes_ppu() {
        return megabytes_ppu;
    }

    public void setMegabytes_ppu(BigDecimal megabytes_ppu) {
        this.megabytes_ppu = megabytes_ppu;
    }

    public BigDecimal getStores_ppu() {
        return stores_ppu;
    }

    public void setStores_ppu(BigDecimal stores_ppu) {
        this.stores_ppu = stores_ppu;
    }

    public BigDecimal getStores_woo_ppu() {
        return stores_woo_ppu;
    }

    public void setStores_woo_ppu(BigDecimal stores_woo_ppu) {
        this.stores_woo_ppu = stores_woo_ppu;
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

    public Long getN_products() {
        return n_products;
    }

    public void setN_products(Long n_products) {
        this.n_products = n_products;
    }

    public Long getN_counterparties() {
        return n_counterparties;
    }

    public void setN_counterparties(Long n_counterparties) {
        this.n_counterparties = n_counterparties;
    }

    public int getN_megabytes() {
        return n_megabytes;
    }

    public void setN_megabytes(int n_megabytes) {
        this.n_megabytes = n_megabytes;
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

    public Long getN_companies_add() {
        return n_companies_add;
    }

    public void setN_companies_add(Long n_companies_add) {
        this.n_companies_add = n_companies_add;
    }

    public Long getN_departments_add() {
        return n_departments_add;
    }

    public void setN_departments_add(Long n_departments_add) {
        this.n_departments_add = n_departments_add;
    }

    public Long getN_users_add() {
        return n_users_add;
    }

    public void setN_users_add(Long n_users_add) {
        this.n_users_add = n_users_add;
    }

    public Long getN_products_add() {
        return n_products_add;
    }

    public void setN_products_add(Long n_products_add) {
        this.n_products_add = n_products_add;
    }

    public Long getN_counterparties_add() {
        return n_counterparties_add;
    }

    public void setN_counterparties_add(Long n_counterparties_add) {
        this.n_counterparties_add = n_counterparties_add;
    }

    public int getN_megabytes_add() {
        return n_megabytes_add;
    }

    public void setN_megabytes_add(int n_megabytes_add) {
        this.n_megabytes_add = n_megabytes_add;
    }

    public Long getN_stores_add() {
        return n_stores_add;
    }

    public void setN_stores_add(Long n_stores_add) {
        this.n_stores_add = n_stores_add;
    }

    public Long getN_stores_woo_add() {
        return n_stores_woo_add;
    }

    public void setN_stores_woo_add(Long n_stores_woo_add) {
        this.n_stores_woo_add = n_stores_woo_add;
    }

    public Long getFree_trial_days() {
        return free_trial_days;
    }

    public void setFree_trial_days(Long free_trial_days) {
        this.free_trial_days = free_trial_days;
    }

    public Long getN_companies_fact() {
        return n_companies_fact;
    }

    public void setN_companies_fact(Long n_companies_fact) {
        this.n_companies_fact = n_companies_fact;
    }

    public Long getN_departments_fact() {
        return n_departments_fact;
    }

    public void setN_departments_fact(Long n_departments_fact) {
        this.n_departments_fact = n_departments_fact;
    }

    public Long getN_users_fact() {
        return n_users_fact;
    }

    public void setN_users_fact(Long n_users_fact) {
        this.n_users_fact = n_users_fact;
    }

    public Long getN_products_fact() {
        return n_products_fact;
    }

    public void setN_products_fact(Long n_products_fact) {
        this.n_products_fact = n_products_fact;
    }

    public Long getN_counterparties_fact() {
        return n_counterparties_fact;
    }

    public void setN_counterparties_fact(Long n_counterparties_fact) {
        this.n_counterparties_fact = n_counterparties_fact;
    }

    public int getN_megabytes_fact() {
        return n_megabytes_fact;
    }

    public void setN_megabytes_fact(int n_megabytes_fact) {
        this.n_megabytes_fact = n_megabytes_fact;
    }

    public Long getN_stores_fact() {
        return n_stores_fact;
    }

    public void setN_stores_fact(Long n_stores_fact) {
        this.n_stores_fact = n_stores_fact;
    }

    public Long getN_stores_woo_fact() {
        return n_stores_woo_fact;
    }

    public void setN_stores_woo_fact(Long n_stores_woo_fact) {
        this.n_stores_woo_fact = n_stores_woo_fact;
    }
}
