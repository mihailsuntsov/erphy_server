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
package com.dokio.repository.Reports;


import com.dokio.message.request.Reports.VolumesReportForm;
import com.dokio.message.response.Reports.VolumeSerie;
import com.dokio.message.response.Reports.VolumesReportJSON;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class VolumesRepository {

    Logger logger = Logger.getLogger("VolumesRepository");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ProductsRepositoryJPA productsRepository;
    @Autowired
    private CommonUtilites commonUtilites;

    // Отдает данные по виджету "Объемы"
    @SuppressWarnings("Duplicates")
    public List<VolumesReportJSON> getVolumesReportData(VolumesReportForm request) {
        String last_time_interval = "";
        String current_time_interval = ""; //текущий временной интервал (ВИ) (поле NAME у объекта VolumesReportJSON). При смене данного интервала VolumesReportJSON добавляется в сет, и формируется новый VolumesReportJSON
        List<VolumeSerie> seriesList = new ArrayList<>();//список значений за один ВИ (от 1 до n здначений на ВИ)
        Set<Long> childCategories = new HashSet<>();
        String stringQuery = "";
        boolean notAll=(!request.getAll()&&request.getReportOnIds().size()>0);//true - то только по выбранным (товарам или категориям) - в форме не выставлен флаг All и есть присланные id


        if(notAll && request.getWithSeparation()){ //если не "Все ..." и "С разбивкой"
            // в данном случае нужно копить разбитые категории/товары в цикле, на каждый временной интервал (ВИ)
            for(Long id : request.getReportOnIds()){
                if(id!=request.getReportOnIds().get(0)){//если это не 1й виток цикла - объединяем запросы
                    stringQuery += " union ";
                }
                stringQuery += "(select ";
                if (request.getReportOn().equals("products"))
                    stringQuery = stringQuery + " (select prd.name from products prd where prd.id="+id+")as name,";
                if (request.getReportOn().equals("categories"))
                    stringQuery = stringQuery + " (select cat.name from product_categories cat where cat.id="+id+") as name, ";
                stringQuery += getVolumesReportTimeInterval(request,false) + ", ";
                stringQuery += " SUM(ABS(rsp.product_count*rsp.product_price)) as summ, ";
                stringQuery +=   id + " as id_, ";
                stringQuery +=  getVolumesReportTimeInterval(request,true);
                stringQuery += " from retail_sales_product rsp";
                stringQuery += " INNER JOIN products p ON p.id=rsp.product_id";
                stringQuery += " INNER JOIN retail_sales rs ON rs.id=rsp.retail_sales_id";
                stringQuery += " where ";
                stringQuery = stringQuery + " rs.date_time_created >=to_timestamp('"+request.getDateFrom()+" 00:00:00','DD.MM.YYYY HH24:MI:SS')"+
                        " and rs.date_time_created <=to_timestamp('"+request.getDateTo()+" 23:59:59','DD.MM.YYYY HH24:MI:SS')";

                stringQuery += getVolumesReportDepAndEmpl(request);// фильтр по отделениям и сотрудникам

                if (request.getReportOn().equals("products"))
                    stringQuery +=  " and p.id = " + id ;
                if (request.getReportOn().equals("categories")) {
                    stringQuery +=  " and rsp.product_id in (select ppc.product_id from product_productcategories ppc where ppc.category_id ";
                    if(request.getIncludeChilds()){//если идет запрос "С подкатегориями"
                        //Для запроса "С подкатегориями" - нужно запросить все айдишники подкатегорий у присланных id родительских категорий
                        childCategories = productsRepository.getProductCategoryChildIds(id);
                        String childIds = commonUtilites.SetOfLongToString(childCategories, ",", "", "");
                        stringQuery +=  "in (" + id + (childIds.length()>0?",":"") + childIds + "))";
                    } else
                        stringQuery +=  " = " + id + ")";
                }
                stringQuery += " and rs.company_id="+request.getCompanyId();
                stringQuery += " group by time_interval, time_interval_sort)";
            }
            stringQuery += " order by time_interval_sort, name";

        } else { // все остальные случаи

            stringQuery = " select ";
            if (notAll) {
                stringQuery += " 'Выбранные' as name,";
            } else {
                stringQuery += "'Все' as name,";
            }

            stringQuery += getVolumesReportTimeInterval(request, false) + ", ";
            stringQuery += "           sum(ABS(rsp.product_count*rsp.product_price)) as summ";

            if (notAll) {//только по выбранным (товарам или категориям)
                stringQuery += ", 0 as id_ "; // чтобы группировалось только по ВИ, а все id категорий, которые в данном случае нули, схлопывались
            }
            stringQuery +=   ", " + getVolumesReportTimeInterval(request,true);
            stringQuery = stringQuery + "           from retail_sales_product rsp" +
                    "           INNER JOIN products p ON p.id=rsp.product_id" +
                    "           INNER JOIN retail_sales rs ON rs.id=rsp.retail_sales_id" +
                    "           where ";

            stringQuery = stringQuery + " rs.date_time_created >=to_timestamp('"+request.getDateFrom()+" 00:00:00','DD.MM.YYYY HH24:MI:SS')"+
                                    " and rs.date_time_created <=to_timestamp('"+request.getDateTo()+" 23:59:59','DD.MM.YYYY HH24:MI:SS')";

            stringQuery += getVolumesReportDepAndEmpl(request);// фильтр по отделениям и сотрудникам

            if (notAll) {//только по выбранным (товарам или категориям)
                String ids = commonUtilites.ListOfLongToString(request.getReportOnIds(), ",", "", "");
                if (request.getReportOn().equals("products"))
                    stringQuery = stringQuery + " and p.id in (" + ids + ")";
                if (request.getReportOn().equals("categories")) {
                    stringQuery = stringQuery + " and rsp.product_id in (select ppc.product_id from product_productcategories ppc where ppc.category_id ";
                    if (request.getIncludeChilds()) {//если идет запрос "С подкатегориями"

                        //Для запроса "С подкатегориями" - нужно запросить все айдишники подкатегорий у присланных id родительских категорий
                        childCategories = productsRepository.getProductCategoriesChildIds(request.getReportOnIds());
                        String childIds = commonUtilites.SetOfLongToString(childCategories, ",", "", "");
                        stringQuery = stringQuery + "in (" + ids + (childIds.length()>0?",":"") + childIds + "))";
                    } else
                        stringQuery = stringQuery + "in (" + ids + "))";
                }
            }
            stringQuery += " and rs.company_id="+request.getCompanyId();
            stringQuery = stringQuery + "           group by time_interval, time_interval_sort order by time_interval_sort";
            if (notAll) {
                stringQuery = stringQuery + ", name";
            }
        }


        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            List<VolumesReportJSON> returnList = new ArrayList<>();

            //если данные (строки "ВИ+имя+значение") есть
            if(!queryList.isEmpty()) {
                //бежим по строкам "ВИ+имя+значение"
                for (Object[] obj : queryList) {
                    current_time_interval = (String) obj[1];

                    //если название прошлого ВИ ещё не определено - значит это первый цикл.
                    //определяем на нём название прошлого ВИ (которые в данном случае будет равен текущему названию ВИ)
                    if (last_time_interval.isEmpty()) {
                        last_time_interval = current_time_interval;
                    }

                    // если текущее название ВИ не равно прошлому названию ВИ - значит мы не находимся на имя+значение (ИЗ) текущего ВИ
                    // значит начался новый временной интервал
                    if (!current_time_interval.equals(last_time_interval))
                    {
                        //Создаём объект ВИ
                        //В этот объект будет помещено название данного ВИ (например, "01.2021") и список "имя+значение" (ИЗ) за этот ВИ (Например сыр 100, колбаса 200)
                        VolumesReportJSON doc = new VolumesReportJSON();
                        //нужно записать прошедший, уже сформированный ВИ, содержащий набор ИЗ, в результирующий список
                        doc.setName(last_time_interval);
                        doc.setSeries(seriesList);
                        returnList.add(doc);

                        //обнуляем название старого ВИ до нового ВИ
                        last_time_interval = current_time_interval;
                        //сбрасываем список ИЗ, готовя его для нового ВИ
                        seriesList = new ArrayList<>();
                    }


                    // пополняем список ИЗ данного ВИ
                    if (notAll)
                    { //если переключатель "Все категории/товары" выключен
                        if (
                            //если нет сочетания "Отчёт по категориям + С подкатегориями"
                            !(request.getReportOn().equals("categories") && request.getIncludeChilds())) // - условие также подходит для товаров, с разбивкой и без
                        {
                            VolumeSerie serie = new VolumeSerie();
                            serie.setName((String) obj[0]);
                            serie.setValue((BigDecimal) obj[2]);
                            seriesList.add(serie);
                        } else {    //если есть Отчёт по "Категориям" и "С подкатегориями"
                            //тут может быть 2 варианта - С разбивкой и без
                            //если "С разбивкой" - вставлены реальные id, и нужно делать проверку на то, что id категоий входят в набор запрашиваемых id
                            //если "Без разбивки" - в SQL-запросе вставляем нули, и в этом случае проверка на то, что id категории входит в набор запрашиваемых id не будет проходить

                            // Если "С разбивкой"
                            if (request.getWithSeparation()) {

                                // только по присланным (т.е. запрашиваемым) категориям
                                if (request.getReportOnIds().contains(new Long(obj[3].toString()))) {
                                    VolumeSerie serie = new VolumeSerie();
                                    serie.setName((String) obj[0]);
                                    serie.setValue((BigDecimal) obj[2]);
                                    seriesList.add(serie);
                                } //если "С разбивкой", но id категории не из присланных - его не включаем, т.к. присланная уже содержит сумму объемов по всем ее подкатегориям
                            } //если "Без разбивки" -
                            else {
                                VolumeSerie serie = new VolumeSerie();
                                serie.setName((String) obj[0]);
                                serie.setValue((BigDecimal) obj[2]);
                                seriesList.add(serie);
                            }
                        }
                    } else {
                        VolumeSerie serie = new VolumeSerie();
                        serie.setName((String) obj[0]);
                        serie.setValue((BigDecimal) obj[2]);
                        seriesList.add(serie);
                    }


                }
                //тут нужно записать последний (или единственный) ВИ в returnList
                //нужно записать прошедший, уже сформированный ВИ, содержащий набор ИЗ, в результирующий список
                VolumesReportJSON doc = new VolumesReportJSON();
                doc.setName(last_time_interval);
                doc.setSeries(seriesList);
                returnList.add(doc);


                return returnList;
            } else return null;
        }
        catch (Exception e) {
            logger.error("Exception in method getVolumesReportData. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Возвращает строку, ответственную за формирование фильтра по отделению и сотруднику
    private String getVolumesReportDepAndEmpl(VolumesReportForm request) {
        String s = "";
        if(request.getDepartmentsIds().size()>0){
            Long[] ids = request.getDepartmentsIds().toArray(new Long[request.getDepartmentsIds().size()]);
            s += " and rs.department_id = " + ids[0];
        }
        if(request.getEmployeeIds().size()>0){
            Long[] ids = request.getEmployeeIds().toArray(new Long[request.getEmployeeIds().size()]);
            s += " and rs.creator_id = " + ids[0];
        }
        return s;
    }

    //ВОзвращает строку, ответственную за формирование временного интервала
    private String getVolumesReportTimeInterval(VolumesReportForm request, Boolean forSorting){
        String s = "";
        // Если ДЕНЬ - ЧАС - извлекаем только часы, т.к шкала юнитов для ДЕНЬ состоит только из 1 элемента - ЧАС
        if(request.getPeriodType().equals("day")){
            if(request.getUnit().equals("hour"))
                s=     s + " to_char(EXTRACT("+request.getUnit()+" from rs.date_time_created),'00')||':00'";
        }
        // Если НЕДЕЛЯ -  шкала юнитов состоит из ЧАС, ДЕНЬ
        if(request.getPeriodType().equals("week")){
            // Хоть для ЧАС, хоть для ДЕНЬ - всегда добавляем дату, т.к. упорядочивать можем только по данному столбцу, и если в пределах одного дня (для ЧАС) или месяца (для ДЕНЬ) порядок еще будет соблюдён,
            // то в случае, если неделя будет покрывать 2 месяца (конец одного и начало следующего) или 2 года, порядок вывода будет нарушен
            s=     s + "to_char(rs.date_time_created, 'DD.MM.YYYY')";
            if(request.getUnit().equals("hour"))
                s=     s + " || ' ' || to_char(EXTRACT(hour from rs.date_time_created),'00.99')";
        }
        // Если МЕСЯЦ -  шкала юнитов состоит из ДЕНЬ, НЕДЕЛЯ
        if(request.getPeriodType().equals("month")){
            if(request.getUnit().equals("day"))
                s=     s + "to_char(rs.date_time_created, 'DD.MM.YYYY')";
            if(request.getUnit().equals("week"))
                s=     s + "to_char(EXTRACT(week from rs.date_time_created),'00')";
        }
        // Если ГОД -  шкала юнитов состоит из ДЕНЬ, НЕДЕЛЯ, МЕСЯЦ
        if(request.getPeriodType().equals("year")) {
            if(request.getUnit().equals("week"))
                s=     s + "to_char(EXTRACT(week from rs.date_time_created),'00')";
            if(request.getUnit().equals("month"))
                s=     s + "to_char(rs.date_time_created, 'MM.YYYY')";
        }
        // Если ПРОИЗВОЛЬНЫЙ ПЕРИОД -  шкала юнитов состоит из ЧАС, ДЕНЬ, НЕДЕЛЯ, МЕСЯЦ, ГОД
        if(request.getPeriodType().equals("period")) {
            if(request.getUnit().equals("hour"))
                s=     s + " to_char(EXTRACT("+request.getUnit()+" from rs.date_time_created),'00')||':00'";
            if(request.getUnit().equals("day"))
                s=     s + "to_char(rs.date_time_created, '"+(forSorting?"YYYYMMDD":"DD.MM.YYYY")+"')";
            if(request.getUnit().equals("week"))
                s=     s + (forSorting?"to_char(rs.date_time_created, 'YYYY')||":"")+"to_char(EXTRACT(week from rs.date_time_created),'FM00')"+(!forSorting?"||chr(39)||to_char(rs.date_time_created, 'YYYY')":"");
            if(request.getUnit().equals("month"))
                s=     s + "to_char(rs.date_time_created, '"+(forSorting?"YYYYMM":"MM.YYYY")+"')";
            if(request.getUnit().equals("year"))
                s=     s + "to_char(EXTRACT(year from rs.date_time_created),'0000')";
        }

        s=     s + " as time_interval"+(forSorting?"_sort":"");
        return s;
    }

}
