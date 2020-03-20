package com.laniakea.controller;
import com.laniakea.message.request.*;
import com.laniakea.message.response.PricesJSON;
import com.laniakea.message.response.PricesTableJSON;
import com.laniakea.repository.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import com.laniakea.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PricesController {
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
    PricesRepository pricesRepository;
    @Autowired
    StorageService storageService;

    @PostMapping("/api/auth/getProductPricesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductPricesTable(@RequestBody PricesForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        Long companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        Long categoryId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        Long cagentId;//контрагент
        Long priceTypeId;//тип цены
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        String priceTypesIdsList;
        PricesJSON returnObject;

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
        companyId=searchRequest.getCompanyId();
        priceTypeId=searchRequest.getPriceTypeId();
        priceTypesIdsList=searchRequest.getPriceTypesIdsList();
        cagentId=searchRequest.getCagentId();
        categoryId=searchRequest.getCategoryId();

        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы

        returnObject = pricesRepository.getPricesTable(
                result,
                offset,
                offsetreal,
                searchString,
                sortColumn,
                sortAsc,
                companyId,
                categoryId,
                cagentId,
                priceTypeId,
                priceTypesIdsList,
                searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal

        ResponseEntity responseEntity = new ResponseEntity<>(returnObject, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/savePrices")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> savePrices(@RequestBody PricesForm request){
        if(pricesRepository.savePrices(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
        }
    }

}
