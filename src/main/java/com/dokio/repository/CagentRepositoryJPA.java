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

import com.dokio.message.request.CagentCategoriesForm;
import com.dokio.message.request.CagentsContactsForm;
import com.dokio.message.request.CagentsForm;
import com.dokio.message.request.CagentsPaymentAccountsForm;
import com.dokio.message.response.CagentCategoriesTableJSON;
import com.dokio.message.response.CagentsJSON;
import com.dokio.message.response.HistoryCagentBalanceObjectJSON;
import com.dokio.message.response.Reports.HistoryCagentBalanceJSON;
import com.dokio.message.response.Settings.UserSettingsJSON;
import com.dokio.message.response.Sprav.CagentsListJSON;
import com.dokio.model.CagentCategories;
import com.dokio.model.Cagents;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CagentRepositoryJPA {
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
    private CommonUtilites commonUtilites;

    Logger logger = Logger.getLogger("CagentRepositoryJPA");

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("p.name","name","company","status_name","creator","contacts","date_time_created_sort","description","cagent","summ_on_start","summ_in","summ_out","summ_on_end")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));


    @SuppressWarnings("Duplicates")
    public List<CagentsJSON> getCagentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int categoryId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
                throw new IllegalArgumentException("Invalid query parameters");

            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24';
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           sso.name as opf, "+
                    "           sso.id as opf_id, "+
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           p.description as description, " +
                    // Апдейт Контрагентов
                    "           p.code as code, " +
                    "           p.telephone as telephone, " +
                    "           p.site as site, " +
                    "           p.email as email, " +
                    "           p.zip_code as zip_code, " +
                    "           p.country_id as country_id, " +
                    "           p.region_id as region_id, " +
                    "           p.city_id as city_id, " +
                    "           p.street as street, " +
                    "           p.home as home, " +
                    "           p.flat as flat, " +
                    "           p.additional_address as additional_address, " +
                    "           p.status_id as status_id, " +
                    "           p.price_type_id as price_type_id, " +
                    "           p.discount_card as discount_card, " +
                    "           p.jr_jur_full_name as jr_jur_full_name, " +
                    "           p.jr_jur_kpp as jr_jur_kpp, " +
                    "           p.jr_jur_ogrn as jr_jur_ogrn, " +
                    "           p.jr_zip_code as jr_zip_code, " +
                    "           p.jr_country_id as jr_country_id, " +
                    "           p.jr_region_id as jr_region_id, " +
                    "           p.jr_city_id as jr_city_id, " +
                    "           p.jr_street as jr_street, " +
                    "           p.jr_home as jr_home, " +
                    "           p.jr_flat as jr_flat, " +
                    "           p.jr_additional_address as jr_additional_address, " +
                    "           p.jr_inn as jr_inn, " +
                    "           p.jr_okpo as jr_okpo, " +
                    "           p.jr_fio_family as jr_fio_family, " +
                    "           p.jr_fio_name as jr_fio_name, " +
                    "           p.jr_fio_otchestvo as jr_fio_otchestvo, " +
                    "           p.jr_ip_ogrnip as jr_ip_ogrnip, " +
                    "           p.jr_ip_svid_num as jr_ip_svid_num, " +
                    "           to_char(p.jr_ip_reg_date, 'DD.MM.YYYY') as jr_ip_reg_date, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           CONCAT(p.telephone,' ',p.email,' ',p.site) as contacts, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +

                    "           from cagents p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf_id=sso.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted +
                    (categoryId!=0?" and p.id in (select ccc.cagent_id from cagent_cagentcategories ccc where ccc.category_id="+categoryId+") ":"");

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (134)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                 stringQuery = stringQuery + " and (" +
                        " upper(p.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(replace(p.email, ' ', '')) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(stat.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " regexp_replace(coalesce(p.telephone,'0'), '\\D', '', 'g') like upper(CONCAT('%',coalesce(nullif(regexp_replace(:sg, '\\D', '', 'g'),''),'---'),'%'))" +
                        ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;

            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<CagentsJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    CagentsJSON doc=new CagentsJSON();

                    doc.setId(Long.parseLong(                           obj[0].toString()));
                    doc.setMaster((String)                              obj[1]);
                    doc.setName((String)                                obj[2]);
                    doc.setCreator((String)                             obj[3]);
                    doc.setChanger((String)                             obj[4]);
                    doc.setMaster_id(Long.parseLong(                    obj[5].toString()));
                    doc.setCreator_id(Long.parseLong(                   obj[6].toString()));
                    doc.setChanger_id(obj[7]!=null?Long.parseLong(      obj[7].toString()):null);
                    doc.setCompany_id(Long.parseLong(                   obj[8].toString()));
                    doc.setCompany((String)                             obj[9]);
                    doc.setOpf((String)                                 obj[10]);
                    doc.setOpf_id((Integer)                             obj[11]);
                    doc.setDate_time_created((String)                   obj[12]);
                    doc.setDate_time_changed((String)                   obj[13]);
                    doc.setDescription((String)                         obj[14]);
                    doc.setCode((String)                                obj[15]);
                    doc.setTelephone((String)                           obj[16]);
                    doc.setSite((String)                                obj[17]);
                    doc.setEmail((String)                               obj[18]);
                    doc.setZip_code((String)                            obj[19]);
                    doc.setCountry_id((Integer)                         obj[20]);
                    doc.setRegion_id((Integer)                          obj[21]);
                    doc.setCity_id((Integer)                            obj[22]);
                    doc.setStreet((String)                              obj[23]);
                    doc.setHome((String)                                obj[24]);
                    doc.setFlat((String)                                obj[25]);
                    doc.setAdditional_address((String)                  obj[26]);
                    doc.setStatus_id(obj[27]!=null?Long.parseLong(      obj[27].toString()):null);
                    doc.setPrice_type_id(obj[28]!=null?Long.parseLong(  obj[28].toString()):null);
                    doc.setDiscount_card((String)                       obj[29]);
                    doc.setJr_jur_full_name((String)                    obj[30]);
                    doc.setJr_jur_kpp((String)                          obj[31]);
                    doc.setJr_jur_ogrn((String)                         obj[32]);
                    doc.setJr_zip_code((String)                         obj[33]);
                    doc.setJr_country_id((Integer)                      obj[34]);
                    doc.setJr_region_id((Integer)                       obj[35]);
                    doc.setJr_city_id((Integer)                         obj[36]);
                    doc.setJr_street((String)                           obj[37]);
                    doc.setJr_home((String)                             obj[38]);
                    doc.setJr_flat((String)                             obj[39]);
                    doc.setJr_additional_address((String)               obj[40]);
                    doc.setJr_inn((String)                              obj[41]);
                    doc.setJr_okpo((String)                             obj[42]);
                    doc.setJr_fio_family((String)                       obj[43]);
                    doc.setJr_fio_name((String)                         obj[44]);
                    doc.setJr_fio_otchestvo((String)                    obj[45]);
                    doc.setJr_ip_ogrnip((String)                        obj[46]);
                    doc.setJr_ip_svid_num((String)                      obj[47]);
                    doc.setJr_ip_reg_date((String)                      obj[48]);
                    doc.setStatus_name((String)                         obj[49]);
                    doc.setStatus_color((String)                        obj[50]);
                    doc.setStatus_description((String)                  obj[51]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCagentsTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    public int getCagentsSize(String searchString, int companyId, int categoryId, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from cagents p " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted +
                    (categoryId!=0?" and p.id in (select ppg.cagent_id from cagent_cagentcategories ppg where ppg.category_id="+categoryId+") ":"");

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Меню - таблица - "Контрагенты" по всем предприятиям"
            {
                //остается только на своё предприятие (110)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        " upper(p.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(p.description) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(replace(p.email, ' ', '')) like upper(CONCAT('%',:sg,'%')) or "+
                        " upper(stat.name) like upper(CONCAT('%',:sg,'%')) or "+
                        " regexp_replace(coalesce(p.telephone,'0'), '\\D', '', 'g') like upper(CONCAT('%',coalesce(nullif(regexp_replace(:sg, '\\D', '', 'g'),''),'---'),'%'))" +
                        ")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                return query.getResultList().size();

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCagentsSize. SQL query:" + stringQuery, e);
                return 0;
            }
        } else return 0;
    }

    @SuppressWarnings("Duplicates")// отдаёт список банковских счетов контрагента
    public List<CagentsPaymentAccountsForm> getCagentsPaymentAccounts(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//(см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.id," +
                    " ap.master_id," +
                    " ap.company_id," +
                    " ap.output_order," +
                    " ap.bik," +
                    " ap.name," +
                    " ap.address," +
                    " ap.payment_account," +
                    " ap.corr_account," +
                    " ap.intermediatery as intermediatery, " +
                    " ap.swift as swift, " +
                    " ap.iban as iban " +
                    " from " +
                    " cagents_payment_accounts ap " +
                    " where ap.master_id = " + myMasterId +
                    " and ap.cagent_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (134)
                stringQuery = stringQuery + " and ap.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by ap.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<CagentsPaymentAccountsForm> returnList = new ArrayList<>();

            for(Object[] obj:queryList){
                CagentsPaymentAccountsForm doc=new CagentsPaymentAccountsForm();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setMaster_id(Long.parseLong(                        obj[1].toString()));
                doc.setCompany_id(Long.parseLong(                       obj[2].toString()));
                doc.setOutput_order((Integer)                           obj[3]);
                doc.setBik((String)                                     obj[4]);
                doc.setName((String)                                    obj[5]);
                doc.setAddress((String)                                 obj[6]);
                doc.setPayment_account((String)                         obj[7]);
                doc.setCorr_account((String)                            obj[8]);
                doc.setIntermediatery((String)                          obj[9]);
                doc.setSwift((String)                                   obj[10]);
                doc.setIban((String)                                    obj[11]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

    @SuppressWarnings("Duplicates")// отдаёт список контактных лиц контрагента
    public List<CagentsContactsForm> getCagentsContacts(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//(см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            stringQuery =   " select " +
                    " ap.id," +
                    " ap.master_id," +
                    " ap.company_id," +
                    " ap.output_order," +
                    " ap.fio," +
                    " ap.position," +
                    " ap.telephone," +
                    " ap.email," +
                    " ap.additional" +
                    " from " +
                    " cagents_contacts ap " +
                    " where ap.master_id = " + myMasterId +
                    " and ap.cagent_id = " + docId;

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (134)
                stringQuery = stringQuery + " and ap.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by ap.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<CagentsContactsForm> returnList = new ArrayList<>();

            for(Object[] obj:queryList){
                CagentsContactsForm doc=new CagentsContactsForm();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setMaster_id(Long.parseLong(                        obj[1].toString()));
                doc.setCompany_id(Long.parseLong(                       obj[2].toString()));
                doc.setOutput_order((Integer)                           obj[3]);
                doc.setFio((String)                                     obj[4]);
                doc.setPosition((String)                                obj[5]);
                doc.setTelephone((String)                               obj[6]);
                doc.setEmail((String)                                   obj[7]);
                doc.setAdditional((String)                              obj[8]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public CagentsJSON getCagentValues(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            UserSettingsJSON userSettings = userRepositoryJPA.getMySettings();
            String myTimeZone = userSettings.getTime_zone();
            String dateFormat = userSettings.getDateFormat();
            String timeFormat = (userSettings.getTimeFormat().equals("12")?" HH12:MI AM":" HH24:MI"); // '12' or '24';

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           sso.name as opf, "+
                    "           sso.id as opf_id, "+
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+timeFormat+"') as date_time_changed, " +
                    "           coalesce(p.description,'') as description, " +
                    // Апдейт Контрагентов
                    "           coalesce(p.code,'') as code, " +
                    "           coalesce(p.telephone,'') as telephone, " +
                    "           coalesce(p.site,'') as site, " +
                    "           coalesce(p.email,'') as email, " +
                    "           coalesce(p.zip_code,'') as zip_code, " +
                    "           p.country_id as country_id, " +
                    "           p.region_id as region_id, " +
                    "           p.city_id as city_id, " +
                    "           coalesce(p.street,'') as street, " +
                    "           coalesce(p.home,'') as home, " +
                    "           coalesce(p.flat,'') as flat, " +
                    "           coalesce(p.additional_address,'') as additional_address, " +
                    "           p.status_id as status_id, " +
                    "           p.price_type_id as price_type_id, " +
                    "           coalesce(p.discount_card,'') as discount_card, " +
                    "           coalesce(p.jr_jur_full_name,'') as jr_jur_full_name, " +
                    "           coalesce(p.jr_jur_kpp,'') as jr_jur_kpp, " +
                    "           coalesce(p.jr_jur_ogrn,'') as jr_jur_ogrn, " +
                    "           coalesce(p.jr_zip_code,'') as jr_zip_code, " +
                    "           p.jr_country_id as jr_country_id, " +
                    "           p.jr_region_id as jr_region_id, " +
                    "           p.jr_city_id as jr_city_id, " +
                    "           coalesce(p.jr_street,'') as jr_street, " +
                    "           coalesce(p.jr_home,'') as jr_home, " +
                    "           coalesce(p.jr_flat,'') as jr_flat, " +
                    "           coalesce(p.jr_additional_address,'') as jr_additional_address, " +
                    "           coalesce(p.jr_inn,'') as jr_inn, " +
                    "           coalesce(p.jr_okpo,'') as jr_okpo, " +
                    "           coalesce(p.jr_fio_family,'') as jr_fio_family, " +
                    "           coalesce(p.jr_fio_name,'') as jr_fio_name, " +
                    "           coalesce(p.jr_fio_otchestvo,'') as jr_fio_otchestvo, " +
                    "           coalesce(p.jr_ip_ogrnip,'') as jr_ip_ogrnip, " +
                    "           coalesce(p.jr_ip_svid_num,'') as jr_ip_svid_num, " +
                    "           to_char(p.jr_ip_reg_date, 'DD.MM.YYYY') as jr_ip_reg_date, " +
                    "           coalesce(stat.name,'') as status_name, " +
                    "           coalesce(stat.color,'') as status_color, " +
                    "           coalesce(stat.description,'') as status_description, " +
                    "           coalesce(ctr.name_ru,'') as country, " +
                    "           coalesce(jr_ctr.name_ru,'') as jr_country, " +
//                    "           reg.name_ru as region, " +
                    "           coalesce(p.region,'') as region, " +
//                    "           jr_reg.name_ru as jr_region, " +
                    "           coalesce(p.jr_region,'') as jr_region, " +
//                    "           cty.name_ru as city, " +
                    "           coalesce(p.city,'') as city, " +
//                    "           jr_cty.name_ru as jr_city, " +
                    "           coalesce(p.jr_city,'') as jr_city, " +
//                    "           coalesce(cty.area_ru,'') as area, " +
//                    "           coalesce(jr_cty.area_ru,'') as jr_area," +
                    "           '' as area, " +
                    "           '' as jr_area," +

                    "           p.type as type, " +// entity or individual
//                    "           p.reg_country_id as reg_country_id, " + // country of registration
//                    "           p.tax_number as tax_number, " + // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//                    "           p.reg_number as reg_number" + // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)

                    "           p.legal_form as legal_form, " +// legal form of individual (ie entrepreneur, ...)
                    "           p.vat as jr_vat" + // VAT identification number
                    "           from cagents p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf_id=sso.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           LEFT OUTER JOIN sprav_sys_countries ctr ON p.country_id=ctr.id" +
//                    "           LEFT OUTER JOIN sprav_sys_regions reg ON p.region_id=reg.id" +
//                    "           LEFT OUTER JOIN sprav_sys_cities cty ON p.city_id=cty.id" +
                    "           LEFT OUTER JOIN sprav_sys_countries jr_ctr ON p.jr_country_id=jr_ctr.id" +
//                    "           LEFT OUTER JOIN sprav_sys_regions jr_reg ON p.jr_region_id=jr_reg.id" +
//                    "           LEFT OUTER JOIN sprav_sys_cities jr_cty ON p.jr_city_id=jr_cty.id" +
                    "           where p.id= " + id +
                    "           and  p.master_id=" + myMasterId;

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (134)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            CagentsJSON doc = new CagentsJSON();

            doc.setId(Long.parseLong(                                       queryList.get(0)[0].toString()));
            doc.setMaster((String)                                          queryList.get(0)[1]);
            doc.setName((String)                                            queryList.get(0)[2]);
            doc.setCreator((String)                                         queryList.get(0)[3]);
            doc.setChanger((String)                                         queryList.get(0)[4]);
            doc.setMaster_id(Long.parseLong(                                queryList.get(0)[5].toString()));
            doc.setCreator_id(Long.parseLong(                               queryList.get(0)[6].toString()));
            doc.setChanger_id(queryList.get(0)[7]!=null?Long.parseLong(     queryList.get(0)[7].toString()):null);
            doc.setCompany_id(Long.parseLong(                               queryList.get(0)[8].toString()));
            doc.setCompany((String)                                         queryList.get(0)[9]);
            doc.setOpf((String)                                             queryList.get(0)[10]);
            doc.setOpf_id((Integer)                                         queryList.get(0)[11]);
            doc.setDate_time_created((String)                               queryList.get(0)[12]);
            doc.setDate_time_changed((String)                               queryList.get(0)[13]);
            doc.setDescription((String)                                     queryList.get(0)[14]);
            doc.setCode((String)                                            queryList.get(0)[15]);
            doc.setTelephone((String)                                       queryList.get(0)[16]);
            doc.setSite((String)                                            queryList.get(0)[17]);
            doc.setEmail((String)                                           queryList.get(0)[18]);
            doc.setZip_code((String)                                        queryList.get(0)[19]);
            doc.setCountry_id((Integer)                                     queryList.get(0)[20]);
            doc.setRegion_id((Integer)                                      queryList.get(0)[21]);
            doc.setCity_id((Integer)                                        queryList.get(0)[22]);
            doc.setStreet((String)                                          queryList.get(0)[23]);
            doc.setHome((String)                                            queryList.get(0)[24]);
            doc.setFlat((String)                                            queryList.get(0)[25]);
            doc.setAdditional_address((String)                              queryList.get(0)[26]);
            doc.setStatus_id(queryList.get(0)[27]!=null?Long.parseLong(     queryList.get(0)[27].toString()):null);
            doc.setPrice_type_id(queryList.get(0)[28]!=null?Long.parseLong( queryList.get(0)[28].toString()):null);
            doc.setDiscount_card((String)                                   queryList.get(0)[29]);
            doc.setJr_jur_full_name((String)                                queryList.get(0)[30]);
            doc.setJr_jur_kpp((String)                                      queryList.get(0)[31]);
            doc.setJr_jur_ogrn((String)                                     queryList.get(0)[32]);
            doc.setJr_zip_code((String)                                     queryList.get(0)[33]);
            doc.setJr_country_id((Integer)                                  queryList.get(0)[34]);
            doc.setJr_region_id((Integer)                                   queryList.get(0)[35]);
            doc.setJr_city_id((Integer)                                     queryList.get(0)[36]);
            doc.setJr_street((String)                                       queryList.get(0)[37]);
            doc.setJr_home((String)                                         queryList.get(0)[38]);
            doc.setJr_flat((String)                                         queryList.get(0)[39]);
            doc.setJr_additional_address((String)                           queryList.get(0)[40]);
            doc.setJr_inn((String)                                          queryList.get(0)[41]);
            doc.setJr_okpo((String)                                         queryList.get(0)[42]);
            doc.setJr_fio_family((String)                                   queryList.get(0)[43]);
            doc.setJr_fio_name((String)                                     queryList.get(0)[44]);
            doc.setJr_fio_otchestvo((String)                                queryList.get(0)[45]);
            doc.setJr_ip_ogrnip((String)                                    queryList.get(0)[46]);
            doc.setJr_ip_svid_num((String)                                  queryList.get(0)[47]);
            doc.setJr_ip_reg_date((String)                                  queryList.get(0)[48]);
            doc.setStatus_name((String)                                     queryList.get(0)[49]);
            doc.setStatus_color((String)                                    queryList.get(0)[50]);
            doc.setStatus_description((String)                              queryList.get(0)[51]);
            doc.setCountry((String)                                         queryList.get(0)[52]);
            doc.setJr_country((String)                                      queryList.get(0)[53]);
            doc.setRegion((String)                                          queryList.get(0)[54]);
            doc.setJr_region((String)                                       queryList.get(0)[55]);
            doc.setCity((String)                                            queryList.get(0)[56]);
            doc.setJr_city((String)                                         queryList.get(0)[57]);
            doc.setArea((String)                                            queryList.get(0)[58]);
            doc.setJr_area((String)                                         queryList.get(0)[59]);
            doc.setType(queryList.get(0)[60]!=null?                 (String)queryList.get(0)[60]:"");
            doc.setLegal_form((String)                                      queryList.get(0)[61]);
            doc.setJr_vat((String)                                          queryList.get(0)[62]);
//            doc.setReg_country_id((Integer)                                 queryList.get(0)[61]);
//            doc.setTax_number(queryList.get(0)[62]!=null?           (String)queryList.get(0)[62]:"");
//            doc.setReg_number(queryList.get(0)[63]!=null?           (String)queryList.get(0)[63]:"");
            //adding categories
            List<Integer> valuesListId =getCagentsCategoriesIdsByCagentId(Long.valueOf(id));
            doc.setCagent_categories_id(valuesListId);
            return doc;
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer updateCagents(CagentsForm request) {

        EntityManager emgr = emf.createEntityManager();
        Cagents document = emgr.find(Cagents.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(12L, "136"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(12L, "135"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany=(Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId=document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDoc =(DocumentMasterId.equals(myMasterId));
        if(((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                ||(!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            if(updateCagentBaseFields(request)){//Сначала сохраняем документ без контактных лиц и банковских счетов
                try
                {   //если сохранился...
                    //Сохраняем контактные лица
                    //удаление лишних контактных лиц(которые удалили в фронтэнде)
                    String ids = "";
                    //собираем id контактов, которые есть на сохранение, и удаляем из базы те, которых в этой сборке нет
                    if (request.getCagentsContactsTable()!=null && request.getCagentsContactsTable().size() > 0) {

                        for (CagentsContactsForm row : request.getCagentsContactsTable()) {
                            ids = ids + ((!ids.equals("")&&row.getId()!=null)?",":"") + (row.getId()==null?"":row.getId().toString());
                        }
                    }
                    ids=(!ids.equals("")?ids:"0");
                    if(deleteCagentContactsExcessRows(ids, request.getId())){
                        //если удаление прошло успешно...
                        for (CagentsContactsForm row : request.getCagentsContactsTable()) {
                            if(row.getId()!=null){//контакт содержит id, значит он есть в БД, и нужно его апдейтить
                                updateCagentContacts(row, myMasterId, request.getCompany_id(),request.getId());
                            }else{//контакт не содержит id, значит его нет в БД, и нужно его инсертить
                                insertCagentContacts(row, myMasterId, request.getCompany_id(),request.getId());
                            }
                        }
                    }

                    //удаление лишних банковских счетов (которые удалили в фронтэнде)
                    ids = "";
                    //собираем id банковских счетов, которые есть на сохранение, и удаляем из базы те, которых в этой сборке нет
                    if (request.getCagentsPaymentAccountsTable()!=null && request.getCagentsPaymentAccountsTable().size() > 0) {
                        for (CagentsPaymentAccountsForm row : request.getCagentsPaymentAccountsTable()) {
                            ids = ids + ((!ids.equals("")&&row.getId()!=null)?",":"") + (row.getId()==null?"":row.getId().toString());
                        }
                    }
                    ids=(!ids.equals("")?ids:"0");
                    if(deleteCagentPaymentAccountsExcessRows(ids, request.getId())){
                        //если удаление прошло успешно...
                        for (CagentsPaymentAccountsForm row : request.getCagentsPaymentAccountsTable()) {
                            if(row.getId()!=null){//счет содержит id, значит он есть в БД, и нужно его апдейтить
                                updateCagentPaymentAccounts(row, myMasterId, request.getCompany_id(),request.getId());
                            }else{//счет не содержит id, значит его нет в БД, и нужно его инсертить
                                insertCagentPaymentAccounts(row, myMasterId, request.getCompany_id(),request.getId());
                            }
                        }
                    }
                    deleteAllCagentCategories(request.getId());
                    Set<Long> categories = request.getSelectedCagentCategories();
                    if (categories!=null && categories.size()>0) { //если есть выбранные чекбоксы категорий
                        addSetOfCagentCategories(request.getId(),categories);
                    }

                    return 1;
                } catch (Exception e){
                    logger.error("Error of updateCagents", e);
                    e.printStackTrace();
                    return null;
                }
            } else return null;
        } else return -1;
    }

    @SuppressWarnings("Duplicates")
    //удаление лишних расчетных счетов (которые удалили в фронтэнде)
    private Boolean deleteCagentPaymentAccountsExcessRows(String accountsIds, Long cagent_id) {
        String stringQuery;
        try {
            stringQuery =   " delete from cagents_payment_accounts " +
                    " where cagent_id=" + cagent_id +
                    " and id not in (" + accountsIds + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of deleteCagentPaymentAccountsExcessRows", e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    //удаление лишних контактных лиц (которые удалили в фронтэнде)
    private Boolean deleteCagentContactsExcessRows(String contactsIds, Long cagent_id) {
        String stringQuery;
        try {
            stringQuery =   " delete from cagents_contacts " +
                    " where cagent_id=" + cagent_id +
                    " and id not in (" + contactsIds + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of deleteCagentContactsExcessRows", e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean insertCagentContacts(CagentsContactsForm row, Long master_id, Long company_id, Long cagent_id) {

        String stringQuery;
        try {
            stringQuery =   " insert into cagents_contacts (" +
                            "master_id," +
                            "company_id," +
                            "cagent_id," +
                            "fio," +
                            "position," +
                            "telephone," +
                            "email," +
                            "additional," +
                            "output_order"+
                            ") values ("
                            + master_id +", "
                            + company_id +", "
                            + cagent_id +", "
                            + ":fio, "
                            + ":posit, "
                            + ":telephone, "
                            + ":email, "
                            + ":additional, "
                            + ":output_order)";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("fio",(row.getFio()!=null?row.getFio():""));
            query.setParameter("posit",(row.getPosition()!=null?row.getPosition():""));
            query.setParameter("telephone",(row.getTelephone()!=null?row.getTelephone():""));
            query.setParameter("email",(row.getEmail()!=null?row.getEmail():""));
            query.setParameter("additional",(row.getAdditional()!=null?row.getAdditional():""));
            query.setParameter("output_order",row.getOutput_order());


            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of insertCagentContacts", e);
            e.printStackTrace();
            return false;
        }

    }

    @SuppressWarnings("Duplicates")
    private Boolean updateCagentContacts(CagentsContactsForm row, Long master_id, Long company_id, Long cagent_id) {

        String stringQuery;
        try {
            stringQuery =   " update cagents_contacts set " +
                    " fio = :fio," +
                    " position = :posit," +
                    " telephone = :telephone," +
                    " email = :email,"+
                    " additional = :additional," +
                    " output_order = :output_order" +
                    " where " +
                    " id="+row.getId()+" and "+
                    " master_id="+master_id+" and "+
                    " company_id="+company_id+" and "+
                    " cagent_id="+ cagent_id;

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("fio",row.getFio());
            query.setParameter("posit",row.getPosition());
            query.setParameter("telephone",row.getTelephone());
            query.setParameter("email",row.getEmail());
            query.setParameter("additional",(row.getAdditional()!=null?row.getAdditional():""));
            query.setParameter("output_order",row.getOutput_order());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of updateCagentContacts", e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean insertCagentPaymentAccounts(CagentsPaymentAccountsForm row, Long master_id, Long company_id, Long cagent_id) {

        String stringQuery;
        try {
            stringQuery =   " insert into cagents_payment_accounts (" +
                            "master_id," +
                            "company_id," +
                            "cagent_id," +
                            "bik," +
                            "name," +
                            "address," +
                            "corr_account," +
                            "payment_account," +
                            "intermediatery, " +
                            "swift, " +
                            "iban, " +
                            "output_order"+
                            ") values ("
                            + master_id +", "
                            + company_id +", "
                            + cagent_id +", "
                            + ":bik,"
                            + ":name,"
                            + ":address,"
                            + ":corr_acc,"
                            + ":paym_acc,"
                            + ":intermediatery, "
                            + ":swift, "
                            + ":iban, "
                            + row.getOutput_order() + ")";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("bik", (row.getBik()!=null?row.getBik():""));
            query.setParameter("name", (row.getName()!=null?row.getName():""));
            query.setParameter("address",(row.getAddress()!=null?row.getAddress():""));
            query.setParameter("corr_acc",(row.getCorr_account()!=null?row.getCorr_account():""));
            query.setParameter("paym_acc",(row.getPayment_account()!=null?row.getPayment_account():""));
            query.setParameter("intermediatery", (row.getIntermediatery()!=null?row.getIntermediatery():""));
            query.setParameter("swift", (row.getSwift()!=null?row.getSwift():""));
            query.setParameter("iban", (row.getIban()!=null?row.getIban():""));


            query.executeUpdate();

            return true;
        }
        catch (Exception e) {
            logger.error("Error of insertCagentPaymentAccounts", e);
            e.printStackTrace();
            return false;
        }

    }

    @SuppressWarnings("Duplicates")
    private Boolean updateCagentPaymentAccounts(CagentsPaymentAccountsForm row, Long master_id, Long company_id, Long cagent_id) {

        String stringQuery;
        try {
            stringQuery =   " update cagents_payment_accounts set " +
                    " bik = :bik, " +
                    " name = :name, " +
                    " address = :address, " +
                    " corr_account = :corr_acc, " +
                    " payment_account = :paym_acc, " +
                    " output_order = :output_order,"+
                    " intermediatery = :intermediatery, " +
                    " swift = :swift, " +
                    " iban = :iban" +
                    " where " +
                    " id="+row.getId()+" and "+
                    " master_id="+master_id+" and "+
                    " company_id="+company_id+" and "+
                    " cagent_id="+ cagent_id;

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("bik", (row.getBik()!=null?row.getBik():""));
            query.setParameter("name", (row.getName()!=null?row.getName():""));
            query.setParameter("address",(row.getAddress()!=null?row.getAddress():""));
            query.setParameter("corr_acc",(row.getCorr_account()!=null?row.getCorr_account():""));
            query.setParameter("paym_acc",(row.getPayment_account()!=null?row.getPayment_account():""));
            query.setParameter("output_order",row.getOutput_order());
            query.setParameter("intermediatery", (row.getIntermediatery()!=null?row.getIntermediatery():""));
            query.setParameter("swift", (row.getSwift()!=null?row.getSwift():""));
            query.setParameter("iban", (row.getIban()!=null?row.getIban():""));
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of updateCagentPaymentAccounts", e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private Boolean updateCagentBaseFields(CagentsForm request){//Апдейт документа без контактных лиц и банковских счетов
        EntityManager emgr = emf.createEntityManager();
        Long myId = userRepositoryJPA.getMyId();
//        Long myMasterId = userRepositoryJPA.getMyMasterId();
        try
        {
            String stringQuery;
            stringQuery =   " update cagents set " +
                    " name = :name, " +//наименование
                    " description = :description, " +//описание
                    " opf_id = " + request.getOpf_id() + ", " +//организационно-правовая форма предприятия
                    " changer_id = " + myId + ", " +// кто изменил
                    " date_time_changed = now() " + ", " +//дату изменения
                    " code = :code, " +//код
                    " telephone = :telephone, " +//телефон
                    " site = :site, " +//факс
                    " email = :email, " +//емейл
                    //фактический адрес
                    " zip_code = :zip_code, " +// почтовый индекс
                    " country_id = " + request.getCountry_id() + ", " +//страна
//                    " region_id = " + request.getRegion_id() + ", " +//область
//                    " city_id = " + request.getCity_id() + ", " +//город/нас.пункт
                    " region =  :region, " +//область
                    " city = :city, " +//город/нас.пункт
                    " street = :street, " +//улица
                    " home = :home, " +//дом
                    " flat = :flat, " +//квартира
                    " additional_address = :additional_address, " +//дополнение к адресу
                    " status_id = " + request.getStatus_id() + ", " +//статус контрагента
                    " price_type_id = " + request.getPrice_type_id() + ", " +//тип цен, назначенный для контрагента
                    " discount_card = :discount_card, " +//номер дисконтной карты
                    //Юридические реквизиты
                    " jr_jur_full_name = :jr_jur_full_name, " +//полное название (для юрлиц)
                    " jr_jur_kpp = :jr_jur_kpp, " +//кпп (для юрлиц)
                    " jr_jur_ogrn = :jr_jur_ogrn, " +//огрн (для юрлиц)
                    //юридический адрес (для юрлиц) /адрес регистрации (для ип и физлиц)
                    " jr_zip_code = :jr_zip_code, " +// почтовый индекс
                    " jr_country_id = " + request.getJr_country_id() + ", " +//страна
//                    " jr_region_id = " + request.getJr_region_id() + ", " +//область
//                    " jr_city_id = " + request.getJr_city_id() + ", " +//город/нас.пункт
                    " jr_region = :jr_region, " +//область
                    " jr_city = :jr_city, " +//город/нас.пункт
                    " jr_street = :jr_street, " +//улица
                    " jr_home = :jr_home, " +//дом
                    " jr_flat = :jr_flat, " +//квартира
                    " jr_additional_address = :jr_additional_address, " +//дополнение к адресу
                    " jr_inn = :jr_inn, " +//ИНН
                    " vat = :jr_vat, " +//VAT
                    " jr_okpo = :jr_okpo, " +//ОКПО
                    " jr_fio_family = :jr_fio_family, " +//Фамилия (для ИП или физлица)
                    " jr_fio_name = :jr_fio_name, " +//Имя (для ИП или физлица)
                    " jr_fio_otchestvo = :jr_fio_otchestvo, " +//Отчество (для ИП или физлица)
                    " jr_ip_ogrnip = :jr_ip_ogrnip, " +//ОГРНИП (для ИП)
                    " jr_ip_svid_num = :jr_ip_svid_num, " +//номер свидетельства (для ИП)
                    " jr_ip_reg_date = to_date(cast(:jr_ip_reg_date as TEXT),'DD.MM.YYYY')," +

                    " type = :type, " +// entity or individual
                    " legal_form = :legal_form"+
//                    " reg_country_id = " + request.getReg_country_id() + "," + // country of registration
//                    " tax_number =      :tax_number, " + // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//                    " reg_number =      :reg_number" + // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)
                    " where " +
                    " id = " + request.getId();
//                            " and master_id = " + myMasterId;// на Master_id = MyMasterId провеврять не надо, т.к. уже проверено в вызывающем методе
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("type",request.getType());
            query.setParameter("name", (request.getName()!=null?request.getName():""));
            query.setParameter("description", (request.getDescription() == null ? "": request.getDescription()));
            query.setParameter("code",(request.getCode() == null ? "": request.getCode()));
            query.setParameter("telephone",(request.getTelephone() == null ? "": request.getTelephone()));
            query.setParameter("site",(request.getSite() == null ? "": request.getSite()));
            query.setParameter("email",(request.getEmail() == null ? "": request.getEmail()));
            query.setParameter("zip_code",(request.getZip_code() == null ? "": request.getZip_code()));
            query.setParameter("region",(request.getRegion() == null ? "": request.getRegion()));
            query.setParameter("city",(request.getCity() == null ? "": request.getCity()));
            query.setParameter("street",(request.getStreet() == null ? "": request.getStreet()));
            query.setParameter("home",(request.getHome() == null ? "": request.getHome()));
            query.setParameter("flat",(request.getFlat() == null ? "": request.getFlat()));
            query.setParameter("additional_address",(request.getAdditional_address() == null ? "": request.getAdditional_address()));
            query.setParameter("discount_card",(request.getDiscount_card() == null ? "": request.getDiscount_card()));
            query.setParameter("jr_jur_full_name",(request.getJr_jur_full_name() == null ? "": request.getJr_jur_full_name()));
            query.setParameter("jr_jur_kpp",(request.getJr_jur_kpp() == null ? "": request.getJr_jur_kpp()));
            query.setParameter("jr_jur_ogrn",(request.getJr_jur_ogrn() == null ? "": request.getJr_jur_ogrn()));
            query.setParameter("jr_zip_code",(request.getJr_zip_code() == null ? "": request.getJr_zip_code()));
            query.setParameter("jr_region",(request.getJr_region() == null ? "": request.getJr_region()));
            query.setParameter("jr_city",(request.getJr_city() == null ? "": request.getJr_city()));
            query.setParameter("jr_street",(request.getJr_street() == null ? "": request.getJr_street()));
            query.setParameter("jr_home",(request.getJr_home() == null ? "": request.getJr_home()));
            query.setParameter("jr_flat",(request.getJr_flat() == null ? "": request.getJr_flat()));
            query.setParameter("jr_additional_address",(request.getJr_additional_address() == null ? "": request.getJr_additional_address()));
            query.setParameter("jr_inn",(request.getJr_inn() == null ? "": request.getJr_inn()));
            query.setParameter("jr_okpo",(request.getJr_okpo() == null ? "": request.getJr_okpo()));
            query.setParameter("jr_fio_family",(request.getJr_fio_family() == null ? "": request.getJr_fio_family()));
            query.setParameter("jr_fio_name",(request.getJr_fio_name() == null ? "": request.getJr_fio_name()));
            query.setParameter("jr_fio_otchestvo",(request.getJr_fio_otchestvo() == null ? "": request.getJr_fio_otchestvo()));
            query.setParameter("jr_ip_ogrnip",(request.getJr_ip_ogrnip() == null ? "": request.getJr_ip_ogrnip()));
            query.setParameter("jr_ip_svid_num",(request.getJr_ip_svid_num() == null ? "": request.getJr_ip_svid_num()));
            query.setParameter("jr_ip_reg_date",(request.getJr_ip_reg_date()!=null && !request.getJr_ip_reg_date().isEmpty()) ? (request.getJr_ip_reg_date()) : null);
            query.setParameter("legal_form",(request.getLegal_form()!=null?request.getLegal_form():""));
            query.setParameter("jr_vat", (request.getJr_vat() == null ? "": request.getJr_vat()));
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Error of updateCagentBaseFields", e);
            e.printStackTrace();
            return false;
        }

    }

    @SuppressWarnings("Duplicates")
    @Transactional(propagation= Propagation.REQUIRED)// класс может вызываться из другой транзакции, и в этом случае он должен выполняться внутри родительской транзакции (вообще это дефолтная установка, просто в данном случае это важно)
    public Long insertCagent(CagentsForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L,"129,130"))//  Контрагенты : "Создание"
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId(); //владелец предприятия создаваемого документа.
            //plan limit check
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if(userRepositoryJPA.getMyConsumedResources().getCounterparties()>=userRepositoryJPA.getMyMaxAllowedResources().getCounterparties())
                    return -120L; // number of companies is out of bounds of tariff plan

            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие для создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long createdCagentId = null;
            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(12L, "129") &&
                    Long.valueOf(myCompanyId) != request.getCompany_id()) || !DocumentMasterId.equals(myMasterId) )
            {
                return -1L;
            }
            else
            {
                try
                {
                    createdCagentId = insertCagentBaseFields(request,myMasterId);
                    if(createdCagentId!=null){//Сначала создаём документ без контактных лиц и банковских счетов
                        try {//если создался...
                            //Сохраняем контактные лица (если есть)
                            if (request.getCagentsContactsTable() != null && request.getCagentsContactsTable().size()>0) {
                                for (CagentsContactsForm row : request.getCagentsContactsTable()) {
                                    insertCagentContacts(row, myMasterId, request.getCompany_id(),createdCagentId);
                                }
                            }//Сохраняем банковские реквизиты (если есть)
                            if(request.getCagentsPaymentAccountsTable()!=null &&  request.getCagentsPaymentAccountsTable().size()>0) {
                                for (CagentsPaymentAccountsForm row : request.getCagentsPaymentAccountsTable()) {
                                    insertCagentPaymentAccounts(row, myMasterId, request.getCompany_id(), createdCagentId);
                                }
                            }
                            Set<Long> categories = request.getSelectedCagentCategories();
                            if (categories!=null && categories.size()>0) { //если есть выбранные чекбоксы категорий
                                addSetOfCagentCategories(createdCagentId,categories);
                            }
                            return createdCagentId;
                        } catch (Exception e){
                            logger.error("Error of insertCagent", e);
                            e.printStackTrace();
                            return null;
                        }
                    } else return null;
                } catch (Exception e) {
                    logger.error("Error of insertCagent", e);
                    e.printStackTrace();
                    return null;
                }
            }
        } else return -1L;
    }

    @SuppressWarnings("Duplicates")
    public Long insertCagentBaseFields(CagentsForm request,Long myMasterId){
        String stringQuery;
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        Long myId = userRepository.getUserId();
        // if Counterparty is creating from the online store, then there is no logged in user, and master user will be in a creator role
        if(Objects.isNull(myId)) myId = myMasterId;
        Long newDocId;
        stringQuery =   "insert into cagents (" +
                " master_id," + //мастер-аккаунт
                " creator_id," + //создатель
                " company_id," + //предприятие, для которого создается документ
                " date_time_created," + //дата и время создания
                " name," + //наименование
                " description," +//описание
                " opf_id,"+//организационно-правовая форма предприятия
                " code,"+//код
                " telephone,"+//телефон
                " site,"+//факс
                " email,"+//емейл
                //фактический адрес
                " zip_code,"+// почтовый индекс
                " country_id,"+//страна
//                " region_id,"+//область
//                " city_id,"+//город/нас.пункт
                " region,"+//область
                " city,"+//город/нас.пункт
                " street,"+//улица
                " home,"+//дом
                " flat,"+//квартира
                " additional_address,"+//дополнение к адресу
                " status_id,"+//статус контрагента
                " price_type_id,"+//тип цен, назначенный для контрагента
                " discount_card,"+//номер дисконтной карты
                //Юридические реквизиты
                " jr_jur_full_name,"+//полное название (для юрлиц)
                " jr_jur_kpp,"+//кпп (для юрлиц)
                " jr_jur_ogrn,"+//огрн (для юрлиц)
                //юридический адрес (для юрлиц) /адрес регистрации (для ип и физлиц)
                " jr_zip_code,"+// почтовый индекс
                " jr_country_id,"+//страна
//                " jr_region_id,"+//область
//                " jr_city_id,"+//город/нас.пункт
                " jr_region,"+//область
                " jr_city,"+//город/нас.пункт
                " jr_street,"+//улица
                " jr_home,"+//дом
                " jr_flat,"+//квартира
                " jr_additional_address,"+//дополнение к адресу
                " jr_inn,"+//ИНН
                " jr_okpo,"+//ОКПО
                " jr_fio_family,"+//Фамилия
                " jr_fio_name,"+//Имя
                " jr_fio_otchestvo,"+//Отчество
                " jr_ip_ogrnip,"+//ОГРНИП (для ИП)
                " jr_ip_svid_num,"+//номер свидетельства (для ИП)
                " jr_ip_reg_date," + //дата регистрации (для ИП)
                " type, " +// entity or individual
                " vat, "+ // VAT number
                " legal_form"+
//                " reg_country_id, " + // country of registration
//                " tax_number, " + // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//                " reg_number" + // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)
                ") values (" +
                myMasterId + ", "+
                myId + ", "+
                request.getCompany_id() + ", "+
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                ":name, " +//наименование
                ":description, " +//описание
                request.getOpf_id() + ", " +//организационно-правовая форма предприятия
                ":code, " +//код
                ":telephone, " +//телефон
                ":site, " +//факс
                ":email, " +//емейл
                //фактический адрес
                ":zip_code, " +//почтовый индекс
                request.getCountry_id() + ", " +//страна
//                request.getRegion_id() + ", " +//область
//                request.getCity_id() + ", " +//город/нас.пункт
                ":region, " +//область
                ":city, " +//город/нас.пункт
                ":street, " +//улица
                ":home, " +//дом
                ":flat, " +//квартира
                ":additional_address, " +//дополнение к адресу
                request.getStatus_id() + ", " +//статус контрагента
                request.getPrice_type_id() + ", " +//тип цен, назначенный для контрагента
                ":discount_card, " +//номер дисконтной карты
                //Юридические реквизиты
                ":jr_jur_full_name, " +//полное название (для юрлиц)
                ":jr_jur_kpp, " +//кпп (для юрлиц)
                ":jr_jur_ogrn, " +//огрн (для юрлиц)
                //юридический адрес (для юрлиц) /адрес регистрации (для ип и физлиц)
                ":jr_zip_code, " +//почтовый индекс
                request.getJr_country_id() + ", " +//страна
//                request.getJr_region_id() + ", " +//область
//                request.getJr_city_id() + ", " +//город/нас.пункт
                ":jr_region, " +//область
                ":jr_city, " +//город/нас.пункт
                ":jr_street, " +//улица
                ":jr_home, " +//дом
                ":jr_flat, " +//квартира
                ":jr_additional_address, " +//дополнение к адресу
                ":jr_inn, " +//ИНН
                ":jr_okpo, " + //ОКПО
                ":jr_fio_family, " +//Фамилия
                ":jr_fio_name, " +//Имя
                ":jr_fio_otchestvo, " +//Отчество
                ":jr_ip_ogrnip, " + //ОГРНИП (для ИП)
                ":jr_ip_svid_num, " +//номер свидетельства (для ИП)
                "to_date(cast(:jr_ip_reg_date as TEXT),'DD.MM.YYYY')," +
                ":type, " +
                ":jr_vat, " +
                ":legal_form"+
//                request.getReg_country_id() + "," +
//                ":tax_number," +
//                ":reg_number" +
                ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("type",request.getType());
            query.setParameter("name", (request.getName()!=null?request.getName():""));
            query.setParameter("description", (request.getDescription() == null ? "": request.getDescription()));
            query.setParameter("code",(request.getCode() == null ? "": request.getCode()));
            query.setParameter("telephone",(request.getTelephone() == null ? "": request.getTelephone()));
            query.setParameter("site",(request.getSite() == null ? "": request.getSite()));
            query.setParameter("email",(request.getEmail() == null ? "": request.getEmail()));
            query.setParameter("zip_code",(request.getZip_code() == null ? "": request.getZip_code()));
            query.setParameter("region",(request.getRegion() == null ? "": request.getRegion()));
            query.setParameter("city",(request.getCity() == null ? "": request.getCity()));
            query.setParameter("street",(request.getStreet() == null ? "": request.getStreet()));
            query.setParameter("home",(request.getHome() == null ? "": request.getHome()));
            query.setParameter("flat",(request.getFlat() == null ? "": request.getFlat()));
            query.setParameter("additional_address",(request.getAdditional_address() == null ? "": request.getAdditional_address()));
            query.setParameter("discount_card",(request.getDiscount_card() == null ? "": request.getDiscount_card()));
            query.setParameter("jr_jur_full_name",(request.getJr_jur_full_name() == null ? "": request.getJr_jur_full_name()));
            query.setParameter("jr_jur_kpp",(request.getJr_jur_kpp() == null ? "": request.getJr_jur_kpp()));
            query.setParameter("jr_jur_ogrn",(request.getJr_jur_ogrn() == null ? "": request.getJr_jur_ogrn()));
            query.setParameter("jr_zip_code",(request.getJr_zip_code() == null ? "": request.getJr_zip_code()));
            query.setParameter("jr_region",(request.getJr_region() == null ? "": request.getJr_region()));
            query.setParameter("jr_city",(request.getJr_city() == null ? "": request.getJr_city()));
            query.setParameter("jr_street",(request.getJr_street() == null ? "": request.getJr_street()));
            query.setParameter("jr_home",(request.getJr_home() == null ? "": request.getJr_home()));
            query.setParameter("jr_flat",(request.getJr_flat() == null ? "": request.getJr_flat()));
            query.setParameter("jr_additional_address",(request.getJr_additional_address() == null ? "": request.getJr_additional_address()));
            query.setParameter("jr_inn",(request.getJr_inn() == null ? "": request.getJr_inn()));
            query.setParameter("jr_okpo",(request.getJr_okpo() == null ? "": request.getJr_okpo()));
            query.setParameter("jr_fio_family",(request.getJr_fio_family() == null ? "": request.getJr_fio_family()));
            query.setParameter("jr_fio_name",(request.getJr_fio_name() == null ? "": request.getJr_fio_name()));
            query.setParameter("jr_fio_otchestvo",(request.getJr_fio_otchestvo() == null ? "": request.getJr_fio_otchestvo()));
            query.setParameter("jr_ip_ogrnip",(request.getJr_ip_ogrnip() == null ? "": request.getJr_ip_ogrnip()));
            query.setParameter("jr_ip_svid_num",(request.getJr_ip_svid_num() == null ? "": request.getJr_ip_svid_num()));
            query.setParameter("jr_ip_reg_date",(request.getJr_ip_reg_date()!=null && !request.getJr_ip_reg_date().isEmpty()) ? (request.getJr_ip_reg_date()) : null);
            query.setParameter("legal_form",(request.getLegal_form()!=null?request.getLegal_form():""));
            query.setParameter("jr_vat", (request.getJr_vat() == null ? "": request.getJr_vat()));

            query.executeUpdate();
            stringQuery="select id from cagents where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            newDocId=Long.valueOf(query2.getSingleResult().toString());
            return newDocId;
        } catch (Exception e) {
            logger.error("Error of insertCagentBaseFields", e);
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer deleteCagents(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(12L,"131") && securityRepositoryJPA.isItAllMyMastersDocuments("cagents",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(12L,"132") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("cagents",delNumbers)))
        {
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update cagents p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " +
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")";

            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method deleteCagents. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteCagents(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(12L,"131") && securityRepositoryJPA.isItAllMyMastersDocuments("cagents",delNumbers)) ||
        //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого восстанавливают) и предприятию аккаунта
        (securityRepositoryJPA.userHasPermissions_OR(12L,"132") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("cagents",delNumbers)))
        {
            //plan limit check
            Long masterId =  userRepositoryJPA.getMyMasterId();
            long amountToRepair = delNumbers.split(",").length;
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(masterId))) // if plan with limits - checking limits
                if((userRepositoryJPA.getMyConsumedResources().getCounterparties()+amountToRepair)>userRepositoryJPA.getMyMaxAllowedResources().getCounterparties())
                    return -120; // number of users is out of bounds of tariff plan
            // на MasterId не проверяю , т.к. выше уже проверено
            Long myId = userRepositoryJPA.getMyId();
            String stringQuery;
            stringQuery = "Update cagents p" +
                    " set changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " +
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                    query.executeUpdate();
                    return 1;
                } else return null;
            }catch (Exception e) {
                logger.error("Exception in method undeleteCagents. SQL query:"+stringQuery, e);
                e.printStackTrace();
                return null;
            }
        } else return -1;
    }
    //отдает список наименований контрагентов по поисковой подстроке и предприятию
    // тут не надо прописывать права, т.к. это сервисный запрос
    public List getCagentsList(String searchString, int companyId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery = "select  p.id as id, " +
                "           p.name as name "+
                "           from cagents p " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_deleted,false) !=true ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " upper(p.name) like upper(CONCAT('%',:searchString,'%')) or "+
                    " upper(p.description) like upper(CONCAT('%',:searchString,'%')) ";
            stringQuery = stringQuery + ")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id order by p.name asc";
        Query query = entityManager.createNativeQuery(stringQuery);
        if (searchString != null && !searchString.isEmpty())
        {query.setParameter("searchString", searchString);}
        List<Object[]> queryList = query.getResultList();
        List<CagentsListJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList){
            CagentsListJSON doc=new CagentsListJSON();
            doc.setId(Long.parseLong(obj[0].toString()));
            doc.setName((String) obj[1]);
            returnList.add(doc);
        }
        return returnList;
    }

@SuppressWarnings("Duplicates")
public List<HistoryCagentBalanceJSON> getMutualpaymentTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, String dateFrom, String dateTo) {
    String stringQuery;
    String myTimeZone = userRepository.getUserTimeZone();
    Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
    if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
        throw new IllegalArgumentException("Invalid query parameters");

    stringQuery =   "select " +
            " cg.id as cagent_id, " +
            " cg.name as cagent, " +
            " coalesce((select SUM(    p4.summ_in-p4.summ_out)  from history_cagent_summ p4 where p4.master_id="+myMasterId+" and p4.company_id="+companyId+" and p4.is_completed=true and p4.object_id = cg.id and p4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_on_start, " +
            " coalesce((select SUM(    p4.summ_in)              from history_cagent_summ p4 where p4.master_id="+myMasterId+" and p4.company_id="+companyId+" and p4.is_completed=true and p4.object_id = cg.id and p4.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and p4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo  ||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_in, " +
            " coalesce((select SUM(    p5.summ_out)             from history_cagent_summ p5 where p5.master_id="+myMasterId+" and p5.company_id="+companyId+" and p5.is_completed=true and p5.object_id = cg.id and p5.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and p5.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo  ||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_out, " +
            " coalesce((select SUM(    p4.summ_in-p4.summ_out)  from history_cagent_summ p4 where p4.master_id="+myMasterId+" and p4.company_id="+companyId+" and p4.is_completed=true and p4.object_id = cg.id and p4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo  ||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_on_end " +
            " from history_cagent_summ p " +
            " INNER JOIN cagents cg ON p.object_id=cg.id "+
            " where p.master_id="+myMasterId+" and p.company_id="+companyId;

    if (searchString != null && !searchString.isEmpty()) {
        stringQuery = stringQuery + " and (" + " upper(cg.name)  like upper(CONCAT('%',:sg,'%'))"+")";
    }

    stringQuery = stringQuery + " group by cagent,cagent_id ";
    stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;

    try{
        Query query = entityManager.createNativeQuery(stringQuery);
        if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);

        query.setFirstResult(offsetreal).setMaxResults(result);

        List<Object[]> queryList = query.getResultList();
        List<HistoryCagentBalanceJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList){
            HistoryCagentBalanceJSON doc=new HistoryCagentBalanceJSON();
            doc.setCagent((String)                                      obj[1]);
            doc.setSumm_on_start((BigDecimal)                           obj[2]);
            doc.setSumm_in((BigDecimal)                                 obj[3]);
            doc.setSumm_out((BigDecimal)                                obj[4]);
            doc.setSumm_on_end((BigDecimal)                             obj[5]);
            doc.setCagent_id(Long.parseLong(                            obj[0].toString()));
            returnList.add(doc);
        }
        return returnList;
    } catch (Exception e) {
        e.printStackTrace();
        logger.error("Exception in method getMutualpaymentTable. SQL query:" + stringQuery, e);
        return null;
    }
}

    @SuppressWarnings("Duplicates")
    public Integer getMutualpaymentSize(String searchString, Long companyId, Set<Integer> filterOptionsIds) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery =   "select " +
                " cg.id as cagent_id, " +
                " cg.name as cagent " +
                " from history_cagent_summ p " +
                " INNER JOIN cagents cg ON p.object_id=cg.id " +
                " where p.master_id="+myMasterId+" and p.company_id="+companyId;
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" + " upper(cg.name)  like upper(CONCAT('%',:sg,'%'))"+")";
        }

        stringQuery = stringQuery + " group by cagent,cagent_id ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            return query.getResultList().size();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMutualpaymentSize. SQL query:" + stringQuery, e);
            return null;
        }
    }

    //возвращает детализированный отчет по взаиморасчетам с выбранным контрагентом за период
    @SuppressWarnings("Duplicates")
    public HistoryCagentBalanceObjectJSON getMutualpaymentDetailedTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Long companyId, Long cagentId, String dateFrom, String dateTo) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        String suffix = userRepositoryJPA.getMySuffix();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
            throw new IllegalArgumentException("Invalid query parameters");
        String dateFormat=userRepositoryJPA.getMyDateFormat();

        stringQuery =   " select " +
                        " d.doc_name_"+suffix+" as doc_name, " +
                        " p.doc_number as doc_number, " +
                        " to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                        " p.summ_in as summ_in, " +
                        " p.summ_out as summ_out, " +
                        " st.name as status, " +
                        " p.doc_page_name as doc_page_name, " +
                        " p.doc_id as doc_id, " +
                        " p.date_time_created as date_time_created_sort " +
                        " from history_cagent_summ p " +
                        " INNER JOIN documents d ON p.doc_table_name=d.table_name " +
                        " LEFT OUTER JOIN sprav_status_dock st ON p.doc_status_id=st.id " +
                        " where " +
                        "     p.master_id="+myMasterId+
                        " and p.company_id="+companyId+
                        " and p.object_id="+cagentId+
                        " and p.is_completed=true"+
                        " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
                        " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" + " upper(d.doc_name_"+suffix+")  like upper(CONCAT('%',:sg,'%'))"+")";
        }

        stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            query.setParameter("dateFrom", dateFrom);
            query.setParameter("dateTo", dateTo);

            query.setFirstResult(offsetreal).setMaxResults(result);

            List<Object[]> queryList = query.getResultList();
            List<HistoryCagentBalanceJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                HistoryCagentBalanceJSON doc=new HistoryCagentBalanceJSON();
                doc.setDoc_name((String)                                    obj[0]);
                doc.setDoc_number((String)                                  obj[1]);
                doc.setDate_time_created((String)                           obj[2]);
                doc.setSumm_in((BigDecimal)                                 obj[3]);
                doc.setSumm_out((BigDecimal)                                obj[4]);
                doc.setStatus((String)                                      obj[5]);
                doc.setDoc_page_name((String)                               obj[6]);
                doc.setDoc_id(Long.parseLong(                               obj[7].toString()));
                returnList.add(doc);
            }
            HistoryCagentBalanceObjectJSON returnObject = new HistoryCagentBalanceObjectJSON();
            returnObject.setTable(returnList);
            List<BigDecimal> indicators = getCagentMutualpaymentIndicators(cagentId, companyId, dateFrom, dateTo);
            returnObject.setSumm_on_start(indicators.get(0));
            returnObject.setSumm_in(indicators.get(1));
            returnObject.setSumm_out(indicators.get(2));
            returnObject.setSumm_on_end(indicators.get(3));
            return returnObject;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMutualpaymentDetailedTable. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public Integer getMutualpaymentDetailedSize(String searchString, Long companyId, Long cagentId, Set<Integer> filterOptionsIds, String dateFrom, String dateTo) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String myTimeZone = userRepository.getUserTimeZone();
        stringQuery =   " select " +
                " p.id " +
                " from history_cagent_summ p " +
                " INNER JOIN documents d ON p.doc_table_name=d.table_name " +
                " LEFT OUTER JOIN sprav_status_dock st ON p.doc_status_id=st.id " +
                " where " +
                "     p.master_id="+myMasterId+
                " and p.company_id="+companyId+
                " and p.object_id="+cagentId+
                " and p.is_completed=true"+
                " and p.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') " +
                " and p.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" + " upper(d.doc_name_ru)  like upper(CONCAT('%',:sg,'%'))"+")";
        }

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            if (searchString != null && !searchString.isEmpty())
            {query.setParameter("sg", searchString);}
            query.setParameter("dateFrom", dateFrom);
            query.setParameter("dateTo", dateTo);
            return query.getResultList().size();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getMutualpaymentDetailedSize. SQL query:" + stringQuery, e);
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public List<BigDecimal> getCagentMutualpaymentIndicators(Long cagentId, Long companyId, String dateFrom, String dateTo) {
        String stringQuery;
        String myTimeZone = userRepository.getUserTimeZone();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery =
                " select " +
                " coalesce((select SUM(    p4.summ_in-p4.summ_out)  from history_cagent_summ p4 where p4.master_id="+myMasterId+" and p4.company_id="+companyId+" and object_id="+cagentId+" and p4.is_completed=true and p4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_on_start, " +
                " coalesce((select SUM(    p4.summ_in)              from history_cagent_summ p4 where p4.master_id="+myMasterId+" and p4.company_id="+companyId+" and object_id="+cagentId+" and p4.is_completed=true and p4.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and p4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo  ||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_in, " +
                " coalesce((select SUM(    p5.summ_out)             from history_cagent_summ p5 where p5.master_id="+myMasterId+" and p5.company_id="+companyId+" and object_id="+cagentId+" and p5.is_completed=true and p5.date_time_created at time zone '"+myTimeZone+"' >= to_timestamp(:dateFrom||' 00:00:00.000','DD.MM.YYYY HH24:MI:SS.MS') and p5.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo  ||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_out, " +
                " coalesce((select SUM(    p4.summ_in-p4.summ_out)  from history_cagent_summ p4 where p4.master_id="+myMasterId+" and p4.company_id="+companyId+" and object_id="+cagentId+" and p4.is_completed=true and p4.date_time_created at time zone '"+myTimeZone+"' <= to_timestamp(:dateTo  ||' 23:59:59.999','DD.MM.YYYY HH24:MI:SS.MS')),0) as summ_on_end";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("dateFrom", dateFrom);
            query.setParameter("dateTo", dateTo);

            List<Object[]> queryList = query.getResultList();
            List<BigDecimal> returnList = new ArrayList<>();
            returnList.add((BigDecimal) queryList.get(0)[0]);//summ_on_start
            returnList.add((BigDecimal) queryList.get(0)[1]);//summ_in
            returnList.add((BigDecimal) queryList.get(0)[2]);//summ_out
            returnList.add((BigDecimal) queryList.get(0)[3]);//summ_on_end

            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception in method getCagentMutualpaymentIndicators. SQL query:" + stringQuery, e);
            return null;
        }
    }
