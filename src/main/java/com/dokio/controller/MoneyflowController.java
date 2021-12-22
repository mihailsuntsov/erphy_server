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
