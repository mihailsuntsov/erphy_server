package com.laniakea.repository;
import com.laniakea.model.Documents;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class DocumentsRepositoryJPA {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Documents> getDocumentsWithPermissionList(String searchString) {
        String stringQuery;
        Long documentOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery="from Documents p where p.show =1";
        if(searchString!= null && !searchString.isEmpty()){
            stringQuery = stringQuery+" and upper(p.name) like upper('%"+searchString+"%')";
        }
        stringQuery = stringQuery+" order by p.id asc";
        Query query =  entityManager.createQuery(stringQuery,Documents.class);
        return query.getResultList();
    }
}
