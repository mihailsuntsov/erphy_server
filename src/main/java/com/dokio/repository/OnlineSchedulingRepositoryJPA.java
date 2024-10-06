package com.dokio.repository;

import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.message.request.onlineScheduling.OnlineSchedulingForm;
import com.dokio.message.response.additional.DepartmentWithPartsJSON;
import com.dokio.message.response.additional.appointment.AppointmentEmployee;
import com.dokio.message.response.additional.appointment.DepartmentPartWithServicesIds;
import com.dokio.message.response.additional.calendar.BreakJSON;
import com.dokio.message.response.onlineScheduling.*;
import com.dokio.message.response.store.woo.v3.ProductCategoriesJSON;
import com.dokio.message.response.store.woo.v3.ProductCategoryJSON;
import com.dokio.message.response.store.woo.v3.products.ImageJSON;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

@Repository("OnlineSchedulingRepositoryJPA")
public class OnlineSchedulingRepositoryJPA {


    @Value("${apiserver.host}")
    private String apiserver;

    Logger logger = Logger.getLogger(OnlineSchedulingRepositoryJPA.class);

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    CompanyRepositoryJPA companyRepository;
    @Autowired
    DepartmentRepositoryJPA departmentRepository;
    @Autowired
    CalendarRepositoryJPA calendarRepository;
    @Autowired
    CommonUtilites commonUtilites;


