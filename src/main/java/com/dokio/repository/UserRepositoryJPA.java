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
import com.dokio.message.request.additional.LegalMasterUserInfoForm;
//import com.dokio.message.request.additional.UserProductDeppartsForm;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.UserJSON_;
import com.dokio.message.response.UsersJSON;
import com.dokio.message.response.UsersListJSON;
import com.dokio.message.response.UsersTableJSON;
import com.dokio.message.response.additional.*;
import com.dokio.message.response.additional.eployeescdl.EmployeeScedule;
import com.dokio.model.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
    private StorageService storageService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SubscriptionRepositoryJPA subscriptionRepository;
    @Autowired
    private CommonUtilites commonUtilites;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    DocumentsRepositoryJPA documentsRepository;
    @Autowired
    SpravStatusDocRepository ssd;



    @Value("${files_path}")
    private String files_path;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    //сравнивает пароль oldPassword с паролем пользователя
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class,Exception.class})
    public Long addUser(SignUpForm signUpRequest){
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "22"))// Пользователи:"Создание"
        {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return (-10L); //login like this is already exists
            }
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return (-11L); //e-mail like this is already exists
            }
            Long masterId =  getMyMasterId();

            //plan limit check
            if(!isPlanNoLimits(getMasterUserPlan(masterId))) // if plan with limits - checking limits
                if(getMyConsumedResources().getUsers()>=getMyMaxAllowedResources().getUsers())
                    return (-120L); // number of users is out of bounds of tariff plan

            try{
                // Если такого логина и емайла нет
                // Создание аккаунта для нового пользователя
                User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
                Long companyId = Long.valueOf(signUpRequest.getCompany_id());
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                roles.add(userRole);
                user.setRoles(roles);//добавили юзеру роль ROLE_USER
                user.setCompany(companyRepositoryJPA.getCompanyById(companyId));//предприятие
                Set<Long> departments = signUpRequest.getSelectedUserDepartments();
                Set<Departments> setDepartmentsOfUser = departmentRepositoryJPA.getDepartmentsSetBySetOfDepartmentsId(departments);
                user.setDepartments(setDepartmentsOfUser);//сет отделений предприятия
                Set<Long> userGroups = signUpRequest.getUserGroupList();
                Set<UserGroup> setUserGroupsOfUser = userGroupRepositoryJPA.getUserGroupSetBySetOfUserGroupId(userGroups);
                user.setUsergroup(setUserGroupsOfUser);//сет групп пользователей
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                String dateBirth = (signUpRequest.getDate_birthday() == null ? "" : signUpRequest.getDate_birthday());
                try {
                    user.setDate_birthday(dateBirth.isEmpty() ? null : dateFormat.parse(dateBirth));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                User creator = userDetailService.getUserByUsername(userDetailService.getUserName());
                user.setCreator(creator);//создателя
                User master = userDetailService.getUserById(masterId);
                user.setMaster(master);//владельца
                user.setDate_time_created(new Timestamp(System.currentTimeMillis()));//дату создания
                user.setFio_family(signUpRequest.getFio_family());
                user.setFio_name(signUpRequest.getFio_name());
                user.setFio_otchestvo(signUpRequest.getFio_otchestvo());
                user.setName(signUpRequest.getName());
                user.setStatus_account(Integer.parseInt(signUpRequest.getStatus_account()));
                user.setSex(signUpRequest.getSex());
                user.setAdditional(signUpRequest.getAdditional());
                user.setIs_employee(signUpRequest.isIs_employee());
                user.setIs_currently_employed(signUpRequest.isIs_currently_employed());
                user.setJob_title_id(signUpRequest.getJob_title_id());
                user.setCounterparty_id(signUpRequest.getCounterparty_id());
                user.setIncoming_service_id(signUpRequest.getIncoming_service_id());

//                user.setTime_zone_id(signUpRequest.getTimeZoneId());
                Long createdUserId = userRepository.save(user).getId();//и сохранили его
                // create settings (language, locale, time zone)
                setUserSettings(createdUserId, signUpRequest.getTimeZoneId(), signUpRequest.getLanguageId(), signUpRequest.getLocaleId(), "24");
                // create print menus for user
                List<BaseFiles> baseFilesList = fileRepository.getFilesIdsByName(fileRepository.assemblyBaseFilesList(masterId), masterId, companyId, null);
                if(baseFilesList.size()>0) documentsRepository.createPrintMenus(baseFilesList,masterId, createdUserId, companyId);
                //create settings
                ssd.insertSettingsFast(masterId,createdUserId,companyId);
                // ответ сервера при удачном создании юзера
//                ResponseEntity<String> responseEntity = new ResponseEntity<>(String.valueOf(createdUserId), HttpStatus.OK);
//                return responseEntity;

                // if user is employee - add services that it can sell
//                for (UserProductDeppartsForm product : signUpRequest.getUserProductsDepparts()) {
                for (IdAndNameJSON product : signUpRequest.getUserProducts()) {

//                    Set<Long>existingDepparts = new HashSet<>();
//                    for (Long dep_part_id : product.getDep_parts_ids()) {
//                        saveUserProductDeppart(masterId, product.getProduct_id(), createdUserId, dep_part_id);
                    saveUserProducts(masterId, product.getId(), createdUserId);
//                        existingDepparts.add(dep_part_id);
//                    }
//                    deleteDeppartsThatNoMoreContainInThisProduct(existingDepparts, masterId, product.getProduct_id(), (long) signUpRequest.getId());
                }
                return createdUserId;
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("Exception in method addUse. Object:"+signUpRequest.toString(), e);
                e.printStackTrace();
                return null;
            }
        } else {
            return -1L;
        }
    }

    @Transactional
    public Integer updateUser(SignUpForm request) {
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(5L, "26"); // Пользователи:"Редактирование своего"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(5L, "27"); // Пользователи:"Редактирование всех"
        boolean requestUserIdEqualMyUserId=(userDetailService.getUserId()==Long.valueOf(request.getId()));

        if(((requestUserIdEqualMyUserId && userHasPermissions_OwnUpdate)//(если пользователь сохраняет свой аккаунт и у него есть на это права
                ||(!requestUserIdEqualMyUserId && userHasPermissions_AllUpdate))//или если пользователь сохраняет чужой аккаунт и у него есть на это права)
                && securityRepositoryJPA.isItMyMastersUser(Long.valueOf(request.getId()))) //и сохраняемый аккаунт под юрисдикцией главного аккаунта
        {
            try{
                EntityManager em = emf.createEntityManager();
                Long masterId =   getMyMasterId();
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

                user.setIs_employee(request.isIs_employee());
                user.setIs_currently_employed(request.isIs_currently_employed());
                user.setJob_title_id(request.getJob_title_id());
                user.setCounterparty_id(request.getCounterparty_id());
                user.setIncoming_service_id(request.getIncoming_service_id());

                em.getTransaction().commit();
                em.close();

                Set<Long>existingUserServices = new HashSet<>();
//                for (UserProductDeppartsForm product : request.getUserProductsDepparts()) {
                for (IdAndNameJSON product : request.getUserProducts()) {
//                    Set<Long>existingDepparts = new HashSet<>();
//                    for (Long dep_part_id : product.getDep_parts_ids()) {
//                        saveUserProductDeppart(masterId, product.getProduct_id(), (long) request.getId(), dep_part_id);
                        saveUserProducts(masterId, product.getId(), (long) request.getId());
//                        existingDepparts.add(dep_part_id);
//                    }
//                    deleteDeppartsThatNoMoreContainInThisProduct(existingDepparts, masterId, product.getProduct_id(), (long) request.getId());
                    existingUserServices.add(product.getId());
                }
                deleteUserServicesNoMoreContainedInUserCard(existingUserServices, masterId, (long) request.getId());
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method updateUser. request:" + request.toString(), e);
                return null;
            }
        }else return -1;
    }

