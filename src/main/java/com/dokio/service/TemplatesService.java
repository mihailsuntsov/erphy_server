package com.dokio.service;

import com.dokio.message.response.CompaniesJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.UserRepositoryJPA;
import com.github.moneytostr.MoneyToStr;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


@Service
@Repository
public class TemplatesService {

    Logger logger = Logger.getLogger("TemplatesService");

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;


    @SuppressWarnings("Duplicates")//отдача данных (original_name, path) о файле по его имени на диске
    public FileInfoJSON getFileInfo(String filename) {
            Long myMasterId = userRepositoryJPA.getMyMasterId();
            String stringQuery;
            stringQuery = "select " +
                    "           p.original_name as original_name, " +
                    "           p.path as path, " +
                    "           p.name as name " +
                    "           from files p " +
                    "           where p.master_id = " + myMasterId + " and p.name= :filename";
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("filename", filename);
            List<Object[]> queryList = query.getResultList();
            if(queryList.size()>0) {
                FileInfoJSON doc = new FileInfoJSON();
                doc.setOriginal_name((String) queryList.get(0)[0]);
                doc.setPath((String) queryList.get(0)[1]);
                doc.setName((String) queryList.get(0)[2]);
                return doc;
            }
            else {
                logger.error("File " + filename + " not found in database.");
                return new FileInfoJSON();
            }
        } catch (Exception e) {
            logger.error("Exception in method getFileName. SQL: " + stringQuery);
            e.printStackTrace();
            return null;
        }

    }
    @SuppressWarnings("Duplicates")//отдача данных (original_name, path) о файле по его ID
    public FileInfoJSON getFileInfo(Long fileId) {
        Long myMasterId = userRepositoryJPA.getMyMasterId();
        String stringQuery;
        stringQuery = "select " +
                "           p.original_name as original_name, " +
                "           p.path as path, " +
                "           p.name as name " +
                "           from files p " +
                "           where p.master_id = " + myMasterId + " and p.id="+fileId;
        try {
            Query query = entityManager.createNativeQuery(stringQuery);
            List<Object[]> queryList = query.getResultList();
            if(queryList.size()>0) {
                FileInfoJSON doc = new FileInfoJSON();
                doc.setOriginal_name((String) queryList.get(0)[0]);
                doc.setPath((String) queryList.get(0)[1]);
                doc.setName((String) queryList.get(0)[2]);
                return doc;
            }
            else {
                logger.error("File with id = " + fileId + " not found in database.");
                return new FileInfoJSON();
            }
        } catch (Exception e) {
            logger.error("Exception in method getFileName. SQL: " + stringQuery);
            e.printStackTrace();
            return null;
        }

    }


    @SuppressWarnings("Duplicates")
    public String getMyCompanyFullName(CompaniesJSON company){
        String result = "";
        // получим наименование организационно-правовой формы
        switch (company.getOpf_id()) {
            case (1):// Индивидуальный предприниматель
                result = company.getOpf() +" "+ company.getJr_fio_family()+" "+company.getJr_fio_name()+" "+company.getJr_fio_otchestvo();
                break;
            case (2): // Самозанятый
                result = company.getJr_fio_family()+" "+company.getJr_fio_name()+" "+company.getJr_fio_otchestvo();
                break;
            default:  // Все юрлица ( ООО, ЗАО и т.д.)
                result = company.getJr_jur_full_name();
                break;
        }
        return result;
    }

    @SuppressWarnings("Duplicates")
    public String getMyCompanyAddress(CompaniesJSON company){
        String result;
        // получим адрес предприятия
        switch (company.getOpf_id()) {
            case (1):// Индивидуальный предприниматель
                result = company.getZip_code()+" "+company.getRegion()+", "+company.getArea()+", "+company.getCity()+" "+company.getStreet()+" д."+company.getHome()+(!Objects.isNull(company.getFlat())?(" кв."+company.getFlat()):"");
                break;
            case (2): // Самозанятый
                result = company.getZip_code()+" "+company.getRegion()+", "+company.getArea()+", "+company.getCity()+" "+company.getStreet()+" д."+company.getHome()+(!Objects.isNull(company.getFlat())?(" кв."+company.getFlat()):"");
                break;
            default:  // Все юрлица ( ООО, ЗАО и т.д.)
                result = company.getZip_code()+" "+company.getJr_region()+", "+company.getJr_area()+", "+company.getJr_city()+" "+company.getJr_street()+" "+company.getJr_home()+(!Objects.isNull(company.getJr_flat())?(" "+company.getJr_flat()):"");
                break;
        }
        return result;
    }

    public String getIfElse(Boolean condition, String ifTrue, String ifFalse){
        if(Objects.isNull(condition))
            return ifFalse;
        else
            return(condition?ifTrue:ifFalse);
    }

    public String getIfElse(Boolean condition, BigDecimal ifTrue, String ifFalse){
        if(Objects.isNull(condition))
            return ifFalse;
        else
            return(condition?ifTrue.toString():ifFalse);
    }

    // сумма прописью
    public String moneyAsString(Double sum){
        MoneyToStr moneyToStr = new MoneyToStr(MoneyToStr.Currency.RUR, MoneyToStr.Language.RUS, MoneyToStr.Pennies.NUMBER);
        return( moneyToStr.convert(sum));
    }

}
