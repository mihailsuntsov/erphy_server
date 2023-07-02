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

    @Size(max = 16384)
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

    //////////////////////////////////////////////////////////////////////////////////////

    @Size(max = 8)
    @Column(name = "type")//Product type. Options: simple, grouped, external and variable. Default is simple.
    private String type;

    @Size(max = 120)
    @Column(name = "slug")//Product slug.
    private String slug;

    @Column(name = "featured")//Featured product. Default is false.
    private Boolean featured;

    @Size(max = 2048)
    @Column(name = "short_description")//If the product is virtual. Default is false.
    private String short_description;

    @Column(name = "virtual")//If the product is downloadable. Default is false.
    private Boolean virtual;

    @Column(name = "downloadable")//
    private Boolean downloadable;

    @Column(name = "download_limit")//Number of times downloadable files can be downloaded after purchase. Default is -1.
    private Integer download_limit;

    @Column(name = "download_expiry")//Number of days until access to downloadable files expires. Default is -1.
    private Integer download_expiry;

    @Size(max = 255)
    @Column(name = "external_url")//Product external URL. Only for external products.
    private String external_url;

    @Size(max = 60)
    @Column(name = "button_text")//Product external button text. Only for external products.
    private String button_text;

    @Size(max = 8)
    @Column(name = "tax_status")//Tax status. Options: taxable, shipping and none. Default is taxable.
    private String tax_status;

    @Column(name = "manage_stock")//Stock management at product level. Default is false.
    private Boolean manage_stock;

    @Size(max = 10)
    @Column(name = "stock_status")//Controls the stock status of the product. Options: instock, outofstock, onbackorder. Default is instock.
    private String stock_status;

    @Size(max = 6)
    @Column(name = "backorders")//If managing stock, this controls if backorders are allowed. Options: no, notify and yes. Default is no.
    private String backorders;

    @Column(name = "sold_individually")//Allow one item to be bought in a single order. Default is false.
    private Boolean sold_individually;

    @Column(name = "height")//Product height.
    private BigDecimal height;

    @Column(name = "width")//Product width.
    private BigDecimal width;

    @Column(name = "length")//Product length.
    private BigDecimal length;

    @Size(max = 120)
    @Column(name = "shipping_class")//Shipping class slug.
    private String shipping_class;

    @Column(name = "reviews_allowed")//Allow reviews. Default is true.
    private Boolean reviews_allowed;

    @Column(name = "outofstock_aftersale")//Set as Out-of-stock after first completed shipment. Default is false.
    private Boolean outofstock_aftersale;

    @ManyToOne
    @JoinColumn(name = "parent_id") // Product parent ID.
    private Products parent_id;

    @Size(max = 1000)
    @Column(name = "purchase_note")//Optional note to send the customer after purchase.
    private String purchase_note;

    @Column(name = "menu_order")//
    private Integer menu_order;

    @Size(max = 2048)
    @Column(name = "label_description")
    private String label_description;

    @Column(name="date_on_sale_from_gmt", nullable = false)
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_on_sale_from_gmt;

    @Column(name="date_on_sale_to_gmt")
    @JsonSerialize(using = com.dokio.util.JSONSerializer.class)
    @JsonDeserialize(using = com.dokio.util.JSONDeserialize.class)
    private Timestamp date_on_sale_to_gmt;

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

    public Boolean getOutofstock_aftersale() {
        return outofstock_aftersale;
    }

    public void setOutofstock_aftersale(Boolean outofstock_aftersale) {
        this.outofstock_aftersale = outofstock_aftersale;
    }

    public String getLabel_description() {
        return label_description;
    }

    public void setLabel_description(String label_description) {
        this.label_description = label_description;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public Boolean getVirtual() {
        return virtual;
    }

    public void setVirtual(Boolean virtual) {
        this.virtual = virtual;
    }

    public Boolean getDownloadable() {
        return downloadable;
    }

    public void setDownloadable(Boolean downloadable) {
        this.downloadable = downloadable;
    }

    public Integer getDownload_limit() {
        return download_limit;
    }

    public void setDownload_limit(Integer download_limit) {
        this.download_limit = download_limit;
    }

    public Integer getDownload_expiry() {
        return download_expiry;
    }

    public void setDownload_expiry(Integer download_expiry) {
        this.download_expiry = download_expiry;
    }

    public String getExternal_url() {
        return external_url;
    }

    public void setExternal_url(String external_url) {
        this.external_url = external_url;
    }

    public String getButton_text() {
        return button_text;
    }

    public void setButton_text(String button_text) {
        this.button_text = button_text;
    }

    public String getTax_status() {
        return tax_status;
    }

    public void setTax_status(String tax_status) {
        this.tax_status = tax_status;
    }

    public Boolean getManage_stock() {
        return manage_stock;
    }

    public void setManage_stock(Boolean manage_stock) {
        this.manage_stock = manage_stock;
    }

    public String getStock_status() {
        return stock_status;
    }

    public void setStock_status(String stock_status) {
        this.stock_status = stock_status;
    }

    public String getBackorders() {
        return backorders;
    }

    public void setBackorders(String backorders) {
        this.backorders = backorders;
    }

    public Boolean getSold_individually() {
        return sold_individually;
    }

    public void setSold_individually(Boolean sold_individually) {
        this.sold_individually = sold_individually;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public String getShipping_class() {
        return shipping_class;
    }

    public void setShipping_class(String shipping_class) {
        this.shipping_class = shipping_class;
    }

    public Boolean getReviews_allowed() {
        return reviews_allowed;
    }

    public void setReviews_allowed(Boolean reviews_allowed) {
        this.reviews_allowed = reviews_allowed;
    }

    public Products getParent_id() {
        return parent_id;
    }

    public void setParent_id(Products parent_id) {
        this.parent_id = parent_id;
    }

    public String getPurchase_note() {
        return purchase_note;
    }

    public void setPurchase_note(String purchase_note) {
        this.purchase_note = purchase_note;
    }

    public Integer getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(Integer menu_order) {
        this.menu_order = menu_order;
    }

    public Timestamp getDate_on_sale_from_gmt() {
        return date_on_sale_from_gmt;
    }

    public void setDate_on_sale_from_gmt(Timestamp date_on_sale_from_gmt) {
        this.date_on_sale_from_gmt = date_on_sale_from_gmt;
    }

    public Timestamp getDate_on_sale_to_gmt() {
        return date_on_sale_to_gmt;
    }

    public void setDate_on_sale_to_gmt(Timestamp date_on_sale_to_gmt) {
        this.date_on_sale_to_gmt = date_on_sale_to_gmt;
    }
}
