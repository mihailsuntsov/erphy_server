/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.repository;
import com.dokio.message.response.DocPermissionsJSON;
import com.dokio.message.response.additional.PermissionsJSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Repository
public class DocumentsRepositoryJPA {

    Logger logger = Logger.getLogger("DocumentsRepositoryJPA");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    public List<DocPermissionsJSON> getDocumentsWithPermissionList (String searchString)
    {
        String suffix = userRepositoryJPA.getMySuffix(); //language requesting
        List<PermissionsJSON> allPermissionsList = getPermissions(suffix);
        String stringQuery = "select" +
                "           p.id as id, " +
                "           p.doc_name_"+suffix+" as name " +
                "           from documents p where p.show = 1";
        if(searchString!= null && !searchString.isEmpty()){
            stringQuery = stringQuery+" and upper(CONCAT('%',:searchString,'%'))";
        }
        stringQuery = stringQuery+" order by p.doc_name_"+suffix;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<DocPermissionsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                List<PermissionsJSON> docPermissionsList = new ArrayList<>();
                if(!Objects.isNull(allPermissionsList))
                    for(PermissionsJSON permission:allPermissionsList){
                        if(obj[0].equals(permission.getDocument_id()))
                            docPermissionsList.add(permission);
                    }
                DocPermissionsJSON doc=new DocPermissionsJSON();
                doc.setId((Integer)                             obj[0]);
                doc.setName((String)                            obj[1]);
                doc.setPermissions(docPermissionsList);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method getDocumentsWithPermissionList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    private List<PermissionsJSON> getPermissions(String suffix){
        String stringQuery = "select" +
                "           p.id as id, " +
                "           p.name_"+suffix+" as name, " +
                "           p.document_id as document_id " +
                "           from permissions p order by p.output_order";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<PermissionsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                PermissionsJSON doc=new PermissionsJSON();
                doc.setId((Integer)                             obj[0]);
                doc.setName((String)                            obj[1]);
                doc.setDocument_id((Integer)                    obj[2]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method getPermissions. SQL query:" + stringQuery, e);
            return null;
        }
    }
}
