package com.dokio.message.response.additional;

public class StoreTranslationProductJSON {

    private String langCode;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String descriptionHtml;
    private String shortDescriptionHtml;

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    public String getShortDescriptionHtml() {
        return shortDescriptionHtml;
    }

    public void setShortDescriptionHtml(String shortDescriptionHtml) {
        this.shortDescriptionHtml = shortDescriptionHtml;
    }
}
