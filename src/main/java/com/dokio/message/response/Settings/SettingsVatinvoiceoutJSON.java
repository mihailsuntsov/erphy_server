/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.message.response.Settings;

public class SettingsVatinvoiceoutJSON {

    private Long        id;
    private Long        companyId;                      // id предприятия
    private Long        cagentId;                       // id поставщика
    private Long        cagent2Id;                      // id предприятия-грузополучателя
    private String      cagent;                         // наименование поставщика
    private String      cagent2;                        // наименование грузополучателя
    private Long        statusIdOnComplete;             // статус при успешном проведении

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCagentId() {
        return cagentId;
    }

    public void setCagentId(Long cagentId) {
        this.cagentId = cagentId;
    }

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public Long getStatusIdOnComplete() {
        return statusIdOnComplete;
    }

    public void setStatusIdOnComplete(Long statusIdOnComplete) {
        this.statusIdOnComplete = statusIdOnComplete;
    }

    public Long getCagent2Id() {
        return cagent2Id;
    }

    public void setCagent2Id(Long cagent2Id) {
        this.cagent2Id = cagent2Id;
    }

    public String getCagent2() {
        return cagent2;
    }

    public void setCagent2(String cagent2) {
        this.cagent2 = cagent2;
    }
}