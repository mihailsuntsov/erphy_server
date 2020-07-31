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
package com.dokio.service.department;

import com.dokio.message.request.DepartmentForm;
import com.dokio.model.Departments;
import com.dokio.message.response.DepartmentsJSON;
import com.dokio.repository.DepartmentRepositoryJPA;
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

