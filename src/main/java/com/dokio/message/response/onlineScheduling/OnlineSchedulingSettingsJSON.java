package com.dokio.message.response.onlineScheduling;

import com.dokio.message.request.additional.CompanyContactsForm;
import com.dokio.message.request.additional.OnlineSchedulingFieldsTranslation;
import com.dokio.message.request.additional.OnlineSchedulingLanguage;

import java.util.List;
import java.util.Set;

public class OnlineSchedulingSettingsJSON {

    private String company_name;
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
    private String  date_format;   //online scheduling settings locale

    private Set<OnlineSchedulingLanguage> onlineSchedulingLanguagesList;
    private List<OnlineSchedulingFieldsTranslation> onlineSchedulingFieldsTranslations;
    private List<CompanyContactsForm> onlineSchedulingContactsList;

    public List<CompanyContactsForm> getOnlineSchedulingContactsList() {
        return onlineSchedulingContactsList;
    }

    public void setOnlineSchedulingContactsList(List<CompanyContactsForm> onlineSchedulingContactsList) {
        this.onlineSchedulingContactsList = onlineSchedulingContactsList;
    }

    public String getDate_format() {
        return date_format;
    }

    public void setDate_format(String date_format) {
        this.date_format = date_format;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
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

    public String getFld_creator() {
        return fld_creator;
    }

    public void setFld_creator(String fld_creator) {
        this.fld_creator = fld_creator;
    }

    public Integer getFld_step() {
        return fld_step;
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

    public Set<OnlineSchedulingLanguage> getOnlineSchedulingLanguagesList() {
        return onlineSchedulingLanguagesList;
    }

    public void setOnlineSchedulingLanguagesList(Set<OnlineSchedulingLanguage> onlineSchedulingLanguagesList) {
        this.onlineSchedulingLanguagesList = onlineSchedulingLanguagesList;
    }

    public List<OnlineSchedulingFieldsTranslation> getOnlineSchedulingFieldsTranslations() {
        return onlineSchedulingFieldsTranslations;
    }

    public void setOnlineSchedulingFieldsTranslations(List<OnlineSchedulingFieldsTranslation> onlineSchedulingFieldsTranslations) {
        this.onlineSchedulingFieldsTranslations = onlineSchedulingFieldsTranslations;
    }
}
