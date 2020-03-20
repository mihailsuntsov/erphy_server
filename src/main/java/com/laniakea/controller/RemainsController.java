package com.laniakea.controller;
import com.laniakea.message.request.*;
import com.laniakea.message.response.RemainsJSON;
import com.laniakea.repository.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import com.laniakea.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RemainsController {
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
    RemainsRepository remainsRepository;
    @Autowired
    StorageService storageService;

    @PostMapping("/api/auth/getProductRemainsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductsTable(@RequestBody RemainsForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int categoryId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        int cagentId;//контрагент
        Long departmentId; //отделение или если 0 - все доступные пользователю отделения, т.е. departmentsIdsList
        String departmentsIdsList;
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        RemainsJSON returnObject;

        departmentId=searchRequest.getDepartmentId();
        departmentsIdsList=searchRequest.getDepartmentsIdsList();

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

        if (searchRequest.getCagentId() != null && !searchRequest.getCagentId().isEmpty() && searchRequest.getCagentId().trim().length() > 0) {
            cagentId = Integer.parseInt(searchRequest.getCagentId());
        } else {
            cagentId = 0;
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
        returnObject = remainsRepository.getProductsTable(
                result,
                offset,
                offsetreal,
                searchString,
                sortColumn,
                sortAsc,
                companyId,
                categoryId,
                cagentId,
                departmentId,
                departmentsIdsList,
                searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal

        ResponseEntity responseEntity = new ResponseEntity<>(returnObject, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/saveRemains")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveRemains(@RequestBody RemainsForm request){
        if(remainsRepository.saveRemains(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
        }
    }

}
