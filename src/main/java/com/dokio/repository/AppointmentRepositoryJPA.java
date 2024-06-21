package com.dokio.repository;

import com.dokio.message.request.Settings.SettingsAppointmentForm;
import com.dokio.message.request.additional.AppointmentCustomer;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.response.Settings.SettingsAppointmentJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.request.AppointmentsForm;
import com.dokio.message.request.AppointmentProductsTableForm;
import com.dokio.message.response.AppointmentsJSON;
import com.dokio.message.response.additional.AppointmentProductsTableJSON;
import com.dokio.message.response.additional.AppointmentUpdateReportJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.appointment.AppointmentService;
import com.dokio.message.response.additional.appointment.DepartmentPartWithResourcesIds;
import com.dokio.message.response.additional.appointment.ResourceOfDepartmentPart;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","store","name","cagent","status_name","sum_price","hasSellReceipt","company","department","creator","date_time_created_sort","shipment_date_sort","description","is_completed","product_count")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************

    public List<AppointmentsJSON> getAppointmentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, long companyId, long departmentId, Set<Integer> filterOptionsIds) {
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
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from scdl_appointment_products where appointment_id=p.id),0) as sum_price, " +
                    "           (select count(*) from scdl_appointment_products ip where ip.appointment_id=p.id) as product_count," + //подсчет кол-ва услуг или товаров
                    "           to_char(p.date_start,   'DD.MM.YYYY') as date_start, " +
                    "           to_char(p.time_start,   'HH24:MI')    as time_start, " +
                    "           to_char(p.date_end,     'DD.MM.YYYY') as date_end, " +
                    "           to_char(p.time_end,     'HH24:MI')    as time_end, " +
                    "           uo.name as owner, " +
                    "           p.name as name" +
                    "           from scdl_appointments p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uo ON p.owner_id=uo.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.shipment_date, 'DD.MM.YYYY') = CONCAT('%',:sg,'%') or "+
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
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
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
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
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getAppointmentsSize(String searchString, long companyId, long departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from scdl_appointments p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users uo ON p.owner_id=uo.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.shipment_date, 'DD.MM.YYYY') = CONCAT('%',:sg,'%') or "+
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
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
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }

        Query query = entityManager.createNativeQuery(stringQuery);

        if (searchString != null && !searchString.isEmpty())
        {query.setParameter("sg", searchString);}

        if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
        {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

        return query.getResultList().size();
    }

    public List<AppointmentProductsTableJSON> getAppointmentsProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(59L, "708,709,710,711"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.appointment_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " ap.edizm_id," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = ap.edizm_id) as edizm," +
                    " ap.nds_id," +
                    " nds.name as nds," +
                    " ap.price_type_id," +
                    " (select pt.name from sprav_type_prices pt where pt.id = ap.price_type_id) as price_type, " +
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = ap.department_id),0) as total, "+ //всего на складе (т.е остаток)
                    " (select " +
                    "   sum(coalesce(reserved_current,0)-0) " +
                    "   from " +
                    "   customers_orders_product " +
                    "   where " +
                    "   product_id=ap.product_id "+
                    "   and department_id = ap.department_id) as reserved, "+//зарезервировано в документах Заказ покупателя
                    " ap.department_id as department_id, " +
                    " (select name from departments where id= ap.department_id) as department, "+
                    " ap.id  as row_id, " +
                    " ppr.name_api_atol as ppr_name_api_atol, " +
                    " ppr.is_material as is_material, " +
