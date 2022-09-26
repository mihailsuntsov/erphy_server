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
import com.dokio.message.request.Settings.SettingsVatinvoiceinForm;
import com.dokio.message.response.*;
import com.dokio.message.response.additional.InvoiceinProductTableJSON;
import com.dokio.message.response.additional.InvoiceoutProductTableJSON;
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.ProductTableJSON;
import com.dokio.repository.*;
import com.dokio.service.TemplatesService;
import com.dokio.util.LinkedDocsUtilites;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Controller
public class VatinvoiceinController {

    Logger logger = Logger.getLogger("VatinvoiceinController");

    @Autowired
    private VatinvoiceinRepositoryJPA vatinvoiceinRepository;
    // связи для печатных форм
    @Autowired
    private TemplatesService tservice;
    @Autowired
    private FileRepositoryJPA fileRepository;
    @Autowired
    private CagentRepositoryJPA cagentRepository;
    @Autowired
    private CompanyRepositoryJPA company;
    @Autowired
    private LinkedDocsUtilites linkedDocsUtilites;
    @Autowired
    private AcceptanceRepository acceptanceRepository;
    @Autowired InvoiceinRepositoryJPA invoiceinRepository;

    @PostMapping("/api/auth/getVatinvoiceinTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getVatinvoiceinTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getVatinvoiceinTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<VatinvoiceinJSON> returnList;

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
        returnList = vatinvoiceinRepository.getVatinvoiceinTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getVatinvoiceinPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getVatinvoiceinPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getVatinvoiceinPagesList: " + searchRequest.toString());

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
        int size = vatinvoiceinRepository.getVatinvoiceinSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertVatinvoicein")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertVatinvoicein(@RequestBody VatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/insertVatinvoicein: " + request.toString());
        return new ResponseEntity<>(vatinvoiceinRepository.insertVatinvoicein(request), HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/auth/getVatinvoiceinValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getVatinvoiceinValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getVatinvoiceinValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(vatinvoiceinRepository.getVatinvoiceinValuesById(id), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateVatinvoicein")
    public ResponseEntity<?> updateVatinvoicein(@RequestBody VatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/updateVatinvoicein: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.updateVatinvoicein(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error saving document", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/saveSettingsVatinvoicein")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsVatinvoicein(@RequestBody SettingsVatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsVatinvoicein: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.saveSettingsVatinvoicein(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка сохранения настроек для документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsVatinvoicein",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsVatinvoicein(){
        logger.info("Processing get request for path /api/auth/getSettingsVatinvoicein without request parameters");
        try {return new ResponseEntity<>(vatinvoiceinRepository.getSettingsVatinvoicein(), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка загрузки настроек", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteVatinvoicein")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteVatinvoicein(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteVatinvoicein: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(vatinvoiceinRepository.deleteVatinvoicein(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Deletion error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/undeleteVatinvoicein")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteVatinvoicein(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteVatinvoicein: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(vatinvoiceinRepository.undeleteVatinvoicein(checked), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Restore error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getListOfVatinvoiceinFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfVatinvoiceinFiles(
            @RequestParam("id") Long id){
        logger.info("Processing post request for path api/auth/getListOfVatinvoiceinFiles: " + id);
        try {return new ResponseEntity<>(vatinvoiceinRepository.getListOfVatinvoiceinFiles(id), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteVatinvoiceinFile")
    public ResponseEntity<?> deleteVatinvoiceinFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteVatinvoiceinFile: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.deleteVatinvoiceinFile(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("File deletion error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToVatinvoicein")
    public ResponseEntity<?> addFilesToVatinvoicein(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToVatinvoicein: " + request.toString());
        try{return new ResponseEntity<>(vatinvoiceinRepository.addFilesToVatinvoicein(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }// печать документов

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/vatinvoiceinPrint",
            params = {"file_name", "doc_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public void vatinvoiceinPrint (HttpServletResponse response,
                                 @RequestParam("file_name") String filename,
                                 @RequestParam("doc_id") Long doc_id) throws Exception {
        FileInfoJSON fileInfo = tservice.getFileInfo(filename);
        InputStream is = new FileInputStream(new File(fileInfo.getPath()+"/"+filename));
        OutputStream os = response.getOutputStream();
        try {
            VatinvoiceinJSON doc = vatinvoiceinRepository.getVatinvoiceinValuesById(doc_id);
            CagentsJSON cg = cagentRepository.getCagentValues(doc.getCagent_id());
//            CagentsJSON consignee = cagentRepository.getCagentValues();
            CompaniesJSON mc = company.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm mainPaymentAccount = company.getMainPaymentAccountOfCompany(doc.getCompany_id());

            // Документ "Счет Фактура" сам по себе не содержит товарных позиций, поэтому нужно по цепочке связанных документов
            // найти родителя, который содержит товарные позиции. Это может быть Заказ поставщику, Счёт поставщика, Приёмка
            // Такой родитель может быть выше на 1 или 2 (максимум) ступени, например:
            // Приёмка (тут товары) -> Счёт-фактура
            // Счёт поставщика (тут товары) -> Исходящий платёж -> Счёт-фактура

            // id родительского документа содержится в одной из переменных: doc.orderout_id, doc.paymentout_id, doc.acceptance_id
            // вычленим его:
            Long parentDocId = !Objects.isNull(doc.getAcceptance_id())?doc.getAcceptance_id():(
                    !Objects.isNull(doc.getOrderout_id())?doc.getOrderout_id():doc.getPaymentout_id()
                    );
            // возьмем информацию по родительскому документу, в котором есть товарные позиции
            LinkedDocsJSON parentDocWithProductsId = linkedDocsUtilites.getParentDocWithProducts(doc.getParent_tablename(), parentDocId);

            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileInfo.getOriginal_name());
            Context context = new Context();
            context.putVar("doc", doc); // документ - Счёт-фактура
            context.putVar("mc", mc); // предприятие
            context.putVar("cg", cg); // контрагент
            context.putVar("tservice", tservice); // helper-класс для формирования файла
            context.putVar("mainPaymentAccount", mainPaymentAccount);// первый в списке расчётный счёт предприятия

            switch (parentDocWithProductsId.getTablename()) {
                case "acceptance":{     // Приёмка
                    AcceptanceJSON parentDoc = acceptanceRepository.getAcceptanceValuesById(parentDocWithProductsId.getId());
                    List<AcceptanceProductForm> product_table=acceptanceRepository.getAcceptanceProductTable(parentDocWithProductsId.getId());
                    context.putVar("parentDocWithProducts", parentDoc); // родительский документ - Приёмка
                    context.putVar("productTable", product_table);// таблица с товарами
                    // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
                    for(AcceptanceProductForm product:product_table){// бежим по товарам
                        if(parentDoc.isNds()){// если в документе включен переключатель НДС
                            BigDecimal nds_val = product.getNds_value();// величина НДС в процентах у текущего товара. Например, 20
                            // Включен переключатель "НДС включён" или нет - в любом случае НДС уже в цене product_sumprice. Нужно его вычленить из нее по формуле (для НДС=20%) "цену с НДС умножить на 20 и разделить на 120"
                            sumNds=sumNds.add(product.getProduct_sumprice().multiply(nds_val).divide(new BigDecimal(100).add(nds_val),2,BigDecimal.ROUND_HALF_UP));
                        }
                        totalSum=totalSum.add(product.getProduct_sumprice());
                    }
                    context.putVar("sumNds", sumNds);
                    context.putVar("totalSum", totalSum);
                }
                case "invoicein":{      // Счёт поставщика
                    InvoiceinJSON parentDoc = invoiceinRepository.getInvoiceinValuesById(parentDocWithProductsId.getId());
                    List<InvoiceinProductTableJSON> product_table=invoiceinRepository.getInvoiceinProductTable(parentDocWithProductsId.getId());
                    context.putVar("parentDocWithProducts", parentDoc); // родительский документ - Счёт поставщика
                    context.putVar("productTable", product_table);// таблица с товарами
                    // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
                    for(InvoiceinProductTableJSON product:product_table){// бежим по товарам
                        if(parentDoc.isNds()){// если в документе включен переключатель НДС
                            BigDecimal nds_val = product.getNds_value();// величина НДС в процентах у текущего товара. Например, 20
                            // Включен переключатель "НДС включён" или нет - в любом случае НДС уже в цене product_sumprice. Нужно его вычленить из нее по формуле (для НДС=20%) "цену с НДС умножить на 20 и разделить на 120"
                            sumNds=sumNds.add(product.getProduct_sumprice().multiply(nds_val).divide(new BigDecimal(100).add(nds_val),2,BigDecimal.ROUND_HALF_UP));
                        }
                        totalSum=totalSum.add(product.getProduct_sumprice());
                    }
                    context.putVar("sumNds", sumNds);
                    context.putVar("totalSum", totalSum);
                }
//                Заказ поставщику  - ???????????????????????????????????????????
                default: InvoiceinJSON parentDoc = null;
            }

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

    @PostMapping("/api/auth/setVatinvoiceinAsDecompleted")
    public ResponseEntity<?> setVatinvoiceinAsDecompleted(@RequestBody VatinvoiceinForm request){
        logger.info("Processing post request for path /api/auth/setVatinvoiceinAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(vatinvoiceinRepository.setVatinvoiceinAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setVatinvoiceinAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
