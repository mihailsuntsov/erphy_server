package com.dokio.message.response.additional;

import java.util.List;

public class UserProductDeppartsJSON {

    private Long product_id;
    private String product_name;
    private List<Long> dep_parts_ids; // - IDs of department parts where employee (user_id) can provide this service (product_id)

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public List<Long> getDep_parts_ids() {
        return dep_parts_ids;
    }

    public void setDep_parts_ids(List<Long> dep_parts_ids) {
        this.dep_parts_ids = dep_parts_ids;
    }
}
