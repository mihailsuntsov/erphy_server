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
import com.dokio.message.response.PricesJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PricesController {
    Logger logger = Logger.getLogger("PricesController");

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
        logger.info("Processing post request for path /api/auth/getProductPricesTable: " + searchRequest.toString());

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
        logger.info("Processing post request for path /api/auth/savePrices: " + request.toString());

        if(pricesRepository.savePrices(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
        }
    }

}
