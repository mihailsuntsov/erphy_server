package com.dokio.message.request.store.woo.v3;

import java.util.List;

public class IntListForm {
    private String crmSecretKey;
    private List<Integer> idsSet;

    public String getCrmSecretKey() {
        return crmSecretKey;
    }

    public void setCrmSecretKey(String crmSecretKey) {
        this.crmSecretKey = crmSecretKey;
    }

    public List<Integer> getIdsSet() {
        return idsSet;
    }

    public void setIdsSet(List<Integer> idsSet) {
        this.idsSet = idsSet;
    }
}
