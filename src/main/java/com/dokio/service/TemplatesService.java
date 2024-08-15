/*
        Dokio CRM - server part. Sales, finance and warehouse management system
        Copyright (C) Mikhail Suntsov /mihail.suntsov@gmail.com/

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>
*/
package com.dokio.service;

import com.dokio.message.response.CagentsJSON;
import com.dokio.message.response.CompaniesJSON;
import com.dokio.message.response.FileInfoJSON;
import com.dokio.message.response.Sprav.SpravCurrenciesJSON;
import com.dokio.repository.SecurityRepositoryJPA;
import com.dokio.repository.SpravCurrenciesRepository;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Repository
public class TemplatesService {

    Logger logger = Logger.getLogger("TemplatesService");

    private static final Set COUNTRIES_WITH_SURNAME_FIRST
            = Collections.unmodifiableSet((Set<? extends Integer>) Stream
            .of(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15)
            .collect(Collectors.toCollection(HashSet::new)));

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    SecurityRepositoryJPA securityRepositoryJPA;
    @Autowired
    UserRepositoryJPA userRepositoryJPA;
    @Autowired
    SpravCurrenciesRepository currenciesRepository;

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
        String result;
        // получим наименование организационно-правовой формы
        switch (company.getType()) {
            case ("individual"):// individual
                if(!Objects.isNull(company.getJr_country_id()) && COUNTRIES_WITH_SURNAME_FIRST.contains(company.getJr_country_id()))
                    result = company.getLegal_form()+" "+company.getJr_fio_family()+" "+company.getJr_fio_name()+" "+company.getJr_fio_otchestvo();
                else
                    result = company.getLegal_form()+" "+company.getJr_fio_name()+((company.getJr_fio_otchestvo()!=null&&!company.getJr_fio_otchestvo().equals(""))?(" "+company.getJr_fio_otchestvo()):"")+" "+company.getJr_fio_family();
                break;
            default:  // Entity
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

    @SuppressWarnings("Duplicates")
    public String getCagentFullName(CagentsJSON company){
        String result;
        switch (company.getType()) {
            case ("individual"):// individual
                if(!Objects.isNull(company.getJr_country_id()) && COUNTRIES_WITH_SURNAME_FIRST.contains(company.getJr_country_id()))
                    result = company.getLegal_form()+" "+((Objects.isNull(company.getJr_fio_family())||company.getJr_fio_family().equals(""))?company.getName():(company.getJr_fio_family()+" "+company.getJr_fio_name()+" "+company.getJr_fio_otchestvo()));
                else
                    result = company.getLegal_form()+" "+((Objects.isNull(company.getJr_fio_family())||company.getJr_fio_family().equals(""))?company.getName():(company.getJr_fio_name()+((company.getJr_fio_otchestvo()!=null&&!company.getJr_fio_otchestvo().equals(""))?(" "+company.getJr_fio_otchestvo()):"")+" "+company.getJr_fio_family()));
                break;
            default:  // Entity
                result = (Objects.isNull(company.getJr_jur_full_name())||company.getJr_jur_full_name().equals(""))?company.getName():company.getJr_jur_full_name();
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

    public BigDecimal getIfElse(Boolean condition, BigDecimal ifTrue, String ifFalse){
        if(Objects.isNull(condition))
            return new BigDecimal(ifFalse);
        else
            return(condition?ifTrue: new BigDecimal(ifFalse));
    }

    public String getIfElse_string(Boolean condition, BigDecimal ifTrue, String ifFalse){
        if(Objects.isNull(condition))
            return ifFalse;
        else
            return(condition?ifTrue.toString().replace(".",","):ifFalse);
    }

    public SpravCurrenciesJSON getCompanyCurrency(CompaniesJSON company){
        return currenciesRepository.getDefaultCompanyCurrency(company.getId());
    }

    // сумма прописью
    public String moneyAsString(Double sum){
        MoneyToStr moneyToStr = new MoneyToStr(MoneyToStr.Currency.RUR, MoneyToStr.Language.RUS, MoneyToStr.Pennies.NUMBER);
        return( moneyToStr.convert(sum));
    }

}
