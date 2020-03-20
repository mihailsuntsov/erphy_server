package com.laniakea.model.ManyToManyKeys;
// Данный класс выполняет роль первичного ключа для класса ProductFields и таблицы product_fields
// которая связывает таблицы products и product_group_fields
// https://www.baeldung.com/jpa-many-to-many

// Класс для связи Товаров и услуг (Products) и их поставщиков (Cagents)

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProductCagentsKey implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "cagent_id")
    private Long cagentId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCagentId() {
        return cagentId;
    }

    public void setCagentId(Long cagentId) {
        this.cagentId = cagentId;
    }
}