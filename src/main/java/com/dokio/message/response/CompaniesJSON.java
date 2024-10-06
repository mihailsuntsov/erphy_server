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

package com.dokio.message.response;

import com.dokio.message.request.additional.OnlineSchedulingFieldsTranslation;
import com.dokio.message.request.additional.OnlineSchedulingLanguage;

import java.util.List;
import java.util.Set;

public class CompaniesJSON {

    private Long id;
    private Long master_id;
    private Long creator_id;
    private Long changer_id;
    private String name;
    private String date_time_created;
    private String date_time_changed;
    private String master;
    private String creator;
    private String changer;
    private Integer currency_id;
    private String  opf;
    private Integer opf_id;
    private String  code;
    private String  telephone;
    private String  site;
    private String  email;
    private String  zip_code;
    private Integer country_id;
    private Integer region_id;
    private Integer city_id;
    private String  street;
    private String  home;
    private String  flat;
    private String  additional_address;
    private Long    status_id;
    private String  jr_jur_full_name;
    private String    jr_jur_kpp;
    private String    jr_jur_ogrn;
    private String  jr_zip_code;
    private Integer jr_country_id;
    private Integer jr_region_id;
    private Integer jr_city_id;
    private String  jr_street;
    private String  jr_home;
    private String  jr_flat;
    private String  jr_additional_address;
    private String   jr_inn;
    private String   jr_vat;
    private String   jr_okpo;
    private String jr_fio_family;
    private String jr_fio_name;
    private String jr_fio_otchestvo;
    private String   jr_ip_ogrnip;
    private String jr_ip_svid_num; // string т.к. оно может быть типа "серия 77 №42343232"
    private String jr_ip_reg_date;
    private Boolean nds_payer;
    private String fio_director;
    private String director_position;
    private String fio_glavbuh;
    private Long   director_signature_id;
    private Long   glavbuh_signature_id;
    private Long   stamp_id;
    private Long   card_template_id;
    private String status_name;
    private String status_color;
    private String status_description;
    private String country;
    private String region;
    private String city;
    private String area;
    private String jr_country;
    private String jr_region;
    private String jr_city;
    private String jr_area;
    private String currency_name;
    private String director_signature_filename;
    private String glavbuh_signature_filename;
    private String stamp_filename;
    private String card_template_original_filename;
    private String card_template_filename;
    // Settings
    private Integer st_prefix_barcode_pieced;   // prefix of barcode for pieced product
    private Integer st_prefix_barcode_packed;   // prefix of barcode for packed product
    private String  st_netcost_policy;          // policy of netcost calculation for all company or for each department separately
    private String type;                        // entity or individual
    private String legal_form;
    private Boolean nds_included;               //-- used with nds_payer as default values for Customers orders fields "Tax" and "Tax included"
    private Integer booking_doc_name_variation_id; // variation's id of name of booking document: 1-appointment, 2-reservation
    private Integer time_zone_id;
//    private Integer reg_country_id;           // country of registration
//    private String tax_number;                // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//    private String reg_number;                // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)

    private Integer fld_step;
    private Integer fld_max_amount_services;
    private Integer fld_locale_id;
    private String fld_time_format;
    private String fld_duration;
    private Integer fld_predefined_duration;
    private Long   fld_predefined_duration_unit_id;
    private String fld_tel_prefix;
    private Boolean fld_ask_telephone;
    private Boolean fld_ask_email;
    private String fld_url_slug;
    private String txt_btn_select_time;
    private String txt_btn_select_specialist;
    private String txt_btn_select_services;
    private String txt_summary_header;
    private String txt_summary_date;
    private String txt_summary_time_start;
    private String txt_summary_time_end;
    private String txt_summary_duration;
    private String txt_summary_specialist;
    private String txt_summary_services;
    private String txt_btn_create_order;
    private String txt_btn_send_order;
    private String txt_msg_send_successful;
    private String txt_msg_send_error;
    private String txt_msg_time_not_enable;
    private String txt_fld_your_name;
    private String txt_fld_your_tel;
    private String txt_fld_your_email;
    private String stl_color_buttons;
    private String stl_color_buttons_text;
    private String stl_color_text;
    private String stl_corner_radius;
    private String stl_font_family;
    private Set<OnlineSchedulingLanguage> onlineSchedulingLanguagesList;
    private List<OnlineSchedulingFieldsTranslation> onlineSchedulingFieldsTranslations;

