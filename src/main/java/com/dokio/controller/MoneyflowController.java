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

package com.dokio.controller;

import com.dokio.message.request.Reports.HistoryCagentDocsSearchForm;
import com.dokio.repository.Reports.MoneyflowRepositoryJPA;
import com.dokio.util.CommonUtilites;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;

@Controller
public class MoneyflowController {
    Logger logger = Logger.getLogger("MoneyflowController");

    @Autowired
    MoneyflowRepositoryJPA moneyflowRepository;
    @Autowired
    CommonUtilites commonUtilites;

    @PostMapping("/api/auth/getMoneyflowTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMoneyflowTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMoneyflowTable: " + searchRequest.toString());
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "date_created";
            sortAsc = "asc";
        }
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int offsetreal = offset * result;//создана переменная с номером страницы
        return new ResponseEntity<List>(moneyflowRepository.getMoneyflowTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getDateFrom(), searchRequest.getDateTo()), HttpStatus.OK);
    }

    @PostMapping("/api/auth/getMoneyflowPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMoneyflowPagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMoneyflowPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = moneyflowRepository.getMoneyflowSize(searchString, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds(),searchRequest.getDateFrom(), searchRequest.getDateTo());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }

    @PostMapping("/api/auth/getMoneyflowBalances")
    public ResponseEntity<?> getMoneyflowBalances(@RequestBody HistoryCagentDocsSearchForm searchRequest){
        logger.info("Processing get request for path /api/auth/getMoneyflowBalances with parameters: " + searchRequest.toString());
        try {return new ResponseEntity<>(moneyflowRepository.getMoneyflowBalances(searchRequest.getCompanyId(), searchRequest.getDateFrom(), searchRequest.getDateTo()), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();return new ResponseEntity<>("Ошибка загрузки крайних значений балансов", HttpStatus.INTERNAL_SERVER_ERROR);}
    }
    @PostMapping("/api/auth/getMoneyflowDetailedTable")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMoneyflowDetailedTable(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMoneyflowDetailedTable: " + searchRequest.toString());
        String searchString = searchRequest.getSearchString();
        String sortColumn = searchRequest.getSortColumn();
        String sortAsc;
        if (searchRequest.getSortColumn() != null && !searchRequest.getSortColumn().isEmpty() && searchRequest.getSortColumn().trim().length() > 0) {
            sortAsc = searchRequest.getSortAsc();// если SortColumn определена, значит и sortAsc есть.
        } else {
            sortColumn = "date_time_created_sort";
            sortAsc = "asc";
        }
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int offsetreal = offset * result;//создана переменная с номером страницы
        return new ResponseEntity<List>(moneyflowRepository.getMoneyflowDetailedTable(result, offsetreal, searchString, sortColumn, sortAsc, searchRequest.getCompanyId(),searchRequest.getDateFrom(), searchRequest.getDateTo()), HttpStatus.OK);
    }
    @PostMapping("/api/auth/getMoneyflowDetailedPagesList")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> getMoneyflowDetailedPagesList(@RequestBody HistoryCagentDocsSearchForm searchRequest) {
        logger.info("Processing post request for path /api/auth/getMoneyflowDetailedPagesList: " + searchRequest.toString());
        int offset = (Objects.isNull(searchRequest.getOffset())?0:searchRequest.getOffset()); // номер страницы. Изначально это null
        int result = (Objects.isNull(searchRequest.getResult())?10:searchRequest.getResult()); // количество записей, отображаемых на странице (по умолчанию 10)
        String searchString = searchRequest.getSearchString();
        int size = moneyflowRepository.getMoneyflowDetailedSize(searchString, searchRequest.getCompanyId(),searchRequest.getFilterOptionsIds(),searchRequest.getDateFrom(), searchRequest.getDateTo());//  - общее количество записей выборки
        return new ResponseEntity<List>(commonUtilites.getPagesList(offset + 1, size, result), HttpStatus.OK);
    }
}
