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
package com.dokio.model.Sprav;

import com.dokio.model.KassaOperations;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="sprav_sys_kassa_operations")
public class SpravSysKassaOperations {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_kassa_operations_id_seq", sequenceName="sprav_sys_kassa_operations_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_kassa_operations_id_seq")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "spravSysKassaOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<KassaOperations> kassaOperations = new HashSet<KassaOperations>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<KassaOperations> getKassaOperations() {
        return kassaOperations;
    }

    public void setKassaOperations(Set<KassaOperations> kassaOperations) {
        this.kassaOperations = kassaOperations;
    }
}
