package com.laniakea.controller.Sprav;
import com.laniakea.message.response.Sprav.SpravSysNdsJSON;
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
public class SpravSysNdsController {
    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysNds")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysNds() {
        List<SpravSysNdsJSON> resultList;
        String stringQuery=
                "select p.id as id, p.name as name, p.description as description" +
                        " from sprav_sys_nds p where p.name !=' '";
        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysNdsJSON.class);
        resultList=query.getResultList();
        ResponseEntity<List> responseEntity = new ResponseEntity<>(resultList, HttpStatus.OK);
        return responseEntity;
    }
}