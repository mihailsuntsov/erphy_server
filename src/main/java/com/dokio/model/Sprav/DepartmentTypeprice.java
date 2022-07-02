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
