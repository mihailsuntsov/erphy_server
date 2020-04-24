package com.laniakea.repository;

import com.laniakea.message.request.UserGroupForm;
import com.laniakea.message.response.UserGroupJSON;
import com.laniakea.message.response.UserGroupTableJSON;
import com.laniakea.message.response.UserGroupListJSON;
import com.laniakea.model.Permissions;
import com.laniakea.model.User;
import com.laniakea.model.UserGroup;
import com.laniakea.security.services.UserDetailsServiceImpl;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("UserGroupRepositoryJPA")
public class UserGroupRepositoryJPA {
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

    public UserGroupRepositoryJPA() {
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getUserGroupSize(String searchString, int companyId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "from UserGroup p where p.master=" + myMasterId;
        stringQuery = stringQuery + " and coalesce(p.is_archive,false) !=true";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
        }

        if (companyId > 0) {
            stringQuery = stringQuery + " and company=" + companyId;
        }

        Query query = entityManager.createQuery(stringQuery, UserGroup.class);
        return query.getResultList().size();
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public List<UserGroupTableJSON> getUserGroupTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(6L, "29,30"))// Группы пользователей: "Меню - все Группы пользователей","Меню - только свого предприятия"
        {
            Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
            String stringQuery = "select            " +
                    "p.id as id,            " +
                    "p.name as name,            " +
                    "p.master_id as master_id,            " +
                    "p.creator_id as creator_id,            " +
                    "p.changer_id as changer_id,            " +
                    "(select name from users where id=p.master_id) as master,            " +
                    "(select name from users where id=p.creator_id) as creator,            " +
                    "(select name from users where id=p.changer_id) as changer,            " +
                    "to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI')as date_time_created,            " +
                    "to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI')as date_time_changed,            " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "coalesce(p.company_id,'0') as company_id,            " +
                    "(select name from companies where id=p.company_id) as company,            " +
                    "p.description as description            " +
                    "from usergroup p           " +
                    "where            " +
                    " p.master_id=" + myMasterId+
                    " and coalesce(p.is_archive,false) !=true";

            if(!securityRepositoryJPA.userHasPermissions_OR(6L, "29"))// Группы пользователей: "Меню - все Группы пользователей"
            {stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();}
            if(!securityRepositoryJPA.userHasPermissions_OR(6L, "30"))// Группы пользователей: "Меню - только свого предприятия"
            {stringQuery = stringQuery + " and p.company_id!=" + userRepositoryJPA.getMyCompanyId();}//хоть это и алогично, но кто-то ж захочет

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }

            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }


            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = this.entityManager.createNativeQuery(stringQuery, UserGroupTableJSON.class).setFirstResult(offsetreal).setMaxResults(result);
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
        if (securityRepositoryJPA.userHasPermissions_OR(6L, "33,34"))//Группы пользователей: "Редактирование только документов своего предприятия","Редактирование документов всех предприятий"
        {
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

                        "           p.description as description " +
                        "           from usergroup p" +
                        " where p.id= " + id;
            stringQuery = stringQuery + " and p.master_id="+userRepositoryJPA.getMyMasterId();//принадлежит к документам моего родителя

            if (!securityRepositoryJPA.userHasPermissions_OR(6L, "34"))//Группы пользователей: "Редактирование документов всех предприятий"
            stringQuery = stringQuery + " and coalesce(p.company_id,'0')="+userRepositoryJPA.getMyCompanyId();
            if (!securityRepositoryJPA.userHasPermissions_OR(6L, "33"))//Группы пользователей: "Редактирование только документов своего предприятия"
            stringQuery = stringQuery + " and coalesce(p.company_id,'0')!="+userRepositoryJPA.getMyCompanyId();

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
        } else return null;
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

       // Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery="select " +
                "           p.id as id, " +
                "           p.description as description, " +
                "           p.name as name " +
                "           from usergroup p " +
                "           where coalesce(p.is_archive,false) !=true and p.company_id="+company_id;


        stringQuery = stringQuery+" order by p.name asc";

        Query query =  entityManager.createNativeQuery(stringQuery, UserGroupListJSON.class);
        return query.getResultList();
    }

    @SuppressWarnings("Duplicates")
    public boolean updateUserGroup(UserGroupForm request) {
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(6L, "33"); // Группы пользователей:"Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(6L, "34"); // Группы пользователей:"Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean myCompanyId_Equal_CompanyIdInRequest=(userRepositoryJPA.getMyCompanyId()==Integer.parseInt(request.getCompany_id()));//сохраняется документ моего предприятия
        boolean isItMyMastersUserGroup=securityRepositoryJPA.isItMyMastersUserGroup(Long.valueOf(request.getId()));

        if(((myCompanyId_Equal_CompanyIdInRequest && userHasPermissions_OwnUpdate)//(если я сохраняю документ своего предприятия и у меня есть на это права
                ||(!myCompanyId_Equal_CompanyIdInRequest && userHasPermissions_AllUpdate))//или если пользователь сохраняет чужой аккаунт и у него есть на это права)
                && isItMyMastersUserGroup) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            EntityManager emgr = emf.createEntityManager();

            emgr.getTransaction().begin();

            Long id=Long.valueOf(request.getId());

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
    public boolean deleteUserGroupsById(String delNumbers) {
        if(securityRepositoryJPA.userHasPermissions_OR(6L,"32")&& //Группы пользователей : "Удаление"
           securityRepositoryJPA.isItAllMyMastersUserGroups(delNumbers))  //все ли Группы пользователей принадлежат текущему родительскому аккаунту
        {
            String stringQuery;
            stringQuery="Update usergroup p" +
                    " set is_archive=true "+
                    " where p.id in ("+ delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if(!stringQuery.isEmpty() && stringQuery.trim().length() > 0){
                int count = query.executeUpdate();
                return true;
            }else return false;
        }else return false;
    }
}
