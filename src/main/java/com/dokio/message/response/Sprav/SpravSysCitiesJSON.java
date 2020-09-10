package com.dokio.message.response.Sprav;

public class SpravSysCitiesJSON {
    private Integer id;
    private Integer country_id;
    private Integer region_id;
    private String  name_ru;
    private String  area_ru;
    private String  region_name_ru;
    private String  country_name_ru;
    private Boolean big;

    public Integer getId() {
        return id;
    }

    public String getCountry_name_ru() {
        return country_name_ru;
    }

    public String getRegion_name_ru() {
        return region_name_ru;
    }

    public void setRegion_name_ru(String region_name_ru) {
        this.region_name_ru = region_name_ru;
    }

    public void setCountry_name_ru(String country_name_ru) {
        this.country_name_ru = country_name_ru;
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

    public Integer getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Integer region_id) {
        this.region_id = region_id;
    }

    public String getName_ru() {
        return name_ru;
    }

    public void setName_ru(String name_ru) {
        this.name_ru = name_ru;
    }

    public String getArea_ru() {
        return area_ru;
    }

    public void setArea_ru(String area_ru) {
        this.area_ru = area_ru;
    }

    public Boolean getBig() {
        return big;
    }

    public void setBig(Boolean big) {
        this.big = big;
    }
}
