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
import java.util.Objects;
import java.util.Set;
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
        String searchString = searchRequest.getSearchString();
        companyId = Integer.parseInt(searchRequest.getCompanyId());
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
    public ResponseEntity<?> getFileValues(@RequestBody SearchForm request) {
        logger.info("Processing post request for path /api/auth/getFileValues: " + request.toString());

        FilesJSON response;
        int id = request.getId();
        response=filesRepositoryJPA.getFileValues(id);//результат запроса помещается в экземпляр класса
        try
        {
            List<Long> valuesListId =filesRepositoryJPA.getFilesCategoriesIdsByFileId(Long.valueOf(id));
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
    public  ResponseEntity<?> updateFiles(@RequestBody FilesForm request) {
        logger.info("Processing post request for path /api/auth/updateFiles: " + request.toString());
        try {return new ResponseEntity<>(filesRepositoryJPA.updateFiles(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateFiles error", e);
            return new ResponseEntity<>("Error update files from trash", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteFiles")
    public  ResponseEntity<?> deleteFiles(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteFiles: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(filesRepositoryJPA.deleteFiles(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteFiles error", e);
            return new ResponseEntity<>("Error deleting files to trash", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/recoverFilesFromTrash")
    public  ResponseEntity<?> recoverFilesFromTrash(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/recoverFilesFromTrash: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(filesRepositoryJPA.recoverFilesFromTrash(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller recoverFilesFromTrash error", e);
            return new ResponseEntity<>("Error recovering files from trash", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteFilesFromTrash")
    public  ResponseEntity<?> deleteFilesFromTrash(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteFilesFromTrash: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(filesRepositoryJPA.deleteFilesFromTrash(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteFilesFromTrash error", e);
            return new ResponseEntity<>("Error deleting files from trash", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/clearTrash")
    public  ResponseEntity<?> clearTrash(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/clearTrash: " + request.toString());
        try {return new ResponseEntity<>(filesRepositoryJPA.clearTrash(Long.parseLong(request.getCompany_id())), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller clearTrash error", e);
            return new ResponseEntity<>("Error cleaning trash", HttpStatus.INTERNAL_SERVER_ERROR);}
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
//    @PostMapping("/api/auth/getFileCategoriesTrees")
//    @SuppressWarnings("Duplicates")
//    public ResponseEntity<?> getFileCategoriesTrees(@RequestBody SearchForm request) throws ParseException {
//        logger.info("Processing post request for path /api/auth/getFileCategoriesTrees: " + request.toString());
//
//        List<FileCategories> returnList;
//        List<Integer> categoriesRootIds = filesRepositoryJPA.getCategoriesRootIds(Long.valueOf(Integer.parseInt((request.getCompanyId()))));//
//        try {
//            returnList = filesRepositoryJPA.getFileCategoriesTrees(categoriesRootIds);
//            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
//            return responseEntity;
//        } catch (Exception e){
//            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
//            return responseEntity;
//        }
//    }


    @RequestMapping(
            value = "/api/auth/getFileCategoriesTrees",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getFileCategoriesTrees( @RequestParam("company_id") Long companyId) {
        logger.info("Processing get request for path /api/auth/getFileCategoriesTrees with fileId=" + companyId.toString());
        List<FileCategories> returnList;
        List<Integer> categoriesRootIds = filesRepositoryJPA.getCategoriesRootIds(companyId);//
        try {
            returnList = filesRepositoryJPA.getFileCategoriesTrees(categoriesRootIds);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        }
        catch (Exception e){e.printStackTrace();logger.error("Controller getFileCategoriesTrees error with companyId=" + companyId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
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
    public ResponseEntity<?> insertFileCategory(@RequestBody FileCategoriesForm request) {
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
    public ResponseEntity<?> updateFileCategory(@RequestBody FileCategoriesForm request){
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
    public ResponseEntity<?> deleteFileCategory(@RequestBody FileCategoriesForm request){
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
    public ResponseEntity<?> saveChangeFileCategoriesOrder(@RequestBody List<FileCategoriesForm> request){
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

    @PostMapping("/api/auth/setCategoriesToFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> setCategoriesToFiles(@RequestBody UniversalForm form) {
        logger.info("Processing post request for path api/auth/setCategoriesToFiles: " + form.toString());

        Set<Long> filesIds = form.getSetOfLongs1();
        Set<Long> categoriesIds = form.getSetOfLongs2();
        Boolean save = form.getYesNo();

        Boolean result = filesRepositoryJPA.setCategoriesToFiles(filesIds, categoriesIds, save);
        if (!Objects.isNull(result)) {//вернет true - ок, false - недостаточно прав,  null - ошибка
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error assigning categories to files!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getImageFileInfo",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getImageFileInfo( @RequestParam("id") Long fileId) {
        logger.info("Processing get request for path /api/auth/getImageFileInfo with fileId=" + fileId.toString());
        try {return new ResponseEntity<>(fileRepository.getImageFileInfo(fileId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getImageFileInfo error with categoryId=" + fileId.toString(), e);
            return new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/setFilesExternalAccess")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> setFilesExternalAccess(@RequestBody UniversalForm form) {
        logger.info("Processing post request for path api/auth/setFilesExternalAccess: " + form.toString());
        Set<Long> filesIds = form.getSetOfLongs1();
        Boolean access = form.getYesNo();
        Boolean result = filesRepositoryJPA.setFilesExternalAccess(filesIds, access);
        if (!Objects.isNull(result)) {//вернет true - ок, false - недостаточно прав,  null - ошибка
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error assigning external access to files!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
