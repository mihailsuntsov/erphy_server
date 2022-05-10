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
import com.dokio.message.response.Sprav.SpravSysPPRJSON;
import com.dokio.repository.UserRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SpravSysPPRController {
    Logger logger = Logger.getLogger("SpravSysPPRController");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSpravSysPPR",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSpravSysPPR()
    {
        logger.info("Processing get request for path /api/auth/getSpravSysPPR");
        String suffix=userRepositoryJPA.getMySuffix();
        String stringQuery=
                "select  p.id as id, " +
                        "p.name_"+suffix+" as name, " +
                        "p.abbreviation as abbreviation," +
                        "p.description as description," +
                        "p.id_api_atol as id_api_atol," +
                        "p.name_api_atol as name_api_atol " +
                        " from sprav_sys_ppr p where id in(1,4)";

        Query query =  entityManager.createNativeQuery(stringQuery);

        List<Object[]> queryList = query.getResultList();
        List<SpravSysPPRJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList) {
            SpravSysPPRJSON doc=new SpravSysPPRJSON();
            doc.setId(Long.parseLong(       obj[0].toString()));
            doc.setName((String)            obj[1]);
            doc.setAbbreviation((String)    obj[2]);
            doc.setDescription((String)     obj[3]);
            doc.setId_api_atol((Integer)    obj[4]);
            doc.setName_api_atol((String)   obj[5]);
            returnList.add(doc);
        }
        return new ResponseEntity<List>(returnList, HttpStatus.OK);
    }
}