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
package com.dokio.controller.Sprav;

import com.dokio.message.request.Sprav.TemplatesListForm;
import com.dokio.repository.TemplateRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
public class TemplatesController {

    @Autowired
    TemplateRepositoryJPA templateRepository;

    Logger logger = Logger.getLogger("TemplatesController");

//    @RequestMapping(
//            value = "/api/auth/getTemplateTypesList",
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getTemplateTypesList(){
//        logger.info("Processing get request for path /api/auth/getTemplateTypesList");
//        try {return new ResponseEntity<>(templateRepository.getTemplateTypesList(), HttpStatus.OK);}
//        catch (Exception e){e.printStackTrace();logger.error("Controller getTemplateTypesList error", e);
//            return new ResponseEntity<>("Ошибка загрузки списка типов шаблонов", HttpStatus.INTERNAL_SERVER_ERROR);}
//    }

    @RequestMapping(
            value = "/api/auth/getTemplatesList",
            params = {"company_id", "document_id", "is_show"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getTemplatesList(
            @RequestParam("company_id") Long company_id,
            @RequestParam("document_id")int document_id,
            @RequestParam("is_show")boolean is_show){
        logger.info("Processing get request for path /api/auth/getTemplatesList with parameters: " + "company_id: " + company_id +", document_id: "+document_id+", is_show: "+is_show);
        try {return new ResponseEntity<>(templateRepository.getTemplatesList(company_id, document_id, is_show), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getTemplatesList error", e);
            return new ResponseEntity<>("Ошибка загрузки списка шаблонов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/saveTemplates")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveTemplates(@RequestBody TemplatesListForm request){
        logger.info("Processing post request for path /api/auth/saveTemplates: " + request.toString());
        try {return new ResponseEntity<>(templateRepository.saveTemplates(request.getCompany_id(), request.getDocument_id(), request.getTemplatesList()), HttpStatus.OK);}
        catch (Exception e){return new ResponseEntity<>("Ошибка сохранения шаблонов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @RequestMapping("/api/auth/getPrintVersion")
    public void getPrintVersion (HttpServletResponse response) throws Exception {



    }
}
