package com.dokio.controller;
import com.dokio.message.request.*;
import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.message.request.Settings.SettingsAppointmentForm;
import com.dokio.message.request.additional.AppointmentDocsListSearchForm;
import com.dokio.message.request.additional.AppointmentMainInfoForm;
import com.dokio.message.request.additional.ChangeOwnerForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.SettingsAppointmentJSON;
import com.dokio.message.response.additional.appointment.AppointmentService;
import com.dokio.repository.*;
import com.dokio.service.StorageService;
import com.dokio.service.TemplatesService;
import com.dokio.service.generate_docs.GenerateDocumentsDocxService;
import com.dokio.util.CommonUtilites;
import com.google.common.collect.Lists;
import org.apache.commons.jexl3.JxltEngine;
import org.apache.log4j.Logger;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.com.google.common.base.Preconditions;
import org.docx4j.convert.in.xhtml.FormattingOption;
import org.docx4j.convert.in.xhtml.ImportXHTMLProperties;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.*;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.jxls.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.tidy.Tidy;
import org.docx4j.model.properties.run.FontSize;

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
    @Autowired
    StorageService storageService;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    GenerateDocumentsDocxService gt;

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
            value = "/api/auth/getDescriptionDefaultTemplate",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getDescriptionDefaultTemplate(@RequestParam("company_id") Long company_id) {
        logger.info("Processing get request for path /api/auth/getDescriptionDefaultTemplate with company_id=" + company_id.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.getDescriptionDefaultTemplate(company_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getDescriptionDefaultTemplate error with company_id=" + company_id.toString(), e);
            return new ResponseEntity<>("Controller getDescriptionDefaultTemplate error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/updateDescriptionDefaultTemplate")
    public ResponseEntity<?> updateDescriptionDefaultTemplate(@RequestBody SettingsAppointmentForm request){
        logger.info("Processing post request for path /api/auth/updateDescriptionDefaultTemplate: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.updateDescriptionDefaultTemplate(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Controller updateDescriptionDefaultTemplate error", HttpStatus.INTERNAL_SERVER_ERROR);}
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


    @PostMapping("/api/auth/changeAppointmentOwner")
    public ResponseEntity<?> changeAppointmentOwner(@RequestBody ChangeOwnerForm request){
        logger.info("Processing get request for path /api/auth/changeAppointmentOwner with parameters: " + request.toString());
        try {return new ResponseEntity<>(appointmentRepositoryJPA.changeAppointmentOwner(request), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Controller changeAppointmentOwner error", e);
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/appointmentPrintDocx",
            params = {"file_name", "doc_id", "cagent_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<byte[]> appointmentPrintDocx (  HttpServletResponse response,
                                        @RequestParam("file_name") String filename,
                                        @RequestParam("cagent_id") Long cagentId,
                                        @RequestParam("doc_id") Long doc_id) throws Exception, UnsupportedEncodingException {
        try{
            FileInfoJSON fileInfo = tservice.getFileInfo(filename);
            if(fileInfo !=null){
                String filePath=fileInfo.getPath()+"/"+fileInfo.getName();
                Long masterId = userRepositoryJPA.getMyMasterId();
                AppointmentsJSON doc = appointmentRepositoryJPA.getAppointmentsValuesById(doc_id);

                Map<String, String> myDateTime = userRepositoryJPA.getMyDateTime();// map with keys ${DAY}, ${MONTH}, ${YEAR}, ${HOUR}, ${MINUTE}, ${AMPM}.

                int indx = 0;
                int firstCustomersServiceIndex = 0;
                for (AppointmentService service : doc.getAppointmentsProductTable()){
                    if(service.getCagent_id().equals(cagentId) && firstCustomersServiceIndex == 0)
                        firstCustomersServiceIndex=indx;
                    indx++;
                }

                final int i = firstCustomersServiceIndex;
                BigDecimal serviceSumprice = doc.getAppointmentsProductTable().get(i).getProduct_count().multiply(doc.getAppointmentsProductTable().get(i).getProduct_price()).setScale(2, BigDecimal.ROUND_HALF_UP);
                Map<String, String> replaceMap = new HashMap<String, String>() {{
                    put("${APP_EMPLOYEE}",              doc.getEmployeeName());
                    put("${APP_JOB_TITLE}",             doc.getJobtitle());
                    put("${DOC_NUMBER}",                doc.getDoc_number().toString());
                    put("${APP_DATE_START}",            doc.getDate_start_user_format());
                    put("${APP_TIME_START}",            doc.getTime_start_user_format());
                    put("${APP_DATE_END}",              doc.getDate_end_user_format());
                    put("${APP_TIME_END}",              doc.getTime_end_user_format());
                    put("${APP_SERVICE_NAME}",          doc.getAppointmentsProductTable().get(i).getName());
                    put("${APP_SERVICE_QTT}",           doc.getAppointmentsProductTable().get(i).getProduct_count().toString().replace(".000", ""));
                    put("${APP_SERVICE_QTT_UOM}",       doc.getAppointmentsProductTable().get(i).getEdizm());
                    put("${APP_SERVICE_SUMPRICE}",      serviceSumprice.toString());
                    // ${APP_SERVICE_ADDED_TAX_VALUE} will be > 0 if taxes added over price, because getProduct_sumprice() is already contains added tax.
                    // Example: qtt = 1, price = 100, VAT = 10%, serviceSumprice = 1*100=100, getProduct_sumprice()=110, ${APP_SERVICE_ADDED_TAX_VALUE} = 110-100=10
                    put("${APP_SERVICE_ADDED_TAX_VALUE}",doc.getAppointmentsProductTable().get(i).getProduct_sumprice().subtract(serviceSumprice).toString());
                    put("${APP_SERVICE_PRICE_WITH_ADDED_TAX_VALUE}",doc.getAppointmentsProductTable().get(i).getProduct_sumprice().toString());
                    put("${APP_SERVICE_PRICE}",         doc.getAppointmentsProductTable().get(i).getProduct_price().toString());
//                    put("${APP_SERVICE_TAX}",           doc.getAppointmentsProductTable().get(i).getNds_value().toString());
                    put("${APP_SERVICE_TAX_NAME}",      (String)commonUtilites.getFieldValueFromTableById("sprav_taxes","name", masterId, doc.getAppointmentsProductTable().get(0).getNds_id()));
                }};

                replaceMap.putAll(commonUtilites.getCagentMapValues(cagentId));
                replaceMap.putAll(commonUtilites.getCompanyMapValues(doc.getCompany_id()));
                replaceMap.putAll(myDateTime);

                BufferedReader br = new BufferedReader(
                        new StringReader(
                                doc.getDescription()
                        ));
                StringWriter sw = new StringWriter();
                Tidy t = new Tidy();
                t.setDropEmptyParas(true);
                t.setShowWarnings(false); //to hide errors
                t.setQuiet(true); //to hide warning
                t.setUpperCaseAttrs(false);
                t.setUpperCaseTags(false);
                t.setInputEncoding("UTF-8");
                t.setOutputEncoding("UTF-8");
                t.setXmlOut(true);
                t.parse(br,sw);
                StringBuffer sb = sw.getBuffer();
                String strClean = sb.toString();
                br.close();
                sw.close();
                String wrappedXHTML = new StringBuilder(strClean).toString().
                replaceAll("class=\"ql-size-huge\"", "style=\"font-size: 26px; margin-bottom: 0px;\"").
                replaceAll("class=\"ql-size-large\"", "style=\"font-size: 18px; margin-bottom: 0px;\"").
                replaceAll("class=\"ql-size-small\"", "style=\"font-size: 10px; margin-bottom: 0px;\"").
                replaceAll("<br />", "").
                replaceAll("<p>", "<p style=\"font-size: 12px; margin-left: -8px; margin-bottom: 0px;\">");

                WordprocessingMLPackage wordMLPackageToConvert = WordprocessingMLPackage.createPackage();

                ImportXHTMLProperties.setProperty("docx4j-ImportXHTML.fonts.default.serif", "Arial");
                ImportXHTMLProperties.setProperty("docx4j-ImportXHTML.fonts.default.sans-serif", "Arial");
                ImportXHTMLProperties.setProperty("docx4j-ImportXHTML.fonts.default.monospace", "Courier New");

                XHTMLImporterImpl XHTMLImporter = new XHTMLImporterImpl(wordMLPackageToConvert);

                final List<Object> descriptionAsWordML = XHTMLImporter.convert( wrappedXHTML, null);

                // in a description variable at the debugging U can see what WordML it contains
//                wordMLPackageToConvert.getMainDocumentPart().getContent().addAll(descriptionAsWordML);
//                String description = XmlUtils.marshaltoString(wordMLPackageToConvert.getMainDocumentPart().getJaxbElement(), true, true);

                WordprocessingMLPackage wordMLPackage = gt.generateMLPackage(filePath, replaceMap, masterId);
                Map<String, List<Object>> replacements = new HashMap<String, List<Object>>() {{
                    put("${APP_ADDITIONAL_INFO}", descriptionAsWordML);
                }};


                MainDocumentPart mainPart = wordMLPackage.getMainDocumentPart();

                // look for all P elements in the specified object
                final List<P> paragraphs = Lists.newArrayList();
                new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
                    @Override
                    public List<Object> apply(Object o) {
                        if (o instanceof P) {
                            paragraphs.add((P) o);
                        }
                        return null;
                    }
                });



                // code by epochcoder from https://gist.github.com/epochcoder/478a72a4b59a43995373
                // run through all found paragraphs to located identifiers
                for (final P paragraph : paragraphs) {
                    // check if this is one of our identifiers
                    final StringWriter paragraphText = new StringWriter();
                    try {
                        TextUtils.extractText(paragraph, paragraphText);
                    } catch (Exception ex) {
                        logger.warn("failure while extracting text from w:p", ex);
                    }
                    final String identifier = paragraphText.toString();
                    if (identifier != null && replacements.containsKey(identifier)) {
                        final List<Object> listToModify = mainPart.getContent();
                        if (listToModify != null) {
                            final int index = listToModify.indexOf(paragraph);
                            Preconditions.checkState(index > -1, "could not located the paragraph in the specified list!");
                            // remove the paragraph from it's current index
                            listToModify.remove(index);
                            // add the converted HTML paragraphs
                            listToModify.addAll(index, replacements.get(identifier));
                        }
                    }
                }



                removeSpacePreserveRecursive(wordMLPackage.getMainDocumentPart().getJaxbElement().getBody());
                OutputStream outputStream = new ByteArrayOutputStream();
                Save saver = new Save(wordMLPackage);
                saver.save(outputStream);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"UTF-8\"")
                        .body(((ByteArrayOutputStream) outputStream).toByteArray());

            } else {ResponseEntity responseEntity = new ResponseEntity<>("Not enought permits or there is no file in a storage.", HttpStatus.FORBIDDEN);
                return responseEntity;}

        } catch (Exception e) {
            logger.error("Exception in method appointmentPrintDocx.", e);
            e.printStackTrace();
            return null;
        }

    }

    // code by achimmihca from https://github.com/plutext/docx4j-ImportXHTML/issues/59
    private static void removeSpacePreserveRecursive(Object obj)
    {
        if (obj instanceof Text)
        {
            Text text = (Text) obj;
            if ("preserve".equals(text.getSpace()))
            {
                text.setSpace(null);
            }
        }
        else if (obj instanceof ContentAccessor)
        {
            ContentAccessor contentAccessor = (ContentAccessor) obj;
            for (Object child : contentAccessor.getContent())
            {
                removeSpacePreserveRecursive(child);
            }
        }
    }

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
        Long masterId = userRepositoryJPA.getMyMasterId();
        byte[] decryptedBytesOfFile = storageService.loadFile(fileInfo.getPath()+"/"+filename, masterId);
        InputStream is = new ByteArrayInputStream(decryptedBytesOfFile);
        OutputStream os = response.getOutputStream();
        try {
            AppointmentsJSON doc = appointmentRepositoryJPA.getAppointmentsValuesById(doc_id);
            List<AppointmentService> all_products_table = doc.getAppointmentsProductTable(); // shared table, contained all services of all customers in this Appointment
            List<AppointmentService> product_table_of_cagent = new ArrayList<>(); // table contained services that belongs only to this customer (cagentId)
            CagentsJSON cg = cagentRepository.getCagentValues(cagentId);
            CompaniesJSON mc = company.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm mainPaymentAccount = company.getMainPaymentAccountOfCompany(doc.getCompany_id());
            doc.setCagent(cg.getName());
            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            int row_num = 1; // номер строки при выводе печатной версии
            // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
            for (AppointmentService product : all_products_table) {// бежим по товарам
                if (product.getCagent_id().equals(cagentId)) { // в таблице товаров представлены товары всех клиентов, но нужно взять только те, которые относятся к запрашиваемому клиенту
                    product.setRow_num(row_num);// номер строки при выводе печатной версии
                    if (doc.isNds()) {// если в документе включен переключатель НДС
                        BigDecimal nds_val = product.getNds_value();// величина НДС в процентах у текущего товара. Например, 20
                        // Включен переключатель "НДС включён" или нет - в любом случае НДС уже в цене product_sumprice. Нужно его вычленить из нее по формуле (для НДС=20%) "цену с НДС умножить на 20 и разделить на 120"
                        sumNds = sumNds.add(product.getProduct_sumprice().multiply(nds_val).divide(new BigDecimal(100).add(nds_val), 2, BigDecimal.ROUND_HALF_UP));
                    }
                    product_table_of_cagent.add(product);
                    totalSum = totalSum.add(product.getProduct_sumprice());
                    row_num++;
                }
            }

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileInfo.getOriginal_name());
            Context context = new Context();
            context.putVar("doc", doc);
            context.putVar("mc", mc); // предприятие
            context.putVar("cg", cg); // контрагент
            context.putVar("tservice", tservice); // helper-класс для формирования файла
            context.putVar("productTable", product_table_of_cagent);// таблица с товарами
            context.putVar("mainPaymentAccount", mainPaymentAccount);// первый в списке расчётный счёт предприятия
            context.putVar("sumNds", sumNds);
            context.putVar("totalSum", totalSum);

            // вставка печати и подписейё
            if (!Objects.isNull(mc.getStamp_id())) {
                FileInfoJSON fileStampInfo = tservice.getFileInfo(mc.getStamp_id());
                InputStream stampIs = new FileInputStream(new File(fileStampInfo.getPath() + "/" + fileStampInfo.getName()));
                byte[] stamp = Util.toByteArray(stampIs);
                context.putVar("stamp", stamp);
                stampIs.close();
            }
            if (!Objects.isNull(mc.getDirector_signature_id())) {
                FileInfoJSON fileDirSignatInfo = tservice.getFileInfo(mc.getDirector_signature_id());
                InputStream dirSignIs = new FileInputStream(new File(fileDirSignatInfo.getPath() + "/" + fileDirSignatInfo.getName()));
                byte[] dirSignature = Util.toByteArray(dirSignIs);
                context.putVar("dirSignature", dirSignature);
                dirSignIs.close();
            }
            if (!Objects.isNull(mc.getGlavbuh_signature_id())) {
                FileInfoJSON fileGbSignatInfo = tservice.getFileInfo(mc.getGlavbuh_signature_id());
                InputStream gbSignIs = new FileInputStream(new File(fileGbSignatInfo.getPath() + "/" + fileGbSignatInfo.getName()));
                byte[] gbSignature = Util.toByteArray(gbSignIs);
                context.putVar("gbSignature", gbSignature);
                gbSignIs.close();
            }

            JxlsHelper.getInstance().processTemplate(is, os, context);
        } catch (Exception e) {
            logger.error("Exception in method appointmentPrint.", e);
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }
}
