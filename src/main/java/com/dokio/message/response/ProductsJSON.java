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

package com.dokio.message.response;

import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.additional.DefaultAttributesJSON;
import com.dokio.message.response.additional.ProductVariationsJSON;
import com.dokio.message.response.additional.ResourceJSON;
import com.dokio.message.response.additional.StoreTranslationProductJSON;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

//@Entity
public class ProductsJSON {
//    @Id
    private Long            id;
    private String          name;
    private String          description;
    private String          article;
    private String          company;
    private Integer         company_id;
    private String          master;
    private Integer         master_id;
    private String          creator;
    private Integer         creator_id;
    private String          changer;
    private Integer         changer_id;
    private String          date_time_created;
    private String          date_time_changed;
    private Integer         productgroup_id;
    private String          productgroup;
//    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer>   product_categories_id;
    private Integer         product_code;
    private Integer         ppr_id;
    private Boolean         by_weight;
    private Integer         edizm_id;
    private Integer         nds_id;
    private BigDecimal      weight;
    private BigDecimal      volume;
    private Integer         weight_edizm_id;
    private Integer         volume_edizm_id;
    private Boolean         markable;
    private Integer         markable_group_id;
    private Boolean         excizable;
    private Long            product_code_free;
    private Boolean         not_buy;
    private Boolean         not_sell;
    private Boolean         indivisible;

///////////////////////////// STORE ///////////////////////////////////

    private String          type;
    private String          slug;
    private Boolean         featured;
    private String          short_description;
    private Boolean         virtual;
    private Boolean         downloadable;
    private Integer         download_limit;
    private Integer         download_expiry;
    private String          external_url;
    private String          button_text;
    private String          tax_status;
    private Boolean         manage_stock;
    private String          stock_status;
    private String          backorders;
    private Boolean         sold_individually;
    private BigDecimal      height;
    private BigDecimal      width;
    private BigDecimal      length;
    private String          shipping_class;
    private Boolean         reviews_allowed;
    private Long            parent_id;
    private String          purchase_note;
    private Integer         menu_order;
    private String          parent_name;
    private String          date_on_sale_to_gmt;
    private String          date_on_sale_from_gmt;
    private List<IdAndName> upsell_ids;
    private List<IdAndName> crosssell_ids;
    private List<IdAndName> grouped_ids;
    private BigDecimal      low_stock_threshold;
    private Boolean         outofstock_aftersale;   //auto set product as out-of-stock after it has been sold
    private String          label_description;
    private String          description_html;       // custom HTML full description
    private String          short_description_html; // custom HTML short description
    private String          description_type;       // "editor" or "custom"
    private String          short_description_type; // "editor" or "custom"
    private List<StoreTranslationProductJSON> storeProductTranslations;
    private Set<DefaultAttributesJSON> defaultAttributes;
    private List<ProductVariationsJSON> productVariations;
    private boolean isVariation;
    private List<ResourceJSON> productResourcesTable; // resources that used this service
    ////////////////// APPOINTMENTS //////////////////////
    private boolean is_srvc_by_appointment;
    private boolean scdl_is_employee_required;
    private int scdl_max_pers_on_same_time;
    private int scdl_srvc_duration;
    private int scdl_appointment_atleast_before_time;
    private Long scdl_appointment_atleast_before_unit_id;
    private List<Integer> scdl_customer_reminders;
    private List<Integer> scdl_employee_reminders;
    private List<String> scdl_assignments;

    public boolean isIs_srvc_by_appointment() {
        return is_srvc_by_appointment;
    }

    public void setIs_srvc_by_appointment(boolean is_srvc_by_appointment) {
        this.is_srvc_by_appointment = is_srvc_by_appointment;
    }

    public boolean isScdl_is_employee_required() {
        return scdl_is_employee_required;
    }

    public void setScdl_is_employee_required(boolean scdl_is_employee_required) {
        this.scdl_is_employee_required = scdl_is_employee_required;
    }

    public int getScdl_max_pers_on_same_time() {
        return scdl_max_pers_on_same_time;
    }

    public void setScdl_max_pers_on_same_time(int scdl_max_pers_on_same_time) {
        this.scdl_max_pers_on_same_time = scdl_max_pers_on_same_time;
    }

