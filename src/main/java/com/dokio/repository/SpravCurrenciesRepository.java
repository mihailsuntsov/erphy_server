/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/
package com.dokio.repository;

import com.dokio.message.request.Sprav.SpravCurrenciesForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.SpravCurrenciesJSON;
import com.dokio.model.Companies;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class SpravCurrenciesRepository {

    @Autowired
    private EntityManagerFactory emf;
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

    private Logger logger = Logger.getLogger("SpravCurrencies");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name_short","company","name_full","creator","date_time_created_sort","code_lit","date_created","code_num","is_default")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


    @SuppressWarnings("Duplicates")
    public List<SpravCurrenciesJSON> getCurrenciesTable (int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(51L, "649,650"))//(см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))//если есть право только на своё предприятие, но запрашиваем не своё
                throw new IllegalArgumentException("Invalid query parameters");

            stringQuery =       "select " +
                    "           p.id as id," +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name_short as name_short, " +
                    "           p.name_full as name_full, " +
                    "           p.code_lit as code_lit, " +
                    "           p.code_num as code_num, " +
                    "           p.is_default as is_default, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from sprav_currencies p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(51L, "649")) //Если нет прав на "Просмотр по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name_short) like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.name_full)  like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.code_lit)   like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.code_num)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            try {
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<SpravCurrenciesJSON> returnList = new ArrayList<>();
                for (Object[] obj : queryList) {
                    SpravCurrenciesJSON doc = new SpravCurrenciesJSON();
                    doc.setId(Long.parseLong(           obj[0].toString()));
                    doc.setCreator((String)             obj[1]);
                    doc.setChanger((String)             obj[2]);
                    doc.setCompany((String)             obj[3]);
                    doc.setDate_time_created((String)   obj[4]);
                    doc.setDate_time_changed((String)   obj[5]);
                    doc.setName_short((String)          obj[6]);
                    doc.setName_full((String)           obj[7]);
                    doc.setCode_lit((String)            obj[8]);
                    doc.setCode_num((String)            obj[9]);
                    doc.setIs_default((Boolean)         obj[10]);
                    returnList.add(doc);
                }
                return returnList;

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCurrenciesTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public Integer getCurrenciesSize(String searchString, Long companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(51L, "649,650")){//(см. файл Permissions Id)
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery =       "select " +
                    "           p.id as id " +
                    "           from sprav_currencies p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;
            if (!securityRepositoryJPA.userHasPermissions_OR(51L, "649")){ //Если нет прав на "Просмотр по всем предприятиям" - остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name_short) like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.name_full)  like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.code_lit)   like upper(CONCAT('%',:sg,'%')) or" +
                        " upper(p.code_num)   like upper(CONCAT('%',:sg,'%'))"+ ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            try {
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                return query.getResultList().size();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCurrenciesSize. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public SpravCurrenciesJSON getCurrenciesValues(Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(51L, "649,650"))// (см. файл Permissions Id)
        {
            String stringQuery;
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24'
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "     select  p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.name_short as name_short, " +
                    "           p.name_full as name_full, " +
                    "           p.code_lit as code_lit, " +
                    "           p.code_num as code_num, " +
                    "           p.is_default as is_default " +
                    "           from sprav_currencies p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(51L, "649")){ //Если нет прав на "Просмотр документов по всем предприятиям"
                //остается только на своё предприятие (650)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SpravCurrenciesJSON doc = new SpravCurrenciesJSON();
            for (Object[] obj : queryList) {
                doc.setId(Long.parseLong(           obj[0].toString()));
                doc.setMaster((String)              obj[1]);
                doc.setCreator((String)             obj[2]);
                doc.setChanger((String)             obj[3]);
                doc.setMaster_id(Long.parseLong(    obj[4].toString()));
                doc.setCreator_id(Long.parseLong(   obj[5].toString()));
                doc.setChanger_id(                  obj[6] != null ? Long.parseLong(obj[6].toString()) : null);
                doc.setCompany_id(Long.parseLong(   obj[7].toString()));
                doc.setCompany((String)             obj[8]);
                doc.setDate_time_created((String)   obj[9]);
                doc.setDate_time_changed((String)   obj[10]);
                doc.setName_short((String)          obj[11]);
                doc.setName_full((String)           obj[12]);
                doc.setCode_lit((String)            obj[13]);
                doc.setCode_num((String)            obj[14]);
                doc.setIs_default((Boolean)         obj[15]);
            }
            return doc;
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer updateCurrencies(SpravCurrenciesForm request) {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого апдейтят ), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(51L,"651") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_currencies",request.getId().toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого апдейтят) и предприятию аккаунта, ИЛИ
                (securityRepositoryJPA.userHasPermissions_OR(51L,"652") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_currencies",request.getId().toString())))
        {
            Long myId = userRepository.getUserIdByUsername(userRepository.getUserName());
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            String stringQuery;
            stringQuery =   " update sprav_currencies set " +
                    " changer_id = " + myId + ", "+
                    " date_time_changed= now()," +
                    " name_short = :name_short, " +
                    " name_full = :name_full, " +
                    " code_lit = :code_lit, " +
                    " code_num = :code_num " +

                    " where " +
                    " id= "+request.getId()+
                    " and master_id="+myMasterId;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name_short",request.getName_short());
                query.setParameter("name_full",request.getName_full());
                query.setParameter("code_lit",request.getCode_lit());
                query.setParameter("code_num",request.getCode_num());
                query.executeUpdate();
                return 1;
            }catch (Exception e) {
                logger.error("Exception in method updateCurrencies. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1; //недостаточно прав
    }

    // Возвращаем id в случае успешного создания
    // Возвращаем null в случае ошибки
    // Возвращаем -1 в случае отсутствия прав
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Long insertCurrencies(SpravCurrenciesForm request) {
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if ((   //если есть право на создание по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(51L, "645")) ||
                //если есть право на создание по всем подразделениям своего предприятия, и предприятие документа своё, и
                (securityRepositoryJPA.userHasPermissions_OR(51L, "646") && myCompanyId.equals(request.getCompany_id()))) &&
                //создается документ для предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            String stringQuery;
            Long myId = userRepository.getUserId();
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();
            stringQuery = "insert into sprav_currencies (" +
                    " master_id," + //мастер-аккаунт
                    " creator_id," + //создатель
                    " company_id," + //предприятие, для которого создается документ
                    " date_time_created," + //дата и время создания
                    " name_short," +//наименование
                    " name_full," +//наименование
                    " code_lit," +//наименование
                    " code_num," +//наименование
                    " is_default," +
                    " is_deleted" +
                    ") values ("+
                    myMasterId + ", "+//мастер-аккаунт
                    myId + ", "+ //создатель
                    request.getCompany_id() + ", "+//предприятие, для которого создается документ
                    " to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +//дата и время создания
                    " :name_short, " +
                    " :name_full, " +
                    " :code_lit, " +
                    " :code_num, " +
                    " false, " +
                    " false)";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name_short",request.getName_short());
                query.setParameter("name_full",request.getName_full());
                query.setParameter("code_lit",request.getCode_lit());
                query.setParameter("code_num",request.getCode_num());
                query.executeUpdate();
                stringQuery="select id from sprav_currencies where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                Query query2 = entityManager.createNativeQuery(stringQuery);
                return Long.valueOf(query2.getSingleResult().toString());
            } catch (Exception e) {
                logger.error("Exception in method insertCurrencies on inserting into sprav_currencies. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else {
            return -1L;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteCurrencies(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(51L, "647") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_currencies", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(51L, "648") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_currencies", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_currencies p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteCurrencies. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteCurrencies(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(51L, "647") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_currencies", delNumbers)) ||
            //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
            (securityRepositoryJPA.userHasPermissions_OR(51L, "648") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_currencies", delNumbers))) {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_currencies p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers.replaceAll("[^0-9\\,]", "") + ")";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method undeleteCurrencies. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Integer setDefaultCurrency(UniversalForm request) {// id : предприятие, id3 : id документа
        EntityManager emgr = emf.createEntityManager();
        Long myCompanyId=userRepositoryJPA.getMyCompanyId_();// моё
        Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getId());//предприятие для редактируемого документа
        Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия редактируемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if ((   //если есть право на редактирование по всем предприятиям, или
                (securityRepositoryJPA.userHasPermissions_OR(51L, "651")) ||
                //если есть право на редактирование по всем отделениям своего предприятия, и предприятие документа своё, и
                (securityRepositoryJPA.userHasPermissions_OR(51L, "652") && myCompanyId.equals(request.getId()))) &&
                //редактируется документ предприятия моего владельца (т.е. под юрисдикцией главного аккаунта)
                DocumentMasterId.equals(myMasterId))
        {
            try
            {
                String stringQuery;
                stringQuery =   " update sprav_currencies set is_default=(" +
                        " case when (id="+request.getId3()+") then true else false end) " +
                        " where " +
                        " company_id= "+request.getId();
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    public SpravCurrenciesJSON getDefaultCompanyCurrency(Long companyId) {
        String stringQuery;
        stringQuery = "select " +
                "   cur.name_full," +                    // full name of Accounting currency of user's company (e.g. US Dollar)
                "   cur.name_short," +                   // short name of Accounting currency of user's company (e.g. $)
                "   cur.code_lit," +                     // (e.g. EUR)
                "   cur.code_num" +                      // (e.g. 978 for Euro)
                "   from    " +
                "   sprav_currencies cur" +
                "   where cur.company_id=" + companyId +
                "   and cur.is_default = true ";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            SpravCurrenciesJSON doc = new SpravCurrenciesJSON();
            if(queryList.size()>0) {
                doc.setName_full((String)       queryList.get(0)[0]);
                doc.setName_short((String)      queryList.get(0)[1]);
                doc.setCode_lit((String)        queryList.get(0)[2]);
                doc.setCode_num((String)        queryList.get(0)[3]);
            }
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getDefaultCompanyCurrency. SQL query:" + stringQuery, e);
            return null;
        }
    }

    // inserting base set of currencies on register of new user
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertCurrenciesFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{
        "'curr_us_dollar'","'curr_euro'","'curr_canadian_dollar'","'curr_australian_dollar'","'curr_new_zealand_dollar'","'curr_russian_rouble'","'curr_pound_sterling'"});
        stringQuery = "insert into sprav_currencies ( master_id,creator_id,company_id,date_time_created,name_short,name_full,code_lit,code_num,is_default,is_deleted) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'$','"+map.get("curr_us_dollar")+"',            'USD','840',true, false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'€',  '"+map.get("curr_euro")+"',                 'EUR','978',false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'C$', '"+map.get("curr_canadian_dollar")+"',      'CAD','124',false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'A$', '"+map.get("curr_australian_dollar")+"',    'AUD','036',false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'$','"+map.get("curr_new_zealand_dollar")+"',   'NZD','554',false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'р.', '"+map.get("curr_russian_rouble")+"',       'RUB','643',false,false),"+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'£',  '"+map.get("curr_pound_sterling")+"',       'GBP','826',false,false);";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertCurrenciesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }



}
