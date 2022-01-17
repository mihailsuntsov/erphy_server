package com.dokio.message.request.Sprav;

import java.util.Set;

public class TemplatesListForm {

    private Long    company_id;
    private int     document_id;
    private Set<TemplatesForm> templatesList;

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    public Set<TemplatesForm> getTemplatesList() {
        return templatesList;
    }

    public void setTemplatesList(Set<TemplatesForm> templatesList) {
        this.templatesList = templatesList;
    }
}
