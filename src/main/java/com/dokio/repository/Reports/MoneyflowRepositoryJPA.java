package com.dokio.repository.Reports;

import com.dokio.message.request.Reports.MoneyflowForm;
import com.dokio.message.response.Reports.MoneyflowTableJSON;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.DepartmentRepositoryJPA;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    CommonUtilites commonUtilites;

    Logger logger = Logger.getLogger("MoneyflowRepositoryJPA");

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
            if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) ||
                !VALID_COLUMNS_FOR_ASC.contains(sortAsc) ||
                !commonUtilites.isDateValid(dateFrom) ||
                !commonUtilites.isDateValid(dateTo) ||
                (!securityRepositoryJPA.userHasPermissions_OR(48L, "587") && !myCompanyId.equals(companyId)))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            stringQuery = "select " +
            " to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_created, " +
            " ABS(summ_before_pa) as summ_before_pa, " +
            " summ_in_pa as summ_in_pa, " +
            " ABS(summ_out_pa) as summ_out_pa, " +
            " ABS(summ_result_pa) as summ_result_pa, " +
            " ABS(summ_before_bx) as summ_before_bx, " +
            " summ_in_bx as summ_in_bx, " +
            " ABS(summ_out_bx) as summ_out_bx, " +
            " ABS(summ_result_bx) as summ_result_bx, " +
            " ABS(summ_before_pa+summ_before_bx) as summ_before_all, " +
            " summ_in_pa+summ_in_bx as summ_in_all, " +
            " ABS(summ_out_pa+summ_out_bx) as summ_out_all, " +
            " ABS(summ_result_pa+summ_result_bx) as summ_result_all " +
            " from " +
            " generate_series(timestamp with time zone '"+dateFrom+"', " +
            " timestamp with time zone '"+dateTo+"' " +
            " , interval  '1 day') AS dc, " +
            " coalesce((select SUM(    pa4.summ_change)  from history_payment_account_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' < to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_before_pa, " +
            " coalesce((select sum(CASE 	WHEN pa2.summ_change>0 THEN  pa2.summ_change ELSE 0 END) from history_payment_account_summ pa2 where pa2.master_id="+myMasterId+" and pa2.company_id="+companyId+" and to_char(pa2.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_in_pa, " +
            " coalesce((select sum(CASE 	WHEN pa3.summ_change<0 THEN  pa3.summ_change ELSE 0 END) from history_payment_account_summ pa3 where pa3.master_id="+myMasterId+" and pa3.company_id="+companyId+" and to_char(pa3.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_out_pa, " +
            " coalesce((select SUM(    pa4.summ_change)  from history_payment_account_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_result_pa, " +
            " coalesce((select SUM(    bx.summ_change)  from history_boxoffice_summ bx where bx.master_id="+myMasterId+" and bx.company_id="+companyId+" and bx.date_time_created at time zone '"+myTimeZone+"' < to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_before_bx, " +
            " coalesce((select sum(CASE 	WHEN bx2.summ_change>0 THEN  bx2.summ_change ELSE 0 END) from history_boxoffice_summ bx2 where bx2.master_id="+myMasterId+" and bx2.company_id="+companyId+" and to_char(bx2.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_in_bx, " +
            " coalesce((select sum(CASE 	WHEN bx3.summ_change<0 THEN  bx3.summ_change ELSE 0 END) from history_boxoffice_summ bx3 where bx3.master_id="+myMasterId+" and bx3.company_id="+companyId+" and to_char(bx3.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_out_bx, " +
            " coalesce((select SUM(    bx4.summ_change)  from history_boxoffice_summ bx4 where bx4.master_id="+myMasterId+" and bx4.company_id="+companyId+" and bx4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_result_bx " +
            " where (summ_in_pa>0 or summ_out_pa>0 or summ_in_bx>0 or summ_out_bx>0) ";
            stringQuery = stringQuery + " group by  date_created, summ_before_pa, summ_before_bx, " +
            " summ_in_pa, summ_out_pa, summ_result_pa, summ_in_bx, summ_out_bx,summ_result_bx, summ_result_pa " +
            " order by date_created asc";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setFirstResult(offsetreal).setMaxResults(result);
                List<Object[]> queryList = query.getResultList();
                List<MoneyflowTableJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    MoneyflowTableJSON doc = new MoneyflowTableJSON();
                    doc.setDate_created((String)            obj[0]);
                    doc.setSumm_before_pa((BigDecimal)      obj[1]);
                    doc.setSumm_in_pa((BigDecimal)          obj[2]);
                    doc.setSumm_out_pa((BigDecimal)         obj[3]);
                    doc.setSumm_result_pa((BigDecimal)      obj[4]);
                    doc.setSumm_before_bx((BigDecimal)      obj[5]);
                    doc.setSumm_in_bx((BigDecimal)          obj[6]);
                    doc.setSumm_out_bx((BigDecimal)         obj[7]);
                    doc.setSumm_result_bx((BigDecimal)      obj[8]);
                    doc.setSumm_before_all((BigDecimal)     obj[9]);
                    doc.setSumm_in_all((BigDecimal)         obj[10]);
                    doc.setSumm_out_all((BigDecimal)        obj[11]);
                    doc.setSumm_result_all((BigDecimal)     obj[12]);
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
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            stringQuery =
                    "select summ_in_pa as summ_in_pa, " +
                    " summ_out_pa as summ_out_pa, " +
                    " summ_in_bx as summ_in_bx, " +
                    " summ_out_bx as summ_out_bx " +
                    " from generate_series(timestamp with time zone '"+dateFrom+"', timestamp with time zone '"+dateTo+"', interval  '1 day') AS dc, " +
                    " coalesce((select sum(CASE 	WHEN pa2.summ_change>0 THEN  pa2.summ_change ELSE 0 END) from history_payment_account_summ pa2 where pa2.master_id="+myMasterId+" and pa2.company_id="+companyId+" and to_char(pa2.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_in_pa, " +
                    " coalesce((select sum(CASE 	WHEN pa3.summ_change<0 THEN  pa3.summ_change ELSE 0 END) from history_payment_account_summ pa3 where pa3.master_id="+myMasterId+" and pa3.company_id="+companyId+" and to_char(pa3.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_out_pa, " +
                    " coalesce((select sum(CASE 	WHEN bx2.summ_change>0 THEN  bx2.summ_change ELSE 0 END) from history_boxoffice_summ bx2 where bx2.master_id="+myMasterId+" and bx2.company_id="+companyId+" and to_char(bx2.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_in_bx, " +
                    " coalesce((select sum(CASE 	WHEN bx3.summ_change<0 THEN  bx3.summ_change ELSE 0 END) from history_boxoffice_summ bx3 where bx3.master_id="+myMasterId+" and bx3.company_id="+companyId+" and to_char(bx3.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_out_bx " +
                    " where (summ_in_pa>0 or summ_out_pa>0 or summ_in_bx>0 or summ_out_bx>0) ";

            stringQuery = stringQuery + " group by  summ_in_pa, summ_out_pa, summ_in_bx, summ_out_bx";

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
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            stringQuery = "select " +
                    " to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_created, " +
                    " ABS(summ_before_pa) as summ_before_pa, " +
                    " summ_in_pa as summ_in_pa, " +
                    " ABS(summ_out_pa) as summ_out_pa, " +
                    " ABS(summ_result_pa) as summ_result_pa, " +
                    " ABS(summ_before_bx) as summ_before_bx, " +
                    " summ_in_bx as summ_in_bx, " +
                    " ABS(summ_out_bx) as summ_out_bx, " +
                    " ABS(summ_result_bx) as summ_result_bx, " +
                    " ABS(summ_before_pa+summ_before_bx) as summ_before_all, " +
                    " summ_in_pa+summ_in_bx as summ_in_all, " +
                    " ABS(summ_out_pa+summ_out_bx) as summ_out_all, " +
                    " ABS(summ_result_pa+summ_result_bx) as summ_result_all, " +
                    " total_summ_in_pa as total_summ_in_pa, " +
                    " ABS(total_summ_out_pa) as total_summ_out_pa, " +
                    " ABS(total_summ_in_bx) as total_summ_in_bx, " +
                    " ABS(total_summ_out_bx) as total_summ_out_bx, " +
                    " total_summ_in_pa+total_summ_in_bx as total_summ_in_all," +
                    " ABS(total_summ_out_pa+total_summ_out_bx) as total_summ_out_all" +
                    " from " +
                    " generate_series(timestamp with time zone '"+dateFrom+"', " +
                    " timestamp with time zone '"+dateTo+"' " +
                    " , interval  '1 day') AS dc, " +
                    " coalesce((select SUM(    pa4.summ_change)  from history_payment_account_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' < to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_before_pa, " +
                    " coalesce((select SUM(CASE 	WHEN pa2.summ_change>0 THEN  pa2.summ_change ELSE 0 END) from history_payment_account_summ pa2 where pa2.master_id="+myMasterId+" and pa2.company_id="+companyId+" and to_char(pa2.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_in_pa, " +
                    " coalesce((select SUM(CASE 	WHEN pa3.summ_change<0 THEN  pa3.summ_change ELSE 0 END) from history_payment_account_summ pa3 where pa3.master_id="+myMasterId+" and pa3.company_id="+companyId+" and to_char(pa3.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_out_pa, " +
                    " coalesce((select SUM(    pa4.summ_change) from history_payment_account_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_result_pa, " +
                    " coalesce((select SUM(    bx.summ_change)  from history_boxoffice_summ bx        where bx.master_id="+myMasterId+"  and bx.company_id="+companyId+"  and bx.date_time_created  at time zone '"+myTimeZone+"' < to_timestamp(to_char(dc  at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_before_bx, " +
                    " coalesce((select SUM(CASE 	WHEN bx2.summ_change>0 THEN  bx2.summ_change ELSE 0 END) from history_boxoffice_summ bx2 where bx2.master_id="+myMasterId+" and bx2.company_id="+companyId+" and to_char(bx2.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_in_bx, " +
                    " coalesce((select SUM(CASE 	WHEN bx3.summ_change<0 THEN  bx3.summ_change ELSE 0 END) from history_boxoffice_summ bx3 where bx3.master_id="+myMasterId+" and bx3.company_id="+companyId+" and to_char(bx3.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY')=to_char(dc at time zone '"+myTimeZone+"', 'DD.MM.YYYY')),0) as summ_out_bx, " +
                    " coalesce((select SUM(    bx4.summ_change)  from history_boxoffice_summ bx4 where bx4.master_id="+myMasterId+" and bx4.company_id="+companyId+" and bx4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(to_char(dc at time zone '"+myTimeZone+"','DD.MM.YYYY')||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_result_bx, " +

                    " coalesce((select SUM(CASE 	WHEN pa4.summ_change>0 THEN  pa4.summ_change ELSE 0 END) from history_payment_account_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and pa4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as total_summ_in_pa, " +
                    " coalesce((select SUM(CASE 	WHEN pa4.summ_change<0 THEN  pa4.summ_change ELSE 0 END) from history_payment_account_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and pa4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as total_summ_out_pa, " +
                    " coalesce((select SUM(CASE 	WHEN pa4.summ_change>0 THEN  pa4.summ_change ELSE 0 END) from history_boxoffice_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and pa4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as total_summ_in_bx, " +
                    " coalesce((select SUM(CASE 	WHEN pa4.summ_change<0 THEN  pa4.summ_change ELSE 0 END) from history_boxoffice_summ pa4 where pa4.master_id="+myMasterId+" and pa4.company_id="+companyId+" and pa4.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and pa4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as total_summ_out_bx " +
                    " where (summ_in_pa>0 or summ_out_pa>0 or summ_in_bx>0 or summ_out_bx>0) ";
            stringQuery = stringQuery + " group by  date_created, summ_before_pa, summ_before_bx, " +
                    " summ_in_pa, summ_out_pa, summ_result_pa, summ_in_bx, summ_out_bx,summ_result_bx, summ_result_pa, " +
                    " total_summ_in_pa,total_summ_out_pa,total_summ_in_bx,total_summ_out_bx,total_summ_in_all,total_summ_out_all,dc order by dc asc";
            try {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("dateFrom",dateFrom);
                query.setParameter("dateTo",dateTo);
                List<Object[]> queryList = query.getResultList();
                MoneyflowTableJSON doc = new MoneyflowTableJSON();
                if(queryList.size()>0) {
                    // Из первой строки получаем Начальный остаток, из последней строки - Конечный остаток
                    doc.setSumm_before_pa((BigDecimal) queryList.get(0)[1]);
                    doc.setSumm_before_bx((BigDecimal) queryList.get(0)[5]);
                    doc.setSumm_before_all((BigDecimal) queryList.get(0)[9]);
                    doc.setSumm_result_pa((BigDecimal) queryList.get(queryList.size() - 1)[4]);
                    doc.setSumm_result_bx((BigDecimal) queryList.get(queryList.size() - 1)[8]);
                    doc.setSumm_result_all((BigDecimal) queryList.get(queryList.size() - 1)[12]);
                    doc.setTotal_summ_in_pa((BigDecimal) queryList.get(0)[13]);
                    doc.setTotal_summ_out_pa((BigDecimal) queryList.get(0)[14]);
                    doc.setTotal_summ_in_bx((BigDecimal) queryList.get(0)[15]);
                    doc.setTotal_summ_out_bx((BigDecimal) queryList.get(0)[16]);
                    doc.setTotal_summ_in_all((BigDecimal) queryList.get(0)[17]);
                    doc.setTotal_summ_out_all((BigDecimal) queryList.get(0)[18]);
                }
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getMoneyflowBalances. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }


    //возвращает детализированный отчет по взаиморасчетам с выбранным контрагентом за период
    @SuppressWarnings("Duplicates")
    public List<MoneyflowTableJSON> getMoneyflowDetailedTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, String dateFrom, String dateTo) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
            throw new IllegalArgumentException("Недопустимые параметры запроса");
        stringQuery =
        " select " +
        " d.doc_name_ru as doc_name, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
        " CASE 	WHEN p.summ_change>0 THEN  p.summ_change ELSE 0 END as summ_in, " +
        " CASE 	WHEN p.summ_change<0 THEN  abs(p.summ_change) ELSE 0 END as summ_out, " +
        " st.name as status, " +
        " p.doc_page_name as doc_page_name, " +
        " p.doc_id as doc_id, " +
        " 'р/с '||obj.payment_account||' в '||obj.name as obj_name, " +
        " p.date_time_created as date_time_created_sort " +
        " from history_payment_account_summ p " +
        " INNER JOIN documents d ON p.doc_table_name=d.table_name " +
        " INNER JOIN companies_payment_accounts obj on p.object_id=obj.id " +
        " LEFT OUTER JOIN sprav_status_dock st ON p.doc_status_id=st.id " +
        " where " +
        " p.master_id="+myMasterId+
        " and p.company_id="+companyId+
        " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.object_id=obj.id ";

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_ru) like upper(CONCAT('%',:sg,'%')) or upper('р/с '||obj.payment_account||' в '||obj.name) like upper(CONCAT('%',:sg,'%')))";
        }
        stringQuery = stringQuery + "                                           UNION " +
        " select " +
        " d.doc_name_ru as doc_name, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
        " CASE 	WHEN p.summ_change>0 THEN  p.summ_change ELSE 0 END as summ_in, " +
        " CASE 	WHEN p.summ_change<0 THEN  abs(p.summ_change) ELSE 0 END as summ_out, " +
        " st.name as status, " +
        " p.doc_page_name as doc_page_name, " +
        " p.doc_id as doc_id, " +
        " 'Касса \"'||obj.name||'\"' as obj_name, " +
        " p.date_time_created as date_time_created_sort " +
        " from history_boxoffice_summ p " +
        " INNER JOIN documents d ON p.doc_table_name=d.table_name " +
        " INNER JOIN sprav_boxoffice obj on p.object_id=obj.id " +
        " LEFT OUTER JOIN sprav_status_dock st ON p.doc_status_id=st.id " +
        " where " +
        " p.master_id="+myMasterId+
        " and p.company_id="+companyId+
        " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.object_id=obj.id ";

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_ru)  like upper(CONCAT('%',:sg,'%')) or upper('Касса \"'||obj.name||'\"') like upper(CONCAT('%',:sg,'%')))";
        }
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
                doc.setDoc_number((String)                                  obj[1]);
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
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
        stringQuery =
        " select " +
        " d.doc_name_ru as doc_name, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
        " CASE 	WHEN p.summ_change>0 THEN  p.summ_change ELSE 0 END as summ_in, " +
        " CASE 	WHEN p.summ_change<0 THEN  abs(p.summ_change) ELSE 0 END as summ_out, " +
        " st.name as status, " +
        " p.doc_page_name as doc_page_name, " +
        " p.doc_id as doc_id, " +
        " 'р/с '||obj.payment_account||' в '||obj.name as obj_name " +
