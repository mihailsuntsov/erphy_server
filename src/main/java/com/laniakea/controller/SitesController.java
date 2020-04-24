package com.laniakea.controller;
import com.laniakea.message.request.SearchForm;
import com.laniakea.message.request.UniversalForm;
import com.laniakea.message.response.SitesJSON;
import com.laniakea.message.response.SitesTableJSON;
import com.laniakea.repository.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SitesController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserGroupRepositoryJPA userGroupRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userRepository2;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private UserDetailsServiceImpl userService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    SitesRepository sitesRepository;

    @RequestMapping(
            value = "/api/public/getHtmlPage",
            params = {"domain", "uid", "route_id", "parameter"},
            method = RequestMethod.GET, produces = "application/html;charset=utf8")
    public ResponseEntity<String> getHtmlPage(
            @RequestParam("domain") String domain,
            @RequestParam("uid") String uid,
            @RequestParam("route_id") Long route_id,
            @RequestParam("parameter") String parameter
    ) {
//        return "domain=" + domain+",uid "+uid+", route_id"+route_id+", parameter"+parameter;
        String returnData;
        try {
            returnData = siteRepository.getHtmlPage(domain,uid,route_id,parameter);

            returnData=returnData + "<br> <h2>Origin - " + getUserAgent() + "</h2>";

            return new ResponseEntity<>(returnData, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Error when requesting", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/auth/getSitesTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSitesTable(@RequestBody SearchForm searchRequest) {
        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        int companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        SitesTableJSON returnObject;

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
        returnObject = sitesRepository.getSitesTable(
                result,
                offset,
                offsetreal,
                searchString,
                sortColumn,
                sortAsc,
                companyId);//запрос списка: взять кол-во rezult, начиная с offsetreal

        ResponseEntity responseEntity = new ResponseEntity<>(returnObject, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getSiteValuesById")
    public ResponseEntity<?> getSiteValuesById(@RequestBody UniversalForm request) {
        SitesJSON response;
        Long id = request.getId();
        response=sitesRepository.getSiteValuesById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    //get user agent
    private String getUserAgent() {
        return request.getHeader("Referer");
    }
}
