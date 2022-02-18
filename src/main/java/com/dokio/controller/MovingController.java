/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.controller;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsMovingForm;
import com.dokio.message.response.MovingJSON;
import com.dokio.message.response.Settings.SettingsMovingJSON;
import com.dokio.message.response.additional.FilesMovingJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.MovingProductTableJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class MovingController {
    Logger logger = Logger.getLogger("MovingController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;
    @Autowired
    MovingRepository movingRepositoryJPA;
    @Autowired
    StorageService storageService;

    @PostMapping("/api/auth/getMovingTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMovingTable(@RequestBody MovingSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMovingTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        Long companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        Long departmentFromId;//id отделения из которого производится перемещение
        Long departmentToId;//id отделения в которое производится перемещение
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<MovingJSON> returnList;

        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "name";
            sortAsc = "asc";
        }
        if (searchRequest.getResult() != null && searchRequest.getResult()>0) {
            result = searchRequest.getResult();
        } else {
            result = 10;
        }
        if (searchRequest.getCompanyId() != null) {
            companyId = searchRequest.getCompanyId();
        } else {
            companyId = 0L;
        }
        if (searchRequest.getDepartmentFromId() != null) {
            departmentFromId = searchRequest.getDepartmentFromId();
        } else {
            departmentFromId = 0L;
        }
        if (searchRequest.getDepartmentToId() != null) {
            departmentToId = searchRequest.getDepartmentToId();
        } else {
            departmentToId = 0L;
        }
        if (searchRequest.getOffset() != null) {
            offset = searchRequest.getOffset();
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = movingRepositoryJPA.getMovingTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, departmentFromId, departmentToId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getMovingProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getMovingProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getMovingProductTable with Moving id=" + docId.toString());
        List<MovingProductTableJSON> returnList;
        try {
            returnList = movingRepositoryJPA.getMovingProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getMovingPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMovingPagesList(@RequestBody MovingSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMovingPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        Long companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        Long departmentFromId;//id отделения из которого производится перемещение
        Long departmentToId;//id отделения в которое производится перемещение
        String searchString = searchRequest.getSearchString();
        if (searchRequest.getCompanyId() != null) {
            companyId = searchRequest.getCompanyId();
        } else {
            companyId = 0L;
        }
        if (searchRequest.getDepartmentFromId() != null) {
            departmentFromId = searchRequest.getDepartmentFromId();
        } else {
            departmentFromId = 0L;
        }
        if (searchRequest.getDepartmentToId() != null) {
            departmentToId = searchRequest.getDepartmentToId();
        } else {
            departmentToId = 0L;
        }
        if (searchRequest.getResult() != null && searchRequest.getResult()>0) {
            result = searchRequest.getResult();
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null) {
            offset = searchRequest.getOffset();
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = movingRepositoryJPA.getMovingSize(searchString, companyId, departmentFromId, departmentToId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertMoving")
    public ResponseEntity<?> insertMoving(@RequestBody MovingForm request){
        logger.info("Processing post request for path /api/auth/insertMoving: " + request.toString());
        Long newDocument = movingRepositoryJPA.insertMoving(request);
        return new ResponseEntity<>(newDocument, HttpStatus.OK);//вернет id созданного документа, 0 - если недостаточно прав, null - ошибка
    }

    @PostMapping("/api/auth/isMovingNumberUnical")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isMovingNumberUnical(@RequestBody UniversalForm request) { // id1 - document_id, id2 - company_id
        logger.info("Processing post request for path /api/auth/isMovingNumberUnical: " + request.toString());

        try {
            Boolean ret = movingRepositoryJPA.isMovingNumberUnical(request);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getMovingValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getMovingValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getMovingValuesById with parameters: " + "id: " + id);
        MovingJSON response;
        try {
            response=movingRepositoryJPA.getMovingValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getMovingValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки значений документа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/updateMoving")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateMoving(@RequestBody MovingForm request){
        logger.info("Processing post request for path /api/auth/updateMoving: " + request.toString());
        return new ResponseEntity<>(movingRepositoryJPA.updateMoving(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/deleteMoving")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteMoving(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteMoving: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(movingRepositoryJPA.deleteMoving(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteMoving")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteMoving(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteMoving: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(movingRepositoryJPA.undeleteMoving(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/getListOfMovingFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfMovingFiles(@RequestBody SearchForm request){
        logger.info("Processing post request for path /api/auth/getListOfMovingFiles: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<FilesMovingJSON> returnList;
        try {
            returnList = movingRepositoryJPA.getListOfMovingFiles(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteMovingFile")
    public ResponseEntity<?> deleteMovingFile(@RequestBody SearchForm request){
        logger.info("Processing post request for path /api/auth/deleteMovingFile: " + request.toString());

        if(movingRepositoryJPA.deleteMovingFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка удаления файла", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToMoving")
    public ResponseEntity<?> addFilesToMoving(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/addFilesToMoving: " + request.toString());

        if(movingRepositoryJPA.addFilesToMoving(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка добавления файла", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PostMapping("/api/auth/saveSettingsMoving")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsMoving(@RequestBody SettingsMovingForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsMoving: " + request.toString());

        if(movingRepositoryJPA.saveSettingsMoving(request)){
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsMoving",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsMoving()
    {
        logger.info("Processing get request for path /api/auth/getSettingsMoving without request parameters");
        SettingsMovingJSON response;
        try {
            response=movingRepositoryJPA.getSettingsMoving();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(
            value = "/api/auth/getMovingLinkedDocsList",
            params = {"id","docName"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getMovingLinkedDocsList(
            @RequestParam("id") Long id, @RequestParam("docName") String docName) {//передали сюда id документа и имя таблицы
        logger.info("Processing get request for path api/auth/getMovingLinkedDocsList with parameters: " + "id: " + id+ ", docName: "+docName);
        List<LinkedDocsJSON> returnList;
        returnList = movingRepositoryJPA.getMovingLinkedDocsList(id,docName);
        if(!Objects.isNull(returnList)){
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка при загрузке списка связанных документов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/setMovingAsDecompleted")
    public ResponseEntity<?> setMovingAsDecompleted(@RequestBody MovingForm request){
        logger.info("Processing post request for path /api/auth/setMovingAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(movingRepositoryJPA.setMovingAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setMovingAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
