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

package com.dokio.message.request;

import com.dokio.message.request.additional.UserProductDeppartsForm;
import com.dokio.message.response.additional.IdAndNameJSON;

import java.util.List;
import java.util.Set;

public class SignUpForm {
    private int id;
    private String name;
    private String username;
    private String email;
    private Set<String> role;
    private Set<Long> selectedUserDepartments;
    private Set<Long> userGroupList;
    private String master_id;
    private String fio_family;
    private String fio_name;
    private String fio_otchestvo;
    private String sex;
    private String status_account;
    private String date_birthday;
    private String additional;
    private String company_id;
    private String checked;
    private String password;
    private Integer timeZoneId;
    private String vatin;
    private Integer localeId;
    private Integer languageId;
    private String language;
    private List<IdAndNameJSON> userProducts;
    private List<IdAndNameJSON> deppartProducts;
    private boolean is_employee;
    private boolean is_currently_employed;
    private Long job_title_id;
    private Long counterparty_id;
    private Long incoming_service_id;

    @Override
    public String toString() {
        return "SignUpForm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", selectedUserDepartments=" + selectedUserDepartments +
                ", userGroupList=" + userGroupList +
                ", master_id='" + master_id + '\'' +
                ", fio_family='" + fio_family + '\'' +
                ", fio_name='" + fio_name + '\'' +
                ", fio_otchestvo='" + fio_otchestvo + '\'' +
                ", sex='" + sex + '\'' +
                ", status_account='" + status_account + '\'' +
                ", date_birthday='" + date_birthday + '\'' +
                ", additional='" + additional + '\'' +
                ", company_id='" + company_id + '\'' +
                ", checked='" + checked + '\'' +
                ", password='" + password + '\'' +
                ", timeZoneId=" + timeZoneId +
                ", vatin='" + vatin + '\'' +
                ", localeId=" + localeId +
                ", languageId=" + languageId +
                ", language='" + language + '\'' +
                ", userProducts=" + userProducts +
                ", deppartProducts=" + deppartProducts +
                ", is_employee=" + is_employee +
                ", is_currently_employed=" + is_currently_employed +
                ", job_title_id=" + job_title_id +
                ", counterparty_id=" + counterparty_id +
                ", incoming_service_id=" + incoming_service_id +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<String> getRole() {
        return role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }

    public Set<Long> getSelectedUserDepartments() {
        return selectedUserDepartments;
    }

    public void setSelectedUserDepartments(Set<Long> selectedUserDepartments) {
        this.selectedUserDepartments = selectedUserDepartments;
    }

    public Set<Long> getUserGroupList() {
        return userGroupList;
    }

    public void setUserGroupList(Set<Long> userGroupList) {
        this.userGroupList = userGroupList;
    }

    public String getMaster_id() {
        return master_id;
    }

    public void setMaster_id(String master_id) {
        this.master_id = master_id;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getStatus_account() {
        return status_account;
    }

    public void setStatus_account(String status_account) {
        this.status_account = status_account;
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

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(Integer timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getVatin() {
        return vatin;
    }

    public void setVatin(String vatin) {
        this.vatin = vatin;
    }

    public Integer getLocaleId() {
        return localeId;
    }

    public void setLocaleId(Integer localeId) {
        this.localeId = localeId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<IdAndNameJSON> getUserProducts() {
        return userProducts;
    }

    public void setUserProducts(List<IdAndNameJSON> userProducts) {
        this.userProducts = userProducts;
    }

    public List<IdAndNameJSON> getDeppartProducts() {
        return deppartProducts;
    }

    public void setDeppartProducts(List<IdAndNameJSON> deppartProducts) {
        this.deppartProducts = deppartProducts;
    }

    public boolean isIs_employee() {
        return is_employee;
    }

    public void setIs_employee(boolean is_employee) {
        this.is_employee = is_employee;
    }

    public boolean isIs_currently_employed() {
        return is_currently_employed;
    }

    public void setIs_currently_employed(boolean is_currently_employed) {
        this.is_currently_employed = is_currently_employed;
    }

    public Long getJob_title_id() {
        return job_title_id;
    }

    public void setJob_title_id(Long job_title_id) {
        this.job_title_id = job_title_id;
    }

    public Long getCounterparty_id() {
        return counterparty_id;
    }

    public void setCounterparty_id(Long counterparty_id) {
        this.counterparty_id = counterparty_id;
    }

    public Long getIncoming_service_id() {
        return incoming_service_id;
    }

    public void setIncoming_service_id(Long incoming_service_id) {
        this.incoming_service_id = incoming_service_id;
    }
}