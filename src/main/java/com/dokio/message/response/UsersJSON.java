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

package com.dokio.message.response;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.List;

@Entity
public class UsersJSON {
    @Id
    private Long id;
    private String name;
    private String fio_family;
    private String fio_name;
    private String fio_otchestvo;
    private String username;
    private String email;
    private String company;
    private String company_id;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> userDepartmentsNames;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> userDepartmentsId;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> userGroupsId;
    private String master;
    private String master_id;
    private String creator;
    private String creator_id;
    private String changer;
    private String changer_id;
    private String sex;
    private String status_account;
    private String date_birthday;
    private String additional;
    private String date_time_created;
    private String date_time_changed;
    private Long time_zone_id;
    private String vatin;

    public String getVatin() {
        return vatin;
    }

    public void setVatin(String vatin) {
        this.vatin = vatin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Long getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(Long time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus_account() {
        return status_account;
    }

    public List<Integer> getUserGroupsId() {
        return userGroupsId;
    }

    public void setUserGroupsId(List<Integer> userGroupsId) {
        this.userGroupsId = userGroupsId;
    }

    public void setStatus_account(String status_account) {
        this.status_account = status_account;
    }

    public String getFio_family() {
        return fio_family;
    }

    public void setFio_family(String fio_family) {
        this.fio_family = fio_family;
    }

    public String getFio_name() {
        return fio_name;
    }

    public void setFio_name(String fio_name) {
        this.fio_name = fio_name;
    }

    public String getFio_otchestvo() {
        return fio_otchestvo;
    }

    public void setFio_otchestvo(String fio_otchestvo) {
        this.fio_otchestvo = fio_otchestvo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public List<String> getUserDepartmentsNames() {
        return userDepartmentsNames;
    }

    public void setUserDepartmentsNames(List<String> userDepartmentsNames) {
        this.userDepartmentsNames = userDepartmentsNames;
    }

    public List<Integer> getUserDepartmentsId() {
        return userDepartmentsId;
    }

    public void setUserDepartmentsId(List<Integer> userDepartmentsId) {
        this.userDepartmentsId = userDepartmentsId;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getMaster_id() {
        return master_id;
    }

    public void setMaster_id(String master_id) {
        this.master_id = master_id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public String getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(String changer_id) {
        this.changer_id = changer_id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDate_birthday() {
        return date_birthday;
    }

    public void setDate_birthday(String date_birthday) {
        this.date_birthday = date_birthday;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }
}
