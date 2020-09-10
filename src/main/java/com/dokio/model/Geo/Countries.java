package com.dokio.model.Geo;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="sprav_sys_countries")
public class Countries {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_countries_id_seq", sequenceName="sprav_sys_countries_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_countries_id_seq")
    private Integer id;

    @Size(max = 128)
    @Column(name = "name_ru", nullable = false)
    private String name_ru;

    @Size(max = 128)
    @Column(name = "name_en", nullable = false)
    private String name_en;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName_ru() {
        return name_ru;
    }

    public void setName_ru(String name_ru) {
        this.name_ru = name_ru;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }
}
