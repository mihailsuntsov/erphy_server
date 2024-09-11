package com.dokio.message.request.additional;

import java.util.List;
import java.util.Set;

public class SetCategoriesToCagentsForm {

    private Set<Long> cagentsIds;
    private List<Long> categoriesIds;
    private Boolean save;

    public Set<Long> getCagentsIds() {
        return cagentsIds;
    }

    public void setCagentsIds(Set<Long> cagentsIds) {
        this.cagentsIds = cagentsIds;
    }

    public List<Long> getCategoriesIds() {
        return categoriesIds;
    }

    public void setCategoriesIds(List<Long> categoriesIds) {
        this.categoriesIds = categoriesIds;
    }

    public Boolean getSave() {
        return save;
    }

    public void setSave(Boolean save) {
        this.save = save;
    }
}
