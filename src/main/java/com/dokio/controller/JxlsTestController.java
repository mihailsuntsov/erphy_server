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
    private TemplatesService templatesService;
    @Autowired
    FileRepositoryJPA fileRepository;
    @Autowired
    CagentRepositoryJPA cagentRepository;
    @Autowired
    CompanyRepositoryJPA companyRepository;
    @Autowired
    InvoiceoutRepositoryJPA invoiceoutRepository;




    @GetMapping("/api/auth/demo1/{filename:.+}")
    public void demo1 (HttpServletResponse response, @PathVariable String filename) throws Exception {

//        logger.info("Running Object Collection demo");
//        String root = JxlsTestController.class.getResource("/").getPath();
//        logger.info("Root of your project is - "+root);

        FileInfoJSON fileInfo = templatesService.getFileInfo(filename);

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


    @RequestMapping(
            value = "/api/auth/invoiceoutPrint",
            params = {"file_name", "tt_id", "doc_id"},
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public void invoiceoutPrint (HttpServletResponse response,
                                 @RequestParam("file_name") String filename,
                                 @RequestParam("doc_id") Long doc_id,
                                 @RequestParam("tt_id")int templateTypeId) throws Exception {

        FileInfoJSON fileInfo = templatesService.getFileInfo(filename);



        InputStream is = new FileInputStream(new File(fileInfo.getPath()+"/"+filename));
        OutputStream os = response.getOutputStream();
        try {


            InvoiceoutJSON doc = invoiceoutRepository.getInvoiceoutValuesById(doc_id);
            List<InvoiceoutProductTableJSON> product_table=invoiceoutRepository.getInvoiceoutProductTable(doc_id);
            CagentsJSON cagent = cagentRepository.getCagentValues(doc.getCagent_id());
            CompaniesJSON company = companyRepository.getCompanyValues(doc.getCompany_id());
            CompaniesPaymentAccountsForm myPaymentAccount = companyRepository.getMainPaymentAccountOfCompany(doc.getCompany_id());

            BigDecimal sumNds = new BigDecimal(0);
            BigDecimal totalSum = new BigDecimal(0);
            // в таблице товаров считаем сумму НДС и общую сумму стоимости товаров (или услуг)
            for(InvoiceoutProductTableJSON product:product_table){// бежим по товарам
                if(doc.isNds()){// если в документе включен переключатель НДС
                    BigDecimal nds_val = new BigDecimal(product.getNds_value());// величина НДС в процентах у текущего товара. Например, 20
                    // Включен переключатель "НДС включён" или нет - в любом случае НДС уже в цене product_sumprice. Нужно его вычленить из нее по формуле (для НДС=20%) "цену с НДС умножить на 20 и разделить на 120"
                    sumNds=sumNds.add(product.getProduct_sumprice().multiply(nds_val).divide(new BigDecimal(100).add(nds_val),2,BigDecimal.ROUND_HALF_UP));
                }
                totalSum=totalSum.add(product.getProduct_sumprice());
            }

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileInfo.getOriginal_name());
            Context context = new Context();

            context.putVar("myCompanyFullName", companyRepository.getMyCompanyFullName(company));
            context.putVar("myCompanyAddress", companyRepository.getMyCompanyAddress(company));
            context.putVar("myCompanyInn", company.getJr_inn());
            context.putVar("myCompanyShortName", company.getName());
            context.putVar("cagentShortName", cagent.getName());
            context.putVar("myKpp", company.getJr_jur_kpp());
            context.putVar("fioDirector", company.getFio_director());
            context.putVar("fioGlavbuh", company.getFio_glavbuh());
            context.putVar("paymentAccount", myPaymentAccount.getPayment_account());
            context.putVar("bik", myPaymentAccount.getBik());
            context.putVar("corrAccount", myPaymentAccount.getCorr_account());
            context.putVar("bankName", myPaymentAccount.getName());
            context.putVar("dateCreate", doc.getDate_time_created().substring(0,10));// т.к. дата в формате DD.MM.YYYY HH24:MI, а нужно DD.MM.YYYY
            context.putVar("docNumber", doc.getDoc_number().toString());
            context.putVar("productTable", product_table);// таблица с товарами
            context.putVar("sumNds", sumNds);

            // вставка печати и подписей

            if(!Objects.isNull(company.getStamp_id())){
                FileInfoJSON fileStampInfo = templatesService.getFileInfo(company.getStamp_id());
                InputStream stampIs = new FileInputStream(new File(fileStampInfo.getPath()+"/"+fileStampInfo.getName()));
                byte[] stamp = Util.toByteArray(stampIs);
                context.putVar("stamp", stamp);
                stampIs.close();}
            if(!Objects.isNull(company.getDirector_signature_id())){
                FileInfoJSON fileDirSignatInfo = templatesService.getFileInfo(company.getDirector_signature_id());
                InputStream dirSignIs = new FileInputStream(new File(fileDirSignatInfo.getPath()+"/"+fileDirSignatInfo.getName()));
                byte[] dirSignature = Util.toByteArray(dirSignIs);
                context.putVar("dirSignature", dirSignature);
                dirSignIs.close();}
            if(!Objects.isNull(company.getGlavbuh_signature_id())){
                FileInfoJSON fileGbSignatInfo = templatesService.getFileInfo(company.getGlavbuh_signature_id());
                InputStream gbSignIs = new FileInputStream(new File(fileGbSignatInfo.getPath()+"/"+fileGbSignatInfo.getName()));
                byte[] gbSignature = Util.toByteArray(gbSignIs);
                context.putVar("gbSignature", gbSignature);
                gbSignIs.close();}

            // сумма прописью
            MoneyToStr moneyToStr = new MoneyToStr(MoneyToStr.Currency.RUR, MoneyToStr.Language.RUS, MoneyToStr.Pennies.NUMBER);
            context.putVar("moneyAsString", moneyToStr.convert(totalSum.doubleValue()));

            JxlsHelper.getInstance().processTemplate(is, os, context);

        } catch (Exception e){
            logger.error("Exception in method invoiceoutPrint.", e);
            e.printStackTrace();
        } finally {
            is.close();
            os.close();

        }
    }





}
