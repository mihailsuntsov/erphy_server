package com.dokio.message.response.Settings;

public class SettingsGeneralJSON {

    private boolean showRegistrationLink;
    private boolean allowRegistration;
    private boolean showForgotLink;
    private boolean allowRecoverPassword;
    private String  backendVersion;
    private String  backendVersionDate;
    private String  databaseVersion;
    private String  databaseVersionDate;
    private String  showInSignin;
    private int     planDefaultId;

    public int getPlanDefaultId() {
        return planDefaultId;
    }

    public void setPlanDefaultId(int planDefaultId) {
        this.planDefaultId = planDefaultId;
    }

    public String getShowInSignin() {
        return showInSignin;
    }

    public void setShowInSignin(String showInSignin) {
        this.showInSignin = showInSignin;
    }

    public boolean isShowRegistrationLink() {
        return showRegistrationLink;
    }

    public void setShowRegistrationLink(boolean showRegistrationLink) {
        this.showRegistrationLink = showRegistrationLink;
    }

    public boolean isAllowRegistration() {
        return allowRegistration;
    }

    public void setAllowRegistration(boolean allowRegistration) {
        this.allowRegistration = allowRegistration;
    }

    public boolean isShowForgotLink() {
        return showForgotLink;
    }

    public void setShowForgotLink(boolean showForgotLink) {
        this.showForgotLink = showForgotLink;
    }

    public boolean isAllowRecoverPassword() {
        return allowRecoverPassword;
    }

    public void setAllowRecoverPassword(boolean allowRecoverPassword) {
        this.allowRecoverPassword = allowRecoverPassword;
    }

    public String getBackendVersion() {
        return backendVersion;
    }

    public void setBackendVersion(String backendVersion) {
        this.backendVersion = backendVersion;
    }

    public String getBackendVersionDate() {
        return backendVersionDate;
    }

    public void setBackendVersionDate(String backendVersionDate) {
        this.backendVersionDate = backendVersionDate;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getDatabaseVersionDate() {
        return databaseVersionDate;
    }

    public void setDatabaseVersionDate(String databaseVersionDate) {
        this.databaseVersionDate = databaseVersionDate;
    }
}
