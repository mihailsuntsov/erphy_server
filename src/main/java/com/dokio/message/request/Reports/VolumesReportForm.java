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
package com.dokio.message.request.Reports;

import java.util.List;
import java.util.Set;

public class VolumesReportForm {

    private Long companyId;             // предприятие, по которому идет запрос данных
    private String periodType;          // отрезок времени для анализа (день, месяц, год, или выбранный период)
    private String unit;                // какая единица одного бара на графике (час, неделя, месяц, день, год)
    private String dateFrom;            // с даты
    private String dateTo;              // по дату
    private String type;                // тип отчета - продажи или закупки (buy, sell)
    private String reportOn;            // по категориям или по товарам/услугам (categories, products)
    private List<Long> reportOnIds;      // id категорий/товаров/услуг (того, что выбрано в reportOn)
    private Set<Long> departmentsIds;   // id всех отобранных отделений
    private Set<Long> employeeIds;      // id всех отобранных сотрудников
    private Boolean all;                // отчет по всем (категориям или товарам-услугам, в зависимости от того, что выбрано)
    private Boolean includeChilds;      // включая все подкатегории выбранных категорий
    private Boolean withSeparation;     // с разбивкой. Например, на каждый временной отрезок будет представлено несколько значений выбранных категорий по отдельности (иначе эти значения суммируются)

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReportOn() {
        return reportOn;
    }

    public void setReportOn(String reportOn) {
        this.reportOn = reportOn;
    }

    public List<Long> getReportOnIds() {
        return reportOnIds;
    }

    public void setReportOnIds(List<Long> reportOnIds) {
        this.reportOnIds = reportOnIds;
    }

    public Set<Long> getDepartmentsIds() {
        return departmentsIds;
    }

    public void setDepartmentsIds(Set<Long> departmentsIds) {
        this.departmentsIds = departmentsIds;
    }

    public Set<Long> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(Set<Long> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public Boolean getAll() {
        return all;
    }

    public void setAll(Boolean all) {
        this.all = all;
    }

    public Boolean getIncludeChilds() {
        return includeChilds;
    }

    public void setIncludeChilds(Boolean includeChilds) {
        this.includeChilds = includeChilds;
    }

    public Boolean getWithSeparation() {
        return withSeparation;
    }

    public void setWithSeparation(Boolean withSeparation) {
        this.withSeparation = withSeparation;
    }
}
