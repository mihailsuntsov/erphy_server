package com.dokio.controller;
import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.AppointmentsForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.response.Settings.SettingsAppointmentJSON;
import com.dokio.repository.*;
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
public class AppointmentsController {
    Logger logger = Logger.getLogger("AppointmentsController");

    @Autowired
    AppointmentRepositoryJPA appointmentRepositoryJPA;
    // связи для печатных форм
//    @Autowired
//    private TemplatesService tservice;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA company;
    @Autowired
    CommonUtilites commonUtilites;


    @PostMapping("/api/auth/getAppointmentTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getAppointmentTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getAppointmentTable: " + searchRequest.toString());
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
        return new ResponseEntity<List>(appointmentRepositoryJPA.getAppointmentsTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(), searchRequest.getDepartmentId(),searchRequest.getFilterOptionsIds()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getAppointmentPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getAppointmentPagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getAppointmentPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = appointmentRepositoryJPA.getAppointmentsSize(searchString, searchRequest.getCompanyId(), searchRequest.getDepartmentId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getAppointmentValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getAppointmentValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getAppointmentValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getAppointmentsValuesById(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getAppointmentValuesById error", e);
            return new ResponseEntity<>("Controller getAppointmentValuesById error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getListOfAppointmentFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfAppointmentFiles(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getListOfAppointmentFiles with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getListOfAppointmentFiles(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getListOfAppointmentFiles error", e);
            return new ResponseEntity<>("Controller getListOfAppointmentFiles error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/insertAppointment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/insertAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.insertAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertAppointment error", e);
            return new ResponseEntity<>("Controller insertAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateAppointment")
    public ResponseEntity<?> updateAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/updateAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.updateAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateAppointment error", e);
            return new ResponseEntity<>("Controller updateAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/deleteAppointment")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteAppointment(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteAppointment: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(appointmentRepositoryJPA.deleteAppointments(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteAppointment error", e);
            return new ResponseEntity<>("Controller deleteAppointment error",  HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteAppointment")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteAppointment(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteAppointment: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(appointmentRepositoryJPA.undeleteAppointments(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller undeleteAppointment error", e);
            return new ResponseEntity<>("Controller undeleteAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/setAppointmentAsDecompleted")
    public ResponseEntity<?> setCustomersOrdersAsDecompleted(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/setCustomersOrdersAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.setAppointmentAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setCustomersOrdersAsDecompleted error", e);
            return new ResponseEntity<>("Controller setAppointmentAsDecompleted exception", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getSettingsAppointments",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsAppointments()
    {   logger.info("Processing get request for path /api/auth/getSettingsAppointments without request parameters");
        try{    return new ResponseEntity<>(appointmentRepositoryJPA.getSettingsAppointments(), HttpStatus.OK);}
        catch (Exception e) {e.printStackTrace();logger.error("Controller getSettingsAppointments exception", e);
            return new ResponseEntity<>("Controller getSettingsAppointments exception", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/addFilesToAppointment")
    public ResponseEntity<?> addFilesToProduct(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/addFilesToAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.addFilesToAppointment(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller addFilesToAppointment error", e);
            return new ResponseEntity<>("Controller addFilesToAppointment exception", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getAppointmentServicesSearchList")
    public  ResponseEntity<?> getAppointmentServicesList(@RequestBody AppointmentMainInfoForm request) {
        logger.info("Processing post request for path /api/auth/getAppointmentServicesSearchList: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getAppointmentServicesSearchList(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getAppointmentServicesSearchList error", e);
            return new ResponseEntity<>("Controller getAppointmentServicesSearchList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/deleteAppointmentFile",
            params = {"doc_id", "file_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteAppointmentFile( @RequestParam("doc_id") Long doc_id, @RequestParam("file_id") Long file_id) {
        logger.info("Processing get request for path /api/auth/deleteAppointmentFile with file_id=" + file_id.toString() + ", doc_id = " + doc_id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.deleteAppointmentFile(file_id,doc_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller deleteAppointmentFile error with file_id=" + file_id.toString() + ", doc_id=" + doc_id.toString(), e);
            return new ResponseEntity<>("Controller deleteAppointmentFile error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
