package com.dokio.message.request.Settings;

import java.math.BigDecimal;

public class SettingsInventoryForm {

    private Long        companyId;          // id предприятия
    private Long        departmentId;       // id отделения
    private String      name;               // наименование инвентаризации по-умолчанию
    private String      pricingType;        // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
    private Long        priceTypeId;        // тип цены из справочника Типы цен
    private BigDecimal  changePrice;        // наценка/скидка в цифре (например, 50)
    private String      plusMinus;          // определят, что есть changePrice - наценка или скидка (plus или minus)
    private String      changePriceType;    // тип наценки/скидки (валюта currency или проценты procents)
    private Boolean     hideTenths;         // убирать десятые (копейки)
    private Long        statusIdOnFinish;   // статус документа при завершении инвентаризации


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPricingType() {
        return pricingType;
    }

    public void setPricingType(String pricingType) {
        this.pricingType = pricingType;
    }

    public Long getPriceTypeId() {
        return priceTypeId;
    }

    public void setPriceTypeId(Long priceTypeId) {
        this.priceTypeId = priceTypeId;
    }

    public BigDecimal getChangePrice() {
        return changePrice;
    }

    public void setChangePrice(BigDecimal changePrice) {
        this.changePrice = changePrice;
    }

    public String getPlusMinus() {
        return plusMinus;
    }

    public void setPlusMinus(String plusMinus) {
        this.plusMinus = plusMinus;
    }

    public String getChangePriceType() {
        return changePriceType;
    }

    public void setChangePriceType(String changePriceType) {
        this.changePriceType = changePriceType;
    }

    public Boolean getHideTenths() {
        return hideTenths;
    }

    public void setHideTenths(Boolean hideTenths) {
        this.hideTenths = hideTenths;
    }

    public Long getStatusIdOnFinish() {
        return statusIdOnFinish;
    }

    public void setStatusIdOnFinish(Long statusIdOnFinish) {
        this.statusIdOnFinish = statusIdOnFinish;
    }
}
