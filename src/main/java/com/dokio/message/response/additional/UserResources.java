package com.dokio.message.response.additional;


// Class return information about user resources (consumed or limits)
public class UserResources {

    private Long companies;
    private Long departments;
    private Long users;
    private Long products;
    private Long counterparties;
    private int  megabytes;

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
}
