package com.dokio.message.request.store.woo.v3;

import java.util.Set;

public class SyncIdsForm {

    private String crmSecretKey;
    private Set<SyncIdForm> idsSet;

    public String getCrmSecretKey() {
        return crmSecretKey;
    }

    public void setCrmSecretKey(String crmSecretKey) {
        this.crmSecretKey = crmSecretKey;
    }

    public Set<SyncIdForm> getIdsSet() {
        return idsSet;
    }

    public void setIdsSet(Set<SyncIdForm> idsSet) {
        this.idsSet = idsSet;
    }

    @Override
    public String toString() {
        return "SyncIdsForm{" +
                "crmSecretKey='" + crmSecretKey + '\'' +
                ", idsSet=" + idsSet +
                '}';
    }
}
