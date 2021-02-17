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

import com.dokio.message.request.UniversalForm;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommonUtilitesController {


    Logger logger = Logger.getLogger("CommonUtilites");

    @Autowired
    CommonUtilites commonUtilites;

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/isDocumentNumberUnical",
            params = {"company_id", "doc_number", "doc_id", "table"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")

    public ResponseEntity<?> isDocumentNumberUnical(
            @RequestParam("company_id") Long company_id,
            @RequestParam("doc_number") Integer doc_number,
            @RequestParam("doc_id") Long doc_id,
            @RequestParam("table") String table)
    {
        logger.info("Processing get request for path /api/auth/isDocumentNumberUnical with parameters: " +
                "company_id: " + company_id.toString() +
                ", doc_number: " + doc_number.toString() +
                ", doc_id: " + doc_id.toString() +
                ", table: "+ table);
        try {
            Boolean ret = commonUtilites.isDocumentNumberUnical(company_id, doc_number, doc_id, table);
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
