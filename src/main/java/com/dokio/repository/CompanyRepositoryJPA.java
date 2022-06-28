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
import com.dokio.controller.AuthRestAPIs;
import com.dokio.message.request.*;
import com.dokio.message.response.CompaniesPaymentAccountsJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.Sprav.SpravCurrenciesJSON;
import com.dokio.message.response.additional.BoxofficeListJSON;
import com.dokio.message.response.additional.FilesCompaniesJSON;
import com.dokio.message.response.Sprav.IdAndName;
import com.dokio.model.Companies;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.model.Departments;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.service.generate_docs.GenerateDocumentsDocxService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository("CompanyRepositoryJPA")
public class CompanyRepositoryJPA {

    Logger logger = Logger.getLogger("CompanyRepositoryJPA");

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
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    UserGroupRepositoryJPA userGroupRepository;
    @Autowired
    SpravCurrenciesRepository currenciesRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    SpravSysEdizmJPA spravSysEdizm;
    @Autowired
    SpravBoxofficeRepositoryJPA boxofficeRepository;
    @Autowired
    SpravExpenditureRepositoryJPA expenditureRepository;
    @Autowired
    TypePricesRepositoryJPA typePricesRepository;
    @Autowired
    SpravTaxesRepository taxesRepository;
    @Autowired
    AuthRestAPIs authRestAPIs;
    @Autowired
    SpravStatusDocRepository statusDocRepository;
    @Autowired
    CompaniesPaymentAccountsRepositoryJPA paymentAccountsRepository;

