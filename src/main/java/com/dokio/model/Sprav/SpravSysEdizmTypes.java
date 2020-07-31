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

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="sprav_sys_edizm_types")
public class SpravSysEdizmTypes {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_edizm_id_seq", sequenceName="sprav_sys_edizm_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_edizm_id_seq")
    private Long id;

    @Column(name = "name")
    @Size(max = 128)
    private String name;

    @Column(name = "short_name")
    @Size(max = 64)
    private String si;

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

    public String getSi() {
        return si;
    }

    public void setSi(String si) {
        this.si = si;
    }
}
