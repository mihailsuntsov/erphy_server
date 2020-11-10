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
import com.dokio.message.response.Sprav.SpravSysWriteoffJSON;
import org.apache.log4j.Logger;
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
public class SpravSysWriteoffController {
    Logger logger = Logger.getLogger("SpravSysWriteoffController");

    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysWriteoff")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysWriteoff() {
        logger.info("Processing post request for path /api/auth/getSpravSysWriteoff");

        String stringQuery=
                "select " +
                        " p.id as id, " +
                        " p.name  as name, " +
                        " p.debet as debet, " +
                        " p.description  as description " +
                        " from sprav_sys_writeoff p order by p.id asc";
        Query query =  entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<SpravSysWriteoffJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList) {
            SpravSysWriteoffJSON doc=new SpravSysWriteoffJSON();
            doc.setId(Long.parseLong(       obj[0].toString()));
            doc.setName((String)            obj[1]);
            doc.setDebet((String)           obj[2]);
            doc.setDescription((String)     obj[3]);
            returnList.add(doc);
        }
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }
}
