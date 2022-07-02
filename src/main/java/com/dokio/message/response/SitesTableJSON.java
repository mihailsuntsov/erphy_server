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

package com.dokio.message.response;

import java.util.List;

public class SitesTableJSON {
    private List<Integer> receivedPagesList;
    private List<SitesJSON> table;

    public List<Integer> getReceivedPagesList() {
        return receivedPagesList;
    }

    public void setReceivedPagesList(List<Integer> receivedPagesList) {
        this.receivedPagesList = receivedPagesList;
    }

    public List<SitesJSON> getTable() {
        return table;
    }

    public void setTable(List<SitesJSON> table) {
        this.table = table;
    }
}
