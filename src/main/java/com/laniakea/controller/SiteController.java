package com.laniakea.controller;
import com.laniakea.message.response.FilesWriteoffJSON;
import com.laniakea.repository.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/public")
public class SiteController {

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



    @RequestMapping(
            value = "/getHtmlPage",
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

    //get user agent
    private String getUserAgent() {
        return request.getHeader("Referer");
    }
}
