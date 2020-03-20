package com.laniakea.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.laniakea.service.StorageService;

@Controller
public class UploadController {

    @Autowired
    StorageService storageService;

    List<String> files = new ArrayList<String>();

//    @PostMapping("/api/auth/postProductImage")
//    public ResponseEntity<String> postProductImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("productId") String productId,
//            @RequestParam("description") String description,
//            @RequestParam("anonyme_access") Boolean anonyme_access) {
//        String message = "";
//        try {
//            storageService.storeProductImage(file,productId,description,anonyme_access);
//
//            //files.add(file.getOriginalFilename());
//
//            message = "You successfully uploaded " + file.getOriginalFilename() + "!";
//            return ResponseEntity.status(HttpStatus.OK).body(message);
//        } catch (Exception e) {
//            message = "FAIL to upload " + file.getOriginalFilename() + "!";
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
//        }
//    }

    @PostMapping("/api/auth/postFile")
    public ResponseEntity<String> postFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companyId") Integer companyId,
            @RequestParam("anonyme_access") Boolean anonyme_access,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Integer categoryId) {
        String message;
        try {
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
        List<String> fileNames = files
                .stream().map(fileName -> MvcUriComponentsBuilder
                        .fromMethodName(UploadController.class, "getFile", fileName).build().toString())
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(fileNames);
    }

    @GetMapping("/api/auth/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = storageService.loadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}