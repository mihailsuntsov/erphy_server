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
import org.apache.log4j.Logger;
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

    @PersistenceContext
    private EntityManager entityManager;

    private String getBackendVersion() {
        return "1.000-0";
    }

    private String getBackendVersionDate() {
        return "02/06/2022";
    }

    @RequestMapping(value = "/api/public/getSettingsGeneral",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsGeneral() {
        logger.info("Processing get request for path /api/auth/getSettingsGeneral with no params");
        String stringQuery =
                "select " +
                        " p.show_registration_link as show_registration_link, " +
                        " p.allow_registration as allow_registration, " +
                        " p.show_forgot_link as show_forgot_link, " +
                        " p.allow_recover_password as allow_recover_password, " +
                        " (select value from version) as database_version," +
                        " (select date from version) as database_date," +
                        " p.show_in_signin as show_in_signin" +
                        " from settings_general p";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsGeneralJSON doc = new SettingsGeneralJSON();
            if (queryList.size() > 0) {
                doc.setShowRegistrationLink((Boolean) queryList.get(0)[0]);
                doc.setAllowRegistration((Boolean) queryList.get(0)[1]);
                doc.setShowForgotLink((Boolean) queryList.get(0)[2]);
                doc.setAllowRecoverPassword((Boolean) queryList.get(0)[3]);
                doc.setDatabaseVersion((String) queryList.get(0)[4]);
                doc.setDatabaseVersionDate((String) queryList.get(0)[5]);
                doc.setShowInSignin((String) queryList.get(0)[6]);
                doc.setBackendVersion(getBackendVersion());
                doc.setBackendVersionDate(getBackendVersionDate());
            }
            return new ResponseEntity<>(doc, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSettingsGeneral. SQL query:" + stringQuery, e);
            return null;
        }
    }

}

