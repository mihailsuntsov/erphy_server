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
import com.dokio.message.response.IsItMy_JSON;
import com.dokio.message.response.IsItMy_Sprav_JSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    @PostMapping("/api/auth/giveMeMyPermissions")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> giveMeMyPermissions(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/giveMeMyPermissions: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        List<Integer> depList =securityRepositoryJPA.giveMeMyPermissions(id);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(depList, HttpStatus.OK);
        return responseEntity;
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
    @PostMapping("/api/auth/isItMyUserGroup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isItMyUserGroup(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/isItMyUserGroup: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        boolean isItMyUserGroup =securityRepositoryJPA.isItMyUserGroup(id);
        ResponseEntity<Boolean> responseEntity = new ResponseEntity<>(isItMyUserGroup, HttpStatus.OK);
        return responseEntity;
    }
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
    //Возвращает набор проверок на документ (документ моего предприятия?/документ предприятий мастер-аккаунта?)
    @PostMapping("/api/auth/getIsItMy_ProductGroups_JSON")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getIsItMy_ProductGroups_JSON(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/getIsItMy_ProductGroups_JSON: " + request.toString());

        Long id = Long.valueOf(Integer.parseInt(request.getDocumentId()));
        IsItMy_Sprav_JSON response;
        response=securityRepositoryJPA.getIsItMy_ProductGroups_JSON(id);
        ResponseEntity<IsItMy_Sprav_JSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }

}
