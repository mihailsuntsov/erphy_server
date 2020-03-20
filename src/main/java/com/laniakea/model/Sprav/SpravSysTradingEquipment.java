package com.laniakea.model.Sprav;

import com.laniakea.model.TradingEquipment;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="sprav_sys_trading_equipment")
public class SpravSysTradingEquipment {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_trading_equipment_id_seq", sequenceName="sprav_sys_trading_equipment_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_trading_equipment_id_seq")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "spravSysTradingEquipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TradingEquipment> tradingEquipment = new HashSet<TradingEquipment>();

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

    public Set<TradingEquipment> getTradingEquipment() {
        return tradingEquipment;
    }

    public void setTradingEquipment(Set<TradingEquipment> tradingEquipment) {
        this.tradingEquipment = tradingEquipment;
    }
}
