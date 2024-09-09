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
import com.dokio.message.request.additional.ChangeOwnerForm;
import com.dokio.message.response.CagentsJSON;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.message.response.Settings.CompanySettingsJSON;
import com.dokio.message.response.Settings.SettingsGeneralJSON;
import com.dokio.message.response.additional.TranslateHTMLmessageResult;
import com.dokio.repository.CagentRepositoryJPA;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.Exceptions.CantSetHistoryCauseNegativeSumException;
import com.dokio.repository.Exceptions.WrongCrmSecretKeyException;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA companyRepository;


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
            logger.error("NoResultException in method getByCrmSecretKey. subj="+subj+", secretKey="+secretKey, nre);
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
    @SuppressWarnings("Duplicates")
    public Boolean isSiteNameAllowed(String name)
    {
        String stringQuery;
        stringQuery = "select count(*) from _saas_stores_for_ordering where third_lvl_user_domain = :name "+// there is no checking on deleted, because third_lvl_user_domain_uq
        " or :name in ('mail','dokio','panel')" +
        " or :name like('ns%')" +
        " or :name like('site%')";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("name", name);
            return !(((BigInteger)query.getSingleResult()).intValue()>0); // >0 - false, name is not uniuqe, !>0 - true, unique
        }
        catch (Exception e) {
            logger.error("Exception in method isSiteNameAllowed. SQL query:" + stringQuery, e);
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
    //превращает список String в строку с заданным делимитером, началом и концом. Например (1,2,3,4,5)
    public String SetOfStringToString(Set<String> stringSet, String delimitter, String prefix, String suffix) {
        String result = stringSet.stream()
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
                        "(select count(*) from stores where company_id="+company_id+" and coalesce(is_deleted,false)=false) > 0 as is_store," +
                        " coalesce(cmp.booking_doc_name_variation_id,1) as booking_doc_name_variation, " +
                        " cmp.time_zone_id as time_zone_id," +
                        " s.canonical_id as time_zone " +
//                        " coalesce(cmp.store_auto_reserve, false) as store_auto_reserve, " +
//                        " coalesce(cmp.is_store, false) as is_store" +
                        " from companies cmp " +
                        " inner join sprav_sys_timezones s on s.id = cmp.time_zone_id "+
                        " where cmp.id="+company_id; // inner - because each company has time zone
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
//                    returnObj.setStore_auto_reserve((Boolean)             obj[10]);
                    returnObj.setIs_store((Boolean)                         obj[5]);
                    returnObj.setBooking_doc_name_variation_id((Integer)    obj[6]);
                    returnObj.setBooking_doc_name_variation(returnObj.getBooking_doc_name_variation_id()==1?"appointment":"reservation");
                    returnObj.setTime_zone_id((Integer)                     obj[7]);
                    returnObj.setTime_zone((String)                         obj[8]);

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
    public boolean isTimeValid(String inputTimeString) {
        try {
            LocalTime.parse(inputTimeString);
            return true;
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
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
            logger.error("Exception in method translateForMe. SQL: " + stringQuery, e);
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
            logger.error("Exception in method translateForUser. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    public Map<String, String> translateFromLanguage(String langSuffix, String[] keys) throws Exception{
        if(langSuffix.length()>2){logger.error("Illegal suffix "+langSuffix); throw new Exception("Illegal suffix "+langSuffix);}
        String stringQuery =            "select key, tr_"+langSuffix+" from _dictionary ";
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
            logger.error("Exception in method translateFromLanguage. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    public Map<String, String> translateHTMLmessages(Long userId, String[] keys){
        String suffix = userRepositoryJPA.getUserSuffix(userId);
        String stringQuery =            "select key, tr_"+suffix+" from _saas_messages ";
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
            logger.error("Exception in method translateHTMLmessages. SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public TranslateHTMLmessageResult translateHTMLmessage(String key) throws Exception {
        String suffix = userRepositoryJPA.getUserSuffix(userRepository.getUserId());
        String stringQuery =
        "select tr_"+suffix+" from _saas_messages where key = :key";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("key",key);
            TranslateHTMLmessageResult res = new TranslateHTMLmessageResult();
            res.setResult((String)query.getSingleResult());
            return res;
        } catch (Exception e) {
            logger.error("Exception in method translateHTMLmessage (key). SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")
    public String translateHTMLmessage(String key, String suffix) throws Exception{
        if(suffix.length()>2){logger.error("Illegal suffix "+suffix); throw new Exception("Illegal suffix "+suffix);}
        String stringQuery =
                "select tr_"+suffix+" from _saas_messages where key = :key";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("key",key);
            return (String)query.getSingleResult();
        } catch (Exception e) {
            logger.error("Exception in method translateHTMLmessage (key, suffix). SQL: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    public SettingsGeneralJSON getSettingsGeneral(boolean withSensitiveInfo) {
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
                        " coalesce(p.is_saas, false) as is_saas," +
                        " coalesce(p.let_woo_plugin_to_sync, false) as let_woo_plugin_to_sync," +
                        " coalesce(p.woo_plugin_oldest_acceptable_ver, '1000.1.1') as woo_plugin_oldest_acceptable_ver," +
                        " is_sites_distribution as is_sites_distribution, " +  //in this SaaS there is a sites distribution
                        " stores_alert_email as stores_alert_email, " +     //email for messages about no more free stores or stores quantity less than min_qtt_stores
                        " min_qtt_stores_alert as min_qtt_stores_alert," + //quantity of stores to sent email to stores_alert_email
                        " max_store_orders_per_24h_1_account as mqtt24acc, " + // max quantity of online stores that can be ordered in 24h from one account
                        " max_store_orders_per_24h_1_ip as mqtt24ip, " + //  max quantity of online stores that can be ordered in 24h from one IP address
                        " saas_payment_currency as saas_payment_currency," +
                        " url_terms_and_conditions as url_terms_and_conditions, " +
                        " url_privacy_policy as url_privacy_policy, " +
                        " url_data_processing_agreement as url_data_processing_agreement," +
                        " root_domain as root_domain," +
                        " billing_master_id as billing_master_id, " +
                        " billing_shipment_creator_id as billing_shipment_creator_id, " +
                        " billing_shipment_company_id as billing_shipment_company_id, " +
                        " billing_shipment_department_id as billing_shipment_department_id, " +
                        " billing_cagents_category_id as billing_cagents_category_id, " +
                        " billing_companies_product_id as billing_companies_product_id, " +
                        " billing_departments_product_id as billing_departments_product_id, " +
                        " billing_users_product_id as billing_users_product_id, " +
                        " billing_products_product_id as billing_products_product_id, " +
                        " billing_counterparties_product_id as billing_counterparties_product_id, " +
                        " billing_megabytes_product_id as billing_megabytes_product_id, " +
                        " billing_stores_product_id as billing_stores_product_id, " +
                        " billing_stores_woo_product_id as billing_stores_woo_product_id, " +
                        " billing_plan_product_id as billing_plan_product_id, " +
                        " coalesce(create_support_user, false) as create_support_user " +
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
                doc.setLet_woo_plugin_to_sync((Boolean) queryList.get(0)[11]);
                doc.setWoo_plugin_oldest_acceptable_ver((String) queryList.get(0)[12]);
                doc.setIs_sites_distribution((Boolean) queryList.get(0)[13]);
                doc.setStores_alert_email(withSensitiveInfo?((String) queryList.get(0)[14]):"");
                doc.setMin_qtt_stores_alert((Integer) queryList.get(0)[15]);
                doc.setMax_store_orders_per_24h_1_account((Integer) queryList.get(0)[16]);
                doc.setMax_store_orders_per_24h_1_ip((Integer) queryList.get(0)[17]);
                doc.setSaas_payment_currency((String) queryList.get(0)[18]);
                doc.setUrl_terms_and_conditions((String) queryList.get(0)[19]);
                doc.setUrl_privacy_policy((String) queryList.get(0)[20]);
                doc.setUrl_data_processing_agreement((String) queryList.get(0)[21]);
                doc.setRoot_domain((String) queryList.get(0)[22]);
                doc.setBilling_master_id(withSensitiveInfo?(                 queryList.get(0)[23]!=null?Long.parseLong(queryList.get(0)[23].toString()):null):null);
                doc.setBilling_shipment_creator_id(withSensitiveInfo?(       queryList.get(0)[24]!=null?Long.parseLong(queryList.get(0)[24].toString()):null):null);
                doc.setBilling_shipment_company_id(withSensitiveInfo?(       queryList.get(0)[25]!=null?Long.parseLong(queryList.get(0)[25].toString()):null):null);
                doc.setBilling_shipment_department_id(withSensitiveInfo?(    queryList.get(0)[26]!=null?Long.parseLong(queryList.get(0)[26].toString()):null):null);
                doc.setBilling_cagents_category_id(withSensitiveInfo?(       queryList.get(0)[27]!=null?Long.parseLong(queryList.get(0)[27].toString()):null):null);
                doc.setBilling_companies_product_id(withSensitiveInfo?(      queryList.get(0)[28]!=null?Long.parseLong(queryList.get(0)[28].toString()):null):null);
                doc.setBilling_departments_product_id(withSensitiveInfo?(    queryList.get(0)[29]!=null?Long.parseLong(queryList.get(0)[29].toString()):null):null);
                doc.setBilling_users_product_id(withSensitiveInfo?(          queryList.get(0)[30]!=null?Long.parseLong(queryList.get(0)[30].toString()):null):null);
                doc.setBilling_products_product_id(withSensitiveInfo?(       queryList.get(0)[31]!=null?Long.parseLong(queryList.get(0)[31].toString()):null):null);
                doc.setBilling_counterparties_product_id(withSensitiveInfo?( queryList.get(0)[32]!=null?Long.parseLong(queryList.get(0)[32].toString()):null):null);
                doc.setBilling_megabytes_product_id(withSensitiveInfo?(      queryList.get(0)[33]!=null?Long.parseLong(queryList.get(0)[33].toString()):null):null);
                doc.setBilling_stores_product_id(withSensitiveInfo?(         queryList.get(0)[34]!=null?Long.parseLong(queryList.get(0)[34].toString()):null):null);
                doc.setBilling_stores_woo_product_id(withSensitiveInfo?(     queryList.get(0)[35]!=null?Long.parseLong(queryList.get(0)[35].toString()):null):null);
                doc.setBilling_plan_product_id(withSensitiveInfo?(           queryList.get(0)[36]!=null?Long.parseLong(queryList.get(0)[36].toString()):null):null);
                doc.setCreate_support_user((Boolean) queryList.get(0)[37]);
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

    public Long SetStoreRentAgreementUnit(Long masterId, Long myId, Long storeId, Long storeWooId, String agreementType, String agreementVer, String timestamp) throws Exception {
        String stringQuery;
        stringQuery =
            " insert into _saas_agreements_units (" +
                " date_time_agree," +
                " master_user," +
                " user_who_agree," +
                " agreement_id," +
                " store_id," +
                " store_woo_id" +
            ") values (" +
                " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                masterId + ", " +
                myId + ", " +
                "(select id from _saas_agreements where type=:agreementType and version = :agreementVer)," +
                storeId + ", " +
                storeWooId +
            " ) ";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("agreementType",agreementType);
            query.setParameter("agreementVer",agreementVer);
            query.executeUpdate();
            stringQuery="select id from _saas_agreements_units where master_user = "+masterId+" and store_id="+storeId+" and date_time_agree=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS'))";
            query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());

        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method SetAgreementUnit. agreementType = "+agreementType+", agreementVer="+agreementVer+", SQL: "+stringQuery, e);
            throw new Exception();
        }
    }

//    public boolean isThereFreeStoresForRent() throws Exception {
//
//        String stringQuery;
//        stringQuery = "select(select count(*) from _saas_stores_for_ordering where ready_to_distribute=true and distributed=false) >0";
//
//        try {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            return (Boolean)query.getSingleResult();
//
//        }catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method isThereFreeStoresForRent. SQL: "+stringQuery, e);
//            throw new Exception();
//        }
//    }

    public Long getFreeSiteToRentId() throws Exception {
        String stringQuery;
        stringQuery = "select id from _saas_stores_for_ordering where ready_to_distribute=true and distributed=false order by date_time_created, id limit 1";

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        }catch (NoResultException nre) {
            logger.error("NoResultException in method getFreeStoreToRentId. SQL="+stringQuery, nre);
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFreeStoreToRentId. SQL: "+stringQuery, e);
            throw new Exception();
        }
    }

    public boolean isCanOrderStoreForRent(Long storeId) throws Exception {

        String stringQuery;
        stringQuery = "select (select count(*) from _saas_stores_for_ordering where store_id="+storeId+" and is_deleted=false)=0";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return (Boolean)query.getSingleResult();

        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method isCanOrderStoreForRent. SQL = "+stringQuery, e);
            throw new Exception();
        }
    }

    public void idBelongsMyMaster(String tableName, Long id, Long masterId) throws Exception {
        String stringQuery;
        stringQuery = "select ((select count(*) from "+tableName+" where id="+id+" and master_id="+masterId+")=0)";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!Objects.isNull(id) && (Boolean)query.getSingleResult()) throw new Exception("Id's of the object don't belong to its master Id. Object of table "+tableName+", id="+id+", masterId="+masterId);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method idBelongsMyMaster. SQL = "+stringQuery, e);
            throw new Exception();
        }
    }

    public void idBelongsMyMaster(String tableName, Integer id, Long masterId) throws Exception {
        String stringQuery;
        stringQuery = "select ((select count(*) from "+tableName+" where id="+id+" and master_id="+masterId+")=0)";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!Objects.isNull(id) && (Boolean)query.getSingleResult()) throw new Exception("Id's of the object don't belong to its master Id. Object of table "+tableName+", id="+id+", masterId="+masterId);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method idBelongsMyMaster. SQL = "+stringQuery, e);
            throw new Exception();
        }
    }
    public void idsBelongMyMaster_Long(String tableName, Set<Long> ids, Long masterId) throws Exception {
        String stringQuery;
        stringQuery = "select ((select count(*) from "+tableName+" where id in "+SetOfLongToString(ids,",","(",")")+" and master_id="+masterId+")="+ids.size()+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            if (ids.size()>0 && !(Boolean)query.getSingleResult()) throw new Exception("Ids don't belong to its master Id. Table of ids: "+tableName+", ids="+ids.toString()+", masterId="+masterId);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method idsBelongMyMaster_Long. SQL = "+stringQuery, e);
            throw new Exception();
        }
    }
    public void idsBelongMyMaster_Int(String tableName, Set<Integer> ids, Long masterId) throws Exception {
        String stringQuery;
        stringQuery = "select ((select count(*) from "+tableName+" where id in "+SetOfIntToString(ids,",","(",")")+" and master_id="+masterId+")="+ids.size()+")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            if (ids.size()>0 && !(Boolean)query.getSingleResult()) throw new Exception("Ids don't belong to its master Id. Table of ids: "+tableName+", ids="+ids.toString()+", masterId="+masterId);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method idsBelongMyMaster_Int. SQL = "+stringQuery, e);
            throw new Exception();
        }
    }
    public Long getCagentIdByUserId(Long userId) throws Exception {
        String stringQuery;
        stringQuery = "select id from cagents where user_id="+userId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        }catch (NoResultException nre) {
            logger.error("Counterparty id not founded by user_id = " + userId +". SQL="+stringQuery, nre);
            return null; // in a case of SaaS it will return cagentId, in a case of opensource install it returns null
            //throw new Exception("Counterparty id not founded by user_id = " + userId);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getCagentIdByUserId. SQL: "+stringQuery, e);
            throw new Exception();
        }
    }

    @Transactional
    public void setLetToRegisterNewUsers(boolean isAllow) throws Exception {
        String stringQuery = "update settings_general set show_registration_link = "+isAllow+", allow_registration = "+isAllow;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            logger.error("Exception in method setLetToRegisterNewUsers. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // return count of users
    public int getCntRegisteredUsers() throws Exception {
        String stringQuery;
        stringQuery = "select count(*) from users";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return ((BigInteger) query.getSingleResult()).intValue();
        }
        catch (Exception e) {
            logger.error("Exception in method getCntRegisteredUsers. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    public String getTimeZoneById(int id){
        String stringQuery;
        stringQuery=    " select " +
                " s.canonical_id " +
                " from " +
                " sprav_sys_timezones s" +
                " where s.id=" + id;
        Query query = entityManager.createNativeQuery(stringQuery);
        return query.getSingleResult().toString();
    }

    public Long getDocumentStatus(int document_id, int type, long masterId, long companyId) throws Exception {
        String stringQuery;
        stringQuery = "select id from " +
                " sprav_status_dock " +
                " where " +
                " master_id="+masterId+" and " +
                " company_id="+companyId+" and " +
                " dock_id="+document_id+" and " +
                " coalesce(is_deleted,false)=false and " +
                " status_type="+type+" limit 1";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        }catch (NoResultException nre) {
            logger.error("NoResultException in method getDocumentStatus. SQL="+stringQuery, nre);
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getDocumentStatus. SQL: "+stringQuery, e);
            throw new Exception();
        }
    }

    public Map<String, String> getCagentMapValues(Long id){
        CagentsJSON cg = cagentRepository.getCagentValues(id);
        return new HashMap<String, String>() {
            {
                put("${CG_FULL_NAME}",          cg.getJr_jur_full_name());
                put("${CG_SHORT_NAME}",         cg.getName());
                put("${CG_SURNAME}",            cg.getJr_fio_family());
                put("${CG_MIDDLENAME}",         cg.getJr_fio_otchestvo());
                put("${CG_NAME}",               cg.getJr_fio_name());
                put("${CG_DESCRIPTION}",        cg.getDescription());
                put("${CG_TELEPHONE}",          cg.getTelephone());
                put("${CG_EMAIL}",              cg.getEmail());
                put("${CG_SITE}",               cg.getSite());
                put("${CG_LEGAL_FORM}",         cg.getLegal_form());
                put("${CG_REG_ADDR_ZIP}",       cg.getJr_zip_code());
                put("${CG_REG_ADDR_COUNTRY}",   cg.getJr_country());
                put("${CG_REG_ADDR_REGION}",    cg.getJr_region());
                put("${CG_REG_ADDR_CITY}",      cg.getJr_city());
                put("${CG_REG_ADDR_STREET}",    cg.getJr_street());
                put("${CG_REG_ADDR_HOUSE}",     cg.getJr_home());
                put("${CG_REG_ADDR_ROOM}",      cg.getJr_flat());
                put("${CG_REG_ADDR_ADDIT}",     cg.getJr_additional_address());
                put("${CG_MAIL_ADDR_ZIP}",      cg.getZip_code());
                put("${CG_MAIL_ADDR_COUNTRY}",  cg.getCountry());
                put("${CG_MAIL_ADDR_REGION}",   cg.getRegion());
                put("${CG_MAIL_ADDR_CITY}",     cg.getCity());
                put("${CG_MAIL_ADDR_STREET}",   cg.getStreet());
                put("${CG_MAIL_ADDR_HOUSE}",    cg.getHome());
                put("${CG_MAIL_ADDR_ROOM}",     cg.getFlat());
                put("${CG_MAIL_ADDR_ADDIT}",    cg.getAdditional_address());
                put("${CG_TIN}",                cg.getJr_inn());
                put("${CG_VAT}",                cg.getJr_vat());
                put("${CG_ID_CARD}",            cg.getId_card());
                put("${CG_DATE_OF_BIRTH}",      cg.getDate_of_birth_user_format());
                put("${CG_SEX}",                cg.getSex_user_format());
            }};
    }
    public Map<String, String> getCompanyMapValues(Long id){
        CompaniesJSON mc = companyRepository.getCompanyValues(id);
        return new HashMap<String, String>() {
            {
                put("${MC_FULL_NAME}",          mc.getJr_jur_full_name());
                put("${MC_SHORT_NAME}",         mc.getName());
                put("${MC_SURNAME}",            mc.getJr_fio_family());
                put("${MC_MIDDLENAME}",         mc.getJr_fio_otchestvo());
                put("${MC_NAME}",               mc.getJr_fio_name());
                put("${MC_TELEPHONE}",          mc.getTelephone());
                put("${MC_EMAIL}",              mc.getEmail());
                put("${MC_SITE}",               mc.getSite());
                put("${MC_LEGAL_FORM}",         mc.getLegal_form());
                put("${MC_REG_ADDR_ZIP}",       mc.getJr_zip_code());
                put("${MC_REG_ADDR_COUNTRY}",   mc.getJr_country());
                put("${MC_REG_ADDR_REGION}",    mc.getJr_region());
                put("${MC_REG_ADDR_CITY}",      mc.getJr_city());
                put("${MC_REG_ADDR_STREET}",    mc.getJr_street());
                put("${MC_REG_ADDR_HOUSE}",     mc.getJr_home());
                put("${MC_REG_ADDR_ROOM}",      mc.getJr_flat());
                put("${MC_REG_ADDR_ADDIT}",     mc.getJr_additional_address());
                put("${MC_MAIL_ADDR_ZIP}",      mc.getZip_code());
                put("${MC_MAIL_ADDR_COUNTRY}",  mc.getCountry());
                put("${MC_MAIL_ADDR_REGION}",   mc.getRegion());
                put("${MC_MAIL_ADDR_CITY}",     mc.getCity());
                put("${MC_MAIL_ADDR_STREET}",   mc.getStreet());
                put("${MC_MAIL_ADDR_HOUSE}",    mc.getHome());
                put("${MC_MAIL_ADDR_ROOM}",     mc.getFlat());
                put("${MC_MAIL_ADDR_ADDIT}",    mc.getAdditional_address());
                put("${MC_TIN}",                mc.getJr_inn());
                put("${MC_VAT}",                mc.getJr_vat());
                put("${MC_CEO}",                mc.getFio_director());
                put("${MC_CEO_JOB_TITLE}",      mc.getDirector_position());
                put("${MC_ACCOUNTANT}",         mc.getFio_glavbuh());
                put("${MC_REGISTRY_NUM}",       mc.getJr_jur_ogrn());


            }};
    }

    @Transactional
    public Integer changeDocumentOwner(ChangeOwnerForm request) {
        String ids = SetOfLongToString(request.getDocumentIds(),",","","");
        if((securityRepositoryJPA.userHasPermissions_OR(request.getDocumentRegistryId(),request.getEditDocAllCompaniesPermit()) && securityRepositoryJPA.isItAllMyMastersDocuments(request.getDocumentName(),ids)) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого редактируют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(request.getDocumentRegistryId(),request.getEditDocMyCompanyPermit()) && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments(request.getDocumentName(),ids))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого редактируют) и владелец документов - я
                (securityRepositoryJPA.userHasPermissions_OR(request.getDocumentRegistryId(),request.getEditMyDocPermit()) && securityRepositoryJPA.isItAllMyMastersAndMyDocuments_(request.getDocumentName(),ids)))
        {
            String stringQuery="";
            try{
                Long masterId = userRepositoryJPA.getMyMasterId();
                idBelongsMyMaster("users", request.getNewOwnerId(), masterId);
                stringQuery =
                        " update files" +
                        " set owner_id=" + request.getNewOwnerId() +
                        ", date_time_changed=now() " +
                        " where master_id=" + masterId +
                        " and id in ("+ids+")";
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return 1;
            }catch (Exception e){
                logger.error("Exception in method changeDocumentOwner. SQL query: "+stringQuery+", Request params:"+ request.toString(), e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
}
