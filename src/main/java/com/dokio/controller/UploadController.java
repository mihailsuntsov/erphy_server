/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public ResponseEntity<String> postFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") Integer companyId,
            @RequestParam("anonyme_access") Boolean anonyme_access,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Integer categoryId) {
        logger.info("Processing post request for path api/auth/postFile: " + "fileName=" + file.getName()
                + "companyId=" + companyId  + "categoryId=" + categoryId);


        String message;
        try {
            int fileSizeMb = Math.round(file.getSize()/1024/1024);
            //plan limit check
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if(userRepositoryJPA.getMyConsumedResources().getMegabytes()+fileSizeMb>userRepositoryJPA.getMyMaxAllowedResources().getMegabytes())
                    return ResponseEntity.status(HttpStatus.OK).body("-120"); // if current file will be uploaded, then sum size of all master-user files will out of bounds of tariff plan
            storageService.store(file,companyId,anonyme_access,categoryId,description);
            message = "You successfully uploaded " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "FAIL to upload " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
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

    @GetMapping("/api/auth/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        logger.info("Processing get request for path api/auth/files: " + "fileName=" + filename);

        Resource file = storageService.loadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}