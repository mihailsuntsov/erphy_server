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

package com.dokio.message.request;

import com.dokio.message.request.additional.DefaultAttributesForm;
import com.dokio.message.request.additional.ProductResourcesForm;
import com.dokio.message.request.additional.ProductVariationsForm;
import com.dokio.message.response.additional.DefaultAttributesJSON;
import com.dokio.message.response.additional.ProductPricesJSON;
import com.dokio.message.response.additional.StoreTranslationProductJSON;
//import com.dokio.message.response.additional.ProductProductAttributeJSON;

//import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class ProductsForm {
    private Long id;
    private String name;
    private String description;
    private String article;
    private Long company_id;
    private Long productgroup_id;
    private Set<Long> selectedProductCategories;
    private List<Long> imagesIdsInOrderOfList;// List id файлов-картинок для упорядочивания по месту в списке картинок товара (вобщем, для сохранения порядка картинок)
    private List<Long> cagentsIdsInOrderOfList;//List id контрагентов для упорядочивания по месту в списке поставщиков товара
    private Set<ProductPricesJSON> productPricesTable;
    private Integer product_code;
    private Long ppr_id;
    private boolean by_weight;
    private Long edizm_id;
    private Long nds_id;
    private String weight;
    private String volume;
    private Long weight_edizm_id;
    private Long volume_edizm_id;
    private boolean  markable;
    private Long markable_group_id;
    private boolean excizable;
    private Long product_code_free;
    private boolean not_buy;
    private boolean not_sell;
    private boolean indivisible;
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String linked_doc_name;//имя (таблицы) связанного документа
    private String  parent_uid;// uid исходящего (родительского) документа
    private String  child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.

