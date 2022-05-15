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
import com.dokio.message.response.additional.DeleteDocsReport;
import com.dokio.message.response.additional.FilesInventoryJSON;
import com.dokio.message.response.additional.InventoryProductsListJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","name","status_name","product_count","is_completed","company","department","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

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
            String dateFormat=userRepositoryJPA.getMyDateFormat();

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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
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
                        " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
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
                throw new IllegalArgumentException("Недопустимые параметры запроса");
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
                    " to_char(p.doc_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(p.name)   like upper(CONCAT('%',:sg,'%')) or "+
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
                    " ip.id  as row_id, " +
                    " p.name as name," +
                    " ip.product_id," +
                    " ip.estimated_balance as estimated_balance," +
                    " ip.actual_balance as actual_balance," +
                    " ip.product_price," +
                    " coalesce((select edizm.short_name from sprav_sys_edizm edizm where edizm.id = coalesce(p.edizm_id,0)),'') as edizm, " +
                    " p.indivisible as indivisible" +// неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)

                    " from " +
                    " inventory_product ip " +
                    " INNER JOIN products p ON ip.product_id=p.id " +
                    " INNER JOIN inventory i ON ip.inventory_id=i.id " +
                    " where ip.master_id = " + myMasterId +
                    " and ip.inventory_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(27L, "336")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(27L, "337")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(27L, "338")) //Если нет прав на просмотр всех доков в своих подразделениях
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
                    doc.setIndivisible((Boolean)                            obj[7]);
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
            String dateFormat=userRepositoryJPA.getMyDateFormat();
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.status_id as status_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           p.name as name, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +  // инвентаризация завершена?
                    "           p.uid as uid" +

                    "           from inventory p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
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
                    doc.setUid((String)                           obj[21]);
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class})
    public Integer updateInventory(InventoryForm request){
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(27L,"340") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(27L,"341") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(27L,"342") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я (т.е. залогиненное лицо)
                (securityRepositoryJPA.userHasPermissions_OR(27L,"343") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("inventory",request.getId().toString())))
        {
            // если при сохранении еще и проводим документ (т.е. фактически была нажата кнопка "Провести"
            // проверим права на проведение
            if((request.getIs_completed()!=null && request.getIs_completed())){
                if(
                    !(  //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(27L,"631") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",request.getId().toString())) ||
                        //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
                        (securityRepositoryJPA.userHasPermissions_OR(27L,"632") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",request.getId().toString()))||
                        //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
                        (securityRepositoryJPA.userHasPermissions_OR(27L,"633") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",request.getId().toString()))||
                        //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                        (securityRepositoryJPA.userHasPermissions_OR(27L,"634") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("inventory",request.getId().toString()))
                    )
                ) return -1;
            }
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery =   " update inventory set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, "+
                    " name = '" + (request.getName() == null ? "" : request.getName()) + "', " +
                    " is_completed = " + (request.getIs_completed() == null ? false : request.getIs_completed()) + ", " +
                    " status_id = " + request.getStatus_id() +
                    " where " +
                    " id= "+request.getId();
            try
            {
                // если документ проводится - проверим, не является ли документ уже проведённым (такое может быть если открыть один и тот же документ в 2 окнах и провести их)
                if(commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "inventory"))
                    throw new DocumentAlreadyCompletedException();

                if(request.getIs_completed()!=null && request.getIs_completed() && request.getInventoryProductTable().size()==0) throw new Exception("There is no products in product list");

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.executeUpdate();
                if(insertInventoryProducts(request, request.getId(), myMasterId)){
                    return 1;
                } else return null;

            } catch (DocumentAlreadyCompletedException e) { //
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateInventory.", e);
                e.printStackTrace();
                return -50; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertInventory on inserting into inventory_products cause error.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                logger.error("Exception in method updateInventory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }


    // смена проведености документа с "Проведён" на "Не проведён"
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, NotEnoughPermissionsException.class})
    public Integer setInventoryAsDecompleted(InventoryForm request) throws Exception {
        // Есть ли права на проведение
        if( //Если есть право на "Проведение по всем предприятиям" и id принадлежат владельцу аккаунта (с которого проводят), ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(27L,"631") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",request.getId().toString())) ||
            //Если есть право на "Проведение по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта, ИЛИ
            (securityRepositoryJPA.userHasPermissions_OR(27L,"632") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",request.getId().toString()))||
            //Если есть право на "Проведение по своим отделениям и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(27L,"633") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",request.getId().toString()))||
            //Если есть право на "Проведение своих документов" и id принадлежат владельцу аккаунта (с которого проводят) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(27L,"634") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("inventory",request.getId().toString()))
        )
        {
            if(request.getInventoryProductTable().size()==0) throw new Exception("There is no products in this document");// на тот случай если документ придет без товаров (случаи всякие бывают)
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery =
                    " update inventory set " +
                            " changer_id = " + myId + ", "+
                            " date_time_changed= now()," +
                            " is_completed = false" +
                            " where " +
                            " id= " + request.getId();

            try {
                // проверим, не снят ли он уже с проведения (такое может быть если открыть один и тот же документ в 2 окнах и пытаться снять с проведения в каждом из них)
                if(!commonUtilites.isDocumentCompleted(request.getCompany_id(),request.getId(), "inventory"))
                    throw new DocumentAlreadyDecompletedException();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                // сохранение истории движения товара не делаем, т.к. в данный документ не влияет на движение товаров
                // по той же причине не делаем коррекцию баланса с контрагентом
                return 1;
            } catch (DocumentAlreadyDecompletedException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InventoryRepository/setInventoryAsDecompleted.", e);
                e.printStackTrace();
                return -60; // см. _ErrorCodes
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InventoryRepository/setInventoryAsDecompleted.", e);
                e.printStackTrace();
                return null;
            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method InventoryRepository/setInventoryAsDecompleted. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // Нет прав на проведение либо отмену проведения документа
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,CantInsertProductRowCauseErrorException.class})
    public Long insertInventory(InventoryForm request) {

        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Boolean iCan = securityRepositoryJPA.userHasPermissionsToCreateDoc( request.getCompany_id(), request.getDepartment_id(), 27L, "329", "330", "331");
        if(iCan==Boolean.TRUE)
        {

            String stringQuery;
            Long myId = userRepository.getUserId();
            Long newDocId;
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
                    " status_id,"+//статус инвентаризации
                    " uid" + // uid
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    request.getDepartment_id() + ", "+//отделение, из(для) которого создается документ
                    "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    doc_number + ", "+//номер заказа
                    " :name_, " +//наименование
                    " :description, " +//описание
                    request.getStatus_id() +//статус инвентаризации
                    ", :uid)";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("name_", (request.getName() == null ? "" : request.getName()));
                query.setParameter("uid", request.getUid());

                query.executeUpdate();
                stringQuery="select id from inventory where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                newDocId=Long.valueOf(query2.getSingleResult().toString());

                if(insertInventoryProducts(request, newDocId, myMasterId)){
                    return newDocId;
                } else return null;
            } catch (CantInsertProductRowCauseErrorException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertInventory on inserting into inventory_products cause error.", e);
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                logger.error("Exception in method insertInventory on inserting into inventory. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            //null - ошибка, т.е. либо предприятие или отдление не принадлежат мастер-аккаунту, либо друг другу
            //0 - недостаточно прав
            if(iCan==null) return null; else return -1L;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertInventoryProducts(InventoryForm request, Long newDocId, Long myMasterId) throws CantInsertProductRowCauseErrorException {
        Boolean insertProductRowResult; // отчет о сохранении позиции товара (строки таблицы). true - успешно false если превышено доступное кол-во товара на складе и записать нельзя, null если ошибка
        String productIds = "";
        //сохранение таблицы
        if (request.getInventoryProductTable()!=null && request.getInventoryProductTable().size() > 0) {

            for (InventoryProductTableForm row : request.getInventoryProductTable()) {
                row.setInventory_id(newDocId);
                insertProductRowResult = saveInventoryProductTable(row, request.getCompany_id(), myMasterId);  //сохранение таблицы товаров
                if (insertProductRowResult==null) {
                    throw new CantInsertProductRowCauseErrorException();//кидаем исключение чтобы произошла отмена транзакции из-за ошибки записи строки в таблицу товаров inventory_product
                }
                //копим id сохранённых товаров
                productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
            }
        }
        deleteInventoryProductTableExcessRows(productIds, request.getId(), myMasterId);
        return true;
    }

    @SuppressWarnings("Duplicates")//  удаляет лишние позиции товаров при сохранении инвентаризации (те позиции, которые ранее были в заказе, но потом их удалили)
    private Boolean deleteInventoryProductTableExcessRows(String productIds, Long inventory_id, Long myMasterId) {
        String stringQuery="";
        try {
            stringQuery =   " delete from inventory_product " +
                    " where inventory_id=" + inventory_id +
                    " and master_id=" + myMasterId +
                    (productIds.length()>0?(" and product_id not in (" + productIds + ")"):"");//если во фронте удалили все товары, то удаляем все товары в данном Заказе покупателя
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteInventoryProductTableExcessRows. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

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
                                " product_price = " + row.getProduct_price();
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

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean deleteInventoryProductTableRow(Long id) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery = " delete from inventory_product " +
                " where id="+id+" and master_id="+myMasterId;
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                return query.executeUpdate() == 1;
            }
            catch (Exception e) {
                logger.error("Exception in method deleteInventoryProductTableRow. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return false;
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
                            "pricing_type, " +          //тип расценки (выпад. список: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                            "price_type_id, " +         //тип цены из справочника Типы цен
                            "change_price, " +          //наценка/скидка в цифре (например, 50)
                            "plus_minus, " +            //определят, чем является changePrice - наценкой или скидкой (принимает значения plus или minus)
                            "change_price_type, " +     //тип наценки/скидки. Принимает значения currency (валюта) или procents(проценты)
                            "hide_tenths, " +           //убирать десятые (копейки) - boolean
                            "department_id, " +         //отделение по умолчанию
                            "name, "+                   //наименование заказа
                            "status_on_finish_id, "+    //статус документа при завершении инвентаризации
                            "default_actual_balance, "+ // фактический баланс по умолчанию. "estimated" - как расчётный, "other" - другой (выбирается в other_actual_balance)
                            "other_actual_balance,"+    // другой фактический баланс по умолчанию. Например, 1
                            "auto_add"+                 // автодобавление товара из формы поиска в таблицу
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
                            row.getStatusOnFinishId() + ",'" +
                            row.getDefaultActualBalance() + "'," +
                            row.getOtherActualBalance() + "," +
                            row.getAutoAdd() +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_inventory_user_uq " +// "upsert"
                            " DO update set " +
                            " pricing_type = '" + row.getPricingType() + "',"+
                            " price_type_id = " + row.getPriceTypeId() + ","+
                            " change_price = " + row.getChangePrice() + ","+
                            " plus_minus = '" + row.getPlusMinus() + "',"+
                            " change_price_type = '" + row.getChangePriceType() + "',"+
                            " hide_tenths = " + row.getHideTenths() +
                            ", department_id = "+row.getDepartmentId()+//некоторые строки (как эту) проверяем на null, потому что при сохранении из расценки они не отправляются, и эти настройки сбрасываются изза того, что в них прописываются null
                            ", company_id = "+row.getCompanyId()+
                            ", name = '"+row.getName()+"'"+
                            ", status_on_finish_id = "+row.getStatusOnFinishId()+
                            ", default_actual_balance = '"+row.getDefaultActualBalance()+"'"+
                            ", other_actual_balance = "+row.getOtherActualBalance()+
                            ", auto_add = "+row.getAutoAdd();

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
                "           p.pricing_type as pricing_type, " +                         // тип расценки (радиокнопки: 1. Тип цены (priceType), 2. Ср. себестоимость (avgCostPrice) 3. Последняя закупочная цена (lastPurchasePrice) 4. Средняя закупочная цена (avgPurchasePrice))
                "           p.price_type_id as price_type_id, " +                       // тип цены из справочника Типы цен
                "           p.change_price as change_price, " +                         // наценка/скидка в цифре (например, 50)
                "           p.plus_minus as plus_minus, " +                             // определят, что есть changePrice - наценка или скидка (plus или minus)
                "           p.change_price_type as change_price_type, " +               // тип наценки/скидки (валюта currency или проценты procents)
                "           coalesce(p.hide_tenths,false) as hide_tenths, " +           // убирать десятые (копейки)
                "           p.department_id as department_id, " +                       // id отделения
                "           p.company_id as company_id, " +                             // id предприятия
                "           p.name as name, " +                                         // наименование инвентаризации по-умолчанию
                "           p.status_on_finish_id as status_on_finish_id, " +           // статус документа при завершении инвентаризации
                "           p.default_actual_balance as default_actual_balance, " +     // фактический баланс по умолчанию. "estimated" - как расчётный, "other" - другой (выбирается в other_actual_balance)
                "           p.other_actual_balance as other_actual_balance, " +         // "другой" фактический баланс по умолчанию. Например, 1
                "           coalesce(p.auto_add,false) as auto_add  " +                 // автодобавление товара из формы поиска в таблицу
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
                returnObj.setDepartmentId(obj[6]!=null?Long.parseLong(  obj[6].toString()):null);
                returnObj.setCompanyId(Long.parseLong(                  obj[7].toString()));
                returnObj.setName((String)                              obj[8]);
                returnObj.setStatusOnFinishId(obj[9]!=null?Long.parseLong(obj[9].toString()):null);
                returnObj.setDefaultActualBalance((String)              obj[10]);
                returnObj.setOtherActualBalance((BigDecimal)            obj[11]);
                returnObj.setAutoAdd((Boolean)                          obj[12]);
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsInventory. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }
    }

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
        public DeleteDocsReport deleteInventory (String delNumbers) {
        DeleteDocsReport delResult = new DeleteDocsReport();
            //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
            if( (securityRepositoryJPA.userHasPermissions_OR(27L,"332") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(27L,"333") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(27L,"334") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",delNumbers)))
            {
                // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
                List<LinkedDocsJSON> checkChilds = linkedDocsUtilites.checkDocHasLinkedChilds(delNumbers, "inventory");

                if(!Objects.isNull(checkChilds)) { //если нет ошибки

                    if(checkChilds.size()==0) { //если связи с дочерними документами отсутствуют
                        String stringQuery;// (на MasterId не проверяю , т.к. выше уже проверено)
                        Long myId = userRepositoryJPA.getMyId();
                        stringQuery = "Update inventory p" +
                        " set is_deleted=true, " + //удален
                        " changer_id="+ myId + ", " + // кто изменил (удалил)
                        " date_time_changed = now() " +//дату и время изменения
                        " where p.id in ("+delNumbers+")" +
                        " and coalesce(p.is_completed,false) !=true";
                        try {
                            entityManager.createNativeQuery(stringQuery).executeUpdate();
                            //удалим документы из группы связанных документов
                            if (!linkedDocsUtilites.deleteFromLinkedDocs(delNumbers, "inventory")) throw new Exception ();
                            delResult.setResult(0);// 0 - Всё ок
                            return delResult;
                        } catch (Exception e) {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            logger.error("Exception in method deleteInventory. SQL query:" + stringQuery, e);
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
        public Integer undeleteInventory(String delNumbers) {
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
                        return 1;
                    } else return null;
                }catch (Exception e) {
                    logger.error("Exception in method undeleteInventory. SQL query:"+stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else return -1;
        }

    @SuppressWarnings("Duplicates")
    public List<InventoryProductsListJSON> getInventoryProductsList(String searchString, Long companyId, Long departmentId, Long priceTypeId) {
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
                "           (select coalesce(quantity,0)   from product_quantity     where department_id = "    + departmentId +" and product_id = p.id) as estimated_balance, " +
                // цена по запрашиваемому типу цены (будет 0 если такой типа цены у товара не назначен)
                "           coalesce((select pp.price_value from product_prices pp where pp.product_id=p.id and  pp.price_type_id = "+priceTypeId+"),0) as price_by_typeprice, " +
                // цена по запрашиваемому типу цены priceTypeId (если тип цены не запрашивается - ставим null в качестве цены по отсутствующему в запросе типу цены)
//                (priceTypeId>0?" pp.price_value":null) + " as price_by_typeprice," +
                // средняя себестоимость
                "           (select ph.avg_netcost_price   from products_history ph where ph.department_id = "  + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgCostPrice, " +
                // средняя закупочная цена
                "           (select ph.avg_purchase_price  from products_history ph  where ph.department_id = " + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as avgPurchasePrice, " +
                // последняя закупочная цена
                "           (select ph.last_purchase_price from products_history ph  where ph.department_id = " + departmentId +" and ph.product_id = p.id order by ph.id desc limit 1) as lastPurchasePrice, " +
                // неделимый товар (нельзя что-то сделать с, например, 0.5 единицами этого товара, только с кратно 1)
                "           p.indivisible as indivisible" +

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
//                (priceTypeId>0?(", pp.price_value"):"") + //если тип цены запрашивается - группируем таже и по нему
                "  order by p.name asc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty()){query.setParameter("sg", searchString);}

            List<Object[]> queryList = query.getResultList();
            List<InventoryProductsListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                InventoryProductsListJSON product = new InventoryProductsListJSON();
                product.setProduct_id(Long.parseLong(                       obj[0].toString()));
                product.setName((String)                                    obj[1]);
                product.setEdizm((String)                                   obj[2]);
                product.setFilename((String)                                obj[3]);
                product.setEstimated_balance(                               obj[4]==null?BigDecimal.ZERO:(BigDecimal)obj[4]);
                product.setPriceOfTypePrice(                                obj[5]==null?BigDecimal.ZERO:(BigDecimal)obj[5]);
                product.setAvgCostPrice(                                    obj[6]==null?BigDecimal.ZERO:(BigDecimal)obj[6]);
                product.setAvgPurchasePrice(                                obj[7]==null?BigDecimal.ZERO:(BigDecimal)obj[7]);
                product.setLastPurchasePrice(                               obj[8]==null?BigDecimal.ZERO:(BigDecimal)obj[8]);
                product.setIndivisible((Boolean)                            obj[9]);
                returnList.add(product);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getInventoryProductsList. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

//    @SuppressWarnings("Duplicates")
//    public List<LinkedDocsJSON> getInventoryLinkedDocsList(Long docId, String docName) {
//        String stringQuery;
//        String myTimeZone = userRepository.getUserTimeZone();
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//        String tableName=(docName.equals("posting")?"posting":"writeoff");//не могу воткнуть имя таблицы параметром, т.к. the parameters can come from the outside and could take any value, whereas the table and column names are static.
//        stringQuery =   " select " +
//                " ap.id," +
//                " to_char(ap.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI'), " +
//                " ap.description," +
//                " coalesce(ap.is_completed,false)," +
//                " ap.doc_number" +
//                " from "+tableName+" ap" +
//                " where ap.master_id = " + myMasterId +
//                " and coalesce(ap.is_deleted,false)!=true "+
//                " and ap.inventory_id = " + docId;
//        stringQuery = stringQuery + " order by ap.date_time_created asc ";
//        try{
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Object[]> queryList = query.getResultList();
//            List<LinkedDocsJSON> returnList = new ArrayList<>();
//            for(Object[] obj:queryList){
//                LinkedDocsJSON doc=new LinkedDocsJSON();
//                doc.setId(Long.parseLong(                       obj[0].toString()));
//                doc.setDate_time_created((String)               obj[1]);
//                doc.setDescription((String)                     obj[2]);
//                doc.setIs_completed((Boolean)                   obj[3]);
//                doc.setDoc_number(Long.parseLong(               obj[4].toString()));
//                returnList.add(doc);
//            }
//            return returnList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getInventoryLinkedDocsList. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }
    //*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean addFilesToInventory(UniversalForm request){
        Long inventoryId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(27L,"340") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory",inventoryId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(27L,"341") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",inventoryId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(27L,"342") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",inventoryId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(27L,"343") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("inventory",inventoryId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select inventory_id from inventory_files where inventory_id=" + inventoryId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_InventoryId_FileId(inventoryId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                logger.error("Exception in method InventoryRepository/addFilesToInventory.", ex);
                ex.printStackTrace();
                return false;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_InventoryId_FileId(Long inventoryId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into inventory_files " +
                    "(inventory_id,file_id) " +
                    "values " +
                    "(" + inventoryId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            logger.error("Exception in method InventoryRepository/manyToMany_InventoryId_FileId." , ex);
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesInventoryJSON> getListOfInventoryFiles(Long inventoryId) {
        if(securityRepositoryJPA.userHasPermissions_OR(27L, "336,337,338,339"))//Просмотр документов
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
                    "           inventory p" +
                    "           inner join" +
                    "           inventory_files pf" +
                    "           on p.id=pf.inventory_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + inventoryId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
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
            stringQuery = stringQuery+" order by f.original_name asc ";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if(needToSetParameter_MyDepthsIds)
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                List<Object[]> queryList = query.getResultList();

                List<FilesInventoryJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    FilesInventoryJSON doc=new FilesInventoryJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setDate_time_created((Timestamp)                    obj[1]);
                    doc.setName((String)                                    obj[2]);
                    doc.setOriginal_name((String)                           obj[3]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getListOfInventoryFiles. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteInventoryFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(27L,"340") && securityRepositoryJPA.isItAllMyMastersDocuments("inventory", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(27L,"341") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("inventory",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(27L,"342") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("inventory",String.valueOf(request.getAny_id())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(27L,"343") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("inventory",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from inventory_files "+
                    " where inventory_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from inventory where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method InventoryRepository/deleteInventoryFile. stringQuery=" + stringQuery, e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}

