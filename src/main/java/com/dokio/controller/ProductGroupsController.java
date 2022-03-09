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
import com.dokio.message.request.*;
import com.dokio.message.response.*;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
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
public class ProductGroupsController {
    Logger logger = Logger.getLogger("ProductGroupsController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;
    @Autowired
    ProductGroupRepositoryJPA productGroupRepositoryJPA;

    @PostMapping("/api/auth/getProductGroupsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductGroupsTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductGroupsTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<ProductGroupsTableJSON> returnList;

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
        returnList = productGroupRepositoryJPA.getProductGroupsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getProductGroupsList")//заполнение Autocomplete для поля "Группа товаров" документа "Товары и услуги"
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductGroupsList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductGroupsList: " + searchRequest.toString());

        int companyId;

        String searchString = searchRequest.getSearchString();

        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Integer.parseInt(searchRequest.getCompanyId());
        } else { return null; }

        List<ProductGroupsListJSON> returnList;
        returnList = productGroupRepositoryJPA.getProductGroupsList(searchString, companyId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getProductFieldsValuesList")//заполнение Autocomplete для настраиваеммых полей во вкладке "Поля" документа "Товары и услуги"
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductFieldsValuesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductFieldsValuesList: " + searchRequest.toString());

        int fieldId = searchRequest.getId();
        String searchString = searchRequest.getSearchString();
        List<ProductFieldValuesListJSON> returnList;
        returnList = productGroupRepositoryJPA.getProductFieldsValuesList(searchString, fieldId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getProductGroupsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductGroupsPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductGroupsPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = productGroupRepositoryJPA.getProductGroupsSize(searchString,companyId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/getProductGroupValuesById")
    public ResponseEntity<?> getProductGroupValuesById(@RequestBody ProductGroupsForm request) {
        logger.info("Processing post request for path /api/auth/getProductGroupValuesById: " + request.toString());

        ProductGroupsJSON response;
        Long id = request.getId();
        response=productGroupRepositoryJPA.getProductGroupValuesById(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<ProductGroupsJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/insertProductGroups")
    public  ResponseEntity<?> insertProductGroups(@RequestBody ProductGroupsForm request) {
        logger.info("Processing post request for path /api/auth/insertProductGroups: " + request.toString());
        try {return new ResponseEntity<>(productGroupRepositoryJPA.insertProductGroups(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertProductGroups error", e);
            return new ResponseEntity<>("Ошибка", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateProductGroups")
    public  ResponseEntity<?> updateProductGroups(@RequestBody ProductGroupsForm request) {
        logger.info("Processing post request for path /api/auth/updateProductGroups: " + request.toString());
        try {return new ResponseEntity<>(productGroupRepositoryJPA.updateProductGroups(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateProductGroups error", e);
            return new ResponseEntity<>("Ошибка", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteProductGroups")
    public  ResponseEntity<?> deleteProductGroups(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteProductGroups: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(productGroupRepositoryJPA.deleteProductGroupsById(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteProductGroups error", e);
        return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteProductGroups")
    public  ResponseEntity<?> undeleteProductGroups(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteProductGroups: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(productGroupRepositoryJPA.undeleteProductGroups(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteProductGroups error", e);
            return new ResponseEntity<>("Ошибка восстановления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/copyProductGroups")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> copyProductGroups(@RequestBody ProductGroupsForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/copyProductGroups: " + request.toString());

        if(productGroupRepositoryJPA.copyProductGroups(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


}
