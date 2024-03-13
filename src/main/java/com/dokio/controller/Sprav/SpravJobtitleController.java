package com.dokio.controller.Sprav;

import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.SpravJobtitleForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.repository.SpravJobtitleRepositoryJPA;
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
public class SpravJobtitleController {

    Logger logger = Logger.getLogger("JobtitleController");

    @Autowired
    SpravJobtitleRepositoryJPA spravJobtitleRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getJobtitleTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getJobtitleTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getJobtitleTable: " + searchRequest.toString());
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
        return new ResponseEntity<List>(spravJobtitleRepository.getJobtitleTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getJobtitlePagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getJobtitlePagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getJobtitlePagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = spravJobtitleRepository.getJobtitleSize(searchString, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getJobtitleValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getJobtitleValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getJobtitleValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(spravJobtitleRepository.getJobtitleValues(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getJobtitleValuesById error", e);
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/insertJobtitle")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertJobtitle(@RequestBody SpravJobtitleForm request){
        logger.info("Processing post request for path /api/auth/insertJobtitle: " + request.toString());
        try {return new ResponseEntity<>(spravJobtitleRepository.insertJobtitle(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertJobtitle error", e);
            return new ResponseEntity<>("Error creating the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateJobtitle")
    public ResponseEntity<?> updateJobtitle(@RequestBody SpravJobtitleForm request){
        logger.info("Processing post request for path /api/auth/updateJobtitle: " + request.toString());
        try {return new ResponseEntity<>(spravJobtitleRepository.updateJobtitle(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateJobtitle error", e);
            return new ResponseEntity<>("Error saving the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/deleteJobtitle")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteJobtitle(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteJobtitle: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(spravJobtitleRepository.deleteJobtitle(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteJobtitle error", e);
            return new ResponseEntity<>("Error deleting the document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteJobtitle")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteJobtitle(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteJobtitle: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(spravJobtitleRepository.undeleteJobtitle(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller undeleteJobtitle error", e);
            return new ResponseEntity<>("Document recovery error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getJobtitlesList",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getJobtitlesList( @RequestParam("company_id") Long companyId) {
        logger.info("Processing get request for path /api/auth/getJobtitlesList with company_id=" + companyId.toString());
        try {return new ResponseEntity<>(spravJobtitleRepository.getJobtitleList(companyId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getJobtitlesList error with company_id=" + companyId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
