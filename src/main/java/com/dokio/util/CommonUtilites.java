/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.util;
import com.dokio.repository.Exceptions.CantSetHistoryCauseNegativeSumException;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.*;
import java.math.BigDecimal;
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


    public Boolean addDocumentHistory(String docAlias, Long companyId, Long objectId, String docTableName, Long docId, BigDecimal summChange) throws Exception {
        // docAlias - alias таблицы объекта, по которому идет запись. Данная таблица хранит историю изменений по этому объекту. Может быть: cagent, payment_account и др (см. VALID_TABLE_NAMES)
        // objectId - id объекта, к которому относится изменение. Например, id контрагента в случае docAlias='cagent', или кассы предприятия в случае docAlias='boxoffice'
        // docTableName - таблица документа, который влияет на сумму (из которого производится запись) - например shipment для отгрузки
        // docId - id документа, из которого производится запись (в таблице docTableName)
        // summChange - сумма, на которую изменится значение в истории. Примеры:
        // - из кассы изъяли 100 р. - summChange = -100.00)
        // - произвели отгрузку на 200 р. - summChange для контрагента  = -200 р. (Отрицательный баланс - Нам должны)
        // - произвели приёмку на 300 р. - summChange для контрагента  = +300 р. (Положительный баланс - Мы должны)
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        BigDecimal summBefore = getSummFromHistory(docAlias, companyId, objectId);
        // если документ не относится к тем, для которых разрешена отрицательная сумма, и сумма отрицательная
        // (например, в кассе проедприятия, или на расчетном счету не может быть отрицательная сумма), ...
        if(!NEGATIVE_ALLOWED_TABLE_NAMES.contains(docAlias) && summChange.add(summBefore).compareTo(new BigDecimal(0)) < 0)
            throw new CantSetHistoryCauseNegativeSumException();// то кидаем исключение 'Невозможно записать отрицательную сумму', чтобы произошла отмена транзакции
        if(     securityRepository.companyBelongsToMyMastersAccount(companyId) &&
                !Objects.isNull(summBefore) &&
                VALID_TABLE_NAMES.contains(docAlias) &&
                VALID_TABLE_NAMES.contains(docTableName)) {
            String stringQuery;
            stringQuery = "" +
                    " insert into history_" + docAlias + "_summ (" +
                    " master_id," +
                    " company_id," +
                    " date_time_created," +
                    " object_id," +
                    " doc_table_name," +
                    " doc_id," +
                    " summ_before," +
                    " summ_change," +
                    " summ_result" +
                    ") values (" +
                    myMasterId + ", " +
                    companyId + ", " +
                    "now()," +
                    objectId + ", " +
                    "'"+docTableName+"', " + // тут не используем setParameter, т.к. выше уже проверили эти таблицы на валидность
                    docId + ", " +
                    summBefore + ", " +
                    summChange + ", " +
                    summChange.add(summBefore) +
                    ")";
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

    public BigDecimal getSummFromHistory(String objectName, Long companyId, Long objectId) {
        if(VALID_TABLE_NAMES.contains(objectName)) {
            String stringQuery =
                    " select summ_result from history_"+objectName+"_summ where " +
                    " company_id= " + companyId +
                    " and object_id= " + objectId +
                    " order by id desc limit 1";
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

}
