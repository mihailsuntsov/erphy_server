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

import com.dokio.message.request.Settings.UserSettingsForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.UsersJSON;
import com.dokio.message.response.UsersListJSON;
import com.dokio.message.response.UsersTableJSON;
import com.dokio.message.response.additional.MyShortInfoJSON;
import com.dokio.message.response.additional.UserResources;
import com.dokio.model.Departments;
import com.dokio.model.User;
import com.dokio.model.UserGroup;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.persistence.*;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository("UserRepositoryJPA")
public class UserRepositoryJPA {

    Logger logger = Logger.getLogger("UserRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DepartmentRepositoryJPA departmentRepositoryJPA;

    @Autowired
    private UserGroupRepositoryJPA userGroupRepositoryJPA;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private UserDetailsServiceImpl userDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityRepositoryJPA securityRepositoryJPA;

    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    @Autowired
    private CommonUtilites commonUtilites;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

//    public boolean putUserToCompany(Long userId, Long companyId){
//        EntityManager em = emf.createEntityManager();
//        em.getTransaction().begin();
//        User usr = em.find(User.class, userId);
//        Companies company = em.find(Companies.class, companyId);
//        usr.setCompany(company);
//        em.getTransaction().commit();
//        em.close();
//        return true;
//    }
//
//    public User getUserById(Long userId){
//        EntityManager em = emf.createEntityManager();
//        User usr = em.find(User.class, userId);
//        return usr;
//    }
//    // меняет пароль пользователя
//    public void changeUserPassword(final User user, final String password) {
//        user.setPassword(passwordEncoder.encode(password));
//        userRepository.save(user);
//    }

    //сравнивает пароль oldPassword с паролем пользователя
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    //@Transactional
    @SuppressWarnings("Duplicates")
    public Integer updateUser(SignUpForm request) {
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(5L, "26"); // Пользователи:"Редактирование своего"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(5L, "27"); // Пользователи:"Редактирование всех"
        boolean requestUserIdEqualMyUserId=(userDetailService.getUserId()==Long.valueOf(request.getId()));

        if(((requestUserIdEqualMyUserId && userHasPermissions_OwnUpdate)//(если пользователь сохраняет свой аккаунт и у него есть на это права
                ||(!requestUserIdEqualMyUserId && userHasPermissions_AllUpdate))//или если пользователь сохраняет чужой аккаунт и у него есть на это права)
                && securityRepositoryJPA.isItMyMastersUser(Long.valueOf(request.getId()))) //и сохраняемый аккаунт под юрисдикцией главного аккаунта
        {
            EntityManager em = emf.createEntityManager();
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            em.getTransaction().begin();

            Long id = Long.valueOf(request.getId());
            String dateBirth = (request.getDate_birthday() == null ? "" : request.getDate_birthday());

            User user = em.find(User.class, id);

            user.setId(id);
            user.setName(request.getName() == null ? "" : request.getName());
            user.setFio_name(request.getFio_name() == null ? "" : request.getFio_name());
            user.setFio_otchestvo(request.getFio_otchestvo() == null ? "" : request.getFio_otchestvo());
            user.setFio_family(request.getFio_family() == null ? "" : request.getFio_family());
            user.setAdditional(request.getAdditional() == null ? "" : request.getAdditional());
            try {
                user.setDate_birthday(dateBirth.isEmpty() ? null : dateFormat.parse(dateBirth));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            user.setStatus_account(Integer.parseInt(request.getStatus_account()));
            user.setSex(request.getSex() == null ? "" : request.getSex());
            user.setAdditional(request.getAdditional() == null ? "" : request.getAdditional());
            Set<Long> departments = request.getSelectedUserDepartments();
            Set<Long> userGroups = request.getUserGroupList();
            Set<Departments> setDepartmentsOfUser = departmentRepositoryJPA.getDepartmentsSetBySetOfDepartmentsId(departments);
            Set<UserGroup> setUserGroup = userGroupRepositoryJPA.getUserGroupSetBySetOfUserGroupId(userGroups);

            user.setDepartments(setDepartmentsOfUser);
            user.setUsergroup(setUserGroup);

            User changer = userDetailService.getUserByUsername(userDetailService.getUserName());
            user.setChanger(changer);//кто изменил

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            user.setDate_time_changed(timestamp);//дату изменения

//            user.setTime_zone_id(request.getTime_zone_id());

            user.setVatin(request.getVatin());

            em.getTransaction().commit();
            em.close();
            return 1;
        }else return -1;
    }

    //Находит родительский аккаунт у текущего пользователя.
    //Родительский - аккаунт, который создал текущего пользователя.
    //Если аккаунт создавался через регистрацию, он сам является своим родителем
    //Все аккаунты, созданные дочерними аккаунтами, также являются дочерними к родителю создавшего их аккаунта
    @SuppressWarnings("Duplicates")
    public Long getUserMasterIdByUsername(String username) {
        Long userId = userDetailService.getUserIdByUsername(userDetailService.getUserName());
        String stringQuery;
        stringQuery="select u.master_id from users u where u.id = "+userId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return  Long.valueOf((Integer) query.getSingleResult());
    }
    public Long getUserMasterIdByUserId(Long userId) {
        String stringQuery;
        stringQuery="select u.master_id from users u where u.id = "+userId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return  Long.parseLong(query.getSingleResult().toString());
    }

    public Long getMyId() {
        return userDetailService.getUserIdByUsername(userDetailService.getUserName());
    }

    @SuppressWarnings("Duplicates")
    public Long getMyMasterId() {
        Long myId = getMyId();
        String stringQuery;
        stringQuery="select u.master_id from users u where u.id = "+myId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return  Long.valueOf((Integer) query.getSingleResult());
    }

    public String getUsernameById(Long userId) {
        String stringQuery;
        stringQuery="select u.username from users u where u.id = "+userId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (String) query.getSingleResult();
    }
//    public String getMasterIdByCompanyId(Long companyId) {
//        String stringQuery;
//        stringQuery="select u.username from users u where u.id = "+userId;
//        Query query = entityManager.createNativeQuery(stringQuery);
//        return (String) query.getSingleResult();
//    }
    public List<String> getUserDepartmentsNames(int id) {
        String stringQuery="select dep.name||' '||dep.address as name " +
                "from departments dep where dep.id in (select p.department_id from user_department p where p.user_id= "+id+")"+
                " and coalesce(dep.is_deleted,false)!=true";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<String> depNames = query.getResultList();
        return depNames;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<UsersListJSON> getUsersListByDepartmentId(int did) {
        String stringQuery;

        stringQuery="select " +
                "           u.id as id, " +
                "           u.name as name " +
                "           from users u " +
                "           where u.status_account<4 and u.company_id="+getMyCompanyId()+
                "           and u.id in(select user_id from user_department where department_id="+did+")";

        stringQuery = stringQuery+" order by u.name asc";

        Query query =  entityManager.createNativeQuery(stringQuery, UsersListJSON.class);
        return query.getResultList();
    }

    //отдает сотрудников (пользователей) по id отделения
    @SuppressWarnings("Duplicates")
    public List<UsersListJSON> getEmployeeListByDepartmentId(Long did) {
        String stringQuery;

        stringQuery="select " +
                "           u.id as id, " +
                "           u.name as name " +
                "           from users u " +
                "           where u.status_account<4 and u.company_id="+getMyCompanyId()+
                "           and u.id in(select user_id from user_department where department_id="+did+")";
        stringQuery = stringQuery+" order by u.name asc";

        try
        {
            Query query =  entityManager.createNativeQuery(stringQuery, UsersListJSON.class);
            return query.getResultList();
        }catch (Exception e) {
            logger.error("Exception in method getEmployeeListByDepartmentId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    //поиск пользователей предприятия по подстроке
    @SuppressWarnings("Duplicates")
    public List<IdAndName> getUsersList(Long companyId, String search_string) {
        String stringQuery="select " +
                "           u.id as id, " +
                "           u.name as name " +
                "           from users u " +
                "           where u.status_account<4 and u.company_id="+companyId+
                "           and master_id=" + getMyMasterId();
        if(!Objects.isNull(search_string) && search_string.length()>0)
            stringQuery=stringQuery+" and upper(u.name) like upper(CONCAT('%',:sg,'%'))";
        stringQuery=stringQuery+" order by u.name asc";
        try
        {
            Query query =  entityManager.createNativeQuery(stringQuery);
            if(!Objects.isNull(search_string) && search_string.length()>0)
                query.setParameter("sg", search_string);
            List<Object[]> queryList = query.getResultList();
            List<IdAndName> usersList = new ArrayList<>();
            for(Object[] obj:queryList){
                IdAndName doc=new IdAndName();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setName((String)                                    obj[1]);
                usersList.add(doc);
            }
            return usersList;
        }catch (Exception e) {
            logger.error("Exception in method getUsersList. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<Integer> getUserDepartmentsId(int id) {
        String stringQuery="" +
                "select p.department_id as did" +
                " from " +
                " user_department p," +
                " departments dpts" +
                " where " +
                " p.user_id= "+id+
                " and p.department_id=dpts.id " +
                " and coalesce(dpts.is_deleted,false)!=true";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }
    @SuppressWarnings("Duplicates")
    public List<Integer> getMyDepartmentsId() {
        Long myId = getMyId();
        String stringQuery="" +
                "select p.department_id as did" +
                " from " +
                " user_department p," +
                " departments dpts" +
                " where " +
                " p.user_id= "+myId+
                " and p.department_id=dpts.id " +
                " and coalesce(dpts.is_deleted,false)!=true";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }
    @SuppressWarnings("Duplicates")
    public List<Long> getMyDepartmentsId_LONG() {
        Long myId = getMyId();
        String stringQuery="select p.department_id as did" +
                " from " +
                " user_department p," +
                " departments dpts" +
                " where " +
                " p.user_id= "+myId+
                " and p.department_id=dpts.id " +
                " and coalesce(dpts.is_deleted,false)!=true";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Long> depIds = new ArrayList<>();
        for(Object i: query.getResultList()){
            depIds.add(new Long(i.toString()));
        }//иначе в этом листе будут интеджеры, хоть он и лонг
        return depIds;
    }
    @SuppressWarnings("Duplicates")
    public List<Integer> getUserGroupsId(int id) {
        String stringQuery="select p.usergroup_id as did from user_usergroup p where p.user_id= "+id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> ids = query.getResultList();
        return ids;
    }

    @SuppressWarnings("Duplicates")
    public Integer getMyCompanyId(){
        Long userId=userDetailService.getUserId();
        if(userId!=null) {
            String stringQuery = "select u.company_id from users u where u.id= " + userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return  (Integer) query.getSingleResult();
        }else return null;
    }
    @SuppressWarnings("Duplicates")
    public Long getMyCompanyId_(){
        Long userId=userDetailService.getUserId();
        if(userId!=null) {
            String stringQuery = "select u.company_id from users u where u.id= " + userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return new Long ((Integer) query.getSingleResult());
        }else return null;
    }
    //возвращает id предприятия пользователя по его username
    @SuppressWarnings("Duplicates")
    public Long getUserCompanyId(String username){
        Long userId=userDetailService.getUserIdByUsername(username);
        if(userId!=null) {
            String stringQuery = "select u.company_id from users u where u.id= " + userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return new Long ((Integer) query.getSingleResult());
        }else return null;
    }
    @SuppressWarnings("Duplicates")
    public List<Integer> getMyDepartmentsIdWithTheirParents(){
        Long userId=userDetailService.getUserId();
        if(userId!=null) {
            String stringQuery = "select ud.department_id from user_department ud where ud.user_id="+ userId+
                    " UNION " +
                    "select d.parent_id from departments d where d.parent_id is not null and d.id in " +
                    "(select u.department_id from user_department u where u.user_id="+userId+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            return  query.getResultList();
        }else return null;
    }
//    @SuppressWarnings("Duplicates")
//    public List<Long> getMyDepartmentsIdWithTheirParents_Long(){
//        Long userId=userDetailService.getUserId();
//        if(userId!=null) {
//            String stringQuery = "select ud.department_id from user_department ud where ud.user_id="+ userId+
//                    " UNION " +
//                    "select d.parent_id from departments d where d.parent_id is not null and d.id in " +
//                    "(select u.department_id from user_department u where u.user_id="+userId+")";
//            Query query = entityManager.createNativeQuery(stringQuery);
//            return  (List<Long>) query.getResultList();
//        }else return null;
//    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public UsersJSON getUserValuesById(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "24,25")) // Пользователи: "Просмотр своего" "Просмотр всех" "Редактирование своего" "Редактирование всех"
        {
//            Long myId = userDetailService.getUserId();
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
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
                    "           (select name from companies where id=p.company_id) as company, " +
                    "           p.fio_family as fio_family, " +
                    "           p.fio_name as fio_name, " +
                    "           p.fio_otchestvo as fio_otchestvo, " +
                    "           p.username as username, " +
                    "           p.email as email, " +
                    "           p.sex as sex, " +
                    "           p.status_account as status_account, " +
                    "           p.time_zone_id  as time_zone_id, " +
                    "           coalesce(p.vatin,'')  as vatin, " +
                    "           to_char(p.date_birthday,'DD.MM.YYYY') as date_birthday, " +
                    "           p.additional as additional " +
                    "           from users p" +
                    " where p.id= " + id;
            stringQuery = stringQuery + " and p.master_id="+getMyMasterId();//принадлежит к предприятиям моего родителя
            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "25")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 24
                stringQuery = stringQuery + " and p.company_id=" + getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery, UsersJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                UsersJSON userJSON = (UsersJSON) query.getSingleResult();
                return userJSON;}
            catch(NoResultException nre){return null;}

        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public MyShortInfoJSON getMyShortInfo() {
        Long myId = userDetailService.getUserId();
        String stringQuery = "select " +
                "           p.username as username, " +
                "           coalesce(p.vatin,'') as vatin, " +
                "           p.fio_family as fio_family, " +
                "           p.fio_name as fio_name, " +
                "           p.fio_otchestvo as fio_otchestvo, " +
                "           p.name as name, " +
                "           p.email as email, " +
                "           coalesce(p.company_id,'0') as company_id, " +
                "           p.status_account as status_account, " +
                "           p.time_zone_id  as time_zone_id, " +
                "           p.sex as sex, " +
                "           to_char(p.date_birthday,'DD.MM.YYYY') as date_birthday " +
                "           from users p" +
                " where p.id= " + myId;
        Query query = entityManager.createNativeQuery(stringQuery);

        List<Object[]> queryList = query.getResultList();
        MyShortInfoJSON doc = new MyShortInfoJSON();
        for(Object[] obj:queryList){
            doc.setId(myId);
            doc.setUsername((String)                         obj[0]);
            doc.setVatin((String)                            obj[1]);
            doc.setFio_family((String)                       obj[2]);
            doc.setFio_name((String)                         obj[3]);
            doc.setFio_otchestvo((String)                    obj[4]);
            doc.setName((String)                             obj[5]);
            doc.setEmail((String)                            obj[6]);
            doc.setCompany_id(Long.parseLong(                obj[7].toString()));
            doc.setStatus_account((Integer)                  obj[8]);
            doc.setTime_zone_id((Integer)                    obj[9]);
            doc.setSex((String)                              obj[10]);
            doc.setDate_birthday((String)                    obj[11]);
        }
        return doc;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public int getUsersSize(String searchString, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "25,24"))// Пользователи: "Меню - таблица - все пользователи","Меню - таблица - только свой документ"
        {
            String stringQuery;
            Long documentOwnerId = getUserMasterIdByUsername(userDetailService.getUserName());
            int myCompanyId=getMyCompanyId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery="select p.id from users p where p.master_id="+documentOwnerId +
                    "           and  p.status_account < 4" +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;
            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "25")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 24
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            if(searchString!= null && !searchString.isEmpty()){
                stringQuery = stringQuery+" and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }
            if(companyId > 0){
                stringQuery = stringQuery+" and p.company_id="+companyId;
            }
//            stringQuery = stringQuery+" and status_account <4";
            Query query =  entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        }else return 0;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<UsersTableJSON> getUsersTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "25,24"))// Пользователи: "Меню - таблица - пользователи всех предприятий","Меню - таблица - пользователи только своего предприятия","Меню - таблица - только свой документ"
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long documentOwnerId = getUserMasterIdByUsername(userDetailService.getUserName());
            int myCompanyId=getMyCompanyId();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           p.name as name, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           (select name from users where id=p.master_id) as master, " +
                    "           (select name from users where id=p.creator_id) as creator, " +
                    "           (select name from users where id=p.changer_id) as changer, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.company_id,'0') as company_id, " +
                    "           (select name from companies where id=p.company_id) as company, " +
                    "           p.fio_family as fio_family, " +
                    "           p.fio_name as fio_name, " +
                    "           p.fio_otchestvo as fio_otchestvo, " +
                    "           p.username as username, " +
                    "           p.email as email, " +
                    "           p.sex as sex, " +
                    "           to_char(p.date_birthday,'DD.MM.YYYY') as date_birthday, " +
                    "           p.additional as additional, " +
                    "           p.status_account as status_account, " +
                    "           (select name from sprav_sys_status_account where id=p.status_account) as status_account_name " +
                    "           from users p" +
                    "           where p.master_id=" + documentOwnerId +
                    "           and  p.status_account < 4" +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "25")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 24
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery, UsersTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList();
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteUsers(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(5L, "23") && securityRepositoryJPA.isItAllMyMastersDocuments("users", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(5L, "23") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("users", delNumbers)))
        {
            Long myMasterId = getUserMasterIdByUsername(userDetailService.getUserName());
            Long myId = getMyId();
            String stringQuery = "update users p" +
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
                logger.error("Exception in method deleteUsers. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteUsers(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(5L,"23") && securityRepositoryJPA.isItAllMyMastersDocuments("users",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(5L,"23") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("users",delNumbers)))
        {
            //plan limit check
            Long masterId =  userRepositoryJPA.getMyMasterId();
            long amountToRepair = delNumbers.split(",").length;
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(masterId))) // if plan with limits - checking limits
                if((userRepositoryJPA.getMyConsumedResources().getUsers()+amountToRepair)>userRepositoryJPA.getMyMaxAllowedResources().getUsers())
                    return -120; // number of users is out of bounds of tariff plan
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = getMyId();
            String stringQuery;
            stringQuery = "Update users p" +
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
                logger.error("Exception in method undeleteUsers. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    public String getUserSuffix(Long userId){
        String stringQuery = "select l.suffix from sprav_sys_languages l where l.id=(select u.language_id from user_settings u where u.user_id= " + userId +")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((String) query.getSingleResult());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getUserSuffix. SQL query:" + stringQuery, e);
            return null;
        }
    }
    public String getMySuffix(){
        Long myId = userDetailService.getUserId();
        String stringQuery = "select l.suffix from sprav_sys_languages l where l.id=(select u.language_id from user_settings u where u.user_id= " + myId +")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((String) query.getSingleResult());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMySuffix. SQL query:" + stringQuery, e);
            return null;
        }
    }
    public String getMyDateFormat(){
        Long myId = userDetailService.getUserId();
        String stringQuery = "select l.date_format from sprav_sys_locales l where l.id=(select u.locale_id from user_settings u where u.user_id= " + myId +")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((String) query.getSingleResult());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMyDateFormat. SQL query:" + stringQuery, e);
            return null;
        }
    }
