/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.repository;

import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.UsersJSON;
import com.dokio.message.response.UsersListJSON;
import com.dokio.message.response.UsersTableJSON;
import com.dokio.message.response.additional.MyShortInfoJSON;
import com.dokio.model.Companies;
import com.dokio.model.Departments;
import com.dokio.model.User;
import com.dokio.model.UserGroup;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.persistence.*;
import com.dokio.repository.UserRepository;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Repository("UserRepositoryJPA")
public class UserRepositoryJPA {

    Logger logger = Logger.getLogger("UserRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;

    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private UserDetailsServiceImpl userDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean putUserToCompany(Long userId, Long companyId){
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User usr = em.find(User.class, userId);
        Companies company = em.find(Companies.class, companyId);
        usr.setCompany(company);
        em.getTransaction().commit();
        em.close();
        return true;
    }

    public User getUserById(Long userId){
        EntityManager em = emf.createEntityManager();
        User usr = em.find(User.class, userId);
        return usr;
    }
    // меняет пароль пользователя
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    //сравнивает пароль oldPassword с паролем пользователя
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
    //@Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateUser(SignUpForm request) {
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

            user.setTime_zone_id(request.getTime_zone_id());

            user.setVatin(request.getVatin());

            em.getTransaction().commit();
            em.close();
            return true;
        }else return false;
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
    @SuppressWarnings("Duplicates")
    public List<Long> getMyDepartmentsIdWithTheirParents_Long(){
        Long userId=userDetailService.getUserId();
        if(userId!=null) {
            String stringQuery = "select ud.department_id from user_department ud where ud.user_id="+ userId+
                    " UNION " +
                    "select d.parent_id from departments d where d.parent_id is not null and d.id in " +
                    "(select u.department_id from user_department u where u.user_id="+userId+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            return  (List<Long>) query.getResultList();
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public UsersJSON getUserValuesById(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "24,25,26,27")) // Пользователи: "Просмотр своего" "Просмотр всех" "Редактирование своего" "Редактирование всех"
        {
            Long myId = userDetailService.getUserId();
            String stringQuery = "select p.id as id, " +
                    "           p.name as name, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           (select name from users where id=p.master_id) as master, " +
                    "           (select name from users where id=p.creator_id) as creator, " +
                    "           (select name from users where id=p.changer_id) as changer, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
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
            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "25,27")) {//если нет прав на Предприятия: "Просмотр всех", "Редактирование всех"
                if(myId != Long.valueOf(id)){//значит остаются на "Просмотр своего", "Редактирование своего", НО если запрашиваем id не своего документа:
                    return null;
                }
            }

            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "24,26")) {//если нет прав на Предприятия: "Просмотр своего", "Редактирование своего"
                if(myId == Long.valueOf(id)){//значит остаются на "Просмотр всех", "Редактирование всех", НО если запрашиваем id  своего документа:
                    return null;
                }
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
    public int getUsersSize(String searchString, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "20,21"))// Пользователи: "Меню - таблица - все пользователи","Меню - таблица - только свой документ"
        {
            String stringQuery;
            Long documentOwnerId = getUserMasterIdByUsername(userDetailService.getUserName());
            stringQuery="from User p where p.master="+documentOwnerId;
            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "20")) {//если нет прав на "Меню - все пользователи"
                stringQuery = stringQuery + " and p.id=" + userDetailService.getUserId();
            }
            if(searchString!= null && !searchString.isEmpty()){
                stringQuery = stringQuery+" and upper(p.name) like upper('%"+searchString+"%')";
            }
            if(companyId > 0){
                stringQuery = stringQuery+" and company="+companyId;
            }
            stringQuery = stringQuery+" and status_account <4";
            Query query =  entityManager.createQuery(stringQuery,User.class);
            return query.getResultList().size();
        }else return 0;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<UsersTableJSON> getUsersTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "20,69,21"))// Пользователи: "Меню - таблица - пользователи всех предприятий","Меню - таблица - пользователи только своего предприятия","Меню - таблица - только свой документ"
        {
            String stringQuery;
            Long documentOwnerId = getUserMasterIdByUsername(userDetailService.getUserName());
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           p.name as name, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           (select name from users where id=p.master_id) as master, " +
                    "           (select name from users where id=p.creator_id) as creator, " +
                    "           (select name from users where id=p.changer_id) as changer, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI')as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI')as date_time_changed, " +
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
                    "           where  p.status_account <4" +
                    "           and p.master_id=" + documentOwnerId;

            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "20")) {//если нет прав на "Меню - таблица - пользователи всех предприятий"
                //остаются Своего предприятия и Только свои
                if (!securityRepositoryJPA.userHasPermissions_OR(5L, "69")) {//если нет прав на "Меню - таблица - пользователи только своего предприятия"
                    //остаются Только свои
                    stringQuery = stringQuery + " and p.id=" + userDetailService.getUserId();
                }else //иначе остаются Своего предприятия и Только свои
                    stringQuery = stringQuery + " and (p.id=" + userDetailService.getUserId()+" or p.id in(select id from users where company_id="+ getMyCompanyId()+"))";
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, UsersTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);
            return query.getResultList();
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteUsersById(String delNumbers) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L,"23")&& //Пользователи : "Удаление"
                securityRepositoryJPA.isItAllMyMastersUsers(delNumbers))  //все ли пользователи принадлежат текущему хозяину
        {
            String stringQuery;
            stringQuery="Update users p" +
                    " set status_account=4 "+
                    " where p.id in ("+ delNumbers+") ";
            Query query = entityManager.createNativeQuery(stringQuery);
            if(!stringQuery.isEmpty() && stringQuery.trim().length() > 0){
                int count = query.executeUpdate();
                return true;
            }else return false;
        } else return false;
    }
}
