package com.laniakea.repository;

import com.laniakea.message.request.Sprav.SpravSysEdizmForm;
import com.laniakea.message.request.UniversalForm;
import com.laniakea.message.response.Sprav.SpravSysEdizmJSON;
import com.laniakea.message.response.Sprav.SpravSysEdizmTableJSON;
import com.laniakea.model.Sprav.SpravSysEdizm;
import com.laniakea.model.User;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SpravSysEdizmJPA {


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
    public List<SpravSysEdizmTableJSON> getSpravSysEdizmTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(11L, "118,119"))//типы цен (см. файл Permissions Id)
        {
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
                    "           p.name as name, " +
                    "           p.short_name as short_name " +
                    "           from sprav_sys_edizm p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId ;

            if (!securityRepositoryJPA.userHasPermissions_OR(11L, "118")) //Если нет прав на "Меню - таблица - Типы цен по всем предприятиям"
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
    public int getSpravSysEdizmSize(String searchString, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(11L, "118,119"))//типы цен (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           p.name as name, " +
                    "           p.short_name as short_name " +
                    "           from sprav_sys_edizm p " +
                    "           where  p.master_id=" + myMasterId ;


            if (!securityRepositoryJPA.userHasPermissions_OR(11L, "118")) //Если нет прав на "Меню - таблица - Типы цен по всем предприятиям"
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
        if (securityRepositoryJPA.userHasPermissions_OR(11L, "122,123,124,125"))//Типы цен: см. _Permissions Id.txt
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

            if (!securityRepositoryJPA.userHasPermissions_OR(11L, "122,124")) //Если нет прав на просм или редакт. по всем предприятиям
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
    public boolean deleteSpravSysEdizmById(String delNumbers) {
        if(securityRepositoryJPA.userHasPermissions_OR(11L,"121")&& //Типы цен: "Удаление"
                securityRepositoryJPA.isItAllMyMastersSpravSysEdizm(delNumbers))  //все ли Типы цен принадлежат текущему родительскому аккаунту
        {
            String stringQuery;
            stringQuery="delete from sprav_type_prices p" +
                    " where p.id in ("+ delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if(!stringQuery.isEmpty() && stringQuery.trim().length() > 0){
                int count = query.executeUpdate();
                return true;
            }else return false;
        }else return false;
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
}
