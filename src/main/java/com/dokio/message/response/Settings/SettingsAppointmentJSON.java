package com.dokio.message.response.Settings;

public class SettingsAppointmentJSON {

    private Long id;
    private Long companyId;
    private String startTime; // current / set_manually
    private String endDateTime; // no_calc / sum_all_length / max_length / calc_date_but_time / no_calc_date_but_time
    private String startTimeManually; // 'HH:mm' if start_time = 'set_manually'
    private String endTimeManually; // 'HH:mm' if end_time = 'calc_date_but_time' || 'no_calc_date_but_time'
    private boolean hideEmployeeField; // If for all services of company employees are not needed
    private boolean calcDateButTime; // if user wants to calc only dates. Suitable for hotels for checkout time
    private Long  statusIdOnComplete;             // status on completion
    private String descriptionDefaultTemplate;
    public boolean isCalcDateButTime() {
        return calcDateButTime;
    }

    public void setCalcDateButTime(boolean calcDateButTime) {
        this.calcDateButTime = calcDateButTime;
    }

    public SettingsAppointmentJSON() {
    }

    public SettingsAppointmentJSON(String startTime, String endDateTime, String startTimeManually, String endTimeManually, boolean hideEmployeeField, boolean calcDateButTime) {
        this.startTime = startTime;
        this.endDateTime = endDateTime;
        this.startTimeManually = startTimeManually;
        this.endTimeManually = endTimeManually;
        this.hideEmployeeField = hideEmployeeField;
        this.calcDateButTime = calcDateButTime;
    }

    public String getDescriptionDefaultTemplate() {
        return descriptionDefaultTemplate;
    }

    public void setDescriptionDefaultTemplate(String descriptionDefaultTemplate) {
        this.descriptionDefaultTemplate = descriptionDefaultTemplate;
    }

    public Long getStatusIdOnComplete() {
        return statusIdOnComplete;
    }

    public void setStatusIdOnComplete(Long statusIdOnComplete) {
        this.statusIdOnComplete = statusIdOnComplete;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getStartTimeManually() {
        return startTimeManually;
    }

    public void setStartTimeManually(String startTimeManually) {
        this.startTimeManually = startTimeManually;
    }

    public String getEndTimeManually() {
        return endTimeManually;
    }

    public void setEndTimeManually(String endTimeManually) {
        this.endTimeManually = endTimeManually;
    }

    public boolean isHideEmployeeField() {
        return hideEmployeeField;
    }

    public void setHideEmployeeField(boolean hideEmployeeField) {
        this.hideEmployeeField = hideEmployeeField;
    }
}
