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
import com.dokio.message.response.IsItMy_JSON;
import com.dokio.message.response.IsItMy_Sprav_JSON;
import com.dokio.security.services.UserDetailsServiceImpl;
import com.dokio.util.CommonUtilites;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
//import org.apache.commons.lang.StringUtils;

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

    public boolean userHasPermissions_OR(Long docId, String permissions){

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
                    " p.document_id=" + docId + " and " +
                    " uugr.user_id = u.id and " +
                    " up.usergroup_id =ugr.id and " +
                    " ugr.id=uugr.usergroup_id and " +
                    " up.permission_id=p.id and " +
                    " coalesce(ugr.is_archive,false) !=true and " +
                    " p.id in(" + permissions + ")";// внутрений запрос
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList().size() > 0);
        }else return false;
    }
    public List<Integer> giveMeMyPermissions(Long docId){
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
                    " p.document_id=" + docId + " and " +
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
                    " ugr.company_id=u.company_id and " +
                    " coalesce(ugr.is_deleted,false) = false" +
                    " group by p.id";
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
    public boolean isItMyMastersCompany(Long docId) {
            Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
            String stringQuery = "select p.id from companies p where p.id="+docId+" and p.master_id=" + myMasterId ;
            Query query = entityManager.createNativeQuery(stringQuery);
            return (query.getResultList().size() > 0);}
    //все ли предприятия принадлежат текущему хозяину?
    public boolean isItAllMyMastersCompanies(ArrayList<Long> docIds) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from companies p where p.id in(:docIds) and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        query.setParameter("docIds", docIds);
        return (query.getResultList().size() == docIds.size());}
    //моё ли это предприятие?
    public boolean isItMyCompany(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from users p where p.id="+myId+"and company_id=" +docId;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }

//**********************************************************************   Department    *******************************

    @SuppressWarnings("Duplicates") //это отделение моего родителя?
    public boolean isItMyMastersDepartment(Long docId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select name from " +
                " departments p " +
                " where " +
                " p.id="+docId+" and " +
                " p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    @SuppressWarnings("Duplicates") //это моё отделение?
    public boolean isItMyDepartment(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select ud.department_id from user_department ud where ud.department_id="+docId+" and ud.user_id=" + myId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    @SuppressWarnings("Duplicates") //это все мои отделения?
    public boolean isItAllMyDepartments(Set<Long> depIds) {
        Long myId=userRepository.getUserId();
        String docIds= StringUtils.join(depIds, ',');
        String stringQuery = "select ud.department_id from user_department ud where ud.department_id in ("+docIds+") and ud.user_id=" + myId ;
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

    public boolean isItMyMastersUserGroup(Long docId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from usergroup p where p.id="+docId+" and p.master_id=" + myMasterId ;
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
    public boolean isItMyUserGroup(Long docId) {
        Long myId=userRepository.getUserId();
        int myCompanyId=userRepositoryJPA.getMyCompanyId();
        String stringQuery = "select p.id from usergroup p where p.id="+docId+" and p.company_id=" + myCompanyId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}

//***********************************************************************   TradeResults    ****************************

    public boolean isItMyTradeResults(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from traderesults p where p.id="+docId+" and p.creator_id=" + myId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_JSON getIsItMy_TradeResults_JSON(Long docId){  //возвращает набор прав (документ моего предприятия, документ моего отделения, мой документ)
        IsItMy_JSON isItMy_json = new IsItMy_JSON();
        isItMy_json.setItIsMyDocument(isItMyTradeResults(docId));
        isItMy_json.setItIsDocumentOfMyDepartments(isItMyDepartmentsTradeResults(docId));
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanyTradeResults(docId));
        return isItMy_json;}
    public boolean isItMyDepartmentsTradeResults(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from traderesults p where p.id="+docId+" and p.department_id in (select department_id from user_department where user_id=" + myId+")";
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItMyCompanyTradeResults(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from traderesults p where p.id="+docId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
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
    public boolean isItMyMastersTradeResults(Long docId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from traderesults p where p.id="+docId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}//этот документ моего родителя?

//**********************************************************************    SpravSysEdizm     **************************

    public boolean isItMyMastersSpravSysEdizm(Long docId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from sprav_sys_edizm p where p.id="+docId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItMyCompanySpravSysEdizm(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from sprav_sys_edizm p where p.id="+docId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_Sprav_JSON getIsItMy_SpravSysEdizm_JSON(Long docId){
        IsItMy_Sprav_JSON isItMy_json = new IsItMy_Sprav_JSON();
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanySpravSysEdizm(docId));
        isItMy_json.setItIsDocumentOfMyMastersCompanies(isItMyMastersSpravSysEdizm(docId));
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

    public boolean isItMyCompanyTypePrices(Long docId) {
        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from sprav_type_prices p where p.id="+docId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public boolean isItMyMastersTypePrices(Long docId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from sprav_type_prices p where p.id="+docId+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_Sprav_JSON getIsItMy_TypePrices_JSON(Long docId)    {
        IsItMy_Sprav_JSON isItMy_json = new IsItMy_Sprav_JSON();
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanyTypePrices(docId));
        isItMy_json.setItIsDocumentOfMyMastersCompanies(isItMyMastersTypePrices(docId));
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
    public boolean isItMyCompanyProductGroups(Long docId) {
//        Long myId=userRepository.getUserId();
        String stringQuery = "select p.id from product_groups p where p.id="+docId+" and p.company_id =" +userRepositoryJPA.getMyCompanyId();
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);}
    public IsItMy_Sprav_JSON getIsItMy_ProductGroups_JSON(Long docId){

        IsItMy_Sprav_JSON isItMy_json = new IsItMy_Sprav_JSON();
        isItMy_json.setItIsDocumentOfMyCompany(isItMyCompanyProductGroups(docId));
        isItMy_json.setItIsDocumentOfMyMastersCompanies(isItMyMastersProductGroups(docId));
        return isItMy_json;}
    public boolean isItMyMastersProductGroups(Long docId) {
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from product_groups p where p.id="+docId+" and p.master_id=" + myMasterId ;
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
    public Boolean companyBelongsToMyMastersAccount(Long id){
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from companies p where p.id="+id+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    //true если id отделения принадлежит аккаунту, который является master-аккаунтом текущего пользователя.
    public Boolean departmentBelongsToMyMastersAccount(Long id){
        Long myMasterId = this.userRepositoryJPA.getUserMasterIdByUsername(this.userRepository.getUserName());
        String stringQuery = "select p.id from departments p where p.id="+id+" and p.master_id=" + myMasterId ;
        Query query = entityManager.createNativeQuery(stringQuery);
        return (query.getResultList().size() > 0);
    }
    @SuppressWarnings("Duplicates")
    //определяет, обладает ли пользователь правами на создание документа для определенных предприятия и отделения
    //docId - id документа в таблице Documents (реестр документов Докио)
    //p1,p2,p3 - права в порядке: Создание документов по всем предприятиям, Создание документов своего предприятия, Создание документов своих отделений
    public Boolean userHasPermissionsToCreateDoc(Long companyId, Long departmentId, Long docId, String p1, String p2, String p3) {

        //предприятие принадлежит мастер-аккаунту
        Boolean companyBelongsToMyMastersAccount=companyBelongsToMyMastersAccount(companyId);
        //отделение принадлежит мастер-аккаунту
        Boolean departmentBelongsToMyMastersAccount=departmentBelongsToMyMastersAccount(departmentId);
        //отделение принадлежит предприятию
        Boolean departmentBelongToCompany=departmentRepositoryJPA.departmentBelongToCompany(companyId,departmentId);

        //Базовые проверки: Предприятие и отделение принадлежит мастер-аккаунту, и отделение входит в предприятие
        if(departmentBelongToCompany && departmentBelongsToMyMastersAccount && companyBelongsToMyMastersAccount) {

            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            //отделение входит в число моих отделений
            boolean itIsMyDepartment = myDepartmentsIds.contains(departmentId);
//            Boolean all = userHasPermissions_OR(docId, p1);
//            Boolean myCompany = (userHasPermissions_OR(docId, p2) && myCompanyId.equals(companyId));
//            Boolean myDeparts = (userHasPermissions_OR(docId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment);
//            Boolean finishResult=all||myCompany||myDeparts;

                    //если есть право на создание по всем предприятиям, или
            return (userHasPermissions_OR(docId, p1)) ||
                    //если есть право на создание по всем отделениям своего предприятия, и предприятие документа своё, или
                    (userHasPermissions_OR(docId, p2) && myCompanyId.equals(companyId)) ||
                    //если есть право на создание по своим отделениям своего предприятия, предприятие своё, и отделение документа входит в число своих
                    (userHasPermissions_OR(docId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment);
                    //false - недостаточно прав

        }else return null;// не прошли базовые проверки - значит тут вообще что-то не чисто
    }
    @SuppressWarnings("Duplicates")
    //определяет, обладает ли пользователь правами на создание документа для определенных предприятия и отделения
    //docId - id документа в таблице Documents (реестр документов Докио)
    //p1,p2,p3 - права в порядке: Создание документов по всем предприятиям, Создание документов своего предприятия, Создание документов своих отделений
    public Boolean userHasPermissionsToUpdateDoc(Long companyId, Long departmentId, String tableName, Long docId, Long id, String p1, String p2, String p3, String p4) {

        //предприятие принадлежит мастер-аккаунту
        Boolean companyBelongsToMyMastersAccount=companyBelongsToMyMastersAccount(companyId);
        //отделение принадлежит мастер-аккаунту
        Boolean departmentBelongsToMyMastersAccount=departmentBelongsToMyMastersAccount(departmentId);
        //отделение принадлежит предприятию
        Boolean departmentBelongToCompany=departmentRepositoryJPA.departmentBelongToCompany(companyId,departmentId);

        //Базовые проверки: Предприятие и отделение принадлежит мастер-аккаунту, и отделение входит в предприятие
        if(departmentBelongToCompany && departmentBelongsToMyMastersAccount && companyBelongsToMyMastersAccount) {

            Long myCompanyId = userRepositoryJPA.getMyCompanyId_();
            List<Long> myDepartmentsIds =  userRepositoryJPA.getMyDepartmentsId_LONG();
            List<Long> myDocsIds = new ArrayList<>();
            myDocsIds.add(id);
            //отделение входит в число моих отделений
            boolean itIsMyDepartment = myDepartmentsIds.contains(departmentId);
//            Boolean all = userHasPermissions_OR(docId, p1);
//            Boolean myCompany = (userHasPermissions_OR(docId, p2) && myCompanyId.equals(companyId));
//            Boolean myDeparts = (userHasPermissions_OR(docId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment);
//            Boolean finishResult=all||myCompany||myDeparts;


            //если есть право на создание по всем предприятиям, или
            return (userHasPermissions_OR(docId, p1)) ||
                    //если есть право на создание по всем отделениям своего предприятия, и предприятие документа своё, или
                    (userHasPermissions_OR(docId, p2) && myCompanyId.equals(companyId)) ||
                    //если есть право на создание по своим отделениям своего предприятия, предприятие своё, и отделение документа входит в число своих
                    (userHasPermissions_OR(docId, p3) && myCompanyId.equals(companyId) && itIsMyDepartment) ||
                    //если есть право на создание по своим отделениям своего предприятия, предприятие своё, отделение документа входит в число своих, и документ свой
                    (userHasPermissions_OR(docId, p4) && myCompanyId.equals(companyId) && itIsMyDepartment && isItAllMyDocuments(tableName, myDocsIds))
                    ;
            //false - недостаточно прав

        }else return null;// не прошли базовые проверки - значит тут вообще что-то не чисто
    }


}
