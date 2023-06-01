/*
        DokioCRM - server part. Sales, finance and warehouse management system
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
package com.dokio.controller;

import com.dokio.message.request.PlanAdditionalOptionsForm;
import com.dokio.message.request.additional.UserPaymentsTableForm;
import com.dokio.repository.SubscriptionRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;

@RestController
@Repository
public class Subscription {
    Logger logger = Logger.getLogger(Subscription.class);
    @Autowired
    SubscriptionRepositoryJPA subscriptionRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @RequestMapping(value = "/api/auth/getMasterAccountInfo",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getMasterAccountInfo() {
        logger.info("Processing get request for path /api/auth/getMasterAccountInfo with no params");
        try {return new ResponseEntity<>(subscriptionRepository.getMasterAccountInfo(), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getMasterAccountInfo error", e);
            return new ResponseEntity<>("Error query of getting master account information", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(value = "/api/auth/getPlansList",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getPlansList() {
        logger.info("Processing get request for path /api/auth/getPlansList with no params");
        try {return new ResponseEntity<>(subscriptionRepository.getPlansList(), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getMasterAccountInfo error", e);
            return new ResponseEntity<>("Error query of getting plans list", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(value = "/api/auth/stopTrialPeriod",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> stopTrialPeriod() {
        logger.info("Processing get request for path /api/auth/stopTrialPeriod with no params");
        try {return new ResponseEntity<>(subscriptionRepository.stopTrialPeriod(), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller stopTrialPeriod error", e);
            return new ResponseEntity<>("Error query of trying to stop trial period", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateAddOptions")
    public ResponseEntity<?> updateAddOptions(@RequestBody PlanAdditionalOptionsForm request){
        logger.info("Processing post request for path /api/auth/updateAddOptions: " + request.toString());
        try {return new ResponseEntity<>(subscriptionRepository.updateAddOptions(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error saving plan additional options", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/getUserPaymentsPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserPaymentsPagesList(@RequestBody UserPaymentsTableForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getUserPaymentsPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int size = subscriptionRepository.getUserPaymentsSize(searchRequest.getDateFrom(), searchRequest.getDateTo());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getUserPaymentsTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getUserPaymentsTable(@RequestBody UserPaymentsTableForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getUserPaymentsTable: " + searchRequest.toString());
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "for_what_date_sort";
            sortAsc = "asc";
        }
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int offsetreal = offset * result;//создана переменная с номером страницы
        return new ResponseEntity(subscriptionRepository.getUserPaymentsTable(result, offsetreal, sortColumn, sortAsc, searchRequest.getDateFrom(), searchRequest.getDateTo()), HttpStatus.OK);
    }
    @RequestMapping(
            value = "/api/auth/getLastVersionAgreement",
            params = {"type"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getLastVersionAgreement(
            @RequestParam("type") String type){
        logger.info("Processing get request for path /api/auth/getLastVersionAgreement with parameters: " + "type: " + type);
        try {return new ResponseEntity<>(subscriptionRepository.getLastVersionAgreement(type), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Error loading agreement object", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}

