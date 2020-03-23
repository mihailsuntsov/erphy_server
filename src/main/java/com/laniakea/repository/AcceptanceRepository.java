package com.laniakea.repository;

import com.laniakea.message.request.AcceptanceForm;
import com.laniakea.message.request.AcceptanceProductForm;
import com.laniakea.message.request.SearchForm;
import com.laniakea.message.request.UniversalForm;
import com.laniakea.message.response.AcceptanceJSON;
import com.laniakea.message.response.FilesAcceptanceJSON;
import com.laniakea.message.response.ProductHistoryJSON;
import com.laniakea.model.*;
import com.laniakea.model.Sprav.SpravSysEdizm;
import com.laniakea.model.Sprav.SpravSysNds;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Repository
public class AcceptanceRepository {
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
    private UserDetailsServiceImpl userService;


//*****************************************************************************************************************************************************
//****************************************************      MENU      *********************************************************************************
//*****************************************************************************************************************************************************
@SuppressWarnings("Duplicates")
public List<AcceptanceJSON> getAcceptanceTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId) {
    if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
    {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
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
                "           coalesce(p.nds,false) as nds, " +
                "           coalesce(p.nds_included,false) as nds_included, " +
                "           coalesce(p.overhead,0) as overhead, " +
                "           p.department_id as department_id, " +
                "           dp.name || ' ' || dp.address as department, " +
                "           p.cagent_id as cagent_id, " +
                "           cg.name as cagent, " +
                "           p.doc_number as doc_number, " +
                "           to_char(p.acceptance_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as acceptance_date, " +
                "           (select abbreviation from sprav_sys_opf where id=cmp.opf)||' '||cmp.name as company, " +
                "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                "           p.description as description, " +
                "           coalesce(p.is_completed,false) as is_completed, " +
                "           p.acceptance_date as acceptance_date_sort, " +
                "           p.date_time_created as date_time_created_sort, " +
                "           p.date_time_changed as date_time_changed_sort, " +
                "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method " +
                "           from acceptance p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_archive,false) !=true ";

        if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.acceptance_date, 'DD.MM.YYYY') ='"+searchString+"' or "+
                    " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                    " upper(dp.name) like upper('%" + searchString + "%') or "+
                    " upper(cmp.name) like upper('%" + searchString + "%') or "+
                    " upper(us.name) like upper('%" + searchString + "%') or "+
                    " upper(uc.name) like upper('%" + searchString + "%') or "+
                    " upper(cg.name) like upper('%" + searchString + "%') or "+
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
        {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

        List<Object[]> queryList = query.getResultList();
        List<AcceptanceJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList){
            AcceptanceJSON doc=new AcceptanceJSON();
            doc.setId(Long.parseLong(                     obj[0].toString()));
            doc.setMaster((String)                        obj[1]);
            doc.setCreator((String)                       obj[2]);
            doc.setChanger((String)                       obj[3]);
            doc.setMaster_id(Long.parseLong(              obj[4].toString()));
            doc.setCreator_id(Long.parseLong(             obj[5].toString()));
            doc.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
            doc.setCompany_id(Long.parseLong(             obj[7].toString()));
            doc.setNds((Boolean)                          obj[8]);
            doc.setNds_included((Boolean)                 obj[9]);
            doc.setOverhead((BigDecimal)                  obj[10]);
            doc.setDepartment_id(Long.parseLong(          obj[11].toString()));
            doc.setDepartment((String)                    obj[12]);
            doc.setCagent_id(Long.parseLong(              obj[13].toString()));
            doc.setCagent((String)                        obj[14]);
            doc.setDoc_number(Long.parseLong(             obj[15].toString()));
            doc.setAcceptance_date((String)(              obj[16]));
            doc.setCompany((String)                       obj[17]);
            doc.setDate_time_created((String)             obj[18]);
            doc.setDate_time_changed((String)             obj[19]);
            doc.setDescription((String)                   obj[20]);
            doc.setIs_completed((Boolean)                 obj[21]);
            doc.setOverhead_netcost_method((Integer)      obj[25]);
            returnList.add(doc);
        }
        return returnList;
    } else return null;
}
    @SuppressWarnings("Duplicates")
    public int getAcceptanceSize(String searchString, int companyId, int departmentId) {
//        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
//        {
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id " +
                    "           from acceptance p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true ";

            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.acceptance_date, 'DD.MM.YYYY') ='"+searchString+"' or "+
                        " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                        " upper(dp.name) like upper('%" + searchString + "%') or "+
                        " upper(cmp.name) like upper('%" + searchString + "%') or "+
                        " upper(us.name) like upper('%" + searchString + "%') or "+
                        " upper(uc.name) like upper('%" + searchString + "%') or "+
                        " upper(cg.name) like upper('%" + searchString + "%') or "+
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
//        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    public List<AcceptanceProductForm> getAcceptanceProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                            " ap.product_id," +
                            " ap.acceptance_id," +
                            " ap.product_count," +
                            " ap.product_price," +
                            " ap.product_sumprice," +
                            " ap.product_netcost," +
                            " ap.nds_id," +
                            " ap.edizm_id," +
                            " p.name as name," +
                            " (select nds.name from sprav_sys_nds nds where nds.id = ap.nds_id) as nds," +
                            " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = ap.edizm_id) as edizm" +
                            " from " +
                            " acceptance_product ap " +
                            " INNER JOIN acceptance a ON ap.acceptance_id=a.id " +
                            " INNER JOIN products p ON ap.product_id=p.id " +
                            " where a.master_id = " + myMasterId +
                            " and ap.acceptance_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<AcceptanceProductForm> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                AcceptanceProductForm doc=new AcceptanceProductForm();
                doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                doc.setAcceptance_id(Long.parseLong(                    obj[1].toString()));
                doc.setProduct_count((BigDecimal)                       obj[2]);
                doc.setProduct_price((BigDecimal)                       obj[3]);
                doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                doc.setProduct_netcost((BigDecimal)                     obj[5]);
                doc.setNds_id((Integer)                                 obj[6]);
                doc.setEdizm_id(obj[7]!=null?Long.parseLong(            obj[7].toString()):null);
                doc.setName((String)                                    obj[8]);
                doc.setNds((String)                                     obj[9]);
                doc.setEdizm((String)                                   obj[10]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

//*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public AcceptanceJSON getAcceptanceValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(15L, "188,189,195,196"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           coalesce(p.nds,false) as nds, " +
                    "           coalesce(p.nds_included,false) as nds_included, " +
                    "           coalesce(p.overhead,0) as overhead, " +
                    "           p.department_id as department_id, " +
                    "           dp.name ||' '||dp.address  as department, " +
                    "           p.cagent_id as cagent_id, " +
                    "           cg.name as cagent, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.acceptance_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as acceptance_date, " +
                    "           (select abbreviation from sprav_sys_opf where id=cmp.opf)||' '||cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.acceptance_date as acceptance_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.overhead_netcost_method,0) as overhead_netcost_method " +
                    "           from acceptance p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           INNER JOIN cagents cg ON p.cagent_id=cg.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            AcceptanceJSON returnObj=new AcceptanceJSON();

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                     obj[0].toString()));
                returnObj.setMaster((String)                        obj[1]);
                returnObj.setCreator((String)                       obj[2]);
                returnObj.setChanger((String)                       obj[3]);
                returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                returnObj.setNds((Boolean)                          obj[8]);
                returnObj.setNds_included((Boolean)                 obj[9]);
                returnObj.setOverhead((BigDecimal)                  obj[10]);
                returnObj.setDepartment_id(Long.parseLong(          obj[11].toString()));
                returnObj.setDepartment((String)                    obj[12]);
                returnObj.setCagent_id(Long.parseLong(              obj[13].toString()));
                returnObj.setCagent((String)                        obj[14]);
                returnObj.setDoc_number(Long.parseLong(             obj[15].toString()));
                returnObj.setAcceptance_date((String)(              obj[16]));
                returnObj.setCompany((String)                       obj[17]);
                returnObj.setDate_time_created((String)             obj[18]);
                returnObj.setDate_time_changed((String)             obj[19]);
                returnObj.setDescription((String)                   obj[20]);
                returnObj.setIs_completed((Boolean)                 obj[21]);
                returnObj.setOverhead_netcost_method((Integer)      obj[25]);
            }
            return returnObj;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertAcceptance(AcceptanceForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L,"184,185,192"))//  "Создание"
        {
            Acceptance newDocument = new Acceptance();
            EntityManager emgr = emf.createEntityManager();
            //владелец
            User master = userRepository.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository.getUserName() )));

            if(companyRepositoryJPA.getCompanyById((request.getCompany_id())).getMaster().getId()==master.getId())
            {//проверка на то, что предприятие, для которого содается документ, наодится под главным аккаунтом
                newDocument.setMaster(master);
                //предприятие
                newDocument.setCompany(companyRepositoryJPA.getCompanyById((request.getCompany_id())));
                //дата и время создания
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                newDocument.setDate_time_created(timestamp);//

                //создатель
                User creator = userRepository.getUserByUsername(userRepository.getUserName());
                newDocument.setCreator(creator);
                //отделение
                newDocument.setDepartment(emgr.find(Departments.class, request.getDepartment_id()));
                //поставщик
                newDocument.setCagent(emgr.find(Cagents.class, request.getCagent_id()));
                //дата торговой смены
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                String acceptanceDate = (request.getAcceptance_date() == null ? "" : request.getAcceptance_date());
                try {
                    newDocument.setAcceptance_date(acceptanceDate.isEmpty() ? null : dateFormat.parse(acceptanceDate));
                } catch (ParseException e) {e.printStackTrace();}
                //номер документа
                if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                    newDocument.setDoc_number(Long.valueOf(request.getDoc_number()));
                } else newDocument.setDoc_number(generateDocNumberCode(request.getCompany_id()));
                //НДС
                newDocument.setNds(request.isNds());
                //НДС включен
                newDocument.setNds_included(request.isNds_included());
                //расходы
                if (request.getOverhead() != null && !request.getOverhead().isEmpty() && request.getOverhead().trim().length() > 0) {
                    newDocument.setOverhead(new BigDecimal(request.getOverhead().replace(",", ".")));
                }

                //дополнительная информация
                newDocument.setDescription(request.getDescription());
                //Распределение затрат на себестоимость товаров. 0 - нет, 1 - по весу цены в поставке
                newDocument.setOverhead_netcost_method(request.getOverhead_netcost_method());
                entityManager.persist(newDocument);
                entityManager.flush();
                return newDocument.getId();
            } else return null;
        } else return null;
    }

    @Transactional
    public  Boolean updateAcceptance(AcceptanceForm request) {
            //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",request.getId().toString())) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",request.getId().toString()))||
            //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",request.getId().toString()))||
            //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",request.getId().toString())))
        {
            if(updateAcceptanceWithoutTable(request)){                                      //апдейт основного документа, без таблицы товаров
                try {//сохранение таблицы
                    String productIds = "";
                    Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                    if (request.getAcceptanceProductTable().size() > 0) {
                        for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                            if (!saveAcceptanceProductTable(row, request.getId(),myMasterId)) {//         //сохранение таблицы товаров
                                break;
                            }
                            productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
                        }
                    }//удаление лишних строк
                    deleteAcceptanceProductTableExcessRows(productIds.length()>0?productIds:"0", request.getId());

                    //если завершается приемка - запись в историю товара
                    if(request.is_completed()){

                        for (AcceptanceProductForm row : request.getAcceptanceProductTable()) {
                            if (!addAcceptanceProductHistory(row, request, myMasterId)) {//       //сохранение истории операций с записью актуальной инфо о количестве товара в отделении
                                break;
                            } else {
                                if (!setProductQuantity(row, request, myMasterId)) {// запись о количестве товара в отделении в отдельной таблице
                                    break;
                                }
                            }
                            productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
                        }
                    }
                    return true;
                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private Boolean updateAcceptanceWithoutTable(AcceptanceForm request) {
        EntityManager emgr = emf.createEntityManager();
        Acceptance document = emgr.find(Acceptance.class, request.getId());//сохраняемый документ
        try
        {
            emgr.getTransaction().begin();

            document.setDescription         (request.getDescription() == null ? "": request.getDescription());
            document.setNds                 (request.isNds());
            document.setNds_included        (request.isNds_included());
            document.setDoc_number          (Long.valueOf(request.getDoc_number()));
            document.setIs_completed        (request.is_completed());
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
            String tradeDate = (request.getAcceptance_date() == null ? "" : request.getAcceptance_date());
            try {
                document.setAcceptance_date(tradeDate.isEmpty() ? null : dateFormat.parse(tradeDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (request.getOverhead() != null && !request.getOverhead().isEmpty() && request.getOverhead().trim().length() > 0) {
                document.setOverhead(new BigDecimal(request.getOverhead().replace(",",".")));
            } else { document.setOverhead(new BigDecimal("0"));}

            User changer = userService.getUserByUsername(userService.getUserName());
            document.setChanger(changer);//кто изменил

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            document.setDate_time_changed(timestamp);//дату изменения

            //Распределение затрат на себестоимость товаров. 0 - нет, 1 - по весу цены в поставке
            document.setOverhead_netcost_method(request.getOverhead_netcost_method());
            emgr.getTransaction().commit();
            emgr.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private Boolean saveAcceptanceProductTable(AcceptanceProductForm row, Long acceptance_id, Long myMasterId) {
        if(clearAcceptanceProductTable(row.getProduct_id(), acceptance_id)){
            String stringQuery;
            try {
                stringQuery =   " insert into acceptance_product (" +
                        "product_id," +
                        "acceptance_id," +
                        "product_count," +
                        "product_price," +
                        "product_sumprice," +
                        "product_netcost," +
                        "nds_id," +
                        "edizm_id" +
                        ") values ("
                        + "(select id from products where id="+row.getProduct_id() +" and master_id="+myMasterId+"),"//Проверки, что никто не шалит, и идёт запись того, чего надо туда, куда надо
                        + "(select id from acceptance where id="+row.getAcceptance_id() +" and master_id="+myMasterId+"),"
                        + row.getProduct_count() + ","
                        + row.getProduct_price() +","
                        + row.getProduct_sumprice() +","
                        + row.getProduct_netcost() +","
                        + row.getNds_id() +","
                        + row.getEdizm_id() +")";
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }
    private Boolean deleteAcceptanceProductTableExcessRows(String productIds, Long acceptance_id) {
        String stringQuery;
        try {
            stringQuery =   " delete from acceptance_product " +
                    " where acceptance_id=" + acceptance_id +
                    " and product_id not in (" + productIds + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    private Boolean addAcceptanceProductHistory(AcceptanceProductForm row, AcceptanceForm request , Long masterId) {
        String stringQuery;
        ProductHistoryJSON lastProductHistoryRecord =  getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
        BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
        BigDecimal lastAvgPurchasePrice= lastProductHistoryRecord.getAvg_purchase_price();
        BigDecimal lastAvgNetcostPrice= lastProductHistoryRecord.getAvg_netcost_price();
        BigDecimal avgPurchasePrice = ((lastQuantity.multiply(lastAvgPurchasePrice)).add(row.getProduct_sumprice())).divide(lastQuantity.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);
        BigDecimal avgNetcostPrice =((lastQuantity.multiply(lastAvgNetcostPrice)).add(row.getProduct_count().multiply(row.getProduct_netcost()))).divide(lastQuantity.add(row.getProduct_count()),2,BigDecimal.ROUND_HALF_UP);
        //для последней закуп. цены нельзя брать row.getProduct_price(), т.к. она не учитывает НДС, если он не включен в цену. А row.getProduct_sumprice() учитывает.
        BigDecimal last_purchase_price=row.getProduct_sumprice().divide(row.getProduct_count(),2,BigDecimal.ROUND_HALF_UP);


        try {
            stringQuery =   " insert into products_history (" +
                            " master_id," +
                            " company_id," +
                            " department_id," +
                            " doc_type_id," +
                            " doc_id," +
                            " product_id," +
                            " quantity," +
                            " change," +
                            " avg_purchase_price," +
                            " avg_netcost_price," +
                            " last_purchase_price," +
                            " last_operation_price," +
                            " date_time_created"+
                            ") values ("+
                            masterId +","+
                            request.getCompany_id() +","+
                            request.getDepartment_id() + ","+
                            15 +","+
                            row.getAcceptance_id() + ","+
                            row.getProduct_id() + ","+
                            lastQuantity.add(row.getProduct_count())+","+
                            row.getProduct_count() +","+
                            avgPurchasePrice +","+
                            avgNetcostPrice +","+
                            last_purchase_price+","+//в операциях поступления (оприходование и приёмка) цена закупки (или оприходования) last_purchase_price равна цене операции last_operation_price,
                            last_purchase_price+","+//..в отличии от операций убытия (списания, продажа), где цена закупки остается старой
                            " now())";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @SuppressWarnings("Duplicates")
    private Boolean setProductQuantity(AcceptanceProductForm row, AcceptanceForm request , Long masterId) {
        String stringQuery;
        ProductHistoryJSON lastProductHistoryRecord =  getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
        BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();

        try {
            stringQuery =
                    " insert into product_quantity (" +
                    " master_id," +
                    " department_id," +
                    " product_id," +
                    " quantity" +
                    ") values ("+
                    masterId + ","+
                    request.getDepartment_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantity +
                    ") ON CONFLICT ON CONSTRAINT product_quantity_uq " +// "upsert"
                    " DO update set " +
                    " department_id = " + request.getDepartment_id() + ","+
                    " product_id = " + row.getProduct_id() + ","+
                    " master_id = "+ masterId + "," +
                    " quantity = "+ lastQuantity;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteAcceptance (String delNumbers) {
            //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"186") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(15L,"187") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",delNumbers))||
            //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(15L,"193") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",delNumbers))||
            //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(15L,"194") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            stringQuery = "Update acceptance p" +
                    " set is_archive=true " +
                    " where p.id in ("+delNumbers+")"+
                    " and coalesce(p.is_completed,false) !=true";
            entityManager.createNativeQuery(stringQuery).executeUpdate();
            return true;
        } else return false;
    }


//*****************************************************************************************************************************************************
//***************************************************      UTILS      *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")  // возвращает значения из последней строки истории изменений товара
    private ProductHistoryJSON getLastProductHistoryRecord(Long product_id, Long department_id)
    {
        String stringQuery;
        stringQuery =
                " select                                        "+
                        " last_purchase_price   as last_purchase_price, "+
                        " avg_purchase_price    as avg_purchase_price,  "+
                        " avg_netcost_price     as avg_netcost_price,   "+
                        " last_operation_price  as last_operation_price,"+
                        " quantity              as quantity,            "+
                        " change                as change               "+
                        "          from products_history                "+
                        "          where                                "+
                        "          product_id="+product_id+" and        "+
                        "          department_id="+department_id         +
                        "          order by id desc limit 1             ";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            ProductHistoryJSON returnObj=new ProductHistoryJSON();
            if(queryList.size()==0){//если записей истории по данному товару ещё нет
                returnObj.setLast_purchase_price(       (new BigDecimal(0)));
                returnObj.setAvg_purchase_price(        (new BigDecimal(0)));
                returnObj.setAvg_netcost_price(         (new BigDecimal(0)));
                returnObj.setLast_operation_price(      (new BigDecimal(0)));
                returnObj.setQuantity(                  (new BigDecimal(0)));
            }else {
                for (Object[] obj : queryList) {
                    returnObj.setLast_purchase_price((BigDecimal)   obj[0]);
                    returnObj.setAvg_purchase_price((BigDecimal)    obj[1]);
                    returnObj.setAvg_netcost_price((BigDecimal)     obj[2]);
                    returnObj.setLast_operation_price((BigDecimal)  obj[3]);
                    returnObj.setQuantity((BigDecimal)              obj[4]);
                }
            }
            return returnObj;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @SuppressWarnings("Duplicates")  //генератор номера документа
    private Long generateDocNumberCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(doc_number)+1,1) from acceptance where company_id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.parseLong(query.getSingleResult().toString(),10);
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @SuppressWarnings("Duplicates") // проверка на уникальность номера документа
    public Boolean isAcceptanceNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long product_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from acceptance where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(product_id>0) stringQuery=stringQuery+" and id !="+product_id; // чтобы он не срабатывал сам на себя
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.getResultList().size()>0)
                return false;// код не уникальный
            else return true; // код уникальный
        }
        catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @SuppressWarnings("Duplicates") //проверка на то, есть ли уже в таблице товаров данный товар
    private Boolean clearAcceptanceProductTable(Long product_id, Long acceptance_id) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = " delete from " +
                " acceptance_product where " +
                "product_id="+product_id+
                " and acceptance_id="+acceptance_id +
                " and (select master_id from acceptance where id="+acceptance_id+")="+myMasterId;
        try
        {
            entityManager.createNativeQuery(stringQuery).executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToAcceptance(UniversalForm request){
        Long acceptanceId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance",acceptanceId.toString())) ||
        //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",acceptanceId.toString()))||
        //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
        (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",acceptanceId.toString()))||
        //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
        (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",acceptanceId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select acceptance_id from acceptance_files where acceptance_id=" + acceptanceId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_AcceptanceId_FileId(acceptanceId,fileId);
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
    boolean manyToMany_AcceptanceId_FileId(Long acceptanceId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into acceptance_files " +
                    "(acceptance_id,file_id) " +
                    "values " +
                    "(" + acceptanceId + ", " + fileId +")")
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
    public List<FilesAcceptanceJSON> getListOfAcceptanceFiles(Long acceptanceId) {
        if(securityRepositoryJPA.userHasPermissions_OR(15L, "188,189"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            Integer MY_COMPANY_ID = userRepositoryJPA.getMyCompanyId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           acceptance p" +
                    "           inner join" +
                    "           acceptance_files pf" +
                    "           on p.id=pf.acceptance_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + acceptanceId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(15L, "188")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(15L, "189")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(15L, "195")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + MY_COMPANY_ID;//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            List<FilesAcceptanceJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                FilesAcceptanceJSON doc=new FilesAcceptanceJSON();
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
    public boolean deleteAcceptanceFile(SearchForm request)
    {
            //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(15L,"190") && securityRepositoryJPA.isItAllMyMastersDocuments("acceptance", String.valueOf(request.getId()))) ||
            //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(15L,"191") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("acceptance",String.valueOf(request.getId())))||
            //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
            (securityRepositoryJPA.userHasPermissions_OR(15L,"197") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("acceptance",String.valueOf(request.getId())))||
            //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
            (securityRepositoryJPA.userHasPermissions_OR(15L,"198") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("acceptance",String.valueOf(request.getId()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from acceptance_files "+
                    " where acceptance_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from acceptance where id="+request.getAny_id()+")="+myMasterId ;
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
    }
}