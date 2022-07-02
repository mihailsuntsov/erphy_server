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

package com.dokio.message.response.Settings;

public class KassaCashierSettingsJSON {
    private Long   selected_kassa_id;       // id выбранной кассы
    private String cashier_value_id;        // кассир: 'current'-текущая учетная запись, 'another'-другая учетная запись, 'custom' произвольные ФИО. Настройка "Другая учетная запись" во фронтенде сохраняется только до конца сессии, затем ставится по умолчанию current
    private String customCashierFio;        // ФИО для кассира, выбранного по cashier_value_id = 'custom'
    private String customCashierVatin;      // ИНН для кассира, выбранного по cashier_value_id = 'custom'
    private String billing_address;         //выбор адреса места расчётов. 'Settings' - как в настройках кассы, 'customer' - брать из адреса заказчика, 'custom' произвольный адрес
    private String custom_billing_address;  //адрес места расчётов для billing_address = 'custom'

    public Long getSelected_kassa_id() {
        return selected_kassa_id;
    }

    public void setSelected_kassa_id(Long selected_kassa_id) {
        this.selected_kassa_id = selected_kassa_id;
    }

    public String getCashier_value_id() {
        return cashier_value_id;
    }

    public void setCashier_value_id(String cashier_value_id) {
        this.cashier_value_id = cashier_value_id;
    }

    public String getCustomCashierFio() {
        return customCashierFio;
    }

    public void setCustomCashierFio(String customCashierFio) {
        this.customCashierFio = customCashierFio;
    }

    public String getCustomCashierVatin() {
        return customCashierVatin;
    }

    public void setCustomCashierVatin(String customCashierVatin) {
        this.customCashierVatin = customCashierVatin;
    }

    public String getBilling_address() {
        return billing_address;
    }

    public void setBilling_address(String billing_address) {
        this.billing_address = billing_address;
    }

    public String getCustom_billing_address() {
        return custom_billing_address;
    }

    public void setCustom_billing_address(String custom_billing_address) {
        this.custom_billing_address = custom_billing_address;
    }
}
