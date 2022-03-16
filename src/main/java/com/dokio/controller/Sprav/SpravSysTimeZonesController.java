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
import com.dokio.message.response.Sprav.SpravSysTimeZonesJSON;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@Repository
public class SpravSysTimeZonesController {
    Logger logger = Logger.getLogger("SpravSysTimeZonesController");

    @PersistenceContext
    private EntityManager entityManager;


    private static final Set VALID_SUFFIXES
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("ru","en","es","it","fr","uk","kz","pt")
            .collect(Collectors.toCollection(HashSet::new)));

    @RequestMapping(value = "/api/auth/getSpravSysTimeZones",
            params = {"suffix"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysTimeZones(@RequestParam("suffix") String suffix) {

        logger.info("Processing get request for path /api/auth/getSpravSysTimeZones with params: suffix="+suffix);

        String stringQuery=
                        "select " +
                        " p.id as id, " +
                        " p.time_offset  as time_offset, " +
                        " p.canonical_id as canonical_id, " +
                        " p.name_"+suffix+"  as name " +
                        " from sprav_sys_timezones p order by output_order asc";
        try {

            if (!VALID_SUFFIXES.contains(suffix)) {
                throw new IllegalArgumentException("Bad query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            List<SpravSysTimeZonesJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                SpravSysTimeZonesJSON doc = new SpravSysTimeZonesJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setTime_offset((String) obj[1]);
                doc.setCanonical_id((String) obj[2]);
                doc.setName((String) obj[3]);
                returnList.add(doc);
            }
            return new ResponseEntity<List>(returnList, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSpravSysTimeZones. SQL query:" + stringQuery, e);
            return null;
        }
    }
}
