package com.laniakea.service.department;

import com.laniakea.message.request.DepartmentForm;
import com.laniakea.model.Departments;
import com.laniakea.message.response.DepartmentsJSON;

import java.util.ArrayList;
import java.util.List;

public interface DepartmentService {

    List<Departments> getDepartmentsTable(int result, int offsetreal, String searchString, String sortColumn, String sortAsc, int companyId);

    public Departments getCompanyById(int id);

    public DepartmentsJSON getDepartmentValuesById(int id);

    public int getDepartmentsSize(String searchString, int companyId);

    public Long insertDepartment(Departments department);

    public List<Departments> getDeptChildrens(int parentDeptId);

    public boolean updateDepartment(DepartmentForm company);

    public boolean deleteDepartmentsByNumber(ArrayList<Long> delNumbers);

    public List<Departments> getDepartmentsListByCompanyId(int company_id, boolean has_parent);



}
