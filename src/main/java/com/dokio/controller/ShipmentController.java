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
import com.dokio.message.request.Settings.SettingsShipmentForm;
import com.dokio.message.response.ShipmentJSON;
import com.dokio.message.response.Settings.SettingsShipmentJSON;
import com.dokio.message.response.additional.*;
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
public class ShipmentController {

    Logger logger = Logger.getLogger("ShipmentController");

    @Autowired
    ShipmentRepositoryJPA shipmentRepository;

    @PostMapping("/api/auth/getShipmentTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getShipmentTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getShipmentTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<ShipmentJSON> returnList;

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
        returnList = shipmentRepository.getShipmentTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getShipmentProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getShipmentProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getShipmentProductTable with Shipment id=" + docId.toString());
        List<ShipmentProductTableJSON> returnList;
        try {
            returnList = shipmentRepository.getShipmentProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getShipmentPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getShipmentPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getShipmentPagesList: " + searchRequest.toString());

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
        int size = shipmentRepository.getShipmentSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertShipment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertShipment(@RequestBody ShipmentForm request){
        logger.info("Processing post request for path /api/auth/insertShipment: " + request.toString());
        return new ResponseEntity<>(shipmentRepository.insertShipment(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getShipmentValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getShipmentValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getShipmentValuesById with parameters: " + "id: " + id);
        ShipmentJSON response;
        try {
            response=shipmentRepository.getShipmentValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getShipmentValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки значений документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/updateShipment")
    public ResponseEntity<?> updateShipment(@RequestBody ShipmentForm request){
        logger.info("Processing post request for path /api/auth/updateShipment: " + request.toString());
            return new ResponseEntity<>(shipmentRepository.updateShipment(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveSettingsShipment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsShipment(@RequestBody SettingsShipmentForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsShipment: " + request.toString());

        if(shipmentRepository.saveSettingsShipment(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Розничная продажа", HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsShipment",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsShipment()
    {
        logger.info("Processing get request for path /api/auth/getSettingsShipment without request parameters");
        SettingsShipmentJSON response;
        try {
            response=shipmentRepository.getSettingsShipment();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/api/auth/deleteShipment")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteShipment(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteShipment: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(shipmentRepository.deleteShipment(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteShipment")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteShipment(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteShipment: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        return new ResponseEntity<>(shipmentRepository.undeleteShipment(checked), HttpStatus.OK);
    }
}
