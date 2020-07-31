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
package com.dokio.message.response;

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
