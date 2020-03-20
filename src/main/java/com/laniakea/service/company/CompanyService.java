package com.laniakea.service.company;

import com.laniakea.message.request.CompanyForm;
import com.laniakea.model.Companies;
import com.laniakea.message.response.CompaniesJSON;
import org.omg.CORBA.LongHolder;

import java.util.ArrayList;
import java.util.List;

public interface CompanyService {


    List<Companies> getCompaniesTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc);

    public Companies getCompanyById(Long id);

    public CompaniesJSON getCompanyValuesById(int id);

    public int getCompaniesSize(String searchString);

    public Long insertCompany(Companies company);

    public boolean updateCompany(CompanyForm company);

    public boolean deleteCompaniesByNumber(ArrayList<Long> delNumbers);

    public List<Companies> getCompaniesList();


}
