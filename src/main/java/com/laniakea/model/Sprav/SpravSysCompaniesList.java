package com.laniakea.model.Sprav;//Класс для формирования JSON

import javax.persistence.*;

@Entity
@Table(name="companies")
public class SpravSysCompaniesList {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


