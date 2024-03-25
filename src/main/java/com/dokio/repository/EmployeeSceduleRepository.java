package com.dokio.repository;

import com.dokio.message.request.additional.EmployeeSceduleForm;
import com.dokio.message.response.additional.DepartmentsWithPartsJSON;
import com.dokio.message.response.additional.EmployeeListJSON;
import com.dokio.message.response.additional.IdAndNameJSON;
import com.dokio.message.response.additional.eployeescdl.*;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


        // Getting the list of customers
        List<EmployeeScedule> employeeList = userRepositoryJPA.getEmployeeListByDepartmentsAndJobtitles(sceduleQuery.getDepartments(), sceduleQuery.getJobtitles());


        String dateFormat=userRepositoryJPA.getMyDateFormat();

        for (EmployeeScedule employee : employeeList) {

            employee.setDays(getSceduleDaysList(employee.getId(), sceduleQuery.getDateFrom(), sceduleQuery.getDateTo(),dateFormat,masterId));

        }






//        EmployeeScedule employeeScedule = new EmployeeScedule("Mikhail Suntsov", "", "Programmist", true, null, new ArrayList<DepartmentsWithPartsJSON>(), new ArrayList<IdAndNameJSON>(), new ArrayList<SceduleDay>(),1L);

//        List<EmployeeScedule> employeeSceduleList = new ArrayList<>();

//        employeeSceduleList.add(employeeScedule);

        return employeeList;
    }


    private List<SceduleDay> getSceduleDaysList(Long userId, String dateFrom, String dateTo, String dateFormat, Long masterId){

        String stringQuery;
        stringQuery =
                        " select " +
                        " to_char(dc, '"+dateFormat+"')     as date, " +
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
                    workshift.setBreaks(getBreaksListByWorkshiftId(workshift.getId(), masterId));
                    workshift.setDepparts(getDeppartsListByWorkshiftId(workshift.getId(), masterId));
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
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSceduleDaysList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private List<Break> getBreaksListByWorkshiftId(Long workshiftId, Long masterId){

        String stringQuery =   " select " +
                        " br.id 						    as id, " +
                        " to_char(br.time_from,'HH24:MI')   as time_from, " + // time of work shift start
                        " to_char(br.time_to,'HH24:MI')     as time_to, " +   // time of work shift end
                        " coalesce(br.is_paid,false)        as is_paid, " +   // is break paid by employer
                        " coalesce(br.precent,0)            as precent " +    // integer 1-100
                        " from scdl_workshift_breaks br" +
                        " where br.workshift_id = " + workshiftId +
                        " and br.master_id = " + masterId +
                        " order by br.id ";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<Break> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                Break doc = new Break();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setTime_from((String)                       obj[1]);
                doc.setTime_to((String)                         obj[2]);
                doc.setPaid((Boolean)                           obj[3]);
                doc.setPrecent((Integer)                        obj[4]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getBreaksListByWorkshiftId. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private List<IdAndNameJSON> getDeppartsListByWorkshiftId(Long workshiftId, Long masterId){

        String stringQuery =   " select " +
                " wd.deppart_id 				    as id, " +          // id of department part
                " CONCAT(d.name,', ', dp.name)      as name " +        //  name of department and department's part
                " from  scdl_workshift_deppart wd" +
                " inner join scdl_dep_parts dp on dp.id = wd.deppart_id " +
                " inner join departments d on d.id = dp.department_id " +
                " where wd.workshift_id = " + workshiftId +
                " and   wd.master_id = " + masterId +
                " order by d.name, dp.name ";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<IdAndNameJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                IdAndNameJSON doc = new IdAndNameJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setName((String)                            obj[1]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getDeppartsListByWorkshiftId. SQL query:" + stringQuery, e);
            return null;
        }
    }

}
