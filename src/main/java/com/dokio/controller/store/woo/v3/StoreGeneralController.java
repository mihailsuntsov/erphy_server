package com.dokio.controller.store.woo.v3;


import com.dokio.repository.store.woo.v3.StoreGeneralRepository;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/woo_v3")
public class StoreGeneralController {

    Logger logger = Logger.getLogger(StoreGeneralController.class);

    @Autowired
    CommonUtilites cu;
    @Autowired
    StoreGeneralRepository storeGeneralRepository;


    @RequestMapping(
            value = "/isLetSync",
            params = {"key","plugin_version"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> isLetSync(HttpServletRequest httpServletRequest,
           @RequestParam("key")             String key,
           @RequestParam("plugin_version")  String pluginVersion){
        logger.info("Processing get request for path /api/public/woo_v3/isLetSync");
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        try{cu.checkStoreIp(ipAddress==null?httpServletRequest.getRemoteAddr():ipAddress, key);
            return new ResponseEntity<>(storeGeneralRepository.isLetSync(pluginVersion,key), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller isLetSync error", e);
            return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }





}
