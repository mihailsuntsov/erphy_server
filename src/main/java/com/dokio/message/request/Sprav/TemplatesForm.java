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

package com.dokio.message.request.Sprav;

public class TemplatesForm {

    private Long    id;
    private Long    company_id;
    private int     document_id;
    private String  name;
    private Long    file_id;
    private String  file_name;
    private String  file_original_name;
    private Boolean is_show;
    private Integer output_order;
    private String  type;
    private Integer num_labels_in_row;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNum_labels_in_row() {
        return num_labels_in_row;
    }

    public void setNum_labels_in_row(Integer num_labels_in_row) {
        this.num_labels_in_row = num_labels_in_row;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getFile_original_name() {
        return file_original_name;
    }

    public void setFile_original_name(String file_original_name) {
        this.file_original_name = file_original_name;
    }

    public Long getFile_id() {
        return file_id;
    }

    public void setFile_id(Long file_id) {
        this.file_id = file_id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    public Boolean getIs_show() {
        return is_show;
    }

    public void setIs_show(Boolean is_show) {
        this.is_show = is_show;
    }

    public Integer getOutput_order() {
        return output_order;
    }

    public void setOutput_order(Integer output_order) {
        this.output_order = output_order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
