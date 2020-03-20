package com.laniakea.model;
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
}
