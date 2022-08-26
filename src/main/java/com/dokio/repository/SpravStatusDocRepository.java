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

import com.dokio.message.request.Sprav.SpravStatusDocForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.SpravStatusDocJSON;
import com.dokio.message.response.Sprav.SpravStatusListJSON;
import com.dokio.model.Companies;
import com.dokio.model.Documents;
import com.dokio.model.Sprav.SpravStatusDocs;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class SpravStatusDocRepository {

    Logger logger = Logger.getLogger("SpravStatusDocRepository");

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
    @Autowired
    CommonUtilites commonUtilites;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","description","output_order","company","creator","date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    @Transactional
    @SuppressWarnings("Duplicates")


    public List<SpravStatusDocJSON> getStatusDocsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(22L, "275,276"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.dock_id as dock_id, " +
                    "           p.status_type as status_type, " +//тип статуса: 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                    "           p.output_order as output_order, " +
                    "           dc.name as dock, " +
                    "           p.color as color, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort,  " +
                    "           coalesce(p.is_default, false) as is_default " +
                    "           from sprav_status_dock p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN documents dc ON p.dock_id=dc.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted +
                    (documentId != 0 ? " and p.dock_id = " + documentId + " " : "");

            if (!securityRepositoryJPA.userHasPermissions_OR(22L, "275")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }

            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            List<Object[]> queryList = query.getResultList();
            List<SpravStatusDocJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                SpravStatusDocJSON doc = new SpravStatusDocJSON();

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setMaster((String) obj[1]);
                doc.setCreator((String) obj[2]);
                doc.setChanger((String) obj[3]);
                doc.setMaster_id(Long.parseLong(obj[4].toString()));
                doc.setCreator_id(Long.parseLong(obj[5].toString()));
                doc.setChanger_id(obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(obj[7].toString()));
                doc.setCompany((String) obj[8]);
                doc.setDate_time_created((String) obj[9]);
                doc.setDate_time_changed((String) obj[10]);
                doc.setName((String) obj[11]);
                doc.setDoc_id((Integer) obj[12]);
                doc.setStatus_type((Integer) obj[13]);
                doc.setOutput_order((Integer) obj[14]);
                doc.setDoc((String) obj[15]);
                doc.setColor((String) obj[16]);
                doc.setDescription((String) obj[17]);
                doc.setIs_default((Boolean) obj[20]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;

    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getStatusDocsSize(String searchString, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(22L, "275,276"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from sprav_status_dock p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted +
                    (documentId != 0 ? " and p.dock_id = " + documentId + " " : "");

            if (!securityRepositoryJPA.userHasPermissions_OR(22L, "275")) //Если нет прав на "Меню - таблица - "Статусы документов" по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public SpravStatusDocJSON getStatusDocsValues(int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(22L, "275,276"))//"Статусы документов" (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String suffix = userRepositoryJPA.getUserSuffix(userService.getUserId());
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.dock_id as doc_id, " +
                    "           p.status_type as status_type, " +//тип статуса: 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                    "           p.output_order as output_order, " +
                    "           dc.doc_name_"+suffix+" as dock, " +
                    "           p.color as color, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_default, false) as is_default " +
                    "           from sprav_status_dock p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN documents dc ON p.dock_id=dc.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(22L, "275")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (276)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SpravStatusDocJSON doc = new SpravStatusDocJSON();

            for (Object[] obj : queryList) {

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setMaster((String) obj[1]);
                doc.setCreator((String) obj[2]);
                doc.setChanger((String) obj[3]);
                doc.setMaster_id(Long.parseLong(obj[4].toString()));
                doc.setCreator_id(Long.parseLong(obj[5].toString()));
                doc.setChanger_id(obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(obj[7].toString()));
                doc.setCompany((String) obj[8]);
                doc.setDate_time_created((String) obj[9]);
                doc.setDate_time_changed((String) obj[10]);
                doc.setName((String) obj[11]);
                doc.setDoc_id((Integer) obj[12]);
                doc.setStatus_type((Integer) obj[13]);
                doc.setOutput_order((Integer) obj[14]);
                doc.setDoc((String) obj[15]);
                doc.setColor((String) obj[16]);
                doc.setDescription((String) obj[17]);
                doc.setIs_default((Boolean) obj[18]);
            }
            return doc;
        } else return null;

    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updateStatusDocs(SpravStatusDocForm request) {
        EntityManager emgr = emf.createEntityManager();
        SpravStatusDocs document = emgr.find(SpravStatusDocs.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate = securityRepositoryJPA.userHasPermissions_OR(22L, "278"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate = securityRepositoryJPA.userHasPermissions_OR(22L, "277"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany = (Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId = document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
        boolean isItMyMastersDoc = (DocumentMasterId.equals(myMasterId));

        if (((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                || (!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try
            {
                try
                {//сохранение порядка вывода статусов
                    if (request.getStatusesIdsInOrderOfList().size() > 1) {
                        int c = 0;
                        for (Long field : request.getStatusesIdsInOrderOfList()) {
                            c++;
                            if (!saveChangesStatusesOrder(field, c)) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method updateStatusDocs on saving orders. SQL query:", e);
                    return null;
                }//сохранение полей документа
                String stringQuery;
                stringQuery =   " update sprav_status_dock set " +
                        " color = '" + request.getColor() +"', " +
                        " changer_id = " + myId + ", "+
                        " date_time_changed= now()," +
                        " description = '" + (request.getDescription() == null ? "" : request.getDescription()) + "', " +
                        " name = '" + (request.getName() == null ? "" : request.getName()) + "', " +
                        " status_type = " + request.getStatus_type() +
                        " where " +
                        " id= "+request.getId();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method updateStatusDocs.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertStatusDocs(SpravStatusDocForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(22L, "271,272"))//  Статусы документов : "Создание"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие создаваемого документа
            Long DocumentMasterId = companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(22L, "271") &&
                    Long.valueOf(myCompanyId) != request.getCompany_id()) || !DocumentMasterId.equals(myMasterId)) {
                return null;
            } else {
                try {
                    SpravStatusDocs newDocument = new SpravStatusDocs();
                    //создатель
                    User creator = userRepository.getUserByUsername(userRepository.getUserName());
                    newDocument.setCreator(creator);//создателя
                    //владелец
                    User master = userRepository.getUserByUsername(
                            userRepositoryJPA.getUsernameById(
                                    userRepositoryJPA.getUserMasterIdByUsername(
                                            userRepository.getUserName())));
                    newDocument.setMaster(master);
                    //дата и время создания
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    newDocument.setDate_time_created(timestamp);//
                    //предприятие
                    newDocument.setCompany(companyRepositoryJPA.getCompanyById(request.getCompany_id()));
                    //Наименование
                    newDocument.setName(request.getName() == null ? "" : request.getName());
                    //дополнительная информация
                    newDocument.setDescription(request.getDescription() == null ? "" : request.getDescription());
                    // цвет статуса
                    newDocument.setColor(request.getColor() == null ? "" : request.getColor());
                    //тип статуса : 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                    newDocument.setStatus_type(request.getStatus_type());
                    //Документ, для которого создается статус
                    newDocument.setDocument(emgr.find(Documents.class, Long.valueOf(request.getDoc_id())));
                    //Порядковый номер вывода. Генерируется как максимальный для этого документа (например, Заказ) в этом предприятии, затем пользователь может пометять порядок сам
                    newDocument.setOutput_order(getNextOutputOrder(request.getDoc_id(), request.getCompany_id()));

                    entityManager.persist(newDocument);
                    entityManager.flush();
                    return newDocument.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method insertStatusDocs.", e);
                    return null;
                }
            }
        } else return -1L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteStatusDocs(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(22L, "273") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_status_dock", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(22L, "274") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_status_dock", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_status_dock p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteStatusDocs on updating sprav_status_dock. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteStatusDocs(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(22L, "273") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_status_dock", delNumbers)) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(22L, "274") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_status_dock", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_status_dock p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteStatusDocs on updating sprav_status_dock. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean setDefaultStatusDoc(UniversalForm request) {// id : предприятие, id2 : документ в реестре всех док-тов системы, id3 : статус
        EntityManager emgr = emf.createEntityManager();
        SpravStatusDocs document = emgr.find(SpravStatusDocs.class, request.getId3());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate = securityRepositoryJPA.userHasPermissions_OR(22L, "278"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate = securityRepositoryJPA.userHasPermissions_OR(22L, "277"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany = (Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getId()));//сохраняется документ моего предприятия
        Long DocumentMasterId = document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getMyMasterId();//владелец моего аккаунта
        boolean isItMyMastersDoc = (DocumentMasterId.equals(myMasterId));

        if (((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                || (!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try
            {
                String stringQuery;
                stringQuery =   " update sprav_status_dock set is_default=(" +
                        " case when (dock_id="+request.getId2()+" and id="+request.getId3()+") then true else false end) " +
                        " where " +
                        " company_id= "+request.getId()+
                        " and dock_id= "+request.getId2();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
    //*****************************************************************************************************************************************************
//*******************************************************************  U T I L S **********************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public int getNextOutputOrder(int docId, Long companyId) {
        String stringQuery = "select coalesce(max(output_order)+1,1) from sprav_status_dock where dock_id=" + docId + " and company_id =  " + companyId;
        Query query = entityManager.createNativeQuery(stringQuery);
        int output_order = 0;
        output_order= (int) query.getSingleResult();
        return output_order;
    }


    @SuppressWarnings("Duplicates")

    public boolean saveChangesStatusesOrder(Long statusId, int order) {
        String stringQuery;
        try {
            stringQuery = " update sprav_status_dock set " +
                    " output_order=" + order +
                    " where id=" + statusId;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional//отдает список статусов документа по его id (таблица documents) и id предприятия
    @SuppressWarnings("Duplicates")
    public List<SpravStatusListJSON> getStatusList(int companyId, int documentId) {

        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id, " +
                "           p.name as name, " +
                "           p.status_type as status_type, " +//тип статуса: 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                "           p.output_order as output_order, " +
                "           p.color as color, " +
                "           p.description as description,  " +
                "           coalesce(p.is_default,false) as is_default  " +
                "           from sprav_status_dock p " +
                "           where  p.master_id=" + myMasterId +
                "           and p.dock_id = " + documentId +
                "           and p.company_id=" + companyId +
                "           and coalesce(p.is_deleted,false)=false" +
                "           order by p.output_order asc";

        Query query = entityManager.createNativeQuery(stringQuery);

        List<Object[]> queryList = query.getResultList();
        List<SpravStatusListJSON> returnList = new ArrayList<>();
        for (Object[] obj : queryList) {
            SpravStatusListJSON doc = new SpravStatusListJSON();

            doc.setId(Long.parseLong(obj[0].toString()));
            doc.setName((String) obj[1]);
            doc.setStatus_type((Integer) obj[2]);
            doc.setOutput_order((Integer) obj[3]);
            doc.setColor((String) obj[4]);
            doc.setDescription((String) obj[5]);
            doc.setIs_default((Boolean) obj[6]);
            returnList.add(doc);
        }
        return returnList;
    }


    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertStatusesFast(Long masterId, Long mId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{
                "'st_new'","'st_cancel'","'st_send'","'st_ret_compl'","'st_assembly'","'st_wait_pay'","'st_wait_receive'","'st_paym_made'","'st_new_order'",
                "'st_assembl_ord'","'st_await_iss'","'st_issd_buyer'","'st_wait_prices'","'st_wait_invoice'","'st_ord_delvrd'","'st_in_process'","'st_completed'",
                "'st_payment_send'","'st_money_accptd'","'st_money_issued'","'st_invc_issued'","'st_invc_paid'","'st_printed'"});
        stringQuery = "insert into sprav_status_dock ( master_id,creator_id,company_id,date_time_created,name,dock_id,status_type,color,output_order,is_deleted,is_default) values "+
                // Возврат покупателя
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',28,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_send")+"',28,1,'#008ad2',2,false,false),"+          //Товары отправлены
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_ret_compl")+"',28,2,'#0cb149',3,false,false),"+     //Возврат произведён
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',28,3,'#000000',4,false,false),"+        //Отмена
                // Возврат поставщику
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',29,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_assembly")+"',29,1,'#6461a8',2,false,false),"+      //Сборка
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_send")+"',29,1,'#008ad2',3,false,false),"+          //Товары отправлены
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_wait_pay")+"',29,1,'#fbb80f',4,false,false),"+      //Ожидание оплаты
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_ret_compl")+"',29,2,'#0cb149',5,false,false),"+     //Возврат произведён
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',29,3,'#000000',6,false,false),"+        //Отмена
                // Входящий платёж
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',33,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_wait_pay")+"',33,1,'#fbb80f',2,false,false),"+      //Ожидание оплаты
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_wait_receive")+"',33,1,'#008ad2',3,false,false),"+  //Ожидание поступления
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_paym_made")+"',33,2,'#0cb149',4,false,false),"+     //Платёж произведён
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',33,3,'#000000',5,false,false),"+        //Отмена
                // Заказ покупателя
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new_order")+"',23,1,'#cccccc',1,false,true),"+      //Новый заказ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_wait_pay")+"',23,1,'#fbb80f',2,false,false),"+      //Ожидание оплаты
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_assembly")+"',23,1,'#6461a8',3,false,false),"+      //Сборка заказа
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_send")+"',23,1,'#008ad2',4,false,false),"+          //Товары отправлены
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_await_iss")+"',23,1,'#cf004d',5,false,false),"+     //Ждёт выдачи
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_issd_buyer")+"',23,2,'#0cb149',6,false,false),"+    //Выдан покупателю
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',23,3,'#000000',7,false,false),"+        //Отмена
                // Заказ поставщику
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new_order")+"',39,1,'#cccccc',1,false,true),"+      //Новый заказ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_wait_prices")+"',39,1,'#fbb80f',2,false,false),"+   //Ожидание цен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_wait_invoice")+"',39,1,'#6362a6',3,false,false),"+  //Ожидание счёта
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_paym_made")+"',39,1,'#008ad2',4,false,false),"+     //Платёж произведён
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_send")+"',39,1,'#008ad2',5,false,false),"+          //Товары отправлены
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_ord_delvrd")+"',39,2,'#0cb149',6,false,false),"+    //Заказ доставлен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',39,3,'#000000',7,false,false),"+        //Отмена
                // Инвентаризация
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',27,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_in_process")+"',27,1,'#008ad2',2,false,false),"+    //В процессе
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',27,2,'#0cb149',3,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',27,3,'#000000',4,false,false),"+        //Отмена
                // Исходящий платёж
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',34,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_payment_send")+"',34,2,'#0cb149',2,false,false),"+  //Отправлен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',34,3,'#000000',3,false,false),"+        //Отмена
                // Корректировка
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',41,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',41,2,'#0cb149',2,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',41,3,'#000000',3,false,false),"+        //Отмена
                // Оприходование
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',16,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_in_process")+"',16,1,'#008ad2',2,false,false),"+    //В процессе
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',16,2,'#0cb149',3,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',16,3,'#000000',4,false,false),"+        //Отмена
                // Отгрузка
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',21,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',21,2,'#0cb149',2,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',21,3,'#000000',3,false,false),"+        //Отмена
                // Перемещение
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',30,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_assembly")+"',30,1,'#6461a8',2,false,false),"+      //Сборка
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_send")+"',30,1,'#008ad2',3,false,false),"+          //Товары отправлены
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',30,2,'#0cb149',4,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',30,3,'#000000',5,false,false),"+        //Отмена
                // Приёмка
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',15,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_in_process")+"',15,1,'#008ad2',2,false,false),"+    //В процессе
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',15,2,'#0cb149',3,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',15,3,'#000000',4,false,false),"+        //Отмена
                // Приходный ордер
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',35,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_money_accptd")+"',35,2,'#0cb149',2,false,false),"+  //Деньги приняты
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',35,3,'#000000',3,false,false),"+        //Отмена
                // Расходный ордер
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',36,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_money_issued")+"',36,2,'#0cb149',2,false,false),"+  //Деньги выданы
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',36,3,'#000000',3,false,false),"+        //Отмена
                // Розничная продажа
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',25,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',25,2,'#0cb149',2,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',25,3,'#000000',3,false,false),"+        //Отмена
                // Списание
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',17,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_in_process")+"',17,1,'#008ad2',2,false,false),"+    //В процессе
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_completed")+"',17,2,'#0cb149',3,false,false),"+     //Завершено
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',17,3,'#000000',4,false,false),"+        //Отмена
                // Счёт покупателю
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',31,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_invc_issued")+"',31,1,'#008ad2',2,false,false),"+   //Счёт выставлен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_invc_paid")+"',31,2,'#0cb149',3,false,false),"+     //Счёт оплачен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',31,3,'#000000',4,false,false),"+        //Отмена
                // Счёт поставщика
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',32,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_invc_issued")+"',32,1,'#008ad2',2,false,false),"+   //Счёт выставлен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_invc_paid")+"',32,2,'#0cb149',3,false,false),"+     //Счёт оплачен
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',32,3,'#000000',4,false,false),"+        //Отмена
                // Счёт-фактура выданный
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',37,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_printed")+"',37,2,'#0cb149',2,false,false),"+       //Напечатан
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',37,3,'#000000',3,false,false),"+        //Отмена
                // Счёт-фактура полученный
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_new")+"',38,1,'#cccccc',1,false,true),"+            //Новый документ
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_printed")+"',38,2,'#0cb149',2,false,false),"+       //Напечатан
                "("+masterId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("st_cancel")+"',38,3,'#000000',3,false,false)";         //Отмена

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertStatusesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }





}