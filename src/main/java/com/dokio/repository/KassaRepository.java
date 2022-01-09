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
import com.dokio.message.request.*;
import com.dokio.message.request.Settings.KassaCashierSettingsForm;
import com.dokio.message.response.*;
import com.dokio.message.response.Reports.ReceiptsJSON;
import com.dokio.message.response.Reports.ShiftsJSON;
import com.dokio.message.response.Settings.KassaCashierSettingsJSON;
import com.dokio.message.response.additional.AcquiringInfoJSON;
import com.dokio.message.response.additional.FilesUniversalJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;


@Repository
public class KassaRepository {

    Logger logger = Logger.getLogger("KassaRepository");

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
    CommonUtilites commonUtilites;
    @Autowired
    AcceptanceRepository acceptanceRepository;
    @Autowired
    PaymentinRepositoryJPA paymentinRepository;
    @Autowired
    PaymentoutRepositoryJPA paymentoutRepository;
    @Autowired
    CorrectionRepositoryJPA correctionRepository;

//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************


    @SuppressWarnings("Duplicates")
    public List<KassaJSON> getKassaTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Long departmentId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(24L, "302,303,304"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.name as name, " +
                    "           p.server_type as server_type, " +
                    "           p.sno1_id as sno1_id, " +
                    "           p.device_server_uid as device_server_uid, " +
                    "           p.additional as additional, " +
                    "           p.server_address as server_address, " +
                    "           coalesce(p.allow_to_use,false) as allow_to_use, " +
                    "           coalesce(p.is_deleted,false) as is_deleted, " +
                    "           p.billing_address as billing_address, " +
                    "           p.zn_kkt as zn_kkt," +
                    "           p.is_virtual as is_virtual, " + //виртуальная касса
                    "           p.allow_acquiring as allow_acquiring, " + //прием безнала на данной кассе
                    "           p.acquiring_bank_id as acquiring_bank_id, " + // id банк-эквайер
                    "           coalesce(p.acquiring_precent,0) as acquiring_precent, " + // процент банку за услугу эквайринга
                    "           cag.name as acquiring_bank, " + // банк-эквайер
                    "           p.acquiring_service_id as acquiring_service_id, " + // id услуги банка-эквайера
                    "           pr.name as acquiring_service, " + // услуга банка-эквайера

                    "           p.payment_account_id as payment_account_id, " + // id расчетного счета
                    "           cpa.name as payment_account, " + //  расчетный счет
                    "           p.expenditure_id as expenditure_id, " + // id статьи расходов
                    "           sei.name as expenditure " + // статья расходов

                    "           from kassa p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN cagents cag ON p.acquiring_bank_id=cag.id " +
                    "           LEFT OUTER JOIN products pr ON p.acquiring_service_id=pr.id " +
                    "           LEFT OUTER JOIN sprav_expenditure_items sei ON p.expenditure_id = sei.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_id = cpa.id " +
                    "           INNER JOIN sprav_sys_taxation_types ss ON p.sno1_id=ss.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "302")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(24L, "303")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (304)
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name) like upper('%" + searchString + "%') or "+
                        " upper(p.device_server_uid) like upper('%" + searchString + "%') or "+
                        " upper(dp.name) like upper('%" + searchString + "%') or "+
                        " upper(cmp.name) like upper('%" + searchString + "%') or "+
                        " upper(us.name) like upper('%" + searchString + "%') or "+
                        " upper(uc.name) like upper('%" + searchString + "%') or "+
                        " upper(p.zn_kkt) like upper('%" + searchString + "%') or "+
                        " upper(p.description) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId_LONG());}

            List<Object[]> queryList = query.getResultList();
            List<KassaJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                KassaJSON doc=new KassaJSON();
                doc.setId(Long.parseLong(                     obj[0].toString()));
                doc.setMaster((String)                        obj[1]);
                doc.setCreator((String)                       obj[2]);
                doc.setChanger((String)                       obj[3]);
                doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                doc.setDepartment((String)                    obj[9]);
                doc.setCompany((String)                       obj[10]);
                doc.setDate_time_created((String)             obj[11]);
                doc.setDate_time_changed((String)             obj[12]);
                doc.setName((String)                          obj[15]);
                doc.setServer_type((String)                   obj[16]);
                doc.setSno1_id((Integer)                      obj[17]);
                doc.setDevice_server_uid((String)             obj[18]);
                doc.setAdditional((String)                    obj[19]);
                doc.setServer_address((String)                obj[20]);
                doc.setAllow_to_use((Boolean)                 obj[21]);
                doc.setIs_deleted((Boolean)                   obj[22]);
                doc.setBilling_address((String)               obj[23]);
                doc.setZn_kkt((String)                        obj[24]);
                doc.setIs_virtual((Boolean)                   obj[25]);
                doc.setAllow_acquiring((Boolean)              obj[26]);
                doc.setAcquiring_bank_id(obj[27]!=null?Long.parseLong(obj[27].toString()):null);
                doc.setAcquiring_precent((BigDecimal)         obj[28]);
                doc.setAcquiring_bank((String)                obj[29]);
                doc.setAcquiring_service_id(obj[30]!=null?Long.parseLong(obj[30].toString()):null);
                doc.setAcquiring_service((String)             obj[31]);
                doc.setPayment_account_id(obj[32]!=null?Long.parseLong(obj[32].toString()):null);
                doc.setPayment_account((String)               obj[33]);
                doc.setExpenditure_id(obj[34]!=null?Long.parseLong(obj[34].toString()):null);
                doc.setExpenditure((String)                   obj[35]);



                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getKassaSize(String searchString, Long companyId, Long departmentId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from kassa p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           LEFT OUTER JOIN cagents cag ON p.acquiring_bank_id=cag.id " +
                "           LEFT OUTER JOIN products pr ON p.acquiring_service_id=pr.id " +
                "           LEFT OUTER JOIN sprav_expenditure_items sei ON p.expenditure_id = sei.id " +
                "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_id = cpa.id " +
                "           INNER JOIN sprav_sys_taxation_types ss ON p.sno1_id=ss.id" +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) ="+showDeleted;

