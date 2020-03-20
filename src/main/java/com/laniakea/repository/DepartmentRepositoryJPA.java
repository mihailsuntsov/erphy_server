package com.laniakea.repository;

import com.laniakea.message.request.DepartmentForm;
import com.laniakea.model.Departments;
import com.laniakea.message.response.DepartmentsJSON;
import com.laniakea.model.Sprav.SpravSysDepartmentsList;
import com.laniakea.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository("DepartmentRepositoryJPA")
public class DepartmentRepositoryJPA {

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



    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Departments> getDepartmentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId) {
        if(securityRepositoryJPA.userHasPermissions_OR(4L, "9,68,10"))// Отделения: "Меню - отделения всех предприятий","Меню - отделения только своего предприятия","Меню - только свои отделения"
        {
            String stringQuery;
            boolean needToSetParameter_MyDepthsIds= false;
            Long departmentOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

            stringQuery = "select " +
                    "           p.id as id, " +
                    "           p.name as name, " +
                    "           u.username as owner, " +
                    "           us.username as creator, " +
                    "           uc.username as changer, " +
                    "           p.master_id as owner_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           p.company_id as company_id, " +
                    "           p.parent_id as parent_id, " +
                    "           p.address as address, " +
                    "           p.additional as additional, " +
                    "           (select name from companies where id=p.company_id) as company, " +
                    "           (select count(*) from departments ds where ds.parent_id=p.id) as num_childrens," +
                    "           (select name from departments where id=p.parent_id) as parent, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
                    "           p.date_time_created as date_time_created_sort, " +
                    "           p.date_time_changed as date_time_changed_sort " +
                    "           from departments p " +
                    "           INNER JOIN users u ON p.master_id=u.id " +
                    "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                    "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                    "           where  p.master_id=" + departmentOwnerId +
                    "           and coalesce(p.is_archive,false) !=true";

            if (!securityRepositoryJPA.userHasPermissions_OR(4L, "9")) {//если нет прав на Отделения: "Меню - отделения всех предприятий"
                stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
            }
            if (!securityRepositoryJPA.userHasPermissions_OR(4L, "68")) {//если нет прав на Отделения: "Меню - отделения только своего предприятия"
                stringQuery = stringQuery + " and p.id in :myDepthsIds";//покажем только "Меню - только свои отделения", но с их родителями (чтобы можно было добраться до своего)
                needToSetParameter_MyDepthsIds= true;
            }
            if (searchString != null && !searchString.isEmpty()) {
                stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
            }
            if (companyId > 0) {
                stringQuery = stringQuery + " and p.company_id=" + companyId;
            }
            stringQuery = stringQuery + " and p.parent_id is null";
            stringQuery = stringQuery + " order by p." + sortColumn + " " + sortAsc;
            Query query = entityManager.createNativeQuery(stringQuery, DepartmentsJSON.class)
                    .setFirstResult(offsetreal)
                    .setMaxResults(result);

            if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
            {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsIdWithTheirParents());}

            return query.getResultList();
        } else return null;
    }

    @Transactional
    public Long insertDepartment(Departments department) {
        if(securityRepositoryJPA.userHasPermissions_OR(4L,"11"))//  Отделения : "Создание"
        {
            entityManager.persist(department);
            entityManager.flush();
            return department.getId();
        } else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public int getDepartmentsSize(String searchString, int companyId) {
        String stringQuery;
        boolean needToSetParameter_MyDepthsIds= false;
        Long departmentOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery = "select " +
                "           p.id as id " +
                "           from departments p " +
                "           where  p.master_id=" + departmentOwnerId +
                "           and coalesce(p.is_archive,false) !=true";

        if (!securityRepositoryJPA.userHasPermissions_OR(4L, "9")) {//если нет прав на Отделения: "Меню - отделения всех предприятий"
            stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();
        }
        if (!securityRepositoryJPA.userHasPermissions_OR(4L, "68")) {//если нет прав на Отделения: "Меню - отделения только своего предприятия"
            stringQuery = stringQuery + " and p.id in :myDepthsIds";//покажем только "Меню - только свои отделения", но с их родителями (чтобы можно было добраться до своего)
            needToSetParameter_MyDepthsIds= true;
        }
        if (searchString != null && !searchString.isEmpty()) {
            stringQuery = stringQuery + " and upper(p.name) like upper('%" + searchString + "%')";
        }
        if (companyId > 0) {
            stringQuery = stringQuery + " and p.company_id=" + companyId;
        }
        stringQuery = stringQuery + " and p.parent_id is null";

        Query query =  entityManager.createNativeQuery(stringQuery);

        if(needToSetParameter_MyDepthsIds)//Иначе получим Unable to resolve given parameter name [myDepthsIds] to QueryParameter reference
        {query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsIdWithTheirParents());}

        return query.getResultList().size();
    }

    public Departments getDepartmentById(Long id){
        EntityManager em = emf.createEntityManager();
        Departments d = em.find(Departments.class, id);
        return d;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Departments> getDeptChildrens(int parentDeptId){
        String stringQuery;
        Long departmentOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        stringQuery="select " +
                "           p.id as id, " +
                "           p.name as name, " +
                "           u.username as owner, " +
                "           us.username as creator, " +
                "           uc.username as changer, " +
                "           p.master_id as owner_id, " +
                "           p.creator_id as creator_id, " +
                "           p.changer_id as changer_id, " +
                "           p.company_id as company_id, " +
                "           p.parent_id as parent_id, " +
                "           p.address as address, " +
                "           p.additional as additional, " +
                "           (select name from companies where id=p.company_id) as company, " +
                "           (select count(*) from departments ds where ds.parent_id=p.id) as num_childrens," +
                "           (select name from departments where id=p.parent_id) as parent, " +
                "           p.date_time_created as date_time_created, " +
                "           p.date_time_changed as date_time_changed " +
                "           from departments p " +
                "           INNER JOIN users u ON p.master_id=u.id " +
                "           LEFT OUTER JOIN users us ON p.creator_id=us.id " +
                "           LEFT OUTER JOIN users uc ON p.changer_id=uc.id " +
                "           where  p.master_id="+departmentOwnerId;

        stringQuery = stringQuery+" and p.parent_id="+parentDeptId;
        stringQuery = stringQuery+" order by p.name asc";
        Query query =  entityManager.createNativeQuery(stringQuery, DepartmentsJSON.class);
        return query.getResultList();
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Departments> getDepartmentsListByCompanyId(int company_id, boolean has_parent) {
        String stringQuery;

        Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());

        stringQuery="select " +
                "           p.id as id, " +
                "           p.name ||' '||p.address as name " +
                "           from departments p " +
                "           where  p.master_id="+companyOwnerId;

        if(company_id>0)stringQuery = stringQuery+" and p.company_id="+company_id;

        stringQuery = stringQuery+"and coalesce(p.is_archive,false) !=true";

        if(has_parent){
            stringQuery = stringQuery+" and p.parent_id is not null";
        }else{
            stringQuery = stringQuery+" and p.parent_id is null";
        }
        stringQuery = stringQuery+" order by p.name asc";

        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysDepartmentsList.class);
        return query.getResultList();
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Departments> getMyDepartmentsListByCompanyId(int company_id, boolean has_parent) {
        String stringQuery;

        Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        List<Integer>param=userRepositoryJPA.getMyDepartmentsId();
        String ids="";
        for (int i:param){ids=ids+i+",";}ids=ids+"0";//Костыли, т.к. хз почему не отрабатывает query.setParameter("param"...)

        stringQuery="select " +
                "           p.id as id, " +
                "           p.name ||' '||p.address as name " +
                "           from departments p " +
                "           where  p.master_id="+companyOwnerId+
                "           and p.id in ("+ids+")";

        if(company_id>0)stringQuery = stringQuery+" and p.company_id="+company_id;
        stringQuery = stringQuery+"and coalesce(p.is_archive,false) !=true";
        if(has_parent){
            stringQuery = stringQuery+" and p.parent_id is not null";
        }else{
            stringQuery = stringQuery+" and p.parent_id is null";
        }
        stringQuery = stringQuery+" order by p.name asc";
        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysDepartmentsList.class);
        //query.setParameter("param", userRepositoryJPA.getMyDepartmentsId());
        return query.getResultList();
    }
    @Transactional
    @SuppressWarnings("Duplicates")
    public List<Departments> getMyDepartmentsList() {
        String stringQuery;

        Long companyOwnerId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        List<Integer>param=userRepositoryJPA.getMyDepartmentsId();
        String ids="";
        for (int i:param){ids=ids+i+",";}ids=ids+"0";//Костыли, т.к. хз почему не отрабатывает query.setParameter("param"...)

        stringQuery="select " +
                "           p.id as id, " +
                "           p.name ||' '||p.address as name " +
                "           from departments p " +
                "           where  p.master_id="+companyOwnerId+
                "           and p.id in ("+ids+")";

        stringQuery = stringQuery+" order by p.name asc";
        Query query =  entityManager.createNativeQuery(stringQuery, SpravSysDepartmentsList.class);
        //query.setParameter("param", userRepositoryJPA.getMyDepartmentsId());
        return query.getResultList();
    }
