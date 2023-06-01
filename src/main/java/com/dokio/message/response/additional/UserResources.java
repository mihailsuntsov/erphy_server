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


// Class return information about user resources (consumed or limits)
public class UserResources {

    private Long companies;
    private Long departments;
    private Long users;
    private Long products;
    private Long counterparties;
    private Long stores;    // store connections
    private Long stores_woo;// server hosted sites
    private int  megabytes;

    public Long getStores_woo() {
        return stores_woo;
    }

    public void setStores_woo(Long stores_woo) {
        this.stores_woo = stores_woo;
    }

    public Long getStores() {
        return stores;
    }

    public void setStores(Long stores) {
        this.stores = stores;
    }

    public Long getCompanies() {
        return companies;
    }

    public void setCompanies(Long companies) {
        this.companies = companies;
    }

    public Long getDepartments() {
        return departments;
    }

    public void setDepartments(Long departments) {
        this.departments = departments;
    }

    public Long getUsers() {
        return users;
    }

    public void setUsers(Long users) {
        this.users = users;
    }

    public Long getProducts() {
        return products;
    }

    public void setProducts(Long products) {
        this.products = products;
    }

    public Long getCounterparties() {
        return counterparties;
    }

    public void setCounterparties(Long counterparties) {
        this.counterparties = counterparties;
    }

    public int getMegabytes() {
        return megabytes;
    }

    public void setMegabytes(int megabytes) {
        this.megabytes = megabytes;
    }

    @Override
    public String toString() {
        return "UserResources{" +
                "companies=" + companies +
                ", departments=" + departments +
                ", users=" + users +
                ", products=" + products +
                ", counterparties=" + counterparties +
                ", stores=" + stores +
                ", stores_woo=" + stores_woo +
                ", megabytes=" + megabytes +
                '}';
    }
}
