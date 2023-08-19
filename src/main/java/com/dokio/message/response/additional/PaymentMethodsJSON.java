package com.dokio.message.response.additional;

public class PaymentMethodsJSON {

    private int id ;
    private String name; //              work (technical) name. Not used in user's interface
    private String img_address; //       like 'https://mysite.com/assets/img/stripe.jpg'
    private int output_order; //         1,2,3,...
    private String link; //              link to payment
    private boolean is_active;//         show or hide
    private String description_msg_key; // description of

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_address() {
        return img_address;
    }

    public void setImg_address(String img_address) {
        this.img_address = img_address;
    }

    public int getOutput_order() {
        return output_order;
    }

    public void setOutput_order(int output_order) {
        this.output_order = output_order;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public String getDescription_msg_key() {
        return description_msg_key;
    }

    public void setDescription_msg_key(String description_msg_key) {
        this.description_msg_key = description_msg_key;
    }
}
