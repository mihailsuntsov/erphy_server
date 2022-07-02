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

import com.dokio.message.request.Settings.SettingsDashboardForm;
import com.dokio.message.response.Settings.SettingsDashboardJSON;
import com.dokio.repository.DashboardRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class DashboardController {

    Logger logger = Logger.getLogger("DashboardController");

    @Autowired
    DashboardRepository dashboardRepository;



    @SuppressWarnings("Duplicates")
    @RequestMapping(
            value = "/api/auth/getSettingsDashboard",
            method = RequestMethod.GET, produces = "application/json;charset=utf8")
    public ResponseEntity<?> getSettingsDashboard()
    {
        logger.info("Processing get request for path /api/auth/getSettingsDashboard without request parameters");
        SettingsDashboardJSON response;
        try {
            response=dashboardRepository.getSettingsDashboard();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка загрузки настроек для стартовой страницы", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/auth/saveSettingsDashboard")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> saveSettingsDashboard(@RequestBody SettingsDashboardForm request){
        logger.info("Processing post request for path /api/auth/saveSettingsDashboard: " + request.toString());

        if(dashboardRepository.saveSettingsDashboard(request)){
            return new ResponseEntity<>("[\n" + "    1\n" +  "]", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Ошибка сохранения настроек для стартовой страницы", HttpStatus.BAD_REQUEST);
        }
    }
}
