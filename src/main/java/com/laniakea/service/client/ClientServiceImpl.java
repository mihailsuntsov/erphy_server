package com.laniakea.service.client;

import com.laniakea.model.Client;
import com.laniakea.message.response.ClientJSON;
import com.laniakea.repository.ClientRepositoryJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {


    @Autowired
    private ClientRepositoryJPA clientRepository;


    @Override
    public boolean insertClient(Client client) {
        return clientRepository.insertClient(client);
    }

    @Override
    public ClientJSON getClientById(int id) {
        return clientRepository.getClientById(id);
    }

    @Override
    public int getSize(String searchString) {
        return clientRepository.getSize(searchString);
    }

    @Override
    public List<Client> getClientsTable(int result, int offsetreal, String searchString,String sortColumn,String sortAsc) {
        return clientRepository.getClientsTable(result,offsetreal,searchString,sortColumn,sortAsc);
    }

    @Override
    public boolean updateClient(Client client) {
        return clientRepository.updateClient(client);
    }

    @Override
    public boolean deleteClientsByNumber(ArrayList<Long> cliNumbers) {
        return clientRepository.deleteClientsByNumber(cliNumbers);
    }


}
