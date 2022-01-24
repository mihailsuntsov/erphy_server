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
import com.dokio.message.request.Settings.SettingsInvoiceinForm;
import com.dokio.message.response.*;
import com.dokio.message.response.additional.InvoiceinProductTableJSON;
import com.dokio.repository.*;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class InvoiceinController {

    Logger logger = Logger.getLogger("InvoiceinController");

    @Autowired
    InvoiceinRepositoryJPA invoiceinRepository;
    // связи для печатных форм
    @Autowired
    private TemplatesService tservice;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA company;

    @PostMapping("/api/auth/getInvoiceinTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInvoiceinTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInvoiceinTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<InvoiceinJSON> returnList;

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
        returnList = invoiceinRepository.getInvoiceinTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getInvoiceinPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getInvoiceinPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getInvoiceinPagesList: " + searchRequest.toString());

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
        int size = invoiceinRepository.getInvoiceinSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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
            value = "/api/auth/getInvoiceinProductTable",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInvoiceinProductTable( @RequestParam("id") Long docId) {
        logger.info("Processing get request for path /api/auth/getInvoiceinProductTable with Invoicein id=" + docId.toString());
        return  new ResponseEntity<>(invoiceinRepository.getInvoiceinProductTable(docId), HttpStatus.OK);
    }

    @PostMapping("/api/auth/insertInvoicein")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertInvoicein(@RequestBody InvoiceinForm request){
        logger.info("Processing post request for path /api/auth/insertInvoicein: " + request.toString());
        return new ResponseEntity<>(invoiceinRepository.insertInvoicein(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getInvoiceinValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getInvoiceinValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getInvoiceinValuesById with parameters: " + "id: " + id);
        return new ResponseEntity<>(invoiceinRepository.getInvoiceinValuesById(id), HttpStatus.OK);
    }

    @PostMapping("/api/auth/updateInvoicein")
    public ResponseEntity<?> updateInvoicein(@RequestBody InvoiceinForm request){
        logger.info("Processing post request for path /api/auth/updateInvoicein: " + request.toString());
        return new ResponseEntity<>(invoiceinRepository.updateInvoicein(request), HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveSettingsInvoicein")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsInvoicein(@RequestBody SettingsInvoiceinForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsInvoicein: " + request.toString());
        if(invoiceinRepository.saveSettingsInvoicein(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для документа", HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsInvoicein",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsInvoicein(){
        logger.info("Processing get request for path /api/auth/getSettingsInvoicein without request parameters");
        return new ResponseEntity<>(invoiceinRepository.getSettingsInvoicein(), HttpStatus.OK);
    }


    @PostMapping("/api/auth/deleteInvoicein")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteInvoicein(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteInvoicein: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        return new ResponseEntity<>(invoiceinRepository.deleteInvoicein(checked), HttpStatus.OK);
    }

    @PostMapping("/api/auth/undeleteInvoicein")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteInvoicein(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteInvoicein: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        return new ResponseEntity<>(invoiceinRepository.undeleteInvoicein(checked), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getListOfInvoiceinFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfInvoiceinFiles(
            @RequestParam("id") Long id)
    {
        logger.info("Processing post request for path api/auth/getListOfInvoiceinFiles: " + id);
        try {
            return new ResponseEntity<>(invoiceinRepository.getListOfInvoiceinFiles(id), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteInvoiceinFile")
    public ResponseEntity<?> deleteInvoiceinFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteInvoiceinFile: " + request.toString());
        try {
            return new ResponseEntity<>(invoiceinRepository.deleteInvoiceinFile(request), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка удаления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToInvoicein")
    public ResponseEntity<?> addFilesToInvoicein(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToInvoicein: " + request.toString());
        try{
            return new ResponseEntity<>(invoiceinRepository.addFilesToInvoicein(request), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // печать документов
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/invoiceinPrint",
            params = {"file_name", "tt_id", "doc_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public void invoiceinPrint (HttpServletResponse response,
                                 @RequestParam("file_name") String filename,
                                 @RequestParam("doc_id") Long doc_id,
                                 @RequestParam("tt_id")int templateTypeId) throws Exception {
        FileInfoJSON fileInfo = tservice.getFileInfo(filename);
        InputStream is = new FileInputStream(new File(fileInfo.getPath()+"/"+filename));
        OutputStream os = response.getOutputStream();
        try {
            InvoiceinJSON doc = invoiceinRepository.getInvoiceinValuesById(doc_id);
            List<InvoiceinProductTableJSON> product_table=invoiceinRepository.getInvoiceinProductTable(doc_id);
            CagentsJSON cg = cagentRepository.getCagentValues(doc.getCagent_id());
            CompaniesJSON mc = company.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm mainPaymentAccount = company.getMainPaymentAccountOfCompany(doc.getCompany_id());

            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
            for(InvoiceinProductTableJSON product:product_table){// бежим по товарам
                if(doc.isNds()){// если в документе включен переключатель НДС
                    BigDecimal nds_val = new BigDecimal(product.getNds_value());// величина НДС в процентах у текущего товара. Например, 20
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
            logger.error("Exception in method invoiceinPrint.", e);
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }
}