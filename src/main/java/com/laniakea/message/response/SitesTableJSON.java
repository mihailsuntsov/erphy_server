package com.laniakea.message.response;

import java.util.List;

public class SitesTableJSON {
    private List<Integer> receivedPagesList;
    private List<SitesJSON> table;

    public List<Integer> getReceivedPagesList() {
        return receivedPagesList;
    }

    public void setReceivedPagesList(List<Integer> receivedPagesList) {
        this.receivedPagesList = receivedPagesList;
    }

    public List<SitesJSON> getTable() {
        return table;
    }

    public void setTable(List<SitesJSON> table) {
        this.table = table;
    }
}
