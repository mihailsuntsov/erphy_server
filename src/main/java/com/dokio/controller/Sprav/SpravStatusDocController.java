package com.dokio.controller.Sprav;

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.Sprav.SpravStatusDocForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Sprav.SpravStatusDocJSON;
import com.dokio.message.response.Sprav.SpravStatusListJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SpravStatusDocController {
    Logger logger = Logger.getLogger("SpravStatusDocController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    SpravStatusDocRepository repository;

    @PostMapping("/api/auth/getStatusDocsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getStatusDocsTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getStatusDocsTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        //int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем
        int documentId;//по какому документу показывать / 0 - ничего не показываем, пока не выберут
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<SpravStatusDocJSON> returnList;

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
        if (searchRequest.getDocumentId() != null && !searchRequest.getDocumentId().isEmpty() && searchRequest.getDocumentId().trim().length() > 0) {
            documentId = Integer.parseInt(searchRequest.getDocumentId());
        } else {
            documentId = 0;
        }
        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Integer.parseInt(searchRequest.getCompanyId());
        } else {
            companyId = 0;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = repository.getStatusDocsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, documentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getSpravStatusDocsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravStatusDocsPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getSpravStatusDocsPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int documentId;//по какому документу показывать / 0 - ничего не показываем, пока не выберут
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        documentId = Integer.parseInt(searchRequest.getDocumentId());

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = repository.getStatusDocsSize(searchString,companyId,documentId,searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/getSpravStatusDocsValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravStatusDocsValuesById(@RequestBody SearchForm request) {
        logger.info("Processing post request for path /api/auth/getSpravStatusDocsValuesById: " + request.toString());

        SpravStatusDocJSON response;
        int id = request.getId();
        response=repository.getStatusDocsValues(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<SpravStatusDocJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/setDefaultStatusDoc")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> setDefaultStatusDoc(@RequestBody UniversalForm request){
        if(repository.setDefaultStatusDoc(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка назначения статуса по-умолчанию", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/insertSpravStatusDocs")
    public  ResponseEntity<?> insertSpravStatusDocs(@RequestBody SpravStatusDocForm request) {
        logger.info("Processing post request for path /api/auth/insertSpravStatusDocs: " + request.toString());
        try {return new ResponseEntity<>(repository.insertStatusDocs(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertSpravStatusDocs error", e);
            return new ResponseEntity<>("Ошибка", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateSpravStatusDocs")
    public  ResponseEntity<?> updateSpravStatusDocs(@RequestBody SpravStatusDocForm request) {
        logger.info("Processing post request for path /api/auth/updateSpravStatusDocs: " + request.toString());
        try {return new ResponseEntity<>(repository.updateStatusDocs(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateSpravStatusDocs error", e);
            return new ResponseEntity<>("Ошибка", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteSpravStatusDocs")
    public  ResponseEntity<?> deleteSpravStatusDocs(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteSpravStatusDocs: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(repository.deleteStatusDocs(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteSpravStatusDocs error", e);
            return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteSpravStatusDocs")
    public  ResponseEntity<?> undeleteSpravStatusDocs(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteSpravStatusDocs: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(repository.undeleteStatusDocs(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteSpravStatusDocs error", e);
            return new ResponseEntity<>("Ошибка восстановления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/getStatusList")//отдает список статусов документа по его id (таблица documents) и id предприятия
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getStatusList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getStatusList: " + searchRequest.toString());

        int companyId;//по какому предприятию показывать
        int documentId;//по какому документу показывать
        List<SpravStatusListJSON> returnList;
        documentId = Integer.parseInt(searchRequest.getDocumentId());
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        returnList = repository.getStatusList(companyId, documentId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
}