///////////////////////////// STORE ///////////////////////////////////

    private String type;
    private String slug;
    private Boolean featured;
    private String short_description;
    private Boolean virtual;
    private Boolean downloadable;
    private Integer download_limit;
    private Integer download_expiry;
    private String external_url;
    private String button_text;
    private String tax_status;
    private Boolean manage_stock;
    private String stock_status;
    private String backorders;
    private Boolean sold_individually;
    private String height;
    private String width;
    private String length;
    private String shipping_class;
    private Boolean reviews_allowed;
    private Long parent_id;
    private String purchase_note;
    private Integer menu_order;
    private String date_on_sale_to_gmt;
    private String date_on_sale_from_gmt;
    private Set<Long> upsell_ids;
    private Set<Long> crosssell_ids;
    private Set<Long> grouped_ids;
    private String low_stock_threshold;
    private List<Long> dfilesIdsInOrderOfList;//List id
    private List<ProductProductAttributeForm> productAttributes;
    private Boolean outofstock_aftersale; //auto set product as out-of-stock after it has been sold
    private String label_description;
    private String          description_html;       // custom HTML full description
    private String          short_description_html; // custom HTML short description
    private String          description_type;       // "editor" or "custom"
    private String          short_description_type; // "editor" or "custom"
    private List<StoreTranslationProductJSON> storeProductTranslations;
    private Set<DefaultAttributesForm> defaultAttributes;
    private List<ProductVariationsForm> productVariations;


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

    public List<ProductVariationsForm> getProductVariations() {
        return productVariations;
    }
    private List<ProductResourcesForm> productResourcesTable; // resources that used this service

    public List<ProductResourcesForm> getProductResourcesTable() {
        return productResourcesTable;
    }

    public void setProductResourcesTable(List<ProductResourcesForm> productResourcesTable) {
        this.productResourcesTable = productResourcesTable;
    }

    public void setProductVariations(List<ProductVariationsForm> productVariations) {
        this.productVariations = productVariations;
    }

    public Set<DefaultAttributesForm> getDefaultAttributes() {
        return defaultAttributes;
    }

    public void setDefaultAttributes(Set<DefaultAttributesForm> defaultAttributes) {
        this.defaultAttributes = defaultAttributes;
    }

    public List<StoreTranslationProductJSON> getStoreProductTranslations() {
        return storeProductTranslations;
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

    public List<ProductProductAttributeForm> getProductAttributes() {
        return productAttributes;
    }

    public void setProductAttributes(List<ProductProductAttributeForm> productAttributes) {
        this.productAttributes = productAttributes;
    }

    public List<Long> getDfilesIdsInOrderOfList() {
        return dfilesIdsInOrderOfList;
    }

    public void setDfilesIdsInOrderOfList(List<Long> dfilesIdsInOrderOfList) {
        this.dfilesIdsInOrderOfList = dfilesIdsInOrderOfList;
    }

    public String getLow_stock_threshold() {
        return low_stock_threshold;
    }

    public void setLow_stock_threshold(String low_stock_threshold) {
        this.low_stock_threshold = low_stock_threshold;
    }

    public Set<Long> getGrouped_ids() {
        return grouped_ids;
    }

    public void setGrouped_ids(Set<Long> grouped_ids) {
        this.grouped_ids = grouped_ids;
    }

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public Long getLinked_doc_id() {
        return linked_doc_id;
    }

    public void setLinked_doc_id(Long linked_doc_id) {
        this.linked_doc_id = linked_doc_id;
    }

    public String getLinked_doc_name() {
        return linked_doc_name;
    }

    public void setLinked_doc_name(String linked_doc_name) {
        this.linked_doc_name = linked_doc_name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isIndivisible() {
        return indivisible;
    }

    public void setIndivisible(boolean indivisible) {
        this.indivisible = indivisible;
    }

    public Long getId() {
        return id;
    }

    public Long getProduct_code_free() {
        return product_code_free;
    }

    public boolean isNot_buy() {
        return not_buy;
    }

    public boolean isNot_sell() {
        return not_sell;
    }

    public void setNot_sell(boolean not_sell) {
        this.not_sell = not_sell;
    }

    public void setNot_buy(boolean not_buy) {
        this.not_buy = not_buy;
    }

    public void setProduct_code_free(Long product_code_free) {
        this.product_code_free = product_code_free;
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

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public Long getProductgroup_id() {
        return productgroup_id;
    }

    public void setProductgroup_id(Long productgroup_id) {
        this.productgroup_id = productgroup_id;
    }

    public Set<Long> getSelectedProductCategories() {
        return selectedProductCategories;
    }

    public void setSelectedProductCategories(Set<Long> selectedProductCategories) {
        this.selectedProductCategories = selectedProductCategories;
    }

    public List<Long> getImagesIdsInOrderOfList() {
        return imagesIdsInOrderOfList;
    }

    public void setImagesIdsInOrderOfList(List<Long> imagesIdsInOrderOfList) {
        this.imagesIdsInOrderOfList = imagesIdsInOrderOfList;
    }

    public Set<ProductPricesJSON> getProductPricesTable() {
        return productPricesTable;
    }

    public void setProductPricesTable(Set<ProductPricesJSON> productPricesTable) {
        this.productPricesTable = productPricesTable;
    }

    public List<Long> getCagentsIdsInOrderOfList() {
        return cagentsIdsInOrderOfList;
    }

    public void setCagentsIdsInOrderOfList(List<Long> cagentsIdsInOrderOfList) {
        this.cagentsIdsInOrderOfList = cagentsIdsInOrderOfList;
    }

    public Integer getProduct_code() {
        return product_code;
    }

    public void setProduct_code(Integer product_code) {
        this.product_code = product_code;
    }

    public Long getPpr_id() {
        return ppr_id;
    }

    public void setPpr_id(Long ppr_id) {
        this.ppr_id = ppr_id;
    }

    public boolean isBy_weight() {
        return by_weight;
    }

    public void setBy_weight(boolean by_weight) {
        this.by_weight = by_weight;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }

    public Long getNds_id() {
        return nds_id;
    }

    public void setNds_id(Long nds_id) {
        this.nds_id = nds_id;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public Long getWeight_edizm_id() {
        return weight_edizm_id;
    }

    public void setWeight_edizm_id(Long weight_edizm_id) {
        this.weight_edizm_id = weight_edizm_id;
    }

    public Long getVolume_edizm_id() {
        return volume_edizm_id;
    }

    public void setVolume_edizm_id(Long volume_edizm_id) {
        this.volume_edizm_id = volume_edizm_id;
    }

    public boolean isMarkable() {
        return markable;
    }

    public void setMarkable(boolean markable) {
        this.markable = markable;
    }

    public Long getMarkable_group_id() {
        return markable_group_id;
    }

    public void setMarkable_group_id(Long markable_group_id) {
        this.markable_group_id = markable_group_id;
    }

    public boolean isExcizable() {
        return excizable;
    }

    public void setExcizable(boolean excizable) {
        this.excizable = excizable;
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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
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

    public Set<Long> getUpsell_ids() {
        return upsell_ids;
    }

    public void setUpsell_ids(Set<Long> upsell_ids) {
        this.upsell_ids = upsell_ids;
    }

    public Set<Long> getCrosssell_ids() {
        return crosssell_ids;
    }

    public void setCrosssell_ids(Set<Long> crosssell_ids) {
        this.crosssell_ids = crosssell_ids;
    }

    @Override
    public String toString() {
        return "ProductsForm{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", article='" + article + '\'' +
                ", company_id=" + company_id +
                ", productgroup_id=" + productgroup_id +
                ", selectedProductCategories=" + selectedProductCategories +
                ", imagesIdsInOrderOfList=" + imagesIdsInOrderOfList +
                ", cagentsIdsInOrderOfList=" + cagentsIdsInOrderOfList +
                ", productPricesTable=" + productPricesTable +
                ", product_code=" + product_code +
                ", ppr_id=" + ppr_id +
                ", by_weight=" + by_weight +
                ", edizm_id=" + edizm_id +
                ", nds_id=" + nds_id +
                ", weight='" + weight + '\'' +
                ", volume='" + volume + '\'' +
                ", weight_edizm_id=" + weight_edizm_id +
                ", volume_edizm_id=" + volume_edizm_id +
                ", markable=" + markable +
                ", markable_group_id=" + markable_group_id +
                ", excizable=" + excizable +
                ", product_code_free=" + product_code_free +
                ", not_buy=" + not_buy +
                ", not_sell=" + not_sell +
                ", indivisible=" + indivisible +
                ", uid='" + uid + '\'' +
                ", linked_doc_id=" + linked_doc_id +
                ", linked_doc_name='" + linked_doc_name + '\'' +
                ", parent_uid='" + parent_uid + '\'' +
                ", child_uid='" + child_uid + '\'' +
                ", type='" + type + '\'' +
                ", slug='" + slug + '\'' +
                ", featured=" + featured +
                ", short_description='" + short_description + '\'' +
                ", virtual=" + virtual +
                ", downloadable=" + downloadable +
                ", download_limit=" + download_limit +
                ", download_expiry=" + download_expiry +
                ", external_url='" + external_url + '\'' +
                ", button_text='" + button_text + '\'' +
                ", tax_status='" + tax_status + '\'' +
                ", manage_stock=" + manage_stock +
                ", stock_status='" + stock_status + '\'' +
                ", backorders='" + backorders + '\'' +
                ", sold_individually=" + sold_individually +
                ", height='" + height + '\'' +
                ", width='" + width + '\'' +
                ", length='" + length + '\'' +
                ", shipping_class='" + shipping_class + '\'' +
                ", reviews_allowed=" + reviews_allowed +
                ", parent_id=" + parent_id +
                ", purchase_note='" + purchase_note + '\'' +
                ", menu_order=" + menu_order +
                ", date_on_sale_to_gmt='" + date_on_sale_to_gmt + '\'' +
                ", date_on_sale_from_gmt='" + date_on_sale_from_gmt + '\'' +
                ", upsell_ids=" + upsell_ids +
                ", crosssell_ids=" + crosssell_ids +
                ", grouped_ids=" + grouped_ids +
                ", low_stock_threshold='" + low_stock_threshold + '\'' +
                ", dfilesIdsInOrderOfList=" + dfilesIdsInOrderOfList +
                ", productAttributes=" + productAttributes +
                ", outofstock_aftersale=" + outofstock_aftersale +
                ", label_description='" + label_description + '\'' +
                ", description_html='" + description_html + '\'' +
                ", short_description_html='" + short_description_html + '\'' +
                ", description_type='" + description_type + '\'' +
                ", short_description_type='" + short_description_type + '\'' +
                ", storeProductTranslations=" + storeProductTranslations +
                ", defaultAttributes=" + defaultAttributes +
                ", productVariationsForm=" + productVariations +
                '}';
    }
}
