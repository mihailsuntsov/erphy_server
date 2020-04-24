package com.laniakea.model;

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

  @Column(name = "page_name")
  private String pageName;

  @Column(name = "icon_style")
  private String iconStyle;

  @Column(name = "show")
  private Integer show;

  @JsonIgnore
  @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<DocumentsMenu> documentsMenu = new HashSet<DocumentsMenu>();

  @OrderBy("name ASC")
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


  public String getPageName() {
    return pageName;
  }

  public void setPageName(String pageName) {
    this.pageName = pageName;
  }


  public String getIconStyle() {
    return iconStyle;
  }

  public void setIconStyle(String iconStyle) {
    this.iconStyle = iconStyle;
  }

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
