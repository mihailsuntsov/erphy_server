package com.dokio.message.request.additional;

public class ResourceDepPartsForm {

    private Long department_id;
    private Long id; // ID of department part
    private int resource_qtt;

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getResource_qtt() {
        return resource_qtt;
    }

    public void setResource_qtt(int resource_qtt) {
        this.resource_qtt = resource_qtt;
    }
}
