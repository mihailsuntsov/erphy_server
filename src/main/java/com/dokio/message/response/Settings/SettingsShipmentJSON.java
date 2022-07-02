/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.response.Settings;

import java.math.BigDecimal;


public class SettingsShipmentJSON {

    private Long id;
    private Long        companyId;                      // id предприятия
    private Long        departmentId;                   // id отделения
    private Long        customerId;                     // id покупателя
    private String      pricingType;                    // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Себестоимость (costPrice) 3. Вручную (manual))
    private Long        priceTypeId;                    // тип цены из справочника Типы цен
    private BigDecimal  changePrice;                    // наценка/скидка в цифре (например, 50)
    private String      plusMinus;                      // определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
    private String      changePriceType;                // тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
    private Boolean     hideTenths;                     // убирать десятые (копейки)
    private Boolean     saveSettings;                   // сохранять настройки (флажок "Сохранить настройки" будет установлен)
    private String      customer;                       // наименование покупателя
    private String      priorityTypePriceSide;          // приоритет типа цены: Склад (sklad) Покупатель (cagent) Цена по-умолчанию (defprice)
    private String      name;                           // наименование заказа
    private Boolean     autocreate;                     // автосоздание нового документа
    private Long        statusIdOnComplete;             // статус при успешном проведении
    private Boolean     showKkm;                        // показывать модуль ККМ
    private Boolean     autoAdd;                        // автодобавление

    public Boolean getAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(Boolean autoAdd) {
        this.autoAdd = autoAdd;
    }

    public Boolean getShowKkm() {
        return showKkm;
    }

    public void setShowKkm(Boolean showKkm) {
        this.showKkm = showKkm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public Boolean getSaveSettings() {
        return saveSettings;
    }

    public void setSaveSettings(Boolean saveSettings) {
        this.saveSettings = saveSettings;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getPriorityTypePriceSide() {
        return priorityTypePriceSide;
    }

    public void setPriorityTypePriceSide(String priorityTypePriceSide) {
        this.priorityTypePriceSide = priorityTypePriceSide;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAutocreate() {
        return autocreate;
    }

    public void setAutocreate(Boolean autocreate) {
        this.autocreate = autocreate;
    }

    public Long getStatusIdOnComplete() {
        return statusIdOnComplete;
    }

    public void setStatusIdOnComplete(Long statusIdOnComplete) {
        this.statusIdOnComplete = statusIdOnComplete;
    }
}
