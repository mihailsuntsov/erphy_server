/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.repository;

import com.dokio.message.request.AcceptanceForm;
import com.dokio.message.request.AcceptanceProductForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsAcceptanceForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.AcceptanceJSON;
import com.dokio.message.response.Settings.CompanySettingsJSON;
import com.dokio.message.response.Settings.SettingsAcceptanceJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesAcceptanceJSON;
import com.dokio.message.response.ProductHistoryJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.repository.Exceptions.*;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
public class AcceptanceRepository {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;

    private Logger logger = Logger.getLogger("AcceptanceRepository");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("sum_price","doc_number","status_name","product_count","is_completed","acceptance_date_sort","company","department","cagent","creator","date_time_created_sort","description")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));
    //*****************************************************************************************************************************************************
    //****************************************************      MENU      *********************************************************************************
    //*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<AcceptanceJSON> getAcceptanceTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'

            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.acceptance_date   at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as acceptance_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.acceptance_date as acceptance_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           (select count(*) from acceptance_product ip where coalesce(ip.acceptance_id,0)=p.id) as product_count," + //подсчет кол-ва товаров
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from acceptance_product where acceptance_id=p.id),0) as sum_price " +
                    "           from acceptance p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +

                        " to_char(p.acceptance_date, 'DD.MM.YYYY') = :sg or "+
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cg.name) like  upper(CONCAT('%',:sg,'%')) or "+
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
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<AcceptanceJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    AcceptanceJSON doc=new AcceptanceJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setNds((Boolean)                          obj[8]);
                    doc.setNds_included((Boolean)                 obj[9]);
                    doc.setOverhead((BigDecimal)                  obj[10]);
                    doc.setDepartment_id(Long.parseLong(          obj[11].toString()));
                    doc.setDepartment((String)                    obj[12]);
                    doc.setCagent_id(Long.parseLong(              obj[13].toString()));
                    doc.setCagent((String)                        obj[14]);
                    doc.setDoc_number(Long.parseLong(             obj[15].toString()));
                    doc.setAcceptance_date((String)(              obj[16]));
                    doc.setCompany((String)                       obj[17]);
                    doc.setDate_time_created((String)             obj[18]);
                    doc.setDate_time_changed((String)             obj[19]);
                    doc.setDescription((String)                   obj[20]);
                    doc.setIs_completed((Boolean)                 obj[21]);
                    doc.setOverhead_netcost_method((Integer)      obj[25]);
                    doc.setStatus_id(obj[26]!=null?Long.parseLong(obj[26].toString()):null);
                    doc.setStatus_name((String)                   obj[27]);
                    doc.setStatus_color((String)                  obj[28]);
                    doc.setStatus_description((String)            obj[29]);
                    doc.setProduct_count(Long.parseLong(          obj[30].toString()));
                    doc.setSum_price((BigDecimal)                 obj[31]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getAcceptanceTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getAcceptanceSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from acceptance p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +

                    " to_char(p.acceptance_date, 'DD.MM.YYYY') = :sg or "+
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cg.name) like  upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getAcceptanceSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<AcceptanceProductForm> getAcceptanceProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.acceptance_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " ap.product_netcost," +
                    " ap.nds_id," +
                    " p.edizm_id," +
                    " p.name as name," +
                    " (select nds.name from sprav_taxes nds where nds.id = p.nds_id) as nds," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = p.edizm_id) as edizm," +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = a.department_id),0) as total, "+ //всего на складе (т.е остаток)
                    " coalesce(nds.value,0) as nds_value" +
                    " from " +
                    " acceptance_product ap " +
                    " INNER JOIN acceptance a ON ap.acceptance_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " LEFT OUTER JOIN sprav_taxes nds ON nds.id = ap.nds_id" +
                    " where a.master_id = " + myMasterId +
                    " and ap.acceptance_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and a.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);
            try{
                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<AcceptanceProductForm> returnList = new ArrayList<>();
                int row_num = 1; // номер строки при выводе печатной версии
                for(Object[] obj:queryList){
                    AcceptanceProductForm doc=new AcceptanceProductForm();
                    doc.setRow_num(row_num);
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setAcceptance_id(Long.parseLong(                    obj[1].toString()));
                    doc.setProduct_count((BigDecimal)                       obj[2]);
                    doc.setProduct_price((BigDecimal)                       obj[3]);
                    doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                    doc.setProduct_netcost((BigDecimal)                     obj[5]);
                    doc.setNds_id((Integer)                                 obj[6]);
                    doc.setEdizm_id(obj[7]!=null?Long.parseLong(            obj[7].toString()):null);
                    doc.setName((String)                                    obj[8]);
                    doc.setNds((String)                                     obj[9]);
                    doc.setEdizm((String)                                   obj[10]);
                    doc.setIndivisible((Boolean)                            obj[11]);
                    doc.setTotal((BigDecimal)                               obj[12]);
                    doc.setNds_value((BigDecimal)                           obj[13]);
                    returnList.add(doc);
                    row_num++;
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getAcceptanceProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public AcceptanceJSON getAcceptanceValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24';
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.acceptance_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as acceptance_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.acceptance_date as acceptance_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.uid as uid, " +
                    "           to_char(p.acceptance_date at time zone '"+myTimeZone+"', 'HH24:MI') as acceptance_time " +
                    "           from acceptance p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                AcceptanceJSON returnObj=new AcceptanceJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                     obj[0].toString()));
                    returnObj.setMaster((String)                        obj[1]);
                    returnObj.setCreator((String)                       obj[2]);
                    returnObj.setChanger((String)                       obj[3]);
                    returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                    returnObj.setNds((Boolean)                          obj[8]);
                    returnObj.setNds_included((Boolean)                 obj[9]);
                    returnObj.setOverhead((BigDecimal)                  obj[10]);
                    returnObj.setDepartment_id(Long.parseLong(          obj[11].toString()));
                    returnObj.setDepartment((String)                    obj[12]);
                    returnObj.setCagent_id(Long.parseLong(              obj[13].toString()));
                    returnObj.setCagent((String)                        obj[14]);
                    returnObj.setDoc_number(Long.parseLong(             obj[15].toString()));
                    returnObj.setAcceptance_date((String)(              obj[16]));
                    returnObj.setCompany((String)                       obj[17]);
                    returnObj.setDate_time_created((String)             obj[18]);
                    returnObj.setDate_time_changed((String)             obj[19]);
                    returnObj.setDescription((String)                   obj[20]);
                    returnObj.setIs_completed((Boolean)                 obj[21]);
                    returnObj.setOverhead_netcost_method((Integer)      obj[25]);
                    returnObj.setStatus_id(obj[26]!=null?Long.parseLong(obj[26].toString()):null);
                    returnObj.setStatus_name((String)                   obj[27]);
                    returnObj.setStatus_color((String)                  obj[28]);
                    returnObj.setStatus_description((String)            obj[29]);
                    returnObj.setUid((String)                           obj[30]);
                    returnObj.setAcceptance_time((String)               obj[31]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getAcceptanceValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, RuntimeException.class, CantInsertProductRowCauseErrorException.class})
    public Long insertAcceptance(AcceptanceForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDoc( request.getCompany_id(), request.getDepartment_id(), 15L, "184", "185", "192");
        if(iCan==Boolean.TRUE)
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDocId;
            Long doc_number;//номер документа
            Long linkedDocsGroupId=null;

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"acceptance");

            // статус по умолчанию (если не выбран)
            if(request.getStatus_id()==null)
                request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(), 15));

            //если документ создается из другого документа
            if (request.getLinked_doc_id() != null) {
                //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
            }

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =
                    " insert into acceptance (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " date_time_created," + //дата и время создания
                    " cagent_id," +//поставщик
                    " nds," +
                    " nds_included," +
                    " overhead," +
                    " overhead_netcost_method," +
                    " doc_number," + //номер заказа
                    " description," +//доп. информация по заказу
                    " status_id," + //статус
                    " acceptance_date, " +// дата поставки
                    " linked_docs_group_id, "+ // id группы связанных документов
                    " uid"+
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    request.getCagent_id() + ", "+
                    request.isNds() + ", "+
                    request.isNds_included() + ", "+
                    request.getOverhead() + ", "+
                    request.getOverhead_netcost_method() + ", "+
                    doc_number + ", "+//номер заказа
                    " :description, " +//описание
                    request.getStatus_id() + ", "+//статус
