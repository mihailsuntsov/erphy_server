/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.repository;

import com.dokio.message.request.Sprav.SpravStatusDocForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Sprav.SpravStatusDocJSON;
import com.dokio.message.response.Sprav.SpravStatusListJSON;
import com.dokio.model.Companies;
import com.dokio.model.Documents;
import com.dokio.model.Sprav.SpravStatusDocs;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class SpravStatusDocRepository {

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


    public List<SpravStatusDocJSON> getStatusDocsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int documentId, Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(22L, "275,276"))//"Статусы документов" (см. файл Permissions Id)
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
    public SpravStatusDocJSON getStatusDocsValues(int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(22L, "275,276"))//"Статусы документов" (см. файл Permissions Id)
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
                    "           p.dock_id as doc_id, " +
                    "           p.status_type as status_type, " +//тип статуса: 1 - обычный; 2 - конечный положительный 3 - конечный отрицательный
                    "           p.output_order as output_order, " +
                    "           dc.name as dock, " +
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
    public boolean updateStatusDocs(SpravStatusDocForm request) {
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
                    return false;
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
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
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
                    Long.valueOf(myCompanyId) != request.getCompany_id()) || DocumentMasterId != myMasterId) {
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
                    return null;
                }
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteStatusDocs(String delNumbers) {
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
                    " and p.id in (" + delNumbers + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                int count = query.executeUpdate();
                return true;
            } else return false;
        } else return false;
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
    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean undeleteStatusDocs(String delNumbers) {
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
                    " and p.id in (" + delNumbers + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                int count = query.executeUpdate();
                return true;
            } else return false;
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

}