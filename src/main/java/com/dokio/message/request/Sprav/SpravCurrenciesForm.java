package com.dokio.message.request.Sprav;

public class SpravCurrenciesForm {

    private Long   id;
    private Long   company_id;
    private String name_short;
    private String name_full;
    private String code_lit;
    private String code_num;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getName_short() {
        return name_short;
    }

    public void setName_short(String name_short) {
        this.name_short = name_short;
    }

    public String getName_full() {
        return name_full;
    }

    public void setName_full(String name_full) {
        this.name_full = name_full;
    }

    public String getCode_lit() {
        return code_lit;
    }

    public void setCode_lit(String code_lit) {
        this.code_lit = code_lit;
    }

    public String getCode_num() {
        return code_num;
    }

    public void setCode_num(String code_num) {
        this.code_num = code_num;
    }
}