//    private void saveUserProductDeppart(Long master_id, Long product_id, Long user_id, Long dep_part_id) throws Exception {
//        String stringQuery = "insert into scdl_user_product_dep_parts (" +
//                "   master_id," +
//                "   user_id," +
//                "   product_id," +
//                "   dep_part_id" +
//                "   ) values (" +
//                master_id+", "+
//                user_id+", "+
//                product_id+", "+
//                dep_part_id+
//                ") ON CONFLICT ON CONSTRAINT scdl_user_product_dep_parts_uq " +// "upsert"
//                "  DO NOTHING ";
//        try{
//            Query query = entityManager.createNativeQuery(stringQuery);
//            query.executeUpdate();
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method saveUserProductDeppart. SQL query:" + stringQuery, e);
//            throw new Exception(e);
//        }
//    }

    private void saveUserProducts(Long master_id, Long product_id, Long user_id) throws Exception {
        String stringQuery = "insert into scdl_user_products (" +
                "   master_id," +
                "   user_id," +
                "   product_id" +
                "   ) values (" +
                master_id+", "+
                user_id+", "+
                product_id +
                ") ON CONFLICT ON CONSTRAINT scdl_user_products_uq " +// "upsert"
                "  DO NOTHING ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method saveUserProducts. SQL query:" + stringQuery, e);
            throw new Exception(e);
        }
    }



    // Deleting user services that user no more has
    private void deleteUserServicesNoMoreContainedInUserCard(Set<Long> existingUserServices, Long master_id, Long user_id) throws Exception  {
        String stringQuery =
//                " delete from scdl_user_product_dep_parts " +
                        " delete from scdl_user_products " +

                        " where " +
                        " master_id = " + master_id + " and " +
                        " user_id =   " + user_id;
        if(existingUserServices.size()>0)
            stringQuery = stringQuery + " and product_id not in " + commonUtilites.SetOfLongToString(existingUserServices,",","(",")");
        try {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method deleteUserServicesNoMoreContainedInUserCard. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    //Находит родительский аккаунт у текущего пользователя (masterId)
    //Родительский - аккаунт, который создал текущего пользователя.
    //masterId один для всех предприятий аккаунта
    //Если аккаунт создавался через регистрацию, он сам является masterId
    //Все аккаунты созданные из пользовательского интерфейса системы, также являются дочерними к masterId
    public Long getUserMasterIdByUsername(String username) {
        try{
            Long userId = userDetailService.getUserIdByUsername(userDetailService.getUserName());
            String stringQuery;
            stringQuery="select u.master_id from users u where u.id = "+userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return  Long.valueOf((Integer) query.getSingleResult());
        }catch (Exception e) {
            logger.error("Exception in method getUserMasterIdByUsername. username ="+userDetailService.getUserName(), e);
            e.printStackTrace();
            return null;
        }
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

    public List<String> getUserDepartmentsNames(long id) {
        String stringQuery="select dep.name||' '||dep.address as name " +
                "from departments dep where dep.id in (select p.department_id from user_department p where p.user_id= "+id+")"+
                " and coalesce(dep.is_deleted,false)!=true";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<String> depNames = query.getResultList();
        return depNames;
    }

    @Transactional
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

    public List<Integer> getUserDepartmentsId(long id) {
        String stringQuery="" +
                "select p.department_id as did" +
                " from " +
                " user_department p," +
                " departments dpts" +
                " where " +
                " p.user_id= "+id+
                " and p.department_id=dpts.id " +
                " and coalesce(dpts.is_deleted,false)!=true";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Integer> depIds = query.getResultList();
            return depIds;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getUserDepartmentsId. SQL query:" + stringQuery, e);
            return null;
        }
    }

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
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Integer> depIds = query.getResultList();
            return depIds;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMyDepartmentsId. SQL query:" + stringQuery, e);
            return null;
        }
    }
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

    public List<Integer> getUserGroupsId(long id) {
        String stringQuery="select p.usergroup_id as did from user_usergroup p where p.user_id= "+id;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Integer> ids = query.getResultList();
            return ids;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getUserGroupsId. SQL query:" + stringQuery, e);
            return null;
        }
    }

    // returns list of services that employee can provide
