package com.dokio.controller;

import com.dokio.message.request.ProductCategoriesForm;
import com.dokio.message.request.additional.EmployeeSceduleForm;
import com.dokio.message.response.additional.eployeescdl.EmployeeScedule;
import com.dokio.message.response.additional.eployeescdl.SceduleDay;
import com.dokio.repository.EmployeeSceduleRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class EmployeeSceduleController {


    Logger logger = Logger.getLogger("EmployeeSceduleController");

    @Autowired
    EmployeeSceduleRepository employeeSceduleRepository;


    @PostMapping("/api/auth/getEmployeesWorkSchedule")
    public  ResponseEntity<?> getEmployeesWorkSchedule(@RequestBody EmployeeSceduleForm request) {
        logger.info("Processing post request for path /api/auth/getEmployeesWorkSchedule: " + request.toString());
        try {return new ResponseEntity<>(employeeSceduleRepository.getEmployeesWorkSchedule(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller getEmployeesWorkSchedule error", e);
            return new ResponseEntity<>("Request error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    @PostMapping("/api/auth/updateEmployeeWorkSchedule")
    public  ResponseEntity<?> updateEmployeeWorkSchedule(@RequestBody List<EmployeeScedule> request) {
        logger.info("Processing post request for path /api/auth/updateEmployeeWorkSchedule: " + request.toString());
        try {return new ResponseEntity<>(employeeSceduleRepository.updateEmployeeWorkSchedule(request), HttpStatus.OK);}
        catch (Exception e){e.printStackTrace();logger.error("Controller updateEmployeeWorkSchedule error", e);
            return new ResponseEntity<>("Request error", HttpStatus.INTERNAL_SERVER_ERROR);}
    }



}
