package com.dokio.message.request.onlineScheduling;

import java.util.Set;

public class OnlineSchedulingForm {

    String      companyUrlSlug;
    String      langCode;
    Set<Long>   servicesIds;
    Long        depId;

    public Long getDepId() {
        return depId;
    }

    public void setDepId(Long depId) {
        this.depId = depId;
    }

    public String getCompanyUrlSlug() {
        return companyUrlSlug;
    }

    public void setCompanyUrlSlug(String companyUrlSlug) {
        this.companyUrlSlug = companyUrlSlug;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public Set<Long> getServicesIds() {
        return servicesIds;
    }

    public void setServicesIds(Set<Long> servicesIds) {
        this.servicesIds = servicesIds;
    }
}
