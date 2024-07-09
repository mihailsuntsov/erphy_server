package com.dokio.message.request.Settings;

public class SettingsCalendarForm {

    private Long companyId;                 // company Id by default
    private String startView;               // month / scheduler / resources
    private int timelineStep;               // step of timeline in minutes
    private int dayStartMinute;             // minute of day start (0-1438) that means 00:00 - 23:58
    private int dayEndMinute;               // minute of day end (1-1439)   that means 00:01 - 23:59
    private String resourcesScreenScale;    // month / week / day
    private Boolean displayCancelled;       // display or not cancelled events by default

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getStartView() {
        return startView;
    }

    public void setStartView(String startView) {
        this.startView = startView;
    }

    public int getTimelineStep() {
        return timelineStep;
    }

    public void setTimelineStep(int timelineStep) {
        this.timelineStep = timelineStep;
    }

    public int getDayStartMinute() {
        return dayStartMinute;
    }

    public void setDayStartMinute(int dayStartMinute) {
        this.dayStartMinute = dayStartMinute;
    }

    public int getDayEndMinute() {
        return dayEndMinute;
    }

    public void setDayEndMinute(int dayEndMinute) {
        this.dayEndMinute = dayEndMinute;
    }

    public String getResourcesScreenScale() {
        return resourcesScreenScale;
    }

    public void setResourcesScreenScale(String resourcesScreenScale) {
        this.resourcesScreenScale = resourcesScreenScale;
    }

    public Boolean getDisplayCancelled() {
        return displayCancelled;
    }

    public void setDisplayCancelled(Boolean displayCancelled) {
        this.displayCancelled = displayCancelled;
    }
}
