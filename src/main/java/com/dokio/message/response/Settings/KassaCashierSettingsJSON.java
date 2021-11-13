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
