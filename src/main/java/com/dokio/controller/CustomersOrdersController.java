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
import com.dokio.message.request.Settings.SettingsCustomersOrdersForm;
import com.dokio.message.response.CustomersOrdersJSON;
//import com.dokio.message.response.FilesCustomersOrdersJSON;
import com.dokio.message.response.Settings.SettingsCustomersOrdersJSON;
import com.dokio.message.response.additional.*;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CustomersOrdersController {
    Logger logger = Logger.getLogger("CustomersOrdersController");

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
    CustomersOrdersRepositoryJPA customersOrdersRepositoryJPA;
    @Autowired
    StorageService storageService;

    @PostMapping("/api/auth/getCustomersOrdersTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCustomersOrdersTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getCustomersOrdersTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<CustomersOrdersJSON> returnList;

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
        returnList = customersOrdersRepositoryJPA.getCustomersOrdersTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getCustomersOrdersProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getCustomersOrdersProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getCustomersOrdersProductTable with CustomersOrders id=" + docId.toString());
        List<CustomersOrdersProductTableJSON> returnList;
        try {
            returnList = customersOrdersRepositoryJPA.getCustomersOrdersProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/api/auth/getCustomersOrdersPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCustomersOrdersPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getCustomersOrdersPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int departmentId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
        } else {
            departmentId = 0;}
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = customersOrdersRepositoryJPA.getCustomersOrdersSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertCustomersOrders")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertCustomersOrders(@RequestBody CustomersOrdersForm request){
        logger.info("Processing post request for path /api/auth/insertCustomersOrders: " + request.toString());

        Long newDocument = customersOrdersRepositoryJPA.insertCustomersOrders(request);
        if(newDocument!=null && newDocument>0){
            return new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка создания документа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/isCustomersOrdersNumberUnical")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isCustomersOrdersNumberUnical(@RequestBody UniversalForm request) { // id1 - document_id, id2 - company_id
        logger.info("Processing post request for path /api/auth/isCustomersOrdersNumberUnical: " + request.toString());

        try {
            Boolean ret = customersOrdersRepositoryJPA.isCustomersOrdersNumberUnical(request);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getCustomersOrdersValuesById")
    public ResponseEntity<?> getProductGroupValuesById(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path /api/auth/getCustomersOrdersValuesById: " + request.toString());

        CustomersOrdersJSON response;
        Long id = request.getId();
        response=customersOrdersRepositoryJPA.getCustomersOrdersValuesById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping("/api/auth/updateCustomersOrders")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateCustomersOrders(@RequestBody CustomersOrdersForm request) throws Exception {
        logger.info("Processing post request for path /api/auth/updateCustomersOrders: " + request.toString());
        CustomersOrdersUpdateReportJSON updateResults = customersOrdersRepositoryJPA.updateCustomersOrders(request);
        if(updateResults.getSuccess()){
            return new ResponseEntity<>(updateResults, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/saveSettingsCustomersOrders")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsCustomersOrders(@RequestBody SettingsCustomersOrdersForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsCustomersOrders: " + request.toString());

        if(customersOrdersRepositoryJPA.saveSettingsCustomersOrders(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек", HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsCustomersOrders",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsCustomersOrders()
    {
        logger.info("Processing get request for path /api/auth/getSettingsCustomersOrders without request parameters");
        SettingsCustomersOrdersJSON response;
        try {
            response=customersOrdersRepositoryJPA.getSettingsCustomersOrders();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteCustomersOrders")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteCustomersOrders(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteCustomersOrders: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(customersOrdersRepositoryJPA.deleteCustomersOrders(checked)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/api/auth/undeleteCustomersOrders")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteCustomersOrders(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteCustomersOrders: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(customersOrdersRepositoryJPA.undeleteCustomersOrders(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка восстановления Заказа покупателя", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    //отдает таблицу Заказов покупателя с неотгруженными резервами по товару в требуемом отделении (или department_id=0 - во всех), за исключением документа document_id,  из которого выполняется запрос (document_id=0 - во всех документах)
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getReservesTable",
            params = {"product_id", "company_id", "document_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReservesTable(
            @RequestParam("product_id") Long product_id,
            @RequestParam("company_id") Long company_id,
            @RequestParam("document_id") Long document_id,
            @RequestParam("department_id") Long department_id)
    {
        logger.info("Processing get request for path /api/auth/getReservesTable with parameters: " +
                "product_id: " + product_id.toString() +
                ", company_id: " + company_id.toString() +
                ", department_id: " + department_id.toString() +
                ", document_id: "+ document_id.toString());
        List<CustomersOrdersReservesTable> returnList;
        try {
            returnList=customersOrdersRepositoryJPA.getReservesTable(company_id,department_id, product_id, document_id);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //отдает список отделений в виде их Id с доступным количеством и общим количеством товара в отделении
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductCount",
            params = {"product_id", "company_id", "document_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductCount(
            @RequestParam("product_id") Long product_id,
            @RequestParam("company_id") Long company_id,
            @RequestParam("document_id") Long document_id)
    {
        logger.info("Processing get request for path /api/auth/getProductCount with parameters: " +
                "product_id: " + product_id.toString() +
                ", company_id: " + company_id.toString() +
                ", document_id: "+ document_id.toString());
        List<IdAndCount> returnList;
        try {
            returnList=customersOrdersRepositoryJPA.getProductCount(product_id, company_id, document_id);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/deleteCustomersOrdersProductTableRow",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteCustomersOrdersProductTableRow(
            @RequestParam("id") Long id)
    {
        boolean result;
        try {
            result=customersOrdersRepositoryJPA.deleteCustomersOrdersProductTableRow(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //отдает краткую информацию о товаре
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductsPriceAndRemains",
            params = {"department_id", "product_id", "price_type_id", "document_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductsPriceAndRemains(
            @RequestParam("department_id") Long department_id,
            @RequestParam("product_id") Long product_id,
            @RequestParam("price_type_id") Long price_type_id,
            @RequestParam("document_id") Long document_id)
    {
        try {

            ProductsPriceAndRemainsJSON response=customersOrdersRepositoryJPA.getProductsPriceAndRemains(department_id,product_id,price_type_id,document_id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
/*
    @PostMapping("/api/auth/getListOfCustomersOrdersFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfCustomersOrdersFiles(@RequestBody SearchForm request)  {
        Long productId=Long.valueOf(request.getId());
        List<FilesCustomersOrdersJSON> returnList;
        try {
            returnList = customersOrdersRepositoryJPA.getListOfCustomersOrdersFiles(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteCustomersOrdersFile")
    public ResponseEntity<?> deleteCustomersOrdersFile(@RequestBody SearchForm request) {
        if(customersOrdersRepositoryJPA.deleteCustomersOrdersFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToCustomersOrders")
    public ResponseEntity<?> addFilesToCustomersOrders(@RequestBody UniversalForm request) {
        if(customersOrdersRepositoryJPA.addFilesToCustomersOrders(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }*/
}
