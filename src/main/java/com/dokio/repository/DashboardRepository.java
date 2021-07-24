package com.dokio.repository;
import com.dokio.message.request.Settings.SettingsDashboardForm;
import com.dokio.message.response.Settings.SettingsDashboardJSON;
import com.dokio.security.services.UserDetailsServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Repository
public class DashboardRepository {

    Logger logger = Logger.getLogger("DashboardRepository");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private UserDetailsServiceImpl userRepository;
    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    //сохраняет настройки документа "Розничные продажи"
    @SuppressWarnings("Duplicates")
    @Transactional
    public Boolean saveSettingsDashboard(SettingsDashboardForm row) {
        String stringQuery="";
        Long myMasterId = userRepositoryJPA.getUserMasterIdByUsername(userRepository.getUserName());
        Long myId=userRepository.getUserId();
        try {
            stringQuery =
                    " insert into settings_dashboard (" +
                            "master_id, " +
                            "company_id, " +
                            "user_id " +
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_dashboard_user_uq " +// "upsert"
                            " DO update set " +
                            " company_id = " +row.getCompanyId();

            Query query = entityManager.createNativeQuery(stringQuery);
            query.executeUpdate();
            return true;
        }
        catch (Exception e) {
            logger.error("Exception in method saveSettingsDashboard. SQL query:"+stringQuery, e);
            e.printStackTrace();
            return null;
        }
    }

    //Загружает настройки документа "Заказ покупателя" для текущего пользователя (из-под которого пришел запрос)
    @SuppressWarnings("Duplicates")
    public SettingsDashboardJSON getSettingsDashboard() {

        String stringQuery;
        Long myId=userRepository.getUserId();
        stringQuery = "select " +
                "           p.company_id as company_id ," +
                "           1 as ballast " + //чтобы получился массив Object, а не BigInteger
                "           from settings_dashboard p " +
                "           where p.user_id= " + myId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();

            SettingsDashboardJSON returnObj=new SettingsDashboardJSON();

            for(Object[] obj:queryList){
                returnObj.setCompanyId(Long.parseLong(                  obj[0].toString()));
            }
            return returnObj;
        }
        catch (Exception e) {
            logger.error("Exception in method getSettingsDashboard. SQL query:"+stringQuery, e);
            e.printStackTrace();
            throw e;
        }

    }
}