//        " p.date_time_created as date_time_created_sort " +
        " from history_payment_account_summ p " +
        " INNER JOIN documents d ON p.doc_table_name=d.table_name " +
        " INNER JOIN companies_payment_accounts obj on p.object_id=obj.id " +
        " LEFT OUTER JOIN sprav_status_dock st ON p.doc_status_id=st.id " +
        " where " +
        " p.master_id="+myMasterId+
        " and p.company_id="+companyId+
        " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.object_id=obj.id ";

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_ru) like upper(CONCAT('%',:sg,'%')) or upper('р/с '||obj.payment_account||' в '||obj.name) like upper(CONCAT('%',:sg,'%')))";
        }
        stringQuery = stringQuery + "                                           UNION " +
        " select " +
        " d.doc_name_ru as doc_name, " +
        " p.doc_number as doc_number, " +
        " to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
        " CASE 	WHEN p.summ_change>0 THEN  p.summ_change ELSE 0 END as summ_in, " +
        " CASE 	WHEN p.summ_change<0 THEN  abs(p.summ_change) ELSE 0 END as summ_out, " +
        " st.name as status, " +
        " p.doc_page_name as doc_page_name, " +
        " p.doc_id as doc_id, " +
        " 'Касса \"'||obj.name||'\"' as obj_name " +
//        " p.date_time_created as date_time_created_sort " +
        " from history_boxoffice_summ p " +
        " INNER JOIN documents d ON p.doc_table_name=d.table_name " +
        " INNER JOIN sprav_boxoffice obj on p.object_id=obj.id " +
        " LEFT OUTER JOIN sprav_status_dock st ON p.doc_status_id=st.id " +
        " where " +
        " p.master_id="+myMasterId+
        " and p.company_id="+companyId+
        " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS') " +
        " and p.object_id=obj.id ";

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (upper(d.doc_name_ru)  like upper(CONCAT('%',:sg,'%')) or upper('Касса \"'||obj.name||'\"') like upper(CONCAT('%',:sg,'%')))";
        }

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
