package com.dokio.message.response.additional.calendar;

import java.sql.Timestamp;

public class PointOfScedule {

    private String      pointOfSceduleTime;
    private Timestamp   pointOfSceduleTimestamp;
    private Long        userId;
    private String      userName;
    private String      pointOfSceduleName;
    private Long        workshift_id;

    public Long getWorkshift_id() {
        return workshift_id;
    }

    public void setWorkshift_id(Long workshift_id) {
        this.workshift_id = workshift_id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPointOfSceduleTime() {
        return pointOfSceduleTime;
    }

    public void setPointOfSceduleTime(String pointOfSceduleTime) {
        this.pointOfSceduleTime = pointOfSceduleTime;
    }

    public Timestamp getPointOfSceduleTimestamp() {
        return pointOfSceduleTimestamp;
    }

    public void setPointOfSceduleTimestamp(Timestamp pointOfSceduleTimestamp) {
        this.pointOfSceduleTimestamp = pointOfSceduleTimestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPointOfSceduleName() {
        return pointOfSceduleName;
    }

    public void setPointOfSceduleName(String pointOfSceduleName) {
        this.pointOfSceduleName = pointOfSceduleName;
    }
}
