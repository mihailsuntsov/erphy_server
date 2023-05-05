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

package com.dokio.util;
import com.dokio.controller._Info;
import com.dokio.message.response.Settings.CompanySettingsJSON;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.repository.Exceptions.CantSetHistoryCauseNegativeSumException;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CommonUtilites {

    Logger logger = Logger.getLogger("CommonUtilites");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private SecurityRepositoryJPA securityRepository;
    @Autowired
    private _Info info;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;


    @SuppressWarnings("Duplicates")
    //возвращает id статуса, установленного по-умолчанию
    public Long getDocumentsDefaultStatus(Long companyId, int documentId){
        try {
            String stringQuery;
            stringQuery = "select id from sprav_status_dock where company_id=" + companyId + " and dock_id=" + documentId + " and is_default=true";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    //возвращает id типа цены, установленного по-умолчанию
    public Long getPriceTypeDefault(Long companyId){
        try {
            String stringQuery;
            stringQuery = "select id from sprav_type_prices where company_id=" + companyId + " and is_default=true ";
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Object getByCrmSecretKey(String subj, String secretKey) throws Exception { // subj can be master_id for master id, or id for company_id
        try {
            String stringQuery;
            stringQuery =
                    "select "+subj+" from stores where " +
                    "crm_secret_key = :secretKey and " +
                    "crm_secret_key is not null and " +
                    "crm_secret_key !=''";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("secretKey",secretKey);
//            return Long.parseLong(query.getSingleResult().toString());
            return query.getSingleResult();
        }catch (NoResultException nre) {
            logger.error("NoResultException in method getByCrmSecretKey.", nre);
            throw new WrongCrmSecretKeyException();// because nno result only can be if data not found by secret key
        }catch (Exception e){
            logger.error("Exception in method getByCrmSecretKey.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

//    public Long getLongFromAnyTableById(String subj, String tableName, Long idValue){
//        try {
//            String stringQuery;
//            stringQuery =
//                    "select " + subj + " from " + tableName + " where id = " + idValue +" limit 1";
//            Query query = entityManager.createNativeQuery(stringQuery);
//            return Long.parseLong(query.getSingleResult().toString());
//        }catch (NoResultException nre) {
//            return null;
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }

    @SuppressWarnings("Duplicates")  //генератор номера документа
    public Long generateDocNumberCode(Long company_id, String docTableName)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from "+docTableName+" where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            return 0L;
        }
    }
    public Long generateDocNumberCode(Long company_id, String docTableName, Long masterId)
    {
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from "+docTableName+" where company_id="+company_id+" and master_id="+masterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method generateDocNumberCode. SQL query:" + stringQuery, e);
            return 0L;
        }
    }
    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isDocumentNumberUnical(Long company_id, Integer code, Long doc_id, String docTableName)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from "+docTableName+" where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(doc_id>0) stringQuery=stringQuery+" and id !="+doc_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return !(query.getResultList().size()>0); // >0 - false, номер не уникальный, !>0 - true, уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isDocumentNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на проведённость документа
    public Boolean isDocumentCompleted(Long company_id, Long doc_id, String docTableName) throws Exception {
        String stringQuery;
        stringQuery =
                " select id from " + docTableName + " where " +
                " company_id = "+company_id+
                " and id = "+doc_id+
                " and is_completed = true";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList().size()>0); // >0 - true, документ с таким id проведён
        }
        catch (NoResultException nre) {
            return false;
        }
        catch (Exception e) {
            logger.error("Exception in method isDocumentNumberUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
//            return null;
            throw new Exception();
        }
    }

    //превращает сет Long в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String SetOfLongToString(Set<Long> longList, String delimitter, String prefix, String suffix) {
        String result = longList.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //превращает список Long в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String ListOfLongToString(List<Long> longList, String delimitter, String prefix, String suffix) {
        String result = longList.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //превращает список Integer в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String ListOfIntToString(List<Integer> longList, String delimitter, String prefix, String suffix) {
        String result = longList.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //превращает список Integer в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String SetOfIntToString(Set<Integer> intSet, String delimitter, String prefix, String suffix) {
        String result = intSet.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(delimitter, prefix, suffix));
        return result;
    }
    //есть ли запись с идентичной UID в таблице? UID используется, чтобы исключить дубли при создании документов с использованием медленного интернета, когда браузер может дублировать POST-запросы
    public Boolean isDocumentUidUnical(String uid, String docTableName){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from "+docTableName+" where " +
                " master_id="+myMasterId+
                " and uid=:uid";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("uid",uid);
            return !(query.getResultList().size()>0); // >0 - false, номер не уникальный, ==0 - true, уникальный
        }
        catch (Exception e) {
            logger.error("Exception in method isDocumentUidUnical. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return true;
        }
    }

    private static final Set VALID_TABLE_NAMES
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "shipment",
                    "acceptance",
                    "retail_sales",
                    "paymentout",
                    "paymentin",
                    "orderout",
                    "orderin",
                    "returnsup",
                    "return",
                    "cagent",
                    "payment_account",
                    "boxoffice",
                    "kassa",
                    "withdrawal", // изъятие из кассы ККТ
                    "depositing", // внесение в кассу ККТ
                    "correction")
            .collect(Collectors.toCollection(HashSet::new)));

    private static final Set NEGATIVE_ALLOWED_TABLE_NAMES // таблицы, исторические данные которых могут содержать отрицательные значения (например баланс контрагента может быть отрицательным, а сумма в кассе нет)
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of(    "cagent")
            .collect(Collectors.toCollection(HashSet::new)));


    public Boolean addDocumentHistory(String docAlias, Long companyId, Long objectId, String docTableName, String docPageName, Long docId, BigDecimal summIn, BigDecimal summOut, boolean isCompleted, String doc_number, Long doc_status_id) throws Exception {
        // docAlias - alias таблицы объекта, по которому идет запись. Данная таблица хранит историю изменений по этому объекту. Может быть: cagent, payment_account и др (см. VALID_TABLE_NAMES)
        // objectId - id объекта, к которому относится изменение. Например, id контрагента в случае docAlias='cagent', или кассы предприятия в случае docAlias='boxoffice'
        // docTableName - таблица документа, который влияет на сумму (из которого производится запись) - например shipment для отгрузки
        // docId - id документа, из которого производится запись (в таблице docTableName)
        // summIn, summOut - суммы, на которые изменится значение в истории. Примеры:
        // - из кассы изъяли 100 р.: summIn = 0, summOut = 100.00)
        // - произвели отгрузку на 200 р.: summIn = 0, summOut = 200.00 (Отрицательный баланс - Нам должны)
        // - произвели приёмку на 300 р.: summIn = 300, summOut = 0 (Положительный баланс - Мы должны)
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        BigDecimal summBefore = getSummFromHistory(docAlias, companyId, objectId);

        if(
                (
                        // если документ проводится, и не относится к тем, для которых разрешена отрицательная сумма, и итоговая сумма отрицательная
                        // (например, в кассе предприятия, или на расчетном счету не может быть отрицательная сумма), ...
                        isCompleted && // документ проводится
                        !NEGATIVE_ALLOWED_TABLE_NAMES.contains(docAlias) && // документ не относится к тем, для которых разрешена отрицательная сумма
                        summIn.subtract(summOut).add(summBefore).compareTo(new BigDecimal(0)) < 0 // и итоговая сумма отрицательная
                )
                        || // ИЛИ
                (
                        // если документ снимается с проведения, и не относится к тем, для которых разрешена отрицательная сумма
                        // (например, в кассе предприятия, или на расчетном счету не может быть отрицательная сумма),
                        // и итоговая сумма после отмены проведения будет отрицательная
                        // (т.е. summBefore (сумма до отмены проведения, или сумма на данный момент) меньше summIn (сумма, которая была про проведении прибавлена на счёт,
                        // а сейчас, с отменой проведения, система пытается её вычесть со счёта.))

                        !isCompleted && // документ снимается с проведения
                        !NEGATIVE_ALLOWED_TABLE_NAMES.contains(docAlias) && // документ не относится к тем, для которых разрешена отрицательная сумма
                        summBefore.compareTo(summIn) < 0 // и итоговая сумма после отмены проведения будет отрицательная
                )
        ) throw new CantSetHistoryCauseNegativeSumException();// то кидаем исключение 'Невозможно записать отрицательную сумму', чтобы произошла отмена транзакции

        if(     securityRepository.companyBelongsToMyMastersAccount(companyId) &&
                !Objects.isNull(summBefore) &&
                VALID_TABLE_NAMES.contains(docAlias) &&
                VALID_TABLE_NAMES.contains(docTableName)) {

            String stringQuery;
            stringQuery =
                    // при первом проведении (т.е. в истории еще нет документа с такими company_id, doc_table_name и doc_id)
                    " insert into history_" + docAlias + "_summ (" +
                    " master_id," +
                    " company_id," +
                    " date_time_created," +
                    " object_id," +
                    " doc_table_name," +
                    " doc_id," +
                    " summ_in," +
                    " summ_out," +
                    " doc_number," +
                    " doc_page_name," +
                    " is_completed," +
                    " doc_status_id" +
                    ") values (" +
                    myMasterId + ", " +
                    companyId + ", " +
                    "now()," +
                    objectId + ", " +
                    "'"+docTableName+"', " + // тут не используем setParameter, т.к. выше уже проверили эти таблицы на валидность
                    docId + ", " +
                    summIn + ", " +
                    summOut + ", " +
                    doc_number + ", " +
                    "'"+docPageName+"', " +
                    isCompleted+", " +
                    doc_status_id +
                    " ) " +
                    // при отмене проведения или повторном проведении срабатывает ключ уникальности записи в БД по company_id, doc_table_name, doc_id
                    " ON CONFLICT ON CONSTRAINT history_" + docAlias + "_uq" +// "upsert"
                    " DO update set " +
                    (isCompleted?(" summ_in = " + summIn +", "):" ") + // только если проводим
                    (isCompleted?(" summ_out = " + summOut +", "):" ") + // только если проводим
                    (isCompleted?(" object_id = " + objectId +", "):" ") + // только если проводим
                    " is_completed = " + isCompleted + // единственный апдейт при отмене проведения
                    (isCompleted?(", " + " doc_status_id = " + doc_status_id):""); // только если проводим
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method addDocumentHistory. SQL: "+stringQuery, e);
                throw new Exception(); // отмена всей транзакции по причине ошибки записи
            }

        } else throw new Exception(); // отмена всей транзакции по причине попытки создать запись по не своему предприятию или не по правильным таблицам
    }


    // возвращает настройки предприятия с проверкой на то что предприятие принадлежит master-аккаунту залогиненного пользователя
    public CompanySettingsJSON getCompanySettings_(Long company_id) {
        if(securityRepositoryJPA.isItAllMyMastersDocuments("companies",company_id.toString())){
            try{
                return getCompanySettings(company_id);
            }catch (Exception e) {
                logger.error("Exception in method getCompanySettings_.", e);
                e.printStackTrace();
                return null;
            }
        } else {
            logger.error("Company with id="+company_id+" is not belongs to master id of logged in user");
            return null;
        }
    }


    // возвращает настройки предприятия
    public CompanySettingsJSON getCompanySettings(Long company_id) throws Exception {
        String stringQuery;
        stringQuery =   " select "+
                        " cmp.st_prefix_barcode_pieced  as st_prefix_barcode_pieced,   "+
                        " cmp.st_prefix_barcode_packed  as st_prefix_barcode_packed,"+
                        " cmp.st_netcost_policy         as st_netcost_policy," +
//                        " cmp.store_orders_department_id  as store_orders_department_id," +
//                        " coalesce(cmp.store_if_customer_not_found,'create_new') as store_if_customer_not_found," +
//                        " cmp.store_default_customer_id   as store_default_customer_id,"+
//                        " cmp.store_default_creator_id as store_default_creator_id," +
//                        " coalesce(cmp.store_days_for_esd,0) as store_days_for_esd, " +
                        " coalesce(cmp.nds_payer, false) as nds_payer, " +
                        " coalesce(cmp.nds_included, false) as nds_included, " +
                        "(select count(*) from stores where company_id="+company_id+" and coalesce(is_deleted,false)=false) > 0 as is_store" +
//                        " coalesce(cmp.store_auto_reserve, false) as store_auto_reserve, " +
//                        " coalesce(cmp.is_store, false) as is_store" +
                        " from companies cmp where cmp.id="+company_id;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            CompanySettingsJSON returnObj=new CompanySettingsJSON();
                for (Object[] obj : queryList) {
                    returnObj.setSt_prefix_barcode_pieced((Integer)         obj[0]);
                    returnObj.setSt_prefix_barcode_packed((Integer)         obj[1]);
                    returnObj.setNetcost_policy((String)                    obj[2]);
//                    returnObj.setStore_orders_department_id(obj[3]!=null?Long.parseLong(obj[3].toString()):null);
//                    returnObj.setStore_if_customer_not_found((String)       obj[4]);
//                    returnObj.setStore_default_customer_id(obj[5]!=null?Long.parseLong(obj[5].toString()):null);
//                    returnObj.setStore_default_creator_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
//                    returnObj.setStore_days_for_esd((Integer)               obj[7]);
                    returnObj.setVat((Boolean)                              obj[3]);
                    returnObj.setVat_included((Boolean)                     obj[4]);
//                    returnObj.setStore_auto_reserve((Boolean)               obj[10]);
                    returnObj.setIs_store((Boolean)                         obj[5]);
                }

            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getCompanySettings. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception("Exception in method getCompanySettings. SQL query:"+stringQuery);
        }
    }

