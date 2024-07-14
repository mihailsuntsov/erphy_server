package com.dokio.repository;

import com.dokio.message.request.Settings.SettingsCalendarForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.message.response.Settings.SettingsCalendarJSON;
import com.dokio.message.response.additional.appointment.DepartmentPartWithServicesIds;
import com.dokio.message.response.additional.calendar.*;
import com.dokio.message.response.additional.appointment.AppointmentEmployee;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.LocalDate.now;

@Repository
public class CalendarRepositoryJPA {

    Logger logger = Logger.getLogger("CalendarRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;





    public List<CalendarEventJSON> getCalendarEventsList(CalendarEventsQueryForm request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        List<CalendarEventJSON> events = new ArrayList<>();
        List<CalendarEventJSON> returnList = new ArrayList<>();
        LocalDate localDate = LocalDate.now();
        Long masterId = userRepositoryJPA.getMyMasterId();
        String myTimeZone = userRepository.getUserTimeZone();

        String calendarDate = localDate.format(formatter);
//        String dateTo = localDate.plusDays(10).format(formatter);
//        String event1_start = localDate.plusDays(14).format(formatter);
//        String event1_end = localDate.plusDays(19).format(formatter);
//        String event2_start = localDate.plusDays(3).format(formatter);
//        String event2_end = localDate.plusDays(9).format(formatter);
//        String event3_start = localDate.plusDays(4).format(formatter);
//        String event3_end = localDate.plusDays(9).format(formatter);
//        String event4_start = localDate.plusDays(5).format(formatter);
//        String event4_end = localDate.plusDays(8).format(formatter);
//        String event5_start = localDate.plusDays(1).format(formatter);
//        String event5_end = localDate.plusDays(8).format(formatter);
//
//        events.add(new CalendarEventJSON(1L, calendarDate+"T08:00:00.000Z",calendarDate+"T10:30:00.000Z", "Стрижка Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
//        events.add(new CalendarEventJSON(2L, calendarDate+"T08:30:00.000Z",calendarDate+"T13:00:00.000Z", "Покраска Иванова Т.А.", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
//        events.add(new CalendarEventJSON(3L, calendarDate+"T14:30:00.000Z",calendarDate+"T17:00:00.000Z", "Сложная покраска Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
//        events.add(new CalendarEventJSON(4L, calendarDate+"T08:00:00.000Z",calendarDate+"T11:30:00.000Z", "Стрижка Ким", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
//        events.add(new CalendarEventJSON(5L, calendarDate+"T10:30:00.000Z",calendarDate+"T12:00:00.000Z", "Стрижка Борисюк", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
//        events.add(new CalendarEventJSON(6L, calendarDate+"T11:30:00.000Z",calendarDate+"T13:00:00.000Z", "Стрижка Холмогорова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
//        events.add(new CalendarEventJSON(7L, calendarDate+"T10:30:00.000Z",calendarDate+"T15:00:00.000Z", "Стрижка Тутти", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(298L, "Анастасия Сунцова", new CalendarColors("#000000","#B0E0E0")),"appointment")));
//
//        events.add(new CalendarEventJSON(22L, calendarDate+"T12:00:00.000Z",dateTo+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
//        events.add(new CalendarEventJSON(23L, event1_start+"T12:00:00.000Z",event1_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
//        events.add(new CalendarEventJSON(24L, event3_start+"T12:00:00.000Z",event3_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
//        events.add(new CalendarEventJSON(25L, event4_start+"T12:00:00.000Z",event4_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
//        events.add(new CalendarEventJSON(26L, event1_start+"T12:00:00.000Z",event1_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
//
//        events.add(new CalendarEventJSON(27L, event2_start+"T12:00:00.000Z",event2_end+"T13:00:00.000Z", "Проживание Люкс", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 21L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(22L,"Кровать люкс", 1, 1))))));

        String depPartsIds_ =  commonUtilites.SetOfLongToString(request.getDepparts(), ",", "(", ")");
//        String jobTitlesIds_ = commonUtilites.SetOfLongToString(request.getJobtitles(), ",", "(", ")");
        String employeesIds_ = commonUtilites.SetOfLongToString(request.getEmployees(), ",", "(", ")");

        String stringQuery = " select " +
            "           a.id as id, " +
            "           a.name  as name, " +
            "           concat(to_char(a.starts_at_time at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0','YYYY-MM-DD'), 'T', to_char(a.starts_at_time at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0','HH24:MI:SS.MS'), 'Z') as start_, " +
            "           concat(to_char(a.ends_at_time at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0','YYYY-MM-DD'), 'T', to_char(a.ends_at_time at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0','HH24:MI:SS.MS'), 'Z') as end_, " +
            "           ue.id as employee_id, " +
            "           ue.name as employee, " +
            "           a.dep_part_id as doc_dep_part_id, " +
            "           r.id as resource_id, " +
            "           r.name as resource_name, " +
            "           sum(coalesce(prq.quantity,0)) as need_res_qtt, " +
            "           ssd.name as status_name, " +
            "           ssd.status_type as status_type " + //тип статуса : 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                                                           //status type:  1 - normal;  2 - final positive         3 - final negative
            "           from scdl_appointments a " +
            "           left outer join users ue ON a.employee_id=ue.id " +
            "           left outer join scdl_appointments_product ap on ap.appointment_id=a.id " +
            "           left outer join products p on p.id=ap.product_id " +
            "           left outer join scdl_product_resource_qtt prq on p.id=prq.product_id  " +
            "           left outer join sprav_resources r on prq.resource_id=r.id " +
            "           inner join sprav_status_dock ssd on a.status_id = ssd.id " +
            "           where " +
            "           a.master_id=" + masterId + " and " +
            "           a.company_id=" + request.getCompanyId() + " and " +
            "           coalesce(a.is_deleted,false) = false and " +
                // Events (appointments/reservations) must intersect the range of dates ( event_start < range_end AND event_end > range_start )
            "           to_timestamp ('"+request.getDateFrom()+" "+request.getTimeFrom()+":00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' < a.ends_at_time and " +
            "           to_timestamp ('"+request.getDateTo()+" "+request.getTimeTo()+":59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' > a.starts_at_time " +
                        (request.getEmployees().size()>0?(" and (ue.id in "+employeesIds_+" or ue.id is null)" ):"") +
                        (request.getDepparts().size()>0?(" and a.dep_part_id in "+depPartsIds_ ):"") +
            "           group by a.id, a.name, start_, end_, ue.id,ue.name,a.dep_part_id,r.id,r.name,ssd.name,ssd.status_type " +
            "           order by a.id, r.id";


        try{
            Long currentAppointmentId = 0L;

            CalendarEventJSON appointment = new CalendarEventJSON();
//            ItemResource resource = new ItemResource();
            Set<ItemResource> resources = new HashSet<>();
            Query query = entityManager.createNativeQuery(stringQuery);//
            List<Object[]> queryList = query.getResultList();
            CalendarUser currentEmployee;
            Meta meta = new Meta();

            for (Object[] obj : queryList) {
                Long currentCycleAppointmentId = Long.parseLong(obj[0].toString());
                String currentCycleAppointmentName = (String) obj[1];
                String currentCycleDateStart = (String) obj[2];
                String currentCycleDateEnd = (String) obj[3];
                Long currentCycleEmployeeId =obj[4] != null ? Long.parseLong(obj[4].toString()) : null;
                String currentCycleEmployeeName = (String) obj[5];
                Long currentCycleDepPartId = Long.parseLong(obj[6].toString());
                Long currentCycleResourceId = obj[7] != null ? Long.parseLong(obj[7].toString()) : null;
                String currentCycleResourceName = (String) obj[8];
                Integer currentCycleResourceQtt = obj[9] != null ? Integer.parseInt((obj[9]).toString()) : null;
                String currentCycleStatusName = (String) obj[10];
                Integer currentCycleStatusType = obj[11] != null ? Integer.parseInt((obj[11]).toString()) : null;

                // on this cycle if it is a new Appointment
                // если это новая Запись
                if (!currentCycleAppointmentId.equals(currentAppointmentId)) {

                    // Если это не первый цикл
                    // If it is not a first cycle
                    if (!currentAppointmentId.equals(0L)) {
                        // Объект, содержащий всю дополнительную информацию по Записи дополнили информацией по накопленным ресурсам
                        // An object containing all additional information on the Record has been supplemented with information on accumulated resources
                        meta.setItemResources(resources);
                        // ... и добавили его в Запись
                        appointment.setMeta(meta);
                        // This Appointment was added to the final list of Appointments
                        // В итоговый список Записей поместили эту Запись
                        returnList.add(appointment);
                    }
                    // Задаём новую текущую Запись
                    // Set a new current Appointment
                    currentAppointmentId = currentCycleAppointmentId;
                    appointment = new CalendarEventJSON();
                    // Для новой Записи задаём её базовые данные
                    // Set a basic data for new current Appointment
                    appointment.setId(                           currentCycleAppointmentId);
                    appointment.setTitle(                         currentCycleAppointmentName);
                    appointment.setColor(new CalendarColors("#008000","#FDF1BA"));
                    appointment.setStart(currentCycleDateStart);
                    appointment.setEnd(currentCycleDateEnd);
                    // Для новой Записи создали новый сет для накопления ресурсов
                    // For a new Appointment, a new set was created to collect resources
                    resources = new HashSet<>();
                    // и добавили в него текущий ресурс
                    // and added the current resource to it
//                    resources.add(new ItemResource(currentCycleResourceId, currentCycleResourceName, currentCycleResourceQtt));
                    // Для новой Записи создали нового пользователя
                    // Created a new user for the new Appointment
                    currentEmployee = new CalendarUser(currentCycleEmployeeId, currentCycleEmployeeName, new CalendarColors("#000000","#B0E0E0"));
                    // Создали новый объект, содержащий всю дополнительную информацию по Записи
                    // Created a new object containing all additional information about the Appointment
                    meta = new Meta(currentEmployee,"appointment", currentCycleDepPartId, currentCycleStatusName, currentCycleStatusType);
                }
                // Копим ресурсы
                // Сollect resources
                if(!Objects.isNull(currentCycleResourceId))
                    resources.add(new ItemResource(currentCycleResourceId, currentCycleResourceName,currentCycleResourceQtt));
            }
            // По окончании цикла, если в Записи что-то было
            // нужно записать последний ресурс
            if (!currentAppointmentId.equals(0L)) {
                // Объект, содержащий всю дополнительную информацию по Записи дополнили информацией по накопленным ресурсам
                meta.setItemResources(resources);
                // ... и добавили его в Запись
                appointment.setMeta(meta);
                // В итоговый список Записей поместили текущую Запись
                returnList.add(appointment);
            }

            return returnList;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getCalendarEventsList.", e);
            return null;
        }
    }

//    public List<BreakJSON> getCalendarUsersBreaksList2(CalendarEventsQueryForm queryForm) {
//        List<BreakJSON> breaks = new ArrayList<>();
//        breaks.add(new BreakJSON(4L, "2024-04-23T00:00:00.000Z", "2024-04-23T08:00:00.000Z"));
//        breaks.add(new BreakJSON(4L, "2024-04-23T17:00:00.000Z", "2024-04-23T23:59:59.999Z"));
//        breaks.add(new BreakJSON(36L, "2024-04-23T00:00:00.000Z", "2024-04-23T08:00:00.000Z"));
//        breaks.add(new BreakJSON(36L, "2024-04-23T17:00:00.000Z", "2024-04-23T23:59:59.999Z"));
//        breaks.add(new BreakJSON(37L, "2024-04-23T00:00:00.000Z", "2024-04-23T08:00:00.000Z"));
//        breaks.add(new BreakJSON(37L, "2024-04-23T17:00:00.000Z", "2024-04-23T23:59:59.999Z"));
//        breaks.add(new BreakJSON(298L, "2024-04-23T00:00:00.000Z", "2024-04-23T08:30:00.000Z"));
//        breaks.add(new BreakJSON(298L, "2024-04-23T17:00:00.000Z", "2024-04-23T23:59:59.999Z"));
//        return breaks;
//
//    }

    public List<BreakJSON> getCalendarUsersBreaksList(CalendarEventsQueryForm queryForm) {
//        if(securityRepositoryJPA.userHasPermissions_OR(60L, "725,726"))//(см. файл Permissions Id)
//        {
            try{
                String myTimeZone = userRepository.getUserTimeZone();
                Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
                Long masterId = userRepositoryJPA.getMyMasterId();
                int companyTimeZoneId = (Integer) commonUtilites.getFieldValueFromTableById("companies", "time_zone_id", masterId, queryForm.getCompanyId());
                String companyTimeZone = commonUtilites.getTimeZoneById(companyTimeZoneId);
                DateTimeFormatter sqlQueryFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                DateTimeFormatter calendarFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateFrom = LocalDate.parse(queryForm.getDateFrom(),sqlQueryFormatter).minusDays(1+7);
                LocalDate dateTo = LocalDate.parse(queryForm.getDateTo(),sqlQueryFormatter).plusDays(1+7);
                // The shift of previous day can be started in the first day of query.
                // In this case to get the information about start of first break we need to get previous day
                // Also week can contain both of months - this is because we need to include an additional week
//                dateFrom = LocalDate(dateFrom.minusDays(1+7));
                // The shift of current day can be finishet before the end of a day.
                // In this case to get the information about finish of the last break of last query day, we need to get a next day
                // Also week can contain both of months - this is because we need to include an additional week
//                dateTo = dateTo.plusDays(1+7);

                if (    !commonUtilites.isDateValid(queryForm.getDateFrom()) ||
                        !commonUtilites.isDateValid(queryForm.getDateTo()) ||
                        (!securityRepositoryJPA.userHasPermissions_OR(60L, "725") && !myCompanyId.equals(queryForm.getCompanyId())))//если есть право только на своё предприятие, но запрашиваем не своё
                    throw new IllegalArgumentException("Invalid query parameters");



                List<PointOfScedule> pointsOfScedule = getPointsOfScedule(
                        queryForm.getCompanyId(),
                        dateFrom.format(sqlQueryFormatter),
                        dateTo.format(sqlQueryFormatter),
                        queryForm.getDepparts(),
                        queryForm.getJobtitles(),
                        queryForm.getEmployees(),
                        companyTimeZone,
                        myTimeZone,
                        masterId
                );

                List<BreakJSON> breaks = new ArrayList<>();
                Long currentUserId = 0L;
                String currentUserName = "";
                boolean isNewUser = false;
                BreakJSON break_= new BreakJSON();
                int i = 0; // index
                for (PointOfScedule pointOfScedule : pointsOfScedule) {
                    if(!pointOfScedule.getUserId().equals(currentUserId)){
                        isNewUser = true;

                        // если закончились данные по пользователю и break не был закрыт
                        // конечным временем, то его надо закрыть последней миллисекудной последней даты,
                        // а так же добавить текущий объект break_ в breaks
                        // if the user data has run out and break was not closed by the end time,
                        // then it must be closed at the last millisecond of the last date,
                        // and also add the current break_ object to breaks
                        if(!currentUserId.equals(0L) && Objects.isNull(break_.getEnd())){
                            break_.setEnd(dateTo.format(calendarFormatter)+"T23:59:59.999Z");
                            breaks.add(break_);
                        }




                        currentUserId = pointOfScedule.getUserId();
                        currentUserName = pointOfScedule.getUserName();
                    }



                    if(pointOfScedule.getPointOfSceduleName().equals("workshift_time_from") && isNewUser){
                        break_ = new BreakJSON(new CalendarUser(currentUserId, currentUserName, new CalendarColors("#008000","#FDF1BA")),dateFrom.format(calendarFormatter)+"T00:00:00.000Z", pointOfScedule.getPointOfSceduleTime());
                        breaks.add(break_);
                    }


                    if(pointOfScedule.getPointOfSceduleName().equals("workshift_time_to")// '..._to' because this is the end of shift (or its part), that equals start of break or non-working time
                        // If the next start point of this shift will not be equals to the end of current shift.
                        // This is designed for seamless work shifts, where the end of the shift coincides with its beginning
                    && !(i+1 < pointsOfScedule.size()
                        && pointsOfScedule.get(i+1).getUserId().equals(currentUserId)
                        && pointsOfScedule.get(i+1).getPointOfSceduleName().equals("workshift_time_from")
                        && pointsOfScedule.get(i+1).getPointOfSceduleTime().equals(pointOfScedule.getPointOfSceduleTime()))
                    ){
                        break_ = new BreakJSON(new CalendarUser(currentUserId, currentUserName, new CalendarColors("#008000","#FDF1BA")),pointOfScedule.getPointOfSceduleTime());
                    }




                    if(pointOfScedule.getPointOfSceduleName().equals("workshift_time_from") && !isNewUser
                    && !(i > 0
                        && pointsOfScedule.get(i-1).getUserId().equals(currentUserId)
                        && pointsOfScedule.get(i-1).getPointOfSceduleName().equals("workshift_time_to")
                        && pointsOfScedule.get(i-1).getPointOfSceduleTime().equals(pointOfScedule.getPointOfSceduleTime()))

                    ){
                        break_.setEnd(pointOfScedule.getPointOfSceduleTime());
                        breaks.add(break_);
                    }





                    isNewUser = false;
                    i++;
                }

                if(!currentUserId.equals(0L) && Objects.isNull(break_.getEnd())){
                    break_.setEnd(dateTo.format(calendarFormatter)+"T23:59:59.999Z");
                    breaks.add(break_);
                }


                return breaks;



            } catch (IllegalArgumentException e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("IllegalArgumentException in method getCalendarUsersBreaksList ", e);
                e.printStackTrace();
                return null; // см. _ErrorCodes
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCalendarUsersBreaksList.", e);
                return null;
            }

//        } else return null;

    }









    private List<PointOfScedule> getPointsOfScedule(Long companyId, String dateFrom, String dateTo, Set<Long> depPartsIds, Set<Long> jobTitlesIds, Set<Long> employeesIds, String companyTimeZone, String myTimeZone, Long masterId){

        String stringQuery;

        String depPartsIds_ =  commonUtilites.SetOfLongToString(depPartsIds, ",", "(", ")");
        String jobTitlesIds_ = commonUtilites.SetOfLongToString(jobTitlesIds, ",", "(", ")");
        String employeesIds_ = commonUtilites.SetOfLongToString(employeesIds, ",", "(", ")");

        stringQuery =
        "   select " +
        "   sd1.day_date as day_date, " +
        //  here getting the time in timezone of querying user because it will be shown in his calendar
        "   to_timestamp(concat(to_char(sd1.day_date, 'YYYY-MM-DD'),' ',to_char(w1.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' as point_of_scedule_timestamp, " +
        "   to_char(to_timestamp(concat(to_char(sd1.day_date, 'YYYY-MM-DD'),' ',to_char(w1.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI:SS.MS') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as point_of_scedule_time, " +
        "   sd1.employee_id as user_id, " +
        "   'workshift_time_from' as point_of_scedule_name, " +
        "   u1.name as user_name " +
        "   from " +
        "   scdl_scedule_day sd1 " +
        "   inner join scdl_workshift w1 on w1.scedule_day_id = sd1.id " +
        "   inner join users u1 on sd1.employee_id = u1.id " +
        "   where " +
        "   sd1.master_id = " + masterId + " and " +
        "   u1.company_id = " + companyId + " and " +
        "   " +
            (jobTitlesIds.size()>0?("sd1.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " ):"") +
            (employeesIds.size()>0?("sd1.employee_id in "+employeesIds_+" and " ):"") +
        "   coalesce(u1.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "       select product_id from scdl_user_products where master_id="+masterId+" and user_id = u1.id " +
        "       INTERSECT " +
        "       select product_id from scdl_dep_part_products where master_id="+masterId+(depPartsIds.size()>0?(" and dep_part_id in " + depPartsIds_):"") +
        "       ) as aaa " +
        "   )>0 " + // here we convert all times to GMT, because we need to compare time of company and time of user
        "   and to_timestamp(to_char(sd1.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' >= to_timestamp('"+dateFrom+" 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +
        "   and to_timestamp(to_char(sd1.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' <= to_timestamp('"+dateTo+" 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +

        "   union all " +

        "   select " +
        "   sd2.day_date as day_date, " +
        "   case " +
        "       when w2.time_to <= w2.time_from " +
        "       then to_timestamp(concat(to_char(sd2.day_date+1, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       else to_timestamp(concat(to_char(sd2.day_date, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   end as point_of_scedule_timestamp, " +
        "   case " +
        "       when w2.time_to <= w2.time_from " +
        "       then to_char(to_timestamp(concat(to_char(sd2.day_date+1, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') " +
        "       else to_char(to_timestamp(concat(to_char(sd2.day_date, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') " +
        "   end as point_of_scedule_time, " +
        "   sd2.employee_id as user_id, " +
        "   'workshift_time_to' as point_of_scedule_name, " +
        "   u2.name as user_name " +
        "   from " +
        "   scdl_scedule_day sd2 " +
        "   inner join scdl_workshift w2 on w2.scedule_day_id = sd2.id " +
        "   inner join users u2 on sd2.employee_id = u2.id " +
        "   where " +
        "   sd2.master_id = " + masterId + " and " +
        "   u2.company_id = " + companyId + " and " +
            (jobTitlesIds.size()>0?("sd2.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " ):"") +
            (employeesIds.size()>0?("sd2.employee_id in "+employeesIds_+" and " ):"") +
        "   coalesce(u2.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u2.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+(depPartsIds.size()>0?(" and dep_part_id in " + depPartsIds_):"") +
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd2.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' >= to_timestamp('"+dateFrom+" 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +
        "   and to_timestamp(to_char(sd2.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' <= to_timestamp('"+dateTo+" 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +

        "   union all " +

        "   select " +
        "   sd3.day_date as day_date, " +
        "   case " +
        "       when br3.time_to <= w3.time_from " +
        "       then to_timestamp(concat(to_char(sd3.day_date+1, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       else to_timestamp(concat(to_char(sd3.day_date, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       end as point_of_scedule_timestamp, " +
        "   case " +
        "       when br3.time_to <= w3.time_from " +
        "       then to_char(to_timestamp(concat(to_char(sd3.day_date+1, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') " +
        "       else to_char(to_timestamp(concat(to_char(sd3.day_date, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') " +
        "       end as point_of_scedule_time, " +
        "   sd3.employee_id as user_id, " +
        "   'workshift_time_from' as point_of_scedule_name, " +
        "   u3.name as user_name " +
        "   from " +
        "   scdl_scedule_day sd3 " +
        "   inner join scdl_workshift w3 on w3.scedule_day_id = sd3.id " +
        "   inner join scdl_workshift_breaks br3 on br3.workshift_id = w3.id " +
        "   inner join users u3 on sd3.employee_id = u3.id " +
        "   where " +
        "   sd3.master_id = " + masterId + " and " +
        "   u3.company_id = " + companyId + " and " +
            (jobTitlesIds.size()>0?("sd3.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " ):"") +
            (employeesIds.size()>0?("sd3.employee_id in "+employeesIds_+" and " ):"") +
        "   coalesce(u3.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u3.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+(depPartsIds.size()>0?(" and dep_part_id in " + depPartsIds_):"")+
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd3.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' >= to_timestamp('"+dateFrom+" 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +
        "   and to_timestamp(to_char(sd3.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' <= to_timestamp('"+dateTo+" 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +

        "   union all " +

        "   select " +
        "   sd4.day_date as day_date, " +
        "   case " +
        "       when br4.time_from <= w4.time_from " +
        "       then to_timestamp(concat(to_char(sd4.day_date+1, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       else to_timestamp(concat(to_char(sd4.day_date, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   end as point_of_scedule_timestamp, " +
        "   case " +
        "       when br4.time_from <= w4.time_from " +
        "       then to_char(to_timestamp(concat(to_char(sd4.day_date+1, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') " +
        "       else to_char(to_timestamp(concat(to_char(sd4.day_date, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+myTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') " +
        "   end as point_of_scedule_time, " +
        "   sd4.employee_id as user_id, " +
        "   'workshift_time_to' as point_of_scedule_name, " +
        "   u4.name as user_name " +
        "   from " +
        "   scdl_scedule_day sd4 " +
        "   inner join scdl_workshift w4 on w4.scedule_day_id = sd4.id " +
        "   inner join scdl_workshift_breaks br4 on br4.workshift_id = w4.id " +
        "   inner join users u4 on sd4.employee_id = u4.id " +
        "   where " +
        "   sd4.master_id = " + masterId + " and " +
        "   u4.company_id = " + companyId + " and " +
            (jobTitlesIds.size()>0?("sd4.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " ):"") +
            (employeesIds.size()>0?("sd4.employee_id in "+employeesIds_+" and " ):"") +
        "   coalesce(u4.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u4.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+(depPartsIds.size()>0?(" and dep_part_id in " + depPartsIds_):"") +
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd4.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' >= to_timestamp('"+dateFrom+" 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +
        "   and to_timestamp(to_char(sd4.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' <= to_timestamp('"+dateTo+" 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' " +

                "   order by user_id, point_of_scedule_time, day_date;";

        try{

            Query query = entityManager.createNativeQuery(stringQuery);//
//            if (searchString != null && !searchString.isEmpty())
//            {query.setParameter("sg", searchString);}
            List<Object[]> queryList = query.getResultList();
            List<PointOfScedule> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                PointOfScedule doc = new PointOfScedule();

                doc.setPointOfSceduleTimestamp((Timestamp)              obj[1]);
                doc.setPointOfSceduleTime((String)                      obj[2]);
                doc.setUserId(                     Long.parseLong(      obj[3].toString()));
                doc.setPointOfSceduleName((String)                      obj[4]);
                doc.setUserName((String)                                obj[5]);
                returnList.add(doc);
            }
            return returnList;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getPointsOfScedule. SQL query:" + stringQuery, e);
            return null;
        }
    }



