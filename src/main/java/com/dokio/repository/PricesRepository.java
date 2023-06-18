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

package com.dokio.repository;
import com.dokio.message.request.*;
import com.dokio.message.response.*;
//import com.dokio.message.response.additional.ProductPricesJSON;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class PricesRepository {

    Logger logger = Logger.getLogger(PricesRepository.class);

    @PersistenceContext
    private EntityManager entityManager;
//    @Autowired
//    private EntityManagerFactory emf;
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
    private CommonUtilites commonUtilites;
//    @Autowired
//    private UserDetailsServiceImpl userService;

    // Инициализация логера
    private static final Logger log = Logger.getLogger(PricesRepository.class);

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("p.name","description","p.article","productgroup","price","not_buy","not_sell")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

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
            priceTypesIdsList=priceTypesIdsList.replaceAll("[^0-9\\,]", "");
            Integer pricesCount = priceTypesIdsList.split(",").length;
            // getProductMinRemains( productId, departmentId, departmentsIdsList, myMasterId)
            stringQuery = "select  p.id as id, " +
                    "           p.name as name, " +
                    "           coalesce(p.article,'') as article, " +
                    "           coalesce(pg.name,'') as productgroup, " +
                    "           coalesce(p.not_buy,false) as not_buy, " +
                    "           coalesce(p.not_sell,false) as not_sell, " +
                    "           coalesce(p.label_description,'') as description, " +
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
                    "           and coalesce(p.is_deleted,false) = false" +
            (categoryId!=0?" and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id="+categoryId+") ":"");
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
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')) or "+
                        "upper(p.article) like upper(CONCAT('%',:sg,'%')) or "+
                        "upper(p.label_description) like upper(CONCAT('%',:sg,'%')) or "+
                        "(upper(CONCAT('%',:sg,'%')) in (select upper(value) from product_barcodes where product_id=p.id))  or " +
                        "to_char(p.product_code_free,'fm0000000000') like upper(CONCAT('%',:sg,'%')) or "+
                        "upper(pg.name) like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by p.name asc";

            try{

                if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
                    throw new IllegalArgumentException("Invalid query parameters");

                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

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
                pricesTableJSON.setTable(returnList.subList(offsetreal > returnListSize?0:offsetreal,(offsetreal+result)>returnListSize?returnListSize:(offsetreal+result)));//проверка на IndexOutOfBoundsException
                pricesTableJSON.setReceivedPagesList(pagesList);
                return pricesTableJSON;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getPricesTable. SQL query:" + stringQuery, e);
                return null;
            }



        } else return null;
    }




    @SuppressWarnings("Duplicates")
    private List<Integer> getPagesList(int result,int offset, int size){
        int listsize;//количество страниц пагинации
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        if(result*offset>=size) offset=0; // если произведение кол-ва товаров на странице и страницы больше найденного кол-ва товаров - сбрасываем страницу в 0
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

//    private boolean isElementsOfArrayAreEachEquals(BigDecimal[] array){//равны ли все элементы массива между собой?
//        BigDecimal firstElement=array[0];
//        return Arrays.asList(array).parallelStream().allMatch(t -> (t.compareTo(firstElement)==0));
//    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, RuntimeException.class})
    public boolean savePrices(PricesForm request) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long userId = userRepository.getUserId();
        //Если есть право на "Установка цен по всем предприятиям", ИЛИ
        try{

            commonUtilites.idBelongsMyMaster("companies", request.getCompanyId(), myMasterId);

            if(canSetPricesOfAllTheseDepartments(request, myMasterId))
            {
                if (clearProductPrices(request, myMasterId))
                {
                    if (request.getPriceTypeId()==0) //если 0 значит были выбраны все типы цен, и нужно установить цены по всем типам цен во всех товарах.
                    {
                        for (Long priceType : request.getPriceTypesIds()) {
                            commonUtilites.idBelongsMyMaster("sprav_type_prices", priceType, myMasterId);
                            for (Long product : request.getProductsIds()) {
                                commonUtilites.idBelongsMyMaster("products", product, myMasterId);
                                if (!insertPrice(priceType, product, myMasterId, request.getCompanyId(), request.getPriceValue())) {
                                    break;
                                }
                                if (!insertPriceHistory(priceType, product, myMasterId, request.getCompanyId(), request.getPriceValue(),userId)) {
                                    break;
                                }
                            }
                        }
                    }else{// если не 0 значит был выбран какой-то определенный тип цены, и нужно установить цены только по этому типу
                        commonUtilites.idBelongsMyMaster("sprav_type_prices", request.getPriceTypeId(), myMasterId);
                        for (Long product : request.getProductsIds()) {
                            commonUtilites.idBelongsMyMaster("products", product, myMasterId);
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
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            log.error("ERROR: ", e);
            return false;
        }

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
                        "       and product_id in (select id from products where id in ("+request.getProductsIdsList().replaceAll("[^0-9\\,]", "")+") and master_id=" +myMasterId+") "+//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        "       and master_id="+myMasterId;
        try{
            commonUtilites.idBelongsMyMaster("sprav_type_prices", (request.getPriceTypeId()==0)?null:request.getPriceTypeId(), myMasterId);
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
