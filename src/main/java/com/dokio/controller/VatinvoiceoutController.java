package com.dokio.controller;
//
//import com.dokio.message.request.VatinvoiceoutForm;
//import com.dokio.message.request.SearchForm;
//import com.dokio.message.request.Settings.SettingsVatinvoiceoutForm;
//import com.dokio.message.request.SignUpForm;
//import com.dokio.message.request.UniversalForm;
//import com.dokio.message.response.VatinvoiceoutJSON;
//import com.dokio.repository.VatinvoiceoutRepositoryJPA;
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Controller
public class VatinvoiceoutController {
//
//    Logger logger = Logger.getLogger("VatinvoiceoutController");
//
//    @Autowired
//    VatinvoiceoutRepositoryJPA vatinvoiceoutRepository;
//
//    @PostMapping("/api/auth/getVatinvoiceoutTable")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> getVatinvoiceoutTable(@RequestBody SearchForm searchRequest) {
//        logger.info("Processing post request for path /api/auth/getVatinvoiceoutTable: " + searchRequest.toString());
//
//        int offset; // номер страницы. Изначально это null
//        int result; // количество записей, отображаемых на странице
//        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
//        int departmentId;//по какому отделению показывать / 0 - по всем (--//--//--//--//--//--//--)
//        String searchString = searchRequest.getSearchString();
//        String sortColumn = searchRequest.getSortColumn();
//        String sortAsc;
//        List<VatinvoiceoutJSON> returnList;
//
//        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
//            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
//        } else {
//            sortColumn = "name";
//            sortAsc = "asc";
//        }
//        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
//            result = Integer.parseInt(searchRequest.getResult());
//        } else {
//            result = 10;
//        }
//        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
//            companyId = Integer.parseInt(searchRequest.getCompanyId());
//        } else {
//            companyId = 0;
//        }
//        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
//            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
//        } else {
//            departmentId = 0;
//        }
//        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
//            offset = Integer.parseInt(searchRequest.getOffset());
//        } else {
//            offset = 0;
//        }
//        int offsetreal = offset * result;//создана переменная с номером страницы
//        returnList = vatinvoiceoutRepository.getVatinvoiceoutTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
//        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
//        return responseEntity;
//    }
//
//    @PostMapping("/api/auth/getVatinvoiceoutPagesList")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> getVatinvoiceoutPagesList(@RequestBody SearchForm searchRequest) {
//        logger.info("Processing post request for path /api/auth/getVatinvoiceoutPagesList: " + searchRequest.toString());
//
//        int offset; // номер страницы. Изначально это null
//        int result; // количество записей, отображаемых на странице
//        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
//        int companyId;//по какому предприятию показывать документы/ 0 - по всем
//        int departmentId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
//        String searchString = searchRequest.getSearchString();
//        companyId = Integer.parseInt(searchRequest.getCompanyId());
//        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
//            departmentId = Integer.parseInt(searchRequest.getDepartmentId());
//        } else {
//            departmentId = 0;}
//        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
//            result = Integer.parseInt(searchRequest.getResult());
//        } else {
//            result = 10;}
//        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
//            offset = Integer.parseInt(searchRequest.getOffset());
//        } else {
//            offset = 0;}
//        pagenum = offset + 1;
//        int size = vatinvoiceoutRepository.getVatinvoiceoutSize(searchString,companyId,departmentId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
//        int listsize;//количество страниц пагинации
//        if((size%result) == 0){//общее количество выборки делим на количество записей на странице
//            listsize= size/result;//если делится без остатка
//        }else{
//            listsize= (size/result)+1;}
//        int maxPagenumInBegin;//
//        List<Integer> pageList = new ArrayList<Integer>();//список, в котором первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
//        pageList.add(size);
//        pageList.add(pagenum);
//        pageList.add(listsize);
//
//        if (listsize<=5){
//            maxPagenumInBegin=listsize;//
//        }else{
//            maxPagenumInBegin=5;
//        }
//        if(pagenum >=3) {
//            if((pagenum==listsize)||(pagenum+1)==listsize){
//                for(int i=(pagenum-(4-(listsize-pagenum))); i<=pagenum-3; i++){
//                    if(i>0) {
//                        pageList.add(i);  //создается список пагинации за - 4 шага до номера страницы (для конца списка пагинации)
//                    }}}
//            for(int i=(pagenum-2); i<=pagenum; i++){
//                pageList.add(i);  //создается список пагинации за -2 шага до номера страницы
//            }
//            if((pagenum+2) <=listsize) {
//                for(int i=(pagenum+1); i<=(pagenum+2); i++){
//                    pageList.add(i);  //создается список пагинации  на +2 шага от номера страницы
//                }
//            }else{
//                if(pagenum<listsize) {
//                    for (int i = (pagenum + (listsize - pagenum)); i <= listsize; i++) {
//                        pageList.add(i);  //создается список пагинации от номера страницы до конца
//                    }}}
//        }else{//номер страницы меньше 3
//            for(int i=1; i<=pagenum; i++){
//                pageList.add(i);  //создается список пагинации от 1 до номера страницы
//            }
//            for(int i=(pagenum+1); i<=maxPagenumInBegin; i++){
//                pageList.add(i);  //создаются дополнительные номера пагинации, но не более 5 в сумме
//            }}
//        ResponseEntity<List> responseEntity = new ResponseEntity<>(pageList, HttpStatus.OK);
//        return responseEntity;
//    }
//
//    @PostMapping("/api/auth/insertVatinvoiceout")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> insertVatinvoiceout(@RequestBody VatinvoiceoutForm request){
//        logger.info("Processing post request for path /api/auth/insertVatinvoiceout: " + request.toString());
//        return new ResponseEntity<>(vatinvoiceoutRepository.insertVatinvoiceout(request), HttpStatus.OK);
//    }
//
//    @RequestMapping(
//            value = "/api/auth/getVatinvoiceoutValuesById",
//            params = {"id"},
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getVatinvoiceoutValuesById(
//            @RequestParam("id") Long id){
//        logger.info("Processing get request for path /api/auth/getVatinvoiceoutValuesById with parameters: " + "id: " + id);
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.getVatinvoiceoutValuesById(id), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Error loading document values", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @PostMapping("/api/auth/updateVatinvoiceout")
//    public ResponseEntity<?> updateVatinvoiceout(@RequestBody VatinvoiceoutForm request){
//        logger.info("Processing post request for path /api/auth/updateVatinvoiceout: " + request.toString());
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.updateVatinvoiceout(request), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Error saving document", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @PostMapping("/api/auth/saveSettingsVatinvoiceout")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> saveSettingsVatinvoiceout(@RequestBody SettingsVatinvoiceoutForm request){
//        logger.info("Processing post request for path /api/auth/saveSettingsVatinvoiceout: " + request.toString());
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.saveSettingsVatinvoiceout(request), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Ошибка сохранения настроек для документа", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @SuppressWarnings("Duplicates")
//    @RequestMapping(
//            value = "/api/auth/getSettingsVatinvoiceout",
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getSettingsVatinvoiceout(){
//        logger.info("Processing get request for path /api/auth/getSettingsVatinvoiceout without request parameters");
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.getSettingsVatinvoiceout(), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Ошибка загрузки настроек", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @PostMapping("/api/auth/deleteVatinvoiceout")
//    @SuppressWarnings("Duplicates")
//    public  ResponseEntity<?> deleteVatinvoiceout(@RequestBody SignUpForm request) {
//        logger.info("Processing post request for path /api/auth/deleteVatinvoiceout: " + request.toString());
//        String checked = request.getChecked() == null ? "": request.getChecked();
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.deleteVatinvoiceout(checked), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Deletion error", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @PostMapping("/api/auth/undeleteVatinvoiceout")
//    @SuppressWarnings("Duplicates")
//    public  ResponseEntity<?> undeleteVatinvoiceout(@RequestBody SignUpForm request) {
//        logger.info("Processing post request for path /api/auth/undeleteVatinvoiceout: " + request.toString());
//        String checked = request.getChecked() == null ? "" : request.getChecked();
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.undeleteVatinvoiceout(checked), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Restore error", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @RequestMapping(
//            value = "/api/auth/getListOfVatinvoiceoutFiles",
//            params = {"id"},
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getListOfVatinvoiceoutFiles(
//            @RequestParam("id") Long id){
//        logger.info("Processing post request for path api/auth/getListOfVatinvoiceoutFiles: " + id);
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.getListOfVatinvoiceoutFiles(id), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @PostMapping("/api/auth/deleteVatinvoiceoutFile")
//    public ResponseEntity<?> deleteVatinvoiceoutFile(@RequestBody SearchForm request) {
//        logger.info("Processing post request for path api/auth/deleteVatinvoiceoutFile: " + request.toString());
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.deleteVatinvoiceoutFile(request), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("File deletion error", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @SuppressWarnings("Duplicates")
//    @PostMapping("/api/auth/addFilesToVatinvoiceout")
//    public ResponseEntity<?> addFilesToVatinvoiceout(@RequestBody UniversalForm request) {
//        logger.info("Processing post request for path api/auth/addFilesToVatinvoiceout: " + request.toString());
//        try{return new ResponseEntity<>(vatinvoiceoutRepository.addFilesToVatinvoiceout(request), HttpStatus.OK);}
//        catch (Exception e){return new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
//
//    @PostMapping("/api/auth/setVatinvoiceoutAsDecompleted")
//    public ResponseEntity<?> setVatinvoiceoutAsDecompleted(@RequestBody VatinvoiceoutForm request){
//        logger.info("Processing post request for path /api/auth/setVatinvoiceoutAsDecompleted: " + request.toString());
//        try {return new ResponseEntity<>(vatinvoiceoutRepository.setVatinvoiceoutAsDecompleted(request), HttpStatus.OK);}
//        catch (Exception e){e.printStackTrace();logger.error("Controller setVatinvoiceoutAsDecompleted error", e);
//            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
}