//    @SuppressWarnings("Duplicates")
//    public String getCagentFullName(CagentsJSON cagent){
//        String result;
//        // получим наименование организационно-правовой формы
//        switch (cagent.getOpf_id()) {
//            case  (1):// Индивидуальный предприниматель
//                result = cagent.getOpf() + cagent.getJr_fio_family()+" "+cagent.getJr_fio_name()+" "+cagent.getJr_fio_otchestvo();
//                break;
//            case (2): // Самозанятый
//                result = cagent.getJr_fio_family()+" "+cagent.getJr_fio_name()+" "+cagent.getJr_fio_otchestvo();
//                break;
//            default:  // Все юрлица ( ООО, ЗАО и т.д.)
//                result = cagent.getJr_jur_full_name();
//                break;
//        }
//        return result;
//    }
//
//    @SuppressWarnings("Duplicates")
//    public String getCagentAddress(CagentsJSON cagent){
//        String result;
//        // получим адрес контрагента
//        switch (cagent.getOpf_id()) {
//            case (1):// Индивидуальный предприниматель
//                result = cagent.getZip_code()+" "+cagent.getRegion()+", "+cagent.getArea()+", "+cagent.getCity()+" "+cagent.getStreet()+" д."+cagent.getHome()+(!Objects.isNull(cagent.getFlat())?(" кв."+cagent.getFlat()):"");
//                break;
//            case (2): // Самозанятый
//                result = cagent.getZip_code()+" "+cagent.getRegion()+", "+cagent.getArea()+", "+cagent.getCity()+" "+cagent.getStreet()+" д."+cagent.getHome()+(!Objects.isNull(cagent.getFlat())?(" кв."+cagent.getFlat()):"");
//                break;
//            default:  // Все юрлица ( ООО, ЗАО и т.д.)
//                result = cagent.getZip_code()+" "+cagent.getJr_region()+", "+cagent.getJr_area()+", "+cagent.getJr_city()+" "+cagent.getJr_street()+" "+cagent.getJr_home()+(!Objects.isNull(cagent.getJr_flat())?(" "+cagent.getJr_flat()):"");
//                break;
//        }
//        return result;
//    }

    // inserting base set of categories of new account or company
    @SuppressWarnings("Duplicates")
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {RuntimeException.class})
    public Boolean insertCagentCategoriesFast(Long mId, Long uId, Long cId) {
        String stringQuery;
        Map<String, String> map = commonUtilites.translateForUser(mId, new String[]{"'catg_accounting'","'catg_suppliers'","'catg_customers'","'catg_employees'","'catg_banks'","'catg_transport'","'catg_rent'","'catg_tax_srvcs'","'cagent_accntnts'","'cagent_director_y'","'cagent_supplier'","'cagent_customer'","'cagent_bank'","'cagent_taxoffce'","'cagent_carrier'","'cagent_landlord'","'catg_leads'"});
        String t = new Timestamp(System.currentTimeMillis()).toString();
        stringQuery = "insert into cagent_categories ( master_id,creator_id,company_id,date_time_created,parent_id,output_order,name) values "+
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,1,'"+map.get("catg_suppliers")+"'),"+ // cagent_supplier
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,2,'"+map.get("catg_customers")+"'),"+ // cagent_customer
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,3,'"+map.get("catg_employees")+"'),"+ // cagent_director_y
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,4,'"+map.get("catg_banks")+"'),"+     // cagent_bank
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,5,'"+map.get("catg_accounting")+"'),"+// cagent_accntnts
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,6,'"+map.get("catg_transport")+"'),"+ // cagent_carrier
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,7,'"+map.get("catg_rent")+"'),"+      // cagent_landlord
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,8,'"+map.get("catg_tax_srvcs")+"')," +// cagent_taxoffce
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),null,8,'"+map.get("catg_leads")+"');" +

                "insert into cagents (master_id, creator_id, company_id, date_time_created, name, jr_jur_full_name, type) values " +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_accntnts")+"'  ,'"+map.get("cagent_accntnts")+"'   ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_director_y")+"','"+map.get("cagent_director_y")+"' ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_supplier")+"'  ,'"+map.get("cagent_supplier")+"'   ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_customer")+"'  ,'"+map.get("cagent_customer")+"'   ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_bank")+"'      ,'"+map.get("cagent_bank")+"'       ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_taxoffce")+"'  ,'"+map.get("cagent_taxoffce")+"'   ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_carrier")+"'   ,'"+map.get("cagent_carrier")+"'    ,'entity')," +
                "("+mId+","+uId+","+cId+","+"to_timestamp('"+t+"','YYYY-MM-DD HH24:MI:SS.MS'),'"+map.get("cagent_landlord")+"'  ,'"+map.get("cagent_landlord")+"'   ,'entity');" +

                "insert into cagent_cagentcategories (category_id,cagent_id) values " +
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_suppliers")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_supplier")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_customers")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_customer")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_employees")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_director_y")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_banks")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_bank")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_accounting")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_accntnts")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_transport")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_carrier")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_rent")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_landlord")+"')),"+
                "((select id from cagent_categories where company_id="+cId+" and name = '"+map.get("catg_tax_srvcs")+"'),(select id from cagents where company_id="+cId+" and name = '"+map.get("cagent_taxoffce")+"'));";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            logger.error("Exception in method insertCagentCategoriesFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

//*****************************************************************************************************************************************************
//***********************************************   C A T E G O R I E S   *****************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates") //права не нужны т.к. private, не вызывается по API
    private Set<CagentCategories> getCategoriesSetBySetOfCategoriesId(Set<Long> categories) {
        EntityManager em = emf.createEntityManager();
        CagentCategories dep ;
        Set<CagentCategories> categoriesSet = new HashSet<>();
        for (Long i : categories) {
            dep = em.find(CagentCategories.class, i);
            categoriesSet.add(dep);
        }
        return categoriesSet;
    }

    //права не нужны т.к. не вызывается по API, только из контроллера
    public List<Integer> getCagentsCategoriesIdsByCagentId(Long id) {
        String stringQuery="select p.category_id from cagent_cagentcategories p where p.cagent_id= "+id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }

    @Transactional//права не нужны т.к. не вызывается по API, только из контроллера
    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
    public List<CagentCategories> getCagentCategoriesTrees(List<Integer> rootIds) {
        List<CagentCategories> returnTreesList = new ArrayList<CagentCategories>();
        String stringQuery;
        stringQuery = "from CagentCategories p ";
        stringQuery = stringQuery + " left join fetch p.children";
        entityManager.createQuery(stringQuery, CagentCategories.class).getResultList();
        for(int rootId : rootIds) {
            returnTreesList.add(entityManager.find(CagentCategories.class, Long.valueOf(rootId)));
        }
        return returnTreesList;
    }

    //права на просмотр документов в таблице меню
    @SuppressWarnings("Duplicates") //отдает только найденные категорий, без иерархии
    public List<CagentCategoriesTableJSON> searchCagentCategory(Long companyId, String searchString) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from cagent_categories " +
                    " where company_id ="+companyId+" and master_id="+ myMasterId+ " and upper(name) like upper(CONCAT('%',:sg,'%'))";
            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, CagentCategoriesTableJSON.class);
            query.setParameter("sg",searchString);
            return query.getResultList();
        } else return null;
    }


    @SuppressWarnings("Duplicates") //возвращает id корневых категорий
    public List<Integer> getCategoriesRootIds(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();// мой аккаунт-владелец
            String stringQuery = "select id from cagent_categories " +
                    "  where company_id =" + id + " and master_id=" + myMasterId + " and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    //отдает только список корневых категорий, без детей
    //нужно для изменения порядка вывода корневых категорий
    public List<CagentCategoriesTableJSON> getRootCagentCategories(Long companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "139,140"))//"Контрагенты" (см. файл Permissions Id)
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from cagent_categories " +
                    "  where company_id ="+companyId+" and master_id="+ myMasterId+" and parent_id is null ";
            if(!securityRepositoryJPA.userHasPermissions_OR(12L, "139")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery, CagentCategoriesTableJSON.class);
            return query.getResultList();

        }else return null;
    }

    @SuppressWarnings("Duplicates") //отдает только список детей, без их детей - нужно для изменения порядка вывода категорий
    public List<CagentCategoriesTableJSON> getChildrensCagentCategories(Long parentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "139,140"))//редактирование категорий
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from cagent_categories " +
                    " where parent_id ="+parentId+" and master_id="+ myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "139")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, CagentCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Long insertCagentCategory(CagentCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(12L,"137,138"))//  "Создание категорий"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(12L, "137") &&
                    Long.valueOf(myCompanyId) != request.getCompanyId()) || !DocumentMasterId.equals(myMasterId) )
            {
                return -1L;
            }
            else
            {
                String stringQuery;
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                Long myId = userRepository.getUserId();
                stringQuery = "insert into cagent_categories (" +
                        "name," +
                        "master_id," +
                        "creator_id," +
                        "parent_id," +
                        "company_id," +
                        "date_time_created" +
                        ") values ( " +
                        ":name, "+
                        myMasterId+","+
                        myId+","+
                        (request.getParentCategoryId()>0?request.getParentCategoryId():null)+", "+
                        request.getCompanyId()+", "+
                        "(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')))";
                try
                {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    query.setParameter("name",request.getName());
                    if(query.executeUpdate()==1){
//                        Long id = (Long)query.getSingleResult();
                        stringQuery="" +
                                "select id from cagent_categories where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
                        Query query2 = entityManager.createNativeQuery(stringQuery);
                        return Long.valueOf(Integer.parseInt(query2.getSingleResult().toString()));
                    } else return(0L);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return 0L;
                }
            }
        } else return 0L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateCagentCategory(CagentCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(12L,"139,140"))//  Контрагенты : "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long changer = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "update cagent_categories set " +
                    " name=:name, "+
                    " date_time_changed= now()," +
                    " changer_id= " + changer +
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "139")) //Если нет прав по всем предприятиям
            {
//            остается только на своё предприятие (140)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                query.setParameter("name",request.getName());
                int i = query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCagentCategory(CagentCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "141,142"))//"Контрагенты" удаление категорий
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "delete from cagent_categories "+
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "141")) //Если нет прав по всем предприятиям
            {
                //остается только на своё предприятие (110)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
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

    @Transactional
    public boolean saveChangeCategoriesOrder(List<CagentCategoriesForm> request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(12L,"139,140"))//  Контрагенты : "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            try
            {
                for (CagentCategoriesForm field : request)
                {
                    stringQuery = "update cagent_categories set " +
                            " output_order=" + field.getOutput_order() +
                            " where id=" + field.getId() +
                            " and master_id=" + myMasterId;
                    if (!securityRepositoryJPA.userHasPermissions_OR(12L, "139")) //Если нет прав по всем предприятиям
                    {
//            остается только на своё предприятие (140)
                        int myCompanyId = userRepositoryJPA.getMyCompanyId();
                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
                    }
                    entityManager.createNativeQuery(stringQuery).executeUpdate();
                }
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    //удаление всех категорий из контрагента - нужно для сохранения контрагента на стадии сохранения его категорий - они перезаписываются заново
    //т.к. некоторые могли быть добавлены, а какие-то удалены.
    private Boolean deleteAllCagentCategories(Long cagent_id) {
        String stringQuery;
        try {
            stringQuery =   " delete from cagent_cagentcategories " +
                    " where cagent_id=" + cagent_id;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean addSetOfCagentCategories(Long cagent_id, Set<Long> categories) {
        String stringQuery;
        int i=0;
        try {
            stringQuery =   " insert into cagent_cagentcategories (" +
                    " cagent_id," +
                    " category_id" +
                    ") values ";
            for(long category_id:categories){
                i++;
                stringQuery =  stringQuery + "(" + cagent_id + "," + category_id + ") ";
                if (i < categories.size()){
                    stringQuery =  stringQuery + ", ";
                }
            }
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public Long getCustomerIdByStoreCustomerData(Long company_id, Integer customerWooId, String customerEmail, String customerTelephone) throws Exception {

        // if customerWooId is not null then this customer there is in a DokioCRM database
        try
        {
            if(!Objects.isNull(customerWooId) && customerWooId > 0) {
                String stringQuery =
                        " select id from cagents where " +
                        " company_id = " + company_id +
                        " and woo_id = " + customerWooId + " limit 1";

                Query query = entityManager.createNativeQuery(stringQuery);
                return (Long.valueOf(query.getSingleResult().toString()));
            } else {
                if(!customerEmail.equals("") || !customerTelephone.equals(""))
                    return getCustomerIdByTelOrEmail( company_id,  customerEmail,  customerTelephone);
                else return null;
            }
        }catch (NoResultException nre) {
            return getCustomerIdByTelOrEmail( company_id,  customerEmail,  customerTelephone);
        }catch (Exception e) {
            logger.error("Exception in method getCustomerIdByStoreCustomerData.:", e);
            e.printStackTrace();
            throw new Exception();
        }
    }

    private Long getCustomerIdByTelOrEmail(Long company_id, String customerEmail, String customerTelephone) throws Exception {

        String stringQuery =
                        " select id from cagents where " +
                        " company_id = " + company_id +
                        " and " +
                        "(";

        if(!customerEmail.equals("") )
            stringQuery = stringQuery + " upper(replace(email, ' ', '')) = upper(:email) ";

        if(!customerEmail.equals("") && !customerTelephone.equals(""))
            stringQuery = stringQuery + " or ";

        if(!customerTelephone.equals(""))
        stringQuery = stringQuery + " regexp_replace(telephone, '\\D', '', 'g')  = regexp_replace(:telephone, '\\D', '', 'g') ";

        stringQuery = stringQuery + ") limit 1";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            if(!customerEmail.equals("") ){
                String email = customerEmail.replaceAll("\\s", "");
                query.setParameter("email", email);
                logger.info("Email in query = " + email);
            }
            if(!customerTelephone.equals("")) {
                String telephone = customerTelephone.replaceAll("[^0-9\\+]", "");
                query.setParameter("telephone", telephone);
                logger.info("Telephone in query = " + telephone);
            }
            return (Long.valueOf(query.getSingleResult().toString()));
        }catch (NoResultException nres) {
            return null;
        }catch (Exception e) {
            logger.error("Exception in method getCustomerIdByStoreCustomerData. SQL query:" + stringQuery, e);
            e.printStackTrace();
            throw new Exception();
        }
    }
}
