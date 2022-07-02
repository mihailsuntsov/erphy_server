/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.message.response.additional;

public class CustomersOrdersUpdateReportJSON {

    private Long    id;                 // id новосозданного документа
    private Boolean success;            // успешно или нет прошло сохранение
    private Integer fail_to_reserve;    // количество позиций, не поставленных в резерв (например кто-то успел поставить в резерв раньше нас, и доступное кол-во стало меньше нашего резерва, а сумма резервов не может быть больше общего кол-ва товара)
    private Integer fail_to_delete;     // количество позиций, не принятых к удалению (например по ним уже началась отгрузка)
    private Integer errorCode;          // код ошибки. 0 - недостаточно прав, 1 - ошибка сохранения документа, 2 - ошибка обработки таблицы товаров

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

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
