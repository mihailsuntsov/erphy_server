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

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="documents_directories")
public class DocumentsDirectories {

  @Id
  @Column(name="id")
  @SequenceGenerator(name="documents_directories_id_seq", sequenceName="documents_directories_id_seq", allocationSize=1)
  @GeneratedValue(generator="documents_directories_id_seq")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "icon_style")
  private String icon_style;

  @Column(name = "group_")
  private Long group_;

  @Column(name = "ordering")
  private Long ordering;

  @OneToMany(mappedBy = "directoryId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<DocumentsMenu> documentsMenu = new HashSet<DocumentsMenu>();

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User userId;

  @Column(name = "parent_id")
  private Long parentId;

  /*@OneToMany(mappedBy = "parentId")
  private Set<DocumentsDirectories> tenants;

  @ManyToOne (optional = false)
  @JoinColumn(name = "parent_id", nullable = false)
  private DocumentsDirectories parentId;*/



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

  public String getIcon_style() {
    return icon_style;
  }

  public void setIcon_style(String icon_style) {
    this.icon_style = icon_style;
  }

  public Long getGroup_() {
    return group_;
  }

  public void setGroup_(Long group_) {
    this.group_ = group_;
  }

  public Long getOrdering() {
    return ordering;
  }

  public void setOrdering(Long ordering) {
    this.ordering = ordering;
  }

  public Set<DocumentsMenu> getDocumentsMenu() {
    return documentsMenu;
  }

  public void setDocumentsMenu(Set<DocumentsMenu> documentsMenu) {
    this.documentsMenu = documentsMenu;
  }

  public User getUserId() {
    return userId;
  }

  public void setUserId(User userId) {
    this.userId = userId;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }


}
