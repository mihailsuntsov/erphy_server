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

import com.dokio.message.request.ProductGroupFieldsForm;
import com.dokio.message.request.ProductGroupsForm;
import com.dokio.message.response.*;
import com.dokio.model.ProductGroupFields;
import com.dokio.model.ProductGroups;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ProductGroupRepositoryJPA {

    Logger logger = Logger.getLogger(ProductGroupRepositoryJPA.class);

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
    UserRepository userService;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("doc_number","name","cagent","status_name","sum_price","company","creator","date_time_created_sort","expenditure","description","is_completed","summ")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<ProductGroupsTableJSON> getProductGroupsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(10L, "113,114"))//"Группы товаров" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            String dateFormat=userRepositoryJPA.getMyDateFormat();

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, '"+dateFormat+" HH24:MI')as date_time_created, " +
                    "           to_char(p.date_time_changed, '"+dateFormat+" HH24:MI')as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.description as description " +
                    "           from product_groups p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(10L, "113")) //Если нет прав на просмотр по всем предприятиям"
            {
                //остается только на своё предприятие (114)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery, ProductGroupsTableJSON.class)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                return query.getResultList();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProductGroupsTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public int getProductGroupsSize(String searchString, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(10L, "113,114"))//"Группы товаров" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from product_groups p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(10L, "113")) //Если нет прав на просмотр по всем предприятиям"
            {
                //остается только на своё предприятие (114)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
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

    @Transactional// тут не надо прописывать права, т.к. это сервисный запрос
    @SuppressWarnings("Duplicates")
    public List<ProductGroupsListJSON> getProductGroupsList(String searchString, int companyId) {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from product_groups p " +
                    "           where  p.master_id=" + myMasterId;
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper(CONCAT('%',:sg,'%'))";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by p.name asc";
            Query query = entityManager.createNativeQuery(stringQuery, ProductGroupsListJSON.class);

            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}

            return query.getResultList();
    }

    @Transactional// тут не надо прописывать права, т.к. это сервисный запрос
    @SuppressWarnings("Duplicates")
    public List<ProductFieldValuesListJSON> getProductFieldsValuesList(String searchString, int fieldId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  " +
                "           field_value as name"+
                "           from product_fields " +
                "           where  field_id=" + fieldId+
                "           and product_id in (select id from products where master_id=" + myMasterId+")"
        ;

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and upper(field_value) like upper(CONCAT('%',:sg,'%'))";
        }
        stringQuery = stringQuery + " group by name order by name asc";
        Query query = entityManager.createNativeQuery(stringQuery, ProductFieldValuesListJSON.class);

        if (searchString != null && !searchString.isEmpty())
        {query.setParameter("sg", searchString);}

        List<ProductFieldValuesListJSON> result = query.getResultList();
        return result;
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public ProductGroupsJSON getProductGroupValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(10L, "113,114"))//"Группы товаров": см. _Permissions Id.txt
        {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select " +
                "           p.id as id, " +
                "           u.name as master, " +
                "           p.name as name, " +
                "           us.name as creator, " +
                "           uc.name as changer, " +
                "           p.master_id as master_id, " +
                "           p.creator_id as creator_id, " +
                "           p.changer_id as changer_id, " +
                "           p.company_id as company_id, " +
                "           cmp.name as company, " +
                "           p.date_time_created as date_time_created, " +
                "           p.date_time_changed as date_time_changed, " +
                "           p.description as description " +
                "           from product_groups p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and p.id= " + id;

        if (!securityRepositoryJPA.userHasPermissions_OR(10L, "113")) //Если нет прав на просм или редакт. по всем предприятиям
        {
            //остается только на своё предприятие
            stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        Query query = entityManager.createNativeQuery(stringQuery, ProductGroupsJSON.class);

        try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
            ProductGroupsJSON response = (ProductGroupsJSON) query.getSingleResult();
            return response;}
        catch(NoResultException nre){return null;}
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertProductGroups(ProductGroupsForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(10L,"111"))//  Группы товаров : "Создание"
        {
            ProductGroups newDocument = new ProductGroups();
            try
            {
                //создатель
                User creator = userRepository.getUserByUsername(userRepository.getUserName());
                newDocument.setCreator(creator);//создателя
                //владелец
                User master = userRepository.getUserByUsername(
                        userRepositoryJPA.getUsernameById(
                                userRepositoryJPA.getUserMasterIdByUsername(
                                        userRepository.getUserName() )));
                newDocument.setMaster(master);
                //дата и время создания
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                newDocument.setDate_time_created(timestamp);//
                //предприятие
                newDocument.setCompany(companyRepositoryJPA.getCompanyById(Long.valueOf(Integer.parseInt(request.getCompany_id()))));
                //Наименование
                newDocument.setName (request.getName() == null ? "": request.getName());
                //дополнительная информация
                newDocument.setDescription(request.getDescription());
                entityManager.persist(newDocument);
                entityManager.flush();
                return newDocument.getId();
            } catch (Exception e) {
                logger.error("Exception in method insertProductGroups.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1L;
    }

    @SuppressWarnings("Duplicates")
    public Integer updateProductGroups(ProductGroupsForm request) {
        boolean perm_AllCompaniesUpdate=securityRepositoryJPA.userHasPermissions_OR(10L, "115"); // "Группы товаров":"Редактирование документов по всем предприятиям" (в пределах родительского аккаунта)
        boolean perm_MyCompanyUpdate=securityRepositoryJPA.userHasPermissions_OR(10L, "116"); // "Группы товаров":"Редактирование документов своего предприятия"

        boolean itIsDocumentOfMyMasters=securityRepositoryJPA.isItMyMastersProductGroups(Long.valueOf(request.getId()));//документ под юрисдикцией главного аккаунта
        boolean itIsDocumentOfMyCompany=userRepositoryJPA.getMyCompanyId()==Integer.parseInt(request.getCompany_id());//сохраняется документ моего предприятия

        if
        (
                (perm_AllCompaniesUpdate ||                                     //если есть права изменять доки всех предприятий
                        (itIsDocumentOfMyCompany && perm_MyCompanyUpdate)            //или это мое предприятие и есть права изменять доки своего предприятия
                )
                        && itIsDocumentOfMyMasters                                      //+документ под юрисдикцией главного (родительского) аккаунта
        ){
            try{
                EntityManager emgr = emf.createEntityManager();
                emgr.getTransaction().begin();
                Long id=Long.valueOf(request.getId());
                ProductGroups updateDocument = emgr.find(ProductGroups.class, id);
                //id
                updateDocument.setId(id);
                //кто изменил
                User changer = userRepository.getUserByUsername(userRepository.getUserName());
                updateDocument.setChanger(changer);
                //дату изменения
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                updateDocument.setDate_time_changed(timestamp);
                //Наименование
                updateDocument.setName (request.getName() == null ? "": request.getName());
                //дополнительная информация
                updateDocument.setDescription (request.getDescription() == null ? "": request.getDescription());
                emgr.getTransaction().commit();
                emgr.close();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method updateProductGroups.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteProductGroupsById(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(10L, "112") && securityRepositoryJPA.isItAllMyMastersDocuments("product_groups", delNumbers)) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(10L, "112") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_groups", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery = "update product_groups p" +
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
                logger.error("Exception in method deleteProductGroupsById on updating product_groups. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteProductGroups(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(10L,"112") && securityRepositoryJPA.isItAllMyMastersDocuments("product_groups",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(10L,"112") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("product_groups",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update product_groups p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") +")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteProductGroups. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean copyProductGroups(ProductGroupsForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(10L,"111"))//  Группы товаров : "Создание"
        {
            try
            {   //СОЗДАЛИ НОВЫ ДОКУМЕНТ Группы товаров (НО ПОКА БЕЗ СЕТОВ И ПОЛЕЙ)
                ProductGroups newDocument = new ProductGroups();
                ProductGroupsJSON response = getProductGroupValuesById(request.getId());
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
                newDocument.setCompany(companyRepositoryJPA.getCompanyById(Long.valueOf(Integer.parseInt(response.getCompany_id()))));
                //Наименование
                newDocument.setName(response.getName() == null ? "" : response.getName()+" (copy)");
                //дополнительная информация
                newDocument.setDescription(response.getDescription());
                entityManager.persist(newDocument);
                entityManager.flush();
                Long newDocId = newDocument.getId();    // ID НОВОГО ДОКУМЕНТА
                Long myId = userRepository.getUserId();
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

                //НАХОДИМ СЕТЫ У КОПИРУЕМОГО ДОКУМЕНТА
                String stringQuery;
                stringQuery = "select  p.id as id, " +
                        "           p.name as name, " +
                        "           p.description as description, " +
                        "           p.company_id as company_id, " +
                        "           p.field_type as field_type, " +
                        "           p.parent_set_id as parent_set_id, " +
                        "           p.group_id as group_id, " +
                        "           p.output_order as output_order" +
                        "           from product_group_fields p" +
                        "           where  p.master_id=" + myMasterId +
                        "           and  p.parent_set_id is null" +
                        "           and  p.group_id=" + request.getId() ;
                Query queryCopy = entityManager.createNativeQuery(stringQuery, ProductGroupFieldsForm.class);
                List <ProductGroupFieldsForm> setsToCopy = queryCopy.getResultList();

                Long newSetId;

                try
                {
                    for (ProductGroupFieldsForm set : setsToCopy)
                    {
                        newSetId=insertProductGroupSet(set,newDocument); //создаем новый сет, где все будет как в старом кроме родительского документа
                        //НАХОДИМ ПОЛЯ У КОПИРУЕМОГО СЕТА
                        String fieldsQuery;
                        fieldsQuery = "select  p.id as id, " +
                                "           p.name as name, " +
                                "           p.description as description, " +
                                "           p.company_id as company_id, " +
                                "           p.field_type as field_type, " +
                                "           p.parent_set_id as parent_set_id, " +
                                "           p.group_id as group_id, " +
                                "           p.output_order as output_order" +
                                "           from product_group_fields p" +
                                "           where  p.parent_set_id=" + set.getId() ;
                        Query queryFields = entityManager.createNativeQuery(fieldsQuery, ProductGroupFieldsForm.class);
                        List <ProductGroupFieldsForm> fieldsToCopy = queryFields.getResultList();

                        for (ProductGroupFieldsForm field : fieldsToCopy) {
                            stringQuery = "insert into product_group_fields (" +
                                    "name," +
                                    "description," +
                                    "master_id," +
                                    "creator_id," +
                                    "parent_set_id," +
                                    "company_id," +
                                    "group_id," +
                                    "date_time_created," +
                                    "output_order," +
                                    "field_type" +
                                    ") values ( " +
                                    ":name, " +
                                    ":description, " +
                                    myMasterId + "," +
                                    myId + "," +
                                    newSetId + ", " +
                                    field.getCompany_id() + ", " +
                                    newDocId + ", " +
                                    "now(), " +
                                    field.getOutput_order() + ", " +
                                    field.getField_type() + ")";
                            try {
                                Query query = entityManager.createNativeQuery(stringQuery);
                                query.setParameter("name", field.getName());
                                query.setParameter("description", field.getDescription());
                                query.executeUpdate();
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }
                    }
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method copyProductGroups. SQL query:"+stringQuery, e);
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method copyProductGroups.", e);
                return false;
            }
        } else return false;
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertProductGroupSet(ProductGroupFieldsForm request, ProductGroups productGroup) {
        EntityManager emgr = emf.createEntityManager();
        emgr.getTransaction().begin();
        ProductGroupFields newDocument = new ProductGroupFields();
        //создатель
        User creator = userRepository.getUserByUsername(userRepository.getUserName());
        newDocument.setCreator(creator);//создателя
        //владелец
        User master = userRepository.getUserByUsername(
                userRepositoryJPA.getUsernameById(
                        userRepositoryJPA.getUserMasterIdByUsername(
                                userRepository.getUserName() )));
        newDocument.setMaster(master);
        //дата и время создания
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        newDocument.setDate_time_created(timestamp);//
        //предприятие
        newDocument.setCompany(companyRepositoryJPA.getCompanyById(request.getCompany_id()));
        //Наименование
        newDocument.setName (request.getName() == null ? "": request.getName());
        //дополнительная информация
        newDocument.setDescription(request.getDescription());
        //родительский документ (Группа товаров)
        newDocument.setProductGroup(productGroup);
        //тип Cет (т.е. не Поле)
        newDocument.setField_type(1);
        //порядок вывода
        newDocument.setOutput_order(Integer.valueOf(request.getOutput_order()));
        entityManager.persist(newDocument);
        entityManager.flush();
        return newDocument.getId();
    }








}
