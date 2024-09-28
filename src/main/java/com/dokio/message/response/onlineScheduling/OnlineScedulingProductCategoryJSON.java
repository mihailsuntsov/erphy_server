package com.dokio.message.response.onlineScheduling;

public class OnlineScedulingProductCategoryJSON {

    private Long            id;
    private Long            parent_id;
    private String          description;
    private String          name;
    private String          slug;
    private String          img_original_name;
    private String          img_address;
    private String          img_alt;
    private Boolean         img_anonyme_access;
    private Integer         menu_order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParent_id() {
        return parent_id;
    }

    public void setParent_id(Long parent_id) {
        this.parent_id = parent_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getImg_original_name() {
        return img_original_name;
    }

    public void setImg_original_name(String img_original_name) {
        this.img_original_name = img_original_name;
    }

    public String getImg_address() {
        return img_address;
    }

    public void setImg_address(String img_address) {
        this.img_address = img_address;
    }

    public String getImg_alt() {
        return img_alt;
    }

    public void setImg_alt(String img_alt) {
        this.img_alt = img_alt;
    }

    public Boolean getImg_anonyme_access() {
        return img_anonyme_access;
    }

    public void setImg_anonyme_access(Boolean img_anonyme_access) {
        this.img_anonyme_access = img_anonyme_access;
    }

    public Integer getMenu_order() {
        return menu_order;
    }

    public void setMenu_order(Integer menu_order) {
        this.menu_order = menu_order;
    }
}
