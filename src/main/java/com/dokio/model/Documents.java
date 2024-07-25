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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="documents")
public class Documents {

  @Id
  @Column(name="id")
  @SequenceGenerator(name="documents_id_seq", sequenceName="documents_id_seq", allocationSize=1)
  @GeneratedValue(generator="documents_id_seq")
  private Long id;

  @Column(name = "name")
  private String name;
//
//  @Column(name = "page_name")
//  private String pageName;
//
//  @Column(name = "icon_style")
//  private String iconStyle;

  @Column(name = "show")
  private Integer show;

//  @JsonIgnore
//  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
//  private Set<DocumentsMenu> documentsMenu = new HashSet<DocumentsMenu>();

  @OrderBy("output_order ASC")
  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JsonManagedReference //antagonist - @JsonBackReference in Permissions
  private Set<Permissions> permissions = new HashSet<Permissions>();


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

//  public Set<DocumentsMenu> getDocumentsMenu() {
//    return documentsMenu;
//  }
//
  public void setId(Long id) {
    this.id = id;
  }

  public Set<Permissions> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<Permissions> permissions) {
    this.permissions = permissions;
  }

//  public void setDocumentsMenu(Set<DocumentsMenu> documentsMenu) {
//    this.documentsMenu = documentsMenu;
//  }
}
