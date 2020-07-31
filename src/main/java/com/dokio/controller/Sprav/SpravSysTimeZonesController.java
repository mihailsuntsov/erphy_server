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
import com.dokio.message.response.Sprav.SpravSysTimeZonesJSON;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
@Controller
@Repository
public class SpravSysTimeZonesController {
    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysTimeZones")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysTimeZones() {
        String stringQuery=
                        "select " +
                        " p.id as id, " +
                        " p.time_offset  as time_offset, " +
                        " p.canonical_id as canonical_id, " +
                        " p.name_rus  as name_rus " +
                        " from sprav_sys_timezones p order by output_order asc";
        Query query =  entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<SpravSysTimeZonesJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList) {
            SpravSysTimeZonesJSON doc=new SpravSysTimeZonesJSON();
            doc.setId(Long.parseLong(obj[0].toString()));
            doc.setTime_offset((String) obj[1]);
            doc.setCanonical_id((String) obj[2]);
            doc.setName_rus((String) obj[3]);
            returnList.add(doc);
        }
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }
}