// воpвращает сумму разности входящих и исходящих поступлений по исторической таблице
    public BigDecimal getSummFromHistory(String objectName, Long companyId, Long objectId) {
        if(VALID_TABLE_NAMES.contains(objectName)) {
            String stringQuery =
                    " select coalesce(SUM(coalesce(summ_in,0)-coalesce(summ_out,0)),0) from history_"+objectName+"_summ where " +
                    " company_id= " + companyId +
                    " and object_id= " + objectId +
                    " and is_completed=true ";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                return (BigDecimal) query.getSingleResult();
            } catch (NoResultException nre) {
                return new BigDecimal(0);
            } catch (Exception e) {
                logger.error("Exception in method getSummFromHistory. SQL: " + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null; // попытка запроса не по правильным таблицам
    }

    private static final Set VALID_OUTCOME_PAYMENTS_TABLE_NAMES
        = Collections.unmodifiableSet((Set<? extends String>) Stream.of("paymentout","orderout","withdrawal").collect(Collectors.toCollection(HashSet::new)));
//    private static final Set VALID_INCOME_PAYMENTS_TABLE_NAMES
//            = Collections.unmodifiableSet((Set<? extends String>) Stream.of("paymentout","orderout","withdrawal").collect(Collectors.toCollection(HashSet::new)));
    @SuppressWarnings("Duplicates")
    // устанавливает доставлено=true для исходящего внутреннего платежа (например Выемки, либо внутреннего Исходящего платежа, или внутреннего Расходного ордера)
    public boolean setDelivered(String tableName, Long id) throws Exception {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = "Update "+tableName+" p" +
                " set is_delivered=true " +
                " where p.id = " + id + " and p.master_id=" + myMasterId;
        if (!VALID_OUTCOME_PAYMENTS_TABLE_NAMES.contains(tableName))
            throw new IllegalArgumentException("Invalid query parameters. The table is not in the allowed list: "+tableName); // отмена всей транзакции из вызывающего метода
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method setDelivered. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception(); // отмена всей транзакции из вызывающего метода
        }
    }

    @SuppressWarnings("Duplicates")
    // устанавливает доставлено=false для исходящего внутреннего платежа (например Выемки, либо внутреннего Исходящего платежа, или внутреннего Расходного ордера)
    // используется при отмене проведения входящего внутреннего платежа
    public boolean setUndelivered(String tableName, Long id) throws Exception {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = "Update "+tableName+" p" +
                " set is_delivered=false " +
                " where p.id = " + id + " and p.master_id=" + myMasterId;
        if (!VALID_OUTCOME_PAYMENTS_TABLE_NAMES.contains(tableName))
            throw new IllegalArgumentException("Invalid query parameters. The table is not in the allowed list: "+tableName); // отмена всей транзакции из вызывающего метода
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method setDelivered. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception(); // отмена всей транзакции из вызывающего метода
        }
    }

    @SuppressWarnings("Duplicates")
    // возвращает список страниц (первые 3 параметра - Найдено: p1, страница p2 из p3)
    // size - общее количество выборки
    // result - количество записей, отображаемых на странице
    // pagenum - отображаемый в пагинации номер страницы
    public List<Integer> getPagesList(int pagenum, int size, int result){
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
        return pageList;
    }

    public Object getFieldValueFromTableById(String tableName, String columnName, Long masterId, Long id) {
            String stringQuery =
            " select " + columnName + " from " + tableName + " where master_id = " + masterId + " and id = " + id;
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                return query.getSingleResult();
            } catch (NoResultException nre) {
                return null;
            } catch (Exception e) {
                logger.error("Exception in method getFieldValueFromTableById. SQL: " + stringQuery, e);
                e.printStackTrace();
                return null;
            }
    }

    public boolean isDateValid(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        sdf.setLenient(false);
        return sdf.parse(s, new ParsePosition(0)) != null;
    }

    // returns map of user's language translated words by their keys in format "key - word"
    // example of using:
    // Map<String, String> map = commonUtilites.translateForMe(new String[]{"'all'","'selected'"});
    // map.get("all");
    @SuppressWarnings("Duplicates")
    public Map<String, String> translateForMe(String[] keys){
        String suffix = userRepositoryJPA.getMySuffix();
        String stringQuery =            "select key, tr_"+suffix+" from _dictionary ";
        if(keys.length>0)
            stringQuery = stringQuery + " where key in("+String.join(",", keys)+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            Map<String, String> map = new HashMap<>();
            for(Object[] obj:queryList){
                map.put((String)obj[0], (String)obj[1]);
            }
            return map;
        } catch (Exception e) {
            logger.error("Exception in method translateMe. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    public Map<String, String> translateForUser(Long userId, String[] keys){
        String suffix = userRepositoryJPA.getUserSuffix(userId);
        String stringQuery =            "select key, tr_"+suffix+" from _dictionary ";
        if(keys.length>0)
            stringQuery = stringQuery + " where key in("+String.join(",", keys)+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            Map<String, String> map = new HashMap<>();
            for(Object[] obj:queryList){
                map.put((String)obj[0], (String)obj[1]);
            }
            return map;
        } catch (Exception e) {
            logger.error("Exception in method translateMe. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    public SettingsGeneralJSON getSettingsGeneral() {
        String stringQuery =
                "select " +
                        " p.show_registration_link as show_registration_link, " +
                        " p.allow_registration as allow_registration, " +
                        " p.show_forgot_link as show_forgot_link, " +
                        " p.allow_recover_password as allow_recover_password, " +
                        " (select value from version) as database_version," +
                        " (select date from version) as database_date," +
                        " p.show_in_signin as show_in_signin," +
                        " p.plan_default_id as plan_default_id," +
                        " (select coalesce(daily_price, 0.0) from plans where id=(select plan_default_id from settings_general)) as daily_price," +
                        " p.free_trial_days as free_trial_days," +
                        " coalesce(p.is_saas, false) as is_saas" +
                        " from settings_general p";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SettingsGeneralJSON doc = new SettingsGeneralJSON();
            if (queryList.size() > 0) {
                doc.setShowRegistrationLink((Boolean) queryList.get(0)[0]);
                doc.setAllowRegistration((Boolean) queryList.get(0)[1]);
                doc.setShowForgotLink((Boolean) queryList.get(0)[2]);
                doc.setAllowRecoverPassword((Boolean) queryList.get(0)[3]);
                doc.setDatabaseVersion((String) queryList.get(0)[4]);
                doc.setDatabaseVersionDate((String) queryList.get(0)[5]);
                doc.setShowInSignin((String) queryList.get(0)[6]);
                doc.setBackendVersion(info.getBackendVersion());
                doc.setBackendVersionDate(info.getBackendVersionDate());
                doc.setPlanDefaultId((Integer) queryList.get(0)[7]);
                doc.setPlanPrice((BigDecimal) queryList.get(0)[8]);
                doc.setFreeTrialDays((Integer) queryList.get(0)[9]);
                doc.setSaas((Boolean) queryList.get(0)[10]);
            }
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getSettingsGeneral. SQL query:" + stringQuery, e);
            return null;
        }
    }
    public boolean isSaas() {
        String stringQuery =
                "select coalesce(p.is_saas, false) as is_saas from settings_general p;";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (Boolean)query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method isSaas. SQL query:" + stringQuery, e);
            return false;
        }
    }

    private String getStoreIp(String crmSecretKey){
        try {
            return getByCrmSecretKey("store_ip", crmSecretKey).toString();
        } catch (Exception e) {
            logger.error("Exception in method getStoreIp.", e);
            e.printStackTrace();
            return null;
        }
    }

    public void checkStoreIp(String storeRemoteAddr, String key) throws Exception {
        String crmRemoteAddress = getStoreIp(key);
        if(Objects.isNull(crmRemoteAddress))
            throw new Exception("Can't found CRM-side StoreIP by given CRM secret key.");
        if(!storeRemoteAddr.equals(crmRemoteAddress)){
            throw new Exception("The store remote address in query is not equals to store remote address in CRM. Query remote address: " + storeRemoteAddr + ", CRM remote address: " + crmRemoteAddress);
        }
    }
}