    private Long fld_creator_id;
    private String txt_any_specialist;
    private String txt_hour;
    private String txt_minute;
    private String txt_nearest_app_time;
    private String txt_today;
    private String txt_tomorrow;
    private String txt_morning;
    private String txt_day;
    private String txt_evening;
    private String txt_night;
    private String stl_background_color;
    private String stl_panel_color;
    private Integer stl_panel_max_width;
    private String stl_panel_max_width_unit;
    private String stl_not_selected_elements_color;
    private String stl_selected_elements_color;
    private String stl_job_title_color;
    private String fld_creator;

    public String getFld_creator() {
        return fld_creator;
    }

    public void setFld_creator(String fld_creator) {
        this.fld_creator = fld_creator;
    }

    public Long getFld_creator_id() {
        return fld_creator_id;
    }

    public void setFld_creator_id(Long fld_creator_id) {
        this.fld_creator_id = fld_creator_id;
    }

    public String getTxt_any_specialist() {
        return txt_any_specialist;
    }

    public void setTxt_any_specialist(String txt_any_specialist) {
        this.txt_any_specialist = txt_any_specialist;
    }

    public String getTxt_hour() {
        return txt_hour;
    }

    public void setTxt_hour(String txt_hour) {
        this.txt_hour = txt_hour;
    }

    public String getTxt_minute() {
        return txt_minute;
    }

    public void setTxt_minute(String txt_minute) {
        this.txt_minute = txt_minute;
    }

    public String getTxt_nearest_app_time() {
        return txt_nearest_app_time;
    }

    public void setTxt_nearest_app_time(String txt_nearest_app_time) {
        this.txt_nearest_app_time = txt_nearest_app_time;
    }

    public String getTxt_today() {
        return txt_today;
    }

    public void setTxt_today(String txt_today) {
        this.txt_today = txt_today;
    }

    public String getTxt_tomorrow() {
        return txt_tomorrow;
    }

    public void setTxt_tomorrow(String txt_tomorrow) {
        this.txt_tomorrow = txt_tomorrow;
    }

    public String getTxt_morning() {
        return txt_morning;
    }

    public void setTxt_morning(String txt_morning) {
        this.txt_morning = txt_morning;
    }

    public String getTxt_day() {
        return txt_day;
    }

    public void setTxt_day(String txt_day) {
        this.txt_day = txt_day;
    }

    public String getTxt_evening() {
        return txt_evening;
    }

    public void setTxt_evening(String txt_evening) {
        this.txt_evening = txt_evening;
    }

    public String getTxt_night() {
        return txt_night;
    }

    public void setTxt_night(String txt_night) {
        this.txt_night = txt_night;
    }

    public String getStl_background_color() {
        return stl_background_color;
    }

    public void setStl_background_color(String stl_background_color) {
        this.stl_background_color = stl_background_color;
    }

    public String getStl_panel_color() {
        return stl_panel_color;
    }

    public void setStl_panel_color(String stl_panel_color) {
        this.stl_panel_color = stl_panel_color;
    }

    public Integer getStl_panel_max_width() {
        return stl_panel_max_width;
    }

    public void setStl_panel_max_width(Integer stl_panel_max_width) {
        this.stl_panel_max_width = stl_panel_max_width;
    }

    public String getStl_panel_max_width_unit() {
        return stl_panel_max_width_unit;
    }

