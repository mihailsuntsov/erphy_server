package com.dokio.message.response.Settings;

public class SettingsReturnsupJSON {
    private Long        companyId;              // id предприятия
    private Long        departmentId;           // id отделения
    private Long        statusOnFinishId;       // статус документа при завершении инвентаризации
    private Boolean     autoAdd;                // автодобавление товара из формы поиска в таблицу

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

    public Long getStatusOnFinishId() {
        return statusOnFinishId;
    }

    public void setStatusOnFinishId(Long statusOnFinishId) {
        this.statusOnFinishId = statusOnFinishId;
    }

    public Boolean getAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(Boolean autoAdd) {
        this.autoAdd = autoAdd;
    }

}
