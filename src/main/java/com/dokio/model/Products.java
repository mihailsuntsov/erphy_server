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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.dokio.model.ManyToManyKeys.ProductFields;
import com.dokio.model.Sprav.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="products")
public class Products {

    @Id
    @Column(name="id")
    @SequenceGenerator(name="products_id_seq", sequenceName="products_id_seq", allocationSize=1)
    @GeneratedValue(generator="products_id_seq")
    private Long id;

    @Size(max = 512)
    private String name;

    @Size(max = 2048)
    @Column(name = "description")
    private String description;

    @Size(max = 128)
    @Column(name = "article")
    private String article;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Companies company;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "changer_id")
    private User changer;

    @Column(name="date_time_created", nullable = false)
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_created;

    @Column(name="date_time_changed")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_time_changed;

    @Column(name = "is_archive")//Удалён
    private Boolean is_archive;

    @Column(name = "not_buy")//товар не закупается (нужно, чтобы отфильтровать товары, которые больше не закупаются)
    private Boolean not_buy;

    @Column(name = "not_sell")//товар сянт с продажи (нужно, чтобы отфильтровать товары, которые сняты с продажи)
    private Boolean not_sell;

    @ManyToOne
    @JoinColumn(name = "group_id")     // группа товаров или услуг
    private ProductGroups productGroup;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_productcategories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<ProductCategories> productCategories = new HashSet<>();

    @OneToMany(mappedBy = "product")
    Set<ProductFields> fieldValues;

    @ManyToMany(mappedBy = "products")
    private Set<Files> images;

    //////////////////////////////////////////////////////////////////////////////////////

    @Column(name = "product_code")// Код весового товара (для штрихкода EAN-13 весовых продуктов), генерируется системой по запросу пользователя в generateWeightProductCode
    private Integer product_code;

    @ManyToOne
    @JoinColumn(name = "ppr_id")// Признак предмета расчета (Товар, Услуга, Работа...)
    private SpravSysPPR ppr;

    @Column(name = "by_weight")//Весовой товар
    private Boolean by_weight;

    @ManyToOne
    @JoinColumn(name = "edizm_id") // единицы измерения
    private SpravSysEdizm edizm;

    @ManyToOne
    @JoinColumn(name = "nds_id") // НДС
    private SpravSysNds nds;

    @Column(name = "weight") // Вес
    private BigDecimal weight;

    @Column(name = "volume") // Объем
    private BigDecimal volume;

    @ManyToOne
    @JoinColumn(name = "weight_edizm_id") // Единица измерения веса
    private SpravSysEdizm weight_edizm;

    @ManyToOne
    @JoinColumn(name = "volume_edizm_id") // Единица измерения объема
    private SpravSysEdizm volume_edizm;

    @Column(name = "indivisible")// неделимый товар
    private Boolean indivisible;

    @Column(name = "markable")// Маркированный товар
    private Boolean markable;

    @ManyToOne
    @JoinColumn(name = "markable_group_id")  // Группа маркированных товаров (обувь, духи, пиво и т.д.)
    private SpravSysMarkableGroup markable_group;

    @Column(name = "excizable")// Подакцизный товар
    private Boolean excizable;

    @ManyToMany(fetch = FetchType.LAZY) // поставщики (у одного товара может быть много поставщиков)
    @JoinTable(name = "product_cagents",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "cagent_id"))
    private Set<Cagents> cagents = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY) // штрих-коды (у одного товара может быть много кодов)
    @JoinTable(name = "product_barcodes",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "barcode_id"))
    private Set<SpravSysBarcode> barcodes = new HashSet<>();


    @Column(name = "product_code_free") // Свободный код товара для самостоятельного ввода (в отличии от генерируемого системой весового кода product_code)
    private Long product_code_free;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<ProductCategories> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(Set<ProductCategories> productCategories) {
        this.productCategories = productCategories;
    }

    public Boolean getNot_sell() {
        return not_sell;
    }

    public void setNot_sell(Boolean not_sell) {
        this.not_sell = not_sell;
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

    public Boolean getNot_buy() {
        return not_buy;
    }

    public void setNot_buy(Boolean not_buy) {
        this.not_buy = not_buy;
    }

    public Long getProduct_code_free() {
        return product_code_free;
    }

    public void setProduct_code_free(Long product_code_free) {
        this.product_code_free = product_code_free;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public User getMaster() {
        return master;
    }

    public void setMaster(User master) {
        this.master = master;
    }

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getChanger() {
        return changer;
    }

    public void setChanger(User changer) {
        this.changer = changer;
    }

    public Timestamp getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(Timestamp date_time_created) {
        this.date_time_created = date_time_created;
    }

    public Timestamp getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(Timestamp date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public Boolean getIs_archive() {
        return is_archive;
    }

    public void setIs_archive(Boolean is_archive) {
        this.is_archive = is_archive;
    }

    public ProductGroups getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(ProductGroups productGroup) {
        this.productGroup = productGroup;
    }

    public Integer getProduct_code() {
        return product_code;
    }

    public void setProduct_code(Integer product_code) {
        this.product_code = product_code;
    }

    public SpravSysPPR getPpr() {
        return ppr;
    }

    public void setPpr(SpravSysPPR ppr) {
        this.ppr = ppr;
    }

    public Boolean getBy_weight() {
        return by_weight;
    }

    public void setBy_weight(Boolean by_weight) {
        this.by_weight = by_weight;
    }

    public SpravSysEdizm getEdizm() {
        return edizm;
    }

    public void setEdizm(SpravSysEdizm edizm) {
        this.edizm = edizm;
    }

    public SpravSysNds getNds() {
        return nds;
    }

    public void setNds(SpravSysNds nds) {
        this.nds = nds;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public SpravSysEdizm getWeight_edizm() {
        return weight_edizm;
    }

    public void setWeight_edizm(SpravSysEdizm weight_edizm) {
        this.weight_edizm = weight_edizm;
    }

    public SpravSysEdizm getVolume_edizm() {
        return volume_edizm;
    }

    public void setVolume_edizm(SpravSysEdizm volume_edizm) {
        this.volume_edizm = volume_edizm;
    }

    public Boolean getMarkable() {
        return markable;
    }

    public void setMarkable(Boolean markable) {
        this.markable = markable;
    }

    public SpravSysMarkableGroup getMarkable_group() {
        return markable_group;
    }

    public void setMarkable_group(SpravSysMarkableGroup markable_group) {
        this.markable_group = markable_group;
    }

    public Boolean getExcizable() {
        return excizable;
    }

    public void setExcizable(Boolean excizable) {
        this.excizable = excizable;
    }

    public Set<ProductFields> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(Set<ProductFields> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public Set<Files> getImages() {
        return images;
    }

    public void setImages(Set<Files> images) {
        this.images = images;
    }

    public Set<Cagents> getCagents() {
        return cagents;
    }

    public void setCagents(Set<Cagents> cagents) {
        this.cagents = cagents;
    }

    public Set<SpravSysBarcode> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(Set<SpravSysBarcode> barcodes) {
        this.barcodes = barcodes;
    }
}