    public int getScdl_srvc_duration() {
        return scdl_srvc_duration;
    }

    public void setScdl_srvc_duration(int scdl_srvc_duration) {
        this.scdl_srvc_duration = scdl_srvc_duration;
    }

    public int getScdl_appointment_atleast_before_time() {
        return scdl_appointment_atleast_before_time;
    }

    public void setScdl_appointment_atleast_before_time(int scdl_appointment_atleast_before_time) {
        this.scdl_appointment_atleast_before_time = scdl_appointment_atleast_before_time;
    }

    public Long getScdl_appointment_atleast_before_unit_id() {
        return scdl_appointment_atleast_before_unit_id;
    }

    public void setScdl_appointment_atleast_before_unit_id(Long scdl_appointment_atleast_before_unit_id) {
        this.scdl_appointment_atleast_before_unit_id = scdl_appointment_atleast_before_unit_id;
    }

    public List<Integer> getScdl_customer_reminders() {
        return scdl_customer_reminders;
    }

    public void setScdl_customer_reminders(List<Integer> scdl_customer_reminders) {
        this.scdl_customer_reminders = scdl_customer_reminders;
    }

    public List<Integer> getScdl_employee_reminders() {
        return scdl_employee_reminders;
    }

    public void setScdl_employee_reminders(List<Integer> scdl_employee_reminders) {
        this.scdl_employee_reminders = scdl_employee_reminders;
    }

    public List<String> getScdl_assignments() {
        return scdl_assignments;
    }

    public void setScdl_assignments(List<String> scdl_assignments) {
        this.scdl_assignments = scdl_assignments;
    }

    public List<ResourceJSON> getProductResourcesTable() {
        return productResourcesTable;
    }

    public void setProductResourcesTable(List<ResourceJSON> productResourcesTable) {
        this.productResourcesTable = productResourcesTable;
    }

    public boolean isVariation() {
        return isVariation;
    }

    public void setVariation(boolean variation) {
        isVariation = variation;
    }

    public List<ProductVariationsJSON> getProductVariations() {
        return productVariations;
    }

    public void setProductVariations(List<ProductVariationsJSON> productVariations) {
        this.productVariations = productVariations;
    }

    public List<StoreTranslationProductJSON> getStoreProductTranslations() {
        return storeProductTranslations;
    }

    public Set<DefaultAttributesJSON> getDefaultAttributes() {
        return defaultAttributes;
    }

    public void setDefaultAttributes(Set<DefaultAttributesJSON> defaultAttributes) {
        this.defaultAttributes = defaultAttributes;
    }

    public void setStoreProductTranslations(List<StoreTranslationProductJSON> storeProductTranslations) {
        this.storeProductTranslations = storeProductTranslations;
    }

    public String getDescription_html() {
        return description_html;
    }

    public void setDescription_html(String description_html) {
        this.description_html = description_html;
    }

    public String getShort_description_html() {
        return short_description_html;
    }

    public void setShort_description_html(String short_description_html) {
        this.short_description_html = short_description_html;
    }

    public String getDescription_type() {
        return description_type;
    }

    public void setDescription_type(String description_type) {
        this.description_type = description_type;
    }

    public String getShort_description_type() {
        return short_description_type;
    }

    public void setShort_description_type(String short_description_type) {
        this.short_description_type = short_description_type;
    }

    public String getLabel_description() {
        return label_description;
    }

    public void setLabel_description(String label_description) {
        this.label_description = label_description;
    }

    public Boolean getOutofstock_aftersale() {
        return outofstock_aftersale;
    }

    public void setOutofstock_aftersale(Boolean outofstock_aftersale) {
        this.outofstock_aftersale = outofstock_aftersale;
    }

    public BigDecimal getLow_stock_threshold() {
        return low_stock_threshold;
    }

    public void setLow_stock_threshold(BigDecimal low_stock_threshold) {
        this.low_stock_threshold = low_stock_threshold;
    }

    public List<IdAndName> getGrouped_ids() {
        return grouped_ids;
    }

