package com.laniakea.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
            "username"
        }),
        @UniqueConstraint(columnNames = {
            "email"
        })
})
public class User{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min=3, max = 50)
    private String name;

    @NaturalId
    @Size(min=3, max = 50)
    private String username;

    @ManyToOne
    @JoinColumn(name = "master_id")
    private User master;

    @NaturalId
    @Size(max = 50)
    private String email;

    @JsonIgnore
    @Size(min=6, max = 100)
    private String password;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", 
    	joinColumns = @JoinColumn(name = "user_id"), 
    	inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_usergroup",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "usergroup_id"))
    private Set<UserGroup> usergroup = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="user_department",
            joinColumns={@JoinColumn(name="user_id")},
            inverseJoinColumns={@JoinColumn(name="department_id")})
    private Set<Departments> departments = new HashSet<Departments>();

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = true)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = true)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id", nullable = true)
    private User changer;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name = "fio_family")
    private String fio_family;

    @Column(name = "fio_name")
    private String fio_name;

    @Column(name = "additional")
    private String additional;

    @Column(name = "fio_otchestvo")
    private String fio_otchestvo;

    @Column(name = "sex")
    private String sex;

    @Column(name="date_birthday")
    @JsonSerialize(using = com.laniakea.util.JSONSerializer.class)
    @JsonDeserialize(using = com.laniakea.util.JSONDeserialize.class)
    private Date date_birthday;

    @Column(name = "status_account")
//    1 E-mail не верифицирован
//    2 Активный
//    3 Забанен
    private Integer status_account;


    @Column(name = "time_zone_id")
    private Long time_zone_id;


    public User() {}

    public User(String name, String username, String email, String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<UserGroup> getUsergroup() {
        return usergroup;
    }

    public void setUsergroup(Set<UserGroup> usergroup) {
        this.usergroup = usergroup;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public Set<Departments> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Departments> departments) {
        this.departments = departments;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public User getChanger() {
        return changer;
    }

    public void setChanger(User changer) {
        this.changer = changer;
    }

    public Timestamp getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(Timestamp date_time_created) {
        this.date_time_created = date_time_created;
    }

    public Timestamp getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(Timestamp date_time_changed) {
        this.date_time_changed = date_time_changed;
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

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
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

    public Date getDate_birthday() {
        return date_birthday;
    }

    public void setDate_birthday(Date date_birthday) {
        this.date_birthday = date_birthday;
    }

    public Integer getStatus_account() {
        return status_account;
    }

    public void setStatus_account(Integer status_account) {
        this.status_account = status_account;
    }
}