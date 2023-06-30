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

package com.dokio.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dokio.util.JSONDeserialize;
import com.dokio.util.JSONSerializer;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="usergroup")
public class UserGroup {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Companies company;

    @ManyToMany(mappedBy = "usergroup")
    private Set<User> users;

    @ManyToOne
    @JoinColumn(name = "creator_id",nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "master_id",nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "changer_id", nullable = true)
    private User changer;

    @Column(name = "date_time_created")
    @JsonSerialize(using = JSONSerializer.class)
    @JsonDeserialize(using = JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name = "date_time_changed")
    @JsonSerialize(using = JSONSerializer.class)
    @JsonDeserialize(using = JSONDeserialize.class)
    private Timestamp date_time_changed;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usergroup_permissions",
            joinColumns = {@JoinColumn(name = "usergroup_id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id")})
    private Set<Permissions> permissions = new HashSet<Permissions>();

    @Size(max = 1024)
    @Column(name = "description")
    private String  description;

    @Column(name = "is_system")
    private Boolean is_system;

    @Column(name = "is_archive")
    private Boolean is_archive;

    @Column(name = "is_deleted")
    private Boolean is_deleted;

    @Column(name = "companyId")
    private Integer companyId;

    public UserGroup() {}

    public UserGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Boolean getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(Boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Companies getCompany() {
        return this.company;
    }

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }
//
    public Set<User> getUsers() {
        return this.users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
//
    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getMaster() {
        return this.master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public User getChanger() {
        return this.changer;
    }

    public void setChanger(User changer) {
        this.changer = changer;
    }

    public Timestamp getDate_time_created() {
        return this.date_time_created;
    }

    public void setDate_time_created(Timestamp date_time_created) {
        this.date_time_created = date_time_created;
    }

    public Timestamp getDate_time_changed() {
        return this.date_time_changed;
    }

    public void setDate_time_changed(Timestamp date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public Set<Permissions> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Set<Permissions> permissions) {
        this.permissions = permissions;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIs_system() {
        return this.is_system;
    }

    public void setIs_system(Boolean is_system) {
        this.is_system = is_system;
    }
}
