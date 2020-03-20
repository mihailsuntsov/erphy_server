package com.laniakea.controller.Sprav;

import com.laniakea.message.request.SearchForm;
import com.laniakea.message.request.SignUpForm;
import com.laniakea.message.request.Sprav.SpravSysEdizmForm;
import com.laniakea.message.request.UniversalForm;
import com.laniakea.message.response.Sprav.SpravSysEdizmJSON;
import com.laniakea.message.response.Sprav.SpravSysEdizmTableJSON;
import com.laniakea.repository.CompanyRepositoryJPA;
import com.laniakea.repository.SpravSysEdizmJPA;
import com.laniakea.repository.UserRepository;
import com.laniakea.repository.UserRepositoryJPA;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SpravSysEdizmController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    SpravSysEdizmJPA spravSysEdizmRepositoryJPA;

    @PostMapping("/api/auth/getSpravSysEdizmTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysEdizmTable(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<SpravSysEdizmTableJSON> returnList;

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
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = spravSysEdizmRepositoryJPA.getSpravSysEdizmTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getSpravSysEdizmPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysEdizmPagesList(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int disabledLINK;// номер страницы на паджинейшене, на которой мы сейчас. Изначально это 1.
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
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
        int size = spravSysEdizmRepositoryJPA.getSpravSysEdizmSize(searchString,companyId);//  - общее количество записей выборки
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

    @PostMapping("/api/auth/getSpravSysEdizmValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysEdizmValuesById(@RequestBody SearchForm request) {
        SpravSysEdizmJSON response;
        int id = request.getId();
        response=spravSysEdizmRepositoryJPA.getSpravSysEdizmValuesById(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<SpravSysEdizmJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/insertSpravSysEdizm")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertSpravSysEdizm(@RequestBody SpravSysEdizmForm request) throws ParseException {
        Long newDocument = spravSysEdizmRepositoryJPA.insertSpravSysEdizm(request);
        if(newDocument!=null && newDocument>0){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/updateSpravSysEdizm")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateSpravSysEdizm(@RequestBody SpravSysEdizmForm request) throws ParseException{
        if(spravSysEdizmRepositoryJPA.updateSpravSysEdizm(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteSpravSysEdizm")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteSpravSysEdizm(@RequestBody SignUpForm request) throws ParseException{
        String checked = request.getChecked() == null ? "": request.getChecked();
        checked=checked.replace("[","");
        checked=checked.replace("]","");

        if(spravSysEdizmRepositoryJPA.deleteSpravSysEdizmById(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/getSpravSysEdizm")
    public ResponseEntity<?> getSpravSysEdizm(@RequestBody UniversalForm request){
        List<SpravSysEdizmTableJSON> returnList;
        try {
            returnList = spravSysEdizmRepositoryJPA.getSpravSysEdizm(request);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            e.printStackTrace();
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

}
