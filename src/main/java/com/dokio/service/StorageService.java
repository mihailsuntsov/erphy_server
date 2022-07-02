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

import com.dokio.repository.FileRepositoryJPA;
//import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
//import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

    @PersistenceContext
    private EntityManager entityManager;
//    @Autowired
//    private EntityManagerFactory emf;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;
//    @Autowired
//    private ProductsRepositoryJPA productsRepository;
    @Autowired
    private FileRepositoryJPA frj;

//***************************************************************************
//***************************** F I L E S ***********************************
//***************************************************************************

    private MultipartFile file;         //сам файл
    private Path UPLOADED_FOLDER;       //путь загрузки файла
    private Path UPLOADED_THUMBS_FOLDER;//путь для превьюшек
    private String originalFilename;    //оригинальное имя файла
    private String fileExtention;       //расширение
    private String generatedFileName;   //сгенерированное имя файла, БЕЗ расширения (нужно для искл-ния дублирования имен файлов, под ним файл сохраняется на диске, а originalFilename записывается в БД)
    private Long fileSize;              //размер файла в байтах
    private String mimeType;
    private String newFileName;         //сгенерированное имя файла и расширение
    private Integer companyId;          //предприятие (передается из формы)
    private Boolean anonyme_access;     //может ли быть анонимный доступ к файлу (передается из формы) - для картинок сайта, фото товаров интернет-магазина, расшаренных документов
    private Integer categoryId;         //выбранная категория
    private String description;         //описание файла
    private int THUMBNAIL_WIDTH;        //размер файла предпросмотра картинки
    private int MAX_IMG_WIDTH;          //макс размер картинки с общим доступом
    private Long myMasterId;
    private Long myId;

    @SuppressWarnings("Duplicates")
    private boolean storePreparation(MultipartFile file, Integer companyId, Boolean anonyme_access, Integer categoryId, String description)//подготовка для записи файла
    {
        try
        {
            String BASE_FILES_FOLDER;
            Calendar calendar = Calendar.getInstance();
            String YEAR = calendar.get(Calendar.YEAR) + "//";
            this.myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            this.myId = userRepository.getUserId();
            if(isPathExists("C://")){   BASE_FILES_FOLDER = "C://Temp//files//";  //запущено в винде
            } else {                    BASE_FILES_FOLDER = "//usr//dokio//files//";} //запущено в linux
            String MY_MASTER_ID_FOLDER = this.myMasterId + "//";
            String MY_COMPANY_ID_FOLDER = companyId + "//";
            String THUMBS_FOLDER = "thumbs//";
            this.UPLOADED_FOLDER= Paths.get(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER + MY_COMPANY_ID_FOLDER + YEAR);
            this.UPLOADED_THUMBS_FOLDER= Paths.get(BASE_FILES_FOLDER + MY_MASTER_ID_FOLDER + MY_COMPANY_ID_FOLDER + YEAR + THUMBS_FOLDER);
            // в итоге получается путь для файла вида /usr/dokio/files/133/1/2019
            // год нужен чтобы не скапливалось много файлов в одной папке
            this.file=file;
            this.originalFilename=file.getOriginalFilename();
            this.fileExtention=getFileExtension(this.originalFilename);
            if (this.fileExtention.isEmpty() || this.fileExtention.trim().length() == 0) {
                    this.fileExtention = "._";
                } else if (this.fileExtention.length() >=16) {
                    this.fileExtention = this.fileExtention.substring(0,15);}//т.к. в БД 16 байт под это дело
            this.generatedFileName=GetGeneratedFileName();
            this.fileSize=file.getSize();
            this.mimeType=file.getContentType();
            this.newFileName=this.generatedFileName+this.fileExtention;
            this.companyId=companyId;
            this.description=description;
            this.anonyme_access=anonyme_access;
            this.categoryId=categoryId;
            this.THUMBNAIL_WIDTH=400;
            this.MAX_IMG_WIDTH=1200;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean store(MultipartFile file, Integer companyId, Boolean anonyme_access, Integer categoryId, String description) {
        try
        {
            if(storePreparation(file,companyId,anonyme_access,categoryId,description)) {
                createDirectory(this.UPLOADED_FOLDER.toString());

                if(// если файл - картинка - надо сохранить его с опр. условиями (размер и thumbnail)
                this.fileExtention.equalsIgnoreCase(".jpg")  ||
                this.fileExtention.equalsIgnoreCase(".jpeg") ||
                this.fileExtention.equalsIgnoreCase(".png"))
                {
                    createDirectory(this.UPLOADED_THUMBS_FOLDER.toString());
                    BufferedImage originalImage = ImageIO.read(file.getInputStream());
                    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
                    String format = getImageFormatName(type);
                    if (originalImage.getWidth()>MAX_IMG_WIDTH && anonyme_access) { originalImage = downscaleImageSize(originalImage, type, MAX_IMG_WIDTH);}
                    BufferedImage thumbImage = downscaleImageSize(originalImage, type, THUMBNAIL_WIDTH);
                    byte[] thumbInByte = getImageInByte(thumbImage, format);
                    byte[] imageInByte = getImageInByte(originalImage, format);
                    Path filePath = Paths.get(this.UPLOADED_FOLDER + "//" + this.newFileName);
                    Path thumbPath = Paths.get(this.UPLOADED_THUMBS_FOLDER + "//" + this.newFileName);
                    Files.write(filePath, imageInByte);
                    Files.write(thumbPath, thumbInByte);
                } else
                    Files.copy(file.getInputStream(), this.UPLOADED_FOLDER.resolve(this.newFileName));

                if(frj.storeFileToDB( // запись в БД информации о файле
                        this.myMasterId,
                        this.companyId,
                        this.myId,
                        this.UPLOADED_FOLDER.toString(),
                        this.newFileName,
                        this.originalFilename,
                        this.fileExtention,
                        this.fileSize,
                        this.mimeType,
                        this.description,
                        this.anonyme_access,
                        this.categoryId
                       ))
                    return true;
                else return false;
            } else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    public String GetImgThumbPathByFileName(String fileName){
        String stringQuery="select path||'//thumbs//'||name from files where name = '"+fileName+"'";
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getSingleResult().toString();

    }

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
}