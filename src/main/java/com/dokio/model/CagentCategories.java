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
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="cagent_categories")
public class CagentCategories {
    @Id
    @Column(name="id")
    @SequenceGenerator(name="cagent_categories_id_seq", sequenceName="cagent_categories_id_seq", allocationSize=1)
    @GeneratedValue(generator="cagent_categories_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @OrderBy("output_order")
    @OneToMany
    @JsonManagedReference //antagonist -
    @JoinColumn(name = "parent_id")
    private List<CagentCategories> children = new LinkedList<CagentCategories>();

    @JsonBackReference//antagonist -
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_id",insertable=false,updatable=false)
    private CagentCategories parent;

    @JsonIgnore
    @ManyToMany(mappedBy = "cagentCategories")
    private Set<Cagents> cagents;

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

    public List<CagentCategories> getChildren() {
        return children;
    }

    public void setChildren(List<CagentCategories> children) {
        this.children = children;
    }

    public CagentCategories getParent() {
        return parent;
    }

    public void setParent(CagentCategories parent) {
        this.parent = parent;
    }

    public Set<Cagents> getCagents() {
        return cagents;
    }

    public void setCagents(Set<Cagents> cagents) {
        this.cagents = cagents;
    }
}
