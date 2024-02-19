package com.dokio.message.request.additional;

public class ProductResourcesForm {

    private Long resource_id; // ID of resource
    private int  resource_qtt;// quantity of resource

    public Long getResource_id() {
        return resource_id;
    }

    public void setResource_id(Long resource_id) {
        this.resource_id = resource_id;
    }

    public int getResource_qtt() {
        return resource_qtt;
    }

    public void setResource_qtt(int resource_qtt) {
        this.resource_qtt = resource_qtt;
    }
}
