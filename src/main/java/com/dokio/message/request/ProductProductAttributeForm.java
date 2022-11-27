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

package com.dokio.message.request;

import java.util.List;

public class ProductProductAttributeForm {
    private Long attribute_id;
    private List<Long> terms_ids;
    private int  position;        //	Attribute position
    private boolean visible;      //    Define if the attribute is visible on the "Additional information" tab in the product's page. Default is false.
    private boolean variation;    //    Define if the attribute can be used as variation. Default is false.

    public List<Long> getTerms_ids() {
        return terms_ids;
    }

    public void setTerms_ids(List<Long> terms_ids) {
        this.terms_ids = terms_ids;
    }

    public Long getAttribute_id() {
        return attribute_id;
    }

    public void setAttribute_id(Long attribute_id) {
        this.attribute_id = attribute_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVariation() {
        return variation;
    }

    public void setVariation(boolean variation) {
        this.variation = variation;
    }
}
