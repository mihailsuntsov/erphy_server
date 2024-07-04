package com.dokio.message.response;

import com.dokio.message.request.additional.AppointmentCustomer;
import com.dokio.message.response.additional.appointment.AppointmentService;

import java.math.BigDecimal;
import java.util.List;

public class AppointmentsJSON {

    private Long        id;
    private String      creator;
    private String      changer;
    private Long        owner_id;
    private String      owner;
    private Long        company_id;
    private String      company;
    private Long        department_id;
    private String      department;
    private Long        dep_part_id;
    private String      dep_part;
    private Long        jobtitle_id;
    private String      jobtitle;
    private Long        doc_number;
    private String      date_time_created;
    private String      date_time_changed;
    private String      name;
    private String      description;
    private boolean     is_completed;
    private boolean     nds;
    private boolean     nds_included;
    private String      uid;
    private Long        status_id;
    private String      status_name;
    private String      status_color;
    private String      status_description;
    private String      date_start;
    private String      date_end;
    private String      time_start;
    private String      time_end;
    private Long        employeeId;
    private String      employeeName;
    private BigDecimal  sum_price;          // для таблицы документов
    private Long        product_count;      // для таблицы документов
    private List<AppointmentService>  appointmentsProductTable;             // Все товары и услуги из данного Appointment
    private List<AppointmentCustomer>           customersTable;             // Все покупатели из данного Appointment
    private String calendar_date_time_start;    // date and time for angular calendar in ISO 8601 format YYYY-MM-DDTHH24:MI:SS.MSZ
    private String calendar_date_time_end;      // date and time for angular calendar in ISO 8601 format YYYY-MM-DDTHH24:MI:SS.MSZ
    private String date_time_start;             // для таблицы документов
    private String cagent;                      // для печати

    public String getCagent() {
        return cagent;
    }

    public void setCagent(String cagent) {
        this.cagent = cagent;
    }

    public String getDate_time_start() {
        return date_time_start;
    }

    public void setDate_time_start(String date_time_start) {
        this.date_time_start = date_time_start;
    }

    public BigDecimal getSum_price() {
        return sum_price;
    }

    public void setSum_price(BigDecimal sum_price) {
        this.sum_price = sum_price;
    }

    public Long getProduct_count() {
        return product_count;
    }

    public void setProduct_count(Long product_count) {
        this.product_count = product_count;
    }

    public List<AppointmentService> getAppointmentsProductTable() {
        return appointmentsProductTable;
    }

    public void setAppointmentsProductTable(List<AppointmentService> appointmentsProductTable) {
        this.appointmentsProductTable = appointmentsProductTable;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getJobtitle_id() {
        return jobtitle_id;
    }

    public void setJobtitle_id(Long jobtitle_id) {
        this.jobtitle_id = jobtitle_id;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

    public String getCalendar_date_time_start() {
        return calendar_date_time_start;
    }

    public void setCalendar_date_time_start(String calendar_date_time_start) {
        this.calendar_date_time_start = calendar_date_time_start;
    }

    public String getCalendar_date_time_end() {
        return calendar_date_time_end;
    }

    public void setCalendar_date_time_end(String calendar_date_time_end) {
        this.calendar_date_time_end = calendar_date_time_end;
    }

    public String getDep_part() {
        return dep_part;
    }

    public void setDep_part(String dep_part) {
        this.dep_part = dep_part;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AppointmentCustomer> getCustomersTable() {
        return customersTable;
    }

    public void setCustomersTable(List<AppointmentCustomer> customersTable) {
        this.customersTable = customersTable;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(Long owner_id) {
        this.owner_id = owner_id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Long getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Long department_id) {
        this.department_id = department_id;
    }

    public Long getDep_part_id() {
        return dep_part_id;
    }

    public void setDep_part_id(Long dep_part_id) {
        this.dep_part_id = dep_part_id;
    }

    public Long getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(Long doc_number) {
        this.doc_number = doc_number;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIs_completed() {
        return is_completed;
    }

    public void setIs_completed(boolean is_completed) {
        this.is_completed = is_completed;
    }

    public boolean isNds() {
        return nds;
    }

    public void setNds(boolean nds) {
        this.nds = nds;
    }

    public boolean isNds_included() {
        return nds_included;
    }

    public void setNds_included(boolean nds_included) {
        this.nds_included = nds_included;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
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

    public String getDate_start() {
        return date_start;
    }

    public void setDate_start(String date_start) {
        this.date_start = date_start;
    }

    public String getDate_end() {
        return date_end;
    }

    public void setDate_end(String date_end) {
        this.date_end = date_end;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

}
