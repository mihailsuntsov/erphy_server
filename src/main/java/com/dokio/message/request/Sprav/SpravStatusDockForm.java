/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.message.request.Sprav;

import java.util.List;

public class SpravStatusDockForm {

    private Long id;
    private Long company_id;
    private int dock_id;
    private String name;
    private int status_type;
    private String color;
    private int output_order;
    private String description;
    private boolean is_default;
    private List<Long> statusesIdsInOrderOfList;//List id статусов для упорядочивания по порядку вывода
    /*getters-setters*/

    public Long getId() {
        return id;
    }

    public boolean isIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public int getDock_id() {
        return dock_id;
    }

    public void setDock_id(int dock_id) {
        this.dock_id = dock_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus_type() {
        return status_type;
    }

    public void setStatus_type(int status_type) {
        this.status_type = status_type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getOutput_order() {
        return output_order;
    }

    public void setOutput_order(int output_order) {
        this.output_order = output_order;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getStatusesIdsInOrderOfList() {
        return statusesIdsInOrderOfList;
    }

    public void setStatusesIdsInOrderOfList(List<Long> statusesIdsInOrderOfList) {
        this.statusesIdsInOrderOfList = statusesIdsInOrderOfList;
    }

    @Override
    public String toString() {
        return "SpravStatusDockForm: id=" + this.id + ", company_id" + this.company_id + ", dock_id" + this.dock_id +
                ", name" + this.name;
    }
}
