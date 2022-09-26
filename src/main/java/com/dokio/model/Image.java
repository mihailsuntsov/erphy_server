package com.dokio.model;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="files")
public class Image {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "files_id_seq", sequenceName = "files_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "files_id_seq")
    private Long id;

    @Size(max = 512)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 512)
    @Column(name = "original_name", nullable = false)
    private String original_name;

    @Size(max = 1024)
    @Column(name = "description")
    private String description;

    @Column(name = "file_size", nullable = false)
    private Long file_size;

    @Size(max = 128)
    @Column(name = "mime_type")
    private String mime_type;

    @Size(max = 120)
    @Column(name = "alt")
    private String alt;

    @Size(max = 256)
    @Column(name = "path", nullable = false)
    private String path;

    @Size(max = 16)
    @Column(name = "extention", nullable = false)
    private String extention;

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

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

    public String getOriginal_name() {
        return original_name;
    }

    public void setOriginal_name(String original_name) {
        this.original_name = original_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getFile_size() {
        return file_size;
    }

    public void setFile_size(Long file_size) {
        this.file_size = file_size;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtention() {
        return extention;
    }

    public void setExtention(String extention) {
        this.extention = extention;
    }
}