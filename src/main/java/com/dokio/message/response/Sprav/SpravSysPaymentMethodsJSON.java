package com.dokio.message.response.Sprav;

public class SpravSysPaymentMethodsJSON {

    private Long id;
    private String name;
    private int id_api_atol;
    private String name_api_atol;

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

    public int getId_api_atol() {
        return id_api_atol;
    }

    public void setId_api_atol(int id_api_atol) {
        this.id_api_atol = id_api_atol;
    }

    public String getName_api_atol() {
        return name_api_atol;
    }

    public void setName_api_atol(String name_api_atol) {
        this.name_api_atol = name_api_atol;
    }
}
