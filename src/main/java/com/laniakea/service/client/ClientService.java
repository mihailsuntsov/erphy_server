package com.laniakea.service.client;

import java.util.ArrayList;
import java.util.List;
import com.laniakea.model.Client;
import com.laniakea.message.response.ClientJSON;

public interface ClientService {

    List<Client> getClientsTable(int result, int offsetreal, String searchString,String sortColumn,String sortAsc);

    public boolean insertClient(Client client);

    public ClientJSON getClientById(int id);

    public boolean updateClient(Client client);

    public boolean deleteClientsByNumber(ArrayList<Long> cliNumbers);

    public int getSize(String searchString);

}
