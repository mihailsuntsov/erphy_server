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

import com.dokio.message.request.FileCategoriesForm;
import com.dokio.message.request.FilesForm;
import com.dokio.message.response.FileCategoriesTableJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.additional.BaseFiles;
import com.dokio.message.response.additional.FileJSON;
import com.dokio.message.response.additional.FilesJSON;
import com.dokio.message.response.additional.FilesTableJSON;
import com.dokio.model.Companies;
import com.dokio.model.FileCategories;
import com.dokio.model.Files;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class FileRepositoryJPA {

    @Value("${start_files_path}")
    private String start_files_path;

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
    @Autowired
    private CommonUtilites commonUtilites;


    Logger logger = Logger.getLogger("FileRepositoryJPA");

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
            String dateFormat=userRepositoryJPA.getMyDateFormat();

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
                    "           to_char(p.date_time_created, '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, '"+dateFormat+" HH24:MI') as date_time_changed, " +
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
                        "upper(p.original_name) like upper(CONCAT('%',:sg,'%')) or "+
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }


            stringQuery = stringQuery + " and coalesce(p.trash,false) " + (trash?"=true":"=false");//отображение только файлов из корзины, если в запросе trash = true



            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, FilesTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
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
                        "upper(p.original_name) like upper(CONCAT('%',:sg,'%')) or "+
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " and coalesce(p.trash,false) " + (trash?"=true":"=false");//отображение только файлов из корзины, если в запросе trash = true

            Query query = entityManager.createNativeQuery(stringQuery);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
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
    public Integer updateFiles (FilesForm request) {

        EntityManager emgr = emf.createEntityManager();
        Files file = emgr.find(Files.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "153"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "152"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany=(userRepositoryJPA.getMyCompanyId().equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId=file.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDoc =(DocumentMasterId.equals(myMasterId));

        if(((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                ||(!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
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
                return 1;
            }catch (Exception e){
                logger.error("Exception in method updateFiles.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Long storeFileToDB(FileJSON fileObj, boolean dontCheckPermissions)
    {
//        dontCheckPermissions need for store files when registering new account - at this time user isn't registered and userRepository.* will returns nulls
        if(dontCheckPermissions || securityRepositoryJPA.userHasPermissions_OR(13L,"146,147"))//  Файлы : "Создание"
        {
            EntityManager emgr = emf.createEntityManager();
            Long myCompanyId = dontCheckPermissions?fileObj.getCompanyId():userRepositoryJPA.getMyCompanyId_();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, fileObj.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = dontCheckPermissions?fileObj.getMyMasterId():userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            //(если нужно проверять права, и (на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца)
            if (
                    !dontCheckPermissions &&
                            ((!securityRepositoryJPA.userHasPermissions_OR(13L, "146") &&
                    !myCompanyId.equals(fileObj.getCompanyId())) || !DocumentMasterId.equals(myMasterId)))
            {
                return -1L;
            }
            else
            {

                try {
                    Files newDocument = new Files();

                    User creator = emgr.find(User.class, fileObj.getMyId());
                    User master = emgr.find(User.class, fileObj.getMyMasterId());
                    Companies company = emgr.find(Companies.class, fileObj.getCompanyId());
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    newDocument.setCreator(creator);//создателя
                    newDocument.setMaster(master);//владельца
                    newDocument.setCompany(company);//предприятие
                    newDocument.setDate_time_created(timestamp);//
                    newDocument.setName(fileObj.getNewFileName());//
                    newDocument.setOriginal_name(fileObj.getOriginalFilename());//
                    newDocument.setPath(fileObj.getUPLOADED_FOLDER().toString());//
                    newDocument.setExtention(fileObj.getFileExtention());
                    newDocument.setFile_size(fileObj.getFileSize());
                    newDocument.setMime_type(fileObj.getMimeType());
                    newDocument.setDescription(fileObj.getDescription());
                    newDocument.setAnonyme_access(fileObj.getAnonyme_access());

                    if (fileObj.getCategoryId() > 0L) {
                        Set<FileCategories> fcSet = new HashSet<>();
                        FileCategories fk = emgr.find(FileCategories.class, fileObj.getCategoryId());
                        fcSet.add(fk);
                        newDocument.setFileCategories(fcSet);
                    }

                    entityManager.persist(newDocument);
                    entityManager.flush();
                    return newDocument.getId();
                }catch (Exception e){
                    logger.error("Exception in method storeFileToDB.", e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteFiles(String delNumbers) {
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
                        " and id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return 1;
            }catch (Exception e){
                logger.error("Exception in method deleteFiles.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer recoverFilesFromTrash(String delNumbers) {
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
                        " and id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return 1;
            }catch (Exception e){
                logger.error("Exception in method recoverFilesFromTrash.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteFilesFromTrash(String delNumbers) {
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
                        " and trash=true and id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
                Query query = entityManager.createNativeQuery(stringQuery);
                if (query.executeUpdate()>0)
                {
                    storageService.deleteFiles(filesToBeDeleted);//удаление файлов с диска
                    return 1;
                } else return null;
            }catch (Exception e){
                logger.error("Exception in method deleteFilesFromTrash.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer clearTrash(Long CompanyId) {
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
                        return 1;
                    }
                    return 1;
                } else return null;
            }catch (Exception e){
                logger.error("Exception in method clearTrash.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")//отдача данных (original_name, path) о файле, если есть права или если он открыт на общий доступ
    public FileInfoJSON getFileAuth(String filename) {

        List<Integer> myPermissions = securityRepositoryJPA.giveMeMyPermissions(13L);
        if(myPermissions.contains(150) || (myPermissions.contains(151)))
//        if (securityRepositoryJPA.userHasPermissions_OR(13L, "150,151"))//Просмотр документов
        {
//            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            stringQuery = "select " +
                    "           p.original_name as original_name, " +
                    "           p.path as path " +
                    "           from files p " +
//                    "           where  p.master_id = :myMasterId and p.name = :filename";
            "           where p.name = :filename";
            if (!myPermissions.contains(150)) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (151)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("filename",filename);
//            query.setParameter("myMasterId",myMasterId);
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
                "           where p.name= :filename and p.anonyme_access = true ";

        Query query = entityManager.createNativeQuery(stringQuery );
        query.setParameter("filename",filename);
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
                " select f.path||'//'||f.name as path from files f where f.id in (" + ids.replaceAll("[^0-9\\,]", "") + ") " +
                        " UNION " +
                        " select d.path||'//thumbs//'||d.name as path from files d where d.id in (" + ids.replaceAll("[^0-9\\,]", "") + ")";
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getResultList();
    }

    @SuppressWarnings("Duplicates")// права не нужны, т.к. не вызывается по API
    private List<String> getPathsInTrash(Long CompanyId){
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

    //права не нужны т.к. не вызывается по API, только из контроллера
    @Transactional//транзакция тут нужна для заполнения hibernate'ом детей категорий в Lazy режиме
    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
    public List<FileCategories> getFileCategoriesTrees(List<Integer> rootIds) {
        List<FileCategories> returnTreesList = new ArrayList<>();
        String stringQuery;
        stringQuery = "from FileCategories p ";
        stringQuery = stringQuery + " left join fetch p.children";
        entityManager.createQuery(stringQuery, FileCategories.class).getResultList();
        for(int rootId : rootIds) {
            returnTreesList.add(entityManager.find(FileCategories.class, (long) rootId));
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
                    " where company_id ="+companyId+" and master_id="+ myMasterId+ " and upper(name) like upper(CONCAT('%',:sg,'%'))";
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на просмотр по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, FileCategoriesTableJSON.class);
            query.setParameter("sg", searchString);
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
                    !Long.valueOf(myCompanyId).equals(request.getCompanyId())) || !DocumentMasterId.equals(myMasterId))
            {
                return null;
            }
            else {
                Long myId = userRepository.getUserId();
                return insertFileCategoryCore(request, myMasterId, myId, 1000);
            }
        } else return -1L;
    }

    private Long insertFileCategoryCore(FileCategoriesForm request, Long masterId, Long creatorId, Integer outputOrder){
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        String stringQuery = "insert into file_categories (" +
                "name," +
                "master_id," +
                "creator_id," +
                "parent_id," +
                "company_id," +
                "date_time_created," +
                "output_order" +
                ") values ( " +
                ":name, " +
                masterId + "," +
                creatorId + "," +
                (request.getParentCategoryId() > 0 ? request.getParentCategoryId() : null) + ", " +
                request.getCompanyId() + ", " +
                "(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS'))," +
                outputOrder+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name",request.getName());
            if (query.executeUpdate() == 1) {
                stringQuery = "select id from file_categories where " +
                        "date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and " +
                        "creator_id=" + creatorId + " and " +
                        "output_order = " + outputOrder;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                String res = query2.getSingleResult().toString();
                return Long.valueOf(res);
            } else return 0L;
        } catch (Exception e) {
            logger.error("Exception in method insertFileCategoryCore. SQL = "+stringQuery, e);
            e.printStackTrace();
            return 0L;
        }
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
                    " name=:name, "+
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
                query.setParameter("name",request.getName());
                int i = query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                logger.error("Exception in method updateFileCategory.", e);
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
                logger.error("Exception in method deleteFileCategory.", e);
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
                logger.error("Exception in method saveChangeCategoriesOrder.", e);
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    // inserting base set of categories of new user
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertFileCategoriesFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'f_ctg_images'","'f_ctg_goods'","'f_ctg_docs'","'f_ctg_templates'"});
        String t = new Timestamp(System.currentTimeMillis()).toString();
        stringQuery = "insert into file_categories ( master_id,creator_id,company_id,date_time_created,parent_id,output_order,name) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,1,'"+map.get("f_ctg_goods")+"'),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,2,'"+map.get("f_ctg_docs")+"'),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,3,'"+map.get("f_ctg_images")+"');";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            // "Templates" category must be created separately, because we need its id
            FileCategoriesForm form = new FileCategoriesForm();
            form.setCompanyId(cId);
            form.setParentCategoryId(0L);
            form.setName(map.get("f_ctg_templates"));
            return insertFileCategoryCore(form, mId, uId, 4);
        } catch (Exception e) {
            logger.error("Exception in method insertFileCategsAndBaseFilesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<BaseFiles> insertBaseFilesFast(Long mId, Long uId, Long cId, Long catgId) {
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'invoiceout'","'f_with_stamp_sign'","'signature'","'logo'","'stamp'"});
        String suffix = userRepositoryJPA.getUserSuffix(mId);
        List<BaseFiles> filePaths = new ArrayList<>();
//      List of :               [String filePath, String menuName, int docId, Long fileId (null)]
//      Returned list contains: [String filePath, String menuName, int docId, Long fileId]
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+".xls",map.get("invoiceout")+".xls",map.get("invoiceout"),31,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign"),31,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("logo")+".jpg",map.get("logo")+".jpg","", null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("signature")+"1.png",map.get("signature")+"1.png","", null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("signature")+"2.png",map.get("signature")+"2.png","", null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("stamp")+".png","",map.get("stamp")+".png", null,null));
        return storageService.copyFilesFromPathToCompany(filePaths,cId,catgId, mId, uId);
    }

    @SuppressWarnings("Duplicates")
    public List<BaseFiles> assemblyBaseFilesList(Long mId) {
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'invoiceout'","'f_with_stamp_sign'"});
        String suffix = userRepositoryJPA.getUserSuffix(mId);
        List<BaseFiles> filePaths = new ArrayList<>();
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+".xls",map.get("invoiceout")+".xls",map.get("invoiceout"),31,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign"),31,null));
        return filePaths;
    }

//      Input:  List of         [String filePath, String fileName, String menuName, int docId, Long fileId (null)]
//      Return: List contains:  [String filePath, String fileName, String menuName, int docId, Long fileId       ]
    // It gets list of BaseFiles with file names, and returns list of BaseFiles with file id's accorded to their names
    public List<BaseFiles> getFilesIdsByName(List<BaseFiles>baseFilesList, Long mId, Long cId, String extention){
        List<BaseFiles> retList = new ArrayList<>();
        String stringQuery = "select  id, original_name from files where master_id = " + mId + " and company_id = " + cId + " and coalesce(trash,false) = false " + (Objects.isNull(extention)?"":" and extention = '." + extention + "'") + " order by id";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            if (queryList.size() > 0)
                for (BaseFiles baseFile : baseFilesList) {
                    for (Object[] obj : queryList) {
                        if (baseFile.getFileName().equals(obj[1]) && (retList.size() == 0 || retList.stream().noneMatch(o -> o.getFileName().equals(obj[1]))))
                            retList.add(new BaseFiles(baseFile.getFilePath(), baseFile.getFileName(), baseFile.getMenuName(), baseFile.getDocId(), Long.parseLong(obj[0].toString())));
                    }
                }
            return retList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFilesIdsByName. SQL = " + stringQuery, e);
            return retList;
        }
    }



}
