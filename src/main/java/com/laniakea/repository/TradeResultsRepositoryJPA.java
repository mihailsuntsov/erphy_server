package com.laniakea.repository;

import com.laniakea.message.request.TradeResultsForm;
import com.laniakea.message.response.TradeResultsJSON;
import com.laniakea.message.response.TradeResultsSumByPeriodJSON;
import com.laniakea.message.response.TradeResultsTableReportJSON;
import com.laniakea.model.TradeResults;
import com.laniakea.model.User;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import com.laniakea.message.response.TradeResultsTableJSON;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Repository("TradeResultsRepositoryJPA")
public class TradeResultsRepositoryJPA {

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
    UserRepository userService;

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<TradeResultsTableJSON> getTradeResultsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(7L, "71,72,73,74"))// Итоги смен: всех предприятий, своего предприятия, своих отделений, только свои документы
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           ue.name as employee, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.employee_id as employee_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           cmp.name as company, " +
                    "           ds.name||' '||ds.address as department, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI')as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI')as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.additional as additional, " +
                    "           p.trade_date as trade_date, " +
                    "           to_char(p.trade_date,'DD.MM.YYYY') as trade_date_char, " +
                    "           p.incoming_cash_checkout as incoming_cash_checkout, " +
                    "           p.incoming_cashless_checkout as incoming_cashless_checkout, " +
                    "           p.incoming_cash2 as incoming_cash2, " +
                    "           p.incoming_cashless2 as incoming_cashless2, " +
                    "           p.refund_cash as refund_cash, " +
                    "           p.refund_cashless as refund_cashless, " +
                    "           p.encashment_cash as encashment_cash, " +
                    "           p.encashment_cashless as encashment_cashless " +
                    "           from traderesults p " +
                    "           INNER JOIN departments ds ON p.department_id=ds.id " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN users ue ON p.employee_id=ue.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(7L, "71")) //Если нет прав на "Меню - таблица - Итоги смен по всем предприятиям"
            {
                if (!securityRepositoryJPA.userHasPermissions_OR(7L, "72")) //Если нет прав на "Меню - таблица - Итоги смен своего предприятия"
                {
                    if (!securityRepositoryJPA.userHasPermissions_OR(7L, "73")) //Если нет прав на "Меню - таблица - Итоги смен своих отделений"
                    { //остается только на свои документы
                         stringQuery = stringQuery + " and p.creator_id ="+userRepositoryJPA.getMyId();
                    }else{stringQuery = stringQuery + " and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(ds.name) like upper('%" + searchString + "%')";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, TradeResultsTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList();
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    @Transactional
    public int getTradeResultsSize(String searchString, int companyId, int departmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(7L, "71,72,73,74"))// Итоги смен: всех предприятий, своего предприятия, своих отделений, только свои документы
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id " +
                    "           from traderesults p " +
                    "           INNER JOIN departments ds ON p.department_id=ds.id " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN users ue ON p.employee_id=ue.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(7L, "71")) //Если нет прав на "Меню - таблица - Итоги смен по всем предприятиям"
            {
                if (!securityRepositoryJPA.userHasPermissions_OR(7L, "72")) //Если нет прав на "Меню - таблица - Итоги смен своего предприятия"
                {
                    if (!securityRepositoryJPA.userHasPermissions_OR(7L, "73")) //Если нет прав на "Меню - таблица - Итоги смен своих отделений"
                    { //остается только на свои документы
                        stringQuery = stringQuery + " and p.creator_id ="+userRepositoryJPA.getMyId();
                    }else{stringQuery = stringQuery + " and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(ds.name) like upper('%" + searchString + "%')";
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
        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public TradeResultsJSON getTradeResultsValuesById (int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(7L, "77,78,79,80,81,82,83,84"))//Итоги смен: см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            List<Integer>param=userRepositoryJPA.getMyDepartmentsId();
            String dIds="";
            for (int i:param){dIds=dIds+i+",";}dIds=dIds+"0";//0 чтобы хоть чтото было, иначе будет and p.department_id in() и ошибка

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           ue.name as employee, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.employee_id as employee_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           cmp.name as company, " +
                    "           ds.name||' '||ds.address as department, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
                    "           p.additional as additional, " +
                    "           p.trade_date as trade_date, " +
                    "           trunc((CAST(p.incoming_cash_checkout AS DEC(12,2))/100),2) as incoming_cash_checkout, " +
                    "           trunc((CAST(p.incoming_cashless_checkout AS DEC(12,2))/100),2) as incoming_cashless_checkout, " +
                    "           trunc((CAST(p.incoming_cash2 AS DEC(12,2))/100),2) as incoming_cash2, " +
                    "           trunc((CAST(p.incoming_cashless2 AS DEC(12,2))/100),2) as incoming_cashless2, " +
                    "           trunc((CAST(p.refund_cash AS DEC(12,2))/100),2) as refund_cash, " +
                    "           trunc((CAST(p.refund_cashless AS DEC(12,2))/100),2) as refund_cashless, " +
                    "           trunc((CAST(p.encashment_cash AS DEC(12,2))/100),2) as encashment_cash, " +
                    "           trunc((CAST(p.encashment_cashless AS DEC(12,2))/100),2) as encashment_cashless " +
                    "           from traderesults p " +
                    "           INNER JOIN departments ds ON p.department_id=ds.id " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN users ue ON p.employee_id=ue.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(7L, "77,81")) //Если нет прав на просм или редакт. по всем предприятиям
            {
                if (!securityRepositoryJPA.userHasPermissions_OR(7L, "78,82")) //Если нет прав на просм или редакт. по своему предприятияю
                {
                    if (!securityRepositoryJPA.userHasPermissions_OR(7L, "79,83")) //Если нет прав на просм или редакт. по своим отделениям
                    { //остается только на свои документы
                        stringQuery = stringQuery + " and p.creator_id ="+userRepositoryJPA.getMyId();
                    }else{stringQuery = stringQuery + " and p.department_id in ("+dIds+")";/*needToSetParameter_MyDepthsIds=true;*/}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery, TradeResultsJSON.class);

            //if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            //{query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                TradeResultsJSON response = (TradeResultsJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertTradeResults(TradeResultsForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(7L,"75"))//  Итоги смен : "Создание"
        {

            TradeResults newDocument = new TradeResults();

            //создатель
            User creator = userRepository.getUserByUsername(userRepository.getUserName());
            newDocument.setCreator(creator);//создателя
            //владелец
            User master = userRepository.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository.getUserName() )));
            newDocument.setMaster(master);
            //дата и время создания
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            newDocument.setDate_time_created(timestamp);//
            //предприятие
            newDocument.setCompany(companyRepositoryJPA.getCompanyById(Long.valueOf(Integer.parseInt(request.getCompany_id()))));
            //отделение
            newDocument.setDepartment(departmentRepositoryJPA.getDepartmentById(Long.valueOf(Integer.parseInt(request.getDepartment_id()))));
            //сотрудник
            newDocument.setEmployee(userRepositoryJPA.getUserById(Long.valueOf(Integer.parseInt(request.getEmployee_id()))));
            //дата торговой смены
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String tradeDate = (request.getTrade_date() == null ? "" : request.getTrade_date());
            try {
                newDocument.setTrade_date(tradeDate.isEmpty() ? null : dateFormat.parse(tradeDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //дополнительная информация
            newDocument.setAdditional(request.getAdditional());
            //деньги (приходят рубли или рубли.копейки, надо заменить на копейки - в БД хранятся копейки. Например пришло 33,99, в БД уйдет 3399

            if (request.getIncoming_cash_checkout() != null && !request.getIncoming_cash_checkout().isEmpty() && request.getIncoming_cash_checkout().trim().length() > 0) {
                newDocument.setIncoming_cash_checkout(new BigDecimal(request.getIncoming_cash_checkout().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setIncoming_cash_checkout(0);}
            if (request.getIncoming_cashless_checkout() != null && !request.getIncoming_cashless_checkout().isEmpty() && request.getIncoming_cashless_checkout().trim().length() > 0) {
                newDocument.setIncoming_cashless_checkout(new BigDecimal(request.getIncoming_cashless_checkout().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setIncoming_cashless_checkout(0);}
            if (request.getEncashment_cash() != null && !request.getEncashment_cash().isEmpty() && request.getEncashment_cash().trim().length() > 0) {
                newDocument.setEncashment_cash(new BigDecimal(request.getEncashment_cash().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setEncashment_cash(0);}
            if (request.getEncashment_cashless() != null && !request.getEncashment_cashless().isEmpty() && request.getEncashment_cashless().trim().length() > 0) {
                newDocument.setEncashment_cashless(new BigDecimal(request.getEncashment_cashless().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setEncashment_cashless(0);}
            if (request.getIncoming_cash2() != null && !request.getIncoming_cash2().isEmpty() && request.getIncoming_cash2().trim().length() > 0) {
                newDocument.setIncoming_cash2(new BigDecimal(request.getIncoming_cash2().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setIncoming_cash2(0);}
            if (request.getIncoming_cashless2() != null && !request.getIncoming_cashless2().isEmpty() && request.getIncoming_cashless2().trim().length() > 0) {
                newDocument.setIncoming_cashless2(new BigDecimal(request.getIncoming_cashless2().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setIncoming_cashless2(0);}
            if (request.getRefund_cashless() != null && !request.getRefund_cashless().isEmpty() && request.getRefund_cashless().trim().length() > 0) {
                newDocument.setRefund_cashless(new BigDecimal(request.getRefund_cashless().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setRefund_cashless(0);}
            if (request.getRefund_cash() != null && !request.getRefund_cash().isEmpty() && request.getRefund_cash().trim().length() > 0) {
                newDocument.setRefund_cash(new BigDecimal(request.getRefund_cash().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { newDocument.setRefund_cash(0);}

            entityManager.persist(newDocument);
            entityManager.flush();

            return newDocument.getId();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public boolean updateTradeResults(TradeResultsForm request) {
        boolean perm_AllCompaniesUpdate=securityRepositoryJPA.userHasPermissions_OR(7L, "81"); // Итоги смен:"Редактирование документов по всем предприятиям" (в пределах родительского аккаунта)
        boolean perm_MyCompanyUpdate=securityRepositoryJPA.userHasPermissions_OR(7L, "82"); // Итоги смен:"Редактирование документов своего предприятия"
        boolean perm_MyDepartmentsUpdate=securityRepositoryJPA.userHasPermissions_OR(7L, "83"); // Итоги смен:"Редактирование документов своих отделений"
        boolean perm_MyDocumentsUpdate=securityRepositoryJPA.userHasPermissions_OR(7L, "84"); // Итоги смен:"Редактирование документов своих отделений"

        boolean itIsDocumentOfMyMasters=securityRepositoryJPA.isItMyMastersTradeResults(Long.valueOf(request.getId()));//документ под юрисдикцией главного аккаунта
        boolean itIsDocumentOfMyCompany=userRepositoryJPA.getMyCompanyId()==Integer.parseInt(request.getCompany_id());//сохраняется документ моего предприятия
        boolean itIsDocumentOfMyDepartments=securityRepositoryJPA.isItMyDepartment(Long.valueOf(request.getDepartment_id()));//сохраняется документ моих отделений
        boolean itIsMyDocument=securityRepositoryJPA.isItMyTradeResults(Long.valueOf(request.getId()));//сохраняется мой документ

        if
        (
            (perm_AllCompaniesUpdate ||                                     //если есть права изменять доки всех предприятий
            (itIsDocumentOfMyCompany && perm_MyCompanyUpdate)||             //или это мое предприятие и есть права изменять доки своего предприятия
            (itIsDocumentOfMyDepartments && perm_MyDepartmentsUpdate)||     //или это мое отделение иесть права изменять доки своих отделений
            (itIsMyDocument && perm_MyDocumentsUpdate))                     //или это мой документ и есть права изменять свои доки (т.е. созданные собой)
            && itIsDocumentOfMyMasters                                      //+документ под юрисдикцией главного (родительского) аккаунта
        ){
            EntityManager emgr = emf.createEntityManager();
            emgr.getTransaction().begin();
            Long id=Long.valueOf(request.getId());
            TradeResults updateDocument = emgr.find(TradeResults.class, id);
            //id
            updateDocument.setId(id);
            //кто изменил
            User changer = userRepository.getUserByUsername(userRepository.getUserName());
            updateDocument.setChanger(changer);
            //дату изменения
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            updateDocument.setDate_time_changed(timestamp);
            //дата торговой смены
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String tradeDate = (request.getTrade_date() == null ? "" : request.getTrade_date());
            try {
                updateDocument.setTrade_date(tradeDate.isEmpty() ? null : dateFormat.parse(tradeDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //дополнительная информация
            updateDocument.setAdditional (request.getAdditional() == null ? "": request.getAdditional());

            if (request.getIncoming_cash_checkout() != null && !request.getIncoming_cash_checkout().isEmpty() && request.getIncoming_cash_checkout().trim().length() > 0) {
                updateDocument.setIncoming_cash_checkout(new BigDecimal(request.getIncoming_cash_checkout().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setIncoming_cash_checkout(0);}
            if (request.getIncoming_cashless_checkout() != null && !request.getIncoming_cashless_checkout().isEmpty() && request.getIncoming_cashless_checkout().trim().length() > 0) {
                updateDocument.setIncoming_cashless_checkout(new BigDecimal(request.getIncoming_cashless_checkout().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setIncoming_cashless_checkout(0);}
            if (request.getEncashment_cash() != null && !request.getEncashment_cash().isEmpty() && request.getEncashment_cash().trim().length() > 0) {
                updateDocument.setEncashment_cash(new BigDecimal(request.getEncashment_cash().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setEncashment_cash(0);}
            if (request.getEncashment_cashless() != null && !request.getEncashment_cashless().isEmpty() && request.getEncashment_cashless().trim().length() > 0) {
                updateDocument.setEncashment_cashless(new BigDecimal(request.getEncashment_cashless().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setEncashment_cashless(0);}
            if (request.getIncoming_cash2() != null && !request.getIncoming_cash2().isEmpty() && request.getIncoming_cash2().trim().length() > 0) {
                updateDocument.setIncoming_cash2(new BigDecimal(request.getIncoming_cash2().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setIncoming_cash2(0);}
            if (request.getIncoming_cashless2() != null && !request.getIncoming_cashless2().isEmpty() && request.getIncoming_cashless2().trim().length() > 0) {
                updateDocument.setIncoming_cashless2(new BigDecimal(request.getIncoming_cashless2().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setIncoming_cashless2(0);}
            if (request.getRefund_cashless() != null && !request.getRefund_cashless().isEmpty() && request.getRefund_cashless().trim().length() > 0) {
                updateDocument.setRefund_cashless(new BigDecimal(request.getRefund_cashless().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setRefund_cashless(0);}
            if (request.getRefund_cash() != null && !request.getRefund_cash().isEmpty() && request.getRefund_cash().trim().length() > 0) {
                updateDocument.setRefund_cash(new BigDecimal(request.getRefund_cash().replace(",",".")).multiply(new BigDecimal(100)).intValueExact());
            } else { updateDocument.setRefund_cash(0);}

            emgr.getTransaction().commit();
            emgr.close();
            return true;
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteTradeResultsById(String delNumbers) {
        if(securityRepositoryJPA.userHasPermissions_OR(7L,"76")&& //Итоги смен: "Удаление"
                securityRepositoryJPA.isItAllMyMastersTradeResults(delNumbers))  //все ли Итоги смен: принадлежат текущему родительскому аккаунту
        {
            String stringQuery;
            stringQuery="Update traderesults p" +
                    " set is_archive=true "+
                    " where p.id in ("+ delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if(!stringQuery.isEmpty() && stringQuery.trim().length() > 0){
                int count = query.executeUpdate();
                return true;
            }else return false;
        }else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<TradeResultsTableReportJSON> getTradeResultsTableReport(int companyId, int departmentId, int employeeId, String dateFrom, String dateTo) {
        if(securityRepositoryJPA.userHasPermissions_OR(8L, "86,87,88,89"))// Отчет Итоги смен: всех предприятий, своего предприятия, своих отделений, только свои документы
        {
            String stringQuery;
           // boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            List<Integer>param=userRepositoryJPA.getMyDepartmentsId();
            String dIds="";
            for (int i:param){dIds=dIds+i+",";}dIds=dIds+"0";//0 чтобы хоть чтото было, иначе будет and p.department_id in() и ошибка

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           ue.name as employee, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.employee_id as employee_id, " +
                    "           p.company_id as company_id, " +
                    "           p.department_id as department_id, " +
                    "           cmp.name as company, " +
                    "           ds.name||' '||ds.address as department, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI')as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI')as date_time_changed, " +
                    "           p.additional as additional, " +
                    "           p.trade_date as trade_date, " +
                    "           to_char(p.trade_date,'DD.MM.YYYY') as trade_date_char, " +
                    "           trunc((CAST(p.incoming_cash_checkout AS DEC(12,2))/100),2) as incoming_cash_checkout, " +
                    "           trunc((CAST(p.incoming_cashless_checkout AS DEC(12,2))/100),2) as incoming_cashless_checkout, " +
                    "           trunc((CAST(p.incoming_cash2 AS DEC(12,2))/100),2) as incoming_cash2, " +
                    "           trunc((CAST(p.incoming_cashless2 AS DEC(12,2))/100),2) as incoming_cashless2, " +
                    "           trunc((CAST(p.refund_cash AS DEC(12,2))/100),2) as refund_cash, " +
                    "           trunc((CAST(p.refund_cashless AS DEC(12,2))/100),2) as refund_cashless, " +
                    "           trunc((CAST(p.encashment_cash AS DEC(12,2))/100),2) as encashment_cash, " +
                    "           trunc((CAST(p.encashment_cashless AS DEC(12,2))/100),2) as encashment_cashless, " +

                    "           trunc((CAST((p.incoming_cash_checkout+p.incoming_cash2) AS DEC(12,2))/100),2) as cash_all, " +
                    "           trunc((CAST((p.incoming_cash_checkout-p.encashment_cash) AS DEC(12,2))/100),2) as cash_minus_encashment, " +
                    "           trunc((CAST((p.incoming_cash_checkout+p.incoming_cashless_checkout+p.incoming_cash2+p.incoming_cashless2) AS DEC(12,2))/100),2) as total_incoming " +

                    "           from traderesults p " +
                    "           INNER JOIN departments ds ON p.department_id=ds.id " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN users ue ON p.employee_id=ue.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(8L, "86")) //Если нет прав на "Информация по всем предприятиям"
            {
                if (!securityRepositoryJPA.userHasPermissions_OR(8L, "87")) //Если нет прав на "Информация по своему предприятию"
                {
                    if (!securityRepositoryJPA.userHasPermissions_OR(8L, "88")) //Если нет прав на "Информация по своим отделениям"
                    { //остается только на "Информация по своим документам", НО! в своих отделениях
                        stringQuery = stringQuery + " and p.creator_id ="+userRepositoryJPA.getMyId()+" and p.department_id in ("+dIds+")";
                    }else{stringQuery = stringQuery + " and p.department_id in ("+dIds+")";}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
//            stringQuery = stringQuery + " and p.trade_date BETWEEN (("+dateFrom+") and ("+dateTo+"))";
            stringQuery = stringQuery + " and p.trade_date >=to_date('"+dateFrom+"','DD.MM.YYYY')"+
                                        " and p.trade_date <=to_date('"+dateTo+"','DD.MM.YYYY')";
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            if (employeeId > 0) {
                stringQuery = stringQuery + " and p.employee_id=" + employeeId;
            }
            stringQuery = stringQuery + " order by p.trade_date asc";
            Query query = entityManager.createNativeQuery(stringQuery, TradeResultsTableReportJSON.class);

//            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
//            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            return query.getResultList();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public TradeResultsSumByPeriodJSON getTradeResultsSumByPeriod(int companyId, int departmentId, int employeeId, String dateFrom, String dateTo) {
        if(securityRepositoryJPA.userHasPermissions_OR(8L, "86,87,88,89"))// Отчет Итоги смен: всех предприятий, своего предприятия, своих отделений, только свои документы
        {
            String stringQuery;
            // boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            List<Integer>param=userRepositoryJPA.getMyDepartmentsId();
            String dIds="";
            for (int i:param){dIds=dIds+i+",";}dIds=dIds+"0";//0 чтобы хоть чтото было, иначе будет and p.department_id in() и ошибка

            stringQuery = "select " +

                    "1 as id,"+
                    "           sum(trunc((CAST((p.incoming_cash_checkout+p.incoming_cash2) AS DEC(12,2))/100),2)) as cash_all, " +
                    "           sum(trunc((CAST((p.incoming_cash_checkout+p.incoming_cash2-p.encashment_cash) AS DEC(12,2))/100),2)) as cash_minus_encashment, " +
                    "           sum(trunc((CAST((p.incoming_cash_checkout+p.incoming_cashless_checkout+p.incoming_cash2+p.incoming_cashless2) AS DEC(12,2))/100),2)) as total_incoming, " +
                    "           sum(trunc((CAST((p.incoming_cash_checkout+p.incoming_cashless_checkout) AS DEC(12,2))/100),2)) as checkout_all " +

                    "           from traderesults p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(8L, "86")) //Если нет прав на "Информация по всем предприятиям"
            {
                if (!securityRepositoryJPA.userHasPermissions_OR(8L, "87")) //Если нет прав на "Информация по своему предприятию"
                {
                    if (!securityRepositoryJPA.userHasPermissions_OR(8L, "88")) //Если нет прав на "Информация по своим отделениям"
                    { //остается только на "Информация по своим документам", НО! в своих отделениях
                        stringQuery = stringQuery + " and p.creator_id ="+userRepositoryJPA.getMyId()+" and p.department_id in ("+dIds+")";
                    }else{stringQuery = stringQuery + " and p.department_id in ("+dIds+")";}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
//            stringQuery = stringQuery + " and p.trade_date BETWEEN (("+dateFrom+") and ("+dateTo+"))";
            stringQuery = stringQuery + " and p.trade_date >=to_date('"+dateFrom+"','DD.MM.YYYY')"+
                    " and p.trade_date <=to_date('"+dateTo+"','DD.MM.YYYY')";
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            if (departmentId > 0) {
                stringQuery = stringQuery + " and p.department_id=" + departmentId;
            }
            if (employeeId > 0) {
                stringQuery = stringQuery + " and p.employee_id=" + employeeId;
            }

            Query query = entityManager.createNativeQuery(stringQuery, TradeResultsSumByPeriodJSON.class);

//            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
//            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}


            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                TradeResultsSumByPeriodJSON response = (TradeResultsSumByPeriodJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }



}