    private static final Set VALID_COLUMNS_FOR_ORDER_BY
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("name","creator","date_time_created_sort","p.name","p.creator","p.date_time_created_sort")
            .collect(Collectors.toCollection(HashSet::new)));
    private static final Set VALID_COLUMNS_FOR_ASC
            = Collections.unmodifiableSet((Set<? extends String>) Stream
            .of("asc","desc")
            .collect(Collectors.toCollection(HashSet::new)));

    public Companies getCompanyById(Long id){
        EntityManager em = emf.createEntityManager();
        Companies cmp = em.find(Companies.class, id);
        return cmp;
    }

    @SuppressWarnings("Duplicates")
    public List<CompaniesJSON> getCompaniesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, Set<Integer> filterOptionsIds) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "5,6"))//"Предприятия" (см. файл Permissions Id)
        {
            if (!VALID_COLUMNS_FOR_ORDER_BY.contains(sortColumn) || !VALID_COLUMNS_FOR_ASC.contains(sortAsc))
                throw new IllegalArgumentException("Invalid query parameters");

            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();
            boolean showDeleted = filterOptionsIds.contains(1);// Показывать только удаленные
            String dateFormat=userRepositoryJPA.getMyDateFormat();

            stringQuery = "select  p.id as id, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.name as name, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           null as currency_id, " +// not use now
                    "           sso.name as opf, "+
                    "           p.opf_id as opf_id, " +
                    // Апдейт Предприятий
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
                    "           p.nds_payer as nds_payer, " +
                    "           p.fio_director as fio_director, " +
                    "           p.director_position as director_position, " +
                    "           p.fio_glavbuh as fio_glavbuh, " +
                    "           p.director_signature_id as director_signature_id, " +
                    "           p.glavbuh_signature_id as glavbuh_signature_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description " +

                    "           from companies p " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf_id=sso.id " +
                    "           LEFT OUTER JOIN sprav_status_dock stat ON p.status_id=stat.id" +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "6")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (5)
                stringQuery = stringQuery + " and p.id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')))";
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            try{
                Query query = entityManager.createNativeQuery(stringQuery)
                        .setFirstResult(offsetreal)
                        .setMaxResults(result);

                if (searchString != null && !searchString.isEmpty())
                {query.setParameter("sg", searchString);}

                List<Object[]> queryList = query.getResultList();
                List<CompaniesJSON> returnList = new ArrayList<>();
                for(Object[] obj:queryList){
                    CompaniesJSON doc=new CompaniesJSON();
                    doc.setId(Long.parseLong(                           obj[0].toString()));
                    doc.setMaster_id(Long.parseLong(                    obj[1].toString()));
                    doc.setCreator_id(Long.parseLong(                   obj[2].toString()));
                    doc.setChanger_id(obj[3]!=null?Long.parseLong(      obj[3].toString()):null);
                    doc.setName((String)                                obj[4]);
                    doc.setDate_time_created((String)                   obj[5]);
                    doc.setDate_time_changed((String)                   obj[6]);
                    doc.setMaster((String)                              obj[7]);
                    doc.setCreator((String)                             obj[8]);
                    doc.setChanger((String)                             obj[9]);
                    doc.setCurrency_id(null);                           // not use now
                    doc.setOpf((String)                                 obj[11]);
                    doc.setOpf_id((Integer)                             obj[12]);
                    doc.setCode((String)                                obj[13]);
                    doc.setTelephone((String)                           obj[14]);
                    doc.setSite((String)                                obj[15]);
                    doc.setEmail((String)                               obj[16]);
                    doc.setZip_code((String)                            obj[17]);
                    doc.setCountry_id((Integer)                         obj[18]);
                    doc.setRegion_id((Integer)                          obj[19]);
                    doc.setCity_id((Integer)                            obj[20]);
                    doc.setStreet((String)                              obj[21]);
                    doc.setHome((String)                                obj[22]);
                    doc.setFlat((String)                                obj[23]);
                    doc.setAdditional_address((String)                  obj[24]);
                    doc.setStatus_id(obj[25]!=null?Long.parseLong(      obj[25].toString()):null);
                    doc.setJr_jur_full_name((String)                    obj[26]);
                    doc.setJr_jur_kpp((String)                          obj[27]);
                    doc.setJr_jur_ogrn((String)                         obj[28]);
                    doc.setJr_zip_code((String)                         obj[29]);
                    doc.setJr_country_id((Integer)                      obj[30]);
                    doc.setJr_region_id((Integer)                       obj[31]);
                    doc.setJr_city_id((Integer)                         obj[32]);
                    doc.setJr_street((String)                           obj[33]);
                    doc.setJr_home((String)                             obj[34]);
                    doc.setJr_flat((String)                             obj[35]);
                    doc.setJr_additional_address((String)               obj[36]);
                    doc.setJr_inn((String)                              obj[37]);
                    doc.setJr_okpo((String)                             obj[38]);
                    doc.setJr_fio_family((String)                       obj[39]);
                    doc.setJr_fio_name((String)                         obj[40]);
                    doc.setJr_fio_otchestvo((String)                    obj[41]);
                    doc.setJr_ip_ogrnip((String)                        obj[42]);
                    doc.setJr_ip_svid_num((String)                      obj[43]);
                    doc.setJr_ip_reg_date((String)                      obj[44]);
                    doc.setNds_payer((Boolean)                          obj[45]);
                    doc.setFio_director((String)                        obj[46]);
                    doc.setDirector_position((String)                   obj[47]);
                    doc.setFio_glavbuh((String)                         obj[48]);
                    doc.setDirector_signature_id(obj[49]!=null?Long.parseLong(   obj[49].toString()):null);
                    doc.setGlavbuh_signature_id(obj[50]!=null?Long.parseLong(    obj[50].toString()):null);
                    doc.setStatus_name((String)                         obj[51]);
                    doc.setStatus_color((String)                        obj[52]);
                    doc.setStatus_description((String)                  obj[53]);
                    returnList.add(doc);
                }
                return returnList;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCompaniesTable. SQL query:" + stringQuery, e);
                return null;
            }
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    public int getCompaniesSize(SearchForm searchRequest) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "6,5"))//"Предприятия" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            boolean showDeleted = searchRequest.getFilterOptionsIds().contains(1);// Показывать только удаленные
            stringQuery = "select  p.id as id " +
                    "           from companies p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_deleted,false) ="+showDeleted;

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "6")) //Если нет прав на "Меню - таблица - "Контрагенты" по всем предприятиям"
            {
                //остается только на своё предприятие (5)
                stringQuery = stringQuery + " and p.id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchRequest.getSearchString() != null && !searchRequest.getSearchString().isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper(CONCAT('%',:sg,'%')))";
            }
            try{
                Query query = entityManager.createNativeQuery(stringQuery);

                if (searchRequest.getSearchString() != null && !searchRequest.getSearchString().isEmpty())
                {query.setParameter("sg", searchRequest.getSearchString());}


                return query.getResultList().size();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception in method getCompaniesSize. SQL query:" + stringQuery, e);
                return 0;
            }

        } else return 0;
    }

    @SuppressWarnings("Duplicates")// отдаёт список банковских счетов предприятия
    public List<CompaniesPaymentAccountsJSON> getCompanyPaymentAccounts(Long docId) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "6,5"))//(см. файл Permissions Id)
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
                    " companies_payment_accounts ap " +
                    " where ap.master_id = " + myMasterId +
                    " and ap.company_id = " + docId + " and coalesce(ap.is_deleted, false) = false";

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "6")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (5)
                stringQuery = stringQuery + " and ap.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by coalesce(ap.is_main, false) desc, ap.output_order asc ";
            logger.info("getCompanyPaymentAccounts SQL = " + stringQuery);
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                List<Object[]> queryList = query.getResultList();
                List<CompaniesPaymentAccountsJSON> returnList = new ArrayList<>();

                for(Object[] obj:queryList){
                    CompaniesPaymentAccountsJSON doc=new CompaniesPaymentAccountsJSON();
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
            }catch (Exception e) {
                logger.error("Error of getCompanyPaymentAccounts, sql="+stringQuery, e);
                e.printStackTrace();
                return null;
            }

        } else return null;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    public CompaniesJSON getCompanyValues(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "6,5"))//"Предприятия" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String myTimeZone = userRepository.getUserTimeZone();
            String suffix = userRepositoryJPA.getMySuffix();
            String dateFormat=userRepositoryJPA.getMyDateFormat();

            stringQuery = "select  p.id as id, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.name as name, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', '"+dateFormat+" HH24:MI') as date_time_changed, " +
                    "           u.name as master, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           null as currency_id, " + // not use now
                    "           sso.name as opf, "+
                    "           p.opf_id as opf_id, " +
                    // Апдейт Предприятий
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
                    "           coalesce(p.nds_payer,false) as nds_payer, " +
                    "           p.fio_director as fio_director, " +
                    "           p.director_position as director_position, " +
                    "           p.fio_glavbuh as fio_glavbuh, " +
                    "           p.director_signature_id as director_signature_id, " +
                    "           p.glavbuh_signature_id as glavbuh_signature_id, " +
                    "           stat.name as status_name, " +
                    "           stat.color as status_color, " +
                    "           stat.description as status_description, " +
                    "           ctr.name_"+suffix+" as country, " +
                    "           jr_ctr.name_"+suffix+" as jr_country, " +
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
                    "           p.stamp_id as stamp_id, " +
                    "           (select f.original_name from files f where f.id=p.director_signature_id) as director_signature_filename, " +
                    "           (select f.original_name from files f where f.id=p.glavbuh_signature_id) as glavbuh_signature_filename, " +
                    "           (select f.original_name from files f where f.id=p.stamp_id) as stamp_filename, " +
                    "           p.card_template_id as card_template_id, " +
                    "           (select f.original_name from files f where f.id=p.card_template_id) as card_template_original_filename, " +
                    "           (select f.name from files f where f.id=p.card_template_id) as card_template_filename, " +
                    "           p.st_prefix_barcode_pieced as st_prefix_barcode_pieced, " +
                    "           p.st_prefix_barcode_packed as st_prefix_barcode_packed, " +
                    "           p.st_netcost_policy as st_netcost_policy, " +
                    "           p.type as type, " +// entity or individual
                    "           p.legal_form as legal_form " +// legal form of individual (ie entrepreneur, ...)
