package com.dokio.message.response.additional.appointment;

public class ResourceOfDepartmentPart {

    private Long id;
    private String name;
    private Integer need_res_qtt;
    private Integer now_used;
    private Integer quantity_in_dep_part;

    public ResourceOfDepartmentPart() {
    }

    public ResourceOfDepartmentPart(Long id, String name, Integer need_res_qtt, Integer now_used, Integer quantity_in_dep_part) {
        this.id = id;
        this.name = name;
        this.need_res_qtt = need_res_qtt;
        this.now_used = now_used;
        this.quantity_in_dep_part = quantity_in_dep_part;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNeed_res_qtt() {
        return need_res_qtt;
    }

    public void setNeed_res_qtt(Integer need_res_qtt) {
        this.need_res_qtt = need_res_qtt;
    }

    public Integer getNow_used() {
        return now_used;
    }

    public void setNow_used(Integer now_used) {
        this.now_used = now_used;
    }

    public Integer getQuantity_in_dep_part() {
        return quantity_in_dep_part;
    }

    public void setQuantity_in_dep_part(Integer quantity_in_dep_part) {
        this.quantity_in_dep_part = quantity_in_dep_part;
    }
}