//                    " to_date(:acceptance_date,'DD.MM.YYYY'), " +
                    "to_timestamp(CONCAT(:acceptance_date,' ',:acceptance_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"'," +
                    linkedDocsGroupId+","+
                    ":uid)";
            try {

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("acceptance_date", ((request.getAcceptance_date()==null || request.getAcceptance_date().equals("")) ? dateFormat.format(dateNow) : request.getAcceptance_date()));
                query.setParameter("acceptance_time", ((request.getAcceptance_time()==null || request.getAcceptance_time().equals("")) ? timeFormat.format(dateNow) : request.getAcceptance_time()));
                query.setParameter("uid",request.getUid());
                query.executeUpdate();
                stringQuery = "select id from acceptance where creator_id=" + myId + " and date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS'))";
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDocId = Long.valueOf(query2.getSingleResult().toString());


                if(insertAcceptanceProducts(request, newDocId, myMasterId)){
                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "acceptance", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    return newDocId;
                } else return null;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertAcceptance on inserting into acceptance_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertAcceptance. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(Objects.isNull(iCan)) return null; else return -1L;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class, CantSaveProductQuantityException.class, InsertProductHistoryExceprions.class,Exception.class})
    public  Integer updateAcceptance(AcceptanceForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого редактируют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                        !(      //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(15L,"611") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(15L,"612") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",request.getId().toString()))||
                                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                                (securityRepositoryJPA.userHasPermissions_OR(15L,"613") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",request.getId().toString()))||
                                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                                (securityRepositoryJPA.userHasPermissions_OR(15L,"614") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",request.getId().toString()))
                        )
                ) return -1;
            }

            Long myMasterId = userRepositoryJPA.getMyMasterId();
            BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всей приёмке

                try {//сохранение таблицы

                    // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                    if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "acceptance"))
                        throw new DocumentAlreadyCompletedException();

                    //апдейт основного документа, без таблицы товаров
                    updateAcceptanceWithoutTable(request,myMasterId);

                    //апдейт товаров
                    insertAcceptanceProducts(request, request.getId(), myMasterId);

                    //если приЁмка ПРОВОДИТСЯ - запись в историю товара
                    if(request.getIs_completed()){

                        //сохранение истории движения товара
                        for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                            docProductsSum=docProductsSum.add(row.getProduct_sumprice());

                            addProductHistory(row, request, myMasterId);
                        }
                        // обновляем баланс с контрагентом
                        commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "acceptance","acceptance", request.getId(), docProductsSum,new BigDecimal(0),true, request.getDoc_number().toString(),request.getStatus_id());//при приёмке баланс с контрагентом должен смещаться в положительную сторону, т.е. в наш долг контрагенту
                    }
                    return 1;
                } catch (DocumentAlreadyCompletedException e) { //
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance.", e);
                    e.printStackTrace();
                    return -50; // см. _ErrorCodes
                }catch (CalculateNetcostNegativeSumException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost.", e);
                    e.printStackTrace();
                    return -70; // см. _ErrorCodes
                } catch (CantSaveProductQuantityException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance on inserting into product_quantity cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantInsertProductRowCauseErrorException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance on inserting into acceptance_products cause error.", e);
                    e.printStackTrace();
                    return null;
                } catch (CantSaveProductHistoryException e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance on inserting into products_history.", e);
                    e.printStackTrace();
                    return null;
                } catch (Exception e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    logger.error("Exception in method updateAcceptance.", e);
                    e.printStackTrace();
                    return null;
                }
        } else return -1;//недостаточно прав
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CalculateNetcostNegativeSumException.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setAcceptanceAsDecompleted(AcceptanceForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(15L,"611") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",request.getId().toString())) ||
            //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(15L,"612") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",request.getId().toString()))||
            //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(15L,"613") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",request.getId().toString()))||
            //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(15L,"614") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",request.getId().toString()))
        )
        {
            if(request.getAcceptanceProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update acceptance set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "acceptance"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();



                //сохранение истории движения товара
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                request.setIs_completed(false);
                BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всем товарам документа
                for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                    docProductsSum=docProductsSum.add(row.getProduct_sumprice());
                    addProductHistory(row, request, myMasterId);
                }
                // обновляем баланс с контрагентом
                commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "acceptance","acceptance", request.getId(), docProductsSum,new BigDecimal(0),false, request.getDoc_number().toString(),request.getStatus_id());//при приёмке баланс с контрагентом должен смещаться в положительную сторону, т.е. в наш долг контрагенту
                return 1;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AcceptanceRepository/addProductHistory on inserting into products_history cause oversell.", e);
                e.printStackTrace();
                return -80;
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (setAcceptanceAsDecompleted).", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AcceptanceRepository/setAcceptanceAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AcceptanceRepository/setAcceptanceAsDecompleted.", e);
                e.printStackTrace();
                return -80; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method AcceptanceRepository/setAcceptanceAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    @SuppressWarnings("Duplicates")
    private Boolean updateAcceptanceWithoutTable(AcceptanceForm request, Long myMasterId) throws Exception {

        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
        String stringQuery;
        stringQuery = " update acceptance set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, "+
                " doc_number =" + request.getDoc_number() + "," +
                " nds =" + request.isNds() + "," +
                " nds_included =" + request.isNds_included() + "," +
                " overhead =" + request.getOverhead() + "," +                               //расходы
                " overhead_netcost_method =" + request.getOverhead_netcost_method() + "," + //Распределение затрат на себестоимость товаров. 0 - нет, 1 - по весу цены в поставке
                " is_completed = " + request.getIs_completed() + "," +
                " acceptance_date = to_timestamp(CONCAT(:acceptance_date,' ',:acceptance_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"',"+
                " status_id = " + request.getStatus_id() +
                " where " +
                " id= "+request.getId() +
                " and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
            query.setParameter("acceptance_date", (request.getAcceptance_date() == "" ? null :request.getAcceptance_date()));
            query.setParameter("acceptance_time", ((request.getAcceptance_time()==null || request.getAcceptance_time().equals("")) ? "00:00" : request.getAcceptance_time()));

            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method AcceptanceRepository/updateAcceptanceWithoutTable. stringQuery=" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    //сохранение таблицы товаров
    @SuppressWarnings("Duplicates")
    private boolean insertAcceptanceProducts(AcceptanceForm request, Long parentDocId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Set<Long> productIds=new HashSet<>();

        if (request.getAcceptanceProductTable()!=null && request.getAcceptanceProductTable().size() > 0) {
            for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                row.setAcceptance_id(parentDocId);// т.к. он может быть неизвестен при создании документа
                if (!saveAcceptanceProductTable(row, myMasterId)) {
                    throw new CantInsertProductRowCauseErrorException();
                }
                productIds.add(row.getProduct_id());
            }
        }
        if (!deleteAcceptanceProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", request.getId())){
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    private Boolean saveAcceptanceProductTable(AcceptanceProductForm row, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        String stringQuery;

        stringQuery =   " insert into acceptance_product (" +
                "product_id," +
                "acceptance_id," +
                "product_count," +
                "product_price," +
                "product_sumprice," +
                "product_netcost," +
                "nds_id," +
                "edizm_id" +
                ") values ("
                + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит
                + "(select id from acceptance where id="+row.getAcceptance_id() +" and master_id="+myMasterId+"),"
                + row.getProduct_count() + ","
                + row.getProduct_price() +","
                + row.getProduct_sumprice() +","
                + row.getProduct_netcost() +","
                + row.getNds_id() +","
                + row.getEdizm_id() +")" +
                "ON CONFLICT ON CONSTRAINT acceptance_product_uq " +// "upsert"
                " DO update set " +
                " product_id = " + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+")," +
                " acceptance_id = "+ "(select id from acceptance where id="+row.getAcceptance_id() +" and master_id="+myMasterId+")," +
                " product_count = " + row.getProduct_count() + "," +
                " product_price = " + row.getProduct_price() + "," +
                " product_sumprice = " + row.getProduct_sumprice() + "," +
                " product_netcost = " + row.getProduct_netcost() +"," +
                " nds_id = " + row.getNds_id() +"," +
                " edizm_id = "+ row.getEdizm_id();
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method AcceptanceRepository/saveAcceptanceProductTable. SQL query:"+stringQuery, e);
            throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    private Boolean deleteAcceptanceProductTableExcessRows(String productIds, Long acceptance_id) {
        String stringQuery;

        stringQuery =   " delete from acceptance_product " +
                " where acceptance_id=" + acceptance_id +
                " and product_id not in (" + productIds.replaceAll("[^0-9\\,]", "") + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method AcceptanceRepository/deleteAcceptanceProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean addProductHistory(AcceptanceProductForm row, AcceptanceForm request , Long masterId) throws Exception {
        try {

            // все записи в таблицы product_history и product_quantity производим только если товар материален (т.е. это не услуга и т.п.)
            if (productsRepository.isProductMaterial(row.getProduct_id())) {
                // загружаем настройки, чтобы узнать политику предприятия по подсчёту себестоимости (по всему предприятию или по каждому отделению отдельно)
                String netcostPolicy = commonUtilites.getCompanySettings(request.getCompany_id()).getNetcost_policy();
                // берём информацию о товаре (кол-во и ср. себестоимость) в данном отделении (если netcostPolicy == "all" то независимо от отделения)
                ProductHistoryJSON productInfo = productsRepository.getProductQuantityAndNetcost(masterId, request.getCompany_id(), row.getProduct_id(), netcostPolicy.equals("each") ? request.getDepartment_id() : null);
                // актуальное количество товара В ОТДЕЛЕНИИ
                // Используется для записи нового кол-ва товара в отделении путем сложения lastQuantity и row.getProduct_count()
                // если политика подсчета себестоимости ПО КАЖДОМУ отделению (each) - то lastQuantity отдельно высчитывать не надо - она уже высчитана шагом ранее в productInfo
                BigDecimal lastQuantity =  netcostPolicy.equals("each") ? productInfo.getQuantity() : productsRepository.getProductQuantity(masterId, request.getCompany_id(), row.getProduct_id(), request.getDepartment_id());
                // имеющееся количество (если учёт себестоимости по отделениям - то В ОТДЕЛЕНИИ, если по всему предприятию - то кол-во ВО ВСЕХ ОТДЕЛЕНИЯХ.)
                // Используется для расчёта себестоимости
                BigDecimal availableQuantity = netcostPolicy.equals("each") ? lastQuantity : productInfo.getQuantity();
                // средняя себестоимость уже имеющегося товара
                BigDecimal lastAvgNetcostPrice = productInfo.getAvg_netcost_price();

                // т.к. это  операция поступления, при отмене её проведения необходимо проверить,
                // сколько товара останется после этого, и если это кол-во <0 то не допустить этого
                if(!request.getIs_completed() && (lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                    logger.error("Acceptance with id = "+request.getId()+", doc number "+request.getDoc_number()+": the quantity of product to be disposed of from the department is greater than the quantity of product in the department");
                    throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
                }

//                Timestamp timestamp = new Timestamp(((Date) commonUtilites.getFieldValueFromTableById("acceptance", "date_time_created", masterId, request.getId())).getTime());

                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        request.getDepartment_id(),
                        15,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count(),
                        row.getProduct_price(),
                        row.getProduct_netcost(), // себестоимость товара в операции
//                        timestamp,
                        request.getIs_completed()
                );

                // новая средняя себестоимость
                BigDecimal avgNetcostPrice;
                if (request.getIs_completed())   // Если проводим, то считаем по формуле
                    // ((ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
                    avgNetcostPrice = ((availableQuantity.multiply(lastAvgNetcostPrice)).add(row.getProduct_count().multiply(row.getProduct_netcost()))).divide(availableQuantity.add(row.getProduct_count()), 2, BigDecimal.ROUND_HALF_UP);
                else // Если снимаем с проведения, то пересчитываем на основании прежних движений товара
                    avgNetcostPrice = productsRepository.recountProductNetcost(request.getCompany_id(), request.getDepartment_id(), row.getProduct_id());

                if (request.getIs_completed())   // Если проводим
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            request.getDepartment_id(),
                            lastQuantity.add(row.getProduct_count()),
                            avgNetcostPrice
                    );
                else                            // Если снимаем с проведения
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            request.getDepartment_id(),
                            lastQuantity.subtract(row.getProduct_count()),
                            avgNetcostPrice
                    );
            }

            return true;

        }catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (addProductHistory).", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantSaveProductQuantityException e) {
            logger.error("Exception in method addAcceptanceProductHistory on inserting into product_quantity cause error.", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantInsertProductRowCauseOversellException e) {
            logger.error("Exception in method addAcceptanceProductHistory on inserting into product_quantity cause error - Not enough product count.", e);
            e.printStackTrace();
            throw new CantInsertProductRowCauseOversellException();
        } catch (CantSaveProductHistoryException e) {
            logger.error("Exception in method addAcceptanceProductHistory on inserting into product_history.", e);
            e.printStackTrace();
            throw new CantSaveProductHistoryException();
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method AcceptanceRepository/addAcceptanceProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    public List<LinkedDocsJSON> getAcceptanceLinkedDocsList(Long docId, String docName) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String tableName=(docName.equals("return")?"return":"");
        stringQuery =   " select " +
                " ap.id," +
                " to_char(ap.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI'), " +
                " ap.description," +
                " coalesce(ap.is_completed,false)," +
                " ap.doc_number" +
                " from "+docName+" ap" +
                " where ap.master_id = " + myMasterId +
                " and coalesce(ap.is_deleted,false)!=true "+
                " and ap.acceptance_id = " + docId;
        stringQuery = stringQuery + " order by ap.date_time_created asc ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                LinkedDocsJSON doc=new LinkedDocsJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setDate_time_created((String)               obj[1]);
                doc.setDescription((String)                     obj[2]);
                doc.setIs_completed((Boolean)                   obj[3]);
                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getAcceptanceLinkedDocsList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteAcceptance (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"186") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(15L,"187") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",delNumbers))||
            //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(15L,"193") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",delNumbers))||
            //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(15L,"194") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "acceptance");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update acceptance p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")"+
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        Query query = entityManager.createNativeQuery(stringQuery);
                        query.executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "acceptance")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteAcceptance. SQL query:" + stringQuery, e);
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
        public Integer undeleteAcceptance (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"186") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"187") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"193") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"194") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update acceptance p" +
                    " set is_deleted=false, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteAcceptance. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
