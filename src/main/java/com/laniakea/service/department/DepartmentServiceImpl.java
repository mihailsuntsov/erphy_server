package com.laniakea.service.department;

import com.laniakea.message.request.DepartmentForm;
import com.laniakea.model.Departments;
import com.laniakea.message.response.DepartmentsJSON;
import com.laniakea.repository.DepartmentRepositoryJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService{

    @Autowired
    private DepartmentRepositoryJPA rep;

    @Override
    public List<Departments> getDepartmentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId) {
        return rep.getDepartmentsTable(result, offsetreal, searchString, sortColumn, sortAsc, companyId);
    }

    @Override
    public Departments getCompanyById(int id) {
        return null;
    }

    @Override
    public List<Departments> getDeptChildrens(int parentDeptId) {
        return rep.getDeptChildrens(parentDeptId);
    }

    @Override
    public DepartmentsJSON getDepartmentValuesById(int id) {
        return rep.getDepartmentValuesById(id);
    }

    @Override
    public int getDepartmentsSize(String searchString, int companyId) {
        return rep.getDepartmentsSize(searchString, companyId);
    }

    @Override
    public boolean updateDepartment(DepartmentForm dep) {
        return rep.updateDepartment(dep);
    }

    @Override
    public Long insertDepartment(Departments department) {
        return rep.insertDepartment(department);
    }

    @Override
    public List<Departments> getDepartmentsListByCompanyId(int company_id, boolean has_parent) {
        return rep.getDepartmentsListByCompanyId(company_id,has_parent);
    }

    @Override
    public boolean deleteDepartmentsByNumber(ArrayList<Long> delNumbers) {
        return false;
    }
}

