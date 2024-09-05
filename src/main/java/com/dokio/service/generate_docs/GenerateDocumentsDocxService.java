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

package com.dokio.service.generate_docs;

import java.io.*;
import java.util.*;

import com.dokio.security.CryptoService;
import com.dokio.service.StorageService;
import de.phip1611.Docx4JSRUtil;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.docx4j.wml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

@Service
public class GenerateDocumentsDocxService {

    Logger logger = Logger.getLogger("GenerateDocumentsDocxService");

    @Autowired
    StorageService storageService;


    public GenerateDocumentsDocxService() {
    }

    public static GenerateDocumentsDocxService getInstance() {
        return new GenerateDocumentsDocxService();
    }


    public Resource generateWordDoc(Resource resource){
        return resource;
    }










    // Загрузка документа с помощью docx4j. Вернет объект, представляющий полный (на данный момент)
    // пустой документ. Теперь мы можем использовать API Docx4J для добавления, удаления и изменения содержимого
    public WordprocessingMLPackage getTemplate(String name) throws Docx4JException, FileNotFoundException {
        WordprocessingMLPackage template = WordprocessingMLPackage.load(new FileInputStream(new File(name)));
        return template;
    }

    //	Этот метод смотрит, содержит ли таблица один из наших заполнителей. Если так, то эта таблица возвращается
    public Tbl getTemplateTable(List<Object> tables, String templateKey) throws Docx4JException, JAXBException {
        for (Iterator<Object> iterator = tables.iterator(); iterator.hasNext();) {
            Object tbl = iterator.next();
            List<?> textElements = getAllElementFromObject(tbl, Text.class);
            for (Object text : textElements) {
                Text textElement = (Text) text;
                if (textElement.getValue() != null && textElement.getValue().equals(templateKey))
                    return (Tbl) tbl;
            }
        }
        return null;
    }
    //	Этот метод копирует наш шаблон и заменяет заполнители в этой строке шаблона предоставленными значениями.
//	Эта копия добавлена ​​в таблицу. С помощью этого фрагмента кода мы можем заполнять произвольные
//	таблицы в текстовом документе, сохраняя при этом макет и стиль таблиц.
    public static void addRowToTable(Tbl reviewtable, Tr templateRow, Map<String, String> replacements) {
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);
        List textElements = getAllElementFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            String replacementValue = (String) replacements.get(text.getValue());
            if (replacementValue != null)
                text.setValue(replacementValue);
        }

        reviewtable.getContent().add(workingRow);
    }
    // Метод позволяет искать определенный элемент и все его дочерние элементы для определенного класса.
    // Например, можно использовать это, чтобы получить все таблицы в документе, все строки в таблице и т.п.
    public static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<Object>();
        if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();

        if (obj!=null && obj.getClass().equals(toSearch))
            result.add(obj);
        else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }

    String clearPlaceholder(String placeholder){//оставляем только большие буквы и подчеркивание
        return(placeholder.replaceAll("[^A-Z_~]",""));
    }


    private void replacePlaceholder(WordprocessingMLPackage template, String name, String placeholder ) {
        List<Object> texts = getAllElementFromObject(template.getMainDocumentPart(), Text.class);

        for (Object text : texts) {
            Text textElement = (Text) text;
            if (clearPlaceholder(textElement.getValue()).equals(placeholder)) {
                textElement.setValue(name);
            }
        }
    }

    private final static String FORMAT = ".docx";
    private final static String MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final static String MIME_TYPE2 = "application/zip";

    static Logger log = Logger.getLogger(GenerateDocumentsDocxService.class.getName());

    public static String getMimeType (File file){
        ContentInfo info;
        try {
            info = (new ContentInfoUtil()).findMatch(file);
            if (info != null)
                return info.getMimeType();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //собирает Map вида "Название колонки - Значение в колонке" для одной строки таблицы.
    public Map<String,String> getHashMap(String[] colNames, String[] colValues){
        Map<String,String> retMap = new HashMap<String,String>();
        int i=0;
        for (String colName : colNames){
            retMap.put(colName,colValues[i]);
            i++;
        }
        return retMap;
    }




    public byte[] generateDocXDocument(String filePath, Map <String, String> replaceMap, Long masterId) throws Exception {
//        try{

//            File template= new File(filePath);//сформировалась строка типа new File("C:\\Temp\\files\\4\\1\\2020/f561b9d3-e27-2020-09-16-15-09-25-572.docx")
//            if (template != null && outputDocument != null && !outputDocument.isEmpty()){
//                if (template.exists()){
//                    if (!outputDocument.endsWith(FORMAT)){
//                        log.warn("The output document must be .docx");
//                        return false;
//                    }
//                    // Проверяем что mime соответствует нашим высоким требованиям
//                    String mimeType = getMimeType(template);
//                    if (mimeType != null && (mimeType.equals(MIME_TYPE)||mimeType.equals(MIME_TYPE2))){
                        try {
                            byte[] decryptedBytesOfFile = storageService.loadFile(filePath, masterId);
                            // Загружаем темплейт
                            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(decryptedBytesOfFile));
//                            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new FileInputStream(template));
                            Docx4JSRUtil.searchAndReplace(wordMLPackage, replaceMap);
                            OutputStream outputStream = new ByteArrayOutputStream();
                            Save saver = new Save(wordMLPackage);
                            saver.save(outputStream);
                            return ((ByteArrayOutputStream) outputStream).toByteArray();
//                            // Вывод docx
//                            OutputStream output = new FileOutputStream(outputDocument);
//                            Save saver = new Save(wordMLPackage);
//                            if (saver.save(output)){
//                                log.info("Document " + outputDocument + " writed successfully");
//                            } else {
//                                log.info("Document " + outputDocument + " writing error!");
//                                throw new Exception("Document " + outputDocument + " writing error!");
//                            }
//                            return true;

                        } catch (Docx4JException e) {
                            e.printStackTrace();
                            logger.error("Docx4JException in method GenerateDocumentsDocxService/generateDocXDocument. ", e);
                            return null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("Exception in method GenerateDocumentsDocxService/generateDocXDocument. ", e);
                            return null;
                        }
//                    }else{
//                        log.error("Invalid document mime type");
//                    }
//                }
//            }
//            return false;
//
//        } catch (Exception e) {
//            logger.error("Exception in method UserRepositoryJPA/getMyDateTime. ", e);
//            e.printStackTrace();
//            throw new Exception();
//        }
    }
    public WordprocessingMLPackage generateMLPackage(String filePath, Map <String, String> replaceMap, Long masterId) throws Exception {
        try {
            byte[] decryptedBytesOfFile = storageService.loadFile(filePath, masterId);
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(decryptedBytesOfFile));
            Docx4JSRUtil.searchAndReplace(wordMLPackage, replaceMap);
            return wordMLPackage;
        } catch (Docx4JException e) {
            e.printStackTrace();
            logger.error("Docx4JException in method GenerateDocumentsDocxService/generateMLPackage. ", e);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method GenerateDocumentsDocxService/generateMLPackage. ", e);
            return null;
        }
    }


