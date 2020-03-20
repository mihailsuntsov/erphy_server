// Данный класс выполняет роль первичного ключа для класса ProductFields и таблицы product_fields
// которая связывает таблицы products и product_group_fields
// https://www.baeldung.com/jpa-many-to-many

package com.laniakea.model.ManyToManyKeys;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ProductFieldsKey  implements Serializable {

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "field_id")
    private Long fieldId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
}
