/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package com.dokio.repository;
import com.dokio.message.response.DocPermissionsJSON;
import com.dokio.message.response.additional.BaseFiles;
import com.dokio.message.response.additional.PermissionsJSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("searchString", searchString);}

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

//            "master_id"
//            "company_id"
//            "file_id"
//            "document_id"
//            "is_show"
//            "output_order"
//            "name"
//            "user_id" /

    @Transactional
    public void createPrintMenus(List<BaseFiles> baseFilesList, Long masterId, Long userId, Long companyId){
        List<String> insertingRows = new ArrayList<>();
        int i = 0;
        for (BaseFiles bf : baseFilesList){
            if(!Objects.isNull(bf.getDocId())){
                insertingRows.add((i>0?", ":"") + "("+masterId+", "+companyId+", "+bf.getFileId()+", "+bf.getDocId()+", true, "+(i+1)+", '"+bf.getMenuName()+"', "+userId+", '"+bf.getType()+"', "+bf.getNum_labels_in_row()+")");
            }
            i++;
        }
        if(insertingRows.size()>0){
            String stringQuery =
                    " insert into template_docs " +
                    " (master_id, company_id, file_id, document_id, is_show, output_order, name, user_id, type, num_labels_in_row)" +
                    " values ";
            for(String row : insertingRows){
                stringQuery = stringQuery + row;
            }
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            } catch (Exception e){
                e.printStackTrace();
                logger.error("Exception in method createPrintMenus. SQL query:" + stringQuery, e);
            }
        }
    }
}
