package com.dokio.message.request;

import com.dokio.message.request.additional.AppointmentCustomer;

import java.util.List;
import java.util.Set;

public class AppointmentsForm {

    private Long id;
    private Long company_id;
    private Long department_part_id;
    private Long status_id;
    private String date_start;
    private String date_end;
    private String time_start;
    private String time_end;
    private String doc_number;
    private Long employeeId;
    private String name;
    private boolean nds;
    private boolean nds_included;
    private String description;
    private String uid;
    private Long linked_doc_id;//id связанного документа
    private String  linked_doc_name;//имя (таблицы) связанного документа
    private String  parent_uid;// uid исходящего (родительского) документа
    private String  child_uid; // uid дочернего документа. Дочерний - не всегда тот, которого создают из текущего документа. Например, при создании из Отгрузки Счёта покупателю - Отгрузка будет дочерней для него.
    private Boolean is_completed;// проведён
    private List<AppointmentProductsTableForm> appointmentsProductTable; //Все товары и услуги из данного заказа
    private List<AppointmentCustomer> customersTable;                   // Все покупатели из данного Заказа
    private Long cagent_id;    // для создания документов из Записи (Отгрузка, входящие платежи)

    public Long getCagent_id() {
        return cagent_id;
    }

    public void setCagent_id(Long cagent_id) {
        this.cagent_id = cagent_id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public List<AppointmentProductsTableForm> getAppointmentsProductTable() {
        return appointmentsProductTable;
    }

    public void setAppointmentsProductTable(List<AppointmentProductsTableForm> appointmentsProductTable) {
        this.appointmentsProductTable = appointmentsProductTable;
    }

    public Long getId() {
        return id;
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

    public Long getDepartment_part_id() {
        return department_part_id;
    }

    public void setDepartment_part_id(Long department_part_id) {
        this.department_part_id = department_part_id;
    }

    public Long getStatus_id() {
        return status_id;
    }

    public void setStatus_id(Long status_id) {
        this.status_id = status_id;
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

    public String getDoc_number() {
        return doc_number;
    }

    public void setDoc_number(String doc_number) {
        this.doc_number = doc_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getLinked_doc_id() {
        return linked_doc_id;
    }

    public void setLinked_doc_id(Long linked_doc_id) {
        this.linked_doc_id = linked_doc_id;
    }

    public String getLinked_doc_name() {
        return linked_doc_name;
    }

    public void setLinked_doc_name(String linked_doc_name) {
        this.linked_doc_name = linked_doc_name;
    }

    public String getParent_uid() {
        return parent_uid;
    }

    public void setParent_uid(String parent_uid) {
        this.parent_uid = parent_uid;
    }

    public String getChild_uid() {
        return child_uid;
    }

    public void setChild_uid(String child_uid) {
        this.child_uid = child_uid;
    }

    public Boolean getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(Boolean is_completed) {
        this.is_completed = is_completed;
    }


    public List<AppointmentCustomer> getCustomersTable() {
        return customersTable;
    }

    public void setCustomersTable(List<AppointmentCustomer> customersTable) {
        this.customersTable = customersTable;
    }

    @Override
    public String toString() {
        return "AppointmentsForm{" +
                "id=" + id +
                ", company_id=" + company_id +
                ", department_part_id=" + department_part_id +
                ", status_id=" + status_id +
                ", date_start='" + date_start + '\'' +
                ", date_end='" + date_end + '\'' +
                ", time_start='" + time_start + '\'' +
                ", time_end='" + time_end + '\'' +
                ", doc_number='" + doc_number + '\'' +
                ", employeeId=" + employeeId +
                ", name='" + name + '\'' +
                ", nds=" + nds +
                ", nds_included=" + nds_included +
                ", description='" + description + '\'' +
                ", uid='" + uid + '\'' +
                ", linked_doc_id=" + linked_doc_id +
                ", linked_doc_name='" + linked_doc_name + '\'' +
                ", parent_uid='" + parent_uid + '\'' +
                ", child_uid='" + child_uid + '\'' +
                ", is_completed=" + is_completed +
                ", appointmentsProductTable=" + appointmentsProductTable +
                ", customersTable=" + customersTable +
                ", cagent_id=" + cagent_id +
                '}';
    }
}
