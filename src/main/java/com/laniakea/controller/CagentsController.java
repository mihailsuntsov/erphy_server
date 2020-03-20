package com.laniakea.controller;

import com.laniakea.message.request.*;
import com.laniakea.message.response.*;
import com.laniakea.model.CagentCategories;
import com.laniakea.repository.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import com.laniakea.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CagentsController {

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
    CagentRepositoryJPA cagentsRepositoryJPA;
    @Autowired
    StorageService storageService;


    @PostMapping("/api/auth/getCagentsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCagentsTable(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int categoryId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<CagentsTableJSON> returnList;

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
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = cagentsRepositoryJPA.getCagentsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,categoryId);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getCagentsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCagentsPagesList(@RequestBody SearchForm searchRequest) {
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
        int size = cagentsRepositoryJPA.getCagentsSize(searchString,companyId,categoryId);//  - общее количество записей выборки
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


    @PostMapping("/api/auth/getCagentValues")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCagentValuesById(@RequestBody SearchForm request) {
        CagentsJSON response;
        int id = request.getId();
        response=cagentsRepositoryJPA.getCagentValues(id);//результат запроса помещается в экземпляр класса
        try
        {
            List<Integer> valuesListId =cagentsRepositoryJPA.getCagentsCategoriesIdsByCagentId(Long.valueOf(id));
            response.setCagent_categories_id(valuesListId);

            ResponseEntity<CagentsJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
            return responseEntity;
        }
        catch(NullPointerException npe){return null;}
    }

    @PostMapping("/api/auth/insertCagent")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertCagent(@RequestBody CagentsForm request) throws ParseException {
        Long newDocument = cagentsRepositoryJPA.insertCagent(request);
        if(newDocument!=null && newDocument>0){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/updateCagents")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateCagents(@RequestBody CagentsForm request) throws ParseException{
        if(cagentsRepositoryJPA.updateCagents(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteCagents")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteCagents(@RequestBody SignUpForm request) throws ParseException{
        String checked = request.getChecked() == null ? "": request.getChecked();
        if(cagentsRepositoryJPA.deleteCagents(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getCagentsList")//заполнение Autocomplete для поля "Группа товаров" документа "Товары и услуги"
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCagentsList(@RequestBody SearchForm searchRequest) {
        int companyId;
        String searchString = searchRequest.getSearchString();
        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Integer.parseInt(searchRequest.getCompanyId());
        } else { return null; }
        List<CagentsListJSON> returnList;
        returnList = cagentsRepositoryJPA.getCagentsList(searchString, companyId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    //*************************************************************************************************************************************************
//**************************************************  C A T E G O R I E S  ************************************************************************
//*************************************************************************************************************************************************
    @PostMapping("/api/auth/getCagentCategoriesTrees")
    @SuppressWarnings("Duplicates")
    //отправляет ID предприятия. По нему в getCategoriesRootIds ищутся id корневых категорий, и уже по ним грузятся деревья категорий
    public ResponseEntity<?> getCagentCategoriesTrees(@RequestBody SearchForm request) throws ParseException {
        List<CagentCategories> returnList;
        List<Integer> categoriesRootIds = cagentsRepositoryJPA.getCategoriesRootIds(Long.valueOf(Integer.parseInt((request.getCompanyId()))));//
        try {
            returnList = cagentsRepositoryJPA.getCagentCategoriesTrees(categoriesRootIds);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getRootCagentCategories")
    @SuppressWarnings("Duplicates")
    //отдает только список корневых категорий, без детей
    //нужно для изменения порядка вывода корневых категорий
    public ResponseEntity<?> getRootCagentCategories(@RequestBody SearchForm request) throws ParseException {
        List<CagentCategoriesTableJSON> returnList ;//
        try {
            returnList = cagentsRepositoryJPA.getRootCagentCategories(Long.valueOf(Integer.parseInt((request.getCompanyId()))));
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getChildrensCagentCategories")
    @SuppressWarnings("Duplicates")
    //отдает только список детей, без их детей
    //нужно для изменения порядка вывода категорий
    public ResponseEntity<?> getChildrensCagentCategories(@RequestBody CagentCategoriesForm request) throws ParseException {
        List<CagentCategoriesTableJSON> returnList;
        try {
            returnList = cagentsRepositoryJPA.getChildrensCagentCategories(request.getParentCategoryId());
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/searchCagentCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> searchCagentCategory(@RequestBody SearchForm request) throws ParseException {
        Long companyId=Long.valueOf(Integer.parseInt((request.getCompanyId())));
        List<CagentCategoriesTableJSON> returnList;
        try {
            returnList = cagentsRepositoryJPA.searchCagentCategory(companyId,request.getSearchString());
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/insertCagentCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertCagentCategory(@RequestBody CagentCategoriesForm request) throws ParseException {
        try {
            Long categoryId = cagentsRepositoryJPA.insertCagentCategory(request);
            ResponseEntity<Long> responseEntity = new ResponseEntity<>(categoryId, HttpStatus.OK);
            return responseEntity;
        }
        catch (Exception e) {
            e.printStackTrace();
            ResponseEntity<Long> responseEntity = new ResponseEntity<>(0L, HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateCagentCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateCagentCategory(@RequestBody CagentCategoriesForm request) throws ParseException{
        if(cagentsRepositoryJPA.updateCagentCategory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteCagentCategory")
    public ResponseEntity<?> deleteCagentCategory(@RequestBody CagentCategoriesForm request) throws ParseException{
        if(cagentsRepositoryJPA.deleteCagentCategory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/saveChangeCagentCategoriesOrder")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveChangeCagentCategoriesOrder(@RequestBody List<CagentCategoriesForm> request) throws ParseException {
        if(cagentsRepositoryJPA.saveChangeCategoriesOrder(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when saving", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

}
