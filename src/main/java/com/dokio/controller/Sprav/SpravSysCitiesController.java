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
import com.dokio.message.response.Sprav.SpravSysCitiesJSON;
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
public class SpravSysCitiesController {
    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysCities")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysCities(@RequestBody SearchForm request ) {
        String searchString=request.getSearchString();
        int countryId=request.getId();
        int regionId=request.getId2();
        String stringQuery;
        stringQuery = "select  p.id as id, " +
                "           p.name_ru as name_ru, " +
                "           p.country_id as country_id, " +
                "           p.region_id as region_id, " +
                "           coalesce(r.name_ru,'') as region_name_ru, " +
                "           coalesce(p.area_ru,'') as area_ru, " +
                "           p.big as big, " +
                "           c.name_ru as country_name_ru" +
                "           from sprav_sys_cities p " +
                "           left outer join sprav_sys_countries c on c.id=p.country_id " +
                "           left outer join sprav_sys_regions r on r.id=p.region_id ";

        stringQuery = stringQuery + " where coalesce(p.country_id,0) "+(countryId>0?("="+countryId):"=coalesce(p.country_id,0)");
        stringQuery = stringQuery + " and   coalesce(p.region_id,0) "+ (regionId>0? ("="+regionId): "=coalesce(p.region_id,0)");

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(p.name_ru) like upper('" + searchString + "%'))";
        }

        stringQuery = stringQuery + " order by p.big desc, p.name_ru asc";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<SpravSysCitiesJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList){
            SpravSysCitiesJSON doc=new SpravSysCitiesJSON();
            doc.setId((Integer)                 obj[0]);
            doc.setName_ru((String)             obj[1]);
            doc.setCountry_id((Integer)         obj[2]);
            doc.setRegion_id((Integer)          obj[3]);
            doc.setRegion_name_ru((String)      obj[4]);
            doc.setArea_ru((String)             obj[5]);
            doc.setBig((Boolean)                obj[6]);
            doc.setCountry_name_ru((String)     obj[7]);
            returnList.add(doc);
        }
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }
}