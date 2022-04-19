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

import com.dokio.message.request.Sprav.TemplatesForm;
import com.dokio.message.request.Sprav.TemplatesListForm;
import com.dokio.message.response.Sprav.TemplateTypesJSON;
import com.dokio.message.response.Sprav.TemplatesListJSON;
import com.dokio.util.CommonUtilites;
import com.sun.org.apache.xml.internal.security.signature.ObjectContainer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Repository
public class TemplateRepositoryJPA {


    Logger logger = Logger.getLogger("TemplateRepositoryJPA");


    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    private CommonUtilites commonUtilites;


//    public List<TemplateTypesJSON> getTemplateTypesList ()
//    {
//            String stringQuery;
//            stringQuery = "select  p.id as id, " +
//                    "           p.name_ru as template_type_name, " +
//                    "           p.template_type as template_type " +
//                    "           from template_types p ";
//            stringQuery = stringQuery + " order by p.name_ru";
//
//            try {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                List<Object[]> queryList = query.getResultList();
//                List<TemplateTypesJSON> returnList = new ArrayList<>();
//                for(Object[] obj:queryList){
//                    TemplateTypesJSON doc=new TemplateTypesJSON();
//                    doc.setId((Integer)                           obj[0]);
//                    doc.setTemplate_type_name((String)            obj[1]);
//                    doc.setTemplate_type((String)                 obj[2]);
//                    returnList.add(doc);
//                }
//                return returnList;
//            } catch (Exception e){
//                e.printStackTrace();
//                logger.error("Exception in method getTemplateTypesList. SQL query:" + stringQuery, e);
//                return null;
//            }
//    }

    // отдаёт список имеющихся шаблонов у типа документа document_id
    public List<TemplatesListJSON> getTemplatesList (Long companyId, int document_id, boolean is_show)
    {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        Long myId = userRepositoryJPA.getMyId();
        stringQuery = "select  p.id as id, " +                            // id шаблона
                "           p.name as name, " +                           // наименование шаблона. Например, Товарный чек
                "           p.document_id as document_id, " +             // id документа, в котором будет возможность печати данного шаблона
                "           coalesce(p.is_show,false) as is_show, " +     // показывать шаблон в выпадающем списке на печать
                "           p.output_order as output_order, " +           // порядок вывода наименований шаблонов в списке на печать
                "           f.name as file_name, " +                      // наименование файла как он хранится на диске
                "           f.original_name as file_original_name, " +    // оригинальное наименование файла
                "           f.id as file_id, " +                          // id файла
                "           p.company_id as company_id " +                // id предприятия

                "           from template_docs p " +
                "           INNER JOIN files f ON p.file_id = f.id " +
                "           where  p.master_id=" + myMasterId +
                "           and p.company_id=" + companyId +
                "           and p.user_id=" + myId +
                "           and p.document_id = "+document_id;
        // если параметр is_show = true, показываем только то что включено переключателем в положение "отображать в списке шаблонов"
        if(is_show) stringQuery = stringQuery + " and p.is_show = true";
        stringQuery = stringQuery + " order by p.output_order";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<TemplatesListJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                TemplatesListJSON doc=new TemplatesListJSON();
                doc.setId(Long.parseLong(                       obj[0].toString()));
                doc.setName((String)                            obj[1]);
                doc.setDocument_id((Integer)                    obj[2]);
                doc.setIs_show((Boolean)                        obj[3]);
                doc.setOutput_order((Integer)                   obj[4]);
                doc.setFile_name((String)                       obj[5]);
                doc.setFile_original_name((String)              obj[6]);
                doc.setFile_id(Long.parseLong(                  obj[7].toString()));
                doc.setCompany_id(Long.parseLong(               obj[8].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method getTemplatesList. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Boolean saveTemplates(Long companyId, int documentId, Set<TemplatesForm> templatesList){
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        Long myId = userRepositoryJPA.getMyId();
        Set<Long> templateIds=new HashSet<>();
        try {

            // Сначала удалим темплейты, которые были удалены на фронете.
            // Собираем List из id оставшихся темплейтов, и удаляем все что не входит в этот List
            for (TemplatesForm template : templatesList) {templateIds.add(template.getId());}
            deleteTemplatesExcessRows(templatesList.size()>0?(commonUtilites.SetOfLongToString(templateIds,",","","")):"0", myMasterId, companyId, documentId, myId);

            // Затем в зависимости от того, есть или нет такой темплейт в БД, делаем соответственно update или insert
            for (TemplatesForm template : templatesList) {
                if(Objects.isNull(template.getId()))
                    insertTemplate(template, myMasterId, myId);
                else
                    updateTemplate(template, myMasterId, myId);
            }

        } catch (Exception e){
            e.printStackTrace();
            logger.error("Exception in method saveTemplates.", e);
            return null;
        }
        return true;
    }
    private Boolean insertTemplate(TemplatesForm template,Long masterId,Long myId) throws Exception {
        String stringQuery =
                " insert into template_docs (" +
                        " master_id," +
                        " company_id," +
                        " user_id," +
                        " name,"+
                        " document_id,"+
                        " file_id," +
                        " is_show," +
                        " output_order" +
                        ") values ("+
                        masterId + ","+
                        template.getCompany_id() + ","+
                        myId + ","+
                        ":name ,"+
                        template.getDocument_id() + ","+
                        template.getFile_id() + ","+
                        template.getIs_show() + ","+
                        template.getOutput_order()+ ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",template.getName());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method insertTemplate. SQL: " + stringQuery, e);
            throw new Exception();//кидаем исключение чтобы произошла отмена транзакции
        }
    }
    private Boolean updateTemplate(TemplatesForm template,Long masterId,Long myId) throws Exception {
        String stringQuery =
                    " update template_docs set" +
                    " name = :name, " +
                    " file_id = " + template.getFile_id() + ","+
                    " is_show = " + template.getIs_show() + ","+
                    " output_order = "+ template.getOutput_order() +
                    " where master_id = " + masterId + " and company_id=" + template.getCompany_id() +" and user_id = " + myId + " and id = " + template.getId();
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",template.getName());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method updateTemplate. SQL: " + stringQuery, e);
            throw new Exception();//кидаем исключение чтобы произошла отмена транзакции
        }
    }
    private Boolean deleteTemplatesExcessRows(String templatesIds, Long masterId, Long companyId, int documentId, Long myId) throws Exception {
        String stringQuery;
        stringQuery =   " delete from template_docs " +
                " where master_id=" + masterId +
                " and company_id=" + companyId +
                " and user_id = " + myId +
                " and document_id=" + documentId +
                " and id not in (" + templatesIds + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method deleteTemplatesExcessRows. SQL - "+stringQuery, e);
            e.printStackTrace();
            throw new Exception();//кидаем исключение чтобы произошла отмена транзакции
        }
    }

}
