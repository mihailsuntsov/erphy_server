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

}
