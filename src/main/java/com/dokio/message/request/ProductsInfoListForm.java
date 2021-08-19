package com.dokio.message.request;

import java.util.List;

//для запроса списка товаров по их id или id их категорий
public class ProductsInfoListForm {
    private Long companyId;             // предприятие, по которому идет запрос данных
    private Long departmentId;          // id отделения
    private Long priceTypeId;           // тип цены, по которому будут выданы цены
    private String reportOn;            // по категориям или по товарам/услугам (categories, products)
    private List<Long> reportOnIds;     // id категорий или товаров/услуг (того, что выбрано в reportOn)

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getPriceTypeId() {
        return priceTypeId;
    }

    public void setPriceTypeId(Long priceTypeId) {
        this.priceTypeId = priceTypeId;
    }

    public String getReportOn() {
        return reportOn;
    }

    public void setReportOn(String reportOn) {
        this.reportOn = reportOn;
    }

    public List<Long> getReportOnIds() {
        return reportOnIds;
    }

    public void setReportOnIds(List<Long> reportOnIds) {
        this.reportOnIds = reportOnIds;
    }
}
