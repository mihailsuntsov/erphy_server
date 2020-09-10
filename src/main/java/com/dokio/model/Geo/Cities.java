package com.dokio.model.Geo;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="sprav_sys_cities")
public class Cities {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_cities_id_seq", sequenceName="sprav_sys_cities_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_cities_id_seq")
    private Integer id;

    @Size(max = 128)
    @Column(name = "name_ru", nullable = false)
    private String name_ru;

    @Size(max = 128)
    @Column(name = "name_en", nullable = false)
    private String name_en;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Countries country;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Regions region;

    @Size(max = 128)
    @Column(name = "area_ru")
    private String area_ru;

    @Size(max = 128)
    @Column(name = "area_en")
    private String area_en;

    @Column(name = "big")
    private Boolean big;

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

    public Countries getCountry() {
        return country;
    }

    public void setCountry(Countries country) {
        this.country = country;
    }

    public Regions getRegion() {
        return region;
    }

    public void setRegion(Regions region) {
        this.region = region;
    }

    public String getArea_ru() {
        return area_ru;
    }

    public void setArea_ru(String area_ru) {
        this.area_ru = area_ru;
    }

    public String getArea_en() {
        return area_en;
    }

    public void setArea_en(String area_en) {
        this.area_en = area_en;
    }

    public Boolean getBig() {
        return big;
    }

    public void setBig(Boolean big) {
        this.big = big;
    }
}