//    public boolean generateDocument(File template, String outputDocument, Map<String,String> changeMap, List<Map<String,String>> mapAsList){
//        if (template != null && outputDocument != null && !outputDocument.isEmpty()){
//            if (template.exists()){
//                if (!outputDocument.endsWith(FORMAT)){
//                    log.warn("The output document must be .docx");
//                    return false;
//                }
//                // Проверяем что mime соответствует нашим высоким требованиям
//                String mimeType = getMimeType(template);
//                if (mimeType != null && (mimeType.equals(MIME_TYPE)||mimeType.equals(MIME_TYPE2))){
//                    WordprocessingMLPackage wordMLPackage;
//                    try {
//                        // Загружаем темплейт
//                        wordMLPackage = WordprocessingMLPackage.load(template);
//                        VariablePrepare.prepare(wordMLPackage);
//                        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
//
//                        // Подменяем плейсхолдеры
//                        documentPart.variableReplace(changeMap);
//                        List<Object> tables = getAllElementFromObject(documentPart, Tbl.class);
//
//                        // Находим таблицу,
//                        Tbl tempTable = getTemplateTable(tables, new String[]{"TM_PARAMETER","TM_VALUE"}[0]);
//                        List<Object> rows = getAllElementFromObject(tempTable, Tr.class);
//
//                        // да не простую, а о двух строчках
//                        if (rows.size() == 2) {
//                            Tr templateRow = (Tr) rows.get(1);
//                            for (Map<String, String> replacements : mapAsList) {
//                                addRowToTable(tempTable, templateRow, replacements);
//                            }
//                            // Удаляем 2й ряд (с метками)
//                            tempTable.getContent().remove(templateRow);
//                        }
//
//                        // Вывод docx
//                        OutputStream output = new FileOutputStream(outputDocument);
//                        Save saver = new Save(wordMLPackage);
//                        if (saver.save(output)){
//                            log.info("Document " + outputDocument + " ok");
//                            return true;
//                        }
//                    } catch (Docx4JException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }else{
//                    log.error("Invalid document mime type");
//                }
//            }
//        }
//        return false;
//    }

}
