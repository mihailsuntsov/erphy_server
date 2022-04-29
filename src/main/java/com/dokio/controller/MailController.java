package com.dokio.controller;

import com.dokio.repository.MailRepository;
import com.dokio.repository.UserRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/public")
@Controller
public class MailController {

    Logger logger = Logger.getLogger("MailController");

    @Autowired
    MailRepository mailRepository;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;

    @RequestMapping(
            value = "/forgotPass",
            params = {"mail"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> forgotPass(
            @RequestParam("mail") String mail){
        logger.info("Processing get request for path api/public/forgotPass: " + mail);
        try {return new ResponseEntity<>(mailRepository.forgotPass(mail), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller forgotPass error", e);
        return new ResponseEntity<>("Error request reset password", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/setNewPass",
            params = {"uuid", "pwd"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> setNewPass(
            @RequestParam("uuid")  String uuid,
            @RequestParam("pwd")   String pwd)
    {
        logger.info("Processing get request for path /api/public/setNewPass with parameters: " +
                "  uuid: "  + uuid +
                ", pwd: "   + pwd );
        try {return new ResponseEntity<>(mailRepository.setNewPass(uuid,pwd), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller setNewPass error", e);
            return new ResponseEntity<>("Error request setting password", HttpStatus.INTERNAL_SERVER_ERROR);}
    }


    @GetMapping("activate/{code}")
    public ResponseEntity<?> activate(@PathVariable String code){
        logger.info("Processing get request for path api/public/activate, code: " + code);
        try {return new ResponseEntity<>(userRepositoryJPA.activateUser(code), HttpStatus.OK);}
        catch (Exception e){logger.error("Controller activate error", e);
            return new ResponseEntity<>("Error request user activation", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
