package com.dokio.message.response.additional.appointment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class AppointmentService {

    private Long        id;
    private String      name;
    private Long        departmentId;
    private String      departmentName;
    private Integer     nds_id;                     // id ндс
    private Long        edizm_id;                   // id ед. измерения / unit of measurement's ID
    private String      edizm;                      // наименование ед. измерения / unit of measurement's name
    private Integer     edizm_type_id;              // 6=time, 2=weight, ...
    private BigDecimal  edizm_multiplier;           // The multiplier tells the system the ratio of your and international units
    private BigDecimal  total;                      // всего товаров в отделении
    private Boolean     is_material;                // определяет материальный ли товар/услуга. Нужен для отображения полей, относящимся к товару и их скрытия в противном случае (например, остатки на складе, резервы - это неприменимо к нематериальным вещам - услугам, работам)
    private Boolean     indivisible;                // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
    private BigDecimal  priceOfTypePrice;           // цена по запрошенному id типа цены

    private Boolean     isEmployeeRequired;         // Whether employee is necessary required to do this service job?
    private Integer     maxPersOnSameTime;          // How many persons can get this service in one appointment by the same time
    private BigDecimal  srvcDurationInSeconds;      // Approx. duration time to fininsh this service
    private BigDecimal  atLeastBeforeTimeInSeconds; // Minimum time before the start of the service for which customers can make an appointment
    private BigDecimal  unitOfMeasureTimeInSeconds; // If unit of measure is 'Time' type - it will be as 1 unit in seconds, else 0
    private Boolean     isServiceByAppointment;     // It's a service and it's a service by appointment
    private BigDecimal  reserved;
    private List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds;

    // these are parameters need for return products list for getAppointmentValuesById query
    private BigDecimal  product_count;
    private BigDecimal  product_price;
    private BigDecimal  product_sumprice;
    private Long        cagent_id;
    private int         cagent_row_id;

    public int getCagent_row_id() {
        return cagent_row_id;
    }

    public void setCagent_row_id(int cagent_row_id) {
        this.cagent_row_id = cagent_row_id;
    }

    public Boolean getServiceByAppointment() {
        return isServiceByAppointment;
    }

    public void setServiceByAppointment(Boolean serviceByAppointment) {
        isServiceByAppointment = serviceByAppointment;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
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

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public BigDecimal getReserved() {
        return reserved;
    }

    public void setReserved(BigDecimal reserved) {
        this.reserved = reserved;
    }

    public Boolean getIsServiceByAppointment() {
        return this.isServiceByAppointment;
    }

    public void setIsServiceByAppointment(Boolean isServiceByAppointment) {
        this.isServiceByAppointment = isServiceByAppointment;
    }

    public BigDecimal getUnitOfMeasureTimeInSeconds() {
        return unitOfMeasureTimeInSeconds;
    }

    public void setUnitOfMeasureTimeInSeconds(BigDecimal unitOfMeasureTimeInSeconds) {
        this.unitOfMeasureTimeInSeconds = unitOfMeasureTimeInSeconds;
    }

    public Boolean getEmployeeRequired() {
        return isEmployeeRequired;
    }

    public void setEmployeeRequired(Boolean employeeRequired) {
        isEmployeeRequired = employeeRequired;
    }

    public Integer getMaxPersOnSameTime() {
        return maxPersOnSameTime;
    }

    public void setMaxPersOnSameTime(Integer maxPersOnSameTime) {
        this.maxPersOnSameTime = maxPersOnSameTime;
    }

    public BigDecimal getSrvcDurationInSeconds() {
        return srvcDurationInSeconds;
    }

    public void setSrvcDurationInSeconds(BigDecimal srvcDurationInSeconds) {
        this.srvcDurationInSeconds = srvcDurationInSeconds;
    }

    public BigDecimal getAtLeastBeforeTimeInSeconds() {
        return atLeastBeforeTimeInSeconds;
    }

    public void setAtLeastBeforeTimeInSeconds(BigDecimal atLeastBeforeTimeInSeconds) {
        this.atLeastBeforeTimeInSeconds = atLeastBeforeTimeInSeconds;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public Integer getEdizm_type_id() {
        return edizm_type_id;
    }

    public void setEdizm_type_id(Integer edizm_type_id) {
        this.edizm_type_id = edizm_type_id;
    }

    public BigDecimal getEdizm_multiplier() {
        return edizm_multiplier;
    }

    public void setEdizm_multiplier(BigDecimal edizm_multiplier) {
        this.edizm_multiplier = edizm_multiplier;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public BigDecimal getPriceOfTypePrice() {
        return priceOfTypePrice;
    }

    public void setPriceOfTypePrice(BigDecimal priceOfTypePrice) {
        this.priceOfTypePrice = priceOfTypePrice;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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

    public List<DepartmentPartWithResourcesIds> getDepartmentPartsWithResourcesIds() {
        return departmentPartsWithResourcesIds;
    }

    public void setDepartmentPartsWithResourcesIds(List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds) {
        this.departmentPartsWithResourcesIds = departmentPartsWithResourcesIds;
    }
}
