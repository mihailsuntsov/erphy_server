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

import com.dokio.message.request.DepartmentForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.additional.DepartmentPartsForm;
import com.dokio.message.response.DepartmentsJSON;
import com.dokio.message.response.DepartmentsListJSON;
import com.dokio.model.*;
import com.dokio.repository.DepartmentRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DepartmentsController {

    Logger logger = Logger.getLogger("DepartmentsController");

    @Autowired
    DepartmentRepositoryJPA departmentService;

    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;

    @PostMapping("/api/auth/getDepartmentsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentsTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getDepartmentsTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        Long companyId;//по какому предприятию показывать отделения/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<DepartmentsJSON> departmentsList;

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
            companyId = Long.parseLong(searchRequest.getCompanyId());
        } else {
            companyId = 0L;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        departmentsList = departmentService.getDepartmentsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getDepartmentsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentsPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getDepartmentsPagesList: " + searchRequest.toString());

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
        int size = departmentService.getDepartmentsSize(searchString,companyId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @RequestMapping(
            value = "/api/auth/getDepartmentsList",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDepartmentsList(
            @RequestParam("company_id") Long company_id){
        logger.info("Processing get request for path /api/auth/getDepartmentsList");
        try {return new ResponseEntity<>(departmentService.getDepartmentsList(company_id), HttpStatus.OK);}
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller /api/auth/getDepartmentsList error", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/insertDepartment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertDepartment(@RequestBody DepartmentForm request){
        logger.info("Processing post request for path /api/auth/insertDepartment: " + request.toString());
        try{return new ResponseEntity<>(departmentService.insertDepartment(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller insertDepartment error", e);
            return new ResponseEntity<>("Document creation error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateDepartment")
    public ResponseEntity<?> updateDepartment(@RequestBody DepartmentForm request){
        logger.info("Processing post request for path /api/auth/updateDepartment: " + request.toString());
        try {return new ResponseEntity<>(departmentService.updateDepartment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateDepartment error", e);
        return new ResponseEntity<>("Error saving document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }


    @PostMapping("/api/auth/getDepartmentsListByCompanyId")//возвращает список отделений предприятия по его id
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentsListByCompanyId(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getDepartmentsListByCompanyId: " + searchRequest.toString());

        int companyId=Integer.parseInt(searchRequest.getCompanyId());
        boolean has_parent=searchRequest.isHas_parent();
        List<DepartmentsListJSON> departmentsList;
        departmentsList = departmentService.getDepartmentsListByCompanyId(companyId,has_parent);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getMyDepartmentsListByCompanyId")//возвращает список отделений предприятия по его id
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyDepartmentsListByCompanyId(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMyDepartmentsListByCompanyId: " + searchRequest.toString());

        int companyId=Integer.parseInt(searchRequest.getCompanyId());
        boolean has_parent=searchRequest.isHas_parent();
        List<DepartmentsListJSON> departmentsList;
        departmentsList = departmentRepositoryJPA.getMyDepartmentsListByCompanyId(companyId,has_parent);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    //Отдает ЗНАЧЕНИЯ из таблицы departments по id отделения
    @PostMapping("/api/auth/getDepartmentValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentValuesById(@RequestBody DepartmentForm request) {
        logger.info("Processing post request for path /api/auth/getDepartmentValuesById: " + request.toString());

        DepartmentsJSON department;
        Long id = request.getId();
        department=departmentService.getDepartmentValuesById(id);//результат запроса помещается в объект
        ResponseEntity<DepartmentsJSON> responseEntity = new ResponseEntity<>(department, HttpStatus.OK);
        return responseEntity;
    }


    @PostMapping("/api/auth/deleteDepartments")
    public  ResponseEntity<?> deleteDepartments(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteDepartments: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(departmentService.deleteDepartments(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteDepartments error", e);
            return new ResponseEntity<>("Error of deleting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteDepartments")
    public  ResponseEntity<?> undeleteDepartments(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteDepartments: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(departmentService.undeleteDepartments(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteDepartments error", e);
            return new ResponseEntity<>("Error of recovering", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getDeptChildrens")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDeptChildrens(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getDeptChildrens: " + searchRequest.toString());

        int parentId=Integer.parseInt(searchRequest.getParentId());
        List<Departments> departmentsList;
        departmentsList = departmentService.getDeptChildrens(parentId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    @RequestMapping(
            value = "/api/auth/getDepartmentPartsList",
            params = {"department_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDepartmentPartsList( @RequestParam("department_id") Long departmentId) {
        logger.info("Processing get request for path /api/auth/getDepartmentPartsList with department_id=" + departmentId.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.getDepartmentPartsList(departmentId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getDepartmentPartsList error with department_id=" + departmentId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getDepartmentsWithPartsList",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDepartmentsWithPartsList( @RequestParam("company_id") Long companyId) {
        logger.info("Processing get request for path /api/auth/getDepartmentsWithPartsList with company_id=" + companyId.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.getDepartmentsWithPartsList(companyId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getDepartmentsWithPartsList error with company_id=" + companyId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    /*@RequestMapping(
            value = "/api/auth/getDepartmentPartsWithResourceQttList",
            params = {"company_id","resource_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDepartmentPartsWithResourceQttList( @RequestParam("company_id") Long companyId, @RequestParam("resource_id") Long resourceId) {
        logger.info("Processing get request for path /api/auth/getDepartmentPartsWithResourceQttList with company_id=" + companyId.toString()+"resource_id=" + resourceId.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.getDepartmentPartsWithResourceQttList(companyId,resourceId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getDepartmentPartsWithResourceQttList error with company_id=" + companyId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getDepartmentPartsWithResourceQttList2",
            params = {"company_id","resource_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDepartmentPartsWithResourceQttList2( @RequestParam("company_id") Long companyId, @RequestParam("resource_id") Long resourceId) {
        logger.info("Processing get request for path /api/auth/getDepartmentPartsWithResourceQttList2 with company_id=" + companyId.toString()+"resource_id=" + resourceId.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.getDepartmentPartsWithResourceQttList2(companyId,resourceId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getDepartmentPartsWithResourceQttList2 error with company_id=" + companyId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }*/
    @PostMapping("/api/auth/insertDepartmentPart")
    public ResponseEntity<?> insertDepartmentPart(@RequestBody DepartmentPartsForm request){
        logger.info("Processing post request for path /api/auth/insertDepartmentPart: " + request.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.insertDepartmentPart(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertDepartmentPart error", e);
            return new ResponseEntity<>("Error saving the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateDepartmentPart")
    public ResponseEntity<?> updateDepartmentPart(@RequestBody DepartmentPartsForm request){
        logger.info("Processing post request for path /api/auth/updateDepartmentPart: " + request.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.updateDepartmentPart(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateDepartmentPart error", e);
            return new ResponseEntity<>("Error saving the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/deleteDepartmentPart",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteDepartmentPart( @RequestParam("id") Long partId) {
        logger.info("Processing get request for path /api/auth/deleteDepartmentPart with termId=" + partId.toString());
        try {return new ResponseEntity<>(departmentRepositoryJPA.deleteDepartmentPart(partId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller deleteDepartmentPart error with termId=" + partId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