//    private List<IdAndNameJSON> getUserProductsDepparts(long userId, long masterId){
    private List<IdAndNameJSON> getUserProducts(long userId, long masterId){
        String stringQuery="select " +
                "   p.product_id as product_id, " +
                "   pr.name as product_name " +
//                "   from scdl_user_product_dep_parts p " +
                "   from scdl_user_products p " +
                "   inner join products pr on pr.id=p.product_id " +
                "   where " +
                "   p.master_id = " + masterId +
                "   and p.user_id= " + userId +
                "   group by p.product_id, p.user_id, pr.name " +
                "   order by pr.name";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<IdAndNameJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                IdAndNameJSON doc = new IdAndNameJSON();

                doc.setId(Long.parseLong(                      obj[0].toString()));
                doc.setName((String)                           obj[1]);

                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getUserProductsDepparts. SQL query:" + stringQuery, e);
            return null;
        }
    }
//    private List<UserProductDeppartsJSON> getUserProductsDepparts(long userId, long masterId){
//
//        String stringQuery="select " +
//                "   p.product_id as product_id, " +
//                "   pr.name as product_name " +
//                "   from scdl_user_product_dep_parts p " +
//                "   inner join products pr on pr.id=p.product_id " +
//                "   where " +
//                "   p.master_id = " + masterId +
//                "   and p.user_id= " + userId +
//                "   group by p.product_id, p.user_id, pr.name " +
//                "   order by pr.name";
//
//        try{
//            Query query = entityManager.createNativeQuery(stringQuery);
//            List<Object[]> queryList = query.getResultList();
//            List<UserProductDeppartsJSON> returnList = new ArrayList<>();
//            for (Object[] obj : queryList) {
//                UserProductDeppartsJSON doc = new UserProductDeppartsJSON();
//
//                doc.setProduct_id(Long.parseLong(                      obj[0].toString()));
//                doc.setProduct_name((String)                           obj[1]);
//                doc.setDep_parts_ids(getProductDeppartsIds(userId, doc.getProduct_id(), masterId));
//
//                returnList.add(doc);
//            }
//            return returnList;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getUserProductsDepparts. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }

    private List<Long> getProductDeppartsIds(long userId, long productId, long masterId){
        String stringQuery="select " +
                "   p.dep_part_id as dep_part_id " +
                "   from scdl_user_product_dep_parts p " +
                "   where " +
                "   p.master_id = " + masterId +
                "   and p.user_id= " + userId +
                "   and p.product_id= " + productId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Long> depIds = new ArrayList<>();
            for(Object i: query.getResultList()){
                depIds.add(new Long(i.toString()));
            }
            return depIds;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProductDeppartsIds. SQL query:" + stringQuery, e);
            return null;
        }

    }

    public Integer getMyCompanyId(){
        Long userId=userDetailService.getUserId();
        if(userId!=null) {
            String stringQuery = "select u.company_id from users u where u.id= " + userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return  (Integer) query.getSingleResult();
        }else return null;
    }
    public Long getMyCompanyId_(){
        Long userId=userDetailService.getUserId();
        if(userId!=null) {
            String stringQuery = "select u.company_id from users u where u.id= " + userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return new Long ((Integer) query.getSingleResult());
        }else return null;
    }
    //возвращает id предприятия пользователя по его username
    public Long getUserCompanyId(String username){
        Long userId=userDetailService.getUserIdByUsername(username);
        if(userId!=null) {
            String stringQuery = "select u.company_id from users u where u.id= " + userId;
            Query query = entityManager.createNativeQuery(stringQuery);
            return new Long ((Integer) query.getSingleResult());
        }else return null;
    }
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

    @Transactional
    public UserJSON_ getUserValuesById(long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "24,25")) // Пользователи: "Просмотр своего" "Просмотр всех" "Редактирование своего" "Редактирование всех"
        {
            UserSettingsJSON userSettings = getMySettings();
            Long myMasterId =   getMyMasterId();
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
                    "           coalesce(p.company_id, 0) as company_id, " +
                    "           (select name from companies where id=p.company_id) as company, " +
                    "           p.fio_family as fio_family, " +
                    "           p.fio_name as fio_name, " +
                    "           p.fio_otchestvo as fio_otchestvo, " +
                    "           p.username as username, " +
                    "           p.email as email, " +
                    "           p.sex as sex, " +
                    "           p.status_account as status_account, " +
                    "           coalesce(p.time_zone_id, 24) as time_zone_id, " +  // CET if null
                    "           coalesce(p.vatin,'')  as vatin, " +
                    "           to_char(p.date_birthday,'DD.MM.YYYY') as date_birthday, " +
                    "           p.additional as additional, " +
                    "           coalesce(p.is_employee, false) as is_employee, " +
                    "           coalesce(p.is_currently_employed, false) as is_currently_employed, " +
                    "           p.job_title_id as job_title_id, " +
                    "           p.counterparty_id as counterparty_id, " +
                    "           p.incoming_service_id as incoming_service_id, " +
                    "           coalesce(sjt.name,'') as jobtitle_name, " +
                    "           cg.name as counterparty_name, " +
                    "           prd.name as service_name " +
                    "           from users p" +

                    "           left outer join sprav_jobtitles sjt on sjt.id = p.job_title_id " +
                    "           left outer join cagents cg on cg.id = p.counterparty_id " +
                    "           left outer join products prd on prd.id = p.incoming_service_id " +

                    "           where p.master_id="+myMasterId+" and p.id= " + id;
            stringQuery = stringQuery + " and p.master_id="+getMyMasterId();//принадлежит к предприятиям моего родителя
            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "25")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 24
                stringQuery = stringQuery + " and p.company_id=" + getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
