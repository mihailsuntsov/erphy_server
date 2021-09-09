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
import com.dokio.message.response.*;
import com.dokio.message.response.additional.FilesJSON;
import com.dokio.message.response.additional.FilesTableJSON;
import com.dokio.model.FileCategories;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FilesController {
    Logger logger = Logger.getLogger("FilesController");

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
    FileRepositoryJPA filesRepositoryJPA;
    @Autowired
    StorageService storageService;
    @Autowired
    FileRepositoryJPA fileRepository;

    @PostMapping("/api/auth/getFilesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getFilesTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getFilesTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int categoryId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<FilesTableJSON> returnList;

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
        if (searchRequest.getCategoryId() != null && !searchRequest.getCategoryId().isEmpty() && searchRequest.getCategoryId().trim().length() > 0) {
            categoryId = Integer.parseInt(searchRequest.getCategoryId());
        } else {
            categoryId = 0;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы/
        // searchRequest.isAny_boolean() - тут приходит true - показывать файлы корзины, false - показывать неудаленные файлы
        // searchRequest.isAny_boolean2() - true - показываем только files.anonyme_access доступ, false - все файлы
        returnList = filesRepositoryJPA.getFilesTable(
                result,
                offsetreal,
                searchString,
                sortColumn,
                sortAsc,
                companyId,
                categoryId,
                searchRequest.isAny_boolean(),
                searchRequest.isAny_boolean2()
        );//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getFilesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getFilesPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getFilesPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать документы/ 0 - по всем
        int categoryId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        int disabledLINK;// номер страницы на паджинейшене, на которой мы сейчас. Изначально это 1.
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
        String sortColumn = searchRequest.getSortColumn();
        if (searchRequest.getCategoryId() != null && !searchRequest.getCategoryId().isEmpty() && searchRequest.getCategoryId().trim().length() > 0) {
            categoryId = Integer.parseInt(searchRequest.getCategoryId());
        } else {
            categoryId = 0;}
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = filesRepositoryJPA.getFilesSize(
                searchString,
                companyId,
                categoryId,
                searchRequest.isAny_boolean(),
                searchRequest.isAny_boolean2()
        );//  - общее количество записей выборки
        int offsetreal = offset * result;//создана переменная с номером страницы
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


    @PostMapping("/api/auth/getFileValues")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTypePricesValuesById(@RequestBody SearchForm request) {
        logger.info("Processing post request for path /api/auth/getFileValues: " + request.toString());

        FilesJSON response;
        int id = request.getId();
        response=filesRepositoryJPA.getFileValues(id);//результат запроса помещается в экземпляр класса
        try
        {
            List<Integer> valuesListId =filesRepositoryJPA.getFilesCategoriesIdsByFileId(Long.valueOf(id));
            response.setFile_categories_id(valuesListId);

            ResponseEntity<FilesJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
            return responseEntity;
        }
        catch(NullPointerException npe){return null;}

    }

//    @PostMapping("/api/auth/insertFile")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> insertFile(@RequestBody FilesForm request) throws ParseException {
//        Long newDocument = filesRepositoryJPA.insertFile(request);
//        if(newDocument!=null && newDocument>0){
//            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
//            return responseEntity;
//        } else {
//            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
//            return responseEntity;
//        }
//    }
    @PostMapping("/api/auth/updateFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateFiles(@RequestBody FilesForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateFiles: " + request.toString());

        if(filesRepositoryJPA.updateFiles(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteFiles")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteFiles(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteFiles: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(filesRepositoryJPA.deleteFiles(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/recoverFilesFromTrash")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> recoverFilesFromTrash(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/recoverFilesFromTrash: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(filesRepositoryJPA.recoverFilesFromTrash(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when repairing", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteFilesFromTrash")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteFilesFromTrash(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteFilesFromTrash: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(filesRepositoryJPA.deleteFilesFromTrash(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/clearTrash")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> clearTrash(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/clearTrash: " + request.toString());

        Integer companyId = Integer.parseInt(request.getCompany_id());
        if(filesRepositoryJPA.clearTrash(companyId)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("Duplicates")
    @GetMapping("/api/public/getFile/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFilePublic(@PathVariable String filename) throws UnsupportedEncodingException {
        logger.info("Processing get request for path /api/public/getFile: filename=" + filename);

        FileInfoJSON fileInfo = fileRepository.getFilePublic(filename);
        if(fileInfo !=null){
            String filePath=fileInfo.getPath()+"//"+filename;
            String originalFileName = fileInfo.getOriginal_name();
            Resource file = storageService.loadFile(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(originalFileName, "UTF-8").replace("+", " ") + "\"")
                    .body(file);
        } else {ResponseEntity responseEntity = new ResponseEntity<>("Недостаточно прав на файл, или файла нет в базе данных.", HttpStatus.FORBIDDEN);
            return responseEntity;}
    }


    @SuppressWarnings("Duplicates")
    @GetMapping("/api/auth/getFile/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFileAuth(@PathVariable String filename) throws UnsupportedEncodingException {
        logger.info("Processing get request for path /api/auth/getFile: filename=" + filename);

        FileInfoJSON fileInfo = fileRepository.getFileAuth(filename);
        if(fileInfo !=null){
            String filePath=fileInfo.getPath()+"//"+filename;
            String originalFileName = fileInfo.getOriginal_name();
            Resource file = storageService.loadFile(filePath);
            HttpHeaders h = new HttpHeaders();
            h.add("Content-type", "text/html;charset=UTF-8");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(originalFileName, "UTF-8").replace("+", " ") + "\"")
                    .body(file);
        } else {ResponseEntity responseEntity = new ResponseEntity<>("Недостаточно прав на файл, или файла нет в базе данных.", HttpStatus.FORBIDDEN);
            return responseEntity;}
    }

    @SuppressWarnings("Duplicates")
    @GetMapping("/api/auth/getFileImageThumb/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFileImageThumb(@PathVariable String filename) throws UnsupportedEncodingException {
        logger.info("Processing get request for path /api/auth/getFileImageThumb: filename=" + filename);

        FileInfoJSON fileInfo = fileRepository.getFileAuth(filename);
        if(fileInfo !=null){
            String filePath=fileInfo.getPath()+"//thumbs//"+filename;
            String originalFileName = fileInfo.getOriginal_name();
            Resource file = storageService.loadFile(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(originalFileName, "UTF-8").replace("+", " ") + "\"")
                    .body(file);
        } else {ResponseEntity responseEntity = new ResponseEntity<>("Недостаточно прав на файл, или файла нет в базе данных.", HttpStatus.FORBIDDEN);
            return responseEntity;}
    }
    //*************************************************************************************************************************************************
//**************************************************  C A T E G O R I E S  ************************************************************************
//*************************************************************************************************************************************************
    @PostMapping("/api/auth/getFileCategoriesTrees")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getFileCategoriesTrees(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getFileCategoriesTrees: " + request.toString());

        List<FileCategories> returnList;
        List<Integer> categoriesRootIds = filesRepositoryJPA.getCategoriesRootIds(Long.valueOf(Integer.parseInt((request.getCompanyId()))));//
        try {
            returnList = filesRepositoryJPA.getFileCategoriesTrees(categoriesRootIds);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getRootFileCategories")
    @SuppressWarnings("Duplicates")
    //отдает только список корневых категорий, без детей
    //нужно для изменения порядка вывода корневых категорий
    public ResponseEntity<?> getRootFileCategories(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getRootFileCategories: " + request.toString());

        List<FileCategoriesTableJSON> returnList ;//
        try {
            returnList = filesRepositoryJPA.getRootFileCategories(Long.valueOf(Integer.parseInt((request.getCompanyId()))));
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }


    @PostMapping("/api/auth/getChildrensFileCategories")//нужно для изменения порядка вывода категорий
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getChildrensFileCategories(@RequestBody FileCategoriesForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getChildrensFileCategories: " + request.toString());

        List<FileCategoriesTableJSON> returnList;
        try {
            returnList = filesRepositoryJPA.getChildrensFileCategories(request.getParentCategoryId());
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/searchFileCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> searchFileCategory(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/searchFileCategory: " + request.toString());

        Long companyId=Long.valueOf(Integer.parseInt((request.getCompanyId())));
        List<FileCategoriesTableJSON> returnList;
        try {
            returnList = filesRepositoryJPA.searchFileCategory(companyId,request.getSearchString());
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/insertFileCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertFileCategory(@RequestBody FileCategoriesForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/insertFileCategory: " + request.toString());

        try {
            Long categoryId = filesRepositoryJPA.insertFileCategory(request);
            ResponseEntity<Long> responseEntity = new ResponseEntity<>(categoryId, HttpStatus.OK);
            return responseEntity;
        }
        catch (Exception e) {
            e.printStackTrace();
            ResponseEntity<Long> responseEntity = new ResponseEntity<>(0L, HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateFileCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateFileCategory(@RequestBody FileCategoriesForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateFileCategory: " + request.toString());

        if(filesRepositoryJPA.updateFileCategory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteFileCategory")
    public ResponseEntity<?> deleteFileCategory(@RequestBody FileCategoriesForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteFileCategory: " + request.toString());

        if(filesRepositoryJPA.deleteFileCategory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/saveChangeFileCategoriesOrder")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveChangeFileCategoriesOrder(@RequestBody List<FileCategoriesForm> request) throws ParseException {
        logger.info("Processing post request for path /api/auth/saveChangeFileCategoriesOrder: [" + request.stream().
                map(FileCategoriesForm::toString).collect(Collectors.joining(", ")) + "]");

        if(filesRepositoryJPA.saveChangeCategoriesOrder(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when saving", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

}
