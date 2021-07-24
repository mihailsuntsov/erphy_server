package com.dokio.controller;

import com.dokio.message.request.Settings.SettingsDashboardForm;
import com.dokio.message.request.Settings.SettingsRetailSalesForm;
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