    public void setStl_panel_max_width_unit(String stl_panel_max_width_unit) {
        this.stl_panel_max_width_unit = stl_panel_max_width_unit;
    }

    public String getStl_not_selected_elements_color() {
        return stl_not_selected_elements_color;
    }

    public void setStl_not_selected_elements_color(String stl_not_selected_elements_color) {
        this.stl_not_selected_elements_color = stl_not_selected_elements_color;
    }

    public String getStl_selected_elements_color() {
        return stl_selected_elements_color;
    }

    public void setStl_selected_elements_color(String stl_selected_elements_color) {
        this.stl_selected_elements_color = stl_selected_elements_color;
    }

    public String getStl_job_title_color() {
        return stl_job_title_color;
    }

    public void setStl_job_title_color(String stl_job_title_color) {
        this.stl_job_title_color = stl_job_title_color;
    }

    public List<OnlineSchedulingFieldsTranslation> getOnlineSchedulingFieldsTranslations() {
        return onlineSchedulingFieldsTranslations;
    }

    public void setOnlineSchedulingFieldsTranslations(List<OnlineSchedulingFieldsTranslation> onlineSchedulingFieldsTranslations) {
        this.onlineSchedulingFieldsTranslations = onlineSchedulingFieldsTranslations;
    }

    public Set<OnlineSchedulingLanguage> getOnlineSchedulingLanguagesList() {
        return onlineSchedulingLanguagesList;
    }

    public void setOnlineSchedulingLanguagesList(Set<OnlineSchedulingLanguage> onlineSchedulingLanguagesList) {
        this.onlineSchedulingLanguagesList = onlineSchedulingLanguagesList;
    }
    public Integer getFld_step() {
        return fld_step;
    }

    public String getTxt_fld_your_name() {
        return txt_fld_your_name;
    }

    public void setTxt_fld_your_name(String txt_fld_your_name) {
        this.txt_fld_your_name = txt_fld_your_name;
    }

    public String getTxt_fld_your_tel() {
        return txt_fld_your_tel;
    }

    public void setTxt_fld_your_tel(String txt_fld_your_tel) {
        this.txt_fld_your_tel = txt_fld_your_tel;
    }

    public String getTxt_fld_your_email() {
        return txt_fld_your_email;
    }

    public void setTxt_fld_your_email(String txt_fld_your_email) {
        this.txt_fld_your_email = txt_fld_your_email;
    }

    public void setFld_step(Integer fld_step) {
        this.fld_step = fld_step;
    }

    public Integer getFld_max_amount_services() {
        return fld_max_amount_services;
    }

    public void setFld_max_amount_services(Integer fld_max_amount_services) {
        this.fld_max_amount_services = fld_max_amount_services;
    }

    public Integer getFld_locale_id() {
        return fld_locale_id;
    }

    public void setFld_locale_id(Integer fld_locale_id) {
        this.fld_locale_id = fld_locale_id;
    }

    public String getFld_time_format() {
        return fld_time_format;
    }

    public void setFld_time_format(String fld_time_format) {
        this.fld_time_format = fld_time_format;
    }

    public String getFld_duration() {
        return fld_duration;
    }

    public void setFld_duration(String fld_duration) {
        this.fld_duration = fld_duration;
    }

    public Integer getFld_predefined_duration() {
        return fld_predefined_duration;
    }

    public void setFld_predefined_duration(Integer fld_predefined_duration) {
        this.fld_predefined_duration = fld_predefined_duration;
    }

    public Long getFld_predefined_duration_unit_id() {
        return fld_predefined_duration_unit_id;
    }

    public void setFld_predefined_duration_unit_id(Long fld_predefined_duration_unit_id) {
        this.fld_predefined_duration_unit_id = fld_predefined_duration_unit_id;
    }

    public String getFld_tel_prefix() {
        return fld_tel_prefix;
    }

