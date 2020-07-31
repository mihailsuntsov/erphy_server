package com.dokio.model;
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

import com.dokio.model.Sprav.SpravSysTradingEquipment;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
//        @NamedQuery(name="tradingEquipment.getKassaForUser", query="" +
//                "select te.id,te.name from public.trading_equipment te where (te.type_id=(select sste.id from public.sprav_sys_trading_equipment sste where sste.name='Касса онлайн'))"
//        )

//        @NamedQuery(name="tradingEquipment.getKassaForUser", query="" +
//                "select id,  from TradingEquipment te"
//        )
})

@Entity
@Table(name="trading_equipment")
public class TradingEquipment {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="trading_equipment_id_seq", sequenceName="trading_equipment_id_seq", allocationSize=1)
    @GeneratedValue(generator="trading_equipment_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private SpravSysTradingEquipment spravSysTradingEquipment;

    @Column(name="type_id", insertable = false, updatable = false)
    private Long type_id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @Column(name="department_id", insertable = false, updatable = false)
    private Long department_id;

    @OneToMany(mappedBy = "tradingEquipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Sessions> session = new HashSet<Sessions>();

    @Column(name="description")
    private String description;

    @Column(name="name")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SpravSysTradingEquipment getSpravSysTradingEquipment() {
        return spravSysTradingEquipment;
    }

    public void setSpravSysTradingEquipment(SpravSysTradingEquipment spravSysTradingEquipment) {
        this.spravSysTradingEquipment = spravSysTradingEquipment;
    }

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
    }

    public Set<Sessions> getSession() {
        return session;
    }

    public void setSession(Set<Sessions> session) {
        this.session = session;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getType_id() {
        return type_id;
    }

    public void setType_id(Long type_id) {
        this.type_id = type_id;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }
}
