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

import com.dokio.message.response.additional.ProductPricesJSON;

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

    @Override
    public String toString() {
        return "ProductsForm: id=" + this.id + ", name=" + this.name + ", article=" + this.article +
                ", company_id=" + this.company_id + ", productgroup_id=" + this.productgroup_id;
    }
}
