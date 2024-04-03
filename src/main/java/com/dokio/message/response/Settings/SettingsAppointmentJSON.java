package com.dokio.message.response.Settings;

public class SettingsAppointmentJSON {
    private Long        id; //id
    private Long        companyId; //id предприятия
    private Long        departmentId; //id отделения
    private Boolean     hideTenths;//убирать десятые (копейки)
    private String      priorityTypePriceSide; // приоритет типа цены: Склад (sklad) Покупатель (cagent) Цена по-умолчанию (defprice)
    private Boolean     autocreateOnStart; //автосоздание на старте документа, если автозаполнились все поля
    private Long        statusIdOnAutocreateOnCheque;//Перед автоматическим созданием после успешного отбития чека документ сохраняется. Данный статус - это статус документа при таком сохранении

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

    public Boolean getHideTenths() {
        return hideTenths;
    }

    public void setHideTenths(Boolean hideTenths) {
        this.hideTenths = hideTenths;
    }

    public String getPriorityTypePriceSide() {
        return priorityTypePriceSide;
    }

    public void setPriorityTypePriceSide(String priorityTypePriceSide) {
        this.priorityTypePriceSide = priorityTypePriceSide;
    }

    public Boolean getAutocreateOnStart() {
        return autocreateOnStart;
    }

    public void setAutocreateOnStart(Boolean autocreateOnStart) {
        this.autocreateOnStart = autocreateOnStart;
    }

    public Long getStatusIdOnAutocreateOnCheque() {
        return statusIdOnAutocreateOnCheque;
    }

    public void setStatusIdOnAutocreateOnCheque(Long statusIdOnAutocreateOnCheque) {
        this.statusIdOnAutocreateOnCheque = statusIdOnAutocreateOnCheque;
    }
}
