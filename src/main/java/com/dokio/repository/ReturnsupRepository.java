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

import com.dokio.message.request.ReturnsupProductTableForm;
import com.dokio.message.response.ReturnsupProductTableJSON;
import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsReturnsupForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsReturnsupJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesReturnsupJSON;
import com.dokio.message.response.additional.ReturnsupProductsListJSON;
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
public class ReturnsupRepository {

    Logger logger = Logger.getLogger("ReturnsupRepository");

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
    private LinkedDocsUtilites linkedDocsUtilites;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    ProductsRepositoryJPA productsRepository;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("cagent","doc_number","name","status_name","product_count","is_completed","company","department","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<ReturnsupJSON> getReturnsupTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            boolean needToSetParameter_MyDepthsIds = false;
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           cg.name as cagent, " +
                    "           (select count(*) from returnsup_product ip where coalesce(ip.returnsup_id,0)=p.id) as product_count," + //подсчет кол-ва товаров в данной инвентаризации
                    "           coalesce(p.is_completed,false) as is_completed " +  //  завершен?

                    "           from returnsup p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
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
                List<ReturnsupJSON> returnsupList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ReturnsupJSON doc=new ReturnsupJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    doc.setDepartment((String)                    obj[9]);
                    doc.setDoc_number(Long.parseLong(             obj[10].toString()));
                    doc.setCompany((String)                       obj[11]);
                    doc.setDate_time_created((String)             obj[12]);
                    doc.setDate_time_changed((String)             obj[13]);
                    doc.setDescription((String)                   obj[14]);
                    doc.setStatus_id(obj[17]!=null?Long.parseLong(obj[17].toString()):null);
                    doc.setStatus_name((String)                   obj[18]);
                    doc.setStatus_color((String)                  obj[19]);
                    doc.setStatus_description((String)            obj[20]);
                    doc.setCagent((String)                        obj[21]);
                    doc.setProduct_count(Long.parseLong(          obj[22].toString()));
                    doc.setIs_completed((Boolean)                 obj[23]);

                    returnsupList.add(doc);
                }
                return returnsupList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnsupTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getReturnsupSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from returnsup p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(cg.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(uc.name)  like upper(CONCAT('%',:sg,'%')) or "+
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
            logger.error("Exception in method getReturnsupSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<ReturnsupProductTableJSON> getReturnsupProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ip.id  as id, " +
                    " p.name as name," +
                    " ip.product_id," +
                    " ip.product_price," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                    " ip.product_count," +
                    " ip.product_sumprice," +
                    " ip.nds_id, " +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce((select quantity from product_quantity where product_id = ip.product_id and department_id = i.department_id),0) as remains, "+ //всего на складе (т.е остаток)
                    " coalesce(nds.value,0) as nds_value, " +
                    " ppr.is_material as is_material " +

                    " from " +
                    " returnsup_product ip " +
                    " INNER JOIN products p ON ip.product_id=p.id " +
                    " INNER JOIN returnsup i ON ip.returnsup_id=i.id " +
                    " INNER JOIN sprav_sys_ppr ppr ON p.ppr_id=ppr.id " +
                    " LEFT OUTER JOIN sprav_taxes nds ON nds.id = ip.nds_id" +
                    " where ip.master_id = " + myMasterId +
                    " and ip.returnsup_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and i.company_id=" + myCompanyId+" and i.department_id in :myDepthsIds and i.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and i.company_id=" + myCompanyId+" and i.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and i.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<ReturnsupProductTableJSON> returnsupList = new ArrayList<>();
                int row_num = 1; // номер строки при выводе печатной версии
                for(Object[] obj:queryList){
                    ReturnsupProductTableJSON doc=new ReturnsupProductTableJSON();
                    doc.setRow_num(row_num);
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setName((String)                                    obj[1]);
                    doc.setProduct_id(Long.parseLong(                       obj[2].toString()));
                    doc.setProduct_price(                                   obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                    doc.setEdizm((String)                                   obj[4]);
                    doc.setProduct_count(                                   obj[5]==null?BigDecimal.ZERO:(BigDecimal)obj[5]);
                    doc.setProduct_sumprice(                                obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                    doc.setNds_id((Integer)                                 obj[7]);
                    doc.setIndivisible((Boolean)                            obj[8]);
                    doc.setRemains((BigDecimal)                             obj[9]);
                    doc.setNds_value((BigDecimal)                           obj[10]);
                    doc.setIs_material((Boolean)                            obj[11]);
                    returnsupList.add(doc);
                    row_num++;
                }
                return returnsupList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnsupProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }


    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public ReturnsupJSON getReturnsupValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//см. _Permissions Id.txt
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           to_char(p.date_return at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_return, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +  // инвентаризация завершена?
                    "           cg.id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.nds as nds, " +
                    "           p.uid as uid, " +
                    "           to_char(p.date_return at time zone '"+myTimeZone+"', 'HH24:MI') as return_time " +

                    "           from returnsup p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;


            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                ReturnsupJSON doc = new ReturnsupJSON();

                for(Object[] obj:queryList){
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setMaster((String)                        obj[1]);
                    doc.setCreator((String)                       obj[2]);
                    doc.setChanger((String)                       obj[3]);
                    doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                    doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    doc.setDepartment((String)                    obj[9]);
                    doc.setDoc_number(Long.parseLong(             obj[10].toString()));
                    doc.setCompany((String)                       obj[11]);
                    doc.setDate_time_created((String)             obj[12]);
                    doc.setDate_time_changed((String)             obj[13]);
                    doc.setDescription((String)                   obj[14]);
                    doc.setStatus_id(obj[15]!=null?Long.parseLong(obj[15].toString()):null);
                    doc.setStatus_name((String)                   obj[16]);
                    doc.setStatus_color((String)                  obj[17]);
                    doc.setStatus_description((String)            obj[18]);
                    doc.setDate_return((String)                   obj[19]);
                    doc.setIs_completed((Boolean)                 obj[20]);
                    doc.setCagent_id(Long.parseLong(              obj[21].toString()));
                    doc.setCagent((String)                        obj[22]);
                    doc.setNds((Boolean)                          obj[23]);
                    doc.setUid((String)                           obj[24]);
                    doc.setReturn_time((String)                   obj[25]);

                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReturnsupValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, RuntimeException.class,CantInsertProductRowCauseErrorException.class, CantInsertProductRowCauseOversellException.class})
    public Integer updateReturnsup(ReturnsupForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(29L,"372") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(29L,"373") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(29L,"374") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(29L,"375") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                        !(      //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(29L,"615") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(29L,"616") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",request.getId().toString()))||
                                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                                (securityRepositoryJPA.userHasPermissions_OR(29L,"617") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",request.getId().toString()))||
                                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                                (securityRepositoryJPA.userHasPermissions_OR(29L,"618") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",request.getId().toString()))
                        )
                ) return -1;
            }
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();
            BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всему возврату
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment
            String stringQuery;
            stringQuery =   "update returnsup set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, "+
                    " nds = " + request.getNds() + ", " +
//                    " date_return = to_date(:date_return,'DD.MM.YYYY'), " +
                    " date_return = to_timestamp(CONCAT(:date_return,' ',:time_return),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"',"+
                    " is_completed = " + (request.getIs_completed() == null ? false : request.getIs_completed()) + ", " +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);

                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "returnsup"))
                    throw new DocumentAlreadyCompletedException();

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("date_return", ((request.getDate_return()==null || request.getDate_return().equals("")) ? dateFormat.format(dateNow) : request.getDate_return()));
                query.setParameter("time_return", ((request.getReturn_time()==null || request.getReturn_time().equals("")) ? "00:00" : request.getReturn_time()));
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));

                query.executeUpdate();

                //сохранение товаров из таблицы товаров
                insertReturnsupProducts(request, request.getId(), myMasterId);

                //если возврат ПРОВОДИТСЯ - запись в историю товара
                if(request.getIs_completed()){
                    //сохранение истории движения товара
                    for (ReturnsupProductTableForm row : request.getReturnsupProductTable()) {
                        docProductsSum=docProductsSum.add(row.getProduct_sumprice());
                        addProductHistory(row, request, myMasterId);
                        productsIdsToSyncWoo.add(row.getProduct_id());
                    }
                    // обновляем баланс с контрагентом
                    commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "returnsup", "returnsup", request.getId(), new BigDecimal(0), docProductsSum,true,request.getDoc_number(),request.getStatus_id());
                    // отмечаем товары как необходимые для синхронизации с WooCommerce
                    productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, myMasterId);
                }
                return 1;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/updateReturnsup.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method ReturnsupRepository/updateReturnsup.", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/updateReturnsup on updating returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductHistoryException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/addReturnsupProductHistory on updating returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductQuantityException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/setReturnsupQuantity on updating returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/addProductHistory on inserting into product_history cause oversell.", e);
                e.printStackTrace();
                return -80;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/updateReturnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CalculateNetcostNegativeSumException.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setReturnsupAsDecompleted(ReturnsupForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(29L,"615") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",request.getId().toString())) ||
                        //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(29L,"616") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",request.getId().toString()))||
                        //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                        (securityRepositoryJPA.userHasPermissions_OR(29L,"617") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",request.getId().toString()))||
                        //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                        (securityRepositoryJPA.userHasPermissions_OR(29L,"618") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",request.getId().toString()))
        )
        {
            if(request.getReturnsupProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment
            String stringQuery =
                    " update returnsup set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "returnsup"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();

                //сохранение истории движения товара
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                request.setIs_completed(false);
                BigDecimal docProductsSum = new BigDecimal(0); // для накопления итоговой суммы по всем товарам документа

                for (ReturnsupProductTableForm row : request.getReturnsupProductTable()) {
                    docProductsSum=docProductsSum.add(row.getProduct_sumprice());
                    addProductHistory(row, request, myMasterId);
                    productsIdsToSyncWoo.add(row.getProduct_id());
                }
                // обновляем баланс с контрагентом
                commonUtilites.addDocumentHistory("cagent", request.getCompany_id(), request.getCagent_id(), "returnsup","returnsup", request.getId(), docProductsSum,new BigDecimal(0),false, request.getDoc_number().toString(),request.getStatus_id());//при приёмке баланс с контрагентом должен смещаться в положительную сторону, т.е. в наш долг контрагенту
                // отмечаем товары как необходимые для синхронизации с WooCommerce
                productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, myMasterId);
                return 1;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/addProductHistory on inserting into product_history cause oversell.", e);
                e.printStackTrace();
                return -80;
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (setReturnsupAsDecompleted).", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/setReturnsupAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/setReturnsupAsDecompleted.", e);
                e.printStackTrace();
                return -80; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method ReturnsupRepository/setReturnsupAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    @SuppressWarnings("Duplicates")
    private Boolean addProductHistory(ReturnsupProductTableForm row, ReturnsupForm request , Long masterId) throws Exception {
        try {
            // все записи в таблицы product_history и product_quantity производим только если товар материален (т.е. это не услуга и т.п.)
            if (productsRepository.isProductMaterial(row.getProduct_id())) {
                // загружаем настройки, чтобы узнать политику предприятия по подсчёту себестоимости (по всему предприятию или по каждому отделению отдельно)
                String netcostPolicy = commonUtilites.getCompanySettings(request.getCompany_id()).getNetcost_policy();
                // берём информацию о товаре (кол-во и ср. себестоимость) в данном отделении (если netcostPolicy == "all" то независимо от отделения)
                ProductHistoryJSON productInfo = productsRepository.getProductQuantityAndNetcost(masterId, request.getCompany_id(), row.getProduct_id(), netcostPolicy.equals("each") ? request.getDepartment_id() : null);
                // актуальное количество товара В ОТДЕЛЕНИИ
                // Используется для записи нового кол-ва товара в отделении путем вычитания row.getProduct_count() из lastQuantity
                // если политика подсчета себестоимости ПО КАЖДОМУ отделению - lastQuantity отдельно высчитывать не надо - она уже высчитана шагом ранее в productInfo
                BigDecimal lastQuantity =  netcostPolicy.equals("each") ? productInfo.getQuantity() : productsRepository.getProductQuantity(masterId, request.getCompany_id(), row.getProduct_id(), request.getDepartment_id());
                // средняя себестоимость уже имеющегося товара
                BigDecimal lastAvgNetcostPrice = productInfo.getAvg_netcost_price();

                // т.к. это  операция "не поступления" (а убытия), при ее проведении необходимо проверить,
                // сколько товара останется после ее проведения, и если это кол-во <0 то не допустить этого
                if(request.getIs_completed() && (lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                    logger.error("Return to the supplier with id = "+request.getId()+", doc number "+request.getDoc_number()+": the quantity of product to be disposed of from the department is greater than the quantity of product in the department");
                    throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
                }

//                Timestamp timestamp = new Timestamp(((Date) commonUtilites.getFieldValueFromTableById("returnsup", "date_time_created", masterId, request.getId())).getTime());

                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), masterId);
                commonUtilites.idBelongsMyMaster("departments", request.getDepartment_id(), masterId);
                commonUtilites.idBelongsMyMaster("products", row.getProduct_id(), masterId);

                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        request.getDepartment_id(),
                        29,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count().negate(),
                        row.getProduct_price(),
                        lastAvgNetcostPrice,// в операциях не поступления товара себестоимость равна актуальной на момент данной операции себестоимости
//                        timestamp,
                        request.getIs_completed()
                );

                if (request.getIs_completed())   // Если проводим
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            request.getDepartment_id(),
                            lastQuantity.subtract(row.getProduct_count()),
                            lastAvgNetcostPrice
                    );
                else                            // Если снимаем с проведения
                    productsRepository.setProductQuantity(
                            masterId, row.getProduct_id(),
                            request.getDepartment_id(),
                            lastQuantity.add(row.getProduct_count()),
                            lastAvgNetcostPrice
                    );
            } else{
                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        request.getDepartment_id(),
                        29,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count().negate(),
                        row.getProduct_price(),
                        new BigDecimal("0"),
                        request.getIs_completed()
                );
            }

            return true;

        } catch (CantInsertProductRowCauseOversellException e) { //т.к. весь метод обёрнут в try, данное исключение ловим сначала здесь и перекидываем в родительский метод updateReturnsup
            e.printStackTrace();
            logger.error("Exception in method ReturnsupRepository/addProductHistory (CantInsertProductRowCauseOversellException). ", e);
            throw new CantInsertProductRowCauseOversellException();
        }catch (CalculateNetcostNegativeSumException e) {
            logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (addProductHistory).", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantSaveProductQuantityException e) {
            logger.error("Exception in method ReturnsupRepository/addProductHistory on inserting into product_quantity cause error.", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantSaveProductHistoryException e) {
            logger.error("Exception in method ReturnsupRepository/addProductHistory on inserting into product_history.", e);
            e.printStackTrace();
            throw new CantSaveProductHistoryException();
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method ReturnsupRepository/addProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class})
    public Long insertReturnsup(ReturnsupForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDoc( request.getCompany_id(), request.getDepartment_id(), 29L, "361", "362", "363");
        if(iCan==Boolean.TRUE)
        {

            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDocId;
            Long doc_number;//номер документа
            Long linkedDocsGroupId=null;

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"returnsup");

            // статус по умолчанию (если не выбран)
            if(request.getStatus_id()==null)
                request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(), 29));

            //если документ создается из другого документа
            if (request.getLinked_doc_id() != null) {
                //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
            }

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery =
                    " insert into returnsup (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " cagent_id," + //покупатель, возвращающий заказ
                    " date_time_created," + //дата и время создания
                    " doc_number," + //номер заказа
                    " date_return," + //дата возврата
                    " description," +//доп. информация по заказу
                    " status_id,"+//статус инвентаризации
                    " linked_docs_group_id," +// id группы связанных документов
                    " uid, "+// уникальный идентификатор документа
                    " nds" +
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    request.getCagent_id() + ", "+//покупатель, возвращающий заказ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    "to_timestamp(CONCAT(:date_return,' ',:time_return),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"'," +// дата и время возврата
                    " :description, " +//описание
                    request.getStatus_id() + ", " + //статус док-та
