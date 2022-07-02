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


package com.dokio.message.response.additional;

public class LinkedDocsLinksJSON {

    private String uid_from;
    private String uid_to;

    public String getUid_from() {
        return uid_from;
    }

    public void setUid_from(String uid_from) {
        this.uid_from = uid_from;
    }

    public String getUid_to() {
        return uid_to;
    }

    public void setUid_to(String uid_to) {
        this.uid_to = uid_to;
    }
}
