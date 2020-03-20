package com.laniakea.repository;

import com.laniakea.message.request.CagentCategoriesForm;
import com.laniakea.message.request.CagentsForm;
import com.laniakea.message.response.CagentCategoriesTableJSON;
import com.laniakea.message.response.CagentsJSON;
import com.laniakea.message.response.CagentsListJSON;
import com.laniakea.message.response.CagentsTableJSON;
import com.laniakea.model.CagentCategories;
import com.laniakea.model.Cagents;
import com.laniakea.model.Companies;
import com.laniakea.model.Sprav.SpravSysOPF;
import com.laniakea.model.User;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

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
    private UserDetailsServiceImpl userService;



    @Transactional
    @SuppressWarnings("Duplicates")
    public List<CagentsTableJSON> getCagentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId, int categoryId) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

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
                    "           to_char(p.date_time_created, 'DD.MM.YYYY HH24:MI') as date_time_created, " +
                    "           to_char(p.date_time_changed, 'DD.MM.YYYY HH24:MI') as date_time_changed, " +
                    "           p.description as description " +
                    "           from cagents p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf_id=sso.id " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                    (categoryId!=0?" and p.id in (select ccc.cagent_id from cagent_cagentcategories ccc where ccc.category_id="+categoryId+") ":"");

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Меню - таблица - "Контрагенты" по всем предприятиям"
            {
                //остается только на своё предприятие (110)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or "+
                        "upper(p.description) like upper('%" + searchString + "%')"+")";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }

            stringQuery = stringQuery + " order by " + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, CagentsTableJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            return query.getResultList();
        } else return null;
    }
    @SuppressWarnings("Duplicates")
    @Transactional
    public int getCagentsSize(String searchString, int companyId, int categoryId) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id " +
                    "           from cagents p " +
                    "           where  p.master_id=" + myMasterId +
                    "           and coalesce(p.is_archive,false) !=true " +
                    (categoryId!=0?" and p.id in (select ppg.cagent_id from cagent_cagentcategories ppg where ppg.category_id="+categoryId+") ":"");

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Меню - таблица - "Контрагенты" по всем предприятиям"
            {
                //остается только на своё предприятие (110)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and (" +
                        "upper(p.name) like upper('%" + searchString + "%') or "+
                        "upper(p.description) like upper('%" + searchString + "%')"+")";
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
    public CagentsJSON getCagentValues(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L, "133,134"))//"Контрагенты" (см. файл Permissions Id)
        {
            String stringQuery;
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select  p.id as id, " +
                    "           u.name as master, " +
                    "           p.name as name, " +
                    "           sso.name as opf, " +
                    "           sso.id as opf_id, " +
                    "           us.name as creator, " +
                    "           uc.name as changer, " +
                    "           p.master_id as master_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           cmp.name as company, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
                    "           p.description as description " +
                    "           from cagents p " +
                    "           INNER JOIN companies cmp ON p.company_id=cmp.id " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           LEFT OUTER JOIN sprav_sys_opf sso ON p.opf_id=sso.id " +
                    "           where p.id= " + id+
                    "           and  p.master_id=" + myMasterId;

            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на "Просмотр документов по всем предприятиям"
            {
                //остается только на своё предприятие (134)
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
            }

            Query query = entityManager.createNativeQuery(stringQuery, CagentsJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                CagentsJSON response = (CagentsJSON) query.getSingleResult();
                return response;}
            catch(NoResultException nre){return null;}
        } else return null;
    }


    @SuppressWarnings("Duplicates")
    public boolean updateCagents(CagentsForm request) {

        EntityManager emgr = emf.createEntityManager();
        Cagents document = emgr.find(Cagents.class, request.getId());//сохраняемый документ
        boolean userHasPermissions_OwnUpdate=securityRepositoryJPA.userHasPermissions_OR(12L, "136"); // "Редактирование док-тов своего предприятия"
        boolean userHasPermissions_AllUpdate=securityRepositoryJPA.userHasPermissions_OR(12L, "135"); // "Редактирование док-тов всех предприятий" (в пределах родительского аккаунта, конечно же)
        boolean updatingDocumentOfMyCompany=(Long.valueOf(userRepositoryJPA.getMyCompanyId()).equals(request.getCompany_id()));//сохраняется документ моего предприятия
        Long DocumentMasterId=document.getMaster().getId(); //владелец сохраняемого документа.
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());//владелец моего аккаунта
        boolean isItMyMastersDock =(DocumentMasterId.equals(myMasterId));

        if(((updatingDocumentOfMyCompany && (userHasPermissions_OwnUpdate || userHasPermissions_AllUpdate))//(если сохраняю документ своего предприятия и у меня есть на это права
                ||(!updatingDocumentOfMyCompany && userHasPermissions_AllUpdate))//или если сохраняю документ не своего предприятия, и есть на это права)
                && isItMyMastersDock) //и сохраняемый документ под юрисдикцией главного аккаунта
        {
            try
            {
                emgr.getTransaction().begin();

                document.setName          (request.getName() == null ? "": request.getName());
                document.setDescription   (request.getDescription() == null ? "": request.getDescription());

                //организационно-правовая форма предприятия
                document.setCagentOpf(emgr.find(SpravSysOPF.class, request.getOpf_id()));
                //категории
                Set<Long> categories = request.getSelectedCagentCategories();
                if (!categories.isEmpty()) { //если есть выбранные чекбоксы категорий
                    Set<CagentCategories> setCategoriesOfCagent= getCategoriesSetBySetOfCategoriesId(categories);
                    document.setCagentCategories(setCategoriesOfCagent);
                } else { // если ни один чекбокс категорий не выбран
                    document.setCagentCategories(null);
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
        } else return false;
    }

    @SuppressWarnings("Duplicates")
    @Transactional
    public Long insertCagent(CagentsForm request) {
        if(securityRepositoryJPA.userHasPermissions_OR(12L,"129,130"))//  Контрагенты : "Создание"
        {
            EntityManager emgr = emf.createEntityManager();
            Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
            Companies companyOfCreatingDoc = emgr.find(Companies.class, request.getCompany_id());//предприятие создаваемого документа
            Long DocumentMasterId=companyOfCreatingDoc.getMaster().getId(); //владелец предприятия создаваемого документа.
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            //(если на создание по всем предприятиям прав нет, а предприятие не своё) или пытаемся создать документ для предприятия не моего владельца
            if ((!securityRepositoryJPA.userHasPermissions_OR(12L, "129") &&
                    Long.valueOf(myCompanyId) != request.getCompany_id()) || DocumentMasterId != myMasterId )
            {
                return null;
            }
            else
            {
                try
                {
                    Cagents newDocument = new Cagents();
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
                    //организационно-правовая форма предприятия
                    newDocument.setCagentOpf(emgr.find(SpravSysOPF.class, request.getOpf_id()));

                    Set<Long> categories = request.getSelectedCagentCategories();
                    if (!categories.isEmpty()) {
                        Set<CagentCategories> setCategoriesOfCagent = getCategoriesSetBySetOfCategoriesId(categories);
                        newDocument.setCagentCategories(setCategoriesOfCagent);
                    }

                    entityManager.persist(newDocument);
                    entityManager.flush();
                    return newDocument.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteCagents(String delNumbers) {
        //Если есть право на "Удаление по всем предприятиям" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют), ИЛИ
        if((securityRepositoryJPA.userHasPermissions_OR(12L,"131") && securityRepositoryJPA.isItAllMyMastersDocuments("cagents",delNumbers)) ||
                //Если есть право на "Удаление по своему предприятияю" и все id для удаления принадлежат владельцу аккаунта (с которого удаляют) и предприятию аккаунта
                (securityRepositoryJPA.userHasPermissions_OR(12L,"132") && securityRepositoryJPA.isItAllMyMastersAndMyCompanyDocuments("cagents",delNumbers)))
        {
            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
            String stringQuery;
            stringQuery = "Update cagents p" +
                    " set is_archive=true " +
                    " where p.master_id=" +myMasterId+
                    " and p.id in (" + delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                int count = query.executeUpdate();
                return true;
            } else return false;
        } else return false;
    }

    //отдает список наименований контрагентов по поисковой подстроке и предприятию
    @Transactional// тут не надо прописывать права, т.к. это сервисный запрос
    @SuppressWarnings("Duplicates")
    public List getCagentsList(String searchString, int companyId) {
        String stringQuery;
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery = "select  p.id as id, " +
                "           p.name as name "+
                "           from cagents p " +
                "           where  p.master_id=" + myMasterId +
                "           and coalesce(p.is_archive,false) !=true ";
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and (" +
                    " upper(p.name) like upper('%" + searchString + "%') or "+
                    " upper(p.description) like upper ('%" + searchString + "%') ";
            stringQuery = stringQuery + ")";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " group by p.id order by p.name asc";
        Query query = entityManager.createNativeQuery(stringQuery);
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
                    " where company_id ="+companyId+" and master_id="+ myMasterId+ " and upper(name) like upper('%"+searchString+"%')";
            if (!securityRepositoryJPA.userHasPermissions_OR(12L, "133")) //Если нет прав на просмотр доков по всем предприятиям
            {//остается только на своё предприятие
                Integer myCompanyId = userRepositoryJPA.getMyCompanyId();// моё предприятие
                stringQuery = stringQuery + " and company_id=" + myCompanyId;
            }
            Query query = entityManager.createNativeQuery(stringQuery, CagentCategoriesTableJSON.class);
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
                    Long.valueOf(myCompanyId) != request.getCompanyId()) || DocumentMasterId != myMasterId )
            {
                return null;
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

                        "'"+request.getName()+"', "+
                        myMasterId+","+
                        myId+","+
                        (request.getParentCategoryId()>0?request.getParentCategoryId():null)+", "+
                        request.getCompanyId()+", "+
                        "(to_timestamp('"+timestamp+"','YYYY-MM-DD HH24:MI:SS.MS')))";
                try
                {
                    Query query = entityManager.createNativeQuery(stringQuery);
                    if(query.executeUpdate()==1){
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
                    " name='" + request.getName()+"', "+
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
    @SuppressWarnings("Duplicates")
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
}
