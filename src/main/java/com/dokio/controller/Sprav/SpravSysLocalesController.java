
package com.dokio.controller.Sprav;

import com.dokio.message.response.Sprav.SpravSysLocalesJSON;
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
public class SpravSysLocalesController {

    Logger logger = Logger.getLogger("SpravSysLocalesController");

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSpravSysLocales",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSpravSysLocales()
    {
        logger.info("Processing get request for path /api/auth/getSpravSysLocales");

        String stringQuery=
                "select p.id as id, " +
                        "p.name as name, " +
                        "p.code as code" +
                        " from sprav_sys_locales p";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            List<SpravSysLocalesJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                SpravSysLocalesJSON doc = new SpravSysLocalesJSON();
                doc.setId(Long.parseLong(           obj[0].toString()));
                doc.setName((String)                obj[1]);
                doc.setCode((String)                obj[2]);
                returnList.add(doc);
            }
            return new ResponseEntity<List>(returnList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSpravSysLocales. SQL query:" + stringQuery, e);
            return null;
        }
    }
}