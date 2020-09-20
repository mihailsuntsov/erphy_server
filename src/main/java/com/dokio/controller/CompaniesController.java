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
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.message.request.CompaniesForm;
import com.dokio.message.request.SearchForm;
import com.dokio.message.response.FilesCompaniesJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.UserGroupRepositoryJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CompaniesController {

    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;

    @PostMapping("/api/auth/getCompaniesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompaniesTable(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<CompaniesJSON> returnList;

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
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = companyRepositoryJPA.getCompaniesTable(result, offsetreal, searchString, sortColumn, sortAsc);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getCompaniesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompaniesList() {
        List<IdAndName> companiesList;
        companiesList = companyRepositoryJPA.getCompaniesList();
        return new ResponseEntity<>(companiesList, HttpStatus.OK);
    }

    @PostMapping("/api/auth/insertCompany")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertCompany(@RequestBody CompaniesForm request){
        Long newDocument = companyRepositoryJPA.insertCompany(request);
        if(newDocument!=null && newDocument>0){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteCompanies")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteCompanies(@RequestBody SignUpForm request){
        String checked = request.getChecked() == null ? "": request.getChecked();
        if(companyRepositoryJPA.deleteCompanies(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when requesting deleteCompanies", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getCompaniesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompaniesPagesList(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        String searchString = searchRequest.getSearchString();

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = companyRepositoryJPA.getCompaniesSize(searchString);//  - общее количество записей выборки
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

    @PostMapping("/api/auth/getCompanyValues")//Отдает ЗНАЧЕНИЯ из таблицы companies по id предприятия
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompanyValues(@RequestBody CompaniesForm companyRequest) {
        CompaniesJSON company;
        Long id = companyRequest.getId();
        company=companyRepositoryJPA.getCompanyValues(id);//результат запроса помещается в объект
        ResponseEntity<CompaniesJSON> responseEntity = new ResponseEntity<>(company, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/updateCompany")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateCompany(@RequestBody CompaniesForm companyRequest) {
        if(companyRepositoryJPA.updateCompany(companyRequest)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getCompaniesPaymentAccounts")// отдаёт список банковских счетов контрагента
    public ResponseEntity<?> getCompaniesPaymentAccounts(@RequestBody UniversalForm searchRequest) {
        try {
            return  new ResponseEntity<>(companyRepositoryJPA.getCompanyPaymentAccounts(searchRequest.getId()), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Error when requesting getCompanyPaymentAccounts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @PostMapping("/api/auth/getListOfCompanyFiles")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfCompanyFiles(@RequestBody UniversalForm request)  {
        Long companyId=request.getId();
        List<FilesCompaniesJSON> returnList;
        try {
            returnList = companyRepositoryJPA.getListOfCompanyFiles(companyId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Error when requesting list of files", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/deleteCompanyFile")
    public ResponseEntity<?> deleteCompanyFile(@RequestBody SearchForm request) {
        if(companyRepositoryJPA.deleteCompanyFile(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when deleting file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToCompany")
    public ResponseEntity<?> addFilesToCompany(@RequestBody UniversalForm request) {
        if(companyRepositoryJPA.addFilesToCompany(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when adding file", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}