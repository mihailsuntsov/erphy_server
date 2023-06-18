package com.dokio.message.request.additional;

public class RentStoreOrderForm{

    private String  userIp;
    private Boolean iagree;
    private Long    companyId;
    private Long    storeId ;
    private String  agreementType;
    private String  thirdLvlName;
    private String  agreementVer;
    private Boolean existedStoreVariation;
    private Long    parentVarSiteId;
    private String  position;
    private String  varName;

    public Boolean getExistedStoreVariation() {
        return existedStoreVariation;
    }

    public void setExistedStoreVariation(Boolean existedStoreVariation) {
        this.existedStoreVariation = existedStoreVariation;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public Boolean getIagree() {
        return iagree;
    }

    public void setIagree(Boolean iagree) {
        this.iagree = iagree;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getAgreementType() {
        return agreementType;
    }

    public void setAgreementType(String agreementType) {
        this.agreementType = agreementType;
    }

    public String getThirdLvlName() {
        return thirdLvlName;
    }

    public void setThirdLvlName(String thirdLvlName) {
        this.thirdLvlName = thirdLvlName;
    }

    public String getAgreementVer() {
        return agreementVer;
    }

    public void setAgreementVer(String agreementVer) {
        this.agreementVer = agreementVer;
    }

    public Long getParentVarSiteId() {
        return parentVarSiteId;
    }

    public void setParentVarSiteId(Long parentVarSiteId) {
        this.parentVarSiteId = parentVarSiteId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    @Override
    public String toString() {
        return "RentStoreOrderForm{" +
                "userIp='" + userIp + '\'' +
                ", iagree=" + iagree +
                ", companyId=" + companyId +
                ", storeId=" + storeId +
                ", agreementType='" + agreementType + '\'' +
                ", thirdLvlName='" + thirdLvlName + '\'' +
                ", agreementVer='" + agreementVer + '\'' +
                ", parentVarSiteId=" + parentVarSiteId +
                ", position='" + position + '\'' +
                ", varName='" + varName + '\'' +
                '}';
    }
}
