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
package com.dokio.controller.Sprav;
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.TypePricesForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.PriceTypesListJSON;
import com.dokio.message.response.TypePricesTableJSON;
import com.dokio.model.Sprav.SpravTypePricesJSON;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.TypePricesRepositoryJPA;
import com.dokio.repository.UserRepository;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SpravTypePricesController {
    Logger logger = Logger.getLogger("SpravTypePricesController");

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    TypePricesRepositoryJPA typePricesRepositoryJPA;
    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/api/auth/getTypePricesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTypePricesTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getTypePricesTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int companyId;//по какому предприятию показывать / 0 - по всем
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<TypePricesTableJSON> returnList;

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
        returnList = typePricesRepositoryJPA.getTypePricesTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getTypePricesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTypePricesPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getTypePricesPagesList: " + searchRequest.toString());

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
        int size = typePricesRepositoryJPA.getTypePricesSize(searchString,companyId, searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
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

    @PostMapping("/api/auth/getTypePricesValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTypePricesValuesById(@RequestBody SearchForm request) {
        logger.info("Processing post request for path api/auth/getTypePricesValuesById: " + request.toString());
        SpravTypePricesJSON response;
        int id = request.getId();
        response=typePricesRepositoryJPA.getTypePricesValuesById(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<SpravTypePricesJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/insertTypePrices")
    public  ResponseEntity<?> insertTypePrices(@RequestBody TypePricesForm request) {
        logger.info("Processing post request for path /api/auth/insertTypePrices: " + request.toString());
        try {return new ResponseEntity<>(typePricesRepositoryJPA.insertTypePrices(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller insertTypePrices error", e);
            return new ResponseEntity<>("Ошибка", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateTypePrices")
    public  ResponseEntity<?> updateTypePrices(@RequestBody TypePricesForm request) {
        logger.info("Processing post request for path /api/auth/updateTypePrices: " + request.toString());
        try {return new ResponseEntity<>(typePricesRepositoryJPA.updateTypePrices(request), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller updateTypePrices error", e);
            return new ResponseEntity<>("Ошибка", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/deleteTypePrices")
    public  ResponseEntity<?> deleteTypePrices(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteTypePrices: " + request.toString());
        String checked = request.getChecked() == null ? "": request.getChecked();
        try {return new ResponseEntity<>(typePricesRepositoryJPA.deleteTypePrices(checked), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller deleteTypePrices error", e);
            return new ResponseEntity<>("Ошибка удаления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/undeleteTypePrices")
    public  ResponseEntity<?> undeleteTypePrices(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/undeleteTypePrices: " + request.toString());
        String checked = request.getChecked() == null ? "" : request.getChecked();
        try {return new ResponseEntity<>(typePricesRepositoryJPA.undeleteTypePrices(checked), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller undeleteTypePrices error", e);
            return new ResponseEntity<>("Ошибка восстановления", HttpStatus.INTERNAL_SERVER_ERROR);}
    }


    @PostMapping("/api/auth/getPriceTypesList")//возвращает список отделений предприятия по его id
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getPriceTypesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path api/auth/getPriceTypesList: " + searchRequest.toString());

        int companyId=Integer.parseInt(searchRequest.getCompanyId());
        List<PriceTypesListJSON> priceTypesList;
        priceTypesList = typePricesRepositoryJPA.getPriceTypesList(companyId);
        ResponseEntity<List> responseEntity = new ResponseEntity<>(priceTypesList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/setDefaultPriceType")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> setDefaultPriceType(@RequestBody UniversalForm request){
        if(typePricesRepositoryJPA.setDefaultPriceType(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка назначения типа цены по-умолчанию", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

}
