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

import com.dokio.message.request.UniversalForm;
import com.dokio.message.request.additional.ChangeOwnerForm;
import com.dokio.repository.AppointmentRepositoryJPA;
import com.dokio.repository.FileRepositoryJPA;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommonUtilitesController {


    Logger logger = Logger.getLogger("CommonUtilites");

    @Autowired
    CommonUtilites commonUtilites;
    @Autowired
    AppointmentRepositoryJPA appointmentRepository;
    @Autowired
    FileRepositoryJPA filesRepository;


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

    @RequestMapping(value = "/api/auth/isSiteNameAllowed",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> isSiteNameAllowed(
            @RequestParam("name") String name)
    {
        logger.info("Processing get request for path /api/auth/isSiteNameAllowed with parameters: " +
                "name: "+ name);
        try {return new ResponseEntity<>(commonUtilites.isSiteNameAllowed(name), HttpStatus.OK);}
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Controller isSiteNameAllowed error", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/api/auth/translateHTMLmessage",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> translateHTMLmessage(
            @RequestParam("key") String key)
    {
        logger.info("Processing get request for path /api/auth/translateHTMLmessage with parameters: " +
                "key: "+ key);
        try {return new ResponseEntity<>(commonUtilites.translateHTMLmessage(key), HttpStatus.OK);}
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Controller translateHTMLmessage error", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/changeDocumentStatus",
            params = {"documentsTableId", "docId", "statusId"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> changeDocumentStatus(
            @RequestParam("documentsTableId") Integer documentsTableId,
            @RequestParam("docId") Long docId,
            @RequestParam("statusId") Long statusId){
        logger.info("Processing get request for path /api/auth/changeDocumentStatus with parameters: " + "documentsTableId: " + documentsTableId.toString() + ", docId: " + docId.toString() + ", statusId: " + statusId.toString());
        try {
            Integer ret = null;
            switch (documentsTableId) {
                case (59):// Appointments
                    ret = appointmentRepository.changeAppointmentStatus(docId, statusId);
                    break;
            }
            return new ResponseEntity<>(ret, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Controller changeDocumentStatus error", e);
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/changeDocumentOwner")
    public ResponseEntity<?> changeDocumentOwner(@RequestBody ChangeOwnerForm request){
        logger.info("Processing get request for path /api/auth/changeDocOwner with parameters: " + request.toString());
        try {return new ResponseEntity<>(commonUtilites.changeDocumentOwner(request), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Controller changeDocumentOwner error", e);
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);}
    }
}
