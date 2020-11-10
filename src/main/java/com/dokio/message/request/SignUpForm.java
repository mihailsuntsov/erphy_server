/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.message.request;

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
    private Long time_zone_id;

    public String getName() {
        return name;
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

    public Long getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(Long time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public void setFio_family(String fio_family) {
        this.fio_family = fio_family;
    }

    public String getCompany_id() {
        return company_id;
    }

    public Set<Long> getUserGroupList() {
        return userGroupList;
    }

    public void setUserGroupList(Set<Long> userGroupList) {
        this.userGroupList = userGroupList;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus_account() {
        return status_account;
    }

    public void setStatus_account(String status_account) {
        this.status_account = status_account;
    }

    public String getChecked() {
        return checked;
    }

    public void setChecked(String checked) {
        this.checked = checked;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public Set<String> getRole() {
    	return this.role;
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

    @Override
    public String toString() {
        return "SignUpForm: id=" + this.id + ", name" + this.name + ", userName" + this.username;
    }
}