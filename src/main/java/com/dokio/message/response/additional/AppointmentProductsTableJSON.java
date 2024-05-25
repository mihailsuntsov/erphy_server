package com.dokio.message.response.additional;

import java.math.BigDecimal;

public class AppointmentProductsTableJSON {

    private Long        id;
    private Long        product_id;
    private Long        appointment_id;
    private Long        department_id; //отделение  (склад)
    private String      department;
    private BigDecimal  product_count;
    private Long        edizm_id;
    private BigDecimal  product_price;
    private BigDecimal  product_sumprice;
    private Long        nds_id;
    private Long        price_type_id;
    private String      price_type;
    private String      name;
    private String      nds;
    private String      edizm;
    private String      additional;
    private BigDecimal  reserved;           // сколько зарезервировано в других (не связанных с этой розн. продажей) Заказах покупателя
    private BigDecimal  total;              // всего на складе
    private String      ppr_name_api_atol;  // признак предмета расчета в системе Атол
    private Boolean     is_material;        // определяет материальный ли товар/услуга. Нужен для отображения полей, относящихся к товару и их скрытия в случае если это услуга (например, остатки на складе, резервы - это неприменимо к нематериальным вещам - услугам, работам)
    private Boolean     indivisible;        // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
    private Integer     row_num;            // номер строки при выводе печатной версии
    private BigDecimal  nds_value;          // сколько % НДС у данного товара
    private Boolean     is_srvc_by_appointment;                  // this service is selling by appointments
    private Boolean     scdl_is_employee_required;                   // a service provider is needed only at the start
    private Integer     scdl_max_pers_on_same_time;              // the number of persons to whom a service can be provided at a time by one service provider (1 - dentist or hairdresser, 5-10 - yoga class)
    private Integer     scdl_srvc_duration;                      // time minimal duration of the service.
    private Integer     scdl_appointment_atleast_before_time;    // minimum time before the start of the service for which customers can make an appointment
    private Integer     scdl_appointment_atleast_before_unit_id; // the unit of measure of minimum time before the start of the service for which customers can make an appointment

    public Boolean getIs_srvc_by_appointment() {
        return is_srvc_by_appointment;
    }

    public void setIs_srvc_by_appointment(Boolean is_srvc_by_appointment) {
        this.is_srvc_by_appointment = is_srvc_by_appointment;
    }

    public Boolean getScdl_is_employee_required() {
        return scdl_is_employee_required;
    }

    public void setScdl_is_employee_required(Boolean scdl_is_employee_required) {
        this.scdl_is_employee_required = scdl_is_employee_required;
    }

    public Integer getScdl_max_pers_on_same_time() {
        return scdl_max_pers_on_same_time;
    }

    public void setScdl_max_pers_on_same_time(Integer scdl_max_pers_on_same_time) {
        this.scdl_max_pers_on_same_time = scdl_max_pers_on_same_time;
    }

    public Integer getScdl_srvc_duration() {
        return scdl_srvc_duration;
    }

    public void setScdl_srvc_duration(Integer scdl_srvc_duration) {
        this.scdl_srvc_duration = scdl_srvc_duration;
    }

    public Integer getScdl_appointment_atleast_before_time() {
        return scdl_appointment_atleast_before_time;
    }

    public void setScdl_appointment_atleast_before_time(Integer scdl_appointment_atleast_before_time) {
        this.scdl_appointment_atleast_before_time = scdl_appointment_atleast_before_time;
    }

    public Integer getScdl_appointment_atleast_before_unit_id() {
        return scdl_appointment_atleast_before_unit_id;
    }

    public void setScdl_appointment_atleast_before_unit_id(Integer scdl_appointment_atleast_before_unit_id) {
        this.scdl_appointment_atleast_before_unit_id = scdl_appointment_atleast_before_unit_id;
    }

    public Integer getRow_num() {
        return row_num;
    }

    public void setRow_num(Integer row_num) {
        this.row_num = row_num;
    }

    public BigDecimal getNds_value() {
        return nds_value;
    }

    public void setNds_value(BigDecimal nds_value) {
        this.nds_value = nds_value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Long appointment_id) {
        this.appointment_id = appointment_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }

    public Long getNds_id() {
        return nds_id;
    }

    public void setNds_id(Long nds_id) {
        this.nds_id = nds_id;
    }

    public Long getPrice_type_id() {
        return price_type_id;
    }

    public void setPrice_type_id(Long price_type_id) {
        this.price_type_id = price_type_id;
    }

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNds() {
        return nds;
    }

    public void setNds(String nds) {
        this.nds = nds;
    }

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getPpr_name_api_atol() {
        return ppr_name_api_atol;
    }

    public void setPpr_name_api_atol(String ppr_name_api_atol) {
        this.ppr_name_api_atol = ppr_name_api_atol;
    }

    public Boolean getIs_material() {
        return is_material;
    }

    public void setIs_material(Boolean is_material) {
        this.is_material = is_material;
    }

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
    }
}
