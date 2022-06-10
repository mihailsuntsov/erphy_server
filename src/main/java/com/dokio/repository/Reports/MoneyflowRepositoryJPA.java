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
import com.dokio.message.response.Reports.MoneyflowTableJSON;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.SecurityRepositoryJPA;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class MoneyflowRepositoryJPA {

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
    CommonUtilites commonUtilites;

    private Logger logger = Logger.getLogger("MoneyflowRepositoryJPA");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","company","department","creator","date_time_created_sort","doc_name","date_created","summ","status","summ_in","summ_out","doc_number","summ_on_start","summ_on_end","obj_name")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));



    @SuppressWarnings("Duplicates")
    public List<MoneyflowTableJSON> getMoneyflowTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, String dateFrom, String dateTo) {
        if(securityRepositoryJPA.userHasPermissions_OR(48L, "587,588"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String dateFormat=userRepositoryJPA.getMyDateFormat();

            if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) ||
                    !VALID_COLUMNS_FOR_ASC.contains(sortAsc) ||
                    !commonUtilites.isDateValid(dateFrom) ||
                    !commonUtilites.isDateValid(dateTo) ||
                    (!securityRepositoryJPA.userHasPermissions_OR(48L, "587") && !myCompanyId.equals(companyId)))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Invalid query parameters");
            stringQuery = "select " +
                    " to_char(dc, '"+dateFormat+"') as date_created,   " +
                    " (summ_in_pa + summ_corr_in_pa) as summ_in_pa_,   " +
                    " (summ_out_pa + ABS(summ_corr_out_pa)) as summ_out_pa_,   " +
                    " (summ_result_in_pa-summ_result_out_pa+summ_result_corr_pa) as summ_result_pa,   " +

                    " (summ_in_bx + summ_corr_in_bx) as summ_in_bx_,   " +
                    " (summ_out_bx + ABS(summ_corr_out_bx)) as summ_out_bx_,   " +
                    " (summ_result_in_bx-summ_result_out_bx+summ_result_corr_bx) as summ_result_bx,   " +

                    " (summ_in_pa + summ_corr_in_pa)+(summ_in_bx + summ_corr_in_bx) as summ_in_all,   " +
                    " (summ_out_pa + ABS(summ_corr_out_pa))+(summ_out_bx + ABS(summ_corr_out_bx))as summ_out_all,   " +
                    " (summ_result_in_pa-summ_result_out_pa+summ_result_corr_pa)+(summ_result_in_bx-summ_result_out_bx+summ_result_corr_bx) as summ_result_all, " +
                    " dc as date_created_sort " +

                    " from   " +
                    " generate_series(timestamp with time zone '"+dateFrom+"',  timestamp with time zone '"+dateTo+"'  , interval  '1 day') AS dc,   " +
//                    " generate_series(timestamp '"+dateFrom+"',  timestamp '"+dateTo+"'  , interval  '1 day') AS dc,   " +

                    " coalesce((select SUM(    tt.summ)  from paymentin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_in_pa, " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ>0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='account'),0) as summ_corr_in_pa, " +

                    " coalesce((select SUM(    tt.summ)  from paymentout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_out_pa, " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ<0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='account'),0) as summ_corr_out_pa,  " +

                    " coalesce((select SUM(    tt.summ) from paymentin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc,'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_in_pa, " +
                    " coalesce((select SUM(    tt.summ) from paymentout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc,'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_out_pa, " +
                    " coalesce((select SUM(    tt.summ) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc,'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='account'),0) as summ_result_corr_pa, " +

                    " coalesce((select SUM(    tt.summ)  from orderin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_in_bx, " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ>0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_corr_in_bx, " +

                    " coalesce((select SUM(    tt.summ)  from orderout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_out_bx, " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ<0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_corr_out_bx,  " +

                    " coalesce((select SUM(    tt.summ) from orderin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc,'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_in_bx, " +
                    " coalesce((select SUM(    tt.summ) from orderout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc,'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_out_bx, " +
                    " coalesce((select SUM(    tt.summ) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc,'DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_result_corr_bx ";

            stringQuery = stringQuery +" where ((summ_in_pa + summ_corr_in_pa) !=0 or (summ_out_pa + ABS(summ_corr_out_pa)) !=0 or (summ_in_bx + summ_corr_in_bx) !=0 or (summ_out_bx + ABS(summ_corr_out_bx)) !=0) ";
            stringQuery = stringQuery +" group by  date_created, summ_in_pa, summ_out_pa, summ_result_pa, summ_in_bx, summ_out_bx,summ_result_bx, summ_corr_in_pa,summ_corr_out_pa,summ_corr_in_bx,summ_corr_out_bx, dc  order by date_created_sort asc ";


            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setFirstResult(offsetreal).setMaxResults(result);
                List<Object[]> queryList = query.getResultList();
                List<MoneyflowTableJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    MoneyflowTableJSON doc = new MoneyflowTableJSON();
                    doc.setDate_created((String)            obj[0]);
                    doc.setSumm_in_pa((BigDecimal)          obj[1]);
                    doc.setSumm_out_pa((BigDecimal)         obj[2]);
                    doc.setSumm_result_pa((BigDecimal)      obj[3]);
                    doc.setSumm_in_bx((BigDecimal)          obj[4]);
                    doc.setSumm_out_bx((BigDecimal)         obj[5]);
                    doc.setSumm_result_bx((BigDecimal)      obj[6]);
                    doc.setSumm_in_all((BigDecimal)         obj[7]);
                    doc.setSumm_out_all((BigDecimal)        obj[8]);
                    doc.setSumm_result_all((BigDecimal)     obj[9]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMoneyflowTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public Integer getMoneyflowSize(String searchString, Long companyId, Set<Integer> filterOptionsIds, String dateFrom, String dateTo) {
        if(securityRepositoryJPA.userHasPermissions_OR(48L, "587,588"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            if (    !commonUtilites.isDateValid(dateFrom) ||
                    !commonUtilites.isDateValid(dateTo) ||
                    (!securityRepositoryJPA.userHasPermissions_OR(48L, "587") && !myCompanyId.equals(companyId)))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Invalid query parameters");
            stringQuery =
                    "select " +
                            " (summ_in_pa + summ_corr_in_pa) as summ_in_pa_, " +
                            " (summ_out_pa + ABS(summ_corr_out_pa)) as summ_out_pa_, " +
                            " (summ_in_bx + summ_corr_in_bx) as summ_in_bx_, " +
                            " (summ_out_bx + ABS(summ_corr_out_bx)) as summ_out_bx_ " +
                            " from " +
                            "generate_series(timestamp with time zone '"+dateFrom+"', timestamp with time zone '"+dateTo+"', interval  '1 day') AS dc, " +
//                    " generate_series(timestamp '"+dateFrom+"',  timestamp '"+dateTo+"'  , interval  '1 day') AS dc,   " +
                            " coalesce((select SUM(    tt.summ)  from paymentin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_in_pa, " +
                            " coalesce((select SUM(CASE 	WHEN tt.summ>0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='account'),0) as summ_corr_in_pa, " +

                            " coalesce((select SUM(    tt.summ)  from paymentout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_out_pa, " +
                            " coalesce((select SUM(CASE 	WHEN tt.summ<0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='account'),0) as summ_corr_out_pa,  " +

                            " coalesce((select SUM(    tt.summ)  from orderin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_in_bx, " +
                            " coalesce((select SUM(CASE 	WHEN tt.summ>0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_corr_in_bx, " +

                            " coalesce((select SUM(    tt.summ)  from orderout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true),0) as summ_out_bx, " +
                            " coalesce((select SUM(CASE 	WHEN tt.summ<0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and to_char(tt.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc, 'DD.MM.YYYY') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_corr_out_bx  ";

            stringQuery = stringQuery +" where ((summ_in_pa + summ_corr_in_pa) !=0 or (summ_out_pa + ABS(summ_corr_out_pa)) !=0 or (summ_in_bx + summ_corr_in_bx) !=0 or (summ_out_bx + ABS(summ_corr_out_bx)) !=0) ";
            stringQuery = stringQuery + " group by  summ_in_pa, summ_out_pa, summ_in_bx, summ_out_bx, summ_corr_in_pa, summ_corr_out_pa, summ_corr_in_bx, summ_corr_out_bx";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                return query.getResultList().size();

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMoneyflowSize. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public MoneyflowTableJSON getMoneyflowBalances(Long companyId, String dateFrom, String dateTo) {
        if(securityRepositoryJPA.userHasPermissions_OR(48L, "587,588"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            if (    !commonUtilites.isDateValid(dateFrom) ||
                    !commonUtilites.isDateValid(dateTo) ||
                    (!securityRepositoryJPA.userHasPermissions_OR(48L, "587") && !myCompanyId.equals(companyId)))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Invalid query parameters");
            stringQuery = " select   " +
                    " to_char(dc, 'DD.MM.YYYY') as date_created,   " +


                    " (summ_in_before_pa-summ_out_before_pa+summ_corr_before_pa) as summ_before_pa,     " +  // Начальный остаток - Расчётные счета
                    " (summ_result_in_pa-summ_result_out_pa+summ_result_corr_pa) as summ_result_pa,   " +    // Конечный остаток - Расчётные счета
                    " (summ_in_before_bx-summ_out_before_bx+summ_corr_before_bx) as summ_before_bx,    " +  // Начальный остаток - Кассы предприятия
                    " (summ_result_in_bx-summ_result_out_bx+summ_result_corr_bx) as summ_result_bx,   " +   // Конечный остаток - Кассы предприятия

                    " (summ_in_before_pa-summ_out_before_pa+summ_corr_before_pa)+(summ_in_before_bx-summ_out_before_bx+summ_corr_before_bx) as summ_before_all,   " +  // Начальный остаток - Все
                    " (summ_result_in_pa-summ_result_out_pa+summ_result_corr_pa)+(summ_result_in_bx-summ_result_out_bx+summ_result_corr_bx) as summ_result_all,   " + // Конечный остаток - Все

                    // Сумма за период
                    " total_summ_in_pa+total_summ_corr_in_pa as total_summ_in_pa,   " +
                    " total_summ_out_pa+ABS(total_summ_corr_out_pa) as total_summ_out_pa,   " +
                    " total_summ_in_bx+total_summ_corr_in_bx as total_summ_in_bx,   " +
                    " total_summ_out_bx+ABS(total_summ_corr_out_bx) as total_summ_out_bx,   " +
                    " (total_summ_in_pa+total_summ_corr_in_pa)+(total_summ_in_bx+total_summ_corr_in_bx) as total_summ_in_all,  " +
                    " (total_summ_out_pa+ABS(total_summ_corr_out_pa))+(total_summ_out_bx+ABS(total_summ_corr_out_bx)) as total_summ_out_all  " +

                    " from   " +
                    " generate_series(timestamp with time zone '"+dateFrom+"',  timestamp with time zone '"+dateTo+"', interval  '1 day') AS dc,   " +
//                    " generate_series(timestamp '"+dateFrom+"',  timestamp '"+dateTo+"'  , interval  '1 day') AS dc,   " +
                    // Начальный остаток
                    " coalesce((select SUM(    tt.summ)  from paymentin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' < to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true ),0) as summ_in_before_pa,   " +
                    " coalesce((select SUM(    tt.summ)  from paymentout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' < to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true ),0) as summ_out_before_pa, " +
                    " coalesce((select SUM(    tt.summ)  from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' < to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='account'),0) as summ_corr_before_pa, " +

                    " coalesce((select SUM(    tt.summ)  from orderin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' < to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true ),0) as summ_in_before_bx,   " +
                    " coalesce((select SUM(    tt.summ)  from orderout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' < to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true ),0) as summ_out_before_bx, " +
                    " coalesce((select SUM(    tt.summ)  from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' < to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_corr_before_bx, " +
                    // Конечный остаток
                    " coalesce((select SUM(    tt.summ) from paymentin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_in_pa, " +
                    " coalesce((select SUM(    tt.summ) from paymentout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_out_pa, " +
                    " coalesce((select SUM(    tt.summ) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='account'),0) as summ_result_corr_pa, " +

                    " coalesce((select SUM(    tt.summ) from orderin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_in_bx, " +
                    " coalesce((select SUM(    tt.summ) from orderout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as summ_result_out_bx, " +
                    " coalesce((select SUM(    tt.summ) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='boxoffice'),0) as summ_result_corr_bx, " +

                    //Сумма за период
                    " coalesce((select SUM(tt.summ) from paymentin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as total_summ_in_pa,   " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ>0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='account'),0) as total_summ_corr_in_pa, " +
                    " coalesce((select SUM(tt.summ) from paymentout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as total_summ_out_pa,   " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ<0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='account'),0) as total_summ_corr_out_pa, " +

                    " coalesce((select SUM(tt.summ) from orderin tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as total_summ_in_bx,   " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ>0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='boxoffice'),0) as total_summ_corr_in_bx, " +
                    " coalesce((select SUM(tt.summ) from orderout tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true),0) as total_summ_out_bx,   " +
                    " coalesce((select SUM(CASE 	WHEN tt.summ<0 THEN  tt.summ ELSE 0 END) from correction tt where tt.master_id="+myMasterId+" and tt.company_id="+companyId+" and tt.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp('"+dateFrom+"'||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and tt.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp('"+dateTo+"'||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') and tt.is_completed=true and tt.type='boxoffice'),0) as total_summ_corr_out_bx " +
//                    " where (summ_in_pa!=0 or summ_out_pa!=0 or summ_in_bx!=0 or summ_out_bx!=0) " +
                    " group by  date_created, summ_before_pa, summ_before_bx,  summ_result_pa, summ_result_bx, total_summ_in_pa,total_summ_out_pa,total_summ_in_bx,total_summ_out_bx,total_summ_in_all,total_summ_out_all,dc,total_summ_corr_in_pa,total_summ_corr_out_pa,total_summ_in_bx,total_summ_corr_in_bx,total_summ_corr_out_bx  order by dc asc ";




            try {
                Query query = entityManager.createNativeQuery(stringQuery);
//                query.setParameter(1,dateFrom); // не знаю почему, но с generate_series не работают ни named параметры, ни параметры типа ?1 .
//                query.setParameter(2,dateTo);
                List<Object[]> queryList = query.getResultList();
                MoneyflowTableJSON doc = new MoneyflowTableJSON();
                if(queryList.size()>0) {


                    // Начальный остаток
                    doc.setSumm_before_pa((BigDecimal) queryList.get(0)[1]); // Расчётные счета
                    doc.setSumm_before_bx((BigDecimal) queryList.get(0)[3]); // Кассы предприятия
                    doc.setSumm_before_all((BigDecimal) queryList.get(0)[5]);// Все

                    // Кончный остаток
                    doc.setSumm_result_pa((BigDecimal) queryList.get(queryList.size() - 1)[2]);   // Расчётные счета
                    doc.setSumm_result_bx((BigDecimal) queryList.get(queryList.size() - 1)[4]);   // Кассы предприятия
                    doc.setSumm_result_all((BigDecimal) queryList.get(queryList.size() - 1)[6]); // Все

                    // Сумма за период
                    doc.setTotal_summ_in_pa((BigDecimal) queryList.get(0)[7]);
                    doc.setTotal_summ_out_pa((BigDecimal) queryList.get(0)[8]);
                    doc.setTotal_summ_in_bx((BigDecimal) queryList.get(0)[9]);
                    doc.setTotal_summ_out_bx((BigDecimal) queryList.get(0)[10]);
                    doc.setTotal_summ_in_all((BigDecimal) queryList.get(0)[11]);
                    doc.setTotal_summ_out_all((BigDecimal) queryList.get(0)[12]);
                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMoneyflowBalances. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    private String getMoneyflowDetailedSQL(String searchString, Long companyId){
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String sx = userRepositoryJPA.getMySuffix();
        Map<String, String> map = commonUtilites.translateForMe(new String[]{"'acc_short'","'cash_room'"});
        String dateFormat=userRepositoryJPA.getMyDateFormat();
        stringQuery = " select  " +
                " d.doc_name_"+sx+" as doc_name, " + // Входящий платеж
                " p.doc_number as doc_number,  " +
                " to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created,  " +
                " p.summ as summ_in,  " +
                " 0.00 as summ_out, " +
                " st.name as status,  " +
                " d.page_name as doc_page_name,  " +
                " p.id as doc_id,  " +
                "'"+map.get("acc_short")+" "+"'||obj.payment_account||', '||obj.name as obj_name,  " +
                " p.date_time_created as date_time_created_sort  " +
                " from paymentin p  " +
                " INNER JOIN companies_payment_accounts obj on p.payment_account_id=obj.id  " +
                " INNER JOIN documents d ON d.id = 33 " +
                " LEFT OUTER JOIN sprav_status_dock st ON p.status_id=st.id  " +
                " where  " +
                " p.master_id="+myMasterId+
                " and p.company_id="+companyId+
                " and p.is_completed=true" +
                " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')  " +
                " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')  ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_"+sx+") like upper(CONCAT('%',:sg,'%')) or upper(obj.payment_account||' '||obj.name) like upper(CONCAT('%',:sg,'%')))";
        }

        stringQuery = stringQuery +  " UNION ALL" +
                " select  " +
                " d.doc_name_"+sx+" as doc_name, " +  // Исходящий платеж
                " p.doc_number as doc_number,  " +
                " to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created,  " +
                " 0.00 as summ_in,  " +
                " p.summ as summ_out, " +
                " st.name as status,  " +
                " d.page_name as doc_page_name,  " +
                " p.id as doc_id,  " +
                "'"+map.get("acc_short")+" "+"'||obj.payment_account||', '||obj.name as obj_name,  " +
                " p.date_time_created as date_time_created_sort  " +
                " from paymentout p  " +
                " INNER JOIN companies_payment_accounts obj on p.payment_account_id=obj.id  " +
                " INNER JOIN documents d ON d.id = 34 " +
                " LEFT OUTER JOIN sprav_status_dock st ON p.status_id=st.id  " +
                " where  " +
                " p.master_id="+myMasterId+
                " and p.company_id="+companyId+
                " and p.is_completed=true" +
                " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
                " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')  ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_"+sx+") like upper(CONCAT('%',:sg,'%')) or upper('р/с '||obj.payment_account||' в '||obj.name) like upper(CONCAT('%',:sg,'%')))";
        }
        stringQuery = stringQuery + " UNION ALL" +
                " select  " +
                " d.doc_name_"+sx+" as doc_name, " +  // Приходный ордер
                " p.doc_number as doc_number,  " +
                " to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created,  " +
                " p.summ as summ_in,  " +
                " 0.00 as summ_out, " +
                " st.name as status,  " +
                " d.page_name as doc_page_name,  " +
                " p.id as doc_id,  " +
                "'"+map.get("cash_room")+" "+"'||obj.name as obj_name,  " +
                " p.date_time_created as date_time_created_sort  " +
                " from orderin p  " +
                " INNER JOIN sprav_boxoffice obj on p.boxoffice_id=obj.id  " +
                " INNER JOIN documents d ON d.id = 35 " +
                " LEFT OUTER JOIN sprav_status_dock st ON p.status_id=st.id  " +
                " where  " +
                " p.master_id="+myMasterId+
                " and p.company_id="+companyId+
                " and p.is_completed=true" +
                " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')" +
                " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_"+sx+")  like upper(CONCAT('%',:sg,'%')) or upper(obj.name) like upper(CONCAT('%',:sg,'%')))";
        }
        stringQuery = stringQuery + " UNION ALL" +
                " select  " +
                " d.doc_name_"+sx+" as doc_name, " +  // Расходный ордер
                " p.doc_number as doc_number,  " +
                " to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created,  " +
                " p.summ as summ_in,  " +
                " 0.00 as summ_out, " +
                " st.name as status,  " +
                " d.page_name as doc_page_name,  " +
                " p.id as doc_id,  " +
                "'"+map.get("cash_room")+" "+"'||obj.name as obj_name,  " +
                " p.date_time_created as date_time_created_sort  " +
                " from orderout p  " +
                " INNER JOIN sprav_boxoffice obj on p.boxoffice_id=obj.id  " +
                " INNER JOIN documents d ON d.id = 36 " +
                " LEFT OUTER JOIN sprav_status_dock st ON p.status_id=st.id  " +
                " where  " +
                " p.master_id="+myMasterId+
                " and p.company_id="+companyId+
                " and p.is_completed=true" +
                " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
                " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')  ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_"+sx+")  like upper(CONCAT('%',:sg,'%')) or upper(obj.name) like upper(CONCAT('%',:sg,'%')))";
        }
        stringQuery = stringQuery + " UNION ALL" +
                " select  " +
                " d.doc_name_"+sx+" as doc_name, " +  // Корректировка
                " p.doc_number as doc_number,  " +
                " to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created,  " +
                " (CASE WHEN p.summ>0 THEN  p.summ ELSE 0.00 END) as summ_in,  " +
                " (CASE WHEN p.summ<0 THEN  p.summ ELSE 0.00 END) as summ_out, " +
                " st.name as status,  " +
                " d.page_name as doc_page_name,  " +
                " p.id as doc_id,  " +
                " (CASE WHEN p.boxoffice_id is null THEN  ('"+map.get("acc_short")+" "+"'||obj2.payment_account||', '||obj2.name) ELSE ('"+map.get("cash_room")+" "+"'||obj1.name) END) as obj_name,  " +
                " p.date_time_created as date_time_created_sort  " +
                " from correction p  " +
                " INNER JOIN documents d ON d.id = 41 " +
                " LEFT OUTER JOIN sprav_status_dock st ON p.status_id=st.id  " +

                " LEFT OUTER JOIN sprav_boxoffice obj1 on p.boxoffice_id=obj1.id  " +
                " LEFT OUTER JOIN companies_payment_accounts obj2 on p.payment_account_id=obj2.id  " +

                " where  " +
                " p.master_id="+myMasterId+
                " and p.company_id="+companyId+
                " and p.is_completed=true" +
                " and type in ('account','boxoffice')" +
                " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
                " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')  ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_"+sx+") like upper(CONCAT('%',:sg,'%')) or upper(" +
                    "(CASE WHEN p.boxoffice_id is null THEN  (obj2.payment_account||' '||obj2.name) ELSE (obj1.name) END)" +
                    ") like upper(CONCAT('%',:sg,'%')))";
        }
