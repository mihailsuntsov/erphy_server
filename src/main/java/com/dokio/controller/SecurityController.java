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
import com.dokio.message.response.IsItMy_JSON;
import com.dokio.message.response.IsItMy_Sprav_JSON;
import com.dokio.message.response.additional.ProductPricesJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SecurityController {
    Logger logger = Logger.getLogger("SecurityController");

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
    SecurityRepositoryJPA securityRepositoryJPA;

    //Отдает набор прав пользователя из таблицы permissions по id документа
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getMyPermissions",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getMyPermissions( @RequestParam("id") Long id) {
        logger.info("Processing get request for path /api/auth/getMyPermissions with id=" + id.toString());
        try {
            List<Integer> returnList =securityRepositoryJPA.giveMeMyPermissions(id);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getMyPermissions", e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке прав", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Отдает весь набор прав пользователя из таблицы permissions
    @PostMapping("/api/auth/getAllMyPermissions")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getAllMyPermissions(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/getAllMyPermissions: " + request.toString());

        List<Integer> depList =securityRepositoryJPA.getAllMyPermissions();
        ResponseEntity<List> responseEntity = new ResponseEntity<>(depList, HttpStatus.OK);
        return responseEntity;
    }
    //Проверяет моё ли это предприятие по его id
    @PostMapping("/api/auth/isItMyCompany")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isItMyCompany(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/isItMyCompany: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        boolean isItMyCompany =securityRepositoryJPA.isItMyCompany(id);
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(isItMyCompany, HttpStatus.OK);
        return responseEntity;
    }
    //Проверяет моё ли это отделение по его id
    @PostMapping("/api/auth/isItMyDepartment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isItMyDepartment(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/isItMyDepartment: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        boolean isItMyDepartment =securityRepositoryJPA.isItMyDepartment(id);
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(isItMyDepartment, HttpStatus.OK);
        return responseEntity;
    }
    //Проверяет мой ли это аккаунт по id
    @PostMapping("/api/auth/isItMyUser")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isItMyUser(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/isItMyUser: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        boolean isItMyUser =securityRepositoryJPA.isItMyUser(id);
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(isItMyUser, HttpStatus.OK);
        return responseEntity;
    }
    //Проверяет, что этот документ мой (создатель)
//    @PostMapping("/api/auth/isItMyUserGroup")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> isItMyUserGroup(@RequestBody SearchForm request) {
//        logger.info("Processing post request for path api/auth/isItMyUserGroup: " + request.toString());
//
//        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
//        boolean isItMyUserGroup =securityRepositoryJPA.isItMyUserGroup(id);
//        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(isItMyUserGroup, HttpStatus.OK);
//        return responseEntity;
//    }
    //Возвращает набор проверок на документ (документ мой?/документ моих отделений?/документ моего предприятия?)
    @PostMapping("/api/auth/getIsItMy_TradeResults_JSON")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getIsItMy_TradeResults_JSON(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/getIsItMy_TradeResults_JSON: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        IsItMy_JSON response;
        response=securityRepositoryJPA.getIsItMy_TradeResults_JSON(id);
        ResponseEntity<IsItMy_JSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
    //Возвращает набор проверок на документ (документ моего предприятия?/документ предприятий мастер-аккаунта?)
    @PostMapping("/api/auth/getIsItMy_SpravSysEdizm_JSON")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getIsItMy_SpravSysEdizm_JSON(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/getIsItMy_SpravSysEdizm_JSON: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        IsItMy_Sprav_JSON response;
        response=securityRepositoryJPA.getIsItMy_SpravSysEdizm_JSON(id);
        ResponseEntity<IsItMy_Sprav_JSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
    //Возвращает набор проверок на документ (документ моего предприятия?/документ предприятий мастер-аккаунта?)
    @PostMapping("/api/auth/getIsItMy_TypePrices_JSON")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getIsItMy_TypePrices_JSON(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/getIsItMy_TypePrices_JSON: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        IsItMy_Sprav_JSON response;
        response=securityRepositoryJPA.getIsItMy_TypePrices_JSON(id);
        ResponseEntity<IsItMy_Sprav_JSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
//    //Возвращает набор проверок на документ (документ моего предприятия?/документ предприятий мастер-аккаунта?)
//    @PostMapping("/api/auth/getIsItMy_ProductGroups_JSON")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> getIsItMy_ProductGroups_JSON(@RequestBody SearchForm request) {
//        logger.info("Processing post request for path api/auth/getIsItMy_ProductGroups_JSON: " + request.toString());
//
//        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
//        IsItMy_Sprav_JSON response;
//        response=securityRepositoryJPA.getIsItMy_ProductGroups_JSON(id);
//        ResponseEntity<IsItMy_Sprav_JSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
//        return responseEntity;
//    }

}
