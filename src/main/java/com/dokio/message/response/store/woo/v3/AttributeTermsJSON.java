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
package com.dokio.message.response.store.woo.v3;

import java.util.List;

public class AttributeTermsJSON {

    private Integer queryResultCode; // look at _ErrorCodes file
    private List<AttributeTermJSON> attributeTerms;
    private List<Integer>  allTermsWooIds; // all Woo Ids that there is

    public List<Integer> getAllTermsWooIds() {
        return allTermsWooIds;
    }

    public void setAllTermsWooIds(List<Integer> allTermsWooIds) {
        this.allTermsWooIds = allTermsWooIds;
    }

    public Integer getQueryResultCode() {
        return queryResultCode;
    }

    public void setQueryResultCode(Integer queryResultCode) {
        this.queryResultCode = queryResultCode;
    }

    public List<AttributeTermJSON> getAttributeTerms() {
        return attributeTerms;
    }

    public void setAttributeTerms(List<AttributeTermJSON> attributeTerms) {
        this.attributeTerms = attributeTerms;
    }
}
