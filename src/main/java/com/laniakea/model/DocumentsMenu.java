package com.laniakea.model;

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