//                    "           p.reg_country_id as reg_country_id, " + // country of registration
//                    "           p.tax_number as tax_number, " + // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//                    "           p.reg_number as reg_number" + // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)

                    "           from companies p " +
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

            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "6")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (5)
                stringQuery = stringQuery + " and p.id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            CompaniesJSON doc = new CompaniesJSON();

            doc.setId(Long.parseLong(                            queryList.get(0)[0].toString()));
            doc.setMaster_id(Long.parseLong(                                queryList.get(0)[1].toString()));
            doc.setCreator_id(Long.parseLong(                               queryList.get(0)[2].toString()));
            doc.setChanger_id(queryList.get(0)[3]!=null?Long.parseLong(     queryList.get(0)[3].toString()):null);
            doc.setName((String)                                            queryList.get(0)[4]);
            doc.setDate_time_created((String)                               queryList.get(0)[5]);
            doc.setDate_time_changed(queryList.get(0)[6]!=null?     (String)queryList.get(0)[6]:"");
            doc.setMaster((String)                                          queryList.get(0)[7]);
            doc.setCreator((String)                                         queryList.get(0)[8]);
            doc.setChanger(queryList.get(0)[9]!=null?               (String)queryList.get(0)[9]:"");
            doc.setCurrency_id((Integer)                                  null);        // not use now
            doc.setOpf(queryList.get(0)[11]!=null?                  (String)queryList.get(0)[11]:"");
            doc.setOpf_id((Integer)                                         queryList.get(0)[12]);
            doc.setCode(queryList.get(0)[13]!=null?                 (String)queryList.get(0)[13]:"");
            doc.setTelephone(queryList.get(0)[14]!=null?            (String)queryList.get(0)[14]:"");
            doc.setSite(queryList.get(0)[15]!=null?                 (String)queryList.get(0)[15]:"");
            doc.setEmail(queryList.get(0)[16]!=null?                (String)queryList.get(0)[16]:"");
            doc.setZip_code(queryList.get(0)[17]!=null?             (String)queryList.get(0)[17]:"");
            doc.setCountry_id((Integer)                                     queryList.get(0)[18]);
            doc.setRegion_id((Integer)                                      queryList.get(0)[19]);
            doc.setCity_id((Integer)                                        queryList.get(0)[20]);
            doc.setStreet(queryList.get(0)[21]!=null?               (String)queryList.get(0)[21]:"");
            doc.setHome(queryList.get(0)[22]!=null?                 (String)queryList.get(0)[22]:"");
            doc.setFlat(queryList.get(0)[23]!=null?                 (String)queryList.get(0)[23]:"");
            doc.setAdditional_address(queryList.get(0)[24]!=null?   (String)queryList.get(0)[24]:"");
            doc.setStatus_id( queryList.get(0)[25]!=null?Long.parseLong(    queryList.get(0)[25].toString()):null);
            doc.setJr_jur_full_name(queryList.get(0)[26]!=null?     (String)queryList.get(0)[26]:"");
            doc.setJr_jur_kpp( queryList.get(0)[27]!=null?          (String)queryList.get(0)[27]:"");
            doc.setJr_jur_ogrn( queryList.get(0)[28]!=null?         (String)queryList.get(0)[28]:"");
            doc.setJr_zip_code(queryList.get(0)[29]!=null?          (String)queryList.get(0)[29]:"");
            doc.setJr_country_id((Integer)                                  queryList.get(0)[30]);
            doc.setJr_region_id((Integer)                                   queryList.get(0)[31]);
            doc.setJr_city_id((Integer)                                     queryList.get(0)[32]);
            doc.setJr_street(queryList.get(0)[33]!=null?            (String)queryList.get(0)[33]:"");
            doc.setJr_home(queryList.get(0)[34]!=null?              (String)queryList.get(0)[34]:"");
            doc.setJr_flat(queryList.get(0)[35]!=null?              (String)queryList.get(0)[35]:"");
            doc.setJr_additional_address(queryList.get(0)[36]!=null?(String)queryList.get(0)[36]:"");
            doc.setJr_inn( queryList.get(0)[37]!=null?              (String)queryList.get(0)[37]:"");
            doc.setJr_okpo( queryList.get(0)[38]!=null?             (String)queryList.get(0)[38]:"");
            doc.setJr_fio_family(queryList.get(0)[39]!=null?        (String)queryList.get(0)[39]:"");
            doc.setJr_fio_name(queryList.get(0)[40]!=null?          (String)queryList.get(0)[40]:"");
            doc.setJr_fio_otchestvo(queryList.get(0)[41]!=null?     (String)queryList.get(0)[41]:"");
            doc.setJr_ip_ogrnip( queryList.get(0)[42]!=null?        (String)queryList.get(0)[42]:"");
            doc.setJr_ip_svid_num(queryList.get(0)[43]!=null?       (String)queryList.get(0)[43]:"");
            doc.setJr_ip_reg_date(queryList.get(0)[44]!=null?       (String)queryList.get(0)[44]:"");
            doc.setNds_payer((Boolean)                                      queryList.get(0)[45]);
            doc.setFio_director(queryList.get(0)[46]!=null?         (String)queryList.get(0)[46]:"");
            doc.setDirector_position(queryList.get(0)[47]!=null?    (String)queryList.get(0)[47]:"");
            doc.setFio_glavbuh(queryList.get(0)[48]!=null?          (String)queryList.get(0)[48]:"");
            doc.setDirector_signature_id( queryList.get(0)[49]!=null?Long.parseLong(  queryList.get(0)[49].toString()):null);
            doc.setGlavbuh_signature_id( queryList.get(0)[50]!=null?Long.parseLong(   queryList.get(0)[50].toString()):null);
            doc.setStatus_name(queryList.get(0)[51]!=null?          (String)queryList.get(0)[51]:"");
            doc.setStatus_color(queryList.get(0)[52]!=null?         (String)queryList.get(0)[52]:"");
            doc.setStatus_description(queryList.get(0)[53]!=null?   (String)queryList.get(0)[53]:"");
            doc.setCountry(queryList.get(0)[54]!=null?              (String)queryList.get(0)[54]:"");
            doc.setJr_country(queryList.get(0)[55]!=null?           (String)queryList.get(0)[55]:"");
            doc.setRegion(queryList.get(0)[56]!=null?               (String)queryList.get(0)[56]:"");
            doc.setJr_region(queryList.get(0)[57]!=null?            (String)queryList.get(0)[57]:"");
            doc.setCity(queryList.get(0)[58]!=null?                 (String)queryList.get(0)[58]:"");
            doc.setJr_city(queryList.get(0)[59]!=null?              (String)queryList.get(0)[59]:"");
            doc.setArea(queryList.get(0)[60]!=null?                 (String)queryList.get(0)[60]:"");
            doc.setJr_area(queryList.get(0)[61]!=null?              (String)queryList.get(0)[61]:"");
            doc.setStamp_id(queryList.get(0)[62]!=null?Long.parseLong(queryList.get(0)[62].toString()):null);
            doc.setDirector_signature_filename(queryList.get(0)[63]!=null?(String)queryList.get(0)[63]:"");
            doc.setGlavbuh_signature_filename(queryList.get(0)[64]!=null?(String)queryList.get(0)[64]:"");
            doc.setStamp_filename(queryList.get(0)[65]!=null?(String)queryList.get(0)[65]:"");
            doc.setCard_template_id(queryList.get(0)[66]!=null?Long.parseLong(queryList.get(0)[66].toString()):null);
            doc.setCard_template_original_filename(queryList.get(0)[67]!=null?(String)queryList.get(0)[67]:"");
            doc.setCard_template_filename(queryList.get(0)[68]!=null?(String)queryList.get(0)[68]:"");
            doc.setSt_prefix_barcode_pieced((Integer)                       queryList.get(0)[69]);
            doc.setSt_prefix_barcode_packed((Integer)                       queryList.get(0)[70]);
            doc.setSt_netcost_policy((String)                               queryList.get(0)[71]);
            doc.setType(queryList.get(0)[72]!=null?                 (String)queryList.get(0)[72]:"");
            doc.setLegal_form((String)                                      queryList.get(0)[73]);
