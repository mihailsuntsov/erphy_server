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
import com.dokio.message.response.Reports.VolumeSerie;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import com.dokio.util.FinanceUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class IndicatorsRepository {

    Logger logger = Logger.getLogger("IndicatorsRepository");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    ProfitLossRepositoryJPA profitLossRepositoryJPA;
    @Autowired
    FinanceUtilites financeUtilites;
    @Autowired
    SecurityRepositoryJPA securityRepository;
    @Autowired
    private CommonUtilites cu;

    @SuppressWarnings("Duplicates")
    public List<VolumeSerie> getIndicatorsData(Long companyId) {
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
        List<Integer> myPermissions = securityRepository.giveMeMyPermissions(26L);
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
//        Date dateNow = new Date();

        Calendar calendar = Calendar.getInstance();// get a calendar instance, which defaults to "now"
        calendar.add(Calendar.DAY_OF_YEAR, 1);// add one day to the date/calendar
        Date tomorrow = calendar.getTime();// now get "tomorrow"
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone(myTimeZone));
        List<VolumeSerie> retList = new ArrayList<>();
        Map<String, String> map = cu.translateForMe(new String[]{"'overdue_invcs'","'overdue_ordrs'","'new_orders'","'money'","'your_debt'","'you_owed'"});

        if(myPermissions.contains(604) || (myPermissions.contains(605) && myCompanyId.equals(companyId))) {
            VolumeSerie serie1 = new VolumeSerie(); // Просроченные счета / Overdue invoices
            Map<String, String> extra_overdue_invcs = new HashMap<>();
            extra_overdue_invcs.put("code", "overdue_invcs");
            serie1.setExtra(extra_overdue_invcs);
            serie1.setName(map.get("overdue_invcs"));
            serie1.setValue(BigDecimal.valueOf(getOverdueBills(companyId, myMasterId)));
            retList.add(serie1);
        }

        if(myPermissions.contains(602) || (myPermissions.contains(603) && myCompanyId.equals(companyId))) {
            VolumeSerie serie2 = new VolumeSerie(); // Просроченные заказы / Overdue orders
            Map<String, String> extra_overdue_ordrs = new HashMap<>();
            extra_overdue_ordrs.put("code", "overdue_ordrs");
            serie2.setExtra(extra_overdue_ordrs);
            serie2.setName(map.get("overdue_ordrs"));
            serie2.setValue(BigDecimal.valueOf(getOverdueOrders(companyId, myMasterId)));
            retList.add(serie2);
        }

        if(myPermissions.contains(600) || (myPermissions.contains(601) && myCompanyId.equals(companyId))) {
            VolumeSerie serie3 = new VolumeSerie(); // Новые заказы / New orders
            Map<String, String> extra_new_orders = new HashMap<>();
            extra_new_orders.put("code", "new_orders");
            serie3.setExtra(extra_new_orders);
            serie3.setName(map.get("new_orders"));
            serie3.setValue(BigDecimal.valueOf(getNewOrders(companyId, myMasterId)));
            retList.add(serie3);
        }

        if(myPermissions.contains(594) || (myPermissions.contains(595) && myCompanyId.equals(companyId))) {
            VolumeSerie serie5 = new VolumeSerie(); // Деньги / Money
            Map<String, String> extra_money = new HashMap<>();
            extra_money.put("code", "money");
            serie5.setExtra(extra_money);
            serie5.setName(map.get("money"));
            serie5.setValue(financeUtilites.getBalancesOnDate(companyId, dateFormat.format(tomorrow)));
            retList.add(serie5);
        }

        if(myPermissions.contains(596) || myPermissions.contains(597) || myPermissions.contains(598) || myPermissions.contains(599)) {
            VolumeSerie serie6_1 = new VolumeSerie();
            VolumeSerie serie6_2 = new VolumeSerie();
            List<BigDecimal> cagentsBalances = getCagentsBalances(companyId, myMasterId);
            BigDecimal weDebt = new BigDecimal(0);
            BigDecimal usDebt = new BigDecimal(0);
            if (!Objects.isNull(cagentsBalances))
                for (BigDecimal m : cagentsBalances) {
                    if (m.compareTo(new BigDecimal(0)) < 0)
                        usDebt = usDebt.add(m);
                    else
                        weDebt = weDebt.add(m);
                }
            if(myPermissions.contains(596) || (myPermissions.contains(597) && myCompanyId.equals(companyId))) {
                Map<String, String> extra_your_debt = new HashMap<>();
                extra_your_debt.put("code", "your_debt");
                serie6_1.setExtra(extra_your_debt);
                serie6_1.setName(map.get("your_debt")); // Вы должны / Your debt
                serie6_1.setValue(weDebt);
                retList.add(serie6_1);
            }
            if(myPermissions.contains(598) || (myPermissions.contains(599) && myCompanyId.equals(companyId))) {
                Map<String, String> extra_you_owed = new HashMap<>();
                extra_you_owed.put("code", "you_owed");
                serie6_2.setExtra(extra_you_owed);
                serie6_2.setName(map.get("you_owed"));  // Вам должны / You are owed
                serie6_2.setValue(usDebt.abs());
                retList.add(serie6_2);
            }
        }
        return retList;
    }


    // возвращает количество просроченных счетов
    @SuppressWarnings("Duplicates")
    private int getOverdueBills(Long companyId, Long masterId) {
        String stringQuery;
        stringQuery =
                " select p.id from invoiceout p " +
                " where " +
                " p.master_id = " + masterId + " and p.company_id = " + companyId +
                " and coalesce(p.is_completed,false)=false " +
                " and coalesce(p.is_deleted,false)=false " +
//                " and p.invoiceout_date < now()";
//                        " and p.invoiceout_date at time zone '" + myTimeZone + "'  < date(timezone('" + myTimeZone + "', now()))";
                " and to_timestamp(to_char(p.invoiceout_date,'DD.MM.YYYY')||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') < now()";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getOverdueBills. SQL query:" + stringQuery, e);
            return 0;
        }
    }
    // возвращает количество просроченных заказов
    @SuppressWarnings("Duplicates")
    private int getOverdueOrders(Long companyId, Long masterId) {
//        Date dateNow = new Date();
//        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
        String stringQuery;
        stringQuery =
                " select p.id from customers_orders p " +
                " where " +
                " p.master_id = " + masterId + " and p.company_id = " + companyId +
                " and coalesce(p.is_completed,false)=false " +
                " and coalesce(p.is_deleted,false)=false " +
//                        " and p.shipment_date < to_date('"+dateFormat.format(dateNow)+"','DD.MM.YYYY')";
                " and to_timestamp(to_char(p.shipment_date,'DD.MM.YYYY')||' 23:59:59.999', 'DD.MM.YYYY HH24:MI:SS.MS') < now()";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getOverdueOrders. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    // возвращает количество новых заказов
    @SuppressWarnings("Duplicates")
    private int getNewOrders(Long companyId, Long masterId) {
        String stringQuery;
        stringQuery =
                " select p.id from customers_orders p " +
                " where " +
                " p.master_id = " + masterId + " and p.company_id = " + companyId +
                " and coalesce(p.is_completed,false)=false " +
                " and coalesce(p.is_deleted,false)=false " +
                " and (p.linked_docs_group_id is null or p.date_time_changed is null)";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getNewOrders. SQL query:" + stringQuery, e);
            return 0;
        }
    }

    // возвращает балансы по контрагентам
    private List<BigDecimal> getCagentsBalances (Long companyId, Long masterId) {
        String stringQuery;
        stringQuery =
                " select " +
                " coalesce((select SUM(p3.summ_in-p3.summ_out) from history_cagent_summ p3 where p3.master_id = " + masterId + " and p3.company_id = " + companyId + " and p3.object_id = p.id and p3.is_completed = true limit 1),0) as summ_on_end  " +
                " from cagents p   " +
                " where p.master_id = " + masterId +
                " and p.company_id = " + companyId +
                " order by summ_on_end desc";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getCagentsBalances. SQL query:" + stringQuery, e);
            return null;
        }
    }
}
