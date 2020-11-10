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
package com.dokio.controller.Sprav;
import com.dokio.message.request.SearchForm;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.Sprav.SpravSysRegionsJSON;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
@Controller
@Repository
public class SpravSysRegionsController {
    Logger logger = Logger.getLogger("SpravSysRegionsController");

    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysRegions")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysRegions(@RequestBody SearchForm request ) {
        logger.info("Processing post request for path /api/auth/getSpravSysRegions: " + request.toString());

        String searchString=request.getSearchString();
        int countryId=request.getId();
        String stringQuery;
        stringQuery = "select  p.id as id, " +
                "           p.name_ru as name_ru, " +
                "           p.country_id as country_id, " +
                "           c.name_ru as country_name_ru" +
                "           from sprav_sys_regions p " +
                "           left outer join sprav_sys_countries c on c.id=p.country_id ";

        stringQuery = stringQuery + " where p.country_id "+(countryId>0?("="+countryId):">0");

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(p.name_ru) like upper('" + searchString + "%'))";
        }

        stringQuery = stringQuery + " order by p.name_ru asc";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<SpravSysRegionsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
            SpravSysRegionsJSON doc=new SpravSysRegionsJSON();
            doc.setId((Integer)                 obj[0]);
            doc.setName_ru((String)             obj[1]);
            doc.setCountry_id((Integer)         obj[2]);
            doc.setCountry_name_ru((String)     obj[3]);
            returnList.add(doc);
        }
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }
    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/getRegionIdByCityId")
    private ResponseEntity<?> getRegionIdByCityId(@RequestBody SearchForm request) {
        logger.info("Processing post request for path /api/auth/getRegionIdByCityId" + request.toString());

        IdAndName doc = new IdAndName();
        try {
            String stringQuery = "select " +
                    " region_id as id," +
                    " (select name_ru from sprav_sys_regions where id=p.region_id) as name from sprav_sys_cities p where p.id = " + request.getId();
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            doc.setId(Long.parseLong(                   queryList.get(0)[0].toString()));
            doc.setName((String)                        queryList.get(0)[1]);
            return new ResponseEntity<>(doc, HttpStatus.OK);
        }catch (Exception e){
            doc.setId(0L);
            doc.setName("");
            return new ResponseEntity<>(doc, HttpStatus.OK);
        }
    }
}
