package com.laniakea.repository;

import com.laniakea.security.services.UserDetailsServiceImpl;
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
                        " and sh.parameter = '" + parameter + "' " +
                        " and st.uid = '" + uid + "' " +
                        " and st.domain = '" + domain + "' ";

        Query query = entityManager.createNativeQuery(stringQuery);

        return (String) query.getSingleResult();
    }

}
