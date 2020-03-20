package com.laniakea.message.response;

public class IsItMy_JSON {
    private boolean itIsDocumentOfMyCompany;
    private boolean itIsDocumentOfMyDepartments;
    private boolean itIsMyDocument;

    public boolean isItIsDocumentOfMyCompany() {
        return itIsDocumentOfMyCompany;
    }

    public void setItIsDocumentOfMyCompany(boolean itIsDocumentOfMyCompany) {
        this.itIsDocumentOfMyCompany = itIsDocumentOfMyCompany;
    }

    public boolean isItIsDocumentOfMyDepartments() {
        return itIsDocumentOfMyDepartments;
    }

    public void setItIsDocumentOfMyDepartments(boolean itIsDocumentOfMyDepartments) {
        this.itIsDocumentOfMyDepartments = itIsDocumentOfMyDepartments;
    }

    public boolean isItIsMyDocument() {
        return itIsMyDocument;
    }

    public void setItIsMyDocument(boolean itIsMyDocument) {
        this.itIsMyDocument = itIsMyDocument;
    }
}
