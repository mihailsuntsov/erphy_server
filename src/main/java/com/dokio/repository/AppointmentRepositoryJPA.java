package com.dokio.repository;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsAppointmentForm;
import com.dokio.message.request.additional.AppointmentCustomer;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.calendar.CalendarEventsQueryForm;
import com.dokio.message.response.OrderinJSON;
import com.dokio.message.response.PaymentinJSON;
import com.dokio.message.response.Settings.SettingsAppointmentJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.AppointmentsJSON;
import com.dokio.message.response.ShipmentJSON;
import com.dokio.message.response.additional.*;
import com.dokio.message.response.additional.appointment.AppointmentChildDocsJSON;
import com.dokio.message.response.additional.appointment.AppointmentService;
import com.dokio.message.response.additional.appointment.DepartmentPartWithResourcesIds;
import com.dokio.message.response.additional.appointment.ResourceOfDepartmentPart;
import com.dokio.message.response.additional.calendar.CalendarEventJSON;
import com.dokio.message.response.additional.calendar.ItemResource;
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
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.UUID;
@Repository
public class AppointmentRepositoryJPA {

    Logger logger = Logger.getLogger("AppointmentRepositoryJPA");

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
    @Autowired
    private ShipmentRepositoryJPA shipmentRepository;
    @Autowired
    private PaymentinRepositoryJPA paymentinRepository;
    @Autowired
    private OrderinRepositoryJPA orderinRepository;
    @Autowired
    private CalendarRepositoryJPA calendarRepository;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","dep_part","name","owner","status_name","sum_price","date_time_start","date_time_start_sort","company","department","creator","date_time_created_sort","shipment_date_sort","description","is_completed","product_count")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************

