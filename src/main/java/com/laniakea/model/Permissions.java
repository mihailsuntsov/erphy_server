package com.laniakea.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "permissions")
public class Permissions {
    @Id
    @SequenceGenerator(name = "permissions_id_seq", sequenceName = "permissions_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "permissions_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @Size(max = 1024)
    @Column(name = "description")
    private String description;

    @Size(max = 255)
    @Column(name = "document_name")
    private String document_name;

    @ManyToOne
    @NotBlank
    @JsonBackReference//antagonist - @JsonManagedReference in Documents
    @JoinColumn(name = "document_id", nullable = false)
    private Documents document;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocument_name() {
        return document_name;
    }

    public void setDocument_name(String document_name) {
        this.document_name = document_name;
    }

    public Documents getDocument() {
        return document;
    }

    public void setDocument(Documents document) {
        this.document = document;
    }
}



