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
