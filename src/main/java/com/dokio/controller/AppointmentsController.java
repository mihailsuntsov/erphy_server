package com.dokio.controller;
import com.dokio.message.request.*;
import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.Settings.SettingsAppointmentForm;
import com.dokio.message.request.additional.AppointmentDocsListSearchForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsAppointmentJSON;
import com.dokio.message.response.additional.appointment.AppointmentService;
import com.dokio.repository.*;
import com.dokio.service.TemplatesService;
import com.dokio.util.CommonUtilites;
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
import java.util.List;
import java.util.Objects;

@Controller
public class AppointmentsController {
    Logger logger = Logger.getLogger("AppointmentsController");

    @Autowired
    AppointmentRepositoryJPA appointmentRepositoryJPA;
    // связи для печатных форм
//    @Autowired
//    private TemplatesService tservice;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA company;
    @Autowired
    CommonUtilites commonUtilites;
    @Autowired
    private TemplatesService tservice;

    @PostMapping("/api/auth/getAppointmentTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getAppointmentTable(@RequestBody AppointmentDocsListSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getAppointmentTable: " + searchRequest.toString());
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "date_created";
            sortAsc = "asc";
        }
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int offsetreal = offset * result;//создана переменная с номером страницы
        return new ResponseEntity<List>(appointmentRepositoryJPA.getAppointmentsTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(), searchRequest.getDepartmentId(),searchRequest.getFilterOptionsIds(),searchRequest.getAppointmentId(),searchRequest.getCustomerId()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getAppointmentPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getAppointmentPagesList(@RequestBody AppointmentDocsListSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getAppointmentPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = appointmentRepositoryJPA.getAppointmentsSize(searchString, searchRequest.getCompanyId(), searchRequest.getDepartmentId(),searchRequest.getFilterOptionsIds(),searchRequest.getAppointmentId(),searchRequest.getCustomerId());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getAppointmentValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getAppointmentValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getAppointmentValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getAppointmentsValuesById(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getAppointmentValuesById error", e);
            return new ResponseEntity<>("Controller getAppointmentValuesById error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getAppointmentChildDocs",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getAppointmentChildDocs(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getAppointmentChildDocs with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getAppointmentChildDocs(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getAppointmentChildDocs error", e);
            return new ResponseEntity<>("Controller getAppointmentChildDocs error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getListOfAppointmentFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfAppointmentFiles(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getListOfAppointmentFiles with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getListOfAppointmentFiles(id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getListOfAppointmentFiles error", e);
            return new ResponseEntity<>("Controller getListOfAppointmentFiles error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/insertAppointment")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/insertAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.insertAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertAppointment error", e);
            return new ResponseEntity<>("Controller insertAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateAppointment")
    public ResponseEntity<?> updateAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/updateAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.updateAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateAppointment error", e);
            return new ResponseEntity<>("Controller updateAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/createAndCompleteShipmentFromAppointment")
    public ResponseEntity<?> createAndCompleteShipmentFromAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/createAndCompleteShipmentFromAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.createAndCompleteShipmentFromAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller createAndCompleteShipmentFromAppointment error", e);
            return new ResponseEntity<>("Controller createAndCompleteShipmentFromAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/createAndCompletePaymentInFromAppointment")
    public ResponseEntity<?> createAndCompletePaymentInFromAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/createAndCompletePaymentInFromAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.createAndCompletePaymentInFromAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller createAndCompletePaymentInFromAppointment error", e);
            return new ResponseEntity<>("Controller createAndCompletePaymentInFromAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/createAndCompleteOrderInFromAppointment")
    public ResponseEntity<?> createAndCompleteOrderInFromAppointment(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/createAndCompleteOrderInFromAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.createAndCompleteOrderInFromAppointment(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller createAndCompleteOrderInFromAppointment error", e);
            return new ResponseEntity<>("Controller createAndCompleteOrderInFromAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/deleteAppointments")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteAppointment(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteAppointments: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(appointmentRepositoryJPA.deleteAppointments(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteAppointments error", e);
            return new ResponseEntity<>("Controller deleteAppointments error",  HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/undeleteAppointments")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteAppointment(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteAppointments: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(appointmentRepositoryJPA.undeleteAppointments(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller undeleteAppointments error", e);
            return new ResponseEntity<>("Controller undeleteAppointments error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/setAppointmentAsDecompleted")
    public ResponseEntity<?> setCustomersOrdersAsDecompleted(@RequestBody AppointmentsForm request){
        logger.info("Processing post request for path /api/auth/setCustomersOrdersAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.setAppointmentAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setCustomersOrdersAsDecompleted error", e);
            return new ResponseEntity<>("Controller setAppointmentAsDecompleted exception", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/saveSettingsAppointment")
    public ResponseEntity<?> saveSettingsCalendar(@RequestBody SettingsAppointmentForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.saveSettingsAppointment(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Controller saveSettingsAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getSettingsAppointment",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsAppointment()
    {   logger.info("Processing get request for path /api/auth/getSettingsAppointments without request parameters");
        try{    return new ResponseEntity<>(appointmentRepositoryJPA.getSettingsAppointment(), HttpStatus.OK);}
        catch (Exception e) {e.printStackTrace();logger.error("Controller getSettingsAppointment exception", e);
            return new ResponseEntity<>("Controller getSettingsAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/addFilesToAppointment")
    public ResponseEntity<?> addFilesToProduct(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/addFilesToAppointment: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.addFilesToAppointment(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller addFilesToAppointment error", e);
            return new ResponseEntity<>("Controller addFilesToAppointment error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getAppointmentServicesSearchList")
    public  ResponseEntity<?> getAppointmentServicesList(@RequestBody AppointmentMainInfoForm request) {
        logger.info("Processing post request for path /api/auth/getAppointmentServicesSearchList: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getAppointmentServicesSearchList(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getAppointmentServicesSearchList error", e);
            return new ResponseEntity<>("Controller getAppointmentServicesSearchList error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/deleteAppointmentFile",
            params = {"doc_id", "file_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteAppointmentFile( @RequestParam("doc_id") Long doc_id, @RequestParam("file_id") Long file_id) {
        logger.info("Processing get request for path /api/auth/deleteAppointmentFile with file_id=" + file_id.toString() + ", doc_id = " + doc_id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.deleteAppointmentFile(file_id,doc_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller deleteAppointmentFile error with file_id=" + file_id.toString() + ", doc_id=" + doc_id.toString(), e);
            return new ResponseEntity<>("Controller deleteAppointmentFile error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/setAppointmentChildDocumentAsDecompleted",
            params = {"doc_id", "doc_name"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> setAppointmentChildDocumentAsDecompleted( @RequestParam("doc_id") Long doc_id, @RequestParam("doc_name") String doc_name) {
        logger.info("Processing get request for path /api/auth/setAppointmentChildDocumentAsDecompleted with doc_name=" + doc_name + ", doc_id = " + doc_id.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.setAppointmentChildDocumentAsDecompleted(doc_name,doc_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller setAppointmentChildDocumentAsDecompleted error with file_id=" + doc_name + ", doc_id=" + doc_id.toString(), e);
            return new ResponseEntity<>("Controller setAppointmentChildDocumentAsDecompleted error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @RequestMapping(
            value = "/api/auth/getPreloadServicesIdsByResourceId",
            params = {"resource_id","deppart_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getPreloadServicesIdsByResourceId(
            @RequestParam("resource_id") Long resource_id,
            @RequestParam("deppart_id") Long deppart_id){
        logger.info("Processing get request for path /api/auth/getPreloadServicesIdsByResourceId with parameters: " + "resource_id: " + resource_id);
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getPreloadServicesIdsByResourceId(resource_id,deppart_id), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller getPreloadServicesIdsByResourceId error", e);
            return new ResponseEntity<>("Controller getPreloadServicesIdsByResourceId error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    // печать документов
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/appointmentPrint",
            params = {"file_name", "doc_id", "cagent_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public void appointmentPrint (HttpServletResponse response,
                                  @RequestParam("file_name") String filename,
                                  @RequestParam("cagent_id") Long cagentId,
                                  @RequestParam("doc_id") Long doc_id) throws Exception {
        FileInfoJSON fileInfo = tservice.getFileInfo(filename);
        InputStream is = new FileInputStream(new File(fileInfo.getPath()+"/"+filename));
        OutputStream os = response.getOutputStream();
        try {
            AppointmentsJSON doc = appointmentRepositoryJPA.getAppointmentsValuesById(doc_id);
            List<AppointmentService> product_table=doc.getAppointmentsProductTable();
            CagentsJSON cg = cagentRepository.getCagentValues(cagentId);
            CompaniesJSON mc = company.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm mainPaymentAccount = company.getMainPaymentAccountOfCompany(doc.getCompany_id());
            doc.setCagent(cg.getName());
            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            int row_num = 1; // номер строки при выводе печатной версии
            // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
            for(AppointmentService product:product_table){// бежим по товарам
                if(product.getCagent_id().equals(cagentId)) { // в таблице товаров представлены товары всех клиентов, но нужно взять только те, которые относятся к запрашиваемому клиенту
                    product.setRow_num(row_num);// номер строки при выводе печатной версии
                    if (doc.isNds()) {// если в документе включен переключатель НДС
                        BigDecimal nds_val = product.getNds_value();// величина НДС в процентах у текущего товара. Например, 20
                        // Включен переключатель "НДС включён" или нет - в любом случае НДС уже в цене product_sumprice. Нужно его вычленить из нее по формуле (для НДС=20%) "цену с НДС умножить на 20 и разделить на 120"
                        sumNds = sumNds.add(product.getProduct_sumprice().multiply(nds_val).divide(new BigDecimal(100).add(nds_val), 2, BigDecimal.ROUND_HALF_UP));
                    }
                    totalSum = totalSum.add(product.getProduct_sumprice());
                    row_num++;
                }
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
            logger.error("Exception in method appointmentPrint.", e);
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }
}