//        Query query = entityManager.createNativeQuery(stringQuery);
        return stringQuery;
    }
    //возвращает детализированный отчет по взаиморасчетам с выбранным контрагентом за период
    @SuppressWarnings("Duplicates")
    public List<MoneyflowTableJSON> getMoneyflowDetailedTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, String dateFrom, String dateTo) {
        if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
            throw new IllegalArgumentException("Invalid query parameters");
        String stringQuery = getMoneyflowDetailedSQL(searchString, companyId);
        stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            query.setParameter("dateFrom", dateFrom);
            query.setParameter("dateTo", dateTo);

            query.setFirstResult(offsetreal).setMaxResults(result);

            List<Object[]> queryList = query.getResultList();
            List<MoneyflowTableJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                MoneyflowTableJSON doc=new MoneyflowTableJSON();
                doc.setDoc_name((String)                                    obj[0]);
                doc.setDoc_number(                                          obj[1].toString());
                doc.setDate_time_created((String)                           obj[2]);
                doc.setSumm_in((BigDecimal)                                 obj[3]);
                doc.setSumm_out((BigDecimal)                                obj[4]);
                doc.setStatus((String)                                      obj[5]);
                doc.setDoc_page_name((String)                               obj[6]);
                doc.setDoc_id(Long.parseLong(                               obj[7].toString()));
                doc.setObj_name((String)                                    obj[8]);
                returnList.add(doc);
            }
            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMoneyflowDetailedTable. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public Integer getMoneyflowDetailedSize(String searchString, Long companyId, Set<Integer> filterOptionsIds, String dateFrom, String dateTo) {
        String stringQuery = getMoneyflowDetailedSQL(searchString, companyId);
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            query.setParameter("dateFrom", dateFrom);
            query.setParameter("dateTo", dateTo);
            return query.getResultList().size();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMoneyflowDetailedSize. SQL query:" + stringQuery, e);
            return null;
        }
    }
}
