package com.laniakea.controller;

import com.laniakea.message.request.ClientForm;
import com.laniakea.message.request.SearchForm;
import com.laniakea.model.Client;
import com.laniakea.message.response.ClientJSON;
import com.laniakea.model.User;
import com.laniakea.security.services.UserDetailsServiceImpl;
import com.laniakea.service.client.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ClientController {

    @Autowired
    ClientService clientService;

    @Autowired
    private UserDetailsServiceImpl userService;

    @PostMapping("/api/auth/updateClient")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateClient(@RequestBody ClientForm clientRequest) throws ParseException{

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        Long id = Long.valueOf(clientRequest.getId());
        String name = clientRequest.getName() == null ? "": clientRequest.getName();
        String lastname = clientRequest.getLastname() == null ? "" : clientRequest.getLastname();
        String dateBirth = clientRequest.getDateBirth() == null ? "": clientRequest.getDateBirth();
        String register = clientRequest.getRegister() == null ? "": clientRequest.getRegister();
        String telephone = clientRequest.getTelephone() == null ? "": clientRequest.getTelephone();
        String additional = clientRequest.getAdditional() == null ? "": clientRequest.getAdditional();
        String email = clientRequest.getEmail() == null ? "": clientRequest.getEmail();

        /*User user = new User();
        user = userService.findByUsername(userService.getUserName());*/

        Client client = new Client();

        client.setCliNumber(id);
        client.setCliName(name);
        client.setCliLastname(lastname);
        client.setCliDatebirth(dateBirth.isEmpty() ? null : dateFormat.parse(dateBirth));
        client.setCliRegister(register);
        client.setCliEmail(email);
        client.setCliAdditional(additional);
        client.setCliTelephone(telephone);
        //client.setUser(user);

        if(clientService.updateClient(client)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/insertClient")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertClient(@RequestBody ClientForm clientRequest) throws ParseException{

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        String name = clientRequest.getName() == null ? "": clientRequest.getName();
        String lastname = clientRequest.getLastname() == null ? "" : clientRequest.getLastname();
        String dateBirth = clientRequest.getDateBirth() == null ? "": clientRequest.getDateBirth();
        String register = clientRequest.getRegister() == null ? "": clientRequest.getRegister();
        String telephone = clientRequest.getTelephone() == null ? "": clientRequest.getTelephone();
        String additional = clientRequest.getAdditional() == null ? "": clientRequest.getAdditional();
        String email = clientRequest.getEmail() == null ? "": clientRequest.getEmail();

        User user = new User();
        user =  userService.getUserByUsername(userService.getUserName());

        Client client = new Client();

        client.setCliName(name);
        client.setCliLastname(lastname);
        client.setCliDatebirth(dateBirth.isEmpty() ? null : dateFormat.parse(dateBirth));
        client.setCliRegister(register);
        client.setCliRegister(register);
        client.setCliEmail(email);
        client.setCliTelephone(telephone);
        client.setCliAdditional(additional);
        client.setUser(user);

        if(clientService.insertClient(client)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/deleteClient")
    public  ResponseEntity<?> deleteClient(@RequestBody ClientForm clientRequest) throws ParseException{
        String checked = clientRequest.getChecked() == null ? "": clientRequest.getChecked();
        ArrayList<Long> decArray = new ArrayList<Long>();
        checked=checked.replace("[","");
        checked=checked.replace("]","");

        for( String s : checked.split(",") ){
            decArray.add( new Long(s) );
        }
        if(clientService.deleteClientsByNumber(decArray)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getClientById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getClientById(@RequestBody ClientForm clientRequest) {
        ClientJSON client;
        int id = clientRequest.getId();
        client=clientService.getClientById(id);//результат запроса помещается в объект
        ResponseEntity<ClientJSON> responseEntity = new ResponseEntity<>(client, HttpStatus.OK);
        return responseEntity;
    }

//    @PostMapping("/api/auth/getSize")
//    public ResponseEntity<?> searchRequest(@RequestBody SearchForm searchRequest){
//    //@RequestBody указывает, что параметр метода должен быть привязан к значению тела запроса HTTP.
//    //Запрос преобразовывается в объект (в данном случае в SearchForm)
//        int size = clientService.getSize(searchRequest.getSearchString());
//        ResponseEntity<String> responseEntity = new ResponseEntity<>("Rows: "+size, HttpStatus.OK);
//        return responseEntity;
//    }

    @PostMapping("/api/auth/getClientsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getClientsTableRequest(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<Client> clientList;
        // чтобы не показывать в html реальные наименования методов в ClientDB
        if (sortColumn != null && sortColumn.equals("columnName")) {
            sortColumn = "cli_name";
        } else if (sortColumn != null && sortColumn.equals("columnDate")) {
            sortColumn = "cli_datebirth";
        }else if (sortColumn != null && sortColumn.equals("cliNumber")) {
            sortColumn = "cli_number";
        }
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0)
        {
               sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        }else{
            sortColumn = "cli_name";
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
        clientList=clientService.getClientsTable(result, offsetreal, searchString, sortColumn, sortAsc);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(clientList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getClientPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getClientPagesList(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int disabledLINK;// номер страницы на паджинейшене, на которой мы сейчас. Изначально это 1.
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();

        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        //disabledLINK=pagenum;
        int size = clientService.getSize(searchString);//  - общее количество записей выборки
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
}