    public void setGrouped_ids(List<IdAndName> grouped_ids) {
        this.grouped_ids = grouped_ids;
    }

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

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Integer getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Integer master_id) {
        this.master_id = master_id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Integer creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public Integer getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Integer changer_id) {
        this.changer_id = changer_id;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public Integer getProductgroup_id() {
        return productgroup_id;
    }

    public void setProductgroup_id(Integer productgroup_id) {
        this.productgroup_id = productgroup_id;
    }

    public String getProductgroup() {
        return productgroup;
    }

    public void setProductgroup(String productgroup) {
        this.productgroup = productgroup;
    }

    public List<Integer> getProduct_categories_id() {
        return product_categories_id;
    }

    public void setProduct_categories_id(List<Integer> product_categories_id) {
        this.product_categories_id = product_categories_id;
    }

    public Integer getProduct_code() {
        return product_code;
    }

    public void setProduct_code(Integer product_code) {
        this.product_code = product_code;
    }

    public Integer getPpr_id() {
        return ppr_id;
    }

    public void setPpr_id(Integer ppr_id) {
        this.ppr_id = ppr_id;
    }

    public Boolean getBy_weight() {
        return by_weight;
    }

    public void setBy_weight(Boolean by_weight) {
        this.by_weight = by_weight;
    }

    public Integer getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Integer edizm_id) {
        this.edizm_id = edizm_id;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
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

    public Integer getWeight_edizm_id() {
        return weight_edizm_id;
    }

    public void setWeight_edizm_id(Integer weight_edizm_id) {
        this.weight_edizm_id = weight_edizm_id;
    }

    public Integer getVolume_edizm_id() {
        return volume_edizm_id;
    }

    public void setVolume_edizm_id(Integer volume_edizm_id) {
        this.volume_edizm_id = volume_edizm_id;
    }

    public Boolean getMarkable() {
        return markable;
    }

    public void setMarkable(Boolean markable) {
        this.markable = markable;
    }

    public Integer getMarkable_group_id() {
        return markable_group_id;
    }

    public void setMarkable_group_id(Integer markable_group_id) {
        this.markable_group_id = markable_group_id;
    }

    public Boolean getExcizable() {
        return excizable;
    }

    public void setExcizable(Boolean excizable) {
        this.excizable = excizable;
    }

    public Long getProduct_code_free() {
        return product_code_free;
    }

    public void setProduct_code_free(Long product_code_free) {
        this.product_code_free = product_code_free;
    }

    public Boolean getNot_buy() {
        return not_buy;
    }

    public void setNot_buy(Boolean not_buy) {
        this.not_buy = not_buy;
    }

    public Boolean getNot_sell() {
        return not_sell;
    }

    public void setNot_sell(Boolean not_sell) {
        this.not_sell = not_sell;
    }

    public Boolean getIndivisible() {
        return indivisible;
    }

    public void setIndivisible(Boolean indivisible) {
        this.indivisible = indivisible;
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

    public Long getParent_id() {
        return parent_id;
    }

    public void setParent_id(Long parent_id) {
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

    public String getParent_name() {
        return parent_name;
    }

    public void setParent_name(String parent_name) {
        this.parent_name = parent_name;
    }

    public String getDate_on_sale_to_gmt() {
        return date_on_sale_to_gmt;
    }

    public void setDate_on_sale_to_gmt(String date_on_sale_to_gmt) {
        this.date_on_sale_to_gmt = date_on_sale_to_gmt;
    }

    public String getDate_on_sale_from_gmt() {
        return date_on_sale_from_gmt;
    }

    public void setDate_on_sale_from_gmt(String date_on_sale_from_gmt) {
        this.date_on_sale_from_gmt = date_on_sale_from_gmt;
    }

    public List<IdAndName> getUpsell_ids() {
        return upsell_ids;
    }

    public void setUpsell_ids(List<IdAndName> upsell_ids) {
        this.upsell_ids = upsell_ids;
    }

    public List<IdAndName> getCrosssell_ids() {
        return crosssell_ids;
    }

    public void setCrosssell_ids(List<IdAndName> crosssell_ids) {
        this.crosssell_ids = crosssell_ids;
    }
}