//                    " 0 as reserved_current, " +//зарезервировано в данном документе (a так как в Букингах нет резервирования - то всегда 0)
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce(nds.value,0) as nds_value," +

                    " coalesce(p.is_srvc_by_appointment, false) as is_srvc_by_appointment, " +
                    " coalesce(p.scdl_is_employee_required, false) as scdl_is_employee_required, " +
                    " coalesce(p.scdl_max_pers_on_same_time, 1) as scdl_max_pers_on_same_time, " +
                    " coalesce(p.scdl_srvc_duration, 1) as scdl_srvc_duration, " +
                    " coalesce(p.scdl_appointment_atleast_before_time, 0) as scdl_appointment_atleast_before_time, " +
                    " p.scdl_appointment_atleast_before_unit_id as scdl_appointment_atleast_before_unit_id " +

                    " from " +
                    " scdl_appointment_products ap " +
                    " INNER JOIN scdl_appointments a ON ap.appointment_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " LEFT OUTER JOIN sprav_taxes nds ON nds.id = ap.nds_id" +
                    " where a.master_id = " + myMasterId +
                    " and ap.appointment_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<AppointmentProductsTableJSON> returnList = new ArrayList<>();
            int row_num = 1; // номер строки при выводе печатной версии
            for(Object[] obj:queryList){
                AppointmentProductsTableJSON doc=new AppointmentProductsTableJSON();
                doc.setRow_num(row_num);
                doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                doc.setAppointment_id(Long.parseLong(                   obj[1].toString()));
                doc.setProduct_count(                                   obj[2]==null?BigDecimal.ZERO:(BigDecimal)obj[2]);
                doc.setProduct_price(                                   obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                doc.setProduct_sumprice(                                obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                doc.setEdizm_id(obj[7]!=null?Long.parseLong(            obj[5].toString()):null);
                doc.setName((String)                                    obj[6]);
                doc.setEdizm((String)                                   obj[7]);
                doc.setNds_id(Long.parseLong(                           obj[8].toString()));
                doc.setNds((String)                                     obj[9]);
                doc.setPrice_type_id(obj[10]!=null?Long.parseLong(      obj[10].toString()):null);
                doc.setPrice_type((String)                              obj[11]);
                doc.setTotal(                                           obj[12]==null?BigDecimal.ZERO:(BigDecimal)obj[12]);
                doc.setReserved(                                        obj[13]==null?BigDecimal.ZERO:(BigDecimal)obj[13]);
                doc.setDepartment_id(Long.parseLong(                    obj[14].toString()));
                doc.setDepartment((String)                              obj[15]);
                doc.setId(Long.parseLong(                               obj[16].toString()));
                doc.setPpr_name_api_atol((String)                       obj[17]);
                doc.setIs_material((Boolean)                            obj[18]);
                doc.setIndivisible((Boolean)                            obj[19]);
                doc.setNds_value((BigDecimal)                           obj[20]);
                doc.setIs_srvc_by_appointment((Boolean)                 obj[21]);// this service is selling by appointments
                doc.setScdl_is_employee_required((Boolean)              obj[22]);// a service provider is needed only at the start
                doc.setScdl_max_pers_on_same_time((Integer)             obj[23]);// the number of persons to whom a service can be provided at a time by one service provider (1 - dentist or hairdresser, 5-10 - yoga class)
                doc.setScdl_srvc_duration((Integer)                     obj[24]);// time minimal duration of the service.
                doc.setScdl_appointment_atleast_before_time((Integer)   obj[25]);// minimum time before the start of the service for which customers can make an appointment
                doc.setScdl_appointment_atleast_before_unit_id((Integer)obj[26]);// the unit of measure of minimum time before the start of the service for which customers can make an appointment
                returnList.add(doc);
                row_num++;
            }
            return returnList;
        } else return null;
    }

    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public AppointmentsJSON getAppointmentsValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(59L, "708,709,710,711"))//см. _Permissions Id.txt
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24';
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           uo.name as owner, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           p.dep_part_id as dep_part_id, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           coalesce(dp.price_id,0) as department_type_price_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           coalesce((select id from sprav_type_prices where company_id=p.company_id and is_default=true),0) as default_type_price_id, " +
                    "           p.uid as uid, " +
                    "           to_char(p.date_start,   'DD.MM.YYYY') as date_start, " +
                    "           to_char(p.time_start,   'HH24:MI')    as time_start, " +
                    "           to_char(p.date_end,     'DD.MM.YYYY') as date_end, " +
                    "           to_char(p.time_end,     'HH24:MI')    as time_end, " +
                    "           concat(to_char(p.date_start,'YYYY-MM-DD'), 'T', to_char(p.time_start,'HH24:MI:SS.MS'), 'Z')," +
                    "           concat(to_char(p.date_end,'YYYY-MM-DD'), 'T', to_char(p.time_end,'HH24:MI:SS.MS'), 'Z')" +
                    "           from scdl_appointments p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           INNER JOIN users uo ON p.owner_id=uo.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           LEFT OUTER JOIN sprav_sys_countries ctr ON p.country_id=ctr.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(59L, "708")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(59L, "709")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(59L, "710")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            AppointmentsJSON returnObj=new AppointmentsJSON();

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                         obj[0].toString()));
                returnObj.setCreator((String)                           obj[1]);
                returnObj.setChanger((String)                           obj[2]);
                returnObj.setOwner((String)                             obj[3]);
                returnObj.setCompany_id(Long.parseLong(                 obj[4].toString()));
                returnObj.setDepartment_id(Long.parseLong(              obj[5].toString()));
                returnObj.setDep_part_id(Long.parseLong(                obj[6].toString()));
                returnObj.setDoc_number(Long.parseLong(                 obj[7].toString()));
                returnObj.setCompany((String)                           obj[8]);
                returnObj.setDate_time_created((String)                 obj[9]);
                returnObj.setDate_time_changed((String)                 obj[10]);
                returnObj.setDescription((String)                       obj[11]);
                returnObj.setIs_completed((Boolean)                     obj[12]);
                returnObj.setDepartment_type_price_id(Long.parseLong(   obj[13].toString()));
                returnObj.setNds((Boolean)                              obj[14]);
                returnObj.setNds_included((Boolean)                     obj[15]);
                returnObj.setStatus_id(obj[16]!=null?Long.parseLong(    obj[16].toString()):null);
                returnObj.setStatus_name((String)                       obj[17]);
                returnObj.setStatus_color((String)                      obj[18]);
                returnObj.setStatus_description((String)                obj[19]);
                returnObj.setDefault_type_price_id(Long.parseLong(      obj[20].toString()));
                returnObj.setUid((String)                               obj[21]);
                returnObj.setDate_start((String)                        obj[22]);
                returnObj.setTime_start((String)                        obj[23]);
                returnObj.setDate_end((String)                          obj[24]);
                returnObj.setTime_end((String)                          obj[25]);
                returnObj.setCalendar_date_time_start((String)          obj[26]);
                returnObj.setCalendar_date_time_end((String)            obj[27]);
            }
            return returnObj;
        } else return null;
    }


    @SuppressWarnings("Duplicates")
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
//                        logger.error("Exception in method insertAppointment on inserting into scdl_appointment_products. ", e);
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
        AppointmentUpdateReportJSON updateResults = new AppointmentUpdateReportJSON();// отчет об апдейте
        Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment
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
            try
            {
                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "scdl_appointments"))
                    throw new DocumentAlreadyCompletedException();
                // если документ проводится и нет товаров - ошибка
                if(request.getIs_completed()!=null && request.getIs_completed() && request.getAppointmentsProductTable().size()==0) throw new CantInsertProductRowCauseErrorException();

                Long masterId = userRepositoryJPA.getMyMasterId();
                Long myId = userRepository.getUserId();

                // commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                // !!!!!!!!!!!!!!!! Сделать проверку списка клиентов на принадлежность к masterId !!!!!!!!!!!!!

                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), masterId);

                // сохранение всего кроме таблицы товаров
                updateAppointmentWithoutTable(request);

                //сохранение и удаление контрагентов и формирование списка <row_id - id> для контрагентов и товаров
                Map<Integer,Long> cagentsMap = saveAppointmentCagents(request, myId, masterId);

                // сохранение таблицы товаров
                insertAppointmentProducts(request, cagentsMap, request.getId(), masterId);

                // отмечаем товары как необходимые для синхронизации с WooCommerce //???
                for (AppointmentProductsTableForm row : request.getAppointmentsProductTable()) {
                    productsIdsToSyncWoo.add(row.getProduct_id());
                }
                productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, masterId);

                // возвращаем весть об успешности операции
                updateResults.setSuccess(true);
                return updateResults;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateAppointment.", e);
                e.printStackTrace();
                updateResults.setSuccess(false);
                updateResults.setErrorCode(-50);      // Документ уже проведён
                return updateResults;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateAppointment on updating of scdl_appointment_productss cause error.", e);
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

        } else {
            updateResults.setSuccess(false);
            updateResults.setErrorCode(0);          // Недостаточно прав
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
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AppointmentsRepository/setAppointmentAsDecompleted.", e);
                e.printStackTrace();
                return null;
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
                saveAppointmentProductsTable(row, myMasterId);
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

    private void saveAppointmentProductsTable(AppointmentProductsTableForm row, Long master_id) throws CantInsertProductRowCauseErrorException {
        String stringQuery="";
        if(Objects.isNull(row.getReserved_current())) row.setReserved_current(new BigDecimal(0));
        try {
            stringQuery =
                    " insert into scdl_appointment_products (" +
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
                            row.getDepartment_id() + ", " +
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
                            " department_id = " + row.getDepartment_id() + ","+
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
    private Boolean updateAppointmentWithoutTable(AppointmentsForm request) throws Exception {
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

        if (!commonUtilites.isTimeValid(request.getTime_start()) || !commonUtilites.isTimeValid(request.getTime_end()))
            throw new IllegalArgumentException("Invalid query parameters (time):"+request.getTime_start()+", "+request.getTime_end());
        if (!commonUtilites.isDateValid(request.getDate_start()) || !commonUtilites.isDateValid(request.getDate_end()))
            throw new IllegalArgumentException("Invalid query parameters (date):"+request.getDate_start()+", "+request.getDate_end());

        String stringQuery;
        stringQuery =   "update scdl_appointments set " +
                " changer_id = " + myId + ", "+
                " owner_id = " + myId + ", "+  // later it can be changed
                " date_time_changed= now()," +
                " description = :description, " +
                " date_start = to_date('"+request.getDate_start()+"', 'DD.MM.YYYY')," +
                " date_end = to_date('"+request.getDate_end()+"', 'DD.MM.YYYY')," +
                " time_start = to_timestamp('"+request.getTime_start()+"', 'HH24:MI')," +
                " time_end = to_timestamp('"+request.getTime_end()+"',   'HH24:MI')," +
                " nds  = " + request.isNds() + ", " +
                " nds_included  = " + request.isNds_included() + ", " +
                " doc_number = " + request.getDoc_number() + ", " +
                " email = :email, " +
                " telephone = :telephone, " +
                " zip_code = :zip_code, " +
                " is_completed  = " + request.getIs_completed() + ", " +
                " status_id = " + request.getStatus_id() +
                " where " +
                " id= "+request.getId();
        try
        {
            Date dateNow = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
            Query query = entityManager.createNativeQuery(stringQuery);
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
    public Boolean saveSettingsAppointments(SettingsAppointmentForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);
            commonUtilites.idBelongsMyMaster("departments", row.getDepartmentId(), myMasterId);
            // commonUtilites.idBelongsMyMaster("cagents", row.getCustomerId(), myMasterId);
            // !!!!!!!!!!!!!!!! Сделать проверку списка клиентов на принадлежность к masterId !!!!!!!!!!!!!


            commonUtilites.idBelongsMyMaster("sprav_status_dock", row.getStatusIdOnAutocreateOnCheque(), myMasterId);

            stringQuery =
                    " insert into settings_appointments (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "hide_tenths, " +       //убирать десятые (копейки) - boolean
                            "department_id, " +     //отделение по умолчанию
                            "priority_type_price_side, "+ // приоритет типа цены: Склад (sklad) Покупатель (cagent) Цена по-умолчанию (defprice)
                            "date_time_update, " +
                            "autocreate_on_start , "+//автосоздание на старте документа, если автозаполнились все поля
                            "status_id_on_autocreate_on_cheque"+//Перед автоматическим созданием после успешного отбития чека документ сохраняется. Данный статус - это статус документа при таком сохранении
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            row.getHideTenths() + "," +
                            row.getDepartmentId() + "," +
                            ":priorityTypePriceSide,"+
                            "now(), " +
                            row.getAutocreateOnStart()+
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_appointments_user_uq " +// "upsert"
                            " DO update set " +
                            " hide_tenths = " + row.getHideTenths() + ","+
                            ", department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", date_time_update = now()" +
                            ", priority_type_price_side = :priorityTypePriceSide"+
                            ", autocreate_on_start = "+row.getAutocreateOnStart()+
                            ", status_id_on_autocreate_on_cheque = " + row.getStatusIdOnAutocreateOnCheque();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("priorityTypePriceSide", row.getPriorityTypePriceSide());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsAppointments. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    public SettingsAppointmentJSON getSettingsAppointments() {
        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           coalesce(p.hide_tenths,false) as hide_tenths, " +
                "           p.department_id as department_id, " +
                "           p.id as id, " +
                "           p.company_id as company_id, " +
                "           coalesce(p.priority_type_price_side,'defprice') as priority_type_price_side," +
                "           coalesce(p.autocreate_on_start,false) as autocreate_on_start," +
                "           p.status_id_on_autocreate_on_cheque as status_id_on_autocreate_on_cheque " +
                "           from settings_appointments p " +
                "           LEFT OUTER JOIN cagents cg ON p.customer_id=cg.id " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            if(queryList.size()==0) throw new NoResultException();
            SettingsAppointmentJSON returnObj=new SettingsAppointmentJSON();
            for(Object[] obj:queryList){
                returnObj.setHideTenths((Boolean)                       obj[0]);
                returnObj.setDepartmentId(obj[1]!=null?Long.parseLong(  obj[1].toString()):null);
                returnObj.setId(Long.parseLong(                         obj[2].toString()));
                returnObj.setCompanyId(Long.parseLong(                  obj[3].toString()));
                returnObj.setPriorityTypePriceSide((String)             obj[4]);
                returnObj.setAutocreateOnStart((Boolean)                obj[5]);
                returnObj.setStatusIdOnAutocreateOnCheque(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
            }
            return returnObj;
        } catch (NoResultException nre) {
            return null;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsAppointments. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

    //  удаляет лишние позиции товаров при сохранении документа (те позиции, которые ранее были в документе, но потом их удалили)
    private void deleteAppointmentProductsTableExcessRows(String rowIds, Long appointment_id, Long myMasterId) throws Exception {
        String stringQuery="";
        try {
            stringQuery =   " delete from scdl_appointment_products " +
                    " where appointment_id=" + appointment_id +
                    " and master_id=" + myMasterId +
                    (rowIds.length()>0?(" and id not in (" + rowIds.replaceAll("[^0-9\\,]", "") + ")"):"");//если во фронте удалили все товары, то удаляем все товары в данном Заказе покупателя
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


    public List<AppointmentService> getAppointmentServicesList(AppointmentMainInfoForm reqest) {

        Long masterId = userRepositoryJPA.getMyMasterId();
        String myTimeZone = userRepository.getUserTimeZone();


        String stringQuery =
                " WITH busy_resources AS ( " +
//              busy_resources - Это выборка с занятыми ресурсами в заданном промежутке времени в виде: / ID ресурса / Название / Используемое количество " +
                        " select  " +
                        " r.id as resource_id,  " +
                        " r.name as resource,  " +
                        " dp.id as dep_part_id, " +
                        " dp.name as dep_part, " +
                        " pr.quantity as quantity_now_used  " + //-- кол-во используемого ресурса во всех Appointments
                        " from   " +
                        " scdl_appointments a, " +
                        " scdl_appointment_products ap,  " +
                        " products p, " +
                        " scdl_product_resource_qtt pr, " +
                        " sprav_resources r, " +
                        " scdl_dep_parts dp, " +
                        " scdl_resource_dep_parts_qtt rdp " +
                        " where " +
                        " p.master_id=" + masterId + " and " +
                        " p.company_id=" + reqest.getCompanyId() + " and " +
                        " ap.appointment_id=a.id and " +
                        " ap.product_id=p.id and " +
                        " pr.product_id=p.id and " +
                        " pr.resource_id=r.id and  " +
                        " dp.id=a.dep_part_id and " +
                        " rdp.dep_part_id=a.dep_part_id and " +
                        " 	a.id != " + reqest.getAppointmentId() + " and  " + //-- filtering by parent Appointment document
                        " rdp.resource_id=r.id and " +
                        " to_timestamp ('" + reqest.getDateFrom() + " " + reqest.getTimeFrom() + "', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '" + myTimeZone + "' < a.ends_at_time and " +
                        " to_timestamp ('" + reqest.getDateTo() + " " + reqest.getTimeTo() + "', 'DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '" + myTimeZone + "' > a.starts_at_time  " +
                        ") " +

                        " select  " +
                        " p.id,  " +
                        " p.name,  " +
                        " coalesce(d.id,0) as department_id, " +
                        " coalesce(d.name,'') as department, " +
                        " coalesce(dp.id,0) as dep_part_id, " +
                        " coalesce(dp.name,'') as dep_part_name, " +
                        " r.id as resource_id, " +
                        " coalesce(r.name,'') as resource_name, " +
                        " coalesce(prq.quantity,0) as need_res_qtt, " +
                        " coalesce((select sum(quantity_now_used) from busy_resources where resource_id=r.id and dep_part_id=dp.id),0) as now_used,  " +
//                " -- coalesce(br.quantity_now_used,0) as now_used,  " +
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
                        " left outer join product_prices pp on pp.product_id = p.id " +
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
                        " coalesce(dp.is_deleted,false)=false and  " +
                        " case when coalesce(p.is_srvc_by_appointment,false) = true then (coalesce(dp.is_active,false)=true) else true end and " +
                        " case when coalesce(p.is_srvc_by_appointment,false) = true then asg.assignment_type = :asg else true end and " +
                        " pp.price_type_id = "+reqest.getPriceTypeId();
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
            Integer     currentCycleNowUsed = Integer.parseInt(                 obj[9].toString());
            Integer     currentCycleQuantityInDepPart = Integer.parseInt(       obj[10].toString());
            BigDecimal  currentCycleTotal = (                                   obj[11]==null?BigDecimal.ZERO:(BigDecimal)obj[11]);
            Integer     currentCycleNdsId=((Integer)                            obj[12]);
            Long        currentCycleEdIzmId = (Long.parseLong(                  obj[13].toString()));
            String      currentCycleEdIzm = ((String)                           obj[14]);
            Integer     currentCycleEdizm_type_id = ((Integer)                  obj[15]);
            BigDecimal  currentCycleEdizm_multiplier = (                        obj[16]==null?BigDecimal.ZERO:(BigDecimal)obj[16]);
            Boolean     currentCycleIs_material = ((Boolean)                    obj[17]);
            Boolean     currentCycleIndivisible = ((Boolean)                    obj[18]);
            BigDecimal  currentCyclePriceOfTypePrice = (                        obj[19]==null?BigDecimal.ZERO:(BigDecimal)obj[19]);
            Boolean     currentCycleIsEmployeeRequired=((Boolean)               obj[20]);
            Integer     currentCycleMaxPersOnSameTime=((Integer)                obj[21]);
            BigDecimal  currentCycleAtLeastBeforeTimeInSeconds=((BigDecimal)    obj[22]);
            BigDecimal  currentCycleSrvcDurationInSeconds=((BigDecimal)         obj[23]);
            BigDecimal  currentCycleUnitOfMeasureDurationInSeconds=((BigDecimal)obj[24]);
            Boolean     currentCycleServiceByAppointment = ((Boolean)           obj[25]);
            BigDecimal  currentCycleReserved =  (                               obj[26]==null?BigDecimal.ZERO:(BigDecimal)obj[26]);

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
//            currentDepPartResources.add(Objects.isNull(currentCycleResourceId) ? new ResourceOfDepartmentPart() : new ResourceOfDepartmentPart(
//                    currentCycleResourceId,
//                    currentCycleResourceName,
//                    currentCycleNeedRresQtt,
//                    currentCycleNowUsed,
//                    currentCycleQuantityInDepPart
//            ));
        }

        // По окончании цикла, если в ней что-то было
        // нужно записать последнего сотрудника
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





    }
