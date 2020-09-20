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

import com.dokio.message.request.FileCategoriesForm;
import com.dokio.message.request.FilesForm;
import com.dokio.message.response.FileCategoriesTableJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.FilesJSON;
import com.dokio.message.response.FilesTableJSON;
import com.dokio.model.Companies;
import com.dokio.model.FileCategories;
import com.dokio.model.Files;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class FileRepositoryJPA {
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
    private UserDetailsServiceImpl userService;
    @Autowired
    private StorageService storageService;



//    @Transactional
    @SuppressWarnings("Duplicates")
    public List<FilesTableJSON> getFilesTable(
            int     result,
            int     offsetreal,
            String  searchString,
            String  sortColumn,
            String  sortAsc,
            int     companyId,
            int     categoryId,
            Boolean trash,
            Boolean anonyme)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))// Меню - таблица
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           p.original_name as original_name, " +
                    "           p.path as path, " +
                    "           p.extention as extention, " +
                    "           p.description as description, " +
                    "           p.file_size as file_size, " +
                    "           p.mime_type as mime_type, " +
                    "           p.anonyme_access as anonyme_access, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from files p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    (categoryId!=0?" and p.id in (select ccc.file_id from file_filecategories ccc where ccc.category_id="+categoryId+") ":" ") +
                    (anonyme?" and p.anonyme_access=true ":" ");

            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            {
                //остается только на своё предприятие (151)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.original_name) like upper('%" + searchString + "%') or "+
                        "upper(p.description) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }


            stringQuery = stringQuery + " and coalesce(p.trash,false) " + (trash?"=true":"=false");//отображение только файлов из корзины, если в запросе trash = true



            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, FilesTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            return query.getResultList();
        } else return null;
    }
    @SuppressWarnings("Duplicates")
