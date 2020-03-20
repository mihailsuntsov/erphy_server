package com.laniakea.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity // создана только для участия в HQL-запросах
@Table(name="user_department")
public class UserDepartment {


    @Id
    @Column(name="user_id", insertable = false, updatable = false)
    private Long user_id;


    @Column(name="department_id", insertable = false, updatable = false)
    private Long department_id;

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }
}
