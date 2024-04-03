package com.dokio.message.response.additional;

public class AppointmentUpdateReportJSON {

    private Long    id;                 // id новосозданного документа
    private Boolean success;            // успешно или нет прошло сохранение
    private Integer errorCode;          // код ошибки. 0 - недостаточно прав, 1 - ошибка сохранения документа, ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