//            doc.setReg_country_id((Integer)                                 queryList.get(0)[73]);
//            doc.setTax_number(queryList.get(0)[74]!=null?           (String)queryList.get(0)[74]:"");
//            doc.setReg_number(queryList.get(0)[75]!=null?           (String)queryList.get(0)[75]:"");

            return doc;

        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateCompany(CompaniesForm request) {

        EntityManager emgr = emf.createEntityManager();
        Companies document = emgr.find(Companies.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(3L, "7"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(3L, "8"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany=(Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getId()));//сохраняется документ моего предприятия
        Long DocumentMasterId=document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDoc =(DocumentMasterId.equals(myMasterId));
        if(((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                ||(!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDoc) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            if(updateCompanyBaseFields(request)){//Сначала сохраняем документ без банковских счетов
                try {//если сохранился...
                    String ids = "";
                    //удаление лишних банковских счетов (которые удалили в фронтэнде)
                    //собираем id банковских счетов, которые есть на сохранение, и удаляем из базы те, которых в этой сборке нет
                    if (request.getCompaniesPaymentAccountsTable()!=null && request.getCompaniesPaymentAccountsTable().size() > 0) {
                        for (CompaniesPaymentAccountsForm row : request.getCompaniesPaymentAccountsTable()) {
                            ids = ids + ((!ids.equals("")&&row.getId()!=null)?",":"") + (row.getId()==null?"":row.getId().toString());
                        }
                    }
                    ids=(!ids.equals("")?ids:"0");
//                    if(deleteCompanyPaymentAccountsExcessRows(ids, request.getId())){
                        //если удаление прошло успешно...
                        for (CompaniesPaymentAccountsForm row : request.getCompaniesPaymentAccountsTable()) {
                            if(row.getId()!=null){//счет содержит id, значит он есть в БД, и нужно его апдейтить
                                updateCompanyPaymentAccounts(row, myMasterId, request.getId());
                            }else{//счет не содержит id, значит его нет в БД, и нужно его инсертить
                                insertCompanyPaymentAccounts(row, myMasterId, request.getId());
                            }
                        }
//                    }
                    return true;
                } catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            } else return false;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    private Boolean updateCompanyBaseFields(CompaniesForm request){//Апдейт документа без банковских счетов
        Long myId = userRepositoryJPA.getMyId();
        try
        {
            String stringQuery;
            stringQuery =   " update companies set " +
                    " name = :name, " +//наименование
                    " opf_id = " + request.getOpf_id() + ", " +//организационно-правовая форма предприятия
                    " changer_id = " + myId + ", " +// кто изменил
                    " date_time_changed = now() " + ", " +//дату изменения
                    " code = :code, " +//код
//                    " currency_id = " + request.getCurrency_id() + ", " +//основная валюта
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
                    " status_id = " + request.getStatus_id() + ", " +//статус предприятия
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
                    " jr_okpo = :jr_okpo, " +//ОКПО
                    " jr_fio_family = :jr_fio_family, " +//Фамилия (для ИП или физлица)
                    " jr_fio_name = :jr_fio_name, " +//Имя (для ИП или физлица)
                    " jr_fio_otchestvo = :jr_fio_otchestvo, " +//Отчество (для ИП или физлица)
                    " jr_ip_ogrnip = :jr_ip_ogrnip, " +//ОГРНИП (для ИП)
                    " jr_ip_svid_num = :jr_ip_svid_num, " +//номер свидетельства (для ИП)
                    " jr_ip_reg_date = to_date(cast(:jr_ip_reg_date as TEXT),'DD.MM.YYYY')," +
                    " nds_payer = " + request.getNds_payer() + ", " +//плательщик НДС
                    " fio_director = :fio_director, " +//ФИО руководителя
                    " director_position = :director_position, " +//Должность руководителя
                    " fio_glavbuh = :fio_glavbuh, " +//ФИО главбуха
                    " director_signature_id = " + request.getDirector_signature_id() + ", " +//подпись директора (id из таблицы Files)
                    " glavbuh_signature_id = " + request.getGlavbuh_signature_id() + ", " + ////подпись главбуха (id из таблицы Files)
                    " stamp_id = " + request.getStamp_id() + ", " + ////печать предприятия (id из таблицы Files)
                    " card_template_id = " + request.getCard_template_id() + ", " +////шаблон карточки предприятия (id из таблицы Files)
                    // Settings
                    " st_prefix_barcode_pieced = "  + request.getSt_prefix_barcode_pieced() + ", " +// prefix of barcode for pieced product
                    " st_prefix_barcode_packed = "  + request.getSt_prefix_barcode_packed() + ", " +// prefix of barcode for packed product
                    " st_netcost_policy = :st_netcost_policy, " +   // policy of netcost calculation by all company or by each department separately

                    " type =            :type," +// entity or individual
                    " legal_form = :legal_form"+
//                    " reg_country_id = " + request.getReg_country_id() + "," + // country of registration
//                    " tax_number =      :tax_number, " + // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//                    " reg_number =      :reg_number" + // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)

                    " where " +
                    " id = " + request.getId();// на Master_id = MyMasterId провеврять не надо, т.к. уже проверено в вызывающем методе
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("st_netcost_policy",request.getSt_netcost_policy());
            query.setParameter("type",request.getType());
            query.setParameter("name", (request.getName()!=null?request.getName():""));
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
            query.setParameter("fio_director",(request.getFio_director()!=null?request.getFio_director():""));
            query.setParameter("director_position",(request.getDirector_position()!=null?request.getDirector_position():""));
            query.setParameter("fio_glavbuh",(request.getFio_glavbuh()!=null?request.getFio_glavbuh():""));
            query.setParameter("legal_form",(request.getLegal_form()!=null?request.getLegal_form():""));
            query.executeUpdate();
            return true;
        }catch (Exception e) {
            logger.error("Error of updateCompanyBaseFields", e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    //удаление лишних расчетных счетов (которые удалили в фронтэнде)
    private Boolean deleteCompanyPaymentAccountsExcessRows(String accountsIds, Long company_id) {
        String stringQuery;
        try {
            stringQuery =   " delete from companies_payment_accounts " +
                    " where company_id=" + company_id +
                    " and id not in (" + accountsIds + ")";
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
    private Boolean insertCompanyPaymentAccounts(CompaniesPaymentAccountsForm row, Long master_id, Long company_id) {
        String stringQuery;
        try {
            stringQuery =   " insert into companies_payment_accounts (" +
                    "master_id," +
                    "company_id," +
                    "bik," +
                    "name," +
                    "address," +
                    "corr_account," +
                    "payment_account," +
                    "output_order"+
                    ") values ("
                    + master_id +", "
                    + company_id +", "
                    + ":bik, "
                    + ":name, "
                    + ":address, "
                    + ":corr_acc, "
                    + ":paym_acc, "
                    + row.getOutput_order() + ")";

            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("bik", (row.getBik()!=null?row.getBik():""));
            query.setParameter("name", (row.getName()!=null?row.getName():""));
            query.setParameter("address",(row.getAddress()!=null?row.getAddress():""));
            query.setParameter("corr_acc",(row.getCorr_account()!=null?row.getCorr_account():""));
            query.setParameter("paym_acc",(row.getPayment_account()!=null?row.getPayment_account():""));
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of insertCompanyPaymentAccounts", e);
            e.printStackTrace();
            return false;
        }

    }

    @SuppressWarnings("Duplicates")
    private Boolean updateCompanyPaymentAccounts(CompaniesPaymentAccountsForm row, Long master_id, Long company_id) {
        String stringQuery;
        try {
            stringQuery =   " update companies_payment_accounts set " +
//                    " bik = :bik, " +
//                    " name = :name, " +
//                    " address = :address, " +
//                    " corr_account = :corr_acc, " +
//                    " payment_account = :paym_acc, " +
                    " output_order = :output_order"+
                    " where " +
                    " id="+row.getId()+" and "+
                    " master_id="+master_id+" and "+
                    " company_id="+company_id;

            Query query = entityManager.createNativeQuery(stringQuery);
//            query.setParameter("bik", (row.getBik()!=null?row.getBik():""));
//            query.setParameter("name", (row.getName()!=null?row.getName():""));
//            query.setParameter("address",(row.getAddress()!=null?row.getAddress():""));
//            query.setParameter("corr_acc",(row.getCorr_account()!=null?row.getCorr_account():""));
//            query.setParameter("paym_acc",(row.getPayment_account()!=null?row.getPayment_account():""));
            query.setParameter("output_order",row.getOutput_order());
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Error of updateCompanyPaymentAccounts", e);
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertCompany(CompaniesForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L,"3"))//  Предприятия : "Создание" (см. файл Permissions Id)
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId(); //владелец предприятия создаваемого документа.
            //plan limit check
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(myMasterId))) // if plan with limits - checking limits
                if(userRepositoryJPA.getMyConsumedResources().getCompanies()>=userRepositoryJPA.getMyMaxAllowedResources().getCompanies())
                    return -120L; // number of companies is out of bounds of tariff plan
            Long createdCompanyId;
            try
            {   //Сначала создаём документ без банковских счетов
                createdCompanyId = insertCompanyBaseFields(request,myMasterId);
                if(createdCompanyId!=null){
                    try {//если создалась..
                        //Сохраняем банковские реквизиты
                        for (CompaniesPaymentAccountsForm row : request.getCompaniesPaymentAccountsTable()) {
                            insertCompanyPaymentAccounts(row, myMasterId, createdCompanyId);
                        }
                        return createdCompanyId;
                    } catch (Exception e){
                        e.printStackTrace();
                        return null;
                    }
                } else return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        } else return -1L;
    }


    public Long insertCompanyBaseFields(CompaniesForm request,Long myMasterId){
        String stringQuery;
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        Long myId = userRepository.getUserId();
        Long newDocId;
        stringQuery =   "insert into companies (" +
                " master_id," + //мастер-аккаунт
                " creator_id," + //создатель
                " date_time_created," + //дата и время создания
                " name, " +//наименование
                " opf_id,"+//организационно-правовая форма предприятия
                " code, " +//код
                " telephone, " +//телефон
                " site, " +//факс
                " email, " +//емейл
//                " currency_id,"+//валюта предприятия
                " status_id,"+//статус предприятия
                //фактический адрес
                " zip_code, " +//почтовый индекс
                " country_id,"+//страна
//                " region_id,"+//область
//                " city_id,"+//город/нас.пункт
                " region, " +//область
                " city, " +//город/нас.пункт
                " street, " +//улица
                " home, " +//дом
                " flat, " +//квартира
                " additional_address, " +//дополнение к адресу
                //Юридические реквизиты
                " jr_jur_full_name, " +//полное название (для юрлиц)
                " jr_jur_kpp, " +//кпп (для юрлиц)
                " jr_jur_ogrn, " +//огрн (для юрлиц)
                //юридический адрес (для юрлиц) /адрес регистрации (для ип и физлиц)
                " jr_zip_code, " +//почтовый индекс
                " jr_country_id,"+//страна
//                " jr_region_id,"+//область
//                " jr_city_id,"+//город/нас.пункт
                " jr_region, " +//область
                " jr_city, " +//город/нас.пункт
                " jr_street, " +//улица
                " jr_home, " +//дом
                " jr_flat, " +//квартира
                " jr_additional_address, " +//дополнение к адресу
                " jr_inn, " +//ИНН
                " jr_okpo, " + //ОКПО
                " jr_fio_family, " +//Фамилия
                " jr_fio_name, " +//Имя
                " jr_fio_otchestvo, " +//Отчество
                " jr_ip_ogrnip, " + //ОГРНИП (для ИП)
                " jr_ip_svid_num, " +//номер свидетельства (для ИП)
                " jr_ip_reg_date," +
                " nds_payer,"+//является ли плательщиком НДС
                " fio_director,"+//ФИО управляющего
                " director_position,"+//должность управляющего
                " fio_glavbuh,"+//ФИО главбуха
                " st_prefix_barcode_pieced, "  + // prefix of barcode for pieced product
                " st_prefix_barcode_packed, "  + // prefix of barcode for packed product
                " st_netcost_policy, " +   // policy of netcost calculation by all company or by each department separately
                " type, " +
                " legal_form"+
//                " reg_country_id, " + // country of registration
//                " tax_number, " + // tax number assigned to the taxpayer in the country of registration (like INN in Russia)
//                " reg_number" + // registration number assigned to the taxpayer in the country of registration (like OGRN or OGRNIP in Russia)

                ") values (" +
                myMasterId + ", "+
                myId + ", "+
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                ":name, " +//наименование
                request.getOpf_id() + ", " +//организационно-правовая форма предприятия
                ":code, " +//код
                ":telephone, " +//телефон
                ":site, " +//факс
                ":email, " +//емейл
//                request.getCurrency_id() + ", " +//валюта
                request.getStatus_id() + ", " +//статус документа
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
                request.getNds_payer() + ", " +//является ли плательщиком НДС
                " :fio_director,"+//ФИО управляющего
                " :director_position,"+//должность управляющего
                " :fio_glavbuh,"+//ФИО главбуха
                (Objects.isNull(request.getSt_prefix_barcode_pieced())?21:request.getSt_prefix_barcode_pieced()) + ", " +// prefix of barcode for pieced product
                (Objects.isNull(request.getSt_prefix_barcode_packed())?20:request.getSt_prefix_barcode_packed()) + ", " +// prefix of barcode for packed product
                ":st_netcost_policy," +  // policy of netcost calculation by all company or by each department separately
                ":type, " +
                ":legal_form"+
//                request.getReg_country_id() + "," +
//                ":tax_number," +
//                ":reg_number" +
                ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            query.setParameter("st_netcost_policy",request.getSt_netcost_policy());
            query.setParameter("type",request.getType());
            query.setParameter("name", (request.getName()!=null?request.getName():""));
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
            query.setParameter("fio_director",(request.getFio_director()!=null?request.getFio_director():""));
            query.setParameter("director_position",(request.getDirector_position()!=null?request.getDirector_position():""));
            query.setParameter("fio_glavbuh",(request.getFio_glavbuh()!=null?request.getFio_glavbuh():""));
            query.setParameter("legal_form",(request.getLegal_form()!=null?request.getLegal_form():""));

//            query.setParameter("tax_number",request.getTax_number());
//            query.setParameter("reg_number",request.getReg_number());
            query.executeUpdate();
            stringQuery="select id from companies where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            newDocId=Long.valueOf(query2.getSingleResult().toString());
            return newDocId;
        } catch (Exception e) {
            logger.error("Exception in method insertCompanyBaseFields. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates")
    public void setCompanyAdditionals(Long companyId){
        Long myId = userRepositoryJPA.getMyId();
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        // типы цен
        Long price = typePricesRepository.insertPriceTypesFast(myId,companyId);
        // кассы предприятия (денежные комнаты)
        Long bo = boxofficeRepository.insertBoxofficesFast(myId,companyId);
        // расчетный счет предприятия
        Long ac = paymentAccountsRepository.insertPaymentAccountsFast(myId,companyId);
        // отделение
        DepartmentForm department = new DepartmentForm();
        department.setName("My department");
        department.setPrice_id(price);
        department.setBoxoffice_id(bo);
        department.setPayment_account_id(ac);
        departmentRepositoryJPA.insertDepartmentFast(department,companyId,myId);
        Long usergroupId = userGroupRepository.insertUsergroupFast("Administrators",companyId,myId);
        Set<Long> permissions = authRestAPIs.getAdminPermissions();
        userGroupRepository.setPermissionsToUserGroup(permissions,usergroupId);
        // набор валют
        currenciesRepository.insertCurrenciesFast(myId,companyId);
        // базовые категоии контрагентов
        cagentRepository.insertCagentCategoriesFast(myId,companyId);
        // единицы имерения
        spravSysEdizm.insertEdizmFast(myId,companyId);
        // налоги
        taxesRepository.insertTaxesFast(myId,companyId);
        // расходы
        expenditureRepository.insertExpendituresFast(myId,companyId);
        // статусы документов
        statusDocRepository.insertStatusesFast(myMasterId,myId,companyId);
    }

    @SuppressWarnings("Duplicates")// отдаёт список касс предприятия
    public List<BoxofficeListJSON> getBoxofficesList(Long companyId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery =   " select " +
                " ap.id," +
                " ap.name," +
                " ap.description," +
                " coalesce(ap.is_main,false)" +
                " from " +
                " sprav_boxoffice ap " +
                " where ap.master_id = " + myMasterId +
                " and ap.company_id = " + companyId +
                " and coalesce(ap.is_deleted,false) != true";

        stringQuery = stringQuery + " order by ap.id asc, ap.name asc ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<BoxofficeListJSON> returnList = new ArrayList<>();

            for(Object[] obj:queryList){
                BoxofficeListJSON doc=new BoxofficeListJSON();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setName((String)                                    obj[1]);
                doc.setDescription((String)                             obj[2]);
                doc.setIs_main((Boolean)                                obj[3]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getBoxofficesList. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @SuppressWarnings("Duplicates")// отдаёт кассу предприятия, к которой привязано отделение departmentId
    public List<BoxofficeListJSON> getBoxofficeByDepId(Long companyId, Long departmentId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery =   " select " +
                " ap.id," +
                " ap.name," +
                " ap.description," +
                " coalesce(ap.is_main,false)" +
                " from " +
                " sprav_boxoffice ap " +
                " where ap.master_id = " + myMasterId +
                " and ap.company_id = " + companyId +
                " and ap.id = coalesce((select d.boxoffice_id from departments d where d.id="+departmentId+"),0)"+
                " and coalesce(ap.is_deleted,false) != true";

        stringQuery = stringQuery + " order by ap.id asc, ap.name asc ";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            List<BoxofficeListJSON> returnList = new ArrayList<>();

            for(Object[] obj:queryList){
                BoxofficeListJSON doc=new BoxofficeListJSON();
                doc.setId(Long.parseLong(                               obj[0].toString()));
                doc.setName((String)                                    obj[1]);
                doc.setDescription((String)                             obj[2]);
                doc.setIs_main((Boolean)                                obj[3]);
                returnList.add(doc);
            }
            return returnList;
        }catch (Exception e) {
            logger.error("Exception in method getBoxofficeByDepId. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCompanies(String delNumbers) {
        //Если есть право на "Удаление" и все id для удаления принадлежат владельцу мастер-аккаунта
        if(securityRepositoryJPA.userHasPermissions_OR(3L,"4") && securityRepositoryJPA.isItAllMyMastersDocuments("companies",delNumbers))
        {
            String stringQuery;
            Long myId = userRepositoryJPA.getMyId();
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            stringQuery = "Update companies p set " +
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=true " + //метка об удалении
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+") and p.master_id="+myMasterId;
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return true;
            }catch(Exception e){
                return false;
            }
        } else return false;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public Integer undeleteCompanies(String delNumbers) {//восстанавливает документ из удаленных
        //Если есть право на "Удаление" и все id для документов принадлежат владельцу мастер-аккаунта
        if(securityRepositoryJPA.userHasPermissions_OR(3L,"4") && securityRepositoryJPA.isItAllMyMastersDocuments("companies",delNumbers))
        {
            //plan limit check
            Long masterId =  userRepositoryJPA.getMyMasterId();
            long amountToRepair = delNumbers.split(",").length;
            if(!userRepositoryJPA.isPlanNoLimits(userRepositoryJPA.getMasterUserPlan(masterId))) // if plan with limits - checking limits
                if((userRepositoryJPA.getMyConsumedResources().getCompanies()+amountToRepair)>userRepositoryJPA.getMyMaxAllowedResources().getCompanies())
                    return -120; // number of users is out of bounds of tariff plan
            String stringQuery;
            Long myId = userRepositoryJPA.getMyId();
            stringQuery = "Update companies p set " +
                    " changer_id="+ myId + ", " + // кто изменил (удалил)
                    " date_time_changed = now(), " +//дату и время изменения
                    " is_deleted=false " + //метка об удалении
                    " where p.id in ("+delNumbers.replaceAll("[^0-9\\,]", "")+")";
            try{
                Query query = entityManager.createNativeQuery(stringQuery);
                query.executeUpdate();
                return 1;
            }catch(Exception e){
                logger.error("Exception in method undeleteCompanies. SQL query:"+stringQuery, e);
                return null;
            }
        } else return -1;
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public List<IdAndName> getCompaniesList() {
        String stringQuery;

        // Владелец предприятия (masterId)
        Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        // Текущий пользователь (он может быть владельцем, а может и не быть).
        // Владельцу (masterId) доступны все предприятия его аккаунта, пользователю - только одно предприятие владельца - то, к которому он привязан
        Long myId = userRepositoryJPA.getMyId();

        //Выясним, является ли пользователь одновременно и владельцем, и доступны ли ему все предприятия аккаунта
        Boolean isIamMaster = myId==companyOwnerId;

        Long myCompanyId=0L;

        if(!isIamMaster) //если пользователь не владелец (не masterId) - найдем id его предприятия, для фильтрации списка предприятий (т.к. он не должен видеть остальные предприятия мастер-аккаунта)
            myCompanyId=userRepositoryJPA.getMyCompanyId_();

        stringQuery = "select " +
                "           p.id as id, " +
                "           p.name as name " +
                "           from companies p " +
                "           where  p.master_id=" + companyOwnerId +
                "           and coalesce(p.is_deleted,false)=false ";

        //если пользователь не владелец (не masterId) - оставляем только его предприятие
        if(!isIamMaster) stringQuery = stringQuery + " and p.id=" + myCompanyId;

        stringQuery = stringQuery + " group by p.id order by p.name asc";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<IdAndName> returnList = new ArrayList<>();
        for (Object[] obj : queryList) {
            IdAndName doc = new IdAndName();
            doc.setId(Long.parseLong(obj[0].toString()));
            doc.setName((String) obj[1]);
            returnList.add(doc);
        }
        return returnList;
    }

//*****************************************************************************************************************************************************
//****************************************************   F   I   L   E   S   **************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addFilesToCompany(UniversalForm request){
        Long companyId = request.getId1();
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(3L,"8") && securityRepositoryJPA.isItAllMyMastersDocuments("companies",companyId.toString())) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(3L,"7") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("companies",companyId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select company_id from companies_files where company_id=" + companyId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких файлов еще нет у документа
                        entityManager.close();
                        manyToMany_CompanyId_FileId(companyId,fileId);
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
    boolean manyToMany_CompanyId_FileId(Long companyId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into companies_files " +
                    "(company_id,file_id) " +
                    "values " +
                    "(" + companyId + ", " + fileId +")")
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
    public List<FilesCompaniesJSON> getListOfCompanyFiles(Long companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(3L, "5,6"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name" +
                    "           from" +
                    "           companies p" +
                    "           inner join" +
                    "           companies_files pf" +
                    "           on p.id=pf.company_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + companyId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(3L, "6")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (5)
                stringQuery = stringQuery + " and p.id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            stringQuery = stringQuery+" order by f.original_name asc ";
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();

            List<FilesCompaniesJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                FilesCompaniesJSON doc=new FilesCompaniesJSON();
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
    public boolean deleteCompanyFile(SearchForm request)
    {
        //Если есть право на "Редактирование по всем предприятиям" и id принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if( (securityRepositoryJPA.userHasPermissions_OR(3L,"8") && securityRepositoryJPA.isItAllMyMastersDocuments("companies", String.valueOf(request.getAny_id()))) ||
                //Если есть право на "Редактирование по своему предприятияю" и  id принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(3L,"7") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("companies",String.valueOf(request.getAny_id()))))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            Long myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from companies_files "+
                    " where company_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from companies where id="+request.getAny_id()+")="+myMasterId ;
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

    @SuppressWarnings("Duplicates")
    public Long getCompanyIdByFilename(String fileName){
        String stringQuery;
        stringQuery  =  " select company_id from files where name = :fileName" ;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("fileName",fileName);
            query.executeUpdate();
            return Long.parseLong(query.getSingleResult().toString());
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
// возвращает id кассы, к которой привязано отделение
    public Long getBoxofficeIdByDepartment(Long depId){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery  =  " select boxoffice_id from departments where master_id = " + myMasterId + " and id = " + depId+
                        " and coalesce(is_deleted,false) != true";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
            logger.info("NoResultException in method getBoxofficeIdByDepartment. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getBoxofficeIdByDepartment. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    // возвращает id кассы предприятия (берет самую раннюю кассу которая не удалена)
    public Long getMainBoxofficeIdOfCompany(Long companyId){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery  =  " select id from sprav_boxoffice where " +
                        " master_id="+myMasterId+
                        " and company_id="+companyId+
                        " and coalesce(is_deleted,false) != true" +
                        " order by id asc limit 1";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
            logger.info("NoResultException in method getMainBoxofficeIdOfCompany. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getMainBoxofficeIdOfCompany. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    // возвращает id расчетного счета отделения
    public Long getPaymentAccountIdByDepartment(Long depId){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery  =  " select payment_account_id from departments where master_id = " + myMasterId + " and id = " + depId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
            logger.info("NoResultException in method getPaymentAccountIdByDepartment. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getPaymentAccountIdByDepartment. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
    // возвращает id расчетного счета отделения
    public Long getMainPaymentAccountIdOfCompany(Long companyId){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery=" select id from companies_payment_accounts where master_id ="+myMasterId+" and company_id="+companyId+" order by output_order asc limit 1";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            return Long.valueOf(query.getSingleResult().toString());
        } catch (NoResultException nre) {
            logger.info("NoResultException in method getMainPaymentAccountIdOfCompany. Sql: " + stringQuery, nre);
            return 0L;
        } catch (Exception e) {
            logger.error("Exception in method getMainPaymentAccountIdOfCompany. Sql: " + stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Банковские реквизиты (если они есть). Берётся самый верхний банк из списка карточек банковских реквизитов
    public CompaniesPaymentAccountsForm getMainPaymentAccountOfCompany(Long companyId){
        List<CompaniesPaymentAccountsJSON> companyPaymentAccounts = getCompanyPaymentAccounts(companyId);
        CompaniesPaymentAccountsForm account = new CompaniesPaymentAccountsForm();
        if(companyPaymentAccounts.size()>0){
            account.setName(companyPaymentAccounts.get(0).getName());
            account.setBik(companyPaymentAccounts.get(0).getBik());
            account.setAddress(companyPaymentAccounts.get(0).getAddress());
            account.setCorr_account(companyPaymentAccounts.get(0).getCorr_account());
            account.setPayment_account(companyPaymentAccounts.get(0).getPayment_account());
            account.setIntermediatery(companyPaymentAccounts.get(0).getIntermediatery());
            account.setSwift(companyPaymentAccounts.get(0).getSwift());
            account.setIban(companyPaymentAccounts.get(0).getIban());
        }
        return account;
    }


    @SuppressWarnings("Duplicates")
    public Resource getCompanyCard(FileInfoJSON fileInfo) {

        GenerateDocumentsDocxService gt = new GenerateDocumentsDocxService();


        //запросим данные по предприятию, найдя его через название файла
        Long companyId=getCompanyIdByFilename(fileInfo.getOriginal_name());
        CompaniesJSON company=getCompanyValues(companyId);

        //создадим данные для подмены плейсхолдеров вида "Имя-Значение", например, "TM_NAME" - "ООО Докио"
        Map<String,String> changeMap = new HashMap<String,String>();
        changeMap.put("TMFIODIRECTOR", company.getFio_director());
        changeMap.put("TMNAME", company.getName());
        changeMap.put("TMDIRECTORPOSITION", company.getDirector_position());

        String orgType;

        List<Map<String,String>> mapAsList=new ArrayList<Map<String, String>>();
        //создадим данные для заполнения таблицы

        switch (company.getOpf_id()) {
            case  (1):// Индивидуальный предприниматель
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Полное наименование","Индивидуальный предприниматель " + company.getJr_fio_family()+" "+company.getJr_fio_name()+" "+company.getJr_fio_otchestvo()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ИНН",company.getJr_inn()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ОГРНИП",company.getJr_ip_ogrnip()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ОКПО",company.getJr_okpo()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Номер свидетельства",company.getJr_ip_svid_num()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Дата регистрации",company.getJr_ip_reg_date()}));
                break;
            case (2): // Самозанятый
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ФИО", company.getJr_fio_family()+" "+company.getJr_fio_name()+" "+company.getJr_fio_otchestvo()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ИНН",company.getJr_inn()}));
                break;
            default:  // Все юрлица ( ООО, ЗАО и т.д.)
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Полное наименование",company.getJr_jur_full_name()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ИНН",company.getJr_inn()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"КПП",company.getJr_jur_kpp()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ОГРН",company.getJr_jur_ogrn()}));
                mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"ОКПО",company.getJr_okpo()}));
                break;
        }
        //Банковские реквизиты (если они есть). Берутся самые верхние из списка карточек банковских реквизитов
        List<CompaniesPaymentAccountsJSON> companyPaymentAccounts = getCompanyPaymentAccounts(companyId);
        if(companyPaymentAccounts.size()>0){
            mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Банковские реквизиты:",""}));
            mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Наименование", companyPaymentAccounts.get(0).getName()}));
            mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"БИК",companyPaymentAccounts.get(0).getBik()}));
            mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Адрес", companyPaymentAccounts.get(0).getAddress()}));
            mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Кор. счёт", companyPaymentAccounts.get(0).getCorr_account()}));
            mapAsList.add(gt.getHashMap(new String[]{"TM_PARAMETER","TM_VALUE"},new String[]{"Расчётный счёт", companyPaymentAccounts.get(0).getPayment_account()}));
        }






        String filePath=fileInfo.getPath()+"/"+fileInfo.getOriginal_name();
        File template= new File(filePath);//сформировалась строка типа new File("C:\\Temp\\files\\4\\1\\2020/f561b9d3-e27-2020-09-16-15-09-25-572.docx")
        String outputDocument = fileInfo.getPath()+"/company_card.docx";

        try
        {
            if(gt.generateDocument(template, outputDocument, changeMap, mapAsList)) {
                Path file = Paths.get(outputDocument);
                Resource resource = new UrlResource(file.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                } else {
                    throw new RuntimeException("Fail to load from filepath '" + outputDocument + "'");
                }
            } else return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException("MalformedURLException! Fail to load from filepath '"+outputDocument+"'");
        }
    }

    @Transactional
    public Long insertCompanyFast(CompaniesForm request,Long myMasterId){
        String stringQuery;
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        Long companyId;
        stringQuery =   "insert into companies (" +
                " master_id," + //мастер-аккаунт
                " creator_id," + //создатель
                " date_time_created," + //дата и время создания
                " st_netcost_policy," +
                " st_prefix_barcode_pieced," +
                " st_prefix_barcode_packed," +
                " name" + //наименование
                ") values (" +
                myMasterId + ", "+
                myMasterId + ", "+
                "to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')," +
                "'all', " +
                request.getSt_prefix_barcode_pieced()+", "+
                request.getSt_prefix_barcode_packed()+", "+
                "'" + (request.getName() == null ? "Company": request.getName()) + "'" +//наименование
                ")";
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            stringQuery="select id from companies where date_time_created=(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id="+myMasterId;
            Query query2 = entityManager.createNativeQuery(stringQuery);
            companyId=Long.valueOf(query2.getSingleResult().toString());
            return companyId;
        } catch (Exception e) {
            logger.error("Exception in method insertCompanyFast. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }
}
