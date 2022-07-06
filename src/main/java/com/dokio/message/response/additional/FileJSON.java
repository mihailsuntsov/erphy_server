package com.dokio.message.response.additional;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public class FileJSON {
    private MultipartFile file;         //сам файл
    private Path UPLOADED_FOLDER;       //путь загрузки файла
    private Path UPLOADED_THUMBS_FOLDER;//путь для превьюшек
    private String originalFilename;    //оригинальное имя файла
    private String fileExtention;       //расширение
    private String generatedFileName;   //сгенерированное имя файла, БЕЗ расширения (нужно для искл-ния дублирования имен файлов, под ним файл сохраняется на диске, а originalFilename записывается в БД)
    private Long fileSize;              //размер файла в байтах
    private String mimeType;
    private String newFileName;         //сгенерированное имя файла и расширение
    private Long companyId;             //предприятие (передается из формы)
    private Boolean anonyme_access;     //может ли быть анонимный доступ к файлу (передается из формы) - для картинок сайта, фото товаров интернет-магазина, расшаренных документов
    private Long categoryId;            //выбранная категория
    private String description;         //описание файла
    private int THUMBNAIL_WIDTH;        //размер файла предпросмотра картинки
    private int MAX_IMG_WIDTH;          //макс размер картинки с общим доступом
    private Long myMasterId;
    private Long myId;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Path getUPLOADED_FOLDER() {
        return UPLOADED_FOLDER;
    }

    public void setUPLOADED_FOLDER(Path UPLOADED_FOLDER) {
        this.UPLOADED_FOLDER = UPLOADED_FOLDER;
    }

    public Path getUPLOADED_THUMBS_FOLDER() {
        return UPLOADED_THUMBS_FOLDER;
    }

    public void setUPLOADED_THUMBS_FOLDER(Path UPLOADED_THUMBS_FOLDER) {
        this.UPLOADED_THUMBS_FOLDER = UPLOADED_THUMBS_FOLDER;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileExtention() {
        return fileExtention;
    }

    public void setFileExtention(String fileExtention) {
        this.fileExtention = fileExtention;
    }

    public String getGeneratedFileName() {
        return generatedFileName;
    }

    public void setGeneratedFileName(String generatedFileName) {
        this.generatedFileName = generatedFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Boolean getAnonyme_access() {
        return anonyme_access;
    }

    public void setAnonyme_access(Boolean anonyme_access) {
        this.anonyme_access = anonyme_access;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTHUMBNAIL_WIDTH() {
        return THUMBNAIL_WIDTH;
    }

    public void setTHUMBNAIL_WIDTH(int THUMBNAIL_WIDTH) {
        this.THUMBNAIL_WIDTH = THUMBNAIL_WIDTH;
    }

    public int getMAX_IMG_WIDTH() {
        return MAX_IMG_WIDTH;
    }

    public void setMAX_IMG_WIDTH(int MAX_IMG_WIDTH) {
        this.MAX_IMG_WIDTH = MAX_IMG_WIDTH;
    }

    public Long getMyMasterId() {
        return myMasterId;
    }

    public void setMyMasterId(Long myMasterId) {
        this.myMasterId = myMasterId;
    }

    public Long getMyId() {
        return myId;
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }
}
