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
package com.dokio.controller.Sprav;

import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.SpravTaxesForm;
import com.dokio.message.request.Sprav.TaxesTableSearchForm;
import com.dokio.repository.SpravTaxesRepository;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

@Controller
class SpravTaxesController {

    Logger logger = Logger.getLogger("SpravTaxesController");

    @Autowired
    SpravTaxesRepository taxesRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getTaxesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTaxesTable(@RequestBody TaxesTableSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getTaxesTable: " + searchRequest.toString());
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "date_created";
            sortAsc = "asc";
        }
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int offsetreal = offset * result;//создана переменная с номером страницы
        return new ResponseEntity<List>(taxesRepository.getTaxesTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(), searchRequest.getFilterOptionsIds()), HttpStatus.OK);
    }

    @PostMapping("/api/auth/getTaxesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTaxesPagesList(@RequestBody TaxesTableSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getTaxesPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = taxesRepository.getTaxesSize(searchString, searchRequest.getCompanyId(), searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }



    @PostMapping("/api/auth/insertTaxes")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertTaxes(@RequestBody SpravTaxesForm request){
        logger.info("Processing post request for path /api/auth/insertTaxes: " + request.toString());
        return new ResponseEntity<>(taxesRepository.insertTaxes(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getTaxesValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getTaxesValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getTaxesValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(taxesRepository.getTaxesValues(id), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateTaxes")
    public ResponseEntity<?> updateTaxes(@RequestBody SpravTaxesForm request){
        logger.info("Processing post request for path /api/auth/updateTaxes: " + request.toString());
        try {return new ResponseEntity<>(taxesRepository.updateTaxes(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error saving document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteTaxes")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteTaxes(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteTaxes: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(taxesRepository.deleteTaxes(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Deletion error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/undeleteTaxes")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteTaxes(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteTaxes: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(taxesRepository.undeleteTaxes(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Restore error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getTaxesList",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getTaxesList(
            @RequestParam("company_id") Long company_id){
        logger.info("Processing get request for path /api/auth/getTaxesList with parameters: " + "company_id: " + company_id);
        try {return new ResponseEntity<>(taxesRepository.getTaxesList(company_id), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }



}
