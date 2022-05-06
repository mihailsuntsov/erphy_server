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

import com.dokio.message.response.*;
import com.dokio.model.Sites;
import com.dokio.model.User;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.*;

@Repository
public class SitesRepository {
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
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    private EntityManagerFactory emf;

    @Transactional
    @SuppressWarnings("Duplicates")
    public SitesTableJSON getSitesTable(int result,
                                        int offset,
                                        int offsetreal,
                                        String searchString,
                                        String sortColumn,
                                        String sortAsc,
                                        int companyId) {
//        if (securityRepositoryJPA.userHasPermissions_OR(18L, "235,236,237")) {
            String stringQuery;
            List<Integer> pagesList;// информация для пагинации. Первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =
                    "select  p.id as id, " +
                            "           p.name as name, " +
                            "           p.description as description, " +
                            "           coalesce(p.stopped,false ) as stopped, " +
                            "           p.domain as domain " +
                            "           from sites p "+
                            "           where  p.master_id=" + myMasterId +
                            "           and coalesce(p.is_archive,false) !=true ";

//            if (!securityRepositoryJPA.userHasPermissions_OR(18L, "235")) //Если нет прав по всем предприятиям"
//            {
//                //остается только на своё предприятие
//                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
//            }

            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or " +
                        "upper(p.description) like upper('%" + searchString + "%')) ";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " order by p.name asc";
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();//получили полный список сайтов  в лист

            List<SitesJSON> returnList = new ArrayList<>();

            for (Object[] obj : queryList) {
                SitesJSON doc = new SitesJSON();
                doc.setId(Long.parseLong(obj[0].toString()));
                doc.setName((String) obj[1]);
                doc.setDescription((String) obj[2]);
                doc.setStopped((Boolean) obj[3]);
                doc.setDomain((String) obj[4]);
                returnList.add(doc);
            }
            if (sortColumn.equals("p.name")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(SitesJSON.COMPARE_BY_NAME_ASC);
                } else {
                    returnList.sort(SitesJSON.COMPARE_BY_NAME_DESC);
                }
            }
            if (sortColumn.equals("description")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(SitesJSON.COMPARE_BY_DESCRIPTION_ASC);
                } else {
                    returnList.sort(SitesJSON.COMPARE_BY_DESCRIPTION_DESC);
                }
            }
            if (sortColumn.equals("domain")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(SitesJSON.COMPARE_BY_DOMAIN_ASC);
                } else {
                    returnList.sort(SitesJSON.COMPARE_BY_DOMAIN_DESC);
                }
            }
            if (sortColumn.equals("stopped")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(SitesJSON.COMPARE_BY_STOPPED_ASC);
                } else {
                    returnList.sort(SitesJSON.COMPARE_BY_STOPPED_DESC);
                }
            }
            if (sortColumn.equals("domain_associated")) {
                if (sortAsc.equals("asc")) {
                    returnList.sort(SitesJSON.COMPARE_BY_DOMAIN_ASSOCIATED_ASC);
                } else {
                    returnList.sort(SitesJSON.COMPARE_BY_DOMAIN_ASSOCIATED_DESC);
                }
            }


            //вычисление пагинации
            int returnListSize = returnList.size();
            pagesList = getPagesList(result, offset, returnListSize);

            //обрезаем лишнее
            returnList = returnList.subList(offsetreal, (offsetreal + result) > returnListSize ? returnListSize : (offsetreal + result));

            SitesTableJSON sitesTableForm = new SitesTableJSON();
            sitesTableForm.setTable(returnList);//проверка на IndexOutOfBoundsException
            sitesTableForm.setReceivedPagesList(pagesList);
            return sitesTableForm;
//        } else return null;
    }

    @SuppressWarnings("Duplicates")
    private List<Integer> getPagesList(int result, int offset, int size) {
        int listsize;//количество страниц пагинации
        int pagenum;// отображаемый в пагинации номер страницы. Всегда на 1 больше чем offset. Если offset не определен то это первая страница
        pagenum = offset + 1;
        if ((size % result) == 0) {//общее количество выборки делим на количество записей на странице
            listsize = size / result;//если делится без остатка
        } else {
            listsize = (size / result) + 1;
        }
        int maxPagenumInBegin;//
        List<Integer> pageList = new ArrayList<Integer>();//список, в котором первые 3 места - "всего найдено", "страница", "всего страниц", остальное - номера страниц для пагинации
        pageList.add(size);
        pageList.add(pagenum);
        pageList.add(listsize);

        if (listsize <= 5) {
            maxPagenumInBegin = listsize;//
        } else {
            maxPagenumInBegin = 5;
        }
        if (pagenum >= 3) {
            if ((pagenum == listsize) || (pagenum + 1) == listsize) {
                for (int i = (pagenum - (4 - (listsize - pagenum))); i <= pagenum - 3; i++) {
                    if (i > 0) {
                        pageList.add(i);  //создается список пагинации за - 4 шага до номера страницы (для конца списка пагинации)
                    }
                }
            }
            for (int i = (pagenum - 2); i <= pagenum; i++) {
                pageList.add(i);  //создается список пагинации за -2 шага до номера страницы
            }
            if ((pagenum + 2) <= listsize) {
                for (int i = (pagenum + 1); i <= (pagenum + 2); i++) {
                    pageList.add(i);  //создается список пагинации  на +2 шага от номера страницы
                }
            } else {
                if (pagenum < listsize) {
                    for (int i = (pagenum + (listsize - pagenum)); i <= listsize; i++) {
                        pageList.add(i);  //создается список пагинации от номера страницы до конца
                    }
                }
            }
        } else {//номер страницы меньше 3
            for (int i = 1; i <= pagenum; i++) {
                pageList.add(i);  //создается список пагинации от 1 до номера страницы
            }
            for (int i = (pagenum + 1); i <= maxPagenumInBegin; i++) {
                pageList.add(i);  //создаются дополнительные номера пагинации, но не более 5 в сумме
            }
        }

        return pageList;
    }

    //*****************************************************************************************************************************************************
