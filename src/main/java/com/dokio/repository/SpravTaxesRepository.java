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

import com.dokio.message.request.Sprav.SpravTaxesForm;
import com.dokio.message.response.Sprav.SpravTaxesJSON;
import com.dokio.message.response.Sprav.SpravTaxesListJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class SpravTaxesRepository {

    Logger logger = Logger.getLogger("SpravTaxesRepository");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserDetailsServiceImpl userService;


    @Transactional
    @SuppressWarnings("Duplicates")


    public List<SpravTaxesJSON> getTaxesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(50L, "640,641"))// (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           p.value as value, " +
                    "           p.multiplier as multiplier, " +
                    "           p.is_active as is_active, " +
                    "           p.is_deleted as is_deleted, " +
                    "           p.name_api_atol as name_api_atol, " +
                    "           p.output_order as output_order, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort  " +
                    "           from sprav_taxes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(50L, "640")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or " +
                        "upper(p.description) like upper('%" + searchString + "%')" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;

            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            List<Object[]> queryList = query.getResultList();
            List<SpravTaxesJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                SpravTaxesJSON doc = new SpravTaxesJSON();

                doc.setId(Long.parseLong(                           obj[0].toString()));
                doc.setMaster((String)                              obj[1]);
                doc.setCreator((String)                             obj[2]);
                doc.setChanger((String)                             obj[3]);
                doc.setMaster_id(Long.parseLong(                    obj[4].toString()));
                doc.setCreator_id(Long.parseLong(                   obj[5].toString()));
                doc.setChanger_id(obj[6] != null ? Long.parseLong(  obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(                   obj[7].toString()));
                doc.setCompany((String)                             obj[8]);
                doc.setDate_time_created((String)                   obj[9]);
                doc.setDate_time_changed((String)                   obj[10]);
                doc.setName((String)                                obj[11]);
                doc.setDescription((String)                         obj[12]);
                doc.setValue((Integer)                              obj[13]);
                doc.setMultiplier((BigDecimal)                      obj[14]);
                doc.setIs_active((Boolean)                          obj[15]);
                doc.setIs_deleted((Boolean)                         obj[16]);
                doc.setName_api_atol((String)                       obj[17]);
                doc.setOutput_order((Integer)                       obj[18]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getTaxesSize(String searchString, Long companyId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(50L, "640,641"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from sprav_taxes p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(50L, "640")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or " +
                        "upper(p.description) like upper('%" + searchString + "%')" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery);

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public SpravTaxesJSON getTaxesValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(50L,"640,641"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description, " +
                    "           p.value as value, " +
                    "           p.multiplier as multiplier, " +
                    "           p.is_active as is_active, " +
                    "           p.is_deleted as is_deleted, " +
                    "           p.name_api_atol as name_api_atol, " +
                    "           p.output_order as output_order " +
                    "           from sprav_taxes p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(50L, "640")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (641)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();

                SpravTaxesJSON doc = new SpravTaxesJSON();

                for (Object[] obj : queryList) {

                    doc.setId(Long.parseLong(                           obj[0].toString()));
                    doc.setMaster((String)                              obj[1]);
                    doc.setCreator((String)                             obj[2]);
                    doc.setChanger((String)                             obj[3]);
                    doc.setMaster_id(Long.parseLong(                    obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(                   obj[5].toString()));
                    doc.setChanger_id(obj[6] != null ? Long.parseLong(  obj[6].toString()) : null);
                    doc.setCompany_id(Long.parseLong(                   obj[7].toString()));
                    doc.setCompany((String)                             obj[8]);
                    doc.setDate_time_created((String)                   obj[9]);
                    doc.setDate_time_changed((String)                   obj[10]);
                    doc.setName((String)                                obj[11]);
                    doc.setDescription((String)                         obj[12]);
                    doc.setValue((Integer)                              obj[13]);
                    doc.setMultiplier((BigDecimal)                      obj[14]);
                    doc.setIs_active((Boolean)                          obj[15]);
                    doc.setIs_deleted((Boolean)                         obj[16]);
                    doc.setName_api_atol((String)                       obj[17]);
                    doc.setOutput_order((Integer)                       obj[18]);
                }
                return doc;
            } catch (Exception e) {
                logger.error("Exception in method getTaxesValues on selecting from sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updateTaxes(SpravTaxesForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(50L,"642") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_taxes",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(50L,"643") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_taxes",request.getId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            Long myId=userRepository.getUserId();
            String stringQuery;
            stringQuery =   " update " +
                    " sprav_taxes " +
                    " set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " description = :description, " +
                    " name = :name, " +
                    " value = " + request.getValue() + ", "+
                    " multiplier = " + request.getMultiplier() + ", "+
                    " is_active = " + request.isIs_active() + ", "+
                    " name_api_atol = :name_api_atol " +
                    " where " +
                    " master_id = " + myMasterId +
                    " and id= "+request.getId();
            try
            {
                try
                {//сохранение порядка вывода налогов
                    if (request.getTaxesIdsInOrderOfList().size() > 1) {
                        int c = 0;
                        for (Long field : request.getTaxesIdsInOrderOfList()) {
                            c++;
                            if (!saveChangesStatusesOrder(field, c, myMasterId)) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception in method insertTaxes on trying to save taxes output order:", e);
                    e.printStackTrace();
                    return null;
                }
                //сохранение полей документа
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name", (request.getName() == null ? "" : request.getName()));
                query.setParameter("description", (request.getDescription() == null ? "" : request.getDescription()));
                query.setParameter("name_api_atol", (request.getName_api_atol() == null ? "" : request.getName_api_atol()));
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method insertTaxes on updating sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertTaxes(SpravTaxesForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(50L, "636")) ||
                //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                (securityRepositoryJPA.userHasPermissions_OR(50L, "636") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            Long myId = userRepository.getUserId();
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            String stringQuery =
                "insert into sprav_taxes (" +
                " master_id," +             // мастер-аккаунт
                " creator_id," +            // создатель
                " company_id," +            // предприятие, для которого создается документ
                " date_time_created," +     // дата и время создания
                " name," +                  // наименование налога
                " description," +           // доп. информация по налогу
                " value," +                 // значение налога в процентах
                " multiplier," +            // множитель налога
                " is_active," +             // налог активен в данный момент
                " name_api_atol," +         // наименование налога в API Атол (актуально только для России)
                " is_deleted," +            // налог удалён
                " output_order" +           // порядок вывода
                ") values ("+
                myMasterId + ", "+//мастер-аккаунт
                myId + ", "+ //создатель
                request.getCompany_id() + ", "+//предприятие, для которого создается документ
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                ":name,"+
                ":description,"+
                request.getValue() + ", " +
                request.getMultiplier() + ", " +
                request.isIs_active() + ", " +
                ":name_api_atol" + ", " +
                false + ", " +
                getNextOutputOrder(request.getCompany_id()) +
                ")";// уникальный идентификатор документа

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.setParameter("description",request.getDescription());
                query.setParameter("name_api_atol",request.getName_api_atol());
                query.executeUpdate();
                stringQuery="select id from sprav_taxes where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                return Long.valueOf(query2.getSingleResult().toString());
            } catch (Exception e) {
                logger.error("Exception in method insertTaxes on inserting into sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteTaxes(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(50L, "638") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_taxes", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(50L, "639") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_taxes", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery = "update sprav_taxes p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteTaxes on updating sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteTaxes(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(50L, "638") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_taxes", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(50L, "639") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_taxes", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery =
                    "update sprav_taxes p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteTaxes on updating sprav_taxes. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
//*****************************************************************************************************************************************************
//*******************************************************************  U T I L S **********************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    private Integer getNextOutputOrder(Long companyId) {
        String stringQuery = "select coalesce(max(output_order)+1,1) from sprav_taxes where company_id =  " + companyId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            int output_order;
            output_order= (int) query.getSingleResult();
            return output_order;
        } catch (Exception e) {
            logger.error("Exception in method getNextOutputOrder. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


    @SuppressWarnings("Duplicates")
    private boolean saveChangesStatusesOrder(Long statusId, int order, Long masterId) {
        String stringQuery;

            stringQuery =   " update sprav_taxes set " +
                            " output_order = " + order +
                            " where id = " + statusId + " and master_id = " + masterId ;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method saveChangesStatusesOrder. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @Transactional//отдает список налогов по id предприятия
    @SuppressWarnings("Duplicates")
    public List<SpravTaxesListJSON> getTaxesList(Long companyId) {

        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select" +
                "           p.id as id, " +
                "           p.name as name, " +
                "           p.description as description,  " +
                "           p.value as value, " +
                "           p.multiplier as multiplier, " +
                "           p.name_api_atol as name_api_atol " +
                "           from sprav_taxes p " +
                "           where  p.master_id=" + myMasterId +
                "           and p.company_id=" + companyId +
                "           and coalesce(p.is_deleted,false)=false" +
                "           and coalesce(p.is_active,false)=true" +
                "           order by p.output_order asc";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            List<SpravTaxesListJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                SpravTaxesListJSON doc = new SpravTaxesListJSON();

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setName((String) obj[1]);
                doc.setDescription((String) obj[2]);
                doc.setValue((int) obj[3]);
                doc.setMultiplier((BigDecimal) obj[4]);
                doc.setName_api_atol((String) obj[5]);
                doc.setCalculated(false);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getTaxesList. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
}