        if (!securityRepositoryJPA.userHasPermissions_OR(24L, "302")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения
            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "303")) //Если нет прав на просм по своему предприятию
            {//остается только на просмотр всех доков в своих отделениях (304)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
            }//т.е. по всем и своему предприятиям нет а на свои отделения есть
            else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " upper(p.name) like upper('%" + searchString + "%') or "+
                    " upper(p.device_server_uid) like upper('%" + searchString + "%') or "+
                    " upper(dp.name) like upper('%" + searchString + "%') or "+
                    " upper(cmp.name) like upper('%" + searchString + "%') or "+
                    " upper(us.name) like upper('%" + searchString + "%') or "+
                    " upper(p.zn_kkt) like upper('%" + searchString + "%') or "+
                    " upper(uc.name) like upper('%" + searchString + "%') or "+
                    " upper(p.description) like upper('%" + searchString + "%')"+")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        if (departmentId > 0) {
            stringQuery = stringQuery + " and p.department_id=" + departmentId;
        }
        Query query = entityManager.createNativeQuery(stringQuery);

        if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
        {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

        return query.getResultList().size();
    }


    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    public KassaJSON getKassaValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(24L, "302,303,304"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           dp.name as department, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.name as name, " +
                    "           p.server_type as server_type, " +
                    "           p.sno1_id as sno1_id, " +
                    "           p.device_server_uid as device_server_uid, " +
                    "           p.additional as additional, " +
                    "           p.server_address as server_address, " +
                    "           coalesce(p.allow_to_use,false) as allow_to_use, " +
                    "           coalesce(p.is_deleted,false) as is_deleted, " +
                    "           p.billing_address as billing_address, " +
                    "           p.zn_kkt as zn_kkt," +
                    "           p.is_virtual as is_virtual, " + //виртуальная касса
                    "           p.allow_acquiring as allow_acquiring, " + //прием безнала на данной кассе
                    "           p.acquiring_bank_id as acquiring_bank_id, " + // id банк-эквайер
                    "           coalesce(p.acquiring_precent,0) as acquiring_precent, " + // процент банку за услугу эквайринга
                    "           cag.name as acquiring_bank, " + // банк-эквайер
                    "           p.acquiring_service_id as acquiring_service_id, " + // id услуги банка-эквайера
                    "           pr.name as acquiring_service, " + // услуга банка-эквайера
                    "           p.payment_account_id as payment_account_id, " + // id расчетного счета
                    "           cpa.name as payment_account, " + //  расчетный счет
                    "           p.expenditure_id as expenditure_id, " + // id статьи расходов
                    "           sei.name as expenditure " + // статья расходов

                    "           from kassa p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN cagents cag ON p.acquiring_bank_id=cag.id " +
                    "           LEFT OUTER JOIN products pr ON p.acquiring_service_id=pr.id " +
                    "           LEFT OUTER JOIN sprav_expenditure_items sei ON p.expenditure_id = sei.id " +
                    "           LEFT OUTER JOIN companies_payment_accounts cpa ON p.payment_account_id = cpa.id " +
                    "           INNER JOIN sprav_sys_taxation_types ss ON p.sno1_id=ss.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id=" + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "302")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(24L, "303")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (304)
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            KassaJSON doc=new KassaJSON();

            for(Object[] obj:queryList){
                doc.setId(Long.parseLong(                     obj[0].toString()));
                doc.setMaster((String)                        obj[1]);
                doc.setCreator((String)                       obj[2]);
                doc.setChanger((String)                       obj[3]);
                doc.setMaster_id(Long.parseLong(              obj[4].toString()));
                doc.setCreator_id(Long.parseLong(             obj[5].toString()));
                doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                doc.setCompany_id(Long.parseLong(             obj[7].toString()));
                doc.setDepartment_id(Long.parseLong(          obj[8].toString()));
                doc.setDepartment((String)                    obj[9]);
                doc.setCompany((String)                       obj[10]);
                doc.setDate_time_created((String)             obj[11]);
                doc.setDate_time_changed((String)             obj[12]);
                doc.setName((String)                          obj[15]);
                doc.setServer_type((String)                   obj[16]);
                doc.setSno1_id((Integer)                      obj[17]);
                doc.setDevice_server_uid((String)             obj[18]);
                doc.setAdditional((String)                    obj[19]);
                doc.setServer_address((String)                obj[20]);
                doc.setAllow_to_use((Boolean)                 obj[21]);
                doc.setIs_deleted((Boolean)                   obj[22]);
                doc.setBilling_address((String)               obj[23]);
                doc.setZn_kkt((String)                        obj[24]);
                doc.setIs_virtual((Boolean)                   obj[25]);
                doc.setAllow_acquiring((Boolean)              obj[26]);
                doc.setAcquiring_bank_id(obj[27]!=null?Long.parseLong(obj[27].toString()):null);
                doc.setAcquiring_precent((BigDecimal)         obj[28]);
                doc.setAcquiring_bank((String)                obj[29]);
                doc.setAcquiring_service_id(obj[30]!=null?Long.parseLong(obj[30].toString()):null);
                doc.setAcquiring_service((String)             obj[31]);
                doc.setPayment_account_id(obj[32]!=null?Long.parseLong(obj[32].toString()):null);
                doc.setPayment_account((String)               obj[33]);
                doc.setExpenditure_id(obj[34]!=null?Long.parseLong(obj[34].toString()):null);
                doc.setExpenditure((String)                   obj[35]);
            }
            return doc;
        } else return null;
    }




    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertKassa(KassaForm request) {

        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё преприятие
        Long docDepartment=request.getDepartment_id();
        List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
        boolean itIsMyDepartment = myDepartmentsIds.contains(docDepartment);
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого докумен
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        try{
            if ((//если есть право на создание по всем предприятиям, или
                    (securityRepositoryJPA.userHasPermissions_OR(24L, "296")) ||
                            //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, или
                            (securityRepositoryJPA.userHasPermissions_OR(24L, "297") && myCompanyId.equals(request.getCompany_id())) ||
                            //если есть право на создание по своим подразделениям своего предприятия, предприятие своё, и подразделение документа входит в число своих, И
                            (securityRepositoryJPA.userHasPermissions_OR(24L, "298") && myCompanyId.equals(request.getCompany_id()) && itIsMyDepartment)) &&
                    //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                    DocumentMasterId.equals(myMasterId))
            {
                String stringQuery;
                Long myId = userRepository.getUserId();
                Long newDocId;

                String timestamp = new Timestamp(System.currentTimeMillis()).toString();

                stringQuery =   "insert into kassa (" +
                        " master_id," + //мастер-аккаунт
                        " creator_id," + //создатель
                        " company_id," + //предприятие кассы
                        " department_id," + //отделение, в котором будет находиться касса
                        " date_time_created," + //дата и время создания
                        " name," + //наименование кассы
                        " server_type," + //тип сервера: atol - atol web server, kkmserver - kkmserver.ru
                        " sno1_id," + // id системы налогообложения
                        " device_server_uid," + // идентификатор кассы на сервере касс (atol web server или kkmserver)
                        " additional," + // дополнительная информация
                        " server_address," + // адрес сервера и порт в локальной сети или интернете вида http://127.0.0.1:16732
                        " allow_to_use," + // разрешено исползовать
                        " billing_address," + // место расчетов
                        " zn_kkt," +//заводской номер ККТ

                        " is_virtual," + //виртуальная касса
                        " allow_acquiring," + //прием безнала на данной кассе
                        " acquiring_bank_id," + // id банк-эквайер
                        " acquiring_precent," + // процент банку за услугу эквайринга
                        " acquiring_service_id," + //id услуги банка-эквайера
                        " payment_account_id," + //id расчетного счета
                        " expenditure_id," + //id статьи расходов

                        " is_deleted" + // касса удалена
                        ") values ("+
                        myMasterId + ", "+//мастер-аккаунт
                        myId + ", "+ //создатель
                        request.getCompany_id() + ", "+//предприятие кассы
                        request.getDepartment_id() + ", "+//отделение
                        "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                        "'" + request.getName() + "', " +//наименование
                        "'" + request.getServer_type() + "', " +//тип сервера: atol - atol web server, kkmserver - kkmserver.ru
                        request.getSno1_id() + ", "+//id системы налогообложения
                        "'" + request.getDevice_server_uid() + "', " +//идентификатор кассы на сервере касс (atol web server или kkmserver)
                        "'" + (request.getAdditional() == null ? "": request.getAdditional()) +  "', " +//дополнительная информация
                        "'" + request.getServer_address() + "', " +//адрес сервера и порт в локальной сети или интернете вида http://127.0.0.1:16732
                        request.getAllow_to_use() + ", " +// разрешено исползовать
                        "'" + (request.getBilling_address() == null ? "":request.getBilling_address())  + "', " +//место расчетов
                        "'" + request.getZn_kkt() + "', " +     // заводской номер ККТ
                        false + ", " +                          // виртуальная касса
                        request.getAllow_acquiring() + ", " +   // прием безнала на данной кассе
                        request.getAcquiring_bank_id() + ", " + // id банк-эквайер
                        request.getAcquiring_precent() + ", " + // процент банку за услугу эквайринга
                        request.getAcquiring_service_id() + ", " + //id услуги банка-эквайера
                        request.getPayment_account_id() + ", " + // id расчетного счета
                        request.getExpenditure_id() + ", " + //id статьи расходов

                        false + ")";                            // касса удалена
                try{
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.executeUpdate();
                    stringQuery="select id from kassa where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    newDocId=Long.valueOf(query2.getSingleResult().toString());
                    return newDocId;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception in method insertKassa ", e);
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method insertKassa ", e);
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Boolean updateKassa(KassaForm request) {
        Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());

        String stringQuery;
        stringQuery =   " update kassa set " +
                " changer_id = " + myId + ", "+ //кто изменяет
                " date_time_changed= now()," + //время изменения
                " department_id = " + request.getDepartment_id() + ", "+//отделение
                " name = '" + request.getName() + "', " +//наименование
                " server_type ='" + request.getServer_type() + "', " +//тип сервера: atol - atol web server, kkmserver - kkmserver.ru
                " sno1_id =" + request.getSno1_id() + ", " +// id системы налогообложения
                " billing_address ='" + request.getBilling_address() + "', " +// место расчетов
                " device_server_uid ='" + request.getDevice_server_uid() + "', " +// идентификатор кассы на сервере касс (atol web server или kkmserver)
                " additional ='" + request.getAdditional() + "', " +// дополнительная информация
                " server_address ='" + request.getServer_address() + "', " +// адрес сервера и порт в локальной сети или интернете вида http://127.0.0.1:16732
                " allow_to_use =" + request.getAllow_to_use() + ", " +// разрешено исползовать
                " zn_kkt = '" + request.getZn_kkt() + "', " +
                " is_virtual = false, " +// виртуальная касса
                " allow_acquiring = " + request.getAllow_acquiring() + ", " +// прием безнала на данной кассе
                " acquiring_bank_id = " + request.getAcquiring_bank_id() + ", " + // id банк-эквайер
                " acquiring_precent = " + request.getAcquiring_precent() + ", " + // процент банку за услугу эквайринга
                " acquiring_service_id = " + request.getAcquiring_service_id() + ", " + // id услуги банка-эквайера
                " payment_account_id = " + request.getPayment_account_id() + ", " + //id расчетного счета
                " expenditure_id = " + request.getExpenditure_id() + ", " + //id статьи расходов

                " is_deleted =" + request.getIs_deleted() + //  касса удалена
                " where " +
                " id= "+request.getId();
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method updateKassa. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteKassa (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(24L,"299") && securityRepositoryJPA.isItAllMyMastersDocuments("kassa",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(24L,"300") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("kassa",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(24L,"301") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("kassa",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update kassa p" +
                    " set is_deleted=true, " + //удалена
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now() " +//дату и время изменения
                    " where p.id in ("+delNumbers+")";
            entityManager.createNativeQuery(stringQuery).executeUpdate();
            return true;
        } else return false;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean undeleteKassa(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(24L,"299") && securityRepositoryJPA.isItAllMyMastersDocuments("kassa",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(24L,"300") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("kassa",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(24L,"301") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("kassa",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update kassa p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                query.executeUpdate();
                return true;
            } else return false;
        } else return false;
    }
//отдает список касс (не удаленных) по id отделения
    @SuppressWarnings("Duplicates")
    public List<KassaJSON> getKassaListByDepId(Long departmentId){
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            Long myCompanyId=userRepositoryJPA.getMyCompanyId_();
            stringQuery = "select p.id as id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           p.name as name, " +
                    "           p.server_type as server_type, " +
                    "           p.sno1_id as sno1_id, " +
                    "           p.device_server_uid as device_server_uid, " +
                    "           p.server_address as server_address, " +
                    "           (select name_api_atol from sprav_sys_taxation_types where id=p.sno1_id) as name_api_atol, "+
                    "           cmp.name as company_name, "+
                    "           cmp.email as company_email, "+
                    "           cmp.jr_inn as company_vatin, "+
                    "           p.billing_address as billing_address, " +
                    "           p.zn_kkt as zn_kkt" +
                    "           from kassa p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           where  p.master_id = " + myMasterId +
                    "           and p.department_id = " + departmentId +
//                    "           and p.company_id = " + myCompanyId +
                    "           and coalesce(p.is_deleted, false) = false " + //касса не удалена
                    "           and coalesce(p.allow_to_use, false) = true " + // и разрешена к использованию
                    "           and p.department_id in :myDepthsIds" + //чтобы нельзя было получить список касс по произвольному id отделения, не принадлежащему пользователю
                    "           order by p.name ";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId_LONG());

                List<Object[]> queryList = query.getResultList();
                List<KassaJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    KassaJSON doc=new KassaJSON();
                    doc.setId(Long.parseLong(                     obj[0].toString()));
                    doc.setCompany_id(Long.parseLong(             obj[1].toString()));
                    doc.setDepartment_id(Long.parseLong(          obj[2].toString()));
                    doc.setName((String)                          obj[3]);
                    doc.setServer_type((String)                   obj[4]);
                    doc.setSno1_id((Integer)                      obj[5]);
                    doc.setDevice_server_uid((String)             obj[6]);
                    doc.setServer_address((String)                obj[7]);
                    doc.setSno1_name_api_atol((String)            obj[8]);
                    doc.setCompany_name((String)                  obj[9]);
                    doc.setCompany_email((String)                 obj[10]);
                    doc.setCompany_vatin((String)                 obj[11]);
                    doc.setBilling_address((String)               obj[12]);
                    doc.setZn_kkt((String)                        obj[13]);
                    returnList.add(doc);
                }
                return returnList;
            }catch (Exception e){
                logger.error("Exception in method getKassaListByDepId. SQL query:"+stringQuery, e);
                e.printStackTrace();
                throw e;
            }
    }

    // отдает список касс ККМ по отделениям, которые привязаны к кассе предприятия boxofficeId
    public List<KassaJSON> getKassaListByBoxofficeId(Long boxofficeId){
        List<Long> depthsListId = getDepthListOfBoxofficeId(boxofficeId);
        if(!Objects.isNull(depthsListId)) {
            List<KassaJSON> returnList = new ArrayList<>();
            if (depthsListId.size() > 0) { // если к кассе привязаны отделения
                String depths = commonUtilites.ListOfLongToString(depthsListId, ",", "(", ")");

                String stringQuery;
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                stringQuery = "select p.id as id, " +
                        "           p.name as name " +
                        "           from kassa p " +
                        "           where  p.master_id = " + myMasterId +
                        "           and p.department_id in " + depths +
                        "           and coalesce(p.is_deleted, false) = false " + //касса не удалена
                        "           order by p.name ";
                try {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    List<Object[]> queryList = query.getResultList();
                    for (Object[] obj : queryList) {
                        KassaJSON doc = new KassaJSON();
                        doc.setId(Long.parseLong(obj[0].toString()));
                        doc.setName((String) obj[1]);
                        returnList.add(doc);
                    }
                    return returnList;
                } catch (Exception e) {
                    logger.error("Exception in method getKassaListByBoxofficeId. SQL query:" + stringQuery, e);
                    e.printStackTrace();
                    return null;
                }
            } else return returnList;
        } else return null;
    }

    // отдает список id отделений, привязанных к кассе предприятия boxofficeId (getDepartmentsListByBoxofficeId getDepartmentListByBoxofficeId)
    private List<Long> getDepthListOfBoxofficeId(Long boxofficeId){
        String stringQuery = "select p.id, p.name as name from departments p where p.boxoffice_id=" +boxofficeId; // p.name надо чтобы был List<Object>
        List<Long> returnList = new ArrayList<>();
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            for(Object[] obj:queryList){
                returnList.add(Long.parseLong(obj[0].toString()));
            }
            return returnList;
        } catch (NoResultException nre) {
            return returnList;
        } catch (Exception e){
            logger.error("Exception in method getDepthListOfBoxofficeId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }






   //отдает настройки кассира по выбранной им кассе
    @SuppressWarnings("Duplicates")
    public KassaCashierSettingsJSON getKassaCashierSettings() {
        String stringQuery;
        Long myId = userRepositoryJPA.getMyId();
        stringQuery = "select " +
                "           p.selected_kassa_id as selected_kassa_id, " + // id выбранной кассы
                "           p.cashier_value_id as cashier_value_id, " + // кассир: 'current'-текущая учетная запись, 'another'-другая учетная запись, 'custom' произвольные ФИО. Настройка "Другая учетная запись" во фронтенде сохраняется только до конца сессии, затем ставится по умолчанию current
                "           p.customCashierFio as customCashierFio, " + // ФИО для кассира, выбранного по cashier_value_id = 'custom'
                "           p.customCashierVatin as customCashierVatin, " + // ИНН для кассира, выбранного по cashier_value_id = 'custom'
                "           p.billing_address as billing_address, " + //выбор адреса места расчётов. 'Settings' - как в настройках кассы, 'customer' - брать из адреса заказчика, 'custom' произвольный адрес
                "           p.custom_billing_address as custom_billing_address " + //адрес места расчётов для billing_address = 'custom'
                "           from kassa_user_settings p " +
                "           where  p.user_id = " + myId;

        Query query = entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();

        KassaCashierSettingsJSON doc=new KassaCashierSettingsJSON();

        for(Object[] obj:queryList){
            doc.setSelected_kassa_id(Long.parseLong(obj[0].toString()));
            doc.setCashier_value_id((String)        obj[1]);
            doc.setCustomCashierFio((String)        obj[2]);
            doc.setCustomCashierVatin((String)      obj[3]);
            doc.setBilling_address((String)         obj[4]);
            doc.setCustom_billing_address((String)  obj[5]);
        }
        return doc;
    }
    //сохраняет настройки кассира. У каждой учетки может быть только одна настройка кассира, и user_id в kassa_user_settings является первичным ключом
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean updateCashierSettings(KassaCashierSettingsForm row) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into kassa_user_settings (" +
                            " user_id, " +
                            " master_id, " +
                            " company_id, " +
                            " selected_kassa_id, " + // id выбранной кассы
                            " cashier_value_id, " + // кассир: 'current'-текущая учетная запись, 'another'-другая учетная запись, 'custom' произвольные ФИО. Настройка "Другая учетная запись" во фронтенде сохраняется только до конца сессии, затем ставится по умолчанию current
                            " customCashierFio, " + // ФИО для кассира, выбранного по cashier_value_id = 'custom'
                            " customCashierVatin, " + // ИНН для кассира, выбранного по cashier_value_id = 'custom'
                            " billing_address, " + //выбор адреса места расчётов. 'Settings' - как в настройках кассы, 'customer' - брать из адреса заказчика, 'custom' произвольный адрес
                            " custom_billing_address " + //адрес места расчётов для billing_address = 'custom'
                            ") values (" +
                            myId + "," +
                            myMasterId + ", " +
                            myCompanyId + ", " +
                            row.getSelected_kassa_id() + ", '" +
                            row.getCashier_value_id() + "', '" +
                            (row.getCustomCashierFio() == null ? "":row.getCustomCashierFio())  + "', '" +
                            (row.getCustomCashierVatin() == null ? "":row.getCustomCashierVatin())  + "', '" +
                            row.getBilling_address() + "', '" +
                            row.getCustom_billing_address() + "'" +
                            ") " +
                            " ON CONFLICT ON CONSTRAINT kassa_user_settings_pkey " +// "upsert"
                            " DO update set " +
                            " selected_kassa_id = " + row.getSelected_kassa_id() + ","+
                            " cashier_value_id = '" + row.getCashier_value_id() + "',"+
                            " customCashierFio = '" + (row.getCustomCashierFio() == null ? "":row.getCustomCashierFio()) + "',"+
                            " customCashierVatin = '" + (row.getCustomCashierVatin() == null ? "":row.getCustomCashierVatin()) + "'," +
                            " billing_address = '" + (row.getBilling_address() == null ? "":row.getBilling_address()) + "', "+
                            " custom_billing_address = '" + (row.getCustom_billing_address() == null ? "":row.getCustom_billing_address()) + "'";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Boolean isKassaUnique (String zn_kkt, Long company_id, Long current_kassa_id){
        String stringQuery;
        stringQuery = "" +
                " select 1 from kassa where " +
                " company_id="+company_id+
//                " and id!="+current_kassa_id +
                " and zn_kkt ='"+zn_kkt + "'";
        if(current_kassa_id>0) stringQuery=stringQuery+" and id!="+current_kassa_id;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return(query.getResultList().size()==0);
        }catch (Exception e) {
            logger.error("Exception in method isKassaUnique. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // запись в БД состояния смены (создание новой смены или изменение её статуса)
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class})
    public Boolean updateShiftStatus(String zn_kkt, String shiftStatusId, Long shiftNumber, String shiftExpiredAt, Long companyId, Long kassaId, String fnSerial) {
        // Из фронтэнда может прийти номер смены (shiftNumber) = 0, если в кассе не установлен ФН. Это возможно в режиме разработки. В этом случае номер смены получаем из счетчика (если статус смены closed - текущее значение, если не closed - текущее значение +1)
        if(shiftNumber==0){shiftNumber=(shiftStatusId.equals("closed")?getShiftNumber_DevMode("currval"):getShiftNumber_DevMode("nextval"));}
        //если смена закрыта, то смысл изменять ее статус в БД отсутствует
        if (!isShiftClosed(kassaId, shiftNumber, fnSerial)){
            // т.е. тут либо смена есть и она открыта, либо ее вообще нет








            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepository.getUserId();
            Long kassaDeptId = getKassaDeptIdByIdKkt(kassaId);// id  отделения кассы на момент изменения статуса смены
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();

            stringQuery =
                    " insert into shifts (" +
                            " master_id, " +
                            " creator_id, " +   // открыватель смены в БД (он же - открыватель смены в ККМ, кроме редких теоретических исключений, когда смена в ККМ открыта кем-то где-то, а потом ККМ подключили к компьютеру)
                            " closer_id, " +    // пользователь, закрывающий смену (опять же кроме редких исключений. Если смена была закрыта где-то еще, при опросе ККМ на форнтэнде выдается
                                                // номер текущей смены или номер последней закрытой смены, и эта закрытая смена так же запишется в БД,
                                                // и открывателем-закрывателем смены в БД будет считаться залогиненный пользователь, хотя он фактически эту смену не открывал/закрывал)
                            " date_time_created, " + // дата и время открытия смены (по-сути это время записи об открытии смены. В некоторых случаях может отличаться от фактического времени)
                            " date_time_closed, " +// дата и время закрытия смены (аналогично предыдущему)
                            " company_id, " + // id предприятия
                            " department_id, " + // id  отделения кассы на момент изменения статуса смены
                            " kassa_id, " + //id кассы в БД
                            " shift_number, " + // номер смены (по фискальному накопителю)
                            " zn_kkt, " + //заводской номер кассы
                            " uid, " + //UUID
                            " shift_status_id, " + // статус смены: opened closed expired
                            " shift_expired_at, " + // время истечения (экспирации) смены в текстовом формате, генерируемом самой ККТ.
                            " fn_serial" + //Серийный номер ФН. Вместе с kassa_id и shift_number используется для уникальности смены (constraint kassaid_shiftnumber_fnserial_uq),
                            // т.к. shift_number при смене ФН сбрасывается.
                            /*Онлайн-кассы не имеют фискальной памяти, все фискальные данные хранятся в фискальном накопителе (ФН), который является временным,
                            сменным элементом, и отправляются в ФНС через оператора фискальных данных (ОФД). Поэтому при каждой замене ФН
                            номер смены начинается с 1 и имеет сквозную нумерацию вплоть до очередной замены ФН.*/
                            ") values (" +
                            myMasterId + ", " +
                            myId + ", " +
                            ((shiftStatusId.equals("closed")) ? myId : null) + ", " +
                            "to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания записи об открытии смены (если запись создается при фактическом открытии смены
                            (shiftStatusId.equals("closed") ? ("to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')") : null) + ", " +
                            companyId + ", " +
                            kassaDeptId + ", " +
                            kassaId + ", " +
                            shiftNumber + ", '" +
                            zn_kkt + "', '" +
                            UUID.randomUUID().toString() + "', '" +
                            shiftStatusId + "', '" +
                            shiftExpiredAt + "', '" +
                            fnSerial + "'" +
                            ") " +
                            " ON CONFLICT ON CONSTRAINT kassaid_shiftnumber_fnserial_uq " +// "upsert"
                            " DO update set " +
                            " closer_id = " + ((shiftStatusId.equals("closed")) ? myId : null) + ", " +
                            " date_time_closed = " + (!shiftStatusId.equals("closed") ? null : ("to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')")) + ", " + // время закрытия вставляем только если статус смены меняется на closed (может меняться с opened ещё и на expired, а это не зактыта)
                            " department_id = " + kassaDeptId + ", " +//потому что отделение за время сессии может измениться (маловероятно, но всё же)
                            " shift_status_id = '" + shiftStatusId + "'";
            try {
                // нам нужно отловить закрытие смены (т.е. что смена закроется в результате запроса в этом методе
                // для этого она должна подходить под требования констрайнта kassaid_shiftnumber_fnserial_uq
                // т.е. в БД уже должна быть смена с такими kassaid, shiftnumber и fnserial
                ShiftsJSON shift =  doesShiftExists (kassaId, shiftNumber, fnSerial);




                // Если смена есть (пришел ее объект)
                if(shift!=null){
                    // До отработки данного запроса смена уже была, значит запрос отработал на ее заакрытие
                    // значит нужно создать все необходимые документы по эквайрингу:
                        // -  Корректировка (Создадим задолженность банка по операциям эквайринга, возникшую после закрытия торговой смены)
                        // -  Входящий платеж (от банка), 100% перекрывающий (равный) корректировке
                        // -  Исходящий платеж (%% банку за услугу эквайринга), без НДС
                        // -  Приёмка услуги на сумму, равную %% банку за услугу эквайринга

                    Long shiftId = shift.getId();
                    String shiftUUID = shift.getUid();
                    // Получим информацию по эквайрингу на данной ККТ:
                    AcquiringInfoJSON acquiringInfo = getAcquiringInfo(kassaId);
                    // На основе данной информации создадим набор документов для эквайринга
                    createAcquiringDocPack(myId, myMasterId, companyId, kassaId, shiftId, shiftUUID, acquiringInfo);

                }
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                closeOtherShifts(zn_kkt, shiftNumber, companyId, timestamp); //закрываем другие смены этой ККТ (подробнее - в методе)
                return true;
            } catch (Exception e) {
                logger.error("Exception in method updateShiftStatus. SQL query:" + stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return true;
    }


    // Создает документы для эквайринга у закрытой торговой смены
    // -  Корректировка (Создадим задолженность банка по операциям эквайринга, возникшую после закрытия торговой смены)
    // -  Входящий платеж (от банка), 100% перекрывающий (равный) корректировке
    // -  Исходящий платеж (%% банку за услугу эквайринга), без НДС
    // -  Приёмка услуги на сумму, равную %% банку за услугу эквайринга
    private Boolean createAcquiringDocPack(Long myId, Long masterId, Long companyId, Long kassaId, Long shiftId, String shiftUUID, AcquiringInfoJSON acquiringInfo) throws Exception {

        try {
            // Загрузим все платежи за данную смену
            List<ReceiptsJSON> shiftReceipts = getShiftReceipts(masterId, kassaId, shiftId);
            // Посчитаем:
            BigDecimal sum = new BigDecimal(0); // общую сумму по электронным платежам за смену (входящие - положительные, исходящие - отрицательные)
            BigDecimal sum_proc = new BigDecimal(0); // сумму по процентам за платежи по покупкам (приход)
            Long acceptanceId = null;
            Long paymentinId  = null;
            Long paymentoutId = null;
            Long correctionId = null;
            for(ReceiptsJSON shiftReceipt: shiftReceipts){
                // если есть действия с безналом
                if(!Objects.isNull(shiftReceipt.getElectronically()))
                    switch (shiftReceipt.getOperation_id()){
                        case "sell":
                            sum_proc=sum_proc.add(shiftReceipt.getElectronically().multiply(acquiringInfo.getAcquiring_precent()).divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP));
                            sum=sum.add(shiftReceipt.getElectronically());
                            break;
                        case "sell_return":
                            sum=sum.subtract(shiftReceipt.getElectronically());
                            break;
                    }
            }

            // создаем Приёмку на услугу эквайринга от банка
            if(sum_proc.compareTo(new BigDecimal(0))!=0) {
                Set<AcceptanceProductForm> acceptanceProductTable = new HashSet<>();
                AcceptanceForm acceptanceForm = new AcceptanceForm();
                AcceptanceProductForm apf = new AcceptanceProductForm();
                String acceptance_UUID = UUID.randomUUID().toString();
                apf.setAcceptance_id(0L);
                apf.setNds_id(1);
                apf.setProduct_count(new BigDecimal(1));
                apf.setProduct_id(acquiringInfo.getAcquiring_service_id());
                apf.setProduct_netcost(sum_proc);
                apf.setProduct_price(sum_proc);
                apf.setProduct_sumprice(sum_proc);
                acceptanceProductTable.add(apf);
                acceptanceForm.setDepartment_id(getMyDepartmentId(myId));
                acceptanceForm.setAcceptanceProductTable(acceptanceProductTable);
                acceptanceForm.setCagent_id(acquiringInfo.getAcquiring_bank_id());
                acceptanceForm.setCompany_id(companyId);
                acceptanceForm.setDescription("Приёмка услуги эквайринга. Документ создан автоматически");
                acceptanceForm.setNds(false);
                acceptanceForm.setNds_included(true);
                acceptanceForm.setOverhead(new BigDecimal(0));
                acceptanceForm.setOverhead_netcost_method(0);
                acceptanceForm.setLinked_doc_id(shiftId);
                acceptanceForm.setLinked_doc_name("shifts");
                acceptanceForm.setParent_uid(shiftUUID);
                acceptanceForm.setChild_uid(acceptance_UUID);
                acceptanceForm.setUid(acceptance_UUID);
                acceptanceId = acceptanceRepository.insertAcceptance(acceptanceForm);
            }
            // Если образовались проценты по платежам - создаём Исходящий платеж
            if(sum_proc.compareTo(new BigDecimal(0))>0) {
                PaymentoutForm paymentout = new PaymentoutForm();
                String paymentout_UUID = UUID.randomUUID().toString();
                paymentout.setCompany_id(companyId);
                paymentout.setPayment_account_id(acquiringInfo.getPayment_account_id());
                paymentout.setCagent_id(acquiringInfo.getAcquiring_bank_id());
                paymentout.setExpenditure_id(acquiringInfo.getExpenditure_id());
                paymentout.setSumm(sum_proc);
                paymentout.setNds(new BigDecimal(0));
                paymentout.setDescription("Расходы по уплате комиссионных банку за услуги эквайринга. Документ создан автоматически");
                paymentout.setLinked_doc_id(shiftId);
                paymentout.setLinked_doc_name("shifts");
                paymentout.setParent_uid(shiftUUID);
                paymentout.setChild_uid(paymentout_UUID);
                paymentout.setUid(paymentout_UUID);
                paymentoutId = paymentoutRepository.insertPaymentout(paymentout);
            }
            // Если есть чему поступать на счёт - создаем Входящий платёж и, для уравнивания баланса, Корректировку, которая создаст задолженность банка по операциям эквайринга, возникшую после закрытия торговой смены
            if(sum.compareTo(new BigDecimal(0))>0){
                String paymentin_UUID = UUID.randomUUID().toString();
                PaymentinForm paymentin = new PaymentinForm();
                paymentin.setCompany_id(companyId);
                paymentin.setPayment_account_id(acquiringInfo.getPayment_account_id());
                paymentin.setInternal(false);
                paymentin.setCagent_id(acquiringInfo.getAcquiring_bank_id());
                paymentin.setSumm(sum);
                paymentin.setNds(new BigDecimal(0));
                paymentin.setDescription("Поступление по эквайрингу платежей, принятых в течении торговой смены. Документ создан автоматически");
                paymentin.setLinked_doc_id(shiftId);
                paymentin.setLinked_doc_name("shifts");
                paymentin.setParent_uid(shiftUUID);
                paymentin.setChild_uid(paymentin_UUID);
                paymentin.setUid(paymentin_UUID);
                paymentinId = paymentinRepository.insertPaymentin(paymentin);
                // Корректировка -  для созданиия задолженности банка по операциям эквайринга
                CorrectionForm correction = new CorrectionForm();
                String corretion_UUID = UUID.randomUUID().toString();
                correction.setCompany_id(companyId);
                correction.setCagent_id(acquiringInfo.getAcquiring_bank_id());
                correction.setSumm(sum.negate());// т.к. "Нам должны" - это отрицательная сумма баланса, нужно отправить в Корректировку отрицательную сумму
                correction.setType("cagent");
                correction.setDescription("Корректировка для созданиия задолженности банка по операциям эквайринга, проведённым в течении торговой смены. Документ создан автоматически");
                correction.setLinked_doc_id(shiftId);
                correction.setLinked_doc_name("shifts");
                correction.setParent_uid(shiftUUID);
                correction.setChild_uid(corretion_UUID);
                correction.setUid(corretion_UUID);
                correctionId = correctionRepository.insertCorrection(correction);
            }
            // запишем в смену информацию об всех созданных документах:
            afterShiftClose(acceptanceId, paymentinId, paymentoutId, correctionId, shiftId, masterId);

            return true;
        } catch (Exception e) {
            logger.error("Exception in method createAcquiringDocPack.", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // после закрытия смены сохраняет id-шники документов, созданных для поддержки эквайринговых операций
    public Boolean afterShiftClose(Long acceptanceId, Long paymentinId, Long paymentoutId, Long correctionId, Long shiftId, Long masterId) throws Exception {
        String stringQuery;
        stringQuery =
                " update shifts set acqu_acceptance_id = "+acceptanceId+", "+
                " acqu_paymentin_id = "+paymentinId+", "+
                " acqu_paymentout_id = "+paymentoutId+", "+
                " acqu_correction_id = "+correctionId+
                " where id="+shiftId+" and master_id="+masterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Exception in method afterShiftClose. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    // отдает чеки за смену
    private List<ReceiptsJSON> getShiftReceipts(Long masterId, Long kassaId, Long shiftId) throws Exception {
        String stringQuery;
        stringQuery =
                " select operation_id, cash, electronically from receipts where " +
                        " master_id = " + masterId + " and " +
                        " kassa_id = " + kassaId + " and " +
                        " shift_id = " + shiftId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<ReceiptsJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                ReceiptsJSON doc=new ReceiptsJSON();
                doc.setOperation_id((String)                  obj[0]);
                doc.setCash((BigDecimal)                      obj[1]);
                doc.setElectronically((BigDecimal)            obj[2]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getShiftReceipts. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }


    }



    // возвращает информацию по эквайрингу у кассы   ***
    private AcquiringInfoJSON getAcquiringInfo(Long kassaId){

        String stringQuery;
        stringQuery = "" +
                " select acquiring_bank_id, acquiring_precent, acquiring_service_id, expenditure_id, payment_account_id from kassa where " +
                " id="+kassaId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            AcquiringInfoJSON doc = new AcquiringInfoJSON();
            for(Object[] obj:queryList) {
                doc.setAcquiring_bank_id(obj[0]!=null?Long.parseLong(obj[0].toString()):null);
                doc.setAcquiring_precent(obj[1]!=null?((BigDecimal) obj[1]):new BigDecimal(0));
                doc.setAcquiring_service_id(obj[2]!=null?Long.parseLong(obj[2].toString()):null);
                doc.setExpenditure_id(obj[3]!=null?Long.parseLong(obj[3].toString()):null);
                doc.setPayment_account_id(obj[4]!=null?Long.parseLong(obj[4].toString()):null);
            }
            return doc;
        }catch (Exception e) {
            logger.error("Exception in method getAcquiringInfo. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    // возвращает один id отделения сотрудника. Нужен для создания Приёмки
    public Long getMyDepartmentId(Long myId) {
        String stringQuery="" +
                "select p.department_id as did" +
                " from " +
                " user_department p," +
                " departments dpts" +
                " where " +
                " p.user_id= "+myId+
                " and p.department_id=dpts.id " +
                " and coalesce(dpts.is_deleted,false)!=true limit 1";
        Query query = entityManager.createNativeQuery(stringQuery);
        try
        {
            return Long.parseLong(query.getSingleResult().toString());
        }catch (Exception e) {
            logger.error("Exception in method getMyDepartmentId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    //возвращает id смены, если такая смена с данными параметрами есть в БД
    private ShiftsJSON doesShiftExists (Long kassaId, Long shift_number, String fnSerial) throws Exception {
        String stringQuery;
        stringQuery =
                " select id, uid from shifts where " +
                " kassa_id="+kassaId+
                " and shift_number="+shift_number +
                " and fn_serial ='"+fnSerial + "'";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            ShiftsJSON doc = new ShiftsJSON();
            for(Object[] obj:queryList) {
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setUid((String) obj[1]);
            }
            return doc;
        } catch (NoResultException nre) {
            return null;
        }catch (Exception e) {
            logger.error("Exception in method doesShiftExists. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    //запись в БД информации об выбитом на ККМ чеке
    public Boolean addReceipt(String zn_kkt,
                              String shiftStatusId,
                              Long shiftNumber,
                              String shiftExpiredAt,
                              Long companyId,
                              Long kassaId,
                              String fnSerial,
                              String operationId,
                              String sno,
                              String billing_address,
                              String payment_type,
                              BigDecimal cash,
                              BigDecimal electronically,
                              Long id,
                              int docId) {

        // Из фронтэнда может прийти номер смены (shiftNumber) = 0, если в кассе не установлен ФН.
        // Это возможно в режиме разработки. В этом случае номер смены получаем из счетчика (если смена closed - текущее значение, если не closed - текущее значение +1)
        if(shiftNumber==0L){
            // при этом смена в БД может быть как открыта, так и закрыта
            if(isShiftOpened_DevMode(kassaId, fnSerial)){//если она откртыта - берём текущий номер из счетчика смен
                shiftNumber=getShiftNumber_DevMode("currval");
            } else { //если смена в БД закрыта - нужно сгенерировать новую смену, а затем по успешности опять же взять текущий номер из счетчика
                if(updateShiftStatus(zn_kkt, shiftStatusId, shiftNumber, shiftExpiredAt, companyId, kassaId, fnSerial)){
                    shiftNumber=getShiftNumber_DevMode("currval");
                } else return false;// в случае ошибки создания смены в БД
            }
        }else{// "рабочий" режим кассы. Смена в БД при этом может быть как открыта, так и отсутствовать, если это первый чек смены. Тогда ее нужно сначала создать, и получить ее номер
        //проверим, что смена с таким shiftNumber есть в БД
            // и если она не открыта или её нет...
            if (!isShiftOpened(kassaId, fnSerial, shiftNumber)){
                //открываем смену в БД
                updateShiftStatus(zn_kkt, shiftStatusId, shiftNumber, shiftExpiredAt, companyId, kassaId, fnSerial);
            } // в противном случае ничего не делаем, т.к. присланный чек принадлежит к уже открытой в БД смене
        }

        //получаем id смены

        Long shiftId=getShiftIdByUniqueKey(kassaId, shiftNumber, fnSerial);
        // Сейчас есть все данные для записи чека в БД как в боевом режиме, так и в режиме разработчика

        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId = userRepository.getUserId();
        Long kassaDeptId = getKassaDeptIdByIdKkt(kassaId);// id  отделения кассы на момент изменения статуса смены
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        Integer sno_id = getSnoIdBySnoName("name_api_atol",sno);

        stringQuery =
                " insert into receipts (" +
                        " master_id, " +
                        " creator_id, " +           // отбиватель чека
                        " date_time_created, " +    // дата и время отбития чека
                        " company_id, " +           // id предприятия
                        " department_id, " +        // id  отделения кассы, в котором был отбит чек
                        " kassa_id, " +             // id кассы в БД
                        " shift_id, "  +            // id смены
                        " document_id, "  +         // id документа из таблицы documents, из которого был отбит чек. Например, Розничные продажи id = 25
                        " retail_sales_id, " +      // id Розничной продажи, если чек отбивался в данном документе. Впоследствии, когда чек можно будет отбивать не только из Розничных продаж, нужно будет собирать эту часть строки в зависимости от docId
                        " operation_id, "  +        // id торговой операции (sell buy и т.д.)
                        " sno_id, "  +              // id системы налогообложения (sprav_sys_taxation_types)
                        " billing_address, "  +     // адрес места расчета
                        " payment_type, "  +        // способ расчёта (cash, electronically, mixed)
                        " cash, "  +                // сколько заплатили наличными
                        " electronically"  +        // сколько заплатили электронными
                        ") values (" +
                        myMasterId + ", " +
                        myId + ", " +
                        "to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')," +
                        companyId + ", " +
                        kassaDeptId + ", " +
                        kassaId + ", " +
                        shiftId + ", " +
                        docId + ", " +
                        id + ", " +
                        "'"+ operationId + "', " +
                        sno_id + ", " +
                        "'" + billing_address + "', " +
                        "'" + payment_type + "', " +
                        cash + ", " +
                        electronically +
                        ") ";

        try {

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            closeOtherShifts(zn_kkt, shiftNumber, companyId, timestamp); //закрываем другие смены этой ККТ (подробнее - в методе)
            return true;
        } catch (Exception e) {
            logger.error("Exception in method addReceipt. SQL query:" + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }




    // Может случиться такое, что смена закроется в ККМ, но не закроется в БД по какой либо причине.
    // Например, пропала связь с сервером во время закрытия смены, либо при смене ФН текущую смену забыли закрыть на рабочем месте кассира, и закрыли уже сервисном центре.
    // Получается рассинхрон состояний смен.
    // Впоследствии эта смена так и останется незакрытой в БД. Данный запрос закрывает все смены ККТ по ее ЗН, кроме текущей (если она открыта)
    private int closeOtherShifts(String zn_kkt, Long shiftNumber, Long companyId, String timestamp){
        String stringQuery;
        stringQuery = "" +
                " update shifts set " +
                " shift_status_id='closed', " +
                " closer_id=1, " + //1 - это всегда Система Докио
                " date_time_closed = to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS') " +
                " where "+
                " company_id=" + companyId + //это берем чтобы БД быстрее искала смены
                " and shift_number!=" + shiftNumber +
                " and shift_status_id != 'closed'" +
                " and zn_kkt ='" + zn_kkt + "'";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return(query.executeUpdate());
        }catch (Exception e) {
            logger.error("Exception in method closeOtherShifts. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return 0;
        }
    }

    // возвращает номер смены для работы с ККМ в режиме разработчика. Т.к. в кассе может не быть ФН, то номер смены будет всегда 0, что не приемлемо.
    // принимает currval и nextval
    private Long getShiftNumber_DevMode(String function){
        String stringQuery;
        if(function.equals("currval")){
            stringQuery = "select last_value FROM developer_shiftnum";//потому что нельзя просто так взять и получить currval
        } else
            stringQuery = "select "+function+"('developer_shiftnum')";
        try{
            return Long.valueOf(entityManager.createNativeQuery(stringQuery).getSingleResult().toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getKassaIdByZnKkt. SQL query:" + stringQuery, e);
            return 0L;
        }
    }

    // В режиме разработчика возвращает состояние сессии (если есть открытая сессия - true, если нет - false.
    // Т.к. чек можно печатать и из закрытой сессии в ККМ, и она откроется в ККМ при печати 1го чека, то возможна ситуация, когда приходят данные по чеку для записи в БД,
    // но в БД сессия еще не открыта
    private Boolean isShiftOpened_DevMode(Long kassaId, String fnSerial){
        String stringQuery;
        stringQuery = "" +
                " select 1 from shifts where " +
                " kassa_id="+kassaId+
                " and fn_serial ='"+fnSerial + "'" +
                " and shift_status_id = 'opened'";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return(query.getResultList().size()==1);//если находит такую строку в БД - значит есть открытая смена по данной кассе
        }catch (Exception e) {
            logger.error("Exception in method isShiftOpened_DevMode. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

    // Возвращает состояние сессии (если есть открытая сессия - true, если нет - false.
    private Boolean isShiftOpened(Long kassaId, String fnSerial, Long shiftNumber){
        String stringQuery;
        stringQuery = "" +
                " select 1 from shifts where " +
                " kassa_id="+kassaId+
                " and fn_serial ='"+fnSerial + "'" +
                " and shift_status_id = 'opened'" +
                " and shift_number = " + shiftNumber;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return(query.getResultList().size()==1);//если находит такую строку в БД - значит есть открытая смена по данной кассе
        }catch (Exception e) {
            logger.error("Exception in method isShiftOpened. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }

// Возвращает id предприятия кассы по ее id
//    private Long getKassaCompanyIdByIdKkt(Long id){
//        String stringQuery = "select k.company_id from kassa k where k.id = "+ id;
//        try{
//            return Long.valueOf(entityManager.createNativeQuery(stringQuery).getSingleResult().toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getKassaCompanyIdByIdKkt. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }

    //возвращает id сессии по уникальному ключу в БД kassaid_shiftnumber_fnserial_uq
    private Long getShiftIdByUniqueKey(Long kassaId, Long shift_number, String fnSerial){
        String stringQuery;
        stringQuery = "" +
                " select id from shifts where " +
                " kassa_id="+kassaId+
                " and shift_number="+shift_number +
                " and fn_serial ='"+fnSerial + "'";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return  Long.valueOf(query.getSingleResult().toString());
        }catch (Exception e) {
            logger.error("Exception in method getShiftIdByUniqueKey. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    // Возвращает id отделения кассы по ее id
    private Long getKassaDeptIdByIdKkt(Long id){
        String stringQuery = "select k.department_id from kassa k where k.id = "+ id;
        try{
            return Long.valueOf(entityManager.createNativeQuery(stringQuery).getSingleResult().toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getKassaDeptIdByIdKkt. SQL query:" + stringQuery, e);
            return null;
        }
    }

    //возвращает true если смена закрыта. Используется для апдейта состояния смены (если она закрыта, то никакие апдейты делать смысла нет)
    public Boolean isShiftClosed (Long kassaId, Long shift_number, String fnSerial){
        String stringQuery;
        stringQuery = "" +
                " select 1 from shifts where " +
                " kassa_id="+kassaId+
                " and shift_number="+shift_number +
                " and fn_serial ='"+fnSerial + "'" +
                " and shift_status_id = 'closed'";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return(query.getResultList().size()==1);//если находит такую строку в БД - значит смена есть и она закрыта. В противном случае смены в БД либо нет, либо она открыта, и вызывающий метод будет делать "upsert"
        }catch (Exception e) {
            logger.error("Exception in method isShiftClosed. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return false;
        }
    }
    //возвращает id системы налогообложения по её наименованию для разных API касс (например для API Атол отправляем "name_api_atol, osn")
    private Integer getSnoIdBySnoName(String column_name, String name){
        String stringQuery;
        stringQuery = "" +
                " select id from sprav_sys_taxation_types where " +
                column_name + " = '" + name + "'";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return  (Integer) query.getSingleResult();
        }catch (Exception e) {
            logger.error("Exception in method getSnoIdBySnoName. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    //возвращает true если смена есть в БД
//    public Boolean isShiftExisted (Long kassaId, int shift_number, String shift_expired_at){
//        String stringQuery;
//        stringQuery = "" +
//                " select 1 from shifts where " +
//                " kassa_id="+kassaId+
//                " and shift_number="+shift_number +
//                " and shift_expired_at ='"+shift_expired_at + "'";
//        try
//        {
//            Query query = entityManager.createNativeQuery(stringQuery);
//            return(query.getResultList().size()==0);
//        }catch (Exception e) {
//            logger.error("Exception in method isShiftExisted. SQL query:"+stringQuery, e);
//            e.printStackTrace();
//            return false;
//        }
//    }
    // Возвращает id кассы по ее заводскому номеру в предприятии
//    private Long getKassaIdByZnKkt(String zn_kkt, Long companyId){
//        String stringQuery = "select coalesce(k.id,0) from kassa k where k.zn_kkt=" + zn_kkt + " and k.company_id = "+ companyId;
//        try{
//            return Long.valueOf(entityManager.createNativeQuery(stringQuery).getSingleResult().toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("Exception in method getKassaIdByZnKkt. SQL query:" + stringQuery, e);
//            return null;
//        }
//    }
//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToKassa(UniversalForm request){
        Long kassaId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(24L,"305") && securityRepositoryJPA.isItAllMyMastersDocuments("kassa",kassaId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(24L,"306") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("kassa",kassaId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(24L,"307") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("kassa",kassaId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select kassa_id from kassa_files where kassa_id=" + kassaId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_KassaId_FileId(kassaId,fileId);
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    boolean manyToMany_KassaId_FileId(Long kassaId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into kassa_files " +
                    "(kassa_id,file_id) " +
                    "values " +
                    "(" + kassaId + ", " + fileId +")")
                    .executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates") //отдает информацию по файлам, прикрепленным к документу
    public List<FilesUniversalJSON> getListOfKassaFiles(Long kassaId) {
        if(securityRepositoryJPA.userHasPermissions_OR(24L, "302,303,304"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           kassa p" +
                    "           inner join" +
                    "           kassa_files pf" +
                    "           on p.id=pf.kassa_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + kassaId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(24L, "302")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения
                if (!securityRepositoryJPA.userHasPermissions_OR(24L, "303")) //Если нет прав на просм по своему предприятию
                {//остается только на просмотр всех доков в своих отделениях (304)
                    stringQuery = stringQuery + " and p.company_id=" + myCompanyId+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;
                }//т.е. по всем и своему предприятиям нет а на свои отделения есть
                else stringQuery = stringQuery + " and p.company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            List<FilesUniversalJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                FilesUniversalJSON doc=new FilesUniversalJSON();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setDate_time_created((Timestamp)                    obj[1]);
                doc.setName((String)                                    obj[2]);
                doc.setOriginal_name((String)                           obj[3]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteKassaFile(Long kassa_id, Long file_id)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(24L,"305") && securityRepositoryJPA.isItAllMyMastersDocuments("kassa", String.valueOf(kassa_id))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(24L,"306") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("kassa",String.valueOf(kassa_id)))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(24L,"307") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("kassa",String.valueOf(kassa_id))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from kassa_files "+
                    " where kassa_id=" + kassa_id+
                    " and file_id="+file_id+
                    " and (select master_id from kassa where id="+kassa_id+")="+myMasterId ;
            try
            {
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }}
