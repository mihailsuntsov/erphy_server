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
package com.dokio.service.client;

import com.dokio.model.Client;
import com.dokio.message.response.ClientJSON;
import com.dokio.repository.ClientRepositoryJPA;
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
