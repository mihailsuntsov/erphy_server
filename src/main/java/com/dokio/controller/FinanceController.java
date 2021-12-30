/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getCagentBalance error", e);
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getPaymentAccountBalance error", e);
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getBoxofficeBalance error", e);
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getKassaBalance error", e);
            return new ResponseEntity<>("Ошибка запроса баланса кассы ККМ", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/getBalancesOnDate")
    public ResponseEntity<?> getBalancesOnDate(@RequestBody ProfitLossForm request){
        logger.info("Processing post request for path /api/auth/getProfitLoss: " + request.toString());
        try {return new ResponseEntity<>(financeUtilites.getBalancesOnDate(request.getCompanyId(),request.getDateFrom()), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка запроса баланса на начальную дату", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}