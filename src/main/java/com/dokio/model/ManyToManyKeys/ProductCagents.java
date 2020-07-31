/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
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
