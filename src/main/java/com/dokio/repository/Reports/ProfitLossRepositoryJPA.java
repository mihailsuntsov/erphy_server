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

import com.dokio.message.request.Reports.ProfitLossForm;
import com.dokio.message.response.Reports.ProfitLossJSON;
import com.dokio.message.response.Reports.ProfitLossSerie;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ProfitLossRepositoryJPA {

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

    private Logger logger = Logger.getLogger("ProfitLossRepositoryJPA");

    @SuppressWarnings("Duplicates")
    public ProfitLossJSON getProfitLoss(ProfitLossForm reqest) {
        if(securityRepositoryJPA.userHasPermissions_OR(49L, "590,591"))//(см. файл Permissions Id)
        {
            String myTimeZone = userRepository.getUserTimeZone();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            if (    !commonUtilites.isDateValid(reqest.getDateFrom()) ||
                    !commonUtilites.isDateValid(reqest.getDateTo()) ||
                    (!securityRepositoryJPA.userHasPermissions_OR(49L, "590") && !myCompanyId.equals(reqest.getCompanyId())))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Недопустимые параметры запроса");

            try {
                ProfitLossJSON doc = new ProfitLossJSON();                      // показатели доходов и расходов
                // выручка
                doc.setRevenue(getProfitLossRevenue(reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone));
                // себестоимость
                doc.setCost_price(getProfitLossCostPrice(myMasterId,reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone));
                // валовая прибыль
                doc.setGross_profit(doc.getRevenue().subtract(doc.getCost_price()));
                // Операционные расходы
                List<ProfitLossSerie> operational=getProfitLossOpex(myMasterId,reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone);
                // списания в операционных расходах считаются отдельно:
                ProfitLossSerie writeoffs = new ProfitLossSerie();
                writeoffs.setName("Списания");
                writeoffs.setValue(getProfitLossWriteoffs(myMasterId,reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone));
                operational.add(writeoffs);
                doc.setOperational(operational);
                // сумма по операционным расходам
                doc.setOperating_expenses(new BigDecimal(0));
                for (ProfitLossSerie profitLossSerie : operational) {
                    doc.setOperating_expenses(doc.getOperating_expenses().add(profitLossSerie.getValue()));
                }
                // операционная прибыль = валовая прибыль − операционные расходы
                doc.setOperating_profit(doc.getGross_profit().subtract(doc.getOperating_expenses()));
                // налоги и сборы
                doc.setTaxes_and_fees(getProfitLossTaxes(myMasterId,reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone));
                // чистая прибыль = операционная прибыль − сумма налогов и сборов
                doc.setNet_profit(doc.getOperating_profit().subtract(doc.getTaxes_and_fees()));
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getProfitLoss.", e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public List<ProfitLossSerie> getOpexOnly(ProfitLossForm reqest) {
        if(securityRepositoryJPA.userHasPermissions_OR(26L, "609,610"))//(см. файл Permissions Id)
        {
            String myTimeZone = userRepository.getUserTimeZone();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            if (    !commonUtilites.isDateValid(reqest.getDateFrom()) ||
                    !commonUtilites.isDateValid(reqest.getDateTo()) ||
                    (!securityRepositoryJPA.userHasPermissions_OR(26L, "609") && !myCompanyId.equals(reqest.getCompanyId())))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Недопустимые параметры запроса");

            try {
                // Операционные расходы
                List<ProfitLossSerie> operational=getProfitLossOpex(myMasterId,reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone);
                // списания в операционных расходах считаются отдельно:
                ProfitLossSerie writeoffs = new ProfitLossSerie();
                writeoffs.setName("Списания");
                writeoffs.setValue(getProfitLossWriteoffs(myMasterId,reqest.getCompanyId(),reqest.getDateFrom(), reqest.getDateTo(), myTimeZone));
                operational.add(writeoffs);
                return operational;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getOpexOnly.", e);
                return null;
            }
        } else return null;
    }

    //возвращает выручку
    @SuppressWarnings("Duplicates")
    private BigDecimal getProfitLossRevenue(Long companyId, String dateFrom, String dateTo, String myTimeZone) {
        String stringQuery;
        stringQuery =
        " select summ1+summ2-summ3 as summ from " +
        " coalesce((select " +
        " sum(ABS(rsp.product_count*rsp.product_price)) as summ " +
        " from retail_sales_product rsp " +
        " inner JOIN retail_sales rs ON rsp.retail_sales_id = rs.id " +
        " where " +
        " rs.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and rs.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and rs.company_id="+companyId+"),0) as summ1, " +
        " coalesce((select " +
        " sum(ABS(rsp.product_count*rsp.product_price)) as summ " +
        " from shipment_product rsp " +
        " inner JOIN shipment rs ON rsp.shipment_id = rs.id " +
        " where " +
        " rs.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and rs.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and rs.company_id="+companyId+"),0) as summ2, "+
        " coalesce((select " +
        " sum(ABS(rsp.product_count*rsp.product_price)) as summ " +
        " from return_product rsp " +
        " inner JOIN return rs ON rsp.return_id = rs.id " +
        " where " +
        " rs.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and rs.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and rs.company_id="+companyId+"),0) as summ3 ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom",dateFrom);
            query.setParameter("dateTo",dateTo);
            BigDecimal result= (BigDecimal) query.getSingleResult();
            return (Objects.isNull(result)?new BigDecimal(0):result.setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProfitLossRevenue. SQL query:" + stringQuery, e);
            return null;
        }
    }
    // возвращает себестоимость
    @SuppressWarnings("Duplicates")
    private BigDecimal getProfitLossCostPrice(Long master_id, Long companyId, String dateFrom, String dateTo, String myTimeZone) {
        String stringQuery;
        stringQuery =
        " select cost_price " +
        " from " +
        " coalesce((select abs(sum(change*avg_netcost_price)) " +
        " from products_history " +
        " where " +
        " master_id="+master_id +
        " and company_id="+companyId +
        " and doc_type_id in(21,25) " +
        " and date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') ),0) as cost_price ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom",dateFrom);
            query.setParameter("dateTo",dateTo);
            BigDecimal result= (BigDecimal) query.getSingleResult();
            return (Objects.isNull(result)?new BigDecimal(0):result.setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (NoResultException nre) {
            return new BigDecimal(0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProfitLossCostPrice. SQL query:" + stringQuery, e);
            return null;
        }
    }
    // возвращает расходы по списаниям
    @SuppressWarnings("Duplicates")
    private BigDecimal getProfitLossWriteoffs(Long master_id, Long companyId, String dateFrom, String dateTo, String myTimeZone) {
        String stringQuery;
        stringQuery =
        " select cost_price " +
        " from " +
        " coalesce((select abs(sum(change*last_operation_price)) " +
        " from products_history " +
        " where " +
        " master_id="+master_id +
        " and company_id="+companyId +
        " and doc_type_id =17 " +
        " and date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') ),0) as cost_price ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom",dateFrom);
            query.setParameter("dateTo",dateTo);
            BigDecimal result= (BigDecimal) query.getSingleResult();
            return (Objects.isNull(result)?new BigDecimal(0):result.setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (NoResultException nre) {
            return new BigDecimal(0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProfitLossCostPrice. SQL query:" + stringQuery, e);
            return null;
        }
    }

    //возвращает операционные расходы (кроме списаний)
    @SuppressWarnings("Duplicates")
    public List<ProfitLossSerie> getProfitLossOpex(Long master_id, Long companyId, String dateFrom, String dateTo, String myTimeZone) {
        String stringQuery;
        stringQuery =
        " select z.name, sum(z.summ) " +
        " from " +
        " (select se.name as name, sum(oo.summ) as summ from orderout oo " +
        " inner join sprav_expenditure_items se on oo.expenditure_id=se.id " +
        " where " +
        " oo.master_id="+master_id +" and oo.company_id="+companyId +
        " and oo.is_completed=true " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.expenditure_id in( " +
        " select sei.id from sprav_expenditure_items sei where sei.master_id="+master_id +" and sei.company_id="+companyId +" and sei.type='other_opex' " +
        " ) group by se.name " +
        " union all " +
        " select se.name as name, sum(oo.summ) as summ from paymentout oo " +
        " inner join sprav_expenditure_items se on oo.expenditure_id=se.id " +
        " where " +
        " oo.master_id="+master_id +" and oo.company_id="+companyId +
        " and oo.is_completed=true " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.expenditure_id in( " +
        " select sei.id from sprav_expenditure_items sei where sei.master_id="+master_id +" and sei.company_id="+companyId +" and sei.type='other_opex' " +
        " ) group by se.name) as z group by z.name ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom", dateFrom);
            query.setParameter("dateTo", dateTo);
            List<Object[]> queryList = query.getResultList();
            List<ProfitLossSerie> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                ProfitLossSerie doc=new ProfitLossSerie();
                doc.setName((String)                                        obj[0]);
                doc.setValue((BigDecimal)                                   obj[1]);
                doc.setValue(Objects.isNull(doc.getValue())?new BigDecimal(0):doc.getValue().setScale(2, BigDecimal.ROUND_HALF_UP));
                returnList.add(doc);
            }
            return returnList;
        } catch (NoResultException nre) {
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProfitLossOpex. SQL query:" + stringQuery, e);
            return null;
        }
    }

    //возвращает расходы по налогам
    @SuppressWarnings("Duplicates")
    public BigDecimal getProfitLossTaxes(Long master_id, Long companyId, String dateFrom, String dateTo, String myTimeZone) {
        String stringQuery;
        stringQuery =
        " select sum(z.summ) " +
        " from " +
        " (" +
        " select sum(oo.summ) as summ from orderout oo " +
        " inner join sprav_expenditure_items se on oo.expenditure_id=se.id " +
        " where " +
        " oo.master_id="+master_id +" and oo.company_id="+companyId +
        " and oo.is_completed=true " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.expenditure_id in ( " +
        " select sei.id from sprav_expenditure_items sei where sei.master_id="+master_id+" and sei.company_id="+companyId+" and sei.type='taxes' " +
        " )" +
        " union all " +
        " select sum(oo.summ) as summ from paymentout oo " +
        " inner join sprav_expenditure_items se on oo.expenditure_id=se.id " +
        " where " +
        " oo.master_id="+master_id+" and oo.company_id="+companyId +
        " and oo.is_completed=true " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' >=to_timestamp(:dateFrom||' 00:00:00','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.date_time_created at time zone '"+myTimeZone+"' <=to_timestamp(:dateTo||' 23:59:59','DD.MM.YYYY HH24:MI:SS') " +
        " and oo.expenditure_id in ( " +
        " select sei.id from sprav_expenditure_items sei where sei.master_id="+master_id+" and sei.company_id="+companyId+" and sei.type='taxes' " +
        " )" +
        " ) as z ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom",dateFrom);
            query.setParameter("dateTo",dateTo);
            BigDecimal result= (BigDecimal) query.getSingleResult();
            return (Objects.isNull(result)?new BigDecimal(0):result.setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (NoResultException nre) {
            return new BigDecimal(0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getProfitLossTaxes. SQL query:" + stringQuery, e);
            return null;
        }
    }
}
