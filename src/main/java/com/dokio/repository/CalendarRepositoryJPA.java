package com.dokio.repository;

import com.dokio.message.request.Settings.SettingsAppointmentForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.message.response.Settings.SettingsAppointmentJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.request.AppointmentsForm;
import com.dokio.message.request.AppointmentProductsTableForm;
import com.dokio.message.response.AppointmentsJSON;
import com.dokio.message.response.additional.AppointmentProductsTableJSON;
import com.dokio.message.response.additional.AppointmentUpdateReportJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.calendar.*;
import com.dokio.model.Companies;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.repository.Exceptions.DocumentAlreadyCompletedException;
import com.dokio.repository.Exceptions.DocumentAlreadyDecompletedException;
import com.dokio.repository.Exceptions.NotEnoughPermissionsException;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        List<CalendarEventJSON> events = new ArrayList<>();

        events.add(new CalendarEventJSON(1L, "2024-04-23T08:00:00.000Z","2024-04-23T10:30:00.000Z", "Стрижка Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(2L, "2024-04-23T08:30:00.000Z","2024-04-23T13:00:00.000Z", "Покраска Иванова Т.А.", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(3L, "2024-04-23T14:30:00.000Z","2024-04-23T17:00:00.000Z", "Сложная покраска Петрова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(37L, "Влад Зырянов", new CalendarColors("#000000","#B0E0E6")),"appointment")));
        events.add(new CalendarEventJSON(4L, "2024-04-23T08:00:00.000Z","2024-04-23T11:30:00.000Z", "Стрижка Ким", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(5L, "2024-04-23T10:30:00.000Z","2024-04-23T12:00:00.000Z", "Стрижка Борисюк", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(4L, "Михаил Сунцов", new CalendarColors("#000000","#FFEFD5")),"appointment")));
        events.add(new CalendarEventJSON(6L, "2024-04-23T11:30:00.000Z","2024-04-23T13:00:00.000Z", "Стрижка Холмогорова", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(36L, "Алёна Попцова", new CalendarColors("#000000","#FFC0CB")),"appointment")));
        events.add(new CalendarEventJSON(7L, "2024-04-23T10:30:00.000Z","2024-04-23T15:00:00.000Z", "Стрижка Тутти", new CalendarColors("#008000","#FDF1BA"), new Meta(new CalendarUser(298L, "Анастасия Сунцова", new CalendarColors("#000000","#B0E0E0")),"appointment")));

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
        if(securityRepositoryJPA.userHasPermissions_OR(60L, "725,726"))//(см. файл Permissions Id)
        {

            try{
                String myTimeZone = userRepository.getUserTimeZone();
                Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
                Long masterId = userRepositoryJPA.getMyMasterId();
                int companyTimeZoneId = (Integer)commonUtilites.getFieldValueFromTableById("companies", "time_zone_id", masterId, queryForm.getCompanyId());
                String companyTimeZone = commonUtilites.getTimeZoneById(companyTimeZoneId);
                DateTimeFormatter sqlQueryFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                DateTimeFormatter calendarFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateFrom = LocalDate.parse(queryForm.getDateFrom(),sqlQueryFormatter);
                LocalDate dateTo = LocalDate.parse(queryForm.getDateTo(),sqlQueryFormatter);
                // the shift of previous day can be started in the first day of query.
                // in this case to get the information about start of first break we need to get previous day
                dateFrom = dateFrom.minusDays(1);
                // the shift of current day can be finishet before the end of a day.
                // in this case to get the information about finish of the last break of last query day, we need to get a next day
                dateTo = dateTo.plusDays(1);

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

        } else return null;

    }









    private List<PointOfScedule> getPointsOfScedule(Long companyId, String dateFrom, String dateTo, Set<Long> depPartsIds, Set<Long> jobTitlesIds, String companyTimeZone, String myTimeZone, Long masterId){

        String stringQuery;

        String depPartsIds_ =  commonUtilites.SetOfLongToString(depPartsIds, ",", "(", ")");
        String jobTitlesIds_ = commonUtilites.SetOfLongToString(jobTitlesIds, ",", "(", ")");

        stringQuery =
        "   select " +
        "   sd1.day_date as day_date, " +
        "   to_timestamp(concat(to_char(sd1.day_date, 'YYYY-MM-DD'),' ',to_char(w1.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' as point_of_scedule_timestamp, " +
        "   to_char(to_timestamp(concat(to_char(sd1.day_date, 'YYYY-MM-DD'),' ',to_char(w1.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as point_of_scedule_time, " +
        "   sd1.employee_id as user_id, " +
        "   'workshift_time_from' as point_of_scedule_name, " +
        "   u1.name as user_name " +
        "   from " +
        "   scdl_scedule_day sd1 " +
        "   inner join scdl_workshift w1 on w1.scedule_day_id = sd1.id " +
        "   inner join users u1 on sd1.employee_id = u1.id " +
        "   where " +
        "   sd1.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " +
        "   coalesce(u1.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "       select product_id from scdl_user_products where master_id="+masterId+" and user_id = u1.id " +
        "       INTERSECT " +
        "       select product_id from scdl_dep_part_products where master_id="+masterId+" and dep_part_id in " + depPartsIds_ +
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd1.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' >= to_timestamp('31.03.2024'||' 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   and to_timestamp(to_char(sd1.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' <= to_timestamp('30.04.2024'||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +

        "   union all " +

        "   select " +
        "   sd2.day_date as day_date, " +
        "   case " +
        "       when w2.time_to <= w2.time_from " +
        "       then to_timestamp(concat(to_char(sd2.day_date+1, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       else to_timestamp(concat(to_char(sd2.day_date, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   end as point_of_scedule_timestamp, " +
        "   case " +
        "       when w2.time_to <= w2.time_from " +
        "       then to_char(to_timestamp(concat(to_char(sd2.day_date+1, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') " +
        "       else to_char(to_timestamp(concat(to_char(sd2.day_date, 'YYYY-MM-DD'),' ',to_char(w2.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') " +
        "   end as point_of_scedule_time, " +
        "   sd2.employee_id as user_id, " +
        "   'workshift_time_to' as point_of_scedule_name, " +
        "   u2.name as user_name " +
        "   from " +
        "   scdl_scedule_day sd2 " +
        "   inner join scdl_workshift w2 on w2.scedule_day_id = sd2.id " +
        "   inner join users u2 on sd2.employee_id = u2.id " +
        "   where " +
        "   sd2.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " +

        "   coalesce(u2.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u2.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+" and dep_part_id in " + depPartsIds_ +
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd2.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' >= to_timestamp('31.03.2024'||' 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   and to_timestamp(to_char(sd2.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' <= to_timestamp('30.04.2024'||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +

        "   union all " +

        "   select " +
        "   sd3.day_date as day_date, " +
        "   case " +
        "       when br3.time_to <= w3.time_from " +
        "       then to_timestamp(concat(to_char(sd3.day_date+1, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       else to_timestamp(concat(to_char(sd3.day_date, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       end as point_of_scedule_timestamp, " +
        "   case " +
        "       when br3.time_to <= w3.time_from " +
        "       then to_char(to_timestamp(concat(to_char(sd3.day_date+1, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') " +
        "       else to_char(to_timestamp(concat(to_char(sd3.day_date, 'YYYY-MM-DD'),' ',to_char(br3.time_to,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') " +
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
        "   sd3.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " +
        "   coalesce(u3.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u3.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+" and dep_part_id in " + depPartsIds_ +
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd3.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' >= to_timestamp('31.03.2024'||' 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   and to_timestamp(to_char(sd3.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' <= to_timestamp('30.04.2024'||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +

        "   union all " +

        "   select " +
        "   sd4.day_date as day_date, " +
        "   case " +
        "       when br4.time_from <= w4.time_from " +
        "       then to_timestamp(concat(to_char(sd4.day_date+1, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "       else to_timestamp(concat(to_char(sd4.day_date, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   end as point_of_scedule_timestamp, " +
        "   case " +
        "       when br4.time_from <= w4.time_from " +
        "       then to_char(to_timestamp(concat(to_char(sd4.day_date+1, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') " +
        "       else to_char(to_timestamp(concat(to_char(sd4.day_date, 'YYYY-MM-DD'),' ',to_char(br4.time_from,'HH24:MI')),'YYYY-MM-DD HH24:MI') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') " +
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
        "   sd4.employee_id in (select id from users where job_title_id in "+jobTitlesIds_+") and " +
        "   coalesce(u4.is_employee, false) = true and " +
        "   ( " +
        "       select count(*) from ( " +
        "           select product_id from scdl_user_products where master_id="+masterId+" and user_id = u4.id " +
        "           INTERSECT " +
        "           select product_id from scdl_dep_part_products where master_id="+masterId+" and dep_part_id in " + depPartsIds_ +
        "       ) as aaa " +
        "   )>0 " +
        "   and to_timestamp(to_char(sd4.day_date, 'DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' >= to_timestamp('31.03.2024'||' 00:00:00.000', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +
        "   and to_timestamp(to_char(sd4.day_date, 'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' <= to_timestamp('30.04.2024'||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') at time zone '"+companyTimeZone+"' at time zone '"+companyTimeZone+"' " +

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






}
