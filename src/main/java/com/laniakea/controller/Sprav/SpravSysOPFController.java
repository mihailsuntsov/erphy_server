package com.laniakea.controller.Sprav;
import com.laniakea.model.Sprav.SpravSysOPFJSON;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;//если будет поиск по подстроке
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Controller
@Repository
public class SpravSysOPFController {

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/api/auth/getSpravSysOPF")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysOPF() {

        List<SpravSysOPFJSON> resultList;
        String stringQuery=
                "select p.id as id, p.name as name, p.abbreviation as abbreviation, p.description as description" +
                " from sprav_sys_opf p where p.name !=' '";
        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysOPFJSON.class);
        resultList=query.getResultList();
        ResponseEntity<List> responseEntity = new ResponseEntity<>(resultList, HttpStatus.OK);
        return responseEntity;
    }
}
