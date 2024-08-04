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

package com.dokio.service;

import com.dokio.message.response.additional.BaseFiles;
import com.dokio.message.response.additional.FileJSON;
import com.dokio.repository.FileRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

@Service
@Repository
public class StorageService {
    Logger logger = Logger.getLogger(StorageService.class);

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private FileRepositoryJPA frj;

    @Value("${files_path}")
    private String files_path;

//***************************************************************************
//***************************** F I L E S ***********************************
//***************************************************************************

    @SuppressWarnings("Duplicates")
    private FileJSON storePreparation(MultipartFile file, Long companyId, Boolean anonyme_access, Long categoryId, String description, Long masterId, Long myId)//подготовка для записи файла
    {
        try
        {
            FileJSON fileObj = new FileJSON();
            String BASE_FILES_FOLDER;
            Calendar calendar = Calendar.getInstance();
            String YEAR = calendar.get(Calendar.YEAR) + "//";
            fileObj.setMyMasterId(masterId);
            fileObj.setMyId(myId);
            if(isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде
            } else {                    BASE_FILES_FOLDER = files_path;} //запущено в linux
            String MY_MASTER_ID_FOLDER = fileObj.getMyMasterId() + "//";
            String MY_COMPANY_ID_FOLDER = companyId + "//";
            String THUMBS_FOLDER = "thumbs//";
            fileObj.setUPLOADED_FOLDER(Paths.get(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER + MY_COMPANY_ID_FOLDER + YEAR));
            fileObj.setUPLOADED_THUMBS_FOLDER(Paths.get(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER + MY_COMPANY_ID_FOLDER + YEAR + THUMBS_FOLDER));
            // в итоге получается путь для файла вида /usr/dokio/files/133/1/2019
            // год нужен чтобы не скапливалось много файлов в одной папке
            fileObj.setFile(file);
            fileObj.setOriginalFilename(file.getOriginalFilename());
            fileObj.setFileExtention(getFileExtension(fileObj.getOriginalFilename()));
            if (fileObj.getFileExtention().isEmpty() || fileObj.getFileExtention().trim().length() == 0) {
                fileObj.setFileExtention("._");
                } else if (fileObj.getFileExtention().length() >=16) {
                fileObj.setFileExtention(fileObj.getFileExtention().substring(0,15));}//т.к. в БД 16 байт под это дело
            fileObj.setGeneratedFileName(GetGeneratedFileName());
            fileObj.setFileSize(file.getSize());
            fileObj.setMimeType(file.getContentType());
            fileObj.setNewFileName(fileObj.getGeneratedFileName()+fileObj.getFileExtention());
            fileObj.setCompanyId(companyId);
            fileObj.setDescription(description);
            fileObj.setAnonyme_access(anonyme_access);
            fileObj.setCategoryId(categoryId);
            fileObj.setTHUMBNAIL_WIDTH(400);
            fileObj.setMAX_IMG_WIDTH(1200);

            return fileObj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long store(MultipartFile file, Long companyId, Boolean anonyme_access, Long categoryId, String description, Long masterId, Long myId, boolean dontCheckPermissions) {
        try
        {
            FileJSON fileObj = storePreparation(file,companyId,anonyme_access,categoryId,description, masterId, myId);
            if(!Objects.isNull(fileObj)){
                createDirectory(fileObj.getUPLOADED_FOLDER().toString());

                if(// если файл - картинка - надо сохранить его с опр. условиями (размер и thumbnail)
                    fileObj.getFileExtention().equalsIgnoreCase(".jpg")  ||
                    fileObj.getFileExtention().equalsIgnoreCase(".jpeg") ||
                    fileObj.getFileExtention().equalsIgnoreCase(".png"))
                {
                    createDirectory(fileObj.getUPLOADED_THUMBS_FOLDER().toString());
                    BufferedImage originalImage = ImageIO.read(file.getInputStream());
                    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                    String format = getImageFormatName(type);
                    if (originalImage.getWidth()>fileObj.getMAX_IMG_WIDTH() && anonyme_access) { originalImage = downscaleImageSize(originalImage, type, fileObj.getMAX_IMG_WIDTH());}
                    BufferedImage thumbImage = downscaleImageSize(originalImage, type, fileObj.getTHUMBNAIL_WIDTH());
                    byte[] thumbInByte = getImageInByte(thumbImage, format);
                    byte[] imageInByte = getImageInByte(originalImage, format);
                    Path filePath = Paths.get(fileObj.getUPLOADED_FOLDER() + "//" + fileObj.getNewFileName());
                    Path thumbPath = Paths.get(fileObj.getUPLOADED_THUMBS_FOLDER() + "//" + fileObj.getNewFileName());
                    Files.write(filePath, imageInByte);
                    Files.write(thumbPath, thumbInByte);
                } else
                    Files.copy(file.getInputStream(), fileObj.getUPLOADED_FOLDER().resolve(fileObj.getNewFileName()));

                // запись в БД информации о файле
                return frj.storeFileToDB( // запись в БД информации о файле (возвращает id файла)
                        fileObj, dontCheckPermissions
                );
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public Resource loadFile(String filepath) {
        try
        {
            Path file = Paths.get(filepath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fail to load from filepath '"+filepath+"'");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("MalformedURLException! Fail to load from filepath '"+filepath+"'");
        }
    }

    public boolean deleteFiles(List<String> filePaths){
        Path path;
        for (String filePath: filePaths)
        {
            path = Paths.get(filePath);
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


     static List<File> listf(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<>();

        // get all the files from a directory
        File[] fList = directory.listFiles();

        resultList.addAll(Arrays.asList(fList));

        for (File file : fList) {
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }


        return resultList;
    }

//***************************************************************************
//***************************** U T I L S ***********************************
//***************************************************************************



    String getFileExtension(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }



    public boolean isPathExists(String path) {
    Path dirPath = Paths.get(path);
    if (Files.exists(dirPath)) {
        return true;
    } else {
        return false;
    }
}

    boolean createDirectory(String path) {
        File files = new File(path);
        if(!files.exists())
        {
            if (files.mkdirs())
            {
                return true;
            }
            else
            {
                return false;
            }
        } return true;
    }

    private String GetGeneratedFileName(){
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        String taboo = "!@#$%^&*()_+!№;%: .?*/\\\"~";
        for (char c : taboo.toCharArray()) { timestamp = timestamp.replace(c, '-');}
        String uuid = UUID.randomUUID().toString().substring(0,12)+"-"+timestamp;//беру только первые 8 сгенерированных символов+timestamp
        return uuid;
    }

    private String getImageFormatName (int type){
        if      (type==5) return "jpg";
        else if (type==6) return "png";
        else              return "jpg"; // что-то же надо вернуть?))
    }

    // Calculating Directory Size
    public long getDirectorySize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        int count = files.length;
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            }
            else {
                length += getDirectorySize(files[i]);
            }
        }
        return length;
    }

    public List<BaseFiles> copyFilesFromPathToCompany(List<BaseFiles> baseFilesList, Long companyId, Long categoryId, Long masterId, Long userId){
        List<BaseFiles> retList = new ArrayList<>();
        try{
            for (BaseFiles baseFile : baseFilesList){
                if(isPathExists(baseFile.getFilePath())){
                    File file = new File(baseFile.getFilePath());
                    FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
                    IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
                    MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                    Long fileId = store(multipartFile, companyId, false, categoryId, "", masterId, userId, true);
                    //Returned list contains: [String filePath, String menuName, int docId, Long fileId]
                    //Returned list forming only for template files. They have docId (in which type of document's menu they will be used) and type - "label" or "document")
                    if(!Objects.isNull(baseFile.getDocId())) retList.add(new BaseFiles(baseFile.getFilePath(), baseFile.getFileName(), baseFile.getMenuName(), baseFile.getDocId(), fileId,baseFile.getType(), baseFile.getNum_labels_in_row()));
                } else logger.error("Method: copyFilesFromPathToCompany. Error: There is no file in path = " + baseFile.getFilePath());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method copyFilesFromPathToUserAccount", e);
            return null;
        }
        return retList;
    }

//***************************************************************************
//***************************** I M A G E S *********************************
//***************************************************************************


    private byte[] getImageInByte(BufferedImage img, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, format, baos);
            baos.flush();
            byte[] returnImgInByte = baos.toByteArray();
            baos.close();
            return returnImgInByte;
        } catch (Exception e) {
            throw new RuntimeException("FAIL!");
        }
    }

    public String GetFilePathByFileName(String fileName){
        String stringQuery="select path||'//'||name from files where name = '"+fileName+"'";
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getSingleResult().toString();

    }

    public String GetOriginalFileName(String fileName){
        String stringQuery="select original_name from files where name = '"+fileName+"'";
        Query query = entityManager.createNativeQuery(stringQuery);
        String originName = query.getSingleResult().toString();
        return originName;
    }

//    public String GetImgThumbPathByFileName(String fileName){
//        String stringQuery="select path||'/thumbs/'||name from files where name = '"+fileName+"'";
//        Query query = entityManager.createNativeQuery(stringQuery);
//        return query.getSingleResult().toString();
//
//    }

    private static BufferedImage downscaleImageSize(BufferedImage originalImage, int type, int newImgWidth){
        float ratio = (float)originalImage.getWidth()/originalImage.getHeight();
        int newIMG_HEIGHT=Math.round(newImgWidth/ratio);
        BufferedImage resizedImage = new BufferedImage(newImgWidth, newIMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(originalImage, 0, 0, newImgWidth, newIMG_HEIGHT, null);
        g.dispose();
        return resizedImage;
    }

    public String getSecretKey() throws Exception {
        try {
            String BASE_FILES_FOLDER;
            String line = "";
            if(isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//";  //запущено в винде
            } else {                    BASE_FILES_FOLDER = "//var//";} //запущено в linux
            String path = BASE_FILES_FOLDER+"erphy.key";
            if(isPathExists(path)){
                BufferedReader reader = new BufferedReader(new FileReader(path));
                line = reader.readLine();
                reader.close();
            }
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

}