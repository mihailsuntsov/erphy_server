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
import com.dokio.message.request.additional.ChangeOwnerForm;
import com.dokio.message.response.FileCategoriesTableJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.ImageFileJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.additional.BaseFiles;
import com.dokio.message.response.additional.FileJSON;
import com.dokio.message.response.additional.FilesJSON;
import com.dokio.message.response.additional.FilesTableJSON;
import com.dokio.model.Companies;
import com.dokio.model.FileCategories;
import com.dokio.model.Files;
import com.dokio.model.User;
import com.dokio.security.CryptoService;
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
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private CryptoService cryptoService;


    Logger logger = Logger.getLogger("FileRepositoryJPA");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("company","cagent","creator","owner","date_time_created_sort","description","file_size","original_name","anonyme_access")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));
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
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151,727"))
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            stringQuery = "select  p.id as id, " +
//                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           uo.id as owner_id, " +
                    "           uo.name as owner, " +
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
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from files p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
//                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users uo ON p.owner_id=uo.id " + // LEFT OUTER because owner_id can be null
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    (categoryId!=0?" and p.id in (select ccc.file_id from file_filecategories ccc where ccc.category_id="+categoryId+") ":" ") +
                    (anonyme?" and p.anonyme_access=true ":" ");

            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Просмотр документов всех предприятий"
            {//остается на: своё предприятие ИЛИ свои документы (151 или 727)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "151")) //Если нет прав на "Просмотр документов своего предприятия"
                {//остается на просмотр своих документов (727)
                    stringQuery = stringQuery + " and (p.owner_id = " + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
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

            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Invalid query parameters");
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();

                List<FilesTableJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {

                    FilesTableJSON doc = new FilesTableJSON();

                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setName((String)                          obj[1]);
                    doc.setOwner_id(obj[2]!=null?Long.parseLong(  obj[2].toString()):null);
                    doc.setOwner((String)                         obj[3]);
                    doc.setOriginal_name((String)                 obj[4]);
                    doc.setPath((String)                          obj[5]);
                    doc.setExtention((String)                     obj[6]);
                    doc.setDescription((String)                   obj[7]);
                    doc.setFile_size((Integer)                    obj[8]);
                    doc.setMime_type((String)                     obj[9]);
                    doc.setAnonyme_access((Boolean)               obj[10]);
                    doc.setCreator((String)                       obj[11]);
                    doc.setChanger((String)                       obj[12]);
                    doc.setCompany((String)                       obj[13]);
                    doc.setDate_time_created((String)             obj[14]);
                    doc.setDate_time_changed((String)             obj[15]);

                    returnList.add(doc);
                }
                return returnList;
            }catch(Exception e){
                logger.error("Exception in method getFilesTable. SQL:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }
    @SuppressWarnings("Duplicates")
//    @Transactional
    public Integer getFilesSize(String searchString, int companyId, int categoryId, Boolean trash,Boolean anonyme) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151,727"))//
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getMyMasterId();

            stringQuery = "select  p.id as id " +
                    "           from files p " +
                    "           where  p.master_id=" + myMasterId +
                    (categoryId != 0 ? " and p.id in (select ppg.file_id from file_filecategories ppg where ppg.category_id=" + categoryId + ") " : " ") +
                    (anonyme ? " and p.anonyme_access=true " : " ");

            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Просмотр документов всех предприятий"
            {//остается на: своё предприятие ИЛИ свои документы (151 или 727)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "151")) //Если нет прав на "Просмотр документов своего предприятия"
                {//остается на просмотр своих документов (727)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.original_name) like upper(CONCAT('%',:sg,'%')) or " +
                        "upper(p.description) like upper(CONCAT('%',:sg,'%'))" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " and coalesce(p.trash,false) " + (trash ? "=true" : "=false");//отображение только файлов из корзины, если в запросе trash = true
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (searchString != null && !searchString.isEmpty()) {
                    query.setParameter("sg", searchString);
                }
                return query.getResultList().size();

            }catch(Exception e){
                logger.error("Exception in method getFilesSize. SQL:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public FilesJSON getFileValues(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151,727"))//Просмотр документов
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = " select  p.id as id, " +
//              "           u.name as master, " +
                "           p.name as name, " +
                "           uo.id   as owner_id, " +
                "           uo.name as owner, " +
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
                "           coalesce(p.alt,'') as alt, " +
                "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed " +
                "           from files p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users uo ON p.owner_id=uo.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where p.id= " + id+
                "           and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Просмотр документов всех предприятий"
            {//остается на: своё предприятие ИЛИ свои документы (151 или 727)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "151")) //Если нет прав на "Просмотр документов своего предприятия"
                {//остается на просмотр своих документов (727)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
            }
//            Query query = entityManager.createNativeQuery(stringQuery, FilesJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
//                FilesJSON response = (FilesJSON) query.getSingleResult();

                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                FilesJSON doc = new FilesJSON();
                if (queryList.size() > 0) {
                    doc.setId(Long.parseLong(queryList.get(0)[0].toString()));
                    doc.setName((String)                          queryList.get(0)[1]);
                    doc.setOwner_id(queryList.get(0)[2]!=null?Long.parseLong(queryList.get(0)[2].toString()):null);
                    doc.setOwner((String)                         queryList.get(0)[3]);
                    doc.setOriginal_name((String)                 queryList.get(0)[4]);
                    doc.setExtention((String)                     queryList.get(0)[5]);
                    doc.setDescription((String)                   queryList.get(0)[6]);
                    doc.setFile_size((Integer)                    queryList.get(0)[7]);
                    doc.setMime_type((String)                     queryList.get(0)[8]);
                    doc.setAnonyme_access((Boolean)               queryList.get(0)[9]);
                    doc.setCreator((String)                       queryList.get(0)[10]);
                    doc.setChanger((String)                       queryList.get(0)[11]);
                    doc.setCompany_id(Long.parseLong(             queryList.get(0)[15].toString()));
                    doc.setCompany((String)                       queryList.get(0)[16]);
                    doc.setAlt((String)                           queryList.get(0)[17]);
                    doc.setDate_time_created((String)             queryList.get(0)[18]);
                    doc.setDate_time_changed((String)             queryList.get(0)[19]);
                }
                return doc;
            }
            catch(NoResultException nre){return null;}
            catch(Exception e){
                logger.error("Exception in method getFileValues. SQL:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public Integer updateFiles (FilesForm request) {

        EntityManager emgr = emf.createEntityManager();
        Files file = emgr.find(Files.class, request.getId());//сохраняемый документ
//        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "152"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
//        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "153"); // "Редактирование док-тов своего предприятия"
//        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(13L, "728"); // "Редактирование своих документов"

//        boolean updatingDocumentOfMyCompany=(userRepositoryJPA.getMyCompanyId().equals(request.getCompany_id()));//сохраняется документ моего предприятия
//        Long DocumentMasterId=file.getMaster().getId(); //владелец сохраняемого документа.
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
//        boolean isItMyMastersDoc =(DocumentMasterId.equals(myMasterId));

        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого редактируют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(13L,"152") && securityRepositoryJPA.isItAllMyMastersDocuments("files",request.getId().toString())) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(13L,"153") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",request.getId().toString()))||
            //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого редактируют) и владелец документа - я
            (securityRepositoryJPA.userHasPermissions_OR(13L,"728") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("files", request.getId().toString())))
        {
            try
            {
                emgr.getTransaction().begin();

                file.setOriginal_name(request.getOriginal_name() == null ? "" : request.getOriginal_name());
                file.setDescription(request.getDescription() == null ? "" : request.getDescription());
                file.setAlt(request.getAlt() == null ? "" : request.getAlt());
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

                    newDocument.setCreator(creator);//creator
                    newDocument.setOwner(creator);//owner
                    newDocument.setMaster(master);// master account
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
    public Integer deleteFiles(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"148") && securityRepositoryJPA.isItAllMyMastersDocuments("files",delNumbers)) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(13L,"149") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",delNumbers)) ||
        //If there is permit "Deleting owned files to the recycle bin"
        (securityRepositoryJPA.userHasPermissions_OR(13L,"729") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("files", delNumbers)))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getMyMasterId();
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
        //Если есть право на "Корзина - Восстановление файлов по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"177") && securityRepositoryJPA.isItAllMyMastersDocuments("files",delNumbers)) ||
        //Если есть право на "Корзина - Восстановление файлов своего предприятия" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(13L,"178") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",delNumbers)) ||
        //If there is permit "Recycle Bin - Recover owned files from recycle bin"
        (securityRepositoryJPA.userHasPermissions_OR(13L,"730") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("files", delNumbers))
        )

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
                (securityRepositoryJPA.userHasPermissions_OR(13L,"180") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",delNumbers)) ||
                //If there is permit "Recycle Bin - Deletion of owned files from recycle bin"
                (securityRepositoryJPA.userHasPermissions_OR(13L,"731") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("files", delNumbers)))
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
                commonUtilites.idBelongsMyMaster("companies", CompanyId, myMasterId);
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

    public FileInfoJSON getFileAuth(Long fileId, Long masterId) {
        String fileName = (String)commonUtilites.getFieldValueFromTableById("files", "name",  masterId, fileId);
        return getFileAuth(fileName, masterId);
    }

    @SuppressWarnings("Duplicates")//отдача данных (original_name, path) о файле, если есть права
    public FileInfoJSON getFileAuth(String filename, Long masterId) {

        List<Integer> myPermissions = securityRepositoryJPA.giveMeMyPermissions(13L);
        if(myPermissions.contains(150) || myPermissions.contains(151) || myPermissions.contains(727))
        {
            String stringQuery;
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery = "select " +
                    "           p.original_name as original_name, " +
                    "           p.path as path " +
                    "           from files p " +
                    "           where  p.master_id = :masterId and p.name = :filename";
            if (!myPermissions.contains(150)) //Если нет прав на "Просмотр документов по всем предприятиям"
            {//остается на: своё предприятие ИЛИ свои документы (151 или 727)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "151")) //Если нет прав на просм по своему предприятию
                {//остается на просмотр своих документов (727)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("filename",filename);
            query.setParameter("masterId", masterId);
            List<Object[]> queryList = query.getResultList();
            FileInfoJSON doc = new FileInfoJSON();
            doc.setOriginal_name((String) queryList.get(0)[0]);
            doc.setPath((String) queryList.get(0)[1]);
            doc.setName(filename);
            return doc;
        } else return null;
    }

    @SuppressWarnings("Duplicates")//отдача данных о файле если он открыт на общий доступ
    public FileInfoJSON getFilePublic(String filename) {
        String stringQuery;
        stringQuery = "select " +
                "           p.original_name as original_name, " +
                "           p.path as path, " +
                "           p.master_id as masterid " +
                "           from files p " +
                "           where p.name= :filename and p.anonyme_access = true ";

        Query query = entityManager.createNativeQuery(stringQuery );
        query.setParameter("filename",filename);
        List<Object[]> queryList = query.getResultList();
        if(queryList.size()>0) {//файл открыт для общего доступа
            FileInfoJSON doc = new FileInfoJSON();
            doc.setOriginal_name((String) queryList.get(0)[0]);
            doc.setPath((String) queryList.get(0)[1]);
            doc.setMasterId(Long.valueOf(queryList.get(0)[2].toString()));
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
                        " select d.path||'/thumbs/'||d.name as path from files d where d.id in (" + ids.replaceAll("[^0-9\\,]", "") + ")";
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getResultList();
    }

    @SuppressWarnings("Duplicates")// права не нужны, т.к. не вызывается по API
    private List<String> getPathsInTrash(Long CompanyId){
        String stringQuery;
        stringQuery =
                " select f.path||'//'||f.name as path from files f where company_id="+CompanyId+" and trash=true" +
                        " UNION " +
                        " select d.path||'/thumbs/'||d.name as path from files d where company_id="+CompanyId+" and trash=true";
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
    public List<Long> getFilesCategoriesIdsByFileId(Long id) {
        String stringQuery="select p.category_id from file_filecategories p where p.file_id= "+id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<BigInteger> queryList = query.getResultList();
        List<Long> returnList = new ArrayList<>();
        for (BigInteger obj : queryList) {returnList.add(Long.parseLong(obj.toString()));}
        return returnList;
    }

    //права не нужны т.к. не вызывается по API, только из контроллера
//    @Transactional//транзакция тут нужна для заполнения hibernate'ом детей категорий в Lazy режиме
//    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
//    public List<FileCategories> getFileCategoriesTrees(List<Integer> rootIds) {
//        List<FileCategories> returnTreesList = new ArrayList<>();
//        String stringQuery;
//        stringQuery = "from FileCategories p ";
////        stringQuery = stringQuery + "  join  p.master";
////        stringQuery = stringQuery + " left join  p.owner";
////        stringQuery = stringQuery + "  join  p.company";
//        stringQuery = stringQuery + " join fetch p.children";
////        if (!securityRepositoryJPA.userHasPermissions_OR(13L, "733")) //Если нет прав на "Просмотр категорий всех предприятий"
////        {//остается на: своё предприятие ИЛИ свои категории (734 или 735)
////            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "734")) //Если нет прав на "Просмотр категорий своего предприятия"
////            {//остается на просмотр своих категорий (735)
////                stringQuery = stringQuery + " where (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
////            } else stringQuery = stringQuery + " where p.company_id=" + userRepositoryJPA.getMyCompanyId();
////        }
//        try {
//            entityManager.createQuery(stringQuery, FileCategories.class).getResultList();
//            for (int rootId : rootIds) {
//                returnTreesList.add(entityManager.find(FileCategories.class, (long) rootId));
//            }
//        } catch (Exception e) {
//            logger.error("Exception in method getFileCategoriesTrees. stringQuery = "+stringQuery, e);
//            e.printStackTrace();
//            return null;
//        }
//        return returnTreesList;
//    }
    @Transactional//транзакция тут нужна для заполнения hibernate'ом детей категорий в Lazy режиме
    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
    public List<FileCategories> getFileCategoriesTrees(List<Integer> rootIds) {
        List<FileCategories> returnTreesList = new ArrayList<>();
        String stringQuery;
        stringQuery = "from FileCategories p ";
        stringQuery = stringQuery + " join fetch p.master e";
        stringQuery = stringQuery + " join fetch p.company c";
        stringQuery = stringQuery + " left join fetch p.owner_user o";
        stringQuery = stringQuery + " left join fetch p.children";
        stringQuery = stringQuery + " where e.id=" + userRepositoryJPA.getMyId();
        if (!securityRepositoryJPA.userHasPermissions_OR(13L, "733")) //Если нет прав на "Просмотр категорий всех предприятий"
        {//остается на: своё предприятие ИЛИ свои категории (734 или 735)
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "734")) //Если нет прав на "Просмотр категорий своего предприятия"
            {//остается на просмотр своих категорий (735)
                stringQuery = stringQuery + " and (o.id=" + userRepositoryJPA.getMyId() + " or o.id is null) ";
            } else stringQuery = stringQuery + " and c.id=" + userRepositoryJPA.getMyCompanyId();
        }
        entityManager.createQuery(stringQuery, FileCategories.class).getResultList();
        for(int rootId : rootIds) {
            returnTreesList.add(entityManager.find(FileCategories.class, (long) rootId));
        }
        return returnTreesList;
    }
    public List<FileCategoriesTableJSON> searchFileCategory(Long companyId, String searchString) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "733,734,735"))//"Файлы" (см. файл Permissions Id)
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from file_categories " +
                    " where company_id ="+companyId+" and master_id="+ myMasterId+ " and upper(name) like upper(CONCAT('%',:sg,'%'))";
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "733")) //Если нет прав на "Просмотр документов всех предприятий"
            {//остается на: своё предприятие ИЛИ свои категории (734 или 735)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "734")) //Если нет прав на "Просмотр документов своего предприятия"
                {//остается на просмотр своих категорий (735)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
            }
            Query query = entityManager.createNativeQuery(stringQuery, FileCategoriesTableJSON.class);
            query.setParameter("sg", searchString);
            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates") //возвращает id корневых категорий
    public List<Integer> getCategoriesRootIds(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "733,734,735"))//
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select id from file_categories p" +
                    "  where p.company_id ="+id+" and p.master_id="+ myMasterId+" and p.parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "733")) //Если нет прав на "Просмотр категорий всех предприятий"
            {//остается на: своё предприятие ИЛИ свои категории (734 или 735)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "734")) //Если нет прав на "Просмотр категорий своего предприятия"
                {//остается на просмотр своих категорий (735)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
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
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав на "Редактирование категорий всех предприятий"
            {//остается на: своё предприятие ИЛИ свои категории (157 или 736)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "157")) //Если нет прав на "Редактирование категорий своего предприятия"
                {//остается на Редактирование своих категорий (736)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
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
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав на "Редактирование категорий всех предприятий"
            {//остается на: своё предприятие ИЛИ свои категории (157 или 736)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "157")) //Если нет прав на "Редактирование категорий своего предприятия"
                {//остается на Редактирование своих категорий (736)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
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
        if(securityRepositoryJPA.userHasPermissions_OR(13L,"154,155,732"))//  "Создание категорий"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean notMyCompany = !((Long.valueOf(myCompanyId)).equals(request.getCompanyId()));
            boolean canCreateAllCompanies = securityRepositoryJPA.userHasPermissions_OR(13L, "154");
            if (
                DocumentMasterId.equals(myMasterId) && // the company in which creating category belongs to master account
                (
                        // can create categories for all companies OR
                    canCreateAllCompanies ||
                        // can create categories only in own company and this is my company OR
                    (securityRepositoryJPA.userHasPermissions_OR(13L,"155") && !notMyCompany) ||
                        // can create own categories
                    securityRepositoryJPA.userHasPermissions_OR(13L,"732")
                )
            ){
                try {
                    commonUtilites.idBelongsMyMaster("companies", request.getCompanyId(), myMasterId);
                    commonUtilites.idBelongsMyMaster("file_categories", request.getParentCategoryId() > 0L ? request.getParentCategoryId() : null, myMasterId);
                    Long myId = userRepository.getUserId();
                    return insertFileCategoryCore(request, myMasterId, myId, 1000);
                } catch (Exception e) {
                    logger.error("Exception in method insertFileCategory. request = "+request.toString(), e);
                    e.printStackTrace();
                    return null;
                }
            } else return -1L;
        } else return -1L;
    }

    private Long insertFileCategoryCore(FileCategoriesForm request, Long masterId, Long creatorId, Integer outputOrder){
//        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        String stringQuery = "insert into file_categories (" +
                "name," +
                "master_id," +
                "creator_id," +
                "owner_id," +
                "parent_id," +
                "company_id," +
                "date_time_created," +
                "output_order" +
                ") values ( " +
                ":name, " +
                masterId + "," +
                creatorId + "," +
                request.getOwnerId() + ", " +
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
            } else return null;
        } catch (Exception e) {
            logger.error("Exception in method insertFileCategoryCore. SQL = "+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer updateFileCategory(FileCategoriesForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого редактируют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(13L,"156") && securityRepositoryJPA.isItAllMyMastersDocuments("file_categories",request.getCategoryId().toString())) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(13L,"157") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("file_categories",request.getCategoryId().toString()))||
            //Если есть право на "Редактирование своих категорий" и id принадлежат владельцу аккаунта (с которого редактируют) и владелец документа - я
            (securityRepositoryJPA.userHasPermissions_OR(13L,"736") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("file_categories", request.getCategoryId().toString())))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long changer = userRepository.getUserIdByUsername(userRepository.getUserName());

            String stringQuery="";

            stringQuery = "update file_categories set " +
                    " name=:name, "+
                    " date_time_changed= now()," +
                    " owner_id = " + request.getOwnerId() + ", " +
                    " changer_id= " + changer +
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            try{
                commonUtilites.idBelongsMyMaster("users", request.getOwnerId(), myMasterId);
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                query.executeUpdate();
                return 1;
            }
            catch (Exception e) {
                logger.error("Exception in method updateFileCategory.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteFileCategory(FileCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "158,159,737"))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "delete from file_categories "+
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "158")) //Если нет прав на "Редактирование категорий всех предприятий"
            {//остается на: своё предприятие ИЛИ свои категории (159 или 737)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "159")) //Если нет прав на "Редактирование категорий своего предприятия"
                {//остается на Удаление своих категорий (737)
                    stringQuery = stringQuery + " and (owner_id=" + userRepositoryJPA.getMyId() + " or owner_id is null) ";
                } else stringQuery = stringQuery + " and company_id=" + userRepositoryJPA.getMyCompanyId();
            }
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
                return 1;
            }
            catch (Exception e) {
                logger.error("Exception in method deleteFileCategory.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer saveChangeCategoriesOrder(List<FileCategoriesForm> request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(13L,"156,157,736"))//  "Редактирование категорий"
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
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "156")) //Если нет прав на "Редактирование категорий всех предприятий"
                {//остается на: своё предприятие ИЛИ свои категории (157 или 736)
                    if (!securityRepositoryJPA.userHasPermissions_OR(13L, "157")) //Если нет прав на "Редактирование категорий своего предприятия"
                    {//остается на Редактирование своих категорий (736)
                        stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                    } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
                }
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
            }
            return 1;
        }
            catch (Exception e) {
                logger.error("Exception in method saveChangeCategoriesOrder.", e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Integer setCategoriesToFiles(Set<Long> filesIds, Set<Long> categoriesIds, Boolean save) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();

           //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"152") && securityRepositoryJPA.isItAllMyMastersDocuments("files",commonUtilites.SetOfLongToString(filesIds,",","",""))) ||
           //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
           (securityRepositoryJPA.userHasPermissions_OR(13L,"153") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",commonUtilites.SetOfLongToString(filesIds,",","",""))) ||
           //If there is permit "Deleting owned files to the recycle bin"
           (securityRepositoryJPA.userHasPermissions_OR(13L,"728") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("files", commonUtilites.SetOfLongToString(filesIds,",","",""))))
        {
            try {
                String files = commonUtilites.SetOfLongToString(filesIds, ",", "", "");
                //поверка на то, что присланные id файлов действительно являются файлами мастер-аккаунта
                if (securityRepositoryJPA.isItAllMyMastersDocuments("files", files)) {
                    if (!save) {//если не нужно сохранять те категории у файла, которые уже есть
                        //удаляем все категории у всех запрашиваемых файлов
                        if (deleteAllFilesCategories(files)) {
                            //назначаем файлам категории
                            if(setCategoriesToFiles(filesIds,categoriesIds,myMasterId))
                                return 1;
                            else return null; // ошибка на прописывании категорий у файла
                        } else return null; // ошибка на стадии удаления категорий файлов в deleteAllFilesCategories
                    } else {//нужно сохранить предыдущие категории у файлов. Тут уже сложнее - нужно отдельно работать с каждым файлом
                        //цикл по файлам
                        for (Long p : filesIds) {
                            //получим уже имеющиеся категории у текущего файла
                            Set<Long> fileCategoriesIds = new HashSet<>();
                            fileCategoriesIds.addAll(getFilesCategoriesIdsByFileId(p));
                            //дополним их новыми категориями
                            fileCategoriesIds.addAll(categoriesIds);
                            //удалим старые категории
                            if (deleteAllFilesCategories(p.toString())) {
                                Set<Long> prod = new HashSet<>();
                                prod.add(p);
                                //назначаем текущему файлу категории
                                if(!setCategoriesToFiles(prod,fileCategoriesIds,myMasterId))
                                    return null; // ошибка на прописывании категорий у файла
                            } else return null; // ошибка на стадии удаления категорий текущего файла в deleteAllFilesCategories
                        }
                        return 1;
                    }
                } else return null; // не прошли по безопасности - подсунуты "левые" id файлов
            } catch (Exception e) {
                logger.error("Exception in method setCategoriesToFiles", e);
                e.printStackTrace();
                return null;
            }
        } else return -1; // не прошли по безопасности
    }

    private Boolean setCategoriesToFiles(Set<Long> filesIds, Set<Long> categoriesIds, Long myMasterId) throws Exception {
        if(categoriesIds.size()>0) {//если категории есть
            //прописываем их у всех запрашиваемых файлов
            StringBuilder stringQuery = new StringBuilder("insert into file_filecategories (file_id, category_id) values ");
            int i = 0;
            for (Long p : filesIds) { // files are checked to belonging to MasterId
                for (Long c : categoriesIds) {
                    commonUtilites.idBelongsMyMaster("file_categories", c, myMasterId);
                    stringQuery.append(i > 0 ? "," : "").append("(").append(p).append(",").append(c).append(")");
                    i++;
                }
            }
            try {
                entityManager.createNativeQuery(stringQuery.toString()).executeUpdate();
                return true;
            } catch (Exception e) {
                logger.error("Exception in method setCategoriesToFiles. SQL query:" + stringQuery, e);
                e.printStackTrace();
                throw new Exception();
            }
        } else return true;
    }

    private Boolean deleteAllFilesCategories(String files) throws Exception {
        String stringQuery = "delete from file_filecategories where file_id in("+files.replaceAll("[^0-9\\,]", "")+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteAllFilesCategories. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean setFilesExternalAccess(Set<Long> filesIds, Boolean access) {
        //if(securityRepositoryJPA.userHasPermissions_OR(13L,"152,153,728"))//  "Редактирование файлов "
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(13L,"152") && securityRepositoryJPA.isItAllMyMastersDocuments("files",commonUtilites.SetOfLongToString(filesIds,",","",""))) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(13L,"153") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("files",commonUtilites.SetOfLongToString(filesIds,",","",""))) ||
        //If there is permit "Deleting owned files to the recycle bin"
        (securityRepositoryJPA.userHasPermissions_OR(13L,"728") && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_("files", commonUtilites.SetOfLongToString(filesIds,",","",""))))
        {
            try {
                String files = commonUtilites.SetOfLongToString(filesIds, ",", "", "");
                //поверка на то, что присланные id файлов действительно являются файлами мастер-аккаунта
                if (securityRepositoryJPA.isItAllMyMastersDocuments("files", files)) {
                    String stringQuery =  "update files set anonyme_access = " + access + " where id in ("+files+")";
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                } else return null; // не прошли по безопасности - подсунуты "левые" id файлов
            } catch (Exception e) {
                logger.error("Exception in method setFilesExternalAccess", e);
                e.printStackTrace();
                return null;
            }
            return true;
        } else return null; // не прошли по безопасности
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
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'invoiceout'","'ordersup'","'f_with_stamp_sign'","'signature'","'logo'","'stamp'","'pricetag'","'f_med_contr_exmpl'"});
        String suffix = userRepositoryJPA.getUserSuffix(mId);
        List<BaseFiles> filePaths = new ArrayList<>();
//      List of :               [String filePath, String menuName, int docId, Long fileId (null)]
//      Returned list contains: [String filePath, String menuName, int docId, Long fileId]
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+".xls",map.get("invoiceout")+".xls",map.get("invoiceout"),31,null, "document",null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign"),31,null, "document",null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+".xls",map.get("invoiceout")+".xls",map.get("invoiceout"),59,null, "document",null));
//        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign"),59,null, "document",null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("ordersup")+".xls",map.get("ordersup")+".xls",map.get("ordersup"),39,null, "document",null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("logo")+".jpg",map.get("logo")+".jpg","", null,null, null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("signature")+"1.png",map.get("signature")+"1.png","", null,null, null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("signature")+"2.png",map.get("signature")+"2.png","", null,null, null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("stamp")+".png","",map.get("stamp")+".png", null,null, null,null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("pricetag")+" 40 x 40 mm.xls",map.get("pricetag")+" 40 x 40 mm.xls",map.get("pricetag")+" 40 x 40 mm", 14,null, "label",5));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("pricetag")+" 65 x 52 mm.xls",map.get("pricetag")+" 65 x 52 mm.xls",map.get("pricetag")+" 65 x 52 mm", 14,null, "label",3));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("f_med_contr_exmpl")+".docx",map.get("f_med_contr_exmpl")+".xls",map.get("f_med_contr_exmpl"),59,null, "document",null));

        return storageService.copyFilesFromPathToCompany(filePaths,cId,catgId, mId, uId);
    }

    @SuppressWarnings("Duplicates")
    public List<BaseFiles> assemblyBaseFilesList(Long mId) {
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'invoiceout'","'ordersup'","'f_with_stamp_sign'","'pricetag'","'f_med_contr_exmpl'"});
        String suffix = userRepositoryJPA.getUserSuffix(mId);
        List<BaseFiles> filePaths = new ArrayList<>();
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+".xls",map.get("invoiceout")+".xls",map.get("invoiceout"),31,null, "document", null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign")+".xls",map.get("invoiceout")+" "+map.get("f_with_stamp_sign"),31,null, "document", null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("ordersup")+".xls",map.get("ordersup")+".xls",map.get("ordersup"),39,null, "document", null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("pricetag")+" 40 x 40 mm.xls",map.get("pricetag")+" 40 x 40 mm.xls",map.get("pricetag")+" 40 x 40 mm", 14,null, "label", 5));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("pricetag")+" 65 x 52 mm.xls",map.get("pricetag")+" 65 x 52 mm.xls",map.get("pricetag")+" 65 x 52 mm", 14,null, "label", 3));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("invoiceout")+".xls",map.get("invoiceout")+".xls",map.get("invoiceout"),59,null, "document", null));
        filePaths.add(new BaseFiles(start_files_path+"//"+suffix+"//"+map.get("f_med_contr_exmpl")+".docx",map.get("f_med_contr_exmpl")+".docx",map.get("f_med_contr_exmpl"),59,null, "document", null));
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
                            retList.add(new BaseFiles(baseFile.getFilePath(), baseFile.getFileName(), baseFile.getMenuName(), baseFile.getDocId(), Long.parseLong(obj[0].toString()),baseFile.getType(),baseFile.getNum_labels_in_row()));
                    }
                }
            return retList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFilesIdsByName. SQL = " + stringQuery, e);
            return retList;
        }
    }

    public ImageFileJSON getImageFileInfo(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(13L, "150,151,727"))//Просмотр документов
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =
                    "           select " +
                    "           p.name as name, " +
                    "           p.original_name as original_name, " +
                    "           p.extention as extention, " +
                    "           p.description as description, " +
                    "           p.file_size as file_size, " +
                    "           p.mime_type as mime_type, " +
                    "           coalesce(p.anonyme_access,false) as anonyme_access, " +
                    "           p.path as path, " +
                    "           coalesce(p.alt,'') as alt " +
                    "           from files p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where p.id= " + id+
                    "           and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(13L, "150")) //Если нет прав на "Просмотр документов всех предприятий"
            {//остается на: своё предприятие ИЛИ свои документы (151 или 727)
                if (!securityRepositoryJPA.userHasPermissions_OR(13L, "151")) //Если нет прав на "Просмотр документов своего предприятия"
                {//остается на просмотр своих документов (727)
                    stringQuery = stringQuery + " and (p.owner_id=" + userRepositoryJPA.getMyId() + " or p.owner_id is null) ";
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            try{
                List<Object[]> queryList = query.getResultList();
                ImageFileJSON doc = new ImageFileJSON();
                if (queryList.size() > 0) {
                    doc.setId(id);
                    doc.setName((String) queryList.get(0)[0]);
                    doc.setOriginal_name((String) queryList.get(0)[1]);
                    doc.setExtention((String) queryList.get(0)[2]);
                    doc.setDescription((String) queryList.get(0)[3]);
                    doc.setFile_size((Integer) queryList.get(0)[4]);
                    doc.setMime_type((String) queryList.get(0)[5]);
                    doc.setAnonyme_access((Boolean) queryList.get(0)[6]);
                    doc.setPath((String) queryList.get(0)[7]);
                    doc.setAlt((String) queryList.get(0)[8]);
                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getImageFileInfo. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

}
