package com.laniakea.repository;

import com.laniakea.message.request.*;
import com.laniakea.message.response.*;
import com.laniakea.model.*;
import com.laniakea.model.Sprav.SpravSysEdizm;
import com.laniakea.model.Sprav.SpravSysMarkableGroup;
import com.laniakea.model.Sprav.SpravSysNds;
import com.laniakea.model.Sprav.SpravSysPPR;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class ProductsRepositoryJPA {
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

    // Инициализация логера
    private static final Logger log = Logger.getLogger(ProductsRepositoryJPA.class);

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<ProductsTableJSON> getProductsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int categoryId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Меню - таблица
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           p.product_code as product_code, " +
                    "           p.ppr_id as ppr_id, " +
                    "           coalesce(p.by_weight,false) as by_weight, " +
                    "           p.edizm_id as edizm_id, " +
                    "           p.nds_id as nds_id, " +
                    "           p.weight as weight, " +
                    "           p.volume as volume, " +
                    "           p.weight_edizm_id as weight_edizm_id, " +
                    "           p.volume_edizm_id as volume_edizm_id, " +
                    "           coalesce(p.markable,false) as markable, " +
                    "           p.markable_group_id as markable_group_id, " +
                    "           coalesce(p.excizable,false) as excizable, " +
                    "           p.article as article, " +
                    "           p.group_id as productgroup_id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pg.name as productgroup, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           coalesce(p.not_buy, false) as not_buy, " +
                    "           coalesce(p.not_sell, false) as not_sell " +
                    "           from products p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                    (categoryId!=0?" and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id="+categoryId+") ":"");

            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or "+
                        "upper(p.article) like upper('%" + searchString + "%') or "+
                        "to_char(p.product_code_free,'fm0000000000') like upper('%" + searchString + "%') or "+
                        "upper(pg.name) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, ProductsTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            return query.getResultList();
        } else return null;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public int getProductsSize(String searchString, int companyId, int categoryId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//"Группы товаров" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           pg.name as productgroup " +
                    "           from products p " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                    (categoryId!=0?" and p.id in (select ppg.product_id from product_productcategories ppg where ppg.category_id="+categoryId+") ":"");

            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or "+
                        "upper(p.article) like upper('%" + searchString + "%') or "+
                        "to_char(p.product_code_free,'fm0000000000') like upper('%" + searchString + "%') or "+
                        "upper(pg.name) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery);

            return query.getResultList().size();
        } else return 0;
    }

