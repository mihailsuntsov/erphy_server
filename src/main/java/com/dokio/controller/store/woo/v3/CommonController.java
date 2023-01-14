package com.dokio.controller.store.woo.v3;

import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/woo_v3")
public class CommonController {
    Logger logger = Logger.getLogger(CommonController.class);
    @Autowired
    CommonUtilites cu;

    @RequestMapping(
            value = "/DokioCrmConnectionTest",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> DokioCrmConnectionTest(HttpServletRequest httpServletRequest,
                                                    @RequestParam("key") String key){
        logger.info("Processing get request for path /api/public/woo_v3/DokioCrmConnectionTest");
        try {
            if(Objects.isNull(cu.getByCrmSecretKey("id", key)))
                return new ResponseEntity<>(-200, HttpStatus.OK);
            cu.checkStoreIp(httpServletRequest.getRemoteAddr(), key);
            return new ResponseEntity<>(1, HttpStatus.OK);}
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller DokioCrmConnectionTest error", e);
            return new ResponseEntity<>("Connection test controller error! " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }



}
