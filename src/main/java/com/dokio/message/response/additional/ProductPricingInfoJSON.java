package com.dokio.message.response.additional;

import java.math.BigDecimal;

//информация по ценам на товар (например, для его расценки)
public class ProductPricingInfoJSON {

    private BigDecimal  priceOfTypePrice;    // цена по запрошенному id типа цены
    private BigDecimal  avgCostPrice;        // средняя себестоимость
    private BigDecimal  lastPurchasePrice;   // последняя закупочная цена
    private BigDecimal  avgPurchasePrice ;   // средняя закупочная цена

    public BigDecimal getPriceOfTypePrice() {
        return priceOfTypePrice;
    }

    public void setPriceOfTypePrice(BigDecimal priceOfTypePrice) {
        this.priceOfTypePrice = priceOfTypePrice;
    }

    public BigDecimal getAvgCostPrice() {
        return avgCostPrice;
    }

    public void setAvgCostPrice(BigDecimal avgCostPrice) {
        this.avgCostPrice = avgCostPrice;
    }

    public BigDecimal getLastPurchasePrice() {
        return lastPurchasePrice;
    }

    public void setLastPurchasePrice(BigDecimal lastPurchasePrice) {
        this.lastPurchasePrice = lastPurchasePrice;
    }

    public BigDecimal getAvgPurchasePrice() {
        return avgPurchasePrice;
    }

    public void setAvgPurchasePrice(BigDecimal avgPurchasePrice) {
        this.avgPurchasePrice = avgPurchasePrice;
    }
}
