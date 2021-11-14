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

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsVatinvoiceinForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.request.VatinvoiceinForm;
import com.dokio.message.response.VatinvoiceinJSON;
import com.dokio.repository.VatinvoiceinRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class VatinvoiceinController {

    Logger logger = Logger.getLogger("VatinvoiceinController");

    @Autowired
    VatinvoiceinRepositoryJPA vatinvoiceinRepository;

    @PostMapping("/api/auth/getVatinvoiceinTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getVatinvoiceinTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getVatinvoiceinTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<VatinvoiceinJSON> returnList;

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
        returnList = vatinvoiceinRepository.getVatinvoiceinTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getVatinvoiceinPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getVatinvoiceinPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getVatinvoiceinPagesList: " + searchRequest.toString());

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
        int size = vatinvoiceinRepository.getVatinvoiceinSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertVatinvoicein")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertVatinvoicein(@RequestBody VatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/insertVatinvoicein: " + request.toString());
        return new ResponseEntity<>(vatinvoiceinRepository.insertVatinvoicein(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getVatinvoiceinValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getVatinvoiceinValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getVatinvoiceinValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(vatinvoiceinRepository.getVatinvoiceinValuesById(id), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка загрузки значений документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateVatinvoicein")
    public ResponseEntity<?> updateVatinvoicein(@RequestBody VatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/updateVatinvoicein: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.updateVatinvoicein(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка сохранения документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/saveSettingsVatinvoicein")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsVatinvoicein(@RequestBody SettingsVatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsVatinvoicein: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.saveSettingsVatinvoicein(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка сохранения настроек для документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsVatinvoicein",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsVatinvoicein(){
        logger.info("Processing get request for path /api/auth/getSettingsVatinvoicein without request parameters");
        try {return new ResponseEntity<>(vatinvoiceinRepository.getSettingsVatinvoicein(), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка загрузки настроек", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteVatinvoicein")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteVatinvoicein(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteVatinvoicein: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(vatinvoiceinRepository.deleteVatinvoicein(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/undeleteVatinvoicein")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteVatinvoicein(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteVatinvoicein: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(vatinvoiceinRepository.undeleteVatinvoicein(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка восстановления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getListOfVatinvoiceinFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfVatinvoiceinFiles(
            @RequestParam("id") Long id){
        logger.info("Processing post request for path api/auth/getListOfVatinvoiceinFiles: " + id);
        try {return new ResponseEntity<>(vatinvoiceinRepository.getListOfVatinvoiceinFiles(id), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteVatinvoiceinFile")
    public ResponseEntity<?> deleteVatinvoiceinFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteVatinvoiceinFile: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.deleteVatinvoiceinFile(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка удаления файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToVatinvoicein")
    public ResponseEntity<?> addFilesToVatinvoicein(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToVatinvoicein: " + request.toString());
        try{return new ResponseEntity<>(vatinvoiceinRepository.addFilesToVatinvoicein(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
