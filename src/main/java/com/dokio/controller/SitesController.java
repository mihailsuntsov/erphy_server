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
import com.dokio.message.request.SearchForm;
import com.dokio.message.request.SignUpForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.SitesJSON;
import com.dokio.message.response.SitesTableJSON;
import com.dokio.repository.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageServiceSite;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SitesController {

    Logger logger = Logger.getLogger("SitesController");

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
    @Autowired
    StorageServiceSite storageService;

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
        logger.info("Processing post request for path /api/auth/getSitesTable: " + searchRequest.toString());

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
        logger.info("Processing post request for path /api/auth/getSiteValuesById: " + request.toString());

        SitesJSON response;
        Long id = request.getId();
        response=sitesRepository.getSiteValuesById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/auth/insertSite")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertSite(@RequestBody SitesJSON request){
        logger.info("Processing post request for path /api/auth/insertSite: " + request.toString());

        Long newDocument = sitesRepository.insertSite(request);
        if(newDocument!=null && newDocument>0){
            return new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when inserting", HttpStatus.BAD_REQUEST);
        }
    }

    //get user agent
    private String getUserAgent() {
        return request.getHeader("Origin");
    }


    @PostMapping("/api/auth/deleteSite")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteSite(@RequestBody SignUpForm request) {
        logger.info("Processing post request for path /api/auth/deleteSite: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(sitesRepository.deleteSite(checked)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error when deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/api/public/getSiteFile/{filePathAndName:[A-z0-9-_+]*.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> getSiteFile(@PathVariable String filePathAndName) {
//        String Str = filePathAndName;
//        Resource file = storageService.loadSiteFile(Str.replace("--","//"));
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
//                .body(file);
//    }


    @GetMapping("/api/public/getSiteFile/{masterId}/{companyId}/sites/{siteId}/{filePathAndName:.+}")
    @ResponseBody public ResponseEntity<Resource> getSiteFile(@PathVariable String masterId,@PathVariable String companyId,@PathVariable String siteId,@PathVariable String filePathAndName) {
        logger.info("Processing get request for path /api/public/getSiteFile: masterId=" + masterId + ", companyId=" + companyId +
                ", siteId=" + siteId + ", filePathAndName=" + filePathAndName);
        Resource file = storageService.loadSiteFile(masterId+"//"+companyId+"//sites//"+siteId+"//"+filePathAndName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);}
    @GetMapping("/api/public/getSiteFile/{masterId}/{companyId}/sites/{siteId}/{lvl1}/{filePathAndName:.+}")
    @ResponseBody public ResponseEntity<Resource> getSiteFile(@PathVariable String masterId,@PathVariable String companyId,@PathVariable String siteId,@PathVariable String lvl1,@PathVariable String filePathAndName) {
        logger.info("Processing get request for path /api/public/getSiteFile: masterId=" + masterId + ", companyId=" + companyId +
                ", siteId=" + siteId + ", lvl1" + lvl1 + ", filePathAndName=" + filePathAndName);
        Resource file = storageService.loadSiteFile(masterId+"//"+companyId+"//sites//"+siteId+"//"+lvl1+"//"+filePathAndName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);}
    @GetMapping("/api/public/getSiteFile/{masterId}/{companyId}/sites/{siteId}/{lvl1}/{lvl2}/{filePathAndName:.+}")
    @ResponseBody public ResponseEntity<Resource> getSiteFile(@PathVariable String masterId,@PathVariable String companyId,@PathVariable String siteId,@PathVariable String lvl1,@PathVariable String lvl2,@PathVariable String filePathAndName) {
        logger.info("Processing get request for path /api/public/getSiteFile: masterId=" + masterId + ", companyId=" + companyId +
                ", siteId=" + siteId + ", lvl1" + lvl1 + ", lvl2" + lvl2 + ", filePathAndName=" + filePathAndName);
        Resource file = storageService.loadSiteFile(masterId+"//"+companyId+"//sites//"+siteId+"//"+lvl1+"//"+lvl2+"//"+filePathAndName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);}
    @GetMapping("/api/public/getSiteFile/{masterId}/{companyId}/sites/{siteId}/{lvl1}/{lvl2}/{lvl3}/{filePathAndName:.+}")
    @ResponseBody public ResponseEntity<Resource> getSiteFile(@PathVariable String masterId,@PathVariable String companyId,@PathVariable String siteId,@PathVariable String lvl1,@PathVariable String lvl2,@PathVariable String lvl3,@PathVariable String filePathAndName) {
        logger.info("Processing get request for path /api/public/getSiteFile: masterId=" + masterId + ", companyId=" + companyId +
                ", siteId=" + siteId + ", lvl1" + lvl1 + ", lvl2" + lvl2 + ", lvl3" + lvl3 + ", filePathAndName=" + filePathAndName);
        Resource file = storageService.loadSiteFile(masterId+"//"+companyId+"//sites//"+siteId+"//"+lvl1+"//"+lvl2+"//"+lvl3+"//"+filePathAndName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);}
    @GetMapping("/api/public/getSiteFile/{masterId}/{companyId}/sites/{siteId}/{lvl1}/{lvl2}/{lvl3}/{lvl4}/{filePathAndName:.+}")
    @ResponseBody public ResponseEntity<Resource> getSiteFile(@PathVariable String masterId,@PathVariable String companyId,@PathVariable String siteId,@PathVariable String lvl1,@PathVariable String lvl2,@PathVariable String lvl3,@PathVariable String lvl4,@PathVariable String filePathAndName) {
        logger.info("Processing get request for path /api/public/getSiteFile: masterId=" + masterId + ", companyId=" + companyId +
                ", siteId=" + siteId + ", lvl1" + lvl1 + ", lvl2" + lvl2 + ", lvl3" + lvl3 + ", lvl4" + lvl4 + ", filePathAndName=" + filePathAndName);
        Resource file = storageService.loadSiteFile(masterId+"//"+companyId+"//sites//"+siteId+"//"+lvl1+"//"+lvl2+"//"+lvl3+"//"+lvl4+"//"+filePathAndName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);}
}
