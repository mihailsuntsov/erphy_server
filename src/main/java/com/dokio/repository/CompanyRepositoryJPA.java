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

import com.dokio.message.request.CompanyForm;
import com.dokio.model.Companies;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.model.Sprav.SpravSysCompaniesList;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Repository("CompanyRepositoryJPA")
public class CompanyRepositoryJPA {

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

    public Companies getCompanyById(Long id){
        EntityManager em = emf.createEntityManager();
        Companies cmp = em.find(Companies.class, id);
        return cmp;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateCompany(CompanyForm company){
        String stringQuery;
        int myCompanyId = userRepositoryJPA.getMyCompanyId();
        int companyId=company.getId();

        if(securityRepositoryJPA.userHasPermissions_OR(3L, "7,8") &&// Предприятия: "Редактирование своего" "Редактирование всех"
           securityRepositoryJPA.isItMyMastersCompany(Long.valueOf(companyId))) {//принадлежит к предприятиям моего родителя

            stringQuery = "update companies set " +
                    "namefull='" + company.getNameFull() + "'," +
                    "name='" + company.getNameShort() + "'," +
                    "addressfact='" + company.getAddressfact() + "'," +
                    "addressjur='" + company.getAddressjur() + "'," +
                    "opf=" + (company.getOpf_id().equals("0") ? null : company.getOpf_id()) + "," +
                    "reg_num='" + company.getReg_num() + "'," +
                    "inn='" + company.getInn() + "'," +
                    "who_got='" + company.getWho_got() + "'," +
                    "datereg=" + (company.getDateReg() == "" ? null : "to_date('" + company.getDateReg() + "','DD.MM.YYYY')") + "," +
                    "korschet='" + company.getKorschet() + "'," +
                    "rs='" + company.getRs() + "'," +
                    "bank='" + company.getBank() + "'," +

                    "currency_id='" + company.getCurrency_id() + "'," +
                    "st_prefix_barcode_packed=" + company.getSt_prefix_barcode_packed() + ", " +
                    "st_prefix_barcode_pieced=" + company.getSt_prefix_barcode_pieced() +
                    " where id=" + company.getId();
            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "8")) {//если нет прав на Предприятия: "Редактирование всех"
                if(myCompanyId != companyId){//значит остаются на "Редактирование своего", НО если запрашиваем id НЕ своего предприятия:
                    return false;
                }
            }
            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "7")) {//если нет прав на Предприятия: "Редактирование своего"
                if(myCompanyId == companyId){//значит остаются на "Редактирование всех", НО если запрашиваем id своего предприятия:
                    return false;
                }
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            int i = query.executeUpdate();
            return (i==1);
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public CompaniesJSON getCompanyValuesById(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "5,6,7,8") // Предприятия: "Просмотр своего" "Просмотр всех" "Редактирование своего" "Редактирование всех"
          )
        {
            String stringQuery;
            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery = "select p.id as id, " +
                    "           p.name as name, " +
                    "           p.namefull as namefull, " +
                    "           p.master_id as owner_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           (select name from users where id=p.master_id) as owner, " +
                    "           (select name from users where id=p.creator_id) as creator, " +
                    "           (select name from users where id=p.changer_id) as changer, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
                    "           p.addressjur as addressjur, " +
                    "           p.addressfact as addressfact, " +
                    "           p.st_prefix_barcode_packed as st_prefix_barcode_packed, " +
                    "           p.st_prefix_barcode_pieced as st_prefix_barcode_pieced, " +
                    "           p.currency_id as currency_id, " +
                    "           coalesce(p.opf,'0') as opf_id, " +
                    "           (select name from sprav_sys_opf where id=p.opf) as opf_name, " +
                    "           p.inn as inn, " +
                    "           p.reg_num as reg_num, " +
                    "           p.who_got as who_got, " +
                    "           coalesce(to_char(p.datereg,'DD.MM.YYYY'),'') as datereg, " +
                    "           p.korschet as korschet, " +
                    "           p.rs as rs, " +
                    "           p.bank as bank, " +
                    "           p.bik as bik " +

                    "           from companies p" +
                    " where p.id= " + id;
            stringQuery = stringQuery + " and p.master_id="+userRepositoryJPA.getMyMasterId();//принадлежит к предприятиям моего родителя

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "6,8")) {//если нет прав на Предприятия: "Просмотр всех", "Редактирование всех"
                if(myCompanyId != id){//значит остаются на "Просмотр своего", "Редактирование своего", НО если запрашиваем id не своего предприятия:
                    return null;
                }//else stringQuery = stringQuery + " and p.id=" + myCompanyId;
            }

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "5,7")) {//если нет прав на Предприятия: "Просмотр своего", "Редактирование своего"
                if(myCompanyId == id){//значит остаются на "Просмотр всех", "Редактирование всех", НО если запрашиваем id  своего предприятия:
                    return null;
                }
            }

            Query query = entityManager.createNativeQuery(stringQuery, CompaniesJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                return (CompaniesJSON) query.getSingleResult();}
            catch(NoResultException nre){return null;}
        } else return null;
    }


    @Transactional
    public Long insertCompany(Companies company) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L,"3"))//  Предприятия : "Создание"
        {
            entityManager.persist(company);
            entityManager.flush();
            return company.getCompId();
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Companies> getCompaniesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc) {
        String stringQuery;
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "1,2"))// Предприятия: "Меню - все предприятия","Меню - только своё предприятие"
        {
            Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           sso.abbreviation||' '||p.name as name, " +
                    "           u.username as owner, " +
                    "           us.username as creator, " +
                    "           uc.username as changer, " +
                    "           p.master_id as owner_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.addressfact as addressfact, " +
                    "           p.addressjur as addressjur, " +
                    "           p.namefull as namefull, " +
                    "           p.opf as opf_id, " +
                    "           (select name from sprav_sys_opf where id=p.opf) as opf_name, " +
                    "           p.inn as inn, " +
                    "           p.reg_num as reg_num, " +
                    "           p.who_got as who_got, " +
                    "           p.datereg as datereg, " +
                    "           p.rs as rs, " +
                    "           p.korschet as korschet, " +
                    "           p.bank as bank, " +
                    "           p.bik as bik, " +
                    "           p.currency_id as currency_id, " +
                    "           p.st_prefix_barcode_packed as st_prefix_barcode_packed, " +
                    "           p.st_prefix_barcode_pieced as st_prefix_barcode_pieced, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed " +
                    "           from companies p " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf=sso.id " +
                    "           where  p.master_id=" + companyOwnerId;

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "1")) {//если нет прав на "Меню - все предприятия"
                stringQuery = stringQuery + " and p.id=" + userRepositoryJPA.getMyCompanyId();
            }


            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }
            stringQuery = stringQuery + " order by p." + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, CompaniesJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);
            return query.getResultList();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public int getCompaniesSize(String searchString) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "1,2"))// Предприятия: "Меню - все предприятия","Меню - только своё предприятие"
        {
            String stringQuery;
            Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery = "from Companies p where p.master=" + companyOwnerId;
            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "1")) {//если нет прав на просмотр всех предприятий (но своего есть)
                stringQuery = stringQuery + " and p.id=" + userRepositoryJPA.getMyCompanyId();
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.compName) like upper('%" + searchString + "%')";
            }
            Query query = entityManager.createQuery(stringQuery, Companies.class);
            return query.getResultList().size();
        }else return 0;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCompaniesByNumber(ArrayList<Long> delNumbers) {
        if(
            securityRepositoryJPA.isItAllMyMastersCompanies(delNumbers) && //все предприятия принадлежат родительскому аккаунту
            securityRepositoryJPA.userHasPermissions_OR(3L,"4")) // Предприятия: "Удаление"
        {
            Query query = entityManager.createNamedQuery("Companies.deleteCompaniesByNumber");
            if (delNumbers != null) {
                query.setParameter("delNumbers", delNumbers);
                int cont = query.executeUpdate();
                return true;
            } else return false;
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Companies> getCompaniesList() {
            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            String stringQuery;
            Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           sso.abbreviation||' '||p.name as name " +
                    "           from companies p " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf=sso.id " +
                    "           where  p.master_id=" + companyOwnerId;

//            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "35")) {//если нет прав на Предприятие: "Выпадающие списки - видеть предприятия кроме своего"
//                stringQuery = stringQuery + " and p.id=" + myCompanyId;//то показывать только своё предприятие
//            }

            Query query = entityManager.createNativeQuery(stringQuery, SpravSysCompaniesList.class);
            return query.getResultList();
    }


}
