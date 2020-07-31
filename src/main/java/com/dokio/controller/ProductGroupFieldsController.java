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
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.text.ParseException;
import java.util.List;

@Controller
public class ProductGroupFieldsController {
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
    ProductGroupFieldsRepositoryJPA productGroupFieldsRepositoryJPA;

    @PostMapping("/api/auth/getProductGroupFieldsList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductGroupFieldsList(@RequestBody SearchForm searchRequest) {
        int groupId; // id группы товаров, которой принадлежат поля или сеты
        int field_type; // тип: 1 - сеты (наборы) полей, 2 - поля
        int parentSetId;// родительский сет поля

        List<ProductGroupFieldTableJSON> returnList;


        if (searchRequest.getGroupId() != null && !searchRequest.getGroupId().isEmpty() && searchRequest.getGroupId().trim().length() > 0) {
            groupId = Integer.parseInt(searchRequest.getGroupId());
        } else {  groupId = 0;  }

        if (searchRequest.getField_type() != null && !searchRequest.getField_type().isEmpty() && searchRequest.getField_type().trim().length() > 0) {
            field_type = Integer.parseInt(searchRequest.getField_type());
        } else {  field_type = 0;  }

        if (searchRequest.getParentSetId() != null && !searchRequest.getParentSetId().isEmpty() && searchRequest.getParentSetId().trim().length() > 0) {
            parentSetId = Integer.parseInt(searchRequest.getParentSetId());
        } else {  parentSetId = 0;  }

        returnList = productGroupFieldsRepositoryJPA.getProductGroupFieldsList(groupId, field_type, parentSetId);//
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }



    @PostMapping("/api/auth/saveChangeFieldsOrder")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveChangeFieldsOrder(@RequestBody List<ProductGroupFieldsForm> request) throws ParseException {
        if(productGroupFieldsRepositoryJPA.saveChangeFieldsOrder(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when saving", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/updateProductGroupField")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateProductGroupField(@RequestBody ProductGroupFieldsForm request) throws ParseException{
        if(productGroupFieldsRepositoryJPA.updateProductGroupField(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/deleteProductGroupField")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> deleteProductGroupField(@RequestBody ProductGroupFieldsForm request) throws ParseException{
        if(productGroupFieldsRepositoryJPA.deleteProductGroupField(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/insertProductGroupField")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertProductGroupField(@RequestBody ProductGroupFieldsForm request) throws ParseException {
        //Long newDocument = productGroupFieldsRepositoryJPA.insertProductGroupField(request);
        //if(newDocument!=null && newDocument>0){
        if(productGroupFieldsRepositoryJPA.insertProductGroupField(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

/*
    @PostMapping("/api/auth/getProductGroupFieldValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getProductGroupFieldValuesById(@RequestBody ProductGroupFieldsForm request) {
        ProductGroupFieldJSON response;
        int id = request.getId();
        response=productGroupFieldsRepositoryJPA.getProductGroupFieldValuesById(id);//результат запроса помещается в экземпляр класса
        ResponseEntity<ProductGroupFieldJSON> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }


    @PostMapping("/api/auth/updateProductGroupField")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateProductGroupField(@RequestBody ProductGroupFieldsForm request) throws ParseException{
        if(productGroupFieldsRepositoryJPA.updateProductGroupField(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteProductGroupFields")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteProductGroupFields(@RequestBody SignUpForm request) throws ParseException{
        String checked = request.getChecked() == null ? "": request.getChecked();
        checked=checked.replace("[","");
        checked=checked.replace("]","");

        if(productGroupFieldsRepositoryJPA.deleteProductGroupFieldsById(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }*/
}