//*****************************************************************************************************************************************************
//****************************************************   C  R  U  D   *********************************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public ProductsJSON getProductValues(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  " +
                    "           p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +

                    "           p.product_code as product_code, " +
                    "           p.ppr_id as ppr_id, " +
                    "           coalesce(p.by_weight,false) as by_weight, " +
                    "           p.edizm_id as edizm_id, " +
                    "           p.nds_id as nds_id, " +
                    "           p.weight as weight, " +
                    "           p.volume as volume, " +
                    "           p.weight_edizm_id as weight_edizm_id, " +
                    "           p.volume_edizm_id as volume_edizm_id, " +
                    "           coalesce(p.markable,false) as markable, " +
                    "           p.markable_group_id as markable_group_id, " +
                    "           coalesce(p.excizable,false) as excizable, " +

                    "           p.article as article, " +
                    "           p.product_code_free as product_code_free, " +
                    "           p.group_id as productgroup_id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           pg.name as productgroup, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           (select abbreviation from sprav_sys_opf where id=cmp.opf)||' '||cmp.name as company, " +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort, " +
                    "           p.description as description, " +
                    "           coalesce(p.not_buy, false) as not_buy, " +
                    "           coalesce(p.not_sell, false) as not_sell " +

                    "           from products p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN product_groups pg ON p.group_id=pg.id " +
                    "           where p.id= " + id+
                    "           and  p.master_id=" + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductsJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                ProductsJSON response = (ProductsJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }

    @Transactional
    // апдейчу в 3 захода (3 метода), т.к. если делать все в одном методе, получаю ошибку Executing an update/delete query
    // т.к. транзакция entityManager не коммитится пока нет выхода из метода
    public boolean updateProducts(ProductsForm request) {
        if(updateProductsWithoutOrders(request)){                                      //метод 1

            try {//сохранение порядка поставщиков товара
                if (request.getCagentsIdsInOrderOfList().size() > 1) {
                    int c = 0;
                    for (Long field : request.getCagentsIdsInOrderOfList()) {
                        c++;
                        if (!saveChangeProductCagentsOrder(field, request.getId(), c)) {//         //метод 2
                            break;
                        }
                    }
                }

                //сохранение порядка картинок товара
                if (request.getImagesIdsInOrderOfList().size() > 1) {
                    int i = 0;
                    for (Long field : request.getImagesIdsInOrderOfList()) {
                        i++;
                        if (!saveChangeProductImagesOrder(field, request.getId(), i)) {//         //метод 3
                            break;
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean updateProductsWithoutOrders(ProductsForm request) {
        EntityManager emgr = emf.createEntityManager();
        Products document = emgr.find(Products.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(14L, "170"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(14L, "169"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany=(Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId=document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDock =(DocumentMasterId.equals(myMasterId));// документ под юрисдикцией главного аккаунта

        if(((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                ||(!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDock) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try
            {
                Long id=Long.valueOf(request.getId());
                Products product = emgr.find(Products.class, id);

                emgr.getTransaction().begin();

                product.setName           (request.getName() == null ? "": request.getName());
                product.setDescription       (request.getDescription() == null ? "": request.getDescription());
                product.setArticle       (request.getArticle() == null ? "": request.getArticle());

                if ((request.getProduct_code_free()==null?0L:request.getProduct_code_free())>0L) {
                    product.setProduct_code_free(request.getProduct_code_free());
                } else product.setProduct_code_free(generateFreeProductCode(request.getCompany_id()));

                //группа товаров
                if (request.getProductgroup_id()!=null) {
                    ProductGroups pg = emgr.find(ProductGroups.class, request.getProductgroup_id());
                    product.setProductGroup  (pg);
                }

                //категории товаров и услуг
                Set<Long> categories = request.getSelectedProductCategories();
                if (!categories.isEmpty()) { //если есть выбранные чекбоксы категорий
                    Set<ProductCategories> setCategoriesOfProduct= getCategoriesSetBySetOfCategoriesId(categories);
                    product.setProductCategories(setCategoriesOfProduct);
                } else { // если ни один чекбокс категорий не выбран
                    product.setProductCategories(null);
                }

                //код товара (до 99999)
                //product.setProduct_code(request.getProduct_code());
                //признак предмета расчёта
                if (request.getPpr_id()!=null) {
                    SpravSysPPR ed = emgr.find(SpravSysPPR.class, request.getPpr_id());
                    product.setPpr  (ed);}
                //весовой товар (Boolean)
                product.setBy_weight(request.isBy_weight());
                //единица измерения товара
                if (request.getEdizm_id()!=null) {
                    SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getEdizm_id());
                    product.setEdizm  (ed);}
                //НДС
                if (request.getNds_id()!=null) {
                    SpravSysNds ed = emgr.find(SpravSysNds.class, request.getNds_id());
                    product.setNds  (ed);}
                //Вес товара (приходит String, конвертим в BigDecimal)
                if (request.getWeight() != null && !request.getWeight().isEmpty() && request.getWeight().trim().length() > 0) {
                    product.setWeight(new BigDecimal(request.getWeight().replace(",",".")));
                } else { product.setWeight(new BigDecimal("0"));}
                //Объём товара (приходит String, конвертим в BigDecimal)
                if (request.getVolume() != null && !request.getVolume().isEmpty() && request.getVolume().trim().length() > 0) {
                    product.setVolume(new BigDecimal(request.getVolume().replace(",",".")));
                } else { product.setVolume(new BigDecimal("0"));}
                //единица измерения веса товара
                if (request.getWeight_edizm_id()!=null) {
                    SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getWeight_edizm_id());
                    product.setWeight_edizm  (ed);}
                //единица измерения объёма товара
                if (request.getVolume_edizm_id()!=null) {
                    SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getVolume_edizm_id());
                    product.setVolume_edizm  (ed);}
                //маркированный товар (Boolean)
                product.setMarkable(request.isMarkable());
                //группа маркированных товаров
                if (request.getMarkable_group_id()!=null) {
                    SpravSysMarkableGroup ed = emgr.find(SpravSysMarkableGroup.class, request.getMarkable_group_id());
                    product.setMarkable_group  (ed);}
                //не закупаемый товар (Boolean)
                product.setNot_buy(request.isNot_buy());
                //снятый с продажи товар (Boolean)
                product.setNot_sell(request.isNot_sell());

                User changer = userService.getUserByUsername(userService.getUserName());
                product.setChanger(changer);//кто изменил

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                product.setDate_time_changed(timestamp);//дату изменения

                emgr.getTransaction().commit();
                emgr.close();
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else return false;
    }

    // сохранение порядка картинок товара (права не нужны, т.к. вызывается после проверки всех прав)
    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean saveChangeProductImagesOrder(Long ProductImageId, Long productId, int order)
    {
        String stringQuery;
        try {
            stringQuery =   " update product_files set " +
                    " output_order=" + order +
                    " where file_id=" + ProductImageId +
                    " and product_id=" +productId;
            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // сохранение порядка картинок товара (права не нужны, т.к. вызывается после проверки всех прав)
    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean saveChangeProductCagentsOrder(Long cagentId, Long productId, int order)
    {
        String stringQuery;
        try {
            stringQuery =   " update product_cagents set " +
                    " output_order=" + order +
                    " where cagent_id=" + cagentId +
                    " and product_id=" +productId;
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
    @Transactional
    public Long insertProduct(ProductsForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L,"163,164"))//  "Создание"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие создаваемого документа
            Long DocumentMasterId = companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(14L, "163") &&
                    Long.valueOf(myCompanyId) != request.getCompany_id()) || DocumentMasterId != myMasterId) {
                return null;
            } else {
                try {
                    Products newDocument = new Products();
                    //создатель
                    User creator = userRepository.getUserByUsername(userRepository.getUserName());
                    newDocument.setCreator(creator);//создателя
                    //владелец
                    User master = userRepository.getUserByUsername(
                            userRepositoryJPA.getUsernameById(
                                    userRepositoryJPA.getUserMasterIdByUsername(
                                            userRepository.getUserName())));
                    newDocument.setMaster(master);
                    //дата и время создания
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    newDocument.setDate_time_created(timestamp);//
                    //предприятие
                    newDocument.setCompany(companyRepositoryJPA.getCompanyById(request.getCompany_id()));
                    //Наименование
                    newDocument.setName(request.getName() == null ? "" : request.getName());
                    //дополнительная информация
                    newDocument.setDescription(request.getDescription() == null ? "" : request.getDescription());
                    //артикул
                    newDocument.setArticle(request.getArticle() == null ? "" : request.getArticle());
                    //свободный код
                    //              newDocument.setProduct_code_free        (request.getProduct_code_free() == null ? "": request.getProduct_code_free());
                    newDocument.setProduct_code_free(generateFreeProductCode(request.getCompany_id()));

                    //группа товаров
                    if (request.getProductgroup_id() != null) {
                        ProductGroups pg = emgr.find(ProductGroups.class, request.getProductgroup_id());
                        newDocument.setProductGroup(pg);
                    }

                    Set<Long> categories = request.getSelectedProductCategories();
                    if (!categories.isEmpty()) {
                        Set<ProductCategories> setCategoriesOfProduct = getCategoriesSetBySetOfCategoriesId(categories);
                        newDocument.setProductCategories(setCategoriesOfProduct);
                    }

                    //код товара (до 99999)
                    //newDocument.setProduct_code(request.getProduct_code());
                    //признак предмета расчёта
                    if (request.getPpr_id() != null) {
                        SpravSysPPR ed = emgr.find(SpravSysPPR.class, request.getPpr_id());
                        newDocument.setPpr(ed);
                    }
                    //весовой товар (Boolean)
                    newDocument.setBy_weight(request.isBy_weight());
                    //единица измерения товара
                    if (request.getEdizm_id() != null) {
                        SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getEdizm_id());
                        newDocument.setEdizm(ed);
                    }
                    //НДС
                    if (request.getNds_id() != null) {
                        SpravSysNds ed = emgr.find(SpravSysNds.class, request.getNds_id());
                        newDocument.setNds(ed);
                    }
                    //Вес товара (приходит String, конвертим в BigDecimal)
                    if (request.getWeight() != null && !request.getWeight().isEmpty() && request.getWeight().trim().length() > 0) {
                        newDocument.setWeight(new BigDecimal(request.getWeight().replace(",", ".")));
                    } else {
                        newDocument.setWeight(new BigDecimal("0"));
                    }
                    //Объём товара (приходит String, конвертим в BigDecimal)
                    if (request.getVolume() != null && !request.getVolume().isEmpty() && request.getVolume().trim().length() > 0) {
                        newDocument.setVolume(new BigDecimal(request.getVolume().replace(",", ".")));
                    } else {
                        newDocument.setVolume(new BigDecimal("0"));
                    }
                    //единица измерения веса товара
                    if (request.getWeight_edizm_id() != null) {
                        SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getWeight_edizm_id());
                        newDocument.setWeight_edizm(ed);
                    }
                    //единица измерения объёма товара
                    if (request.getVolume_edizm_id() != null) {
                        SpravSysEdizm ed = emgr.find(SpravSysEdizm.class, request.getVolume_edizm_id());
                        newDocument.setVolume_edizm(ed);
                    }
                    //маркированный товар (Boolean)
                    newDocument.setMarkable(request.isMarkable());
                    //группа маркированных товаров
                    if (request.getMarkable_group_id() != null) {
                        SpravSysMarkableGroup ed = emgr.find(SpravSysMarkableGroup.class, request.getMarkable_group_id());
                        newDocument.setMarkable_group(ed);
                    }
                    //не закупаемый товар (Boolean)
                    newDocument.setNot_buy(request.isNot_buy());
                    //товар снят с продажи (Boolean)
                    newDocument.setNot_sell(request.isNot_sell());

                    entityManager.persist(newDocument);
                    entityManager.flush();
                    return newDocument.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProducts(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"165") && securityRepositoryJPA.isItAllMyMastersDocuments("products",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"166") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "Update products p" +
                    " set is_archive=true " +
                    " where p.master_id=" +myMasterId+
                    " and p.id in (" + delNumbers+")";
            entityManager.createNativeQuery(stringQuery).executeUpdate();
            return true;
        } else return false;
    }


    @SuppressWarnings("Duplicates")
    public List<ProductHistoryJSON> getProductHistoryTable(Long companyId, String departmentId, Long productId, String dateFrom, String dateTo, String sortColumn, String sortAsc, int result, String dockTypesIds, int offsetreal) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))// Просмотр по (всем,своим) предприятиям
        {
            String stringQuery;
            String myTimeZone = userRepository.getUserTimeZone();
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  " +
                    "           p.id as id, " +
                    "           dep.name as department," +
                    "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           doc.name as docName," +
                    "           p.doc_id as docId," +
                    "           p.doc_type_id as docTypeId," +
                    "           p.quantity as quantity," +
                    "           p.change as change," +
                    "           p.last_purchase_price as last_purchase_price," +
                    "           p.avg_purchase_price as avg_purchase_price," +
                    "           p.avg_netcost_price as avg_netcost_price," +
                    "           p.last_operation_price as last_operation_price," +
                    "           p.date_time_created as date_time_created_sort " +
                    "           from products_history p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN departments dep ON p.department_id=dep.id " +
                    "           INNER JOIN documents doc ON p.doc_type_id=doc.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and  p.doc_type_id in (" +dockTypesIds+")" +
                    "           and  p.department_id in (" +departmentId+")" +
                    "           and  p.product_id = " +productId+
                    "           and p.date_time_created >=to_date('"+dateFrom+"','DD.MM.YYYY')"+
                    "           and p.date_time_created <=to_date('"+dateTo+"','DD.MM.YYYY')";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Меню - таблица - "Группы товаров" по всем предприятиям"
            { //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);
            List<Object[]> queryList = query.getResultList();
            List<ProductHistoryJSON> returnList = new ArrayList<>();
            for(Object[] obj:queryList){
                ProductHistoryJSON doc=new ProductHistoryJSON();

                doc.setId(Long.parseLong(                     obj[0].toString()));
                doc.setDepartment((String)                    obj[1]);
                doc.setDate_time_created((String)             obj[2]);
                doc.setDocName((String)                       obj[3]);
                doc.setDocId(Long.parseLong(                  obj[4].toString()));
                doc.setDocTypeId((Integer)                    obj[5]);
                doc.setQuantity((BigDecimal)                  obj[6]);
                doc.setChange((BigDecimal)                    obj[7]);
                doc.setLast_purchase_price((BigDecimal)       obj[8]);
                doc.setAvg_purchase_price((BigDecimal)        obj[9]);
                doc.setAvg_netcost_price((BigDecimal)         obj[10]);
                doc.setLast_operation_price((BigDecimal)      obj[11]);
                returnList.add(doc);
            }
            return returnList;
        } else return null;
    }


    @Transactional
    public Products getProduct(Long Id){
        EntityManager em = emf.createEntityManager();
        Products response = em.find(Products.class, Id);
        em.close();
        return response;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean copyProducts (UniversalForm request) {
        if (securityRepositoryJPA.userHasPermissions_OR(14L, "163,164"))//  Группы товаров : "Создание"
        {
            int numCopies = request.getId1()>4L?1000:(request.getId1().intValue());  // количество копий. Проверка на случай если пошлют более 5
            try
            {
                for (int i = 0; i < numCopies; i++) { //цикл по заданному количеству копий.
                    if(!copyProduct(request,i+1)){
                        return false;
                    }
                } return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean copyProduct (UniversalForm request, int countCopy) {
        Long origProductId = request.getId();   // id товара
        Long copyBarcode = request.getId4();  // штрих-код: 1-оставить пустым, 2-как в оригинале
        Products response = getProduct(origProductId);
        response.setProductCategories(getCategoriesSetByProductId(origProductId));
        try {
            Long newProductId = copyProducts_createBaseDocument(response, request, countCopy);
            //копирование картинок
            Set<Long> imagesIds = getProductsImagesSetIdsByProductId(origProductId);
            if (imagesIds.size() > 0) {//если есть картинки
                UniversalForm universalForm = new UniversalForm();
                universalForm.setId1(newProductId);
                universalForm.setSetOfLongs1(imagesIds);
                addImagesToProduct(universalForm);
            }
            //копирование поставщиков
            List<ProductCagentsJSON> listOfProductCagents = getListOfProductCagents(origProductId);
            for (ProductCagentsJSON val : listOfProductCagents) {
                addCagentToProduct(val, newProductId);
            }
            //копирование штрих-кодов
            if (copyBarcode == 2L) {
                List<ProductBarcodesJSON> listOfProductBarcodes = getListOfProductBarcodes(origProductId);
                for (ProductBarcodesJSON val : listOfProductBarcodes) {
                    addBarcodeToProduct(val, newProductId);
                }
            }
            //копирование доп. полей
            List<Object[]> listOfProductFields = getListOfProductFields(origProductId);
            for (Object[] val : listOfProductFields) {
                //            Long product_id,       Long field_id,                    String value
                createCustomField(newProductId, Long.parseLong(String.valueOf(val[1])), String.valueOf(val[2]));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("Duplicates")//СОздание базового документа (поля+категории). Остальное (поставщики, штрихкоды, картинки) прицепляется после его создания в copyProducts
    @Transactional//первым параметром передаём сам объект документа с категориями, вторым - опции копирования
    public Long copyProducts_createBaseDocument (Products response,UniversalForm request, int countCopy) {
        Long copyArticle    =request.getId2();  // артикул: 1-копировать, 2-не копировать
        Long copyCode       =request.getId3();  // код: 1-оставить пустым, 2-как в оригинале, 3-присвоить новый
        try
        {
            //СОЗДАЛИ НОВЫЙ ДОКУМЕНТ Товары и услуги (НО ПОКА БЕЗ СЕТОВ И ПОЛЕЙ)
            Products newDocument = new Products();
            //создатель
            User creator = userRepository.getUserByUsername(userRepository.getUserName());
            newDocument.setCreator(creator);//создателя
            //владелец
            User master = userRepository.getUserByUsername(
                    userRepositoryJPA.getUsernameById(
                            userRepositoryJPA.getUserMasterIdByUsername(
                                    userRepository.getUserName())));
            newDocument.setMaster(master);
            //дата и время создания
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            newDocument.setDate_time_created(timestamp);//
            //предприятие
            newDocument.setCompany(response.getCompany());
            //Наименование
            newDocument.setName(response.getName() == null ? "" : response.getName()+" (копия "+countCopy+")");
            //дополнительная информация
            newDocument.setDescription(response.getDescription());
            //признак предмета расчёта
            newDocument.setPpr(response.getPpr());
            //НДС
            newDocument.setNds(response.getNds());
            //артикул
            if(copyArticle==1L) {newDocument.setArticle(response.getArticle());} else newDocument.setArticle(null);
            //код// код: 1-присвоить новый, 2-как в оригинале, 3-оставить пустым
            if(copyCode==3){
                newDocument.setProduct_code_free(null);
            } else if(copyCode==1){
                newDocument.setProduct_code_free(generateFreeProductCode(response.getCompany().getCompId()));
            } else
                newDocument.setProduct_code_free(response.getProduct_code_free());
            //группа товаров
            newDocument.setProductGroup(response.getProductGroup());
            //единица измерения
            newDocument.setEdizm(response.getEdizm());
            //весовой товар
            newDocument.setBy_weight(response.getBy_weight());
            //весовой код
            newDocument.setProduct_code(response.getProduct_code());
            //маркированный товар
            newDocument.setMarkable(response.getMarkable());
            //группа маркированных товаров
            newDocument.setMarkable_group(response.getMarkable_group());
            //вес
            newDocument.setWeight(response.getWeight());
            //ед. изм. веса
            newDocument.setWeight_edizm(response.getWeight_edizm());
            //объём
            newDocument.setVolume(response.getVolume());
            //ед. изм. объёма
            newDocument.setVolume_edizm(response.getVolume_edizm());
            //товар не закупается
            newDocument.setNot_buy(response.getNot_buy());
            //товар снят с продажи
            newDocument.setNot_sell(response.getNot_sell());

            newDocument.setProductCategories(response.getProductCategories());

            newDocument.setImages(response.getImages());

            entityManager.persist(newDocument);

            entityManager.flush();

            return newDocument.getId();//временно

        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Transactional// тут не надо прописывать права, т.к. это сервисный запрос
    @SuppressWarnings("Duplicates")
    public List getProductsList(String searchString, int companyId, int departmentId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id, " +
                "           p.name as name, " +
                "           p.nds_id as nds_id, " +
                "           coalesce(p.edizm_id,0) as edizm_id, " +
                "           f.name as filename "+
                "           from products p " +
                "           left outer join" +
                "           product_barcodes pb " +
                "           on pb.product_id=p.id" +
                "           left outer join" +
                "           files f " +
                "           on f.id=(select file_id from product_files where product_id=p.id and output_order=1)" +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_archive,false) !=true ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " upper(p.name) like upper('%" + searchString + "%') or "+
                    " upper(p.article) like upper ('%" + searchString + "%') or "+
                    " to_char(p.product_code_free,'fm0000000000') = '"+searchString+"' or "+
                    " pb.value = '"+searchString+"'";
            stringQuery = stringQuery + ")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id,f.name  order by p.name asc";
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Object[]> queryList = query.getResultList();
        List<ProductsListJSON> returnList = new ArrayList<>();
        for(Object[] obj:queryList){
            ProductsListJSON product=new ProductsListJSON();
            product.setId(Long.parseLong(           obj[0].toString()));
            product.setName((String)                obj[1]);
            product.setNds_id((Integer)             obj[2]);
            product.setEdizm_id(Long.parseLong(     obj[3].toString()));
            product.setFilename((String)            obj[4]);
            returnList.add(product);
        }
        return returnList;
    }


    @SuppressWarnings("Duplicates") //отдает информацию состоянии товара (кол-во, последняя поставка) в отделении, и средним ценам (закупочной и себестоимости) товара
    public ShortInfoAboutProductJSON getShortInfoAboutProduct(Long department_id, Long product_id) {

        Long myMasterId=userRepositoryJPA.getMyMasterId();
        String myTimeZone = userRepository.getUserTimeZone();
        String stringQuery="select" +
                "           p.quantity as quantity," +
                "           p.change as change," +
                "           p.avg_purchase_price as avg_purchase_price," +
                "           p.last_purchase_price as last_purchase_price," +
                "           p.avg_netcost_price as avg_netcost_price," +
                "           to_char(p.date_time_created at time zone '"+myTimeZone+"', 'DD.MM.YYYY') as date_time_created " +
                "           from" +
                "           products_history p" +
                "           where" +
                "               p.department_id= " + department_id +
                "           and p.product_id= "  + product_id +
                "           and p.master_id= "   + myMasterId +
                "           order by p.id desc limit 1";

        try{
            Query query = entityManager.createNativeQuery(stringQuery);

            List<Object[]> queryList = query.getResultList();
            ShortInfoAboutProductJSON returnObj=new ShortInfoAboutProductJSON();

            for(Object[] obj:queryList){
                returnObj.setQuantity((BigDecimal)                obj[0]);
                returnObj.setChange((BigDecimal)                  obj[1]);
                returnObj.setAvg_purchase_price((BigDecimal)      obj[2]);
                returnObj.setLast_purchase_price((BigDecimal)     obj[3]);
                returnObj.setAvg_netcost_price((BigDecimal)       obj[4]);
                returnObj.setDate_time_created((String)           obj[5]);
            }
            return returnObj;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    //*****************************************************************************************************************************************************
//***********************************************   C A T E G O R I E S   *****************************************************************************
//*****************************************************************************************************************************************************
    @SuppressWarnings("Duplicates")
    private Set<ProductCategories> getCategoriesSetByProductId(Long productId) {
        Set<Long> categories=getProductsCategoriesSetIdsByProductId(productId);
        Set<ProductCategories> setCategoriesOfProduct = getCategoriesSetBySetOfCategoriesId(categories);
        return setCategoriesOfProduct;
    }

    @SuppressWarnings("Duplicates") //права не нужны т.к. private, не вызывается по API
    private Set<ProductCategories> getCategoriesSetBySetOfCategoriesId(Set<Long> categories) {
        EntityManager em = emf.createEntityManager();
        Set<ProductCategories> categoriesSet = new HashSet<>();
        for (Long i : categories) {
            categoriesSet.add(em.find(ProductCategories.class, i));
        }
        return categoriesSet;
    }

    //права не нужны т.к. не вызывается по API, только из контроллера
    public Set<Long> getProductsCategoriesSetIdsByProductId (Long id) {
        String stringQuery="select p.category_id from product_productcategories p where p.product_id= "+id;
        List<Integer> depIds = entityManager.createNativeQuery(stringQuery).getResultList();
        Set<Long> categoriesSet = new HashSet<>();
        for (Integer i : depIds) {categoriesSet.add(Long.valueOf(i));}
        //иначе в categoriesSet один хрен попадают Integer'ы, хоть как кастуй, и здравствуй, java.lang.Integer cannot be cast to java.lang.Long в getCategoriesSetBySetOfCategoriesId
        return categoriesSet;
    }

    //права не нужны т.к. не вызывается по API, только из контроллера
    public List<Integer> getProductsCategoriesIdsByProductId(Long id) {
        String stringQuery="select p.category_id from product_productcategories p where p.product_id= "+id;
        Query query = entityManager.createNativeQuery(stringQuery);
        List<Integer> depIds = query.getResultList();
        return depIds;
    }

    @Transactional//права не нужны т.к. не вызывается по API, только из контроллера
    @SuppressWarnings("Duplicates") //возвращает набор деревьев категорий по их корневым id
    public List<ProductCategories> getProductCategoriesTrees(List<Integer> rootIds) {
        List<ProductCategories> returnTreesList = new ArrayList<ProductCategories>();
        String stringQuery;
        stringQuery = "from ProductCategories p ";
        stringQuery = stringQuery + " left join fetch p.children";
        entityManager.createQuery(stringQuery, ProductCategories.class).getResultList();
        for(int rootId : rootIds) {
            returnTreesList.add(entityManager.find(ProductCategories.class, Long.valueOf(rootId)));
        }
        return returnTreesList;
    }

    //права на просмотр документов в таблице меню
    @SuppressWarnings("Duplicates") //отдает только найденные категорий, без иерархии
    public List<ProductCategoriesTableJSON> searchProductCategory(Long companyId, String searchString) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))// Меню - таблица
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from product_categories " +
                    " where company_id ="+companyId+" and master_id="+ myMasterId+ " and upper(name) like upper('%"+searchString+"%')";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public Long insertProductCategory(ProductCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "171,172"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompanyId());//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(14L, "171") &&
                    Long.valueOf(myCompanyId) != request.getCompanyId()) || DocumentMasterId != myMasterId )
            {
                return null;
            }
            else {
                String stringQuery;
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                Long myId = userRepository.getUserId();
                stringQuery = "insert into product_categories (" +
                        "name," +
                        "master_id," +
                        "creator_id," +
                        "parent_id," +
                        "company_id," +
                        "date_time_created" +
                        ") values ( " +
                        "'" + request.getName() + "', " +
                        myMasterId + "," +
                        myId + "," +
                        (request.getParentCategoryId() > 0 ? request.getParentCategoryId() : null) + ", " +
                        request.getCompanyId() + ", " +
                        "(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')))";
                try {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.executeUpdate() == 1) {
                        stringQuery = "select id from product_categories where date_time_created=(to_timestamp('" + timestamp + "','YYYY-MM-DD HH24:MI:SS.MS')) and creator_id=" + myId;
                        Query query2 = entityManager.createNativeQuery(stringQuery);
                        return Long.valueOf(Integer.parseInt(query2.getSingleResult().toString()));
                    } else return (0L);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0L;
                }
            }
        } else return 0L;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateProductCategory(ProductCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(14L,"173,174"))//  "Редактирование категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            Long changer = userRepository.getUserIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "update product_categories set " +
                    " name='" + request.getName()+"', "+
                    " date_time_changed= now()," +
                    " changer_id= " + changer +
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав по всем предприятиям
            {
//            остается только на своё предприятие (140)
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
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
    public boolean deleteProductCategory(ProductCategoriesForm request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "175,176"))// "Удаление категорий"
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "delete from product_categories "+
                    " where id=" + request.getCategoryId()+
                    " and master_id="+myMasterId ;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "175")) //Если нет прав по всем предприятиям
            {
                //остается только на своё предприятие
                int myCompanyId = userRepositoryJPA.getMyCompanyId();
                stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
            }
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
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
    public boolean saveChangeCategoriesOrder(List<ProductCategoriesForm> request)
    {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))// редактирование своих или чужих предприятий
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            User changer = userRepository.getUserByUsername(userRepository.getUserName());
            String stringQuery;
            try
            {
                for (ProductCategoriesForm field : request)
                {
                    stringQuery = "update product_categories set " +
                            " output_order=" + field.getOutput_order() +
                            " where id=" + field.getId() +
                            " and master_id=" + myMasterId;
                    if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав по всем предприятиям
                    {
//            остается только на своё предприятие
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

    @SuppressWarnings("Duplicates") //возвращает id корневых категорий
    public List<Integer> getCategoriesRootIds(Long id) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Меню - таблица
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select id from product_categories " +
                    "  where company_id ="+id+" and master_id="+ myMasterId+" and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на просмотр доков по всем предприятиям
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
//    отдает только список корневых категорий, без детей
//    нужно для изменения порядка вывода корневых категорий
    public List<ProductCategoriesTableJSON> getRootProductCategories(Long companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))//"Контрагенты" (см. файл Permissions Id)
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from product_categories " +
                    "  where company_id ="+companyId+" and master_id="+ myMasterId+" and parent_id is null ";
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            stringQuery = stringQuery + " order by output_order";
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            return query.getResultList();
        }else return null;
    }

    @SuppressWarnings("Duplicates") //отдает только список детей, без их детей
    public List<ProductCategoriesTableJSON> getChildrensProductCategories(Long parentId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "173,174"))//редактирование категорий
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select " +
                    " id as id," +
                    " name as name," +
                    " parent_id as parent_id," +
                    " output_order as output_order" +
                    " from product_categories " +
                    " where parent_id ="+parentId+" and master_id="+ myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "173")) //Если нет прав на редактирование категорий по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, ProductCategoriesTableJSON.class);
            return query.getResultList();
        } else return null;
    }


//*****************************************************************************************************************************************************
//***********************************************   Product Custom Fields   ***************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean updateProductCustomFields(List<ProductCustomFieldsSaveForm> request) {
        //log.info("in updateProductCustomFields class");
        if(request.size()>0) { //если поля на сохранение есть
            Long productId=request.get(0).getProduct_id();//за id товара берем productId из первого же объекта (т.к. они ДОЛЖНЫ быть все одинаковы)
            //log.info("productId="+productId.toString());
            //log.info("Поля на сохранение есть ");
            for (ProductCustomFieldsSaveForm custumField : request)
            {
                if(!productId.equals(custumField.getProduct_id())) {
                    //log.info("В листе не одинаковые productId!");
                    return false; //проверяю что в листе все productId одинаковые
                }
            }
            //log.info("Прошли цикл, перед проверкой прав.");
            // проверка на то, что все поля принадлежат к тем документам Товары, на которые есть соответствующие права:
            //Если есть право на "Изменение по всем предприятиям" и все id для изменения принадлежат владельцу аккаунта (с которого изменяют), ИЛИ
            if ((securityRepositoryJPA.userHasPermissions_OR(14L, "169") && securityRepositoryJPA.isItAllMyMastersDocuments("products", productId.toString())) ||
                    //Если есть право на "Изменение по своему предприятияю" и все id для изменения принадлежат владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                    (securityRepositoryJPA.userHasPermissions_OR(14L, "170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products", productId.toString())))
            {
                //log.info("Права есть!");
                try {
                    for (ProductCustomFieldsSaveForm custumField : request) {
                        //log.info("В цикле: поле "+custumField.getName());
                        if (isThereThisField(custumField.getProduct_id(), custumField.getId())) { // если поле уже есть в product_fields
                            //log.info("поле уже есть в product_fields");
                            updateCustomField(custumField.getProduct_id(), custumField.getId(), custumField.getValue()); //то апдейтим
                        } else {
                            //log.info("поля нет в product_fields");
                            if (custumField.getValue() != null && !custumField.getValue().isEmpty() && custumField.getValue().trim().length() > 0) {
                                //если поля нет в product_fields, и в нём есть текст, то инсертим (чтобы пустые строки в product_fields не разводить)
                                //log.info("поля нет в product_fields, и в нём есть текст - инсертим");
                                createCustomField(custumField.getProduct_id(), custumField.getId(), custumField.getValue()); // нет - то инсертим
                                //log.info("после инсерта");
                            }
                        }
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    //log.error("ERROR: ", e);
                    return false;
                }
            } else {
                //log.info("НЕТ ПРАВ! ");
                return false;}
        } else return true;// тут true чтобы не было ошибки в консоли браузера.
    }

    private List<Object[]> getListOfProductFields (Long productId){
        List<Object[]> a;
        String stringQuery = "select  product_id as product_id, field_id as id, field_value as value, '1' as name, '1' as parent_set_id from product_fields p where p.product_id="+productId;
        Query query = entityManager.createNativeQuery(stringQuery);
        a=query.getResultList();
        return a;
    }

    public boolean isThereThisField (Long product_id, Long field_id){
        String stringQuery = "select 1 from product_fields p where p.product_id="+product_id+" and p.field_id =" +field_id;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    @SuppressWarnings("Duplicates")
    public boolean updateCustomField(Long product_id, Long field_id, String value){
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "update product_fields set " +
                " field_value='" + value +"'"+
                " where product_id=" + product_id+
                " and field_id="+field_id+
                " and (select master_id from products where id="+product_id+") = "+myMasterId; //для безопасности, чтобы не кидали json на авось у кого-то что-то проапдейтить
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            int i = query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @SuppressWarnings("Duplicates")
    public boolean createCustomField (Long product_id, Long field_id, String value){
        String stringQuery;
        stringQuery = "insert into product_fields (product_id, field_id, field_value) values ("+product_id+","+field_id+",'"+value+"')";
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            if(query.executeUpdate()==1) {
                return true;
            }else{
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<ProductGroupFieldTableJSON> getProductGroupFieldsListWithValues(int field_type, int productId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168,169,170"))//просмотр или редактирование (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());


            stringQuery = "select " +
                    " pgf.id as id, " +
                    " pgf.name as name, " +
                    " pgf.description as description, " +
                    " pgf.field_type as field_type, " +
                    " pgf.group_id as group_id, " +
                    " pgf.output_order as output_order, " +
                    //field_type: 1 - сеты (наборы) полей, 2 - поля
                    (field_type==1?"''":"(select coalesce (pf.field_value,'') from product_fields pf where pf.field_id=pgf.id and pf.product_id=p.id)")+" as value, "+
                    " pgf.parent_set_id as parent_set_id " +
                    " from  " +
                    " product_group_fields pgf," +
                    " product_groups pg," +
                    " products p " +
                    " where pgf.group_id=pg.id " +
                    " and p.group_id=pg.id " +
                    " and pgf.field_type = " +field_type+// тип: 1 - сеты (наборы) полей, 2 - поля
                    " and p.id=" + productId +
                    " and pgf.master_id=" + myMasterId;


            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167,169")) //Если нет прав на просм. или редактир. по всем предприятиям"
            {
                //остается только на своё предприятие
                stringQuery = stringQuery + " and pgf.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery + " order by pgf.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery, ProductGroupFieldTableJSON.class);
            return query.getResultList();
        } else return null;
    }


//*****************************************************************************************************************************************************
//******************************************************   C A G E N T S    ***************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addCagentsToProduct(UniversalForm request){
        String stringQuery;
        Set<Long> Ids = request.getSetOfLongs1();
        Long prouctId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",prouctId.toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",prouctId.toString())))
        {
            try
            {
                for (Long Id : Ids) {

                    stringQuery = "select product_id from product_cagents where product_id=" + prouctId + " and cagent_id=" + Id;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (query.getResultList().size() == 0) {//если таких поставщиков еще нет у товара
                        entityManager.close();
                        manyToMany_productId_CagentId(prouctId,Id);
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
    @Transactional//права не нужны, внутренниЙ вызов
    @SuppressWarnings("Duplicates")
    boolean manyToMany_productId_CagentId(Long prouctId, Long cagentId){
        try
        {
            entityManager.createNativeQuery("" +
                    "insert into product_cagents " +
                    "(product_id,cagent_id,output_order) " +
                    "values " +
                    "(" + prouctId + ", " + cagentId + " , (select coalesce(max(output_order)+1,1) from product_cagents where product_id=" + prouctId + "))")
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

    @Transactional//права не нужны, внутренниЙ вызов
    @SuppressWarnings("Duplicates") //используется при копировании (создании дубликата) документа
    boolean addCagentToProduct(ProductCagentsJSON request,Long newProductId){
        try
        {
            entityManager.createNativeQuery("" +
                    "insert into product_cagents " +
                    "(product_id, cagent_id, output_order, cagent_article, additional) " +
                    "values " +
                    "(" + newProductId + ", " +
                    request.getCagent_id() +
                    " , (select coalesce(max(output_order)+1,1) from product_cagents where product_id=" +newProductId + "), '"+
                    request.getCagent_article()+"', '"+request.getAdditional()+"')")

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



    @SuppressWarnings("Duplicates") //отдает информацию по поставщикам товара
    public List<ProductCagentsJSON> getListOfProductCagents(Long productId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            List<ProductCagentsJSON> returnlist;
            String stringQuery = "select" +
                    "           c.id as cagent_id," +
                    "           f.product_id as product_id," +
                    "           c.name as name," +
                    "           f.output_order as output_order," +
                    "           f.cagent_article as cagent_article," +
                    "           f.additional as additional" +
                    "           from" +
                    "           product_cagents f" +
                    "           inner join" +
                    "           products p" +
                    "           on f.product_id=p.id" +
                    "           inner join" +
                    "           cagents c" +
                    "           on f.cagent_id=c.id" +
                    "           where" +
                    "           f.product_id= " + productId +
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by f.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery, ProductCagentsJSON.class);
            returnlist = query.getResultList();
            return returnlist;
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateProductCagentProperties(UniversalForm request){
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",request.getId2().toString())))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                String stringQuery;
                stringQuery = "Update product_cagents p" +
                        " set  cagent_article= '" + (request.getString1() != null ? request.getString1() : "")+"'"+
                        "    , additional= '" + (request.getString2() != null ? request.getString2() : "")+"'"+
                        " where (select master_id from cagents where id=p.cagent_id)="+ myMasterId + //контроль того, что лицо, имеющее доступ к редактированию документа, не может через сторонние сервисы типа postman изменить документы других аккаунтов
                        " and p.cagent_id=" + request.getId1() +
                        " and p.product_id=" + request.getId2();
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductCagent(UniversalForm request)
    {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",request.getId2().toString())))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery  =  " delete from product_cagents "+
                    " where product_id=" + request.getId2()+
                    " and cagent_id="+request.getId1()+
                    " and (select master_id from products where id="+request.getId2()+")="+myMasterId ;
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





//*****************************************************************************************************************************************************
//****************************************************   I  M  A  G  E  S   ***************************************************************************
//*****************************************************************************************************************************************************

    @SuppressWarnings("Duplicates")
    //права не нужны т.к. не вызывается по API, только из контроллера
    public Set<Long> getProductsImagesSetIdsByProductId (Long id) {
        String stringQuery="select p.file_id from product_files p where p.product_id= "+id;
        List<Integer> depIds = entityManager.createNativeQuery(stringQuery).getResultList();
        Set<Long> categoriesSet = new HashSet<>();
        for (Integer i : depIds) {categoriesSet.add(Long.valueOf(i));}
        return categoriesSet;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public boolean addImagesToProduct(UniversalForm request){
        Long prouctId = request.getId1();
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",prouctId.toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",prouctId.toString())))
        {
            try
            {
                String stringQuery;
                Set<Long> filesIds = request.getSetOfLongs1();
                for (Long fileId : filesIds) {

                    stringQuery = "select product_id from product_files where product_id=" + prouctId + " and file_id=" + fileId;
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if (fileIsImage(fileId) && query.getResultList().size() == 0) {//если таких картинок еще нет у товара, и файл является картинкой
                        entityManager.close();
                        manyToMany_productId_FileId(prouctId,fileId);
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
    boolean manyToMany_productId_FileId(Long prouctId, Long fileId){
        try
        {
            entityManager.createNativeQuery(" " +
                    "insert into product_files " +
                    "(product_id,file_id,output_order) " +
                    "values " +
                    "(" + prouctId + ", " + fileId + " , (select coalesce(max(output_order)+1,1) from product_files where product_id=" + prouctId + "))")
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

    private boolean fileIsImage(Long fileId){
        return true;
    }

    @SuppressWarnings("Duplicates") //отдает информацию по картинкам товара (fullSize - полным, нет - по их thumbnails)
    public List<FilesProductImagesJSON> getListOfProductImages(Long productId, boolean fullSize) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId=userRepositoryJPA.getMyMasterId();
            String stringQuery="select" +
                    "           f.id as id," +
                    "           f.date_time_created as date_time_created," +
                    "           f.name as name," +
                    "           f.original_name as original_name," +
                    "           pf.output_order as output_order" +
                    "           from" +
                    "           products p" +
                    "           inner join" +
                    "           product_files pf" +
                    "           on p.id=pf.product_id" +
                    "           inner join" +
                    "           files f" +
                    "           on pf.file_id=f.id" +
                    "           where" +
                    "           p.id= " + productId +
                    "           and p.master_id=" + myMasterId +
                    "           and f.trash is not true"+
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by pf.output_order asc ";
            Query query = entityManager.createNativeQuery(stringQuery, FilesProductImagesJSON.class);
            return query.getResultList();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductImage(SearchForm request)
    {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",request.getAny_id().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",request.getAny_id().toString())))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from product_files "+
                    " where product_id=" + request.getAny_id()+
                    " and file_id="+request.getId()+
                    " and (select master_id from products where id="+request.getAny_id()+")="+myMasterId ;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                int i = query.executeUpdate();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }



//*****************************************************************************************************************************************************
//****************************************************   B  A  R  C  O  D  E  S   *********************************************************************
//*****************************************************************************************************************************************************

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean insertProductBarcode(UniversalForm request){
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",request.getId2().toString())))
        {
            try
            {
                entityManager.createNativeQuery("" +
                        "insert into product_barcodes " +
                        "(product_id,barcode_id,value, description) " +
                        "values " +
                        "(" + request.getId2() + " , " +
                        request.getId3() + " , " +
                        (request.getString1() != null ? ("'"+request.getString1()+"'") : "''")+ " , " +
                        (request.getString2() != null ? ("'"+request.getString2()+"'") : "''")+")")
                        .executeUpdate();
                entityManager.close();
                return true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return false;
            }
        } else return false;
    }

    @SuppressWarnings("Duplicates") //отдает информацию по штрих-кодам товара
    public List<ProductBarcodesJSON> getListOfProductBarcodes(Long productId) {
        if(securityRepositoryJPA.userHasPermissions_OR(14L, "167,168"))//Просмотр документов
        {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            List<ProductBarcodesJSON> returnlist;
            String stringQuery = "select" +
                    "           pb.id as id," +
                    "           pb.barcode_id as barcode_id," +
                    "           pb.product_id as product_id," +
                    "           b.name as name," +
                    "           pb.value as value," +
                    "           pb.description as description" +
                    "           from" +
                    "           product_barcodes pb" +
                    "           inner join" +
                    "           sprav_sys_barcode b" +
                    "           on b.id=pb.barcode_id" +
                    "           inner join" +
                    "           products p" +
                    "           on p.id=pb.product_id" +
                    "           where" +
                    "           pb.product_id= " + productId +
                    "           and p.master_id= " + myMasterId;
            if (!securityRepositoryJPA.userHasPermissions_OR(14L, "167")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (168)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            stringQuery = stringQuery+" order by pb.id asc ";
            Query query = entityManager.createNativeQuery(stringQuery, ProductBarcodesJSON.class);
            returnlist = query.getResultList();
            return returnlist;
        }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateProductBarcode(UniversalForm request){
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",getProductIdByBarcodeId(request.getId1()))) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",getProductIdByBarcodeId(request.getId1()))))
        {
            try
            {
                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
                String stringQuery;
                stringQuery = "Update product_barcodes p" +
                        " set  value= '" + (request.getString1() != null ? request.getString1() : "")+"'"+
                        "    , description= '" + (request.getString2() != null ? request.getString2() : "")+"'"+
                        " where p.id=" + request.getId1() +
                        " and (select master_id from products where id=p.product_id)="+ myMasterId; //контроль того, что лицо, имеющее доступ к редактированию документа, не может через сторонние сервисы типа postman изменить документы других аккаунтов
                entityManager.createNativeQuery(stringQuery).executeUpdate();
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteProductBarcode(UniversalForm request)
    {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",request.getId2().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",request.getId2().toString())))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            //int myCompanyId = userRepositoryJPA.getMyCompanyId();
            stringQuery  =  " delete from product_barcodes "+
                    " where id=" + request.getId1()+
                    " and (select master_id from products where id="+request.getId2()+")="+myMasterId;
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
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
    public Integer generateWeightProductCode(UniversalForm request)
    {
        //Если есть право на "Изменение по всем предприятиям" и id товара принадлежит владельцу аккаунта (с которого изменяют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(14L,"169") && securityRepositoryJPA.isItAllMyMastersDocuments("products",request.getId1().toString())) ||
                //Если есть право на "Изменение по своему предприятияю" и id товара принадлежит владельцу аккаунта (с которого изменяют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(14L,"170") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("products",request.getId1().toString())))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            String timestamp = new Timestamp(System.currentTimeMillis()).toString();


            stringQuery = "update products set " +
                    " product_code=(select coalesce(max(product_code)+1,1) from products where company_id="+request.getId2()+" and master_id="+myMasterId+")"+
                    " where id=" + request.getId1()+
                    " and master_id=" + myMasterId +
                    " and company_id=" + request.getId2()+
                    " and product_code is null";
            try
            {
                Query query = entityManager.createNativeQuery(stringQuery);
                if(query.executeUpdate()==1){
                    stringQuery="select product_code from products where id="+request.getId1();
                    Query query2 = entityManager.createNativeQuery(stringQuery);
                    return Integer.parseInt(query2.getSingleResult().toString());
                } else return(0);
            }
            catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else return 0;
    }

    @SuppressWarnings("Duplicates")  //права не нужны, внутренний вызов
    private Long generateFreeProductCode(Long company_id)
    {
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "select coalesce(max(product_code_free)+1,1) from products where company_id="+company_id+" and master_id="+myMasterId;
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

    @SuppressWarnings("Duplicates")
    public Boolean isProductCodeFreeUnical(UniversalForm request)
    {
        Long company_id=request.getId1();
        Long code=request.getId2();
        Long product_id=request.getId3();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery;
        stringQuery = "" +
                "select id from products where " +
                "company_id="+company_id+
                " and master_id="+myMasterId+
                " and product_code_free="+code;
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

    @SuppressWarnings("Duplicates")
    public Object getProductBarcodesPrefixes(UniversalForm request){
        Long company_id=request.getId1();
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        String stringQuery = "select " +
                " st_prefix_barcode_pieced as st_prefix_barcode_pieced, " +//штучный
                " st_prefix_barcode_packed as st_prefix_barcode_packed " + //весовой
                " from companies where id="+company_id+" and master_id="+myMasterId;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getSingleResult();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("Duplicates") //права не нужны, внутренний вызов
    private String getProductIdByBarcodeId(Long barcodeIdKey){ //да, мне тут нужен стринг

        String stringQuery = "select product_id from product_barcodes where id="+barcodeIdKey;
        try
        {
            Query query = entityManager.createNativeQuery(stringQuery);
            return query.getSingleResult().toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional//права не нужны, внутренниЙ вызов
    @SuppressWarnings("Duplicates") //используется при копировании (создании дубликата) документа
    boolean addBarcodeToProduct(ProductBarcodesJSON request,Long newProductId){
        try
        {
            entityManager.createNativeQuery("" +
                    "insert into product_barcodes " +
                    "(product_id, barcode_id, value, description) " +
                    "values " +
                    "(" + newProductId + ", " +
                    request.getBarcode_id() + ", '" +
                    request.getValue() + "', '" +
                    request.getDescription()+"')").executeUpdate();
            entityManager.close();
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }


}