//            Query query = entityManager.createNativeQuery(stringQuery, UsersJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query


//                UsersJSON userJSON = (UsersJSON) query.getSingleResult();
//                return userJSON;
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                UserJSON_ doc = new UserJSON_();
                for(Object[] obj:queryList){

                    doc.setId(id);
                    doc.setName((String)                        obj[1]);
                    doc.setFio_family((String)                  obj[12]);
                    doc.setFio_name((String)                    obj[13]);
                    doc.setFio_otchestvo((String)               obj[14]);
                    doc.setUsername((String)                    obj[15]);
                    doc.setEmail((String)                       obj[16]);
                    doc.setCompany((String)                     obj[11]);
                    doc.setCompany_id(Long.parseLong(           obj[10].toString()));
                    doc.setCreator((String)                     obj[6]);
                    doc.setChanger((String)                     obj[7]);
                    doc.setDate_time_created((String)           obj[8]);
                    doc.setDate_time_changed((String)           obj[9]);
                    doc.setSex((String)                         obj[17]);
                    doc.setStatus_account((Integer)             obj[18]);
                    doc.setDate_birthday((String)               obj[21]);
                    doc.setAdditional((String)                  obj[22]);
                    doc.setTime_zone_id(Long.parseLong(         obj[19].toString()));
                    doc.setVatin((String)                       obj[20]);
                    doc.setIs_employee((Boolean)                obj[23]);
                    doc.setIs_currently_employed((Boolean)      obj[24]);
                    doc.setJob_title_id(obj[25] != null ? Long.parseLong(obj[25].toString()) : null);
                    doc.setCounterparty_id(obj[26] != null ? Long.parseLong(obj[26].toString()) : null);
                    doc.setIncoming_service_id(obj[27] != null ? Long.parseLong(obj[27].toString()) : null);
                    doc.setJob_title_name((String)              obj[28]);
                    doc.setCounterparty_name((String)           obj[29]);
                    doc.setIncoming_service_name((String)       obj[30]);

                    doc.setUserDepartmentsNames(getUserDepartmentsNames(id));
                    doc.setUserDepartmentsId(getUserDepartmentsId(id));
                    doc.setUserGroupsId(getUserGroupsId(id));
                    doc.setUserProducts(getUserProducts(id, myMasterId));

                }
                return doc;
            }
            catch(NoResultException nre){return null;}
            catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getUserValuesById. SQL query:" + stringQuery, e);
                return null;
            }
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
            UserSettingsJSON userSettings = getMySettings();
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
            Long myMasterId = getMyMasterId();
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
            Long masterId =  getMyMasterId();
            long amountToRepair = delNumbers.split(",").length;
            if(!isPlanNoLimits(getMasterUserPlan(masterId))) // if plan with limits - checking limits
                if((getMyConsumedResources().getUsers()+amountToRepair)>getMyMaxAllowedResources().getUsers())
                    return -120; // number of users is out of bounds of tariff plan
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = getMyId();
            String stringQuery;
            stringQuery = "Update users p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.master_id=" + masterId +" and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")";
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
                "   sst.canonical_id as time_zone, " +      // time zone name, e.g. 'CET'
                "   coalesce(sidenav_drawer,'open') as sidenav_drawer" +     // "open" or "close"
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
                doc.setSidenav((String)             queryList.get(0)[11]);
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

    @Transactional
    public Boolean setSidenavDrawer(Long userId, String sidenav) {
        Long myMasterId = getUserMasterIdByUserId(userId);
        String stringQuery;
        stringQuery = "update user_settings set sidenav_drawer = :sidenav_drawer where user_id = "+userId+" and master_id = " +myMasterId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("sidenav_drawer", sidenav);
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method setSidenavDrawer. SQL query:"+stringQuery, e);
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
        Long myMasterId = getMyMasterId();
        try{
            if(storageService.isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде (dev mode)
            } else {                    BASE_FILES_FOLDER = files_path;} //запущено в linux (prod mode)
            String MY_MASTER_ID_FOLDER = myMasterId.toString();
            File folder = new File(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER);
            long size = 0L;
            if(storageService.isPathExists(folder.getPath()))
                size = storageService.getDirectorySize(folder);
            String stringQuery = getSQLForMyConsumedResources(myMasterId);
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
            doc.setStores_woo(Long.parseLong(               queryList.get(0)[6].toString()));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getConsumedUserResources", e);
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    public UserResources getMyConsumedResources(Long myMasterId){
        String BASE_FILES_FOLDER;
        try{
            if(storageService.isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде (dev mode)
            } else {                    BASE_FILES_FOLDER = files_path;} //запущено в linux (prod mode)
            String MY_MASTER_ID_FOLDER = myMasterId.toString();
            File folder = new File(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER);
            long size = 0L;
            if(storageService.isPathExists(folder.getPath()))
                size = storageService.getDirectorySize(folder);
            String stringQuery = getSQLForMyConsumedResources(myMasterId);
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
            doc.setStores_woo(Long.parseLong(               queryList.get(0)[6].toString()));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getConsumedUserResources", e);
            return null;
        }
    }
    private String getSQLForMyConsumedResources(Long myMasterId){
        return ("   select" +
                "   (select count(*) from companies   where master_id="+myMasterId+" and coalesce(is_deleted,false)=false)               as companies," +
                "   (select count(*) from departments where master_id="+myMasterId+" and coalesce(is_deleted,false)=false)               as departments," +
                "   (select count(*) from users       where master_id="+myMasterId+" and coalesce(is_deleted,false)=false)               as users," +
                "   (select count(*) from products    where master_id="+myMasterId+" and coalesce(is_deleted,false)=false)               as products," +
                "   (select count(*) from cagents     where master_id="+myMasterId+" and coalesce(is_deleted,false)=false)               as counterparties," +
                "   (select count(*) from stores      where master_id="+myMasterId+" and coalesce(is_deleted,false)=false)               as stores," +
                "   (select count(*) from _saas_stores_for_ordering  where master_id="+myMasterId+" and is_deleted=false) as stores_woo");
    }

    // Counting maximal allowed user resources
    @SuppressWarnings("Duplicates")
    public UserResources getMyMaxAllowedResources(){
        Long myMasterId=getMyMasterId();
        try{
            int plan_id = getMasterUserPlan(myMasterId);
            boolean isPlanFree = (Boolean)subscriptionRepository.isPlanFree(plan_id);
            String stringQuery =getSQLForMyMaxAllowedResources1(plan_id);
            // on free plans there is no additional options
            if(!isPlanFree)
                stringQuery = stringQuery + getSQLForMyMaxAllowedResources2(myMasterId);
            else
                stringQuery = stringQuery + ") AS result ";
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            UserResources doc = new UserResources();
            doc.setCompanies(Long.parseLong(                queryList.get(0)[0].toString()));
            doc.setDepartments(Long.parseLong(              queryList.get(0)[1].toString()));
            doc.setUsers(Long.parseLong(                    queryList.get(0)[2].toString()));
            doc.setProducts(Long.parseLong(                 queryList.get(0)[3].toString().replace(".00","")));
            doc.setCounterparties(Long.parseLong(           queryList.get(0)[4].toString().replace(".00","")));
            doc.setMegabytes(              Integer.parseInt(queryList.get(0)[5].toString().replace(".00","")));
            doc.setStores(        Long.parseLong(           queryList.get(0)[6].toString()));
            doc.setStores_woo(    Long.parseLong(           queryList.get(0)[7].toString()));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMaxAllowedUserResources", e);
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    public UserResources getMyMaxAllowedResources(Long myMasterId){
        try{
            int plan_id = getMasterUserPlan(myMasterId);
            boolean isPlanFree = (Boolean)subscriptionRepository.isPlanFree(plan_id);
            String stringQuery =getSQLForMyMaxAllowedResources1(plan_id);
            // on free plans there is no additional options
            if(!isPlanFree)
                stringQuery = stringQuery + getSQLForMyMaxAllowedResources2(myMasterId);
            else
                stringQuery = stringQuery + ") AS result ";
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            UserResources doc = new UserResources();
            doc.setCompanies(Long.parseLong(                queryList.get(0)[0].toString()));
            doc.setDepartments(Long.parseLong(              queryList.get(0)[1].toString()));
            doc.setUsers(Long.parseLong(                    queryList.get(0)[2].toString()));
            doc.setProducts(Long.parseLong(                 queryList.get(0)[3].toString().replace(".00","")));
            doc.setCounterparties(Long.parseLong(           queryList.get(0)[4].toString().replace(".00","")));
            doc.setMegabytes(              Integer.parseInt(queryList.get(0)[5].toString().replace(".00","")));
            doc.setStores(        Long.parseLong(           queryList.get(0)[6].toString()));
            doc.setStores_woo(    Long.parseLong(           queryList.get(0)[7].toString()));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMaxAllowedUserResources", e);
            return null;
        }
    }
    private String getSQLForMyMaxAllowedResources1(int plan_id){
      return("select   sum(companies) as companies, " +
              " sum(departments) as departments, " +
              " sum(users) as users, " +
              " sum(products) as products, " +
              " sum(counterparties) as counterparties, " +
              " sum(megabytes) as megabytes, " +
              " sum(stores) as stores, " +
              " sum(n_stores_woo) as n_stores_woo " +
              " from (" +
              "(select n_companies as companies, " +
              " n_departments as departments, " +
              " n_users as users, " +
              " ROUND(n_products*1000,0) as products, " +  // in plans table 1000 products is 1. ROUND because n_products is decimal
              " ROUND(n_counterparties*1000) as counterparties, " +// in plans table 1000 counterparties is 1. ROUND because n_counterparties is decimal
              " ROUND(n_megabytes*1024) as megabytes, " +// in plans table 1024 megabytes is 1. ROUND because n_megabytes is decimal
              " n_stores as stores, " +
              " n_stores_woo as n_stores_woo " +
              " from plans " +
              " where id = "+plan_id+")");
    }

    private String getSQLForMyMaxAllowedResources2(Long myMasterId){
        return( " UNION " +
                " (select " +
                " n_companies as companies, " +
                " n_departments as departments, n_users as users, " +
                " n_products*1000 as products, " +// in plans_add_options table 1000 products is 1
                " n_counterparties*1000 as counterparties, " +// in plans_add_options table 1000 counterparties is 1
                " n_megabytes*1024 as megabytes, " +// in plans_add_options table 1000 megabytes is 1
                " n_stores as stores, " +
                " n_stores_woo as n_stores_woo " +
                " from plans_add_options " +
                " where user_id="+myMasterId+")"+
                ") AS result ");
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

    @Transactional
    public Integer updateLegalMasterUserInfo(LegalMasterUserInfoForm userInfo){
        Long myId = getMyId();
        Long masterId = getMyMasterId();
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(5L, "26"); // Пользователи:"Редактирование своего"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(5L, "27"); // Пользователи:"Редактирование всех"
        boolean myIdIsMasterId=(myId.equals(masterId));
        if(((myIdIsMasterId && userHasPermissions_OwnUpdate)//(если пользователь сохраняет свой аккаунт и у него есть на это права
            ||(!myIdIsMasterId && userHasPermissions_AllUpdate))//или если пользователь сохраняет чужой аккаунт и у него есть на это права)
            ) //и сохраняемый аккаунт под юрисдикцией главного аккаунта
        {
            String stringQuery =
            " update users set " +
            " jr_legal_form = :jr_legal_form, " +
            " jr_jur_name = :jr_jur_name, " +
            " jr_name = :jr_name, " +
            " jr_surname = :jr_surname, " +
            " jr_country_id = " + userInfo.getJr_country_id() + ", " +
            " jr_vat = :jr_vat, " +
            " jr_changer_id = " + myId + ", " +
            " jr_date_time_changed = now()" +
            " where id = " + masterId;
           try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("jr_legal_form", userInfo.getJr_legal_form());
                query.setParameter("jr_jur_name",userInfo.getJr_jur_name());
                query.setParameter("jr_name",userInfo.getJr_name());
                query.setParameter("jr_vat",userInfo.getJr_vat());
                query.setParameter("jr_surname",userInfo.getJr_surname());
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method updateLegalMasterUserInfo", e);
                return null;
            }
        } else return -1;
    }

    public LegalMasterUserInfoJSON getLegalMasterUserInfo(){
        Long masterId = getMyMasterId();
        if(securityRepositoryJPA.userHasPermissions_OR(5L, "24,25")){

            UserSettingsJSON userSettings = getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String suffix=getMySuffix();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'

            String stringQuery = "select" +
                    "           coalesce(p.jr_legal_form,'') as  jr_legal_form, " +
                    "           coalesce(p.jr_jur_name,'') as    jr_jur_name, " +
                    "           coalesce(p.jr_name,'') as        jr_name, " +
                    "           coalesce(p.jr_surname,'') as     jr_surname, " +
                    "           p.jr_country_id as               jr_country_id, " +
                    "           coalesce(p.jr_vat,'') as         jr_vat, " +
                    "           p.jr_changer_id as               jr_changer_id, " +
                    "           to_char(p.jr_date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as jr_date_time_changed, " +
                    "           (select name_"+suffix+" from sprav_sys_countries where id=p.jr_country_id) as jr_country, " +
                    "           (select name from users where id=p.jr_changer_id) as jr_changer " +
                    "           from users p" +
                    "           where p.id= " + masterId;

            if (!securityRepositoryJPA.userHasPermissions_OR(5L, "25")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие 24
                stringQuery = stringQuery + " and p.company_id=" + getMyCompanyId();
            }

            try {

                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                LegalMasterUserInfoJSON doc = new LegalMasterUserInfoJSON();

                if(queryList.size()>0) {

                    doc.setJr_legal_form((String) queryList.get(0)[0]);
                    doc.setJr_jur_name((String) queryList.get(0)[1]);
                    doc.setJr_name((String) queryList.get(0)[2]);
                    doc.setJr_surname((String) queryList.get(0)[3]);
                    doc.setJr_country_id((Integer) queryList.get(0)[4]);
                    doc.setJr_vat((String) queryList.get(0)[5]);
                    doc.setJr_changer_id(queryList.get(0)[6] != null ? Long.parseLong(queryList.get(0)[6].toString()) : null);
                    doc.setJr_date_time_changed((String) queryList.get(0)[7]);
                    doc.setJr_country((String) queryList.get(0)[8]);
                    doc.setJr_changer((String) queryList.get(0)[9]);

                }
                return doc;
            } catch(NoResultException nre){
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method updateLegalMasterUserInfo", e);
                return null;
            }
        } else return null;
    }

    @Transactional
    public void setUserAsCagent(Long userId, String userName, String userEmail, SettingsGeneralJSON settingsGeneral){
        String stringQuery;
        stringQuery =
            " insert into cagents ( " +
                " master_id, " +
                " creator_id, " +
                " company_id, " +
                " date_time_created, " +
                " user_id, " +
                " name, " +
                " description, " +
                " email, " +
                " type," +
                " legal_form" +
            ") values ( " +
                settingsGeneral.getBilling_master_id()           + ", " +
                settingsGeneral.getBilling_shipment_creator_id() + ", " +
                settingsGeneral.getBilling_shipment_company_id() + ", " +
                " now(), " +
                userId + ", " +
                " :userName, " +
                " CONCAT('User with id = ',"+userId+"), " +
                " :userEmail," +
                "'individual', " +
                "''" +
            "); " +

            " insert into cagent_cagentcategories (" +
                "category_id, " +
                "cagent_id" +
            ") values ( " +
                settingsGeneral.getBilling_cagents_category_id() + ", " +
                "(select id from cagents where email='"+userEmail+"')" +
            ") " +
            " ON CONFLICT ON CONSTRAINT cagent_cagentcategories_uq DO NOTHING;";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("userEmail",userEmail);
            query.setParameter("userName",userName);
            query.executeUpdate();
        }catch (Exception e) {
            logger.error("Exception in method setUserAsCagent. SQL query:"+stringQuery, e);
            e.printStackTrace();
        }
    }

    //отдает сотрудников по списку должностей и отделений
    public List<EmployeeScedule> getEmployeeListByDepartmentsAndJobtitles(List<Long> depIds, List<Long> jobttlsIds) {
        String stringQuery;
        Long masterId = getMyMasterId();
        String departmentsIds = commonUtilites.ListOfLongToString(depIds, ",", "(", ")");
        String jobtitlesIds = commonUtilites.ListOfLongToString(jobttlsIds, ",", "(", ")");
        stringQuery =   "     select " +
                "     u.id as id, " +
                "     u.name as name, " +
                "     sj.name as jobtitle, " +
                "     coalesce(u.is_currently_employed, false) as is_currently_employed " +
                "     from users u " +
                "     inner join sprav_jobtitles sj on sj.id=u.job_title_id " +
                "     inner join user_department ud on ud.user_id=u.id " +
                "     where " +
                "     coalesce(u.is_employee, false)=true and " +
                "     coalesce(u.is_deleted, false)=false and " +
                "     coalesce(u.is_currently_employed, false)=true and " +
                "     ud.department_id in " + departmentsIds + " and " +
                "     u.job_title_id in " + jobtitlesIds +
                "     group by u.id, u.name, sj.name " +
                "     order by u.name asc";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<EmployeeScedule> employeeList = new ArrayList<>();
            for (Object[] obj : queryList) {
                EmployeeScedule doc = new EmployeeScedule();
                doc.setId(Long.parseLong(               obj[0].toString()));
                doc.setName((String)                    obj[1]);
                doc.setJobtitle((String)                obj[2]);
                doc.setIs_currently_employed((Boolean)  obj[3]);
                doc.setDepartments_with_parts(departmentRepositoryJPA.getDepartmentsWithPartsListOfUser(doc.getId(),masterId));
                doc.setEmployee_services(getUserProducts(doc.getId(), masterId));
                employeeList.add(doc);
            }
            return employeeList;
        } catch (Exception e) {
            logger.error("Exception in method getEmployeeListByDepartmentsIds. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


}
