/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
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
@Table(name="product_categories")
public class ProductCategories {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="product_categories_id_seq", sequenceName="product_categories_id_seq", allocationSize=1)
    @GeneratedValue(generator="product_categories_id_seq")
    private Long id;

    @Size(max = 512)
    @Column(name="name")
    private String name;

    @Size(max = 120)
    @Column(name="slug")
    private String slug;

    @Size(max = 250)
    @Column(name="description")
    private String description;

    @Size(max = 16)
    @Column(name="display")
    private String display;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

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

    @Column(name="is_store_category")
    private Boolean is_store_category;

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
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

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
    }

    public Boolean getIs_store_category() {
        return is_store_category;
    }

    public void setIs_store_category(Boolean is_store_category) {
        this.is_store_category = is_store_category;
    }
}
