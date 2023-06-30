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

import com.dokio.message.request.UserGroupForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.UserGroupJSON;
import com.dokio.message.response.UserGroupTableJSON;
import com.dokio.message.response.UserGroupListJSON;
import com.dokio.model.Permissions;
import com.dokio.model.User;
import com.dokio.model.UserGroup;
import com.dokio.security.services.UserDetailsServiceImpl;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Repository("UserGroupRepositoryJPA")
public class UserGroupRepositoryJPA {

    Logger logger = Logger.getLogger("UserGroupRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private UserDetailsServiceImpl userService;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    public UserGroupRepositoryJPA() {
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getUserGroupSize(String searchString, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(6L, "29,30"))// Группы пользователей: "Меню - все Группы пользователей","Меню - только свого предприятия"
        {
            Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
            int myCompanyId=userRepositoryJPA.getMyCompanyId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            String stringQuery = "from UserGroup p where p.master=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

//            stringQuery = stringQuery + " and coalesce(p.is_delete,false) !=true";
            if (!securityRepositoryJPA.userHasPermissions_OR(6L, "29")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 30
                stringQuery = stringQuery + " and p.companyId=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }

            if (companyId > 0) {
                stringQuery = stringQuery + " and  p.companyId=" + companyId;
            }

            Query query = entityManager.createQuery(stringQuery, UserGroup.class);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public List<UserGroupTableJSON> getUserGroupTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(6L, "29,30"))// Группы пользователей: "Меню - все Группы пользователей","Меню - только свого предприятия"
        {
            Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            int myCompanyId=userRepositoryJPA.getMyCompanyId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            String stringQuery = "select            " +
                    "   p.id as id,            " +
                    "   p.name as name,            " +
                    "   p.master_id as master_id,            " +
                    "   p.creator_id as creator_id,            " +
                    "   p.changer_id as changer_id,            " +
                    "   (select name from users where id=p.master_id) as master,            " +
                    "   (select name from users where id=p.creator_id) as creator,            " +
                    "   (select name from users where id=p.changer_id) as changer,            " +
                    "   to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "   to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "   p.date_time_created as date_time_created_sort, " +
                    "   p.date_time_changed as date_time_changed_sort, " +
                    "   coalesce(p.company_id,'0') as company_id,            " +
                    "   (select name from companies where id=p.company_id) as company,            " +
                    "   p.description as description            " +
                    "   from usergroup p           " +
                    "   where            " +
                    "   p.master_id=" + myMasterId +
                    "   and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(6L, "29")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 30
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }

            if (companyId > 0) {
                stringQuery = stringQuery + " and  p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = this.entityManager.createNativeQuery(stringQuery, UserGroupTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList();
        } else return null;
    }

    public List<Integer> getUserGroupPermissionsIdsByUserGroupId(int id) {
        String stringQuery="select p.permission_id from usergroup_permissions p where p.usergroup_id= "+id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public UserGroupJSON getUserGroupValuesById (int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(6L, "29,30"))//Группы пользователей: "Редактирование только документов своего предприятия","Редактирование документов всех предприятий"
        {
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            int myCompanyId=userRepositoryJPA.getMyCompanyId();
            String stringQuery = "select p.id as id, " +
                        "           p.name as name, " +
                        "           p.master_id as master_id, " +
                        "           p.creator_id as creator_id, " +
                        "           p.changer_id as changer_id, " +
                        "           (select name from users where id=p.master_id) as master, " +
                        "           (select name from users where id=p.creator_id) as creator, " +
                        "           (select name from users where id=p.changer_id) as changer, " +
                        "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                        "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                        "           coalesce(p.company_id,'0') as company_id, " +

                        "           p.description as description, " +
                        "           (select name from companies where id=p.company_id) as company " +
                        "           from usergroup p" +
                        " where p.id= " + id;
            stringQuery = stringQuery + " and p.master_id="+userRepositoryJPA.getMyMasterId();//принадлежит к документам моего родителя

            if (!securityRepositoryJPA.userHasPermissions_OR(6L, "29")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 30
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery, UserGroupJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
            UserGroupJSON response = (UserGroupJSON) query.getSingleResult();
            return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertUserGroup(UserGroupForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(6L,"31"))//  Группы пользователей : "Создание"
        {
            try{
                UserGroup userGroup = new UserGroup(request.getName(), request.getDescription());

                userGroup.setCompany(companyRepositoryJPA.getCompanyById(Long.valueOf(Integer.parseInt(request.getCompany_id()))));//предприятие

                User creator = userService.getUserByUsername(userService.getUserName());
                userGroup.setCreator(creator);//создателя

                User master = userRepository2.getUserByUsername(
                        userRepositoryJPA.getUsernameById(
                                userRepositoryJPA.getUserMasterIdByUsername(
                                        userRepository2.getUserName() )));
                userGroup.setMaster(master);//владельца

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                userGroup.setDate_time_created(timestamp);//дату создания

                entityManager.persist(userGroup);
                entityManager.flush();

                return userGroup.getId();

            }catch (Exception e) {
                logger.error("Exception in method insertUserGroup.", e);
                e.printStackTrace();
                return null;
            }

        } else return -1L;
    }

    @SuppressWarnings("Duplicates")
    public Set<UserGroup> getUserGroupSetBySetOfUserGroupId(Set<Long> userGroups) {
        EntityManager em = emf.createEntityManager();
        UserGroup dep = new UserGroup();
        Set<UserGroup> userGroupSet = new HashSet<>();
        for (Long i : userGroups) {
            dep = em.find(UserGroup.class, i);
            userGroupSet.add(dep);
        }
        return userGroupSet;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<UserGroupListJSON> getUserGroupListByCompanyId(int company_id) {
        String stringQuery;

        stringQuery="select " +
                "           p.id as id, " +
                "           p.description as description, " +
                "           p.name as name " +
                "           from usergroup p " +
                "           where coalesce(p.is_deleted,false) !=true and p.company_id="+company_id;


        stringQuery = stringQuery+" order by p.name asc";

        Query query =  entityManager.createNativeQuery(stringQuery, UserGroupListJSON.class);
        return query.getResultList();
    }

    @SuppressWarnings("Duplicates")
    public boolean updateUserGroup(UserGroupForm request) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(6L,"34") && securityRepositoryJPA.isItAllMyMastersDocuments("usergroup",String.valueOf(request.getId()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(6L,"33") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("usergroup",String.valueOf(request.getId()))))
        {
            EntityManager emgr = emf.createEntityManager();

            emgr.getTransaction().begin();

            Long id=Long.valueOf(request.getId());
            try {
                UserGroup userGroup = emgr.find(UserGroup.class, id);

                userGroup.setId(id);
                userGroup.setName           (request.getName() == null ? "": request.getName());
                userGroup.setDescription       (request.getDescription() == null ? "": request.getDescription());
                Set<Long> permissions = request.getSelectedUserGroupPermissions();
                if (!permissions.isEmpty()) {//если есть выбранные чекбоксы
                Set<Permissions> setPermissionsOfUserGroup= getPermissionsSetBySetOfPermissionsId(permissions);
                userGroup.setPermissions(setPermissionsOfUserGroup);
                } else { // если ни один чекбокс не выбран
                    userGroup.setPermissions(null);
                }

                User changer = userService.getUserByUsername(userService.getUserName());
                userGroup.setChanger(changer);//кто изменил

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                userGroup.setDate_time_changed(timestamp);//дату изменения

                emgr.getTransaction().commit();
                emgr.close();
                return true;

            } catch (Exception e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method updateUserGroup.", e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private Set<Permissions> getPermissionsSetBySetOfPermissionsId(Set<Long> permissions) {
        EntityManager em = emf.createEntityManager();
        Permissions dep = new Permissions();
        Set<Permissions> permissionsSet = new HashSet<>();
        for (Long i : permissions) {
            dep = em.find(Permissions.class, i);
            permissionsSet.add(dep);
        }
        return permissionsSet;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteUserGroups(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(6L, "32") && securityRepositoryJPA.isItAllMyMastersDocuments("usergroup", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(6L, "32") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("usergroup", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery = "update usergroup p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteUserGroups. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteUserGroups(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(6L,"32") && securityRepositoryJPA.isItAllMyMastersDocuments("usergroup",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(6L,"32") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("usergroup",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update usergroup p" +
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
                logger.error("Exception in method undeleteUserGroups. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
    @SuppressWarnings("Duplicates")
    public boolean setPermissionsToUserGroup(Set<Long> permissions, Long usergroupId) {
        EntityManager emgr = emf.createEntityManager();
        emgr.getTransaction().begin();
        try {
            UserGroup userGroup = emgr.find(UserGroup.class, usergroupId);
            userGroup.setId(usergroupId);
            Set<Permissions> setPermissionsOfUserGroup= getPermissionsSetBySetOfPermissionsId(permissions);
            userGroup.setPermissions(setPermissionsOfUserGroup);
            emgr.getTransaction().commit();
            emgr.close();
            return true;

        } catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method setPermissionsToUser.", e);
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertUsergroupFast(String name, Long companyId, Long myId, Long myMasterId) {
        String stringQuery;
        Long newDocId;
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        stringQuery = "insert into usergroup (" +
                " master_id," + //мастер-аккаунт
                " creator_id," + //создатель
                " company_id," + //предприятие, для которого создается документ
                " date_time_created," + //дата и время создания
                " is_deleted," +
                " name" +
                ") values ("+
                myMasterId + ", "+//мастер-аккаунт
                myId + ", "+ //создатель
                companyId + ", "+//предприятие, для которого создается документ
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                "false," +
                ":name" +
                ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",name);
            query.executeUpdate();
            stringQuery="select id from usergroup where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            newDocId=Long.valueOf(query2.getSingleResult().toString());
            stringQuery="insert into user_usergroup (user_id, usergroup_id) values ("+myId+", "+newDocId+")";
            Query query3 = entityManager.createNativeQuery(stringQuery);
            query3.executeUpdate();

            return newDocId;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Exception in method insertUsergroupFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
}
