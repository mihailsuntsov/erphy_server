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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="sessions")
public class Sessions {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sessions_id_seq", sequenceName="sessions_id_seq", allocationSize=1)
    @GeneratedValue(generator="sessions_id_seq")
    private Long id;

    @Column(name="time_open")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date time_open;

    @Column(name="time_close")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Date time_close;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @ManyToOne
    @JoinColumn(name = "kassa_id", nullable = false)
    private TradingEquipment tradingEquipment;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<KassaOperations> kassaOperations = new HashSet<KassaOperations>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
    }

    public TradingEquipment getTradingEquipment() {
        return tradingEquipment;
    }

    public void setTradingEquipment(TradingEquipment tradingEquipment) {
        this.tradingEquipment = tradingEquipment;
    }

    public Set<KassaOperations> getKassaOperations() {
        return kassaOperations;
    }

    public void setKassaOperations(Set<KassaOperations> kassaOperations) {
        this.kassaOperations = kassaOperations;
    }

    public Date getTime_open() {
        return time_open;
    }

    public void setTime_open(Date time_open) {
        this.time_open = time_open;
    }

    public Date getTime_close() {
        return time_close;
    }

    public void setTime_close(Date time_close) {
        this.time_close = time_close;
    }
}
