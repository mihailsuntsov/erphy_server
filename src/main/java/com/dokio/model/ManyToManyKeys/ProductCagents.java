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

import com.dokio.model.Cagents;
import com.dokio.model.Products;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
public class ProductCagents {

    @EmbeddedId
    private ProductCagentsKey id;

    @ManyToOne
    @MapsId("product_id") //@MapsId означает, что мы связываем эти поля с частью ключа (ProductCagentsKey), и что
    @JoinColumn(name = "product_id")//они являются foreign keys для связи many-to-one
    private Products product;

    @ManyToOne
    @MapsId("field_id")//@MapsId ---//---//---
    @JoinColumn(name = "field_id")
    private Cagents cagents;

    @Column(name = "output_order", nullable = false)
    private Integer outputOrder;

    @Column(name = "cagent_article")
    @Size(max = 128)
    private String cagentArticle;

    @Column(name = "additional")
    @Size(max = 4096)
    private String additional;

    public ProductCagentsKey getId() {
        return id;
    }

    public void setId(ProductCagentsKey id) {
        this.id = id;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public Cagents getCagents() {
        return cagents;
    }

    public void setCagents(Cagents cagents) {
        this.cagents = cagents;
    }

    public Integer getOutputOrder() {
        return outputOrder;
    }

    public void setOutputOrder(Integer outputOrder) {
        this.outputOrder = outputOrder;
    }

    public String getCagentArticle() {
        return cagentArticle;
    }

    public void setCagentArticle(String cagentArticle) {
        this.cagentArticle = cagentArticle;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }
}
