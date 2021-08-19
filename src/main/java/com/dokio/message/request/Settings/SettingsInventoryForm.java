package com.dokio.message.request.Settings;

import java.math.BigDecimal;

public class SettingsInventoryForm {

    private Long        companyId;              // id предприятия
    private Long        departmentId;           // id отделения
    private String      name;                   // наименование инвентаризации по-умолчанию
    private String      pricingType;            // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice) 5. Вручную (manual))
    private Long        priceTypeId;            // тип цены из справочника Типы цен
    private BigDecimal  changePrice;            // наценка/скидка в цифре (например, 50)
    private String      plusMinus;              // определят, что есть changePrice - наценка или скидка (plus или minus)
    private String      changePriceType;        // тип наценки/скидки (валюта currency или проценты procents)
    private Boolean     hideTenths;             // убирать десятые (копейки)
    private Long        statusOnFinishId;       // статус документа при завершении инвентаризации
    private String      defaultActualBalance;   // фактический баланс по умолчанию. "estimated" - как расчётный, "other" - другой (выбирается в other_actual_balance)
    private BigDecimal  otherActualBalance;     // другой фактический баланс по умолчанию. Например, 1
    private Boolean     autoAdd;                // автодобавление товара из формы поиска в таблицу

    public Boolean getAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(Boolean autoAdd) {
        this.autoAdd = autoAdd;
    }

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

    public Long getStatusOnFinishId() {
        return statusOnFinishId;
    }

    public void setStatusOnFinishId(Long statusOnFinishId) {
        this.statusOnFinishId = statusOnFinishId;
    }

    public BigDecimal getOtherActualBalance() {
        return otherActualBalance;
    }

    public void setOtherActualBalance(BigDecimal otherActualBalance) {
        this.otherActualBalance = otherActualBalance;
    }

    public String getDefaultActualBalance() {
        return defaultActualBalance;
    }

    public void setDefaultActualBalance(String defaultActualBalance) {
        this.defaultActualBalance = defaultActualBalance;
    }
}
