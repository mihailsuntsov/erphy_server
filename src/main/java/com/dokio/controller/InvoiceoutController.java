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
import com.dokio.message.request.Settings.SettingsInvoiceoutForm;
import com.dokio.message.response.InvoiceoutJSON;
import com.dokio.repository.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class InvoiceoutController {

    Logger logger = Logger.getLogger("InvoiceoutController");

    @Autowired
    InvoiceoutRepositoryJPA invoiceoutRepository;

    @PostMapping("/api/auth/getInvoiceoutTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInvoiceoutTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInvoiceoutTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<InvoiceoutJSON> returnList;

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
        returnList = invoiceoutRepository.getInvoiceoutTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getInvoiceoutPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInvoiceoutPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInvoiceoutPagesList: " + searchRequest.toString());

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
        int size = invoiceoutRepository.getInvoiceoutSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getInvoiceoutProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInvoiceoutProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getInvoiceoutProductTable with Invoiceout id=" + docId.toString());
        return  new ResponseEntity<>(invoiceoutRepository.getInvoiceoutProductTable(docId), HttpStatus.OK);
    }

    @PostMapping("/api/auth/insertInvoiceout")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertInvoiceout(@RequestBody InvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/insertInvoiceout: " + request.toString());
        return new ResponseEntity<>(invoiceoutRepository.insertInvoiceout(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getInvoiceoutValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInvoiceoutValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getInvoiceoutValuesById with parameters: " + "id: " + id);
        return new ResponseEntity<>(invoiceoutRepository.getInvoiceoutValuesById(id), HttpStatus.OK);
    }

    @PostMapping("/api/auth/updateInvoiceout")
    public ResponseEntity<?> updateInvoiceout(@RequestBody InvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/updateInvoiceout: " + request.toString());
        return new ResponseEntity<>(invoiceoutRepository.updateInvoiceout(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveSettingsInvoiceout")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsInvoiceout(@RequestBody SettingsInvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsInvoiceout: " + request.toString());
        if(invoiceoutRepository.saveSettingsInvoiceout(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Розничная продажа", HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsInvoiceout",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsInvoiceout(){
        logger.info("Processing get request for path /api/auth/getSettingsInvoiceout without request parameters");
            return new ResponseEntity<>(invoiceoutRepository.getSettingsInvoiceout(), HttpStatus.OK);
    }


    @PostMapping("/api/auth/deleteInvoiceout")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteInvoiceout(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteInvoiceout: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(invoiceoutRepository.deleteInvoiceout(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteInvoiceout")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteInvoiceout(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteInvoiceout: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        return new ResponseEntity<>(invoiceoutRepository.undeleteInvoiceout(checked), HttpStatus.OK);
    }
}