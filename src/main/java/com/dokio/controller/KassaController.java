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
package com.dokio.controller;

import com.dokio.message.request.*;
import com.dokio.message.request.Settings.KassaCashierSettingsForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Settings.KassaCashierSettingsJSON;
import com.dokio.message.response.additional.FilesUniversalJSON;
import com.dokio.repository.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KassaController {
    Logger logger = Logger.getLogger("KassaController");

    @Autowired
    KassaRepository kassaRepository;


    @PostMapping("/api/auth/getKassaTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getKassaTable(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getKassaTable: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        Long companyId;//по какому предприятию показывать / 0 - по всем (подставляется ниже, а так то прередаётся "" если по всем)
        Long departmentId;//по какому отделению / 0 - по всем (--//--//--//--//--//--//--)
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        List<KassaJSON> returnList;

        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "name";
            sortAsc = "asc";
        }
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;
        }
        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Long.parseLong(searchRequest.getCompanyId());
        } else {
            companyId = 0L;
        }
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Long.parseLong(searchRequest.getDepartmentId());
        } else {
            departmentId = 0L;
        }
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;
        }
        int offsetreal = offset * result;//создана переменная с номером страницы
        returnList = kassaRepository.getKassaTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId, departmentId, searchRequest.getFilterOptionsIds());//запрос списка: взять кол-во rezult, начиная с offsetreal
        ResponseEntity<List> responseEntity = new ResponseEntity<>(returnList, HttpStatus.OK);
        return responseEntity;
    }

    @PostMapping("/api/auth/getKassaPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getKassaPagesList(@RequestBody SearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getKassaPagesList: " + searchRequest.toString());

        int offset; // номер страницы. Изначально это null
        int result; // количество записей, отображаемых на странице
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        Long companyId;//по какому предприятию показывать документы/ 0 - по всем
        Long departmentId;//по какому отделению / 0 - по всем
        String searchString = searchRequest.getSearchString();
        if (searchRequest.getCompanyId() != null && !searchRequest.getCompanyId().isEmpty() && searchRequest.getCompanyId().trim().length() > 0) {
            companyId = Long.parseLong(searchRequest.getCompanyId());
        } else {
            companyId = 0L;
        }
        if (searchRequest.getDepartmentId() != null && !searchRequest.getDepartmentId().isEmpty() && searchRequest.getDepartmentId().trim().length() > 0) {
            departmentId = Long.parseLong(searchRequest.getDepartmentId());
        } else {
            departmentId = 0L;
        }
        if (searchRequest.getResult() != null && !searchRequest.getResult().isEmpty() && searchRequest.getResult().trim().length() > 0) {
            result = Integer.parseInt(searchRequest.getResult());
        } else {
            result = 10;}
        if (searchRequest.getOffset() != null && !searchRequest.getOffset().isEmpty() && searchRequest.getOffset().trim().length() > 0) {
            offset = Integer.parseInt(searchRequest.getOffset());
        } else {
            offset = 0;}
        pagenum = offset + 1;
        int size = kassaRepository.getKassaSize(searchString,companyId,departmentId,searchRequest.getFilterOptionsIds());//  - общее количество записей выборки
        int listsize;//количество страниц пагинации
        if((size%result) == 0){//общее количество выборки делим на количество записей на странице
            listsize= size/result;//если делится без остатка
        }else{
            listsize= (size/result)+1;}
        int maxPagenumInBegin;//
        List<Integer> pageList = new ArrayList<Integer>();//список, в котором первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
        pageList.add(size);
        pageList.add(pagenum);
        pageList.add(listsize);

        if (listsize<=5){
            maxPagenumInBegin=listsize;//
        }else{
            maxPagenumInBegin=5;
        }
        if(pagenum >=3) {
            if((pagenum==listsize)||(pagenum+1)==listsize){
                for(int i=(pagenum-(4-(listsize-pagenum))); i<=pagenum-3; i++){
                    if(i>0) {
                        pageList.add(i);  //создается список пагинации за - 4 шага до номера страницы (для конца списка пагинации)
                    }}}
            for(int i=(pagenum-2); i<=pagenum; i++){
                pageList.add(i);  //создается список пагинации за -2 шага до номера страницы
            }
            if((pagenum+2) <=listsize) {
                for(int i=(pagenum+1); i<=(pagenum+2); i++){
                    pageList.add(i);  //создается список пагинации  на +2 шага от номера страницы
                }
            }else{
                if(pagenum<listsize) {
                    for (int i = (pagenum + (listsize - pagenum)); i <= listsize; i++) {
                        pageList.add(i);  //создается список пагинации от номера страницы до конца
                    }}}
        }else{//номер страницы меньше 3
            for(int i=1; i<=pagenum; i++){
                pageList.add(i);  //создается список пагинации от 1 до номера страницы
            }
            for(int i=(pagenum+1); i<=maxPagenumInBegin; i++){
                pageList.add(i);  //создаются дополнительные номера пагинации, но не более 5 в сумме
            }}
        ResponseEntity<List> responseEntity = new ResponseEntity<>(pageList, HttpStatus.OK);
        return responseEntity;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getKassaValuesById",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getKassaValuesById(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getKassaValuesById with parameters: " +
                "id: " + id.toString());
        KassaJSON response=kassaRepository.getKassaValuesById(id);
        try {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка запроса данных по кассе", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/insertKassa")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> insertKassa(@RequestBody KassaForm request){
        logger.info("Processing post request for path /api/auth/insertKassa: " + request.toString());
        //перед созданием новой кассы идет проверка на ее уникальность.
        if (kassaRepository.isKassaUnique(request.getZn_kkt(), request.getCompany_id(),0L)){
            Long newDocument = kassaRepository.insertKassa(request);
            if(newDocument!=null && newDocument>0){
                ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + String.valueOf(newDocument)+"\n" +  "]", HttpStatus.OK);
                return responseEntity;
            } else {
                ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка создания", HttpStatus.INTERNAL_SERVER_ERROR);
                return responseEntity;
            }
        }else{
            logger.info("Ошибка создания кассы. Касса не уникальна по заводскому номеру в данном предприятии.");
            return new ResponseEntity<>(0, HttpStatus.OK); //если фронтэнд получает 0 - он понимает, что касса не уникальна, и сообщает об этом пользователю
        }
    }

    @PostMapping("/api/auth/updateKassa")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> updateKassa(@RequestBody KassaForm request){
        logger.info("Processing post request for path /api/auth/updateKassa: " + request.toString());
        //перед сохранением кассы проверка на ее уникальность.
        if (kassaRepository.isKassaUnique(request.getZn_kkt(), request.getCompany_id(), request.getId())){
            return new ResponseEntity<>(kassaRepository.updateKassa(request), HttpStatus.OK);
        }else{
            logger.info("Ошибка сохранения кассы. Касса не уникальна по заводскому номеру в данном предприятии.");
            return new ResponseEntity<>(0, HttpStatus.OK); //если фронтэнд получает 0 - он понимает, что касса не уникальна, и сообщает об этом пользователю
        }

    }

    @RequestMapping(
            value = "/api/auth/isKassaUnique",
            params = {"zn_kkt","company_id","current_kassa_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> isKassaUnique(
            @RequestParam("zn_kkt") String zn_kkt, //заводской номер ККТ
            @RequestParam("company_id") Long company_id, // id предприятия
            @RequestParam("current_kassa_id") Long current_kassa_id) //id кассы, которую не нужно принимать во внимание (чтобы не получилось так, что при сохранении мы стали проверять на уникальность, и нашли сохраняемую кассу, и сказали, что сохраняемая касса не уникальна)
    {
        logger.info("Processing get request for path /api/auth/isKassaUnique with parameters: " +
                "company_id: " + company_id +
                ", zn_kkt: " + zn_kkt +
                ", current_kassa_id: " + current_kassa_id
        );
        Boolean response;
        try {
            response=kassaRepository.isKassaUnique(zn_kkt, company_id,current_kassa_id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method isKassaUnique. company_id=" + company_id + ", zn_kkt=" + zn_kkt + ", current_kassa_id=" + current_kassa_id, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка запроса на уникальность ККТ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Запись в БД состояния смены (создание новой смены или изменение её статуса)
    @RequestMapping(
            value = "/api/auth/updateShiftStatus",
            params = {"zn_kkt", "shiftStatusId", "shiftNumber", "shiftExpiredAt", "companyId", "kassaId", "fnSerial"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> updateShiftStatus(
            @RequestParam("zn_kkt") String zn_kkt, //заводской номер ККТ
            @RequestParam("shiftStatusId") String shiftStatusId, //статус смены: opened closed expired
            @RequestParam("shiftNumber") Long shiftNumber, //номер смены, генерируется ККТ
            @RequestParam("shiftExpiredAt") String shiftExpiredAt, // время истечения (экспирации) смены в текстовом формате, генерируемом самой ККТ. Вместе с kassa_id и shift_number используется для уникальности смены
            @RequestParam("companyId") Long companyId, // id предприятия
            @RequestParam("kassaId") Long kassaId, //id кассы
            @RequestParam("fnSerial") String fnSerial) //Серийный номер ФН
    {
        logger.info("Processing get request for path /api/auth/updateShiftStatus with parameters: " +
                    "company_id: " + companyId +
                    ", zn_kkt: " + zn_kkt +
                    ", shiftStatusId: " + shiftStatusId +
                    ", shiftNumber: " + shiftNumber +
                    ", shiftExpiredAt: " + shiftExpiredAt +
                    ", kassaId: " + kassaId +
                    ", fnSerial: " + fnSerial
        );
        try {
            Boolean response=kassaRepository.updateShiftStatus(zn_kkt, shiftStatusId, shiftNumber, shiftExpiredAt, companyId, kassaId, fnSerial);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method updateShiftStatus. Parameters: company_id: " + companyId +
                    ", zn_kkt: " + zn_kkt +
                    ", shiftStatusId: " + shiftStatusId +
                    ", shiftNumber: " + shiftNumber +
                    ", shiftExpiredAt: " + shiftExpiredAt +
                    ", kassaId: " + kassaId +
                    ", fnSerial: " + fnSerial, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка запроса на запись состояния смены ККТ в базу данных", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Запись в БД чека
    @RequestMapping(
            value = "/api/auth/addReceipt",
            params = {"zn_kkt", "shiftStatusId", "shiftNumber", "shiftExpiredAt", "companyId", "kassaId", "fnSerial"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> addReceipt(
            @RequestParam("zn_kkt") String zn_kkt, //заводской номер ККТ
            @RequestParam("shiftStatusId") String shiftStatusId, //статус смены: opened closed expired
            @RequestParam("shiftNumber") Long shiftNumber, //номер смены, генерируется ККТ
            @RequestParam("shiftExpiredAt") String shiftExpiredAt, // время истечения (экспирации) смены в текстовом формате, генерируемом самой ККТ. Вместе с kassa_id и shift_number используется для уникальности смены
            @RequestParam("companyId") Long companyId, // id предприятия
            @RequestParam("kassaId") Long kassaId, //id кассы
            @RequestParam("fnSerial") String fnSerial, //Серийный номер ФН
            @RequestParam("operationId") String operationId, //id операции (sell, buy и т.д.)
            @RequestParam("sno") String sno, //система налогообложения кассы (из паспорта кассы)
            @RequestParam("billing_address") String billing_address, //место расчетов
            @RequestParam("payment_type") String payment_type, // тип оплаты (cash,electronically,mixed)
            @RequestParam("cash") BigDecimal cash, //
            @RequestParam("electronically") BigDecimal electronically, // электронными
            @RequestParam("id") Long id, // id документа
            @RequestParam("docId") int docId) // id наименования документа в таблице documents
    {
        logger.info("Processing get request for path /api/auth/addReceipt with parameters: " +
                    "company_id: " + companyId +
                    ", zn_kkt: " + zn_kkt +
                    ", shiftStatusId: " + shiftStatusId +
                    ", shiftNumber: " + shiftNumber +
                    ", shiftExpiredAt: " + shiftExpiredAt +
                    ", kassaId: " + kassaId +
                    ", fnSerial: " + fnSerial +
                    ", operationId: " + operationId +
                    ", sno: " + sno +
                    ", billing_address: " + billing_address +
                    ", payment_type: " + payment_type +
                    ", cash: " + cash +
                    ", electronically: " + electronically +
                    ", id: " + id +
                    ", docId: " + docId
        );
        try {
            Boolean response=kassaRepository.addReceipt(zn_kkt, shiftStatusId, shiftNumber, shiftExpiredAt, companyId, kassaId, fnSerial, operationId, sno, billing_address, payment_type, cash, electronically, id, docId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error("Exception in method addReceipt. Parameters: company_id: " + companyId +
                    ", zn_kkt: " + zn_kkt +
                    ", shiftStatusId: " + shiftStatusId +
                    ", shiftNumber: " + shiftNumber +
                    ", shiftExpiredAt: " + shiftExpiredAt +
                    ", kassaId: " + kassaId +
                    ", fnSerial: " + fnSerial +
                    ", operationId: " + operationId +
                    ", sno: " + sno +
                    ", billing_address: " + billing_address +
                    ", payment_type: " + payment_type +
                    ", cash: " + cash +
                    ", electronically: " + electronically +
                    ", id: " + id +
                    ", docId: " + docId, e);
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка запроса на запись чека в базу данных", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    //отдаёт номер смены для работы с ККМ в режиме разработчика. Т.к. в кассе может не быть ФН, то номер смены будет всегда 0, что не приемлемо.
//    @SuppressWarnings("Duplicates")
//    @RequestMapping(
//            value = "/api/auth/getShiftNum",
//            method = RequestMethod.GET, produces = "application/json;charset=utf8")
//    public ResponseEntity<?> getShiftNum()
//    {
//        logger.info("Processing get request for path /api/auth/getShiftNum with no parameters. ");
//        KassaCashierSettingsJSON response=kassaRepository.getKassaCashierSettings();
//        try {
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Ошибка запроса getShiftNum", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    @PostMapping("/api/auth/deleteKassa")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> deleteKassa(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/deleteKassa with id's: " + request.getChecked());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(kassaRepository.deleteKassa(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка удаления кассы", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    @PostMapping("/api/auth/undeleteKassa")
    @SuppressWarnings("Duplicates")
    public  ResponseEntity<?> undeleteKassa(@RequestBody SignUpForm request){
        logger.info("Processing post request for path /api/auth/undeleteKassa: " + request.toString());

        String checked = request.getChecked() == null ? "": request.getChecked();
        if(kassaRepository.undeleteKassa(checked)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка восстановления кассы", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
    //отдаёт список касс для кассира по id отделения
    @RequestMapping(
            value = "/api/auth/getKassaListByDepId",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getKassaListByDepId(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getKassaListByDepId with parameters: " +
                "id: " + id.toString());
        try {
            return new ResponseEntity<>(kassaRepository.getKassaListByDepId(id), HttpStatus.OK);
        } catch (Exception e){
            logger.error("Controller getKassaListByDepId error", e);
            return new ResponseEntity<>("Ошибка запроса на список касс, доступных для пользователя", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //отдаёт список касс для кассира по id отделения
    @RequestMapping(
            value = "/api/auth/getKassaListByBoxofficeId",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getKassaListByBoxofficeId(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getKassaListByBoxofficeId with parameters: id: " + id.toString());
        try {return new ResponseEntity<>(kassaRepository.getKassaListByBoxofficeId(id), HttpStatus.OK);
        } catch (Exception e){
            logger.error("Controller getKassaListByBoxofficeId error", e);
            return new ResponseEntity<>("Ошибка запроса на список касс, доступных для пользователя", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//сохраняет кассовые настройки для пользователя
    @PostMapping("/api/auth/updateCashierSettings")
    @SuppressWarnings("Duplicates")
        public ResponseEntity<?> updateKassa(@RequestBody KassaCashierSettingsForm request){
        logger.info("Processing post request for path /api/auth/KassaCashierSettingsForm: " + request.toString());
        if(kassaRepository.updateCashierSettings(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка сохранения кассовых настроек для пользователя", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
//отдаёт кассовые настройки для пользователя
    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getKassaCashierSettings",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getKassaCashierSettings()
    {
        logger.info("Processing get request for path /api/auth/getKassaCashierSettings with no parameters. ");
        KassaCashierSettingsJSON response=kassaRepository.getKassaCashierSettings();
        try {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка запроса данных по кассе", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(
            value = "/api/auth/getListOfKassaFiles",
            params = {"id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getListOfKassaFiles(
            @RequestParam("id") Long id)
    {
        logger.info("Processing get request for path /api/auth/getListOfKassaFiles with parameters: " +
                "id: " + id.toString());
        List<FilesUniversalJSON> returnList;
        try {
            returnList = kassaRepository.getListOfKassaFiles(id);
            return new ResponseEntity<>(returnList, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка запроса на список файлов", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            value = "/api/auth/deleteKassaFile",
            params = {"kassa_id","file_id"},
            method = RequestMethod.DELETE, produces = "application/json;charset=utf8")
    public ResponseEntity<?> deleteKassaFile(
            @RequestParam("kassa_id") Long kassa_id,
            @RequestParam("file_id") Long file_id)
    {
        logger.info("Processing delete request for path /api/auth/deleteKassaFile with parameters: " +
                "kassa_id: " + kassa_id.toString()+
                ", file_id: " + file_id);
        if(kassaRepository.deleteKassaFile(kassa_id,file_id)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка удаления файла", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("Duplicates")
    @PostMapping("/api/auth/addFilesToKassa")
    public ResponseEntity<?> addFilesToKassa(@RequestBody UniversalForm request) throws ParseException{
        logger.info("Processing post request for path /api/auth/addFilesToKassa: " + request.toString());

        if(kassaRepository.addFilesToKassa(request)){
            ResponseEntity<String> responseEntity = new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
            return responseEntity;
        } else {
            ResponseEntity<String> responseEntity = new ResponseEntity<>("Ошибка добавления файла", HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

}
