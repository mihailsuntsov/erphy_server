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

package com.dokio.controller.Reports;

import com.dokio.message.request.Reports.ProfitLossForm;
import com.dokio.repository.Reports.ProfitLossRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfitLossController {

    Logger logger = Logger.getLogger("ProfitLossController");

    @Autowired
    ProfitLossRepositoryJPA profitLossRepositoryJPA;

    @PostMapping("/api/auth/getProfitLoss")
    public ResponseEntity<?> getProfitLoss(@RequestBody ProfitLossForm request){
        logger.info("Processing post request for path /api/auth/getProfitLoss: " + request.toString());
        try {return new ResponseEntity<>(profitLossRepositoryJPA.getProfitLoss(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка запроса данных отчёта", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/getOpexOnly")
    public ResponseEntity<?> getOpexOnly(@RequestBody ProfitLossForm request){
        logger.info("Processing post request for path /api/auth/getOpexOnly: " + request.toString());
        try {return new ResponseEntity<>(profitLossRepositoryJPA.getOpexOnly(request), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка запроса данных по операционным расходам", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
