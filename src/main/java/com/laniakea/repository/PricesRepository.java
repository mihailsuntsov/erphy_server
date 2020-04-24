package com.laniakea.repository;
import com.laniakea.message.request.*;
import com.laniakea.message.response.*;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Repository
public class PricesRepository {
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
    private static final Logger log = Logger.getLogger(PricesRepository.class);

    @Transactional
    @SuppressWarnings("Duplicates")
    public PricesJSON getPricesTable(int result,
                                        int offset,
                                        int offsetreal,
                                        String searchString,
                                        String sortColumn,
                                        String sortAsc,
                                        Long companyId,
                                        Long categoryId,
                                        Long cagentId,
                                        Long priceTypeId,
                                        String priceTypesIdsList,
                                        Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(19L, "242,243"))
        {
            String stringQuery;
            List<Integer> pagesList;// информация для пагинации. Первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
            Boolean hideNotBuyingProducts = filterOptionsIds.contains(3);// скрывать товары, у которых в карточке стоит флаг "Товар не закупается"
            Boolean hideNotSellingProducts = filterOptionsIds.contains(4);// скрывать снятые с продажи товары (у которых в карточке стоит флаг "Снято с продажи")
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Boolean notBuy = false;
            Boolean notSell = false;
            Integer pricesCount = priceTypesIdsList.split(",").length;
            // getProductMinRemains( productId, departmentId, departmentsIdsList, myMasterId)
            stringQuery = "select  p.id as id, " +
                    "           p.name as name, " +
                    "           p.article as article, " +
                    "           coalesce(pg.name,'') as productgroup, " +
                    "           coalesce(p.not_buy,false) as not_buy, " +
                    "           coalesce(p.not_sell,false) as not_sell, " +
                    "           p.description as description, " +
                    "           p.ppr_id as ppr, ";






            if (priceTypeId > 0L) { // "Разные" -1 / "Не установлено" 0
                stringQuery = stringQuery +
                        " coalesce((select coalesce(price_value,0) from product_prices where product_id = p.id and price_type_id=" + priceTypeId + "),0) a ";
            } else {
                stringQuery = stringQuery +
                        " CASE WHEN " +// одинаковые значения (т.е. было например 30,30,30, сгруппировали - стало 30 - везде одинаково - можно вывести 30)
                        "   ((select count (*) from (select coalesce(price_value,0) as mq " +
                        "   from product_prices where product_id = p.id and price_type_id in (" + priceTypesIdsList + ") " +
                        "   group by mq)f) =1 " +
                        " and " +
                        "   ( select count (*) from (select coalesce(price_value,0) as mq " +
                        "   from product_prices where product_id = p.id and price_type_id in (" + priceTypesIdsList + ")" +
                        "   )ff) =" + pricesCount +
                        ") " +
                        " THEN " +
                        "   coalesce((select coalesce(price_value,0) as mq1 " +
                        "   from product_prices where product_id = p.id and price_type_id in (" + priceTypesIdsList + ") " +
                        "   group by mq1),0)" +
//                        " THEN 1.000" +

                        " WHEN " + // когда в "таблице с записями о цене aka product_prices" по данному товару только один или несколько 0
                                    // или цен нет вообще ни по одному из типов цен (например, товар только что создан)
                        "(select coalesce(sum(price_value),0) from product_prices where product_id = p.id and price_type_id in (" + priceTypesIdsList + ") "+
                        ")=0"+
                        " THEN 0" +
                        " ELSE -1 " +
                        " END as price ";
            }



            stringQuery = stringQuery +
                    "           from products p " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                 (categoryId!=0?" and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id="+categoryId+") ":"");
//            stringQuery = stringQuery + " and p.id in (33,128,149)";
            if (!securityRepositoryJPA.userHasPermissions_OR(19L, "242")) //Если нет прав по всем предприятиям"
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

            List<Object[]> queryList = query.getResultList();
            List<PricesTableJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){

                notBuy  =(Boolean) obj[4] ;
                notSell =(Boolean) obj[5] ;
                if(!(hideNotBuyingProducts && notBuy)&&!(hideNotSellingProducts && notSell))
                {//если не: ( [v] Скрывать не закупаемые товары и товар не закупаемый) и ( [v] Скрывать снятые с продажи и товар или услуга снят с продажи)
                    PricesTableJSON doc=new PricesTableJSON();

                    doc.setId(Long.parseLong (      obj[0].toString()));
                    doc.setName((String)            obj[1]);
                    doc.setArticle((String)         obj[2]);
                    doc.setProductgroup((String)    obj[3]);
                    doc.setNot_buy((Boolean)        obj[4]);
                    doc.setNot_sell((Boolean)       obj[5]);
                    doc.setDescription((String)     obj[6]);
                    doc.setPpr_id((Integer)         obj[7]);
                    doc.setPrice((BigDecimal) obj[8]);
                    //doc.setPrice(getProductPrices(Long.parseLong(obj[0].toString()), priceTypeId, priceTypesIdsList, myMasterId));

                    returnList.add(doc);
                }
    }

