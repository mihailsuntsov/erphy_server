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

import com.dokio.message.request.Reports.IncomeOutcomeReportForm;
import com.dokio.message.request.Reports.IndicatorsForm;
import com.dokio.message.request.Reports.VolumesReportForm;
import com.dokio.message.response.Reports.VolumesReportJSON;
import com.dokio.repository.Reports.IncomeOutcomeRepository;
import com.dokio.repository.Reports.IndicatorsRepository;
import com.dokio.repository.Reports.VolumesRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DashboardVidgetsController {

    Logger logger = Logger.getLogger("DashboardVidgetsController");

    @Autowired
    VolumesRepository volumesRepository;
    @Autowired
    IncomeOutcomeRepository incomeOutcomeRepository;
    @Autowired
    IndicatorsRepository indicatorsRepository;

    @PostMapping("/api/auth/getVolumesReportData")
    public ResponseEntity<?> getVolumesReportData(@RequestBody VolumesReportForm request){
        logger.info("Processing post request for path /api/auth/getVolumesReportData: " + request.toString());
        try {
            return new ResponseEntity<>(volumesRepository.getVolumesReportData(request), HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method getVolumesReportData.", e);
            return new ResponseEntity<>("Controller getVolumesReportData error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/getIncomeOutcomeReportData")
    public ResponseEntity<?> getIncomeOutcomeReportData(@RequestBody IncomeOutcomeReportForm request){
        logger.info("Processing post request for path /api/auth/getIncomeOutcomeReportData: " + request.toString());
        try {
            return new ResponseEntity<>(incomeOutcomeRepository.getIncomeOutcomeReportData(request), HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method getIncomeOutcomeReportData.", e);
            return new ResponseEntity<>("Controller getIncomeOutcomeReportData error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/getIndicatorsData",
            params = {"company_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getIndicatorsData(
            @RequestParam("company_id") Long company_id){
        logger.info("Processing get request for path /api/auth/getIndicatorsData with parameters: " + "company_id: " + company_id);
        try {return new ResponseEntity<>(indicatorsRepository.getIndicatorsData(company_id), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getIndicatorsData error", e);
            return new ResponseEntity<>("Controller getIndicatorsData error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
