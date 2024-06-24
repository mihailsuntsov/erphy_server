package com.dokio.message.request;

import com.dokio.message.response.additional.appointment.DepartmentPartWithResourcesIds;
import java.math.BigDecimal;
import java.util.List;

public class AppointmentProductsTableForm {

    private int  customerRowId;                     // ID строки покупателя (нужно для фронтэнда, т.к. все операции в таблице товаров делаются не по ID товаров и покупателей, а по их row_id
    private Long customerId;                        // ID покупателя
    private Long product_id;                        // id товара/услуги из таблицы products
    private Long appointment_id;                    // ID главного (родительского) документа
    private String name;                            // Наименование главного (родительского) документа
    private BigDecimal product_count;               // кол-во товара
    private String edizm;                           // Наименование единицы измерения
    private Long edizm_id;                          // ID единицы измерения
    private Long edizm_type_id;                     // ID типа единицы измерения
    private BigDecimal product_price;               // цена продажи
    private BigDecimal product_price_of_type_price; // цена по типу цены на момент составления документа
    private BigDecimal product_sumprice;            // сумма (цена*кол-во)
    private BigDecimal available;                   // Доступное количество товаров (относится только к товарам)
    private Long price_type_id;                     // ID примененного к цене товара типа цены
    private Long nds_id;                            // id ндс
    private BigDecimal total;                       // всего товара на складе
    private Long department_id;                     // ID отделения
    private Boolean is_material;                    // Материален ли товар (true = не услуга)
    private BigDecimal shipped;                     // отгружено (высчитывается, не сохраняется)
    private Boolean indivisible;                    // Неделимость (чтобы запрещать продавать дробное количество товара)
    private Boolean employeeRequired;               // Нужен ли исполнитель (сотрудник) для выполнения услуги (отнисится только у услугам по записи, т.е. isServiceByAppointment=true)
    private List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds; // все части отделений, где предлагается услуга, с информацией по количеству ресурсов в них
    private Integer unitOfMeasureTimeInSeconds;     // Едииница измерения выраженная в секундах (относится только к edizm_type_id = 6, 'Время')
    private Boolean isServiceByAppointment;         // сервис по записи
    private BigDecimal reserved_current;          // сколько зарезервировано в данном документе

    public BigDecimal getReserved_current() {
        return reserved_current;
    }

    public void setReserved_current(BigDecimal reserved_current) {
        this.reserved_current = reserved_current;
    }

    public Long getNds_id() {
        return nds_id;
    }

    public void setNds_id(Long nds_id) {
        this.nds_id = nds_id;
    }

    public BigDecimal getProduct_count() {
        return product_count;
    }

    public void setProduct_count(BigDecimal product_count) {
        this.product_count = product_count;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public BigDecimal getProduct_price() {
        return product_price;
    }

    public void setProduct_price(BigDecimal product_price) {
        this.product_price = product_price;
    }

    public BigDecimal getProduct_price_of_type_price() {
        return product_price_of_type_price;
    }

    public void setProduct_price_of_type_price(BigDecimal product_price_of_type_price) {
        this.product_price_of_type_price = product_price_of_type_price;
    }

    public BigDecimal getProduct_sumprice() {
        return product_sumprice;
    }

    public void setProduct_sumprice(BigDecimal product_sumprice) {
        this.product_sumprice = product_sumprice;
    }

    public BigDecimal getShipped() {
        return shipped;
    }

    public void setShipped(BigDecimal shipped) {
        this.shipped = shipped;
    }

    public int getCustomerRowId() {
        return customerRowId;
    }

    public void setCustomerRowId(int customerRowId) {
        this.customerRowId = customerRowId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Long appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEdizm() {
        return edizm;
    }

    public void setEdizm(String edizm) {
        this.edizm = edizm;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }

    public Long getEdizm_type_id() {
        return edizm_type_id;
    }

    public void setEdizm_type_id(Long edizm_type_id) {
        this.edizm_type_id = edizm_type_id;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public Long getPrice_type_id() {
        return price_type_id;
    }

    public void setPrice_type_id(Long price_type_id) {
        this.price_type_id = price_type_id;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
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

    public Boolean getEmployeeRequired() {
        return employeeRequired;
    }

    public void setEmployeeRequired(Boolean employeeRequired) {
        this.employeeRequired = employeeRequired;
    }

    public Integer getUnitOfMeasureTimeInSeconds() {
        return unitOfMeasureTimeInSeconds;
    }

    public void setUnitOfMeasureTimeInSeconds(Integer unitOfMeasureTimeInSeconds) {
        this.unitOfMeasureTimeInSeconds = unitOfMeasureTimeInSeconds;
    }

    public Boolean getIsServiceByAppointment() {
        return this.isServiceByAppointment;
    }

    public void setIsServiceByAppointment(Boolean isServiceByAppointment) {
        this.isServiceByAppointment = isServiceByAppointment;
    }

    public List<DepartmentPartWithResourcesIds> getDepartmentPartsWithResourcesIds() {
        return departmentPartsWithResourcesIds;
    }

    public void setDepartmentPartsWithResourcesIds(List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds) {
        this.departmentPartsWithResourcesIds = departmentPartsWithResourcesIds;
    }
}

