package com.laniakea.model.Sprav;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laniakea.model.Products;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="sprav_sys_barcode")
public class SpravSysBarcode {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="sprav_sys_barcode_id_seq", sequenceName="sprav_sys_barcode_id_seq", allocationSize=1)
    @GeneratedValue(generator="sprav_sys_barcode_id_seq")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "barcodes")
    private Set<Products> products;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
    }
}