//    for (Long i : userGroups) {
//        dep = em.find(UserGroup.class, i);
//        userGroupSet.add(dep);
//    }
//
    @Transactional
    @SuppressWarnings("Duplicates")
    public DepartmentsJSON getDepartmentValuesById(int id) {
        if(securityRepositoryJPA.userHasPermissions_OR(4L, "13,14,15,16") &&// Отделения: "Просмотр своего" "Просмотр всех" "Редактирование своего" "Редактирование всех"
           securityRepositoryJPA.isItMyMastersDepartment(Long.valueOf(id)))//принадлежит к отделениям моего родителя
        {
            String stringQuery;
            stringQuery = "select p.id as id, " +
                    "           p.name as name, " +
                    "           p.master_id as owner_id, " +
                    "           p.creator_id as creator_id, " +
                    "           p.changer_id as changer_id, " +
                    "           (select name from users where id=p.master_id) as owner, " +
                    "           (select name from users where id=p.creator_id) as creator, " +
                    "           (select name from users where id=p.changer_id) as changer, " +
                    "           p.date_time_created as date_time_created, " +
                    "           p.date_time_changed as date_time_changed, " +
                    "           p.address as address, " +
                    "           p.additional as additional, " +
                    "           coalesce(p.parent_id,'0') as parent_id, " +
                    "           (select name from departments where id=p.parent_id) as parent, " +
                    "           coalesce(p.company_id,'0') as company_id, " +
                    "           (select name from companies where id=p.company_id) as company, " +
                    "           (select count(*) from departments ds where ds.parent_id=p.id) as num_childrens" +
                    "           from departments p" +
                    " where p.id= " + id;

            if (!securityRepositoryJPA.userHasPermissions_OR(4L, "14,16")) {//если нет прав на Отделения: "Просмотр всех" или "Редактирование всех"
                //значит остаются на "Просмотр своего" или "Редактирование своего":
                stringQuery = stringQuery + " and p.id in (select ud.department_id from user_department ud where ud.user_id="+userRepository.getUserId()+")";
                }

            if (!securityRepositoryJPA.userHasPermissions_OR(4L, "13,15")) {//если нет прав на Отделения: "Просмотр своего" или "Редактирование своего"
                //значит остаются на "Просмотр всех", "Редактирование всех"
                stringQuery = stringQuery + " and p.id not in (select ud.department_id from user_department ud where ud.user_id="+userRepository.getUserId()+")";
                }
            Query query = entityManager.createNativeQuery(stringQuery, DepartmentsJSON.class);
            try {// если ничего не найдено, то javax.persistence.NoResultException: No entity found for query
                    return (DepartmentsJSON) query.getSingleResult();
                }
            catch(NoResultException nre)
                {
                    return null;
                }
            }else return null;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean updateDepartment(DepartmentForm form){
        if(securityRepositoryJPA.userHasPermissions_OR(4L, "15,16") &&// Отделения: "Редактирование своего" "Редактирование всех"
           securityRepositoryJPA.isItMyMastersDepartment(Long.valueOf(form.getId())))//принадлежит к отделениям моего родительского аккаунта
        {
            String stringQuery;
            stringQuery="update departments set " +
                    "name='"+form.getName()+"'," +
                    "address='"+form.getAddress()+"'," +
                    "additional='"+form.getAdditional()+"'," +
                    "company_id="+(form.getCompany_id().equals("0") ? null:form.getCompany_id())+"," +
                    "parent_id="+(form.getParent_id().equals("0") ? null:form.getParent_id()) +
                    " where id="+form.getId();

            if (!securityRepositoryJPA.userHasPermissions_OR(4L, "16")&&//если нет прав на Отделения: "Редактирование всех"
                !securityRepositoryJPA.isItMyDepartment(Long.valueOf(form.getId()))){//значит остаются на "Редактирование своего", НО если запрашиваем id НЕ своего отделения:
                    return false;
                }
            if (!securityRepositoryJPA.userHasPermissions_OR(4L, "15")&&//если нет прав на Отделения: "Редактирование своего"
                 securityRepositoryJPA.isItMyDepartment(Long.valueOf(form.getId()))){//значит остаются на "Редактирование всех", НО если запрашиваем id своего отделения:
                    return false;
                }
            Query query = entityManager.createNativeQuery(stringQuery);
            int i=query.executeUpdate();
            return (i==1);
        }else return false;
    }

    public Set<Departments> getDepartmentsSetBySetOfDepartmentsId(Set<Long> departments) {
        EntityManager em = emf.createEntityManager();
        Departments dep = new Departments();
        Set<Departments> departmentsSet = new HashSet<>();
        for (Long i : departments) {
            dep = em.find(Departments.class, i);
            departmentsSet.add(dep);
        }
        return departmentsSet;
    }

    @Transactional
    @SuppressWarnings("Duplicates")
    public boolean deleteDepartmentsById(String delNumbers) {
        if(securityRepositoryJPA.userHasPermissions_OR(4L,"12")&& //Отделение : "Удаление"
           securityRepositoryJPA.isItAllMyMastersDepartments(delNumbers))  //все ли отделения принадлежат текущему хозяину
        {
            String stringQuery;
            stringQuery = "Update departments p" +
                    " set is_archive=true " +
                    " where p.id in (" + delNumbers+")";
            Query query = entityManager.createNativeQuery(stringQuery);
            if (!stringQuery.isEmpty() && stringQuery.trim().length() > 0) {
                int count = query.executeUpdate();
                return true;
            } else return false;
        } else return false;
    }

}
