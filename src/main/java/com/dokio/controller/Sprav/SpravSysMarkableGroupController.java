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
import com.dokio.model.Sprav.SpravSysMarkableGroup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
@Controller
@Repository
public class SpravSysMarkableGroupController {
    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysMarkableGroup")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysMarkableGroup() {
        List<SpravSysMarkableGroup> resultList;
        String stringQuery=
                "select p.id as id, p.name as name, p.description as description" +
                        " from sprav_sys_markable_group p where p.name !=' '";
        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysMarkableGroup.class);
        resultList=query.getResultList();
        ResponseEntity<List> responseEntity = new ResponseEntity<>(resultList, HttpStatus.OK);
        return responseEntity;
    }
}
