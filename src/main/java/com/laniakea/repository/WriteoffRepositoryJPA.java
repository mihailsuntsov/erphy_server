package com.laniakea.repository;

import com.laniakea.message.request.WriteoffForm;
import com.laniakea.message.request.WriteoffProductForm;
import com.laniakea.message.request.SearchForm;
import com.laniakea.message.request.UniversalForm;
import com.laniakea.message.response.ProductHistoryJSON;
import com.laniakea.message.response.WriteoffJSON;
import com.laniakea.message.response.FilesWriteoffJSON;
import com.laniakea.model.*;
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
public class WriteoffRepositoryJPA {
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
    public List<WriteoffJSON> getWriteoffTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int departmentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//(см. файл Permissions Id)
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
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
                    "           dp.name || ' ' || dp.address as department, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.writeoff_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as writeoff_date, " +
                    "           (select abbreviation from sprav_sys_opf where id=cmp.opf)||' '||cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.writeoff_date as writeoff_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from writeoff p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true ";

            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " to_char(p.writeoff_date, 'DD.MM.YYYY') ='"+searchString+"' or "+
                        " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                        " upper(dp.name) like upper('%" + searchString + "%') or "+
                        " upper(cmp.name) like upper('%" + searchString + "%') or "+
                        " upper(us.name) like upper('%" + searchString + "%') or "+
                        " upper(uc.name) like upper('%" + searchString + "%') or "+
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
            List<WriteoffJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                WriteoffJSON doc=new WriteoffJSON();
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
                doc.setDoc_number(Long.parseLong(             obj[10].toString()));
                doc.setWriteoff_date((String)(                 obj[11]));
                doc.setCompany((String)                       obj[12]);
                doc.setDate_time_created((String)             obj[13]);
                doc.setDate_time_changed((String)             obj[14]);
                doc.setDescription((String)                   obj[15]);
                doc.setIs_completed((Boolean)                 obj[16]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getWriteoffSize(String searchString, int companyId, int departmentId) {
//        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//(см. файл Permissions Id)
//        {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds = false;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id " +
                "           from writeoff p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN departments dp ON p.department_id=dp.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_archive,false) !=true ";

        if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
        {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
            {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                {//остается только на свои документы
                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
            } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
        }

        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " to_char(p.writeoff_date, 'DD.MM.YYYY') ='"+searchString+"' or "+
                    " to_char(p.doc_number,'0000000000') like '%"+searchString+"' or "+
                    " upper(dp.name) like upper('%" + searchString + "%') or "+
                    " upper(cmp.name) like upper('%" + searchString + "%') or "+
                    " upper(us.name) like upper('%" + searchString + "%') or "+
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
//        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    public List<WriteoffProductForm> getWriteoffProductTable(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//(см. файл Permissions Id)
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.product_id," +
                    " ap.writeoff_id," +
                    " ap.product_count," +
                    " ap.product_price," +
                    " ap.product_sumprice," +
                    " ap.edizm_id," +
                    " p.name as name," +
                    " (select edizm.short_name from sprav_sys_edizm edizm where edizm.id = ap.edizm_id) as edizm," +
                    " ap.reason_id," +
                    " coalesce(ap.additional,'')," +
                    " (select rasons.name from sprav_sys_writeoff rasons where rasons.id = ap.reason_id) as reason" +
                    " from " +
                    " writeoff_product ap " +
                    " INNER JOIN writeoff a ON ap.writeoff_id=a.id " +
                    " INNER JOIN products p ON ap.product_id=p.id " +
                    " where a.master_id = " + myMasterId +
                    " and ap.writeoff_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds and a.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and a.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery + " order by p.name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();
            List<WriteoffProductForm> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                WriteoffProductForm doc=new WriteoffProductForm();
                doc.setProduct_id(Long.parseLong(                       obj[0].toString()));
                doc.setWriteoff_id(Long.parseLong(                      obj[1].toString()));
                doc.setProduct_count((BigDecimal)                       obj[2]);
                doc.setProduct_price((BigDecimal)                       obj[3]);
                doc.setProduct_sumprice((BigDecimal)                    obj[4]);
                doc.setEdizm_id(obj[7]!=null?Long.parseLong(            obj[5].toString()):null);
                doc.setName((String)                                    obj[6]);
                doc.setEdizm((String)                                   obj[7]);
                doc.setReason_id((Integer)                              obj[8]);
                doc.setAdditional((String)                              obj[9]);
                doc.setReason((String)                                  obj[10]);
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
    public WriteoffJSON getWriteoffValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(17L, "223,224,225,226"))//см. _Permissions Id.txt
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds = false;
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
                    "           p.department_id as department_id, " +
                    "           dp.name ||' '||dp.address  as department, " +
                    "           p.doc_number as doc_number, " +
                    "           to_char(p.writeoff_date at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as writeoff_date, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           coalesce(p.is_completed,false) as is_completed, " +
                    "           p.writeoff_date as writeoff_date_sort, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from writeoff p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN departments dp ON p.department_id=dp.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";
            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            WriteoffJSON returnObj=new WriteoffJSON();

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                     obj[0].toString()));
                returnObj.setMaster((String)                        obj[1]);
                returnObj.setCreator((String)                       obj[2]);
                returnObj.setChanger((String)                       obj[3]);
                returnObj.setMaster_id(Long.parseLong(              obj[4].toString()));
                returnObj.setCreator_id(Long.parseLong(             obj[5].toString()));
                returnObj.setChanger_id(obj[6]!=null?Long.parseLong(obj[6].toString()):null);
                returnObj.setCompany_id(Long.parseLong(             obj[7].toString()));
                returnObj.setDepartment_id(Long.parseLong(          obj[8].toString()));
                returnObj.setDepartment((String)                    obj[9]);
                returnObj.setDoc_number(Long.parseLong(             obj[10].toString()));
                returnObj.setWriteoff_date((String)(                 obj[11]));
                returnObj.setCompany((String)                       obj[12]);
                returnObj.setDate_time_created((String)             obj[13]);
                returnObj.setDate_time_changed((String)             obj[14]);
                returnObj.setDescription((String)                   obj[15]);
                returnObj.setIs_completed((Boolean)                 obj[16]);
            }
            return returnObj;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertWriteoff(WriteoffForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L,"216,217,218"))//  "Создание"
        {
            Writeoff newDocument = new Writeoff();
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
                //дата торговой смены
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
                String writeoffDate = (request.getWriteoff_date() == null ? "" : request.getWriteoff_date());
                try {
                    newDocument.setWriteoff_date(writeoffDate.isEmpty() ? null : dateFormat.parse(writeoffDate));
                } catch (ParseException e) {e.printStackTrace();}
                //номер документа
                if (request.getDoc_number() != null && !request.getDoc_number().isEmpty() && request.getDoc_number().trim().length() > 0) {
                    newDocument.setDoc_number(Long.valueOf(request.getDoc_number()));
                } else newDocument.setDoc_number(generateDocNumberCode(request.getCompany_id()));


                //дополнительная информация
                newDocument.setDescription(request.getDescription());
                entityManager.persist(newDocument);
                entityManager.flush();
                return newDocument.getId();
            } else return null;
        } else return null;
    }

    @Transactional
    public  Boolean updateWriteoff(WriteoffForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"227") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"228") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",request.getId().toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"229") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",request.getId().toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"230") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",request.getId().toString())))
        {
            if(updateWriteoffWithoutTable(request)){                                      //метод 1
                try {//сохранение таблицы
                    String productIds = "";
                    if (request.getWriteoffProductTable().size() > 0) {
                        for (WriteoffProductForm row : request.getWriteoffProductTable()) {
                            if (!saveWriteoffProductTable(row, request.getId())) {//         //метод 2
                                break;
                            }
                            productIds = productIds + (productIds.length()>0?",":"") + row.getProduct_id();
                        }
                    }//удаление лишних строк
                    deleteWriteoffProductTableExcessRows(productIds.length()>0?productIds:"0", request.getId());

                    //если завершается приемка - запись в историю товара
                    if(request.isIs_completed()){
                        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                        for (WriteoffProductForm row : request.getWriteoffProductTable()) {
                            if (!addWriteoffProductHistory(row, request, myMasterId)) {//         //метод 3
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
    private Boolean updateWriteoffWithoutTable(WriteoffForm request) {
        EntityManager emgr = emf.createEntityManager();
        Writeoff document = emgr.find(Writeoff.class, request.getId());//сохраняемый документ
        try
        {
            emgr.getTransaction().begin();

            document.setDescription         (request.getDescription() == null ? "": request.getDescription());
            document.setDoc_number          (Long.valueOf(request.getDoc_number()));
            document.setIs_completed        (request.isIs_completed());
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
            String tradeDate = (request.getWriteoff_date() == null ? "" : request.getWriteoff_date());
            try {
                document.setWriteoff_date(tradeDate.isEmpty() ? null : dateFormat.parse(tradeDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            User changer = userService.getUserByUsername(userService.getUserName());
            document.setChanger(changer);//кто изменил

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            document.setDate_time_changed(timestamp);//дату изменения

            emgr.getTransaction().commit();
            emgr.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private Boolean saveWriteoffProductTable(WriteoffProductForm row, Long writeoff_id) {
        if(clearWriteoffProductTable(row.getProduct_id(), writeoff_id)){
            String stringQuery;
            try {
                stringQuery =   " insert into writeoff_product (" +
                        "product_id," +
                        "writeoff_id," +
                        "product_count," +
                        "product_price," +
                        "product_sumprice," +
                        "edizm_id," +
                        "reason_id,"+
                        "additional"+
                        ") values ("
                        + row.getProduct_id() +","
                        + row.getWriteoff_id() +","
                        + row.getProduct_count() + ","
                        + row.getProduct_price() +","
                        + row.getProduct_sumprice() +","
                        + row.getEdizm_id() +","
                        + row.getReason_id() +", '"
                        + row.getAdditional()+ "')";
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
    private Boolean deleteWriteoffProductTableExcessRows(String productIds, Long writeoff_id) {
        String stringQuery;
        try {
            stringQuery =   " delete from writeoff_product " +
                    " where writeoff_id=" + writeoff_id +
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
    private Boolean addWriteoffProductHistory(WriteoffProductForm row, WriteoffForm request , Long masterId) {
        String stringQuery;
        ProductHistoryJSON lastProductHistoryRecord =  getLastProductHistoryRecord(row.getProduct_id(),request.getDepartment_id());
        BigDecimal lastQuantity= lastProductHistoryRecord.getQuantity();
        BigDecimal lastAvgPurchasePrice= lastProductHistoryRecord.getAvg_purchase_price();
        BigDecimal lastAvgNetcostPrice= lastProductHistoryRecord.getAvg_netcost_price();
        BigDecimal lastPurchasePrice= lastProductHistoryRecord.getLast_purchase_price();

        try {
            stringQuery =
                    " insert into products_history (" +
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
                    17 +","+
                    row.getWriteoff_id() + ","+
                    row.getProduct_id() + ","+
                    lastQuantity.subtract(row.getProduct_count())+","+
                    row.getProduct_count().multiply(new BigDecimal(-1)) +","+
                    lastAvgPurchasePrice +","+
                    lastAvgNetcostPrice +","+
                    lastPurchasePrice+","+
                    row.getProduct_price()+","+
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
    private Boolean setProductQuantity(WriteoffProductForm row, WriteoffForm request , Long masterId) {
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
    public boolean deleteWriteoff (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"219") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"220") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",delNumbers))||
                //Если есть право на "Удаление по своим отделениям " и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"221") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",delNumbers))||
                //Если есть право на "Удаление своих документов" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"222") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            stringQuery = "Update writeoff p" +
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
        stringQuery = "select coalesce(max(doc_number)+1,1) from writeoff where company_id="+company_id+" and master_id="+myMasterId;
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
    public Boolean isWriteoffNumberUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long doc_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from writeoff where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and doc_number="+code;
        if(doc_id>0) stringQuery=stringQuery+" and id !="+doc_id; // чтобы он не срабатывал сам на себя
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
    private Boolean clearWriteoffProductTable(Long product_id, Long writeoff_id) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = " delete from " +
                " writeoff_product where " +
                "product_id="+product_id+
                " and writeoff_id="+writeoff_id +
                " and (select master_id from writeoff where id="+writeoff_id+")="+myMasterId;
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
    public boolean addFilesToWriteoff(UniversalForm request){
        Long writeoffId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id докмента принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"227") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff",writeoffId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"228") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",writeoffId.toString()))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"229") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",writeoffId.toString()))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"230") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",writeoffId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select writeoff_id from writeoff_files where writeoff_id=" + writeoffId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_WriteoffId_FileId(writeoffId,fileId);
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
    boolean manyToMany_WriteoffId_FileId(Long writeoffId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into writeoff_files " +
                    "(writeoff_id,file_id) " +
                    "values " +
                    "(" + writeoffId + ", " + fileId +")")
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
    public List<FilesWriteoffJSON> getListOfWriteoffFiles(Long writeoffId) {
        if(securityRepositoryJPA.userHasPermissions_OR(17L, "223,224"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            boolean needToSetParameter_MyDepthsIds = false;
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           writeoff p" +
                    "           inner join" +
                    "           writeoff_files pf" +
                    "           on p.id=pf.writeoff_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + writeoffId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(17L, "223")) //Если нет прав на просм по всем предприятиям
            {//остается на: своё предприятие ИЛИ свои подразделения или свои документы
                if (!securityRepositoryJPA.userHasPermissions_OR(17L, "224")) //Если нет прав на просм по своему предприятию
                {//остается на: просмотр всех доков в своих подразделениях ИЛИ свои документы
                    if (!securityRepositoryJPA.userHasPermissions_OR(17L, "225")) //Если нет прав на просмотр всех доков в своих подразделениях
                    {//остается только на свои документы
                        stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds and p.creator_id ="+userRepositoryJPA.getMyId();needToSetParameter_MyDepthsIds=true;
                    }else{stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId()+" and p.department_id in :myDepthsIds";needToSetParameter_MyDepthsIds=true;}//т.е. по всем и своему предприятиям нет а на свои отделения есть
                } else stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.original_name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            if(needToSetParameter_MyDepthsIds)
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());}

            List<Object[]> queryList = query.getResultList();

            List<FilesWriteoffJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                FilesWriteoffJSON doc=new FilesWriteoffJSON();
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
    public boolean deleteWriteoffFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(17L,"227") && securityRepositoryJPA.isItAllMyMastersDocuments("writeoff", String.valueOf(request.getId()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(17L,"228") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("writeoff",String.valueOf(request.getId())))||
                //Если есть право на "Редактирование по своим отделениям и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях
                (securityRepositoryJPA.userHasPermissions_OR(17L,"229") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsDocuments("writeoff",String.valueOf(request.getId())))||
                //Если есть право на "Редактирование своих документов" и id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта и отделение в моих отделениях и создатель документа - я
                (securityRepositoryJPA.userHasPermissions_OR(17L,"230") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments("writeoff",String.valueOf(request.getId()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from writeoff_files "+
                    " where writeoff_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from writeoff where id="+request.getAny_id()+")="+myMasterId ;
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
