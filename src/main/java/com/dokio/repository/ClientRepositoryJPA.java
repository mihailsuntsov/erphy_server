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
import java.util.ArrayList;
import java.util.List;
import com.dokio.message.response.ClientJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import com.dokio.model.Client;
import org.springframework.transaction.annotation.Transactional;


@Repository("ClientRepositoryJPA")
public class ClientRepositoryJPA {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory emf;

    //@Transactional
    public boolean updateClient(Client client) {
//        entityManager.merge(client);
//        return true;

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Client cli = em.find(Client.class, client.getCliNumber());
        cli.setCliName      (client.getCliName());
        cli.setCliLastname  (client.getCliLastname());
        cli.setCliDatebirth (client.getCliDatebirth());
        cli.setCliEmail     (client.getCliEmail());
        cli.setCliTelephone (client.getCliTelephone());
        cli.setCliRegister  (client.getCliRegister());
        cli.setCliAdditional(client.getCliAdditional());
        em.getTransaction().commit();
        em.close();
        return true;
    }

    @Transactional
    public boolean deleteClientsByNumber(ArrayList<Long> cliNumbers){
        Query query = entityManager.createNamedQuery("Client.deleteClientsByNumber");
        if( cliNumbers != null ){
            query.setParameter("cliNumbers", cliNumbers);
            int cont = query.executeUpdate();
            return true;
        }else{
            return false;
        }
    }

    @Transactional
    public boolean insertClient(Client client) {
        entityManager.persist(client);
        return true;
    }

    @Transactional
    public List<Client> getClientsTable(int result, int offsetreal, String searchString,String sortColumn,String sortAsc) {
        String stringQuery;

        stringQuery="select p.cli_number as cliNumber, " +
                "           p.cli_name as cliName, " +
                "           p.cli_datebirth as cliDatebirth, " +
                "           p.cli_lastname as cliLastname, " +
                "           p.cli_register as cliRegister," +
                "           p.cli_telephone as cliTelephone," +
                "           p.cli_email as cliEmail," +
                "           p.cli_additional as cliAdditional," +
                "           u.username as creator" +
                "           from Client p LEFT OUTER JOIN Users u ON " +
                "           p.added_by_user_id=u.id "        ;
        if(searchString!= null && !searchString.isEmpty()){
            stringQuery = stringQuery+" where upper(p.cli_name) like upper('%"+searchString+"%')";
        }
        stringQuery = stringQuery+" order by p."+sortColumn+" "+sortAsc;
        Query query =  entityManager.createNativeQuery(stringQuery, ClientJSON.class)
                .setFirstResult(offsetreal)
                .setMaxResults(result);
        return query.getResultList();
    }

     @Transactional
    @SuppressWarnings("Duplicates")
    public ClientJSON getClientById(int id) {
        String stringQuery;
        stringQuery="select p.cli_number as cliNumber, " +
                "           p.cli_name as cliName, " +
                "           p.cli_datebirth as cliDatebirth, " +
                "           p.cli_lastname as cliLastname, " +
                "           p.cli_register as cliRegister," +
                "           p.cli_telephone as cliTelephone," +
                "           p.cli_email as cliEmail," +
                "           p.cli_additional as cliAdditional," +
                "           u.username as creator" +
                "           from Client p LEFT OUTER JOIN Users u ON " +
                "           p.added_by_user_id=u.id "        ;
        stringQuery = stringQuery+" where p.cli_number= "+id;
        Query query = entityManager.createNativeQuery(stringQuery,ClientJSON.class);
        return (ClientJSON) query.getSingleResult();
    }

    @Transactional
    public int getSize(String searchString) {
        String stringQuery;
        stringQuery="from Client p";
        if(searchString!= null && !searchString.isEmpty()){
            stringQuery = stringQuery+" where upper(p.cliName) like upper('%"+searchString+"%')";
        }
        Query query =  entityManager.createQuery(stringQuery,Client.class);
        return query.getResultList().size();
    }

}