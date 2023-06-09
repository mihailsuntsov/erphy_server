package com.dokio.message.response.additional;

public class StoreOrderingResultJSON {

    private StoreForOrderingJSON storeInfo;
    private String message;
    private Integer result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StoreForOrderingJSON getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(StoreForOrderingJSON storeInfo) {
        this.storeInfo = storeInfo;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }
}
