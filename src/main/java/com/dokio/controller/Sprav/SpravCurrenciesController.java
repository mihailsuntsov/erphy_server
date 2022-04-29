package com.dokio.controller.Sprav;

import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.SpravCurrenciesForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.repository.SpravCurrenciesRepository;
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
public class SpravCurrenciesController {

    Logger logger = Logger.getLogger("CurrenciesController");

    @Autowired
    SpravCurrenciesRepository spravCurrenciesRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getCurrenciesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCurrenciesTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getCurrenciesTable: " + searchRequest.toString());
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
        return new ResponseEntity<List>(spravCurrenciesRepository.getCurrenciesTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getCurrenciesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCurrenciesPagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getCurrenciesPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = spravCurrenciesRepository.getCurrenciesSize(searchString, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getCurrenciesValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getCurrenciesValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getCurrenciesValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(spravCurrenciesRepository.getCurrenciesValues(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getCurrenciesValuesById error", e);
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/insertCurrencies")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertCurrencies(@RequestBody SpravCurrenciesForm request){
        logger.info("Processing post request for path /api/auth/insertCurrencies: " + request.toString());
        try {return new ResponseEntity<>(spravCurrenciesRepository.insertCurrencies(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertCurrencies error", e);
            return new ResponseEntity<>("Error creating the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateCurrencies")
    public ResponseEntity<?> updateCurrencies(@RequestBody SpravCurrenciesForm request){
        logger.info("Processing post request for path /api/auth/updateCurrencies: " + request.toString());
        try {return new ResponseEntity<>(spravCurrenciesRepository.updateCurrencies(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateCurrencies error", e);
            return new ResponseEntity<>("Error saving the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/deleteCurrencies")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteCurrencies(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteCurrencies: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(spravCurrenciesRepository.deleteCurrencies(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteCurrencies error", e);
            return new ResponseEntity<>("Error deleting the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteCurrencies")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteCurrencies(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteCurrencies: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(spravCurrenciesRepository.undeleteCurrencies(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller undeleteCurrencies error", e);
            return new ResponseEntity<>("Document recovery error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/setDefaultCurrency")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> setDefaultCurrency(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/setDefaultCurrency: " + request.toString());
        try {return new ResponseEntity<>(spravCurrenciesRepository.setDefaultCurrency(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller setDefaultCurrency error", e);
            return new ResponseEntity<>("Operation error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
