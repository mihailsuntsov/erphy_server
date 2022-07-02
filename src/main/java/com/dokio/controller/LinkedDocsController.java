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

import com.dokio.util.LinkedDocsUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
