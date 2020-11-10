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
package com.dokio.message.response.additional;

public class CustomersOrdersUpdateReportJSON {

    private Boolean success; // успешно или нет прошло сохранение
    private Integer fail_to_reserve; // количество позиций, не поставленных в резерв (например кто-то успел поставить в резерв раньше нас, и доступное кол-во стало меньше нашего резерва, а сумма резервов не может быть больше общего кол-ва товара)
    private Integer fail_to_delete;  // количество позиций, не принятых к удалению (например по ним уже началась отгрузка)

    public Integer getFail_to_reserve() {
        return fail_to_reserve;
    }

    public void setFail_to_reserve(Integer fail_to_reserve) {
        this.fail_to_reserve = fail_to_reserve;
    }

    public Integer getFail_to_delete() {
        return fail_to_delete;
    }

    public void setFail_to_delete(Integer fail_to_delete) {
        this.fail_to_delete = fail_to_delete;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
