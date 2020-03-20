package com.laniakea.controller.Sprav;
import com.laniakea.model.Sprav.SpravSysPriceRoleJSON;
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
public class SpravSysPriceRoleController {
    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysPriceRole")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysPriceRole() {
        List<SpravSysPriceRoleJSON> resultList;
        String stringQuery=
                "select p.id as id, p.name || ' (' || p.description || ')' as name, p.description as description" +
                        " from sprav_sys_pricerole p where p.name !=' '";
        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysPriceRoleJSON.class);
        resultList=query.getResultList();
        ResponseEntity<List> responseEntity = new ResponseEntity<>(resultList, HttpStatus.OK);
        return responseEntity;
    }
}
