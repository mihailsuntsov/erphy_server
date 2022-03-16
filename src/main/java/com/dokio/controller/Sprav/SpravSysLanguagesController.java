
package com.dokio.controller.Sprav;

import com.dokio.message.response.Sprav.SpravSysLanguagesJSON;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Controller
@Repository
public class SpravSysLanguagesController {

    Logger logger = Logger.getLogger("SpravSysLanguagesController");

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSpravSysLanguages",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSpravSysLanguages()
    {
        logger.info("Processing get request for path /api/auth/getSpravSysLanguages");

        String stringQuery=
                "select p.id as id, " +
                "p.name as name, " +
                "p.suffix as suffix," +
                "p.default_locale_id as default_locale_id" +
                " from sprav_sys_languages p";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            List<SpravSysLanguagesJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                SpravSysLanguagesJSON doc = new SpravSysLanguagesJSON();
                doc.setId(Long.parseLong(           obj[0].toString()));
                doc.setName((String)                obj[1]);
                doc.setSuffix((String)              obj[2]);
                doc.setDefaultLocaleId((Integer)    obj[3]);
                returnList.add(doc);
            }
            return new ResponseEntity<List>(returnList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSpravSysLanguages. SQL query:" + stringQuery, e);
            return null;
        }
    }
}