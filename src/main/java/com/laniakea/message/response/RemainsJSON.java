package com.laniakea.message.response;

import java.util.List;

public class RemainsJSON {
    private List<Integer> receivedPagesList;
    private List<RemainsTableJSON> table;

    public List<Integer> getReceivedPagesList() {
        return receivedPagesList;
    }

    public void setReceivedPagesList(List<Integer> receivedPagesList) {
        this.receivedPagesList = receivedPagesList;
    }

    public List<RemainsTableJSON> getTable() {
        return table;
    }

    public void setTable(List<RemainsTableJSON> table) {
        this.table = table;
    }
}
