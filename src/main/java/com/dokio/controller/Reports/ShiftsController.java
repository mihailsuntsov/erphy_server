package com.dokio.controller.Reports;

import com.dokio.message.request.Reports.ShiftSearchForm;
import com.dokio.message.response.Reports.ShiftsJSON;
import com.dokio.repository.ShiftsRepository;
import com.dokio.util.CommonUtilites;
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
public class ShiftsController {

    private Logger logger = Logger.getLogger("ShiftsController");

    @Autowired
    ShiftsRepository shiftsRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getShiftsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getShiftsTable(@RequestBody ShiftSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getShiftsTable: " + searchRequest.toString());
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "name";
            sortAsc = "asc";
        }
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int offsetreal = offset * result;//создана переменная с номером страницы
        List<ShiftsJSON> returnList = shiftsRepository.getShiftsTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getDepartmentId(),searchRequest.getCashierId(),searchRequest.getKassaId(),searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }

    @PostMapping("/api/auth/getShiftsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getShiftsPagesList(@RequestBody ShiftSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getShiftsPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = shiftsRepository.getShiftsSize(result, searchString, searchRequest.getCompanyId(),searchRequest.getDepartmentId(),searchRequest.getCashierId(),searchRequest.getKassaId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }

    // Возвращает список всех пользователей, работавших с кассой под своей учеткой
    @RequestMapping(
            value = "/api/auth/getShiftsKassa",
            params = {"company_id","department_id","docName"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getShiftsKassaList(
            @RequestParam("company_id") Long company_id,
            @RequestParam("docName") String docName,
            @RequestParam("department_id") Long department_id){
        logger.info("Processing get request for path /api/auth/getShiftsKassa with parameters: " + "company_id: " + company_id + ", department_id = "+ department_id + ", docName = "+ docName);
        try {return new ResponseEntity<>(shiftsRepository.getShiftsKassa(company_id, department_id, docName), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getShiftsKassa error", e);
            return new ResponseEntity<>("Ошибка загрузки списка касс ККМ", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    // Возвращает список всех пользователей, работавших с кассой под своей учеткой
    @RequestMapping(
            value = "/api/auth/getShiftsCashiers",
            params = {"company_id","department_id","docName"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getShiftsCashiersList(
            @RequestParam("company_id") Long company_id,
            @RequestParam("docName") String docName,
            @RequestParam("department_id") Long department_id){
        logger.info("Processing get request for path /api/auth/getShiftsCashiers with parameters: " + "company_id: " + company_id + ", department_id = "+ department_id+ ", docName = "+ docName);
        try {return new ResponseEntity<>(shiftsRepository.getShiftsCashiers(company_id, department_id, docName), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getShiftsCashiersList error", e);
            return new ResponseEntity<>("Ошибка загрузки списка кассиров", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
