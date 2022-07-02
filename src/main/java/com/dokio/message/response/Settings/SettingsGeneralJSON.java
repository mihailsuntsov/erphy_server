/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/
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