    // IDs of employees that are free by Appointment events. Does not take into account accessibility of employees by scedule of workshifts
    // IDs сотрудников, которые свободны по Записям в заданный промежуток времени. Не учитывает доступность/занятость сотрудников по графику рабочих смен
    private List<Long> getEmployeesIdsByAppointments(boolean isFree, Long currentAppointmentId, Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, Set<Long> servicesIds, Set<Long> depPartsIds, Set<Long> jobTitlesIds, Set<Long> employeesIds, String myTimeZone, Long masterId) {

        String stringQuery;
        String depPartsIds_ =  commonUtilites.SetOfLongToString(depPartsIds, ",", "(", ")");
        String jobTitlesIds_ = commonUtilites.SetOfLongToString(jobTitlesIds, ",", "(", ")");
        String servicesIds_ =  commonUtilites.SetOfLongToString(servicesIds, ",", "(", ")");
        String employeesIds_ =  commonUtilites.SetOfLongToString(employeesIds, ",", "(", ")");

        stringQuery =
        " select u.id from users u where " +
        " u.company_id = "+companyId+" and " +
        " u.master_id = "+masterId+" and " +
        " u.status_account = 2 and " +
        " u.is_employee = true and " +
        " u.is_currently_employed = true and " +
        (servicesIds.size() >0?(" u.id in (select user_id from scdl_user_products where product_id in "+servicesIds_+") and "):"") +
        (jobTitlesIds.size()>0?(" u.job_title_id in "+jobTitlesIds_+" and "):"") +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+(depPartsIds.size()>0?(" and dep_part_id in " + depPartsIds_):"")+
        "       ) as aaa " +
        "   )>0 and " +
        "   u.id " +
            (isFree?" not ":"") +
        "   in (" +
        "   select sa.employee_id from scdl_appointments sa " +
        "   inner join scdl_appointments_product ap on ap.appointment_id = sa.id" +
        "   inner join products p on p.id = ap.product_id " +
        "   inner join sprav_status_dock ssd on sa.status_id = ssd.id " +
        "   where " +
        "   sa.master_id = "+masterId+" and " +
        "   sa.company_id = "+companyId+" and " +
        "   sa.id != "+currentAppointmentId + " and " + // if query runs from existed appointment - its ID must not be matter
        "   ssd.status_type != 3 and " +                // cancelled Appointments are no matter
        "   sa.employee_id is not null and " +

                // dateFrom & timeFrom                 are in time zone of user
                // sa.ends_at_time & sa.starts_at_time are in UTC (GMT+0) time zone
                // dateFrom & timeFrom should be converted to UTC

        "   to_timestamp ('"+dateFrom+" "+timeFrom+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' < sa.ends_at_time and " +
        "   to_timestamp ('"+dateTo+" "+timeTo+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' > sa.starts_at_time and " +
        "   coalesce(p.scdl_is_employee_required, false) = true )";


        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Integer> queryList = query.getResultList();
            List<Long> returnList = new ArrayList<>();
            for (Integer obj : queryList) {
                returnList.add(obj.longValue());
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFreeOrOccupiedEmployeesIds. SQL query:" + stringQuery, e);
            return new ArrayList<>();
        }
    }


