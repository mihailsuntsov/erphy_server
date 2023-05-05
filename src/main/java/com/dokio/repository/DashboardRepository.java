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
                            "user_id, " +
                            "date_time_update" +
                            ") values (" +
                            myMasterId + "," +
                            row.getCompanyId() + "," +
                            myId + "," +
                            " now()" +
                            ") " +
                            "ON CONFLICT ON CONSTRAINT settings_dashboard_user_uq " +// "upsert"
                            " DO update set " +
                            " company_id = " +row.getCompanyId()+
                            ", date_time_update = now()";

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
                "           where p.user_id= " + myId +" ORDER BY coalesce(date_time_update,to_timestamp('01.01.2000 00:00:00','DD.MM.YYYY HH24:MI:SS')) DESC  limit 1";
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
