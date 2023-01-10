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
package com.dokio.message.response.additional;

public class BaseFiles {
    private String filePath;
    private String fileName;
    private String menuName;
    private Integer docId;
    private Long fileId;
    private String type; // the type of template/ Can be: "document", "label"
    private Integer num_labels_in_row; // quantity of labels in the each row (if type='label'), else = null

    public BaseFiles(String filePath, String fileName, String menuName, Integer docId, Long fileId, String type, Integer num_labels_in_row) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.menuName = menuName;
        this.docId = docId;
        this.fileId = fileId;
        this.type = type;
        this.num_labels_in_row = num_labels_in_row;
    }

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