    public void setFld_tel_prefix(String fld_tel_prefix) {
        this.fld_tel_prefix = fld_tel_prefix;
    }

    public Boolean getFld_ask_telephone() {
        return fld_ask_telephone;
    }

    public void setFld_ask_telephone(Boolean fld_ask_telephone) {
        this.fld_ask_telephone = fld_ask_telephone;
    }

    public Boolean getFld_ask_email() {
        return fld_ask_email;
    }

    public void setFld_ask_email(Boolean fld_ask_email) {
        this.fld_ask_email = fld_ask_email;
    }

    public String getFld_url_slug() {
        return fld_url_slug;
    }

    public void setFld_url_slug(String fld_url_slug) {
        this.fld_url_slug = fld_url_slug;
    }

    public String getTxt_btn_select_time() {
        return txt_btn_select_time;
    }

    public void setTxt_btn_select_time(String txt_btn_select_time) {
        this.txt_btn_select_time = txt_btn_select_time;
    }

    public String getTxt_btn_select_specialist() {
        return txt_btn_select_specialist;
    }

    public void setTxt_btn_select_specialist(String txt_btn_select_specialist) {
        this.txt_btn_select_specialist = txt_btn_select_specialist;
    }

    public String getTxt_btn_select_services() {
        return txt_btn_select_services;
    }

    public void setTxt_btn_select_services(String txt_btn_select_services) {
        this.txt_btn_select_services = txt_btn_select_services;
    }

    public String getTxt_summary_header() {
        return txt_summary_header;
    }

    public void setTxt_summary_header(String txt_summary_header) {
        this.txt_summary_header = txt_summary_header;
    }

    public String getTxt_summary_date() {
        return txt_summary_date;
    }

    public void setTxt_summary_date(String txt_summary_date) {
        this.txt_summary_date = txt_summary_date;
    }

    public String getTxt_summary_time_start() {
        return txt_summary_time_start;
    }

    public void setTxt_summary_time_start(String txt_summary_time_start) {
        this.txt_summary_time_start = txt_summary_time_start;
    }

    public String getTxt_summary_time_end() {
        return txt_summary_time_end;
    }

    public void setTxt_summary_time_end(String txt_summary_time_end) {
        this.txt_summary_time_end = txt_summary_time_end;
    }

    public String getTxt_summary_duration() {
        return txt_summary_duration;
    }

    public void setTxt_summary_duration(String txt_summary_duration) {
        this.txt_summary_duration = txt_summary_duration;
    }

    public String getTxt_summary_specialist() {
        return txt_summary_specialist;
    }

    public void setTxt_summary_specialist(String txt_summary_specialist) {
        this.txt_summary_specialist = txt_summary_specialist;
    }

    public String getTxt_summary_services() {
        return txt_summary_services;
    }

    public void setTxt_summary_services(String txt_summary_services) {
        this.txt_summary_services = txt_summary_services;
    }

    public String getTxt_btn_create_order() {
        return txt_btn_create_order;
    }

    public void setTxt_btn_create_order(String txt_btn_create_order) {
        this.txt_btn_create_order = txt_btn_create_order;
    }

    public String getTxt_btn_send_order() {
        return txt_btn_send_order;
    }

    public void setTxt_btn_send_order(String txt_btn_send_order) {
        this.txt_btn_send_order = txt_btn_send_order;
    }

    public String getTxt_msg_send_successful() {
        return txt_msg_send_successful;
    }

    public void setTxt_msg_send_successful(String txt_msg_send_successful) {
        this.txt_msg_send_successful = txt_msg_send_successful;
    }

    public String getTxt_msg_send_error() {
        return txt_msg_send_error;
    }

    public void setTxt_msg_send_error(String txt_msg_send_error) {
        this.txt_msg_send_error = txt_msg_send_error;
    }

    public String getTxt_msg_time_not_enable() {
        return txt_msg_time_not_enable;
    }

