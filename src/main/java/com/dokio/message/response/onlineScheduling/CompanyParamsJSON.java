package com.dokio.message.response.onlineScheduling;

public class CompanyParamsJSON {

    private Long    masterId;
    private Long    companyId;
    private Integer time_zone_id;  //online scheduling settings time zone id
    private String  time_zone_name;//online scheduling settings time zone name
    private String  date_format;   //online scheduling settings locale
    private String  time_format;   //online scheduling settings time format

    public String getDate_format() {
        return date_format;
    }

    public void setDate_format(String date_format) {
        this.date_format = date_format;
    }

    public String getTime_format() {
        return time_format;
    }

    public void setTime_format(String time_format) {
        this.time_format = time_format;
    }

    public Integer getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(Integer time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    public String getTime_zone_name() {
        return time_zone_name;
    }

    public void setTime_zone_name(String time_zone_name) {
        this.time_zone_name = time_zone_name;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
