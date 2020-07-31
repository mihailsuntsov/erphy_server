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

import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.*;

@Repository
public class SiteRepository {

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
    private static final Logger log = Logger.getLogger(PricesRepository.class);



    @SuppressWarnings("Duplicates")
    public String getHtmlPage(String domain, String uid, Long route_id, String parameter) {

        String stringQuery;
        stringQuery =
                " select " +
                        " sh.html_content " +
                        " from " +
                        " sites_html sh " +
                        " join sites st on sh.site_id=st.id " +
                        " where " +
                        " sh.route_id = " + route_id +
                        (parameter.equals("") ? " and sh.parameter is null":" and sh.parameter = '" + parameter + "' ") +
                        " and st.uid = '" + uid + "' " +
                        " and st.domain = '" + domain + "' ";

        Query query = entityManager.createNativeQuery(stringQuery);

        return (String) query.getSingleResult();
    }

}
