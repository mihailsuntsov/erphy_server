package com.dokio.message.response;

import com.dokio.message.response.additional.PermissionsJSON;
import java.util.List;

public class DocPermissionsJSON {

    private int                     id;
    private String                  name;
    private List<PermissionsJSON>   permissions; // List типа {id: 584, name: "Просмотр документов по всем предприятиям"}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getPermissions() {
        return permissions;
    }

    public void setPermissions(List permissions) {
        this.permissions = permissions;
    }
}