    public void setTxt_msg_time_not_enable(String txt_msg_time_not_enable) {
        this.txt_msg_time_not_enable = txt_msg_time_not_enable;
    }

    public String getStl_color_buttons() {
        return stl_color_buttons;
    }

    public void setStl_color_buttons(String stl_color_buttons) {
        this.stl_color_buttons = stl_color_buttons;
    }

    public String getStl_color_buttons_text() {
        return stl_color_buttons_text;
    }

    public void setStl_color_buttons_text(String stl_color_buttons_text) {
        this.stl_color_buttons_text = stl_color_buttons_text;
    }

    public String getStl_color_text() {
        return stl_color_text;
    }

    public void setStl_color_text(String stl_color_text) {
        this.stl_color_text = stl_color_text;
    }

    public String getStl_corner_radius() {
        return stl_corner_radius;
    }

    public void setStl_corner_radius(String stl_corner_radius) {
        this.stl_corner_radius = stl_corner_radius;
    }

    public String getStl_font_family() {
        return stl_font_family;
    }

    public void setStl_font_family(String stl_font_family) {
        this.stl_font_family = stl_font_family;
    }

    public Integer getTime_zone_id() {
        return time_zone_id;
    }

    public void setTime_zone_id(Integer time_zone_id) {
        this.time_zone_id = time_zone_id;
    }

    private String store_default_lang_code;      // internet-store basic language, e.g. EN, RU, UA, ...

    public Integer getBooking_doc_name_variation_id() {
        return booking_doc_name_variation_id;
    }

    public void setBooking_doc_name_variation_id(Integer booking_doc_name_variation_id) {
        this.booking_doc_name_variation_id = booking_doc_name_variation_id;
    }

    public String getStore_default_lang_code() {
        return store_default_lang_code;
    }

    public void setStore_default_lang_code(String store_default_lang_code) {
        this.store_default_lang_code = store_default_lang_code;
    }

    public String getJr_vat() {
        return jr_vat;
    }

    public void setJr_vat(String jr_vat) {
        this.jr_vat = jr_vat;
    }

    public Boolean getNds_included() {
        return nds_included;
    }

