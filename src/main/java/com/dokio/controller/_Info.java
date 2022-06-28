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


import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@RestController
@Repository
public class _Info {
    Logger logger = Logger.getLogger(_Info.class);
    @Autowired
    CommonUtilites cu;

    @PersistenceContext
    private EntityManager entityManager;

    public String getBackendVersion() {
        return "1.0.1-0";
    }

    public String getBackendVersionDate() {
        return "27-06-2022";
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