//                    request.getAcceptance_id() + ", " + //id родительского документа Розничная продажа, из которого может быть создан возврат
                    linkedDocsGroupId+"," + // id группы связанных документов
                    ":uid, "+// уникальный идентификатор документа
                    request.getNds()+")";
            try{
                commonUtilites.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("departments", request.getDepartment_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("cagents", request.getCagent_id(), myMasterId);
                commonUtilites.idBelongsMyMaster("sprav_status_dock", request.getStatus_id(), myMasterId);

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("date_return", ((request.getDate_return()==null || request.getDate_return().equals("")) ? dateFormat.format(dateNow) : request.getDate_return()));
                query.setParameter("time_return", ((request.getReturn_time()==null || request.getReturn_time().equals("")) ? timeFormat.format(dateNow) : request.getReturn_time()));
                query.setParameter("uid",request.getUid());

                query.executeUpdate();
                stringQuery="select id from returnsup where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDocId=Long.valueOf(query2.getSingleResult().toString());

                if(insertReturnsupProducts(request, newDocId, myMasterId)){
                    //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                    if (request.getLinked_doc_id() != null) {
                        linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "returnsup", request.getUid(), request.getCompany_id(), myMasterId);
                    }
                    return newDocId;
                } else return null;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertReturnsup on inserting into returnsup_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertReturnsup on inserting into returnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(iCan==null) return null; else return 0L;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertReturnsupProducts(ReturnsupForm request, Long newDocId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        String productIds = "";
        //сохранение таблицы
        if (request.getReturnsupProductTable()!=null && request.getReturnsupProductTable().size() > 0) {

            for (ReturnsupProductTableForm row : request.getReturnsupProductTable()) {
                row.setReturnsup_id(newDocId);
                insertProductRowResult = saveReturnsupProductTable(row, request.getCompany_id(), myMasterId);  //сохранение таблицы товаров
                if (insertProductRowResult==null) {
                    throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции из-за ошибки записи строки в таблицу товаров returnsup_product
                }
                //копим id сохранённых товаров
                productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
            }
        }
        deleteReturnsupProductTableExcessRows(productIds, request.getId(), myMasterId);
        return true;
    }

    @SuppressWarnings("Duplicates")//  удаляет лишние позиции товаров при сохранении инвентаризации (те позиции, которые ранее были в заказе, но потом их удалили)
    private Boolean deleteReturnsupProductTableExcessRows(String productIds, Long returnsup_id, Long myMasterId) {
        String stringQuery="";
        try {
            stringQuery =   " delete from returnsup_product " +
                    " where returnsup_id=" + returnsup_id +
                    " and master_id=" + myMasterId +
                    (productIds.length()>0?(" and product_id not in (" + productIds.replaceAll("[^0-9\\,]", "") + ")"):"");//если во фронте удалили все товары, то удаляем все товары в данном Заказе покупателя
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteReturnsupProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean saveReturnsupProductTable(ReturnsupProductTableForm row, Long company_id, Long master_id) {
        String stringQuery="";
        try {
            commonUtilites.idBelongsMyMaster("products",    row.getProduct_id(), master_id);
            commonUtilites.idBelongsMyMaster("returnsup",   row.getReturnsup_id(), master_id);
            commonUtilites.idBelongsMyMaster("sprav_taxes", row.getNds_id(), master_id);
            commonUtilites.idBelongsMyMaster("companies",   company_id, master_id);
            stringQuery =
                    " insert into returnsup_product (" +
                            "master_id, " +
                            "company_id, " +
                            "product_id, " +
                            "returnsup_id, " +
                            "product_count, " +
                            "product_price, " +
                            "product_sumprice, " +
                            "nds_id" +
                            ") values (" +
                            master_id + "," +
                            company_id + "," +
                            row.getProduct_id() + "," +
                            row.getReturnsup_id() + "," +
                            row.getProduct_count() + "," +
                            row.getProduct_price() + "," +
                            row.getProduct_sumprice() + "," +
                            row.getNds_id() +
                            " ) " +
                            "ON CONFLICT ON CONSTRAINT returnsup_product_uq " +// "upsert"  - уникальность по product_id, returnsup_id
                            " DO update set " +
                            " product_id = " + row.getProduct_id() + "," +
                            " returnsup_id = " + row.getReturnsup_id() + "," +
                            " product_count = " + row.getProduct_count() + "," +
                            " product_price = " + row.getProduct_price() + "," +
                            " product_sumprice = " + row.getProduct_sumprice() + "," +
                            " nds_id = " + row.getNds_id();
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveReturnsupProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean deleteReturnsupProductTableRow(Long id) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery = " delete from returnsup_product " +
                " where id="+id+" and master_id="+myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.executeUpdate() == 1;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteReturnsupProductTableRow. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsReturnsup(SettingsReturnsupForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            commonUtilites.idBelongsMyMaster("companies", row.getCompanyId(), myMasterId);
            commonUtilites.idBelongsMyMaster("departments", row.getDepartmentId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_status_dock", row.getStatusOnFinishId(), myMasterId);
            commonUtilites.idBelongsMyMaster("sprav_type_prices", row.getPriceTypeId(), myMasterId);

            stringQuery =
                    " insert into settings_returnsup (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "date_time_update, " +
                            "pricing_type, " +          //тип расценки (выпад. список: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                            "price_type_id, " +         //тип цены из справочника Типы цен
                            "change_price, " +          //наценка/скидка в цифре (например, 50)
                            "plus_minus, " +            //определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
                            "change_price_type, " +     //тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
                            "hide_tenths, " +           //убирать десятые (копейки) - boolean
                            "department_id, " +         //отделение по умолчанию
                            "status_on_finish_id, "+    //статус документа при завершении инвентаризации
                            "auto_add"+                 // автодобавление товара из формы поиска в таблицу
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            "now(), " +
                            ":pricing_type," +
                            row.getPriceTypeId() + "," +
                            row.getChangePrice() + "," +
                            ":plusMinus," +
                            ":changePriceType," +
                            row.getHideTenths() + "," +
                            row.getDepartmentId() + "," +
                            row.getStatusOnFinishId() + "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_returnsup_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = :pricing_type, " +
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = :plusMinus" +
                            ", change_price_type = :changePriceType" +
                            ", date_time_update = now()" +
                            ", hide_tenths = " + row.getHideTenths() +
                            ", department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", auto_add = "+row.getAutoAdd();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("pricing_type", row.getPricingType());
            query.setParameter("plusMinus", row.getPlusMinus());
            query.setParameter("changePriceType", row.getChangePriceType());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsReturnsup. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Возврат покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsReturnsupJSON getSettingsReturnsup() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add, " +                 // автодобавление товара из формы поиска в таблицу
                "           coalesce(p.pricing_type,'avgCostPrice') as pricing_type,"+  // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice))
                "           p.price_type_id as price_type_id, " +                       // тип цены из справочника Типы цен
                "           coalesce(p.change_price, 0.00) as change_price, " +         // наценка/скидка в цифре (например, 50)
                "           coalesce(p.plus_minus,'plus') as plus_minus, " +            // определят, что есть changePrice - наценка или скидка (plus или minus)
                "           coalesce(p.change_price_type,'procents') as change_price_type,"+// тип наценки/скидки (валюта currency или проценты procents)
                "           coalesce(p.hide_tenths,false) as hide_tenths " +            // убирать десятые (копейки)

                "           from settings_returnsup p " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            if(queryList.size()==0) throw new NoResultException();
            SettingsReturnsupJSON returnsupObj=new SettingsReturnsupJSON();

            for(Object[] obj:queryList){
                returnsupObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                returnsupObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                returnsupObj.setStatusOnFinishId(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                returnsupObj.setAutoAdd((Boolean)                              obj[3]);
                returnsupObj.setPricingType((String)                           obj[4]);
                returnsupObj.setPriceTypeId(obj[5]!=null?Long.parseLong(       obj[5].toString()):null);
                returnsupObj.setChangePrice((BigDecimal)                       obj[6]);
                returnsupObj.setPlusMinus((String)                             obj[7]);
                returnsupObj.setChangePriceType((String)                       obj[8]);
                returnsupObj.setHideTenths((Boolean)                           obj[9]);

            }
            return returnsupObj;
        } catch (NoResultException nre) {
            return new SettingsReturnsupJSON(false,"avgCostPrice",new BigDecimal("0"),"plus","procents",false);
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsReturnsup. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deleteReturnsup (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"364") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"365") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"366") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление документов созданных собой" и id принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"367") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "returnsup");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update returnsup p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "returnsup")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deleteReturnsup. SQL query:" + stringQuery, e);
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
    public Integer undeleteReturnsup(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"364") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"365") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"366") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",delNumbers))||
                //Если есть право на "Удаление документов созданных собой" и id принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"367") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update returnsup p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteReturnsup. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    public List<ReturnsupProductsListJSON> getReturnsupProductsList(String searchString, Long companyId, Long departmentId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery = "select  p.id as id, " +
                // наименование товара
                "           p.name as name, " +
                // наименование ед. измерения
                "           ei.short_name as edizm, " +
                // картинка
                "           f.name as filename, " +
                // всего единиц товара в отделении (складе)
                "           (select coalesce(quantity,0)   from product_quantity     where department_id = "    + departmentId +" and product_id = p.id) as remains, " +
                // НДС
                "           coalesce(p.nds_id,null)  as nds_id, " +
                // материален ли товар
                " (select is_material from sprav_sys_ppr where id=p.ppr_id) as is_material, " +
                // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                " p.indivisible as indivisible" +

                " from products p " +
                " left outer join product_barcodes pb on pb.product_id=p.id" +
                " left outer join files f on f.id=(select file_id from product_files where product_id=p.id and output_order=1 limit 1)" +
                " left outer join sprav_sys_ppr ssp on ssp.id=p.ppr_id" +
                " left outer join sprav_sys_edizm ei on p.edizm_id=ei.id" +
                " where  p.master_id=" + myMasterId +
                " and coalesce(p.is_archive,false) !=true ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.product_code_free,'fm0000000000') like CONCAT('%',:sg) or "+
                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(p.article) like upper (CONCAT('%',:sg,'%')) or " +
                    " pb.value = :sg";

            stringQuery = stringQuery + ")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id, f.name, ei.short_name" +

                "  order by p.name asc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty()){query.setParameter("sg", searchString);}

            List<Object[]> queryList = query.getResultList();
            List<ReturnsupProductsListJSON> returnsupList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ReturnsupProductsListJSON product = new ReturnsupProductsListJSON();
                product.setProduct_id(Long.parseLong(                       obj[0].toString()));
                product.setName((String)                                    obj[1]);
                product.setEdizm((String)                                   obj[2]);
                product.setFilename((String)                                obj[3]);
                product.setRemains(                                         obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                product.setNds_id((Integer)                                 obj[5]);
                product.setIs_material((Boolean)                            obj[6]);
                product.setIndivisible((Boolean)                            obj[7]);
                returnsupList.add(product);
            }
            return returnsupList;
        } catch (Exception e) {
            logger.error("Exception in method getReturnsupProductsList. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<LinkedDocsJSON> getReturnsupLinkedDocsList(Long docId, String docName) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String tableName=(docName.equals("writeoff")?"writeoff":"");//не могу воткнуть имя таблицы параметром, т.к. the parameters can come from the outside and could take any value, whereas the table and column names are static.
        stringQuery =   " select " +
                " ap.id," +
                " to_char(ap.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI'), " +
                " ap.description," +
                " coalesce(ap.is_completed,false)," +
                " ap.doc_number" +
                " from "+tableName+" ap" +
                " where ap.master_id = " + myMasterId +
                " and coalesce(ap.is_archive,false)!=true "+
                " and ap.returnsup_id = " + docId;
        stringQuery = stringQuery + " order by ap.date_time_created asc ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsJSON> returnsupList = new ArrayList<>();
            for(Object[] obj:queryList){
                LinkedDocsJSON doc=new LinkedDocsJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setDate_time_created((String)               obj[1]);
                doc.setDescription((String)                     obj[2]);
                doc.setIs_completed((Boolean)                   obj[3]);
                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
                returnsupList.add(doc);
            }
            return returnsupList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getReturnsupLinkedDocsList. SQL query:" + stringQuery, e);
            return null;
        }
    }
//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean addFilesToReturnsup(UniversalForm request){
        Long returnsupId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого запрашивают), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"372") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup",returnsupId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"373") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",returnsupId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"374") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",returnsupId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого запрашивают) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"375") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",returnsupId.toString())))
        {
            try
            {
                String stringQuery;
                Long masterId = userRepositoryJPA.getMyMasterId();
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {
                    commonUtilites.idBelongsMyMaster("files", fileId, masterId);
                    stringQuery = "select returnsup_id from returnsup_files where returnsup_id=" + returnsupId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_ReturnsupId_FileId(returnsupId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method ReturnsupRepository/addFilesToReturnsup.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_ReturnsupId_FileId(Long returnsupId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into returnsup_files " +
                    "(returnsup_id,file_id) " +
                    "values " +
                    "(" + returnsupId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method ReturnsupRepository/manyToMany_ReturnsupId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesReturnsupJSON> getListOfReturnsupFiles(Long returnsupId) {
        if(securityRepositoryJPA.userHasPermissions_OR(29L, "368,369,370,371"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           returnsup p" +
                    "           inner join" +
                    "           returnsup_files pf" +
                    "           on p.id=pf.returnsup_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + returnsupId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(29L, "368")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(29L, "369")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(29L, "370")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                List<FilesReturnsupJSON> returnsupList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesReturnsupJSON doc=new FilesReturnsupJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnsupList.add(doc);
                }
                return returnsupList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfReturnsupFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteReturnsupFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(29L,"372") && securityRepositoryJPA.isItAllMyMastersDocuments("returnsup", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(29L,"373") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("returnsup",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(29L,"374") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("returnsup",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(29L,"375") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("returnsup",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from returnsup_files "+
                    " where returnsup_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from returnsup where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method ReturnsupRepository/deleteReturnsupFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}

