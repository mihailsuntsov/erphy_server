package com.laniakea.message.response;

import com.laniakea.message.response.PricesTableJSON;

import java.util.List;

public class PricesJSON {
    private List<Integer> receivedPagesList;
    private List<PricesTableJSON> table;

    public List<Integer> getReceivedPagesList() {
        return receivedPagesList;
    }

    public void setReceivedPagesList(List<Integer> receivedPagesList) {
        this.receivedPagesList = receivedPagesList;
    }

    public List<PricesTableJSON> getTable() {
        return table;
    }

    public void setTable(List<PricesTableJSON> table) {
        this.table = table;
    }
}
