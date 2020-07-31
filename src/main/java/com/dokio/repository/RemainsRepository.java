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
package com.dokio.repository;

import com.dokio.message.request.*;
import com.dokio.message.response.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Repository
public class RemainsRepository {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;

    // Инициализация логера
    //private static final Logger log = Logger.getLogger(RemainsRepository.class);

    @Transactional
    @SuppressWarnings("Duplicates")
    public RemainsJSON getProductsTable(int result,
                                        int offset,
                                        int offsetreal,
                                        String searchString,
                                        String sortColumn,
                                        String sortAsc,
                                        int companyId,
                                        int categoryId,
                                        int cagentId,
                                        Long departmentId,
                                        String departmentsIdsList,
                                        Set<Integer> filterOptionsIds) {
        if (securityRepositoryJPA.userHasPermissions_OR(18L, "235,236,237")) {
            String stringQuery;
            List<Integer> pagesList;// информация для пагинации. Первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
            Boolean hideNotBuyingProducts = filterOptionsIds.contains(3);// скрывать товары, у которых в карточке стоит флаг "Товар не закупается"
            Boolean hideNotSellingProducts = filterOptionsIds.contains(4);// скрывать снятые с продажи товары (у которых в карточке стоит флаг "Снято с продажи")
            Boolean showNotAviable = filterOptionsIds.contains(0);// отображать товары с оценкой остатков "Отсутствует"
            Boolean showLess = filterOptionsIds.contains(1);// отображать товары с оценкой остатков "Мало"
            Boolean showMany = filterOptionsIds.contains(2);// отображать товары с оценкой остатков "Достаточно"
            Integer depthsCount = departmentsIdsList.split(",").length;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                stringQuery =
                    "select  p.id as id, " +
                    "           p.name as name, " +
                    "           p.article as article, " +
                    "           coalesce(pg.name,'') as productgroup, " +
                    "           coalesce(p.not_buy,false) as not_buy, " +
                    "           coalesce(p.not_sell,false) as not_sell, " +
                    "           p.description as description, " +
                    " coalesce((select " +
                    " sum(coalesce(quantity,0)) " +
                    " from " +
                    " product_quantity " +
                    " where " +
                    " product_id = p.id " +

                    (departmentId > 0L ? " and department_id = " + departmentId : " and department_id in (" + departmentsIdsList + ") ") + "),0) ,";

            if (departmentId > 0L) { // "Разные" -1 / "Не установлено" 0
                stringQuery = stringQuery +
                        " coalesce((select coalesce(min_quantity,0) from product_remains where product_id = p.id and department_id=" + departmentId + "),0) a ";
            } else {
                stringQuery = stringQuery +
                    " CASE WHEN " + // одинаковые значения (т.е. было например 30,30,30, сгруппировали - стало 30 - везде одинаково - можно вывести 30)
                    "   ((select count (*) from (select coalesce(min_quantity,0) as mq " +
                    "   from product_remains where product_id = p.id and department_id in (" + departmentsIdsList + ") " +
                    "   group by mq)f) =1 " +
                    " and " +
                    "   (select count (*) from (select coalesce(min_quantity,0) as mq " +
                    "   from product_remains where product_id = p.id and department_id in (" + departmentsIdsList + ")" +
                    "   )f) =" + depthsCount +
                    ") " +
                    " THEN " +
                    "   coalesce((select coalesce(min_quantity,0) as mq1 " +
                    "   from product_remains where product_id = p.id and department_id in (" + departmentsIdsList + ") " +
                    "   group by mq1),0)" +

                    " WHEN " +  // когда в "таблице с записями о мин. кол-ве aka product_remains" по данному товару только один или несколько 0
                                // или мин. остатков нет вообще ни по одному из отделений (например, товар только что создан)
                    "(select coalesce(sum(min_quantity),0) from product_remains where product_id = p.id and department_id in (" + departmentsIdsList + ") "+
                    ")=0"+
                    " THEN 0" +
                    " ELSE -1 " +
                    " END as min_remains ";
        }

            stringQuery = stringQuery +
                    ", CASE " +
// кол-во товара задано не для всех отделений (т.е. не для всех отделений есть строка в product_quantity)
                    " WHEN    " +
                    " (select count (*) from product_quantity where product_id = p.id and department_id in (" + (departmentId > 0L ? departmentId : departmentsIdsList) + "))" +
                    " < " +(departmentId > 0L ? 1 : depthsCount)+
                    " THEN 0  " +

                    " WHEN    " +
                    " (select count(*) from product_quantity " +
                    "       where product_id = p.id and " +
                    "       department_id in (" + (departmentId > 0L ? departmentId : departmentsIdsList) + ") and quantity=0)>0 " +
                    " THEN 0  " +
// мин. остаток задан для всех отделений и в каждом из отделений кол-во товара больше, чем минимальный остаток в этом отделении
                    " WHEN " +
                    "(" +
                    " (select count (*) " +
                    " from " +
                    " product_remains pr1 " +
                    " where " +
                    " pr1.product_id = p.id and  " +
                    " pr1.department_id in (" + (departmentId > 0L ? departmentId : departmentsIdsList) + ") and " +
                    " (" +
                    "   (coalesce(pr1.min_quantity,0) < (select coalesce(quantity,0) from product_quantity where product_id = p.id and department_id=pr1.department_id))" +
                    " )" +
                    " )=" + (departmentId > 0L ? 1 : depthsCount) +
                    " ) " +
                    " THEN 2  " +
// мин. остаток задан не для всех отделений, и в каждом отделении где он задан кол-во товара больше 0
                    " WHEN    " +
                    " ( " +
                    " select count (*) " +
                    " from " +
                    " product_remains pr2 " +
                    " where " +
                    "   pr2.product_id = p.id and " +
                    "   pr2.department_id in (" + (departmentId > 0L ? departmentId : departmentsIdsList) + ") " +
                    "   )<" + (departmentId > 0L ? 1 : depthsCount) +// мин. остаток задан не для всех отделений
                    " and " +
                    "   (" +
                    "       select  count(*)" +
                    "       from product_quantity " +
                    "       where " +
                    "       product_id = p.id and " +
                    "       department_id in (" + (departmentId > 0L ? departmentId : departmentsIdsList) + ") and " +
                    "       quantity>0 " +
                    "   )=" + (departmentId > 0L ? 1 : depthsCount) +
                    " THEN 2  " +
                    " ELSE 1   " +
                    " END as estimate";

            stringQuery = stringQuery +
                    "           from products p " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                    "           and p.ppr_id in (1,2) " +
                    (categoryId != 0 ? " and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id=" + categoryId + ") " : "");

            if (!securityRepositoryJPA.userHasPermissions_OR(18L, "235")) //Если нет прав по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (cagentId > 0) {
                stringQuery = stringQuery + " and exists (select * from product_cagents pc where pc.product_id=p.id and pc.cagent_id=" + cagentId + ")";
            }
            //если не: ( [v] Скрывать не закупаемые товары и товар не закупаемый) и ( [v] Скрывать снятые с продажи и товар или услуга снят с продажи)
            if (hideNotBuyingProducts) stringQuery = stringQuery + " and coalesce(p.not_buy,false)  is false ";
            if (hideNotSellingProducts) stringQuery = stringQuery + " and coalesce(p.not_sell,false) is false ";

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or " +
                        "upper(p.article) like upper('%" + searchString + "%') or " +
                        "upper(p.description) like upper('%" + searchString + "%') or " +
                        "to_char(p.product_code_free,'fm0000000000') like upper('%" + searchString + "%') or " +
                        "upper(pg.name) like upper('%" + searchString + "%')" + ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by p.name asc";
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();//получили полный список товаров в лист

            List<RemainsTableJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                RemainsTableJSON doc = new RemainsTableJSON();

                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setName((String) obj[1]);
                doc.setArticle((String) obj[2]);
                doc.setProductgroup((String) obj[3]);
                doc.setNot_buy((Boolean) obj[4]);
                doc.setNot_sell((Boolean) obj[5]);
                doc.setDescription((String) obj[6]);
                doc.setQuantity((BigDecimal) obj[7]);
                doc.setMin_quantity((BigDecimal) obj[8]);
                doc.setEstimate_quantity((Integer) obj[9]);

                if ((showNotAviable && doc.getEstimate_quantity() == 0) ||
                        (showLess && doc.getEstimate_quantity() == 1) ||
                        (showMany && doc.getEstimate_quantity() == 2)) {
                    returnList.add(doc);
                }
            }
            if (sortColumn.equals("p.name")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_NAME_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_NAME_DESC);
                }
            }
            if (sortColumn.equals("description")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_DESCRIPTION_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_DESCRIPTION_DESC);
                }
            }
            if (sortColumn.equals("p.article")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_ARTICLE_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_ARTICLE_DESC);
                }
            }
            if (sortColumn.equals("productgroup")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_PRODUCTGROUP_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_PRODUCTGROUP_DESC);
                }
            }
            if (sortColumn.equals("quantity")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_QUANTITY_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_QUANTITY_DESC);
                }
            }
            if (sortColumn.equals("min_quantity")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_MINQUANTITY_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_MINQUANTITY_DESC);
                }
            }
            if (sortColumn.equals("estimate_quantity")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_ESTIMATEQUANTITY_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_ESTIMATEQUANTITY_DESC);
                }
            }
            if (sortColumn.equals("not_buy")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_NOTBUY_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_NOTBUY_DESC);
                }
            }
            if (sortColumn.equals("not_sell")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_NOTSELL_ASC);
                } else {
                    returnList.sort(RemainsTableJSON.COMPARE_BY_NOTSELL_DESC);
                }
            }

            //вычисление пагинации
            int returnListSize = returnList.size();
            pagesList = getPagesList(result, offset, returnListSize);

            //обрезаем лишнее
            returnList = returnList.subList(offsetreal, (offsetreal + result) > returnListSize ? returnListSize : (offsetreal + result));

            RemainsJSON remainsTableForm = new RemainsJSON();
            remainsTableForm.setTable(returnList);//проверка на IndexOutOfBoundsException
            remainsTableForm.setReceivedPagesList(pagesList);
            return remainsTableForm;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    private List<Integer> getPagesList(int result, int offset, int size) {
        int listsize;//количество страниц пагинации
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        pagenum = offset + 1;
        if ((size % result) == 0) {//общее количество выборки делим на количество записей на странице
            listsize = size / result;//если делится без остатка
        } else {
            listsize = (size / result) + 1;
        }
        int maxPagenumInBegin;//
        List<Integer> pageList = new ArrayList<Integer>();//список, в котором первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
        pageList.add(size);
        pageList.add(pagenum);
        pageList.add(listsize);

        if (listsize <= 5) {
            maxPagenumInBegin = listsize;//
        } else {
            maxPagenumInBegin = 5;
        }
        if (pagenum >= 3) {
            if ((pagenum == listsize) || (pagenum + 1) == listsize) {
                for (int i = (pagenum - (4 - (listsize - pagenum))); i <= pagenum - 3; i++) {
                    if (i > 0) {
                        pageList.add(i);  //создается список пагинации за - 4 шага до номера страницы (для конца списка пагинации)
                    }
                }
            }
            for (int i = (pagenum - 2); i <= pagenum; i++) {
                pageList.add(i);  //создается список пагинации за -2 шага до номера страницы
            }
            if ((pagenum + 2) <= listsize) {
                for (int i = (pagenum + 1); i <= (pagenum + 2); i++) {
                    pageList.add(i);  //создается список пагинации  на +2 шага от номера страницы
                }
            } else {
                if (pagenum < listsize) {
                    for (int i = (pagenum + (listsize - pagenum)); i <= listsize; i++) {
                        pageList.add(i);  //создается список пагинации от номера страницы до конца
                    }
                }
            }
        } else {//номер страницы меньше 3
            for (int i = 1; i <= pagenum; i++) {
                pageList.add(i);  //создается список пагинации от 1 до номера страницы
            }
            for (int i = (pagenum + 1); i <= maxPagenumInBegin; i++) {
                pageList.add(i);  //создаются дополнительные номера пагинации, но не более 5 в сумме
            }
        }

        return pageList;
    }

    @Transactional
    public boolean saveRemains(RemainsForm request) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        //Если есть право на "Установка остатков по всем предприятиям", ИЛИ
        if (canSetRemainsOfAllTheseDepartments(request, myMasterId)) {
                if (request.getDepartmentId() == 0) //если 0 значит были выбраны все доступные отделения, и нужно установить остатки по всем отделениям во всех товарах.
                {
                    for (Long dep : request.getDepartmentsIds()) {
                        for (Long prod : request.getProductsIds()) {
                            if (!upsertRemain(dep, prod, myMasterId, request.getMin_quantity())) {
                                break;
                            }
                        }
                    }
                } else {// если не 0 значит было выбрано какое то определенное отделение, и нужно установить остатки только в нём
                    for (Long prod : request.getProductsIds()) {
                        if (!upsertRemain(request.getDepartmentId(), prod, myMasterId, request.getMin_quantity())) {
                            break;
                        }
                    }
                }
                return true;
        } else return false;
    }


    @SuppressWarnings("Duplicates")
    private boolean upsertRemain(Long departmentId, Long productId, Long myMasterId, BigDecimal min_quantity) {
        String stringQuery;
        stringQuery =
                "   insert into product_remains (" +
                        "   product_id," +
                        "   department_id," +
                        "   min_quantity," +
                        "   master_id " +
                        "   ) values (" +
                        "(select id from products where id=" + productId + " and master_id=" + myMasterId + "), " +//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        "(select id from departments where id=" + departmentId + " and master_id=" + myMasterId + "), " +//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        min_quantity.toString() + "," +
                        myMasterId + ")"+
                    " ON CONFLICT ON CONSTRAINT product_remains_uq " +
                    " DO update set " +
                        " department_id = (select id from departments where id=" + departmentId + " and master_id=" + myMasterId + "),"+
                        " product_id = (select id from products where id=" + productId + " and master_id=" + myMasterId + "), " +
                        " min_quantity = "+ min_quantity.toString() + "," +
                        " master_id = "+ myMasterId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    private boolean canSetRemainsOfAllTheseDepartments(RemainsForm request, Long myMasterId) {
        if (securityRepositoryJPA.userHasPermissions_OR(18L, "232,233,234")) {
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            Integer REQUEST_COMPANY_ID = Integer.parseInt(request.getCompanyId());

            return
                    //если "Установка остатков по всем предприятиям" и предприятие владельца аккаунта  или
                    (securityRepositoryJPA.userHasPermissions_OR(18L, "232") && securityRepositoryJPA.isItAllMyMastersDocuments("companies", request.getCompanyId())) ||
                            //на своё предприятие и оно по id действительно моё  или
                            (securityRepositoryJPA.userHasPermissions_OR(18L, "233") && REQUEST_COMPANY_ID.equals(MY_COMPANY_ID)) ||
                            //на свои отделения и они мои (или оно моё, в зависимости что пришло)
                            (securityRepositoryJPA.userHasPermissions_OR(18L, "234") && REQUEST_COMPANY_ID.equals(MY_COMPANY_ID) &&
                                    (request.getDepartmentId() == 0 ?
                                            securityRepositoryJPA.isItAllMyDepartments(request.getDepartmentsIds()) :
                                            securityRepositoryJPA.isItMyDepartment(request.getDepartmentId())
                                    )
                            );
        } else return false;
    }
}