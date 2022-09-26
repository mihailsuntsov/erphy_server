/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.request;

import java.util.Set;

public class FilesForm {
    private Long        id;
    private String      name;
    private String      original_name;
    private String      description;
    private Long        file_size;
    private String      mime_type;
    private Integer     company_id;
    private Boolean     anonyme_access;
    private String      alt;
    private Set<Long>   selectedFileCategories;

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

    @Override
    public String toString() {
        return "FilesForm: id" + this.id + ", name=" + this.name +
                ", original_name=" + this.original_name;
    }
}
