package com.dokio.message.response.additional;

public class ProductLabel {
    private Long   id;
    private String companyName;
    private String productName;
    private String priceFull;
    private String priceIntegerPart;
    private String priceDecimalPart;
    private String productCode;
    private String sku;
    private String labelDescription;
    private String barcode;
    private String currency;
    private String shortUnit;
    private String date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getShortUnit() {
        return shortUnit;
    }

    public void setShortUnit(String shortUnit) {
        this.shortUnit = shortUnit;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPriceFull() {
        return priceFull;
    }

    public void setPriceFull(String priceFull) {
        this.priceFull = priceFull;
    }

    public String getPriceIntegerPart() {
        return priceIntegerPart;
    }

    public void setPriceIntegerPart(String priceIntegerPart) {
        this.priceIntegerPart = priceIntegerPart;
    }

    public String getPriceDecimalPart() {
        return priceDecimalPart;
    }

    public void setPriceDecimalPart(String priceDecimalPart) {
        this.priceDecimalPart = priceDecimalPart;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getLabelDescription() {
        return labelDescription;
    }

    public void setLabelDescription(String labelDescription) {
        this.labelDescription = labelDescription;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

}
