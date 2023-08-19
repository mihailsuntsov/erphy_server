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

package com.dokio.message.response.Settings;

public class UserSettingsJSON {

    private int     time_zone_id;
    private int     language_id ;
    private int     locale_id;
    private String  time_zone;
    private String  language;
    private String  locale;
    private String  suffix;
    private Integer country_id;             // id of user's company country of jurisdiction
    private String  organization;           // organization of country of jurisdiction(e.g. EU)
    private String  accounting_currency;    // short name of Accounting currency of user's company (e.g. $ or EUR)
    private String  dateFormat;
    private String  timeFormat;
    private String  sidenav;                // "open" or "close"

    public String getSidenav() {
        return sidenav;
    }

    public void setSidenav(String sidenav) {
        this.sidenav = sidenav;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Integer getCountry_id() {
        return country_id;
    }

    public void setCountry_id(Integer country_id) {
        this.country_id = country_id;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAccounting_currency() {
        return accounting_currency;
    }

    public void setAccounting_currency(String accounting_currency) {
        this.accounting_currency = accounting_currency;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(int time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public int getLanguage_id() {
        return language_id;
    }

    public void setLanguage_id(int language_id) {
        this.language_id = language_id;
    }

    public int getLocale_id() {
        return locale_id;
    }

    public void setLocale_id(int locale_id) {
        this.locale_id = locale_id;
    }

    public String getTime_zone() {
        return time_zone;
    }

    public void setTime_zone(String time_zone) {
        this.time_zone = time_zone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
