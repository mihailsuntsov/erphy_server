package com.laniakea.message.response;

public class ProductsListJSON {
    private Long id;
    private String name;
    private Integer nds_id;
    private Long edizm_id;
    private String filename;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNds_id() {
        return nds_id;
    }

    public void setNds_id(Integer nds_id) {
        this.nds_id = nds_id;
    }

    public Long getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(Long edizm_id) {
        this.edizm_id = edizm_id;
    }
}
