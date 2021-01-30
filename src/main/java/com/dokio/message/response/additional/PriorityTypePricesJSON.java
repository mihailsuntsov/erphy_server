package com.dokio.message.response.additional;

public class PriorityTypePricesJSON {

    private Long department_type_price_id; // тип цены отделения
    private Long cagent_type_price_id; // тип цены контрагента
    private Long default_type_price_id; // тип цены по умолчанию в справочнике типов цен

    public Long getDepartment_type_price_id() {
        return department_type_price_id;
    }

    public void setDepartment_type_price_id(Long department_type_price_id) {
        this.department_type_price_id = department_type_price_id;
    }

    public Long getCagent_type_price_id() {
        return cagent_type_price_id;
    }

    public void setCagent_type_price_id(Long cagent_type_price_id) {
        this.cagent_type_price_id = cagent_type_price_id;
    }

    public Long getDefault_type_price_id() {
        return default_type_price_id;
    }

    public void setDefault_type_price_id(Long default_type_price_id) {
        this.default_type_price_id = default_type_price_id;
    }
}
