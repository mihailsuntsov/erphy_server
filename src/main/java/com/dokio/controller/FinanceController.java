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

package com.dokio.controller;

import com.dokio.message.request.Reports.ProfitLossForm;
import com.dokio.util.CommonUtilites;
import com.dokio.util.FinanceUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
class FinanceController {

    Logger logger = Logger.getLogger("FinanceController");
    @Autowired
    FinanceUtilites financeUtilites;
    @Autowired
    private CommonUtilites commonUtilites;

    @RequestMapping(
            value = "/api/auth/getCagentBalance",
            params = {"companyId","typeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getCagentBalance(
            @RequestParam("companyId") Long companyId,
            @RequestParam("typeId") Long cagentId){
        logger.info("Processing get request for path /api/auth/getCagentBalance with parameters: " + "companyId: " + companyId+", cagentId: "+cagentId);
        try {return new ResponseEntity<>(commonUtilites.getSummFromHistory("cagent",companyId,cagentId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getCagentBalance error", e);
        return new ResponseEntity<>("Ошибка запроса баланса контрагента", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getPaymentAccountBalance",
            params = {"companyId","typeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getPaymentAccountBalance(
            @RequestParam("companyId") Long companyId,
            @RequestParam("typeId") Long accountId){
        logger.info("Processing get request for path /api/auth/getPaymentAccountBalance with parameters: " + "companyId: " + companyId+", accountId: "+accountId);
        try {return new ResponseEntity<>(commonUtilites.getSummFromHistory("payment_account",companyId,accountId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getPaymentAccountBalance error", e);
        return new ResponseEntity<>("Ошибка запроса баланса расчётного счёта", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getBoxofficeBalance",
            params = {"companyId","typeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getBoxofficeBalance(
            @RequestParam("companyId") Long companyId,
            @RequestParam("typeId") Long boxofficeId){
        logger.info("Processing get request for path /api/auth/getBoxofficeBalance with parameters: " + "companyId: " + companyId+", boxofficeId: "+boxofficeId);
        try {return new ResponseEntity<>(commonUtilites.getSummFromHistory("boxoffice",companyId,boxofficeId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getBoxofficeBalance error", e);
            return new ResponseEntity<>("Ошибка запроса баланса кассы предприятия", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getKassaBalance",
            params = {"companyId","typeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getKassaBalance(
            @RequestParam("companyId") Long companyId,
            @RequestParam("typeId") Long kassaId){
        logger.info("Processing get request for path /api/auth/getKassaBalance with parameters: " + "companyId: " + companyId+", kassaId: "+kassaId);
        try {return new ResponseEntity<>(commonUtilites.getSummFromHistory("kassa",companyId,kassaId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getKassaBalance error", e);
            return new ResponseEntity<>("Ошибка запроса баланса кассы ККМ", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping(
            value = "/api/auth/getAccountBalance",
            params = {"companyId","typeId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getAccountBalance(
            @RequestParam("companyId") Long companyId,
            @RequestParam("typeId") Long accountId){
        logger.info("Processing get request for path /api/auth/getAccountBalance with parameters: " + "companyId: " + companyId+", accountId: "+accountId);
        try {return new ResponseEntity<>(commonUtilites.getSummFromHistory("payment_account",companyId,accountId), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getAccountBalance error", e);
            return new ResponseEntity<>("Ошибка запроса баланса расчётного счёта", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getBalancesOnDate")
    public ResponseEntity<?> getBalancesOnDate(@RequestBody ProfitLossForm request){
        logger.info("Processing post request for path /api/auth/getProfitLoss: " + request.toString());
        try {return new ResponseEntity<>(financeUtilites.getBalancesOnDate(request.getCompanyId(),request.getDateFrom()), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка запроса баланса на начальную дату", HttpStatus.INTERNAL_SERVER_ERROR);}
    }


}