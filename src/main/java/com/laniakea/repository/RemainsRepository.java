package com.laniakea.repository;

import com.laniakea.message.request.*;
import com.laniakea.message.response.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class RemainsRepository {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
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
    @Autowired
    private UserDetailsServiceImpl userService;

    // Инициализация логера
    private static final Logger log = Logger.getLogger(RemainsRepository.class);
    private BigDecimal minQuantity = new BigDecimal("0");
    private List<DepartmentQuantity> departmentQuantities = new ArrayList<>();
    private Set<DepartmentQuantity> finalDepartmentQuantities = new HashSet<>();

    //private Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();

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
                                        Set<Integer>filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(18L, "235,236,237"))
        {
            String stringQuery;
            List<Integer> pagesList;// информация для пагинации. Первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
            Boolean hideNotBuyingProducts = filterOptionsIds.contains(3);// скрывать товары, у которых в карточке стоит флаг "Товар не закупается"
            Boolean hideNotSellingProducts = filterOptionsIds.contains(4);// скрывать снятые с продажи товары (у которых в карточке стоит флаг "Снято с продажи")
            Boolean showNotAviable = filterOptionsIds.contains(0);// отображать товары с оценкой остатков "Отсутствует"
            Boolean showLess = filterOptionsIds.contains(1);// отображать товары с оценкой остатков "Мало"
            Boolean showMany = filterOptionsIds.contains(2);// отображать товары с оценкой остатков "Достаточно"
            Boolean notBuy = false;
            Boolean notSell = false;
            Integer depthsCount=departmentsIdsList.split(",").length;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
           // getProductMinRemains( productId, departmentId, departmentsIdsList, myMasterId)
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
                    " product_id = p.id "+
                    (departmentId>0L?" and department_id = "+departmentId:" and department_id in ("+departmentsIdsList+") ")+"),0) as quantity, ";

            if(departmentId>0L) {
                stringQuery = stringQuery +
                                "coalesce((select coalesce(min_quantity,0) from product_remains where product_id = p.id and department_id="+departmentId+"),0) a";
            } else{
                stringQuery = stringQuery +
                    "CASE WHEN " +
                    "   ((select count (*) from (select coalesce(min_quantity,0) as mq " +
                    "   from product_remains where product_id = p.id and department_id in ("+departmentsIdsList+") " +
                    "   group by mq)f) <2) " +
                    " THEN " +
                    "   coalesce((select coalesce(min_quantity,0) as mq1 " +
                    "   from product_remains where product_id = p.id and department_id in ("+departmentsIdsList+") " +
                    "   group by mq1),0)" +
                    " ELSE -1 " +
                    " END as min_remains";
                    }

            stringQuery = stringQuery +
                    "           from products p " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                    "           and p.ppr_id in (1,2) " +
                    (categoryId!=0?" and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id="+categoryId+") ":"");

//            //оценка количества в отделении (если запрошено конкретное отделение). Если запрошены все отделения - оценка качества вычисляется отдельно ниже ↓
//            if(departmentId>0L) {
//                stringQuery = stringQuery +
//                        "";
//            }


            if (!securityRepositoryJPA.userHasPermissions_OR(18L, "235")) //Если нет прав по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (cagentId > 0) {
                stringQuery = stringQuery + " and exists (select * from product_cagents pc where pc.product_id=p.id and pc.cagent_id="+cagentId+")";
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or "+
                        "upper(p.article) like upper('%" + searchString + "%') or "+
                        "upper(p.description) like upper('%" + searchString + "%') or "+
                        "to_char(p.product_code_free,'fm0000000000') like upper('%" + searchString + "%') or "+
                        "upper(pg.name) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by p.name asc";
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();//получили полный список товаров в лист

            List<RemainsTableJSON> returnList = new ArrayList<>();

            for(Object[] obj:queryList){

                notBuy =(Boolean) obj[4] ;
                notSell =(Boolean) obj[5] ;
                if(!(hideNotBuyingProducts && notBuy)&&!(hideNotSellingProducts && notSell))
                {//если не: ( [v] Скрывать не закупаемые товары и товар не закупаемый) и ( [v] Скрывать снятые с продажи и товар или услуга снят с продажи)
                    RemainsTableJSON doc=new RemainsTableJSON();
                    departmentQuantities.clear();
                    finalDepartmentQuantities.clear();

                    doc.setId(Long.parseLong (      obj[0].toString()));
                    doc.setName((String)            obj[1]);
                    doc.setArticle((String)         obj[2]);
                    doc.setProductgroup((String)    obj[3]);
                    doc.setNot_buy((Boolean)        obj[4]);
                    doc.setNot_sell((Boolean)       obj[5]);
                    doc.setDescription((String)     obj[6]);
                    doc.setQuantity((BigDecimal)    obj[7]);
                    doc.setMin_quantity((BigDecimal)obj[8]);
                    //если сортировка по минимальному кол-ву - вычисляем (остальные довычислим потом)
                    //if(sortColumn.equals("min_quantity")) doc.setMin_quantity(getProductMinRemains(Long.parseLong(obj[0].toString()), departmentId, departmentsIdsList, myMasterId));
//                    doc.setQuantity(getQuantity(Long.parseLong(obj[0].toString()), departmentId, departmentsIdsList));
                    //если сортировка по оценке кол-ва - вычисляем (остальные довычислим потом)

                    //Если сортировка по оценке количества, и запрошены все отделения (departmentId=0)
                    //
                    if(sortColumn.equals("estimate_quantity")) {
                        //нужно пробежаться по всему результату запроса и заполнить
                        //инфу по отделениям с их мин. остатками для текущего товара в departmentQuantities
                        fillMinRemains(doc.getId(), departmentId, departmentsIdsList, myMasterId );
                        fillQuantity(doc.getId(), departmentId, departmentsIdsList);

                        int estimateQuantity = doEstimateQuantity();
                        doc.setEstimate_quantity(estimateQuantity);
                    }

/*                    if(     (showNotAviable && estimateQuantity==0) ||
                            (showLess && estimateQuantity==1) ||
                            (showMany && estimateQuantity==2))
                    {*/
                        returnList.add(doc);
                    /*}*/
                }
            }
            if(sortColumn.equals("p.name")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_NAME_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_NAME_DESC);}}
            if(sortColumn.equals("description")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_DESCRIPTION_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_DESCRIPTION_DESC);}}
            if(sortColumn.equals("p.article")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_ARTICLE_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_ARTICLE_DESC);}}
            if(sortColumn.equals("productgroup")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_PRODUCTGROUP_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_PRODUCTGROUP_DESC);}}
            if(sortColumn.equals("quantity")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_QUANTITY_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_QUANTITY_DESC);}}
            if(sortColumn.equals("min_quantity")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_MINQUANTITY_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_MINQUANTITY_DESC);}}
            if(sortColumn.equals("estimate_quantity")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_ESTIMATEQUANTITY_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_ESTIMATEQUANTITY_DESC);}}
            if(sortColumn.equals("not_buy")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_NOTBUY_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_NOTBUY_DESC);}}
            if(sortColumn.equals("not_sell")){if(sortAsc.equals("asc")){returnList.sort(RemainsTableJSON.COMPARE_BY_NOTSELL_ASC);}else{returnList.sort(RemainsTableJSON.COMPARE_BY_NOTSELL_DESC);}}

            //вычисление пагинации
            int returnListSize=returnList.size();
            pagesList=getPagesList(result,offset, returnListSize);

            //обрезаем лишнее
            returnList=returnList.subList(offsetreal,(offsetreal+result)>returnListSize?returnListSize:(offsetreal+result));

            /*Сейчас у того что осталось от обрезки, нужно довычислить оценку количества, если по ней не было сортировки,
            и следовательно, не имело смысла вычислять ее для всего количества товаров.*/
            if(!sortColumn.equals("estimate_quantity")) {
                for(RemainsTableJSON obj:returnList){
                    departmentQuantities.clear();
                    finalDepartmentQuantities.clear();
                    fillMinRemains(obj.getId(), departmentId, departmentsIdsList, myMasterId );
                    fillQuantity(obj.getId(), departmentId, departmentsIdsList);
                    int estimateQuantity = doEstimateQuantity();
                    obj.setEstimate_quantity(estimateQuantity);
                }
            }

            RemainsJSON remainsTableForm=new RemainsJSON();
            remainsTableForm.setTable(returnList);//проверка на IndexOutOfBoundsException
            remainsTableForm.setReceivedPagesList(pagesList);
            return remainsTableForm;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    private List<Integer> getPagesList(int result,int offset, int size){
        int listsize;//количество страниц пагинации
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        pagenum = offset + 1;
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

    @Transactional
    public boolean saveRemains(RemainsForm request) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

                //Если есть право на "Установка остатков по всем предприятиям", ИЛИ
        if(canSetRemainsOfAllTheseDepartments(request, myMasterId))
        {
            if (clearProductRemains(request, myMasterId))
            {
                if (request.getDepartmentId()==0) //если 0 значит были выбраны все доступные отделения, и нужно установить остатки по всем отделениям во всех товарах.
                {
                    for (Long dep : request.getDepartmentsIds()) {
                        for (Long prod : request.getProductsIds()) {
                            if (!insertRemain(dep, prod, myMasterId, request.getMin_quantity())) {
                                break;
                            }
                        }
                    }
                }else{// если не 0 значит было выбрано какое то определенное отделение, и нужно установить остатки только в нём
                    for (Long prod : request.getProductsIds()) {
                        if (!insertRemain(request.getDepartmentId(), prod, myMasterId, request.getMin_quantity())) {
                            break;
                        }
                    }
                }
                return true;
            } else return false;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private boolean clearProductRemains(RemainsForm request,Long myMasterId) {
        String stringQuery;
        stringQuery=
                "       delete from product_remains " +
                "       where" +
                ((request.getDepartmentId()==0)?(" department_id in ("+request.getDepartmentsIdsList()+")"):(" department_id=("+request.getDepartmentId()+")")) +
                "       and product_id in (select id from products where id in ("+request.getProductsIdsList()+") and master_id=" +myMasterId+") "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                "       and master_id="+myMasterId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    private boolean insertRemain(Long departmentId, Long productId, Long myMasterId, BigDecimal min_quantity){
        String stringQuery;
        stringQuery=
                "   insert into product_remains (" +
                "   product_id," +
                "   department_id," +
                "   min_quantity," +
                "   master_id " +
                "   ) values (" +
                "(select id from products where id="+productId +" and master_id="+myMasterId+"), "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                "(select id from departments where id="+departmentId +" and master_id="+myMasterId+"), "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                min_quantity.toString() + "," +
                myMasterId + ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    private boolean canSetRemainsOfAllTheseDepartments(RemainsForm request, Long myMasterId){
        if(securityRepositoryJPA.userHasPermissions_OR(18L,"232,233,234")){
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
        }else return false;
    }

    @SuppressWarnings("Duplicates")
    private void fillMinRemains(Long productId, Long departmentId, String departmentsIdsList, Long myMasterId){

        String stringQuery;
        stringQuery=
                " select " +
                        " coalesce(min_quantity,0), department_id " +
                        " from " +
                        " product_remains " +
                        " where " +
                        " product_id = " + productId +
                        (departmentId>0L?" and department_id = "+departmentId:" and department_id in ("+departmentsIdsList+") ")+
                        " and master_id = " + myMasterId +
                        " and min_quantity != 0";// если мин.остаток 0 - считаем что его нет. Это нужно
        // чтобы если, например, вернёт для всего 2х отделений 0 и null, то это определится как "Разные" мин.остатки, а должно как 0.

        Query query = entityManager.createNativeQuery(stringQuery);
        // запрашивается список объектов содержащих отделение и его мин. остатки
        List<Object[]> queryList = query.getResultList();

        if (queryList.size() > 0) // если для товара установлены 1 или несколько мин.остатков
        {
                for (Object[] obj : queryList) {
                    DepartmentQuantity dmq = new DepartmentQuantity();
                    dmq.setMin_quantity((BigDecimal) obj[0]);
                    dmq.setDepartmentId((Long.parseLong(obj[1].toString())));
                    departmentQuantities.add(dmq); // копим инфу по отделениям с их мин. остатками для текущего товара
                }
        }
    }

    @SuppressWarnings("Duplicates")
    private BigDecimal getProductMinRemains(Long productId, Long departmentId, String departmentsIdsList, Long myMasterId){

        /*
         * Если запрос идет по конкретному отделению (т.е. departmentId != 0), то запрос вернет только 1 строку с этим отделением, и проблем нет, но!
         * Если запрашиваются "Все доступные отделения" (departmentId=0) то нужно вывести:
         * -если мин. остаток по всем отделениям одинаковый - этот мин. остаток
         * -если мин. остаток 0 или его нет - 0
         * -если мин. остаток разный (>0,0,null) то -1, что будет отображено в таблице как "Разные"
         * данная задача и решается в этом методе
         */


        String stringQuery;
        stringQuery=
                " select " +
                " coalesce(min_quantity,0), department_id " +
                " from " +
                " product_remains " +
                " where " +
                " product_id = " + productId +
                (departmentId>0L?" and department_id = "+departmentId:" and department_id in ("+departmentsIdsList+") ")+
                " and master_id = " + myMasterId +
                " and min_quantity != 0";// если мин.остаток 0 - считаем что его нет. Это нужно
        // чтобы если, например, вернёт для всего 2х отделений 0 и null, то это определится как "Разные" мин.остатки, а должно как 0.

        Query query = entityManager.createNativeQuery(stringQuery);
// запрашивается список объектов содержащих отделение и его мин. остатки
        List<Object[]> queryList = query.getResultList();

        if (queryList.size() > 0) // если для товара установлены 1 или несколько мин.остатков
        {   // если запрошены "Все доступные отделения", и количество установленных для товара мин.остатков равно общему количеству отделений
            // ИЛИ запрос по конкретному отделению
            if ((departmentId==0 && queryList.size() == departmentsIdsList.split(",").length)||departmentId>0)
            {
                List<BigDecimal> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    returnList.add((BigDecimal) obj[0]);// собираем мин. остатки по отделениям
                    DepartmentQuantity dmq = new DepartmentQuantity();
                    dmq.setMin_quantity((BigDecimal) obj[0]);
                    dmq.setDepartmentId((Long.parseLong(obj[1].toString())));
                    departmentQuantities.add(dmq); // копим инфу по отделениям с их мин. остатками для текущего товара
                }

                BigDecimal[] array = new BigDecimal[returnList.size()];
                returnList.toArray(array);

                if (isElementsOfArrayAreEachEquals(array)) {// если все мин. остатки отделений равны между собой
                    return array[0];// возвращаем первый попавшийся остаток в качестве минимального остатка для всех отделений
                } else {
                    return new BigDecimal(-1);//если не равны между совой - возвращаем -1, в таблице будет отображаться как "Разные"
                }
            } else return new BigDecimal(-1);//если не равны между собой - возвращаем -1, в таблице будет отображаться как "Разные"
        } else return new BigDecimal("0");// если цены для товара не заданы - возвращается 0
}

    @SuppressWarnings("Duplicates")
    private void fillQuantity(Long productId, Long departmentId, String departmentsIdsList){
        BigDecimal returnQuantity = new BigDecimal("0");
        if (departmentId >0){//если отделение выбрано конкретное, то по нему придет ид больше 0, и нужно лишь запросить актуальное количество товара в данном отделении
            returnQuantity=getProductQuantity(productId, departmentId);// запрашиваем количество товара в этом отделении
            DepartmentQuantity dmq = new DepartmentQuantity();
            dmq.setQuantity(returnQuantity);
            dmq.setDepartmentId(departmentId);
            departmentQuantities.add(dmq);// копим инфу по отделениям с их мин. остатками для текущего товара
//            return returnQuantity;
        } else {//если пришел 0 т.е. выбраны все доступные пользователю отделения (прилетели в departmentsIdsList в виде строки 1,3,4,9)
                //то нужно запрашивать количество для каждого отделения, и всё суммировать
            List<Long> depIds = Stream.of(departmentsIdsList.split(",")).map(Long::valueOf).collect(Collectors.toList());
            BigDecimal depQuantity;
            for(Long depId:depIds){
                depQuantity=getProductQuantity(productId,depId);

                DepartmentQuantity dmq = new DepartmentQuantity();
                dmq.setQuantity(depQuantity);
                dmq.setDepartmentId(depId);
                departmentQuantities.add(dmq);// копим инфу по отделениям с их мин. остатками для текущего товара
                if(depQuantity==null){depQuantity = new BigDecimal("0");}
                returnQuantity=returnQuantity.add(depQuantity);
            }
//            return returnQuantity;
        }
    }

    @SuppressWarnings("Duplicates")
    private BigDecimal getProductQuantity(Long productId, Long departmentId){
        String stringQuery;
        stringQuery=
                        " select " +
                        " quantity " +
                        " from " +
                        " product_quantity " +
                        " where " +
                        " product_id = " + productId +
                        " and department_id = "+departmentId;
        List returnList = entityManager.createNativeQuery(stringQuery).getResultList();
        if(returnList.size()>0) {
            return (BigDecimal) returnList.get(0);}
            else{ return new BigDecimal("0");}
    }

//    private Set<DepartmentQuantity> assemblySetDepartmentQuantity(List<DepartmentQuantity> dqList){
      private void assemblySetDepartmentQuantity(){
        /*
        в метод принимается List с накопленной информацией по отделениям с минимальным количеством и наличием товара.
        но т.к. в разных методах добавлялось разная информация (то по мин. кол-ву, то по наличию), то она разбросана по всему List
        в виде (пример для 2х отделений):
        ____________________________________________
        | department_id | min_quantity |  quantity |
        |__________________________________________|
        |       1       |      30      |    null   |
        |       2       |      25      |    null   |
        |       1       |     null     |     40    |
        |       2       |     null     |     10    |
        |_______________|______________|___________|

        объединив её в данном методе, получим Set вида:
        ____________________________________________
        | department_id | min_quantity |  quantity |
        |__________________________________________|
        |       1       |      30      |     40    |
        |       2       |      25      |     10    |
        |_______________|______________|___________|

        */


          for (DepartmentQuantity obj_1:departmentQuantities){
              Long currentDepId=obj_1.getDepartmentId();
              DepartmentQuantity newDepartmentQuantityObject = new DepartmentQuantity();
              newDepartmentQuantityObject.setDepartmentId(currentDepId);
              if (obj_1.getMin_quantity() != null) {
                  newDepartmentQuantityObject.setMin_quantity(obj_1.getMin_quantity());
              }
              if (obj_1.getQuantity() != null) {
                  newDepartmentQuantityObject.setQuantity(obj_1.getQuantity());
              }
              for (DepartmentQuantity obj_2:departmentQuantities){
                  if(obj_2.getDepartmentId().equals(currentDepId) && obj_2.getMin_quantity()!= null && obj_1.getMin_quantity()== null){
                      newDepartmentQuantityObject.setMin_quantity(obj_2.getMin_quantity());
                  }
                  if(obj_2.getDepartmentId().equals(currentDepId) && obj_2.getQuantity()!= null && obj_1.getQuantity()== null){
                      newDepartmentQuantityObject.setQuantity(obj_2.getQuantity());
                  }
              }
              //если где-то остались null - заменяем на нули (такое например может быть если у товара ещё не установлены мин. остатки, но есть количество)
              if(newDepartmentQuantityObject.getQuantity()==null) newDepartmentQuantityObject.setQuantity(new BigDecimal("0"));
              if(newDepartmentQuantityObject.getMin_quantity()==null) newDepartmentQuantityObject.setMin_quantity(new BigDecimal("0"));
              finalDepartmentQuantities.add(newDepartmentQuantityObject);
          }
    }


     private boolean isElementsOfArrayAreEachEquals(BigDecimal[] array){
            BigDecimal firstElement=array[0];
            return Arrays.asList(array).parallelStream().allMatch(t -> (t.compareTo(firstElement)==0));
     }

     private int doEstimateQuantity(){
        /* Оценка количества товара в отделении или в отделениях (если приходит запрос на все отделения, доступные пользователю)
        *
        * */
        int grade=2;
         assemblySetDepartmentQuantity();
         for (DepartmentQuantity obj:finalDepartmentQuantities) {
             //если количество null или 0 - остальное можно не проверять, сразу возвращаем минимальную оценку - 0
             if(obj.getQuantity()==null || isEqual(obj.getQuantity(), new BigDecimal("0")))
             {
                 return 0; // КРАСНЫЙ индикатор наличия в отчете
             }else{
                 //если меньше или равно минимальному количеству, установленному для какого-либо отделения
                 if (obj.getQuantity().compareTo(obj.getMin_quantity()) < 0 || isEqual(obj.getQuantity(), obj.getMin_quantity())) {
                     grade = 1;// ОРАНЖЕВЫЙ индикатор наличия в отчете
                 }
                 //если больше минимального количества, установленного для какого-либо отделения, и оценки 1 ещё не было
                 if (obj.getQuantity().compareTo(obj.getMin_quantity()) > 0 && grade !=1) {
                     grade = 2;// ЗЕЛЕНЫЙ индикатор наличия в отчете
                 }
             }
         }
         return grade;

     }

     private boolean isEqual(BigDecimal pNumber1, BigDecimal pNumber2)
     {
         if ( pNumber1 == null )
         {
             return pNumber2 == null;
         }
         if ( pNumber2 == null)
             return false;
         return pNumber1.compareTo(pNumber2)==0;
     }
}

 class DepartmentQuantity {
     Long       departmentId;
     BigDecimal quantity;
     BigDecimal min_quantity;

     public Long getDepartmentId() {
         return departmentId;
     }

     public void setDepartmentId(Long departmentId) {
         this.departmentId = departmentId;
     }

     public BigDecimal getQuantity() {
         return quantity;
     }

     public void setQuantity(BigDecimal quantity) {
         this.quantity = quantity;
     }

     public BigDecimal getMin_quantity() {
         return min_quantity;
     }

     public void setMin_quantity(BigDecimal min_quantity) {
         this.min_quantity = min_quantity;
     }

     @Override
     public boolean equals(Object o) {
         if (this == o) return true;

         if (o == null || getClass() != o.getClass()) return false;

         DepartmentQuantity that = (DepartmentQuantity) o;

         return new EqualsBuilder()
                 .append(departmentId, that.departmentId)
                 .append(quantity, that.quantity)
                 .append(min_quantity, that.min_quantity)
                 .isEquals();
     }

     @Override
     public int hashCode() {
         return new HashCodeBuilder(17, 37)
                 .append(departmentId)
                 .append(quantity)
                 .append(min_quantity)
                 .toHashCode();
     }
 }