            if(sortColumn.equals("p.name")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_NAME_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_NAME_DESC);}}
            if(sortColumn.equals("description")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_DESCRIPTION_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_DESCRIPTION_DESC);}}
            if(sortColumn.equals("p.article")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_ARTICLE_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_ARTICLE_DESC);}}
            if(sortColumn.equals("productgroup")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_PRODUCTGROUP_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_PRODUCTGROUP_DESC);}}
            if(sortColumn.equals("price")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_PRICE_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_PRICE_DESC);}}
            if(sortColumn.equals("not_buy")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_NOTBUY_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_NOTBUY_DESC);}}
            if(sortColumn.equals("not_sell")){if(sortAsc.equals("asc")){returnList.sort(PricesTableJSON.COMPARE_BY_NOTSELL_ASC);}else{returnList.sort(PricesTableJSON.COMPARE_BY_NOTSELL_DESC);}}
            int returnListSize=returnList.size();
            pagesList=getPagesList(result,offset, returnListSize);
            PricesJSON pricesTableJSON=new PricesJSON();
            pricesTableJSON.setTable(returnList.subList(offsetreal,(offsetreal+result)>returnListSize?returnListSize:(offsetreal+result)));//проверка на IndexOutOfBoundsException
            pricesTableJSON.setReceivedPagesList(pagesList);
            return pricesTableJSON;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    private BigDecimal getProductPrices(Long productId, Long priceTypeId, String priceTypesIdsList, Long myMasterId){
/*
* Если запрос идет по конкретному типу цен (т.е. priceTypeId != 0), то запрос вернет только 1 строку с этим типом цены, и проблем нет, но!
* Если запрашиваются "Все типы цен" (priceTypeId=0) то нужно вывести:
* -если цена по всем типам цен одинаковая - эту цену
* -если цена 0 или цены нет - 0
* -если цена разная (>0,0,null) то -1, что будет отображено в таблице как "Разные"
* данная задача и решается в этом методе
*/

        String stringQuery;
        stringQuery=
                " select " +
                        " coalesce(price_value,0), price_type_id " +
                        " from " +
                        " product_prices " +
                        " where " +
                        " product_id = " + productId +
                        (priceTypeId>0L?" and price_type_id = "+priceTypeId:" and price_type_id in ("+priceTypesIdsList+") ")+
                        " and master_id = " + myMasterId +
                        " and price_value != 0";// если цена 0 - считаем что цены нет. Это нужно
                        // чтобы если, например, вернёт для всего 2х типов цен 0 и null, то это определится как "Разные" цены, а должно как 0.

        Query query = entityManager.createNativeQuery(stringQuery);
// запрашивается список объектов содержащих тип цены и ее значение
        List<Object[]> queryList = query.getResultList();

        if (queryList.size() > 0) // если для товара установлены 1 или несколько типов цены
        {// если запрошены "Все типы цен", и количество установленных для товара цен равно общему количеств типов цен
         // ИЛИ запрос по конкретному типу цен
            if ((priceTypeId==0 && queryList.size() == priceTypesIdsList.split(",").length)||priceTypeId>0)
            {
                List<BigDecimal> returnList = new ArrayList<>();//коллеция, куда будут собираться значения всех типов цен товара для дальнейшего их анализа
                for (Object[] obj : queryList) {
                    returnList.add((BigDecimal) obj[0]);// собираем мин. остатки по отделениям
//                    ProductPrice dmq = new ProductPrice();
//                    dmq.setPriceValue((BigDecimal) obj[0]);
//                    dmq.setPriceTypeId((Long.parseLong(obj[1].toString())));
//                    productPrices.add(dmq); // копим инфу по ценам для текущего товара
                }

                BigDecimal[] array = new BigDecimal[returnList.size()];
                returnList.toArray(array);

                if (isElementsOfArrayAreEachEquals(array)) {// если во всех типах цен цены равны между собой
                    return array[0];// возвращаем первую попавшуюся цену в качестве цены для всех типов цен
                } else {
                    return new BigDecimal(-1);//если не равны между собой - возвращаем -1, в таблице будет отображаться как "Разные"
                }
            } else return new BigDecimal(-1);//если не равны между собой - возвращаем -1, в таблице будет отображаться как "Разные"
        } else return new BigDecimal("0");// если цены для товара не заданы - возвращается 0
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

    private boolean isElementsOfArrayAreEachEquals(BigDecimal[] array){//равны ли все элементы массива между собой?
        BigDecimal firstElement=array[0];
        return Arrays.asList(array).parallelStream().allMatch(t -> (t.compareTo(firstElement)==0));
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean savePrices(PricesForm request) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long userId = userRepository.getUserId();
        //Если есть право на "Установка остатков по всем предприятиям", ИЛИ
        if(canSetPricesOfAllTheseDepartments(request, myMasterId))
        {
            if (clearProductPrices(request, myMasterId))
            {
                if (request.getPriceTypeId()==0) //если 0 значит были выбраны все типы цен, и нужно установить цены по всем типам цен во всех товарах.
                {
                    for (Long priceType : request.getPriceTypesIds()) {
                        for (Long product : request.getProductsIds()) {
                            if (!insertPrice(priceType, product, myMasterId, request.getCompanyId(), request.getPriceValue())) {
                                break;
                            }
                            if (!insertPriceHistory(priceType, product, myMasterId, request.getCompanyId(), request.getPriceValue(),userId)) {
                                break;
                            }
                        }
                    }
                }else{// если не 0 значит был выбран какой-то определенный тип цены, и нужно установить цены только по этому типу
                    for (Long product : request.getProductsIds()) {
                        if (!insertPrice(request.getPriceTypeId(), product, myMasterId, request.getCompanyId(), request.getPriceValue())) {
                            break;
                        }
                        if (!insertPriceHistory(request.getPriceTypeId(), product, myMasterId, request.getCompanyId(), request.getPriceValue(),userId)) {
                            break;
                        }
                    }
                }
                return true;
            } else return false;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private boolean clearProductPrices(PricesForm request,Long myMasterId) {
        String stringQuery;
        stringQuery=
                "       delete from product_prices " +
                        "       where" +
                        ((request.getPriceTypeId()==0)?(
                                " price_type_id in " +
                                "(select id from sprav_type_prices where master_id="+myMasterId+" and company_id="+request.getCompanyId()+" and coalesce(is_archive, false) is false)"
                        ):(" price_type_id=("+request.getPriceTypeId()+")")) +
                        "       and product_id in (select id from products where id in ("+request.getProductsIdsList()+") and master_id=" +myMasterId+") "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        "       and master_id="+myMasterId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR: ", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    private boolean insertPrice(Long priceTypeId, Long productId, Long myMasterId, Long companyId, BigDecimal priceValue){
        String stringQuery;
        stringQuery=
                "   insert into product_prices (" +
                        "   product_id," +
                        "   price_type_id," +
                        "   price_value," +
                        "   master_id, " +
                        "   company_id " +
                        "   ) values (" +
                        "(select id from products where id="+productId +" and master_id="+myMasterId+"), "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        priceTypeId+", "+
                        priceValue.toString() + "," +
                        myMasterId + ", "+ companyId + ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR: ", e);
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean insertPriceHistory(Long priceTypeId, Long productId, Long myMasterId, Long companyId, BigDecimal priceValue, Long changerId){
        String stringQuery;
        stringQuery=
                "   insert into product_prices_history (" +
                        "   product_id," +
                        "   price_type_id," +
                        "   price_value," +
                        "   master_id, " +
                        "   changer_id," +
                        "   company_id, " +
                        "   date_time_created" +
                        "   ) values (" +
                        "(select id from products where id="+productId +" and master_id="+myMasterId+"), "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        priceTypeId+","+
                        priceValue.toString() + "," +
                        myMasterId + ","+
                        changerId + "," +
                        companyId + ","+
                        " now())";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR: ", e);
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private boolean canSetPricesOfAllTheseDepartments(PricesForm request, Long myMasterId){
        if(securityRepositoryJPA.userHasPermissions_OR(19L,"239,240")){
            Long MY_COMPANY_ID = Long.valueOf(userRepositoryJPA.getMyCompanyId());
            Long REQUEST_COMPANY_ID = request.getCompanyId();
            return
                    //если есть право "Установка цен по всем предприятиям" и предприятие владельца аккаунта или
                    (securityRepositoryJPA.userHasPermissions_OR(19L, "239") && securityRepositoryJPA.isItAllMyMastersDocuments("companies", request.getCompanyId().toString())) ||
                    //на своё предприятие и оно по id действительно моё  или
                    (securityRepositoryJPA.userHasPermissions_OR(19L, "240") && REQUEST_COMPANY_ID.equals(MY_COMPANY_ID));
        }else return false;
    }
}
