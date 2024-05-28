package com.dokio.repository;

import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.message.response.additional.appointment.DepartmentPartWithServicesIds;
import com.dokio.message.response.additional.calendar.*;
import com.dokio.message.response.additional.appointment.AppointmentEmployee;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
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





    public List<CalendarEventJSON> getCalendarEventsList(CalendarEventsQueryForm queryForm){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<CalendarEventJSON> events = new ArrayList<>();
        LocalDate localDate = LocalDate.now();
        String calendarDate = localDate.format(formatter);
        String dateTo = localDate.plusDays(10).format(formatter);
        String event1_start = localDate.plusDays(14).format(formatter);
        String event1_end = localDate.plusDays(19).format(formatter);
        String event2_start = localDate.plusDays(3).format(formatter);
        String event2_end = localDate.plusDays(9).format(formatter);
        String event3_start = localDate.plusDays(4).format(formatter);
        String event3_end = localDate.plusDays(9).format(formatter);
        String event4_start = localDate.plusDays(5).format(formatter);
        String event4_end = localDate.plusDays(8).format(formatter);
        String event5_start = localDate.plusDays(1).format(formatter);
        String event5_end = localDate.plusDays(8).format(formatter);





        events.add(new CalendarEventJSON(1L, calendarDate+"T08:00:00.000Z",calendarDate+"T10:30:00.000Z", "Стрижка Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(2L, calendarDate+"T08:30:00.000Z",calendarDate+"T13:00:00.000Z", "Покраска Иванова Т.А.", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(3L, calendarDate+"T14:30:00.000Z",calendarDate+"T17:00:00.000Z", "Сложная покраска Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(4L, calendarDate+"T08:00:00.000Z",calendarDate+"T11:30:00.000Z", "Стрижка Ким", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(5L, calendarDate+"T10:30:00.000Z",calendarDate+"T12:00:00.000Z", "Стрижка Борисюк", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(6L, calendarDate+"T11:30:00.000Z",calendarDate+"T13:00:00.000Z", "Стрижка Холмогорова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(7L, calendarDate+"T10:30:00.000Z",calendarDate+"T15:00:00.000Z", "Стрижка Тутти", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(298L, "Анастасия Сунцова", new CalendarColors("#000000","#B0E0E0")),"appointment")));
 /*
       events.add(new CalendarEventJSON(8L, "2024-04-30T08:00:00.000Z","2024-04-30T10:30:00.000Z", "Стрижка Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(9L, "2024-04-30T08:30:00.000Z","2024-04-30T13:00:00.000Z", "Покраска Иванова Т.А.", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(10L, "2024-04-30T14:30:00.000Z","2024-04-30T17:00:00.000Z", "Сложная покраска Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(11L, "2024-04-30T08:00:00.000Z","2024-04-30T11:30:00.000Z", "Стрижка Ким", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(12L, "2024-04-30T10:30:00.000Z","2024-04-30T12:00:00.000Z", "Стрижка Борисюк", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(13L, "2024-04-30T11:30:00.000Z","2024-04-30T13:00:00.000Z", "Стрижка Холмогорова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(14L, "2024-04-30T10:30:00.000Z","2024-04-30T15:00:00.000Z", "Стрижка Тутти", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(298L, "Анастасия Сунцова", new CalendarColors("#000000","#B0E0E0")),"appointment")));


        events.add(new CalendarEventJSON(15L, "2024-05-01T08:00:00.000Z","2024-05-01T10:30:00.000Z", "Стрижка Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(16L, "2024-05-01T08:30:00.000Z","2024-05-01T13:00:00.000Z", "Покраска Иванова Т.А.", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(17L, "2024-05-01T14:30:00.000Z","2024-05-01T17:00:00.000Z", "Сложная покраска Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(18L, "2024-05-01T08:00:00.000Z","2024-05-01T11:30:00.000Z", "Стрижка Ким", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(19L, "2024-05-01T10:30:00.000Z","2024-05-01T12:00:00.000Z", "Стрижка Борисюк", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(20L, "2024-05-01T11:30:00.000Z","2024-05-01T13:00:00.000Z", "Стрижка Холмогорова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(21L, "2024-05-01T10:30:00.000Z","2024-05-01T15:00:00.000Z", "Стрижка Тутти", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(298L, "Анастасия Сунцова", new CalendarColors("#000000","#B0E0E0")),"appointment")));
*/


        events.add(new CalendarEventJSON(22L, calendarDate+"T12:00:00.000Z",dateTo+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
        events.add(new CalendarEventJSON(23L, event1_start+"T12:00:00.000Z",event1_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
        events.add(new CalendarEventJSON(24L, event3_start+"T12:00:00.000Z",event3_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
        events.add(new CalendarEventJSON(25L, event4_start+"T12:00:00.000Z",event4_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));
        events.add(new CalendarEventJSON(26L, event1_start+"T12:00:00.000Z",event1_end+"T13:00:00.000Z", "Проживание Стандарт", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 14L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(21L,"Кровать", 1, 4))))));

        events.add(new CalendarEventJSON(27L, event2_start+"T12:00:00.000Z",event2_end+"T13:00:00.000Z", "Проживание Люкс", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(0L, "No employee", new CalendarColors("#000000","#B0E0E0")),"appointment", 21L, new HashSet<ItemResource>(Arrays.asList(new ItemResource(22L,"Кровать люкс", 1, 1))))));






        return events;

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
                int companyTimeZoneId = (Integer)commonUtilites.getFieldValueFromTableById("companies", "time_zone_id", masterId, queryForm.getCompanyId());
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









    private List<PointOfScedule> getPointsOfScedule(Long companyId, String dateFrom, String dateTo, Set<Long> depPartsIds, Set<Long> jobTitlesIds, String companyTimeZone, String myTimeZone, Long masterId){

        String stringQuery;

        String depPartsIds_ =  commonUtilites.SetOfLongToString(depPartsIds, ",", "(", ")");
        String jobTitlesIds_ = commonUtilites.SetOfLongToString(jobTitlesIds, ",", "(", ")");

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



    // IDs of employees that are free by Appointment events.
    // Does not take into account accessibility of employees by scedule of workshifts
    // IDs сотрудников, которые свободны по Записям в заданный промежуток времени. Не учитывает доступность/занятость сотрудников по графику рабочих смен
    private List<Long> getEmployeesIdsByAppointments(boolean isFree, Long currentAppointmentId, Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, Set<Long> servicesIds, Set<Long> depPartsIds, Set<Long> jobTitlesIds, String myTimeZone, Long masterId) {

        String stringQuery;
        String depPartsIds_ =  commonUtilites.SetOfLongToString(depPartsIds, ",", "(", ")");
        String jobTitlesIds_ = commonUtilites.SetOfLongToString(jobTitlesIds, ",", "(", ")");
        String servicesIds_ =  commonUtilites.SetOfLongToString(servicesIds, ",", "(", ")");

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
        "   in ( " +
        "   select sa.employee_id from scdl_appointments sa " +
        "   inner join products p on p.id = sa.service_id " +
        "   where " +
        "   sa.master_id = "+masterId+" and " +
        "   sa.company_id = "+companyId+" and " +

        // if query runs from existed appointment - its ID must not be matter
        "   sa.id !="+currentAppointmentId + " and " +

                // dateFrom & timeFrom                 are in time zone of user
                // sa.ends_at_time & sa.starts_at_time are in UTC (GMT+0) time zone
                // dateFrom & timeFrom should be converted to UTC

        "   to_timestamp ('"+dateFrom+" "+timeFrom+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' < sa.ends_at_time and " +
        "   to_timestamp ('"+dateTo+" "+timeTo+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' > sa.starts_at_time and " +
        "   coalesce(p.scdl_is_employee_required, false) = true " +
        " ); ";


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
    private Set<Long> getFreeEmployeeIdsList (Long currentAppointmentId, Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, Set<Long> servicesIds, Set<Long> depPartsIds, Set<Long> jobTitlesIds, String myTimeZone,Long masterId){
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
            Set<Long> employeesIdsFreeByAppointments = new HashSet<>(getEmployeesIdsByAppointments(true, currentAppointmentId,companyId,dateFrom,timeFrom,dateTo,timeTo,servicesIds,depPartsIds,jobTitlesIds,myTimeZone,masterId));
        DateTimeFormatter ISO8601_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        DateTimeFormatter system_formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm").withZone(ZoneId.of("UTC"));
        LocalDateTime a_date_start = LocalDateTime.parse(dateFrom+" "+timeFrom, system_formatter);
        LocalDateTime a_date_end =   LocalDateTime.parse(dateTo+" "+timeTo, system_formatter);
        /*
        If there are employees who are free by the time of Appointments, then we need to get their work shifts schedule
        and find the employee's IDs who is free by it.
        */
        if(employeesIdsFreeByAppointments.size()>0){

            List<BreakJSON>employeesBreaksList = getCalendarUsersBreaksList(new CalendarEventsQueryForm(companyId,dateFrom,dateTo,depPartsIds,jobTitlesIds));

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
    private Set<Long> getNonAccessibleEmployeesIdsList (String kindOfNoFree, Long currentAppointmentId, Long companyId, String dateFrom, String timeFrom, String dateTo, String timeTo, Set<Long> servicesIds, Set<Long> depPartsIds, Set<Long> jobTitlesIds, String myTimeZone,Long masterId){
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
        Set<Long> employeesIdsBusyByAppointments = new HashSet<>(getEmployeesIdsByAppointments(false, currentAppointmentId,companyId,dateFrom,timeFrom,dateTo,timeTo,servicesIds,depPartsIds,jobTitlesIds,myTimeZone,masterId));
        // It querying by "Busy by Appointments"
        if (kindOfNoFree.equals("busyByAppointments"))
            return employeesIdsBusyByAppointments;

//       If not querying by "Busy by Appointments", then by "Busy by schedule of working time":
        Set<Long> employeesIdsFreeByAppointments = new HashSet<>(getEmployeesIdsByAppointments(true, currentAppointmentId,companyId,dateFrom,timeFrom,dateTo,timeTo,servicesIds,depPartsIds,jobTitlesIds,myTimeZone,masterId));
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
        List<BreakJSON>employeesBreaksList = getCalendarUsersBreaksList(new CalendarEventsQueryForm(companyId,dateFrom,dateTo,depPartsIds,jobTitlesIds));
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
//    private String getEmployeesListSQL(AppointmentMainInfoForm request, long masterId, String freeEmployeesIds, String servicesIds_, String depPartsIds_, String jobTitlesIds_,String myTimeZone,String companyTimeZone) {
//        return "select " +
//                " u.id as u_id, " +
//                " u.name as u_name, " +
//                " jt.id as jt_id, " +
//                " jt.name as jt_name, " +
//                " dp.id as dp_id, " +
//                " dp.name as dp_name, " +
//                " p.id as p_id, " +
//                " p.name as p_name " +
//                " from " +
//                " users u, " +
//                " sprav_jobtitles jt, " +
//                " scdl_user_products up, " +
//                " products p, " +
//                " scdl_dep_part_products dpp, " +
//                " scdl_dep_parts dp " +
//                " where " +
//                " u.company_id = "+request.getCompanyId()+" and " +
//                " u.master_id =  "+masterId+" and " +
//                " u.status_account = 2 and " +
//                " u.is_employee = true and " +
//                " u.is_currently_employed = true and " +
//                " u.id in "+freeEmployeesIds+
//                (request.getServicesIds(). size() > 0 ? (" and p.id  in " + servicesIds_ ) : "") +
//                (request.getDepPartsIds(). size() > 0 ? (" and dp.id in " + depPartsIds_ ) : "") +
//                (request.getJobTitlesIds().size() > 0 ? (" and jt.id in " + jobTitlesIds_) : "") +
//                " and u.job_title_id=jt.id and " +
//                " u.id = up.user_id and " +
//                " p.id=up.product_id and " +
//                " dpp.product_id=p.id and " +
//                " dpp.dep_part_id=dp.id" +
//                " order by u.name,dp.name,p.name;";
//    }

    public List<AppointmentEmployee> getEmployeesList(AppointmentMainInfoForm request){

        String stringQuery = "";
        try {
            Long masterId = userRepositoryJPA.getMyMasterId();
            String myTimeZone = userRepository.getUserTimeZone();
            int companyTimeZoneId = (Integer) commonUtilites.getFieldValueFromTableById("companies", "time_zone_id", masterId, request.getCompanyId());
            String companyTimeZone = commonUtilites.getTimeZoneById(companyTimeZoneId);
            List<AppointmentEmployee> returnList = new ArrayList<>();
            Set<Long> employeesIdsList = new HashSet<>();

            if (request.getIsFree())
                employeesIdsList = getFreeEmployeeIdsList(request.getAppointmentId(), request.getCompanyId(), request.getDateFrom(), request.getTimeFrom(), request.getDateTo(), request.getTimeTo(), request.getServicesIds(), request.getDepPartsIds(), request.getJobTitlesIds(), myTimeZone, masterId);
            else
                employeesIdsList = getNonAccessibleEmployeesIdsList(request.getKindOfNoFree(), request.getAppointmentId(), request.getCompanyId(), request.getDateFrom(), request.getTimeFrom(), request.getDateTo(), request.getTimeTo(), request.getServicesIds(), request.getDepPartsIds(), request.getJobTitlesIds(), myTimeZone, masterId);


            if (employeesIdsList.size() > 0) {

                String depPartsIds_ = commonUtilites.SetOfLongToString(request.getDepPartsIds(), ",", "(", ")");
                String jobTitlesIds_ = commonUtilites.SetOfLongToString(request.getJobTitlesIds(), ",", "(", ")");
                String servicesIds_ = commonUtilites.SetOfLongToString(request.getServicesIds(), ",", "(", ")");
                String employeesIds = commonUtilites.SetOfLongToString(employeesIdsList, ",", "(", ")");

//                if (request.getIsFree())
//                stringQuery = getEmployeesListSQL(request, masterId, employeesIds, servicesIds_, depPartsIds_, jobTitlesIds_, myTimeZone, companyTimeZone);
                stringQuery="select " +
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
                " u.is_currently_employed = true and " +
                " u.id in "+employeesIds+
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
                boolean isNewUser = false;
                Set<Long> currentDepPartServicesIds = new HashSet<>();
                BreakJSON break_ = new BreakJSON();
                int i = 0; // index

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
                        appointmentEmployee.setState(request.getFree()?"free":request.getKindOfNoFree());

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
