package com.dokio.controller.Reports;

import com.dokio.message.request.Reports.ShiftSearchForm;
import com.dokio.message.response.additional.ShiftsJSON;
import com.dokio.repository.ShiftsRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ShiftsController {

    Logger logger = Logger.getLogger("ShiftsController");

    @Autowired
    ShiftsRepository shiftsRepository;



    @PostMapping("/api/auth/getShiftsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getShiftsTable(@RequestBody ShiftSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getShiftsTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<ShiftsJSON> returnList;

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
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = shiftsRepository.getShiftsTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getDepartmentId(),searchRequest.getCashierId(),searchRequest.getKassaId(),searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getShiftsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getShiftsPagesList(@RequestBody ShiftSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getShiftsPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        String searchString = searchRequest.getSearchString();

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = shiftsRepository.getShiftsSize(result, searchString, searchRequest.getCompanyId(),searchRequest.getDepartmentId(),searchRequest.getCashierId(),searchRequest.getKassaId(),searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getShiftsKassa error", e);
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getShiftsCashiersList error", e);
            return new ResponseEntity<>("Ошибка загрузки списка кассиров", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
