package com.laniakea.model.Sprav;

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
