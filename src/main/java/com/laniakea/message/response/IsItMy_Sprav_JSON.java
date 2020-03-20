package com.laniakea.message.response;

public class IsItMy_Sprav_JSON {

    private boolean itIsDocumentOfMyCompany;
    private boolean itIsDocumentOfMyMastersCompanies;

    public boolean isItIsDocumentOfMyCompany() {
        return itIsDocumentOfMyCompany;
    }

    public void setItIsDocumentOfMyCompany(boolean itIsDocumentOfMyCompany) {
        this.itIsDocumentOfMyCompany = itIsDocumentOfMyCompany;
    }

    public boolean isItIsDocumentOfMyMastersCompanies() {
        return itIsDocumentOfMyMastersCompanies;
    }

    public void setItIsDocumentOfMyMastersCompanies(boolean itIsDocumentOfMyMastersCompanies) {
        this.itIsDocumentOfMyMastersCompanies = itIsDocumentOfMyMastersCompanies;
    }
}