//****************************************************      CRUD      *********************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    @Transactional
    public SitesJSON getSiteValuesById (Long id) {
        if (securityRepositoryJPA.userHasPermissions_OR(20L, "246,247"))//см. _Permissions Id.txt
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String dateFormat=userRepositoryJPA.getMyDateFormat();
            stringQuery = "select " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           cmp.name as company, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.description as description, " +
                    "           p.domain as domain, " +
                    "           coalesce(p.stopped,false ) as stopped, " +
                    "           coalesce(p.domain_associated,false ) as domain_associated, " +
//                    "           p._______ as __________, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           p.name as name " +
                    "           from sites p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and p.id= " + id+
                    "           and coalesce(p.is_archive,false) !=true";
            if (!securityRepositoryJPA.userHasPermissions_OR(20L, "247")) //Если нет прав на "Редактирование документов по всем предприятиям"
            {
                //остается только на своё предприятие (248)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            SitesJSON returnObj=new SitesJSON();

            for(Object[] obj:queryList){
                returnObj.setId(Long.parseLong(                     obj[0].toString()));
                returnObj.setMaster((String)                        obj[1]);
                returnObj.setCreator((String)                       obj[2]);
                returnObj.setChanger((String)                       obj[3]);
                returnObj.setCompany((String)                       obj[4]);
                returnObj.setMaster_id(Long.parseLong(              obj[5].toString()));
                returnObj.setCreator_id(Long.parseLong(             obj[6].toString()));
                returnObj.setChanger_id(obj[7]!=null?Long.parseLong(obj[7].toString()):null);
                returnObj.setCompany_id(Long.parseLong(             obj[8].toString()));
                returnObj.setDescription((String)                   obj[9]);
                returnObj.setDomain((String)                        obj[10]);
                returnObj.setStopped((Boolean)                      obj[11]);
                returnObj.setDomain_associated((Boolean)            obj[12]);
                returnObj.setDate_time_created((String)             obj[13]);
                returnObj.setDate_time_changed((String)             obj[14]);
                returnObj.setName((String)                          obj[15]);
            }
            return returnObj;
        } else return null;
    }


    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertSite(SitesJSON request) {
        if(securityRepositoryJPA.userHasPermissions_OR(20L,"248,249"))//  "Создание"
        {
            Sites newDocument = new Sites();
            EntityManager emgr = emf.createEntityManager();
            //владелец
            User master = userRepository.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository.getUserName() )));

            if(companyRepositoryJPA.getCompanyById((request.getCompany_id())).getMaster().getId()==master.getId())
            {//проверка на то, что предприятие, для которого содается документ, находится под главным аккаунтом
                newDocument.setMaster(master);
                //предприятие
                newDocument.setCompany(companyRepositoryJPA.getCompanyById((request.getCompany_id())));
                //дата и время создания
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                newDocument.setDate_time_created(timestamp);//
                //создатель
                User creator = userRepository.getUserByUsername(userRepository.getUserName());
                newDocument.setCreator(creator);
                //название сайта
                newDocument.setName(request.getName());
                //UUID
                newDocument.setUid(UUID.randomUUID());
                //дополнительная информация
                newDocument.setDescription(request.getDescription());
                entityManager.persist(newDocument);
                entityManager.flush();
                return newDocument.getId();
            } else return null;
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteSite (String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(20L,"250") && securityRepositoryJPA.isItAllMyMastersDocuments("sites",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(20L,"251") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("sites",delNumbers)))
        {
            String stringQuery;// на MasterId не проверяю , т.к. выше уже проверено
            stringQuery = "Update sites p" +
                    " set is_archive=true " +
                    " where p.id in ("+delNumbers+")";
            entityManager.createNativeQuery(stringQuery).executeUpdate();
            return true;
        } else return false;
    }



}
