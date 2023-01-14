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

import com.dokio.message.request.store.woo.v3.IntListForm;
import com.dokio.message.request.store.woo.v3.SyncIdsForm;
import com.dokio.message.response.store.woo.v3.products.ProductCountJSON;
import com.dokio.repository.store.woo.v3.StoreProductsRepository;
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
public class ProductsSyncController {

    Logger logger = Logger.getLogger(ProductsSyncController.class);

    @Autowired
    StoreProductsRepository storeProductsRepository;
    @Autowired
    CommonUtilites cu;

    @RequestMapping(
            value = "/countProductsToStoreSync",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> countProductsToStoreSync(HttpServletRequest httpServletRequest,
                                                      @RequestParam("key") String key){
        logger.info("Processing post request for path /api/public/woo_v3/countProductsToStoreSync");
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), key);
            ProductCountJSON ret = storeProductsRepository.countProductsToStoreSync(key);
            ResponseEntity response = new ResponseEntity<>(ret, HttpStatus.OK);
            return response;
        }
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller countProductsToStoreSync error. " + e, e);
            return new ResponseEntity<>("Operation of the synchronization error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/syncProductsToStore",
            params = {"key","first_result","max_results"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> syncProductsToStore(HttpServletRequest httpServletRequest,
                                                 @RequestParam("key") String key,
            @RequestParam("first_result") Integer firstResult,
            @RequestParam("max_results") Integer maxResults){
        logger.info("Processing post request for path /api/public/woo_v3/syncProductsToStore");
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(),key);
            return new ResponseEntity<>(storeProductsRepository.syncProductsToStore(key,firstResult,maxResults), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncProductsToStore error", e);
            return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/syncProductsIds")
    public ResponseEntity<?> syncProductsIds(HttpServletRequest httpServletRequest, @RequestBody SyncIdsForm request){
        logger.info("Processing post request for path /api/public/woo_v3/syncProductsIds: " + request.toString());
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), request.getCrmSecretKey());
            return new ResponseEntity<>(storeProductsRepository.syncProductsIds(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncProductsIds error", e);
            return new ResponseEntity<>("Operation of the synchronization ids error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/getProductWooIdsToDeleteInStore",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getProductWooIdsToDeleteInStore(HttpServletRequest httpServletRequest,
                                                             @RequestParam("key") String key){
        logger.info("Processing post request for path /api/public/woo_v3/getProductWooIdsToDeleteInStore");
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), key);
            return new ResponseEntity<>(storeProductsRepository.getProductWooIdsToDeleteInStore(key), HttpStatus.OK);}
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller getProductWooIdsToDeleteInStore error", e);
            return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/deleteWooIdsFromProducts")
    public ResponseEntity<?> deleteWooIdsFromProducts(HttpServletRequest httpServletRequest, @RequestBody IntListForm request){
        logger.info("Processing post request for path /api/public/woo_v3/deleteWooIdsFromProducts: " + request.toString());
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), request.getCrmSecretKey());
            return new ResponseEntity<>(storeProductsRepository.deleteWooIdsFromProducts(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller deleteWooIdsFromProducts error", e);
            return new ResponseEntity<>("Operation of the synchronization ids error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
