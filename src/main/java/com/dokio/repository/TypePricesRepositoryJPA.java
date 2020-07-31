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

import com.dokio.message.request.TypePricesForm;
import com.dokio.message.response.PriceTypesListJSON;
import com.dokio.message.response.TypePricesTableJSON;
import com.dokio.model.Sprav.SpravSysPriceRole;
import com.dokio.model.Sprav.SpravTypePrices;
import com.dokio.model.Sprav.SpravTypePricesJSON;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TypePricesRepositoryJPA {


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
    public List<TypePricesTableJSON> getTypePricesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L, "91,92"))//типы цен (см. файл Permissions Id)
             {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pr.name as pricerole, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.pricerole_id as pricerole_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI')as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI')as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from sprav_type_prices p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_pricerole pr ON p.pricerole_id=pr.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(9L, "91")) //Если нет прав на "Меню - таблица - Типы цен по всем предприятиям"
            {
                //остается только на своё предприятие (92)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, TypePricesTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            return query.getResultList();
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    @Transactional
    public int getTypePricesSize(String searchString, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L, "91,92"))//типы цен (см. файл Permissions Id)
        {
            String stringQuery;
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
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI')as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI')as date_time_changed, " +
                    "           p.name as name, " +
                    "           p.description as description " +
                    "           from sprav_type_prices p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true";



            if (!securityRepositoryJPA.userHasPermissions_OR(9L, "91")) //Если нет прав на "Меню - таблица - Типы цен по всем предприятиям"
            {
                //остается только на своё предприятие (92)
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
    public SpravTypePricesJSON getTypePricesValuesById (int id) {
        if (securityRepositoryJPA.userHasPermissions_OR(9L, "95,96,97,98"))//Типы цен: см. _Permissions Id.txt
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pr.name as pricerole, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.pricerole_id as pricerole_id, " +
                    "           cmp.name as company, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
                    "           p.description as description " +
                    "           from sprav_type_prices p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_pricerole pr ON p.pricerole_id=pr.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(9L, "95,97")) //Если нет прав на просм или редакт. по всем предприятиям
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery, SpravTypePricesJSON.class);

            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                SpravTypePricesJSON response = (SpravTypePricesJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public boolean updateTypePrices(TypePricesForm request) {
        boolean perm_AllCompaniesUpdate=securityRepositoryJPA.userHasPermissions_OR(9L, "97"); // Типы цен:"Редактирование документов по всем предприятиям" (в пределах родительского аккаунта)
        boolean perm_MyCompanyUpdate=securityRepositoryJPA.userHasPermissions_OR(9L, "98"); // Типы цен:"Редактирование документов своего предприятия"

        boolean itIsDocumentOfMyMasters=securityRepositoryJPA.isItMyMastersTypePrices(Long.valueOf(request.getId()));//документ под юрисдикцией главного аккаунта
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
            SpravTypePrices updateDocument = emgr.find(SpravTypePrices.class, id);
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
            //Роль цены
            EntityManager em = emf.createEntityManager();
            SpravSysPriceRole priceRole = em.find(SpravSysPriceRole.class, (Long.valueOf(Integer.parseInt(request.getPricerole_id()))));
            updateDocument.setPricerole(priceRole);
            //дополнительная информация
            updateDocument.setDescription (request.getDescription() == null ? "": request.getDescription());

            emgr.getTransaction().commit();
            emgr.close();
            return true;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertTypePrices(TypePricesForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L,"93"))//  Типы цен : "Создание"
        {
            SpravTypePrices newDocument = new SpravTypePrices();
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
            //Роль цены
            EntityManager em = emf.createEntityManager();
            SpravSysPriceRole priceRole = em.find(SpravSysPriceRole.class, (Long.valueOf(Integer.parseInt(request.getPricerole_id()))));
            newDocument.setPricerole(priceRole);
            //дополнительная информация
            newDocument.setDescription(request.getDescription());
            entityManager.persist(newDocument);
            entityManager.flush();
            return newDocument.getId();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteTypePricesById(String delNumbers) {
        if(securityRepositoryJPA.userHasPermissions_OR(9L,"94")&& //Типы цен: "Удаление"
                securityRepositoryJPA.isItAllMyMastersTypePrices(delNumbers))  //все ли Типы цен принадлежат текущему родительскому аккаунту
        {
            String stringQuery;
            stringQuery="Update sprav_type_prices p" +
                    " set is_archive=true "+
                    " where p.id in ("+ delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if(!stringQuery.isEmpty() && stringQuery.trim().length() > 0){
                int count = query.executeUpdate();
                return true;
            }else return false;
        }else return false;
    }

    @SuppressWarnings("Duplicates")
    public List<PriceTypesListJSON> getPriceTypesList(int companyId) {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery=
                "select " +
                        " p.id as id, " +
                        " p.name  as name, " +
                        " p.description as description " +
                        " from sprav_type_prices p " +
                        " where p.master_id=" + myMasterId +
                        " and p.company_id=" + companyId +
                        " and coalesce(p.is_archive, false) = false"+
                        " order by p.name asc";
        Query query =  entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<PriceTypesListJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList) {
            PriceTypesListJSON doc=new PriceTypesListJSON();
            doc.setId(Long.parseLong(obj[0].toString()));
            doc.setName((String) obj[1]);
            doc.setDescription((String) obj[2]);
            returnList.add(doc);
        }
        return returnList;
    }
}