    public List<AppointmentsJSON> getAppointmentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, long companyId, long departmentId, Set<Integer> filterOptionsIds, long appointmentId, long customerId) {
        if(securityRepositoryJPA.userHasPermissions_OR(59L, "708,709,710,711"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24';
            boolean needToSetParameter_MyDepthsIds = false;
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.company_id as company_id, " +
                    "           depp.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from scdl_appointments_product where appointment_id=p.id),0) as sum_price, " +
                    "           (select count(*) from scdl_appointments_product ip where ip.appointment_id=p.id) as product_count," + //подсчет кол-ва услуг или товаров
                    "           to_char(p.starts_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',   'DD.MM.YYYY') as date_start, " +
                    "           to_char(p.starts_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',   'HH24:MI')    as time_start, " +
                    "           to_char(p.ends_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',     'DD.MM.YYYY') as date_end, " +
                    "           to_char(p.ends_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',     'HH24:MI')    as time_end, " +
                    "           uo.name as owner, " +
                    "           p.name as name," +
                    "           depp.name as dep_part, " +
                    "           to_char(p.starts_at_time at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0', '"+dateFormat+timeFormat+"') as date_time_start, " +
                    "           p.starts_at_time as date_time_start_sort " +
                    "           from scdl_appointments p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN scdl_dep_parts depp ON p.dep_part_id=depp.id " +
                    "           INNER JOIN departments dp ON depp.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uo ON p.owner_id=uo.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

//          if this table requesting from the window of Appointment document by customer:
            if(customerId>0L) stringQuery = stringQuery + " and p.id != "+appointmentId+" and p.id in (select appointment_id from scdl_appointments_product where cagent_id = "+customerId+") ";

            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and depp.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and depp.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
//                        " to_char(p.shipment_date, 'DD.MM.YYYY') = CONCAT('%',:sg,'%') or "+
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(stat.name)like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(depp.name)like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uo.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and depp.department_id=" + departmentId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<AppointmentsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                AppointmentsJSON doc=new AppointmentsJSON();
                doc.setId(Long.parseLong(                     obj[0].toString()));
                doc.setCreator((String)                       obj[1]);
                doc.setChanger((String)                       obj[2]);
                doc.setCompany_id(Long.parseLong(             obj[3].toString()));
                doc.setDepartment_id(Long.parseLong(          obj[4].toString()));
                doc.setDepartment((String)                    obj[5]);
                doc.setDoc_number(Long.parseLong(             obj[6].toString()));
                doc.setCompany((String)                       obj[7]);
                doc.setDate_time_created((String)             obj[8]);
                doc.setDate_time_changed((String)             obj[9]);
                doc.setDescription((String)                   obj[10]);
                doc.setIs_completed((Boolean)                 obj[11]);
                doc.setStatus_id(obj[14]!=null?Long.parseLong(obj[14].toString()):null);
                doc.setStatus_name((String)                   obj[15]);
                doc.setStatus_color((String)                  obj[16]);
                doc.setStatus_description((String)            obj[17]);
                doc.setSum_price((BigDecimal)                 obj[18]);
                doc.setProduct_count(Long.parseLong(          obj[19].toString()));
                doc.setDate_start((String)                    obj[20]);
                doc.setDate_end((String)                      obj[21]);
                doc.setTime_start((String)                    obj[22]);
                doc.setTime_end((String)                      obj[23]);
                doc.setOwner((String)                         obj[24]);
                doc.setName((String)                          obj[25]);
                doc.setDep_part((String)                      obj[26]);
                doc.setDate_time_start((String)               obj[27]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getAppointmentsSize(String searchString, long companyId, long departmentId, Set<Integer> filterOptionsIds, long appointmentId, long customerId) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from scdl_appointments p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN scdl_dep_parts depp ON p.dep_part_id=depp.id " +
                "           INNER JOIN departments dp ON depp.department_id=dp.id " +
                "           LEFT OUTER JOIN users uo ON p.owner_id=uo.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

//      if this table requesting from the window of Appointment document by customer:
        if(customerId>0L) stringQuery = stringQuery + " and p.id != "+appointmentId+" and p.id in (select appointment_id from scdl_appointments_product where cagent_id = "+customerId+") ";

        if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and depp.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and depp.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
//                    " to_char(p.shipment_date, 'DD.MM.YYYY') = CONCAT('%',:sg,'%') or "+
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(stat.name)like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(depp.name)like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uo.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and depp.department_id =" + departmentId;
        }

        Query query = entityManager.createNativeQuery(stringQuery);

        if (searchString != null && !searchString.isEmpty())
        {query.setParameter("sg", searchString);}

        if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
        {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

        return query.getResultList().size();
    }


//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public AppointmentsJSON getAppointmentsValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(59L, "708,709,710,711"))//см. _Permissions Id.txt
        {
            String stringQuery = "";
            try {
                UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
                String myTimeZone = userSettings.getTime_zone();
                String dateFormat = userSettings.getDateFormat();
                String timeFormat = (userSettings.getTimeFormat().equals("12") ? " HH12:MI AM" : " HH24:MI"); // '12' or '24';
                boolean needToSetParameter_MyDepthsIds = false;
                Long masterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

                stringQuery =
                        "           select " +
                                "           p.id as doc_id, " +
                                "           us.name as doc_creator, " +
                                "           uc.name as doc_changer, " +
                                "           uo.name as doc_owner, " +
                                "           p.company_id as company_id, " +
                                "           dp.id as doc_department_id, " +
                                "           p.dep_part_id as doc_dep_part_id, " +
                                "           p.doc_number as doc_number, " +
                                "           cmp.name as company, " +
                                "           to_char(p.date_time_created at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0', '" + dateFormat + timeFormat + "') as date_time_created, " +
                                "           to_char(p.date_time_changed at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0', '" + dateFormat + timeFormat + "') as date_time_changed, " +
                                "           p.description as description, " +
                                "           coalesce(p.is_completed,false) as is_completed, " +
                                "           dprts.name as department_part, " +
                                "           coalesce(p.nds,false) as nds, " +
                                "           coalesce(p.nds_included,false) as nds_included, " +
                                "           p.status_id as status_id, " +
                                "           stat.name as status_name, " +
                                "           stat.color as status_color, " +
                                "           stat.description as status_description, " +
                                "           p.name as appointment_name, " +
                                "           p.uid as uid, " +
                                "           to_char(p.starts_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',   'DD.MM.YYYY') as date_start, " +
                                "           to_char(p.starts_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',   'HH24:MI')    as time_start, " +
                                "           to_char(p.ends_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',     'DD.MM.YYYY') as date_end, " +
                                "           to_char(p.ends_at_time at time zone '" + myTimeZone + "' at time zone 'Etc/GMT+0',     'HH24:MI')    as time_end, " +
                                "           concat(to_char(p.starts_at_time,'YYYY-MM-DD'), 'T', to_char(p.starts_at_time,'HH24:MI:SS.MS'), 'Z') as calendar_date_start," +
                                "           concat(to_char(p.ends_at_time,'YYYY-MM-DD'), 'T', to_char(p.ends_at_time,'HH24:MI:SS.MS'), 'Z') as calendar_date_end," +
                                "           jt.id as jobtitle_id, " +
                                "           jt.name as jobtitle," +
                                "           ue.id as employee_id," +
                                "           ue.name as employee_name," +
                                "           dp.name as department_name," +
                                "           uo.id as owner_id" +
                                "           from scdl_appointments p " +
                                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                                "           INNER JOIN scdl_dep_parts dprts ON p.dep_part_id=dprts.id " +
                                "           INNER JOIN departments dp ON dprts.department_id=dp.id " +
                                "           INNER JOIN users us ON p.creator_id=us.id " +
                                "           INNER JOIN users uo ON p.owner_id=uo.id " +
                                "           LEFT OUTER JOIN users ue ON p.employee_id=ue.id " +
                                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                                "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                                "           LEFT OUTER JOIN sprav_jobtitles jt ON ue.job_title_id=jt.id" +
                                "           where  p.master_id=" + masterId +
                                "           and p.id= " + id;

                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
                {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
                    {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                        if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                        {//остается только на свои документы
                            stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId() + " and p.department_id in :myDepthsIds and p.creator_id =" + userRepositoryJPA.getMyId();
                            needToSetParameter_MyDepthsIds = true;
                        } else {
                            stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId() + " and p.department_id in :myDepthsIds";
                            needToSetParameter_MyDepthsIds = true;
                        }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                    } else
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
                }

                Query query = entityManager.createNativeQuery(stringQuery);

                if (needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {
                    query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
                }

                List<Object[]> queryList = query.getResultList();

                AppointmentsJSON returnObj = new AppointmentsJSON();

                if(queryList.size()>0) {

                    returnObj.setId(Long.parseLong(                         queryList.get(0)[0].toString()));
                    returnObj.setCreator((String)                           queryList.get(0)[1]);
                    returnObj.setChanger((String)                           queryList.get(0)[2]);
                    returnObj.setOwner((String)                             queryList.get(0)[3]);
                    returnObj.setCompany_id(Long.parseLong(                 queryList.get(0)[4].toString()));
                    returnObj.setDepartment_id(Long.parseLong(              queryList.get(0)[5].toString()));
                    returnObj.setDep_part_id(Long.parseLong(                queryList.get(0)[6].toString()));
                    returnObj.setDoc_number(Long.parseLong(                 queryList.get(0)[7].toString()));
                    returnObj.setCompany((String)                           queryList.get(0)[8]);
                    returnObj.setDate_time_created((String)                 queryList.get(0)[9]);
                    returnObj.setDate_time_changed((String)                 queryList.get(0)[10]);
                    returnObj.setDescription((String)                       queryList.get(0)[11]);
                    returnObj.setIs_completed((Boolean)                     queryList.get(0)[12]);
                    returnObj.setDep_part((String)                          queryList.get(0)[11]);
                    returnObj.setNds((Boolean)                              queryList.get(0)[14]);
                    returnObj.setNds_included((Boolean)                     queryList.get(0)[15]);
                    returnObj.setStatus_id(                                 queryList.get(0)[16] != null ? Long.parseLong(queryList.get(0)[16].toString()) : null);
                    returnObj.setStatus_name((String)                       queryList.get(0)[17]);
                    returnObj.setStatus_color((String)                      queryList.get(0)[18]);
                    returnObj.setStatus_description((String)                queryList.get(0)[19]);
                    returnObj.setName((String)                              queryList.get(0)[20]);
                    returnObj.setUid((String)                               queryList.get(0)[21]);
                    returnObj.setDate_start((String)                        queryList.get(0)[22]);
                    returnObj.setTime_start((String)                        queryList.get(0)[23]);
                    returnObj.setDate_end((String)                          queryList.get(0)[24]);
                    returnObj.setTime_end((String)                          queryList.get(0)[25]);
                    returnObj.setCalendar_date_time_start((String)          queryList.get(0)[26]);
                    returnObj.setCalendar_date_time_end((String)            queryList.get(0)[27]);
                    returnObj.setJobtitle_id(                               queryList.get(0)[28] != null ? Long.parseLong(queryList.get(0)[28].toString()) : null);
                    returnObj.setJobtitle((String)                          queryList.get(0)[29]);
                    returnObj.setEmployeeId(                                queryList.get(0)[30] != null ? Long.parseLong(queryList.get(0)[30].toString()) : null);
                    returnObj.setEmployeeName((String)                      queryList.get(0)[31]);
                    returnObj.setDepartment((String)                        queryList.get(0)[32]);
                    returnObj.setOwner_id(Long.parseLong(                   queryList.get(0)[33].toString()));

                    AppointmentMainInfoForm reqest = new AppointmentMainInfoForm(
                            returnObj.getId(),
                            returnObj.getCompany_id(),
                            returnObj.getDate_start(),
                            returnObj.getTime_start(),
                            returnObj.getDate_end(),
                            returnObj.getTime_end()
                    );
                    returnObj.setCustomersTable(                            getAppointmentCustomersTable(returnObj.getId(), masterId));
                    List<AppointmentService> services = getAppointmentServicesList(reqest, masterId, myTimeZone);
                    // since we use on frontend row_id instead of id for customers, here need to find and set row_id
                    for(AppointmentService service : services){
                        service.setCagent_row_id(getCustomerRowIdByCustomerId(returnObj.getCustomersTable(),service.getCagent_id()));
                    }
                    returnObj.setAppointmentsProductTable(                 services);
                }
                return returnObj;

            } catch (Exception e) {
                logger.error("Exception in method getAppointmentsValuesById. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    private Integer getCustomerRowIdByCustomerId(List<AppointmentCustomer> customers, Long customerId) throws Exception {
        for(AppointmentCustomer customer : customers){
            if(customer.getId().equals(customerId))
                return customer.getRow_id();
        }
        logger.error("Exception in method getCustomerRowIdByCustomerId. Can't find customer with id = " + customerId);
        throw new Exception("Exception in method getCustomerRowIdByCustomerId. Can't find customer with id = " + customerId);
    }

    private List<AppointmentCustomer> getAppointmentCustomersTable(Long appointmentId, Long masterId) throws Exception {
        String stringQuery =
                " select " +
                " id," +
                " name," +
                " email," +
                " telephone " +
                " from cagents " +
                " where id in (" +
                    "select cagent_id from scdl_appointments_product where appointment_id = " + appointmentId + " and master_id = " + masterId +
                ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            int row_id = 0;
            List<AppointmentCustomer> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                AppointmentCustomer doc = new AppointmentCustomer();
                doc.setId(Long.parseLong(               obj[0].toString()));
                doc.setName((String)                    obj[1]);
                doc.setEmail((String)                   obj[2]);
                doc.setTelephone((String)               obj[3]);
                doc.setRow_id(row_id);
                returnList.add(doc);
                row_id++;
            }
            return returnList;
        }
        catch (Exception e) {
            logger.error("Exception in method getAppointmentCustomersTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public AppointmentUpdateReportJSON insertAppointment(AppointmentsForm request) {
        if(commonUtilites.isDocumentUidUnical(request.getUid(), "scdl_appointments"))
        {
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();
            String myTimeZone = userRepository.getUserTimeZone();
            Long masterId = userRepositoryJPA.getMyMasterId();
            Long departmentId=((BigInteger)commonUtilites.getFieldValueFromTableById("scdl_dep_parts","department_id",masterId,request.getDepartment_part_id())).longValue();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            boolean itIsMyDepartment = myDepartmentsIds.contains(departmentId);
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            AppointmentUpdateReportJSON updateResults = new AppointmentUpdateReportJSON();// отчет о создании
            try{
                if(Objects.isNull(departmentId)) throw new Exception("Department part with id = "+request.getDepartment_part_id()+" is not belongs to master Id = "+masterId);
                if (//если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(59L, "705")) ||
                    //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                    (securityRepositoryJPA.userHasPermissions_OR(59L, "706") && myCompanyId.equals(request.getCompany_id())) ||
                    //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                    (securityRepositoryJPA.userHasPermissions_OR(59L, "707") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment))
                {
                    String stringQuery;
                    Long myId = userRepository.getUserId();
                    Long newDocId;
                    Long doc_number;//номер документа( = номер заказа)

                    //генерируем номер документа, если его (номера) нет
                    if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                        doc_number=Long.valueOf(request.getDoc_number());
                    } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"scdl_appointments");

                    commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), masterId);
//                    commonUtilites.idBelongsMyMaster("departments", request.getDepartment_part_id(), myMasterId);
                    commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), masterId);

                    String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                    if (!commonUtilites.isTimeValid(request.getTime_start()) || !commonUtilites.isTimeValid(request.getTime_end()))
                        throw new IllegalArgumentException("Invalid query parameters (time):"+request.getTime_start()+", "+request.getTime_end());
                    if (!commonUtilites.isDateValid(request.getDate_start()) || !commonUtilites.isDateValid(request.getDate_end()))
                        throw new IllegalArgumentException("Invalid query parameters (date):"+request.getDate_start()+", "+request.getDate_end());
                    stringQuery =
                            " insert into scdl_appointments (" +
                                    " master_id," + //мастер-аккаунт
                                    " creator_id," + //создатель
                                    " owner_id," + //владелец
                                    " company_id," + //предприятие, для которого создается документ
                                    " dep_part_id," + //отделение, из(для) которого создается документ
                                    " date_time_created," + //дата и время создания
                                    " doc_number," + //номер документа
                                    " name," +
                                    " description," +//доп. информация по документe
                                    " starts_at_time," +
                                    " ends_at_time," +
                                    " employee_id, " +
                                    " nds," +// НДС
                                    " nds_included," +// НДС включен в цену
                                    " status_id,"+//статус документа
                                    " uid"+// уникальный идентификатор документа
                                    ") values ("+
                                    masterId + ", "+//мастер-аккаунт
                                    myId + ", "+ //создатель
                                    myId + ", "+ //владелец
                                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                                    request.getDepartment_part_id() + ", "+//отделение, из(для) которого создается документ
                                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                                    doc_number + ", "+//номер документа
                                    " :name, "  +
                                    " :description, " +//описание
                                    " to_timestamp ('"+request.getDate_start()+" "+request.getTime_start()+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"'," +
                                    " to_timestamp ('"+request.getDate_end()+" "+request.getTime_end()+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"'," +
                                    request.getEmployeeId() + ", "+ // employee
                                    request.isNds() + ", "+// НДС
                                    request.isNds_included() + ", "+// НДС включен в цену
                                    request.getStatus_id() + "," +//статус документа
                                    ":uid"+// уникальный идентификатор документа
                                    ")";

                    try{
//                        Date dateNow = new Date();
//                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//                        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
//                        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                        Query query = entityManager.createNativeQuery(stringQuery);
                        query.setParameter("uid",request.getUid());
                        query.setParameter("name",request.getName());
                        query.setParameter("description",request.getDescription());
                        query.executeUpdate();
                        stringQuery="select id from scdl_appointments where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                        Query query2 = entityManager.createNativeQuery(stringQuery);
                        newDocId=Long.valueOf(query2.getSingleResult().toString());

                        //создание новых контрагентов и формирование списка <row_id - id> для контрагентов
                        Map<Integer,Long> cagentsMap = saveAppointmentCagents(request, myId, masterId);



                        //сохранение таблицы товаров
                        insertAppointmentProducts(request, cagentsMap, newDocId, masterId);
                        updateResults.setId(newDocId);
                        updateResults.setSuccess(true);
                        return updateResults;

//                    } catch (CantInsertProductRowCauseErrorException e) {
//                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                        logger.error("Exception in method insertAppointment on inserting into scdl_appointments_product. ", e);
//                        updateResults.setSuccess(false);
//                        updateResults.setErrorCode(2);      // Ошибка обработки таблицы товаров
//                        e.printStackTrace();
//                        return updateResults; // ошибка сохранения таблицы товаров
                    } catch (Exception e) {
                        logger.error("Exception in method insertAppointment on querying of created document id. SQL query:"+stringQuery, e);
                        e.printStackTrace();
                        updateResults.setSuccess(false);
                        updateResults.setErrorCode(1);      // Error saving document
                        return updateResults;
                    }
                } else {
                    updateResults.setSuccess(false);
                    updateResults.setErrorCode(-1);          // Недостаточно прав
                    return updateResults;
                }
            } catch (Exception e) {
                logger.error("Exception in method insertAppointment on inserting into scdl_appointments.", e);
                e.printStackTrace();
                updateResults.setSuccess(false);
                updateResults.setErrorCode(1);      // Error saving document
                return updateResults;
            }
        } else {
            logger.info("Double UUID found on insertAppointment. UUID: " + request.getUid());
            return null;
        }
//        return new AppointmentUpdateReportJSON();
    }

    private Map<Integer,Long> saveAppointmentCagents(AppointmentsForm request, Long creatorId, Long masterId) throws Exception {
        Map<Integer,Long> result = new HashMap<>();
        try {
            for (AppointmentCustomer cagent : request.getCustomersTable()) {
                result.put(cagent.getRow_id(), insertAppointmentCagents(cagent, masterId, creatorId, request.getCompany_id()));
            }
            return result;
        } catch (Exception e) {
            logger.error("Exception in method AppointmentsRepository/saveAppointmentCagents.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }
//    private void deleteCagentsThatNoMoreContainedInThisAppointment(Set<Long> existedCagents, Long appointmentId, Long masterId) throws Exception  {
//        String stringQuery =
//                " delete from scdl_product_resource_qtt " +
//                        " where " +
//                        " master_id = " + masterId + " and " +
//                        " product_id = " +productId;
//        if(existedCagents.size()>0)
//            stringQuery = stringQuery + " and resource_id not in " + commonUtilites.SetOfLongToString(existingResources,",","(",")");
//        try {
//            entityManager.createNativeQuery(stringQuery).executeUpdate();
//        } catch (Exception e) {
//            logger.error("Exception in method deleteResourcesThatNoMoreContainedInThisProduct. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            throw new Exception(e);
//        }
//    }
    private Long insertAppointmentCagents(AppointmentCustomer cagent, Long masterId, Long creatorId, Long companyId) throws Exception {
        String stringQuery="";
        try{
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery =
                    " insert into cagents ( " +
                            (Objects.isNull(cagent.getId())?"":" id, ") +
                            " master_id," +
                            " creator_id, " +
                            " company_id, " +
                            " date_time_created, " +
                            " name, " +
                            " email, " +
                            " telephone, " +
                            " jr_jur_full_name, " +
                            " type " +
                            " ) values (" +
                            (Objects.isNull(cagent.getId())?"":cagent.getId()+",") +
                            ""+masterId+"," +
                            ""+creatorId+"," +
                            ""+companyId+"," +
                            " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                            " :name," +
                            " :email," +
                            " :telephone," +
                            " :name," +
                            "'individual') " +
                            " ON CONFLICT ON CONSTRAINT cagents_pkey DO NOTHING";// "upsert"
//                            " DO update set " +
//                            " name = :name," +
//                            " email = :email," +
//                            " telephone :telephone," +
//                            " date_time_changed = now()";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",cagent.getName());
            query.setParameter("email",cagent.getEmail());
            query.setParameter("telephone",cagent.getTelephone());
            query.executeUpdate();
            if(Objects.isNull(cagent.getId())){
                stringQuery="select id from cagents where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+creatorId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                cagent.setId(Long.valueOf(query2.getSingleResult().toString()));
            }
            return cagent.getId();
        } catch (Exception e) {
            logger.error("Exception in method AppointmentsRepository/insertAppointmentCagents. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public AppointmentUpdateReportJSON updateAppointment(AppointmentsForm request) throws Exception {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();
        String myTimeZone = userRepository.getUserTimeZone();
        Long masterId = userRepositoryJPA.getMyMasterId();
        Long myId = userRepository.getUserId();
        Long departmentId=((BigInteger)commonUtilites.getFieldValueFromTableById("scdl_dep_parts","department_id",masterId,request.getDepartment_part_id())).longValue();
        List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
        boolean itIsMyDepartment = myDepartmentsIds.contains(departmentId);
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        AppointmentUpdateReportJSON updateResults = new AppointmentUpdateReportJSON();// отчет о создании
        try{
            if(Objects.isNull(departmentId)) throw new Exception("Department part with id = "+request.getDepartment_part_id()+" is not belongs to master Id = "+masterId);
            //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
            if(     (securityRepositoryJPA.userHasPermissions_OR(59L,"712") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments",request.getId().toString())) ||
                    //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(59L,"713") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",request.getId().toString()))||
                    //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                    (securityRepositoryJPA.userHasPermissions_OR(59L,"714") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",request.getId().toString()))||
                    //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                    (securityRepositoryJPA.userHasPermissions_OR(59L,"715") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",request.getId().toString())))
            {
                // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
                // проверим права на проведение
                if((request.getIs_completed()!=null && request.getIs_completed())){

                    if(
                            !(  //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                                    (securityRepositoryJPA.userHasPermissions_OR(59L,"720") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments",request.getId().toString())) ||
                                            //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                            (securityRepositoryJPA.userHasPermissions_OR(59L,"721") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",request.getId().toString()))||
                                            //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                                            (securityRepositoryJPA.userHasPermissions_OR(59L,"722") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",request.getId().toString()))||
                                            //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                                            (securityRepositoryJPA.userHasPermissions_OR(59L,"723") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",request.getId().toString()))
                            )
                    ) {
                        updateResults.setSuccess(false);
                        updateResults.setErrorCode(0);          // Недостаточно прав
                        return updateResults;
                    }
                }

                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "scdl_appointments"))
                    throw new DocumentAlreadyCompletedException();
                // если документ проводится и нет товаров - ошибка
                if(request.getIs_completed()!=null && request.getIs_completed() && request.getAppointmentsProductTable().size()==0) throw new CantInsertProductRowCauseErrorException();



                // commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                // !!!!!!!!!!!!!!!! Сделать проверку списка клиентов на принадлежность к masterId !!!!!!!!!!!!!

                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), masterId);

                // сохранение всего кроме таблицы товаров
                updateAppointmentWithoutTable(request, myTimeZone);

                //сохранение и удаление контрагентов и формирование списка <row_id - id> для контрагентов и товаров
                Map<Integer,Long> cagentsMap = saveAppointmentCagents(request, myId, masterId);

                // сохранение таблицы товаров
                insertAppointmentProducts(request, cagentsMap, request.getId(), masterId);

                // возвращаем весть об успешности операции
                updateResults.setSuccess(true);
                return updateResults;

            } else {
                updateResults.setSuccess(false);
                updateResults.setErrorCode(0);          // Недостаточно прав
                return updateResults;
            }
        } catch (DocumentAlreadyCompletedException e) { //
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method updateAppointment.", e);
            e.printStackTrace();
            updateResults.setSuccess(false);
            updateResults.setErrorCode(-50);      // Документ уже проведён
            return updateResults;
        } catch (CantInsertProductRowCauseErrorException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method updateAppointment on updating of scdl_appointments_product cause error.", e);
            updateResults.setSuccess(false);
            updateResults.setErrorCode(2);      // Ошибка обработки таблицы товаров
            e.printStackTrace();
            return updateResults;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method updateAppointment on updating of scdl_appointments cause error.", e);
            updateResults.setSuccess(false);
            updateResults.setErrorCode(1);      // Error saving document
            return updateResults;
        }
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, NotEnoughPermissionsException.class})
    public Integer setAppointmentAsDecompleted(AppointmentsForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(59L,"720") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments",request.getId().toString())) ||
                        //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(59L,"721") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",request.getId().toString()))||
                        //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                        (securityRepositoryJPA.userHasPermissions_OR(59L,"722") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",request.getId().toString()))||
                        //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                        (securityRepositoryJPA.userHasPermissions_OR(59L,"723") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",request.getId().toString()))
        )
        {
            if(request.getAppointmentsProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update scdl_appointments set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "scdl_appointments"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                // сохранение истории движения товара не делаем, т.к. в данный документ не влияет на движение товаров
                // по той же причине не делаем коррекцию баланса с контрагентом
                return 1;
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AppointmentsRepository/setAppointmentAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AppointmentsRepository/setAppointmentAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }

    //сохранение таблицы товаров
    public void insertAppointmentProducts(AppointmentsForm request, Map<Integer,Long> cagentsMap, Long parentDocId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> rowIds=new HashSet<>();
        try {
            for (AppointmentProductsTableForm row : request.getAppointmentsProductTable()) {
                row.setAppointment_id(parentDocId);// т.к. он может быть неизвестен при создании документа
                Long cagentId = cagentsMap.get(row.getCustomerRowId());
                row.setCustomerId(cagentId);
                saveAppointmentProductsTable(row, myMasterId, request.getDepartment_part_id());
                rowIds.add(row.getProduct_id());
            }
            deleteAppointmentProductsTableExcessRows(rowIds.size() > 0 ? (commonUtilites.SetOfLongToString(rowIds, ",", "", "")) : "0", request.getId(), myMasterId);
        }
        catch (Exception e) {
            logger.error("Exception in method AppointmentsRepositoryJPA/insertAppointmentProducts.", e);
            e.printStackTrace();
            throw new CantInsertProductRowCauseErrorException();
        }
    }

    private void saveAppointmentProductsTable(AppointmentProductsTableForm row, Long master_id, Long depPartId) throws CantInsertProductRowCauseErrorException {
        String stringQuery="";
        if(Objects.isNull(row.getReserved_current())) row.setReserved_current(new BigDecimal(0));
        try {
            stringQuery =
                    " insert into scdl_appointments_product (" +
                            "master_id, " +
                            "product_id, " +
                            "appointment_id, " +
                            "cagent_id, " +
                            "product_count, " +
                            "product_price, " +
                            "product_sumprice, " +
                            "edizm_id, " +
                            "price_type_id, " +
                            "nds_id, " +
                            "department_id, " +
                            "product_price_of_type_price " +
                            ") values (" +
                            master_id + "," +
                            row.getProduct_id() + "," +
                            row.getAppointment_id() + "," +
                            row.getCustomerId() + "," +
                            row.getProduct_count() + "," +
                            row.getProduct_price() + "," +
                            row.getProduct_sumprice() + "," +
                            row.getEdizm_id() + "," +
                            row.getPrice_type_id() + "," +
                            row.getNds_id() + ", " +
                            "(select department_id from scdl_dep_parts where id = "+depPartId+" and master_id="+master_id+"), " +
                            row.getProduct_price_of_type_price() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT scdl_appointment_cagent_product_uq " +// "upsert"
                            " DO update set " +
//                            " product_id = " + row.getProduct_id() + ","+
//                            " appointment_id = " + row.getAppointment_id() + ","+
                            " product_count = " + row.getProduct_count() + ","+
                            " product_price = " + row.getProduct_price() + ","+
                            " product_sumprice = " + row.getProduct_sumprice() + ","+
                            " edizm_id = " + row.getEdizm_id() + ","+
                            " price_type_id = " + row.getPrice_type_id() + ","+
                            " nds_id = " + row.getNds_id() + ","+
                            " department_id = (select department_id from scdl_dep_parts where id = "+depPartId+" and master_id="+master_id+"),"+
                            " product_price_of_type_price = " + row.getProduct_price_of_type_price();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        }
        catch (Exception e) {
            logger.error("Exception in method AppointmentsRepositoryJPA/saveAppointmentProductsTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new CantInsertProductRowCauseErrorException();
        }
    }
    @SuppressWarnings("Duplicates")
    private Boolean updateAppointmentWithoutTable(AppointmentsForm request, String myTimeZone) throws Exception {
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

        if (!commonUtilites.isTimeValid(request.getTime_start()) || !commonUtilites.isTimeValid(request.getTime_end()))
            throw new IllegalArgumentException("Invalid query parameters (time):"+request.getTime_start()+", "+request.getTime_end());
        if (!commonUtilites.isDateValid(request.getDate_start()) || !commonUtilites.isDateValid(request.getDate_end()))
            throw new IllegalArgumentException("Invalid query parameters (date):"+request.getDate_start()+", "+request.getDate_end());





                String stringQuery;
        stringQuery =
                " update scdl_appointments set " +
                " changer_id = " + myId + ", " +
                " owner_id = " + myId + ", " +  // later it can be changed
                " dep_part_id = " + request.getDepartment_part_id() + ", " +
                " date_time_changed= now()," +
                " name = :name, " +
                " description = :description, " +
                " employee_id = " + request.getEmployeeId() + ", " +
                " starts_at_time = to_timestamp ('"+request.getDate_start()+" "+request.getTime_start()+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"'," +
                " ends_at_time = to_timestamp ('"+request.getDate_end()+" "+request.getTime_end()+"', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"'," +
                " nds  = " + request.isNds() + ", " +
                " nds_included  = " + request.isNds_included() + ", " +
                " doc_number = " + request.getDoc_number() + ", " +
                " is_completed  = " + request.getIs_completed() + ", " +
                " status_id = " + request.getStatus_id() +
                " where " +
                " id= "+request.getId();
        try
        {
//            Date dateNow = new Date();
//            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//            dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",(request.getName() == null ? "" : request.getName()));
            query.setParameter("description",(request.getDescription() == null ? "" : request.getDescription()));
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method updateAppointmentWithoutTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }


    //сохраняет настройки документа

    @Transactional
    public Boolean saveSettingsAppointment(SettingsAppointmentForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);

            stringQuery =
            " insert into settings_appointment (" +
            " master_id, " +
            " company_id, " +
            " user_id, " +
            " date_time_update, " +
            " start_time, "+          // current / set_manually
            " end_date_time, "+       // no_calc / sum_all_length / max_length /
            " start_time_manually, "+ // 'HH:mm' if start_time = 'set_manually'
            " end_time_manually, "+   // 'HH:mm' if end_time = 'calc_date_but_time' || 'no_calc_date_but_time'
            " calc_date_but_time, "+   //  If user wants to calc only dates. Suitable for hotels for checkout time
            " hide_employee_field"+   // If for all services of company employees are not needed
            ") values (" +
            myMasterId + "," +
            row.getCompanyId() + "," +
            myId + "," +
            " now(), " +
            " :start_time,"+
            " :end_date_time," +
            " :start_time_manually," +
            " :end_time_manually," +
            row.isCalcDateButTime() + "," +
            row.isHideEmployeeField() +
            ") " +
            " ON CONFLICT ON CONSTRAINT settings_appointment_user_uq " +// "upsert"
            " DO update set " +
            " date_time_update = now()," +
            " company_id = " + row.getCompanyId() + "," +
            " start_time = :start_time," +
            " end_date_time = :end_date_time," +
            " start_time_manually = :start_time_manually," +
            " end_time_manually = :end_time_manually," +
            " calc_date_but_time = "+row.isCalcDateButTime() + "," +
            " hide_employee_field = "+row.isHideEmployeeField();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("start_time",row.getStartTime());
            query.setParameter("end_date_time",row.getEndDateTime());
            query.setParameter("start_time_manually",row.getStartTimeManually());
            query.setParameter("end_time_manually",row.getEndTimeManually());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsAppointment. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public SettingsAppointmentJSON getSettingsAppointment() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "     select " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +                                     // company
                "           coalesce(start_time,'current') as start_time, " +                   // current / set_manually   The last one is suitable for hotels for checkin time
                "           coalesce(end_date_time, 'sum_all_length') as end_date_time, " +     // no_calc / sum_all_length / max_length
                "           coalesce(start_time_manually, '00:00') as start_time_manually, " +  // 'HH:mm' if start_time = 'set_manually'
                "           coalesce(end_time_manually, '00:01') as end_time_manually, " +      // 'HH:mm' if end_time = 'calc_date_but_time' || 'no_calc_date_but_time'
                "           coalesce(hide_employee_field, false) as hide_employee_field, " +     //  If for all services of company employees are not needed
                "           coalesce(calc_date_but_time, false) as calc_date_but_time " +       //  If user wants to calc only dates. Suitable for hotels for checkout time

                "           from settings_appointment p " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsAppointmentJSON returnObj=new SettingsAppointmentJSON(
                    "current","sum_all_length","00:00","00:01",false, false);

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                             obj[0].toString()));
                returnObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                returnObj.setStartTime((String)                             obj[2]);
                returnObj.setEndDateTime((String)                           obj[3]);
                returnObj.setStartTimeManually((String)                     obj[4]);
                returnObj.setEndTimeManually((String)                       obj[5]);
                returnObj.setHideEmployeeField((Boolean)                    obj[6]);
                returnObj.setCalcDateButTime((Boolean)                      obj[7]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsAppointment. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }

    }

    //  удаляет лишние позиции товаров при сохранении документа (те позиции, которые ранее были в документе, но потом их удалили)
    private void deleteAppointmentProductsTableExcessRows(String rowIds, Long appointment_id, Long myMasterId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =   " delete from scdl_appointments_product " +
                    " where appointment_id=" + appointment_id +
                    " and master_id=" + myMasterId +
                    (rowIds.length()>0?(" and product_id not in (" + rowIds.replaceAll("[^0-9\\,]", "") + ")"):"");//если во фронте удалили все товары, то удаляем все товары в данном Заказе покупателя
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        }
        catch (Exception e) {
            logger.error("Exception in method deleteAppointmentProductsTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteAppointments (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(59L,"716") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(59L,"717") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(59L,"718") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(59L,"719") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "scdl_appointments");
            Long myMasterId=userRepositoryJPA.getMyMasterId();

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update scdl_appointments p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true and master_id="+ myMasterId;
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "scdl_appointments")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteAppointments. SQL query:" + stringQuery, e);
                        e.printStackTrace();
                        delResult.setResult(1);// 1 - ошибка выполнения операции
                        return delResult;
                    }
                } else { //один или несколько документов имеют связь с дочерними документами
                    delResult.setResult(3);// 3 -  связи с дочерними документами
                    delResult.setDocs(checkChilds);
                    return delResult;
                }
            } else { //ошибка проверки на связь с дочерними документами
                delResult.setResult(1);// 1 - ошибка выполнения операции
                return delResult;
            }
        } else {
            delResult.setResult(2);// 2 - нет прав
            return delResult;
        }
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteAppointments(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(59L,"716") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(59L,"717") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(59L,"718") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(59L,"719") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery;
            stringQuery = "Update scdl_appointments p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+") and master_id="+ myMasterId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteAppointments. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    private List<AppointmentService> getAppointmentServicesList(AppointmentMainInfoForm reqest, Long masterId, String myTimeZone) {

