package com.laniakea.controller;

import com.laniakea.message.request.SearchForm;
import com.laniakea.message.request.SignUpForm;
import com.laniakea.message.request.UserPermForm;
import com.laniakea.message.response.ResponseMessage;
import com.laniakea.message.response.UsersJSON;
import com.laniakea.message.response.UsersListJSON;
import com.laniakea.message.response.UsersTableJSON;
import com.laniakea.model.*;
import com.laniakea.repository.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.NoResultException;
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
    UserDetailsServiceImpl userRepository2;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private UserDetailsServiceImpl userService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;

    @PostMapping("/api/auth/addUser")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> addUser(@Valid @RequestBody SignUpForm signUpRequest) {
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

            User creator = userService.getUserByUsername(userService.getUserName());
            user.setCreator(creator);//создателя

            User master = userRepository2.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository2.getUserName())));
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

    //Отдает ЗНАЧЕНИЯ из таблицы users по id отделения
    @PostMapping("/api/auth/getUserValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserValuesById(@RequestBody SignUpForm request) {
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
        int id = request.getId();
        List<Integer> depList =userRepositoryJPA.getUserDepartmentsId(id);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(depList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateUser")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateUser(@RequestBody SignUpForm request) throws ParseException{
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
        int id = request.getId();
        List<UsersListJSON> usersList =userRepositoryJPA.getUsersListByDepartmentId(id);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(usersList, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/api/auth/getMyId")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyId() {
        Long id=userRepositoryJPA.getMyId();
        ResponseEntity<Long> responseEntity = new ResponseEntity<>(id, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/api/auth/getMyCompanyId")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyCompanyId() {
        int id=userRepositoryJPA.getMyCompanyId();
        ResponseEntity<Integer> responseEntity = new ResponseEntity<>(id, HttpStatus.OK);
        return responseEntity;
    }
}
