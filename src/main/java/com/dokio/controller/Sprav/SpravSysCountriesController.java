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
import com.dokio.message.response.CagentsJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.message.response.Sprav.SpravSysCountriesJSON;
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
public class SpravSysCountriesController {
    Logger logger = Logger.getLogger("SpravSysCountriesController");

    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysCountries")
    @SuppressWarnings("Duplicates")
    private ResponseEntity<?> getSpravSysCountries() {
        logger.info("Processing post request for path /api/auth/getSpravSysCountries");

        String stringQuery=
                "select " +
                        " p.id as id, " +
                        " p.name_ru  as name_ru " +
                        " from sprav_sys_countries p where name_ru='Россия' or name_ru = 'Беларусь'  or name_ru = 'Украина' order by p.name_ru asc";
        Query query =  entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<SpravSysCountriesJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList) {
            SpravSysCountriesJSON doc=new SpravSysCountriesJSON();
            doc.setId ((Integer) obj[0]);
            doc.setName_ru((String) obj[1]);
            returnList.add(doc);
        }
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/getCountryIdByRegionId")
    private ResponseEntity<?> getCountryIdByRegionId(@RequestBody SearchForm request) {
        logger.info("Processing post request for path /api/auth/getCountryIdByRegionId: " + request.toString());

        IdAndName doc = new IdAndName();
        try{
            String stringQuery="select p.country_id as id, (select name_ru from sprav_sys_countries where id=p.country_id) as name from sprav_sys_regions p where p.id = "+request.getId();
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
