package com.laniakea.message.response;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProductFieldValuesListJSON {
    @Id
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
