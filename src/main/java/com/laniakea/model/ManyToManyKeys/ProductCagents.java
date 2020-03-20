package com.laniakea.model.ManyToManyKeys;

import com.laniakea.model.Cagents;
import com.laniakea.model.Products;

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
