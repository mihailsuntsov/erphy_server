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
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="file_categories")
public class FileCategories {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="file_categories_id_seq", sequenceName="file_categories_id_seq", allocationSize=1)
    @GeneratedValue(generator="file_categories_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @OrderBy("output_order")
    @OneToMany
    @JsonManagedReference //antagonist -
    @JoinColumn(name = "parent_id")
    @OnDelete(action= OnDeleteAction.CASCADE)
    private List<FileCategories> children = new LinkedList<FileCategories>();

    @JsonBackReference//antagonist -
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_id",insertable=false,updatable=false)
    private FileCategories parent;

    @JsonIgnore
    @ManyToMany(mappedBy = "fileCategories")
    private Set<Files> files;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "owner_id")
    private User owner_user;

    private String owner;

    private Long ownerId;

    @PostLoad
    public void postLoad() {
        if(owner_user != null) {
            this.owner = owner_user.getName();
            this.ownerId = owner_user.getId();
        } else {
            this.owner = null;
            this.ownerId = null;
        }
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

    public List<FileCategories> getChildren() {
        return children;
    }

    public void setChildren(List<FileCategories> children) {
        this.children = children;
    }

    public FileCategories getParent() {
        return parent;
    }

    public void setParent(FileCategories parent) {
        this.parent = parent;
    }

    public Set<Files> getFiles() {
        return files;
    }

    public void setFiles(Set<Files> files) {
        this.files = files;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public User getOwner_user() {
        return owner_user;
    }

    public void setOwner_user(User owner_user) {
        this.owner_user = owner_user;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
