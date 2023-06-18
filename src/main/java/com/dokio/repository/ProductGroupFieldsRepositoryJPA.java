///*
//        Dokio CRM - server part. Sales, finance and warehouse management system
//        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU Affero General Public License as
//        published by the Free Software Foundation, either version 3 of the
//        License, or (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU Affero General Public License for more details.
//
//        You should have received a copy of the GNU Affero General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>
//*/
//
//package com.dokio.repository;
//
//import com.dokio.message.request.ProductGroupFieldsForm;
//import com.dokio.message.response.ProductGroupFieldTableJSON;
//import com.dokio.model.User;
//import com.dokio.security.services.UserDetailsServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.*;
//import java.util.List;
//
//
//@Repository
//public class ProductGroupFieldsRepositoryJPA {
//    @PersistenceContext
//    private EntityManager entityManager;
////    @Autowired
////    private EntityManagerFactory emf;
//    @Autowired
//    private UserDetailsServiceImpl userRepository;
//    @Autowired
//    private UserRepositoryJPA userRepositoryJPA;
//    @Autowired
//    SecurityRepositoryJPA securityRepositoryJPA;
//    @Autowired
//    CompanyRepositoryJPA companyRepositoryJPA;
//    @Autowired
//    DepartmentRepositoryJPA departmentRepositoryJPA;
//    @Autowired
//    UserRepository userService;
//
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public List<ProductGroupFieldTableJSON> getProductGroupFieldsList(int groupId, int field_type, int parentSetId) {
//        if(securityRepositoryJPA.userHasPermissions_OR(10L, "113,114,115,116"))//"Группы товаров" - просмотр или редактирование (см. файл Permissions Id)
//        {
//            String stringQuery;
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            stringQuery = "select  p.id as id, " +
//                    "           p.name as name, " +
//                    "           p.description as description, " +
//                    "           p.field_type as field_type, " +
//                    "           p.parent_set_id as parent_set_id, " +
//                    "           p.group_id as group_id, " +
//                    "           '' as value, " +
//                    "           p.output_order as output_order" +
//                    "           from product_group_fields p" +
//                    "           where  p.master_id=" + myMasterId +
//                    "           and  p.group_id=" + groupId +
//                    "           and  p.field_type=" + field_type +// тип: 1 - сеты (наборы) полей, 2 - поля
//                    "           and  p.parent_set_id" + (parentSetId>0 ? ("="+parentSetId) : " is null ");
//                if (!securityRepositoryJPA.userHasPermissions_OR(10L, "113,115")) //Если нет прав на просм. или редактир. по всем предприятиям"
//                {
//                    //остается только на своё предприятие
//                    stringQuery = stringQuery + " and p.company_id=" + userRepositoryJPA.getMyCompanyId();//т.е. нет прав на все предприятия, а на своё есть
//                }
//            stringQuery = stringQuery + " order by p.output_order asc ";
//            Query query = entityManager.createNativeQuery(stringQuery, ProductGroupFieldTableJSON.class);
//            return query.getResultList();
//        } else return null;
//    }
//
//
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public boolean saveChangeFieldsOrder(List<ProductGroupFieldsForm> request)
//    {
//        if(securityRepositoryJPA.userHasPermissions_OR(10L, "115,116"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
//        {
//                Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
////                User changer = userRepository.getUserByUsername(userRepository.getUserName());
//                String stringQuery;
//                int myCompanyId = userRepositoryJPA.getMyCompanyId();
//
//                try
//                {
//                    for (ProductGroupFieldsForm field : request)
//                    {
//                        stringQuery = "update product_group_fields set " +
//
//                                " output_order=" + field.getOutput_order() +
//                                " where id=" + field.getId() +
//                                " and master_id=" + myMasterId;
//                        if (!securityRepositoryJPA.userHasPermissions_OR(10L, "115")) //Если нет прав по всем предприятиям
//                        {
//                            //остается только на своё предприятие (110)
//                            stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
//                        }
//                        Query query = entityManager.createNativeQuery(stringQuery);
//                        int i = query.executeUpdate();
//                    }
//                    return true;
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//        } else return false;
//    }
//
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public boolean updateProductGroupField(ProductGroupFieldsForm request)
//    {
//        if(securityRepositoryJPA.userHasPermissions_OR(10L, "115,116"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
//        {
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            User changer = userRepository.getUserByUsername(userRepository.getUserName());
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
//
//            String stringQuery;
//            stringQuery = "update product_group_fields set " +
//
//                    " name=:name, "+
//                    " description=:description, "+
//                    " parent_set_id=" + request.getParent_set_id() +
//                    " where id=" + request.getId()+
//                    " and master_id="+myMasterId ;
//                    if (!securityRepositoryJPA.userHasPermissions_OR(10L, "115")) //Если нет прав по всем предприятиям
//                    {
//                        //остается только на своё предприятие (110)
//                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
//                    }
//            try
//            {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                query.setParameter("name", request.getName());
//                query.setParameter("description", request.getDescription());
//                int i = query.executeUpdate();
//                return true;
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        } else return false;
//    }
//
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public boolean insertProductGroupField(ProductGroupFieldsForm request)
//    {
//        if(securityRepositoryJPA.userHasPermissions_OR(10L, "115,116"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
//        {
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            String stringQuery;
//            Long myId = userRepository.getUserId();
//
//            stringQuery = "insert into product_group_fields (" +
//                    "name," +
//                    "description," +
//                    "master_id," +
//                    "creator_id," +
//                    "parent_set_id," +
//                    "company_id," +
//                    "group_id," +
//                    "date_time_created," +
//                    "output_order,"+
//                    "field_type" +
//
//                    ") values ( " +
//
//                    ":name, "+
//                    ":description, "+
//                    myMasterId+","+
//                    myId+","+
//                    request.getParent_set_id()+", "+
//                    request.getCompany_id()+", "+
//                    request.getGroup_id()+", "+
//                    "now(), "+
//                    "(select coalesce(max(output_order)+1,1) from product_group_fields where group_id=" + request.getGroup_id() + "),"+
//                    request.getField_type()+")";
//            try
//            {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                query.setParameter("name", request.getName());
//                query.setParameter("description", (request.getDescription() == null?"":request.getDescription()));
//                query.executeUpdate();
//                return true;
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        } else return false;
//    }
//
//    @Transactional
//    @SuppressWarnings("Duplicates")
//    public boolean deleteProductGroupField(ProductGroupFieldsForm request)
//    {
//        if(securityRepositoryJPA.userHasPermissions_OR(10L, "115,116"))//"Группы товаров" редактирование своих или чужих предприятий (в пределах род. аккаунта разумеется)
//        {
//            Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
//            User changer = userRepository.getUserByUsername(userRepository.getUserName());
//            String stringQuery;
//            int myCompanyId = userRepositoryJPA.getMyCompanyId();
//            stringQuery = "delete from product_group_fields "+
//                    " where id=" + request.getId()+
//                    " and master_id="+myMasterId ;
//                    if (!securityRepositoryJPA.userHasPermissions_OR(10L, "115")) //Если нет прав по всем предприятиям
//                    {
//                        //остается только на своё предприятие (110)
//                        stringQuery = stringQuery + " and company_id=" + myCompanyId;//т.е. нет прав на все предприятия, а на своё есть
//                    }
//            try
//            {
//                Query query = entityManager.createNativeQuery(stringQuery);
//                int i = query.executeUpdate();
//                return true;
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        } else return false;
//    }
//
//
//}