//    @SuppressWarnings("Duplicates")
//    public UserSettingsJSON getUserSettings(Long userId) {
//        String stringQuery;
//        stringQuery = "select " +
//                "   p.time_zone_id as time_zone_id, " +
//                "   p.language_id as language_id, " +
//                "   p.locale_id as locale_id, " +
//                "   sslc.code as locale, " +
//                "   sslg.suffix as suffix " +
//                "   from    user_settings p, " +
//                "           sprav_sys_languages sslg, " +
//                "           sprav_sys_locales sslc " +
//                "   where   p.user_id=" + userId +
//                "   and     p.language_id=sslg.id" +
//                "   and     p.locale_id=sslc.id";
//        try{
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Object[]> queryList = query.getResultList();
//            UserSettingsJSON doc = new UserSettingsJSON();
//            if(queryList.size()>0) {
//                doc.setTime_zone_id((Integer)   queryList.get(0)[0]);
//                doc.setLanguage_id((Integer)    queryList.get(0)[1]);
//                doc.setLocale_id((Integer)      queryList.get(0)[2]);
//                doc.setLocale((String)          queryList.get(0)[3]);
//                doc.setSuffix((String)          queryList.get(0)[4]);
//            }
//            return doc;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getUserSettings. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }

    @SuppressWarnings("Duplicates")
    public UserSettingsJSON getMySettings() {
        String stringQuery;
        Long myId = userDetailService.getUserId();
        stringQuery = "select " +
                "   p.time_zone_id as time_zone_id, " +
                "   p.language_id as language_id, " +
                "   p.locale_id as locale_id, " +
                "   sslc.code as locale, " +
                "   sslg.suffix as suffix, " +
                "   c.jr_country_id as country_id," +       // id of user's company country of jurisdiction
                "   ssc.organization," +                    // organization of country of jurisdiction(e.g. EU)
                "   cur.name_short," +                      // short name of Accounting currency of user's company (e.g. $ or EUR)
                "   sslc.date_format," +                    // date format of the user, like DD/MM/YYYY, YYYY-MM-DD e.t.c
                "   p.time_format as time_format, " +       // 12 or 24
                "   sst.canonical_id as time_zone " +       // time zone name, e.g. 'CET'
                "   from    user_settings p, " +
                "           sprav_sys_languages sslg, " +
                "           sprav_sys_locales sslc, " +
                "           sprav_sys_timezones sst, " +
                "           users u, " +
                "           companies c" +
                "           LEFT OUTER JOIN sprav_sys_countries ssc ON ssc.id=c.jr_country_id " +
                "           LEFT OUTER JOIN sprav_currencies cur ON cur.company_id=c.id " +
                "   where   p.user_id =" + myId +
                "   and     p.language_id = sslg.id" +
                "   and     p.locale_id = sslc.id" +
                "   and     p.user_id = u.id " +
                "   and     cur.is_default = true " +
                "   and     p.time_zone_id = sst.id " +
                "   and     u.company_id = c.id";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            UserSettingsJSON doc = new UserSettingsJSON();
            if(queryList.size()>0) {
                doc.setTime_zone_id((Integer)       queryList.get(0)[0]);
                doc.setLanguage_id((Integer)        queryList.get(0)[1]);
                doc.setLocale_id((Integer)          queryList.get(0)[2]);
                doc.setLocale((String)              queryList.get(0)[3]);
                doc.setSuffix((String)              queryList.get(0)[4]);
                doc.setCountry_id((Integer)         queryList.get(0)[5]);
                doc.setOrganization((String)        queryList.get(0)[6]);
                doc.setAccounting_currency((String) queryList.get(0)[7]);
                doc.setDateFormat((String)          queryList.get(0)[8]);
                doc.setTimeFormat((String)          queryList.get(0)[9]);
                doc.setTime_zone((String)           queryList.get(0)[10]);
            }
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMySettings. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean saveUserSettings(UserSettingsForm settings) {
        Long myId = getMyId();
        Long myMasterId = getMyMasterId();
        String stringQuery;
        stringQuery =
                "insert into user_settings (" +
                " user_id, " +
                " master_id, " +
                " time_zone_id," +
                " language_id," +
                " time_format," +
                " locale_id" +
                ") " +
                "values " +
                "(" +
                myId + ", " +
                myMasterId + ", " +
                settings.getTimeZoneId() + ", " +
                settings.getLanguageId() + ", " +
                settings.getTimeFormat() + ", " +
                settings.getLocaleId() +
                ")"+
                " ON CONFLICT ON CONSTRAINT user_uq " +
                " DO update set " +
                " time_zone_id="+ settings.getTimeZoneId() + ", " +
                " language_id="+ settings.getLanguageId() + ", " +
                " time_format="+ settings.getTimeFormat() + ", " +
                " locale_id="+ settings.getLocaleId();
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method saveUserSettings. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean setUserSettings(Long userId, int timeZoneId, int langId, int localeId, String timeFormat) {
        Long myMasterId = getUserMasterIdByUserId(userId);
        String stringQuery;
        stringQuery =
                "insert into user_settings (" +
                        " user_id, " +
                        " master_id, " +
                        " time_zone_id," +
                        " language_id," +
                        " locale_id," +
                        " time_format" +
                        ") " +
                        "values " +
                        "(" +
                        userId + ", " +
                        myMasterId + ", " +
                        timeZoneId + ", " +
                        langId + ", " +
                        localeId + ", " +
                        ":timeFormat" +
                        ")"+
                        " ON CONFLICT ON CONSTRAINT user_uq " +
                        " DO update set " +
                        " time_zone_id="+ timeZoneId + ", " +
                        " language_id="+ langId + ", " +
                        " time_format = :timeFormat, " +
                        " locale_id="+ localeId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("timeFormat", timeFormat);
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method setUserSettings. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public Integer activateUser(String code) {
        try{
            User user = userRepository.findByActivationCode(code);
            if(user==null){return -102;}
            user.setActivationCode(null);
            user.setStatus_account(2); // 2 = active account
            userRepository.save(user);
            return 1;
        }catch (Exception e) {
            logger.error("Exception in method activateUser. code = " + code, e);
            e.printStackTrace();
            return null;
        }
    }

    public int getLangIdBySuffix(String suffix){
        String stringQuery = "select l.id from sprav_sys_languages l where l.suffix=:suffix";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("suffix",suffix);
            return ((Integer) query.getSingleResult());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getLangIdBySuffix. SQL query:" + stringQuery, e);
            return 1;
        }
    }

    // Counting consumed user resources
    @SuppressWarnings("Duplicates")
    public UserResources getMyConsumedResources(){
        String BASE_FILES_FOLDER;
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        try{
            if(storageService.isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде (dev mode)
            } else {                    BASE_FILES_FOLDER = "//usr//dokio//files//";} //запущено в linux (prod mode)
            String MY_MASTER_ID_FOLDER = myMasterId.toString();
            File folder = new File(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER);
            long size = 0L;
            if(storageService.isPathExists(folder.getPath()))
                size = storageService.getDirectorySize(folder);
            String stringQuery =
                "   select" +
                "   (select count(*) from companies     where master_id="+myMasterId+" and coalesce(is_deleted,false)=false) as companies," +
                "   (select count(*) from departments   where master_id="+myMasterId+" and coalesce(is_deleted,false)=false) as departments," +
                "   (select count(*) from users         where master_id="+myMasterId+" and coalesce(is_deleted,false)=false) as users," +
                "   (select count(*) from products      where master_id="+myMasterId+" and coalesce(is_deleted,false)=false) as products," +
                "   (select count(*) from cagents       where master_id="+myMasterId+" and coalesce(is_deleted,false)=false) as counterparties," +
                "   (select count(*) from stores        where master_id="+myMasterId+" and coalesce(is_deleted,false)=true ) as stores";
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            UserResources doc = new UserResources();
            doc.setCompanies(Long.parseLong(                queryList.get(0)[0].toString()));
            doc.setDepartments(Long.parseLong(              queryList.get(0)[1].toString()));
            doc.setUsers(Long.parseLong(                    queryList.get(0)[2].toString()));
            doc.setProducts(Long.parseLong(                 queryList.get(0)[3].toString()));
            doc.setCounterparties(Long.parseLong(           queryList.get(0)[4].toString()));
            doc.setMegabytes(Math.round(size/1024/1024));
            doc.setStores(Long.parseLong(                   queryList.get(0)[5].toString()));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getConsumedUserResources", e);
            return null;
        }
    }

    // Counting maximal allowed user resources
    @SuppressWarnings("Duplicates")
    public UserResources getMyMaxAllowedResources(){
        Long myMasterId=userRepositoryJPA.getMyMasterId();
        try{
            int plan_id = getMasterUserPlan(myMasterId);
            String stringQuery =
                    "select   sum(companies) as companies, " +
                            " sum(departments) as departments, " +
                            " sum(users) as users, " +
                            " sum(products) as products, " +
                            " sum(counterparties) as counterparties, " +
                            " sum(megabytes) as megabytes, " +
                            " sum(stores) as stores " +
                            " from (" +
                            "(select n_companies as companies, " +
                            " n_departments as departments, " +
                            " n_users as users, " +
                            " n_products as products, " +
                            " n_counterparties as counterparties, " +
                            " n_megabytes as megabytes, " +
                            " n_stores as stores " +
                            " from plans " +
                            " where id = "+plan_id+")" +
                            " UNION " +
                            " (select " +
                            " n_companies as companies, " +
                            " n_departments as departments, n_users as users, " +
                            " n_products as products, " +
                            " n_counterparties as counterparties, " +
                            " n_megabytes as megabytes, " +
                            " n_stores as stores " +
                            " from plans_add_options " +
                            " where user_id="+myMasterId+")" +
                            ") AS result ";
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            UserResources doc = new UserResources();
            doc.setCompanies(Long.parseLong(                queryList.get(0)[0].toString()));
            doc.setDepartments(Long.parseLong(              queryList.get(0)[1].toString()));
            doc.setUsers(Long.parseLong(                    queryList.get(0)[2].toString()));
            doc.setProducts(Long.parseLong(                 queryList.get(0)[3].toString()));
            doc.setCounterparties(Long.parseLong(           queryList.get(0)[4].toString()));
            doc.setMegabytes(              Integer.parseInt(queryList.get(0)[5].toString()));
            doc.setStores(        Long.parseLong(           queryList.get(0)[6].toString()));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMaxAllowedUserResources", e);
            return null;
        }
    }

    public int getMasterUserPlan(Long userId){
            String stringQuery= "select plan_id from users u where u.id = "+userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return (Integer) query.getSingleResult();
    }
    public boolean isPlanNoLimits(int planId){
        String stringQuery = "select * from plans u where u.id = "+planId+" and is_nolimits = true";
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }


}
