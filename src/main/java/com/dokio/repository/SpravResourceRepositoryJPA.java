/*
        DokioCRM - server part. Sales, finance and warehouse management system
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

import com.dokio.message.request.Sprav.SpravResourceForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.ResourceDepPartsForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.SpravResourceJSON;
import com.dokio.message.response.additional.ResourceDepPart;
import com.dokio.message.response.additional.ResourceJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class SpravResourceRepositoryJPA {

    Logger logger = Logger.getLogger("SpravResourceRepositoryJPA");

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
    UserDetailsServiceImpl userService;
    @Autowired
    CommonUtilites cu;
    @Autowired
    DepartmentRepositoryJPA departmentRepository;
    @Autowired
    private CommonUtilites commonUtilites;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","description","cagent","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    public List<SpravResourceJSON> getResourceTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(56L, "688,689"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort  " +
                    "           from sprav_resources p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;


            if (!securityRepositoryJPA.userHasPermissions_OR(56L, "688")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
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

                List<Object[]> queryList = query.getResultList();
                List<SpravResourceJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    SpravResourceJSON doc = new SpravResourceJSON();

                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setCreator((String)                                 obj[1]);
                    doc.setChanger((String)                                 obj[2]);
                    doc.setCreator_id(obj[3] != null ? Long.parseLong(      obj[3].toString()) : null);
                    doc.setChanger_id(obj[4] != null ? Long.parseLong(      obj[4].toString()) : null);
                    doc.setCompany_id(Long.parseLong(                       obj[5].toString()));
                    doc.setCompany((String)                                 obj[6]);
                    doc.setDate_time_created((String)                       obj[7]);
                    doc.setDate_time_changed((String)                       obj[8]);
                    doc.setName((String)                                    obj[9]);
                    doc.setDescription((String)                             obj[10]);
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getResourceTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getResourceSize(String searchString, long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(56L, "688,689"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from sprav_resources p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(56L, "688")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public SpravResourceJSON getResourceValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(56L, "688,689"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            stringQuery = "select  p.id as id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from sprav_resources p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(56L, "688")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (689)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SpravResourceJSON doc = new SpravResourceJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setCreator((String) obj[1]);
                doc.setChanger((String) obj[2]);
                doc.setCreator_id(obj[3] != null ? Long.parseLong(obj[3].toString()) : null);
                doc.setChanger_id(obj[4] != null ? Long.parseLong(obj[4].toString()) : null);
                doc.setCompany_id(Long.parseLong(obj[5].toString()));
                doc.setCompany((String) obj[6]);
                doc.setDate_time_created((String) obj[7]);
                doc.setDate_time_changed((String) obj[8]);
                doc.setName((String) obj[9]);
                doc.setDescription((String) obj[10]);
                doc.setDep_parts(departmentRepository.getDepartmentPartsWithResourceQttList (id, myMasterId));
            }
            return doc;
        } else return null;

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Integer updateResource(SpravResourceForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(56L,"690") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_resources",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(56L,"691") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_resources",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            String stringQuery;
            stringQuery =   " update sprav_resources set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " name = :name, " +
                    " description = :description " +
                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.executeUpdate();
                Set<Long>existingDepartmentParts = new HashSet<>();
                for (ResourceDepPartsForm row : request.getDepartmentPartsTable()) {
                    saveDepartmentPartsQtt(myMasterId, row.getId(), request.getId(), row.getResource_qtt());
                    existingDepartmentParts.add(row.getId());
                }
                deleteDepartmentPartsThatNoMoreContainThisResource(existingDepartmentParts,request.getId(), myMasterId );
                return 1;

            }catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateResource. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав

    }

    // Возвращаем id в случае успешного создания
    // Возвращаем null в случае ошибки
    // Возвращаем -1 в случае отсутствия прав
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,Exception.class})
    public Long insertResource(SpravResourceForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getMyMasterId();

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(56L, "684")) ||
                        //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                        (securityRepositoryJPA.userHasPermissions_OR(56L, "685") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();

            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery = "insert into sprav_resources (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " date_time_created," + //дата и время создания
                    " name," +//наименование
                    " description" +// тип
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    " :name, " +
                    " :description)";
            try{
                cu.idBelongsMyMaster("companies", request.getCompany_id(), myMasterId);

                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.executeUpdate();
                stringQuery="select id from sprav_resources where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                Long newDocId = Long.valueOf(query2.getSingleResult().toString());
                for (ResourceDepPartsForm row : request.getDepartmentPartsTable()) {
                    saveDepartmentPartsQtt(myMasterId, row.getId(), newDocId, row.getResource_qtt());
                }
                return newDocId;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method insertResource on inserting into sprav_resources. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            return -1L;
        }
    }

    @Transactional
    public Integer deleteResource(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(56L, "686") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_resources", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(56L, "687") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_resources", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_resources p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteResource. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    @Transactional
    public Integer undeleteResource(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(56L, "686") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_resources", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(56L, "687") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_resources", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_resources p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteResource. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }


    private void saveDepartmentPartsQtt(Long master_id, Long dep_part_id, Long resource_id, int quantity) throws Exception {
        String stringQuery = "insert into scdl_resource_dep_parts_qtt (" +
                "   master_id," +
                "   dep_part_id," +
                "   resource_id," +
                "   quantity " +
                "   ) values (" +
                    master_id+", "+
                    dep_part_id+", "+
                    resource_id+", " +
                    quantity +// чтобы не мочь изменить категорию другого master_id, случайно или намеренно
                ") ON CONFLICT ON CONSTRAINT scdl_resource_dep_parts_qtt_uq " +// "upsert"
                "   DO update set " +
                "   quantity = "+quantity;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveDepartmentPartsQtt. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }

    // Deleting department parts that no more contain this resource
    private void deleteDepartmentPartsThatNoMoreContainThisResource(Set<Long> existingDepartmentParts, Long resourceId, Long masterId) throws Exception  {
        String stringQuery =
                " delete from scdl_resource_dep_parts_qtt " +
                        " where " +
                        " master_id = " + masterId + " and " +
                        " resource_id = " +resourceId;
        if(existingDepartmentParts.size()>0)
            stringQuery = stringQuery + " and dep_part_id not in " + commonUtilites.SetOfLongToString(existingDepartmentParts,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteDepartmentPartsThatNoMoreContainThisResource. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    public List<ResourceJSON> getResourcesList(long companyId) {
//        if (securityRepositoryJPA.userHasPermissions_OR(56L, "688,689"))//(см. файл Permissions Id)
//        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            stringQuery = "select   p.id as id, " +
                    "               p.name as name, " +
                    "               p.description as description" +
                    "               from sprav_resources p " +
                    "               where  p.master_id=" + myMasterId +
                    "               and p.company_id=" + companyId +
                    "               and coalesce(p.is_deleted,false) = false";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                List<ResourceJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    ResourceJSON doc = new ResourceJSON();
                    doc.setResource_id(Long.parseLong(                      obj[0].toString()));
                    doc.setName((String)                                    obj[1]);
                    doc.setDescription((String)                             obj[2]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getResourcesList. SQL query:" + stringQuery, e);
                return null;
            }
//        } else return null;
    }

    public List<ResourceDepPart> getResourcesList(long companyId, long myMasterId) {
        String stringQuery;
        stringQuery =   " select  r.id as resource_id, " +
                        " r.name as resource_name, " +
                        " r.description as resource_description, " +
                        " dpr.quantity as resource_qtt, " +
                        " dp.id as dep_part_id, " +
                        " true as is_active" +
                        " from" +
                        " sprav_resources r, " +
                        " scdl_dep_parts dp, " +
                        " scdl_resource_dep_parts_qtt dpr " +
                        " where  r.master_id=" + myMasterId +
                        " and r.company_id=" + companyId +
                        " and coalesce(r.is_deleted,false) = false " +
                        " and r.id=dpr.resource_id " +
                        " and dp.id=dpr.dep_part_id " +
                        " and coalesce(dpr.quantity,0) > 0 " +
                        " order by dpr.dep_part_id ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ResourceDepPart> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ResourceDepPart doc = new ResourceDepPart();
                doc.setResource_id(Long.parseLong(                      obj[0].toString()));
                doc.setName((String)                                    obj[1]);
                doc.setDescription((String)                             obj[2]);
                doc.setResource_qtt((Integer)                           obj[3]);
                doc.setDep_part_id(Long.parseLong(                      obj[4].toString()));
                doc.setActive((Boolean)                                 obj[5]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getResourcesList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    public List<ResourceDepPart> getNowUsedResourcesList(AppointmentMainInfoForm request) {
        Long masterId = userRepositoryJPA.getMyMasterId();
        UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
        String myTimeZone = userSettings.getTime_zone();
        String stringQuery;
        stringQuery = 	" select  r.id as resource_id, " +
                        " r.name as resource_name, " +
                        " r.description as resource_description, " +
                        " dpr.quantity as resource_qtt, " +
                        " dp.id as dep_part_id, " +
                        " true as is_active, " +
						" (	select  " +
						"  	coalesce(sum(pr.quantity),0)" +
						"  	from scdl_product_resource_qtt pr  " +
						"  	where  " +
						"  	pr.resource_id=r.id and " +
						" 	pr.product_id in ( " +
						" 		select ap.product_id  " +
						" 		from scdl_appointment_products ap " +
						" 		where " +
						" 	 	ap.appointment_id in ( " +
						" 			select a.id  " +
						" 			from scdl_appointments a, " +
                        "           sprav_status_dock ssd " +
						" 			where " +
						" 			a.master_id="+masterId+" and " +
						" 			a.company_id="+request.getCompanyId() + " and " +
                        "           a.status_id = ssd.id and " +
                        "           ssd.status_type != 3 and " + // don't collect resources of cancelled Appointments
                        "           a.id != " + request.getAppointmentId() + " and " + //  don't collect resources of current appointment , from which is going this calling
						" 			coalesce(a.is_deleted,false)=false and " + //The formula of intersection is: A_end > B_start AND A_start < B_end
                        "           to_timestamp('"+request.getDateTo()+" "+request.getTimeTo()+"','DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' > a.starts_at_time and " +
                        "           to_timestamp('"+request.getDateFrom()+" "+request.getTimeFrom()+"','DD.MM.YYYY HH24:MI') at time zone 'Etc/GMT+0' at time zone '"+myTimeZone+"' < a.ends_at_time" +
                        //  !!! ADD STATUS OF CANCELLED APPOINTMENT WHEN STATUSES WILL BE DONE !!!
                        " 			) " +
						" 		) " +
						" 	) as now_used" +
                        " from " +
                        " sprav_resources r, " +
                        " scdl_dep_parts dp, " +
                        " scdl_resource_dep_parts_qtt dpr " +
                        " where  r.master_id=" + masterId +
                        " and r.company_id=" +request.getCompanyId() +
                        " and coalesce(r.is_deleted,false) = false " +
                        " and r.id=dpr.resource_id " +
                        " and dp.id=dpr.dep_part_id " +
                        " order by dpr.dep_part_id ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ResourceDepPart> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                ResourceDepPart doc = new ResourceDepPart();
                doc.setResource_id(Long.parseLong(                      obj[0].toString()));
                doc.setName((String)                                    obj[1]);
                doc.setDescription((String)                             obj[2]);
                doc.setResource_qtt((Integer)                           obj[3]);
                doc.setDep_part_id(Long.parseLong(                      obj[4].toString()));
                doc.setActive((Boolean)                                 obj[5]);
                doc.setNow_used(((BigInteger)                           obj[6]).longValue());
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getNowUsedResourcesList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    // inserting base set of cash room for new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertResourcesFast(Long mId, Long masterId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = cu.translateForUser(mId, new String[]{"'work_place'"});
        stringQuery = "insert into sprav_resources ( master_id,creator_id,company_id,date_time_created,name,is_deleted) values "+
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("work_place")+"',false)";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery="select id from sprav_resources where date_time_created=(to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+mId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query2.getSingleResult().toString());
        } catch (Exception e) {
            logger.error("Exception in method insertResourcesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

}