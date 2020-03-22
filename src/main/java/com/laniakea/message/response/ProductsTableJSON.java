package com.laniakea.message.response;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.List;

@Entity
public class ProductsTableJSON {
    @Id
    private Long        id;
    private String      name;
    private String      description;
    private String      article;
    private String      company;
    private String      company_id;
    private String      master;
    private String      master_id;
    private String      creator;
    private String      creator_id;
    private String      changer;
    private String      changer_id;
    private String      date_time_created;
    private String      date_time_changed;
    private String      productgroup_id;
    private String      productgroup;
    ////////////////////////////////////////////////////////////////
    private String      product_code;
    private String      ppr_id;
    private Boolean     by_weight;
    private String      edizm_id;
    private String      nds_id;
    private String      weight;
    private String      volume;
    private String      weight_edizm_id;
    private String      volume_edizm_id;
    private Boolean     markable;
    private String      markable_group_id;
    private Boolean     excizable;
    private Boolean     not_buy;
    private Boolean     not_sell;

    public Long getId() {
        return id;
    }

    public String getProduct_code() {
        return product_code;
    }

    public Boolean getNot_buy() {
        return not_buy;
    }

    public void setNot_buy(Boolean not_buy) {
        this.not_buy = not_buy;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }

    public String getPpr_id() {
        return ppr_id;
    }

    public void setPpr_id(String ppr_id) {
        this.ppr_id = ppr_id;
    }

    public Boolean getNot_sell() {
        return not_sell;
    }

    public void setNot_sell(Boolean not_sell) {
        this.not_sell = not_sell;
    }

    public Boolean getBy_weight() {
        return by_weight;
    }

    public void setBy_weight(Boolean by_weight) {
        this.by_weight = by_weight;
    }

    public String getEdizm_id() {
        return edizm_id;
    }

    public void setEdizm_id(String edizm_id) {
        this.edizm_id = edizm_id;
    }

    public String getNds_id() {
        return nds_id;
    }

    public void setNds_id(String nds_id) {
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

    public String getWeight_edizm_id() {
        return weight_edizm_id;
    }

    public void setWeight_edizm_id(String weight_edizm_id) {
        this.weight_edizm_id = weight_edizm_id;
    }

    public String getVolume_edizm_id() {
        return volume_edizm_id;
    }

    public void setVolume_edizm_id(String volume_edizm_id) {
        this.volume_edizm_id = volume_edizm_id;
    }

    public Boolean getMarkable() {
        return markable;
    }

    public void setMarkable(Boolean markable) {
        this.markable = markable;
    }

    public String getMarkable_group_id() {
        return markable_group_id;
    }

    public void setMarkable_group_id(String markable_group_id) {
        this.markable_group_id = markable_group_id;
    }

    public Boolean getExcizable() {
        return excizable;
    }

    public void setExcizable(Boolean excizable) {
        this.excizable = excizable;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getMaster_id() {
        return master_id;
    }

    public void setMaster_id(String master_id) {
        this.master_id = master_id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public String getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(String changer_id) {
        this.changer_id = changer_id;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public String getProductgroup_id() {
        return productgroup_id;
    }

    public void setProductgroup_id(String productgroup_id) {
        this.productgroup_id = productgroup_id;
    }

    public String getProductgroup() {
        return productgroup;
    }

    public void setProductgroup(String productgroup) {
        this.productgroup = productgroup;
    }

}
