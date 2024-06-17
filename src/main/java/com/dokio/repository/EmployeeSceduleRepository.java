package com.dokio.repository;

import com.dokio.message.request.additional.EmployeeSceduleForm;
import com.dokio.message.response.additional.eployeescdl.*;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Repository
public class EmployeeSceduleRepository {

    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CommonUtilites commonUtilites;


    Logger logger = Logger.getLogger("EmployeeSceduleRepository");


    @PersistenceContext
    private EntityManager entityManager;


    public List<EmployeeScedule> getEmployeesWorkSchedule(EmployeeSceduleForm sceduleQuery){
        Long masterId = userRepositoryJPA.getMyMasterId();
        List<EmployeeScedule> employeeList = userRepositoryJPA.getEmployeeListByDepartmentsAndJobtitles(sceduleQuery.getDepartments(), sceduleQuery.getJobtitles(), masterId);
//        String dateFormat=userRepositoryJPA.getMyDateFormat();
        for (EmployeeScedule employee : employeeList) {
            employee.setDays(getSceduleDaysList(employee.getId(), sceduleQuery.getDateFrom(), sceduleQuery.getDateTo(),masterId));
        }
        return employeeList;
    }


    private List<SceduleDay> getSceduleDaysList(Long userId, String dateFrom, String dateTo, Long masterId){

        String stringQuery;
        stringQuery =
                        " select " +
                        " to_char(dc, 'DD.MM.YYYY')         as date, " +                  // internal format of date
                        " dc                                as date_created_sort, " +
                        " ssd.id 						    as scedule_day_id, " +
                        " ws.id 						    as workshift_id, " +
                        " to_char(ws.time_from,'HH24:MI')   as workshift_time_from, " +
                        " to_char(ws.time_to,'HH24:MI')     as workshift_time_to, " +
                        " vc.id							    as vacation_id, " +
                        " coalesce(vc.name,'')              as vacation_name, " +
                        " coalesce(vc.is_paid,false)        as vacation_is_paid, " +
                        " coalesce(vc.payment_per_day,0)    as vacation_payment_per_day " +
                        " from " +
                        " generate_series(timestamp '"+dateFrom+"',  timestamp '"+dateTo+"'  , interval  '1 day') as dc " +
                        " left outer join scdl_scedule_day ssd on ssd.employee_id = "+userId+" and to_char(ssd.day_date, 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') " +
                        " left outer join scdl_workshift ws on ws.scedule_day_id=ssd.id " +
                        " left outer join scdl_vacation vc on vc.scedule_day_id=ssd.id " +
                        " order by date_created_sort ";


        try {

            if (!commonUtilites.isDateValid(dateFrom) || !commonUtilites.isDateValid(dateTo))
                throw new IllegalArgumentException("Invalid query parameters");

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<SceduleDay> returnList = new ArrayList<>();
            Set<Long> workshiftIds = new HashSet<>();
            for (Object[] obj : queryList) {
                SceduleDay doc = new SceduleDay();
                doc.setDate((String)            obj[0]);
                doc.setName((String)            obj[0]);
                doc.setId(obj[2] != null ? Long.parseLong(obj[2].toString()) : null);

                if(!Objects.isNull(obj[3])){ // if there is a work shift in this day
                    Workshift workshift = new Workshift();
                    workshift.setId(Long.parseLong(                      obj[3].toString()));
                    workshift.setTime_from((String)                      obj[4]);
                    workshift.setTime_to((String)                        obj[5]);
                    // This commented approach is very slow, because for each day need to do two SQL queries
                    // Below I make only 2 queries, and distribute their results according to days data

//                    workshift.setBreaks(getBreaksListByWorkshiftId(workshift.getId(), masterId));
//                    workshift.setDepparts(getDeppartsIdsListByWorkshiftId(workshift.getId(), masterId));

                    workshiftIds.add(workshift.getId());

                    doc.setWorkshift(workshift);
                }

                if(!Objects.isNull(obj[6])){ // if there is a vacation in this day
                    Vacation vacation = new Vacation();
                    vacation.setId(Long.parseLong(                       obj[6].toString()));
                    vacation.setName((String)                            obj[7]);
                    vacation.setIs_paid((Boolean)                        obj[8]);
                    vacation.setPayment_per_day((BigDecimal)             obj[9]);
                    doc.setVacation(vacation);
                }
                returnList.add(doc);
            }


            if(workshiftIds.size()>0) {
                List<Break_> breaks = getBreaksListBySetWorkshiftIds(workshiftIds, masterId);
                List<Depparts_> depparts = getDeppartsIdsListBySetWorkshiftIds(workshiftIds, masterId);
                for (SceduleDay sceduleDay : returnList) {
                    // adding workshift's breaks to all existed workshifts
                    if (!Objects.isNull(sceduleDay.getWorkshift())) {
                        if (breaks.size() > 0){
                            List<Break> workshiftBreaks = new ArrayList<>();
                            for (Break_ break_ : breaks) {
                                if (sceduleDay.getWorkshift().getId().equals(break_.getWorkshiftId()))
                                    workshiftBreaks.add(new Break(break_.getId(), break_.getTime_from(), break_.getTime_to(), break_.getPaid(), break_.getPrecent()));
                            }
                            sceduleDay.getWorkshift().setBreaks(workshiftBreaks);
                        } else sceduleDay.getWorkshift().setBreaks(new ArrayList<>());// for breaks be not null but [] in JSON
                    }
                    // adding workshift's department parts
                    if (!Objects.isNull(sceduleDay.getWorkshift())) {
                        List<Long> workshiftDepparts = new ArrayList<>();
                        for (Depparts_ deppart_ : depparts) {
                            if (sceduleDay.getWorkshift().getId().equals(deppart_.getWorkshiftId()))
                                workshiftDepparts.add(deppart_.getId());
                        }
                        sceduleDay.getWorkshift().setDepparts(workshiftDepparts);
                    }
                }
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSceduleDaysList. SQL query:" + stringQuery, e);
            return null;
        }
    }

//    private List<Break> getBreaksListByWorkshiftId(Long workshiftId, Long masterId){
////
////        String stringQuery =   " select " +
////                        " br.id 						    as id, " +
////                        " to_char(br.time_from,'HH24:MI')   as time_from, " + // time of work shift start
////                        " to_char(br.time_to,'HH24:MI')     as time_to, " +   // time of work shift end
////                        " coalesce(br.is_paid,false)        as is_paid, " +   // is break paid by employer
////                        " coalesce(br.precent,0)            as precent " +    // integer 1-100
////                        " from scdl_workshift_breaks br" +
////                        " where br.workshift_id = " + workshiftId +
////                        " and br.master_id = " + masterId +
////                        " order by br.id ";
////        try {
////            Query query = entityManager.createNativeQuery(stringQuery);
////            List<Object[]> queryList = query.getResultList();
////            List<Break> returnList = new ArrayList<>();
////            for (Object[] obj : queryList) {
////                Break doc = new Break();
////                doc.setId(Long.parseLong(                       obj[0].toString()));
////                doc.setTime_from((String)                       obj[1]);
////                doc.setTime_to((String)                         obj[2]);
////                doc.setPaid((Boolean)                           obj[3]);
////                doc.setPrecent((Integer)                        obj[4]);
////                returnList.add(doc);
////            }
////            return returnList;
////        } catch (Exception e) {
////            e.printStackTrace();
////            logger.error("Exception in method getBreaksListByWorkshiftId. SQL query:" + stringQuery, e);
////            return null;
////        }
////    }

    private List<Break_> getBreaksListBySetWorkshiftIds(Set<Long> workshiftIds, Long masterId) throws Exception {

        String stringQuery =   " select " +
                " br.id 						    as id, " +
                " to_char(br.time_from,'HH24:MI')   as time_from, " + // time of work shift start
                " to_char(br.time_to,'HH24:MI')     as time_to, " +   // time of work shift end
                " coalesce(br.is_paid,false)        as is_paid, " +   // is break paid by employer
                " coalesce(br.precent,0)            as precent, " +    // integer 1-100
                " br.workshift_id                   as workshift_id " +
                " from scdl_workshift_breaks br" +
                " where " +
                " br.master_id = " + masterId +
                " and br.workshift_id in " + commonUtilites.SetOfLongToString(workshiftIds, ",","(",")") +
                " order by br.workshift_id, br.id ";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<Break_> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                Break_ doc = new Break_();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setTime_from((String)                       obj[1]);
                doc.setTime_to((String)                         obj[2]);
                doc.setPaid((Boolean)                           obj[3]);
                doc.setPrecent((Integer)                        obj[4]);
                doc.setWorkshiftId(Long.parseLong(              obj[5].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getBreaksListBySetWorkshiftIds. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private List<Depparts_> getDeppartsIdsListBySetWorkshiftIds(Set<Long> workshiftIds, Long masterId) throws Exception {

        String stringQuery =   " select " +
                " wd.deppart_id 				    as id, " +          // id of department part
                " wd.workshift_id                   as workshift_id " +
                " from  scdl_workshift_deppart wd " +
                " where " +
                " wd.master_id = " + masterId +
                " and wd.workshift_id in " + commonUtilites.SetOfLongToString(workshiftIds, ",","(",")");
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<Depparts_> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                Depparts_ doc = new Depparts_();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setWorkshiftId(Long.parseLong(              obj[1].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getDeppartsIdsListBySetWorkshiftIds. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

//    private List<Long> getDeppartsIdsListByWorkshiftId(Long workshiftId, Long masterId){
//
//        String stringQuery =   " select " +
//                " wd.deppart_id 				    as id " +          // id of department part
//                " from  scdl_workshift_deppart wd " +
//                " where wd.workshift_id = " + workshiftId +
//                " and   wd.master_id = " + masterId;
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<BigInteger> queryList = query.getResultList();
//            List<Long> returnList = new ArrayList<>();
//            for (BigInteger obj : queryList) {
//                returnList.add(obj.longValue());
//            }
//            return returnList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getDeppartsIdsListByWorkshiftId. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }
//    private List<IdAndNameJSON> getDeppartsListByWorkshiftId(Long workshiftId, Long masterId){
//
//        String stringQuery =   " select " +
//                " wd.deppart_id 				    as id, " +          // id of department part
//                " CONCAT(d.name,', ', dp.name)      as name " +        //  name of department and department's part
//                " from  scdl_workshift_deppart wd" +
//                " inner join scdl_dep_parts dp on dp.id = wd.deppart_id " +
//                " inner join departments d on d.id = dp.department_id " +
//                " where wd.workshift_id = " + workshiftId +
//                " and   wd.master_id = " + masterId +
//                " order by d.name, dp.name ";
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Object[]> queryList = query.getResultList();
//            List<IdAndNameJSON> returnList = new ArrayList<>();
//            for (Object[] obj : queryList) {
//                IdAndNameJSON doc = new IdAndNameJSON();
//                doc.setId(Long.parseLong(                       obj[0].toString()));
//                doc.setName((String)                            obj[1]);
//                returnList.add(doc);
//            }
//            return returnList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getDeppartsListByWorkshiftId. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }




    @Transactional (propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, RuntimeException.class})
    public Integer updateEmployeeWorkSchedule(List<EmployeeScedule> employeesSceduleList){

        Long masterId = userRepositoryJPA.getMyMasterId();

        try {

            for (EmployeeScedule employeeScedule : employeesSceduleList) {

                for (SceduleDay day : employeeScedule.getDays()) {

                    if (!Objects.isNull(day.getIs_changed()) && day.getIs_changed()) {


                        if (!Objects.isNull(day.getWorkshift()) || !Objects.isNull(day.getVacation())) {
                            // day can be created or updated

                            addDay(day, masterId, employeeScedule.getId());
                            Long dayId = getSceduleDayId(employeeScedule.getId(), masterId, day.getDate());


                            if (!Objects.isNull(day.getWorkshift())) {
                                // saving work shift


                                addWorkshift(day.getWorkshift(), masterId, dayId);
                                Long workshiftId = getWorkshiftId(dayId, masterId);
                                updateWorkshifBreaks(day.getWorkshift().getBreaks(), masterId, workshiftId);
                                updateWorkshifDepparts(day.getWorkshift().getDepparts(), masterId, workshiftId);
                                deleteUnactualWorkshiftDepparts(day.getWorkshift().getDepparts(), masterId, workshiftId);



                            } else
                                if(!Objects.isNull(day.getId())) // id can be null if day was new-created on a front-end side, and wasn't added into database before
                                    deleteWorkshift(day.getId(),masterId);

                            if (!Objects.isNull(day.getVacation())) {
                                // saving vacation


                                addVacation(day.getVacation(), masterId, dayId);



                            } else
                                if(!Objects.isNull(day.getId())) // id can be null if day was new-created on a front-end side, and wasn't added into database before
                                    deleteVacation(day.getId(),masterId);

                        } else
                            if(!Objects.isNull(day.getId()))
                            // day can be deleted from database because it is not contains work shift or vacation - it is empty and not useful
                                deleteDay(day, masterId, employeeScedule.getId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method updateEmployeeWorkSchedule. ", e);
            return null;
        }
        return 1;
    }

    private void addDay(SceduleDay day, Long masterId, Long employeeId) throws Exception {

        String stringQuery = "";
        if (!commonUtilites.isDateValid(day.getDate()))
            throw new IllegalArgumentException("Invalid query parameters");

            stringQuery = stringQuery+
                    "   insert into scdl_scedule_day (" +
                    "   master_id," +
                    "   employee_id," +
                    "   day_date" +
                    "   ) values (" +
                    masterId+", "+
                    employeeId+", "+
                    "to_date('"+day.getDate()+"', 'DD.MM.YYYY')" +
                    "   ) ON CONFLICT ON CONSTRAINT day_type_for_employee_is_uq " +// "upsert"
                    "   DO NOTHING; ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method addDay. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void addWorkshift(Workshift workshift, Long masterId, Long dayId) throws Exception {

        String stringQuery = "";

        if (!commonUtilites.isTimeValid(workshift.getTime_from()) || !commonUtilites.isTimeValid(workshift.getTime_to()))
            throw new IllegalArgumentException("Invalid query parameters (time):"+workshift.getTime_from()+", "+workshift.getTime_to());


        stringQuery = stringQuery+
                "   insert into scdl_workshift (" +
                "   master_id," +
                "   scedule_day_id," +
                "   time_from," +
                "   time_to" +
                "   ) values (" +
                masterId+", "+
                dayId +", "+
                "to_timestamp('"+workshift.getTime_from()+"', 'HH24:MI')," +
                "to_timestamp('"+workshift.getTime_to()+"',   'HH24:MI')" +
                "   ) ON CONFLICT ON CONSTRAINT workshift_scedule_day_uq " +// "upsert"
                " DO update set " +
                " time_from = to_timestamp('"+workshift.getTime_from()+"', 'HH24:MI')," +
                " time_to =   to_timestamp('"+workshift.getTime_to()+"',   'HH24:MI');" ;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method addWorkshift. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void updateWorkshifBreaks(List<Break> breaks, Long masterId, Long workshiftId) throws Exception {
        String stringQuery = "";
        // delete old breaks
        stringQuery =
                "   delete from scdl_workshift_breaks" +
                "   where " +
                "   master_id = " + masterId + " and " +
                "   workshift_id = " + workshiftId +";";
        try{
            if (!Objects.isNull(breaks) && breaks.size() > 0){
                for(Break break_ : breaks){
                    if (!commonUtilites.isTimeValid(break_.getTime_from()) || !commonUtilites.isTimeValid(break_.getTime_to()))
                        throw new IllegalArgumentException("Invalid query parameters (time):"+break_.getTime_from()+", "+break_.getTime_to());
                    stringQuery = stringQuery +
                        "   insert into scdl_workshift_breaks (" +
                        "   master_id," +
                        "   workshift_id," +
                        "   time_from," +
                        "   time_to," +
                        "   is_paid," +
                        "   precent" +
                        ") values (" +
                            masterId + ", " +
                            workshiftId + ", " +
                            "to_timestamp('"+break_.getTime_from()+"', 'HH24:MI'), " +
                            "to_timestamp('"+break_.getTime_to()+"',   'HH24:MI'), " +
                            break_.getPaid() + ", " +
                            break_.getPrecent() +
                        "); ";
                }
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method updateWorkshifBreaks. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void updateWorkshifDepparts(List<Long> depparts, Long masterId, Long workshiftId) throws Exception {

        String stringQuery = "";
        try{
            for(Long deppartId:depparts){

                stringQuery = stringQuery + "   insert into scdl_workshift_deppart (" +
                        "   master_id," +
                        "   workshift_id," +
                        "   deppart_id" +
                        "   ) values (" +
                            masterId + ", "+
                            workshiftId + ", " +
                            deppartId +
                        "   ) ON CONFLICT ON CONSTRAINT scdl_workshift_deppart_uq " +// "upsert"
                        "   DO NOTHING; ";
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method updateWorkshifDepparts. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void deleteUnactualWorkshiftDepparts(List<Long> depparts, Long masterId, Long workshiftId) throws Exception {
        String stringQuery = "";
        try{

                stringQuery = stringQuery + "   delete from scdl_workshift_deppart " +
                        "   where " +
                        "   master_id = " + masterId + " and " +
                        "   workshift_id = " + workshiftId + " and " +
                        "   deppart_id not in " + commonUtilites.ListOfLongToString(depparts, ",", "(", ")");

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method deleteUnactualWorkshiftDepparts. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private Long getSceduleDayId(Long employeeId, Long masterId, String dayDate) throws Exception {
        String stringQuery="";
        try{
            stringQuery="(select id from scdl_scedule_day where " +
                "               master_id = "+masterId+" and " +
                "               employee_id = "+employeeId+" and " +
                "               day_date = to_date('"+dayDate+"','DD.MM.YYYY')" +
                "           )";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            logger.error("Exception in method getSceduleDayId. SQL: " + stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private Long getWorkshiftId(Long sceduleDayId, Long masterId) throws Exception {
        String stringQuery="";
        try{
            stringQuery="select id from scdl_workshift where " +
                    "               master_id = "+masterId+" and " +
                    "               scedule_day_id = "+sceduleDayId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            logger.error("Exception in method getWorkshiftId. SQL: " + stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private void deleteDay(SceduleDay day, Long masterId, Long employeeId) throws Exception {

        String stringQuery = "";
        if (!commonUtilites.isDateValid(day.getDate()))
            throw new IllegalArgumentException("Invalid query parameters");

        stringQuery = stringQuery+
                "   delete from scdl_scedule_day " +
                "   where " +
                "   master_id = " + masterId + " and " +
                "   employee_id = " + employeeId + " and " +
                "   day_date = to_date('"+day.getDate()+"', 'DD.MM.YYYY')";
        try{

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method deleteDay. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void deleteWorkshift(Long dayId, Long masterId) throws Exception {
        String stringQuery =
                "   delete from scdl_workshift " +
                "   where " +
                "   master_id = " + masterId + " and " +
                "   scedule_day_id = " + dayId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method deleteWorkshift. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void deleteVacation(Long dayId, Long masterId) throws Exception {
        String stringQuery =
                "   delete from scdl_vacation " +
                        "   where " +
                        "   master_id = " + masterId + " and " +
                        "   scedule_day_id = " + dayId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method deleteVacation. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    private void addVacation(Vacation vacation, Long masterId, Long dayId) throws Exception {

        String stringQuery =
                "   insert into scdl_vacation (" +
                "   master_id," +
                "   scedule_day_id," +
                "   name," +
                "   is_paid," +
                "   payment_per_day" +
                "   ) values (" +
                masterId+", "+
                dayId +", "+
                "   :name," +
                vacation.getIs_paid() +", "+
                vacation.getPayment_per_day() +
                "   ) ON CONFLICT ON CONSTRAINT vacation_scedule_day_uq " +// "upsert"
                "   DO update set " +
                "   is_paid = "           + vacation.getIs_paid() + "," +
                "   payment_per_day = "   + vacation.getPayment_per_day()  + "," +
                "   name = :name";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name", vacation.getName());
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method addVacation. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }



}