//*****************************************************************************************************************************************************
//***************************************************      UTILS      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")  //генератор номера документа
    private Long generateDocNumberCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from acceptance where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isAcceptanceNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long product_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from acceptance where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(product_id>0) stringQuery=stringQuery+" and id !="+product_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.getResultList().size()>0)
                return false;// код не уникальный
            else return true; // код уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isAcceptanceNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }



//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToAcceptance(UniversalForm request){
        Long acceptanceId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта, ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",acceptanceId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",acceptanceId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",acceptanceId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",acceptanceId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select acceptance_id from acceptance_files where acceptance_id=" + acceptanceId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_AcceptanceId_FileId(acceptanceId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method addFilesToAcceptance.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_AcceptanceId_FileId(Long acceptanceId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into acceptance_files " +
                    "(acceptance_id,file_id) " +
                    "values " +
                    "(" + acceptanceId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method manyToMany_AcceptanceId_FileId. ", ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesAcceptanceJSON> getListOfAcceptanceFiles(Long acceptanceId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//Просмотр документов
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
                    "           acceptance p" +
                    "           inner join" +
                    "           acceptance_files pf" +
                    "           on p.id=pf.acceptance_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + acceptanceId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                List<FilesAcceptanceJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    FilesAcceptanceJSON doc = new FilesAcceptanceJSON();
                    doc.setId(Long.parseLong(obj[0].toString()));
                    doc.setDate_time_created((Timestamp) obj[1]);
                    doc.setName((String) obj[2]);
                    doc.setOriginal_name((String) obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            }
            catch (Exception e) {
                logger.error("Exception in method getListOfAcceptanceFiles. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteAcceptanceFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from acceptance_files "+
                    " where acceptance_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from acceptance where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method deleteAcceptanceFile. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsAcceptance(SettingsAcceptanceForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_acceptance (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "department_id, " +         // отделение по умолчанию
                            "status_on_finish_id, "+    // статус документа при завершении инвентаризации
                            "auto_add,"+                // автодобавление товара из формы поиска в таблицу
                            "auto_price"+               // автодоматическое подставление последней закупочной цены
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            row.getDepartmentId() + "," +
                            row.getStatusOnFinishId() + "," +
                            row.getAutoAdd() + "," +
                            row.getAutoPrice() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_acceptance_user_uq " +// "upsert"
                            " DO update set " +
                            "  department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", auto_add = "+row.getAutoAdd()+
                            ", auto_price = "+row.getAutoPrice();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsAcceptance. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsAcceptanceJSON getSettingsAcceptance() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add, " +                 // автодобавление товара из формы поиска в таблицу
                "           coalesce(p.auto_price,false) as auto_price " +              // автодоматическое подставление последней закупочной цены
                "           from settings_acceptance p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsAcceptanceJSON acceptanceObj=new SettingsAcceptanceJSON();

            for(Object[] obj:queryList){
                acceptanceObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                acceptanceObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                acceptanceObj.setStatusOnFinishId(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                acceptanceObj.setAutoAdd((Boolean)                              obj[3]);
                acceptanceObj.setAutoPrice((Boolean)                            obj[4]);
            }
            return acceptanceObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsAcceptance. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

}