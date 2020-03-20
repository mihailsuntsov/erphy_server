package com.laniakea.controller;
import com.laniakea.message.response.CompaniesJSON;
import com.laniakea.message.request.CompanyForm;
import com.laniakea.message.request.SearchForm;
import com.laniakea.model.Companies;
import com.laniakea.model.Sprav.SpravSysOPF;
import com.laniakea.model.User;
import com.laniakea.repository.UserRepositoryJPA;
import com.laniakea.security.services.UserDetailsServiceImpl;
import com.laniakea.service.company.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

import java.util.Date;

@Controller
public class CompaniesController {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    CompanyService companyService;

    @Autowired
    private UserDetailsServiceImpl userRepository;

    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    @PostMapping("/api/auth/getCompaniesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompaniesTableRequest(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        //String masterId;
        List<Companies> companiesList;
        // чтобы не показывать в html реальные наименования методов в Companies
        if (sortColumn != null && sortColumn.equals("columnName")) {
            sortColumn = "name";
//        } else if (sortColumn != null && sortColumn.equals("columnDate")) {
//            sortColumn = "cli_datebirth";
        }else if (sortColumn != null && sortColumn.equals("columnId")) {
            sortColumn = "id";
        }
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
        //masterId = searchRequest.getMasterId();
        companiesList = companyService.getCompaniesTable(result, offsetreal, searchString, sortColumn, sortAsc);//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(companiesList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getCompaniesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompaniesList() {
        List<Companies> companiesList;
        companiesList = companyService.getCompaniesList();
        return new ResponseEntity<>(companiesList, HttpStatus.OK);
    }

    @PostMapping("/api/auth/getMyCompanyList")//возвращает List из 1 предприятия.
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMyCompanyList() {
        List<Companies> companiesList;
        companiesList = companyService.getCompaniesList();
        return new ResponseEntity<>(companiesList, HttpStatus.OK);
    }

    @PostMapping("/api/auth/deleteCompanies")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteCompanies(@RequestBody CompanyForm companyRequest) throws ParseException{
        String checked = companyRequest.getChecked() == null ? "": companyRequest.getChecked();
        ArrayList<Long> decArray = new ArrayList<Long>();
        checked=checked.replace("[","");
        checked=checked.replace("]","");

        for( String s : checked.split(",") ){
            decArray.add( new Long(s) );
        }
        if(companyService.deleteCompaniesByNumber(decArray)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PostMapping("/api/auth/getCompaniesPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompaniesPagesList(@RequestBody SearchForm searchRequest) {
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
        int size = companyService.getCompaniesSize(searchString);//  - общее количество записей выборки
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

    //Отдает ЗНАЧЕНИЯ из таблицы companies по id предприятия
    @PostMapping("/api/auth/getCompanyValuesById")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getCompanyValuesById(@RequestBody CompanyForm companyRequest) {
        CompaniesJSON company;
        int id = companyRequest.getId();
        company=companyService.getCompanyValuesById(id);//результат запроса помещается в объект
        ResponseEntity<CompaniesJSON> responseEntity = new ResponseEntity<>(company, HttpStatus.OK);
        return responseEntity;
    }


    @PostMapping("/api/auth/updateCompany")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateCompany(@RequestBody CompanyForm companyRequest) throws ParseException {
        if(companyService.updateCompany(companyRequest)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when updating", HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/api/auth/insertCompany")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertCompany(@RequestBody CompanyForm companyRequest) throws ParseException {

        String name = companyRequest.getNameShort() == null ? "": companyRequest.getNameShort();

        SpravSysOPF opf;
        if (companyRequest.getOpf_id() != null && !companyRequest.getOpf_id().isEmpty() && companyRequest.getOpf_id().trim().length() > 0 && companyRequest.getOpf_id() != "0")  {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        opf = em.find(SpravSysOPF.class, Long.valueOf(Integer.parseInt(companyRequest.getOpf_id())));
        em.close();}
        else{opf=null;}

        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date dateReg =null;
        if (companyRequest.getDateReg() != null && !companyRequest.getDateReg().isEmpty() && companyRequest.getDateReg().trim().length() > 0) {
            dateReg = format.parse(companyRequest.getDateReg());
        }

        User creator =  userRepository.getUserByUsername(userRepository.getUserName());
        User master = userRepository.getUserByUsername(
                        userRepositoryJPA.getUsernameById(
                              userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository.getUserName() )));

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Companies company = new Companies();
        company.setCompName(name);
        company.setCreator(creator);
        company.setMaster(master);
        company.setCompNameFull(companyRequest.getNameFull());
        company.setCompAddressFact(companyRequest.getAddressfact());
        company.setCompAddressJur(companyRequest.getAddressjur());
        company.setCompBank(companyRequest.getBank());
        company.setCompbik(companyRequest.getBik());
        company.setCompDateReg(dateReg);
        company.setCompInn(companyRequest.getInn());
        company.setCompKorschet(companyRequest.getKorschet());
        company.setCompOpf(opf);
        company.setCompReg_num(companyRequest.getReg_num());
        company.setCompRs(companyRequest.getRs());
        company.setCompWho_got(companyRequest.getWho_got());
        company.setCompDateTimeCreated(timestamp);
        company.setSt_prefix_barcode_packed(companyRequest.getSt_prefix_barcode_packed());
        company.setSt_prefix_barcode_pieced(companyRequest.getSt_prefix_barcode_pieced());

        Long idNewCompany=companyService.insertCompany(company);


        if(idNewCompany !=null){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + idNewCompany+"\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Error when inserting", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}
