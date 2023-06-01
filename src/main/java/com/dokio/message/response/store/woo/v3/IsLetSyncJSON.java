package com.dokio.message.response.store.woo.v3;

public class IsLetSyncJSON {

    private boolean is_sync_allowed;
    private String reason;

    public boolean isIs_sync_allowed() {
        return is_sync_allowed;
    }

    public void setIs_sync_allowed(boolean is_sync_allowed) {
        this.is_sync_allowed = is_sync_allowed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
