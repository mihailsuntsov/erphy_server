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
import com.dokio.repository.store.woo.v3.StoreProductCategoriesRepository;
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
public class ProductCategoriesController {

    Logger logger = Logger.getLogger(ProductCategoriesController.class);

    @Autowired
    StoreProductCategoriesRepository storeProductCategoriesRepository;
    @Autowired
    CommonUtilites cu;

    @RequestMapping(
            value = "/syncProductCategoriesToStore",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> syncProductCategoriesToStore(HttpServletRequest httpServletRequest,
                                                          @RequestParam("key") String key){
        logger.info("Processing post request for path /api/public/woo_v3/syncProductCategoriesToStore");
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(),key);
            return new ResponseEntity<>(storeProductCategoriesRepository.syncProductCategoriesToStore(key), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncProductCategoriesToStore error", e);
        return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/syncProductCategoriesIds")
    public ResponseEntity<?> syncProductCategoriesIds(HttpServletRequest httpServletRequest, @RequestBody SyncIdsForm request){
        logger.info("Processing post request for path /api/public/woo_v3/syncProductCategoriesIds: " + request.toString());
        try {cu.checkStoreIp(httpServletRequest.getRemoteAddr(), request.getCrmSecretKey());
            return new ResponseEntity<>(storeProductCategoriesRepository.syncProductCategoriesIds(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller syncProductCategoriesIds error", e);
        return new ResponseEntity<>("Operation of the synchronization ids error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