    public CompanyParamsJSON getCompanyParamsBySlug(String companyUrlSlug){
        String stringQuery="select " +
                " os.master_id as master_id, " +
                " os.company_id as company_id, " +
                " cmp.time_zone_id as time_zone_id, " +
                " tz.canonical_id as time_zone_name," +
                " sslc.date_format as date_format, " +
                " os.fld_time_format as time_format " +
                " from scdl_os_company_settings os " +
                " inner join companies cmp on cmp.id=os.company_id" +
                " inner join sprav_sys_locales sslc on os.fld_locale_id = sslc.id" +
                " left outer join sprav_sys_timezones tz on cmp.time_zone_id=tz.id" +
                " where " +
                " os.fld_url_slug = :fld_url_slug";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("fld_url_slug", companyUrlSlug);
            Object[] obj = (Object[]) query.getSingleResult();
            CompanyParamsJSON params = new CompanyParamsJSON();
            params.setMasterId(Long.parseLong(      obj[0].toString()));
            params.setCompanyId(Long.parseLong(     obj[1].toString()));
            params.setTime_zone_id((Integer)        obj[2]);
            params.setTime_zone_name((String)       obj[3]);
            params.setDate_format((String)          obj[4]);
            params.setTime_format((String)          obj[5]);

            return params;
        } catch (NoResultException nre) {
            return new CompanyParamsJSON();
        } catch (Exception e) {
            logger.error("Error of getCompanyParams. stringQuery="+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public OnlineSchedulingSettingsJSON getOnlineSchedulingSettings(String companyUrlSlug){

        CompanyParamsJSON params = getCompanyParamsBySlug(companyUrlSlug);

        String stringQuery="select " +
                "           coalesce(os.fld_step, 15) as fld_step, " +                          // step of time slots (10,15,30,60 minutes)
                "           coalesce(os.fld_max_amount_services, 10) as fld_max_amount_services, " +           // max amount of services that customer can select at once
                "           coalesce(os.fld_locale_id, 3) as fld_locale_id, " +                      // date format: 3 = "English (United States)"
                "           coalesce(os.fld_time_format, '12') as fld_time_format, " +                 // 12 or 24
                "           coalesce(os.fld_duration, 'summary') as fld_duration, " +               // duration of vizit: "summary" - as sum of all services duration. "longest" - duration of a longest service. "defined" - predefined duration
                "           coalesce(os.fld_predefined_duration, 1) as fld_predefined_duration, " +            // predefined duration of the appointment (if duration = "defined")
                "           coalesce(os.fld_predefined_duration_unit_id, (select id from sprav_sys_edizm where company_id="+params.getCompanyId()+" and type_id=6 order by id desc offset 1 limit 1)) as fld_predefined_duration_unit_id, " +   // Should be "hour". The unit of measure of predefined duration of the appointment's time (if duration = "defined")
                "           coalesce(os.fld_tel_prefix, '+1') as fld_tel_prefix, " +
                "           coalesce(os.fld_ask_telephone, true) as fld_ask_telephone, " +
                "           coalesce(os.fld_ask_email, true) as fld_ask_email, " +
                "           coalesce(os.fld_url_slug, '') as fld_url_slug, " +
                "           coalesce(os.txt_btn_select_time, 'Select time') as txt_btn_select_time, " +
                "           coalesce(os.txt_btn_select_specialist, 'Select specialist') as txt_btn_select_specialist, " +
                "           coalesce(os.txt_btn_select_services, 'Select services') as txt_btn_select_services, " +
                "           coalesce(os.txt_summary_header, 'Summary') as txt_summary_header, " +
                "           coalesce(os.txt_summary_date, 'Date') as txt_summary_date, " +
                "           coalesce(os.txt_summary_time_start, 'Start time') as txt_summary_time_start, " +
                "           coalesce(os.txt_summary_time_end, 'End time') as txt_summary_time_end, " +
                "           coalesce(os.txt_summary_duration, 'Duration') as txt_summary_duration, " +
                "           coalesce(os.txt_summary_specialist, 'Specialist') as txt_summary_specialist, " +
                "           coalesce(os.txt_summary_services, 'Selected services') as txt_summary_services, " +
                "           coalesce(os.txt_btn_create_order, 'Create order') as txt_btn_create_order, " +
                "           coalesce(os.txt_btn_send_order, 'Send order') as txt_btn_send_order, " +
                "           coalesce(os.txt_msg_send_successful, 'Your appointment reservation was successfully sent') as txt_msg_send_successful, " +
                "           coalesce(os.txt_msg_send_error, 'Error of appointment reservation sending') as txt_msg_send_error, " +
                "           coalesce(os.txt_msg_time_not_enable, 'Sorry, but the selected time slot is no longer available. Please choose another time slot.') as txt_msg_time_not_enable, " +
                "           coalesce(os.stl_color_buttons, '#223559') as stl_color_buttons, " +
                "           coalesce(os.stl_color_buttons_text, '#ffffff') as stl_color_buttons_text, " +
                "           coalesce(os.stl_color_text, '#333333') as stl_color_text, " +
                "           coalesce(os.stl_corner_radius, '5') as stl_corner_radius, " +
                "           coalesce(os.stl_font_family, 'Roboto, sans-serif') as stl_font_family, " +
                "           coalesce(os.txt_fld_your_name, 'Your name') as txt_fld_your_name, " +
                "           coalesce(os.txt_fld_your_tel, 'Telephone') as txt_fld_your_tel, " +
                "           coalesce(os.txt_fld_your_email, 'Email') as txt_fld_your_email," +

                "           coalesce(os.txt_any_specialist,'Any specialist') as txt_any_specialist, " +
                "           coalesce(os.txt_hour,'hour') as txt_hour, " +
                "           coalesce(os.txt_minute,'minutes') as txt_minute, " +
                "           coalesce(os.txt_nearest_app_time,'The nearest appointment time') as txt_nearest_app_time, " +
                "           coalesce(os.txt_today,'Today') as txt_today, " +
                "           coalesce(os.txt_tomorrow,'Tomorrow') as txt_tomorrow, " +
                "           coalesce(os.txt_morning,'Morning') as txt_morning, " +
                "           coalesce(os.txt_day,'Day') as txt_day, " +
                "           coalesce(os.txt_evening,'Evening') as txt_evening, " +
                "           coalesce(os.txt_night,'Night') as txt_night, " +
                "           coalesce(os.stl_background_color,'#f5f5f5') as stl_background_color, " +
                "           coalesce(os.stl_panel_color,'#ffffff') as stl_panel_color, " +
                "           coalesce(os.stl_panel_max_width,'600') as stl_panel_max_width, " +
                "           coalesce(os.stl_panel_max_width_unit,'px') as stl_panel_max_width_unit, " +
                "           coalesce(os.stl_not_selected_elements_color,'#c2c2c2') as stl_not_selected_elements_color, " +
                "           coalesce(os.stl_selected_elements_color,'#223559') as stl_selected_elements_color, " +
                "           coalesce(os.stl_job_title_color,'#545454') as stl_job_title_color," +
                "           cmp.name as company_name " +
                "           from scdl_os_company_settings os " +
                "           inner join companies cmp on cmp.id = os.company_id " +
//                "           inner join sprav_sys_locales sslc on os.fld_locale_id = sslc.id" +
                "           where os.master_id="+params.getMasterId()+" and os.company_id="+params.getCompanyId();
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            Object[] obj = (Object[]) query.getSingleResult();
            OnlineSchedulingSettingsJSON result = new OnlineSchedulingSettingsJSON();
            result.setFld_step((Integer)                        obj[0]);
            result.setFld_max_amount_services((Integer)         obj[1]);
            result.setFld_locale_id((Integer)                   obj[2]);
            result.setFld_time_format((String)                  obj[3]);
            result.setFld_duration((String)                     obj[4]);
            result.setFld_predefined_duration((Integer)         obj[5]);
            result.setFld_predefined_duration_unit_id(          obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
            result.setFld_tel_prefix((String)                   obj[7]);
            result.setFld_ask_telephone((Boolean)               obj[8]);
            result.setFld_ask_email((Boolean)                   obj[9]);
            result.setFld_url_slug((String)                     obj[10]);
            result.setTxt_btn_select_time((String)              obj[11]);
            result.setTxt_btn_select_specialist((String)        obj[12]);
            result.setTxt_btn_select_services((String)          obj[13]);
            result.setTxt_summary_header((String)               obj[14]);
            result.setTxt_summary_date((String)                 obj[15]);
            result.setTxt_summary_time_start((String)           obj[16]);
            result.setTxt_summary_time_end((String)             obj[17]);
            result.setTxt_summary_duration((String)             obj[18]);
            result.setTxt_summary_specialist((String)           obj[19]);
            result.setTxt_summary_services((String)             obj[20]);
            result.setTxt_btn_create_order((String)             obj[21]);
            result.setTxt_btn_send_order((String)               obj[22]);
            result.setTxt_msg_send_successful((String)          obj[23]);
            result.setTxt_msg_send_error((String)               obj[24]);
            result.setTxt_msg_time_not_enable((String)          obj[25]);
            result.setStl_color_buttons((String)                obj[26]);
            result.setStl_color_buttons_text((String)           obj[27]);
            result.setStl_color_text((String)                   obj[28]);
            result.setStl_corner_radius((String)                obj[29]);
            result.setStl_font_family((String)                  obj[30]);
            result.setTxt_fld_your_name((String)                obj[31]);
            result.setTxt_fld_your_tel((String)                 obj[32]);
            result.setTxt_fld_your_email((String)               obj[33]);
            result.setTxt_any_specialist((String)               obj[34]);
            result.setTxt_hour((String)                         obj[35]);
            result.setTxt_minute((String)                       obj[36]);
            result.setTxt_nearest_app_time((String)             obj[37]);
            result.setTxt_today((String)                        obj[38]);
            result.setTxt_tomorrow((String)                     obj[39]);
            result.setTxt_morning((String)                      obj[40]);
            result.setTxt_day((String)                          obj[41]);
            result.setTxt_evening((String)                      obj[42]);
            result.setTxt_night((String)                        obj[43]);
            result.setStl_background_color((String)             obj[44]);
            result.setStl_panel_color((String)                  obj[45]);
            result.setStl_panel_max_width((Integer)             obj[46]);
            result.setStl_panel_max_width_unit((String)         obj[47]);
            result.setStl_not_selected_elements_color((String)  obj[48]);
            result.setStl_selected_elements_color((String)      obj[49]);
            result.setStl_job_title_color((String)              obj[50]);
            result.setCompany_name((String)                     obj[51]);
            result.setDate_format(              params.getDate_format());

            result.setOnlineSchedulingLanguagesList(companyRepository.getOnlineSchedulingLanguagesList(params.getCompanyId(),params.getMasterId()));
            result.setOnlineSchedulingFieldsTranslations(companyRepository.getOnlineSchedulingFieldsTranslationsList(params.getCompanyId(),params.getMasterId()));
            return result;
        } catch (NoResultException nre) {
            return new OnlineSchedulingSettingsJSON();
        } catch (Exception e) {
            logger.error("Error of getOnlineSchedulingSettings. stringQuery="+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public List<DepartmentWithPartsJSON> getDepartmentsWithPartsList(String companyUrlSlug){

        CompanyParamsJSON params = getCompanyParamsBySlug(companyUrlSlug);

        String stringQuery;
        Long companyId =params.getCompanyId();
        Long masterId =params.getMasterId();
        stringQuery =
                "           select" +
                        "           p.id as id," +
                        "           p.name as name," +
                        "           p.description as description," +
                        "           coalesce(p.is_active, true) as is_active," +
                        "           d.name as department_name, " +
                        "           d.id as department_id, " +
                        "           coalesce(d.address,'') as dep_address, " +
                        "           coalesce(d.additional,'') as dep_additional " +
                        "           from scdl_dep_parts p" +
                        "           INNER JOIN users u ON p.master_id=u.id" +
                        "           INNER JOIN departments d ON p.department_id=d.id" +
                        "           where  p.master_id=" + masterId +
                        "           and p.department_id in (select id from departments where company_id="+companyId+" and coalesce(is_deleted,false)=false)" +
                        "           and coalesce(p.is_deleted, false) = false" +
                        "           order by d.name,p.menu_order";
        try {
            return departmentRepository.departmentsPartsListConstruct(stringQuery, masterId, companyId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method OnlineSchedulingRepositoryJPA/getDepartmentsWithPartsList. SQL query:" + stringQuery, e);
            return null;
        }
    }







    // Return IDs of employees who has shifts schedule in a selected period of time
    // IDs сотрудников, у которых есть расписание в заданном промежутке времени.
    private Set<Long> getEmployeesWithSchedulingIdsList (Long companyId, String dateFrom, String dateTo, Set<Long> depPartsIds, String myTimeZone, Long masterId){
        Set<Long> employeesWithWorkingSchedule = new HashSet<>(); // employees who have at least one shift schedule
            List<BreakJSON>employeesBreaksList = calendarRepository.getCalendarUsersBreaksList(new CalendarEventsQueryForm(companyId,dateFrom,dateTo,depPartsIds),myTimeZone,masterId);
            for(BreakJSON break_:employeesBreaksList){
                employeesWithWorkingSchedule.add(break_.getUser().getId());
            }
        return employeesWithWorkingSchedule;
    }

    public List<AppointmentEmployee> getEmployeesWithSchedulingList(AppointmentMainInfoForm request){
        String stringQuery="";
        try {
            CompanyParamsJSON params = getCompanyParamsBySlug(request.getCompanyUrlSlug());
            Long companyId =params.getCompanyId();
            Long masterId =params.getMasterId();
            List<AppointmentEmployee> returnList = new ArrayList<>();
            Set<Long> employeesWithWorkingSchedule = getEmployeesWithSchedulingIdsList(companyId, request.getDateFrom(), request.getDateTo(), request.getDepPartsIds(), params.getTime_zone_name() ,masterId);
            if(employeesWithWorkingSchedule.size()>0) {
                String employeesIds = commonUtilites.SetOfLongToString(employeesWithWorkingSchedule, ",", "(", ")");
                stringQuery = " select " +
                        " u.id as u_id, " +
                        " u.name as u_name, " +
                        " jt.id as jt_id, " +
                        " coalesce(jt.name,'') as jt_name, " +
                        " dp.id as dp_id, " +
                        " dp.name as dp_name, " +
                        " p.id as p_id, " +
                        " p.name as p_name " +
                        " from " +
                        " users u " +
                        " inner join sprav_jobtitles jt on u.job_title_id=jt.id " +
                        " left outer join scdl_user_products up on u.id = up.user_id " +
                        " left outer join products p on p.id=up.product_id " +
                        " left outer join scdl_dep_part_products dpp on dpp.product_id=p.id " +
                        " left outer join scdl_dep_parts dp on dpp.dep_part_id=dp.id " +
                        " where " +
                        " u.company_id = "+companyId+" and " +
                        " u.master_id =  "+masterId+" and " +
                        " u.status_account = 2 and " +
                        " coalesce(u.is_employee,false) = true and " +
                        " coalesce(u.is_currently_employed,false) = true and " +
                        " coalesce(u.is_display_in_employee_list,false) = true " +
                        " and u.id in " + employeesIds +
                        " order by u.name,dp.name,p.name;";
                Long currentUserId = 0L;
                Long currentDepPartId = 0L;
                AppointmentEmployee appointmentEmployee = new AppointmentEmployee();
                DepartmentPartWithServicesIds departmentPartWithServicesIds = new DepartmentPartWithServicesIds();
                List<DepartmentPartWithServicesIds> departmentPartsWithServicesIds = new ArrayList<>();
                Set<Long> currentDepPartServicesIds = new HashSet<>();
                Query query = entityManager.createNativeQuery(stringQuery);//
                List<Object[]> queryList = query.getResultList();
                for (Object[] obj : queryList) {
                    Long currentCycleEmployeeId = Long.parseLong(obj[0].toString());
                    String currentCycleEmployeeName = obj[1].toString();
                    Long currentCycleJobTitleId = Long.parseLong(obj[2].toString());
                    String currentCycleJobTitleName = obj[3].toString();
                    Long currentCycleDepPartId = Objects.isNull(obj[4])?null:Long.parseLong(obj[4].toString());
                    Long currentCycleServiceId = Objects.isNull(obj[6])?null:Long.parseLong(obj[6].toString());
                    // on this cycle if it is a new user
                    if (!currentCycleEmployeeId.equals(currentUserId)) {
                        // Если это не первый цикл
                        // If it is not a first cycle
                        if (!currentUserId.equals(0L)) {
                            // В текущую часть отделения сохранили все накопленные IDs сервисов
                            departmentPartWithServicesIds.setServicesIds(currentDepPartServicesIds);
                            // В список частей отделения текущего пользователя добавили текущее отделение
                            if(!Objects.isNull(departmentPartWithServicesIds.getId()))
                                departmentPartsWithServicesIds.add(departmentPartWithServicesIds);
                            // В текущего сотрудника поместили список частей отделений
                            appointmentEmployee.setDepartmentPartsWithServicesIds(departmentPartsWithServicesIds);
                            // В итоговый список сотрудников поместили этого сотрудника
                            returnList.add(appointmentEmployee);
                            // Cоздали нового сотрудника
                            appointmentEmployee = new AppointmentEmployee();
                            // Для нового сотрудника создаем новую часть отделенияи сбрасываем накопление IDs сервисов
                            currentDepPartId = currentCycleDepPartId;
                            // Cоздали новую часть отделения, и прописали туда её ID
                            if(!Objects.isNull(currentDepPartId))
                                departmentPartWithServicesIds = new DepartmentPartWithServicesIds(currentDepPartId);
                            else departmentPartWithServicesIds = new DepartmentPartWithServicesIds();
                            // Cбросили текущее накопление ID сервисов для новой части отделения
                            currentDepPartServicesIds = new HashSet<>();
                        }
                        currentUserId = currentCycleEmployeeId;
                        // Для нового сотрудника задаём его ID, имя и должность
                        appointmentEmployee.setId(currentCycleEmployeeId);
                        appointmentEmployee.setName(currentCycleEmployeeName);
                        appointmentEmployee.setJobtitle_id(currentCycleJobTitleId);
                        appointmentEmployee.setJobtitle_name(currentCycleJobTitleName);
                        // Cоздали новый лист для накопления частей отделений для нового сотрудника
                        departmentPartsWithServicesIds = new ArrayList<>();
                    }
                    // Если сотрудник не новый, но часть отделения сменилась
                    if (!Objects.isNull(currentCycleDepPartId) && !currentCycleDepPartId.equals(currentDepPartId)) {
                        if (!currentDepPartId.equals(0L)) {
                            // В текущую часть отделения сохранили все накопленные IDs сервисов
                            departmentPartWithServicesIds.setServicesIds(currentDepPartServicesIds);
                            // В список частей отделения текущего пользователя добавили текущее отделение
                            departmentPartsWithServicesIds.add(departmentPartWithServicesIds);
                        }
                        currentDepPartId = currentCycleDepPartId;
                        // Cоздали новую часть отделения, и прописали туда её ID
                        departmentPartWithServicesIds = new DepartmentPartWithServicesIds(currentDepPartId);
                        // Cбросили текущее накопление ID сервисов для новой части отделения
                        currentDepPartServicesIds = new HashSet<>();
                    }
                    // collect services
                    if(!Objects.isNull(currentCycleServiceId))
                        currentDepPartServicesIds.add(currentCycleServiceId);
                }
                // По окончании цикла, если в ней что-то было
                // нужно записать последнего сотрудника
                if (!currentUserId.equals(0L)) {
                    // В текущую часть отделения сохранили все накопленные IDs сервисов
                    departmentPartWithServicesIds.setServicesIds(currentDepPartServicesIds);
                    // В список частей отделения текущего пользователя добавили текущее отделение
                    if(!Objects.isNull(departmentPartWithServicesIds.getId()))
                        departmentPartsWithServicesIds.add(departmentPartWithServicesIds);
                    // В текущего сотрудника поместили список частей отделений
                    appointmentEmployee.setDepartmentPartsWithServicesIds(departmentPartsWithServicesIds);
                    // В итоговый список сотрудников поместили этого сотрудника
                    returnList.add(appointmentEmployee);
                }
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getEmployeesWithSchedulingList. SQL query:" + stringQuery, e);
            return new ArrayList<>();
        }
    }




    public List<EmployeeWorkingTimeAndPlacesJSON> getEmployeeWorkingTimeAndPlaces(CalendarEventsQueryForm request, String myTimeZone, Long masterId) {


        // Getting breaks list for all users of company who has a schedule of work shifts in a period of time "dateFrom - dateTo"
        List<BreakJSON> breaksList = calendarRepository.getCalendarUsersBreaksList(request,myTimeZone,masterId);

        Set workshiftsIds = new HashSet();

        //Getting the list of work shift IDs
        for(BreakJSON break_: breaksList){
            workshiftsIds.add(break_.getWorkshift_id());
        }

        // Now need to get the data contained information on employee_id, workshift_id, department_part_id like
        //                      employee_id     workshift_id        department_part_id
        //                      298	            1705	            12
        //                      298	            1706	            12
        //                      298	            1707	            12
        //
        // It will be in ewdList list

        String workshiftsIds_ = commonUtilites.SetOfLongToString(workshiftsIds, ",", "(", ")");
        String depPartsIds_ =  commonUtilites.SetOfLongToString(request.getDepparts(), ",", "(", ")");
        String stringQuery = " select " +
        " ssd.employee_id                   as employee_id,     " +
        " wd.workshift_id				    as workshift_id,    " +
        " wd.deppart_id 				    as deppart_id       " +
        " from  scdl_workshift_deppart wd                       " +
        " inner join scdl_workshift ws on ws.id=wd.workshift_id " +
        " inner join scdl_scedule_day ssd on ssd.id = ws.scedule_day_id  " +
        " where  wd.master_id = " + masterId +
        " and wd.deppart_id in " + depPartsIds_ +
        " and wd.workshift_id in " + workshiftsIds_ +
        " order by employee_id, wd.workshift_id, deppart_id";

        try{

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<EmployeeWorkshiftDeppart> ewdList = new ArrayList<>();
            for (Object[] obj : queryList) {
                EmployeeWorkshiftDeppart doc = new EmployeeWorkshiftDeppart();
                doc.setEmployeeId( Long.parseLong(      obj[0].toString()));
                doc.setWorkshiftId(Long.parseLong(      obj[1].toString()));
                doc.setDeppartId(  Long.parseLong(      obj[2].toString()));
                ewdList.add(doc);
            }

            // Now need to convert list of breaks to the list of working time with startTime and endTime
            // and add to each:
            // - work time shift ID
            // - employee ID
            // - IDs of department parts where this employee will work at this work shift
            List<EmployeeWorkingTimeAndPlacesJSON> employeeWorkingTimeAndPlaces = new ArrayList<>();
            int i = 0; // index
            for(BreakJSON break_: breaksList){

                // последний объект списка перерывов пользователя всегда имеет ID смены = null
                // the last record of each user's breaks list always has work shift ID equal to null
                if(!Objects.isNull(break_.getWorkshift_id())){
                    EmployeeWorkingTimeAndPlacesJSON employeeWorkingTimeAndPlace = new EmployeeWorkingTimeAndPlacesJSON();
                    employeeWorkingTimeAndPlace.setEmployee_id(break_.getUser().getId());
                    employeeWorkingTimeAndPlace.setWorkshift_id(break_.getWorkshift_id());
                    employeeWorkingTimeAndPlace.setStart(break_.getEnd());
                    employeeWorkingTimeAndPlace.setEnd(breaksList.get(i+1).getStart());
                    // collect the list of department parts
                    List<Long>      department_parts_ids = new ArrayList<>();
                    for(EmployeeWorkshiftDeppart ewd : ewdList){
                        if(ewd.getEmployeeId().equals(employeeWorkingTimeAndPlace.getEmployee_id()) &&
                           ewd.getWorkshiftId().equals(employeeWorkingTimeAndPlace.getWorkshift_id())
                        )
                            department_parts_ids.add(ewd.getDeppartId());
                    }
                    employeeWorkingTimeAndPlace.setDepartment_parts_ids(department_parts_ids);
                    employeeWorkingTimeAndPlaces.add(employeeWorkingTimeAndPlace);
                }
                i++;
            }

            return employeeWorkingTimeAndPlaces;


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getEmployeesWithSchedulingList. SQL query:" + stringQuery, e);
            return new ArrayList<>();
        }
    }

    public List<OnlineScedulingProductCategoryJSON> getOnlineSchedulingProductCategories(String companyUrlSlug,String langCode) {
        try{
            CompanyParamsJSON params = getCompanyParamsBySlug(companyUrlSlug);
            Long companyId = params.getCompanyId();
            Long masterId = params.getMasterId();
            String stringQuery =
                    " select " +
                            " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
                            " coalesce(NULLIF(translator.description, ''), coalesce(p.description,'')) as description, " +
//                            " coalesce(NULLIF(translator.slug, ''), coalesce(p.slug,'')) as slug, " +
//                            " coalesce(p.display, 'default') as display, " +
                            " p.id as crm_id, " +
                            " coalesce(p.parent_id, 0) as parent_crm_id, " +
                            " coalesce(f.original_name,'') as img_original_name, " +
                            " coalesce(f.name,'') as img_name, " +
                            " coalesce(f.alt,'') as img_alt, " +
                            " coalesce(f.anonyme_access, false) as img_anonyme_access, " +
                            " coalesce(p.output_order,10000) as menu_order, " +
                            " coalesce(parent_ctg_.is_booking_category,false) as is_parent_booking_category" +
                            " from product_categories p " +
                            " INNER JOIN companies c ON p.company_id = c.id  " +
//                            " INNER JOIN stores_productcategories spc ON spc.category_id = p.id  " +
//                            " INNER JOIN stores str ON spc.store_id = str.id " +
                            " LEFT OUTER JOIN files f ON p.image_id=f.id  " +
//                            " LEFT OUTER JOIN stores_productcategories parent_ctg ON p.parent_id=parent_ctg.category_id and parent_ctg.store_id=str.id " +
                            " LEFT OUTER JOIN product_categories parent_ctg_ ON p.parent_id=parent_ctg_.id   " +
                            " LEFT OUTER JOIN store_translate_categories translator ON p.id = translator.category_id and translator.lang_code = upper('" + langCode + "')" +
                            " where p.company_id = " + companyId +
                            " and p.master_id = " + masterId +
//                            " and str.id = " + storeId +
                            " and coalesce(p.is_booking_category, false) = true; ";
//                            " and (" +
//                            " (coalesce(spc.need_to_syncwoo,true) = true) or " + // if the category is need to be synchronized
//                            " (spc.date_time_syncwoo is null) or " + // if the category is created recently, or changed, but still not synchronized
//                            " (p.date_time_changed is not null and spc.date_time_syncwoo is not null and p.date_time_changed > spc.date_time_syncwoo)" +
//                            " ) ";

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<OnlineScedulingProductCategoryJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                OnlineScedulingProductCategoryJSON doc = new OnlineScedulingProductCategoryJSON();
                doc.setName((String)                                obj[0]);
                doc.setDescription((String)                         obj[1]);
//                doc.setSlug((String)                                obj[2]);
//                doc.setDisplay((String)                             obj[3]);
                doc.setId(Long.parseLong(                       obj[2].toString()));
                doc.setParent_id(((Boolean)obj[9])?Long.parseLong(obj[3].toString()):0L);// If parent category of the current category is not "store category" - then the current category will be the one of root categories in Online scheduling
                doc.setImg_original_name((String)                   obj[4]);
                doc.setImg_address(((Boolean)obj[7])?apiserver+"/api/public/getFile/"+obj[5]:null); // if the image is not shared file - then it couldn't be opened in Online scheduling
                doc.setImg_alt((String)                             obj[6]);
                doc.setImg_anonyme_access((Boolean)                 obj[7]);
                doc.setMenu_order((Integer)                         obj[8]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getOnlineSchedulingProductCategories", e);
            e.printStackTrace();
            return null;
        }
    }


    public Set<OnlineSchedulingServiceJSON> getOnlineSchedulingServices(OnlineSchedulingForm onlineSchedulingForm) {

        String stringQuery = "";
        String companyUrlSlug = onlineSchedulingForm.getCompanyUrlSlug();
        String langCode = onlineSchedulingForm.getLangCode();
        Set<Long> servicesIds = onlineSchedulingForm.getServicesIds();
        String servicesIds_ = commonUtilites.SetOfLongToString(servicesIds, ",", "(", ")");
        try{
            CompanyParamsJSON params = getCompanyParamsBySlug(companyUrlSlug);
            Long companyId = params.getCompanyId();
            Long masterId = params.getMasterId();
//            OnlineSchedulingSettingsJSON osSettings = getOnlineSchedulingSettings(companyUrlSlug);
            Long priceTypeId = Long.valueOf(commonUtilites.getFieldValueFromTableById("departments", "price_id", masterId, onlineSchedulingForm.getDepId()).toString());

            stringQuery =   " select " +
                            " p.id as id," +
                            " coalesce(NULLIF(translator.name, ''), p.name) as name, " +
                            " coalesce(p.type, 'simple') as type, " +
                            " p.article as sku, " +
                            " pr.price_value as price " +

                            " from products p " +
                            " INNER JOIN product_productcategories ppc ON ppc.product_id=p.id " +
                            " INNER JOIN product_categories pc ON pc.id=ppc.category_id " +
                            " INNER JOIN product_prices pr ON pr.product_id=p.id and pr.price_type_id=" + priceTypeId +
                            " LEFT OUTER JOIN store_translate_products translator ON p.id = translator.product_id and translator.lang_code = upper('" + langCode + "')" +

                            " where " +

                            " p.company_id = " + companyId +
                            " and coalesce(pc.is_booking_category,false)=true " + // if product is in the booking category
                            " and coalesce(p.is_deleted, false) = false " +
                            " and p.id not in (select variation_product_id from product_variations where master_id = " + masterId + ")" + // product is not used as variation
                            " and coalesce(p.is_deleted, false) = false " +
                            " and p.id in " + servicesIds_ +
                            " order by p.name ";

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            Set<OnlineSchedulingServiceJSON> returnList = new HashSet<>();
            Set<Long> productIds = new HashSet<>();

            for (Object[] obj : queryList) {
                OnlineSchedulingServiceJSON doc = new OnlineSchedulingServiceJSON();
                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setName((String)                                obj[1]);
                doc.setType((String)                                obj[2]);
                doc.setSku((String)                                 obj[3]);
                doc.setPrice((                                      obj[4]).toString());
                returnList.add(doc);
                productIds.add(doc.getId());
            }
            if(returnList.size()>0){
                Map<Long,Set<Long>> productCategoriesIds = getServicesCategoriesIds(productIds);
                Map<Long,List<ImageJSON>> productImages  = getServicesImages(productIds, companyId);

                for(OnlineSchedulingServiceJSON doc : returnList){
                    doc.setCategories(productCategoriesIds.get(doc.getId()));
                    doc.setImages(productImages.get(doc.getId()));
                }
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getOnlineSchedulingServices. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Map<Long,Set<Long>> getServicesCategoriesIds(Set<Long> productIds) {
        String productIds_ = commonUtilites.SetOfLongToString(productIds,",","(",")");
        String stringQuery= " SELECT " +
                            " product_id, " +
                            " category_id " +
                            " FROM product_productcategories " +
                            " WHERE " +
                            " product_id in " + productIds_ +
                            " ORDER BY product_id";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            Long currentProductId=0L;
            Set<Long>categoriesIds = new HashSet<>();
            Map<Long,Set<Long>> returnMap = new HashMap<>();
            for (Object[] obj : queryList) {
                Long currentCycleProductId = Long.parseLong(obj[0].toString());
                if(!currentCycleProductId.equals(currentProductId) && !currentProductId.equals(0L)){
                    returnMap.put(currentProductId, categoriesIds);
                    categoriesIds = new HashSet<>();
                    currentProductId = currentCycleProductId;
                } else if (!currentCycleProductId.equals(currentProductId) && currentProductId.equals(0L)) {
                    currentProductId = currentCycleProductId;
                }
                categoriesIds.add(Long.parseLong(obj[1].toString()));
            }
            returnMap.put(currentProductId, categoriesIds);
            return returnMap;
        }catch (Exception e) {
            logger.error("Exception in method getProductCategoriesIds. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private Map<Long,List<ImageJSON>> getServicesImages(Set<Long> productIds, Long companyId) throws Exception {
        String productIds_ = commonUtilites.SetOfLongToString(productIds,",","(",")");
        String stringQuery =
                "           select " +
                        "           p.id as product_id, " +
                        "           coalesce(f.original_name,'') as img_original_name, "+
                        "           coalesce(f.name,'') as img_name, "+
                        "           coalesce(f.alt,'') as img_alt "+
                        "           from " +
                        "           products p " +
                        "           inner join product_files pf on p.id=pf.product_id " +
                        "           inner join files f on pf.file_id=f.id " +
                        "           where " +
                        "           p.id in " + productIds_ +
                        "           and f.trash is not true " +
                        "           and p.company_id= " + companyId +
                        "           and coalesce(f.anonyme_access, false) is true " +
                        "           order by p.id, pf.output_order";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            Long currentProductId=0L;
            List<ImageJSON> imagesList = new ArrayList<>();
            Map<Long,List<ImageJSON>> returnMap = new HashMap<>();

            for (Object[] obj : queryList) {
                Long currentCycleProductId = Long.parseLong(obj[0].toString());
                if(!currentCycleProductId.equals(currentProductId) && !currentProductId.equals(0L)){
                    returnMap.put(currentProductId, imagesList);
                    imagesList = new ArrayList<>();
                    currentProductId = currentCycleProductId;
                } else if (!currentCycleProductId.equals(currentProductId) && currentProductId.equals(0L)) {
                    currentProductId = currentCycleProductId;
                }
                imagesList.add( new ImageJSON(
                        (String)                                        obj[1],
                        apiserver + "/api/public/getFile/" + obj[2],
                        (String)                                        obj[3]
                    )
                );
            }
            returnMap.put(currentProductId, imagesList);

            return returnMap;
        } catch (Exception e) {
            logger.error("Exception in method getServicesImages. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }







    // get ID of all current employees
    // получить ID всех действующих сотрудников
//    public List<Long> getAllEmployeesIds(Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, String myTimeZone, Long masterId) {
//        String stringQuery =
//                " select u.id from users u where " +
//                        " u.company_id = "+companyId+" and " +
//                        " u.master_id = "+masterId+" and " +
//                        " u.status_account = 2 and " +
//                        " coalesce(u.is_employee,false) = true and " +
//                        " coalesce(u.is_currently_employed,false) = true and " +
//                        " coalesce(u.is_display_in_employee_list,false) = true;";
//        try{
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Integer> queryList = query.getResultList();
//            List<Long> returnList = new ArrayList<>();
//            for (Integer obj : queryList) {
//                returnList.add(obj.longValue());
//            }
//            return returnList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getAllEmployeesIds. SQL query:" + stringQuery, e);
//            return new ArrayList<>();
//        }
//    }

}
