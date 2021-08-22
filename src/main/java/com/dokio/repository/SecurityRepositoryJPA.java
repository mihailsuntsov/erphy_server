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
import com.dokio.message.response.IsItMy_JSON;
import com.dokio.message.response.IsItMy_Sprav_JSON;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

@Repository
public class SecurityRepositoryJPA {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    DepartmentRepositoryJPA departmentRepositoryJPA;
    @Autowired
    CompanyRepositoryJPA companyRepositoryJPA;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;
    @Autowired
    private CommonUtilites commonUtilites;

    public boolean userHasPermissions_OR(Long dockId, String permissions){

        Long userId=userRepository.getUserId();
        if(userId!=null) {
            String stringQuery = "select p.id from " +
                    " permissions p, " +
                    " usergroup_permissions up, " +
                    " usergroup ugr, " +
                    " user_usergroup uugr, " +
                    " users u " +
                    " where " +
                    " u.id="+userId+" and " +
                    " p.document_id=" + dockId + " and " +
                    " uugr.user_id = u.id and " +
                    " up.usergroup_id =ugr.id and " +
                    " ugr.id=uugr.usergroup_id and " +
                    " up.permission_id=p.id and " +
                    " coalesce(ugr.is_archive,false) !=true and " +
                    " p.id in(" + permissions + ")";
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList().size() > 0);
        }else return false;
    }
    public List<Integer> giveMeMyPermissions(Long dockId){
        Long userId=userRepository.getUserId();
        if(userId!=null) {
            String stringQuery = "select p.id from " +
                    " permissions p, " +
                    " usergroup_permissions up, " +
                    " usergroup ugr, " +
                    " user_usergroup uugr, " +
                    " users u " +
                    " where " +
                    " u.id="+userId+" and " +
                    " p.document_id=" + dockId + " and " +
                    " uugr.user_id = u.id and " +
                    " up.usergroup_id =ugr.id and " +
                    " ugr.id=uugr.usergroup_id and " +
                    " up.permission_id=p.id and " +
                    " coalesce(ugr.is_archive,false) !=true";
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList());
        }else return null;
    }

    public List<Integer> getAllMyPermissions(){
        Long userId=userRepository.getUserId();
        if(userId!=null) {
            String stringQuery = "select p.id from " +
                    " permissions p, " +
                    " usergroup_permissions up, " +
                    " usergroup ugr, " +
                    " user_usergroup uugr, " +
                    " users u " +
                    " where " +
                    " u.id="+userId+" and " +
                    " uugr.user_id = u.id and " +
                    " up.usergroup_id =ugr.id and " +
                    " ugr.id=uugr.usergroup_id and " +
                    " up.permission_id=p.id and " +
                    " coalesce(ugr.is_archive,false) !=true";
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList());
        }else return null;
    }

// Зачем производятся проверки isIt... :
// Если из пользовательского интерфейса можно работать только с теми объектами, которые принадлежат непосредственно родительскому аккаунту,
// то через rest-api можно (по ошибке или намеренно) отправить запрос на действия (чтение, изменение, удаление) с объектами, принадлежащими сторонним аккаунтам.
// Данные проверки призваны устранить эту возможность.

    // !! Устароело!! сейчас используются универсальные проверки для любых документов в разделе   ** U N I V E R S A L **


