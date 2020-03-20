package com.laniakea.message.response;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProductCagentsJSON {

    @Id
    private Long cagent_id;
    private Long product_id;
    private String name;
    private String output_order;
    private String cagent_article;
    private String additional;

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOutput_order() {
        return output_order;
    }

    public void setOutput_order(String output_order) {
        this.output_order = output_order;
    }

    public String getCagent_article() {
        return cagent_article;
    }

    public void setCagent_article(String cagent_article) {
        this.cagent_article = cagent_article;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }
}


