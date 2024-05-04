package com.dokio.message.response.additional;

public class ResourceDepPart {

    private Long   resource_id;
    private String name;
    private String description;
    private int    resource_qtt;// quantity of resource
    private Long   dep_part_id; // department part of resource
    private Boolean isActive;   // for example, room may be under construction, car is on repairing, etc.

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Long getResource_id() {
        return resource_id;
    }

    public void setResource_id(Long resource_id) {
        this.resource_id = resource_id;
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

    public int getResource_qtt() {
        return resource_qtt;
    }

    public void setResource_qtt(int resource_qtt) {
        this.resource_qtt = resource_qtt;
    }

    public Long getDep_part_id() {
        return dep_part_id;
    }

    public void setDep_part_id(Long dep_part_id) {
        this.dep_part_id = dep_part_id;
    }
}
