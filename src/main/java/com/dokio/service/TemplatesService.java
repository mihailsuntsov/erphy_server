package com.dokio.service;

import com.dokio.message.response.FileInfoJSON;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;



@Service
@Repository
public class TemplatesService {

    Logger logger = Logger.getLogger("TemplatesService");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;


    @SuppressWarnings("Duplicates")//отдача данных (original_name, path) о файле, если есть права или если он открыт на общий доступ
    public FileInfoJSON getFileName(String filename) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            stringQuery = "select " +
                    "           p.original_name as original_name, " +
                    "           p.path as path " +
                    "           from files p " +
                    "           where p.master_id = " + myMasterId + " and p.name= :filename";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("filename", filename);
            List<Object[]> queryList = query.getResultList();
            if(queryList.size()>0) {
                FileInfoJSON doc = new FileInfoJSON();
                doc.setOriginal_name((String) queryList.get(0)[0]);
                doc.setPath((String) queryList.get(0)[1]);
                return doc;
            }
            else {
                logger.error("File " + filename + " not found in database.");
                return new FileInfoJSON();
            }
        } catch (Exception e) {
            logger.error("Exception in method getFileName. SQL: " + stringQuery);
            e.printStackTrace();
            return null;
        }

    }



}