//    @Transactional
    public int getFilesSize(String searchString, int companyId, int categoryId, Boolean trash,Boolean anonyme) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))//"Группы товаров" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id " +
                    "           from files p " +
                    "           where  p.master_id=" + myMasterId +
                    (categoryId!=0?" and p.id in (select ppg.file_id from file_filecategories ppg where ppg.category_id="+categoryId+") ":" ") +
                    (anonyme?" and p.anonyme_access=true ":" ");

            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            {
                //остается только на своё предприятие (151)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.original_name) like upper('%" + searchString + "%') or "+
                        "upper(p.description) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " and coalesce(p.trash,false) " + (trash?"=true":"=false");//отображение только файлов из корзины, если в запросе trash = true

            Query query = entityManager.createNativeQuery(stringQuery);

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public FilesJSON getFileValues(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))//Просмотр документов
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           p.original_name as original_name, " +
                    "           p.extention as extention, " +
                    "           p.description as description, " +
                    "           p.file_size as file_size, " +
                    "           p.mime_type as mime_type, " +
                    "           p.anonyme_access as anonyme_access, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed " +
                    "           from files p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where p.id= " + id+
                    "           and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (151)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery, FilesJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                FilesJSON response = (FilesJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }


    @SuppressWarnings("Duplicates")
    public boolean updateFiles (FilesForm request) {

        EntityManager emgr = emf.createEntityManager();
        Files file = emgr.find(Files.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "153"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "152"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany=(userRepositoryJPA.getMyCompanyId().equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId=file.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDock =(DocumentMasterId.equals(myMasterId));

        if(((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                ||(!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDock) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try
            {
                emgr.getTransaction().begin();

                file.setOriginal_name(request.getOriginal_name() == null ? "" : request.getOriginal_name());
                file.setDescription(request.getDescription() == null ? "" : request.getDescription());
                file.setAnonyme_access(request.getAnonyme_access());

                //категории
                Set<Long> categories = request.getSelectedFileCategories();
                if (!categories.isEmpty()) { //если есть выбранные чекбоксы категорий
                    Set<FileCategories> setCategoriesOfFile = getCategoriesSetBySetOfCategoriesId(categories);
                    file.setFileCategories(setCategoriesOfFile);
                } else { // если ни один чекбокс категорий не выбран
                    file.setFileCategories(null);
                }

                User changer = userService.getUserByUsername(userService.getUserName());
                file.setChanger(changer);//кто изменил

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                file.setDate_time_changed(timestamp);//дату изменения

                emgr.getTransaction().commit();
                emgr.close();
                return true;
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean storeFileToDB(
            Long master_id,
            int companyId,
            Long myId,
            String path,
            String fileName,
            String originalFileName,
            String fileExtention,
            Long fileSize,
            String mimeType,
            String description,
            Boolean anonyme_access,
            Integer categoryId)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L,"146,147"))//  Файлы : "Создание"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, Long.valueOf(companyId));//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(13L, "146") &&
                    myCompanyId != companyId) || DocumentMasterId != myMasterId )
            {
                return false;
            }
            else
            {

                try {
                    Files newDocument = new Files();

                    User creator = emgr.find(User.class, myId);
                    User master = emgr.find(User.class, master_id);
                    Companies company = emgr.find(Companies.class, Long.valueOf(companyId));
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    newDocument.setCreator(creator);//создателя
                    newDocument.setMaster(master);//владельца
                    newDocument.setCompany(company);//предприятие
                    newDocument.setDate_time_created(timestamp);//
                    newDocument.setName(fileName);//
                    newDocument.setOriginal_name(originalFileName);//
                    newDocument.setPath(path);//
                    newDocument.setExtention(fileExtention);
                    newDocument.setFile_size(fileSize);
                    newDocument.setMime_type(mimeType);
                    newDocument.setDescription(description);
                    newDocument.setAnonyme_access(anonyme_access);

                    if (categoryId > 0) {
                        Set<FileCategories> fcSet = new HashSet<>();
                        FileCategories fk = emgr.find(FileCategories.class, Long.valueOf(categoryId));
                        fcSet.add(fk);
                        newDocument.setFileCategories(fcSet);
                    }

                    entityManager.persist(newDocument);
                    entityManager.flush();
                    //            return newDocument.getId();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteFiles(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"148") && securityRepositoryJPA.isItAllMyMastersDocuments("files",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(13L,"149") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",delNumbers)))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                Long changer = userService.getUserId();
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                String stringQuery;
                stringQuery = "update files" +
                        " set trash=true, changer_id=" + changer + ", date_time_changed=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) " +
                        " where master_id=" + myMasterId +
                        " and id in (" + delNumbers + ")";
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e)
            {return false;}
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean recoverFilesFromTrash(String delNumbers) {
        //Если есть право на "Корзина - Восстановление файлов по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"177") && securityRepositoryJPA.isItAllMyMastersDocuments("files",delNumbers)) ||
                //Если есть право на "Корзина - Восстановление файлов своего предприятия" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(13L,"178") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",delNumbers)))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                Long changer = userService.getUserId();
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                String stringQuery;
                stringQuery = "update files" +
                        " set trash=false, changer_id=" + changer + ", date_time_changed=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) " +
                        " where master_id=" + myMasterId +
                        " and id in (" + delNumbers + ")";
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }catch (Exception e)
            {return false;}
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteFilesFromTrash(String delNumbers) {
        //Если есть право на "Корзина - Удаление из корзины файлов по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"179") && securityRepositoryJPA.isItAllMyMastersDocuments("files",delNumbers)) ||
                //Если есть право на "Корзина - Удаление из корзины файлов своего предприятия" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(13L,"180") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",delNumbers)))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                List<String> filesToBeDeleted=getPathsByIds(delNumbers);//массив путей к файлам на диске
                String stringQuery;
                stringQuery = "delete from files" +
                        " where master_id=" + myMasterId +
                        " and trash=true and id in (" + delNumbers + ")";
                Query query = entityManager.createNativeQuery(stringQuery);
                if (query.executeUpdate()>0)
                {
                    storageService.deleteFiles(filesToBeDeleted);//удаление файлов с диска
                    return true;
                } else return false;
            }catch (Exception e)
            {return false;}
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean clearTrash(Integer CompanyId) {
        //Если есть право на "Корзина - Очистка корзины по всем предприятиям" , ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"181")) ||
                //Если есть право на "Корзина - Очистка корзины своего предприятия"
                (securityRepositoryJPA.userHasPermissions_OR(13L,"182")))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                List<String> filesToBeDeleted=getPathsInTrash(CompanyId);//массив путей к файлам на диске
                String stringQuery;
                stringQuery = "delete from files" +
                        " where master_id=" + myMasterId +
                        " and company_id="+CompanyId+" and trash=true";
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "181")) //Если нет прав на "Корзина - Очистка корзины по всем предприятиям"
                {
                    //остается только на своё предприятие (182)
                    stringQuery = stringQuery + " and company_id=" + userRepositoryJPA.getMyCompanyId();
                }
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    if(query.executeUpdate()>0){
                        storageService.deleteFiles(filesToBeDeleted);//удаление файлов с диска
                        return true;
                    }
                    return true;
                } else return false;
            }catch (Exception e)
            {return false;}

        } else return false;
    }

    @SuppressWarnings("Duplicates")//отдача данных о файле если есть права или если он открыт на общий доступ
    public FileInfoJSON getFileAuth(String filename) {
        //сначала проверим, не открыт ли он для общего доступа:
        if (securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            stringQuery = "select " +
                    "           p.original_name as original_name, " +
                    "           p.path as path " +
                    "           from files p " +
                    "           where p.name= '" + filename + "' and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (151)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            if(queryList.size()>0) {//есть права на просмотр и скачивание файла
                FileInfoJSON doc = new FileInfoJSON();
                doc.setOriginal_name((String) queryList.get(0)[0]);
                doc.setPath((String) queryList.get(0)[1]);
                return doc;
            }
            else { // нет прав на файл, но может он открыт на общий доступ? Данный случай может наступить,
                   // если файл не входит в предприятие пользователя, а прав на все предприятия нет, либо файл вне зоны ответственности мастер-аккаунта
                return getFilePublic(filename);
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")//отдача данных о файле если он открыт на общий доступ
    public FileInfoJSON getFilePublic(String filename) {
        String stringQuery;
        stringQuery = "select " +
                "           p.original_name as original_name, " +
                "           p.path as path " +
                "           from files p " +
                "           where p.name= '" + filename + "' and p.anonyme_access = true ";

        Query query = entityManager.createNativeQuery(stringQuery );
        List<Object[]> queryList = query.getResultList();
        if(queryList.size()>0) {//файл открыт для общего доступа
            FileInfoJSON doc = new FileInfoJSON();
            doc.setOriginal_name((String) queryList.get(0)[0]);
            doc.setPath((String) queryList.get(0)[1]);
            return doc;
        }
        else return null;
    }

    @SuppressWarnings("Duplicates")// права не нужны, т.к. не вызывается по API
    private List<String> getPathsByIds(String ids){
        String stringQuery;
        stringQuery =
                " select f.path||'//'||f.name as path from files f where f.id in (" + ids + ") " +
                        " UNION " +
                        " select d.path||'//thumbs//'||d.name as path from files d where d.id in (" + ids + ")";
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getResultList();
    }

    @SuppressWarnings("Duplicates")// права не нужны, т.к. не вызывается по API
    private List<String> getPathsInTrash(Integer CompanyId){
        String stringQuery;
        stringQuery =
                " select f.path||'//'||f.name as path from files f where company_id="+CompanyId+" and trash=true" +
                        " UNION " +
                        " select d.path||'//thumbs//'||d.name as path from files d where company_id="+CompanyId+" and trash=true";
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getResultList();
    }



//*****************************************************************************************************************************************************
//***********************************************   C A T E G O R I E S   *****************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")//права не нужны т.к. private, не вызывается по API
    private Set<FileCategories> getCategoriesSetBySetOfCategoriesId(Set<Long> categories) {
        EntityManager em = emf.createEntityManager();
        FileCategories dep ;
        Set<FileCategories> categoriesSet = new HashSet<>();
        for (Long i : categories) {
            dep = em.find(FileCategories.class, i);
            categoriesSet.add(dep);
        }
        return categoriesSet;
    }

    //права не нужны т.к. не вызывается по API, только из контроллера
    public List<Integer> getFilesCategoriesIdsByFileId(Long id) {
        String stringQuery="select p.category_id from file_filecategories p where p.file_id= "+id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }

//    @Transactional//права не нужны т.к. не вызывается по API, только из контроллера
    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
    public List<FileCategories> getFileCategoriesTrees(List<Integer> rootIds) {
        List<FileCategories> returnTreesList = new ArrayList<>();
        String stringQuery;
        stringQuery = "from FileCategories p ";
        stringQuery = stringQuery + " left join fetch p.children";
        entityManager.createQuery(stringQuery, FileCategories.class).getResultList();
        for(int rootId : rootIds) {
            returnTreesList.add(entityManager.find(FileCategories.class, Long.valueOf(rootId)));
        }
        return returnTreesList;
    }

    //права на просмотр документов в таблице меню
    @SuppressWarnings("Duplicates") //поиск категорий
    public List<FileCategoriesTableJSON> searchFileCategory(Long companyId, String searchString) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))//"Файлы" (см. файл Permissions Id)
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from file_categories " +
                    " where company_id ="+companyId+" and master_id="+ myMasterId+ " and upper(name) like upper('%"+searchString+"%')";
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на просмотр по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, FileCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates") //возвращает id корневых категорий
    public List<Integer> getCategoriesRootIds(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))//
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select id from file_categories " +
                    "  where company_id ="+id+" and master_id="+ myMasterId+" and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    //отдает только список корневых категорий, без детей
    //нужно для изменения порядка вывода корневых категорий
    public List<FileCategoriesTableJSON> getRootFileCategories(Long companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "156,157"))//
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from file_categories " +
                    "  where company_id ="+companyId+" and master_id="+ myMasterId+" and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery, FileCategoriesTableJSON.class);
            return query.getResultList();
        }else return null;
    }

    @SuppressWarnings("Duplicates")
    //отдает только список детей, без их детей
    //нужно для изменения порядка вывода категорий
    public List<FileCategoriesTableJSON> getChildrensFileCategories(Long parentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "156,157"))//редактирование категорий
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from file_categories " +
                    " where parent_id ="+parentId+" and master_id="+ myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery, FileCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }


    @Transactional
    @SuppressWarnings("Duplicates")
    public Long insertFileCategory(FileCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L,"154,155"))//  "Создание категорий"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(13L, "154") &&
                    Long.valueOf(myCompanyId) != request.getCompanyId()) || DocumentMasterId != myMasterId )
            {
                return null;
            }
            else {
                String stringQuery;
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                Long myId = userRepository.getUserId();
                stringQuery = "insert into file_categories (" +
                        "name," +
                        "master_id," +
                        "creator_id," +
                        "parent_id," +
                        "company_id," +
                        "date_time_created" +
                        ") values ( " +
                        "'" + request.getName() + "', " +
                        myMasterId + "," +
                        myId + "," +
                        (request.getParentCategoryId() > 0 ? request.getParentCategoryId() : null) + ", " +
                        request.getCompanyId() + ", " +
                        "(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')))";
                try {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.executeUpdate() == 1) {
                        stringQuery = "select id from file_categories where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id=" + myId;
                        Query query2 = entityManager.createNativeQuery(stringQuery);
                        return Long.valueOf(Integer.parseInt(query2.getSingleResult().toString()));
                    } else return 0L;
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0L;
                }
            }
        } else return 0L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateFileCategory(FileCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L,"156,157"))//  "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long changer = userRepository.getUserIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery = "update file_categories set " +
                    " name='" + request.getName()+"', "+
                    " date_time_changed= now()," +
                    " changer_id= " + changer +
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав по всем предприятиям
            {
//            остается только на своё предприятие (157)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteFileCategory(FileCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "158,159"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "delete from file_categories "+
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "158")) //Если нет прав по всем предприятиям
            {
                //остается только на своё предприятие (159)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean saveChangeCategoriesOrder(List<FileCategoriesForm> request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L,"156,157"))//  "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            try
            {
                for (FileCategoriesForm field : request)
                {
                    stringQuery = "update file_categories set " +

                            " output_order=" + field.getOutput_order() +
                            " where id=" + field.getId() +
                            " and master_id=" + myMasterId;
                    if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав по всем предприятиям
                    {
                        //остается только на своё предприятие (157)
                        int myCompanyId = userRepositoryJPA.getMyCompanyId();
                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
                    }
                    Query query = entityManager.createNativeQuery(stringQuery);
                    int i = query.executeUpdate();
                }
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
}
