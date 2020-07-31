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
package com.dokio.service;
import com.dokio.repository.FileRepositoryJPA;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Repository
public class StorageServiceSite extends StorageService{
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private ProductsRepositoryJPA productsRepository;
    @Autowired
    private FileRepositoryJPA frj;

//***************************************************************************
//***************************** F I L E S ***********************************
//***************************************************************************

    private MultipartFile file;         //сам файл
    private Path UPLOADED_FOLDER;       //путь загрузки файла
    private Integer companyId;          //предприятие (передается из формы)
    private Long myMasterId;
    private Long myId;

    @SuppressWarnings("Duplicates")
    private boolean storePreparation(MultipartFile file, Integer companyId, String folderPath, Long siteId )//подготовка для записи файла
    {
        try
        {
            String BASE_FILES_FOLDER;
            this.myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            this.myId = userRepository.getUserId();
            if(isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде
            } else {                    BASE_FILES_FOLDER = "//usr//dokio//files//";} //запущено в linux
            String MY_MASTER_ID_FOLDER = this.myMasterId + "//";
            String MY_COMPANY_ID_FOLDER = companyId + "//";
            this.UPLOADED_FOLDER= Paths.get(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER + MY_COMPANY_ID_FOLDER + "sites//" + siteId+"//"+folderPath+"//");
            // в итоге получается путь для файла вида /usr/dokio/files/133/sites/56/assets/img/
            this.file=file;
            this.companyId=companyId;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    public boolean storeSiteFile(MultipartFile file, Integer companyId, String folderPath, Long siteId ) {
        try
        {
            if(storePreparation(file,companyId,folderPath,siteId)) {

                createDirectory(this.UPLOADED_FOLDER.toString());

                Files.copy(file.getInputStream(), this.UPLOADED_FOLDER.resolve(file.getOriginalFilename()));

                return true;

            } else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//    public SiteFilesList getFilesList(),

@SuppressWarnings("Duplicates")
    public Resource loadSiteFile(String filepath) {
    String BASE_FILES_FOLDER;
    if(isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде
    } else {                    BASE_FILES_FOLDER = "//usr//dokio//files//";} //запущено в linux
    String fullFilePath=BASE_FILES_FOLDER+filepath;
        try
        {
            Path file = Paths.get(fullFilePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fail to load from filepath '"+fullFilePath+"'");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("MalformedURLException! Fail to load from filepath '"+fullFilePath+"'");
        }
    }




}
