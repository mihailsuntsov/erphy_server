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
package com.dokio.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="cagent_categories")
public class CagentCategories {
    @Id
    @Column(name="id")
    @SequenceGenerator(name="cagent_categories_id_seq", sequenceName="cagent_categories_id_seq", allocationSize=1)
    @GeneratedValue(generator="cagent_categories_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @OrderBy("output_order")
    @OneToMany
    @JsonManagedReference //antagonist -
    @JoinColumn(name = "parent_id")
    private List<CagentCategories> children = new LinkedList<CagentCategories>();

    @JsonBackReference//antagonist -
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_id",insertable=false,updatable=false)
    private CagentCategories parent;

    @JsonIgnore
    @ManyToMany(mappedBy = "cagentCategories")
    private Set<Cagents> cagents;

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

    public List<CagentCategories> getChildren() {
        return children;
    }

    public void setChildren(List<CagentCategories> children) {
        this.children = children;
    }

    public CagentCategories getParent() {
        return parent;
    }

    public void setParent(CagentCategories parent) {
        this.parent = parent;
    }

    public Set<Cagents> getCagents() {
        return cagents;
    }

    public void setCagents(Set<Cagents> cagents) {
        this.cagents = cagents;
    }
}
