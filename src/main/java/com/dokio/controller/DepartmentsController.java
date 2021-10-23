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


import com.dokio.message.request.DepartmentForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.response.DepartmentsJSON;
import com.dokio.message.response.DepartmentsListJSON;
import com.dokio.model.*;
import com.dokio.model.Sprav.SpravTypePrices;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.repository.DepartmentRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.department.DepartmentService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

@Controller
public class DepartmentsController {
    Logger logger = Logger.getLogger("DepartmentsController");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    private UserDetailsServiceImpl userRepository;

    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;

    @PostMapping("/api/auth/getDepartmentsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentsTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getDepartmentsTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать отделения/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        //String masterId;
        List<Departments> departmentsList;

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
        departmentsList = departmentService.getDepartmentsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId);//запрос списка: взять кол-во rezult, начиная с offsetreal
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
        int size = departmentService.getDepartmentsSize(searchString,companyId);//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertDepartment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertDepartment(@RequestBody DepartmentForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/insertDepartment: " + request.toString());

        String name = request.getName() == null ? "": request.getName();

        Companies company;
        if (request.getCompany_id() != null && !request.getCompany_id().isEmpty() && request.getCompany_id().trim().length() > 0 && request.getCompany_id() != "0")  {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            company = em.find(Companies.class, Long.valueOf(Integer.parseInt(request.getCompany_id())));
            em.close();}
        else{company=null;}

        Departments parentDepartment;
        if (request.getParent_id() != null && !request.getParent_id().isEmpty() && request.getParent_id().trim().length() > 0 && request.getParent_id() != "0")  {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            parentDepartment = em.find(Departments.class, Long.valueOf(Integer.parseInt(request.getParent_id())));
            em.close();}
        else{parentDepartment=null;}

        SpravTypePrices priceType;
        if (request.getPrice_id() != null && !request.getPrice_id().isEmpty() && request.getPrice_id().trim().length() > 0 && request.getPrice_id() != "0")  {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            priceType = em.find(SpravTypePrices.class, Long.valueOf(Integer.parseInt(request.getPrice_id())));
            em.close();}
        else{priceType=null;}

        User creator =  userRepository.getUserByUsername(userRepository.getUserName());
        User master = userRepository.getUserByUsername(
                userRepositoryJPA.getUsernameById(
                        userRepositoryJPA.getUserMasterIdByUsername(
                                userRepository.getUserName() )));

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Departments newDoc = new Departments();
        newDoc.setName(name);
        newDoc.setCreator(creator);
        newDoc.setMaster(master);
        newDoc.setDate_time_created(timestamp);
        newDoc.setAdditional(request.getAdditional());
        newDoc.setAddress(request.getAddress());
        newDoc.setCompany(company);
        newDoc.setParent(parentDepartment);
        newDoc.setPriceType(priceType);

        Long idNewDoc=departmentService.insertDepartment(newDoc);

        if(idNewDoc !=null){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + idNewDoc+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
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
        int id = request.getId();
        department=departmentService.getDepartmentValuesById(id);//результат запроса помещается в объект
        ResponseEntity<DepartmentsJSON> responseEntity = new ResponseEntity<>(department, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateDepartment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateDepartment(@RequestBody DepartmentForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/updateDepartment: " + request.toString());

        if(departmentService.updateDepartment(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/deleteDepartments")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteDepartments(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteDepartments: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        checked=checked.replace("[","");
        checked=checked.replace("]","");
        if(departmentRepositoryJPA.deleteDepartmentsById(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}
