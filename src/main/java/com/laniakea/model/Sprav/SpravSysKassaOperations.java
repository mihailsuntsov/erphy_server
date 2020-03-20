package com.laniakea.model.Sprav;

import com.laniakea.model.KassaOperations;

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
