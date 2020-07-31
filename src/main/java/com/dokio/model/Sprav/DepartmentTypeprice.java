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
//Класс для связи отделений с типами цен, относящихся к отделению
//Например, для отделения А могут быть привязаны типы цен "Оффлайн-цена для отделения А" и "Скидочная цена для всех отделений"

package com.dokio.model.Sprav;
import com.dokio.model.Departments;

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
