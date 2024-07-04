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

package com.dokio.controller;

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.UserSettingsForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.additional.LegalMasterUserInfoForm;
import com.dokio.message.response.*;
import com.dokio.message.response.additional.BaseFiles;
import com.dokio.message.response.additional.MyShortInfoJSON;
import com.dokio.message.response.additional.UserResources;
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
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    DocumentsRepositoryJPA documentsRepository;
    @Autowired
    SpravStatusDocRepository ssd;

//    @PostMapping("/api/auth/addUser")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> addUser(@Valid @RequestBody SignUpForm signUpRequest) {
//        logger.info("Processing post request for path api/auth/addUser: " + signUpRequest.toString());
//
//
//
//    }

    //Отдает ЗНАЧЕНИЯ из таблицы users по id
    @PostMapping("/api/auth/getUserValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserValuesById(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path api/auth/getUserValuesById: " + request.toString());

        UserJSON_ user;
        long id = request.getId();
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

            ResponseEntity<UserJSON_> responseEntity = new ResponseEntity<>(user, HttpStatus.OK);
            return responseEntity;
        }
        catch(NullPointerException npe){return null;}
    }

    //Id отделений пользователя
    @PostMapping("/api/auth/getUserDepartments")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserDepartments(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path api/auth/getUserDepartments: " + request.toString());
        long id = request.getId();
        List<Integer> depList =userRepositoryJPA.getUserDepartmentsId(id);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(depList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/updateUser: " + request.toString());
        try {return new ResponseEntity<>(userRepositoryJPA.updateUser(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Exception in method updateUser. " + request.toString(), e);
            e.printStackTrace();
            return new ResponseEntity<>("Operation error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/addUser")
    public ResponseEntity<?> addUser(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/addUser: " + request.toString());
        try {return new ResponseEntity<>(userRepositoryJPA.addUser(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Exception in method addUser. " + request.toString(), e);
            e.printStackTrace();
            return new ResponseEntity<>("Operation error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/getUsersTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUsersTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getUsersTable: " + searchRequest.toString());
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать отделения/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
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
        returnList = userRepositoryJPA.getUsersTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
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
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = userRepositoryJPA.getUsersSize(searchString,companyId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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
    public  ResponseEntity<?> deleteUsers(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteUsers: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(userRepositoryJPA.deleteUsers(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteUsers error", e);
            return new ResponseEntity<>("Error of deleting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteUsers")
    public  ResponseEntity<?> undeleteUsers(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteUsers: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(userRepositoryJPA.undeleteUsers(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteUsers error", e);
            return new ResponseEntity<>("Error of recovering", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(value = "/api/auth/getMySettings",
        method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public  ResponseEntity<?> getMySettings(){
        logger.info("Processing get request for path /api/auth/getMySettings");
        try {return new ResponseEntity<>(userRepositoryJPA.getMySettings(), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getMySettings error", e);
            return new ResponseEntity<>("Error of getting user's settings", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/saveUserSettings")
    public  ResponseEntity<?> saveUserSettings(@RequestBody UserSettingsForm request) {
        logger.info("Processing post request for path /api/auth/saveUserSettings: " + request.toString());
        try {return new ResponseEntity<>(userRepositoryJPA.saveUserSettings(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller saveUserSettings error", e);
            return new ResponseEntity<>("Error of saving user settings", HttpStatus.INTERNAL_SERVER_ERROR);}
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

    @RequestMapping(
            value = "/api/auth/getUsersList",
            params = {"company_id", "search_string"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getUsersList(
            @RequestParam("company_id") Long companyId,
            @RequestParam("search_string") String searchString
            ){
        logger.info("Processing post request for path /api/auth/getUsersList");
        try {return new ResponseEntity<>(userRepositoryJPA.getUsersList(companyId, searchString), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getUsersList error", e);
            return new ResponseEntity<>("Operation of the synchronization error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateLegalMasterUserInfo")
    public ResponseEntity<?> updateLegalMasterUserInfo(@RequestBody LegalMasterUserInfoForm request){
        logger.info("Processing post request for path /api/auth/updateLegalMasterUserInfo: " + request.toString());
        try {return new ResponseEntity<>(userRepositoryJPA.updateLegalMasterUserInfo(request), HttpStatus.OK);}
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller updateLegalMasterUserInfo error", e);
            return new ResponseEntity<>("Error of updating master user legal information", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/api/auth/getLegalMasterUserInfo",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getLegalMasterUserInfo() {
        logger.info("Processing get request for path /api/auth/getLegalMasterUserInfo with no params");
        try {return new ResponseEntity<>(userRepositoryJPA.getLegalMasterUserInfo(), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getLegalMasterUserInfo error", e);
            return new ResponseEntity<>("Error query of getting master user legal information", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(value = "/api/auth/setSidenavDrawer",
            params = {"user_id", "sidenav"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> setSidenavDrawer(
            @RequestParam("user_id") Long user_id,
            @RequestParam("sidenav") String sidenav) {
        logger.info("Processing get request for path /api/auth/setSidenavDrawer with no params");
        try {return new ResponseEntity<>(userRepositoryJPA.setSidenavDrawer(user_id,sidenav), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setSidenavDrawer error", e);
            return new ResponseEntity<>("Error query of setting sidenav drawer information", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getJobtitlesWithEmployeesList",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getJobtitlesWithEmployeesList(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getJobtitlesWithEmployeesList with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(userRepositoryJPA.getJobtitlesWithEmployeesList(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getJobtitlesWithEmployeesList error", e);
            return new ResponseEntity<>("Controller getJobtitlesWithEmployeesList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
