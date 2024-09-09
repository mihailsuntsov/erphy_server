package com.dokio.message.request.additional;

public class AppointmentCustomer {

    private Long id;
    private int    row_id;
    private String name;
    private String email;
    private String telephone;
    private String additional;
    private String id_card;
    private String date_of_birth;    // always in DD.MM.YYYY
    private String sex;              // always male / female
    private String date_of_birth_user_format;  // formatted into user format like MM/DD/YYYY
    private String sex_user_format;            // translated to the user language like Male / Female, or Мужской / Женский

    public String getDate_of_birth_user_format() {
        return date_of_birth_user_format;
    }

    public void setDate_of_birth_user_format(String date_of_birth_user_format) {
        this.date_of_birth_user_format = date_of_birth_user_format;
    }

    public String getSex_user_format() {
        return sex_user_format;
    }

    public void setSex_user_format(String sex_user_format) {
        this.sex_user_format = sex_user_format;
    }

    public String getId_card() {
        return id_card;
    }

    public void setId_card(String id_card) {
        this.id_card = id_card;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRow_id() {
        return row_id;
    }

    public void setRow_id(int row_id) {
        this.row_id = row_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
