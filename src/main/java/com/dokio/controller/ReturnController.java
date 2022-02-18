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
import com.dokio.message.request.Settings.SettingsReturnForm;
import com.dokio.message.response.additional.FilesReturnJSON;
import com.dokio.message.response.ReturnJSON;
import com.dokio.message.response.ReturnProductTableJSON;
import com.dokio.message.response.Settings.SettingsReturnJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.repository.*;
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
public class ReturnController {


    Logger logger = Logger.getLogger("ReturnController");

    @Autowired
    ReturnRepository returnRepository;


    @PostMapping("/api/auth/getReturnTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getReturnTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getReturnTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<ReturnJSON> returnList;

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
        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Integer.parseInt(searchRequest.getCompanyId());
        } else {
            companyId = 0;
        }
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
        } else {
            departmentId = 0;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = returnRepository.getReturnTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getReturnProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getReturnProductTable with Return id=" + docId.toString());
        List<ReturnProductTableJSON> returnList;
        try {
            returnList = returnRepository.getReturnProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getReturnPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getReturnPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getReturnPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int departmentId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
        } else {
            departmentId = 0;}
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = returnRepository.getReturnSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertReturn")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertReturn(@RequestBody ReturnForm request) {
        logger.info("Processing post request for path /api/auth/insertReturn: " + request.toString());

        Long newDocument = returnRepository.insertReturn(request);
        if(newDocument!=null){//вернет id созданного документа либо 0, если недостаточно прав
            return new ResponseEntity<>(String.valueOf(newDocument), HttpStatus.OK);
        } else {//если null - значит на одной из стадий сохранения произошла ошибка
            return new ResponseEntity<>("Ошибка создания документа Возврат покупателя", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getReturnValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getReturnValuesById with parameters: " + "id: " + id);
        ReturnJSON response;
        try {
            response=returnRepository.getReturnValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getReturnValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки значений документа Возврат покупателя", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getReturnLinkedDocsList",
            params = {"id","docName"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnLinkedDocsList(
            @RequestParam("id") Long id, @RequestParam("docName") String docName) {//передали сюда id документа и имя таблицы
        logger.info("Processing get request for path api/auth/getReturnLinkedDocsList with parameters: " + "id: " + id+ ", docName: "+docName);
        List<LinkedDocsJSON> returnList;
        returnList = returnRepository.getReturnLinkedDocsList(id,docName);
        if(!Objects.isNull(returnList)){
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка при загрузке списка связанных документов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/updateReturn")
    public ResponseEntity<?> updateReturn(@RequestBody ReturnForm request){
        logger.info("Processing post request for path /api/auth/updateReturn: " + request.toString());
        return new ResponseEntity<>(returnRepository.updateReturn(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveSettingsReturn")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsReturn(@RequestBody SettingsReturnForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsReturn: " + request.toString());

        if(returnRepository.saveSettingsReturn(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Возврат покупателя", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsReturn",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsReturn()
    {
        logger.info("Processing get request for path /api/auth/getSettingsReturn without request parameters");
        SettingsReturnJSON response;
        try {
            response=returnRepository.getSettingsReturn();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для документа Возврат покупателя", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteReturn")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteReturn(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteReturn: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        Boolean result=returnRepository.deleteReturn(checked);
        if(!Objects.isNull(result)){//вернет true - ок, false - недостаточно прав,  null - ошибка
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/undeleteReturn")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteReturn(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteReturn: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(returnRepository.undeleteReturn(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка восстановления документов", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @RequestMapping(
            value = "/api/auth/getReturnProductsList",
            params = {"searchString", "companyId", "departmentId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnProductsList(
            @RequestParam("searchString")   String searchString,
            @RequestParam("companyId")      Long companyId,
            @RequestParam("departmentId")   Long departmentId)
    {
        logger.info("Processing post request for path /api/auth/getReturnProductsList with parameters: " +
                "  searchString: "  + searchString +
                ", companyId: "     + companyId.toString() +
                ", departmentId: "  + departmentId.toString());
        List returnList;
        returnList = returnRepository.getReturnProductsList(searchString, companyId, departmentId);
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/deleteReturnProductTableRow",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteCustomersOrdersProductTableRow(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/deleteReturnProductTableRow with parameters: " +
                "id: " + id);
        boolean result;
        try {
            result=returnRepository.deleteReturnProductTableRow(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getListOfReturnFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfReturnFiles(@RequestBody SearchForm request)  {
        logger.info("Processing post request for path api/auth/getListOfReturnFiles: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<FilesReturnJSON> returnList;
        try {
            returnList = returnRepository.getListOfReturnFiles(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteReturnFile")
    public ResponseEntity<?> deleteReturnFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteReturnFile: " + request.toString());

        if(returnRepository.deleteReturnFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка удаления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/setReturnAsDecompleted")
    public ResponseEntity<?> setReturnAsDecompleted(@RequestBody ReturnForm request){
        logger.info("Processing post request for path /api/auth/setReturnAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(returnRepository.setReturnAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setReturnAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToReturn")
    public ResponseEntity<?> addFilesToReturn(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToReturn: " + request.toString());

        if(returnRepository.addFilesToReturn(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}




