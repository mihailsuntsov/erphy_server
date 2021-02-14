/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.message.request.Settings;

import java.math.BigDecimal;

public class SettingsRetailSalesForm {

    private Long        companyId; //id предприятия
    private Long        departmentId; //id отделения
    private Long        customerId; //id покупателя
    private String      pricingType; //тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Себестоимость (costPrice) 3. Вручную (manual))
    private Long        priceTypeId;//тип цены из справочника Типы цен
    private BigDecimal changePrice; //наценка/скидка в цифре (например, 50)
    private String      plusMinus; //определяте что есть changePrice - наценка или скидка (plus или minus)
    private String      changePriceType;// тип наценки/скидки (валюта currency или проценты procents)
    private Boolean     hideTenths;//убирать десятые (копейки)
    private Boolean     saveSettings;//сохранять настройки (флажок "Сохранить настройки" будет установлен)
    private String      priorityTypePriceSide; // приоритет типа цены: Склад (sklad) Покупатель (cagent) Цена по-умолчанию (defprice)
    private String      name;// наименование заказа
    private Boolean     autocreateOnCheque; //автосоздание нового документа, если в текущем успешно напечатан чек
    private Long        statusIdOnAutocreateOnCheque;//Перед автоматическим созданием после успешного отбития чека документ сохраняется. Данный статус - это статус документа при таком сохранении

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getStatusIdOnAutocreateOnCheque() {
        return statusIdOnAutocreateOnCheque;
    }

    public void setStatusIdOnAutocreateOnCheque(Long statusIdOnAutocreateOnCheque) {
        this.statusIdOnAutocreateOnCheque = statusIdOnAutocreateOnCheque;
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

    public Boolean getAutocreateOnCheque() {
        return autocreateOnCheque;
    }

    public void setAutocreateOnCheque(Boolean autocreateOnCheque) {
        this.autocreateOnCheque = autocreateOnCheque;
    }
}
