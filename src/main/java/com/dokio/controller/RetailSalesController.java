/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.controller;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsRetailSalesForm;
import com.dokio.message.response.RetailSalesJSON;
import com.dokio.message.response.Settings.SettingsRetailSalesJSON;
import com.dokio.message.response.additional.*;
import com.dokio.repository.*;
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
public class RetailSalesController {

    Logger logger = Logger.getLogger("RetailSalesController");

    @Autowired
    RetailSalesRepository retailSalesRepository;

    @PostMapping("/api/auth/getRetailSalesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getRetailSalesTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getRetailSalesTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<RetailSalesJSON> returnList;

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
        returnList = retailSalesRepository.getRetailSalesTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getRetailSalesProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getRetailSalesProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getRetailSalesProductTable with RetailSales id=" + docId.toString());
        List<RetailSalesProductTableJSON> returnList;
        try {
            returnList = retailSalesRepository.getRetailSalesProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getRetailSalesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getRetailSalesPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getRetailSalesPagesList: " + searchRequest.toString());

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
        int size = retailSalesRepository.getRetailSalesSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertRetailSales")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertRetailSales(@RequestBody RetailSalesForm request){
        logger.info("Processing post request for path /api/auth/insertRetailSales: " + request.toString());

        Long newDocument = retailSalesRepository.insertRetailSales(request);
        if(newDocument!=null){//если Розничная продажа создалась (>0) или не создалась (0) - (0 обрабатывается на фронте как недостаточно объема склада для операции)
            return new ResponseEntity<>(String.valueOf(newDocument), HttpStatus.OK);
        } else {//если null - значит на одной из стадий сохранения произошла ошибка
            return new ResponseEntity<>("Ошибка создания документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getSetOfTypePrices",
            params = {"company_id","department_id","cagent_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSetOfTypePrices(
            @RequestParam("company_id") Long company_id,
            @RequestParam("department_id") Long department_id,
            @RequestParam("cagent_id") Long cagent_id)
    {
        logger.info("Processing get request for path /api/auth/getSetOfTypePrices with parameters: " +
                "company_id: " + company_id +
                " department_id: " + department_id +
                " cagent_id: " + cagent_id );
        SetOfTypePricesJSON response;
        try {
            response=retailSalesRepository.getSetOfTypePrices(company_id, department_id, cagent_id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getSetOfTypePrices. company_id=" + company_id + ", department_id=" + department_id + ", cagent_id=" + cagent_id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки набора типов цен", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/isReceiptPrinted",
            params = {"company_id","document_id","id","operation_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> isReceiptPrinted(
            @RequestParam("company_id") Long company_id,
            @RequestParam("document_id") int document_id,
            @RequestParam("id") Long id,
            @RequestParam("operation_id") String operation_id)
    {
        logger.info("Processing get request for path /api/auth/isReceiptPrinted with parameters: " +
                "company_id: " + company_id +
                " document_id: " + document_id +
                " id: " + id +
                " operation_id: " + operation_id
        );
        Boolean response;
        try {
            response=retailSalesRepository.isReceiptPrinted(company_id, document_id, id, operation_id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method isReceiptPrinted. company_id=" + company_id + ", document_id=" + document_id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка запроса на наличие чека", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(
            value = "/api/auth/getRetailSalesValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getRetailSalesValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getRetailSalesValuesById with parameters: " + "id: " + id);
        RetailSalesJSON response;
        try {
            response=retailSalesRepository.getRetailSalesValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getRetailSalesValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки значений документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/updateRetailSales")
    public ResponseEntity<?> updateRetailSales(@RequestBody RetailSalesForm request){
        logger.info("Processing post request for path /api/auth/updateRetailSales: " + request.toString());
        Boolean updateResults = retailSalesRepository.updateRetailSales(request);
        if(updateResults){
            return new ResponseEntity<>(updateResults, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/saveSettingsRetailSales")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsRetailSales(@RequestBody SettingsRetailSalesForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsRetailSales: " + request.toString());
        try {
            return new ResponseEntity<>(retailSalesRepository.saveSettingsRetailSales(request), HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/api/auth/savePricingSettingsRetailSales")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> savePricingSettingsRetailSales(@RequestBody SettingsRetailSalesForm request){
        logger.info("Processing post request for path /api/auth/savePricingSettingsRetailSales: " + request.toString());
        try {
            return new ResponseEntity<>(retailSalesRepository.savePricingSettingsRetailSales(request), HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка сохранения настроек расценки для документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsRetailSales",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsRetailSales()
    {
        logger.info("Processing get request for path /api/auth/getSettingsRetailSales without request parameters");
        SettingsRetailSalesJSON response;
        try {
            response=retailSalesRepository.getSettingsRetailSales();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для документа Розничная продажа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getRetailSalesLinkedDocsList",
            params = {"id","docName"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getRetailSalesLinkedDocsList(
    @RequestParam("id") Long id, @RequestParam("docName") String docName) {//передали сюда id документа и имя таблицы
        logger.info("Processing get request for path api/auth/getRetailSalesLinkedDocsList with parameters: " + "id: " + id+ ", docName: "+docName);
        List<LinkedDocsJSON> returnList;
        returnList = retailSalesRepository.getRetailSalesLinkedDocsList(id,docName);
        if(!Objects.isNull(returnList)){
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка при загрузке списка связанных документов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // С удалением пока все непонятно - Розничная продажа создается тогда, когда уже пробит чек, т.е. продажа уже совершена, и товар выбыл. Удалять такое однозначно нельзя. Но возможно будут какие-то
    // другие ситуации. Поэтому удаление пока оставляю закомментированным
/*    @PostMapping("/api/auth/deleteRetailSales")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteRetailSales(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteRetailSales: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(retailSalesRepository.deleteRetailSales(checked)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/undeleteRetailSales")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteRetailSales(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteRetailSales: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(retailSalesRepository.undeleteRetailSales(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка восстановления Заказа покупателя", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }*/

}
