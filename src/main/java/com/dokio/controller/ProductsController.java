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

//import com.dokio.message.TestForm;
import com.dokio.message.request.*;
import com.dokio.message.response.*;
import com.dokio.message.response.additional.*;
import com.dokio.model.ProductCategories;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ProductsController {
    Logger logger = Logger.getLogger("ProductsController");

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
    ProductsRepositoryJPA productsRepositoryJPA;
    @Autowired
    StorageService storageService;


    @PostMapping("/api/auth/getProductsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductsTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductsTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        int categoryId;//по какой категории товаров показывать / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<ProductsTableJSON> returnList;

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
        returnList = productsRepositoryJPA.getProductsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId,categoryId);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }
    @PostMapping("/api/auth/getProductsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductsPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductsPagesList: " + searchRequest.toString());

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
        int size = productsRepositoryJPA.getProductsSize(searchString,companyId,categoryId);//  - общее количество записей выборки
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



    @PostMapping("/api/auth/getProductValues")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getTypePricesValuesById(@RequestBody UniversalForm request) {
        logger.info("Processing post request for path /api/auth/getProductValues: " + request.toString());

        ProductsJSON response;
        Long id = request.getId();
        response=productsRepositoryJPA.getProductValues(id);//результат запроса помещается в экземпляр класса


        try
        {
            List<Integer> valuesListId =productsRepositoryJPA.getProductsCategoriesIdsByProductId(Long.valueOf(id));
            response.setProduct_categories_id(valuesListId);

            ResponseEntity<ProductsJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
            return responseEntity;
        }
        catch(NullPointerException npe){return null;}

    }

    @PostMapping("/api/auth/insertProduct")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertProduct(@RequestBody ProductsForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/insertProduct: " + request.toString());

        Long newDocument = productsRepositoryJPA.insertProduct(request);
        if(newDocument!=null && newDocument>0){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/updateProducts")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateProducts(@RequestBody ProductsForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateProducts: " + request.toString());

        if(productsRepositoryJPA.updateProducts(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateProductCustomFields")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateProductCustomFields(@RequestBody List<ProductCustomFieldsSaveForm> request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateProductCustomFields: [" + request.stream().
                map(ProductCustomFieldsSaveForm::toString).collect(Collectors.joining(", ")) + "]");

        if(productsRepositoryJPA.updateProductCustomFields(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PostMapping("/api/auth/deleteProducts")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteProducts(@RequestBody SignUpForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteProducts: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(productsRepositoryJPA.deleteProducts(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductPrices",
            params = {"productId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductPrices( @RequestParam("productId") Long productId) {
        logger.info("Processing get request for path /api/auth/getProductPrices with productId=" + productId.toString());
        List<ProductPricesJSON> returnList;
        try {
            returnList = productsRepositoryJPA.getProductPrices(productId);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при загрузке таблицы с товарами", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getProductsList",
            params = {"searchString", "companyId", "departmentId", "document_id", "priceTypeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductsList(
            @RequestParam("searchString") String searchString,
            @RequestParam("companyId") Long companyId,
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("priceTypeId")  Long priceTypeId,
            @RequestParam("document_id") Long document_id)
    {
        logger.info("Processing post request for path /api/auth/getProductsLists with parameters: " +
                "searchString: " + searchString +
                ", companyId: " + companyId.toString() +
                ", departmentId: " + departmentId.toString() +
                ", priceTypeId: "   + priceTypeId.toString() +
                ", document_id: "+ document_id.toString());
        List returnList;
        returnList = productsRepositoryJPA.getProductsList(searchString, companyId, departmentId, document_id, priceTypeId);
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    @PostMapping("/api/auth/getProductGroupFieldsListWithValues")
    // аналогично getProductGroupFieldsList, но отдает поля со значениями (если есть). Используется в документе Товары и услуги
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductGroupFieldsListWithValues(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductGroupFieldsListWithValues: " + searchRequest.toString());

        int field_type; // тип: 1 - сеты (наборы) полей, 2 - поля
        int productId;// id товара, значения полей которого хотим получить

        List<ProductGroupFieldTableJSON> returnList;

        if (searchRequest.getField_type() != null && !searchRequest.getField_type().isEmpty() && searchRequest.getField_type().trim().length() > 0) {
            field_type = Integer.parseInt(searchRequest.getField_type());
        } else {  field_type = 0;  }

        if (searchRequest.getDocumentId() != null && !searchRequest.getDocumentId().isEmpty() && searchRequest.getDocumentId().trim().length() > 0) {
            productId = Integer.parseInt(searchRequest.getDocumentId());
        } else {  productId = 0;  }

        returnList = productsRepositoryJPA.getProductGroupFieldsListWithValues(field_type, productId);//
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    //отдает список отделений в виде их Id с доступным количеством и общим количеством товара в отделении
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductCount",
            params = {"product_id", "company_id", "document_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductCount(
            @RequestParam("product_id") Long product_id,
            @RequestParam("company_id") Long company_id,
            @RequestParam("document_id") Long document_id)
    {
        logger.info("Processing get request for path /api/auth/getProductCount with parameters: " +
                "product_id: " + product_id.toString() +
                ", company_id: " + company_id.toString() +
                ", document_id: "+ document_id.toString());
        List<IdAndCount> returnList;
        try {
            returnList=productsRepositoryJPA.getProductCount(product_id, company_id, document_id);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //отдает краткую информацию о товаре
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductsPriceAndRemains",
            params = {"department_id", "product_id", "price_type_id", "document_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductsPriceAndRemains(
            @RequestParam("department_id") Long department_id,
            @RequestParam("product_id") Long product_id,
            @RequestParam("price_type_id") Long price_type_id,
            @RequestParam("document_id") Long document_id)
    {
        try {
            ProductsPriceAndRemainsJSON response=productsRepositoryJPA.getProductsPriceAndRemains(department_id,product_id,price_type_id,document_id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //отдает 4 цены на товар (средняя себестоимость, последяя закупочная, средняя закупочная, цена по запрошенному типу цены)
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductPricesAll",
            params = {"departmentId", "productId", "priceTypeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductPricesAll(
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("productId") Long productId,
            @RequestParam("priceTypeId") Long priceTypeId)
    {
        try {
            ProductPricingInfoJSON response=productsRepositoryJPA.getProductPricesAll(departmentId,productId,priceTypeId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //*************************************************************************************************************************************************
//**************************************************  C A T E G O R I E S  ************************************************************************
//*************************************************************************************************************************************************
    @PostMapping("/api/auth/getProductCategoriesTrees")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductCategoriesTrees(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getProductCategoriesTrees: " + request.toString());

        List<ProductCategories> returnList;
        List<Integer> categoriesRootIds = productsRepositoryJPA.getCategoriesRootIds(Long.valueOf(Integer.parseInt((request.getCompanyId()))));//
        try {
            returnList = productsRepositoryJPA.getProductCategoriesTrees(categoriesRootIds);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/searchProductCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> searchProductCategory(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/searchProductCategory: " + request.toString());

        Long companyId=Long.valueOf(Integer.parseInt((request.getCompanyId())));
        List<ProductCategoriesTableJSON> returnList;
        try {
            returnList = productsRepositoryJPA.searchProductCategory(companyId,request.getSearchString());
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/insertProductCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertProductCategory(@RequestBody ProductCategoriesForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/insertProductCategory: " + request.toString());

        //Long newDocument = productGroupFieldsRepositoryJPA.insertProductGroupField(request);
        //if(newDocument!=null && newDocument>0){
        try {
            Long categoryId = productsRepositoryJPA.insertProductCategory(request);
            ResponseEntity<Long> responseEntity = new ResponseEntity<>(categoryId, HttpStatus.OK);
            return responseEntity;
        }
        catch (Exception e) {
            e.printStackTrace();
            ResponseEntity<Long> responseEntity = new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateProductCategory")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateProductCategory(@RequestBody ProductCategoriesForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateProductCategory: " + request.toString());

        if(productsRepositoryJPA.updateProductCategory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteProductCategory")
    public ResponseEntity<?> deleteProductCategory(@RequestBody ProductCategoriesForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteProductCategory: " + request.toString());

        if(productsRepositoryJPA.deleteProductCategory(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/saveChangeCategoriesOrder")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveChangeCategoriesOrder(@RequestBody List<ProductCategoriesForm> request) throws ParseException {
        logger.info("Processing post request for path /api/auth/saveChangeCategoriesOrder: [" + request.stream().
                map(ProductCategoriesForm::toString).collect(Collectors.joining(", ")) + "]");

        if(productsRepositoryJPA.saveChangeCategoriesOrder(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when saving", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getRootProductCategories")
    @SuppressWarnings("Duplicates")
    //отдает только список корневых категорий, без детей
    //нужно для изменения порядка вывода корневых категорий
    public ResponseEntity<?> getRootProductCategories(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getRootProductCategories: " + request.toString());

        List<ProductCategoriesTableJSON> returnList ;//
        try {
            returnList = productsRepositoryJPA.getRootProductCategories(Long.valueOf(Integer.parseInt((request.getCompanyId()))));
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getChildrensProductCategories")
    @SuppressWarnings("Duplicates")
    //отдает только список детей, без их детей
    //нужно для изменения порядка вывода категорий
    public ResponseEntity<?> getChildrensProductCategories(@RequestBody CagentCategoriesForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getChildrensProductCategories: " + request.toString());

        List<ProductCategoriesTableJSON> returnList;
        try {
            returnList = productsRepositoryJPA.getChildrensProductCategories(request.getParentCategoryId());
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

//*************************************************************************************************************************************************
//**********************************************************  I M A G E S  ************************************************************************
//*************************************************************************************************************************************************

    @PostMapping("/api/auth/getListOfProductImages")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfProductImages(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getListOfProductImages: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        boolean fullSize=request.isAny_boolean();
        List<FilesProductImagesJSON> returnList;
        try {
            returnList = productsRepositoryJPA.getListOfProductImages(productId, fullSize);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteProductImage")
    public ResponseEntity<?> deleteProductImage(@RequestBody SearchForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/deleteProductImage: " + request.toString());

        if(productsRepositoryJPA.deleteProductImage(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addImagesToProduct")
    public ResponseEntity<?> addImagesToProduct(@RequestBody UniversalForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/addImagesToProduct: " + request.toString());

        if(productsRepositoryJPA.addImagesToProduct(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addCagentsToProduct")
    public ResponseEntity<?> addCagentsToProduct(@RequestBody UniversalForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/addCagentsToProduct: " + request.toString());

        if(productsRepositoryJPA.addCagentsToProduct(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getListOfProductCagents")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfProductCagents(@RequestBody SearchForm request) throws ParseException {
        logger.info("Processing post request for path /api/auth/getListOfProductCagents: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<ProductCagentsJSON> returnList;
        try {
            returnList = productsRepositoryJPA.getListOfProductCagents(productId);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateProductCagentProperties")
    public ResponseEntity<?> updateProductCagentProperties(@RequestBody UniversalForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateProductCagentProperties: " + request.toString());

        if(productsRepositoryJPA.updateProductCagentProperties(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteProductCagent")
    public ResponseEntity<?> deleteProductCagent(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/deleteProductCagent: " + request.toString());

        if(productsRepositoryJPA.deleteProductCagent(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PostMapping("/api/auth/insertProductBarcode")
    public ResponseEntity<?> insertProductBarcode(@RequestBody UniversalForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/insertProductBarcode: " + request.toString());

        if(productsRepositoryJPA.insertProductBarcode(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getListOfProductBarcodes")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getListOfProductBarcodes(@RequestBody SearchForm request){
        logger.info("Processing post request for path /api/auth/getListOfProductBarcodes: " + request.toString());

        Long productId=Long.valueOf(request.getId());
        List<ProductBarcodesJSON> returnList;
        try {
            returnList = productsRepositoryJPA.getListOfProductBarcodes(productId);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            e.printStackTrace();
            ResponseEntity responseEntity = new ResponseEntity<>("Error when requesting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateProductBarcode")
    public ResponseEntity<?> updateProductBarcode(@RequestBody UniversalForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/updateProductBarcode: " + request.toString());

        if(productsRepositoryJPA.updateProductBarcode(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteProductBarcode")
    public ResponseEntity<?> deleteProductBarcode(@RequestBody UniversalForm request){
        logger.info("Processing post request for path /api/auth/deleteProductBarcode: " + request.toString());

        if(productsRepositoryJPA.deleteProductBarcode(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            //responseEntity = ResponseEntity.ok("");
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PostMapping("/api/auth/generateWeightProductCode")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> generateWeightProductCode(@RequestBody UniversalForm request) { // id1 - product_id, id2 - company_id
        logger.info("Processing post request for path /api/auth/generateWeightProductCode: " + request.toString());

        try {
            Integer weightProductCode = productsRepositoryJPA.generateWeightProductCode(request);
            return new ResponseEntity<>(weightProductCode, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/isProductCodeFreeUnical")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> isProductCodeFreeUnical(@RequestBody UniversalForm request) { // id1 - product_id, id2 - company_id
        logger.info("Processing post request for path /api/auth/isProductCodeFreeUnical: " + request.toString());

        try {
            Boolean ret = productsRepositoryJPA.isProductCodeFreeUnical(request);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getProductBarcodesPrefixes")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductBarcodesPrefixes(@RequestBody UniversalForm request) { // id1 - company_id
        logger.info("Processing post request for path /api/auth/getProductBarcodesPrefixes: " + request.toString());

        try {
            Object prefixes = productsRepositoryJPA.getProductBarcodesPrefixes(request);
            return new ResponseEntity<>(prefixes, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(0, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/copyProducts")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> copyProducts(@RequestBody UniversalForm request) { //
        logger.info("Processing post request for path /api/auth/copyProducts: " + request.toString());

        try {
            Boolean ret = productsRepositoryJPA.copyProducts(request);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //отдает краткую информацию о товаре
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getShortInfoAboutProduct",
            params = {"department_id", "product_id"/*, "price_type_id"*/},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getShortInfoAboutProduct(
            @RequestParam("department_id") Long department_id,
            @RequestParam("product_id") Long product_id)
//            @RequestParam("price_type_id") Long price_type_id)
    {
        logger.info("Processing get request for path /api/auth/getShortInfoAboutProduct with parameters: " +
                "department_id: "+ department_id.toString() +
                ", product_id: " + product_id.toString());
//                ", price_type_id: "+ price_type_id.toString());
        try {
            ShortInfoAboutProductJSON response=productsRepositoryJPA.getShortInfoAboutProduct(department_id,product_id/*,price_type_id*/);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //отдает краткую информацию о товаре
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getProductPrice",
            params = {"company_id", "product_id", "price_type_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductPrice(
            @RequestParam("company_id") Long company_id,
            @RequestParam("product_id") Long product_id,
            @RequestParam("price_type_id") Long price_type_id)
    {
        logger.info("Processing get request for path /api/auth/getProductPrice with parameters: " +
                "company_id: "+company_id.toString()+
                ", product_id: "+product_id.toString()+
                ", price_type_id: "+price_type_id.toString());

        try {
            BigDecimal response=productsRepositoryJPA.getProductPrice(company_id,product_id,price_type_id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getProductHistoryTableReport")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductHistoryTableReport(@RequestBody ProductHistoryForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getProductHistoryTableReport: " + searchRequest.toString());

        Long companyId;//по какому предприятию показывать
        Long departmentId;//по какому/каким отделениям показывать / 0 - по всем отделениям пользователя
        Long productId;//по какому товару показывать
        String dateFrom;//с какой даты
        String dateTo;//по какую дату (включительно)

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        List<ProductHistoryJSON> returnList;
        String sortAsc;

        //******** companyId
        companyId = searchRequest.getCompanyId();

        //******** departmentId
        departmentId = searchRequest.getDepartmentId();

        //******** productId
        productId = searchRequest.getProductId();

        //******** dateFrom
        if (searchRequest.getDateFrom() != null && !searchRequest.getDateFrom().isEmpty() && searchRequest.getDateFrom().trim().length() > 0) {
            dateFrom = searchRequest.getDateFrom();
        } else dateFrom = "01.01.1970";

        //******** dateTo
        if (searchRequest.getDateTo() != null && !searchRequest.getDateTo().isEmpty() && searchRequest.getDateTo().trim().length() > 0)
        {
            dateTo = searchRequest.getDateTo();
        } else {
            long curTime = System.currentTimeMillis();
            String pattern = "dd.MM.yyyy";
            DateFormat df = new SimpleDateFormat(pattern);
            dateTo = df.format(curTime);
        }

        //******** sortColumn
        String sortColumn = searchRequest.getSortColumn();

        //******** offset
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }

        //******** sortAsc
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "date_time_created_sort";
            sortAsc = "desc";
        }

        //******** result
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;
        }

        //******** docTypesIds
        List<Long> docTypesIds = searchRequest.getDocTypesIds();

        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = productsRepositoryJPA.getProductHistoryTable(companyId, departmentId, productId, dateFrom, dateTo, sortColumn, sortAsc, result, docTypesIds, offsetreal);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;

    }

    @PostMapping("/api/auth/syncQuantityProducts")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> syncQuantityProducts(@RequestBody UniversalForm request) { //синхронизирует кол-во товаров в products_history и в product_quantity
        logger.info("Processing post request for path /api/auth/syncQuantityProducts: " + request.toString());

        try {
            Boolean ret = productsRepositoryJPA.syncQuantityProducts(request);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //загружает список товаров по их id или id их категорий
    @PostMapping("/api/auth/getProductsInfoListByIds")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductsInfoListByIds(@RequestBody ProductsInfoListForm request){
        logger.info("Processing post request for path /api/auth/getVolumesReportData: " + request.toString());
        List<ProductsInfoListJSON> returnList;
        try {
            returnList = productsRepositoryJPA.getProductsInfoListByIds(request);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            e.printStackTrace();
            ResponseEntity responseEntity = new ResponseEntity<>("Ошибка при запросе информации о товарах", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/setCategoriesToProducts")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> setCategoriesToProducts(@RequestBody UniversalForm form) {
        logger.info("Processing post request for path api/auth/setCategoriesToProducts: " + form.toString());

        Set<Long> productsIds = form.getSetOfLongs1();
        Set<Long> categoriesIds = form.getSetOfLongs2();
        Boolean save = form.getYesNo();

        Boolean result = productsRepositoryJPA.setCategoriesToProducts(productsIds, categoriesIds, save);
        if (!Objects.isNull(result)) {//вернет true - ок, false - недостаточно прав,  null - ошибка
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка при назначении товарам категорий", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
