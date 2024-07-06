package com.dokio.controller.Sprav;

import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.SpravResourceForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.repository.SpravResourceRepositoryJPA;
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
public class SpravResourceController {

    Logger logger = Logger.getLogger("ResourceController");

    @Autowired
    SpravResourceRepositoryJPA spravResourceRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getResourceTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getResourceTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getResourceTable: " + searchRequest.toString());
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
        return new ResponseEntity<List>(spravResourceRepository.getResourceTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getResourcePagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getResourcePagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getResourcePagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = spravResourceRepository.getResourceSize(searchString, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getResourceValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getResourceValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getResourceValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(spravResourceRepository.getResourceValues(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getResourceValuesById error", e);
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/insertResource")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertResource(@RequestBody SpravResourceForm request){
        logger.info("Processing post request for path /api/auth/insertResource: " + request.toString());
        try {return new ResponseEntity<>(spravResourceRepository.insertResource(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertResource error", e);
            return new ResponseEntity<>("Error creating the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateResource")
    public ResponseEntity<?> updateResource(@RequestBody SpravResourceForm request){
        logger.info("Processing post request for path /api/auth/updateResource: " + request.toString());
        try {return new ResponseEntity<>(spravResourceRepository.updateResource(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateResource error", e);
            return new ResponseEntity<>("Error saving the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/deleteResource")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteResource(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteResource: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(spravResourceRepository.deleteResource(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteResource error", e);
            return new ResponseEntity<>("Error deleting the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteResource")
    public  ResponseEntity<?> undeleteResource(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteResource: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(spravResourceRepository.undeleteResource(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller undeleteResource error", e);
            return new ResponseEntity<>("Document recovery error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
//    @PostMapping("/api/auth/getNowUsedResourcesList")
//    public  ResponseEntity<?> getNowUsedResourcesList(@RequestBody AppointmentMainInfoForm request) {
//        logger.info("Processing post request for path /api/auth/getNowUsedResourcesList: " + request.toString());
//        try {return new ResponseEntity<>(spravResourceRepository.getNowUsedResourcesList(request), HttpStatus.OK);}
//        catch (Exception e){logger.error("Controller getNowUsedResourcesList error", e);
//            return new ResponseEntity<>("Controller getNowUsedResourcesList error", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
    @RequestMapping(
            value = "/api/auth/getResourcesList",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getResourcesList( @RequestParam("company_id") Long companyId) {
        logger.info("Processing get request for path /api/auth/getResourcesList with company_id=" + companyId.toString());
        try {return new ResponseEntity<>(spravResourceRepository.getResourcesList(companyId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getResourcesList error with company_id=" + companyId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
