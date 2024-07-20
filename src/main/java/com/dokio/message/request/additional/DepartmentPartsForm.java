package com.dokio.message.request.additional;

import com.dokio.message.response.additional.IdAndNameJSON;
import com.dokio.message.response.additional.ResourceJSON;

import java.util.List;

public class DepartmentPartsForm {

    private Long id;
    private Long department_id;
    private String name;
    private String description;
    private int menu_order;
    private Boolean is_active;
    private List<IdAndNameJSON> deppartProducts;
    private List<ResourceJSON> deppartResourcesTable;

    public List<ResourceJSON> getDeppartResourcesTable() {
        return deppartResourcesTable;
    }

    public void setDeppartResourcesTable(List<ResourceJSON> deppartResourcesTable) {
        this.deppartResourcesTable = deppartResourcesTable;
    }

    public List<IdAndNameJSON> getDeppartProducts() {
        return deppartProducts;
    }

    public void setDeppartProducts(List<IdAndNameJSON> deppartProducts) {
        this.deppartProducts = deppartProducts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
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

    public int getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(int menu_order) {
        this.menu_order = menu_order;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }
}
