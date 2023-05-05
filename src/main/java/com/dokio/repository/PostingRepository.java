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

import com.dokio.message.request.PostingForm;
import com.dokio.message.request.PostingProductForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsPostingForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.PostingJSON;
import com.dokio.message.response.Settings.SettingsPostingJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesPostingJSON;
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
public class PostingRepository {
    @PersistenceContext
    private EntityManager           entityManager;
    @Autowired
    private UserDetailsServiceImpl  userRepository;
    @Autowired
    private UserRepositoryJPA       userRepositoryJPA;
    @Autowired
    private SecurityRepositoryJPA   securityRepositoryJPA;
    @Autowired
    private CommonUtilites          commonUtilites;
    @Autowired
    private LinkedDocsUtilites      linkedDocsUtilites;
    @Autowired
    private ProductsRepositoryJPA   productsRepository;

    Logger logger = Logger.getLogger("PostingRepository");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("sum_price","doc_number","status_name","product_count","is_completed","posting_date_sort","company","department","creator","date_time_created_sort","description")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<PostingJSON> getPostingTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
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
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.posting_date at time zone '"+myTimeZone+"', '"+dateFormat+"') as posting_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.posting_date as posting_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           (select count(*) from posting_product ip where coalesce(ip.posting_id,0)=p.id) as product_count," + //подсчет кол-ва товаров
                    "           coalesce((select sum(coalesce(product_sumprice,0)) from posting_product where posting_id=p.id),0) as sum_price " +
                    "           from posting p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.posting_date, 'DD.MM.YYYY') = :sg or "+
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
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
                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}
                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<PostingJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    PostingJSON doc=new PostingJSON();
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
                    doc.setPosting_date((String)(                 obj[11]));
                    doc.setCompany((String)                       obj[12]);
                    doc.setDate_time_created((String)             obj[13]);
                    doc.setDate_time_changed((String)             obj[14]);
                    doc.setDescription((String)                   obj[15]);
                    doc.setIs_completed((Boolean)                 obj[16]);
                    doc.setStatus_id(obj[20]!=null?Long.parseLong(obj[20].toString()):null);
                    doc.setStatus_name((String)                   obj[21]);
                    doc.setStatus_color((String)                  obj[22]);
                    doc.setProduct_count(Long.parseLong(          obj[23].toString()));
                    doc.setSum_price((BigDecimal)                 obj[24]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPostingTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;

    }
    @SuppressWarnings("Duplicates")
    public int getPostingSize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