    public void setNds_included(Boolean nds_included) {
        this.nds_included = nds_included;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLegal_form() {
        return legal_form;
    }

    public void setLegal_form(String legal_form) {
        this.legal_form = legal_form;
    }
    public Integer getSt_prefix_barcode_pieced() {
        return st_prefix_barcode_pieced;
    }

    public void setSt_prefix_barcode_pieced(Integer st_prefix_barcode_pieced) {
        this.st_prefix_barcode_pieced = st_prefix_barcode_pieced;
    }

    public Integer getSt_prefix_barcode_packed() {
        return st_prefix_barcode_packed;
    }

    public void setSt_prefix_barcode_packed(Integer st_prefix_barcode_packed) {
        this.st_prefix_barcode_packed = st_prefix_barcode_packed;
    }

    public String getSt_netcost_policy() {
        return st_netcost_policy;
    }

    public void setSt_netcost_policy(String st_netcost_policy) {
        this.st_netcost_policy = st_netcost_policy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaster_id() {
        return master_id;
    }

    public void setMaster_id(Long master_id) {
        this.master_id = master_id;
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }

    public Long getChanger_id() {
        return changer_id;
    }

    public void setChanger_id(Long changer_id) {
        this.changer_id = changer_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_time_created() {
        return date_time_created;
    }

    public void setDate_time_created(String date_time_created) {
        this.date_time_created = date_time_created;
    }

    public String getDate_time_changed() {
        return date_time_changed;
    }

    public void setDate_time_changed(String date_time_changed) {
        this.date_time_changed = date_time_changed;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public Integer getCurrency_id() {
        return currency_id;
    }

    public void setCurrency_id(Integer currency_id) {
        this.currency_id = currency_id;
    }

    public String getOpf() {
        return opf;
    }

    public void setOpf(String opf) {
        this.opf = opf;
    }

    public Integer getOpf_id() {
        return opf_id;
    }

    public void setOpf_id(Integer opf_id) {
        this.opf_id = opf_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public Integer getCountry_id() {
        return country_id;
    }

    public void setCountry_id(Integer country_id) {
        this.country_id = country_id;
    }

    public Integer getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Integer region_id) {
        this.region_id = region_id;
    }

    public Integer getCity_id() {
        return city_id;
    }

    public void setCity_id(Integer city_id) {
        this.city_id = city_id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getAdditional_address() {
        return additional_address;
    }

    public void setAdditional_address(String additional_address) {
        this.additional_address = additional_address;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
    }

    public String getJr_jur_full_name() {
        return jr_jur_full_name;
    }

    public void setJr_jur_full_name(String jr_jur_full_name) {
        this.jr_jur_full_name = jr_jur_full_name;
    }

    public String getJr_jur_kpp() {
        return jr_jur_kpp;
    }

    public void setJr_jur_kpp(String jr_jur_kpp) {
        this.jr_jur_kpp = jr_jur_kpp;
    }

    public String getJr_jur_ogrn() {
        return jr_jur_ogrn;
    }

    public void setJr_jur_ogrn(String jr_jur_ogrn) {
        this.jr_jur_ogrn = jr_jur_ogrn;
    }

    public String getJr_zip_code() {
        return jr_zip_code;
    }

    public void setJr_zip_code(String jr_zip_code) {
        this.jr_zip_code = jr_zip_code;
    }

    public Integer getJr_country_id() {
        return jr_country_id;
    }

    public void setJr_country_id(Integer jr_country_id) {
        this.jr_country_id = jr_country_id;
    }

    public Integer getJr_region_id() {
        return jr_region_id;
    }

    public void setJr_region_id(Integer jr_region_id) {
        this.jr_region_id = jr_region_id;
    }

    public Integer getJr_city_id() {
        return jr_city_id;
    }

    public void setJr_city_id(Integer jr_city_id) {
        this.jr_city_id = jr_city_id;
    }

    public String getJr_street() {
        return jr_street;
    }

    public void setJr_street(String jr_street) {
        this.jr_street = jr_street;
    }

    public String getJr_home() {
        return jr_home;
    }

    public void setJr_home(String jr_home) {
        this.jr_home = jr_home;
    }

    public String getJr_flat() {
        return jr_flat;
    }

    public void setJr_flat(String jr_flat) {
        this.jr_flat = jr_flat;
    }

    public String getJr_additional_address() {
        return jr_additional_address;
    }

    public void setJr_additional_address(String jr_additional_address) {
        this.jr_additional_address = jr_additional_address;
    }

    public String getJr_inn() {
        return jr_inn;
    }

    public void setJr_inn(String jr_inn) {
        this.jr_inn = jr_inn;
    }

    public String getJr_okpo() {
        return jr_okpo;
    }

    public void setJr_okpo(String jr_okpo) {
        this.jr_okpo = jr_okpo;
    }

    public String getJr_fio_family() {
        return jr_fio_family;
    }

    public void setJr_fio_family(String jr_fio_family) {
        this.jr_fio_family = jr_fio_family;
    }

    public String getJr_fio_name() {
        return jr_fio_name;
    }

    public void setJr_fio_name(String jr_fio_name) {
        this.jr_fio_name = jr_fio_name;
    }

    public String getJr_fio_otchestvo() {
        return jr_fio_otchestvo;
    }

    public void setJr_fio_otchestvo(String jr_fio_otchestvo) {
        this.jr_fio_otchestvo = jr_fio_otchestvo;
    }

    public String getJr_ip_ogrnip() {
        return jr_ip_ogrnip;
    }

    public void setJr_ip_ogrnip(String jr_ip_ogrnip) {
        this.jr_ip_ogrnip = jr_ip_ogrnip;
    }

    public String getJr_ip_svid_num() {
        return jr_ip_svid_num;
    }

    public void setJr_ip_svid_num(String jr_ip_svid_num) {
        this.jr_ip_svid_num = jr_ip_svid_num;
    }

    public String getJr_ip_reg_date() {
        return jr_ip_reg_date;
    }

    public void setJr_ip_reg_date(String jr_ip_reg_date) {
        this.jr_ip_reg_date = jr_ip_reg_date;
    }

    public Boolean getNds_payer() {
        return nds_payer;
    }

    public void setNds_payer(Boolean nds_payer) {
        this.nds_payer = nds_payer;
    }

    public String getFio_director() {
        return fio_director;
    }

    public void setFio_director(String fio_director) {
        this.fio_director = fio_director;
    }

    public String getDirector_position() {
        return director_position;
    }

    public void setDirector_position(String director_position) {
        this.director_position = director_position;
    }

    public String getFio_glavbuh() {
        return fio_glavbuh;
    }

    public void setFio_glavbuh(String fio_glavbuh) {
        this.fio_glavbuh = fio_glavbuh;
    }

    public Long getDirector_signature_id() {
        return director_signature_id;
    }

    public void setDirector_signature_id(Long director_signature_id) {
        this.director_signature_id = director_signature_id;
    }

    public Long getGlavbuh_signature_id() {
        return glavbuh_signature_id;
    }

    public void setGlavbuh_signature_id(Long glavbuh_signature_id) {
        this.glavbuh_signature_id = glavbuh_signature_id;
    }

    public Long getStamp_id() {
        return stamp_id;
    }

    public void setStamp_id(Long stamp_id) {
        this.stamp_id = stamp_id;
    }

    public Long getCard_template_id() {
        return card_template_id;
    }

    public void setCard_template_id(Long card_template_id) {
        this.card_template_id = card_template_id;
    }

    public String getStatus_name() {
        return status_name;
    }

    public void setStatus_name(String status_name) {
        this.status_name = status_name;
    }

    public String getStatus_color() {
        return status_color;
    }

    public void setStatus_color(String status_color) {
        this.status_color = status_color;
    }

    public String getStatus_description() {
        return status_description;
    }

    public void setStatus_description(String status_description) {
        this.status_description = status_description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getJr_country() {
        return jr_country;
    }

    public void setJr_country(String jr_country) {
        this.jr_country = jr_country;
    }

    public String getJr_region() {
        return jr_region;
    }

    public void setJr_region(String jr_region) {
        this.jr_region = jr_region;
    }

    public String getJr_city() {
        return jr_city;
    }

    public void setJr_city(String jr_city) {
        this.jr_city = jr_city;
    }

    public String getJr_area() {
        return jr_area;
    }

    public void setJr_area(String jr_area) {
        this.jr_area = jr_area;
    }

    public String getCurrency_name() {
        return currency_name;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public String getDirector_signature_filename() {
        return director_signature_filename;
    }

    public void setDirector_signature_filename(String director_signature_filename) {
        this.director_signature_filename = director_signature_filename;
    }

    public String getGlavbuh_signature_filename() {
        return glavbuh_signature_filename;
    }

    public void setGlavbuh_signature_filename(String glavbuh_signature_filename) {
        this.glavbuh_signature_filename = glavbuh_signature_filename;
    }

    public String getStamp_filename() {
        return stamp_filename;
    }

    public void setStamp_filename(String stamp_filename) {
        this.stamp_filename = stamp_filename;
    }

    public String getCard_template_original_filename() {
        return card_template_original_filename;
    }

    public void setCard_template_original_filename(String card_template_original_filename) {
        this.card_template_original_filename = card_template_original_filename;
    }

    public String getCard_template_filename() {
        return card_template_filename;
    }

    public void setCard_template_filename(String card_template_filename) {
        this.card_template_filename = card_template_filename;
    }
}