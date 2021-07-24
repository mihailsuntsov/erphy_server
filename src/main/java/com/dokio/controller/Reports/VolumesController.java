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

import com.dokio.message.request.Reports.VolumesReportForm;
import com.dokio.message.response.Reports.VolumesReportJSON;
import com.dokio.repository.Reports.VolumesRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;




@Controller
public class VolumesController {

    Logger logger = Logger.getLogger("VolumesController");
    @Autowired
    VolumesRepository volumesRepository;

    @PostMapping("/api/auth/getVolumesReportData")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getVolumesReportData(@RequestBody VolumesReportForm request){
        logger.info("Processing post request for path /api/auth/getVolumesReportData: " + request.toString());
        List<VolumesReportJSON> returnList;
        try {
            returnList = volumesRepository.getVolumesReportData(request);
            ResponseEntity responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e){
            e.printStackTrace();
            ResponseEntity responseEntity = new ResponseEntity<>("Ошибка при запросе отчёта объёмов", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}
