package com.dokio.message.response.Sprav;

public class SpravSysRegionsJSON {
    private Integer id;
    private Integer country_id;
    private String  name_ru;
    private String  country_name_ru;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCountry_id() {
        return country_id;
    }

    public void setCountry_id(Integer country_id) {
        this.country_id = country_id;
    }

    public String getName_ru() {
        return name_ru;
    }

    public void setName_ru(String name_ru) {
        this.name_ru = name_ru;
    }

    public String getCountry_name_ru() {
        return country_name_ru;
    }

    public void setCountry_name_ru(String country_name_ru) {
        this.country_name_ru = country_name_ru;
    }
}
