package com.laniakea.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="product_categories")
public class ProductCategories {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="product_categories_id_seq", sequenceName="product_categories_id_seq", allocationSize=1)
    @GeneratedValue(generator="product_categories_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @OrderBy("output_order")
    @OneToMany
    @JsonManagedReference //antagonist -
    @JoinColumn(name = "parent_id")
    private List<ProductCategories> children = new LinkedList<ProductCategories>();

    @JsonBackReference//antagonist -
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_id",insertable=false,updatable=false)
    private ProductCategories parent;

    @JsonIgnore
    @ManyToMany(mappedBy = "productCategories")
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

    public List<ProductCategories> getChildren() {
        return children;
    }

    public void setChildren(List<ProductCategories> children) {
        this.children = children;
    }

    public ProductCategories getParent() {
        return parent;
    }

    public void setParent(ProductCategories parent) {
        this.parent = parent;
    }
}
