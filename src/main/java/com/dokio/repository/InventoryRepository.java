/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.repository;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsInventoryForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsInventoryJSON;
import com.dokio.model.*;
import com.dokio.repository.Exceptions.CantInsertProductRowCauseErrorException;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class InventoryRepository {

    Logger logger = Logger.getLogger("InventoryRepository");

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

    //*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<InventoryJSON> getInventoryTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(27L, "336,337,338,339"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.name as name, " +
                    "           (select count(*) from inventory_product ip where coalesce(ip.inventory_id,0)=p.id) as product_count," + //подсчет кол-ва товаров в данной инвентаризации
                    "           coalesce(p.is_completed,false) as is_completed " +  // инвентаризация завершена?

                    "           from inventory p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN shifts sh ON p.shift_id=sh.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(27L, "336")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(27L, "337")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(27L, "338")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                        " upper(dp.name) like upper('%" + searchString + "%') or "+
                        " upper(cmp.name) like upper('%" + searchString + "%') or "+
                        " upper(us.name) like upper('%" + searchString + "%') or "+
                        " upper(uc.name) like upper('%" + searchString + "%') or "+
                        " upper(p.description) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<InventoryJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    InventoryJSON doc=new InventoryJSON();
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
                    doc.setName((String)                          obj[21]);
                    doc.setProduct_count(Long.parseLong(          obj[22].toString()));
                    doc.setIs_completed((Boolean)                 obj[23]);

                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInventoryTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getInventorySize(String searchString, int companyId, int departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from inventory p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(27L, "336")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(27L, "337")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(27L, "338")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                    " upper(dp.name) like upper('%" + searchString + "%') or "+
                    " upper(cmp.name) like upper('%" + searchString + "%') or "+
                    " upper(us.name) like upper('%" + searchString + "%') or "+
                    " upper(uc.name) like upper('%" + searchString + "%') or "+
                    " upper(p.description) like upper('%" + searchString + "%')"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getInventorySize. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<InventoryProductTableJSON> getInventoryProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(27L, "336,337,338,339"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            stringQuery =   " select " +
                    " ip.id  as row_id " +
                    " p.name as name," +
                    " ip.product_id," +
                    " ip.estimated_balance as estimated_balance," +
                    " ip.actual_balance as actual_balance," +
                    " ip.product_price," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = ip.edizm_id) as edizm," +

                    " from " +
                    " inventory_product ip " +
                    " INNER JOIN products p ON ip.product_id=p.id " +
                    " where a.master_id = " + myMasterId +
                    " and ip.inventory_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(27L, "336")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(27L, "337")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(27L, "338")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();
                List<InventoryProductTableJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    InventoryProductTableJSON doc=new InventoryProductTableJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setName((String)                                    obj[1]);
                    doc.setProduct_id(Long.parseLong(                       obj[2].toString()));
                    doc.setEstimated_balance(                               obj[3]==null?BigDecimal.ZERO:(BigDecimal)obj[3]);
                    doc.setActual_balance(                                  obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                    doc.setProduct_price(                                   obj[5]==null?BigDecimal.ZERO:(BigDecimal)obj[5]);
                    doc.setEdizm((String)                                   obj[6]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInventoryProductTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }


//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
//    @Transactional
    public InventoryJSON getInventoryValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(27L, "336,337,338,339"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.name as name, " +
                    "           coalesce(p.is_completed,false) as is_completed " +  // инвентаризация завершена?

                    "           from inventory p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN shifts sh ON p.shift_id=sh.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;


            if (!securityRepositoryJPA.userHasPermissions_OR(27L, "336")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(27L, "337")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(27L, "338")) //Если нет прав на просмотр всех доков в своих подразделениях
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

                InventoryJSON doc = new InventoryJSON();

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
                    doc.setName((String)                          obj[19]);
                    doc.setIs_completed((Boolean)                 obj[20]);
                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getInventoryValuesById. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean updateInventory(InventoryForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(27L,"340") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(27L,"341") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(27L,"342") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(27L,"343") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("inventory",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update inventory set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = '" + (request.getDescription() == null ? "" : request.getDescription()) + "', " +
                    " name = '" + (request.getName() == null ? "" : request.getName()) + "', " +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }catch (Exception e) {
                logger.error("Exception in method updateInventory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class})
    public Long insertInventory(InventoryForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Long dockDepartment=request.getDepartment_id();
        List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
        boolean itIsMyDepartment = myDepartmentsIds.contains(dockDepartment);
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(27L, "329")) ||
                //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                (securityRepositoryJPA.userHasPermissions_OR(27L, "330") && myCompanyId.equals(request.getCompany_id())) ||
                //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                (securityRepositoryJPA.userHasPermissions_OR(27L, "331") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDockId;
            Long doc_number;//номер документа

            //генерируем номер документа, если его (номера) нет
            if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                doc_number=Long.valueOf(request.getDoc_number());
            } else doc_number=commonUtilites.generateDocNumberCode(request.getCompany_id(),"inventory");


            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery =   "insert into inventory (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " department_id," + //отделение, из(для) которого создается документ
                    " date_time_created," + //дата и время создания
                    " doc_number," + //номер заказа
                    " name," + //наименование заказа
                    " description," +//доп. информация по заказу
                    " status_id"+//статус инвентаризации
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    "'" + (request.getName() == null ? "": request.getName()) + "', " +//наименование
                    "'" + (request.getDescription() == null ? "": request.getDescription()) +  "', " +//описание
                    request.getStatus_id() + ")";//статус инвентаризации
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                stringQuery="select id from inventory where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDockId=Long.valueOf(query2.getSingleResult().toString());

                if(insertInventoryProducts(request, newDockId, myMasterId)){
                    return newDockId;
                } else return null;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertInventory on inserting into inventory_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method " + e.getClass().getName() + " on inserting into inventory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

    }

    @SuppressWarnings("Duplicates")
    private boolean insertInventoryProducts(InventoryForm request, Long newDockId, Long myMasterId) throws CantInsertProductRowCauseErrorException {

        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка

        //сохранение таблицы
        if (request.getInventoryProductTable()!=null && request.getInventoryProductTable().size() > 0) {
            for (InventoryProductTableForm row : request.getInventoryProductTable()) {
                row.setInventory_id(newDockId);
                insertProductRowResult = saveInventoryProductTable(row, request.getCompany_id(), myMasterId);  //сохранение таблицы товаров
                if (insertProductRowResult==null) {
                    throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции из-за ошибки записи строки в таблицу товаров inventory_product
                }
            }
            return true;
        } else {
            throw new CantInsertProductRowCauseErrorException();
        }
    }



    //проверяет, не превышает ли продаваемое количество товара доступное количество, имеющееся на складе
    //если не превышает - пишется строка с товаром в БД
    //возвращает: true если все ок, false если превышает и записать нельзя, null если ошибка
    @SuppressWarnings("Duplicates")
    private Boolean saveInventoryProductTable(InventoryProductTableForm row, Long company_id, Long master_id) {
        String stringQuery="";
        try {
                stringQuery =
                        " insert into inventory_product (" +
                                "master_id, " +
                                "company_id, " +
                                "product_id, " +
                                "inventory_id, " +
                                "estimated_balance, " +
                                "actual_balance, " +
                                "product_price " +
                                ") values (" +
                                master_id + "," +
                                company_id + "," +
                                row.getProduct_id() + "," +
                                row.getInventory_id() + "," +
                                row.getEstimated_balance() + "," +
                                row.getActual_balance() + "," +
                                row.getProduct_price() +
                                " ) " +
                                "ON CONFLICT ON CONSTRAINT inventory_product_uq " +// "upsert"  - уникальность по product_id, inventory_id
                                " DO update set " +
                                " product_id = " + row.getProduct_id() + "," +
                                " inventory_id = " + row.getInventory_id() + "," +
                                " estimated_balance = " + row.getEstimated_balance() + "," +
                                " actual_balance = " + row.getActual_balance() + "," +
                                " product_price = ";
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveInventoryProductTable. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsInventory(SettingsInventoryForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {

            stringQuery =
                    " insert into settings_inventory (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id, " +
                            "pricing_type, " +      //тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                            "price_type_id, " +     //тип цены из справочника Типы цен
                            "change_price, " +      //наценка/скидка в цифре (например, 50)
                            "plus_minus, " +        //определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
                            "change_price_type, " + //тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
                            "hide_tenths, " +       //убирать десятые (копейки) - boolean
                            "department_id, " +     //отделение по умолчанию
                            "name, "+               //наименование заказа
                            "status_on_finish_id"+  //статус документа при завершении инвентаризации
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + ",'" +
                            row.getPricingType() + "'," +
                            row.getPriceTypeId() + "," +
                            row.getChangePrice() + ",'" +
                            row.getPlusMinus() + "','" +
                            row.getChangePriceType() + "'," +
                            row.getHideTenths() + "," +
                            row.getDepartmentId() + "," +
                            "'" + (row.getName() == null ? "": row.getName()) + "', " +//наименование
                            row.getStatusIdOnFinish() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_inventory_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = '" + row.getPricingType() + "',"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = '" + row.getPlusMinus() + "',"+
                            " change_price_type = '" + row.getChangePriceType() + "',"+
                            " hide_tenths = " + row.getHideTenths() + ","+
                            " department_id = " +row.getDepartmentId()  + ","+
                            " company_id = " +row.getCompanyId()  + ","+
                            " name = '" +(row.getName() == null ? "": row.getName()) + "',"+
                            " status_on_finish_id = "+row.getStatusIdOnFinish();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsInventory. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsInventoryJSON getSettingsInventory() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.pricing_type as pricing_type, " +                 // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                "           p.price_type_id as price_type_id, " +               // тип цены из справочника Типы цен
                "           p.change_price as change_price, " +                 // наценка/скидка в цифре (например, 50)
                "           p.plus_minus as plus_minus, " +                     // определят, что есть changePrice - наценка или скидка (plus или minus)
                "           p.change_price_type as change_price_type, " +       // тип наценки/скидки (валюта currency или проценты procents)
                "           coalesce(p.hide_tenths,false) as hide_tenths, " +   // убирать десятые (копейки)
                "           p.department_id as department_id, " +               // id отделения
                "           p.company_id as company_id, " +                     // id предприятия
                "           p.name as name, " +                                 // наименование инвентаризации по-умолчанию
                "           p.status_on_finish_id as status_on_finish_id " +    // статус документа при завершении инвентаризации
                "           from settings_inventory p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsInventoryJSON returnObj=new SettingsInventoryJSON();

            for(Object[] obj:queryList){
                returnObj.setPricingType((String)                       obj[0]);
                returnObj.setPriceTypeId(obj[1]!=null?Long.parseLong(   obj[1].toString()):null);
                returnObj.setChangePrice((BigDecimal)                   obj[2]);
                returnObj.setPlusMinus((String)                         obj[3]);
                returnObj.setChangePriceType((String)                   obj[4]);
                returnObj.setHideTenths((Boolean)                       obj[5]);
                returnObj.setDepartmentId(obj[7]!=null?Long.parseLong(  obj[7].toString()):null);
                returnObj.setCompanyId(Long.parseLong(                  obj[11].toString()));
                returnObj.setName((String)                              obj[14]);
                returnObj.setStatusOnFinishId(obj[15]!=null?Long.parseLong(obj[15].toString()):null);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsInventory. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

        @Transactional
        @SuppressWarnings("Duplicates")
        public boolean deleteInventory (String delNumbers) {
            //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
            if( (securityRepositoryJPA.userHasPermissions_OR(27L,"332") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(27L,"333") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(27L,"334") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",delNumbers)))
            {
                String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
                Long myId = userRepositoryJPA.getMyId();
                stringQuery = "Update inventory p" +
                        " set is_deleted=true, " + //удален
                        " changer_id="+ myId + ", " + // кто изменил (удалил)
                        " date_time_changed = now() " +//дату и время изменения
                        " where p.id in ("+delNumbers+")" +
                        " and coalesce(p.is_completed,false) !=true";
                try{
                    entityManager.createNativeQuery(stringQuery).executeUpdate();
                    return true;
                }catch (Exception e) {
                    logger.error("Exception in method deleteInventory. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        }

        @Transactional
        @SuppressWarnings("Duplicates")
        public boolean undeleteInventory(String delNumbers) {
            //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
            if( (securityRepositoryJPA.userHasPermissions_OR(27L,"332") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(27L,"333") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(27L,"334") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",delNumbers)))
            {
                // на MasterId не проверяю , т.к. выше уже проверено
                Long myId = userRepositoryJPA.getMyId();
                String stringQuery;
                stringQuery = "Update inventory p" +
                        " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                        " date_time_changed = now(), " +//дату и время изменения
                        " is_deleted=false " + //не удалена
                        " where p.id in (" + delNumbers+")";
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                        query.executeUpdate();
                        return true;
                    } else return false;
                }catch (Exception e) {
                    logger.error("Exception in method undeleteInventory. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        }

}

