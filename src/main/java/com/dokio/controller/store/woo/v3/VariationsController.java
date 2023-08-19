/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/
package com.dokio.controller.store.woo.v3;

import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.response.store.woo.v3.products.ProductCountJSON;
import com.dokio.repository.store.woo.v3.StoreVariationsRepository;
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
public class VariationsController {

    Logger logger = Logger.getLogger(VariationsController.class);

    @Autowired
    StoreVariationsRepository storeVariationsRepository;
    @Autowired
    CommonUtilites cu;

    @RequestMapping(
            value = "/countVariationsToStoreSync",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> countVariationsToStoreSync(HttpServletRequest httpServletRequest,
                                                      @RequestParam("key") String key){
        logger.info("Processing get request for path /api/public/woo_v3/countVariationsToStoreSync");
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        try{cu.checkStoreIp(ipAddress==null?httpServletRequest.getRemoteAddr():ipAddress, key);
            ProductCountJSON ret = storeVariationsRepository.countVariationsToStoreSync(key);
            ResponseEntity response = new ResponseEntity<>(ret, HttpStatus.OK);
            return response;
        }
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller countVariationsToStoreSync error. " + e, e);
            return new ResponseEntity<>("Operation of the synchronization error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/syncVariationsToStore",
            params = {"key","first_result","max_results"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> syncVariationsToStore(HttpServletRequest httpServletRequest,
                                                 @RequestParam("key") String key,
            @RequestParam("first_result") Integer firstResult,
            @RequestParam("max_results") Integer maxResults){
        logger.info("Processing get request for path /api/public/woo_v3/syncVariationsToStore");
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        try{cu.checkStoreIp(ipAddress==null?httpServletRequest.getRemoteAddr():ipAddress, key);
            return new ResponseEntity<>(storeVariationsRepository.syncVariationsToStore(key,firstResult,maxResults), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncVariationsToStore error", e);
            return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

//    @RequestMapping(
//            value = "/getVariationsWooIdsToDeleteInStore",
//            params = {"key"},
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getVariationsWooIdsToDeleteInStore(HttpServletRequest httpServletRequest,
//                                                             @RequestParam("key") String key){
//        logger.info("Processing post request for path /api/public/woo_v3/getVariationsWooIdsToDeleteInStore");
//        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), key);
//            return new ResponseEntity<>(storeVariationsRepository.getVariationsWooIdsToDeleteInStore(key), HttpStatus.OK);}
//        catch (Exception e){
//            e.printStackTrace();
//            logger.error("Controller getVariationsWooIdsToDeleteInStore error", e);
//            return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
//    }
    @PostMapping("/syncVariationsIds")
    public ResponseEntity<?> syncVariationsIds(HttpServletRequest httpServletRequest, @RequestBody SyncIdsForm request){
        logger.info("Processing post request for path /api/public/woo_v3/syncVariationsIds: " + request.toString());
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        try{cu.checkStoreIp(ipAddress==null?httpServletRequest.getRemoteAddr():ipAddress, request.getCrmSecretKey());
            return new ResponseEntity<>(storeVariationsRepository.syncVariationsIds(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncVariationsIds error", e);
            return new ResponseEntity<>("Operation of the synchronization variations ids error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
