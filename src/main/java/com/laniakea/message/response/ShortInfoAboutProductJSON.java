package com.laniakea.message.response;
import java.math.BigDecimal;
public class ShortInfoAboutProductJSON {


    private BigDecimal  quantity;
    private BigDecimal  change;
    private BigDecimal  avg_purchase_price;
    private BigDecimal  last_purchase_price;
    private BigDecimal  avg_netcost_price;
    private String      date_time_created;

    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getChange() {
        return change;
    }
    public void setChange(BigDecimal change) {
        this.change = change;
    }
    public BigDecimal getAvg_purchase_price() {
        return avg_purchase_price;
    }
    public void setAvg_purchase_price(BigDecimal avg_purchase_price) {
        this.avg_purchase_price = avg_purchase_price;
    }
    public BigDecimal getAvg_netcost_price() {
        return avg_netcost_price;
    }
    public void setAvg_netcost_price(BigDecimal avg_netcost_price) {
        this.avg_netcost_price = avg_netcost_price;
    }
    public String getDate_time_created() {
        return date_time_created;
    }
    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }
    public BigDecimal getLast_purchase_price() {
        return last_purchase_price;
    }
    public void setLast_purchase_price(BigDecimal last_purchase_price) {
        this.last_purchase_price = last_purchase_price;
    }
}