//        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//(см. файл Permissions Id)
//        {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

        stringQuery = "select  p.id as id " +
                "           from posting p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.posting_date, 'DD.MM.YYYY') = :sg or "+
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
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
            logger.error("Exception in method getPostingSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<PostingProductForm> getPostingProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.posting_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " p.name as name," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm," +
                    " p.indivisible as indivisible," +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                    " coalesce((select quantity from product_quantity where product_id = ap.product_id and department_id = a.department_id),0) as total "+ //всего на складе (т.е остаток)
                    " from " +
                    " posting_product ap " +
                    " INNER JOIN posting a ON ap.posting_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.posting_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and a.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and a.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and a.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<PostingProductForm> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    PostingProductForm doc=new PostingProductForm();
                    doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                    doc.setPosting_id(Long.parseLong(                       obj[1].toString()));
                    doc.setProduct_count((BigDecimal)                       obj[2]);
                    doc.setProduct_price((BigDecimal)                       obj[3]);
                    doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                    doc.setName((String)                                    obj[5]);
                    doc.setEdizm((String)                                   obj[6]);
                    doc.setIndivisible((Boolean)                            obj[7]);
                    doc.setTotal((BigDecimal)                               obj[8]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPostingProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    //*****************************************************************************************************************************************************
    //****************************************************      CRUD      *********************************************************************************
    //*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public PostingJSON getPostingValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//см. _Permissions Id.txt
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            boolean needToSetParameter_MyDepthsIds = false;
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
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.posting_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as posting_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.posting_date as posting_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.uid as uid, " +
                    "           to_char(p.posting_date at time zone '"+myTimeZone+"', 'HH24:MI') as _time " +
                    "           from posting p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;
            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                PostingJSON returnObj=new PostingJSON();

                for(Object[] obj:queryList){
                    returnObj.setId(Long.parseLong(                     obj[0].toString()));
                    returnObj.setMaster((String)                        obj[1]);
                    returnObj.setCreator((String)                       obj[2]);
                    returnObj.setChanger((String)                       obj[3]);
                    returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                    returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                    returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                    returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                    returnObj.setDepartment_id(Long.parseLong(          obj[8].toString()));
                    returnObj.setDepartment((String)                    obj[9]);
                    returnObj.setDoc_number(Long.parseLong(             obj[10].toString()));
                    returnObj.setPosting_date((String)(                 obj[11]));
                    returnObj.setCompany((String)                       obj[12]);
                    returnObj.setDate_time_created((String)             obj[13]);
                    returnObj.setDate_time_changed((String)             obj[14]);
                    returnObj.setDescription((String)                   obj[15]);
                    returnObj.setIs_completed((Boolean)                 obj[16]);
                    returnObj.setStatus_id(obj[20]!=null?Long.parseLong(obj[20].toString()):null);
                    returnObj.setStatus_name((String)                   obj[21]);
                    returnObj.setStatus_color((String)                  obj[22]);
                    returnObj.setStatus_description((String)            obj[23]);
                    returnObj.setUid((String)                           obj[24]);
                    returnObj.setPosting_time((String)                  obj[25]);
                }
                return returnObj;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPostingValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, CantInsertProductRowCauseErrorException.class,ThereIsServicesInProductsListException.class})
    public Long insertPosting(PostingForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();

        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDoc( request.getCompany_id(), request.getDepartment_id(), 16L, "200", "201", "202");
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
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"posting");

            if (request.getStatus_id() ==null){
                request.setStatus_id(commonUtilites.getDocumentsDefaultStatus(request.getCompany_id(),16));
            }

            //если документ создается из другого документа
            if (request.getLinked_doc_id() != null) {
                //получаем для этих объектов id группы связанных документов (если ее нет - она создастся)
                linkedDocsGroupId=linkedDocsUtilites.getOrCreateAndGetGroupId(request.getLinked_doc_id(),request.getLinked_doc_name(),request.getCompany_id(),myMasterId);
                if (Objects.isNull(linkedDocsGroupId)) return null; // ошибка при запросе id группы связанных документов, либо её создании
            }

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =
                    " insert into posting (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " date_time_created," + //дата и время создания
                    " doc_number," + //номер заказа
                    " description," +//доп. информация по заказу
                    " inventory_id, " + //если документ создаётся из Инвенторизации - тут будет ее id
                    " status_id," + //статус
                    " uid," +//
                    " linked_docs_group_id," +// id группы связанных документов
                    " posting_date " +// дата списания
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    " :description, " +//описание
                    request.getInventory_id() + ", "+//
                    request.getStatus_id() + ", "+//статус
                    " :uid, " + //uid
                    linkedDocsGroupId+"," + // id группы связанных документов
                    "to_timestamp(CONCAT(:posting_date,' ',:posting_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"')";// дата оприходования