//*****************************************************************   Company    ***************************************

    @SuppressWarnings("Duplicates") //это предприятие моего родителя?
    public boolean isItMyMastersCompany(Long dockId) {
            Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
            String stringQuery = "select p.id from companies p where p.id="+dockId+" and p.master_id=" + myMasterId ;
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList().size() > 0);}
    //все ли предприятия принадлежат текущему хозяину?
    public boolean isItAllMyMastersCompanies(ArrayList<Long> dockIds) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from companies p where p.id in(:dockIds) and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        query.setParameter("dockIds", dockIds);
        return (query.getResultList().size() == dockIds.size());}
    //моё ли это предприятие?
    public boolean isItMyCompany(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from users p where p.id="+myId+"and company_id=" +dockId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }

//**********************************************************************   Department    *******************************

    @SuppressWarnings("Duplicates") //это отделение моего родителя?
    public boolean isItMyMastersDepartment(Long dockId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select name from " +
                " departments p " +
                " where " +
                " p.id="+dockId+" and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    @SuppressWarnings("Duplicates") //это моё отделение?
    public boolean isItMyDepartment(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select ud.department_id from user_department ud where ud.department_id="+dockId+" and ud.user_id=" + myId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    @SuppressWarnings("Duplicates") //это все мои отделения?
    public boolean isItAllMyDepartments(Set<Long> depIds) {
        Long myId=userRepository.getUserId();
        String dockIds=StringUtils.join(depIds, ',');
        String stringQuery = "select ud.department_id from user_department ud where ud.department_id in ("+dockIds+") and ud.user_id=" + myId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size()==depIds.size());}
    @SuppressWarnings("Duplicates") //все ли отделения принадлежат текущему хозяину?
    public boolean isItAllMyMastersDepartments(String depthIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : depthIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " departments p " +
                " where " +
                " p.id in("+depthIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }

//*************************************************************************   User    **********************************

    public boolean isItMyUser(Long userId) {//это пользователь - я?
    Long myId=userRepository.getUserId();
    return (userId==myId);}
    public boolean isItMyMastersUser(Long userId) {//это пользователь родительского аккаунта
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from users p where p.id="+userId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    @SuppressWarnings("Duplicates") //все ли пользователи принадлежат текущему родителю?
    public boolean isItAllMyMastersUsers(String usrIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : usrIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " users p " +
                " where " +
                " p.id in("+usrIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }

//***********************************************************************   UserGroup    *******************************

    public boolean isItMyMastersUserGroup(Long dockId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from usergroup p where p.id="+dockId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItAllMyMastersUserGroups(String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : ugIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " usergroup p " +
                " where " +
                " p.id in("+ugIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }
    public boolean isItMyUserGroup(Long dockId) {
        Long myId=userRepository.getUserId();
        int myCompanyId=userRepositoryJPA.getMyCompanyId();
        String stringQuery = "select p.id from usergroup p where p.id="+dockId+" and p.company_id=" + myCompanyId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}

//***********************************************************************   TradeResults    ****************************

    public boolean isItMyTradeResults(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from traderesults p where p.id="+dockId+" and p.creator_id=" + myId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_JSON getIsItMy_TradeResults_JSON(Long dockId){  //возвращает набор прав (документ моего предприятия, документ моего отделения, мой документ)
        IsItMy_JSON isItMy_json = new IsItMy_JSON();
        isItMy_json.setItIsMyDocument(isItMyTradeResults(dockId));
        isItMy_json.setItIsDocumentOfMyDepartments(isItMyDepartmentsTradeResults(dockId));
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanyTradeResults(dockId));
        return isItMy_json;}
    public boolean isItMyDepartmentsTradeResults(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from traderesults p where p.id="+dockId+" and p.department_id in (select department_id from user_department where user_id=" + myId+")";
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItMyCompanyTradeResults(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from traderesults p where p.id="+dockId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItAllMyMastersTradeResults(String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : ugIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " traderesults p " +
                " where " +
                " p.id in("+ugIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }//все ли  документы принадлежат текущему родительскому аккаунту
    public boolean isItMyMastersTradeResults(Long dockId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from traderesults p where p.id="+dockId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}//этот документ моего родителя?

//**********************************************************************    SpravSysEdizm     **************************

    public boolean isItMyMastersSpravSysEdizm(Long dockId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from sprav_sys_edizm p where p.id="+dockId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItMyCompanySpravSysEdizm(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from sprav_sys_edizm p where p.id="+dockId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_Sprav_JSON getIsItMy_SpravSysEdizm_JSON(Long dockId){
        IsItMy_Sprav_JSON isItMy_json = new IsItMy_Sprav_JSON();
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanySpravSysEdizm(dockId));
        isItMy_json.setItIsDocumentOfMyMastersCompanies(isItMyMastersSpravSysEdizm(dockId));
        return isItMy_json;}
    public boolean isItAllMyMastersSpravSysEdizm(String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : ugIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " sprav_sys_edizm p " +
                " where " +
                " p.id in("+ugIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }

//*************************************************************************   TypePrices   *****************************

    public boolean isItMyCompanyTypePrices(Long dockId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from sprav_type_prices p where p.id="+dockId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItMyMastersTypePrices(Long dockId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from sprav_type_prices p where p.id="+dockId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_Sprav_JSON getIsItMy_TypePrices_JSON(Long dockId)    {
        IsItMy_Sprav_JSON isItMy_json = new IsItMy_Sprav_JSON();
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanyTypePrices(dockId));
        isItMy_json.setItIsDocumentOfMyMastersCompanies(isItMyMastersTypePrices(dockId));
        return isItMy_json;}
    public boolean isItAllMyMastersTypePrices(String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : ugIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " sprav_type_prices p " +
                " where " +
                " p.id in("+ugIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }

//***********************************************************************     ProductGroups     ************************

    @SuppressWarnings("Duplicates")//все ли  документы принадлежат текущему родительскому аккаунту
    public boolean isItAllMyMastersProductGroups(String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<Long>();
        for( String s : ugIds.split(",") ){decArray.add( new Long(s) );}
        String stringQuery = "select p.id from " +
                " product_groups p " +
                " where " +
                " p.id in("+ugIds+") and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }
    public boolean isItMyCompanyProductGroups(Long dockId) {
//        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from product_groups p where p.id="+dockId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_Sprav_JSON getIsItMy_ProductGroups_JSON(Long dockId){

        IsItMy_Sprav_JSON isItMy_json = new IsItMy_Sprav_JSON();
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanyProductGroups(dockId));
        isItMy_json.setItIsDocumentOfMyMastersCompanies(isItMyMastersProductGroups(dockId));
        return isItMy_json;}
    public boolean isItMyMastersProductGroups(Long dockId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from product_groups p where p.id="+dockId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}


//**********************************************************  U N I V E R S A L   **************************************

    @SuppressWarnings("Duplicates")//все ли  документы принадлежат текущему родительскому аккаунту
    public boolean isItAllMyMastersDocuments(String docTableName, String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<>();
        for (String s : ugIds.split(",")) {
            decArray.add(new Long(s));
        }
        String stringQuery = "select p.id from " +
                docTableName + " p " +
                " where " +
                " p.id in(" + ugIds + ") and " +
                " p.master_id=" + myMasterId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }
    @SuppressWarnings("Duplicates")//все ли  документы принадлежат текущему родительскому аккаунту и предприятию
    public boolean isItAllMyMastersAndMyCompanyDocuments(String docTableName, String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<>();
        for (String s : ugIds.split(",")) {
            decArray.add(new Long(s));
        }
        String stringQuery = "select p.id from " +
                docTableName + " p " +
                " where " +
                " p.id in(" + ugIds + ") and " +
                " p.company_id =" +userRepositoryJPA.getMyCompanyId() +" and "+
                " p.master_id=" + myMasterId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() == decArray.size());
    }
    @SuppressWarnings("Duplicates")//все ли  документы принадлежат текущему родительскому аккаунту и предприятию и отделениям
    public boolean isItAllMyMastersAndMyCompanyAndMyDepthsDocuments(String docTableName, String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<>();
        for (String s : ugIds.split(",")) {
            decArray.add(new Long(s));
        }
        String stringQuery = "select p.id from " +
                docTableName + " p " +
                " where " +
                " p.id in(" + ugIds + ") and " +
                " p.department_id in :myDepthsIds and "+
                " p.company_id =" +userRepositoryJPA.getMyCompanyId() +" and "+
                " p.master_id=" + myMasterId;
        Query query = entityManager.createNativeQuery(stringQuery);
        query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
        return (query.getResultList().size() == decArray.size());
    }
    @SuppressWarnings("Duplicates")//все ли  документы принадлежат текущему родительскому аккаунту и предприятию и отделениям и я - создатель
    public boolean isItAllMyMastersAndMyCompanyAndMyDepthsAndMyDocuments(String docTableName, String ugIds) {//строка типа "1,2,3,4,5..."
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        ArrayList<Long> decArray = new ArrayList<>();
        for (String s : ugIds.split(",")) {
            decArray.add(new Long(s));
        }
        String stringQuery = "select p.id from " +
                docTableName + " p " +
                " where " +
                " p.id in(" + ugIds + ") and " +
                " p.department_id in :myDepthsIds and "+
                " p.creator_id = :myId and "+
                " p.company_id =" +userRepositoryJPA.getMyCompanyId() +" and "+
                " p.master_id=" + myMasterId;
        Query query = entityManager.createNativeQuery(stringQuery);
        query.setParameter("myDepthsIds", userRepositoryJPA.getMyDepartmentsId());
        query.setParameter("myId", userRepository.getUserId());
        return (query.getResultList().size() == decArray.size());
    }
    public boolean isItAllMyDocuments(String docTableName, List<Long> decArray) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String ugIds = commonUtilites.ListOfLongToString(decArray,",","","");
        String stringQuery = "select p.id from " +
                docTableName + " p " +
                " where " +
                " p.id in(" + ugIds + ") and " +
                " p.creator_id = :myId and "+
                " p.company_id =" +userRepositoryJPA.getMyCompanyId() +" and "+
                " p.master_id=" + myMasterId;
        Query query = entityManager.createNativeQuery(stringQuery);
        query.setParameter("myId", userRepository.getUserId());
        return (query.getResultList().size() == decArray.size());
    }

    //true если id предприятия принадлежит аккаунту, который является master-аккаунтом текущего пользователя.
    public Boolean companyBelongToMyMastersAccount(Long id){
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from companies p where p.id="+id+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    //true если id отделения принадлежит аккаунту, который является master-аккаунтом текущего пользователя.
    public Boolean departmentBelongToMyMastersAccount(Long id){
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from departments p where p.id="+id+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    @SuppressWarnings("Duplicates")
    //определяет, обладает ли пользователь правами на создание документа для определенных предприятия и отделения
    //dockId - id документа в таблице Documents (реестр документов Докио)
    //p1,p2,p3 - права в порядке: Создание документов по всем предприятиям, Создание документов своего предприятия, Создание документов своих отделений
    public Boolean userHasPermissionsToCreateDock(Long companyId, Long departmentId, Long dockId, String p1, String p2, String p3) {

        //предприятие принадлежит мастер-аккаунту
        Boolean companyBelongToMyMastersAccount=companyBelongToMyMastersAccount(companyId);
        //отделение принадлежит мастер-аккаунту
        Boolean departmentBelongToMyMastersAccount=departmentBelongToMyMastersAccount(departmentId);
        //отделение принадлежит предприятию
        Boolean departmentBelongToCompany=departmentRepositoryJPA.departmentBelongToCompany(companyId,departmentId);

        //Базовые проверки: Предприятие и отделение принадлежит мастер-аккаунту, и отделение входит в предприятие
        if(departmentBelongToCompany && departmentBelongToMyMastersAccount && companyBelongToMyMastersAccount) {

            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            //отделение входит в число моих отделений
            boolean itIsMyDepartment = myDepartmentsIds.contains(departmentId);
//            Boolean all = userHasPermissions_OR(dockId, p1);
//            Boolean myCompany = (userHasPermissions_OR(dockId, p2) && myCompanyId.equals(companyId));
//            Boolean myDeparts = (userHasPermissions_OR(dockId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment);
//            Boolean finishResult=all||myCompany||myDeparts;

                    //если есть право на создание по всем предприятиям, или
            return (userHasPermissions_OR(dockId, p1)) ||
                    //если есть право на создание по всем отделениям своего предприятия, и предприятие документа своё, или
                    (userHasPermissions_OR(dockId, p2) && myCompanyId.equals(companyId)) ||
                    //если есть право на создание по своим отделениям своего предприятия, предприятие своё, и отделение документа входит в число своих
                    (userHasPermissions_OR(dockId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment);
                    //false - недостаточно прав

        }else return null;// не прошли базовые проверки - значит тут вообще что-то не чисто
    }
    @SuppressWarnings("Duplicates")
    //определяет, обладает ли пользователь правами на создание документа для определенных предприятия и отделения
    //dockId - id документа в таблице Documents (реестр документов Докио)
    //p1,p2,p3 - права в порядке: Создание документов по всем предприятиям, Создание документов своего предприятия, Создание документов своих отделений
    public Boolean userHasPermissionsToUpdateDock(Long companyId, Long departmentId, String tableName, Long dockId, Long id, String p1, String p2, String p3, String p4) {

        //предприятие принадлежит мастер-аккаунту
        Boolean companyBelongToMyMastersAccount=companyBelongToMyMastersAccount(companyId);
        //отделение принадлежит мастер-аккаунту
        Boolean departmentBelongToMyMastersAccount=departmentBelongToMyMastersAccount(departmentId);
        //отделение принадлежит предприятию
        Boolean departmentBelongToCompany=departmentRepositoryJPA.departmentBelongToCompany(companyId,departmentId);

        //Базовые проверки: Предприятие и отделение принадлежит мастер-аккаунту, и отделение входит в предприятие
        if(departmentBelongToCompany && departmentBelongToMyMastersAccount && companyBelongToMyMastersAccount) {

            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            List<Long> myDocksIds = new ArrayList<>();
            myDocksIds.add(id);
            //отделение входит в число моих отделений
            boolean itIsMyDepartment = myDepartmentsIds.contains(departmentId);
//            Boolean all = userHasPermissions_OR(dockId, p1);
//            Boolean myCompany = (userHasPermissions_OR(dockId, p2) && myCompanyId.equals(companyId));
//            Boolean myDeparts = (userHasPermissions_OR(dockId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment);
//            Boolean finishResult=all||myCompany||myDeparts;


            //если есть право на создание по всем предприятиям, или
            return (userHasPermissions_OR(dockId, p1)) ||
                    //если есть право на создание по всем отделениям своего предприятия, и предприятие документа своё, или
                    (userHasPermissions_OR(dockId, p2) && myCompanyId.equals(companyId)) ||
                    //если есть право на создание по своим отделениям своего предприятия, предприятие своё, и отделение документа входит в число своих
                    (userHasPermissions_OR(dockId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment) ||
                    //если есть право на создание по своим отделениям своего предприятия, предприятие своё, отделение документа входит в число своих, и документ свой
                    (userHasPermissions_OR(dockId, p4) && myCompanyId.equals(companyId) && itIsMyDepartment && isItAllMyDocuments(tableName, myDocksIds))
                    ;
            //false - недостаточно прав

        }else return null;// не прошли базовые проверки - значит тут вообще что-то не чисто
    }


}
