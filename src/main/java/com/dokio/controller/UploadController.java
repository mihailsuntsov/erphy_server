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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dokio.repository.UserRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import com.dokio.service.StorageService;

@Controller
public class UploadController {
    Logger logger = Logger.getLogger("UploadController");

    @Autowired
    StorageService storageService;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    List<String> files = new ArrayList<String>();

    @PostMapping("/api/auth/postFile")
    public ResponseEntity<?> postFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") Long companyId,
            @RequestParam("anonyme_access") Boolean anonyme_access,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId) {
        logger.info("Processing post request for path api/auth/postFile: " + "fileName=" + file.getName()
                + "companyId=" + companyId  + "categoryId=" + categoryId);


        try {
            int fileSizeMb = Math.round(file.getSize()/1024/1024);
            //plan limit check
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if(userRepositoryJPA.getMyConsumedResources().getMegabytes()+fileSizeMb>userRepositoryJPA.getMyMaxAllowedResources().getMegabytes())
                    return ResponseEntity.status(HttpStatus.OK).body("-120"); // if current file will be uploaded, then sum size of all master-user files will out of bounds of tariff plan
            Long fileId = storageService.store(file,companyId,anonyme_access,categoryId,description,userRepositoryJPA.getMyMasterId(),userRepositoryJPA.getMyId(), false);
            return ResponseEntity.status(HttpStatus.OK).body(fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
    }




    @GetMapping("/api/auth/getallfiles")
    public ResponseEntity<List<String>> getListFiles(Model model) {
        logger.info("Processing get request for path api/auth/getallfiles");

        List<String> fileNames = files
                .stream().map(fileName -> MvcUriComponentsBuilder
                        .fromMethodName(UploadController.class, "getFile", fileName).build().toString())
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(fileNames);
    }

//    @GetMapping("/api/auth/files/{filename:.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
//        logger.info("Processing get request for path api/auth/files: " + "fileName=" + filename);
//
//        Resource file = storageService.loadFile(filename);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
//                .body(file);
//    }
}