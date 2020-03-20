package com.laniakea.model.Sprav;

// признак предмета расчета (товар, услуга, работа ...)

import javax.persistence.*;
@Entity
@Table(name="sprav_sys_ppr")
public class SpravSysPPR {
    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_ppr_id_seq", sequenceName="sprav_sys_ppr_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_ppr_id_seq")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "abbreviation")
    private String abbreviation;
    @Column(name = "description")
    private String description;

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

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