//        String stringQuery = getBusyResourcesSqlQueryPart( masterId, reqest, myTimeZone);
        CalendarEventsQueryForm eventsQuery = new CalendarEventsQueryForm();
        eventsQuery.setCompanyId(reqest.getCompanyId());
        eventsQuery.setDateFrom(reqest.getDateFrom());
        eventsQuery.setDateTo(reqest.getDateTo());
        eventsQuery.setTimeFrom(reqest.getTimeFrom());
        eventsQuery.setTimeTo(reqest.getTimeTo());
        eventsQuery.setDepparts(new HashSet<>());
        eventsQuery.setEmployees(new HashSet<>());
        List<CalendarEventJSON> calendarEventsLis = calendarRepository.getCalendarEventsList(eventsQuery);


        String stringQuery = " select  " +
                        " p.id, " +
                        " p.name, " +
                        " coalesce(d.id,0) as department_id, " +
                        " coalesce(d.name,'') as department, " +
                        " coalesce(dp.id,0) as dep_part_id, " +
                        " coalesce(dp.name,'') as dep_part_name, " +
                        " r.id as resource_id, " +
                        " coalesce(r.name,'') as resource_name, " +
                        " coalesce(prq.quantity,0) as need_res_qtt, " +
//                        " coalesce((select sum(quantity_now_used) from busy_resources where resource_id=r.id and dep_part_id=dp.id),0) as now_used,  " +
                        " coalesce(rdp.quantity,0) as res_quantity_in_dep_part, " +
                        " coalesce(pqtt.quantity, 0) product_quantity_in_department, " +
                        " p.nds_id as nds_id, " +
                        " coalesce(p.edizm_id,0) as edizm_id, " +
                        " coalesce(edizm.short_name,'') as edizm, " +
                        " coalesce(edizm.type_id, 0) as edizm_type_id, " +       // 6=time, 2=weight, ...
                        " coalesce(edizm.equals_si, 1.000) as si_multiplier, " +
                        " ppr.is_material as is_material, " +
                        " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                        " coalesce(pp.price_value,0.00) as price_by_typeprice, " +
                        " coalesce(scdl_is_employee_required, false) as is_employee_required, " +// Whether employee is necessary required to do this service job?
                        " coalesce(scdl_max_pers_on_same_time,0) as max_pers_on_same_time, " +  // How many persons can get this service in one appointment by the same time
                        " coalesce(p.scdl_appointment_atleast_before_time * sse_alb.equals_si,0.00)*1.0 as atleast_before_seconds, " + // Minimum time before the start of the service for which customers can make an appointment
                        " coalesce(p.scdl_srvc_duration * sse_sd.equals_si,0.00)*1.0 as service_duration_seconds, " + // Approx. duration time to fininsh this service
                        " case when edizm.type_id=6 then coalesce(sse_tu.equals_si,0.00)*1.0 else 0.0 end as edizm_in_seconds, " +
                        " coalesce(p.is_srvc_by_appointment,false) as srvc_by_appntmnt," +
                        " case when coalesce(ppr.is_material,true) then (select " +
                        "   sum(coalesce(reserved_current,0)-0) " +
                        "   from " +
                        "   customers_orders_product " +
                        "   where " +
                        "   product_id=p.id "+
                        "   and department_id = d.id) else 0 end as reserved, " +//зарезервировано в документах Заказ покупателя
                        " app.product_count as product_count, " +
                        " app.product_price as product_price, " +
                        " app.product_sumprice as product_sumprice, " +
                        " app.cagent_id as cagent_id, " +
                        " app.price_type_id as price_type_id," +
                        " coalesce(nds.value,0) as nds_value" +
                        " from " +
                        " scdl_appointments_product app " +
                        " inner join products p on p.id = app.product_id " +
                        " left outer join scdl_product_resource_qtt prq on p.id=prq.product_id  " +
                        " left outer join sprav_resources r on prq.resource_id=r.id " +
                        " left outer join scdl_dep_part_products dpp on dpp.product_id=p.id " +
                        " left outer join scdl_dep_parts dp on dp.id=dpp.dep_part_id  " +
                        " left outer join departments d on d.id=app.department_id " +
                        " left outer join scdl_resource_dep_parts_qtt rdp on rdp.resource_id=r.id and rdp.dep_part_id=dp.id " +
                        " left outer join product_prices pp on pp.product_id = p.id and pp.price_type_id = app.price_type_id" +
                        " left outer join product_quantity pqtt on pqtt.product_id=p.id and pqtt.department_id = d.id " +
                        " left outer join sprav_sys_edizm edizm on edizm.id=p.edizm_id" +
                        " left outer join sprav_sys_edizm sse_alb on sse_alb.id = p.scdl_appointment_atleast_before_unit_id " +
                        " left outer join sprav_sys_edizm sse_sd on sse_sd.id = p.scdl_srvc_duration_unit_id " +
                        " left outer join sprav_sys_edizm sse_tu on sse_tu.id = p.edizm_id " +
                        " left outer join sprav_taxes nds ON nds.id = p.nds_id" +
                        " inner join sprav_sys_ppr ppr ON p.ppr_id = ppr.id " +
                        " where " +
                        " app.master_id=" + masterId + " and " +
                        " app.appointment_id = " + reqest.getAppointmentId();

        stringQuery = stringQuery +" order by app.cagent_id, p.id, d.name, dp.name, r.name ";

        String currentPair  = "";
        Long currentServiceId = 0L;
        Long currentDepPartId = 0L;
        AppointmentService appointmentService = new AppointmentService();
        DepartmentPartWithResourcesIds departmentPartWithResourcesIds = new DepartmentPartWithResourcesIds();
        List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds = new ArrayList<>();
        Set<ResourceOfDepartmentPart> currentDepPartResources = new HashSet<>();
        List<AppointmentService> returnList = new ArrayList<>();
        Query query = entityManager.createNativeQuery(stringQuery);//
        List<Object[]> queryList = query.getResultList();

        for (Object[] obj : queryList) {
            Long        currentCycleServiceId = Long.parseLong(                 obj[0].toString());
            String      currentCycleServiceName =                               obj[1].toString();
            Long        currentCycleDepartmentId = Long.parseLong(              obj[2].toString());
            String      currentCycleDepartmentName =                            obj[3].toString();
            Long        currentCycleDepPartId = Long.parseLong(                 obj[4].toString());
            String      currentCycleDepPartName =                               obj[5].toString();
            Long        currentCycleResourceId = (                              obj[6] == null?null:Long.parseLong(obj[6].toString()));
            String      currentCycleResourceName =                              obj[7].toString();
            Integer     currentCycleNeedRresQtt = Integer.parseInt(             obj[8].toString());
//            Integer     currentCycleNowUsed = Integer.parseInt(                 obj[9].toString());
            Integer     currentCycleNowUsed = getMaxUsedResourceQtt(currentCycleDepPartId, currentCycleResourceId, reqest.getAppointmentId(), calendarEventsLis);
            Integer     currentCycleQuantityInDepPart = Integer.parseInt(       obj[9].toString());
            BigDecimal  currentCycleTotal = (                                   obj[10]==null?BigDecimal.ZERO:(BigDecimal)obj[10]);
            Integer     currentCycleNdsId=((Integer)                            obj[11]);
            Long        currentCycleEdIzmId = (Long.parseLong(                  obj[12].toString()));
            String      currentCycleEdIzm = ((String)                           obj[13]);
            Integer     currentCycleEdizm_type_id = ((Integer)                  obj[14]);
            BigDecimal  currentCycleEdizm_multiplier = (                        obj[15]==null?BigDecimal.ZERO:(BigDecimal)obj[15]);
            Boolean     currentCycleIs_material = ((Boolean)                    obj[16]);
            Boolean     currentCycleIndivisible = ((Boolean)                    obj[17]);
            BigDecimal  currentCyclePriceOfTypePrice = (                        obj[18]==null?BigDecimal.ZERO:(BigDecimal)obj[18]);
            Boolean     currentCycleIsEmployeeRequired=((Boolean)               obj[19]);
            Integer     currentCycleMaxPersOnSameTime=((Integer)                obj[20]);
            BigDecimal  currentCycleAtLeastBeforeTimeInSeconds=((BigDecimal)    obj[21]);
            BigDecimal  currentCycleSrvcDurationInSeconds=((BigDecimal)         obj[22]);
            BigDecimal  currentCycleUnitOfMeasureDurationInSeconds=((BigDecimal)obj[23]);
            Boolean     currentCycleServiceByAppointment = ((Boolean)           obj[24]);
            BigDecimal  currentCycleReserved =  (                               obj[25]==null?BigDecimal.ZERO:(BigDecimal)obj[25]);
            BigDecimal  currentCycleProductCount =  (                           obj[26]==null?BigDecimal.ZERO:(BigDecimal)obj[26]);
            BigDecimal  currentCycleProductPrice =  (                           obj[27]==null?BigDecimal.ZERO:(BigDecimal)obj[27]);
            BigDecimal  currentCycleProductSumprice =  (                        obj[28]==null?BigDecimal.ZERO:(BigDecimal)obj[28]);
            Long        currentCycleCagentId = (Long.parseLong(                 obj[29].toString()));
            Long        currentCyclePriceTypeId = (                             obj[30] == null?null:Long.parseLong(      obj[30].toString()));
            String      currentCyclePair = currentCycleCagentId.toString() + " " + currentCycleServiceId.toString();
            BigDecimal  currentCycleNdsValue = ((BigDecimal)                    obj[31]);


                // on this cycle if it is a new service
                if (!currentCyclePair.equals(currentPair)) {
                    // If it is not a first cycle// Если это не первый цикл
                    if (!currentServiceId.equals(0L)) {
                        // В текущую часть отделения сохранили все накопленные IDs сервисов
                        departmentPartWithResourcesIds.setResourcesOfDepartmentPart(currentDepPartResources);
                        // В список частей отделения текущего пользователя добавили текущее отделение
                        departmentPartsWithResourcesIds.add(departmentPartWithResourcesIds);
                        // В текущеий сервис поместили список частей отделений
                        appointmentService.setDepartmentPartsWithResourcesIds(departmentPartsWithResourcesIds);
                        // В итоговый список сервисов поместили этот сервис
                        returnList.add(appointmentService);
                        // Cоздали новый сервис
                        appointmentService = new AppointmentService();
                        // Для нового сервиса создаем новую часть отделенияи сбрасываем накопление IDs сервисов
                        currentDepPartId = currentCycleDepPartId;
                        // Cоздали новую часть отделения, и прописали туда её ID
                        departmentPartWithResourcesIds = new DepartmentPartWithResourcesIds(currentDepPartId, currentCycleDepPartName);
                        // Cбросили текущее накопление ID сервисов для новой части отделения
                        currentDepPartResources = new HashSet<>();
                    }
                    currentPair = currentCyclePair;
                    currentServiceId = currentCycleServiceId;
                    // Для нового сервиса задаём его ID, имя, отделение и др..
                    appointmentService.setId(                           currentCycleServiceId);
                    appointmentService.setName(                         currentCycleServiceName);
                    appointmentService.setDepartmentId(                 currentCycleDepartmentId);
                    appointmentService.setDepartmentName(               currentCycleDepartmentName);
                    appointmentService.setNds_id(                       currentCycleNdsId);
                    appointmentService.setEdizm_id(                     currentCycleEdIzmId);
                    appointmentService.setEdizm(                        currentCycleEdIzm);
                    appointmentService.setEdizm_multiplier(             currentCycleEdizm_multiplier);
                    appointmentService.setEdizm_type_id(                currentCycleEdizm_type_id);
                    appointmentService.setTotal(                        currentCycleTotal);
                    appointmentService.setIs_material(                  currentCycleIs_material);
                    appointmentService.setIndivisible(                  currentCycleIndivisible);
                    appointmentService.setPriceOfTypePrice(             currentCyclePriceOfTypePrice);
                    appointmentService.setEmployeeRequired(             currentCycleIsEmployeeRequired);
                    appointmentService.setMaxPersOnSameTime(            currentCycleMaxPersOnSameTime);
                    appointmentService.setSrvcDurationInSeconds(        currentCycleSrvcDurationInSeconds);
                    appointmentService.setAtLeastBeforeTimeInSeconds(   currentCycleAtLeastBeforeTimeInSeconds);
                    appointmentService.setUnitOfMeasureTimeInSeconds(   currentCycleUnitOfMeasureDurationInSeconds);
                    appointmentService.setIsServiceByAppointment(       currentCycleServiceByAppointment);
                    appointmentService.setReserved(                     currentCycleReserved);
                    appointmentService.setProduct_count(                currentCycleProductCount);
                    appointmentService.setProduct_price(                currentCycleProductPrice);
                    appointmentService.setProduct_sumprice(             currentCycleProductSumprice);
                    appointmentService.setCagent_id(                    currentCycleCagentId);
                    appointmentService.setPrice_type_id(                currentCyclePriceTypeId);
                    appointmentService.setNds_value(                    currentCycleNdsValue);
                    // Cоздали новый лист для накопления частей отделений для нового сервиса
                    departmentPartsWithResourcesIds = new ArrayList<>();
                }
                // Если сервис не новый, но часть отделения сменилась
                if (!currentCycleDepPartId.equals(currentDepPartId)) {
                    if (!currentDepPartId.equals(0L)) {
                        // В текущую часть отделения сохранили все накопленные IDs сервисов
                        departmentPartWithResourcesIds.setResourcesOfDepartmentPart(currentDepPartResources);
                        // В список частей отделения текущего пользователя добавили текущее отделение
                        departmentPartsWithResourcesIds.add(departmentPartWithResourcesIds);
                    }
                    currentDepPartId = currentCycleDepPartId;
                    // Cоздали новую часть отделения, и прописали туда её ID
                    departmentPartWithResourcesIds = new DepartmentPartWithResourcesIds(currentDepPartId, currentCycleDepPartName);
                    // Cбросили текущее накопление ID сервисов для новой части отделения
                    currentDepPartResources = new HashSet<>();
                }
                if(!Objects.isNull(currentCycleResourceId)) currentDepPartResources.add(new ResourceOfDepartmentPart(
                        currentCycleResourceId,
                        currentCycleResourceName,
                        currentCycleNeedRresQtt,
                        currentCycleNowUsed,
                        currentCycleQuantityInDepPart
                ));
            }
        // По окончании цикла, если в ней что-то было - нужно записать последний сервис
        if (!currentServiceId.equals(0L)) {
            // В текущую часть отделения сохранили все накопленные IDs сервисов
            departmentPartWithResourcesIds.setResourcesOfDepartmentPart(currentDepPartResources);
            // В список частей отделения текущего пользователя добавили текущее отделение
            departmentPartsWithResourcesIds.add(departmentPartWithResourcesIds);
            // В текущего сотрудника поместили список частей отделений
            appointmentService.setDepartmentPartsWithResourcesIds(departmentPartsWithResourcesIds);
            // В итоговый список сотрудников поместили этого сотрудника
            returnList.add(appointmentService);
        }
        return returnList;
    }

