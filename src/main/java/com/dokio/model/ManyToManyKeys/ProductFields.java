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

package com.dokio.model.ManyToManyKeys;

import com.dokio.model.ProductGroupFields;
import com.dokio.model.Products;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
public class ProductFields {

    @EmbeddedId
    ProductFieldsKey id;

    @ManyToOne
    @MapsId("product_id") //@MapsId означает, что мы связываем эти поля с частью ключа (ProductFieldsKey), и что
    @JoinColumn(name = "product_id")//они являются foreign keys для связи many-to-one
    private Products product;

    @ManyToOne
    @MapsId("field_id")//@MapsId ---//---//---
    @JoinColumn(name = "field_id")
    private ProductGroupFields productGroupField;

    @Column(name = "field_value")
    @Size(max = 256)
    private String fieldValue;

    public ProductFieldsKey getId() {
        return id;
    }

    public void setId(ProductFieldsKey id) {
        this.id = id;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public ProductGroupFields getProductGroupField() {
        return productGroupField;
    }

    public void setProductGroupField(ProductGroupFields productGroupField) {
        this.productGroupField = productGroupField;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
