package com.dokio.message.response;

import com.dokio.message.response.additional.IdAndNameJSON;
import com.dokio.message.response.additional.UserProductDeppartsJSON;

import java.util.List;

public class UserJSON_ {
        private Long   id;
        private String name;
        private String fio_family;
        private String fio_name;
        private String fio_otchestvo;
        private String username;
        private String email;
        private String company;
        private Long   company_id;
        private List<String> userDepartmentsNames;
        private List<Integer> userDepartmentsId;
        private List<Integer> userGroupsId;
        private String master;
        private Long   master_id;
        private String creator;
        private Long   creator_id;
        private String changer;
        private Long   changer_id;
        private String sex;
        private Integer status_account;
        private String date_birthday;
        private String additional;
        private String date_time_created;
        private String date_time_changed;
        private Long   time_zone_id;
        private String vatin;
        private boolean is_employee;
        private boolean is_currently_employed;
        private Long job_title_id;
        private Long counterparty_id;
        private Long incoming_service_id;
        private String job_title_name;
        private String counterparty_name;
        private String incoming_service_name;

//        private List<UserProductDeppartsJSON> userProductsDepparts; // list of services that employee (this user) can provide, and where (parts of departments) he can provide these services
        private List<IdAndNameJSON> userProductsDepparts; // list of services that employee (this user) can provide

    public boolean isIs_employee() {
        return is_employee;
    }

    public void setIs_employee(boolean is_employee) {
        this.is_employee = is_employee;
    }

    public Integer getStatus_account() {
        return status_account;
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

    public String getJob_title_name() {
        return job_title_name;
    }

    public void setJob_title_name(String job_title_name) {
        this.job_title_name = job_title_name;
    }

    public String getCounterparty_name() {
        return counterparty_name;
    }

    public void setCounterparty_name(String counterparty_name) {
        this.counterparty_name = counterparty_name;
    }

    public String getIncoming_service_name() {
        return incoming_service_name;
    }

    public void setIncoming_service_name(String incoming_service_name) {
        this.incoming_service_name = incoming_service_name;
    }

    public void setStatus_account(Integer status_account) {
        this.status_account = status_account;
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

    public void setName(String name) {
        this.name = name;
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

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
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

    public List<Integer> getUserGroupsId() {
        return userGroupsId;
    }

    public void setUserGroupsId(List<Integer> userGroupsId) {
        this.userGroupsId = userGroupsId;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Long changer_id) {
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

    public Long getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(Long time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public String getVatin() {
        return vatin;
    }

    public void setVatin(String vatin) {
        this.vatin = vatin;
    }

    public List<IdAndNameJSON> getUserProductsDepparts() {
        return userProductsDepparts;
    }

    public void setUserProductsDepparts(List<IdAndNameJSON> userProductsDepparts) {
        this.userProductsDepparts = userProductsDepparts;
    }
//    public List<UserProductDeppartsJSON> getUserProductsDepparts() {
//        return userProductsDepparts;
//    }
//
//    public void setUserProductsDepparts(List<UserProductDeppartsJSON> userProductsDepparts) {
//        this.userProductsDepparts = userProductsDepparts;
//    }
}
