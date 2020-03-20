package com.laniakea.message.request;

import java.util.Set;

public class UserGroupForm {
    private int id;
    private String name;
    private String description;
    private String company_id;
    private Set<Long> selectedUserGroupPermissions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getSelectedUserGroupPermissions() {
        return selectedUserGroupPermissions;
    }

    public void setSelectedUserGroupPermissions(Set<Long> selectedUserGroupPermissions) {
        this.selectedUserGroupPermissions = selectedUserGroupPermissions;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }
}
