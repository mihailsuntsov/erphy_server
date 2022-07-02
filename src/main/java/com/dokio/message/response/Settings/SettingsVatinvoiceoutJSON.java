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

public class SettingsVatinvoiceoutJSON {

    private Long        id;
    private Long        companyId;                      // id предприятия
    private Long        cagentId;                       // id поставщика
    private Long        cagent2Id;                      // id предприятия-грузополучателя
    private String      cagent;                         // наименование поставщика
    private String      cagent2;                        // наименование грузополучателя
    private Long        statusIdOnComplete;             // статус при успешном проведении

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

    public Long getCagentId() {
        return cagentId;
    }

    public void setCagentId(Long cagentId) {
        this.cagentId = cagentId;
    }

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public Long getStatusIdOnComplete() {
        return statusIdOnComplete;
    }

    public void setStatusIdOnComplete(Long statusIdOnComplete) {
        this.statusIdOnComplete = statusIdOnComplete;
    }

    public Long getCagent2Id() {
        return cagent2Id;
    }

    public void setCagent2Id(Long cagent2Id) {
        this.cagent2Id = cagent2Id;
    }

    public String getCagent2() {
        return cagent2;
    }

    public void setCagent2(String cagent2) {
        this.cagent2 = cagent2;
    }
}