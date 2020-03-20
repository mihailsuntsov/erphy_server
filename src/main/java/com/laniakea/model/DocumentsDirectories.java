package com.laniakea.model;

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