//                    " to_date(:posting_date,'DD.MM.YYYY')) ";// дата оприходования
            try {

                Date dateNow = new Date();
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("uid", (request.getUid() == null ? "" : request.getUid()));
                //если дата не пришла (это может быть, если создаем из Инвентаризации) - нужно вставить текукщую
                query.setParameter("posting_date", ((request.getPosting_date()==null || request.getPosting_date().equals("")) ? dateFormat.format(dateNow) : request.getPosting_date()));
                query.setParameter("posting_time", ((request.getPosting_time()==null || request.getPosting_time().equals("")) ? timeFormat.format(dateNow) : request.getPosting_time()));
                query.executeUpdate();
                stringQuery = "select id from posting where creator_id=" + myId + " and date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS'))";
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDocId = Long.valueOf(query2.getSingleResult().toString());

                //если есть таблица с товарами - нужно создать их
                insertPostingProducts(request, newDocId, myMasterId);

                //если документ создался из другого документа - добавим эти документы в их общую группу связанных документов linkedDocsGroupId и залинкуем между собой
                if (request.getLinked_doc_id() != null) {
                    linkedDocsUtilites.addDocsToGroupAndLinkDocs(request.getLinked_doc_id(), newDocId, linkedDocsGroupId, request.getParent_uid(),request.getChild_uid(),request.getLinked_doc_name(), "posting", request.getUid(), request.getCompany_id(), myMasterId);
                }

                return newDocId;

            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertPosting on inserting into posting_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (ThereIsServicesInProductsListException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertWriteoff on inserting into writeoff_product cause error - there is service(s) in a products list.", e);
                e.printStackTrace();
                return -240L;
            } catch (Exception e) {
                logger.error("Exception in method insertPosting. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(Objects.isNull(iCan)) return null; else return 0L;
        }
    }

    //сохранение таблицы товаров
    @SuppressWarnings("Duplicates")
    private boolean insertPostingProducts(PostingForm request, Long parentDocId, Long myMasterId) throws CantInsertProductRowCauseErrorException, ThereIsServicesInProductsListException {
        Set<Long> productIds=new HashSet<>();

        if (request.getPostingProductTable()!=null && request.getPostingProductTable().size() > 0) {
            for (PostingProductForm row : request.getPostingProductTable()) {
                row.setPosting_id(parentDocId);// чтобы через API сюда нельзя было подсунуть рандомный id
                if (!savePostingProductTable(row, myMasterId)) {
                    throw new CantInsertProductRowCauseErrorException();
                }
                productIds.add(row.getProduct_id());
            }
            //checking on there is services in products list
            if(productsRepository.isThereServicesInProductsList(productIds))
                throw new ThereIsServicesInProductsListException();
        }
        if (!deletePostingProductTableExcessRows(productIds.size()>0?(commonUtilites.SetOfLongToString(productIds,",","","")):"0", parentDocId)) {
            throw new CantInsertProductRowCauseErrorException();
        } else return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class, ThereIsServicesInProductsListException.class, CantInsertProductRowCauseErrorException.class, CantSaveProductQuantityException.class, CantSaveProductHistoryException.class, InsertProductHistoryExceprions.class})
    public  Integer updatePosting(PostingForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(16L,"211") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"212") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"213") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"214") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.isIs_completed())){
                if(
                        !(      //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(16L,"627") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",request.getId().toString())) ||
                                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                                (securityRepositoryJPA.userHasPermissions_OR(16L,"628") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",request.getId().toString()))||
                                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                                (securityRepositoryJPA.userHasPermissions_OR(16L,"629") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",request.getId().toString()))||
                                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                                (securityRepositoryJPA.userHasPermissions_OR(16L,"630") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",request.getId().toString()))
                        )
                ) return -1;
            }

            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment
            try {//сохранение таблицы

                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "posting"))
                    throw new DocumentAlreadyCompletedException();

                //апдейт основного документа, без таблицы товаров
                updatePostingWithoutTable(request);
                //апдейт товаров
                insertPostingProducts(request, request.getId(),myMasterId);
                //если документ завершается - запись в историю товара
                if(request.isIs_completed()){
                    for (PostingProductForm row : request.getPostingProductTable()) {
                        addProductHistory(row, request, myMasterId);
                        productsIdsToSyncWoo.add(row.getProduct_id());
                    }
                    // отмечаем товары как необходимые для синхронизации с WooCommerce
                    productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, myMasterId);
                }
                return 1;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updatePosting.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost.", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (CantSaveProductQuantityException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updatePosting on inserting into product_quantity cause error.", e);
                e.printStackTrace();
                return null;
            } catch (ThereIsServicesInProductsListException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertWriteoff on inserting into writeoff_product cause error - there is service(s) in a products list.", e);
                e.printStackTrace();
                return -240;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updatePosting on inserting into posting_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (CantSaveProductHistoryException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updatePosting on inserting into products_history.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PostingRepository/updatePosting.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, CalculateNetcostNegativeSumException.class, CantSetHistoryCauseNegativeSumException.class, NotEnoughPermissionsException.class})
    public Integer setPostingAsDecompleted(PostingForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(16L,"627") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",request.getId().toString())) ||
                //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(16L,"628") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",request.getId().toString()))||
                //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"629") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",request.getId().toString()))||
                //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"630") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",request.getId().toString()))
        )
        {
            if(request.getPostingProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Set<Long> productsIdsToSyncWoo = new HashSet<>(); // Set IDs of products with changed quantity as a result of shipment
            String stringQuery =
                    " update posting set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "posting"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();

                //сохранение истории движения товара
                Long myMasterId = userRepositoryJPA.getMyMasterId();
                request.setIs_completed(false);
                for (PostingProductForm row : request.getPostingProductTable()) {
                    addProductHistory(row, request, myMasterId);
                    productsIdsToSyncWoo.add(row.getProduct_id());
                }
                // отмечаем товары как необходимые для синхронизации с WooCommerce
                productsRepository.markProductsAsNeedToSyncWoo(productsIdsToSyncWoo, myMasterId);
                return 1;
            } catch (CantInsertProductRowCauseOversellException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PostingRepository/addProductHistory on inserting into products_history cause oversell.", e);
                e.printStackTrace();
                return -80;
            }catch (CalculateNetcostNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("CalculateNetcostNegativeSumException in method recountProductNetcost (setPostingAsDecompleted).", e);
                e.printStackTrace();
                return -70; // см. _ErrorCodes
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PostingRepository/setPostingAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantSetHistoryCauseNegativeSumException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PostingRepository/setPostingAsDecompleted.", e);
                e.printStackTrace();
                return -80; // см. _ErrorCodes
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method PostingRepository/setPostingAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }
    @SuppressWarnings("Duplicates")
    private Boolean addProductHistory(PostingProductForm row, PostingForm request , Long masterId) throws Exception {
        try {

            // все записи в таблицы product_history и product_quantity производим только если товар материален (т.е. это не услуга и т.п.)
            if (productsRepository.isProductMaterial(row.getProduct_id())) {
                // загружаем настройки, чтобы узнать политику предприятия по подсчёту себестоимости (по всему предприятию или по каждому отделению отдельно)
                String netcostPolicy = commonUtilites.getCompanySettings(request.getCompany_id()).getNetcost_policy();
                // берём информацию о товаре (кол-во и ср. себестоимость) в данном отделении (если netcostPolicy == "all" то независимо от отделения)
                ProductHistoryJSON productInfo = productsRepository.getProductQuantityAndNetcost(masterId, request.getCompany_id(), row.getProduct_id(), netcostPolicy.equals("each") ? request.getDepartment_id() : null);
                // актуальное количество товара В ОТДЕЛЕНИИ
                // Используется для записи нового кол-ва товара в отделении путем сложения lastQuantity и row.getProduct_count()
                // если политика подсчета себестоимости ПО КАЖДОМУ отделению - lastQuantity отдельно высчитывать не надо - она уже высчитана шагом ранее в productInfo
                BigDecimal lastQuantity =  netcostPolicy.equals("each") ? productInfo.getQuantity() : productsRepository.getProductQuantity(masterId, request.getCompany_id(), row.getProduct_id(), request.getDepartment_id());
                // имеющееся количество (если учёт себестоимости по отделениям - то В ОТДЕЛЕНИИ, если по всему предприятию - то кол-во ВО ВСЕХ ОТДЕЛЕНИЯХ.)
                // Используется для расчёта себестоимости
                BigDecimal availableQuantity = netcostPolicy.equals("each") ? lastQuantity : productInfo.getQuantity();
                // средняя себестоимость уже имеющегося товара
                BigDecimal lastAvgNetcostPrice = productInfo.getAvg_netcost_price();

                // т.к. это  операция поступления, при отмене её проведения необходимо проверить,
                // сколько товара останется после этого, и если это кол-во <0 то не допустить этого
                if(!request.isIs_completed() && (lastQuantity.subtract(row.getProduct_count())).compareTo(new BigDecimal("0")) < 0) {
                    logger.error("Cancelling of Posting completion with id = "+request.getId()+", doc number "+request.getDoc_number()+": the quantity of product to be disposed of from the department is greater than the quantity of product in the department");
                    throw new CantInsertProductRowCauseOversellException();//кидаем исключение чтобы произошла отмена транзакции
                }

//                Timestamp timestamp = new Timestamp(((Date) commonUtilites.getFieldValueFromTableById("posting", "date_time_created", masterId, request.getId())).getTime());

                productsRepository.setProductHistory(
                        masterId,
                        request.getCompany_id(),
                        request.getDepartment_id(),
                        16,
                        request.getId(),
                        row.getProduct_id(),
                        row.getProduct_count(),
                        row.getProduct_price(),
                        row.getProduct_price(),// в оприходовании нет себестоимости - за цену себестоимости берем цену оприходования
//                        timestamp,
                        request.isIs_completed()
                );

                // новая средняя себестоимость
                BigDecimal avgNetcostPrice;
                if (request.isIs_completed())   // Если проводим, то считаем по формуле
                    // ((ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО*СРЕДНЯЯ_СЕБЕСТОИМОСТЬ) + КОЛ-ВО_НОВОГО_ТОВАРА * ЕГО_СЕБЕСТОИМОСТЬ) / ИМЕЮЩЕЕСЯ_КОЛИЧЕСТВО + КОЛ-ВО_НОВОГО_ТОВАРА
                    avgNetcostPrice = ((availableQuantity.multiply(lastAvgNetcostPrice)).add(row.getProduct_count().multiply(row.getProduct_price()))).divide(availableQuantity.add(row.getProduct_count()), 2, BigDecimal.ROUND_HALF_UP);
                else // Если снимаем с проведения, то пересчитываем на основании прежних движений товара
                    avgNetcostPrice = productsRepository.recountProductNetcost(request.getCompany_id(), request.getDepartment_id(), row.getProduct_id());

                if (request.isIs_completed())   // Если проводим
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
            logger.error("Exception in method addPostingProductHistory on inserting into product_quantity cause error.", e);
            e.printStackTrace();
            throw new CalculateNetcostNegativeSumException();
        } catch (CantInsertProductRowCauseOversellException e) {
            logger.error("Exception in method addPostingProductHistory on inserting into product_quantity cause error - Not enough product count.", e);
            e.printStackTrace();
            throw new CantInsertProductRowCauseOversellException();
        } catch (CantSaveProductHistoryException e) {
            logger.error("Exception in method addPostingProductHistory on inserting into product_history.", e);
            e.printStackTrace();
            throw new CantSaveProductHistoryException();
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method PostingRepository/addPostingProductHistory. ", e);
            throw new CantSaveProductHistoryException();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean updatePostingWithoutTable(PostingForm request) throws Exception {
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String myTimeZone = userRepository.getUserTimeZone();
        String stringQuery;
        stringQuery =   " update posting set " +
                " changer_id = " + myId + ", "+
                " date_time_changed= now()," +
                " description = :description, "+
                " doc_number =" + request.getDoc_number() + "," +
                " is_completed = " + request.isIs_completed() + "," +
//                " posting_date = to_date(:posting_date,'DD.MM.YYYY') " + "," +
                " posting_date = to_timestamp(CONCAT(:posting_date,' ',:posting_time),'DD.MM.YYYY HH24:MI') at time zone 'GMT' at time zone '"+myTimeZone+"',"+
                " status_id = " + request.getStatus_id() +
                " where " +
                " id= "+request.getId() +
                " and master_id="+myMasterId;
        try
        {
            Date dateNow = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
//            query.setParameter("posting_date", (request.getPosting_date() == "" ? null :request.getPosting_date()));
            query.setParameter("posting_date", ((request.getPosting_date()==null || request.getPosting_date().equals("")) ? dateFormat.format(dateNow) : request.getPosting_date()));
            query.setParameter("posting_time", ((request.getPosting_time()==null || request.getPosting_time().equals("")) ? timeFormat.format(dateNow) : request.getPosting_time()));

            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method PostingRepository/updatePostingWithoutTable. stringQuery=" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
    private Boolean savePostingProductTable(PostingProductForm row, Long myMasterId) {
        String stringQuery;

        stringQuery =   " insert into posting_product (" +
                "product_id," +
                "posting_id," +
                "product_count," +
                "product_price," +
                "product_sumprice" +
                ") values ("
                + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит
                + "(select id from posting where id="+row.getPosting_id() +" and master_id="+myMasterId+"),"
                + row.getProduct_count() + ","
                + row.getProduct_price() +","
                + row.getProduct_sumprice()+")" +
                "ON CONFLICT ON CONSTRAINT posting_product_uq " +// "upsert"
                " DO update set " +
                " product_id = " + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+")," +
                " posting_id = "+ "(select id from posting where id="+row.getPosting_id() +" and master_id="+myMasterId+")," +
                " product_count = " + row.getProduct_count() + "," +
                " product_price = " + row.getProduct_price() + "," +
                " product_sumprice = " + row.getProduct_sumprice();
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method PostingRepository/savePostingProductTable. SQL query:"+stringQuery, e);
            return false;
        }
    }

    private Boolean deletePostingProductTableExcessRows(String productIds, Long posting_id) {
        String stringQuery;

        stringQuery =   " delete from posting_product " +
                " where posting_id=" + posting_id +
                " and product_id not in (" + productIds.replaceAll("[^0-9\\,]", "") + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;

        } catch (Exception e) {
            logger.error("Exception in method PostingRepository/deletePostingProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    @SuppressWarnings("Duplicates")
    public DeleteDocsReport deletePosting (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(16L,"203") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"204") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"205") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"206") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",delNumbers)))
        {
            // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
            List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "posting");

            if(!Objects.isNull(checkChilds)) { //если нет ошибки

                if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                    String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                    Long myId = userRepositoryJPA.getMyId();
                    stringQuery = "Update posting p" +
                            " set is_deleted=true, " + //удален
                            " changer_id="+ myId + ", " + // кто изменил (удалил)
                            " date_time_changed = now() " +//дату и время изменения
                            " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")" +
                            " and coalesce(p.is_completed,false) !=true";
                    try {
                        entityManager.createNativeQuery(stringQuery).executeUpdate();
                        //удалим документы из группы связанных документов
                        if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "posting")) throw new Exception ();
                        delResult.setResult(0);// 0 - Всё ок
                        return delResult;
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        logger.error("Exception in method deletePosting. SQL query:" + stringQuery, e);
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
    public Integer undeletePosting (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(16L,"203") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"204") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"205") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"206") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update posting p" +
                    " set is_deleted=false, " + //удален
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            }catch (Exception e) {
                logger.error("Exception in method undeletePosting. SQL query:"+stringQuery, e);
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
        stringQuery = "select coalesce(max(doc_number)+1,1) from posting where company_id="+company_id+" and master_id="+myMasterId;
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
    public Boolean isPostingNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long doc_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from posting where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(doc_id>0) stringQuery=stringQuery+" and id !="+doc_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.getResultList().size()>0)
                return false;// код не уникальный
            else return true; // код уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isPostingNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

//    @SuppressWarnings("Duplicates") //проверка на то, есть ли уже в таблице товаров данный товар
//    private Boolean clearPostingProductTable(Long product_id, Long posting_id) {
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String stringQuery = " delete from " +
//                " posting_product where " +
//                "product_id="+product_id+
//                " and posting_id="+posting_id +
//                " and (select master_id from posting where id="+posting_id+")="+myMasterId;
//        try
//        {
//            entityManager.createNativeQuery(stringQuery).executeUpdate();
//            return true;
//        }
//        catch (Exception e) {
//            logger.error("Exception in method clearPostingProductTable. SQL query:" + stringQuery, e);
//            e.printStackTrace();
//            return false;
//        }
//    }

//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToPosting(UniversalForm request){
        Long postingId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(16L,"211") && securityRepositoryJPA.isItAllMyMastersDocuments("posting",postingId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"212") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",postingId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"213") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",postingId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"214") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",postingId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select posting_id from posting_files where posting_id=" + postingId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_PostingId_FileId(postingId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                logger.error("Exception in method addFilesToPosting. " , ex);
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_PostingId_FileId(Long postingId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into posting_files " +
                    "(posting_id,file_id) " +
                    "values " +
                    "(" + postingId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("Exception in method manyToMany_PostingId_FileId. " , ex);
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesPostingJSON> getListOfPostingFiles(Long postingId) {
        if(securityRepositoryJPA.userHasPermissions_OR(16L, "207,208,209,210"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           posting p" +
                    "           inner join" +
                    "           posting_files pf" +
                    "           on p.id=pf.posting_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + postingId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(16L, "207")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(16L, "208")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(16L, "209")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                List<FilesPostingJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesPostingJSON doc=new FilesPostingJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfPostingFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deletePostingFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(16L,"211") && securityRepositoryJPA.isItAllMyMastersDocuments("posting", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(16L,"212") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("posting",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(16L,"213") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("posting",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(16L,"214") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("posting",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from posting_files "+
                    " where posting_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from posting where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method deletePostingFile. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }


    //сохраняет настройки документа
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsPosting(SettingsPostingForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_posting (" +
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
                            "now(), '" +
                            row.getPricingType() + "'," +
                            row.getPriceTypeId() + "," +
                            row.getChangePrice() + ",'" +
                            row.getPlusMinus() + "','" +
                            row.getChangePriceType() + "'," +
                            row.getHideTenths() + "," +
                            row.getDepartmentId() + "," +
                            row.getStatusOnFinishId() + "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_posting_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = '" + row.getPricingType() + "',"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = '" + row.getPlusMinus() + "',"+
                            " change_price_type = '" + row.getChangePriceType() + "',"+
                            " hide_tenths = " + row.getHideTenths() +
                            ", date_time_update = now()" +
                            ", department_id = "+row.getDepartmentId()+
                            ", company_id = "+row.getCompanyId()+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", auto_add = "+row.getAutoAdd();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsPosting. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsPostingJSON getSettingsPosting() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           coalesce(p.auto_add,false) as auto_add, " +                 // автодобавление товара из формы поиска в таблицу
                "           p.pricing_type as pricing_type, " +                         // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                "           p.price_type_id as price_type_id, " +                       // тип цены из справочника Типы цен
                "           p.change_price as change_price, " +                         // наценка/скидка в цифре (например, 50)
                "           p.plus_minus as plus_minus, " +                             // определят, что есть changePrice - наценка или скидка (plus или minus)
                "           p.change_price_type as change_price_type, " +               // тип наценки/скидки (валюта currency или проценты procents)
                "           coalesce(p.hide_tenths,false) as hide_tenths " +            // убирать десятые (копейки)
                "           from settings_posting p " +
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsPostingJSON postingObj=new SettingsPostingJSON();

            for(Object[] obj:queryList){
                postingObj.setDepartmentId(obj[0]!=null?Long.parseLong(      obj[0].toString()):null);
                postingObj.setCompanyId(Long.parseLong(                      obj[1].toString()));
                postingObj.setStatusOnFinishId(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                postingObj.setAutoAdd((Boolean)                              obj[3]);
                postingObj.setPricingType((String)                           obj[4]);
                postingObj.setPriceTypeId(obj[5]!=null?Long.parseLong(       obj[5].toString()):null);
                postingObj.setChangePrice((BigDecimal)                       obj[6]);
                postingObj.setPlusMinus((String)                             obj[7]);
                postingObj.setChangePriceType((String)                       obj[8]);
                postingObj.setHideTenths((Boolean)                           obj[9]);
            }
            return postingObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsPosting. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }


}