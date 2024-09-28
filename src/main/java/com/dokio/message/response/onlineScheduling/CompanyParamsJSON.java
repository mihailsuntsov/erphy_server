package com.dokio.message.response.onlineScheduling;

public class CompanyParamsJSON {

    private Long masterId;
    private Long companyId;
    private Integer time_zone_id;
    private String time_zone_name;

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
