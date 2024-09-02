package com.dokio.message.request.additional;

import java.util.Set;

public class ChangeOwnerForm {

    Set<Long> documentIds;
    Long      newOwnerId;
    String    documentName;
    Long      documentRegistryId;
    String    editDocAllCompaniesPermit;
    String    editDocMyCompanyPermit;
    String    editMyDocPermit;

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Long getDocumentRegistryId() {
        return documentRegistryId;
    }

    public void setDocumentRegistryId(Long documentRegistryId) {
        this.documentRegistryId = documentRegistryId;
    }

    public String getEditDocAllCompaniesPermit() {
        return editDocAllCompaniesPermit;
    }

    public void setEditDocAllCompaniesPermit(String editDocAllCompaniesPermit) {
        this.editDocAllCompaniesPermit = editDocAllCompaniesPermit;
    }

    public String getEditDocMyCompanyPermit() {
        return editDocMyCompanyPermit;
    }

    public void setEditDocMyCompanyPermit(String editDocMyCompanyPermit) {
        this.editDocMyCompanyPermit = editDocMyCompanyPermit;
    }

    public String getEditMyDocPermit() {
        return editMyDocPermit;
    }

    public void setEditMyDocPermit(String editMyDocPermit) {
        this.editMyDocPermit = editMyDocPermit;
    }

    public Set<Long> getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(Set<Long> documentIds) {
        this.documentIds = documentIds;
    }

    public Long getNewOwnerId() {
        return newOwnerId;
    }

    public void setNewOwnerId(Long newOwnerId) {
        this.newOwnerId = newOwnerId;
    }

    @Override
    public String toString() {
        return "ChangeOwnerForm{" +
                "documentIds=" + documentIds +
                ", newOwnerId=" + newOwnerId +
                ", documentName='" + documentName + '\'' +
                ", documentRegistryId=" + documentRegistryId +
                ", editDocAllCompaniesPermit='" + editDocAllCompaniesPermit + '\'' +
                ", editDocMyCompanyPermit='" + editDocMyCompanyPermit + '\'' +
                ", editMyDocPermit='" + editMyDocPermit + '\'' +
                '}';
    }
}
