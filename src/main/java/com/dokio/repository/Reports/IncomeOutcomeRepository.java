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


import com.dokio.message.request.Reports.IncomeOutcomeReportForm;
import com.dokio.message.response.Reports.VolumeSerie;
import com.dokio.message.response.Reports.VolumesReportJSON;
import com.dokio.repository.ProductsRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
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
public class IncomeOutcomeRepository {

    Logger logger = Logger.getLogger("IncomeOutcomeRepository");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    // Отдает данные по виджету "Объемы"
    @SuppressWarnings("Duplicates")
    public List<VolumesReportJSON> getIncomeOutcomeReportData(IncomeOutcomeReportForm request) {
        String last_time_interval = "";
        String current_time_interval = ""; //текущий временной интервал (ВИ) (поле NAME у объекта VolumesReportJSON). При смене данного интервала VolumesReportJSON добавляется в сет, и формируется новый VolumesReportJSON
        List<VolumeSerie> seriesList = new ArrayList<>();//список значений за один ВИ (от 1 до n здначений на ВИ)
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;

            stringQuery =
                    " select  " +
                            " z.name, " +
                            " z.time_interval, " +
                            " sum(z.summ), " +
                            " z.time_interval_sort " +
                            " from " +
                            "(";
            // нужно сконструировать 2 запроса, объединенные union, один по розничным продажам (retail_sales), второй по отгрузкам (shipment)

            stringQuery = stringQuery+"select   " +
                    " 'Приход' as name, " +
                    " to_char(p.date_time_created, 'MM.YYYY') as time_interval, " +
                    " SUM(ABS(p.summ)) as summ, " +
                    " to_char(p.date_time_created, 'YYYYMM') as time_interval_sort " +
                    " from paymentin p " +
                    " where  p.date_time_created >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.date_time_created <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.master_id="+myMasterId+" and  p.company_id="+request.getCompanyId()+" and coalesce(p.internal,false)=false and coalesce(p.is_deleted,false)=false and coalesce(p.is_completed,false)=true group by time_interval, time_interval_sort " +
        " union all " +
                    " select " +
                    " 'Приход' as name, " +
                    " to_char(p.date_time_created, 'MM.YYYY') as time_interval, " +
                    " SUM(ABS(p.summ)) as summ, " +
                    " to_char(p.date_time_created, 'YYYYMM') as time_interval_sort " +
                    " from orderin p " +
                    " where  p.date_time_created >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.date_time_created <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.master_id="+myMasterId+" and  p.company_id="+request.getCompanyId()+" and coalesce(p.internal,false)=false and coalesce(p.is_deleted,false)=false and coalesce(p.is_completed,false)=true group by time_interval, time_interval_sort " +
        " union all " +
                    " select " +
                    " 'Приход' as name, " +
                    " to_char(p.date_time_created, 'MM.YYYY') as time_interval, " +
                    " SUM(ABS(p.summ)) as summ, " +
                    " to_char(p.date_time_created, 'YYYYMM') as time_interval_sort " +
                    " from correction p " +
                    " where  p.date_time_created >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.date_time_created <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.master_id="+myMasterId+" and  p.company_id="+request.getCompanyId()+" and coalesce(p.is_deleted,false)=false and coalesce(p.is_completed,false)=true and p.summ>0 and p.type in ('account','boxoffice') group by time_interval, time_interval_sort " +

        " union all " +

                    " select " +
                    " 'Расход' as name, " +
                    " to_char(p.date_time_created, 'MM.YYYY') as time_interval, " +
                    " SUM(ABS(p.summ)) as summ, " +
                    " to_char(p.date_time_created, 'YYYYMM') as time_interval_sort " +
                    " from paymentout p " +
                    " inner join sprav_expenditure_items sei on p.expenditure_id=sei.id " +
                    " where  p.date_time_created >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.date_time_created <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
                    " and sei.type!='moving' " +
                    " and p.master_id="+myMasterId+" and  p.company_id="+request.getCompanyId()+" and coalesce(p.is_deleted,false)=false and coalesce(p.is_completed,false)=true group by time_interval, time_interval_sort " +
        " union all " +
                    " select " +
                    " 'Расход' as name, " +
                    " to_char(p.date_time_created, 'MM.YYYY') as time_interval, " +
                    " SUM(ABS(p.summ)) as summ, " +
                    " to_char(p.date_time_created, 'YYYYMM') as time_interval_sort " +
                    " from orderout p " +
                    " inner join sprav_expenditure_items sei on p.expenditure_id=sei.id " +
                    " where  p.date_time_created >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.date_time_created <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
                    " and sei.type!='moving' " +
                    " and p.master_id="+myMasterId+" and  p.company_id="+request.getCompanyId()+" and coalesce(p.is_deleted,false)=false and coalesce(p.is_completed,false)=true group by time_interval, time_interval_sort " +
        " union all " +
                    " select " +
                    " 'Расход' as name, " +
                    " to_char(p.date_time_created, 'MM.YYYY') as time_interval, " +
                    " SUM(ABS(p.summ)) as summ, " +
                    " to_char(p.date_time_created, 'YYYYMM') as time_interval_sort " +
                    " from correction p " +
                    " where  p.date_time_created >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.date_time_created <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
                    " and p.master_id="+myMasterId+" and  p.company_id="+request.getCompanyId()+" and coalesce(p.is_deleted,false)=false and coalesce(p.is_completed,false)=true and p.summ<0 and p.type in ('account','boxoffice') group by time_interval, time_interval_sort ";

        stringQuery = stringQuery+") as z group by  z.name,  z.time_interval, z.time_interval_sort " +
                                    "order by z.time_interval_sort, name";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom", request.getDateFrom());
            query.setParameter("dateTo", request.getDateTo());
            List<Object[]> queryList = query.getResultList();
            List<VolumesReportJSON> returnList = new ArrayList<>();

            //если данные (строки "ВИ+имя+значение") есть
            if(!queryList.isEmpty()) {
                //бежим по строкам "ВИ+имя+значение"
                for (Object[] obj : queryList) {
//                    [0] - name
//                    [1] - time_interval
//                    [2] - summ
//                    [3] - time_interval_sort
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
                        //В этот объект будет помещено название данного ВИ (например, "01.2021") и список "имя+значение" (ИЗ) за этот ВИ (Приход 100, Расход 200)
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

                    VolumeSerie serie = new VolumeSerie();
                    serie.setName((String) obj[0]);
                    serie.setValue((BigDecimal) obj[2]);
                    seriesList.add(serie);



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
            logger.error("Exception in method getIncomeOutcomeReportData. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

}
