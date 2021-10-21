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
import com.dokio.message.response.additional.LinkedDocsJSON;
import com.dokio.message.response.additional.LinkedDocsLinksJSON;
import com.dokio.message.response.additional.LinkedDocsSchemeJSON;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class LinkedDocsUtilites {

    Logger logger = Logger.getLogger("CommonUtilites");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;

    private static final Set VALID_TABLENAMES
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("customers_orders", "acceptance", "return", "returnsup", "shipment", "retail_sales", "products", "inventory", "writeoff", "posting", "moving")
            .collect(Collectors.toCollection(HashSet::new)));

    private static final Set DOCS_WITH_PRODUCT_SUMPRICE // таблицы документов, у которых (в их table_prduct) есть колонка product_sumprice, по которой можно посчитать сумму стоимости товаров в отдельном документе
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("acceptance", "return", "returnsup", "shipment", "retail_sales", "writeoff", "posting", "moving", "customers_orders", "inventory")
            .collect(Collectors.toCollection(HashSet::new)));

    // Если у документа linked_doc_name с id = linked_doc_id есть группа связанных документов (т.е. linked_docs_group_id в его таблице != null)
    // то возвращаем id этой группы, иначе:
    // 1. Создаём новую группу (createLinkedGruoup)
    // 2. Прописываем ее id в таблице документа (setLinkedGroup)
    // 3. Возвращаем её id
