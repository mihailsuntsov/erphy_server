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

    @ManyToMany(mappedBy = "usergroup")
    private Set<User> users;

    @ManyToOne
    @JoinColumn(name = "creator_id",nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "master_id",nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "changer_id")
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

    @Column(name = "is_deleted")
    private Boolean is_deleted;

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

}
