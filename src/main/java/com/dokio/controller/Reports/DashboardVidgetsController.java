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
            return new ResponseEntity<>("Ошибка при запросе отчёта объёмов", HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>("Ошибка при запросе отчёта прихода и расхода", HttpStatus.INTERNAL_SERVER_ERROR);
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
        catch (Exception e){e.printStackTrace();logger.error("Contrloller getIndicatorsData error", e);
            return new ResponseEntity<>("Ошибка загрузки данных для индикаторов стартовой страницы", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}
