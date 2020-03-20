package com.laniakea.message.request;

import java.util.Set;

public class FilesForm {
    private Long        id;
    private String      name;
    private String      original_name;
    private String      description;
    private Long     file_size;
    private String      mime_type;
    private Integer        company_id;
    private Boolean     anonyme_access;
    private Set<Long>   selectedFileCategories;

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

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

    public Boolean getAnonyme_access() {
        return anonyme_access;
    }

    public void setAnonyme_access(Boolean anonyme_access) {
        this.anonyme_access = anonyme_access;
    }

    public Set<Long> getSelectedFileCategories() {
        return selectedFileCategories;
    }

    public void setSelectedFileCategories(Set<Long> selectedFileCategories) {
        this.selectedFileCategories = selectedFileCategories;
    }
}
