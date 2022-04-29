/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.repository;

import com.dokio.message.request.Sprav.SpravSysEdizmForm;
import com.dokio.message.request.UniversalForm;
import com.dokio.message.response.Sprav.SpravSysEdizmJSON;
import com.dokio.message.response.Sprav.SpravSysEdizmTableJSON;
import com.dokio.model.Sprav.SpravSysEdizm;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Repository
public class SpravSysEdizmJPA {


    Logger logger = Logger.getLogger("SpravSysEdizmJPA");

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
    public List<SpravSysEdizmTableJSON> getSpravSysEdizmTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(11L, "122,123"))//типы цен (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.short_name as short_name, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from sprav_sys_edizm p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(11L, "122")) //Если нет прав на по всем предприятиям"
            {
                //остается только на своё предприятие (119)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, SpravSysEdizmTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            return query.getResultList();
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    @Transactional
    public int getSpravSysEdizmSize(String searchString, int companyId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(11L, "122,123"))//типы цен (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           p.name as name, " +
                    "           p.short_name as short_name " +
                    "           from sprav_sys_edizm p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;


            if (!securityRepositoryJPA.userHasPermissions_OR(11L, "122")) //Если нет прав на по всем предприятиям"
            {
                //остается только на своё предприятие (119)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery);

            return query.getResultList().size();
        } else return 0;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public SpravSysEdizmJSON getSpravSysEdizmValuesById (int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(11L, "122,123"))//Типы цен: см. _Permissions Id.txt
        {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select " +
                "           p.id as id, " +
                "           u.name as master, " +
                "           p.name as name, " +
                "           pr.id as type_id, " +
                "           p.short_name as short_name, " +
                "           us.name as creator, " +
                "           uc.name as changer, " +
                "           trunc(equals_si,3) as equals_si, " +
                "           p.master_id as master_id, " +
                "           p.creator_id as creator_id, " +
                "           p.changer_id as changer_id, " +
                "           p.company_id as company_id, " +
                "           cmp.name as company, " +
                "           p.date_time_created as date_time_created, " +
                "           p.date_time_changed as date_time_changed " +
                "           from sprav_sys_edizm p " +
                "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           LEFT OUTER JOIN sprav_sys_edizm_types pr ON p.type_id=pr.id " +
                "           where  p.master_id=" + myMasterId +
                "           and p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(11L, "122")) //Если нет прав на просм или редакт. по всем предприятиям
            {
         //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

        Query query = entityManager.createNativeQuery(stringQuery, SpravSysEdizmJSON.class);

        try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
            SpravSysEdizmJSON response = (SpravSysEdizmJSON) query.getSingleResult();
            return response;}
        catch(NoResultException nre){return null;}
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public boolean updateSpravSysEdizm(SpravSysEdizmForm request) {
        boolean perm_AllCompaniesUpdate=securityRepositoryJPA.userHasPermissions_OR(11L, "124"); // Типы цен:"Редактирование документов по всем предприятиям" (в пределах родительского аккаунта)
        boolean perm_MyCompanyUpdate=securityRepositoryJPA.userHasPermissions_OR(11L, "125"); // Типы цен:"Редактирование документов своего предприятия"

        boolean itIsDocumentOfMyMasters=securityRepositoryJPA.isItMyMastersSpravSysEdizm(Long.valueOf(request.getId()));//документ под юрисдикцией главного аккаунта
        boolean itIsDocumentOfMyCompany=userRepositoryJPA.getMyCompanyId()==Integer.parseInt(request.getCompany_id());//сохраняется документ моего предприятия

        if
        (
                (perm_AllCompaniesUpdate ||                                     //если есть права изменять доки всех предприятий
                        (itIsDocumentOfMyCompany && perm_MyCompanyUpdate)            //или это мое предприятие и есть права изменять доки своего предприятия
                )
                        && itIsDocumentOfMyMasters                                      //+документ под юрисдикцией главного (родительского) аккаунта
        ){
        EntityManager emgr = emf.createEntityManager();
        emgr.getTransaction().begin();
        Long id=Long.valueOf(request.getId());
        SpravSysEdizm updateDocument = emgr.find(SpravSysEdizm.class, id);
        //id
        updateDocument.setId(id);
        //кто изменил
        User changer = userRepository.getUserByUsername(userRepository.getUserName());
        updateDocument.setChanger(changer);
        //дату изменения
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        updateDocument.setDate_time_changed(timestamp);
        //Наименование
        updateDocument.setName (request.getName() == null ? "": request.getName());
        //Краткое наименование единицы измерения (например, шт., кг.)
        updateDocument.setShort_name (request.getShort_name() == null ? "": request.getShort_name());
        //Тип единицы измерения (1-Предмет, 2-Дина, 3-Пощадь, 4-Объем)
        Integer edizmType = (Integer.parseInt(request.getType_id()));
        updateDocument.setType_id(edizmType);
        //множитель для приведения кастомной единицы измерения к величине СИ, выбранной пунктом выше (кроме Предмет)
        if (request.getEquals_si() != null && !request.getEquals_si().isEmpty() && request.getEquals_si().trim().length() > 0) {
            updateDocument.setEquals_si(new BigDecimal(request.getEquals_si().replace(",",".")));
        } else { updateDocument.setEquals_si(new BigDecimal("0"));}



        emgr.getTransaction().commit();
        emgr.close();
        return true;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertSpravSysEdizm(SpravSysEdizmForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(11L,"120"))//  Типы цен : "Создание"
        {
            SpravSysEdizm newDocument = new SpravSysEdizm();
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
            //Наименование
            newDocument.setName (request.getName() == null ? "": request.getName());
            //Краткое наименование единицы измерения (например, шт., кг.)
            newDocument.setShort_name (request.getShort_name() == null ? "": request.getShort_name());
            //Тип единицы измерения (1-Предмет, 2-Дина, 3-Пощадь, 4-Объем)
            Integer edizmType = (Integer.parseInt(request.getType_id()));
            newDocument.setType_id(edizmType);
            //множитель для приведения кастомной единицы измерения к величине СИ, выбранной пунктом выше (кроме Предмет)
            if (request.getEquals_si() != null && !request.getEquals_si().isEmpty() && request.getEquals_si().trim().length() > 0) {
                newDocument.setEquals_si(new BigDecimal(request.getEquals_si().replace(",",".")));
            } else { newDocument.setEquals_si(new BigDecimal("0"));}


            entityManager.persist(newDocument);
            entityManager.flush();
            return newDocument.getId();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteEdizmById(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if ((securityRepositoryJPA.userHasPermissions_OR(11L, "121") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_sys_edizm", delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(11L, "121") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_sys_edizm", delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery = "update sprav_sys_edizm p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.master_id=" + myMasterId +
                    " and p.id in (" + delNumbers + ")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            } catch (Exception e) {
                logger.error("Exception in method deleteTaxes on updating deleteEdizmById. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteEdizm(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if(     (securityRepositoryJPA.userHasPermissions_OR(11L,"121") && securityRepositoryJPA.isItAllMyMastersDocuments("sprav_sys_edizm",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(11L,"121") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sprav_sys_edizm",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update sprav_sys_edizm p" +
                    " set changer_id="+ myId + ", " + // кто изменил (восстановил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //не удалена
                    " where p.id in (" + delNumbers+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteEdizm. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
    @SuppressWarnings("Duplicates")
    public List<SpravSysEdizmTableJSON> getSpravSysEdizm(UniversalForm request) {
//        if(securityRepositoryJPA.userHasPermissions_OR(11L, "118,119"))//типы цен (см. файл Permissions Id)
//        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.name as name, " +
                    "           p.short_name as short_name " +
                    "           from sprav_sys_edizm p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           INNER JOIN sprav_sys_edizm_types et ON p.type_id=et.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.company_id="+ request.getId1() +
                    "           and p.type_id in "+ request.getString1()+ " order by p.id asc";

            Query query = entityManager.createNativeQuery(stringQuery, SpravSysEdizmTableJSON.class);
            return query.getResultList();
//        } else return null;
    }


    // inserting base set of units of measurement of new user
    // Types oа UoM 1 - object, 2 - mass, 3 - length, 4 - area, 5 - volume
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertEdizmFast(Long mId, Long cId) {
        String stringQuery;
        String t = new Timestamp(System.currentTimeMillis()).toString();
        stringQuery = "insert into sprav_sys_edizm ( master_id,creator_id,company_id,date_time_created,name,short_name,type_id,equals_si) values "+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Uncountable','',1,null),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Kilogramm','kg',2,1),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Gramm','g',2,0.001),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Ton','t',2,1000),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Meter','m',3,1),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Centimeter','cm',3,0.01),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Litr','L',5,0.001),"+
                "("+mId+","+mId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'Square meter','m2',4,1);";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertEdizmFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }



}
