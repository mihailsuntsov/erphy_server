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
package com.dokio.service.company;


import com.dokio.message.request.CompanyForm;
import com.dokio.model.Companies;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.repository.CompanyRepositoryJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService{

    @Autowired
    private CompanyRepositoryJPA cr;


    @Override
    public List<Companies> getCompaniesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc) {
        return cr.getCompaniesTable(result, offsetreal, searchString, sortColumn, sortAsc);
    }

    @Override
    public int getCompaniesSize(String searchString) {
        return cr.getCompaniesSize(searchString);
    }

    @Override
    public Companies getCompanyById(Long id)  {
        return cr.getCompanyById(id);
    }

    @Override
    public CompaniesJSON getCompanyValuesById(int id)  {
        return cr.getCompanyValuesById(id);
    }

    @Override
    public Long insertCompany(Companies company) {
        return cr.insertCompany(company);
    }

    @Override
    public boolean updateCompany(CompanyForm company) {
        return cr.updateCompany(company);
    }

    @Override
    public boolean deleteCompaniesByNumber(ArrayList<Long> delNumbers) {
        return cr.deleteCompaniesByNumber(delNumbers);
    }

    @Override
    public List<Companies> getCompaniesList() {
        return cr.getCompaniesList();
    }
}
