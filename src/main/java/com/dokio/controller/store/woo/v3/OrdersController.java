package com.dokio.controller.store.woo.v3;

import com.dokio.message.request.store.woo.v3.orders.OrdersForm;
import com.dokio.repository.store.woo.v3.StoreOrdersRepository;
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
public class OrdersController {
    Logger logger = Logger.getLogger(OrdersController.class);

    @Autowired
    StoreOrdersRepository storeOrdersRepository;
    @Autowired
    CommonUtilites cu;



    @RequestMapping(
            value = "/getLastSynchronizedOrderTime",
            params = {"key"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getLastSynchronizedOrderTime(HttpServletRequest httpServletRequest,
                                                          @RequestParam("key") String key){
        logger.info("Processing get request for path /api/public/woo_v3/getLastSynchronizedOrderTime");
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        try{cu.checkStoreIp(ipAddress==null?httpServletRequest.getRemoteAddr():ipAddress, key);
            return new ResponseEntity<>(storeOrdersRepository.getLastSynchronizedOrderTime(key), HttpStatus.OK);}
        catch (Exception e){
            e.printStackTrace();
            logger.error("Controller getLastSynchronizedOrderTime error", e);
            return new ResponseEntity<>("Operation of the synchronization error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }


    @PostMapping("/putOrdersIntoCRM")
    public ResponseEntity<?> putOrdersIntoCRM(HttpServletRequest httpServletRequest, @RequestBody OrdersForm request){
        logger.info("Processing post request for path /api/public/woo_v3/putOrdersIntoCRM: " + request.toString());
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        try{cu.checkStoreIp(ipAddress==null?httpServletRequest.getRemoteAddr():ipAddress, request.getCrmSecretKey());
            return new ResponseEntity<>(storeOrdersRepository.putOrdersIntoCRM(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller putOrdersIntoCRM error", e);
            return new ResponseEntity<>("Operation of the synchronization ids error. " + e, HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
