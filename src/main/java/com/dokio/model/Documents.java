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

  @JsonIgnore
  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<DocumentsMenu> documentsMenu = new HashSet<DocumentsMenu>();

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


//  public String getPageName() {
//    return pageName;
//  }
//
//  public void setPageName(String pageName) {
//    this.pageName = pageName;
//  }

//
//  public String getIconStyle() {
//    return iconStyle;
//  }
//
//  public void setIconStyle(String iconStyle) {
//    this.iconStyle = iconStyle;
//  }

  public Set<DocumentsMenu> getDocumentsMenu() {
    return documentsMenu;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Set<Permissions> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<Permissions> permissions) {
    this.permissions = permissions;
  }

  public void setDocumentsMenu(Set<DocumentsMenu> documentsMenu) {
    this.documentsMenu = documentsMenu;
  }
}
