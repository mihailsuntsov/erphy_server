package com.dokio.message.response.store.woo.v3.products;

public class ImageJSON {

    private String          img_original_name;
    private String          img_address;
    private String          img_alt;

    public ImageJSON(String img_original_name, String img_address, String img_alt) {
        this.img_original_name = img_original_name;
        this.img_address = img_address;
        this.img_alt = img_alt;
    }

    public ImageJSON() {
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
}
