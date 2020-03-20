//Класс для связи отделений с типами цен, относящихся к отделению
//Например, для отделения А могут быть привязаны типы цен "Оффлайн-цена для отделения А" и "Скидочная цена для всех отделений"

package com.laniakea.model.Sprav;
import com.laniakea.model.Departments;

import javax.persistence.*;

@Entity
@Table(name="department_typeprice")
public class DepartmentTypeprice {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="department_typeprice_id_seq", sequenceName="department_typeprice_id_seq", allocationSize=1)
    @GeneratedValue(generator="department_typeprice_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @ManyToOne
    @JoinColumn(name = "typeprice_id", nullable = false)
    private SpravTypePrices spravTypePrices;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
    }

    public SpravTypePrices getSpravTypePrices() {
        return spravTypePrices;
    }

    public void setSpravTypePrices(SpravTypePrices spravTypePrices) {
        this.spravTypePrices = spravTypePrices;
    }
}
