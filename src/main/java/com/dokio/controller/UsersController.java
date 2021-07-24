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
package com.dokio.controller;

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.ResponseMessage;
import com.dokio.message.response.UsersJSON;
import com.dokio.message.response.UsersListJSON;
import com.dokio.message.response.UsersTableJSON;
import com.dokio.message.response.additional.MyShortInfoJSON;
import com.dokio.model.*;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UsersController {
    Logger logger = Logger.getLogger("UsersController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;

    @PostMapping("/api/auth/addUser")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> addUser(@Valid @RequestBody SignUpForm signUpRequest) {
        logger.info("Processing post request for path api/auth/addUser: " + signUpRequest.toString());

        if(securityRepositoryJPA.userHasPermissions_OR(5L, "22"))// Пользователи:"Создание"
        {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return new ResponseEntity<>(new ResponseMessage("Такой логин уже зарегистрирован"),
                        HttpStatus.BAD_REQUEST);
            }
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return new ResponseEntity<>(new ResponseMessage("Такой Email уже зарегистрирован"),
                        HttpStatus.BAD_REQUEST);
            }

            // Если такого логина и емайла нет
            // Создание аккаунта для нового пользователя
            User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
            roles.add(userRole);
            user.setRoles(roles);//добавили юзеру роль ROLE_USER

            user.setCompany(companyRepositoryJPA.getCompanyById(Long.valueOf(Integer.parseInt(signUpRequest.getCompany_id()))));//предприятие

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

            User master = userDetailService.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userDetailService.getUserName())));
            user.setMaster(master);//владельца

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            user.setDate_time_created(timestamp);//дату создания

            user.setFio_family(signUpRequest.getFio_family());
            user.setFio_name(signUpRequest.getFio_name());
            user.setFio_otchestvo(signUpRequest.getFio_otchestvo());
            user.setName(signUpRequest.getName());
            user.setStatus_account(Integer.parseInt(signUpRequest.getStatus_account()));
            user.setSex(signUpRequest.getSex());
            user.setAdditional(signUpRequest.getAdditional());

            Long createdUserId = userRepository.save(user).getId();//и сохранили его
            //ответ сервера при удачном создании юзера
            ResponseEntity<String> responseEntity = new ResponseEntity<>(String.valueOf(createdUserId), HttpStatus.OK);
            return responseEntity;
        }else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("You haven't permissions for this operation", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
            return responseEntity;
        }

    }

    //Отдает ЗНАЧЕНИЯ из таблицы users по id
    @PostMapping("/api/auth/getUserValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserValuesById(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path api/auth/getUserValuesById: " + request.toString());

        UsersJSON user;
        int id = request.getId();
        user=userRepositoryJPA.getUserValuesById(id);//результат запроса помещается в объект
        // нужен try catch т.к. если user возвратит null, то user.setUserDepart... вызовет NullPointerException
        try
        {
            List<Integer> depListId = userRepositoryJPA.getUserDepartmentsId(id);
            user.setUserDepartmentsId(depListId);

            List<String> depListNames = userRepositoryJPA.getUserDepartmentsNames(id);
            user.setUserDepartmentsNames(depListNames);

            List<Integer> userGroupsListId = userRepositoryJPA.getUserGroupsId(id);
            user.setUserGroupsId(userGroupsListId);

            ResponseEntity<UsersJSON> responseEntity = new ResponseEntity<>(user, HttpStatus.OK);
            return responseEntity;
        }
        catch(NullPointerException npe){return null;}
    }

    //Id отделений пользователя
    @PostMapping("/api/auth/getUserDepartments")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserDepartments(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path api/auth/getUserDepartments: " + request.toString());

        int id = request.getId();
        List<Integer> depList =userRepositoryJPA.getUserDepartmentsId(id);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(depList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateUser")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateUser(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path api/auth/updateUser: " + request.toString());

        if(userRepositoryJPA.updateUser(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getUsersTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUsersTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getUsersTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать отделения/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        //String masterId;
        List<UsersTableJSON> returnList;

        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "name";
            sortAsc = "asc";
        }
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;
        }
        if (searchRequest.getCompanyId() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            companyId = Integer.parseInt(searchRequest.getCompanyId());
        } else {
            companyId = 0;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = userRepositoryJPA.getUsersTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getUsersPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUsersPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getUsersPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать отделения/ 0 - по всем
        int disabledLINK;// номер страницы на паджинейшене, на которой мы сейчас. Изначально это 1.
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        String sortColumn = searchRequest.getSortColumn();

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        //disabledLINK=pagenum;
        int size = userRepositoryJPA.getUsersSize(searchString,companyId);//  - общее количество записей выборки
        int offsetreal = offset * result;//создана переменная с номером страницы
        int listsize;//количество страниц пагинации
        if((size%result) == 0){//общее количество выборки делим на количество записей на странице
            listsize= size/result;//если делится без остатка
        }else{
            listsize= (size/result)+1;}
        int maxPagenumInBegin;//
        List<Integer> pageList = new ArrayList<Integer>();//список, в котором первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
        pageList.add(size);
        pageList.add(pagenum);
        pageList.add(listsize);

        if (listsize<=5){
            maxPagenumInBegin=listsize;//
        }else{
            maxPagenumInBegin=5;
        }
        if(pagenum >=3) {
            if((pagenum==listsize)||(pagenum+1)==listsize){
                for(int i=(pagenum-(4-(listsize-pagenum))); i<=pagenum-3; i++){
                    if(i>0) {
                        pageList.add(i);  //создается список пагинации за - 4 шага до номера страницы (для конца списка пагинации)
                    }}}
            for(int i=(pagenum-2); i<=pagenum; i++){
                pageList.add(i);  //создается список пагинации за -2 шага до номера страницы
            }
            if((pagenum+2) <=listsize) {
                for(int i=(pagenum+1); i<=(pagenum+2); i++){
                    pageList.add(i);  //создается список пагинации  на +2 шага от номера страницы
                }
            }else{
                if(pagenum<listsize) {
                    for (int i = (pagenum + (listsize - pagenum)); i <= listsize; i++) {
                        pageList.add(i);  //создается список пагинации от номера страницы до конца
                    }}}
        }else{//номер страницы меньше 3
            for(int i=1; i<=pagenum; i++){
                pageList.add(i);  //создается список пагинации от 1 до номера страницы
            }
            for(int i=(pagenum+1); i<=maxPagenumInBegin; i++){
                pageList.add(i);  //создаются дополнительные номера пагинации, но не более 5 в сумме
            }}
        ResponseEntity<List> responseEntity = new ResponseEntity<>(pageList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/deleteUsers")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteUsers(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path api/auth/deleteUsers: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        ArrayList<Long> decArray = new ArrayList<Long>();
        checked=checked.replace("[","");
        checked=checked.replace("]","");

        if(userRepositoryJPA.deleteUsersById(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    //Id и Name пользователей в отделении
    @PostMapping("/api/auth/getUsersListByDepartmentId")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUsersListByDepartmentId(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path api/auth/getUsersListByDepartmentId: " + request.toString());

        int id = request.getId();
        List<UsersListJSON> usersList =userRepositoryJPA.getUsersListByDepartmentId(id);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(usersList, HttpStatus.OK);
        return responseEntity;
    }

    //отдает сотрудников (пользователей) по id отделения
    @RequestMapping(
            value = "/api/auth/getEmployeeListByDepartmentId",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getEmployeeListByDepartmentId(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getEmployeeListByDepartmentId with parameters: " +
                "id: " + id);
        try {
            List<UsersListJSON> usersList = userRepositoryJPA.getEmployeeListByDepartmentId(id);
            return new ResponseEntity<>(usersList, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception in method getEmployeeListByDepartmentId. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки списка сотрудников", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/auth/getMyId")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyId() {
        logger.info("Processing get request for path api/auth/getMyId");

        Long id=userRepositoryJPA.getMyId();
        ResponseEntity<Long> responseEntity = new ResponseEntity<>(id, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/api/auth/getMyCompanyId")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyCompanyId() {
        logger.info("Processing get request for path api/auth/getMyCompanyId");

        int id=userRepositoryJPA.getMyCompanyId();
        ResponseEntity<Integer> responseEntity = new ResponseEntity<>(id, HttpStatus.OK);
        return responseEntity;
    }
    // возвращает true если пользователь может работать с кассовым аппаратом в рамках текущей сессии другого пользователя:
    // пара login-пароль есть в системе, И пользователь принадлежит к тому же предприятию что и пользователь сессии, И пользователь не заблокирован
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/isUserCanWorkWithKKM",
            params = {"username", "password"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> isUserCanWorkWithKKM(
            @RequestParam("username") String username,
            @RequestParam("password") String password)
    {
        logger.info("Processing get request for path /api/auth/isUserCanWorkWithKKM with parameters: " +
                "username: " + username +
                ", password: ******");
        boolean yesItIs;
        try {
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long userCompanyId = userRepositoryJPA.getUserCompanyId(username);
            yesItIs=userRepositoryJPA.checkIfValidOldPassword(userDetailService.getUserByUsername(username),password) && myCompanyId.equals(userCompanyId) && userDetailService.isUserNotBlocked_byUsername(username);
            return new ResponseEntity<>(yesItIs, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // возвращает true если пользователь может работать с кассовым аппаратом в рамках текущей сессии другого пользователя:
    // пара login-пароль есть в системе, И пользователь принадлежит к тому же предприятию что и пользователь сессии, И пользователь не заблокирован
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getUserByLoginInfo",
            params = {"username", "password"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getUserByLoginInfo(
            @RequestParam("username") String username,
            @RequestParam("password") String password)
    {
        logger.info("Processing get request for path /api/auth/getUserByLoginInfo with parameters: " +
                "username: " + username +
                ", password: ******");
        try {
            if(userRepositoryJPA.checkIfValidOldPassword(userDetailService.getUserByUsername(username),password) && userDetailService.isUserNotBlocked_byUsername(username)){
                MyShortInfoJSON response=new MyShortInfoJSON();
                User user = userDetailService.getUserByUsername(username);
                response.setName(user.getName());
                response.setVatin(user.getVatin());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else return  new ResponseEntity<>(null, HttpStatus.OK);

        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //загрузка краткой информации о пользователе
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getMyShortInfo",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getMyShortInfo()
    {
        logger.info("Processing get request for path /api/auth/getMyShortInfo without request parameters");
        MyShortInfoJSON response;
        try {
            response=userRepositoryJPA.getMyShortInfo();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки информации о пользователе", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
