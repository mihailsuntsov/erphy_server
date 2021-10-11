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

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.SettingsCustomersOrdersForm;
import com.dokio.message.response.CustomersOrdersJSON;
import com.dokio.message.response.Settings.SettingsCustomersOrdersJSON;
import com.dokio.message.response.additional.*;
import com.dokio.repository.*;
import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LinkedDocsController {

    Logger logger = Logger.getLogger("LinkedDocsController");

    @Autowired
    LinkedDocsUtilites linkedDocsUtilites;


    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getLinkedDocsScheme",
            params = {"uid"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getLinkedDocsScheme(
            @RequestParam("uid") String uid)
    {
        logger.info("Processing get request for path /api/auth/getLinkedDocsScheme with parameters: " +
                "uid: " + uid);
        return new ResponseEntity(linkedDocsUtilites.getLinkedDocsScheme(uid), HttpStatus.OK);
    }




}
