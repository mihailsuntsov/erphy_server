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
package com.dokio.repository;

import com.dokio.message.response.additional.*;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ReceiptsRepository {

    Logger logger = Logger.getLogger("ReceiptsRepository");

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
    ProductsRepositoryJPA productsRepository;


    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("shift_number","name","kassa","acquiring_bank","company","sno","acquiring_bank","department","payment_type","creator","date_time_created_sort","document","operation_id")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public List<ReceiptsJSON> getReceiptsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Long departmentId, Long cashierId, Long kassaId, Integer shift_id) {
        if(securityRepositoryJPA.userHasPermissions_OR(44L, "563,564,565"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            boolean needToSetParameter_MyDepthsIds = false;
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());


            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           tax.short_name as sno, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.sno_id as sno_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           sh.shift_number as shift_number, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           p.shift_id as shift_id," +
                    "           p.kassa_id as kassa_id, " +// id KKM
                    "           ka.name as kassa, " +
                    "           p.acquiring_bank_id as acquiring_bank_id, " + // id банка эквайера
                    "           aqu.name as acquiring_bank, " + // наименование банка эквайера
                    "           (select name from documents where id=p.document_id) as document,  " +
                    "           p.return_id as return_id, " +
                    "           p.retail_sales_id as retail_sales_id, " +
                    "           p.shipment_id as shipment_id, " +
                    "           p.parent_tablename as parent_tablename, " +
                    "           p.operation_id as operation_id, " +
                    "           p.billing_address as billing_address, " +
                    "           p.payment_type as payment_type, " +
                    "           p.cash as cash, " +
                    "           p.electronically as electronically, " +
                    "           p.uid as uid, " +
                    "           p.cash+p.electronically as summ," +
                    "           p.parent_doc_id as parent_doc_id," +

                    "           p.date_time_created as date_time_created_sort " +

                    "           from receipts p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN shifts sh ON p.shift_id=sh.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN sprav_sys_taxation_types tax on p.sno_id=tax.id "+
                    "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN cagents aqu ON p.acquiring_bank_id=aqu.id " +
                    "           where  p.master_id=" + myMasterId+
                    ((cashierId>0)?" and p.creator_id = "+cashierId:"") +
                    ((!Objects.isNull(shift_id) && shift_id>0)?" and p.shift_id = "+shift_id:"") +
                    ((kassaId>0)?" and p.kassa_id = "+kassaId:"");

            if (!securityRepositoryJPA.userHasPermissions_OR(44L, "563")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(44L, "564")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (565)
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(sh.shift_number,'0000000000') like CONCAT('%',:sg) or "+
                        " upper(tax.short_name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(aqu.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(ka.name)  like upper(CONCAT('%',:sg,'%'))"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }


            if (VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) && VALID_COLUMNS_FOR_ASC.contains(sortAsc)) {
                stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            } else {
                throw new IllegalArgumentException("Недопустимые параметры запроса");
            }


            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
                {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

                query.setFirstResult(offsetreal).setMaxResults(result);


                List<Object[]> queryList = query.getResultList();
                List<ReceiptsJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    ReceiptsJSON doc=new ReceiptsJSON();
                    doc.setId(Long.parseLong(                               obj[0].toString()));
                    doc.setMaster((String)                                  obj[1]);
                    doc.setCreator((String)                                 obj[2]);
                    doc.setSno((String)                                     obj[3]);
                    doc.setMaster_id(Long.parseLong(                        obj[4].toString()));
                    doc.setCreator_id(Long.parseLong(                       obj[5].toString()));
                    doc.setSno_id((Integer)                                 obj[6]);
                    doc.setCompany_id(Long.parseLong(                       obj[7].toString()));
                    doc.setDepartment_id(Long.parseLong(                    obj[8].toString()));
                    doc.setDepartment((String)                              obj[9]);
                    doc.setShift_number((Integer)                           obj[10]);
                    doc.setCompany((String)                                 obj[11]);
                    doc.setDate_time_created((String)                       obj[12]);
                    doc.setShift_id(Long.parseLong(                         obj[13].toString()));
                    doc.setKassa_id(Long.parseLong(                         obj[14].toString()));
                    doc.setKassa((String)                                   obj[15]);
                    doc.setAcquiring_bank_id(obj[16]!=null?Long.parseLong(  obj[16].toString()):null);
                    doc.setAcquiring_bank((String)                          obj[17]);
                    doc.setDocument((String)                                obj[18]);
                    doc.setReturn_id(obj[19]!=null?Long.parseLong(          obj[19].toString()):null);
                    doc.setRetail_sales_id(obj[20]!=null?Long.parseLong(    obj[20].toString()):null);
                    doc.setShipment_id(obj[21]!=null?Long.parseLong(        obj[21].toString()):null);
                    doc.setParent_tablename((String)                        obj[22]);
                    doc.setOperation_id((String)                            obj[23]);
                    doc.setBilling_address((String)                         obj[24]);
                    doc.setPayment_type((String)                            obj[25]);
                    doc.setCash((BigDecimal)                                obj[26]);
                    doc.setElectronically((BigDecimal)                      obj[27]);
                    doc.setUid((String)                                     obj[28]);
                    doc.setSumm((BigDecimal)                                obj[29]);
                    doc.setParent_doc_id(obj[30]!=null?Long.parseLong(      obj[30].toString()):null);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getReceiptsTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getReceiptsSize(int result, String searchString, Long companyId, Long departmentId, Long cashierId, Long kassaId,  Integer shift_id) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
//        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from receipts p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN shifts sh ON p.shift_id=sh.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN sprav_sys_taxation_types tax on p.sno_id=tax.id "+
                "           INNER JOIN kassa ka ON p.kassa_id=ka.id " +
                "           INNER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN cagents aqu ON p.acquiring_bank_id=aqu.id " +
                "           where  p.master_id=" + myMasterId+
                ((cashierId>0)?" and p.creator_id = "+cashierId:"") +
                ((!Objects.isNull(shift_id) && shift_id>0)?" and p.shift_id = "+shift_id:"") +
                ((kassaId>0)?" and p.kassa_id = "+kassaId:"");

        if (!securityRepositoryJPA.userHasPermissions_OR(44L, "563")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения
            if (!securityRepositoryJPA.userHasPermissions_OR(44L, "564")) //Если нет прав на просм по своему предприятию
            {//остается только на просмотр всех доков в своих отделениях (565)
                stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
            }//т.е. по всем и своему предприятиям нет а на свои отделения есть
            else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(sh.shift_number,'0000000000') like CONCAT('%',:sg) or "+
                    " upper(tax.short_name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(dp.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(cmp.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(us.name)  like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(aqu.name) like upper(CONCAT('%',:sg,'%')) or "+
                    " upper(ka.name)  like upper(CONCAT('%',:sg,'%'))"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            return query.getResultList().size();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getReceiptsSize. SQL query:" + stringQuery, e);
            return 0;
        }
    }
}