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

import com.dokio.message.response.additional.StoreTranslationCategoryJSON;

import java.util.List;

public class ProductCategoryJSON {
    private Long            id;
    private String          description;
    private String          display;
    private ImageFileJSON   image;
    private String          name;
    private String          slug;
    private Long            parentCategoryId;
    private Long            companyId;
    private Boolean         isStoreCategory;
    private Boolean         isBookingCategory;
    private List<StoreTranslationCategoryJSON> storeCategoryTranslations;
    private List<Long>      storesIds;

    public Boolean getIsBookingCategory() {
        return isBookingCategory;
    }

    public void setIsBookingCategory(Boolean bookingCategory) {
        isBookingCategory = bookingCategory;
    }

    public List<Long> getStoresIds() {
        return storesIds;
    }

    public void setStoresIds(List<Long> storesIds) {
        this.storesIds = storesIds;
    }

    public Boolean getStoreCategory() {
        return isStoreCategory;
    }

    public void setStoreCategory(Boolean storeCategory) {
        isStoreCategory = storeCategory;
    }

    public List<StoreTranslationCategoryJSON> getStoreCategoryTranslations() {
        return storeCategoryTranslations;
    }

    public void setStoreCategoryTranslations(List<StoreTranslationCategoryJSON> storeCategoryTranslations) {
        this.storeCategoryTranslations = storeCategoryTranslations;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ImageFileJSON getImage() {
        return image;
    }

    public void setImage(ImageFileJSON image) {
        this.image = image;
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

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public Boolean getIsStoreCategory() {
        return this.isStoreCategory;
    }

    public void setIsStoreCategory(Boolean isStoreCategory) {
        this.isStoreCategory = isStoreCategory;
    }
}
