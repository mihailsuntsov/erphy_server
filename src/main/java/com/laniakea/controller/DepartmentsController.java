package com.laniakea.controller;


import com.laniakea.message.request.DepartmentForm;
import com.laniakea.message.request.SearchForm;
import com.laniakea.message.request.SignUpForm;
import com.laniakea.message.response.DepartmentsJSON;
import com.laniakea.model.*;
import com.laniakea.repository.UserRepositoryJPA;
import com.laniakea.repository.DepartmentRepositoryJPA;
import com.laniakea.security.services.UserDetailsServiceImpl;
import com.laniakea.service.department.DepartmentService;
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
        int parentId=Integer.parseInt(searchRequest.getParentId());
        List<Departments> departmentsList;
        departmentsList = departmentService.getDeptChildrens(parentId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/insertDepartment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertDepartment(@RequestBody DepartmentForm request) throws ParseException {

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

        User creator =  userRepository.getUserByUsername(userRepository.getUserName());
        User master = userRepository.getUserByUsername(
                userRepositoryJPA.getUsernameById(
                        userRepositoryJPA.getUserMasterIdByUsername(
                                userRepository.getUserName() )));

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Departments newDock = new Departments();
        newDock.setName(name);
        newDock.setCreator(creator);
        newDock.setMaster(master);
        newDock.setDate_time_created(timestamp);
        newDock.setAdditional(request.getAdditional());
        newDock.setAddress(request.getAddress());
        newDock.setCompany(company);
        newDock.setParent(parentDepartment);

        Long idNewDock=departmentService.insertDepartment(newDock);

        if(idNewDock !=null){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + idNewDock+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getDepartmentsListByCompanyId")//возвращает список отделений предприятия по его id
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentsListByCompanyId(@RequestBody SearchForm searchRequest) {
        int companyId=Integer.parseInt(searchRequest.getCompanyId());
        boolean has_parent=searchRequest.isHas_parent();
        List<Departments> departmentsList;
        departmentsList = departmentService.getDepartmentsListByCompanyId(companyId,has_parent);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getMyDepartmentsListByCompanyId")//возвращает список отделений предприятия по его id
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyDepartmentsListByCompanyId(@RequestBody SearchForm searchRequest) {
        int companyId=Integer.parseInt(searchRequest.getCompanyId());
        boolean has_parent=searchRequest.isHas_parent();
        List<Departments> departmentsList;
        departmentsList = departmentRepositoryJPA.getMyDepartmentsListByCompanyId(companyId,has_parent);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(departmentsList, HttpStatus.OK);
        return responseEntity;
    }

    //Отдает ЗНАЧЕНИЯ из таблицы departments по id отделения
    @PostMapping("/api/auth/getDepartmentValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getDepartmentValuesById(@RequestBody DepartmentForm request) {
        DepartmentsJSON department;
        int id = request.getId();
        department=departmentService.getDepartmentValuesById(id);//результат запроса помещается в объект
        ResponseEntity<DepartmentsJSON> responseEntity = new ResponseEntity<>(department, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateDepartment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateDepartment(@RequestBody DepartmentForm request) throws ParseException {
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