//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long getOrCreateAndGetGroupId(Long linked_doc_id, String linked_doc_name, Long companyId, Long masterId) {

        Long result = getLinkedGroupId(masterId, linked_doc_name, linked_doc_id);

        if (!Objects.isNull(result)) {

            // если документ не состоит в группе связанных документов
            if (result == 0L) {

                // создаём эту группу
                Long groupId = createLinkedGruoup(companyId, masterId);

                if (!Objects.isNull(groupId)) {

                    //прописываем id группы в этом документе
                    if (setLinkedGroup(linked_doc_name, groupId, linked_doc_id, masterId)) {

                        return groupId;

                    } else return null; //ошибка при назначении id группы документу

                } else return null; // ошибка при создании группы

                // документ уже состоит в группе связанных.
            } else return result; // возвращаем id группы

        } else return null;//ошибка при проверке на наличие у документа id группы
    }

    // Если у документа уже есть группа - возвращаем ее id, нет - 0, ошибка - null
    private Long getLinkedGroupId(Long masterId, String docTable, Long docId) {

        String stringQuery = "select linked_docs_group_id from " + docTable + " where id = " + docId + " and master_id=" + masterId;

        if (!VALID_TABLENAMES.contains(docTable)) {
            throw new IllegalArgumentException("Недопустимые параметры запроса в isDocHaveLinkedGroup");
        }

        try {

            Query query = entityManager.createNativeQuery(stringQuery);
            Object obj = query.getSingleResult();

            if (Objects.isNull(obj)) {
                return 0L;
            } else {
                return Long.valueOf(obj.toString());
            }

        } catch (Exception e) {
            logger.error("Exception in method getLinkedGroupId. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // создаёт новую группу для связанных документов, возвращает её id
    private Long createLinkedGruoup(Long companyId, Long masterId) {
        Long newDockId;
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        String stringQuery = " insert into linked_docs_groups (" +
                " master_id," + //мастер-аккаунт
                " company_id," + //предприятие, для которого создается документ
                " date_time_created" + //дата и время создания
                ") values (" +
                masterId + ", " +//мастер-аккаунт
                companyId + ", " +//предприятие, для которого создается документ
                "to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')" +//дата и время создания
                ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery = "select id from linked_docs_groups where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and master_id=" + masterId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            newDockId = Long.valueOf(query2.getSingleResult().toString());
            return newDockId;
        } catch (Exception e) {
            logger.error("Exception in method createLinkedGruoup on inserting into linked_docs_groups. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // Назначает для документа группу связанных документов
    private Boolean setLinkedGroup(String docTable, Long groupId, Long docId, Long masterId) {
        String stringQuery = "update " + docTable + " set linked_docs_group_id =" + groupId + " where id = " + docId + " and master_id = " + masterId;

        if (!VALID_TABLENAMES.contains(docTable)) {
            throw new IllegalArgumentException("Недопустимые параметры запроса в setLinkedGroup");
        }

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method setLinkedGroup on updating " + docTable + ". SQL query:" + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // linked_doc_id - id документа, из которого создавали другой документ (например, Инвентаризация)
    // created_doc_id - id созданного документа (например, Списание)
    // linkedDocsGroupId - id группы связанных документов, в которую помещаем эти 2 документа
    // parent_uid - UUID родительского документа (это не обязательно документ, из которого создавали, т.к. можно создать из дочернего родительский
    // child_uid - UUID дочернего документа (это не обязательно будет созданный документ. Например при создании из Отгрузки Счета покупателю - последний будет родительским (parent_uid и createdDocId)
    // linked_doc_name - имя таблицы документа, из которого создавали другой документ (например, inventory)
    // created_doc_name - имя таблицы созданного документа (например, writeoff)
    public Boolean addDocsToGroupAndLinkDocs(Long linked_doc_id, Long created_doc_id, Long linkedDocsGroupId, String parent_uid, String child_uid, String linked_doc_name, String created_doc_name, Long companyId, Long masterId) {

        // Добавляем оба документа в группу связанных документов. Если прошло успешно
        if (addDocsToGroup(linked_doc_id, created_doc_id, linkedDocsGroupId, parent_uid, child_uid, linked_doc_name, created_doc_name, companyId, masterId)) {
            // ... то залинкуем их
            return addLinksBetweenLinkedDocs(linkedDocsGroupId, parent_uid, child_uid, companyId, masterId);
        } else return false;
    }


    // добавляем в группу связанных документов документ, из которого создавали (если он еще не добавлен), и созданный документ
    private Boolean addDocsToGroup(Long linked_doc_id, Long created_doc_id, Long linkedDocsGroupId, String parent_uid, String child_uid, String linked_doc_name, String created_table_name, Long companyId, Long masterId) {

        try {

            if (!VALID_TABLENAMES.contains(linked_doc_name)) {
                throw new IllegalArgumentException("Недопустимые параметры запроса в addDocsToGroup");
            }

            String stringQuery = " insert into linked_docs (" +
                    " master_id, " +
                    " company_id, " +
                    " group_id, " +
                    " doc_id, " +
                    " doc_uid, " +
                    " tablename, " +
                    linked_doc_name + "_id" +
                    ") values (" +
                    masterId + ", " +
                    companyId + ", " +
                    linkedDocsGroupId + ", " +
                    linked_doc_id + ", " +
                    ":parent_uid " + ", " +
                    "'" + linked_doc_name + "', " +
                    linked_doc_id + ")" +
                    "ON CONFLICT ON CONSTRAINT linked_docs_uq DO NOTHING";//значит он уже есть в данной группе

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("parent_uid", (parent_uid == null ? "" : parent_uid));
            query.executeUpdate();

            stringQuery = " insert into linked_docs (" +
                    " master_id, " +
                    " company_id, " +
                    " group_id, " +
                    " doc_id, " +
                    " doc_uid, " +
                    " tablename, " +
                    created_table_name + "_id" +
                    ") values (" +
                    masterId + ", " +
                    companyId + ", " +
                    linkedDocsGroupId + ", " +
                    created_doc_id + ", " +
                    ":child_uid " + ", " +
                    "'" + created_table_name + "', " +
                    created_doc_id + ")" +
                    "ON CONFLICT ON CONSTRAINT linked_docs_uq DO NOTHING";

            Query query2 = entityManager.createNativeQuery(stringQuery);
            query2.setParameter("child_uid", (child_uid == null ? "" : child_uid));
            query2.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addDocsToGroup on inserting into linked_docs.", e);
            e.printStackTrace();
            return false;
        }
    }

    //создание собственно связей между связанными документами
    private Boolean addLinksBetweenLinkedDocs(Long linkedDocsGroupId, String parent_uid, String child_uid, Long companyId, Long masterId) {

        String stringQuery = " insert into linked_docs_links (" +
                " master_id, " +
                " company_id, " +
                " group_id, " +
                " parent_uid, " +
                " child_uid " +
                ") values (" +
                masterId + ", " +
                companyId + ", " +
                linkedDocsGroupId + ", " +
                ":parent_uid " + ", " +
                ":child_uid)" +
                "ON CONFLICT DO NOTHING";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("parent_uid", (parent_uid == null ? "" : parent_uid));
            query.setParameter("child_uid", (child_uid == null ? "" : child_uid));
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addLinksBetweenLinkedDocs on inserting into linked_docs_links.", e);
            e.printStackTrace();
            return false;
        }
    }


    public LinkedDocsSchemeJSON getLinkedDocsScheme(String uid) {

        LinkedDocsSchemeJSON linkedDocsScheme = new LinkedDocsSchemeJSON();
//
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String shemeText;
        Integer count = 0; //кол-во докуменов в группе

        //узнаем id группы связанных документов по UID одного из документов
        Long groupId = getGroupIdByUid(uid, myMasterId);
        if (!Objects.isNull(groupId) && groupId > 0) { //null - ошибка, 0 - нет результата, т.е. у документа нет связанных с ним других документов, и он не состоит в группе связанных

            // по UID группы достанем начальную информацию по документам группы
            // (начальную - потому что она не будет включать внутреннюю информацию документа,
            // такую как наименование статуса или сумму по товарам, т.к. наименования таблиц
            // документов пока неизвестны (они в linked_docs, и нельзя делать конструкции типа " from ld.tablename").
            // Данную информацию придется получать в цикле по полученным данным)

            List<LinkedDocsJSON> baseList = getBaseInfoOfLinkedDocs(groupId);
            if (!Objects.isNull(baseList)) {

                //достаем полную информацию и инфо по связям документов
                List<LinkedDocsJSON> returnList = getFullInfoOfLinkedDocs(baseList);
                List<LinkedDocsLinksJSON> linksList = getLinks(groupId);
                if (!Objects.isNull(returnList) && !Objects.isNull(linksList)) {

                    LinkedDocsSchemeJSON sheme = new LinkedDocsSchemeJSON();

                    shemeText = "digraph {" +
                            "              rankdir=TB;" +
                            "              node [ shape=record;" +
                            "              margin=0;" +
                            "              fixedsize = true;" +
                            "              width=2.3;" +
                            "              height=1.3;" +
                            "              fontsize=12;" +
                            "              fontname=\"Arial\";" +
                            "              style=filled;" +
                            "              fillcolor=\"#ededed\";" +
                            "              color=\"#2b2a2a\";" +
                            "              ]; ";
                    // сборка массива информации по документам. В данном цикле необходимо получить массив из элементов вида
                    //                    <UUID документа> [
                    //                              color="black"
                    //                              fillcolor="#acee00";
                    //                              URL="ui/writeoffdock/113";
                    //                              label = "Оприходование\n №135\n000231\n23.05.2021\nПроведено: Да\nЗавершено";
                    //                              tooltip="Перейти в документ";
                    //                    ];
                    for (LinkedDocsJSON linkedDoc : returnList) {

                        //перед UID добавляю букву, т.к. на фронте Graphviz некорректно работает с наименованиями node-ов, которые начинаются на цифры

                        shemeText = shemeText + "a" + linkedDoc.getUid().replace("-", "") + " [";
                        shemeText = shemeText + "URL=\"ui/" + linkedDoc.getPagename() + "dock/" + linkedDoc.getId() + "\";";
                        if (uid.equals(linkedDoc.getUid()))
                            shemeText = shemeText + " fillcolor=\"#acee00\";"; // если это node документа, из которого запрашивали схему - окрасим ноду в другой цвет
                        shemeText = shemeText + "label=\"{" + linkedDoc.getName() + "|№" + linkedDoc.getDoc_number() + "\\n" + linkedDoc.getDate_time_created() + "\\n";
                        if (!Objects.isNull(linkedDoc.getSumprice()))
                            shemeText = shemeText + linkedDoc.getSumprice() + "\\n";
                        shemeText = shemeText + "Проведено: " + (linkedDoc.isIs_completed() ? "Да" : "Нет") + "\\n" + linkedDoc.getStatus() + "}\";";
                        shemeText = shemeText + "tooltip=\"Открыть документ в новом окне\";";
                        shemeText = shemeText + "] ";

                        count++;

                    }
                    // сборка массива информации по связям документов. В данном цикле необходимо получить массив вида
                    //                    <UUID документа> -> <UUID документа>;
                    //                    <UUID документа> -> <UUID документа>;
                    //                    <UUID документа> -> <UUID документа>;
                    for (LinkedDocsLinksJSON link : linksList) {

                        shemeText = shemeText + "a" + link.getUid_from().replace("-", "") + " -> " + "a" + link.getUid_to().replace("-", "") + ";";

                    }
                    shemeText = shemeText + "}";

                    sheme.setText(shemeText);

                    sheme.setCount(count);
                    return sheme;

                } else return null;

            } else return null;

        } else { // либо ошибка ( groupId = null),  либо нет связей (groupId = 0)

            if (Objects.isNull(groupId))
                return null; // ошибка
            else { // groupId=0, т.е. нет связей
                linkedDocsScheme.setErrorCode(0L);
                return linkedDocsScheme;
            }
        }
    }

    // возвращает лист линков по UID документов (от, к)
    private List<LinkedDocsLinksJSON> getLinks(Long groupId) {
        String stringQuery = "select " +
                "   ldl.parent_uid as parent_uid, " +
                "   ldl.child_uid as child_uid" +
                "   from " +
                "   linked_docs_links ldl" +
                "   where ldl.group_id = " + groupId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<LinkedDocsLinksJSON> returnList = new ArrayList<>();
            for (Object[] obj : queryList) {
                LinkedDocsLinksJSON doc = new LinkedDocsLinksJSON();
                doc.setUid_from((String) obj[0]);
                doc.setUid_to((String) obj[1]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getLinks. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    private List<LinkedDocsJSON> getFullInfoOfLinkedDocs(List<LinkedDocsJSON> linkedDocs) {
        try {
            List<LinkedDocsJSON> returnList = new ArrayList<>();

            LinkedDocsJSON doc;

            for (LinkedDocsJSON linkedDoc : linkedDocs) {

                doc = getFullInfoOfLinkedDoc(linkedDoc.getTablename(), linkedDoc.getId());

                doc.setId(linkedDoc.getId());
                doc.setTablename(linkedDoc.getTablename());
                doc.setGroup_id(linkedDoc.getGroup_id());
                doc.setUid(linkedDoc.getUid());
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFullInfoOfLinkedDocs.", e);
            return null;
        }
    }

    private LinkedDocsJSON getFullInfoOfLinkedDoc(String tablename, Long id) {

        String myTimeZone = userRepository.getUserTimeZone();

        String stringQuery = "select " +
                "   d.doc_number as doc_number, " +
                "   to_char(d.date_time_created at time zone '" + myTimeZone + "', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                "   (select ds.doc_name_ru from documents ds where ds.table_name = '" + tablename + "') as doc_name," +
                "   coalesce(ssd.name,'-')," +
                (DOCS_WITH_PRODUCT_SUMPRICE.contains(tablename) ?
                        ("  coalesce((select sum(coalesce(product_sumprice,0)) from " + tablename + "_product where " + tablename + "_id=" + id + "),0)") : null) + " as sum_price," +
                "   coalesce(d.is_completed,false) as is_completed," +
                "   (select ds.page_name from documents ds where ds.table_name = '" + tablename + "') as page_name" +
                "   from " + tablename + " d" +
                "   left outer join sprav_status_dock ssd on d.status_id = ssd.id " +
                "   where " +
                "   d.id = " + id;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            LinkedDocsJSON returnObj = new LinkedDocsJSON();

            for (Object[] obj : queryList) {
                returnObj.setDoc_number(Long.parseLong(obj[0].toString()));
                returnObj.setDate_time_created((String) obj[1]);
                returnObj.setName((String) obj[2]);
                returnObj.setStatus((String) obj[3]);
                returnObj.setSumprice((BigDecimal) obj[4]);
                returnObj.setIs_completed((Boolean) obj[5]);
                returnObj.setName((String) obj[2]);
                returnObj.setPagename((String) obj[6]);
            }
            return returnObj;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getFullInfoOfLinkedDoc. stringQuery=" + stringQuery, e);
            return null;
        }
    }


    private List<LinkedDocsJSON> getBaseInfoOfLinkedDocs(Long groupId) {

        String stringQuery = "select " +
                "   ld.doc_id as doc_id, " +
                "   ld.tablename as tablename," +
                "   ld.group_id as group_id, " +
                "   ld.doc_uid as doc_uid" +
                "   from " +
                "   linked_docs ld" +
                "   where ld.group_id = " + groupId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            List<LinkedDocsJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                LinkedDocsJSON doc = new LinkedDocsJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setTablename((String) obj[1]);
                doc.setGroup_id(Long.parseLong(obj[2].toString()));
                doc.setUid((String) obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getBaseInfoOfLinkedDocs. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }


    private Long getGroupIdByUid(String uid, Long myMasterId) {

        String stringQuery = "select group_id from linked_docs where doc_uid=:uid and master_id=" + myMasterId;

        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("uid", uid);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
            logger.error("NoResultException in method getGroupIdByUid. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getGroupIdByUid. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // возвращает List документов, которые не могут быть удалены по причине наличия у них связанных с ними дочерних документов
    public List<LinkedDocsJSON> checkDocHasLinkedChilds(String ids, String docTableName) {
        if (!VALID_TABLENAMES.contains(docTableName)) {
            throw new IllegalArgumentException("Недопустимые параметры запроса в checkDocHasLinkedChilds");
        }
        List<LinkedDocsJSON> docs = getSetDocUidsByIds(ids, docTableName);
        if (!Objects.isNull(docs) && docs.size() > 0) {
            Set<String> uids = new HashSet<>();
            //из списка документов собрали сет UID'ов
            for (LinkedDocsJSON doc : docs) {
                uids.add(doc.getUid());
            }

            //сейчас нужно понять, какие из UID являются родительскими по отношению к другим документам

            String stringQuery = "select " +
                    "   ld.parent_uid as parent_uid, " +
                    "   ld.child_uid as child_uid " +
                    "   from linked_docs_links ld" +
                    "   where ld.parent_uid in ('" + StringUtils.join(uids, "','") + "')";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                Set<String> parentUidsList = new HashSet<>();
                for (Object[] obj : queryList) {
                    parentUidsList.add((String) obj[0]);
                }

                List<LinkedDocsJSON> returnList = new ArrayList<>();
                //если в присланном списке документов есть те, которые являются родительскими, нужно вернуть инфо по ним ( id, номер док-та, uid)
                if (parentUidsList.size() > 0) {

                    for (LinkedDocsJSON doc : docs) {
                        if (parentUidsList.contains(doc.getUid()))
                            returnList.add(doc);
                    }
                    return returnList;
                } else
                    return returnList; // возвращаем пустой список, значит у ни один из присланных id документов не является родительским
            } catch (Exception e) {
                logger.error("Exception in method getSetDocUidsByIds. Sql: " + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    // отдает информацию о документах таблицы docTableName по их id
    private List<LinkedDocsJSON> getSetDocUidsByIds(String ids, String docTableName) {

        String stringQuery = "select " +
                "   ld.id as id, " +
                "   ld.uid as uid, " +
                "   ld.doc_number as doc_number " +
                "   from " + docTableName + " ld" +
                "   where ld.id in (" + ids + ")";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            List<LinkedDocsJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                LinkedDocsJSON doc = new LinkedDocsJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setUid((String) obj[1]);
                doc.setDoc_number(Long.parseLong(obj[2].toString()));
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            logger.error("Exception in method getSetDocUidsByIds. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //удаляет документы из группы связанных документов (удаляя его из linked_docs и linked_docs_links). Необходимо при удалении документа
    @SuppressWarnings("Duplicates")
    public Boolean deleteFromLinkedDocs(String ids, String docTableName) {
        if (!VALID_TABLENAMES.contains(docTableName)) {
            throw new IllegalArgumentException("Недопустимые параметры запроса в deleteFromLinkedDocs");
        }
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        // сначала проверим, не имеет ли какой-либо из документов связанных с ним дочерних документов
        List<LinkedDocsJSON> checkChilds = checkDocHasLinkedChilds(ids, docTableName);

        if (!Objects.isNull(checkChilds) && checkChilds.size() == 0) { //если нет ошибки и если связи с дочерними документами отсутствуют

            //соберем информацию о документах
            List<LinkedDocsJSON> docs = getSetDocUidsByIds(ids, docTableName);
            if (!Objects.isNull(docs) && docs.size() > 0) { //если собрали
                Set<String> uids = new HashSet<>();
                //из этой информации выделим сет UID'ов
                for (LinkedDocsJSON doc : docs) {
                    uids.add(doc.getUid());
                }
                if(uids.size()>0) {//если в информации были uid
                    //удаляем все ссылки на документы и сами документы из группы связанных
                    if (deleteLinksByDocUid(uids, myMasterId) && deleteFromLinkedDocsByDocUid(uids, myMasterId)) {
                        return true;
                    } else return false;
                } else return true;//uid'ов нет, но это не повод для ошибки
            } else return false;
        } else return false;
    }

    // удаляет все ссылки на документы
    private Boolean deleteLinksByDocUid(Set<String> uids, Long myMasterId) {

        String stringQuery = "delete from linked_docs_links where master_id=" + myMasterId + " and child_uid in ('" + StringUtils.join(uids, "','") + "')";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteLinksByDocUid. Sql: " + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // удаляет документы из группы связанных документов
    private Boolean deleteFromLinkedDocsByDocUid(Set<String> uids, Long myMasterId) {
        String stringQuery = "delete from linked_docs where master_id=" + myMasterId + " and doc_uid in ('" + StringUtils.join(uids, "','") + "')";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method deleteFromLinkedDocsByDocUid. Sql: " + stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

}
