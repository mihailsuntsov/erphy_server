/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.controller;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsReturnsupForm;
import com.dokio.message.response.*;
import com.dokio.message.response.additional.FilesReturnsupJSON;
import com.dokio.message.response.Settings.SettingsReturnsupJSON;
import com.dokio.message.response.additional.InvoiceoutProductTableJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.repository.*;
import com.dokio.service.StorageService;
import com.dokio.service.TemplatesService;
import org.apache.log4j.Logger;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.jxls.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class ReturnsupController {


    Logger logger = Logger.getLogger("ReturnsupController");

    @Autowired
    ReturnsupRepository returnsupRepository;
    // связи для печатных форм
    @Autowired
    private TemplatesService tservice;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA company;
    @Autowired
    StorageService storageService;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;


    @PostMapping("/api/auth/getReturnsupTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getReturnsupTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getReturnsupTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<ReturnsupJSON> returnList;

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
        returnList = returnsupRepository.getReturnsupTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getReturnsupProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnsupProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getReturnsupProductTable with Returnsup id=" + docId.toString());
        List<ReturnsupProductTableJSON> returnList;
        try {
            returnList = returnsupRepository.getReturnsupProductTable(docId);
            return  new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getReturnsupPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getReturnsupPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getReturnsupPagesList: " + searchRequest.toString());

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
        int size = returnsupRepository.getReturnsupSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertReturnsup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertReturnsup(@RequestBody ReturnsupForm request) {
        logger.info("Processing post request for path /api/auth/insertReturnsup: " + request.toString());

        Long newDocument = returnsupRepository.insertReturnsup(request);
        if(newDocument!=null){//вернет id созданного документа либо 0, если недостаточно прав
            return new ResponseEntity<>(String.valueOf(newDocument), HttpStatus.OK);
        } else {//если null - значит на одной из стадий сохранения произошла ошибка
            return new ResponseEntity<>("Document creation error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getReturnsupValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnsupValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getReturnsupValuesById with parameters: " + "id: " + id);
        ReturnsupJSON response;
        try {
            response=returnsupRepository.getReturnsupValuesById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method getReturnsupValuesById. id = " + id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getReturnsupLinkedDocsList",
            params = {"id","docName"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnsupLinkedDocsList(
            @RequestParam("id") Long id, @RequestParam("docName") String docName) {//передали сюда id документа и имя таблицы
        logger.info("Processing get request for path api/auth/getReturnsupLinkedDocsList with parameters: " + "id: " + id+ ", docName: "+docName);
        List<LinkedDocsJSON> returnList;
        returnList = returnsupRepository.getReturnsupLinkedDocsList(id,docName);
        if(!Objects.isNull(returnList)){
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка при загрузке списка связанных документов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/updateReturnsup")
    public ResponseEntity<?> updateReturnsup(@RequestBody ReturnsupForm request){
        logger.info("Processing post request for path /api/auth/updateReturnsup: " + request.toString());
//        Integer result=returnsupRepository.updateReturnsup(request);
        return new ResponseEntity<>(returnsupRepository.updateReturnsup(request), HttpStatus.OK);//return 1 = все ок, 0 = не достаточное кол-во товаров для списания со склада, -1 = недостаточно прав

//        if(!Objects.isNull(result)){
//            return new ResponseEntity<>(result, HttpStatus.OK);//return 1 = все ок, 0 = не достаточное кол-во товаров для списания со склада, -1 = недостаточно прав
//        } else {
//            return new ResponseEntity<>("Ошибка сохранения либо завершения Возврата поставщику", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    @PostMapping("/api/auth/saveSettingsReturnsup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsReturnsup(@RequestBody SettingsReturnsupForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsReturnsup: " + request.toString());

        if(returnsupRepository.saveSettingsReturnsup(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Возврат поставщику", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsReturnsup",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsReturnsup()
    {
        logger.info("Processing get request for path /api/auth/getSettingsReturnsup without request parameters");
        SettingsReturnsupJSON response;
        try {
            response=returnsupRepository.getSettingsReturnsup();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для документа Возврат поставщику", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteReturnsup")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteReturnsup(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteReturnsup: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
            return new ResponseEntity<>(returnsupRepository.deleteReturnsup(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteReturnsup")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteReturnsup(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteReturnsup: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(returnsupRepository.undeleteReturnsup(checked), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getReturnsupProductsList",
            params = {"searchString", "companyId", "departmentId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getReturnsupProductsList(
            @RequestParam("searchString")   String searchString,
            @RequestParam("companyId")      Long companyId,
            @RequestParam("departmentId")   Long departmentId)
    {
        logger.info("Processing post request for path /api/auth/getReturnsupProductsList with parameters: " +
                "  searchString: "  + searchString +
                ", companyId: "     + companyId.toString() +
                ", departmentId: "  + departmentId.toString());
        List returnList;
        returnList = returnsupRepository.getReturnsupProductsList(searchString, companyId, departmentId);
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    //удаление 1 строки из таблицы товаров
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/deleteReturnsupProductTableRow",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteCustomersOrdersProductTableRow(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/deleteReturnsupProductTableRow with parameters: " +
                "id: " + id);
        boolean result;
        try {
            result=returnsupRepository.deleteReturnsupProductTableRow(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getListOfReturnsupFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfReturnsupFiles(@RequestBody SearchForm request)  {
        logger.info("Processing post request for path api/auth/getListOfReturnsupFiles: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<FilesReturnsupJSON> returnList;
        try {
            returnList = returnsupRepository.getListOfReturnsupFiles(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteReturnsupFile")
    public ResponseEntity<?> deleteReturnsupFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteReturnsupFile: " + request.toString());

        if(returnsupRepository.deleteReturnsupFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("File deletion error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToReturnsup")
    public ResponseEntity<?> addFilesToReturnsup(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToReturnsup: " + request.toString());

        if(returnsupRepository.addFilesToReturnsup(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/setReturnsupAsDecompleted")
    public ResponseEntity<?> setReturnsupAsDecompleted(@RequestBody ReturnsupForm request){
        logger.info("Processing post request for path /api/auth/setReturnsupAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(returnsupRepository.setReturnsupAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setReturnsupAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    // печать документов
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/returnsupPrint",
            params = {"file_name", "doc_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public void returnsupPrint (HttpServletResponse response,
                                 @RequestParam("file_name") String filename,
                                 @RequestParam("doc_id") Long doc_id) throws Exception {
        FileInfoJSON fileInfo = tservice.getFileInfo(filename);
        Long masterId = userRepositoryJPA.getMyMasterId();
        byte[] decryptedBytesOfFile = storageService.loadFile(fileInfo.getPath()+"/"+filename, masterId);
        InputStream is = new ByteArrayInputStream(decryptedBytesOfFile);
        OutputStream os = response.getOutputStream();
        try {
            ReturnsupJSON doc = returnsupRepository.getReturnsupValuesById(doc_id);
            List<ReturnsupProductTableJSON> product_table=returnsupRepository.getReturnsupProductTable(doc_id);
            CagentsJSON cg = cagentRepository.getCagentValues(doc.getCagent_id());
            CompaniesJSON mc = company.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm mainPaymentAccount = company.getMainPaymentAccountOfCompany(doc.getCompany_id());

            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
            for(ReturnsupProductTableJSON product:product_table){// бежим по товарам
                if(doc.isNds()){// если в документе включен переключатель НДС
                    BigDecimal nds_val = product.getNds_value();// величина НДС в процентах у текущего товара. Например, 20
                    // Включен переключатель "НДС включён" или нет - в любом случае НДС уже в цене product_sumprice. Нужно его вычленить из нее по формуле (для НДС=20%) "цену с НДС умножить на 20 и разделить на 120"
                    sumNds=sumNds.add(product.getProduct_sumprice().multiply(nds_val).divide(new BigDecimal(100).add(nds_val),2,BigDecimal.ROUND_HALF_UP));
                }
                totalSum=totalSum.add(product.getProduct_sumprice());
            }
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileInfo.getOriginal_name());
            Context context = new Context();
            context.putVar("doc", doc);
            context.putVar("mc", mc); // предприятие
            context.putVar("cg", cg); // контрагент
            context.putVar("tservice", tservice); // helper-класс для формирования файла
            context.putVar("productTable", product_table);// таблица с товарами
            context.putVar("mainPaymentAccount", mainPaymentAccount);// первый в списке расчётный счёт предприятия
            context.putVar("sumNds", sumNds);
            context.putVar("totalSum", totalSum);

            // вставка печати и подписей
            if(!Objects.isNull(mc.getStamp_id())){
                FileInfoJSON fileStampInfo = tservice.getFileInfo(mc.getStamp_id());
                InputStream stampIs = new FileInputStream(new File(fileStampInfo.getPath()+"/"+fileStampInfo.getName()));
                byte[] stamp = Util.toByteArray(stampIs);
                context.putVar("stamp", stamp);
                stampIs.close();}
            if(!Objects.isNull(mc.getDirector_signature_id())){
                FileInfoJSON fileDirSignatInfo = tservice.getFileInfo(mc.getDirector_signature_id());
                InputStream dirSignIs = new FileInputStream(new File(fileDirSignatInfo.getPath()+"/"+fileDirSignatInfo.getName()));
                byte[] dirSignature = Util.toByteArray(dirSignIs);
                context.putVar("dirSignature", dirSignature);
                dirSignIs.close();}
            if(!Objects.isNull(mc.getGlavbuh_signature_id())){
                FileInfoJSON fileGbSignatInfo = tservice.getFileInfo(mc.getGlavbuh_signature_id());
                InputStream gbSignIs = new FileInputStream(new File(fileGbSignatInfo.getPath()+"/"+fileGbSignatInfo.getName()));
                byte[] gbSignature = Util.toByteArray(gbSignIs);
                context.putVar("gbSignature", gbSignature);
                gbSignIs.close();}

            JxlsHelper.getInstance().processTemplate(is, os, context);
        } catch (Exception e){
            logger.error("Exception in method returnsupPrint.", e);
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }
}