    // Return IDs of free employees by shifts schedule and by period of time of all they appointments
    // IDs сотрудников, которые свободны и по расписанию смен, и по заданному промежутку времени.
    private Set<Long> getFreeEmployeeIdsList (Long currentAppointmentId, Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, Set<Long> servicesIds, Set<Long> depPartsIds, Set<Long> jobTitlesIds, Set<Long> employeesIds, String myTimeZone,Long masterId){
//-------------------------------------------------------------------------------------------------------
//   employee Id   Has shifts schedule?    Is it working time?    Is employee free by appointment?
//
//        1              V  	                  V                       V
//        2              V  	                  X  	    	          V
//        3              V  	                  V 				      X
//        4              X  				      -				          V
//
//        getEmployeesIdsFreeByAppointments
//        [1,2,4]
//        employeesWithWorkingSchedule
//        [1,2,3]
//        restingEmployees
//        [2]
//        employeesWithoutWorkingSchedule = [1,2,4]-[1,2,3]
//        [4]
//        restingEmployees = restingEmployees + employeesWithoutWorkingSchedule = [2]+[4]
//        [2,4]
//        return employeesIdsFreeByAppointments - restingEmployees = [1,2,4]-[2,4]
//        [1]
//        return FreeEmployeeIds = [1]
//-------------------------------------------------------------------------------------------------------

        Set<Long> restingEmployees = new HashSet<>(); // emplouees who have rest by schedule (non-working day or break)
        Set<Long> employeesWithWorkingSchedule = new HashSet<>(); // employees who have at least one shift schedule

//        int companyTimeZoneId = (Integer)commonUtilites.getFieldValueFromTableById("companies", "time_zone_id", masterId, companyId);
//        String companyTimeZone = commonUtilites.getTimeZoneById(companyTimeZoneId);
        // get IDs of employees who is free by the time of Appointments (but we do not know whether they free by work shifts scedule)
        // получить ID сотрудников, свободных по времени Записей (но мы не знаем, свободны ли они по графику рабочих смен)
        Set<Long> employeesIdsFreeByAppointments = new HashSet<>(getEmployeesIdsByAppointments(true, currentAppointmentId,companyId,dateFrom,timeFrom,dateTo,timeTo,servicesIds,depPartsIds,jobTitlesIds,employeesIds,myTimeZone,masterId));
        DateTimeFormatter ISO8601_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        DateTimeFormatter system_formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm").withZone(ZoneId.of("UTC"));
        LocalDateTime a_date_start = LocalDateTime.parse(dateFrom+" "+timeFrom, system_formatter);
        LocalDateTime a_date_end =   LocalDateTime.parse(dateTo+" "+timeTo, system_formatter);
        /*
        If there are employees who are free by the time of Appointments, then we need to get their work shifts schedule
        and find the employee's IDs who is free by it.
        Если есть сотрудники, свободные по времени Записи, то нам необходимо получить график их рабочих смен.
        и найти ID сотрудников, которые по нему свободны.
        */
        if(employeesIdsFreeByAppointments.size()>0){

            List<BreakJSON>employeesBreaksList = getCalendarUsersBreaksList(new CalendarEventsQueryForm(companyId,dateFrom,dateTo,depPartsIds,jobTitlesIds,employeesIds));

            // break contains the start and end of employee's non-working time unit. Time has a format YYYY-MM-DDTHH:MM:00Z like 2024-03-25T13:00:00Z
            // this time presented in the time zone settings of user (myTimeZone)
            // dateFrom, timeFrom,dateTo,timeTo           are also in the time zone of user

            for(BreakJSON break_:employeesBreaksList){

                employeesWithWorkingSchedule.add(break_.getUser().getId());

                LocalDateTime b_date_start = LocalDateTime.parse(break_.getStart(), ISO8601_formatter);
                LocalDateTime b_date_end =   LocalDateTime.parse(break_.getEnd(), ISO8601_formatter);

                // If there is an intersection of Appointment time with at least of one of employee's breaks time - employee's ID should be added to restingEmployees set.
                // So, we collect the IDs of employees with whom it is impossible to make an appointment.

                // The formula of intersection is:
                // a_start < b_end AND a_end > b_start

                if(a_date_start.isBefore(b_date_end) && a_date_end.isAfter(b_date_start)){
                    restingEmployees.add(break_.getUser().getId());
                }

            }
            Set<Long> employeesWithoutWorkingSchedule = new HashSet<>(employeesIdsFreeByAppointments); // employees who have no shifts schedule
            employeesWithoutWorkingSchedule.removeAll(employeesWithWorkingSchedule);
            restingEmployees.addAll(employeesWithoutWorkingSchedule);

            // Now need to substract resting employees IDs and non-have scedule employees IDs from employees who are free by appointments schedule
            employeesIdsFreeByAppointments.removeAll(restingEmployees);
        }


        return employeesIdsFreeByAppointments;

    }
    // Return IDs of non-free employees by shifts schedule or by period of time of all they appointments
    // IDs сотрудников, которые не свободны по расписанию смен или по заданному промежутку времени.
    private Set<Long> getNonAccessibleEmployeesIdsList (String kindOfNoFree, Long currentAppointmentId, Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, Set<Long> servicesIds, Set<Long> depPartsIds, Set<Long> jobTitlesIds, Set<Long> employeesIds, String myTimeZone,Long masterId){
//-------------------------------------------------------------------------------------------------------
//   employee Id   Has shifts schedule?    Is it working time?    Is employee free by appointment?
//
//        1              V  	                  V                       V
//        2              V  	                  X  	    	          V
//        3              V  	                  V 				      X
//        4              X  				      -				          V
//
//        employeesIdsFreeByAppointments
//          [1,2,4]
//        employeesIdsBusyByAppointments
//          [3]
//        allEmployeesIds = [1,2,4]+[3]
//          [1,2,3,4]
//        employeesWithWorkingSchedule
//          [1,2,3]
//        restingEmployees
//          [2]
//        employeesWithoutWorkingSchedule = [1,2,3,4]-[1,2,3]
//          [4]
//        restingEmployees=restingEmployees+employeesWithoutWorkingSchedule
//          [2,4]
//
//        return:
//        employeesIdsBusyByAppointments
//          [3]
//        restingEmployees
//          [2,4]
//-------------------------------------------------------------------------------------------------------
        Set<Long> employeesWithWorkingSchedule = new HashSet<>(); // employees who have at least one shift schedule
        // It can be querying by "Busy by Appointments" or "Busy by schedule of working time"
        // get IDs of employees who is "Busy by Appointments"
        Set<Long> employeesIdsBusyByAppointments = new HashSet<>(getEmployeesIdsByAppointments(false, currentAppointmentId,companyId,dateFrom,timeFrom,dateTo,timeTo,servicesIds,depPartsIds,jobTitlesIds,employeesIds,myTimeZone,masterId));
        // It querying by "Busy by Appointments"
        if (kindOfNoFree.equals("busyByAppointments"))
            return employeesIdsBusyByAppointments;

//       If not querying by "Busy by Appointments", then by "Busy by schedule of working time":
        Set<Long> employeesIdsFreeByAppointments = new HashSet<>(getEmployeesIdsByAppointments(true, currentAppointmentId,companyId,dateFrom,timeFrom,dateTo,timeTo,servicesIds,depPartsIds,jobTitlesIds,employeesIds,myTimeZone,masterId));
        Set<Long> allEmployeesIds = new HashSet<>();
        allEmployeesIds.addAll(employeesIdsFreeByAppointments);
        allEmployeesIds.addAll(employeesIdsBusyByAppointments);

        // If all employees are "Busy by Appointment" - then there are no employees who "Busy by schedule of working time"
        if(employeesIdsBusyByAppointments.size()==allEmployeesIds.size())
            return new HashSet<>();

        Set<Long> restingEmployees = new HashSet<>(); // employees who have rest by schedule (non-working day or break)
        DateTimeFormatter ISO8601_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        DateTimeFormatter system_formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm").withZone(ZoneId.of("UTC"));
        LocalDateTime a_date_start = LocalDateTime.parse(dateFrom+" "+timeFrom, system_formatter);
        LocalDateTime a_date_end =   LocalDateTime.parse(dateTo+" "+timeTo, system_formatter);
        List<BreakJSON>employeesBreaksList = getCalendarUsersBreaksList(new CalendarEventsQueryForm(companyId,dateFrom,dateTo,depPartsIds,jobTitlesIds,employeesIds));
        // break contains the start and end of employee's non-working time unit. Time has a format YYYY-MM-DDTHH:MM:00Z like 2024-03-25T13:00:00Z
        // this time presented in the time zone settings of user (myTimeZone)
        // dateFrom, timeFrom,dateTo,timeTo           are also in the time zone of user
        for(BreakJSON break_:employeesBreaksList){
            employeesWithWorkingSchedule.add(break_.getUser().getId());
            LocalDateTime b_date_start = LocalDateTime.parse(break_.getStart(), ISO8601_formatter);
            LocalDateTime b_date_end =   LocalDateTime.parse(break_.getEnd(), ISO8601_formatter);
            // If there is an intersection of Appointment time with at least of one of employee's breaks time - employee's ID should be added to restingEmployees set.
            // So, we collect the IDs of employees with whom it is impossible to make an appointment.
            // The formula of intersection is:
            // a_start < b_end AND a_end > b_start
            if(a_date_start.isBefore(b_date_end) && a_date_end.isAfter(b_date_start)){
                restingEmployees.add(break_.getUser().getId());
            }
        }
        Set<Long> employeesWithoutWorkingSchedule = new HashSet<>(allEmployeesIds); // employees who have no shifts schedule
        employeesWithoutWorkingSchedule.removeAll(employeesWithWorkingSchedule);
        restingEmployees.addAll(employeesWithoutWorkingSchedule);
        return restingEmployees;
    }