//    private String getBusyResourcesSqlQueryPart(Long masterId, AppointmentMainInfoForm reqest, String myTimeZone){
//        return " WITH busy_resources AS ( " +
////              busy_resources - Это выборка с занятыми ресурсами в заданном промежутке времени в виде: / ID ресурса / Название / Используемое количество " +
//                        " select  " +
//                        " r.id as resource_id,  " +
//                        " r.name as resource,  " +
//                        " dp.id as dep_part_id, " +
//                        " dp.name as dep_part, " +
//                        " pr.quantity as quantity_now_used  " + //-- кол-во используемого ресурса во всех Appointments
//                        " from   " +
//                        " scdl_appointments a, " +
//                        " scdl_appointments_product ap,  " +
//                        " products p, " +
//                        " scdl_product_resource_qtt pr, " +
//                        " sprav_resources r, " +
//                        " scdl_dep_parts dp, " +
//                        " scdl_resource_dep_parts_qtt rdp, " +
//                        " sprav_status_dock ssd " +
//                        " where " +
//                        " p.master_id=" + masterId + " and " +
//                        " p.company_id=" + reqest.getCompanyId() + " and " +
//                        " ap.appointment_id=a.id and " +
//                        " ap.product_id=p.id and " +
//                        " pr.product_id=p.id and " +
//                        " pr.resource_id=r.id and  " +
//                        " dp.id=a.dep_part_id and " +
//                        " a.status_id = ssd.id and " +
//                        " ssd.status_type != 3 and " + // cancelled Appointment type
//                        " rdp.dep_part_id=a.dep_part_id and " +
//                        " 	a.id != " + reqest.getAppointmentId() + " and  " + //-- filtering by parent Appointment document
//                        " rdp.resource_id=r.id and " +
//                        " to_timestamp ('" + reqest.getDateFrom() + " " + reqest.getTimeFrom() + "', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '" + myTimeZone + "' < a.ends_at_time and " +
//                        " to_timestamp ('" + reqest.getDateTo() + " " + reqest.getTimeTo() + "', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '" + myTimeZone + "' > a.starts_at_time  " +
//                        ") ";
//    }

    public List<AppointmentService> getAppointmentServicesSearchList(AppointmentMainInfoForm reqest) {

        Long masterId = userRepositoryJPA.getMyMasterId();
//        String myTimeZone = userRepository.getUserTimeZone();
        CalendarEventsQueryForm eventsQuery = new CalendarEventsQueryForm();
        eventsQuery.setCompanyId(reqest.getCompanyId());
        eventsQuery.setDateFrom(reqest.getDateFrom());
        eventsQuery.setDateTo(reqest.getDateTo());
        eventsQuery.setTimeFrom(reqest.getTimeFrom());
        eventsQuery.setTimeTo(reqest.getTimeTo());
        eventsQuery.setDepparts(new HashSet<>());
        eventsQuery.setEmployees(new HashSet<>());
        List<CalendarEventJSON> calendarEventsLis = calendarRepository.getCalendarEventsList(eventsQuery);

//        String stringQuery = getBusyResourcesSqlQueryPart( masterId, reqest, myTimeZone);

        String stringQuery = " select  " +
                        " p.id,  " +
                        " p.name,  " +
                        " coalesce(d.id,0) as department_id, " +
                        " coalesce(d.name,'') as department, " +
                        " coalesce(dp.id,0) as dep_part_id, " +
                        " coalesce(dp.name,'') as dep_part_name, " +
                        " r.id as resource_id, " +
                        " coalesce(r.name,'') as resource_name, " +
                        " coalesce(prq.quantity,0) as need_res_qtt, " +
//                        " coalesce((select sum(quantity_now_used) from busy_resources where resource_id=r.id and dep_part_id=dp.id),0) as now_used,  " +
                        " coalesce(rdp.quantity,0) as res_quantity_in_dep_part, " +
                        " coalesce(pqtt.quantity, 0) product_quantity_in_department, " +
                        " p.nds_id as nds_id, " +
                        " coalesce(p.edizm_id,0) as edizm_id, " +
                        " coalesce(edizm.short_name,'') as edizm," +
                        " coalesce(edizm.type_id, 0) as edizm_type_id," +       // 6=time, 2=weight, ...
                        " coalesce(edizm.equals_si, 1.000) as si_multiplier," +
                        " ppr.is_material as is_material, " +
                        " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                        " coalesce(pp.price_value,0.00) as price_by_typeprice, " +
                        " coalesce(scdl_is_employee_required, false) as is_employee_required, " +// Whether employee is necessary required to do this service job?
                        " coalesce(scdl_max_pers_on_same_time,0) as max_pers_on_same_time, " +  // How many persons can get this service in one appointment by the same time
                        " coalesce(p.scdl_appointment_atleast_before_time * sse_alb.equals_si,0.00)*1.0 as atleast_before_seconds, " + // Minimum time before the start of the service for which customers can make an appointment
                        " coalesce(p.scdl_srvc_duration * sse_sd.equals_si,0.00)*1.0 as service_duration_seconds, " + // Approx. duration time to fininsh this service
                        " case when edizm.type_id=6 then coalesce(sse_tu.equals_si,0.00)*1.0 else 0.0 end as edizm_in_seconds," +
                        " coalesce(p.is_srvc_by_appointment,false) as srvc_by_appntmnt," +
                        " case when coalesce(ppr.is_material,true) then (select " +
                        "   sum(coalesce(reserved_current,0)-0) " +
                        "   from " +
                        "   customers_orders_product " +
                        "   where " +
                        "   product_id=p.id "+
                        "   and department_id = d.id) else 0 end as reserved "+//зарезервировано в документах Заказ покупателя
                        " from " +
                        " products p " +
                        " left outer join scdl_product_resource_qtt prq on p.id=prq.product_id  " +
                        " left outer join sprav_resources r on prq.resource_id=r.id " +
//                      " -- left outer join busy_resources br on br.resource_id=r.id " +
                        " left outer join scdl_dep_part_products dpp on dpp.product_id=p.id " +
                        " left outer join scdl_dep_parts dp on dp.id=dpp.dep_part_id  " +
                        " left outer join departments d on d.id=dp.department_id " +
                        " left outer join scdl_resource_dep_parts_qtt rdp on rdp.resource_id=r.id and rdp.dep_part_id=dp.id " +
                        " left outer join product_prices pp on pp.product_id = p.id and pp.price_type_id = " + reqest.getPriceTypeId() +
                        " left outer join product_barcodes pb on pb.product_id=p.id" +
                        " left outer join product_quantity pqtt on pqtt.product_id=p.id and pqtt.department_id = d.id " +
                        " left outer join sprav_sys_edizm edizm on edizm.id=p.edizm_id" +
                        " left outer join scdl_assignments asg on asg.product_id=p.id " +// The source where is query going from.  'customer' - from website by customer, or 'manually' - from crm manually by staff (administrator of salon, etc.)
                        " left outer join sprav_sys_edizm sse_alb on sse_alb.id = p.scdl_appointment_atleast_before_unit_id " +
                        " left outer join sprav_sys_edizm sse_sd on sse_sd.id = p.scdl_srvc_duration_unit_id " +
                        " left outer join sprav_sys_edizm sse_tu on sse_tu.id = p.edizm_id " +
                        " inner join sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                        " where " +
                        " p.master_id=" + masterId + " and " +
                        " p.company_id=" + reqest.getCompanyId() + " and " +
                        " p.ppr_id in (1,4) and " + //-- products and services
                        " coalesce(p.is_deleted,false)=false and " +
                        " coalesce(dp.is_deleted,false)=false and " +
                        " case when coalesce(p.is_srvc_by_appointment,false) = true then (coalesce(dp.is_active,false)=true) else true end and " +
                        " case when coalesce(p.is_srvc_by_appointment,false) = true then asg.assignment_type = :asg else true end ";
//                        " pp.price_type_id = "+reqest.getPriceTypeId();
                        if (reqest.getSearchString() != null && !reqest.getSearchString().isEmpty()) {
                        stringQuery = stringQuery + " and (" +
                            " upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                            " upper(p.article) like upper (CONCAT('%',:sg,'%')) or " +
                            " to_char(p.product_code_free,'fm0000000000') = :sg or " +
                            " pb.value = :sg" +
                            ")";
                        }
                        if(reqest.getQuerySource().equals("customer"))// The source where is query going from.  'customer' - from website by customer, or 'manually' - from crm manually by staff (administrator of salon, etc.)
                            stringQuery = stringQuery +
                            " and concat(p.id,'_',dp.id) not in ( " +

    //                      Cписок услуг у которых хотя бы 1 ресурса не достаточно для выполнения этой услуги в данной части отделения
    //                      В данном случае эта часть отделения не будет включена в услугу как часть отделения, где эта услуга выполняется.
    //                      А если это была единственная часть отделения - эта услуга вообще не будет включена в список на выдачу.
    //                      Если запрос идет от администратора сервиса из CRM - данная фильтрация будет проведена на фронтеэнде в CRM, т.к. пользователю нужно объяснить причины не отображения услуге в списке поиска.
    //                      List of services for whi    ch at least 1 resource is not sufficient to perform this service in this part of the department
    //                      In this case, this part of the department will not be included in the service as part of the department where this service is performed.
    //                      And if this was the only part of the department, this service will not be included in the list for issue at all
    //                      If the request comes from the service administrator from CRM, this filtering will be carried out on the front end in CRM, because the user needs the explanation of the reasons of not displaying the service in the search list.

                            " 	select concat(p.id,'_',dp.id) " +
                            " 	from " +
                            " products p " +
                            " left outer join scdl_product_resource_qtt prq on p.id=prq.product_id  " +
                            " left outer join sprav_resources r on prq.resource_id=r.id " +
                            //" -- left outer join busy_resources br on br.resource_id=r.id " +
                            " left outer join scdl_dep_part_products dpp on dpp.product_id=p.id " +
                            " left outer join scdl_dep_parts dp on dp.id=dpp.dep_part_id  " +
                            " left outer join departments d on d.id=dp.department_id " +
                            " left outer join scdl_resource_dep_parts_qtt rdp on rdp.resource_id=r.id and rdp.dep_part_id=dp.id " +
                            " where " +
                            " p.master_id=" + masterId + " and " +
                            " p.company_id=" + reqest.getCompanyId() + " and " +
                            " p.ppr_id=4 and  " + //-- this is a service
                            " coalesce(d.is_deleted,false)=false and  " +
                            " coalesce(dp.is_deleted,false)=false and  " +
                            " (dp.is_active is null or coalesce(dp.is_active,false)=true) and " +
                            " p.is_srvc_by_appointment = true  " + //-- this is a service by appointment
                            " and coalesce(rdp.quantity,0)-coalesce((select sum(quantity_now_used) from busy_resources where resource_id=r.id and dep_part_id=dp.id),0)<coalesce(prq.quantity,0) " +
                            " ) ";

                        stringQuery = stringQuery +" order by p.name, d.name, dp.name, r.name ";

        Long currentServiceId = 0L;
        Long currentDepPartId = 0L;
        AppointmentService appointmentService = new AppointmentService();
        DepartmentPartWithResourcesIds departmentPartWithResourcesIds = new DepartmentPartWithResourcesIds();
        List<DepartmentPartWithResourcesIds> departmentPartsWithResourcesIds = new ArrayList<>();
        Set<ResourceOfDepartmentPart> currentDepPartResources = new HashSet<>();
        List<AppointmentService> returnList = new ArrayList<>();
        Query query = entityManager.createNativeQuery(stringQuery);//
        if (reqest.getSearchString() != null && !reqest.getSearchString().isEmpty())
        {query.setParameter("sg", reqest.getSearchString());}
        query.setParameter("asg", reqest.getQuerySource());
        List<Object[]> queryList = query.getResultList();

        for (Object[] obj : queryList) {
            Long        currentCycleServiceId = Long.parseLong(                 obj[0].toString());
            String      currentCycleServiceName =                               obj[1].toString();
            Long        currentCycleDepartmentId = Long.parseLong(              obj[2].toString());
            String      currentCycleDepartmentName =                            obj[3].toString();
            Long        currentCycleDepPartId = Long.parseLong(                 obj[4].toString());
            String      currentCycleDepPartName =                               obj[5].toString();
            Long        currentCycleResourceId = (                              obj[6] == null?null:Long.parseLong(obj[6].toString()));
            String      currentCycleResourceName =                              obj[7].toString();
            Integer     currentCycleNeedRresQtt = Integer.parseInt(             obj[8].toString());
//            Integer     currentCycleNowUsed = Integer.parseInt(                 obj[9].toString());
            Integer     currentCycleNowUsed = getMaxUsedResourceQtt(currentCycleDepPartId, currentCycleResourceId, reqest.getAppointmentId(), calendarEventsLis);
            Integer     currentCycleQuantityInDepPart = Integer.parseInt(       obj[9].toString());
            BigDecimal  currentCycleTotal = (                                   obj[10]==null?BigDecimal.ZERO:(BigDecimal)obj[10]);
            Integer     currentCycleNdsId=((Integer)                            obj[11]);
            Long        currentCycleEdIzmId = (Long.parseLong(                  obj[12].toString()));
            String      currentCycleEdIzm = ((String)                           obj[13]);
            Integer     currentCycleEdizm_type_id = ((Integer)                  obj[14]);
            BigDecimal  currentCycleEdizm_multiplier = (                        obj[15]==null?BigDecimal.ZERO:(BigDecimal)obj[15]);
            Boolean     currentCycleIs_material = ((Boolean)                    obj[16]);
            Boolean     currentCycleIndivisible = ((Boolean)                    obj[17]);
            BigDecimal  currentCyclePriceOfTypePrice = (                        obj[18]==null?BigDecimal.ZERO:(BigDecimal)obj[18]);
            Boolean     currentCycleIsEmployeeRequired=((Boolean)               obj[19]);
            Integer     currentCycleMaxPersOnSameTime=((Integer)                obj[20]);
            BigDecimal  currentCycleAtLeastBeforeTimeInSeconds=((BigDecimal)    obj[21]);
            BigDecimal  currentCycleSrvcDurationInSeconds=((BigDecimal)         obj[22]);
            BigDecimal  currentCycleUnitOfMeasureDurationInSeconds=((BigDecimal)obj[23]);
            Boolean     currentCycleServiceByAppointment = ((Boolean)           obj[24]);
            BigDecimal  currentCycleReserved =  (                               obj[25]==null?BigDecimal.ZERO:(BigDecimal)obj[25]);

            // on this cycle if it is a new user
            if (!currentCycleServiceId.equals(currentServiceId)) {

                // Если это не первый цикл
                // If it is not a first cycle
                if (!currentServiceId.equals(0L)) {

                    // В текущую часть отделения сохранили все накопленные IDs сервисов
                    departmentPartWithResourcesIds.setResourcesOfDepartmentPart(currentDepPartResources);

                    // В список частей отделения текущего пользователя добавили текущее отделение
                    departmentPartsWithResourcesIds.add(departmentPartWithResourcesIds);

                    // В текущего сотрудника поместили список частей отделений
                    appointmentService.setDepartmentPartsWithResourcesIds(departmentPartsWithResourcesIds);

                    // В итоговый список сотрудников поместили этого сотрудника
                    returnList.add(appointmentService);

                    // Cоздали новой услуги
                    appointmentService = new AppointmentService();

                    // Для новой услуги создаем новую часть отделенияи сбрасываем накопление IDs сервисов
                    currentDepPartId = currentCycleDepPartId;

                    // Cоздали новую часть отделения, и прописали туда её ID
                    departmentPartWithResourcesIds = new DepartmentPartWithResourcesIds(currentDepPartId, currentCycleDepPartName);

                    // Cбросили текущее накопление ID сервисов для новой части отделения
                    currentDepPartResources = new HashSet<>();

                }

                currentServiceId = currentCycleServiceId;

                // Для новой услуги задаём её ID, имя и её отделение
                appointmentService.setId(                           currentCycleServiceId);
                appointmentService.setName(                         currentCycleServiceName);
                appointmentService.setDepartmentId(                 currentCycleDepartmentId);
                appointmentService.setDepartmentName(               currentCycleDepartmentName);
                appointmentService.setNds_id(                       currentCycleNdsId);
                appointmentService.setEdizm_id(                     currentCycleEdIzmId);
                appointmentService.setEdizm(                        currentCycleEdIzm);
                appointmentService.setEdizm_multiplier(             currentCycleEdizm_multiplier);
                appointmentService.setEdizm_type_id(                currentCycleEdizm_type_id);
                appointmentService.setTotal(                        currentCycleTotal);
                appointmentService.setIs_material(                  currentCycleIs_material);
                appointmentService.setIndivisible(                  currentCycleIndivisible);
                appointmentService.setPriceOfTypePrice(             currentCyclePriceOfTypePrice);
                appointmentService.setEmployeeRequired(             currentCycleIsEmployeeRequired);
                appointmentService.setMaxPersOnSameTime(            currentCycleMaxPersOnSameTime);
                appointmentService.setSrvcDurationInSeconds(        currentCycleSrvcDurationInSeconds);
                appointmentService.setAtLeastBeforeTimeInSeconds(   currentCycleAtLeastBeforeTimeInSeconds);
                appointmentService.setUnitOfMeasureTimeInSeconds(   currentCycleUnitOfMeasureDurationInSeconds);
                appointmentService.setIsServiceByAppointment(       currentCycleServiceByAppointment);
                appointmentService.setReserved(                     currentCycleReserved);

                // Cоздали новый лист для накопления частей отделений для новой услуги
                departmentPartsWithResourcesIds = new ArrayList<>();
            }

            // Если сотрудник не новый, но часть отделения сменилась
            if (!currentCycleDepPartId.equals(currentDepPartId)) {

                if (!currentDepPartId.equals(0L)) {

                    // В текущую часть отделения сохранили все накопленные IDs сервисов
                    departmentPartWithResourcesIds.setResourcesOfDepartmentPart(currentDepPartResources);

                    // В список частей отделения текущего пользователя добавили текущее отделение
                    departmentPartsWithResourcesIds.add(departmentPartWithResourcesIds);

                }

                currentDepPartId = currentCycleDepPartId;

                // Cоздали новую часть отделения, и прописали туда её ID
                departmentPartWithResourcesIds = new DepartmentPartWithResourcesIds(currentDepPartId, currentCycleDepPartName);

                // Cбросили текущее накопление ID сервисов для новой части отделения
                currentDepPartResources = new HashSet<>();
            }
            if(!Objects.isNull(currentCycleResourceId)) currentDepPartResources.add(new ResourceOfDepartmentPart(
                    currentCycleResourceId,
                    currentCycleResourceName,
                    currentCycleNeedRresQtt,
                    currentCycleNowUsed,
                    currentCycleQuantityInDepPart
            ));
        }

        // По окончании цикла, если в ней что-то было
        // нужно записать последнего сотрудника
        if (!currentServiceId.equals(0L)) {

            // В текущую часть отделения сохранили все накопленные IDs сервисов
            departmentPartWithResourcesIds.setResourcesOfDepartmentPart(currentDepPartResources);

            // В список частей отделений добавили текущую часть отделения
            departmentPartsWithResourcesIds.add(departmentPartWithResourcesIds);

            // В текущую услугу поместили список частей отделений
            appointmentService.setDepartmentPartsWithResourcesIds(departmentPartsWithResourcesIds);

            // В итоговый список поместили текущую услугу
            returnList.add(appointmentService);
        }

        return returnList;
    }

    // Каждое событие (Запись) имеет список ресурсов, которые используются в услугах этого события
    // Эта функция помогает узнать, есть ли ресурс с идентификатором в списке ресурсов события
    // Each event (Appointment) has a list of resources that used in services of this event
    // This function helps to know whether resource with ID is in the list of resources of event
    private boolean isEventResourcesHasResource(Set<ItemResource> itemResources, Long resourceId){
        Boolean result=false;
        for(ItemResource resource :itemResources){
            if(resource.getId().equals(resourceId))
                result=true;
        }
        return result;
    }

    // Высчитывает максимальное используемое количество ресурса resourceId в части отделения depPartId кроме ресурса, используемого в Appointment exclAppointmentId
    // Каждый event (Запись) сревнивается с остальными, и те элементы, которые имеют пересечения "каждый с каждым" заносятся в массив.
    // Впоследствии бежим по этому массиву и суммируем нужный ресурс (bed в примере)
    // Calculates the maximum usable amount of resource resourceId in the department part depPartId other than the resource used in Appointment exclAppointmentId
    // Each event (Appointment) is compared with the others, and those elements that have intersections "each with each" are entered into the array.
    // Subsequently, we run through this array and summarize the required resource (bed in the example)

    //     Dates            |   1   2   3   4   5   6   7   8   9   10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29   30
    //   -------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //   | Room 5           |      [Event 1    bed: 2]              [Event 2    bed: 1]                         [Event 3    bed: 1]
    //   | Amount of beds:3 |                                 [Event 4    bed: 2                                                            ]
    //   -------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //  For example it'll return 2 at dates range 01 - 04, 2 at 05 - 09, 3 at 11 - 21
    //  In this example you can add an appointment only from 01 to 09 or from 23 to 30 dates, because in other dates
    //  the maximum amount of using resource "Bed" will be more than 3 (max amount beds in a room)

    private int getMaxUsedResourceQtt(Long depPartId, Long resourceId, Long exclAppointmentId, List<CalendarEventJSON> allEvents){

        int maxSumOfQueriedResource = 0;

        List<CalendarEventJSON> events = new ArrayList<>();
        DateTimeFormatter ISO8601_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        if(allEvents.size()>0){
            // Создаем локальный список событий, оставляя из общего списка только те события, что относятся к запрашиваемым части отделения и ресурсу
            // Create a local list of events, leaving from the general list only those events that relate to the queried part of the department and resource
            for(CalendarEventJSON event : allEvents){
                if(isEventResourcesHasResource(event.getMeta().getItemResources(), resourceId) && event.getMeta().getDepartmentPartId().equals(depPartId)){
                    events.add(event);
                }
            }

            for(CalendarEventJSON mainCycleEvent : events){
//          events.map(mainCycleEvent=>{
                List<CalendarEventJSON> intersectedWithEachOtherEventsGroup = new ArrayList<>();
                // группа где каждый пересекается с каждым
                // array where all have intersections "each with each"
                intersectedWithEachOtherEventsGroup.add(mainCycleEvent);

                for(CalendarEventJSON compareCycleEvent : events){
                    if(!mainCycleEvent.getId().equals(compareCycleEvent.getId())){ // сравниваем с каждым другим, но не с самим собой

                        int countOfIntersectionsWithGroupEvents = 0;

                        for(CalendarEventJSON eventOfIntersectiondGroup : intersectedWithEachOtherEventsGroup){

                            LocalDateTime compareCycleEventStart = LocalDateTime.parse(compareCycleEvent.getStart(), ISO8601_formatter);
                            LocalDateTime compareCycleEventEnd = LocalDateTime.parse(compareCycleEvent.getEnd(), ISO8601_formatter);
                            LocalDateTime intersectiondGroupEventStart = LocalDateTime.parse(eventOfIntersectiondGroup.getStart(), ISO8601_formatter);
                            LocalDateTime intersectiondGroupEventEnd = LocalDateTime.parse(eventOfIntersectiondGroup.getEnd(), ISO8601_formatter);

                            if(compareCycleEventStart.isBefore(intersectiondGroupEventEnd) && compareCycleEventEnd.isAfter(intersectiondGroupEventStart))
                                countOfIntersectionsWithGroupEvents++;
                        }
                        if(countOfIntersectionsWithGroupEvents==intersectedWithEachOtherEventsGroup.size())
                            intersectedWithEachOtherEventsGroup.add(compareCycleEvent);
                    }
                }

                // Сейчас у получившейся группы событий, у events которой есть общее одновременное пересечение, нужно получить сумму по запрашиваемому ресурсу
                int sumOfQueriedResource = 0;
                for(CalendarEventJSON eventOfIntersectiondGroup : intersectedWithEachOtherEventsGroup){
                    for( ItemResource resource : eventOfIntersectiondGroup.getMeta().getItemResources()){
                        if(resource.getId().equals(resourceId) &&
                            // не берем во внимание ресурсы из текущего докуммента // do not take into account resources from the current document
                            !eventOfIntersectiondGroup.getId().equals(exclAppointmentId) &&
                            // не берем во внимание ресурсы из отменённых документов // do not take into account resources from the cancelled documents
                            !eventOfIntersectiondGroup.getMeta().getStatusType().equals(3)  //тип статуса : 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                                                                                            //status type:  1 - normal;  2 - final positive         3 - final negative
                        )
                            sumOfQueriedResource = sumOfQueriedResource + resource.getUsedQuantity();
                    }
                }

                if(sumOfQueriedResource > maxSumOfQueriedResource)
                    maxSumOfQueriedResource=sumOfQueriedResource;
            }
        }
        return maxSumOfQueriedResource;
    }

    //ShipmentForm request
    public Long createAndCompleteShipmentFromAppointment(AppointmentsForm request){

        ShipmentForm shipmentDoc = new ShipmentForm();
        Long masterId = userRepositoryJPA.getMyMasterId();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String uuid = UUID.randomUUID().toString();
        try{
            Long departmentId=((BigInteger)commonUtilites.getFieldValueFromTableById("scdl_dep_parts","department_id",masterId,request.getDepartment_part_id())).longValue();

            shipmentDoc.setCompany_id(request.getCompany_id());
            shipmentDoc.setDepartment_id(departmentId);
            shipmentDoc.setCagent_id(request.getCagent_id());
            shipmentDoc.setStatus_id(commonUtilites.getDocumentStatus(21,2,masterId,request.getCompany_id()));
            shipmentDoc.setShipment_date(dateFormat.format(date));
            shipmentDoc.setDescription("");
            shipmentDoc.setShipment_time(timeFormat.format(date));
            shipmentDoc.setNds(request.isNds());
            shipmentDoc.setNds_included(request.isNds_included());
            shipmentDoc.setUid(uuid);
            shipmentDoc.setLinked_doc_id(request.getId());
            shipmentDoc.setLinked_doc_name("scdl_appointments");
            shipmentDoc.setParent_uid(request.getUid());
            shipmentDoc.setChild_uid(uuid);
            shipmentDoc.setIs_completed(false); // because this is only creation
            Set<ShipmentProductTableForm> shipmentProductTable = new HashSet<>();
            for (AppointmentProductsTableForm row : request.getAppointmentsProductTable()) {
                if(row.getCustomerId().equals(request.getCagent_id())){
                    ShipmentProductTableForm shipmentProduct = new ShipmentProductTableForm();
                    shipmentProduct.setDepartment_id(row.getDepartment_id());
                    shipmentProduct.setIs_material(row.getIs_material());
                    shipmentProduct.setNds_id(row.getNds_id());
                    shipmentProduct.setPrice_type_id(row.getPrice_type_id());
                    shipmentProduct.setProduct_id(row.getProduct_id());
                    shipmentProduct.setProduct_price(row.getProduct_price());
                    shipmentProduct.setProduct_count(row.getProduct_count());
                    shipmentProduct.setProduct_price_of_type_price(row.getProduct_price_of_type_price());
                    shipmentProduct.setProduct_sumprice(row.getProduct_sumprice());
                    shipmentProductTable.add(shipmentProduct);
                }
            }
            shipmentDoc.setShipmentProductTable(shipmentProductTable);

            Long resultOfShipmentCreation = (shipmentRepository.insertShipment(shipmentDoc));

            if(Objects.isNull(resultOfShipmentCreation) || resultOfShipmentCreation <= 0L)
                return resultOfShipmentCreation; // errors

            // If no errors - completing Shipment
            for (ShipmentProductTableForm shipmentProduct : shipmentProductTable){
                shipmentProduct.setShipment_id(resultOfShipmentCreation);
            }
            shipmentDoc.setId(resultOfShipmentCreation);
            shipmentDoc.setIs_completed(true);
            shipmentRepository.updateShipment(shipmentDoc);
            return resultOfShipmentCreation>0L?1L:resultOfShipmentCreation; // if everything is OK - send 1 else send error code
        }catch (Exception e) {
            logger.error("Exception in method createAndCompleteShipmentFromAppointment. ShipmentForm: "+shipmentDoc.toString()+",/ form:"+request.toString(), e);
            e.printStackTrace();
            return null;
        }
    }

    public Long createAndCompletePaymentInFromAppointment(AppointmentsForm request){
        PaymentinForm paymentDoc = new PaymentinForm();
        Long masterId = userRepositoryJPA.getMyMasterId();
        String uuid = UUID.randomUUID().toString();
        try{
            Long departmentId=((BigInteger)commonUtilites.getFieldValueFromTableById("scdl_dep_parts","department_id",masterId,request.getDepartment_part_id())).longValue();
            paymentDoc.setCompany_id(request.getCompany_id());
            paymentDoc.setDepartment_id(departmentId);
            paymentDoc.setCagent_id(request.getCagent_id());
            paymentDoc.setStatus_id(commonUtilites.getDocumentStatus(33,2,masterId,request.getCompany_id()));
            paymentDoc.setDescription("");
            paymentDoc.setSumm(request.getTotal_summ());
            paymentDoc.setNds(request.getTotal_nds());
            paymentDoc.setUid(uuid);
            paymentDoc.setLinked_doc_id(request.getId());
            paymentDoc.setLinked_doc_name("scdl_appointments");
            paymentDoc.setParent_uid(request.getUid());
            paymentDoc.setChild_uid(uuid);
            paymentDoc.setIs_completed(false); // because this is only creation
            paymentDoc.setIncome_number("");
            paymentDoc.setIncome_number_date("");
            paymentDoc.setPayment_account_id(null);// will be selected automatically
            paymentDoc.setInternal(false);
            paymentDoc.setMoving_type(null);
            Long resultOfDocumentCreation = (paymentinRepository.insertPaymentin(paymentDoc));
            if(Objects.isNull(resultOfDocumentCreation) || resultOfDocumentCreation <= 0L)
                return resultOfDocumentCreation; // errors
            // If no errors - completing Document
            paymentDoc.setId(resultOfDocumentCreation);
            paymentDoc.setIs_completed(true);
            paymentinRepository.updatePaymentin(paymentDoc);
            return resultOfDocumentCreation>0L?1L:resultOfDocumentCreation; // if everything is OK - send 1 else send error code
        }catch (Exception e) {
            logger.error("Exception in method createAndCompletePaymentInFromAppointment. form:"+request.toString(), e);
            e.printStackTrace();
            return null;
        }
    }
    public Long createAndCompleteOrderInFromAppointment(AppointmentsForm request){
        OrderinForm paymentDoc = new OrderinForm();
        Long masterId = userRepositoryJPA.getMyMasterId();
        String uuid = UUID.randomUUID().toString();
        try{
            Long departmentId=((BigInteger)commonUtilites.getFieldValueFromTableById("scdl_dep_parts","department_id",masterId,request.getDepartment_part_id())).longValue();
            paymentDoc.setCompany_id(request.getCompany_id());
            paymentDoc.setDepartment_id(departmentId);
            paymentDoc.setCagent_id(request.getCagent_id());
            paymentDoc.setStatus_id(commonUtilites.getDocumentStatus(35,2,masterId,request.getCompany_id()));
            paymentDoc.setDescription("");
            paymentDoc.setSumm(request.getTotal_summ());
            paymentDoc.setNds(request.getTotal_nds());
            paymentDoc.setUid(uuid);
            paymentDoc.setLinked_doc_id(request.getId());
            paymentDoc.setLinked_doc_name("scdl_appointments");
            paymentDoc.setParent_uid(request.getUid());
            paymentDoc.setChild_uid(uuid);
            paymentDoc.setIs_completed(false); // because this is only creation
            paymentDoc.setInternal(false);
            paymentDoc.setMoving_type(null);
            Long resultOfDocumentCreation = (orderinRepository.insertOrderin(paymentDoc));
            if(Objects.isNull(resultOfDocumentCreation) || resultOfDocumentCreation <= 0L)
                return resultOfDocumentCreation; // errors
            // If no errors - completing Document
            paymentDoc.setId(resultOfDocumentCreation);
            paymentDoc.setIs_completed(true);
            orderinRepository.updateOrderin(paymentDoc);
            return resultOfDocumentCreation>0L?1L:resultOfDocumentCreation; // if everything is OK - send 1 else send error code
        }catch (Exception e) {
            logger.error("Exception in method createAndCompleteOrderInFromAppointment. form:"+request.toString(), e);
            e.printStackTrace();
            return null;
        }
    }

    public List<AppointmentChildDocsJSON> getAppointmentChildDocs(Long docId){
        Long masterId = userRepositoryJPA.getMyMasterId();
        UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
        String myTimeZone = userSettings.getTime_zone();
        String dateFormat = userSettings.getDateFormat();
        String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24';
        String stringQuery =
        " WITH linkeddocs as (select " +
        " doc_id as doc_id, " +
        " group_id as group_id, " +
        " tablename as tablename, " +
        " scdl_appointments_id as appointment_id, " +
        " shipment_id as shipment_id, " +
        " paymentin_id as paymentin_id, " +
        " orderin_id as orderin_id " +
        " from  " +
        " linked_docs where group_id = ( " +
        "   select (group_id) from linked_docs where master_id="+masterId+" and scdl_appointments_id=" + docId +
        " )) " +
        " select  " +
        " p.id as id, " +
        " 'shipment' as doc_name, " +
        " coalesce((select sum(coalesce(product_sumprice,0)) from shipment_product where shipment_id=p.id),0) as sum, " +
        " p.cagent_id as customer_id, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0', '"+dateFormat+timeFormat+"') as date_time_created " +
        " from shipment p where " +
        " p.id in (select shipment_id from linkeddocs) " +
        " and coalesce(p.is_completed,false) = true " +
        " union all " +
        " select  " +
        " p.id as id, " +
        " 'paymentin' as doc_name, " +
        " p.summ as sum, " +
        " p.cagent_id as customer_id, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0', '"+dateFormat+timeFormat+"') as date_time_created " +
        " from paymentin p where " +
        " p.id in (select paymentin_id from linkeddocs) " +
        " and coalesce(p.is_completed,false) = true " +
        " union all " +
        " select  " +
        " p.id as id, " +
        " 'orderin' as doc_name, " +
        " p.summ as sum, " +
        " p.cagent_id as customer_id, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"' at time zone 'Etc/GMT+0', '"+dateFormat+timeFormat+"') as date_time_created " +
        " from orderin p where " +
        " p.id in (select orderin_id from linkeddocs) " +
        " and coalesce(p.is_completed,false) = true ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<AppointmentChildDocsJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                AppointmentChildDocsJSON doc = new AppointmentChildDocsJSON();
                doc.setId(Long.parseLong(                   obj[0].toString()));
                doc.setDocName((String)                     obj[1]);
                doc.setSum((BigDecimal)                     obj[2]);
                doc.setCustomerId(Long.parseLong(           obj[3].toString()));
                doc.setDocNumber(                           obj[4].toString());
                doc.setDate((String)                        obj[5]);
                returnList.add(doc);
            }
            return returnList;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method getAppointmentChildDocs.", ex);
            ex.printStackTrace();
            return null;
        }
    }

    public Integer setAppointmentChildDocumentAsDecompleted(String docName, Long docId){
        switch(docName) {
            case "shipment":
                return setShipmentAsDecompleted(docId);
            case "paymentin":
                return setPaymentinAsDecompleted(docId);
            default: // "orderin"
                return setOrderinAsDecompleted(docId);
        }
    }

    private Integer setShipmentAsDecompleted(Long docId){
        ShipmentForm docToSend = new ShipmentForm();
        try{
            ShipmentJSON doc = shipmentRepository.getShipmentValuesById(docId);
            List<ShipmentProductTableJSON> product_table=shipmentRepository.getShipmentProductTable(docId);
            docToSend.setId(docId);
            docToSend.setCompany_id(doc.getCompany_id());
            docToSend.setCagent_id(doc.getCagent_id());
            docToSend.setDoc_number(doc.getDoc_number().toString());
            docToSend.setStatus_id(doc.getStatus_id());
            docToSend.setDepartment_id(doc.getDepartment_id());
            Set<ShipmentProductTableForm> docToSendProducts  = new HashSet<>();
            for(ShipmentProductTableJSON product : product_table){
                ShipmentProductTableForm sendProduct = new ShipmentProductTableForm();
                sendProduct.setShipment_id(docId);
                sendProduct.setProduct_id(product.getProduct_id());
                sendProduct.setProduct_sumprice(product.getProduct_sumprice());
    //            sendProduct.setProduct_price_of_type_price(product.getPro);
                sendProduct.setProduct_price(product.getProduct_price());
                sendProduct.setPrice_type_id(product.getPrice_type_id());
                sendProduct.setProduct_count(product.getProduct_count());
                sendProduct.setNds_id(product.getNds_id());
                sendProduct.setNds(product.getNds());
                sendProduct.setIs_material(product.getIs_material());
                sendProduct.setDepartment_id(product.getDepartment_id());
                sendProduct.setAdditional(product.getAdditional());
                sendProduct.setName(product.getName());
                sendProduct.setId(product.getId());
                sendProduct.setPrice_type(product.getPrice_type());
                docToSendProducts.add(sendProduct);
            }
            docToSend.setShipmentProductTable(docToSendProducts);
            return shipmentRepository.setShipmentAsDecompleted(docToSend);
        }catch (Exception e) {
            logger.error("Exception in method setShipmentAsDecompleted. Id:"+docId+", ShipmentForm: "+docToSend.toString(), e);
            e.printStackTrace();
            return null;
        }
    }
    private Integer setPaymentinAsDecompleted(Long docId){
        PaymentinForm docToSend = new PaymentinForm();
        try{
            PaymentinJSON doc = paymentinRepository.getPaymentinValuesById(docId);
            docToSend.setId(docId);
            docToSend.setInternal(doc.getInternal());
            docToSend.setMoving_type(doc.getMoving_type());
            docToSend.setSumm(doc.getSumm());
            docToSend.setPayment_account_id(doc.getPayment_account_id());
            docToSend.setCompany_id(doc.getCompany_id());
            docToSend.setCagent_id(doc.getCagent_id());
            docToSend.setDoc_number(doc.getDoc_number().toString());
            docToSend.setStatus_id(doc.getStatus_id());
            return paymentinRepository.setPaymentinAsDecompleted(docToSend);
        }catch (Exception e) {
            logger.error("Exception in method setPaymentinAsDecompleted. Id:"+docId+", PaymentinForm: "+docToSend.toString(), e);
            e.printStackTrace();
            return null;
        }
    }

    private Integer setOrderinAsDecompleted(Long docId){
        OrderinForm docToSend = new OrderinForm();
        try{
            OrderinJSON doc = orderinRepository.getOrderinValuesById(docId);
            docToSend.setId(docId);
            docToSend.setInternal(doc.getInternal());
            docToSend.setMoving_type(doc.getMoving_type());
            docToSend.setBoxoffice_id(doc.getBoxoffice_id());
            docToSend.setSumm(doc.getSumm());
            docToSend.setCompany_id(doc.getCompany_id());
            docToSend.setCagent_id(doc.getCagent_id());
            docToSend.setDoc_number(doc.getDoc_number().toString());
            docToSend.setStatus_id(doc.getStatus_id());
            return orderinRepository.setOrderinAsDecompleted(docToSend);
        }catch (Exception e) {
            logger.error("Exception in method setOrderinAsDecompleted. Id:"+docId+", OrderinForm: "+docToSend.toString(), e);
            e.printStackTrace();
            return null;
        }
    }
