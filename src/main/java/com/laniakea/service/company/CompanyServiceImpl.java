package com.laniakea.service.company;


import com.laniakea.message.request.CompanyForm;
import com.laniakea.model.Companies;
import com.laniakea.message.response.CompaniesJSON;
import com.laniakea.repository.CompanyRepositoryJPA;
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
