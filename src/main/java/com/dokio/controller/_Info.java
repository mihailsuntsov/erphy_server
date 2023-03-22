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

import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RestController
@Repository
public class _Info {
    Logger logger = Logger.getLogger(_Info.class);
    @Autowired
    CommonUtilites cu;

    @PersistenceContext
    private EntityManager entityManager; // do NOT touch, even if it seems like not active!

    public String getBackendVersion() {
        return "1.2.0-1";
    }

    public String getBackendVersionDate() {
        return "13-03-2023";
    }

    @RequestMapping(value = "/api/public/getSettingsGeneral",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsGeneral() {
        logger.info("Processing get request for path /api/auth/getSettingsGeneral with no params");
        try {return new ResponseEntity<>(cu.getSettingsGeneral(), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getSettingsGeneral error", e);
            return new ResponseEntity<>("Error query of general settings", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}

