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

@Entity
@Table(name="documents_menu")
public class DocumentsMenu {

  @Id
  @Column(name="id")
  @SequenceGenerator(name="documents_menu_id_seq", sequenceName="documents_menu_id_seq", allocationSize=1)
  @GeneratedValue(generator="documents_menu_id_seq")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "directory_id", nullable = false)
  private DocumentsDirectories directoryId;

  @Column(name = "icon_style")
  private String icon_style;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User userId;

  @ManyToOne
  @JoinColumn(name = "document_id", nullable = false)
  private Documents document;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public User getUserId() {
    return userId;
  }

  public void setUserId(User userId) {
    this.userId = userId;
  }

  public Documents getDocument() {
    return document;
  }

  public void setDocument(Documents document) {
    this.document = document;
  }

  public DocumentsDirectories getDirectoryId() {
    return directoryId;
  }

  public void setDirectoryId(DocumentsDirectories directoryId) {
    this.directoryId = directoryId;
  }

  public String getIcon_style() {
    return icon_style;
  }

  public void setIcon_style(String icon_style) {
    this.icon_style = icon_style;
  }
}