//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    public boolean addFilesToAppointment(UniversalForm request){
        Long appointmentId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта, ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(59L,"712") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments",appointmentId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(59L,"713") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",appointmentId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(59L,"714") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",appointmentId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(59L,"715") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",appointmentId.toString())))
        {
            try
            {
                String stringQuery;
                Long masterId = userRepositoryJPA.getMyMasterId();
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {
                    commonUtilites.idBelongsMyMaster("files", fileId, masterId);
                    stringQuery = "select appointment_id from scdl_appointment_files where appointment_id=" + appointmentId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_AppointmentId_FileId(appointmentId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method addFilesToAppointment.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_AppointmentId_FileId(Long appointmentId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into scdl_appointment_files " +
                    "(appointment_id,file_id) " +
                    "values " +
                    "(" + appointmentId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method manyToMany_AppointmentId_FileId. ", ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesUniversalJSON> getListOfAppointmentFiles(Long appointmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(59L, "708,709,710,711"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           scdl_appointments p" +
                    "           inner join" +
                    "           scdl_appointment_files pf" +
                    "           on p.id=pf.appointment_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + appointmentId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";

            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                if (needToSetParameter_MyDepthsIds) {
                    query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
                }

                List<Object[]> queryList = query.getResultList();

                List<FilesUniversalJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    FilesUniversalJSON doc = new FilesUniversalJSON();
                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setDate_time_created((Timestamp) obj[1]);
                    doc.setName((String) obj[2]);
                    doc.setOriginal_name((String) obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            }
            catch (Exception e) {
                logger.error("Exception in method getListOfAppointmentsFiles. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteAppointmentFile(long file_id, long doc_id)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(59L,"712") && securityRepositoryJPA.isItAllMyMastersDocuments("scdl_appointments", String.valueOf(doc_id))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(59L,"713") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("scdl_appointments",String.valueOf(doc_id)))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(59L,"714") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("scdl_appointments",String.valueOf(doc_id)))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(59L,"715") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("scdl_appointments",String.valueOf(doc_id))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from scdl_appointment_files "+
                    " where appointment_id=" + doc_id+
                    " and file_id="+file_id+
                    " and (select master_id from scdl_appointments where id="+doc_id+")="+myMasterId;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method deleteAppointmentFile. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}
