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
import com.dokio.message.request.Settings.SettingsInvoiceoutForm;
import com.dokio.message.response.CagentsJSON;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.InvoiceoutJSON;
import com.dokio.message.response.additional.InvoiceoutProductTableJSON;
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
public class InvoiceoutController {

    Logger logger = Logger.getLogger("InvoiceoutController");

    @Autowired
    InvoiceoutRepositoryJPA invoiceoutRepository;
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

    @PostMapping("/api/auth/getInvoiceoutTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInvoiceoutTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInvoiceoutTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<InvoiceoutJSON> returnList;

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
        returnList = invoiceoutRepository.getInvoiceoutTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getInvoiceoutPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInvoiceoutPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInvoiceoutPagesList: " + searchRequest.toString());

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
        int size = invoiceoutRepository.getInvoiceoutSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getInvoiceoutProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInvoiceoutProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getInvoiceoutProductTable with Invoiceout id=" + docId.toString());
        return  new ResponseEntity<>(invoiceoutRepository.getInvoiceoutProductTable(docId), HttpStatus.OK);
    }

    @PostMapping("/api/auth/insertInvoiceout")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertInvoiceout(@RequestBody InvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/insertInvoiceout: " + request.toString());
        return new ResponseEntity<>(invoiceoutRepository.insertInvoiceout(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getInvoiceoutValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInvoiceoutValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getInvoiceoutValuesById with parameters: " + "id: " + id);
        return new ResponseEntity<>(invoiceoutRepository.getInvoiceoutValuesById(id), HttpStatus.OK);
    }

    @PostMapping("/api/auth/updateInvoiceout")
    public ResponseEntity<?> updateInvoiceout(@RequestBody InvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/updateInvoiceout: " + request.toString());
        return new ResponseEntity<>(invoiceoutRepository.updateInvoiceout(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveSettingsInvoiceout")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsInvoiceout(@RequestBody SettingsInvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsInvoiceout: " + request.toString());
        if(invoiceoutRepository.saveSettingsInvoiceout(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа Розничная продажа", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/auth/savePricingSettingsInvoiceout")
    public ResponseEntity<?> savePricingSettingsInvoiceout(@RequestBody SettingsInvoiceoutForm request) {
        logger.info("Processing post request for path /api/auth/savePricingSettingsInvoiceout: " + request.toString());
        try {
            return new ResponseEntity<>(invoiceoutRepository.savePricingSettingsInvoiceout(request), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error of saving of Invoice to customers pricing settings", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getSettingsInvoiceout",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsInvoiceout(){
        logger.info("Processing get request for path /api/auth/getSettingsInvoiceout without request parameters");
            return new ResponseEntity<>(invoiceoutRepository.getSettingsInvoiceout(), HttpStatus.OK);
    }


    @PostMapping("/api/auth/deleteInvoiceout")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteInvoiceout(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteInvoiceout: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(invoiceoutRepository.deleteInvoiceout(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteInvoiceout")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteInvoiceout(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteInvoiceout: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        return new ResponseEntity<>(invoiceoutRepository.undeleteInvoiceout(checked), HttpStatus.OK);
    }


    // печать документов
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/invoiceoutPrint",
            params = {"file_name", "doc_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public void invoiceoutPrint (HttpServletResponse response,
                                 @RequestParam("file_name") String filename,
                                 @RequestParam("doc_id") Long doc_id) throws Exception {
        FileInfoJSON fileInfo = tservice.getFileInfo(filename);
        Long masterId = userRepositoryJPA.getMyMasterId();
        byte[] decryptedBytesOfFile = storageService.loadFile(fileInfo.getPath()+"/"+filename, masterId);
        InputStream is = new ByteArrayInputStream(decryptedBytesOfFile);
        OutputStream os = response.getOutputStream();
        try {
            InvoiceoutJSON doc = invoiceoutRepository.getInvoiceoutValuesById(doc_id);
            List<InvoiceoutProductTableJSON> product_table=invoiceoutRepository.getInvoiceoutProductTable(doc_id);
            CagentsJSON cg = cagentRepository.getCagentValues(doc.getCagent_id());
            CompaniesJSON mc = company.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm mainPaymentAccount = company.getMainPaymentAccountOfCompany(doc.getCompany_id());

            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
            for(InvoiceoutProductTableJSON product:product_table){// бежим по товарам
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
            logger.error("Exception in method invoiceoutPrint.", e);
            e.printStackTrace();
            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getOutputStream().println("{");
            response.getOutputStream().println("\"status\": " + 500 + ",");
            response.getOutputStream().println("\"error\": \"" + "Internal server error" + "\",");
            response.getOutputStream().println("\"message\": \"" + "Error -> INTERNAL_SERVER_ERROR" + "\"");
            response.getOutputStream().println("}");
            response.flushBuffer();
        } finally {

            is.close();
            os.close();
        }
    }

    @PostMapping("/api/auth/setInvoiceoutAsDecompleted")
    public ResponseEntity<?> setInvoiceoutAsDecompleted(@RequestBody InvoiceoutForm request){
        logger.info("Processing post request for path /api/auth/setInvoiceoutAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(invoiceoutRepository.setInvoiceoutAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setInvoiceoutAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
