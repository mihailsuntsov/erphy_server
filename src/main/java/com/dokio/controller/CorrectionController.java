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

import com.dokio.message.request.SearchForm;
import com.dokio.message.request.Settings.SettingsCorrectionForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.CorrectionForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.CorrectionJSON;
import com.dokio.repository.CorrectionRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
class CorrectionController {

    Logger logger = Logger.getLogger("CorrectionController");

    @Autowired
    CorrectionRepositoryJPA correctionRepository;

    @PostMapping("/api/auth/getCorrectionTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCorrectionTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getCorrectionTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<CorrectionJSON> returnList;

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
        returnList = correctionRepository.getCorrectionTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getCorrectionPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCorrectionPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getCorrectionPagesList: " + searchRequest.toString());

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
        int size = correctionRepository.getCorrectionSize(searchString,companyId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/insertCorrection")
    public ResponseEntity<?> insertCorrection(@RequestBody CorrectionForm request){
        logger.info("Processing post request for path /api/auth/insertCorrection: " + request.toString());
        try {return new ResponseEntity<>(correctionRepository.insertCorrection(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller insertCorrection error", e);
            return new ResponseEntity<>("Ошибка создания документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getCorrectionValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getCorrectionValuesById(
            @RequestParam("id") Long id){
        logger.info("Processing get request for path /api/auth/getCorrectionValuesById with parameters: " + "id: " + id);
        try {return new ResponseEntity<>(correctionRepository.getCorrectionValues(id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getCorrectionValuesById error", e);
        return new ResponseEntity<>("Ошибка загрузки значений документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateCorrection")
    public ResponseEntity<?> updateCorrection(@RequestBody CorrectionForm request){
        logger.info("Processing post request for path /api/auth/updateCorrection: " + request.toString());
        try {return new ResponseEntity<>(correctionRepository.updateCorrection(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller updateCorrection error", e);
        return new ResponseEntity<>("Ошибка сохранения документа", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteCorrection")
    public  ResponseEntity<?> deleteCorrection(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteCorrection: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(correctionRepository.deleteCorrection(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller deleteCorrection error", e);
        return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/undeleteCorrection")
    public  ResponseEntity<?> undeleteCorrection(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteCorrection: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(correctionRepository.undeleteCorrection(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller undeleteCorrection error", e);
        return new ResponseEntity<>("Ошибка восстановления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/saveSettingsCorrection")
    public ResponseEntity<?> saveSettingsCorrection(@RequestBody SettingsCorrectionForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsCorrection: " + request.toString());
        try {return new ResponseEntity<>(correctionRepository.saveSettingsCorrection(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller saveSettingsCorrection error", e);
        return new ResponseEntity<>("Ошибка сохранения настроек", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getSettingsCorrection",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsCorrection(){
        logger.info("Processing get request for path /api/auth/getSettingsCorrection without request parameters");
        try {return new ResponseEntity<>(correctionRepository.getSettingsCorrection(), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getSettingsCorrection error", e);
        return new ResponseEntity<>("Ошибка загрузки настроек", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getListOfCorrectionFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfCorrectionFiles(
            @RequestParam("id") Long id){
        logger.info("Processing post request for path api/auth/getListOfCorrectionFiles: " + id);
        try {return new ResponseEntity<>(correctionRepository.getListOfCorrectionFiles(id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getListOfCorrectionFiles error", e);
        return new ResponseEntity<>("Ошибка запроса списка файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteCorrectionFile")
    public ResponseEntity<?> deleteCorrectionFile(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/deleteCorrectionFile: " + request.toString());
        try {return new ResponseEntity<>(correctionRepository.deleteCorrectionFile(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller deleteCorrectionFile error", e);
        return new ResponseEntity<>("Ошибка удаления файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToCorrection")
    public ResponseEntity<?> addFilesToCorrection(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path api/auth/addFilesToCorrection: " + request.toString());
        try{return new ResponseEntity<>(correctionRepository.addFilesToCorrection(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller addFilesToCorrection error", e);
        return new ResponseEntity<>("Ошибка добавления файлов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/setCorrectionAsDecompleted")
    public ResponseEntity<?> setCorrectionAsDecompleted(@RequestBody CorrectionForm request){
        logger.info("Processing post request for path /api/auth/setCorrectionAsDecompleted: " + request.toString());
        try {return new ResponseEntity<>(correctionRepository.setCorrectionAsDecompleted(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Contrloller setCorrectionAsDecompleted error", e);
            return new ResponseEntity<>("Ошибка запроса на снятие с проведения", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
