/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.controller;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.TradeResultsForm;
import com.dokio.message.response.TradeResultsJSON;
import com.dokio.message.response.TradeResultsSumByPeriodJSON;
import com.dokio.message.response.TradeResultsTableJSON;
import com.dokio.message.response.TradeResultsTableReportJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.dokio.message.request.SearchForm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TradeResultsController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DocumentsRepositoryJPA documentsRepositoryJPA;
    @Autowired
    TradeResultsRepositoryJPA tradeResultsRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;


    @PostMapping("/api/auth/getTradeResultsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTradeResultsTable(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем
        int departmentId;//по какому отделению показывать / 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<TradeResultsTableJSON> returnList;

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
        returnList = tradeResultsRepositoryJPA.getTradeResultsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, departmentId);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getTradeResultsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTradeResultsPagesList(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int departmentId;//по какому отделению показывать документы/ 0 - по всем
        int disabledLINK;// номер страницы на паджинейшене, на которой мы сейчас. Изначально это 1.
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        departmentId = Integer.parseInt(searchRequest.getDepartmentId());
        String sortColumn = searchRequest.getSortColumn();

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = tradeResultsRepositoryJPA.getTradeResultsSize(searchString,companyId,departmentId);//  - общее количество записей выборки
        int offsetreal = offset * result;//создана переменная с номером страницы
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
    //Отдает ЗНАЧЕНИЯ из таблицы traderesults по id
    @PostMapping("/api/auth/getTradeResultsValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTradeResultsValuesById(@RequestBody SearchForm request) {
        TradeResultsJSON response;
        int id = request.getId();
        response=tradeResultsRepositoryJPA.getTradeResultsValuesById(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<TradeResultsJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/insertTradeResults")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertTradeResults(@RequestBody TradeResultsForm request) throws ParseException {
        Long newDocument = tradeResultsRepositoryJPA.insertTradeResults(request);
        if(newDocument!=null && newDocument>0){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/updateTradeResults")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateTradeResults(@RequestBody TradeResultsForm request) throws ParseException{
        if(tradeResultsRepositoryJPA.updateTradeResults(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteTradeResults")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteTradeResults(@RequestBody SignUpForm request) throws ParseException{
        String checked = request.getChecked() == null ? "": request.getChecked();
        checked=checked.replace("[","");
        checked=checked.replace("]","");

        if(tradeResultsRepositoryJPA.deleteTradeResultsById(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getTradeResultsTableReport")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTradeResultsTableReport(@RequestBody SearchForm searchRequest) {
        int companyId;//по какому предприятию показывать / 0 - по всем
        int departmentId;//по какому отделению показывать / 0 - по всем
        int employeeId;//по какому сотруднику показывать / 0 - по всем

        String dateFrom;//с какой даты
        String dateTo;//по какую дату (включительно)

        List<TradeResultsTableReportJSON> returnList;

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
        if (searchRequest.getEmployeeId() != null && !searchRequest.getEmployeeId().isEmpty() && searchRequest.getEmployeeId().trim().length() > 0) {
            employeeId = Integer.parseInt(searchRequest.getEmployeeId());
        } else {
            employeeId = 0;
        }

        if (searchRequest.getDateFrom() != null && !searchRequest.getDateFrom().isEmpty() && searchRequest.getDateFrom().trim().length() > 0) {
            dateFrom = searchRequest.getDateFrom();
        } else dateFrom = "01.01.1970";


        if (searchRequest.getDateTo() != null && !searchRequest.getDateTo().isEmpty() && searchRequest.getDateTo().trim().length() > 0)
        {
            dateTo = searchRequest.getDateTo();
        } else {
            long curTime = System.currentTimeMillis();
            String pattern = "dd.MM.yyyy";
            DateFormat df = new SimpleDateFormat(pattern);
            dateTo = df.format(curTime);
        }
        returnList = tradeResultsRepositoryJPA.getTradeResultsTableReport(companyId, departmentId, employeeId, dateFrom, dateTo);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getTradeResultsSumByPeriod")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTradeResultsSumByPeriod(@RequestBody SearchForm searchRequest) {
        int companyId;//по какому предприятию показывать / 0 - по всем
        int departmentId;//по какому отделению показывать / 0 - по всем
        int employeeId;//по какому сотруднику показывать / 0 - по всем

        String dateFrom;//с какой даты
        String dateTo;//по какую дату (включительно)

        TradeResultsSumByPeriodJSON response;



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
        if (searchRequest.getEmployeeId() != null && !searchRequest.getEmployeeId().isEmpty() && searchRequest.getEmployeeId().trim().length() > 0) {
            employeeId = Integer.parseInt(searchRequest.getEmployeeId());
        } else {
            employeeId = 0;
        }

        if (searchRequest.getDateFrom() != null && !searchRequest.getDateFrom().isEmpty() && searchRequest.getDateFrom().trim().length() > 0) {
            dateFrom = searchRequest.getDateFrom();
        } else dateFrom = "01.01.1970";


        if (searchRequest.getDateTo() != null && !searchRequest.getDateTo().isEmpty() && searchRequest.getDateTo().trim().length() > 0)
        {
            dateTo = searchRequest.getDateTo();
        } else {
            long curTime = System.currentTimeMillis();
            String pattern = "dd.MM.yyyy";
            DateFormat df = new SimpleDateFormat(pattern);
            dateTo = df.format(curTime);
        }

        response=tradeResultsRepositoryJPA.getTradeResultsSumByPeriod(companyId, departmentId, employeeId, dateFrom, dateTo);
        ResponseEntity<TradeResultsSumByPeriodJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
}