    public List<AppointmentEmployee> getEmployeesList(AppointmentMainInfoForm request){

        String stringQuery = "";
        try {
            Long masterId = userRepositoryJPA.getMyMasterId();
            String myTimeZone = userRepository.getUserTimeZone();
//            int companyTimeZoneId = (Integer) commonUtilites.getFieldValueFromTableById("companies", "time_zone_id", masterId, request.getCompanyId());
//            String companyTimeZone = commonUtilites.getTimeZoneById(companyTimeZoneId);
            List<AppointmentEmployee> returnList = new ArrayList<>();
            Set<Long> employeesIdsList = new HashSet<>();
            boolean isAll = request.getIsAll();

            if (!isAll && request.getIsFree()) // when query is going from Appointment to get free employees list
                employeesIdsList = getFreeEmployeeIdsList(request.getAppointmentId(), request.getCompanyId(), request.getDateFrom(), request.getTimeFrom(), request.getDateTo(), request.getTimeTo(), request.getServicesIds(), request.getDepPartsIds(), request.getJobTitlesIds(), request.getEmployeesIds(), myTimeZone, masterId);
            else if (!isAll && !request.getIsFree()) // when query is going from Appointment to get busy employees list
                employeesIdsList = getNonAccessibleEmployeesIdsList(request.getKindOfNoFree(), request.getAppointmentId(), request.getCompanyId(), request.getDateFrom(), request.getTimeFrom(), request.getDateTo(), request.getTimeTo(), request.getServicesIds(), request.getDepPartsIds(), request.getJobTitlesIds(), request.getEmployeesIds(), myTimeZone, masterId);


            if (isAll || employeesIdsList.size() > 0) {

                String depPartsIds_ = commonUtilites.SetOfLongToString(request.getDepPartsIds(), ",", "(", ")");
                String jobTitlesIds_ = commonUtilites.SetOfLongToString(request.getJobTitlesIds(), ",", "(", ")");
                String servicesIds_ = commonUtilites.SetOfLongToString(request.getServicesIds(), ",", "(", ")");
                String employeesIds = commonUtilites.SetOfLongToString(employeesIdsList, ",", "(", ")");

//              With the help of this "employees_workshift_depparts" helping table with columns <employee_id>-<deppart_id> is going filtering of employees list by department parts,
//              contained in work shift of employee
//              С помощью этой вспомогательной таблицы "employees_workshift_depparts" со столбцами <employee_id>-<deppart_id> происходит фильтрация списка сотрудников по частям отделений,
//              содержащимся в рабочей смене сотрудника.
                stringQuery="" +
                " WITH employees_workshift_depparts AS " +
                " ( " +
                "   select " +
                "   ssd.employee_id                 as employee_id, " +
                "   wd.deppart_id 				    as deppart_id " +
                "   from  scdl_workshift_deppart wd " +
                "   inner join scdl_workshift ws on ws.id=wd.workshift_id " +
                "   inner join scdl_scedule_day ssd on ssd.id = ws.scedule_day_id " +
                "   where  wd.master_id = "+masterId+" and " +
                "   wd.workshift_id in ( " +
                "       select " +
                "       ws.id 						    as workshift_id " +
                "       from " +
                "       scdl_scedule_day ssd " +
                "       left outer join scdl_workshift ws on ws.scedule_day_id=ssd.id " +
                "       where " +
                "       ssd.master_id="+masterId+" and " +
                "       ws.id is not null and " +
                (employeesIdsList.size() > 0?("       ssd.employee_id in "+employeesIds+" and "):"") +
//              Сравнение производится по времени пользователя, т.к. запрос идет из UI. По этому нужно адаптировать время начала и окончания смены ко времени пользователя
//              The comparison is made based on the user's time, because the request comes from the UI. Therefore, it is necessary to adapt the start and end times of the work shift to the user’s time

//              The formula of Entering is:
//              Start of work shift <= Start of Appointment
                "       to_timestamp(concat(to_char(ssd.day_date, 'DD.MM.YYYY'),' ',to_char(ws.time_from,'HH24:MI')),'DD.MM.YYYY HH24:MI') at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0' <= to_timestamp('"+request.getDateFrom()+" "+request.getTimeFrom()+":00.000', 'DD.MM.YYYY HH24:MI:SS.MS') and " +
//              AND End of Workshift >= End of Appointment
                "       case " +
                "           when " +
                "               ws.time_to <= ws.time_from " +
                "           then ( " +
                "               to_timestamp(concat(to_char(ssd.day_date+1, 'DD.MM.YYYY'),' ',to_char(ws.time_to,'HH24:MI')),'DD.MM.YYYY HH24:MI') at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0' >= to_timestamp('"+request.getDateTo()+" "+request.getTimeTo()+":00.000', 'DD.MM.YYYY HH24:MI:SS.MS')" +
                "           ) else ( " +
                "               to_timestamp(concat(to_char(ssd.day_date, 'DD.MM.YYYY'),' ',to_char(ws.time_to,'HH24:MI')),'DD.MM.YYYY HH24:MI') at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0' >= to_timestamp('"+request.getDateTo()+" "+request.getTimeTo()+":00.000', 'DD.MM.YYYY HH24:MI:SS.MS')" +
                "           ) " +
                "       end " +
                "   )" +
                " )" +
                " select " +
                " u.id as u_id, " +
                " u.name as u_name, " +
                " jt.id as jt_id, " +
                " jt.name as jt_name, " +
                " dp.id as dp_id, " +
                " dp.name as dp_name, " +
                " p.id as p_id, " +
                " p.name as p_name " +
                " from " +
                " users u, " +
                " sprav_jobtitles jt, " +
                " scdl_user_products up, " +
                " products p, " +
                " scdl_dep_part_products dpp, " +
                " scdl_dep_parts dp " +
                " where " +
                " u.company_id = "+request.getCompanyId()+" and " +
                " u.master_id =  "+masterId+" and " +
                " u.status_account = 2 and " +
                " u.is_employee = true and " +
                " u.is_currently_employed = true " +
                ((!isAll && request.getIsFree())?" and dp.id in (select deppart_id from employees_workshift_depparts where employee_id = u.id) ":"") +
                (!isAll?(" and u.id in "+employeesIds):"") +
                (request.getServicesIds(). size() > 0 ? (" and p.id  in " + servicesIds_ ) : "") +
                (request.getDepPartsIds(). size() > 0 ? (" and dp.id in " + depPartsIds_ ) : "") +
                (request.getJobTitlesIds().size() > 0 ? (" and jt.id in " + jobTitlesIds_) : "") +
                " and u.job_title_id=jt.id and " +
                " u.id = up.user_id and " +
                " p.id=up.product_id and " +
                " dpp.product_id=p.id and " +
                " dpp.dep_part_id=dp.id" +
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
                    Long currentCycleDepPartId = Long.parseLong(obj[4].toString());
                    Long currentCycleServiceId = Long.parseLong(obj[6].toString());

                    // on this cycle if it is a new user
                    if (!currentCycleEmployeeId.equals(currentUserId)) {

                        // Если это не первый цикл
                        // If it is not a first cycle
                        if (!currentUserId.equals(0L)) {

                            // В текущую часть отделения сохранили все накопленные IDs сервисов
                            departmentPartWithServicesIds.setServicesIds(currentDepPartServicesIds);

                            // В список частей отделения текущего пользователя добавили текущее отделение
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
                            departmentPartWithServicesIds = new DepartmentPartWithServicesIds(currentDepPartId);

                            // Cбросили текущее накопление ID сервисов для новой части отделения
                            currentDepPartServicesIds = new HashSet<>();

                        }

                        currentUserId = currentCycleEmployeeId;

                        // Для нового сотрудника задаём его ID, имя и должность
                        appointmentEmployee.setId(currentCycleEmployeeId);
                        appointmentEmployee.setName(currentCycleEmployeeName);
                        appointmentEmployee.setJobtitle_id(currentCycleJobTitleId);
                        if(!isAll) appointmentEmployee.setState(request.getFree()?"free":request.getKindOfNoFree());

                        // Cоздали новый лист для накопления частей отделений для нового сотрудника
                        departmentPartsWithServicesIds = new ArrayList<>();

                    }

                    // Если сотрудник не новый, но часть отделения сменилась
                    if (!currentCycleDepPartId.equals(currentDepPartId)) {

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

                    currentDepPartServicesIds.add(currentCycleServiceId);
                }

                // По окончании цикла, если в ней что-то было
                // нужно записать последнего сотрудника
                if (!currentUserId.equals(0L)) {

                    // В текущую часть отделения сохранили все накопленные IDs сервисов
                    departmentPartWithServicesIds.setServicesIds(currentDepPartServicesIds);

                    // В список частей отделения текущего пользователя добавили текущее отделение
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
            logger.error("Exception in method getEmployeesList. SQL query:" + stringQuery, e);
            return new ArrayList<>();
        }
    }


    @Transactional
    public Boolean saveSettingsCalendar(SettingsCalendarForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);

            stringQuery =
                    " insert into settings_calendar (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "date_time_update, " +
                            "start_view, "+
                            "timeline_step, "+
                            "day_start_minute, "+
                            "day_end_minute, "+
                            "resources_screen_scale, "+
                            "display_cancelled"+
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            "now(), " +
                            ":start_view,"+
                            row.getTimelineStep() + "," +
                            row.getDayStartMinute() + "," +
                            row.getDayEndMinute() + "," +
                            ":resources_screen_scale," +
                            row.getDisplayCancelled() +
            ") " +
                    " ON CONFLICT ON CONSTRAINT settings_calendar_user_uq " +// "upsert"
                    " DO update set " +
                    " company_id = " + row.getCompanyId() + "," +
                    " start_view = :start_view," +
                    " timeline_step = " + row.getTimelineStep() + "," +
                    " day_start_minute = " + row.getDayStartMinute() + "," +
                    " day_end_minute = " + row.getDayEndMinute() + "," +
                    " date_time_update = now()," +
                    " resources_screen_scale = :resources_screen_scale," +
                    " display_cancelled = "+row.getDisplayCancelled();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("start_view",row.getStartView());
            query.setParameter("resources_screen_scale",row.getResourcesScreenScale());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsCalendar. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public SettingsCalendarJSON getSettingsCalendar() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "     select " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                 // предприятие
                "           coalesce(start_view,'month') as start_view, " + // month / scheduler / resources
                "           coalesce(timeline_step, 30) as timeline_step, " + // step in minutes
                "           coalesce(day_start_minute, 0) as day_start_minute, " + // minute of day start (0-1438) that means 00:00 - 23:58
                "           coalesce(day_end_minute, 1439) as day_end_minute, " + // minute of day end (1-1439)   that means 00:01 - 23:59
                "           coalesce(resources_screen_scale,'month') as resources_screen_scale, " + //  month / week / day
                "           coalesce(display_cancelled, false) as display_cancelled " + //  display or not cancelled events by default"
                "           from settings_calendar p " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsCalendarJSON returnObj=new SettingsCalendarJSON(
                    "month",30,0,1439,"month",false);

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                             obj[0].toString()));
                returnObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                returnObj.setStartView((String)                             obj[2]);
                returnObj.setTimelineStep((Integer)                         obj[3]);
                returnObj.setDayStartMinute((Integer)                       obj[4]);
                returnObj.setDayEndMinute((Integer)                         obj[5]);
                returnObj.setResourcesScreenScale((String)                  obj[6]);
                returnObj.setDisplayCancelled((Boolean)                     obj[7]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsCalendar. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }























//    private String getFreeEmployeesListSQL(AppointmentMainInfoForm request, long masterId, String freeEmployeesIds, String servicesIds_, String depPartsIds_, String jobTitlesIds_,String myTimeZone,String companyTimeZone){
//
//    return      "select " +
//                    " u.id as u_id, " +
//                    " u.name as u_name, " +
//                    " jt.id as jt_id, " +
//                    " jt.name as jt_name, " +
//                    " dp.id as dp_id, " +
//                    " dp.name as dp_name, " +
//                    " p.id as p_id, " +
//                    " p.name as p_name " +
//                    " from " +
//                    " users u, " +
//                    " sprav_jobtitles jt, " +
//                    " scdl_user_products up, " +
//                    " products p, " +
//                    " scdl_dep_part_products dpp, " +
//                    " scdl_dep_parts dp, " +
//                    " scdl_scedule_day scd, " +
//                    " scdl_workshift w, " +
//                    " scdl_workshift_deppart wdp " +
//                    " where " +
//                    " u.company_id = "+request.getCompanyId()+" and " +
//                    " u.master_id =  "+masterId+" and " +
//                    " u.status_account = 2 and " +
//                    " u.is_employee = true and " +
//                    " u.is_currently_employed = true and " +
//                    " u.id in "+freeEmployeesIds+
//                    (request.getServicesIds(). size() > 0 ? (" and p.id  in " + servicesIds_ ) : "") +
//                    (request.getDepPartsIds(). size() > 0 ? (" and dp.id in " + depPartsIds_ ) : "") +
//                    (request.getJobTitlesIds().size() > 0 ? (" and jt.id in " + jobTitlesIds_) : "") +
//                    " and u.job_title_id=jt.id and " +
//                    " u.id = up.user_id and " +
//                    " p.id=up.product_id and " +
//                    " dpp.product_id=p.id and " +
//                    " dpp.dep_part_id=dp.id and " +
//                    " scd.employee_id=u.id and " +
//                    " w.scedule_day_id=scd.id and " +
//                    " wdp.workshift_id=w.id and " +
//                    " wdp.deppart_id=dp.id and " +
////                    To get only actual (accessible by the Appointment time period) parts of departments, services and job titles,
////                    it is necessary to determine which shift the Appointment time period belongs to.
////                    To do this, I need to check it for intersections with all shifts of free employees
//
////                      The formula of intersection is:
////                      A_end > B_start AND A_start < B_end
//
////                    End of Appointment > Start of Workshift
//                    " to_timestamp('"+request.getDateTo()+" "+request.getTimeTo()+"','DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' > " +
//                    " to_timestamp(concat(to_char(scd.day_date, 'YYYY-MM-DD'),' ',to_char(w.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' " +
//                    " and " +
////                    AND Start of Appointment < End of Workshift
//                    " case " +
//                    "   when " +
//                    "       w.time_to <= w.time_from " +
//                    "   then ( " +
//                    "       to_timestamp('"+request.getDateFrom()+" "+request.getTimeFrom()+"','DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' < " +
//                    "       to_timestamp(concat(to_char(scd.day_date+1, 'YYYY-MM-DD'),' ',to_char(w.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' " +
//                    "   ) else ( " +
//                    "       to_timestamp('"+request.getDateFrom()+" "+request.getTimeFrom()+"','DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' < " +
//                    "       to_timestamp(concat(to_char(scd.day_date, 'YYYY-MM-DD'),' ',to_char(w.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+companyTimeZone+"' " +
//                    "   ) " +
//                    " end " +
//
//                    " order by u.name,dp.name,p.name;";
//    }


}
