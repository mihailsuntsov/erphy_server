package com.laniakea.controller.Sprav;
import com.laniakea.message.response.Sprav.SpravSysWriteoffJSON;
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
    @PersistenceContext
    private EntityManager entityManager;
    @PostMapping("/api/auth/getSpravSysWriteoff")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getSpravSysWriteoff() {
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
