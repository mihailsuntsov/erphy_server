package com.dokio.controller.Sprav;

import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.ProductAttributeForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.repository.SpravProductAttributeRepository;
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
public class SpravProductAttributeController {

    Logger logger = Logger.getLogger("ProductAttributeController");

    @Autowired
    SpravProductAttributeRepository spravProductAttributeRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getProductAttributeTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductAttributeTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductAttributeTable: " + searchRequest.toString());
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
        return new ResponseEntity<List>(spravProductAttributeRepository.getProductAttributeTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getProductAttributePagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductAttributePagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductAttributePagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = spravProductAttributeRepository.getProductAttributeSize(searchString, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getProductAttributeValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductAttributeValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getProductAttributeValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(spravProductAttributeRepository.getProductAttributeValues(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getProductAttributeValuesById error", e);
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/insertProductAttribute")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertProductAttribute(@RequestBody ProductAttributeForm request){
        logger.info("Processing post request for path /api/auth/insertProductAttribute: " + request.toString());
        try {
            Long result = spravProductAttributeRepository.insertProductAttribute(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e){logger.error("Controller insertProductAttribute error", e);
            return new ResponseEntity<>("Error creating the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateProductAttribute")
    public ResponseEntity<?> updateProductAttribute(@RequestBody ProductAttributeForm request){
        logger.info("Processing post request for path /api/auth/updateProductAttribute: " + request.toString());
        try {return new ResponseEntity<>(spravProductAttributeRepository.updateProductAttribute(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateProductAttribute error", e);
            return new ResponseEntity<>("Error saving the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/deleteProductAttribute")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteProductAttribute(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteProductAttribute: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(spravProductAttributeRepository.deleteProductAttribute(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteProductAttribute error", e);
            return new ResponseEntity<>("Error deleting the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteProductAttribute")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteProductAttribute(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteProductAttribute: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(spravProductAttributeRepository.undeleteProductAttribute(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller undeleteProductAttribute error", e);
            return new ResponseEntity<>("Document recovery error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
