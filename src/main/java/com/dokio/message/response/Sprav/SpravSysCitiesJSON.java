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
