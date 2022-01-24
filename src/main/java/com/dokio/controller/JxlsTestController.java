/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.controller;

import com.dokio.domain.Employee;
import com.dokio.message.request.CompaniesPaymentAccountsForm;
import com.dokio.message.response.CagentsJSON;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.InvoiceoutJSON;
import com.dokio.message.response.additional.InvoiceoutProductTableJSON;
import com.dokio.repository.CagentRepositoryJPA;
import com.dokio.repository.CompanyRepositoryJPA;
import com.dokio.repository.FileRepositoryJPA;
import com.dokio.repository.InvoiceoutRepositoryJPA;
import com.dokio.service.TemplatesService;
import com.github.moneytostr.MoneyToStr;
import org.apache.log4j.Logger;
import org.jxls.util.JxlsHelper;
import org.jxls.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.jxls.common.Context;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class JxlsTestController
{
    Logger logger = Logger.getLogger("JxlsTestController");

    @Autowired
    private TemplatesService tservice;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA company;
    @Autowired
    InvoiceoutRepositoryJPA invoiceoutRepository;

    @GetMapping("/api/auth/demo1/{filename:.+}")
    public void demo1 (HttpServletResponse response, @PathVariable String filename) throws Exception {

//        logger.info("Running Object Collection demo");
//        String root = JxlsTestController.class.getResource("/").getPath();
//        logger.info("Root of your project is - "+root);

        FileInfoJSON fileInfo = tservice.getFileInfo(filename);

        List<Employee> employees = generateSampleEmployeeData();
//        try(InputStream is = JxlsTestController.class.getResourceAsStream("/object_collection_template.xls")) {
        InputStream is = new FileInputStream(new File(fileInfo.getPath()+"/"+filename));
//        OutputStream os = new FileOutputStream("excel_file_output.xls");
        OutputStream os = response.getOutputStream();
        try {

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+"object_collection_output.xls");
                Context context = new Context();
                context.putVar("employees", employees);
                context.putVar("test", "Misha");
                JxlsHelper.getInstance().processTemplate(is, os, context);
        } catch (Exception e){
            logger.error("Exception in method demo1.", e);
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }
    private static List<Employee> generateSampleEmployeeData(){
        List<Employee> list = new ArrayList<>();

        list.add(new Employee("Test1", new Date(), new BigDecimal(1.23), new BigDecimal(4.13)));
        list.add(new Employee("Test2", new Date(), new BigDecimal(6.29), new BigDecimal(1.83)));
        list.add(new Employee("Test3", new Date(), new BigDecimal(3.18), new BigDecimal(3.66)));

        return list;
    }
}
